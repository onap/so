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

package org.onap.so.bpmn.infrastructure.scripts


import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

/**
 * This class supports the DoDeleteVnf subFlow
 * with the Deletion of a generic vnf for
 * infrastructure.
 *
 */
class DoDeleteVnf extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVnf.class);

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

		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED DoDeleteVnf PreProcessRequest Process")

		execution.setVariable("DoDVNF_SuccessIndicator", false)
		execution.setVariable("DoDVNF_vnfInUse", false)

		try{
			// Get Variables

			String vnfId = execution.getVariable("vnfId")
			execution.setVariable("DoDVNF_vnfId", vnfId)
			msoLogger.debug("Incoming Vnf(Instance) Id is: " + vnfId)

			// Setting for sub flow calls
			execution.setVariable("DoDVNF_type", "generic-vnf")
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error Occured in DoDeleteVnf PreProcessRequest method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoDeleteVnf PreProcessRequest")

		}
		msoLogger.trace("COMPLETED DoDeleteVnf PreProcessRequest Process ")
	}


	public void processGetVnfResponse(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED DoDeleteVnf processGetVnfResponse Process ")
		try {
			String vnf = execution.getVariable("DoDVNF_genericVnf")
			String resourceVersion = utils.getNodeText(vnf, "resource-version")
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
							msoLogger.debug("Generic Vnf still has relationship to OpenStack.")
							execution.setVariable("DoDVNF_vnfInUse", true)
						}else{
							msoLogger.debug("Relationship NOT related to OpenStack")
						}
					}
				}
			}

			if(utils.nodeExists(vnf, "vf-module")){
				execution.setVariable("DoDVNF_vnfInUse", true)
				msoLogger.debug("Generic Vnf still has vf-modules.")
			}


		} catch (Exception ex) {
			msoLogger.debug("Error Occured in DoDeleteVnf processGetVnfResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoDeleteVnf processGetVnfResponse Process")

		}
		msoLogger.trace("COMPLETED DoDeleteVnf processGetVnfResponse Process ")
	}

	/**
	 * Deletes the generic vnf from aai
	 */
	public void deleteVnf(DelegateExecution execution) {
		msoLogger.trace("STARTED deleteVnf")
		try {
			String vnfId = execution.getVariable("DoDVNF_vnfId")

			AAIResourcesClient resourceClient = new AAIResourcesClient();
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			resourceClient.delete(uri)

			msoLogger.trace("COMPLETED deleteVnf")
		} catch (Exception ex) {
			msoLogger.debug("Error Occured in DoDeleteVnf deleteVnf Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoDeleteVnf deleteVnf Process")
		}
	}

}
