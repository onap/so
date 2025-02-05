/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC All rights reserved. *
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

import org.onap.so.logger.LoggingAnchor
import org.onap.so.logging.filter.base.ErrorCode

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils;



/**
 * This groovy class supports the <class>DoScaleServiceInstance.bpmn</class> process.
 *
 */
public class DoScaleE2EServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoScaleE2EServiceInstance.class);


    String Prefix = "DCRESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    public void preProcessRequest(DelegateExecution execution) {
        String msg = ""
        logger.trace("preProcessRequest ")

        try {
            String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix", Prefix)

            //Inputs
            String globalSubscriberId = execution.getVariable("globalSubscriberId")

            String serviceType = execution.getVariable("serviceType")
            String serviceInstanceName = execution.getVariable("serviceInstanceName")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            execution.setVariable("serviceType", serviceType)

            String resourceTemplateUUIDs = ""
            String scaleNsRequest = execution.getVariable("bpmnRequest")
            JSONObject jsonObject = new JSONObject(scaleNsRequest).getJSONObject("service")
            JSONArray jsonArray = jsonObject.getJSONArray("resources")

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject reqBodyJsonObj = jsonArray.getJSONObject(i)
                String nsInstanceId = reqBodyJsonObj.getString("resourceInstanceId")
                resourceTemplateUUIDs = resourceTemplateUUIDs + nsInstanceId + ":"
            }

            execution.setVariable("resourceTemplateUUIDs", resourceTemplateUUIDs)

            if (serviceInstanceName == null) {
                execution.setVariable("serviceInstanceName", "")
            }
            if (isBlank(serviceInstanceId)) {
                msg = "Input serviceInstanceId is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit preProcessRequest ")
    }


    public void preInitResourcesOperStatus(DelegateExecution execution){
        logger.trace("STARTED preInitResourcesOperStatus Process ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = "SCALE"

            // resourceTemplateUUIDs should be created ??
            String resourceTemplateUUIDs = execution.getVariable("resourceTemplateUUIDs")
            logger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            execution.setVariable("URN_mso_openecomp_adapters_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initResourceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <resourceTemplateUUIDs>${MsoUtils.xmlEscape(resourceTemplateUUIDs)}</resourceTemplateUUIDs>
                        </ns:initResourceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_initResOperStatusRequest", payload)
            logger.info("Outgoing initResourceOperationStatus: \n" + payload)
            logger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing preInitResourcesOperStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), e);
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preInitResourcesOperStatus Process ")
    }

}
