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
package org.openecomp.mso.bpmn.infrastructure.scripts

import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;

import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException

/**
 * This class supports the macro VID Flow
 * with the deletion of a generic vnf and related VF modules.
 */
class DoDeleteVnfAndModules extends AbstractServiceTaskProcessor {

	String Prefix="DDVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)	

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *	
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoDeleteVnfAndModules PreProcessRequest Process*** ", isDebugEnabled)

		try{
			// Get Variables				
			
			String cloudConfiguration = execution.getVariable("cloudConfiguration")			
			
			String requestId = execution.getVariable("requestId")
			execution.setVariable("mso-request-id", requestId)
			utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

			String vnfId = execution.getVariable("vnfId")			
			utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)			
			
			String source = "VID"
			execution.setVariable("source", source)
			utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)
			
			execution.setVariable("DDVAM_moduleCount", 0)
			execution.setVariable("DDVAM_nextModule", 0)
			
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnfAndModules PreProcessRequest Process ***", isDebugEnabled)
	}	

	
	
	public void preProcessAddOnModule(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessAddOnModule ======== ", isDebugLogEnabled)
		
		try {			
			JSONArray addOnModules = (JSONArray) execution.getVariable("addOnModules")
			int addOnIndex = (int) execution.getVariable("addOnModulesDeployed")
			
			JSONObject addOnModule = addOnModules[addOnIndex]
			
			def newVfModuleId = UUID.randomUUID().toString()
			execution.setVariable("addOnVfModuleId", newVfModuleId)
			
			execution.setVariable("instancesOfThisModelDeployed", 0)
			
			JSONObject addOnVfModuleModelInfoObject = jsonUtil.getJsonValueForKey(addOnModule, "modelInfo")
			String addOnVfModuleModelInfo = addOnVfModuleModelInfoObject.toString()
			execution.setVariable("addOnVfModuleModelInfo", addOnVfModuleModelInfo)
			String addOnVfModuleLabel = jsonUtil.getJsonValueForKey(addOnModule, "vfModuleLabel")
			execution.setVariable("addOnVfModuleLabel", addOnVfModuleLabel)
			String addOnPersonaModelId = jsonUtil.getJsonValueForKey(addOnVfModuleModelInfoObject, "modelInvariantId")
			execution.setVariable("addOnPersonaModelId", addOnPersonaModelId)
			String addOnInitialCount = jsonUtil.getJsonValueForKey(addOnModule, "initialCount")
			execution.setVariable("initialCount", addOnInitialCount)
					
		
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.queryAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DvnfId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DCVFM_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
				List<Map<String,String>> vfModulesList = new ArrayList<Map<String,String>>();
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logDebug('Parsing the VNF data to find base module info', isDebugLogEnabled)
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
						def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
						execution.setVariable("DDVAM_moduleCount", vfModules.size())
						int vfModulesSize = 0
						for (i in 0..vfModules.size()-1) {
							def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							
							Map<String, String> vfModuleEntry = new HashMap<String, String>()
							def vfModuleId = utils.getNodeText1(vfModuleXml, "vf-module-id")
							vfModuleEntry.put("vfModuleId", vfModuleName)
							def vfModuleName = utils.getNodeText1(vfModuleXml, "vf-module-name")
							vfModuleEntry.put("vfModuleName", vfModuleName)		
							vfModulesList.add(vfModuleEntry)					
						}
						
					}					
				}
				execution.setVariable("DDVAM_vfModules", vfModules)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}
	
	public void prepareNextModuleToDelete(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED prepareNextModuleToDelete ======== ", isDebugLogEnabled)
		
		try {
			int i = execution.getVariable("DDVAM_nextModule")
			def vfModules = execution.getVariable("DDVAM_vfModules")
			def vfModule = vfModules[i]
			
			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DDVAM_vfModuleId", vfModuleId)
			
			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DDVAM_vfModuleName", vfModuleName)
			
			
			// HARDCODED FOR NOW
			def vfModuleModelInfo = ""
			execution.setVariable("DDVAM_vfModuleModelInfo", vfModuleModelInfo)			
			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	
	
	
}
