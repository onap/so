/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2019 Nokia.
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

import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Optional;

@Component
public class InformDmaapClient implements JavaDelegate {

    private DmaapClient dmaapClient;

    @Override
    public void execute(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(ExecutionVariableNames.PNF_CORRELATION_ID);
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        String processBusinessKey = execution.getProcessBusinessKey();
        dmaapClient.registerForUpdate(pnfCorrelationId,
                () -> runtimeService.createMessageCorrelation("WorkflowMessage")
                        .processInstanceBusinessKey(processBusinessKey).correlateWithResult(),
                createUpdateInfoMap(execution));
    }

    private Map<String, String> createUpdateInfoMap(DelegateExecution execution) {
        Map<String, String> updateInfoMap = new HashMap();
        updateInfoMap.put("pnfCorrelationId",
                (String) execution.getVariable(ExecutionVariableNames.PNF_CORRELATION_ID));
        getResourceInput(execution).ifPresent(resourceInput -> {
            updateInfoMap.put("globalSubscriberID", resourceInput.getGlobalSubscriberId());
            updateInfoMap.put("serviceType", resourceInput.getServiceType());
            updateInfoMap.put("serviceInstanceId", resourceInput.getServiceInstanceId());
        });
        return updateInfoMap;
    }

    private Optional<ResourceInput> getResourceInput(DelegateExecution execution) {
        ResourceInput resourceInput = null;
        if (execution.getVariable("resourceInput") != null) {
            resourceInput = ResourceRequestBuilder.getJsonObject((String) execution.getVariable("resourceInput"),
                    ResourceInput.class);
        }
        return Optional.ofNullable(resourceInput);
    }

    @Autowired
    public void setDmaapClient(DmaapClient dmaapClient) {
        this.dmaapClient = dmaapClient;
    }
}
