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

package org.openecomp.mso.bpmn.common.scripts;

import java.text.SimpleDateFormat

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.WorkflowException


// SDNC Adapter Request/Response processing

public class SDNCAdapter extends AbstractServiceTaskProcessor {

	def Prefix="SDNCA_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	// Script Task: Process SDNC Workflow Request
	// Params: 	Workflow DelegateExecution
	// Assume:	Received SDNCAdapterWorkflowRequest is in variable 'sdncAdapterWorkflowRequest'
	//			Put created SDNCAdapterRequest in variable 'sdncAdapterRequest'
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		try{

			utils.log("DEBUG", "=========== Begin PreProcess SDNCAdapterRequestScript  ===========", isDebugEnabled)
			utils.log("DEBUG", "Incoming sdncAdapterWorkflowRequest:\n" + execution.getVariable("sdncAdapterWorkflowRequest"), isDebugEnabled)

			// Initialize some variables used throughout the flow
			execution.setVariable("prefix", Prefix)
			execution.setVariable("sdncAdapterResponse", "")
			execution.setVariable("asynchronousResponseTimeout", false)
			execution.setVariable("continueListening", false)
			execution.setVariable("SDNCA_SuccessIndicator", false)
			execution.setVariable("SDNCA_InterimNotify", false)
			
			// Authorization Info
			String basicAuthValue = execution.getVariable("URN_mso_adapters_po_auth")
			utils.log("DEBUG", "Obtained BasicAuth userid password for sdnc adapter:" + basicAuthValue, isDebugEnabled)
			try {
				def encodedString = utils.getBasicAuth(basicAuthValue, execution.getVariable("URN_mso_msoKey"))
				execution.setVariable("BasicAuthHeaderValue",encodedString)
			} catch (IOException ex) {
				utils.log("ERROR", "Unable to encode username password string")
			}

			// TODO Use variables instead of passing xml request - Huh?

			// Get original RequestHeader
			def sdncwfreq= execution.getVariable("sdncAdapterWorkflowRequest")
			def requestHeader = utils.getNodeXml(sdncwfreq, "RequestHeader")
			requestHeader = requestHeader.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
			utils.log("DEBUG", "RequestHeader:\n" + requestHeader, isDebugEnabled)

			// Set Callback URL to use from URN Mapping or jBoss Property
			def origCallbackUrl = utils.getNodeText(requestHeader, "CallbackUrl")
			def callbackUrlToUse = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			MsoUtils msoUtil = new MsoUtils()
			def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
			if((useQualifiedHostName!=null) && (useQualifiedHostName.equals("true"))){
				callbackUrlToUse = msoUtil.getQualifiedHostNameForCallback(callbackUrlToUse)
			}
			utils.log("DEBUG", "Callback URL to use:\n" + callbackUrlToUse, isDebugEnabled)
			requestHeader = requestHeader.replace(origCallbackUrl, callbackUrlToUse)

			// Get parameters from request header
			def sdnca_svcInstanceId = utils.getNodeText1(requestHeader, "SvcInstanceId") // optional
			utils.log("DEBUG", "SvcInstanceId: " + sdnca_svcInstanceId, isDebugEnabled)
			def sdnca_msoAction = utils.getNodeText1(requestHeader, "MsoAction") // optional
			utils.log("DEBUG", "MsoAction: " + sdnca_msoAction, isDebugEnabled)
			def sdnca_svcAction = utils.getNodeText(requestHeader, "SvcAction")
			utils.log("DEBUG", "SvcAction: " + sdnca_svcAction, isDebugEnabled)
			def sdnca_svcOperation = utils.getNodeText(requestHeader, "SvcOperation")
			utils.log("DEBUG", "SvcOperation: " + sdnca_svcOperation, isDebugEnabled)
			def sdncRequestData = utils.getChildNodes(sdncwfreq, "SDNCRequestData")
			sdncRequestData = sdncRequestData.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
			sdncRequestData = sdncRequestData.replaceAll('tag0:', '').replaceAll(':tag0', '')
			utils.log("DEBUG", "SDNCRequestData:\n" + sdncRequestData, isDebugEnabled)
			def sdnca_serviceType = ""
			if (utils.nodeExists(sdncwfreq, "service-type")) {
				sdnca_serviceType = utils.getNodeText(sdncwfreq, "service-type")
			}
			utils.log("DEBUG", "service-type: " + sdnca_serviceType, isDebugEnabled)
			def serviceConfigActivate = false
			def source = ''
			if ((sdnca_svcAction == 'activate') && (sdnca_svcOperation == 'service-configuration-operation') && (sdnca_serviceType == 'uCPE-VMS')) {
				serviceConfigActivate = true
				if (utils.nodeExists(sdncwfreq, 'source')) {
					source = utils.getNodeText(sdncwfreq, 'source')
				}
			}
			execution.setVariable("serviceConfigActivate", serviceConfigActivate)
			utils.log("DEBUG", "serviceConfigActivate: " + serviceConfigActivate, isDebugEnabled)
			execution.setVariable("source", source)
			utils.log("DEBUG", "source: " + source, isDebugEnabled)

			//calling process should pass a generated uuid if sending multiple sdnc requests
			def requestId = utils.getNodeText(requestHeader, "RequestId")
			execution.setVariable(Prefix + "requestId", requestId)

			// Prepare SDNC Request to the SDNC Adapter
			String sdncAdapterRequest = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
			<SOAP-ENV:Body>
			<aetgt:SDNCAdapterRequest xmlns:aetgt="http://org.openecomp/workflow/sdnc/adapter/schema/v1" xmlns:sdncadaptersc="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestHeader xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>"""

			if (sdnca_svcInstanceId != null) {
				sdncAdapterRequest += """
			<sdncadapter:SvcInstanceId>${sdnca_svcInstanceId}</sdncadapter:SvcInstanceId>"""
			}

			sdncAdapterRequest += """
			<sdncadapter:SvcAction>${sdnca_svcAction}</sdncadapter:SvcAction>
			<sdncadapter:SvcOperation>${sdnca_svcOperation}</sdncadapter:SvcOperation>
			<sdncadapter:CallbackUrl>${callbackUrlToUse}</sdncadapter:CallbackUrl>"""

			if (sdnca_msoAction != null) {
				sdncAdapterRequest += """
			<sdncadapter:MsoAction>${sdnca_msoAction}</sdncadapter:MsoAction>"""
			}

			sdncAdapterRequest += """
			</sdncadapter:RequestHeader>
			<sdncadaptersc:RequestData>${sdncRequestData}</sdncadaptersc:RequestData></aetgt:SDNCAdapterRequest></SOAP-ENV:Body></SOAP-ENV:Envelope>"""

			utils.logAudit("Outgoing SDNCAdapterRequest:\n" + sdncAdapterRequest)
			execution.setVariable("sdncAdapterRequest", sdncAdapterRequest)

			utils.log("DEBUG", execution.getVariable("sdncAdapterRequest"), isDebugEnabled)
			utils.log("DEBUG", execution.getVariable("URN_mso_adapters_sdnc_endpoint"), isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", 'Internal Error occured during PreProcess Method: ' + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'Internal Error occured during PreProcess Method') // TODO: what message and error code?
		}
		utils.log("DEBUG","=========== End pre Process SDNCRequestScript ===========", isDebugEnabled)
	}

