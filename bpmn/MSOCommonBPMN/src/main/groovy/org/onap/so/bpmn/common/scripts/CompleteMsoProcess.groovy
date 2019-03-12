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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class CompleteMsoProcess extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( CompleteMsoProcess.class);

	String Prefix="CMSO_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	// Complete MSO Request processing
	public initializeProcessVariables(DelegateExecution execution){

		def method = getClass().getSimpleName() + '.initializeProcessVariables(' +'execution=' + execution.getId() +')'
		logger.trace('Entered ' + method)
		try {

			/* Initialize all the process request variables in this block */
			execution.setVariable("prefix",Prefix)
			execution.setVariable("CMSO_request_id","")
			execution.setVariable("CMSO_notification-url","")
			execution.setVariable("CMSO_mso-bpel-name","")
			execution.setVariable("CMSO_request_action","")
			execution.setVariable("CMSO_notification-url-Ok", false)
			execution.setVariable("CMSO_request_id-Ok", false)

			//updateRequest Adapter process variables
			execution.setVariable("CMSO_updateRequestResponse", "")
			execution.setVariable("CMSO_updateRequestResponseCode", "")
			execution.setVariable("CMSO_updateFinalNotifyAckStatusFailedPayload", "")

			//Set DB adapter variables here
			execution.setVariable("CMSO_updateDBStatusToSuccessPayload", "")
			execution.setVariable("CMSO_updateInfraRequestDBPayload", "")
			execution.setVariable("CMSO_setUpdateDBstatustoSuccessPayload", "")

			//Auth variables
			execution.setVariable("BasicAuthHeaderValue","")

			//Response variables
			execution.setVariable("CompletionHandlerResponse","")
			execution.setVariable("CMSO_ErrorResponse", null)
			execution.setVariable("CMSO_ResponseCode", "")

			setSuccessIndicator(execution, false)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} {} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

	}

	public void preProcessRequest (DelegateExecution execution) {

		initializeProcessVariables(execution)
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		//		logger.trace("Started CompleteMsoProcess preProcessRequest Method ");
		logger.trace('Entered ' + method)
		
		setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))

		try {
			def xml = execution.getVariable("CompleteMsoProcessRequest")

			logger.debug("CompleteMsoProcess Request: " + xml)
			logger.debug("Incoming Request is: "+ xml)

			//mso-bpel-name from the incoming request
			def msoBpelName = utils.getNodeText(xml,"mso-bpel-name")
			execution.setVariable("CMSO_mso-bpel-name",msoBpelName)

			//Check the incoming request type
			//Incoming request can be ACTIVE_REQUESTS (request-information node) or  INFRA_ACTIVE_REQUESTS (request-info node)
			if (utils.nodeExists(xml, "request-information")) {
				execution.setVariable("CMSO_request_id-Ok", true) // Incoming request is for ACTIVE_REQUESTS
			}

			//Check for rehome indicator
			def rehomeIndicator = utils.getNodeText(xml,"rehomeDone")
			execution.setVariable("rehomeDone", rehomeIndicator)

			//Check notification-url for the incoming request type
			//ACTIVE_REQUESTS may have notificationurl node
			//INFRA_ACTIVE_REQUESTS notificationurl node does not exist
			def notificationurl = ""
			if (utils.nodeExists(xml, "notification-url")) {
				notificationurl = utils.getNodeText(xml,"notification-url")
				if(notificationurl != null && !notificationurl.isEmpty()) {
					execution.setVariable("CMSO_notification-url-Ok", true)
					execution.setVariable("CMSO_notification-url",notificationurl)
				}
			}

			//Check request_id for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload request-id IS optional (Not sure why this is option since req id is primary key ... also tried exe through SOAP UI to check if MSO code handles null like auto generated seq not it does not)
			//For ACTIVE_REQUESTS payload request-id is NOT optional
			def request_id = ""
			if (utils.nodeExists(xml, "request-id")) {
				execution.setVariable("CMSO_request_id",utils.getNodeText(xml,"request-id"))
			}


			// INFRA_ACTIVE_REQUESTS	 have "action" element ... mandatory
			// ACTIVE_REQUEST have "request-action" ... mandatory
			if (utils.nodeExists(xml, "request-action")) {
				execution.setVariable("CMSO_request_action",utils.getNodeText(xml,"request-action"))
			} else if (utils.nodeExists(xml, "action")) {
				execution.setVariable("CMSO_request_action",utils.getNodeText(xml,"action"))
			}

			//Check source for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload source IS optional
			//For ACTIVE_REQUESTS payload source is NOT optional
			def source = ""
			if (utils.nodeExists(xml, "source")) {
				execution.setVariable("CMSO_source",utils.getNodeText(xml,"source"))
			}

			logger.trace("--> " + execution.getVariable(""))
			logger.trace("--> " + execution.getVariable(""))

			// set the DHV/Service Instantiation values if specified in the request
			execution.setVariable("CMSO_is_srv_inst_req", String.valueOf("true".equals(utils.getNodeText(xml, "is-srv-inst-req"))))
			logger.trace("--> " + execution.getVariable(""))
			execution.setVariable("CMSO_is_json_content", String.valueOf("JSON".equals(utils.getNodeText(xml, "resp-content-type"))))
			logger.trace("--> " + execution.getVariable(""))
			execution.setVariable("CMSO_service_inst_id", utils.getNodeText(xml, "service-instance-id"))
			logger.trace("--> " + execution.getVariable(""))
			execution.setVariable("CMSO_start_time", utils.getNodeText(xml, "start-time"))
			logger.trace("--> " + execution.getVariable(""))
			// this variable is used by the camunda flow to set the Content-Type for the async response
			if (execution.getVariable("CMSO_is_srv_inst_req").equals("true") &&
				execution.getVariable("CMSO_is_json_content").equals("true")) {
				execution.setVariable("CMSO_content_type", "application/json")
			} else {
				execution.setVariable("CMSO_content_type", "text/xml")
			}

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.debug("Exception Occured During PreProcessRequest: " + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}

	public void setUpdateDBstatustoSuccessPayload (DelegateExecution execution){

		def method = getClass().getSimpleName() + '.setUpdateDBstatustoSuccessPayload(' +'execution=' + execution.getId() +')'
		logger.trace('Entered ' + method)

		try {

			def xml = execution.getVariable("CompleteMsoProcessRequest")

			//Get statusMessage if exists
			def statusMessage
			if(utils.nodeExists(xml, "status-message")){
				statusMessage = utils.getNodeText(xml, "status-message")
			}else{
				statusMessage = "Resource Completed Successfully"
			}

			//Get instance Id if exist
			String idXml = ""
			if(utils.nodeExists(xml, "vnfId")){
				idXml = utils.getNodeXml(xml, "vnfId")
			}else if(utils.nodeExists(xml, "networkId")){
				idXml = utils.getNodeXml(xml, "networkId")
			}else if(utils.nodeExists(xml, "configurationId")){
				idXml = utils.getNodeXml(xml, "configurationId")
			}else if(utils.nodeExists(xml, "serviceInstanceId")){
				idXml = utils.getNodeXml(xml, "serviceInstanceId")
			}else if(utils.nodeExists(xml, "vfModuleId")){
				idXml = utils.getNodeXml(xml, "vfModuleId")
			}else if(utils.nodeExists(xml, "volumeGroupId")){
				idXml = utils.getNodeXml(xml, "volumeGroupId")
			}else{
				idXml = ""
			}
			idXml = utils.removeXmlPreamble(idXml)
			idXml = utils.removeXmlNamespaces(idXml)
			logger.debug("Incoming Instance Id Xml: " + idXml)

			String payload = """
						<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>${MsoUtils.xmlEscape(execution.getVariable("CMSO_request_id"))}</requestId>
						         <lastModifiedBy>${MsoUtils.xmlEscape(execution.getVariable("CMSO_mso-bpel-name"))}</lastModifiedBy>
						         <statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
						         <requestStatus>COMPLETE</requestStatus>
								 <progress>100</progress>
								 ${idXml}
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>"""

			execution.setVariable("CMSO_setUpdateDBstatustoSuccessPayload", payload)
			logger.debug("Outgoing Update Mso Request Payload is: " + payload)
			logger.debug("setUpdateDBstatustoSuccessPayload: " + payload)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} {} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
		logger.trace('Exited ' + method)
	}

	public void buildDataError (DelegateExecution execution, String message) {

		def method = getClass().getSimpleName() + '.buildDataError(' +'execution=' + execution.getId() +')'
		logger.trace('Entered ' + method)
		try {

			String msoCompletionResponse = """
			<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
			   <sdncadapterworkflow:out>BPEL ${execution.getVariable("CMSO_mso-bpel-name")} FAILED</sdncadapterworkflow:out>
			</sdncadapterworkflow:MsoCompletionResponse>
			""".trim()

			// Format Response
			def xmlMsoCompletionResponse = utils.formatXml(msoCompletionResponse)
			String buildMsoCompletionResponseAsString = xmlMsoCompletionResponse.drop(38).trim()
			logger.debug("CompleteMsoProcess Response: " + buildMsoCompletionResponseAsString)
			execution.setVariable("CompleteMsoProcessResponse", buildMsoCompletionResponseAsString)
			logger.debug("@@ CompleteMsoProcess Response @@ " + "\n" + execution.getVariable("CompletionHandlerResponse"))

			exceptionUtil.buildAndThrowWorkflowException(execution, 500, message)

		} catch (BpmnError e) {
			logger.debug("Rethrowing MSOWorkflowException")
			throw e;
		} catch (Exception e) {
			logger.error("{} {} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

	}

	public void postProcessResponse (DelegateExecution execution) {

				def method = getClass().getSimpleName() + '.postProcessResponse(' +'execution=' + execution.getId() +')'
				logger.trace('Entered ' + method)
		//		logger.trace("Started CompleteMsoProcess PostProcessRequest Method ");
				try {

					String msoCompletionResponse = """
			<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow="http://ecomp.com/mso/workflow/schema/v1">
			   <sdncadapterworkflow:out>BPEL ${execution.getVariable("CMSO_mso-bpel-name")} completed</sdncadapterworkflow:out>
			</sdncadapterworkflow:MsoCompletionResponse>
			""".trim()

					// Format Response
					def xmlMsoCompletionResponse = utils.formatXML(msoCompletionResponse)
					String buildMsoCompletionResponseAsString = xmlMsoCompletionResponse.drop(38).trim()
					// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
					execution.setVariable("WorkflowResponse", buildMsoCompletionResponseAsString)
					logger.debug("CompleteMsoProcess Response: " + buildMsoCompletionResponseAsString)
					execution.setVariable("CompleteMsoProcessResponse", buildMsoCompletionResponseAsString)
					execution.setVariable("CMSO_ResponseCode", "200")

					setSuccessIndicator(execution, true)

					logger.debug("@@ CompleteMsoProcess Response @@ " + "\n" + execution.getVariable("CompleteMsoProcessResponse"))

					logger.trace('Exited ' + method)
				} catch (BpmnError e) {
					throw e;
				} catch (Exception e) {
					logger.error("{} {} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
				}
	}


}
