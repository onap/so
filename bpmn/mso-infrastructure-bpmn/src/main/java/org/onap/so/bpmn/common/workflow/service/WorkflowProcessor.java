/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2026 Deutsche telekom
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

import java.sql.Timestamp;
import java.util.*;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkflowProcessor extends ProcessEngineAwareService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);
    protected static final String logMarker = "[WRKFLOW-RESOURCE]";
    public volatile String soProcessInstanceId = "";
    public volatile List<String> listOfProcessInstanceId = Collections.synchronizedList(new ArrayList<>());
    public volatile String requestId = "";
    public volatile List<String> listOfRequestId = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private RequestsDbClient requestDbclient;

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

            soProcessInstanceId = processInstanceId;
            logger.info("Saved soProcessInstanceId: {}", soProcessInstanceId);
            listOfProcessInstanceId.add(soProcessInstanceId);
            logger.info("Saved listOfProcessInstanceId: {}", listOfProcessInstanceId);
            requestId = businessKey;
            listOfRequestId.add(requestId);
            logger.info("Saved listOfRequestId: {}", listOfRequestId);

            logger.debug(logMarker + "Process " + processKey + ":" + processInstanceId + " "
                    + (processInstance.isEnded() ? "ENDED" : "RUNNING"));
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

    /*
     * This method will be used for pausing of SO(Service Instantiation) processKey is using only for logging in caller
     * method we are taking reference of runtime service from ProcessEngine and process instance id used for suspending
     * the workflow process.
     */
    public void pauseProcess(String processKey) {
        try {
            logger.debug("***Reached pauseProcess for listOfProcessInstanceId: {}", listOfProcessInstanceId);
            RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();

            if (listOfProcessInstanceId.size() > 0) {
                for (String processInsId : listOfProcessInstanceId) {
                    logger.debug("so Process InstanceId: {}", processInsId);
                    ProcessInstance processInstance =
                            runtimeService.createProcessInstanceQuery().processInstanceId(processInsId).singleResult();
                    if (processInstance != null) {
                        runtimeService.suspendProcessInstanceById(processInsId);
                    }
                    logger.debug("processInstance: {} and processInsId: {}", processInstance, processInsId);
                }
            } else {
                throw new RuntimeException();
            }
            logger.debug("***Completed pauseProcess for ProcessKey: {}", processKey);
        } catch (RuntimeException r) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Process instance id is not saved yet please try again");
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        } catch (Exception e) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Pause could not be perform due to some technical issue:");
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        }
    }

    /*
     * This method will be used for aborting of SO(Service Instantiation) processKey is using only for logging in caller
     * method we are taking reference of runtime service from ProcessEngine and process instance id used for terminating
     * the workflow process.
     */
    public void abortProcess(String processKey) {
        try {
            logger.debug("***Reached abortProcess for listOfProcessInstanceId: {}", listOfProcessInstanceId);
            RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();
            HistoryService historyService = getProcessEngineServices().getHistoryService();

            if (listOfProcessInstanceId.size() > 0) {
                for (String parentProcessInsId : listOfProcessInstanceId) {
                    logger.debug("so Process InstanceId: {}", parentProcessInsId);
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(parentProcessInsId).singleResult();
                    logger.debug("processInstance: {} and processInsId: {}", processInstance, parentProcessInsId);

                    if (processInstance != null) {
                        runtimeService.deleteProcessInstance(parentProcessInsId, "User aborted the process", true,
                                true);
                    }
                    ProcessInstance parentProcessInstance = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(parentProcessInsId).singleResult();
                    logger.debug("parentProcessInstance: {} and processInsId: {}", parentProcessInstance,
                            parentProcessInsId);
                    // deleting data for super process instance based on parent process instance id
                    if (parentProcessInstance == null) {
                        historyService.deleteHistoricProcessInstance(parentProcessInsId);
                    }
                }
            } else {
                throw new RuntimeException();
            }
            // deleting data for sub process instance based on sub process instance id
            for (String businessKey : listOfRequestId) {
                List<HistoricProcessInstance> historicInstances = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceBusinessKey(businessKey).list();
                for (HistoricProcessInstance instance : historicInstances) {
                    logger.debug("Subprocess insId:{} and businessKey: {}", instance.getId(), businessKey);
                    historyService.deleteHistoricProcessInstance(instance.getId());
                }
                InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(businessKey);
                request.setRequestStatus("ABORTED");
                request.setStatusMessage("Service Instantiation is Aborted from this : ");
                request.setLastModifiedBy("CamundaBPMN");
                request.setEndTime(new Timestamp(System.currentTimeMillis()));
                requestDbclient.updateInfraActiveRequests(request);
            }
            logger.debug("***Completed abortProcess for ProcessKey: {}", processKey);
        } catch (RuntimeException r) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Process instance id is not saved yet please try again");
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        } catch (Exception e) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Abort could not be perform due to some technical issue:");
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        }
    }

    /*
     * This method will be used for activating of SO(Service Instantiation) from where from there it was left processKey
     * is using only for logging in caller method we are taking reference of runtime service from ProcessEngine and
     * process instance id used for resuming the workflow process.
     */
    public void activateProcess(String processKey, String processInstanceId) {
        try {
            logger.debug("***Reached activateProcess for ProcessInstanceId: {}", processInstanceId);

            RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();
            runtimeService.activateProcessInstanceById(processInstanceId);
            logger.debug("***Completed activateProcess for ProcessInstanceId: {}", processInstanceId);

            ProcessInstance processInstance =
                    runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            logger.debug(logMarker + "Process key: " + processKey + " ProcessInstanceId:" + processInstanceId + " "
                    + (processInstance.isEnded() ? "ENDED" : "RUNNING"));

        } catch (Exception e) {
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse("Error occurred while executing the process: " + e);
            workflowResponse.setMessageCode(500);
            workflowResponse.setMessage("Fail");
            throw new WorkflowProcessorException(workflowResponse);
        }
    }
}
