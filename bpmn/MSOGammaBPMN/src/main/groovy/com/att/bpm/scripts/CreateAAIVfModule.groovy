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

package com.att.bpm.scripts
import java.io.Serializable;

import org.camunda.bpm.engine.runtime.Execution
import org.springframework.web.util.UriUtils

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException


public class CreateAAIVfModule extends AbstractServiceTaskProcessor{
	
	def Prefix="CAAIVfMod_"
	
	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("CAAIVfMod_vnfId",null)
		execution.setVariable("CAAIVfMod_vnfName",null)
		execution.setVariable("CAAIVfMod_vnfType",null)
		execution.setVariable("CAAIVfMod_serviceId",null)
		execution.setVariable("CAAIVfMod_personaId",null)
		execution.setVariable("CAAIVfMod_personaVer",null)
		execution.setVariable("CAAIVfMod_vnfPersonaId",null)
		execution.setVariable("CAAIVfMod_vnfPersonaVer",null)
		execution.setVariable("CAAIVfMod_isBaseVfModule", false)
		execution.setVariable("CAAIVfMod_moduleName",null)
		execution.setVariable("CAAIVfMod_moduleModelName",null)
		execution.setVariable("CAAIVfMod_newGenericVnf",false)
		execution.setVariable("CAAIVfMod_genericVnfGetEndpoint",null)
		execution.setVariable("CAAIVfMod_genericVnfPutEndpoint",null)
		execution.setVariable("CAAIVfMod_aaiNamespace",null)
		execution.setVariable("CAAIVfMod_moduleExists",false)
		execution.setVariable("CAAIVfMod_baseModuleConflict", false)
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", null)
		
		
		// CreateAAIVfModule workflow response variable placeholders
		execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_queryGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_createGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_createGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_createVfModuleResponseCode",null)
		execution.setVariable("CAAIVfMod_createVfModuleResponse","")
		execution.setVariable("CAAIVfMod_parseModuleResponse","")
		execution.setVariable("CAAIVfMod_deleteGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_deleteGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_deleteVfModuleResponseCode",null)
		execution.setVariable("CAAIVfMod_deleteVfModuleResponse","")
//		execution.setVariable("CAAIVfMod_ResponseCode",null)
//		execution.setVariable("CAAIVfMod_ErrorResponse","")
		execution.setVariable("CreateAAIVfModuleResponse","")
		execution.setVariable("RollbackData", null)

	}	
	
	// parse the incoming CREATE_VF_MODULE request and store the Generic VNF
	// and VF Module data in the flow Execution
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("CreateAAIVfModuleRequest")
		utils.log("DEBUG", "input request xml:" + xml, isDebugEnabled)
		initProcessVariables(execution)

		def vnfId = ""
		if (utils.nodeExists(xml, "vnf-id")) {
			vnfId = utils.getNodeText(xml,"vnf-id")
		}
		if (vnfId.isEmpty()) {
			execution.setVariable("CAAIVfMod_newGenericVnf", true)
		}
		execution.setVariable("CAAIVfMod_vnfId",vnfId)

		def vnfName = ""
		if (utils.nodeExists(xml, "vnf-name")) {
			vnfName = utils.getNodeText(xml,"vnf-name")
		}
		execution.setVariable("CAAIVfMod_vnfName", vnfName)

		if (utils.nodeExists(xml, "vnf-type")) {
			execution.setVariable("CAAIVfMod_vnfType",utils.getNodeText(xml,"vnf-type"))
		} else {
			execution.setVariable("CAAIVfMod_vnfType","")
		}

		execution.setVariable("CAAIVfMod_serviceId",utils.getNodeText(xml,"service-id"))

		if (utils.nodeExists(xml, "persona-model-id")) {
			execution.setVariable("CAAIVfMod_personaId",utils.getNodeText(xml,"persona-model-id"))
		} else {
			execution.setVariable("CAAIVfMod_personaId","")
		}

		if (utils.nodeExists(xml, "persona-model-version")) {
			execution.setVariable("CAAIVfMod_personaVer",utils.getNodeText(xml,"persona-model-version"))
		} else {
			execution.setVariable("CAAIVfMod_personaVer","")
		}
		
		if (utils.nodeExists(xml, "vnf-persona-model-id")) {
			execution.setVariable("CAAIVfMod_vnfPersonaId",utils.getNodeText(xml,"vnf-persona-model-id"))
		} else {
			execution.setVariable("CAAIVfMod_vnfPersonaId","")
		}

		if (utils.nodeExists(xml, "vnf-persona-model-version")) {
			execution.setVariable("CAAIVfMod_vnfPersonaVer",utils.getNodeText(xml,"vnf-persona-model-version"))
		} else {
			execution.setVariable("CAAIVfMod_vnfPersonaVer","")
		}
		
		//isBaseVfModule
		Boolean isBaseVfModule = false
		if (utils.nodeExists(xml, "is-base-vf-module")) {
			String isBaseVfModuleString = utils.getNodeText(xml, "is-base-vf-module")
			if (isBaseVfModuleString.equals("true")) {
				isBaseVfModule = true
			}
		}
		execution.setVariable("CAAIVfMod_isBaseVfModule", isBaseVfModule)
		
		String isVidRequest = execution.getVariable("isVidRequest")
		if (isVidRequest != null && "true".equals(isVidRequest)) {
			logDebug("VID Request received", isDebugEnabled)		
		}

		execution.setVariable("CAAIVfMod_moduleName",utils.getNodeText(xml,"vf-module-name"))
		execution.setVariable("CAAIVfMod_moduleModelName",utils.getNodeText(xml,"vf-module-model-name"))

		AaiUtil aaiUriUtil = new AaiUtil(this)
		def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
		logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)
		String aaiNamespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
		logDebug('AAI namespace is: ' + aaiNamespace, isDebugEnabled)
		
		execution.setVariable("CAAIVfMod_aaiNamespace","${aaiNamespace}")
		
	
		if (vnfId.isEmpty()) {
			// TBD - assert that the vnfName is not empty
			execution.setVariable("CAAIVfMod_genericVnfGetEndpoint",
				"${aai_uri}/?vnf-name=" +
					UriUtils.encode(vnfName,"UTF-8"))
		} else {
			execution.setVariable("CAAIVfMod_genericVnfGetEndpoint",
				"${aai_uri}/" + UriUtils.encode(vnfId,"UTF-8"))
		}

		
		execution.setVariable("CAAIVfMod_genericVnfPutEndpoint","${aai_uri}/")
	}
	
	// send a GET request to AA&I to retrieve the Generic VNF/VF Module information based on a Vnf Name
	// expect a 200 response with the information in the response body or a 404 if the Generic VNF does not exist
	public void queryAAIForGenericVnf(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("CAAIVfMod_genericVnfGetEndpoint")
		def aaiRequestId = UUID.randomUUID().toString()

		RESTConfig config = new RESTConfig(endPoint);
		utils.log("DEBUG","queryAAIForGenericVnf() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""

		String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").addHeader("Content-Type", "application/xml").
				addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			utils.log("DEBUG", "invoking GET call to AAI endpoint :"+System.lineSeparator()+endPoint,isDebugEnabled)
			APIResponse response = client.httpGet()
				
			responseData = response.getResponseBodyAsString()
			execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("CAAIVfMod_queryGenericVnfResponse", responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)
		} catch (Exception ex) {
			ex.printStackTrace()
			utils.log("DEBUG", "Exception occurred while executing AAI GET:" + ex.getMessage(),isDebugEnabled)
			execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode", 500)
			execution.setVariable("CAAIVfMod_queryGenericVnfResponse", "AAI GET Failed:" + ex.getMessage())
		}
	}
	
	// process the result from queryAAIForGenericVnf()
	// note: this method is primarily for logging as the actual decision logic is embedded in the bpmn flow 
	public void processAAIGenericVnfQuery(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def result = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
		
		if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
			execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			utils.log("DEBUG", "New Generic VNF requested and it does not already exist", isDebugEnabled)
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 200 &&
				!execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			utils.log("DEBUG", "Adding module to existing Generic VNF", isDebugEnabled)	
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 200 &&
				execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			utils.log("DEBUG", "Invalid request for new Generic VNF which already exists", isDebugEnabled)
			execution.setVariable("CAAIVfMod_queryGenericVnfResponse",
				"Invalid request for new Generic VNF which already exists, Vnf Name=" +
				 execution.getVariable("CAAIVfMod_vnfName"))	
		} else { // execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
			   // !execution.getVariable("CAAIVfMod_vnfId").isEmpty())
			utils.log("DEBUG", "Invalid request for Add-on Module requested for non-existant Generic VNF", isDebugEnabled)	
			execution.setVariable("CAAIVfMod_createVfModuleResponse",
				"Invalid request for Add-on Module requested for non-existant Generic VNF, VNF Id=" +
				execution.getVariable("CAAIVfMod_vnfId"))
		}
	}

	// construct and send a PUT request to A&AI to create a new Generic VNF
	// note: to get here, the vnf-id in the original CREATE_VF_MODULE request was absent or ""
	public void createGenericVnf(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def aaiRequestId = UUID.randomUUID().toString()
		// TBD - is this how we want to generate the Id for the new Generic VNF?
		def newVnfId = UUID.randomUUID().toString()
		def endPoint = execution.getVariable("URN_aai_endpoint") +
			execution.getVariable("CAAIVfMod_genericVnfPutEndpoint") + newVnfId
		// update the flow execution with the new Vnf Id
		execution.setVariable("CAAIVfMod_vnfId",newVnfId)

		String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

	//	AaiUriUtil aaiUriUtil = new AaiUriUtil(this)
	//	def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
	//	logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)
	//	String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
	//	logDebug('AAI namespace is: ' + namespace, isDebugEnabled)
		
				
		String payload = """<generic-vnf xmlns="${execution.getVariable("CAAIVfMod_aaiNamespace")}">
								<vnf-id>${newVnfId}</vnf-id>
								<vnf-name>${execution.getVariable("CAAIVfMod_vnfName")}</vnf-name>
								<vnf-type>${execution.getVariable("CAAIVfMod_vnfType")}</vnf-type>
								<service-id>${execution.getVariable("CAAIVfMod_serviceId")}</service-id>
								<orchestration-status>active</orchestration-status>
								<persona-model-id>${execution.getVariable("CAAIVfMod_vnfPersonaId")}</persona-model-id>
								<persona-model-version>${execution.getVariable("CAAIVfMod_vnfPersonaVer")}</persona-model-version>
							</generic-vnf>""" as String
		execution.setVariable("CAAIVfMod_createGenericVnfPayload", payload)

		RESTConfig config = new RESTConfig(endPoint);
		utils.log("DEBUG","createGenericVnf() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").addHeader("Content-Type", "application/xml").
				addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			utils.log("DEBUG", "invoking PUT call to AAI with payload:"+System.lineSeparator()+payload,isDebugEnabled)
			APIResponse response = client.httpPut(payload)
				
			responseData = response.getResponseBodyAsString()
			execution.setVariable("CAAIVfMod_createGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("CAAIVfMod_createGenericVnfResponse", responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)
		} catch (Exception ex) {
			ex.printStackTrace()
			utils.log("DEBUG", "Exception occurred while executing AAI PUT:" + ex.getMessage(),isDebugEnabled)
			execution.setVariable("CAAIVfMod_createGenericVnfResponseCode", 500)
			execution.setVariable("CAAIVfMod_createGenericVnfResponse", "AAI PUT Failed:" + ex.getMessage())
		}		
	}

	// construct and send a PUT request to A&AI to create a Base or Add-on VF Module
	public void createVfModule(Execution execution, Boolean isBaseModule) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		// TBD - is this how we want to generate the Id for the new (Base) VF Module?
		
		// Generate the new VF Module ID here if it has not been provided by the parent process
		def newModuleId = execution.getVariable('newVfModuleId')
		if (newModuleId == null || newModuleId.isEmpty()) {
			newModuleId = UUID.randomUUID().toString()
		}
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("CAAIVfMod_genericVnfPutEndpoint")
		// need to append the existing Vnf Id or the one generated in createGenericVnf() to the url
		endPoint = endPoint + UriUtils.encode(execution.getVariable("CAAIVfMod_vnfId"), "UTF-8") +
			"/vf-modules/vf-module/" + newModuleId;
		def aaiRequestId = UUID.randomUUID().toString()

		String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

		// if we get to this point, we may be about to create the Vf Module,
		// add rollback information about the Generic VNF for this base/add-on module
		def rollbackData = execution.getVariable("RollbackData")
		if (rollbackData == null) {
			rollbackData = new RollbackData();
		}
		rollbackData.put("VFMODULE", "vnfId", execution.getVariable("CAAIVfMod_vnfId"))
		rollbackData.put("VFMODULE", "vnfName", execution.getVariable("CAAIVfMod_vnfName"))
		rollbackData.put("VFMODULE", "isBaseModule", isBaseModule.toString())
		execution.setVariable("RollbackData", rollbackData)
		utils.log("DEBUG", "RollbackData:" + rollbackData, isDebugEnabled)
		String payload = """<vf-module xmlns="${execution.getVariable("CAAIVfMod_aaiNamespace")}">
								<vf-module-id>${newModuleId}</vf-module-id>
								<vf-module-name>${execution.getVariable("CAAIVfMod_moduleName")}</vf-module-name>
								<persona-model-id>${execution.getVariable("CAAIVfMod_personaId")}</persona-model-id>
								<persona-model-version>${execution.getVariable("CAAIVfMod_personaVer")}</persona-model-version>
								<is-base-vf-module>${isBaseModule}</is-base-vf-module>
								<orchestration-status>pending-create</orchestration-status>
								</vf-module>""" as String
		execution.setVariable("CAAIVfMod_createVfModulePayload", payload)

		RESTConfig config = new RESTConfig(endPoint);
		utils.log("DEBUG","createVfModule() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").addHeader("Content-Type", "application/xml").
				addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			utils.log("DEBUG", "invoking PUT call to AAI with payload:"+System.lineSeparator()+payload,isDebugEnabled)
			APIResponse response = client.httpPut(payload)
				
			responseData = response.getResponseBodyAsString()
			execution.setVariable("CAAIVfMod_createVfModuleResponseCode", response.getStatusCode())
			execution.setVariable("CAAIVfMod_createVfModuleResponse", responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)

			// the base or add-on VF Module was successfully created,
			// add the module name to the rollback data and the response
			if (isOneOf(response.getStatusCode(), 200, 201)) {
				rollbackData.put("VFMODULE", "vfModuleId", newModuleId)
				rollbackData.put("VFMODULE", "vfModuleName", execution.getVariable("CAAIVfMod_moduleName"))
				execution.setVariable("RollbackData", rollbackData)
				utils.log("DEBUG", "RollbackData:" + rollbackData, isDebugEnabled)
				
				String responseOut = ""
				
				String isVidRequest = execution.getVariable("isVidRequest")
				
				if (isBaseModule && (isVidRequest == null || "false".equals(isVidRequest))) {				
				
					responseOut = """<CreateAAIVfModuleResponse>
											<vnf-id>${execution.getVariable("CAAIVfMod_vnfId")}</vnf-id>
											<vf-module-id>${newModuleId}</vf-module-id>
										</CreateAAIVfModuleResponse>""" as String
				}
				else {
					responseOut = """<CreateAAIVfModuleResponse>
											<vnf-name>${execution.getVariable("CAAIVfMod_vnfNameFromAAI")}</vnf-name>
											<vnf-id>${execution.getVariable("CAAIVfMod_vnfId")}</vnf-id>
											<vf-module-id>${newModuleId}</vf-module-id>
										</CreateAAIVfModuleResponse>""" as String
				}
				
				execution.setVariable("CreateAAIVfModuleResponse", responseOut)
				utils.log("DEBUG", "CreateAAIVfModuleResponse:" + System.lineSeparator()+responseOut,isDebugEnabled)
			}
		} catch (Exception ex) {
			ex.printStackTrace()
			utils.log("DEBUG", "Exception occurred while executing AAI PUT:" + ex.getMessage(),isDebugEnabled)
			execution.setVariable("CAAIVfMod_createVfModuleResponseCode", 500)
			execution.setVariable("CAAIVfMod_createVfModuleResponse", "AAI PUT Failed:" + ex.getMessage())
		}				
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the vf-module-name
	// requested for an Add-on VF Module does not already exist for the specified Generic VNF
	// also retrieves VNF name from AAI response for existing VNF
	public void parseForAddOnModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
		def vnfNameFromAAI = utils.getNodeText1(xml, "vnf-name")
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", vnfNameFromAAI)
		utils.log("DEBUG", "Obtained vnf-name from AAI for existing VNF: " + vnfNameFromAAI)	
		def newModuleName = execution.getVariable("CAAIVfMod_moduleName")
		utils.log("DEBUG", "VF Module to be added: " + newModuleName, isDebugEnabled)
		def qryModuleNameList = utils.getMultNodes(xml, "vf-module-name")
		execution.setVariable("CAAIVfMod_moduleExists", false)
		if (qryModuleNameList != null) {
			utils.log("DEBUG", "Existing VF Module List: " + qryModuleNameList, isDebugEnabled)
			for (String qryModuleName : qryModuleNameList) {
				if (newModuleName.equals(qryModuleName)) {
					// a module with the requested name already exists - failure
					utils.log("DEBUG", "VF Module " + qryModuleName + " already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"), isDebugEnabled)
					execution.setVariable("CAAIVfMod_moduleExists", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"VF Module " + qryModuleName + " already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
					break
				}
			}
		}
		if (execution.getVariable("CAAIVfMod_moduleExists") == false) {
			utils.log("DEBUG", "VF Module " + execution.getVariable("CAAIVfMod_moduleName") +
				" does not exist for Generic VNF " + execution.getVariable("CAAIVfMod_vnfNameFromAAI"), isDebugEnabled)
			execution.setVariable("CAAIVfMod_parseModuleResponse",
				"VF Module " + newModuleName + " does not exist for Generic VNF " +
				execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
		}		
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the vf-module-name
	// requested for an Add-on VF Module does not already exist for the specified Generic VNF; 
	// also retrieves VNF name from AAI response for existing VNF
	public void parseForBaseModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
		def vnfNameFromAAI = utils.getNodeText1(xml, "vnf-name")
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", vnfNameFromAAI)
		utils.log("DEBUG", "Obtained vnf-name from AAI for existing VNF: " + vnfNameFromAAI)	
		def newModuleName = execution.getVariable("CAAIVfMod_moduleName")
		utils.log("DEBUG", "VF Module to be added: " + newModuleName, isDebugEnabled)
		def qryModuleNameList = utils.getMultNodes(xml, "vf-module-name")
		execution.setVariable("CAAIVfMod_moduleExists", false)
		if (qryModuleNameList != null) {
			utils.log("DEBUG", "Existing VF Module List: " + qryModuleNameList, isDebugEnabled)
			for (String qryModuleName : qryModuleNameList) {
				if (newModuleName.equals(qryModuleName)) {
					// a module with the requested name already exists - failure
					utils.log("DEBUG", "VF Module " + qryModuleName + " already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"), isDebugEnabled)
					execution.setVariable("CAAIVfMod_baseModuleConflict", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"VF Module " + qryModuleName + " already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
					break
				}
			}
		}
		def isBaseVfModuleList = utils.getMultNodes(xml, "is-base-vf-module")
		if (isBaseVfModuleList != null && !execution.getVariable("CAAIVfMod_baseModuleConflict")) {
			
			for (String baseValue : isBaseVfModuleList) {
				if (baseValue.equals("true")) {
					// a base module already exists in this VNF - failure
					utils.log("DEBUG", "Base VF Module already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"), isDebugEnabled)
					execution.setVariable("CAAIVfMod_baseModuleConflict", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"Base VF Module already exists for Generic VNF " +
						execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
					break
				}
			}
		
		}
		if (execution.getVariable("CAAIVfMod_moduleExists") == false && execution.getVariable("CAAIVfMod_baseModuleConflict") == false) {
			utils.log("DEBUG", "VF Module " + execution.getVariable("CAAIVfMod_moduleName") +
				" does not exist for Generic VNF " + execution.getVariable("CAAIVfMod_vnfNameFromAAI"), isDebugEnabled)
			execution.setVariable("CAAIVfMod_parseModuleResponse",
				"VF Module " + newModuleName + " does not exist for Generic VNF " +
				execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
		}		
	}
	
	// generates a WorkflowException when the A&AI query returns a response code other than 200 or 404
	public void handleAAIQueryFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		
		utils.log("ERROR", "Error occurred attempting to query AAI, Response Code " +
			execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") + ", Error Response " +
			execution.getVariable("CAAIVfMod_queryGenericVnfResponse"), isDebugEnabled)
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable("CAAIVfMod_queryGenericVnfResponse"))
		execution.setVariable("WorkflowException", exception)
	}
	
	// generates a WorkflowException if
	//		- the A&AI Generic VNF PUT returns a response code other than 200 or 201
	//		- the requested Generic VNF already exists but vnf-id == null
	//		- the requested Generic VNF does not exist but vnf-id != null
	// 		- the A&AI VF Module PUT returns a response code other than 200 or 201
	//		- the requested VF Module already exists for the Generic VNF
	public void handleCreateVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		
		def errorCode
		def errorResponse
		if (execution.getVariable("CAAIVfMod_createGenericVnfResponseCode") != null &&
				!isOneOf(execution.getVariable("CAAIVfMod_createGenericVnfResponseCode"), 200, 201)) {
			utils.log("DEBUG", "Failure creating Generic VNF: " +
				execution.getVariable("CAAIVfMod_createGenericVnfResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_createGenericVnfResponse")
			errorCode = 5000
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponse") != null &&
				execution.getVariable("CAAIVfMod_newGenericVnf") == true) {
			// attempted to create a Generic VNF that already exists but vnf-id == null
			utils.log("DEBUG", execution.getVariable("CAAIVfMod_queryGenericVnfResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
				execution.getVariable("CAAIVfMod_newGenericVnf") == false) {
			// attempted to create a Generic VNF where vnf-name does not exist but vnf-id != null
			utils.log("DEBUG", execution.getVariable("CAAIVfMod_queryGenericVnfResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_createVfModuleResponseCode") != null) {
			utils.log("DEBUG", "Failed to add VF Module: " +
			execution.getVariable("CAAIVfMod_createVfModuleResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_createVfModuleResponse")
			errorCode = 5000
		} else if (execution.getVariable("CAAIVfMod_moduleExists") == true) {
			utils.log("DEBUG", "Attempting to add VF Module that already exists: " +
				execution.getVariable("CAAIVfMod_parseModuleResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_parseModuleResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_baseModuleConflict") == true) {
			utils.log("DEBUG", "Attempting to add Base VF Module to VNF that already has a Base VF Module: " +
				execution.getVariable("CAAIVfMod_parseModuleResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("CAAIVfMod_parseModuleResponse")
			errorCode = 1002
		} else {
			// if the responses get populated corerctly, we should never get here
			errorResponse = "Unknown error occurred during CreateAAIVfModule flow"
			errorCode = 2000
		}

		utils.log("ERROR", "Error occurred during CreateAAIVfModule flow: " + errorResponse, isDebugEnabled)
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, errorCode, errorResponse)
		execution.setVariable("WorkflowException", exception)
	}

	/**
	 * Performs a rollback.
	 * TBD: This method requires additional testing once integrated with the
	 *      main CreateVfModule flow.
	 * @param execution the execution
	 */
	public void rollback(Execution execution) {
		def method = getClass().getSimpleName() + ".rollback(" +
			"execution=" + execution.getId() +
			")"
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		logDebug("Entered " + method, isDebugLogEnabled)

		try {
			RollbackData rollbackData = (RollbackData) execution.getVariable("RollbackData")
			logDebug("RollbackData:" + rollbackData, isDebugLogEnabled)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			
			if (rollbackData != null) {
				if (rollbackData.hasType("VFMODULE")) {
					// use the DeleteAAIVfModule groovy methods for the rollback
					def vnfId = rollbackData.get("VFMODULE", "vnfId")
					def vfModuleId = rollbackData.get("VFMODULE", "vfModuleId")
					def isBaseModule = rollbackData.get("VFMODULE", "isBaseModule")
					execution.setVariable("DAAIVfMod_vnfId", vnfId)		
					execution.setVariable("DAAIVfMod_vfModuleId", vfModuleId)
					execution.setVariable("DAAIVfMod_genericVnfEndpoint", "${aai_uri}/" + vnfId)
					execution.setVariable("DAAIVfMod_vfModuleEndpoint", "${aai_uri}/" + vnfId +
						 "/vf-modules/vf-module/" + vfModuleId)
					DeleteAAIVfModule dvm = new DeleteAAIVfModule()
					// query A&AI to get the needed information for the delete(s)
					dvm.queryAAIForGenericVnf(execution)
					dvm.parseForVfModule(execution)
					
					// roll back the base or add-on module
					dvm.deleteVfModule(execution)
					def responseCode = execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode")
					def response = execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode")

					if (isOneOf(responseCode, 200, 204)) {
						logDebug("Received " + responseCode + " to VF Module rollback request", isDebugLogEnabled)
//						execution.setVariable("RollbackResult", "SUCCESS")
					} else {
						logError("Received " + responseCode + " to VF Module rollback request: " + rollbackData +
							System.lineSeparator() + "Response: " + response)
					}
					
					// a new Generic VNF was created that needs to be rolled back
					if (isBaseModule.equals("true")) {
						dvm.deleteGenericVnf(execution)
						responseCode = execution.getVariable("DAAIVfMod_deleteGenericVnfResponseCode")
						response = execution.getVariable("DAAIVfMod_deleteGenericVnfResponse")
	
						if (isOneOf(responseCode, 200, 204)) {
							logDebug("Received " + responseCode + " to Generic VNF rollback request", isDebugLogEnabled)
							execution.setVariable("RollbackResult", "SUCCESS")
						} else {
							logError("Received " + responseCode + " to Generic VNF rollback request: " + rollbackData +
								System.lineSeparator() + "Response: " + response)
						}
					} else {
						execution.setVariable("RollbackResult", "SUCCESS")
					}
				}
			}

			logDebug("Exited " + method, isDebugLogEnabled)
		} catch (Exception e) {
			logError("Caught exception in " + method, e)
		}
	}
}
