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

    private static final String SCOPE_PNF = "pnf";
    private static final String SCOPE_VNF = "vnf";
    private static final String SCOPE_VF = "vf-module";

    private static final String MODE_SYNC = "sync";
    private static final String MODE_ASYNC = "async";

    static {
        Yaml myYaml = new Yaml();
        InputStream inputStream = GenericRequestObjectCreater.class.getClassLoader().getResourceAsStream("mapper.yaml");
        PAIR_MAP = myYaml.load(inputStream);
    }

    protected Action action;

    protected Scope scope;

    public enum Mode {
        SYNC(MODE_SYNC), ASYNC(MODE_ASYNC);

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // added this just for future implementations
    public enum Scope {
        VNF(SCOPE_VNF), PNF(SCOPE_PNF), VF(SCOPE_VF);

        private final String name;

        private Scope(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum Action {
        ASSIGN(CONFIG_ASSIGN, Mode.SYNC),
        DEPLOY(CONFIG_DEPLOY, Mode.ASYNC),
        DOWNLOAD(SW_DOWNLOAD, Mode.ASYNC),
        ACTIVATE(SW_ACTIVATE, Mode.ASYNC);

        private final String name;
        private String yamlFile; // this yaml file name is complete when scope name is prepend with it e.g.
                                 // pnf/sw-download.yaml or vnf/sw-download.yaml
        private Mode mode;

        private Action(String name, Mode mode) {
            this.name = name;
            this.yamlFile = name + ".yaml";
            this.mode = mode;
        }

        public String getYamlFile() {
            return yamlFile;
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
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {

        logger.debug("Running execute block for activity:{}", delegateExecution.getCurrentActivityId());
        Action action = extractAction(delegateExecution);
        Scope scope = extractScope(delegateExecution);
        AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        cdsPropertiesBean.setBlueprintName((String) delegateExecution.getVariable(PRC_BLUEPRINT_NAME));
        cdsPropertiesBean.setBlueprintVersion((String) delegateExecution.getVariable(PRC_BLUEPRINT_VERSION));
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setActionName(action.getName());
        cdsPropertiesBean.setMode(action.getMode().getName());
        cdsPropertiesBean.setRequestId((String) delegateExecution.getVariable(MSO_REQUEST_ID));
        cdsPropertiesBean.setSubRequestId((String) delegateExecution.getVariable(PNF_UUID));
        cdsPropertiesBean.setRequestObject(getdynamicRequestObject(delegateExecution, action, scope));
        delegateExecution.setVariable(EXECUTION_OBJECT, cdsPropertiesBean);
    }


    public String getdynamicRequestObject(final DelegateExecution delegateExecution, Action action, Scope scope) {

        Yaml myYaml = new Yaml();
        InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(getYamlFileNameUsingScopeAndAction(action, scope));
        Map<String, Object> flatRequestObj = myYaml.load(inputStream);

        fillValues(flatRequestObj, delegateExecution);

        JSONObject json = new JSONObject(flatRequestObj);

        return json.toString();
    }

    private String getYamlFileNameUsingScopeAndAction(Action action, Scope scope) {
        return scope.getName() + "/" + action.getYamlFile();
    }

    /**
     * This method fills the configMap by using mapper.yaml and {scope}-{actionName}.yaml.
     **/
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

    /**
     * This method extracts action from execution object, if no action is found, it will throw a (@link
     * WorflowException).
     **/
    private Action extractAction(final DelegateExecution delegateExecution) {
        String actionName = (String) delegateExecution.getVariable("action");
        if (actionName == null) {
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, "action does not exist");
        }
        switch (actionName) {
            case CONFIG_ASSIGN: {
                action = Action.ASSIGN;
                break;
            }
            case CONFIG_DEPLOY: {
                action = Action.DEPLOY;
                break;
            }
            case SW_DOWNLOAD: {
                action = Action.DOWNLOAD;
                break;
            }
            case SW_ACTIVATE: {
                action = Action.ACTIVATE;
                break;
            }
            default:
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, "action does not exist");
        }
        return action;
    }

    /**
     * This method extracts scope from execution object, if no scope is found default scope set to (@link Scope.PNF).
     **/
    private Scope extractScope(final DelegateExecution delegateExecution) {
        String scopeName = (String) delegateExecution.getVariable("scope");
        if (scopeName == null) {
            return Scope.PNF;
        }
        switch (scopeName) {
            case SCOPE_VNF: {
                scope = Scope.VNF;
                break;
            }
            case SCOPE_VF: {
                scope = Scope.VF;
                break;
            }
            case SCOPE_PNF:
            default: {
                scope = Scope.PNF;
                break;
            }
        }
        return scope;
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
