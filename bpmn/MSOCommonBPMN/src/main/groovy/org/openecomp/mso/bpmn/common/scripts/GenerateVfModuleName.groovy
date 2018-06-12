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

import org.openecomp.mso.bpmn.core.UrnPropertiesReader

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.web.util.UriUtils

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.logger.MessageEnum
import org.openecomp.mso.logger.MsoLogger

public class GenerateVfModuleName extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenerateVfModuleName.class);

	def Prefix="GVFMN_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	
	
	public void preProcessRequest(DelegateExecution execution) {
		try {
			def vnfId = execution.getVariable("vnfId")
			msoLogger.debug("vnfId is " + vnfId)
			def vnfName = execution.getVariable("vnfName")
			msoLogger.debug("vnfName is " + vnfName)
			def vfModuleLabel = execution.getVariable("vfModuleLabel")
			msoLogger.debug("vfModuleLabel is " + vfModuleLabel)
			def personaModelId = execution.getVariable("personaModelId")
			msoLogger.debug("personaModelId is " + personaModelId)
			execution.setVariable("GVFMN_vfModuleXml", "")
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in initVariables method!")
		}
	}


	public void queryAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.queryAAI(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			def personaModelId = execution.getVariable('personaModelId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			msoLogger.debug('AAI URI is: ' + aai_uri)

			String endPoint = UrnPropertiesReader.getVariable("aai.endpoint", execution) + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			msoLogger.debug("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				msoLogger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				APIResponse response = client.httpGet()
				msoLogger.debug("GenerateVfModuleName - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					msoLogger.debug("Received generic VNF data: " + responseData)

				}

				msoLogger.debug("GenerateVfModuleName - queryAAIVfModule Response: " + responseData)
				msoLogger.debug("GenerateVfModuleName - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('GVFMN_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('GVFMN_queryAAIVfModuleResponse', responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
				if (response.getStatusCode() == 200) {
					// Set the VfModuleXML					
					if (responseData != null) {						
						String vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						if (vfModulesText == null || vfModulesText.isEmpty()) {
							msoLogger.debug("There are no VF modules in this VNF yet")
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
							msoLogger.debug("Matching VF Modules: " + matchingVfModules)					
							execution.setVariable("GVFMN_vfModuleXml", matchingVfModules)
						}
					}
				}	
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAI(): ' + e.getMessage())
		}
		
	}
					
	public void generateName (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.generateName() ' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
	
		String vfModuleXml = execution.getVariable("GVFMN_vfModuleXml")		
		
		String moduleIndex = utils.getLowestUnusedIndex(vfModuleXml)			
		msoLogger.debug("moduleIndex is: " + moduleIndex)
		def vnfName = execution.getVariable("vnfName")
		def vfModuleLabel = execution.getVariable("vfModuleLabel")
		def vfModuleName = vnfName + "_" + vfModuleLabel + "_" + moduleIndex
		msoLogger.debug("vfModuleName is: " + vfModuleName)
		execution.setVariable("vfModuleName", vfModuleName)
	}
}
