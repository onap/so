/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

/**
 * Class for preparing CDS call.
 */
@Component
public class GenericRequestObjectCreater implements JavaDelegate {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PnfManagement pnfManagement;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    private static final String ORIGINATOR_ID = "SO";
    private static final int ERROR_CODE = 7010;

    private static final Map<String, Object> PAIR_MAP;
    private static final String CONFIG_ASSIGN = "config-assign";
    private static final String CONFIG_DEPLOY = "config-deploy";
    private static final String SW_DOWNLOAD = "sw-download";
    private static final String SW_ACTIVATE = "sw-activate";

    static {
        Yaml myYaml = new Yaml();
        InputStream inputStream = GenericRequestObjectCreater.class.getClassLoader().getResourceAsStream("mapper.yaml");
        PAIR_MAP = myYaml.load(inputStream);
    }

    protected Action action;

    public enum Mode {
        SYNC("sync"), ASYNC("async");

        private Mode(String type) {
            this.type = type;
        }

        private final String type;

        public String getType() {
            return type;
        }
    }

    // added this just for future implementations
    public enum Scope {
        VNF("vnf"), PNF("pnf");

        private Scope(String type) {
            this.type = type;
        }

        private final String type;

        public String getType() {
            return type;
        }
    }

    public enum Action {
        ASSIGN("assign", CONFIG_ASSIGN, "assign.yaml", Mode.SYNC),
        DEPLOY("deploy", CONFIG_DEPLOY, "deploy.yaml", Mode.ASYNC),
        DOWNLOAD("download", SW_DOWNLOAD, "download.yaml", Mode.ASYNC),
        ACTIVATE("activate", SW_ACTIVATE, "activate.yaml", Mode.ASYNC);

        private Action(String name, String type, String yaml, Mode mode) {
            this.name = name;
            this.type = type;
            this.yaml = yaml;
            this.mode = mode;
        }

        private final String name;
        private String yaml;
        private String type;
        private Mode mode;

        public String getYaml() {
            return yaml;
        }

        public String getName() {
            return name;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public void setYaml(String yaml) {
            this.yaml = yaml;
        }

        public String getType() {
            return type;
        }
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {

        logger.debug("Running execute block for activity:{}", delegateExecution.getCurrentActivityId());
        Action action = extractAction(delegateExecution);
        AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        cdsPropertiesBean.setBlueprintName((String) delegateExecution.getVariable(PRC_BLUEPRINT_NAME));
        cdsPropertiesBean.setBlueprintVersion((String) delegateExecution.getVariable(PRC_BLUEPRINT_VERSION));
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setActionName(action.getType());
        cdsPropertiesBean.setMode(action.getMode().getType());
        cdsPropertiesBean.setRequestId((String) delegateExecution.getVariable(MSO_REQUEST_ID));
        cdsPropertiesBean.setSubRequestId((String) delegateExecution.getVariable(PNF_UUID));
        cdsPropertiesBean.setRequestObject(getdynamicRequestObject(delegateExecution, action));
        delegateExecution.setVariable(EXECUTION_OBJECT, cdsPropertiesBean);
    }


    public String getdynamicRequestObject(final DelegateExecution delegateExecution, Action action) {

        Yaml myYaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(action.getYaml());
        Map<String, Object> flatRequestObj = myYaml.load(inputStream);

        fillValues(flatRequestObj, delegateExecution);

        JSONObject json = new JSONObject(flatRequestObj);

        return json.toString();
    }

    private void fillValues(Map<String, Object> configMap, final DelegateExecution delegateExecution) {
        final String AAI_PNF = "aai-pnf";
        String key = "";
        Pnf pnf = null;// TODO---for caching this object put this into delegateexecution and retrieve anytime in
                       // workflow.
        Map<String, String> aaiPnfMap = (Map<String, String>) PAIR_MAP.get(AAI_PNF);
        for (Map.Entry entry : configMap.entrySet()) {
            key = (String) entry.getKey();
            if (entry.getValue() == null) {
                if (PAIR_MAP.containsKey(key)) {
                    entry.setValue((String) delegateExecution.getVariable((String) PAIR_MAP.get(key)));
                } else if (aaiPnfMap.containsKey(key)) {
                    if (pnf == null) {// TODO---for caching this object put this into delegateexecution and retrieve
                                      // anytime in workflow.
                        pnf = getPnf(delegateExecution);
                    }
                    try {
                        java.lang.reflect.Method method = pnf.getClass().getMethod("get" + aaiPnfMap.get(key));
                        entry.setValue((String) method.invoke(pnf));
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                        exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                                "Unable to fetch from AAI" + e.getMessage());
                    }
                }
            } else {
                fillValues((Map<String, Object>) entry.getValue(), delegateExecution);
            }
        }
    }

    private Action extractAction(final DelegateExecution delegateExecution) {
        String actionName = (String) delegateExecution.getVariable("actionName");
        switch (actionName) {
            case CONFIG_ASSIGN:
                return Action.ASSIGN;
            case CONFIG_DEPLOY:
                return Action.DEPLOY;
            case "download":
                return Action.DOWNLOAD;
            case "activate":
                return Action.ACTIVATE;
            default:
                return action;
        }
    }

    private Pnf getPnf(DelegateExecution delegateExecution) {
        try {
            String pnfName = (String) delegateExecution.getVariable(PNF_CORRELATION_ID);
            Optional<Pnf> pnfOptional = pnfManagement.getEntryFor(pnfName);
            if (pnfOptional.isPresent()) {
                return pnfOptional.get();
            }
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "AAI entry for PNF: " + pnfName + " does not exist");

        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to fetch from AAI" + e.getMessage());
        }
        return null;
    }

}
