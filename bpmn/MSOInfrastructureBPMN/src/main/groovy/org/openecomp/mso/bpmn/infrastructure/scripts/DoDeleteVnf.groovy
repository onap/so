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


import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource


/**
 * This class supports the DoDeleteVnf subFlow
 * with the Deletion of a generic vnf for
 * infrastructure.
 *
 */
class DoDeleteVnf extends AbstractServiceTaskProcessor {

	String Prefix="DoDVNF_"
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
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoDeleteVnf PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("DoDVNF_SuccessIndicator", false)
		execution.setVariable("DoDVNF_vnfInUse", false)

		try{
			// Get Variables

			String vnfId = execution.getVariable("vnfId")
			execution.setVariable("DoDVNF_vnfId", vnfId)
			utils.log("DEBUG", "Incoming Vnf(Instance) Id is: " + vnfId, isDebugEnabled)

			// Setting for sub flow calls
			execution.setVariable("DoDVNF_type", "generic-vnf")
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoDeleteVnf PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoDeleteVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoDeleteVnf PreProcessRequest Process ***", isDebugEnabled)
	}


	public void processGetVnfResponse(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoDeleteVnf processGetVnfResponse Process *** ", isDebugEnabled)
		try {
			String vnf = execution.getVariable("DoDVNF_genericVnf")
			String resourceVersion = utils.getNodeText1(vnf, "resource-version")
			execution.setVariable("DoDVNF_resourceVersion", resourceVersion)

			if(utils.nodeExists(vnf, "relationship")){
				InputSource source = new InputSource(new StringReader(vnf));
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
				Document vnfXml = docBuilder.parse(source)

				NodeList nodeList = vnfXml.getElementsByTagName("relationship")
				for (int x = 0; x < nodeList.getLength(); x++) {
					Node node = nodeList.item(x)
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) node
						def e = eElement.getElementsByTagName("related-to").item(0).getTextContent()
						if(e.equals("volume-group") || e.equals("l3-network")){
							utils.log("DEBUG", "Generic Vnf still has relationship to OpenStack.", isDebugEnabled)
							execution.setVariable("DoDVNF_vnfInUse", true)
						}else{
							utils.log("DEBUG", "Relationship NOT related to OpenStack", isDebugEnabled)
						}
					}
				}
			}

			if(utils.nodeExists(vnf, "vf-module")){
				execution.setVariable("DoDVNF_vnfInUse", true)
				utils.log("DEBUG", "Generic Vnf still has vf-modules.", isDebugEnabled)
			}


		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in DoDeleteVnf processGetVnfResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoDeleteVnf processGetVnfResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED DoDeleteVnf processGetVnfResponse Process ***", isDebugEnabled)
	}



}
