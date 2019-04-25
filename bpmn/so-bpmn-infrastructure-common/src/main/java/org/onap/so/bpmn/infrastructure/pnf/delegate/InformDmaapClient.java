/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.onap.aai.domain.yang.v13.Metadatum;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Optional;

@Component
public class InformDmaapClient implements JavaDelegate {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private DmaapClient dmaapClient;

    @Override
    public void execute(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(ExecutionVariableNames.PNF_CORRELATION_ID);
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        String processBusinessKey = execution.getProcessBusinessKey();
        HashMap<String, String> updateInfo = createUpdateInfo(execution);
        updateInfo.put("pnfCorrelationId", pnfCorrelationId);
        dmaapClient
                .registerForUpdate(pnfCorrelationId,
                        () -> runtimeService.createMessageCorrelation("WorkflowMessage")
                                .processInstanceBusinessKey(processBusinessKey).correlateWithResult(),
                        Optional.of(updateInfo));
    }

    private HashMap<String, String> createUpdateInfo(DelegateExecution execution) {
        HashMap<String, String> map = new HashMap();

        ResourceInput resourceInputObj = ResourceRequestBuilder
                .getJsonObject((String) execution.getVariable("resourceInput"), ResourceInput.class);
        map.put("globalSubscriberID", resourceInputObj.getGlobalSubscriberId());
        map.put("serviceType", resourceInputObj.getServiceType());
        map.put("serviceInstanceId", resourceInputObj.getServiceInstanceId());
        return map;
    }

    @Autowired
    public void setDmaapClient(DmaapClient dmaapClient) {
        this.dmaapClient = dmaapClient;
    }
}
