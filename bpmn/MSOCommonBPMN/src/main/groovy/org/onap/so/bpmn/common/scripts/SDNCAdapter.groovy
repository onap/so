/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.scripts;
import org.onap.so.bpmn.core.UrnPropertiesReader;

import java.text.SimpleDateFormat

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger




// SDNC Adapter Request/Response processing

public class SDNCAdapter extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCAdapter.class);


	def Prefix="SDNCA_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	// Script Task: Process SDNC Workflow Request
	// Params: 	Workflow DelegateExecution
	// Assume:	Received SDNCAdapterWorkflowRequest is in variable 'sdncAdapterWorkflowRequest'
	//			Put created SDNCAdapterRequest in variable 'sdncAdapterRequest'
	public void preProcessRequest (DelegateExecution execution) {
		try{

			msoLogger.trace("Begin PreProcess SDNCAdapterRequestScript  ")
			msoLogger.debug("Incoming sdncAdapterWorkflowRequest:\n" + execution.getVariable("sdncAdapterWorkflowRequest"))

			// Initialize some variables used throughout the flow
			execution.setVariable("prefix", Prefix)
			execution.setVariable("sdncAdapterResponse", "")
			execution.setVariable("asynchronousResponseTimeout", false)
			execution.setVariable("continueListening", false)
			execution.setVariable("SDNCA_SuccessIndicator", false)
			execution.setVariable("SDNCA_InterimNotify", false)

			// Authorization Info
			String basicAuthValue = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

			try {
				def encodedString = utils.getBasicAuth(basicAuthValue, UrnPropertiesReader.getVariable("mso.msoKey", execution))
				execution.setVariable("BasicAuthHeaderValue",encodedString)
			} catch (IOException ex) {
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Unable to encode username password string", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			}

			// TODO Use variables instead of passing xml request - Huh?

			// Get original RequestHeader
			def sdncwfreq= execution.getVariable("sdncAdapterWorkflowRequest")
			def requestHeader = utils.getNodeXml(sdncwfreq, "RequestHeader")
			requestHeader = requestHeader.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")

			// Set Callback URL to use from URN Mapping or jBoss Property
			def origCallbackUrl = utils.getNodeText(requestHeader, "CallbackUrl")
			def callbackUrlToUse = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution)
			MsoUtils msoUtil = new MsoUtils()
			def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host", execution)
			if((useQualifiedHostName!=null) && (useQualifiedHostName.equals("true"))){
				callbackUrlToUse = msoUtil.getQualifiedHostNameForCallback(callbackUrlToUse)
			}
			msoLogger.debug("Callback URL to use:\n" + callbackUrlToUse)
			requestHeader = requestHeader.replace(origCallbackUrl, callbackUrlToUse)

			// Get parameters from request header
			def sdnca_svcInstanceId = utils.getNodeText(requestHeader, "SvcInstanceId") // optional
			msoLogger.debug("SvcInstanceId: " + sdnca_svcInstanceId)
			def sdnca_msoAction = utils.getNodeText(requestHeader, "MsoAction") // optional
			msoLogger.debug("MsoAction: " + sdnca_msoAction)
			def sdnca_svcAction = utils.getNodeText(requestHeader, "SvcAction")
			msoLogger.debug("SvcAction: " + sdnca_svcAction)
			def sdnca_svcOperation = utils.getNodeText(requestHeader, "SvcOperation")
			msoLogger.debug("SvcOperation: " + sdnca_svcOperation)
			def sdncRequestData = utils.getChildNodes(sdncwfreq, "SDNCRequestData")
			sdncRequestData = sdncRequestData.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
			sdncRequestData = sdncRequestData.replaceAll('tag0:', '').replaceAll(':tag0', '')
			msoLogger.debug("SDNCRequestData:\n" + sdncRequestData)
			def sdnca_serviceType = ""
			if (utils.nodeExists(sdncwfreq, "service-type")) {
				sdnca_serviceType = utils.getNodeText(sdncwfreq, "service-type")
			}
			msoLogger.debug("service-type: " + sdnca_serviceType)
			def serviceConfigActivate = false
			def source = ''
			if ((sdnca_svcAction == 'activate') && (sdnca_svcOperation == 'service-configuration-operation') && (sdnca_serviceType == 'uCPE-VMS')) {
				serviceConfigActivate = true
				if (utils.nodeExists(sdncwfreq, 'source')) {
					source = utils.getNodeText(sdncwfreq, 'source')
				}
			}
			execution.setVariable("serviceConfigActivate", serviceConfigActivate)
			msoLogger.debug("serviceConfigActivate: " + serviceConfigActivate)
			execution.setVariable("source", source)
			msoLogger.debug("source: " + source)

			//calling process should pass a generated uuid if sending multiple sdnc requests
			def requestId = utils.getNodeText(requestHeader, "RequestId")
			execution.setVariable(Prefix + "requestId", requestId)

			// Prepare SDNC Request to the SDNC Adapter
			String sdncAdapterRequest = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
			<SOAP-ENV:Body>
			<aetgt:SDNCAdapterRequest xmlns:aetgt="http://org.onap/workflow/sdnc/adapter/schema/v1" xmlns:sdncadaptersc="http://org.onap/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestHeader xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>"""

			if (sdnca_svcInstanceId != null) {
				sdncAdapterRequest += """
			<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(sdnca_svcInstanceId)}</sdncadapter:SvcInstanceId>"""
				execution.setVariable("serviceInstanceId", sdnca_svcInstanceId)
			}

			sdncAdapterRequest += """
			<sdncadapter:SvcAction>${MsoUtils.xmlEscape(sdnca_svcAction)}</sdncadapter:SvcAction>
			<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(sdnca_svcOperation)}</sdncadapter:SvcOperation>
			<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrlToUse)}</sdncadapter:CallbackUrl>"""

			if (sdnca_msoAction != null) {
				sdncAdapterRequest += """
			<sdncadapter:MsoAction>${MsoUtils.xmlEscape(sdnca_msoAction)}</sdncadapter:MsoAction>"""
			}

			sdncAdapterRequest += """
			</sdncadapter:RequestHeader>
			<sdncadaptersc:RequestData>
				${sdncRequestData}
			</sdncadaptersc:RequestData></aetgt:SDNCAdapterRequest></SOAP-ENV:Body></SOAP-ENV:Envelope>"""

			msoLogger.debug("Outgoing SDNCAdapterRequest:\n" + sdncAdapterRequest)
			execution.setVariable("sdncAdapterRequest", sdncAdapterRequest)

			msoLogger.debug(UrnPropertiesReader.getVariable("mso.adapters.sdnc.endpoint", execution))
		}catch(Exception e){
			msoLogger.debug('Internal Error occured during PreProcess Method: ', e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'Internal Error occured during PreProcess Method') // TODO: what message and error code?
		}
		msoLogger.trace("End pre Process SDNCRequestScript ")
	}

	public void postProcessResponse (DelegateExecution execution) {

		try{
			msoLogger.trace("Begin POSTProcess SDNCAdapter ")
			msoLogger.trace("Incoming sdncAdapterCallbackRequest:\n" + execution.getVariable("sdncAdapterCallbackRequest"))

			// Check the sdnccallback request and get the responsecode
			def sdnccallbackreq = execution.getVariable("sdncAdapterCallbackRequest")
			def callbackRequestData = ""
			def callbackHeader = ""

			if(sdnccallbackreq != null){
				callbackHeader = utils.getNodeXml(sdnccallbackreq, "CallbackHeader")
				callbackRequestData = utils.getNodeXml(sdnccallbackreq, "RequestData")

				callbackHeader = callbackHeader.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")

				callbackRequestData = callbackRequestData.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
				
				msoLogger.trace("EnhancedCallbackRequestData:\n" + callbackRequestData)
				execution.setVariable("enhancedCallbackRequestData", callbackRequestData)

				String sdncAdapterWorkflowResponse ="""
						<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
						<sdncadapterworkflow:response-data>
						${callbackHeader}
						${callbackRequestData}
						</sdncadapterworkflow:response-data>
						</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""


				sdncAdapterWorkflowResponse = utils.formatXml(sdncAdapterWorkflowResponse)
				execution.setVariable("sdncAdapterResponse", sdncAdapterWorkflowResponse)
				// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
				execution.setVariable("WorkflowResponse", sdncAdapterWorkflowResponse)

				// Check final indicator to determine if we are to continue listening or not
				def continueListening = false
				if (utils.nodeExists(callbackRequestData, "ack-final-indicator")) {
					if (utils.getNodeText(callbackRequestData, "ack-final-indicator") == 'N') {
						continueListening = true
					}
				}
				execution.setVariable("continueListening", continueListening)
				msoLogger.debug("Continue Listening: " + continueListening)
				execution.setVariable("asynchronousResponseTimeout", false)
			}else{
				// Timed out waiting for asynchronous message, build error response
				exceptionUtil.buildWorkflowException(execution, 500, "SDNC Callback Timeout Error")
				execution.setVariable("asynchronousResponseTimeout", true)
				msoLogger.debug("Timed out waiting for asynchronous message")
			}
		}catch(Exception e){
			msoLogger.debug('Internal Error occured during PostProcess Method: ' + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'Internal Error occured during PostProcess Method') // TODO: what message and error code?
		}
		msoLogger.trace("End POSTProcess SDNCAdapter ")
	}

	public void callbackResponsecheck(DelegateExecution execution){

		def sdnccallbackreq=execution.getVariable("sdncAdapterCallbackRequest")
		msoLogger.debug("sdncAdapterCallbackRequest :" + sdnccallbackreq)
		if (sdnccallbackreq==null){
			execution.setVariable("callbackResponseReceived",false);
		}else{
			execution.setVariable("callbackResponseReceived",true);
		}
	}

	public void resetCallbackRequest(DelegateExecution execution) {

		msoLogger.trace("Begin Reset Callback Info SDNCAdapter ")

		// Clear sdncAdapterCallbackRequest variable
		execution.removeVariable("sdncAdapterCallbackRequest")

		// Determine and set SDNC Timeout Value
		def enhancedCallbackRequestData = execution.getVariable("enhancedCallbackRequestData")
		msoLogger.debug("sdncAdapter - enhancedCallbackRequestData :" + enhancedCallbackRequestData)
		def interim = false
		if (enhancedCallbackRequestData != null) {
			if (utils.nodeExists(enhancedCallbackRequestData, "ack-final-indicator")) {
				if (utils.getNodeText(enhancedCallbackRequestData, "ack-final-indicator") == 'N') {
					interim = true
				}
			}
		}
		def timeoutValue = UrnPropertiesReader.getVariable("mso.adapters.sdnc.timeout", execution)
 		if(timeoutValue==null)
 			timeoutValue="PT5M"
		def sdncAdapterWorkflowRequest = execution.getVariable("sdncAdapterWorkflowRequest")
		if (interim && utils.nodeExists(sdncAdapterWorkflowRequest, "InterimSDNCTimeOutValueInHours")) {
			timeoutValue = "PT" + utils.getNodeText(sdncAdapterWorkflowRequest, "InterimSDNCTimeOutValueInHours") + "H"
		} else if (utils.nodeExists(sdncAdapterWorkflowRequest, "SDNCTimeOutValueInMinutes")) {
			timeoutValue = "PT" + utils.getNodeText(sdncAdapterWorkflowRequest, "SDNCTimeOutValueInMinutes") + "M"
		}
		execution.setVariable("sdncTimeoutValue", timeoutValue)
		msoLogger.debug("Setting SDNC Timeout Value to " + timeoutValue)

		msoLogger.trace("End Reset Callback Info SDNCAdapter ")
	}


	public void prepareDBMessage(DelegateExecution execution) {

		msoLogger.trace("Begin Prepare DB Message SDNCAdapter ")

		// Create DB Message
		def dbRequestId = execution.getVariable("mso-request-id")
		String dbUpdateInterimStageCompletion = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
				<SOAP-ENV:Body>
					<DBAdapter:updateInterimStageCompletion xmlns:DBAdapter="http://org.onap.so/requestsdb">
   						<requestId>${MsoUtils.xmlEscape(dbRequestId)}</requestId>
   						<interimStageCompletion>1</interimStageCompletion>
   						<lastModifiedBy>BPEL</lastModifiedBy>
				</DBAdapter:updateInterimStageCompletion>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>
			"""

		execution.setVariable("dbUpdateInterimStageCompletion", dbUpdateInterimStageCompletion)
		msoLogger.debug("sdncAdapter - dbUpdateInterimStageCompletion :" + dbUpdateInterimStageCompletion)
		msoLogger.debug("DB UpdateInterimStageCompletion:\n" + dbUpdateInterimStageCompletion)
		msoLogger.trace("End Prepare DB Message SDNCAdapter ")
	}

	public String generateCurrentTimeInUtc(){
		final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());
		return utcTime;
	}

	public void toggleSuccessIndicator(DelegateExecution execution){
		execution.setVariable("SDNCA_SuccessIndicator", true)
		msoLogger.debug("Setting SDNCA Success Indicator to True")
	}

	public void assignError(DelegateExecution execution){
		msoLogger.trace("Started Assign Error ")
		WorkflowException wf = execution.getVariable("WorkflowException")
		if(wf == null){
			exceptionUtil.buildWorkflowException(execution, 5000, "SDNCAdapter Encountered an Internal Error") // TODO: Not sure what message and error code we want here.....
		}else{
			execution.setVariable("WorkflowException", wf)
		}

		msoLogger.debug("Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"))
		msoLogger.trace("End Assign Error ")
	}

	public void setTimeout(DelegateExecution execution){
		msoLogger.trace("Started SetTimeout ")
		msoLogger.debug("Timer expired, telling correlation service to stop listening")
		execution.setVariable("asynchronousResponseTimeout", true)

		msoLogger.debug("Timed out branch sleeping for one second to give success branch a chance to complete if running")
		Thread.sleep(1000)
		msoLogger.trace("End SetTimeout ")
	}
}
