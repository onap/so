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
package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*

import org.openecomp.mso.bpmn.core.RollbackData
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.web.util.UriUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils


/**
 * This class supports the DoCreateVnf building block subflow
 * with the creation of a generic vnf for
 * infrastructure.
 *
 */
class DoCreateVnf extends AbstractServiceTaskProcessor {

	String Prefix="DoCVNF_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoCreateVnf PreProcessRequest Process*** ", isDebugEnabled)
	
		// DISABLE SDNC INTERACTION FOR NOW
		execution.setVariable("SDNCInteractionEnabled", false)
		
		
		/*******************/
		try{
			// Get Variables			
			
			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData == null) {
				rollbackData = new RollbackData()
			}
						
			String vnfModelInfo = execution.getVariable("vnfModelInfo")			
			String serviceModelInfo = execution.getVariable("serviceModelInfo")
			
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("DoCVNF_requestId", requestId)
			execution.setVariable("mso-request-id", requestId)
			utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			execution.setVariable("DoCVNF_serviceInstanceId", serviceInstanceId)
			rollbackData.put("VNF", "serviceInstanceId", serviceInstanceId)
			utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

			String vnfType = execution.getVariable("vnfType")
			execution.setVariable("DoCVNF_vnfType", vnfType)
			utils.log("DEBUG", "Incoming Vnf Type is: " + vnfType, isDebugEnabled)

			String vnfName = execution.getVariable("vnfName")
			if (vnfName.equals("") || vnfName.equals("null")) {
				vnfName = null
			}
			execution.setVariable("DoCVNF_vnfName", vnfName)
			utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugEnabled)

			String serviceId = execution.getVariable("productFamilyId")
			execution.setVariable("DoCVNF_serviceId", serviceId)
			utils.log("DEBUG", "Incoming Service Id is: " + serviceId, isDebugEnabled)

			String source = "VID"
			execution.setVariable("DoCVNF_source", source)
			rollbackData.put("VNF", "source", source)
			utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)

			String suppressRollback = execution.getVariable("disableRollback")
			execution.setVariable("DoCVNF_suppressRollback", suppressRollback)
			utils.log("DEBUG", "Incoming Suppress Rollback is: " + suppressRollback, isDebugEnabled)
			
			String modelInvariantId = jsonUtil.getJsonValue(vnfModelInfo, "modelInvariantUuid")
			execution.setVariable("DoCVNF_modelInvariantId", modelInvariantId)
			utils.log("DEBUG", "Incoming Invariant Id is: " + modelInvariantId, isDebugEnabled)
			
			String modelVersionId = jsonUtil.getJsonValue(vnfModelInfo, "modelUuid")
			if (modelVersionId == null) {
				modelVersionId = ""
			}
			execution.setVariable("DoCVNF_modelVersionId", modelVersionId)
			utils.log("DEBUG", "Incoming Version Id is: " + modelVersionId, isDebugEnabled)

			String modelVersion = jsonUtil.getJsonValue(vnfModelInfo, "modelVersion")
			execution.setVariable("DoCVNF_modelVersion", modelVersion)
			utils.log("DEBUG", "Incoming Model Version is: " + modelVersion, isDebugEnabled)
			
			String modelName = jsonUtil.getJsonValue(vnfModelInfo, "modelName")
			execution.setVariable("DoCVNF_modelName", modelName)
			utils.log("DEBUG", "Incoming Model Name is: " + modelName, isDebugEnabled)
			
			String modelCustomizationId = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")
			if (modelCustomizationId == null) {				
				modelCustomizationId = ""				
			}
			execution.setVariable("DoCVNF_modelCustomizationId", modelCustomizationId)
			utils.log("DEBUG", "Incoming Model Customization Id is: " + modelCustomizationId, isDebugEnabled)
				
			String cloudSiteId = execution.getVariable("lcpCloudRegionId")
			execution.setVariable("DoCVNF_cloudSiteId", cloudSiteId)
			rollbackData.put("VNF", "cloudSiteId", cloudSiteId)
			utils.log("DEBUG", "Incoming Cloud Site Id is: " + cloudSiteId, isDebugEnabled)
				
			String tenantId = execution.getVariable("tenantId")
			execution.setVariable("DoCVNF_tenantId", tenantId)
			rollbackData.put("VNF", "tenantId", tenantId)
			utils.log("DEBUG", "Incoming Tenant Id is: " + tenantId, isDebugEnabled)			
			
			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			if (globalSubscriberId == null) {
				globalSubscriberId = ""
			}
			execution.setVariable("DoCVNF_globalSubscriberId", globalSubscriberId)
			utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DoCVNF_sdncVersion", sdncVersion)
			utils.log("DEBUG", "Incoming Sdnc Version is: " + sdncVersion, isDebugEnabled)

			//For Completion Handler & Fallout Handler
			String requestInfo =
				"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

			execution.setVariable("DoCVNF_requestInfo", requestInfo)
			//TODO: Orch Status - TBD, will come from SDN-C Response in 1702
			String orchStatus = "Created"
			execution.setVariable("DoCVNF_orchStatus", orchStatus)

			//TODO: Equipment Role - Should come from SDN-C Response in 1702
			String equipmentRole = " "
			execution.setVariable("DoCVNF_equipmentRole", equipmentRole)
			String vnfId = execution.getVariable("testVnfId") // for junits
			if(isBlank(vnfId)){
				vnfId = execution.getVariable("vnfId")
				if (isBlank(vnfId)) {
					vnfId = UUID.randomUUID().toString()
					utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugEnabled)
				}
			}
			execution.setVariable("DoCVNF_vnfId", vnfId)

			// Setting for Sub Flow Calls
			execution.setVariable("DoCVNF_type", "generic-vnf")
			execution.setVariable("GENGS_type", "service-instance")
				
			String sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable("DoCVNF_sdncCallbackUrl", sdncCallbackUrl)
			rollbackData.put("VNF", "sdncCallbackUrl", sdncCallbackUrl)
			utils.logAudit("SDNC Callback URL: " + sdncCallbackUrl)
			logDebug("SDNC Callback URL is: " + sdncCallbackUrl, isDebugEnabled)
			
			VnfResource vnfResource = (VnfResource) execution.getVariable((String)"vnfResourceDecomposition")
			String nfRole = vnfResource.getNfRole()

			execution.setVariable("DoCVNF_nfRole", nfRole)
			logDebug("NF Role is: " + nfRole, isDebugEnabled)
			
			String nfNamingCode = vnfResource.getNfNamingCode()
			execution.setVariable("DoCVNF_nfNamingCode", nfNamingCode)
			logDebug("NF Naming Code is: " + nfNamingCode, isDebugEnabled)
			
			String nfType = vnfResource.getNfType()
			execution.setVariable("DoCVNF_nfType", nfType)
			logDebug("NF Type is: " + nfType, isDebugEnabled)
			
			String nfFunction = vnfResource.getNfFunction()
			execution.setVariable("DoCVNF_nfFunction", nfFunction)
			logDebug("NF Function is: " + nfFunction, isDebugEnabled)			
			
			rollbackData.put("VNF", "rollbackSDNCAssign", "false")
			rollbackData.put("VNF", "rollbackSDNCActivate", "false")
			rollbackData.put("VNF", "rollbackVnfCreate", "false")
			
			execution.setVariable("rollbackData", rollbackData)
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoCreateVnf PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnf PreProcessRequest Process ***", isDebugEnabled)
	}
	
	private Object getVariableEnforced(DelegateExecution execution, String name){
		Object enforced = execution.getVariable(name)
		if(!enforced){
		return "";
		}
		return enforced;
	}
	
	public void prepareCreateGenericVnf (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED DoCreateVnf PrepareCreateGenericVnf Process *** ", isDebugEnabled)
		try {
			//Get Vnf Info
			String vnfId = getVariableEnforced(execution, "DoCVNF_vnfId")
			String vnfName = getVariableEnforced(execution, "DoCVNF_vnfName")
			if (vnfName == null) {
				vnfName = "sdncGenerated"
				utils.log("DEBUG", "Sending a dummy VNF name to AAI - the name will be generated by SDNC: " + vnfName, isDebugEnabled)
			}
			String vnfType = getVariableEnforced(execution, "DoCVNF_vnfType")
			utils.log("DEBUG", "WE ARE HERE:" + vnfType, isDebugEnabled)
			String serviceId = getVariableEnforced(execution, "DoCVNF_serviceId")
			String orchStatus = getVariableEnforced(execution, "DoCVNF_orchStatus")
			String modelInvariantId = getVariableEnforced(execution, "DoCVNF_modelInvariantId")
			String modelVersionId = getVariableEnforced(execution, "DoCVNF_modelVersionId")
			String modelCustomizationId = getVariableEnforced(execution, "DoCVNF_modelCustomizationId")
			// TODO: 1702 Variable
			String equipmentRole = getVariableEnforced(execution, "DoCVNF_equipmentRole")
			String nfType = getVariableEnforced(execution, "DoCVNF_nfType")
			String nfRole = getVariableEnforced(execution, "DoCVNF_nfRole")
			String nfFunction = getVariableEnforced(execution, "DoCVNF_nfFunction")
			String nfNamingCode = getVariableEnforced(execution, "DoCVNF_nfNamingCode")
			
			//Get Service Instance Info
			String serviceInstanceId = getVariableEnforced(execution, "DoCVNF_serviceInstanceId")
			String siRelatedLink = getVariableEnforced(execution, "GENGS_siResourceLink")

			int custStart = siRelatedLink.indexOf("customer/")
			int custEnd = siRelatedLink.indexOf("/service-subscriptions")
			String globalCustId = siRelatedLink.substring(custStart + 9, custEnd)
			int serviceStart = siRelatedLink.indexOf("service-subscription/")
			int serviceEnd = siRelatedLink.indexOf("/service-instances/")
			String serviceType = siRelatedLink.substring(serviceStart + 21, serviceEnd)
			serviceType = UriUtils.decode(serviceType,"UTF-8")

			//Get Namespace
			AaiUtil aaiUtil = new AaiUtil(this)
			def aai_uri = aaiUtil.getNetworkGenericVnfUri(execution)
			String namespace = aaiUtil.getNamespaceFromUri(execution, aai_uri)

			String payload =
					"""<generic-vnf xmlns="${namespace}">
				<vnf-id>${vnfId}</vnf-id>
				<vnf-name>${vnfName}</vnf-name>
				<service-id>${serviceId}</service-id>
				<vnf-type>${vnfType}</vnf-type>
				<prov-status>PREPROV</prov-status>
				<orchestration-status>${orchStatus}</orchestration-status>
				<model-invariant-id>${modelInvariantId}</model-invariant-id>
				<model-version-id>${modelVersionId}</model-version-id>
				<model-customization-id>${modelCustomizationId}</model-customization-id>
				<nf-type>${nfType}</nf-type>
				<nf-role>${nfRole}</nf-role>
				<nf-function>${nfFunction}</nf-function>
				<nf-naming-code>${nfNamingCode}</nf-naming-code>
				<relationship-list>
					<relationship>
               		<related-to>service-instance</related-to>
               		<related-link>${siRelatedLink}</related-link>
               		<relationship-data>
                  		<relationship-key>customer.global-customer-id</relationship-key>
                  		<relationship-value>${globalCustId}</relationship-value>
              		</relationship-data>
               		<relationship-data>
                  		<relationship-key>service-subscription.service-type</relationship-key>
                  		<relationship-value>${serviceType}</relationship-value>
               		</relationship-data>
					<relationship-data>
                  		<relationship-key>service-instance.service-instance-id</relationship-key>
                  		<relationship-value>${serviceInstanceId}</relationship-value>
               		</relationship-data>
            		</relationship>
				</relationship-list>
			</generic-vnf>"""

			execution.setVariable("DoCVNF_genericVnfPayload", payload)

		}catch(Exception ex) {
			utils.log("DEBUG", "Error Occured in DoCreateVnf PrepareCreateGenericVnf Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PrepareCreateGenericVnf Process")
		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnf PrepareCreateGenericVnf Process ***", isDebugEnabled)
	}
	
	public void postProcessCreateGenericVnf (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED DoCreateVnf PostProcessCreateGenericVnf Process *** ", isDebugEnabled)
		try {
			//Get Vnf Info
			String vnfId = execution.getVariable("DoCVNF_vnfId")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("VNF", "vnfId", vnfId)
			rollbackData.put("VNF", "rollbackVnfCreate", "true")
			execution.setVariable("rollbackData", rollbackData)
		}catch(Exception ex) {
			utils.log("DEBUG", "Error Occured in DoCreateVnf PostProcessCreateGenericVnf Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PostProcessCreateGenericVnf Process")
		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnf PostProcessCreateGenericVnf Process ***", isDebugEnabled)
	}
	
	
	public void preProcessSDNCAssignRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
		def vnfId = execution.getVariable("DoCVNF_vnfId")
		def serviceInstanceId = execution.getVariable("DoCVNF_serviceInstanceId")
		logDebug("NEW VNF ID: " + vnfId, isDebugLogEnabled)
		utils.logAudit("NEW VNF ID: " + vnfId)

		try{
			//Build SDNC Request
			
			String assignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("DoCVNF_assignSDNCRequest", assignSDNCRequest)
			logDebug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCAssignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCAssignRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
		try{
			String vnfId = execution.getVariable("DoCVNF_vnfId")
			String serviceInstanceId = execution.getVariable("DoCVNF_serviceInstanceId")

			String activateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "activate")

			execution.setVariable("DoCVNF_activateSDNCRequest", activateSDNCRequest)
			logDebug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing CommitSDNCRequest is: \n"  + activateSDNCRequest)

		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
	}
	
	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){
		
				String uuid = execution.getVariable('testReqId') // for junits
				if(uuid==null){
					uuid = execution.getVariable("DoCVNF_requestId") + "-" +  	System.currentTimeMillis()
				}
				def callbackURL = execution.getVariable("DoCVNF_sdncCallbackUrl")
				def requestId = execution.getVariable("DoCVNF_requestId")
				def serviceId = execution.getVariable("DoCVNF_serviceId")
				def vnfType = execution.getVariable("DoCVNF_vnfType")
				def vnfName = execution.getVariable("DoCVNF_vnfName")
				// Only send vnfName to SDNC if it is not null, otherwise it will get generated by SDNC on Assign
				String vnfNameString = ""
				if (vnfName != null) {
					vnfNameString = """<vnf-name>${vnfName}</vnf-name>"""					
				}
				def tenantId = execution.getVariable("DoCVNF_tenantId")
				def source = execution.getVariable("DoCVNF_source")
				def vnfId = execution.getVariable("DoCVNF_vnfId")
				def cloudSiteId = execution.getVariable("DoCVNF_cloudSiteId")				
				def modelCustomizationId = execution.getVariable("DoCVNF_modelCustomizationId")
				def serviceModelInfo = execution.getVariable("serviceModelInfo")
				def vnfModelInfo = execution.getVariable("vnfModelInfo")
				String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
				String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)				
				def globalSubscriberId = execution.getVariable("DoCVNF_globalSubscriberId")
				def sdncVersion = execution.getVariable("DoCVNF_sdncVersion")
						
				String sdncVNFParamsXml = ""
		
				if(execution.getVariable("DoCVNF_vnfParamsExistFlag") == true){
					sdncVNFParamsXml = buildSDNCParamsXml(execution)
				}else{
					sdncVNFParamsXml = ""
				}
		
				String sdncRequest =
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>CreateVnfInstance</request-action>
			<source>${source}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id>${serviceId}</service-id>
			<subscription-service-type>${serviceId}</subscription-service-type>
			${serviceEcompModelInformation}			
			<service-instance-id>${svcInstId}</service-instance-id>
			<global-customer-id>${globalSubscriberId}</global-customer-id>			
		</service-information>
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type>${vnfType}</vnf-type>
			${vnfEcompModelInformation}			
		</vnf-information>
		<vnf-request-input>			
			${vnfNameString}
			<tenant>${tenantId}</tenant>		
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>			
			${sdncVNFParamsXml}
		</vnf-request-input>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
			utils.logAudit("sdncRequest:  " + sdncRequest)
			return sdncRequest
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		logDebug(" *** STARTED ValidateSDNCResponse Process*** ", isDebugLogEnabled)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		utils.logAudit("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		utils.logAudit("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logDebug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse, isDebugLogEnabled)
			if(method.equals("get")){
				String topologyGetResponse = execution.getVariable("DoCVNF_getSDNCAdapterResponse")
				String data = utils.getNodeXml(topologyGetResponse, "response-data")
				data = data.replaceAll("&lt;", "<")
				data = data.replaceAll("&gt;", ">")
				utils.log("DEBUG", "topologyGetResponseData: " + data, isDebugLogEnabled)
				String vnfName = utils.getNodeText1(data, "vnf-name")
				utils.log("DEBUG", "vnfName received from SDNC: " + vnfName, isDebugLogEnabled)
				execution.setVariable("vnfName", vnfName)
				execution.setVariable("DoCVNF_vnfName", vnfName)
			}
			def rollbackData = execution.getVariable("rollbackData")
			if (method.equals("assign")) {
				rollbackData.put("VNF", "rollbackSDNCAssign", "true")
			}
			else if (method.equals("activate")) {
				rollbackData.put("VNF", "rollbackSDNCActivate", "true")
			}
			execution.setVariable("rollbackData", rollbackData)

			
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCGetRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		utils.log("DEBUG", " ======== STARTED preProcessSDNCGetRequest Process ======== ", isDebugLogEnabled)
		try{
			def serviceInstanceId = execution.getVariable('DoCVNF_serviceInstanceId')
			
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}
					
			def callbackUrl = execution.getVariable("DoCVFM_sdncCallbackUrl")
			utils.logAudit("callbackUrl:" + callbackUrl)
			
			def vnfId = execution.getVariable('DCVFM_vnfId')
			
			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vnfId
			}
			else {
				svcInstId = serviceInstanceId
			}
			// serviceOperation will be retrieved from "object-path" element
			// in SDNC Assign Response for VNF
			String response = execution.getVariable("DoCVNF_assignSDNCAdapterResponse")
			utils.logAudit("DoCVNF_assignSDNCAdapterResponse is: \n" + response)
			
			String serviceOperation = ""
			
			String data = utils.getNodeXml(response, "response-data")
			data = data.replaceAll("&lt;", "<")
			data = data.replaceAll("&gt;", ">")
			utils.log("DEBUG", "responseData: " + data, isDebugLogEnabled)
			serviceOperation = utils.getNodeText1(data, "object-path")
			utils.log("DEBUG", "VNF with sdncVersion of 1707 or later - service operation: " + serviceOperation, isDebugLogEnabled)			
			

			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
											xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>vfmodule</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			utils.logAudit("SDNCGetRequest: \n" + SDNCGetRequest)
			execution.setVariable("DoCVNF_getSDNCRequest", SDNCGetRequest)
			utils.log("DEBUG", "Outgoing GetSDNCRequest is: \n" + SDNCGetRequest, isDebugLogEnabled)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occurred Processing preProcessSDNCGetRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED preProcessSDNCGetRequest Process ======== ", isDebugLogEnabled)
	}
	
	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
 
		try {
			def vnfId = execution.getVariable('DoCVNF_vnfId')
			logDebug("VNF ID: " + vnfId, isDebugLogEnabled)		
 
			String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${vnfId}</vnf-id>
						<orchestration-status>Active</orchestration-status>						
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DoCVNF_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)
 
 
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}
 


	
}
