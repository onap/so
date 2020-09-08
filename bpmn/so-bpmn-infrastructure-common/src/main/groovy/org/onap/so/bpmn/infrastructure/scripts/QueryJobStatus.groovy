/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.onap.so.bpmn.infrastructure.scripts

import groovy.json.JsonSlurper
import org.json.JSONObject
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class QueryJobStatus extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger(QueryJobStatus.class)

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    
    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        
        try{
            String requestId = execution.getVariable("msoRequestId")
            logger.debug("RequestId :" + requestId)
            String responseId = execution.getVariable("responseId")
            String jobId = execution.getVariable("jobId")   
            def jsonSlurper = new JsonSlurper()
            
            HashMap<String,?> esrInfo=jsonSlurper.parseText(execution.getVariable("esrInfo"))
            logger.debug("esrInfo" + esrInfo.toString())
            
            HashMap<String,?> serviceInfo=jsonSlurper.parseText(execution.getVariable("serviceInfo"))
            logger.debug("serviceInfo" + serviceInfo.toString())
            
            execution.setVariable("esrInfo", esrInfo)
            execution.setVariable("serviceInfo", serviceInfo)
            
            String url = "http://so-nssmf-adapter.onap:8088/api/rest/provMns/v1/NSS/jobs/" + jobId
            execution.setVariable("NSSMF_AdapterEndpoint", url)
            
            String payload = """
                {
                  "responseId": "${responseId}",
                  "esrInfo":  ${execution.getVariable("esrInfo") as JSONObject},
                  "serviceInfo": ${execution.getVariable("serviceInfo") as JSONObject}
                }
              """
               
            execution.setVariable("NSSMF_AdapterRequest", payload.replaceAll("\\s+", ""))   
            execution.setVariable("startTime", System.currentTimeMillis())
            logger.debug("Outgoing NSSMF_AdapterRequest: \n" + payload)
        }catch(Exception e){
            String msg = "Exception in QueryJobStatus.preProcessRequest " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("exit preProcessRequest")
    }

    public void checkJobStatus(DelegateExecution execution) {
        logger.debug(" *** checkJobStatus *** ")
        def NSSMF_ResponseCode = execution.getVariable("NSSMF_ResponseCode") as Integer
        logger.debug("NSSMF_ResponseCode:" + NSSMF_ResponseCode)
        def NSSMF_Response = execution.getVariable("NSSMF_Response") as String
        def status = jsonUtil.getJsonValue(NSSMF_Response, "responseDescriptor.status")
        logger.debug("NSSMF_Response" + NSSMF_Response)
        
        Long startTime = execution.getVariable("startTime") as Long
        Long timeout = execution.getVariable("timeout") as Long
        timeout = timeout == null ? 600000 : timeout * 60000
        
        if(NSSMF_Response != null) {                            
            if (status.equalsIgnoreCase("processing") && (System.currentTimeMillis() - startTime) > timeout) {
                handleTimeOut(execution)
            } 
            else if(status.equalsIgnoreCase("finished") || status.equalsIgnoreCase("failed")) {
                execution.setVariable("JobStatusCompleted", "TRUE")
            } else {
                execution.setVariable("JobStatusCompleted", "FALSE")
            }
            } else {
                 Map<String, ?> responseDescriptorMap = new HashMap<>()
                 responseDescriptorMap.put("status","failed")
                 responseDescriptorMap.put("statusDescription","Exception while querying job status")
                 String responseDescriptor = """
                 {
                   "responseDescriptor": "${responseDescriptorMap}",
                 }
               """
                 execution.setVariable("JobStatusCompleted", "TRUE")
                 execution.setVariable("NSSMF_Response",responseDescriptor.replaceAll("\\s+", ""))
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad Response from NSSMF.")
            }
        logger.debug("exit checkJobStatus")
    }
    
    private handleTimeOut(DelegateExecution execution) {
        Map<String, ?> responseDescriptorMap = new HashMap<>()
        responseDescriptorMap.put("status","failed")
        responseDescriptorMap.put("statusDescription","timeout")
        String responseDescriptor = """
                {
                  "responseDescriptor": "${responseDescriptorMap}",
                }
              """
        execution.setVariable("JobStatusCompleted", "TRUE")
        execution.setVariable("NSSMF_Response",responseDescriptor.replaceAll("\\s+", ""))
    }
    
    public void updateJobStatusDetails(DelegateExecution execution)
    {
        logger.debug("**updateJobStatusDetails**")
        def NSSMF_Response = execution.getVariable("NSSMF_Response") as String
        def responseDescriptor = jsonUtil.getJsonValue(NSSMF_Response, "responseDescriptor")
        execution.setVariable("responseDescriptor",responseDescriptor)
        logger.debug("**exit updateJobStatusDetails")
    }
}
