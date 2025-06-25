
/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory


public class DeleteVFCNSResource extends AbstractServiceTaskProcessor {

    private static final ObjectMapper mapper = new ObjectMapper()
    String Prefix = "DCUSE_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    private static final Logger logger = LoggerFactory.getLogger( DeleteVFCNSResource.class);

    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        logger.info(" ***** start preProcessRequest *****")

        String resourceInputStr = execution.getVariable("resourceInput")
        ResourceInput resourceInput = mapper.readValue(resourceInputStr, ResourceInput.class)

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

        logger.info(" ***** end preProcessRequest *****")
    }

    void postProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        logger.info(" ***** start postProcessRequest *****")

        logger.info(" ***** end postProcessRequest *****")
    }

    void sendSyncResponse (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        logger.debug( " *** sendSyncResponse *** ")

        try {
            String nsInstanceId = execution.getVariable("nsInstanceId")
            String operationStatus = execution.getVariable("operationStatus")
            // RESTResponse for main flow
            String createVFCResourceRestRsp = """{"nsInstanceId":"${nsInstanceId}","operationStatus":"${operationStatus}"}""".trim()
            logger.debug( " sendSyncResponse to APIH:" + "\n" + createVFCResourceRestRsp)
            sendWorkflowResponse(execution, 202, createVFCResourceRestRsp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            logger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(" ***** Exit sendSyncResopnse *****")
    }
}
