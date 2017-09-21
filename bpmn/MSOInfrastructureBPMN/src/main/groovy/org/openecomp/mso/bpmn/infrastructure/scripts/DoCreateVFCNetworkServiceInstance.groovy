/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil 
import org.openecomp.mso.bpmn.common.scripts.VidUtils 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils 
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>CreateGenericE2EServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Create
 */
public class CreateGenericE2EServiceInstance extends AbstractServiceTaskProcessor {

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    VidUtils vidUtils = new VidUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * Deal with the parameters
     * generate the service instance id
     * generate the operation id
     */
    public void preProcessRequest (Execution execution) {
	  /* def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
       String msg = ""
       utils.log("DEBUG", " *** preProcessRequest() *** ", isDebugEnabled)
       try {

           String siRequest = execution.getVariable("bpmnRequest")
           utils.logAudit(siRequest)

           String requestId = execution.getVariable("mso-request-id")
           execution.setVariable("msoRequestId", requestId)
           utils.log("DEBUG", "Input Request:" + siRequest + " reqId:" + requestId, isDebugEnabled)

           String serviceInstanceId = execution.getVariable("serviceInstanceId")
           if (isBlank(serviceInstanceId)) {
               serviceInstanceId = UUID.randomUUID().toString()
           }
           utils.log("DEBUG", "Generated new Service Instance:" + serviceInstanceId, isDebugEnabled)
           serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
           execution.setVariable("serviceInstanceId", serviceInstanceId)
           
           String operationId = execution.getVariable("operationId")
           if (isBlank(operationId)) {
               operationId = UUID.randomUUID().toString()
           }
           utils.log("DEBUG", "Generated new Service Instance Operation:" + operationId, isDebugEnabled)
           serviceInstanceId = UriUtils.encode(operationId,"UTF-8")
           execution.setVariable("operationId", operationId)
           //subscriberInfo, TBD , there is no globalSubscriberId in R1 for E2E Service.
           //requestInfo TBD , there is no requestDetails for R1 E2E service

           //TBD need to insert operationInfo to RequestDb
           
           //set service Instance Name
           execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "service.name"))
           execution.setVariable("serviceDescription", jsonUtil.getJsonValue(siRequest, "service.description"))
           execution.setVariable("templateId", jsonUtil.getJsonValue(siRequest, "service.templateId"))
     
           //serviceParamters
           String serviceParamters = jsonUtil.getJsonValue(siRequest, "service.parameters")
           if (isBlank(serviceParamters)) {
               msg = "Input service paramters is null"
               utils.log("DEBUG", msg, isDebugEnabled)
               exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
           } else
           {
               execution.setVariable("serviceParamters", serviceParamters)
           }

           utils.log("DEBUG", "service parameters:" + serviceParamters,  isDebugEnabled)
       } catch (BpmnError e) {
           throw e;
       } catch (Exception ex){
           msg = "Exception in preProcessRequest " + ex.getMessage()
           utils.log("DEBUG", msg, isDebugEnabled)
           exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
       }
       utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)*/
	}


    public void createNetworkService(Execution execution) {
    }

    public void instantiateNetworkService(Execution execution) {
    }

    public void queryNSProgress(Execution execution) {
    }

    public void timeDelay(Execution execution) {
    }

    public void finishNSCreate(Execution execution) {
    }
}
