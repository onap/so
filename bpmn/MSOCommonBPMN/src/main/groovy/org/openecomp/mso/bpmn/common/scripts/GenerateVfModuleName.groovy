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
package org.openecomp.mso.bpmn.common.scripts
import java.io.Serializable

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.springframework.web.util.UriUtils

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

public class GenerateVfModuleName extends AbstractServiceTaskProcessor{

	def Prefix="GVFMN_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	
	
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		
		try {
			def vnfId = execution.getVariable("vnfId")
			utils.log("DEBUG", "vnfId is " + vnfId, isDebugEnabled)
			def vnfName = execution.getVariable("vnfName")
			utils.log("DEBUG", "vnfName is " + vnfName, isDebugEnabled)
			def vfModuleLabel = execution.getVariable("vfModuleLabel")
			utils.log("DEBUG", "vfModuleLabel is " + vfModuleLabel, isDebugEnabled)
			def personaModelId = execution.getVariable("personaModelId")
			utils.log("DEBUG", "personaModelId is " + personaModelId, isDebugEnabled)
			execution.setVariable("GVFMN_vfModuleXml", "")
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in initVariables method!")
		}
	}


	public void queryAAI(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.queryAAI(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('vnfId')
			def personaModelId = execution.getVariable('personaModelId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint)
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml')
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("GenerateVfModuleName - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("GenerateVfModuleName - queryAAIVfModule Response: " + responseData)
				utils.logAudit("GenerateVfModuleName - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('GVFMN_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('GVFMN_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				if (response.getStatusCode() == 200) {
					// Set the VfModuleXML					
					if (responseData != null) {						
						String vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						if (vfModulesText == null || vfModulesText.isEmpty()) {
							logDebug("There are no VF modules in this VNF yet", isDebugLogEnabled)
							execution.setVariable("GVFMN_vfModuleXml", null)
						}
						else {
							def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
							def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
							int vfModulesSize = 0
							if (vfModules != null) {
								vfModulesSize = vfModules.size()
							}
							String matchingVfModules = "<vfModules>"
							for (i in 0..vfModulesSize-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
								def personaModelIdFromAAI = utils.getNodeText(vfModuleXml, "model-invariant-id")
								if (!personaModelIdFromAAI) {
									// check old attribute name
								   personaModelIdFromAAI = utils.getNodeText(vfModuleXml, "persona-model-id")								  
								}
								if (personaModelIdFromAAI != null && personaModelIdFromAAI.equals(personaModelId)) {
									matchingVfModules = matchingVfModules + utils.removeXmlPreamble(vfModuleXml)
								}							
							}
							matchingVfModules = matchingVfModules + "</vfModules>"
							logDebug("Matching VF Modules: " + matchingVfModules, isDebugLogEnabled)					
							execution.setVariable("GVFMN_vfModuleXml", matchingVfModules)
						}
					}
				}	
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAI(): ' + e.getMessage())
		}
		
	}
					
	public void generateName (Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.generateName() ' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)
	
		String vfModuleXml = execution.getVariable("GVFMN_vfModuleXml")		
		
		String moduleIndex = utils.getLowestUnusedIndex(vfModuleXml)			
		logDebug("moduleIndex is: " + moduleIndex, isDebugLogEnabled)
		def vnfName = execution.getVariable("vnfName")
		def vfModuleLabel = execution.getVariable("vfModuleLabel")
		def vfModuleName = vnfName + "_" + vfModuleLabel + "_" + moduleIndex
		logDebug("vfModuleName is: " + vfModuleName, isDebugLogEnabled)
		execution.setVariable("vfModuleName", vfModuleName)
	}
}