	public void postProcessResponse (DelegateExecution execution) {

		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		try{
			utils.log("DEBUG","=========== Begin POSTProcess SDNCAdapter ===========", isDebugEnabled)
			utils.log("DEBUG","Incoming sdncAdapterCallbackRequest:\n" + execution.getVariable("sdncAdapterCallbackRequest"), isDebugEnabled)

			// Check the sdnccallback request and get the responsecode
			def sdnccallbackreq = execution.getVariable("sdncAdapterCallbackRequest")
			def callbackRequestData = ""
			def callbackHeader = ""
			utils.logAudit("SDNCAdapterCallback Request :" + sdnccallbackreq)
			
			if(sdnccallbackreq != null){
				callbackHeader = utils.getNodeXml(sdnccallbackreq, "CallbackHeader")
				callbackRequestData = utils.getNodeXml(sdnccallbackreq, "RequestData")

				callbackHeader = callbackHeader.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
				utils.log("DEBUG","SDNCCallbackHeader is:\n" + callbackHeader, isDebugEnabled)

				callbackRequestData = callbackRequestData.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
				utils.log("DEBUG","DECODED SDNCCallback RequestData is:\n" + callbackRequestData, isDebugEnabled)

				String sdncAdapterWorkflowResponse ="""
						<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
						<sdncadapterworkflow:response-data>
						${callbackHeader}
						${callbackRequestData}
						</sdncadapterworkflow:response-data>
						</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""


				utils.log("DEBUG","Outgoing sdncAdapterWorkflowResponse:\n" + sdncAdapterWorkflowResponse, isDebugEnabled)
				sdncAdapterWorkflowResponse = utils.formatXml(sdncAdapterWorkflowResponse)
				utils.logAudit("sdncAdapterWorkflowResponse :" + sdncAdapterWorkflowResponse)
				execution.setVariable("sdncAdapterResponse", sdncAdapterWorkflowResponse)
				// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
				execution.setVariable("WorkflowResponse", sdncAdapterWorkflowResponse)

				// Check final indicator to determine if we are to continue listening or not
				def String enhancedCallbackRequestData = callbackRequestData.replaceAll("&amp;", "&")
				enhancedCallbackRequestData = enhancedCallbackRequestData.replaceAll("&lt;", "<")
				enhancedCallbackRequestData = enhancedCallbackRequestData.replaceAll("&gt;", ">")
				// replace the data with '&' (ex: subscriber-name= 'FOUR SEASONS HEATING & COOLING'
				enhancedCallbackRequestData = enhancedCallbackRequestData.replace("&", "&amp;")
				utils.log("DEBUG","EnhancedCallbackRequestData:\n" + enhancedCallbackRequestData, isDebugEnabled)
				execution.setVariable("enhancedCallbackRequestData", enhancedCallbackRequestData)
				def continueListening = false
				if (utils.nodeExists(enhancedCallbackRequestData, "ack-final-indicator")) {
					if (utils.getNodeText(enhancedCallbackRequestData, "ack-final-indicator") == 'N') {
						continueListening = true
					}
				}
				execution.setVariable("continueListening", continueListening)
				utils.log("DEBUG", "Continue Listening: " + continueListening, isDebugEnabled)
				execution.setVariable("asynchronousResponseTimeout", false)
			}else{
				// Timed out waiting for asynchronous message, build error response
				exceptionUtil.buildWorkflowException(execution, 500, "SDNC Callback Timeout Error")
				execution.setVariable("asynchronousResponseTimeout", true)
				utils.log("DEBUG", "Timed out waiting for asynchronous message", isDebugEnabled)
			}
		}catch(Exception e){
			utils.log("DEBUG", 'Internal Error occured during PostProcess Method: ' + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'Internal Error occured during PostProcess Method') // TODO: what message and error code?
		}
		utils.log("DEBUG","=========== End POSTProcess SDNCAdapter ===========", isDebugEnabled)
	}

	public void callbackResponsecheck(DelegateExecution execution){

		def sdnccallbackreq=execution.getVariable("sdncAdapterCallbackRequest")
		utils.logAudit("sdncAdapterCallbackRequest :" + sdnccallbackreq)
		if (sdnccallbackreq==null){
			execution.setVariable("callbackResponseReceived",false);
		}else{
			execution.setVariable("callbackResponseReceived",true);
		}
	}

	public void resetCallbackRequest(DelegateExecution execution) {

		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG","=========== Begin Reset Callback Info SDNCAdapter ===========", isDebugEnabled)

		// Clear sdncAdapterCallbackRequest variable
		execution.removeVariable("sdncAdapterCallbackRequest")

		// Determine and set SDNC Timeout Value
		def enhancedCallbackRequestData = execution.getVariable("enhancedCallbackRequestData")
		utils.logAudit("sdncAdapter - enhancedCallbackRequestData :" + enhancedCallbackRequestData)
		def interim = false
		if (enhancedCallbackRequestData != null) {
			if (utils.nodeExists(enhancedCallbackRequestData, "ack-final-indicator")) {
				if (utils.getNodeText(enhancedCallbackRequestData, "ack-final-indicator") == 'N') {
					interim = true
				}
			}
		}
		def timeoutValue = execution.getVariable("URN_mso_sdnc_timeout")
		def sdncAdapterWorkflowRequest = execution.getVariable("sdncAdapterWorkflowRequest")
		if (interim && utils.nodeExists(sdncAdapterWorkflowRequest, "InterimSDNCTimeOutValueInHours")) {
			timeoutValue = "PT" + utils.getNodeText(sdncAdapterWorkflowRequest, "InterimSDNCTimeOutValueInHours") + "H"
		} else if (utils.nodeExists(sdncAdapterWorkflowRequest, "SDNCTimeOutValueInMinutes")) {
			timeoutValue = "PT" + utils.getNodeText(sdncAdapterWorkflowRequest, "SDNCTimeOutValueInMinutes") + "M"
		}
		execution.setVariable("sdncTimeoutValue", timeoutValue)
		utils.log("DEBUG", "Setting SDNC Timeout Value to " + timeoutValue, isDebugEnabled)

		utils.log("DEBUG","=========== End Reset Callback Info SDNCAdapter ===========", isDebugEnabled)
	}


	public void prepareDBMessage(DelegateExecution execution) {

		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG","=========== Begin Prepare DB Message SDNCAdapter ===========", isDebugEnabled)

		// Create DB Message
		def dbRequestId = execution.getVariable("mso-request-id")
		String dbUpdateInterimStageCompletion = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
				<SOAP-ENV:Body>
					<DBAdapter:updateInterimStageCompletion xmlns:DBAdapter="http://org.openecomp.mso/requestsdb">
   						<requestId>${dbRequestId}</requestId>
   						<interimStageCompletion>1</interimStageCompletion>
   						<lastModifiedBy>BPEL</lastModifiedBy>
				</DBAdapter:updateInterimStageCompletion>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>
			"""

		execution.setVariable("dbUpdateInterimStageCompletion", dbUpdateInterimStageCompletion)
		utils.logAudit("sdncAdapter - dbUpdateInterimStageCompletion :" + dbUpdateInterimStageCompletion)
		utils.log("DEBUG","DB UpdateInterimStageCompletion:\n" + dbUpdateInterimStageCompletion, isDebugEnabled)
		utils.log("DEBUG","=========== End Prepare DB Message SDNCAdapter ===========", isDebugEnabled)
	}

	public String generateCurrentTimeInUtc(){
		final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());
		return utcTime;
	}

	public void toggleSuccessIndicator(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("SDNCA_SuccessIndicator", true)
		utils.log("DEBUG","Setting SDNCA Success Indicator to True", isDebugEnabled)
	}

	public void assignError(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG","=========== Started Assign Error ===========", isDebugEnabled)
		WorkflowException wf = execution.getVariable("WorkflowException")
		if(wf == null){
			exceptionUtil.buildWorkflowException(execution, 5000, "SDNCAdapter Encountered an Internal Error") // TODO: Not sure what message and error code we want here.....
		}else{
			execution.setVariable("WorkflowException", wf)
		}

		utils.log("DEBUG","Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"), isDebugEnabled)
		utils.log("DEBUG","=========== End Assign Error ===========", isDebugEnabled)
	}
	
	public void setTimeout(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG","=========== Started SetTimeout ===========", isDebugEnabled)
		utils.log("DEBUG", "Timer expired, telling correlation service to stop listening", isDebugEnabled)
		execution.setVariable("asynchronousResponseTimeout", true)
		
		utils.log("DEBUG", "Timed out branch sleeping for one second to give success branch a chance to complete if running", isDebugEnabled)
		Thread.sleep(1000)
		utils.log("DEBUG","=========== End SetTimeout ===========", isDebugEnabled)
	}
}
