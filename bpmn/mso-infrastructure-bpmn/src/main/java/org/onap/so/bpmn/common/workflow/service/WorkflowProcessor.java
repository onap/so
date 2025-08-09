/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkflowProcessor extends ProcessEngineAwareService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);
    protected static final String logMarker = "[WRKFLOW-RESOURCE]";

    @Async
    public void startProcess(String processKey, VariableMapImpl variableMap) {

        Map<String, Object> inputVariables;
        String processInstanceId = null;
        try {
            inputVariables = getInputVariables(variableMap);
            // This variable indicates that the flow was invoked asynchronously
            inputVariables.put("isAsyncProcess", "true");

            // Note: this creates a random businessKey if it wasn't specified.
            String businessKey = getBusinessKey(inputVariables);

            logger.debug("***Received MSO startProcessInstanceByKey with processKey: {} and variables: {}", processKey,
                    inputVariables);

            RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();
            ProcessInstance processInstance =
                    runtimeService.startProcessInstanceByKey(processKey, businessKey, inputVariables);
            processInstanceId = processInstance.getId();

            logger.debug("{}Process {}:{} {}", logMarker, processKey, processInstanceId,
                    (processInstance.isEnded() ? "ENDED" : "RUNNING"));
        } catch (Exception e) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Error occurred while executing the process: " + e);
            workflowResponse.setProcessInstanceID(processInstanceId);
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        }
    }

    protected static String getBusinessKey(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-request-id");
    }


    protected static Map<String, Object> getInputVariables(VariableMapImpl variableMap) {
        Map<String, Object> inputVariables = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> vMap = (Map<String, Object>) variableMap.get("variables");
        if (vMap != null) {
            for (Map.Entry<String, Object> entry : vMap.entrySet()) {
                String vName = entry.getKey();
                Object value = entry.getValue();
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value; // value, type
                inputVariables.put(vName, valueMap.get("value"));
            }
        }
        return inputVariables;
    }

    protected static String getOrCreate(Map<String, Object> inputVariables, String key) {
        String value = Objects.toString(inputVariables.get(key), null);
        if (value == null) {
            value = UUID.randomUUID().toString();
            inputVariables.put(key, value);
        }
        return value;
    }

}
