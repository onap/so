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
import org.onap.so.db.request.client.RequestDbCommands
import org.springframework.web.client.HttpStatusCodeException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

class RequestDBUtil {
    private static final Logger logger = LoggerFactory.getLogger( RequestDBUtil.class);
    private ExceptionUtil exceptionUtil = new ExceptionUtil()

    static RequestDBUtil forHttp() {
        return new HttpPreparation()
    }

    void prepareUpdateOperationStatusHttp(DelegateExecution execution, OperationStatus status) {
        execution.setVariable('updateOperationStatusHttp', [serviceId: UriUtils.encode(status.serviceId, 'UTF-8'),
                operationId: status.operationId, operationType: status.operation, userId: status.userId,
                result: status.result, operationContent: status.operationContent ?: '',
                progress: status.progress, reason: status.reason ?: ''].collectEntries { name, value ->
                    [(name): String.valueOf(value)]
                })
    }

    void getOperationStatusHttp(DelegateExecution execution, String serviceId, String operationId) {
        execution.setVariable('getOperationStatusHttp', [serviceId: UriUtils.encode(serviceId, 'UTF-8'),
            operationId: UriUtils.encode(operationId, 'UTF-8')])
    }

    void prepareInitResourceOperationStatusHttp(DelegateExecution execution, ResourceOperationStatus status) {
        execution.setVariable('initResourceOperationStatusHttp', [serviceId: UriUtils.encode(status.serviceId, 'UTF-8'),
                operationId: status.operationId, operationType: status.operType,
                resourceTemplateUUIDs: status.resourceTemplateUUID].collectEntries { name, value ->
                    [(name): String.valueOf(value)]
                })
    }

    void prepareUpdateResourceOperationStatusHttp(DelegateExecution execution, ResourceOperationStatus status) {
        execution.setVariable('updateResourceOperationStatusHttp', [serviceId: UriUtils.encode(status.serviceId, 'UTF-8'),
                operationId: status.operationId, resourceTemplateUUID: status.resourceTemplateUUID,
                operType: status.operType, resourceInstanceID: status.resourceInstanceID, jobId: status.jobId,
                status: status.status, progress: status.progress, errorCode: status.errorCode ?: '',
                statusDescription: status.statusDescription ?: ''].collectEntries { name, value ->
                    [(name): String.valueOf(value)]
                })
    }

    void invokeHttp(DelegateExecution execution, String operation, String responseVariable, String codeVariable,
            String commandVariable = operation + 'Http') {
        try {
            Map command = execution.getVariable(commandVariable) as Map
            def client = RequestDbHttp.createClient(execution)
            Object response = ''
            int code = 204
            switch (operation) {
                case 'updateInfraRequest':
                    Map fields = new LinkedHashMap(command)
                    String requestId = fields.remove('requestId')
                    client.updateInfraRequest(requestId, new RequestDbCommands.InfraRequestUpdate(fields))
                    break
                case 'updateOperationStatus':
                    client.updateServiceOperationStatus(new RequestDbCommands.ServiceOperationWrite(command))
                    break
                case 'initServiceOperationStatus':
                    client.initServiceOperationStatus(new RequestDbCommands.ServiceOperationWrite(command))
                    break
                case 'initResourceOperationStatus':
                    client.initResourceOperationStatus(new RequestDbCommands.ResourceOperationInit(command))
                    break
                case 'updateResourceOperationStatus':
                    Map fields = new LinkedHashMap(command)
                    String serviceId = fields.remove('serviceId')
                    String operationId = fields.remove('operationId')
                    String templateId = fields.remove('resourceTemplateUUID')
                    client.updateResourceOperationStatus(serviceId, operationId, templateId,
                            new RequestDbCommands.ResourceOperationUpdate(fields))
                    break
                case 'getOperationStatus':
                    response = client.getServiceOperationStatus(command.serviceId, command.operationId)
                    code = response == null ? 404 : 200
                    break
                default:
                    throw new IllegalArgumentException('Unsupported RequestDB operation: ' + operation)
            }
            execution.setVariable(responseVariable, response)
            execution.setVariable(codeVariable, code)
        } catch (HttpStatusCodeException failure) {
            execution.setVariable(responseVariable, null)
            execution.setVariable(codeVariable, failure.statusCode.value())
        } catch (Exception failure) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, 'RequestDB HTTP operation failed')
        }
    }

    private static class HttpPreparation extends RequestDBUtil {
        @Override
        void prepareUpdateOperationStatus(DelegateExecution execution, OperationStatus status) {
            prepareUpdateOperationStatusHttp(execution, status)
        }

        @Override
        void getOperationStatus(DelegateExecution execution, String serviceId, String operationId) {
            getOperationStatusHttp(execution, serviceId, operationId)
        }

        @Override
        void prepareInitResourceOperationStatus(DelegateExecution execution, ResourceOperationStatus status) {
            prepareInitResourceOperationStatusHttp(execution, status)
        }

        @Override
        void prepareUpdateResourceOperationStatus(DelegateExecution execution, ResourceOperationStatus status) {
            prepareUpdateResourceOperationStatusHttp(execution, status)
        }
    }

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
	 * init resource  operation status in requestDB
	 * @param execution
	 * @param resourceoperationStatus
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
                                    <operType>${MsoUtils.xmlEscape(operType)}</operType>
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
	 * update resource operation status in requestDB
	 * @param execution
	 * @param resourceoperationStatus
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
									<resourceTemplateUUID>${MsoUtils.xmlEscape(resourceTemplateUUID)}</resourceTemplateUUID>
                                    <operType>${MsoUtils.xmlEscape(operType)}</operType>
                                    <resourceInstanceID>${MsoUtils.xmlEscape(resourceInstanceID)}</resourceInstanceID>
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
