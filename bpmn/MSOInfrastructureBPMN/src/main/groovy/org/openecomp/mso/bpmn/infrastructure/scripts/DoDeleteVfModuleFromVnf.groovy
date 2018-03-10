package org.openecomp.mso.bpmn.infrastructure.scripts

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VfModule
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import static org.apache.commons.lang3.StringUtils.*

public class DoDeleteVfModuleFromVnf extends VfModuleBase {

	def Prefix="DDVFMV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DDVFMV_contrailNetworkPolicyFqdnList", null)
	}

	// parse the incoming request
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		initProcessVariables(execution)

		try {
			
				// Building Block-type request

				// Set mso-request-id to request-id for VNF Adapter interface
				String requestId = execution.getVariable("msoRequestId")
				execution.setVariable("mso-request-id", requestId)
				execution.setVariable("requestId", requestId)
				utils.log("DEBUG", "msoRequestId: " + requestId, isDebugEnabled)
				String tenantId = execution.getVariable("tenantId")
				utils.log("DEBUG", "tenantId: " + tenantId, isDebugEnabled)				
				String cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("cloudSiteId", cloudSiteId)
				utils.log("DEBUG", "cloudSiteId: " + cloudSiteId, isDebugEnabled)
				// Source is HARDCODED
				String source = "VID"
				execution.setVariable("source", source)
				// isVidRequest is hardcoded to "true"
				execution.setVariable("isVidRequest", "true")
				// SrvInstId is hardcoded to empty
				execution.setVariable("srvInstId", "")
				// ServiceId is hardcoded to empty
				execution.setVariable("serviceId", "")
				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				utils.log("DEBUG", "serviceInstanceId: " + serviceInstanceId, isDebugEnabled)
				String vnfId = execution.getVariable("vnfId")
				utils.log("DEBUG", "vnfId: " + vnfId, isDebugEnabled)
				String vfModuleId = execution.getVariable("vfModuleId")
				utils.log("DEBUG", "vfModuleId: " + vfModuleId, isDebugEnabled)
				if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
					execution.setVariable(Prefix + "serviceInstanceIdToSdnc", vfModuleId)
				}
				else {
					execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceId)
				}				
				
				String sdncVersion = execution.getVariable("sdncVersion")
				if (sdncVersion == null) {
					sdncVersion = "1707"
				}
				execution.setVariable(Prefix + "sdncVersion", sdncVersion)
				utils.log("DEBUG", "Incoming Sdnc Version is: " + sdncVersion, isDebugEnabled)				
				
				String sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
					logError(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
				utils.logAudit("SDNC Callback URL: " + sdncCallbackUrl)
				utils.log("DEBUG:", "SDNC Callback URL is: " + sdncCallbackUrl, isDebugEnabled)

			
			
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Exception is: " + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in PreProcess method!")
		}
	}
	
	public void queryAAIForVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.queryAAIForVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('vnfId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"

			utils.logAudit("DoDeleteVfModuleFromVnf: AAI endPoint  : " + endPoint)

			try {
				utils.logAudit("DoDeleteVfModuleFromVnf: - invoking httpGet to AAI")
				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)

				def responseData = response.getResponseBodyAsString()
				execution.setVariable('DDVMFV_getVnfResponseCode', response.getStatusCode())
				execution.setVariable('DDVMFV_getVnfResponse', responseData)

				utils.logAudit("DoDeleteVfModuleFromVnf: AAI Response : " + responseData)
				utils.logAudit("DoDeleteVfModuleFromVnf: AAI ResponseCode : " + response.getStatusCode())

				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)			
				
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(), isDebugLogEnabled)
				execution.setVariable('DDVMFV_getVnfResponseCode', 500)
				execution.setVariable('DDVFMV_getVnfResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIForVfModule(): ' + e.getMessage())
		}
	}
	
	/**
	 * Validate the VF Module.  That is, confirm that a VF Module with the input VF Module ID
	 * exists in the retrieved Generic VNF.  Then, check to make sure that if that VF Module
	 * is the base VF Module and it's not the only VF Module for this Generic VNF, that we're not
	 * attempting to delete it.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void validateVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.validateVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def genericVnf = execution.getVariable('DDVMFV_getVnfResponse')
			def vnfId = execution.getVariable('_vnfId')
			def vfModuleId = execution.getVariable('vfModuleId')			
			def VfModule vfModule = findVfModule(genericVnf, vfModuleId)
			if (vfModule == null) {
				def String msg = 'VF Module \'' + vfModuleId + '\' does not exist in Generic VNF \'' + vnfId + '\''
				logDebug(msg, isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, msg)
			} else {
				
				if (isDebugLogEnabled) {
					logDebug('VF Module \'' + vfModuleId + '\': isBaseVfModule=' + vfModule.isBaseVfModule() +
						', isOnlyVfModule=' + vfModule.isOnlyVfModule(),
						isDebugLogEnabled)
				}
				if (vfModule.isBaseVfModule() && !vfModule.isOnlyVfModule()) {
					def String msg = 'Cannot delete VF Module \'' + vfModuleId +
						'\'since it is the base VF Module and it\'s not the only VF Module in Generic VNF \'' + vnfId + '\''
						logDebug("Received a BAD Response from VNF Adapter for CREATE_VF_MODULE Call.", isDebugLogEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
				}
				
				def heatStackId = vfModule.getElementText('heat-stack-id')
				execution.setVariable('DDVMFV_heatStackId', heatStackId)
				logDebug('VF Module heatStackId retrieved from AAI: ' + heatStackId, isDebugLogEnabled)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in validateVfModule(): ' + e.getMessage())
		}
	}


	public void preProcessSDNCDeactivateRequest(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
		
		def serviceInstanceId = execution.getVariable("serviceInstanceId")
	
		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")
	
			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DDVMFV_deactivateSDNCRequest", deactivateSDNCRequest)
			logDebug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCDeactivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
	}

	public void preProcessSDNCUnassignRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
		try{			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
	
			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")
	
			execution.setVariable("DDVMFV_unassignSDNCRequest", unassignSDNCRequest)
			logDebug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)
	
		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
	}

	public String buildSDNCRequest(Execution execution, String svcInstId, String action){
	
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("msoRequestId") + "-" +  	System.currentTimeMillis()
			}
			def callbackURL = execution.getVariable("sdncCallbackUrl")	
			def requestId = execution.getVariable("msoRequestId")
			def serviceId = execution.getVariable("serviceId")
			def serviceInstanceId = execution.getVariable("serviceInstanceId")
			def vfModuleId = execution.getVariable("vfModuleId")
			def source = execution.getVariable("source")
			def vnfId = execution.getVariable("vnfId")
				
			def sdncVersion = execution.getVariable(Prefix + "sdncVersion")
			
			String sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>DeleteVfModuleInstance</request-action>
			<source>${source}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>			
			<service-instance-id>${serviceInstanceId}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type/>			
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${vfModuleId}</vf-module-id>
		</vf-module-information>
		<vf-module-request-input/>		
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
	
		utils.logAudit("sdncRequest:  " + sdncRequest)
		return sdncRequest
	}
	
	public void validateSDNCResponse(Execution execution, String response, String method){
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
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}


	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def requestId = UUID.randomUUID().toString()
		def origRequestId = execution.getVariable('requestId')
		def srvInstId = execution.getVariable("serviceInstanceId")
		def aicCloudRegion = execution.getVariable("cloudSiteId")
		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		def vfModuleStackId = execution.getVariable('DDVMFV_heatStackId')
		def tenantId = execution.getVariable("tenantId")
		def messageId = execution.getVariable('requestId') + '-' +
			System.currentTimeMillis()
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String request = """
			<deleteVfModuleRequest>
			    <cloudSiteId>${aicCloudRegion}</cloudSiteId>
			    <tenantId>${tenantId}</tenantId>
			    <vnfId>${vnfId}</vnfId>
			    <vfModuleId>${vfModuleId}</vfModuleId>
			    <vfModuleStackId>${vfModuleStackId}</vfModuleStackId>
			    <skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${origRequestId}</requestId>
			        <serviceInstanceId>${srvInstId}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${messageId}</messageId>
			    <notificationUrl>${notificationUrl}</notificationUrl>
			</deleteVfModuleRequest>
			""" as String

		utils.log("DEBUG", "vnfAdapterRestV1Request: " + request, isDebugEnabled)
		utils.logAudit("deleteVfModuleRequest: " + request)
		execution.setVariable("vnfAdapterRestV1Request", request)
	}

	
	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("ERROR", "AAI error occurred deleting the Generic Vnf: "
			+ execution.getVariable("DDVFMV_deleteGenericVnfResponse"), isDebugEnabled)
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable("DDVFMV_deleteGenericVnfResponse"))
		execution.setVariable("WorkflowException", exception)
	}
	
	public void postProcessVNFAdapterRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix",Prefix)
		try{
		logDebug(" *** STARTED postProcessVNFAdapterRequest Process*** ", isDebugLogEnabled)
		
		String vnfResponse = execution.getVariable("DDVMFV_doDeleteVfModuleResponse")
		logDebug("VNF Adapter Response is: " + vnfResponse, isDebugLogEnabled)
		utils.logAudit("deleteVnfAResponse is: \n"  + vnfResponse)

		if(vnfResponse != null){

			if(vnfResponse.contains("deleteVfModuleResponse")){
				logDebug("Received a Good Response from VNF Adapter for DELETE_VF_MODULE Call.", isDebugLogEnabled)
				execution.setVariable("DDVFMV_vnfVfModuleDeleteCompleted", true)

				// Parse vnfOutputs for contrail network polcy FQDNs
				def vfModuleOutputsXml = utils.getNodeXml(vnfResponse, "vfModuleOutputs")
				if(!isBlank(vfModuleOutputsXml)) {
					vfModuleOutputsXml = utils.removeXmlNamespaces(vfModuleOutputsXml)
					List contrailNetworkPolicyFqdnList = []
					for(Node node: utils.getMultNodeObjects(vfModuleOutputsXml, "entry")) {
						String key = utils.getChildNodeText(node, "key")
						if(key == null) {
							
						} else if (key.endsWith("contrail_network_policy_fqdn")) {
							String contrailNetworkPolicyFqdn = utils.getChildNodeText(node, "value")
							logDebug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn, isDebugLogEnabled)
							contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
						}
						else if (key.equals("oam_management_v4_address")) {
							String oamManagementV4Address = utils.getChildNodeText(node, "value")
							logDebug("Obtained oamManagementV4Address: " + oamManagementV4Address, isDebugLogEnabled)
							execution.setVariable(Prefix + "oamManagementV4Address", oamManagementV4Address)
						}
						else if (key.equals("oam_management_v6_address")) {
							String oamManagementV6Address = utils.getChildNodeText(node, "value")
							logDebug("Obtained oamManagementV6Address: " + oamManagementV6Address, isDebugLogEnabled)
							execution.setVariable(Prefix + "oamManagementV6Address", oamManagementV6Address)
						}
					}
					if (!contrailNetworkPolicyFqdnList.isEmpty()) {
						logDebug("Setting the fqdn list", isDebugLogEnabled)
						execution.setVariable("DDVFMV_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
					}
				}
			}else{
				logDebug("Received a BAD Response from VNF Adapter for DELETE_VF_MODULE Call.", isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
			}
		}else{
			logDebug("Response from VNF Adapter is Null for DELETE_VF_MODULE Call.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
		}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logDebug("Internal Error Occured in PostProcess Method", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
		}
		logDebug(" *** COMPLETED postProcessVnfAdapterResponse Process*** ", isDebugLogEnabled)
	}

	public void deleteNetworkPoliciesFromAAI(Execution execution) {
		def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED deleteNetworkPoliciesFromAAI ======== ", isDebugLogEnabled)

		try {
			// get variables
			List fqdnList = execution.getVariable("DDVFMV_contrailNetworkPolicyFqdnList")
			if (fqdnList == null) {
				logDebug("No network policies to delete", isDebugLogEnabled)
				return
			}
			int fqdnCount = fqdnList.size()

			execution.setVariable("DDVFMV_networkPolicyFqdnCount", fqdnCount)
			logDebug("DDVFMV_networkPolicyFqdnCount - " + fqdnCount, isDebugLogEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkPolicyUri(execution)

			if (fqdnCount > 0) {
				// AII loop call over contrail network policy fqdn list
				for (i in 0..fqdnCount-1) {

					int counting = i+1
					String fqdn = fqdnList[i]

					// Query AAI for this network policy FQDN

					String queryNetworkPolicyByFqdnAAIRequest = "${aai_endpoint}${aai_uri}?network-policy-fqdn=" + UriUtils.encode(fqdn, "UTF-8")
					utils.logAudit("AAI request endpoint: " + queryNetworkPolicyByFqdnAAIRequest)
					logDebug("AAI request endpoint: "  + queryNetworkPolicyByFqdnAAIRequest, isDebugLogEnabled)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyByFqdnAAIRequest)
					int returnCode = response.getStatusCode()
					execution.setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", returnCode)
					logDebug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugLogEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (isOneOf(returnCode, 200, 201)) {
						logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						// This network policy FQDN exists in AAI - need to delete it now
						utils.logAudit(aaiResponseAsString)
						execution.setVariable("DDVFMV_queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
						logDebug("QueryAAINetworkPolicyByFQDN Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString, isDebugLogEnabled)
						// Retrieve the network policy id for this FQDN
						def networkPolicyId = utils.getNodeText1(aaiResponseAsString, "network-policy-id")
						logDebug("Deleting network-policy with network-policy-id " + networkPolicyId, isDebugLogEnabled)

						// Retrieve the resource version for this network policy
						def resourceVersion = utils.getNodeText1(aaiResponseAsString, "resource-version")
						logDebug("Deleting network-policy with resource-version " + resourceVersion, isDebugLogEnabled)

						String delNetworkPolicyAAIRequest = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(networkPolicyId, "UTF-8") +
							"?resource-version=" + UriUtils.encode(resourceVersion, "UTF-8")
						utils.logAudit("AAI request endpoint: " + delNetworkPolicyAAIRequest)
						logDebug("AAI request endpoint: " + delNetworkPolicyAAIRequest, isDebugLogEnabled)

						logDebug("invoking DELETE call to AAI", isDebugLogEnabled)
						utils.logAudit("Sending DELETE call to AAI with Endpoint /n" + delNetworkPolicyAAIRequest)
						APIResponse responseDel = aaiUriUtil.executeAAIDeleteCall(execution, delNetworkPolicyAAIRequest)
						int returnCodeDel = responseDel.getStatusCode()
						execution.setVariable("DDVFMV_aaiDeleteNetworkPolicyReturnCode", returnCodeDel)
						logDebug(" ***** AAI delete network policy Response Code, NetworkPolicy #" + counting + " : " + returnCodeDel, isDebugLogEnabled)

						if (isOneOf(returnCodeDel, 200, 201, 204)) {
							logDebug("The return code from deleting network policy is: "  + returnCodeDel, isDebugLogEnabled)
							// This network policy was deleted from AAI successfully
							logDebug(" DelAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : ", isDebugLogEnabled)

						} else {
								// aai all errors
								String delErrorMessage = "Unable to delete network-policy to AAI deleteNetworkPoliciesFromAAI - " + returnCodeDel
							 logDebug(delErrorMessage, isDebugLogEnabled)
							 exceptionUtil.buildAndThrowWorkflowException(execution, 2500, delErrorMessage)
						}
					} else if (returnCode == 404) {
						// This network policy FQDN is not in AAI. No need to delete.
						logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						logDebug("This network policy FQDN is not in AAI: " + fqdn, isDebugLogEnabled)
						utils.logAudit("Network policy FQDN is not in AAI")
					} else {
					   if (aaiResponseAsString.contains("RESTFault")) {
						   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						   execution.setVariable("WorkflowException", exceptionObject)
						   throw new BpmnError("MSOWorkflowException")

						   } else {
								// aai all errors
								String dataErrorMessage = "Unexpected Response from deleteNetworkPoliciesFromAAI - " + returnCode
								logDebug(dataErrorMessage, isDebugLogEnabled)
								exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						  }
					}



				} // end loop


			} else {
				   logDebug("No contrail network policies to query/create", isDebugLogEnabled)

			}

		} catch (BpmnError e) {
			throw e

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoDeletVfModule flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
			logDebug(exceptionMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}	
	
	// and formulate the outgoing DeleteAAIVfModuleRequest request
	public void prepDeleteAAIVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "DeleteAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("DeleteAAIVfModuleRequest: " + request)
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}
}