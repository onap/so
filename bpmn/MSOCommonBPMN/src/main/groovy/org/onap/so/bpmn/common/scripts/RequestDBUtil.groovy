/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

class RequestDBUtil {
    private static final Logger logger = LoggerFactory.getLogger( RequestDBUtil.class);
    private ExceptionUtil exceptionUtil = new ExceptionUtil()

    /**
     * update operation status in requestDB
     * @param execution
     * @param operationStatus
     */
    void prepareUpdateOperationStatus(DelegateExecution execution, final OperationStatus operationStatus){
        logger.debug("start prepareUpdateOperationStatus")
        try{
            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("dbAdapterEndpoint", dbAdapterEndpoint)
            logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            String serviceId = operationStatus.getServiceId()
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            String operationId = operationStatus.getOperationId()
            String userId = operationStatus.getUserId()
            String operationType = operationStatus.getOperation()
            String result = operationStatus.getResult()
            String progress = operationStatus.getProgress()
            String operationContent = operationStatus.getOperationContent()?: ""
            String reason = operationStatus.getReason()?: ""

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                                    <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                                    <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                                    <userId>${MsoUtils.xmlEscape(userId)}</userId>
                                    <result>${MsoUtils.xmlEscape(result)}</result>
                                    <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                                    <progress>${MsoUtils.xmlEscape(progress)}</progress>
                                    <reason>${MsoUtils.xmlEscape(reason)}</reason>
                                </ns:updateServiceOperationStatus>
                            </soapenv:Body>
                        </soapenv:Envelope>
                    """
            execution.setVariable("updateOperationStatus", payload)

        }catch(any){
            String exceptionMessage = "Prepare update ServiceOperationStatus failed. cause - " + any.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        logger.trace("finished update OperationStatus")
    }


    /**
     * get operation status from requestDB by serviceId and operationId
     * @param execution
     * @param serviceId
     * @param operationId
     */
    void getOperationStatus(DelegateExecution execution, String serviceId, String operationId) {
        logger.trace("start getOperationStatus")
        try {
            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("dbAdapterEndpoint", dbAdapterEndpoint)
            logger.trace("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            serviceId = UriUtils.encode(serviceId,"UTF-8")
            operationId = UriUtils.encode(operationId,"UTF-8")
            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:getServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                                    <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>                       
                                </ns:getServiceOperationStatus>
                            </soapenv:Body>
                        </soapenv:Envelope>
                    """
            execution.setVariable("getOperationStatus", payload)

        } catch(any){
            String exceptionMessage = "Get ServiceOperationStatus failed. cause - " + any.getMessage()
            logger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }
	
	/**
	 * init operation status in requestDB
	 * @param execution
	 * @param operationStatus
	 */
	void prepareInitResourceOperationStatus(DelegateExecution execution, final ResourceOperationStatus resourceoperationStatus){
		logger.debug("start prepareinitResourceOperationStatus")
		try{
			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String serviceId = resourceoperationStatus.getServiceId()
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			String operationId = resourceoperationStatus.getOperationId()
			String resourceTemplateUUID = resourceoperationStatus.getResourceTemplateUUID()
			String operType = resourceoperationStatus.getOperType()

			String payload =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:initResourceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                                    <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                                    <operationType>${MsoUtils.xmlEscape(operType)}</operationType>
                                    <resourceTemplateUUIDs>${MsoUtils.xmlEscape(resourceTemplateUUID)}</resourceTemplateUUIDs>
                                </ns:initResourceOperationStatus>
                            </soapenv:Body>
                        </soapenv:Envelope>
                    """
			execution.setVariable("initResourceOperationStatus", payload)

		}catch(any){
			String exceptionMessage = "Prepare init ResourceOperationStatus failed. cause - " + any.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.trace("finished init ResourceOperationStatus")
	}
	
	/**
	 * update operation status in requestDB
	 * @param execution
	 * @param operationStatus
	 */
	void prepareUpdateResourceOperationStatus(DelegateExecution execution, final ResourceOperationStatus resourceoperationStatus){
		logger.debug("start prepareUpdateResourceOperationStatus")
		try{
			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String serviceId = resourceoperationStatus.getServiceId()
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			String operationId = resourceoperationStatus.getOperationId()
			String resourceTemplateUUID = resourceoperationStatus.getResourceTemplateUUID()
			String operType = resourceoperationStatus.getOperType()
			String resourceInstanceID = resourceoperationStatus.getResourceInstanceID()
			String jobId = resourceoperationStatus.getJobId()
			String status = resourceoperationStatus.getStatus()
			String progress = resourceoperationStatus.getProgress()
			String errorCode = resourceoperationStatus.getErrorCode()?: ""
			String statusDescription = resourceoperationStatus.getStatusDescription()?: ""

			String payload =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:updateResourceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                                    <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
									<resourceTemplateUUIDs>${MsoUtils.xmlEscape(resourceTemplateUUID)}</resourceTemplateUUIDs>
                                    <operationType>${MsoUtils.xmlEscape(operType)}</operationType>
                                    <jobId>${MsoUtils.xmlEscape(jobId)}</jobId>
                                    <status>${MsoUtils.xmlEscape(status)}</status>
                                    <progress>${MsoUtils.xmlEscape(progress)}</progress>
                                    <errorCode>${MsoUtils.xmlEscape(errorCode)}</errorCode>
									<statusDescription>${MsoUtils.xmlEscape(statusDescription)}</statusDescription>
                                </ns:updateResourceOperationStatus>
                            </soapenv:Body>
                        </soapenv:Envelope>
                    """
			execution.setVariable("updateResourceOperationStatus", payload)

		}catch(any){
			String exceptionMessage = "Prepare update ResourceOperationStatus failed. cause - " + any.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.trace("finished update ResourceOperationStatus")
	}

}
