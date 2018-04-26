
/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil


public class DeleteVFCNSResource extends AbstractServiceTaskProcessor {

    String Prefix = "DCUSE_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** start preProcessRequest *****",  isDebugEnabled)

        String resourceInputStr = execution.getVariable("resourceInput")
        ResourceInput resourceInput = new ObjectMapper().readValue(resourceInputStr, ResourceInput.class)

        String globalSubscriberId = resourceInput.getGlobalSubscriberId()
        String serviceType = execution.getVariable("serviceType")
        String operationId = resourceInput.getOperationId()
        String resourceModeluuid = resourceInput.getResourceModelInfo().getModelCustomizationUuid()
        String resourceInstanceId = resourceInput.getResourceInstancenUuid()


        execution.setVariable("globalSubscriberId",globalSubscriberId)
        execution.setVariable("serviceType", serviceType)
        execution.setVariable("operationId", operationId)
        execution.setVariable("resourceTemplateId", resourceModeluuid)
        execution.setVariable("resourceInstanceId", resourceInstanceId)

        utils.log("INFO"," ***** end preProcessRequest *****",  isDebugEnabled)
    }

    public void postProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** start postProcessRequest *****",  isDebugEnabled)

        utils.log("INFO"," ***** end postProcessRequest *****",  isDebugEnabled)
    }

    public void sendSyncResponse (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

        try {
            String nsInstanceId = execution.getVariable("nsInstanceId")
            String operationStatus = execution.getVariable("operationStatus")
            // RESTResponse for main flow
            String createVFCResourceRestRsp = """{"nsInstanceId":"${nsInstanceId}","operationStatus":"${operationStatus}"}""".trim()
            utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + createVFCResourceRestRsp, isDebugEnabled)
            sendWorkflowResponse(execution, 202, createVFCResourceRestRsp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            utils.log("DEBUG", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
    }
}