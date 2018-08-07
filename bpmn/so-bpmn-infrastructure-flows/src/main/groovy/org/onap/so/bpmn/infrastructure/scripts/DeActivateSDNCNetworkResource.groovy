/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
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

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.json.JSONObject
import org.json.XML
import org.onap.so.logger.MsoLogger
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils

/**
 * This groovy class supports the <class>ActivateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Activate
 */
public class DeActivateSDNCNetworkResource extends AbstractServiceTaskProcessor {
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
            CreateSDNCNetworkResource.class);
    String Prefix = "DEACTSDNCRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    public void preProcessRequest(DelegateExecution execution) {
        msoLogger.info(" ***** started  preProcessRequest*****")
    }

    public void prepareSDNCRequest(DelegateExecution execution) {
        msoLogger.info(" ***** started prepareSDNCRequest *****")
    }

    public void prepareUpdateAfterDeActivateSDNCResource(DelegateExecution execution) {
        msoLogger.info("***** started prepareUpdateAfterDeActivateSDNCResource *****")
    }

    public void postDeactivateSDNCCall(DelegateExecution execution) {
        msoLogger.info(" ***** started postDeactivateSDNCCall *****")
    }

    public void sendSyncResponse(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        msoLogger.info(" ***** started sendSyncResponse *****")

        try {
            String operationStatus = "finished"
            // RESTResponse for main flow
            String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
            msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
            sendWorkflowResponse(execution, 202, resourceOperationResp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" ***** Exit sendSyncResopnse *****")
    }

}