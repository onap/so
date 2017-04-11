/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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
package org.openecomp.mso.bpmn.common.scripts

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.delegate.BpmnError

/**
 * Please describe the CreateGenericVNFUtils.groovy class
 *
 */
class CreateGenericVNFUtils{

	String Prefix="CRTGVNF_"

	def utils=new MsoUtils()

	private AbstractServiceTaskProcessor taskProcessor
	
	public CreateGenericVNFUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	/**
	 * This method is executed during the Initialization task of the process.
	 *
	 * @param execution
	 *
	 */

	public APIResponse queryAAI(Execution execution, String path){

	def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
	execution.setVariable("prefix", Prefix)
	utils.log("DEBUG", " ======== STARTED queryAAI Process ======== ", isDebugEnabled)

	def uuid = execution.getVariable("CRTGVNF_uuid")
	utils.log("DEBUG", "UUID is: " + uuid, isDebugEnabled)

	//Setting request path
	String queryAAIRequestPath = execution.getVariable("URN_aai_endpoint")+path
//	execution.setVariable("CRTGVNF_queryAAIRequestPath", queryAAIRequestPath)
	utils.log("DEBUG", "QueryAAIRequest Path is: " + "\n" + queryAAIRequestPath, isDebugEnabled)

	try {
		AaiUtil aaiUtil = new AaiUtil(taskProcessor)
		return aaiUtil.executeAAIGetCall(execution, queryAAIRequestPath)
	}catch(Exception e){
		utils.log("ERROR", "Exception Occured Processing queryAAI. Exception is:\n" + e, isDebugEnabled)
		execution.setVariable("CRTGVNF_dataValidationFlag", false)
		execution.setVariable("CRTGVNF_ErrorResponse", "Error Occured during queryAAI Method:\n" + e.getMessage())
	}
	utils.log("DEBUG", "======== COMPLETED queryAAI Process ======== ", isDebugEnabled)
}

	public String buildSDNCRequest(Execution execution, String svcInstId, String action){

		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("att-mso-request-id") + "-" +  	System.currentTimeMillis()
		}
		def callbackURL = execution.getVariable("CRTGVNF_sdncCallbackUrl")
		def requestId = execution.getVariable("CRTGVNF_requestId")
		def serviceType = execution.getVariable("CRTGVNF_serviceType")
		def vnfType = execution.getVariable("CRTGVNF_vnfType")
		def vnfName = execution.getVariable("CRTGVNF_vnfName")
		def tenantId = execution.getVariable("CRTGVNF_tenantId")
		def source = execution.getVariable("CRTGVNF_source")
		String vnfId = svcInstId
		String sdncVNFParamsXml = ""

		if(execution.getVariable("CRTGVNF_vnfParamsExistFlag") == true){
			sdncVNFParamsXml = buildSDNCParamsXml(execution)
		}else{
			sdncVNFParamsXml = ""
		}

		String sdncRequest =
		"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://ecomp.att.com/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://domain2.att.com/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>VNFActivateRequest</request-action>
			<source>${source}</source>
		</request-information>
		<service-information>
			<service-type>${serviceType}</service-type>
			<service-instance-id>${vnfId}</service-instance-id>
			<subscriber-name>notsurewecare</subscriber-name>
		</service-information>
		<vnf-request-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type>${vnfType}</vnf-type>
			<vnf-name>${vnfName}</vnf-name>
			<tenant>${tenantId}</tenant>
${sdncVNFParamsXml}
		</vnf-request-information>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

	return sdncRequest

	}

	public String buildSDNCParamsXml(Execution execution){

		String params = ""
		StringBuilder sb = new StringBuilder()
		Map<String, String> paramsMap = execution.getVariable("CRTGVNF_vnfParamsMap")

		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			String paramsXml
			String key = entry.getKey();
			if(key.endsWith("_network")){
				String requestKey = key.substring(0, key.indexOf("_network"))
				String requestValue = entry.getValue()
				paramsXml =
"""<vnf-networks>
	<network-role>{ functx:substring-before-match(data($param/@name), '_network') }</network-role>
	<network-name>{ $param/text() }</network-name>
</vnf-networks>"""
			}else{
			paramsXml = ""
			}
			params = sb.append(paramsXml)
		}
		return params
	}

	/**
	 * Builds a "CompletionHandler" request and stores it in the specified
	 * execution variable.
	 * @param execution the execution
	 */
	public void buildCompletionHandlerRequest(Execution execution, String flowName) {
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
			'execution=' + execution.getId() +
			')'
		def prefix = execution.getVariable('prefix')
		def resultVar = prefix + "CompletionHandlerRequest"
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def request = taskProcessor.getVariable(execution, prefix+'Request')
			def requestInformation = taskProcessor.utils.getNodeXml(request, 'request-information', false)
			if (requestInformation == null || requestInformation == ""){
				requestInformation = taskProcessor.utils.getNodeXml(request, 'request-info', false)
			}

			String content = """
		<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
				xmlns:reqtype="http://ecomp.att.com/mso/request/types/v1">
			${requestInformation}
			<sdncadapterworkflow:mso-bpel-name>${flowName}</sdncadapterworkflow:mso-bpel-name>
		</sdncadapterworkflow:MsoCompletionRequest>
	"""

			content = taskProcessor.utils.removeXmlPreamble(taskProcessor.utils.formatXML(content))
			taskProcessor.logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			taskProcessor.logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error Occured during completion handler request")
		}
	}

	/**
	 * Builds a "FalloutHandler" request and stores it in the specified
	 * execution variable.
	 * @param execution the execution
	 */
	public void buildfalloutHandlerRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.falloutHandlerPrep(' +
			'execution=' + execution.getId() +
			')'
		def prefix = execution.getVariable('prefix')
		def resultVar = prefix + "FalloutHandlerRequest"
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)
		try {
			def request = taskProcessor.getVariable(execution, prefix+'Request')

			def requestInformation = ""
			if (request != null){
			 requestInformation = taskProcessor.utils.getNodeXml(request, 'request-information', false)
			 if (requestInformation == null || requestInformation == ""){
				 requestInformation = taskProcessor.utils.getNodeXml(request, 'request-info', false)
			 }
			}
			def  errorInformation = ""
			def encErrorResponseMsg = ""
			def errorResponseCode = ""
			String content = ""
			
			def WorkflowException workflowException
			def exception =  execution.getVariable("WorkflowException")
						
			if (exception instanceof WorkflowException)
			 {
				 workflowException = execution.getVariable("WorkflowException")
			 }
			 
			if (workflowException != null){
				errorResponseCode = workflowException.getErrorCode()
				def errorResponseMsg = workflowException.getErrorMessage()

				if (errorResponseMsg != null) {
					encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				}

				errorInformation = """<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
						<aetgt:ErrorMessage>${encErrorResponseMsg}</aetgt:ErrorMessage>
						<aetgt:ErrorCode>${errorResponseCode}</aetgt:ErrorCode>
				</aetgt:WorkflowException>"""
			}
			else {
				errorInformation = execution.getVariable(prefix+'ErrorResponse')

				if (errorInformation == null) errorInformation = ""
			}
			
			content = """
				<wfsch:FalloutHandlerRequest xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1"
                             xmlns:reqtype="http://ecomp.att.com/mso/request/types/v1">
				${requestInformation}
                ${errorInformation}
				</wfsch:FalloutHandlerRequest>
			"""
			 
			content = taskProcessor.utils.removeXmlPreamble(taskProcessor.utils.formatXML(content))
		
			taskProcessor.logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)
			taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (Exception e) {
			taskProcessor.logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error in buildfalloutHandlerRequest")
		}
	}
	
}





