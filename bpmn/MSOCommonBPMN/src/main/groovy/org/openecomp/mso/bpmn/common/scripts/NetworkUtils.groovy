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

import org.apache.commons.lang3.*

import groovy.xml.XmlUtil

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.TransformerException
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.w3c.dom.Document
import org.w3c.dom.Element

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource


/**
 * This groovy class supports the any Network processes that need the methods defined here.
 */
class NetworkUtils {

	public MsoUtils utils = new MsoUtils()
	private AbstractServiceTaskProcessor taskProcessor

	public NetworkUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	/**
	 * This method returns the string for Network request
	 * V2 for Contrail 3.x will populate cloud-region data in same cloudSiteId filed
	 * Network adapter will handle it properly
	 * @param requestId either 'request-id' or 'mso-request-id'
	 * @param requestInput the request in the process
	 * @param queryIdResponse the response of REST AAI query by Id
	 * @param routeCollection the collection
	 * @param policyFqdns the policy
	 * @param tableCollection the collection
	 * @param cloudRegionId the cloud-region-region
	 * @return String request
	 */
	def CreateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyFqdns, tableCollection, cloudRegionId, backoutOnFailure, source) {
		String createNetworkRequest = null
		if(requestInput!=null && queryIdResponse!=null) {
				String serviceInstanceId = ""
				String sharedValue = ""
				String externalValue = ""

				if (source == "VID") {
					sharedValue = utils.getNodeText1(queryIdResponse, "is-shared-network") != null ? utils.getNodeText1(queryIdResponse, "is-shared-network") : "false"
					externalValue = utils.getNodeText1(queryIdResponse, "is-external-network") != null ? utils.getNodeText1(queryIdResponse, "is-external-network") : "false"
					serviceInstanceId = utils.getNodeText1(requestInput, "service-instance-id")

				} else { // source = 'PORTAL'
					sharedValue = getParameterValue(requestInput, "shared")
					externalValue = getParameterValue(requestInput, "external")
					serviceInstanceId = utils.getNodeText1(requestInput, "service-instance-id") != null ? utils.getNodeText1(requestInput, "service-instance-id") : ""
				}

				String networkParams = ""
				if (utils.nodeExists(requestInput, "network-params")) {
					String netParams = utils.getNodeXml(requestInput, "network-params", false).replace("tag0:","").replace(":tag0","")
					networkParams = buildParams(netParams)
				}

				String failIfExists = "false"
				// requestInput
				String cloudRegion = cloudRegionId
				String tenantId = utils.getNodeText1(requestInput, "tenant-id")

				String networkType = ""
				String modelCustomizationUuid = ""
				if (utils.nodeExists(requestInput, "networkModelInfo")) {
					String networkModelInfo = utils.getNodeXml(requestInput, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
					networkType = utils.getNodeText1(networkModelInfo, "modelName")
					modelCustomizationUuid = utils.getNodeText1(networkModelInfo, "modelCustomizationUuid")
				} else {
					networkType = utils.getNodeText1(queryIdResponse, "network-type")
					modelCustomizationUuid = utils.getNodeText1(requestInput, "modelCustomizationId")
				}

				// queryIdResponse
				String networkName = utils.getNodeText1(queryIdResponse, "network-name")
				String networkId = utils.getNodeText1(queryIdResponse, "network-id")
				String networkTechnology = utils.getNodeText1(queryIdResponse, "network-technology")

				// contrailNetwork - networkTechnology = 'Contrail' vs. 'AIC_SR_IOV')
				String contrailNetwork = ""
				if (networkTechnology.contains('Contrail') || networkTechnology.contains('contrail') || networkTechnology.contains('CONTRAIL')) {
					contrailNetwork = """<contrailNetwork>
					                       <shared>${sharedValue}</shared>
					                       <external>${externalValue}</external>
					                       ${routeCollection}
					                       ${policyFqdns}
					                       ${tableCollection}
				                         </contrailNetwork>"""
					networkTechnology = "CONTRAIL"    // replace
			    }

				// rebuild subnets
				String subnets = ""
				if (utils.nodeExists(queryIdResponse, "subnets")) {
					def subnetsGroup = utils.getNodeXml(queryIdResponse, "subnets", false)
					subnets = buildSubnets(subnetsGroup)
				}

				String physicalNetworkName = ""
				physicalNetworkName = utils.getNodeText1(queryIdResponse, "physical-network-name")

				String vlansCollection = buildVlans(queryIdResponse)

				String notificationUrl = ""                                   //TODO - is this coming from URN? What variable/value to use?
				//String notificationUrl = execution.getVariable("URN_?????") //TODO - is this coming from URN? What variable/value to use?

				createNetworkRequest = """
								<createNetworkRequest>
									<cloudSiteId>${cloudRegion}</cloudSiteId>
									<tenantId>${tenantId}</tenantId>
									<networkId>${networkId}</networkId>
									<networkName>${networkName}</networkName>
									<networkType>${networkType}</networkType>
									<modelCustomizationUuid>${modelCustomizationUuid}</modelCustomizationUuid>
									<networkTechnology>${networkTechnology}</networkTechnology>
									<providerVlanNetwork>
										<physicalNetworkName>${physicalNetworkName}</physicalNetworkName >
										${vlansCollection}
									</providerVlanNetwork>
                                    ${contrailNetwork}
									${subnets}
									<skipAAI>true</skipAAI>
									<backout>${backoutOnFailure}</backout>
									<failIfExists>${failIfExists}</failIfExists>
									${networkParams}
									<msoRequest>
										<requestId>${requestId}</requestId>
										<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
			      					</msoRequest>
									<messageId>${messageId}</messageId>
									<notificationUrl>${notificationUrl}</notificationUrl>
								</createNetworkRequest>
								""".trim()
		}
		return createNetworkRequest

	}

	/**
	 * This method returns the string for Network request
	 * V2 for Contrail 3.x will populate cloud-region data in same cloudSiteId filed
	 * Network adapter will handle it properly
	 * @param requestId either 'request-id' or 'mso-request-id'
	 * @param requestInput the request in the process
	 * @param queryIdResponse the response of REST AAI query by Id
	 * @param routeCollection the collection
	 * @param policyFqdns the policy
	 * @param cloudRegionId the cloud-region-region
	 * @return String request
	 */
	def UpdateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyFqdns, tableCollection, cloudRegionId, backoutOnFailure, source) {
		String updateNetworkRequest = null
		if(requestInput!=null && queryIdResponse!=null) {
				String serviceInstanceId = ""
				String sharedValue = ""
				String externalValue = ""

				if (source == "VID") {
					sharedValue = utils.getNodeText1(queryIdResponse, "is-shared-network") != null ? utils.getNodeText1(queryIdResponse, "is-shared-network") : "false"
					externalValue = utils.getNodeText1(queryIdResponse, "is-external-network") != null ? utils.getNodeText1(queryIdResponse, "is-external-network") : "false"
					serviceInstanceId = utils.getNodeText1(requestInput, "service-instance-id")

				} else { // source = 'PORTAL'
					sharedValue = getParameterValue(requestInput, "shared")
					externalValue = getParameterValue(requestInput, "external")
					serviceInstanceId = utils.getNodeText1(requestInput, "service-instance-id") != null ? utils.getNodeText1(requestInput, "service-instance-id") : ""
				}

				String failIfExists = "false"
				// requestInput
				String cloudRegion = cloudRegionId
				String tenantId = utils.getNodeText1(requestInput, "tenant-id")

				// queryIdResponse
				String networkName = utils.getNodeText1(queryIdResponse, "network-name")
				String networkId = utils.getNodeText1(queryIdResponse, "network-id")
				
				String networkType = ""
				String modelCustomizationUuid = ""
				if (utils.nodeExists(requestInput, "networkModelInfo")) {
					String networkModelInfo = utils.getNodeXml(requestInput, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
					networkType = utils.getNodeText1(networkModelInfo, "modelName")
					modelCustomizationUuid = utils.getNodeText1(networkModelInfo, "modelCustomizationUuid")
				} else {
					networkType = utils.getNodeText1(queryIdResponse, "network-type")
					modelCustomizationUuid = utils.getNodeText1(requestInput, "modelCustomizationId")
				}

				// rebuild subnets
				String subnets = ""
				if (utils.nodeExists(queryIdResponse, "subnets")) {
					def subnetsGroup = utils.getNodeXml(queryIdResponse, "subnets", false)
					subnets = buildSubnets(subnetsGroup)
				}

				String networkParams = ""
				if (utils.nodeExists(requestInput, "network-params")) {
					String netParams = utils.getNodeXml(requestInput, "network-params", false).replace("tag0:","").replace(":tag0","")
					networkParams = buildParams(netParams)
				}

				String networkStackId = utils.getNodeText1(queryIdResponse, "heat-stack-id")
				if (networkStackId == 'null' || networkStackId == "" || networkStackId == null) {
					networkStackId = "force_update"
				}

				String physicalNetworkName = utils.getNodeText1(queryIdResponse, "physical-network-name")
				String vlansCollection = buildVlans(queryIdResponse)

				updateNetworkRequest =
                         """<updateNetworkRequest>
								<cloudSiteId>${cloudRegion}</cloudSiteId>
								<tenantId>${tenantId}</tenantId>
								<networkId>${networkId}</networkId>
						        <networkStackId>${networkStackId}</networkStackId>
								<networkName>${networkName}</networkName>
								<networkType>${networkType}</networkType>
								<modelCustomizationUuid>${modelCustomizationUuid}</modelCustomizationUuid>
								<networkTypeVersion/>
								<networkTechnology>CONTRAIL</networkTechnology>
								<providerVlanNetwork>
									<physicalNetworkName>${physicalNetworkName}</physicalNetworkName>
									${vlansCollection}
								</providerVlanNetwork>
								<contrailNetwork>
									<shared>${sharedValue}</shared>
									<external>${externalValue}</external>
									${routeCollection}
									${policyFqdns}
									${tableCollection}
								</contrailNetwork>
								${subnets}
								<skipAAI>true</skipAAI>
								<backout>${backoutOnFailure}</backout>
								<failIfExists>${failIfExists}</failIfExists>
									${networkParams}

								<msoRequest>
								  <requestId>${requestId}</requestId>
								  <serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
								</msoRequest>
						 		<messageId>${messageId}</messageId>
						 		<notificationUrl></notificationUrl>
 							</updateNetworkRequest>""".trim()

		}
		return updateNetworkRequest

	}

	/**
	 * This method returns the string for Create Volume Request payload
	 * @param groupId the volume-group-id
	 * @param volumeName the volume-group-name
	 * @param vnfType the vnf-type
	 * @param tenantId the value of relationship-key 'tenant.tenant-id'
	 * @return String request payload
	 */
	def String CreateNetworkVolumeRequest(groupId, volumeName, vnfType, tenantId) {

		String requestPayload =
		"""<volume-group xmlns="http://org.openecomp.mso/v6">
				<volume-group-id>${groupId}</volume-group-id>
				<volume-group-name>${volumeName}</volume-group-name>
				<heat-stack-id></heat-stack-id>
				<vnf-type>${vnfType}</vnf-type>
				<orchestration-status>Pending</orchestration-status>
				<relationship-list>
				   <relationship>
					   <related-to>tenant</related-to>
					   <relationship-data>
						   <relationship-key>tenant.tenant-id</relationship-key>
						   <relationship-value>${tenantId}</relationship-value>
					   </relationship-data>
				   </relationship>
			   </relationship-list>
		   </volume-group>"""

		return requestPayload
	}

	def String createCloudRegionVolumeRequest(groupId, volumeName, vnfType, tenantId, cloudRegion, namespace, modelCustomizationId) {

				String requestPayload =
				"""<volume-group xmlns="${namespace}">
				<volume-group-id>${groupId}</volume-group-id>
				<volume-group-name>${volumeName}</volume-group-name>
				<heat-stack-id></heat-stack-id>
				<vnf-type>${vnfType}</vnf-type>
				<orchestration-status>Pending</orchestration-status>
				<vf-module-model-customization-id>${modelCustomizationId}</vf-module-model-customization-id>
				<relationship-list>
				   <relationship>
					   <related-to>tenant</related-to>
					   <relationship-data>
						   <relationship-key>tenant.tenant-id</relationship-key>
						   <relationship-value>${tenantId}</relationship-value>
					   </relationship-data>
					   <relationship-data>
						   <relationship-key>cloud-region.cloud-owner</relationship-key>
						   <relationship-value>att-aic</relationship-value>
					   </relationship-data>
					   <relationship-data>
						   <relationship-key>cloud-region.cloud-region-id</relationship-key>
						   <relationship-value>${cloudRegion}</relationship-value>
					   </relationship-data>
				   </relationship>
			   </relationship-list>
		   </volume-group>"""

				return requestPayload
			}

	def String createCloudRegionVolumeRequest(groupId, volumeName, vnfType, vnfId, tenantId, cloudRegion, namespace, modelCustomizationId) {

		String requestPayload =
		"""<volume-group xmlns="${namespace}">
			<volume-group-id>${groupId}</volume-group-id>
			<volume-group-name>${volumeName}</volume-group-name>
			<heat-stack-id></heat-stack-id>
			<vnf-type>${vnfType}</vnf-type>
			<orchestration-status>Pending</orchestration-status>
			<vf-module-model-customization-id>${modelCustomizationId}</vf-module-model-customization-id>
			<relationship-list>
				<relationship>
				   <related-to>generic-vnf</related-to>
				   <relationship-data>
					   <relationship-key>generic-vnf.vnf-id</relationship-key>
					   <relationship-value>${vnfId}</relationship-value>
				   </relationship-data>
			   </relationship>
			   <relationship>
				   <related-to>tenant</related-to>
				   <relationship-data>
					   <relationship-key>tenant.tenant-id</relationship-key>
					   <relationship-value>${tenantId}</relationship-value>
				   </relationship-data>
				   <relationship-data>
					   <relationship-key>cloud-region.cloud-owner</relationship-key>
					   <relationship-value>att-aic</relationship-value>
				   </relationship-data>
				   <relationship-data>
					   <relationship-key>cloud-region.cloud-region-id</relationship-key>
					   <relationship-value>${cloudRegion}</relationship-value>
				   </relationship-data>
			   </relationship>
		   </relationship-list>
		</volume-group>"""

		return requestPayload
	}


	/**
	 * This method returns the string for Update Volume Request payload
	 * @param requeryAAIVolGrpNameResponse the response of query volume group name (in AAI)
	  * @param heatStackId the value of heat stack id
	 * @return String request payload
	 */
	def String updateCloudRegionVolumeRequest(requeryAAIVolGrpNameResponse, heatStackId, namespace, modelCustomizationId) {
		String requestPayload = ""
		if (requeryAAIVolGrpNameResponse != null) {
			def groupId = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-id")
			def volumeName = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-name")
			def vnfType = utils.getNodeText(requeryAAIVolGrpNameResponse, "vnf-type")
			def resourceVersion = utils.getNodeText(requeryAAIVolGrpNameResponse, "resource-version")
			def relationshipList = ""
			if (utils.nodeExists(requeryAAIVolGrpNameResponse, "relationship")) {
				relationshipList = rebuildRelationship(requeryAAIVolGrpNameResponse)
			}

			requestPayload =
				"""<volume-group xmlns="${namespace}">
					<volume-group-id>${groupId}</volume-group-id>
					<volume-group-name>${volumeName}</volume-group-name>
					<heat-stack-id>${heatStackId}</heat-stack-id>
					<vnf-type>${vnfType}</vnf-type>
					<orchestration-status>Active</orchestration-status>
					<resource-version>${resourceVersion}</resource-version>
					<vf-module-model-customization-id>${modelCustomizationId}</vf-module-model-customization-id>
					${relationshipList}
				 </volume-group>"""
		}

		return requestPayload
	}


	/**
	 * This method returns the string for Update Volume Request payload
	 * @param requeryAAIVolGrpNameResponse the response of query volume group name (in AAI)
 	 * @param heatStackId the value of heat stack id
	 * @return String request payload
	 */
	def String UpdateNetworkVolumeRequest(requeryAAIVolGrpNameResponse, heatStackId) {
		String requestPayload = ""
		if (requeryAAIVolGrpNameResponse != null) {
			def groupId = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-id")
			def volumeName = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-name")
			def vnfType = utils.getNodeText(requeryAAIVolGrpNameResponse, "vnf-type")
			def resourceVersion = utils.getNodeText(requeryAAIVolGrpNameResponse, "resource-version")
			def relationshipList = ""
			if (utils.nodeExists(requeryAAIVolGrpNameResponse, "relationship")) {
				relationshipList = rebuildRelationship(requeryAAIVolGrpNameResponse)
			}

			requestPayload =
				"""<volume-group xmlns="http://org.openecomp.mso/v6">
					<volume-group-id>${groupId}</volume-group-id>
					<volume-group-name>${volumeName}</volume-group-name>
					<heat-stack-id>${heatStackId}</heat-stack-id>
					<vnf-type>${vnfType}</vnf-type>
					<orchestration-status>Active</orchestration-status>
					<resource-version>${resourceVersion}</resource-version>
					${relationshipList}
				 </volume-group>"""
		}

		return requestPayload
	}

	/**
	 * This method returns the string for Create Contrail Network payload
	 * @param requeryIdAAIResponse the response from AAI query by id
	 * @param createNetworkResponse the response of create network
	 * @return String contrailNetworkCreatedUpdate
	 */
	def ContrailNetworkCreatedUpdate(requeryIdAAIResponse, createNetworkResponse, schemaVersion) {

		String contrailNetworkCreatedUpdate = ""
		if(requeryIdAAIResponse!=null && createNetworkResponse!=null) {

			def l3Network = utils.getNodeXml(requeryIdAAIResponse, "l3-network", false).replace("tag0:","").replace(":tag0","")
			def createNetworkContrailResponse = ""
			if (utils.nodeExists(createNetworkResponse, 'createNetworkResponse')) {
			   createNetworkContrailResponse = utils.getNodeXml(createNetworkResponse, "createNetworkResponse", false).replace("tag0:","").replace(":tag0","")
			} else {
			   createNetworkContrailResponse = utils.getNodeXml(createNetworkResponse, "updateNetworkContrailResponse", false).replace("tag0:","").replace(":tag0","")
			}

			// rebuild network
			def networkList = ["network-id", "network-name", "network-type", "network-role", "network-technology", "neutron-network-id", "is-bound-to-vpn", "service-id", "network-role-instance", "resource-version", "resource-model-uuid", "orchestration-status", "heat-stack-id", "mso-catalog-key", "contrail-network-fqdn",
				                             "physical-network-name", "is-provider-network", "is-shared-network", "is-external-network"]
			String rebuildNetworkElements = buildNetworkElements(l3Network, createNetworkContrailResponse, networkList)

			// rebuild 'subnets'
			def rebuildSubnetList = ""
			if (utils.nodeExists(requeryIdAAIResponse, 'subnet')) {
			     rebuildSubnetList = buildSubnets(requeryIdAAIResponse, createNetworkResponse)
			}

			// rebuild 'segmentation-assignments'
			def rebuildSegmentationAssignments = ""
			if (utils.nodeExists(requeryIdAAIResponse, 'segmentation-assignments')) {
				List elementList = ["segmentation-id", "resource-version"]
				if (utils.nodeExists(requeryIdAAIResponse, 'segmentation-assignment')) {  // new tag
				    rebuildSegmentationAssignments =  buildXMLElements(requeryIdAAIResponse, "segmentation-assignments", "segmentation-assignment", elementList)
				} else {
				   rebuildSegmentationAssignments =  buildXMLElements(requeryIdAAIResponse, "", "segmentation-assignments", elementList)
				}   
			}

			// rebuild 'ctag-assignments' / rebuildCtagAssignments
			def rebuildCtagAssignmentsList = ""
			if (utils.nodeExists(requeryIdAAIResponse, 'ctag-assignment')) {
				rebuildCtagAssignmentsList = rebuildCtagAssignments(requeryIdAAIResponse)
			}

			// rebuild 'relationship'
			def relationshipList = ""
			if (utils.nodeExists(requeryIdAAIResponse, 'relationship-list')) {
				String rootRelationshipData = getFirstNodeXml(requeryIdAAIResponse, "relationship-list").drop(38).trim().replace("tag0:","").replace(":tag0","")
				if (utils.nodeExists(rootRelationshipData, 'relationship')) {
					relationshipList = rebuildRelationship(rootRelationshipData)
				}
			}

			//Check for optional contrail network fqdn within CreateNetworkResponse
			String contrailNetworkFQDN
			if(utils.nodeExists(createNetworkResponse, "contrail-network-fqdn")){
				contrailNetworkFQDN = utils.getNodeXml(createNetworkResponse, "contrail-network-fqdn")
				contrailNetworkFQDN = utils.removeXmlNamespaces(contrailNetworkFQDN)
				contrailNetworkFQDN = utils.removeXmlPreamble(contrailNetworkFQDN)
			}else{
				contrailNetworkFQDN = ""
			}

			contrailNetworkCreatedUpdate =
				 """<l3-network xmlns="${schemaVersion}">
					 	${rebuildNetworkElements}
						${rebuildSubnetList}
						${rebuildSegmentationAssignments}
						${rebuildCtagAssignmentsList}
						${relationshipList}
						${contrailNetworkFQDN}
				      </l3-network>""".trim()

		}
			return contrailNetworkCreatedUpdate
	}



	/**
	 * This method returns the value for the name paramName.
	 *   Ex:   <network-params>
	 *            <param name="shared">1</param>
	 *            <param name="external">0</external>
	 *         </network-params>
	 *
	 * @param xmlInput the XML document
	 * @param paramName the param name (ex: 'shared')
	 * @return a param value for 'shared' (ex: '1')
	 */
	def getParameterValue(xmlInput, paramName) {
		def rtn=""
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			rtn= xml.'**'.find {param->param.'@name'.text() == paramName}
		}
		if (rtn==null) {
			return ""
		} else {
		   return rtn
		}
	}

	/**
	 * This method returns the name of param if found/match with paramName.
	 *   Ex:   <network-params>
	 *            <param name="shared">1</param>
	 *            <param name="external">0</external>
	 *         </network-params>
	 *
	 * @param xmlInput the XML document
	 * @param paramName the param name (ex: 'shared', )
	 * @return a param name for 'shared' (ex: 'shared' if found)
	 */
	def getParameterName(xmlInput, paramName) {
		def rtn=""
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			try {
				rtn= xml.'**'.find {param->param.'@name' == paramName}.'@name'
			} catch (Exception ex) {
			    rtn=""
			}
		}
		if (rtn==null || rtn=="") {
			return ""
		} else {
		   return rtn
		}
	}

	/**
	 * This method returns the networkParams xml string.
	 *   Ex: input:
	 *         <network-params>
	 *            <param name="shared">1</param>
	 *            <param name="external">0</external>
	 *         </network-params>
	 *
	 *   Sample result:
	 *         <networkParams>
     *            <shared>1</shared>
     *            <external>0</external>
     *         </networkParams>
     *
	 */

	def buildParams(networkParams) {
		def build = ""
			def netParams = new XmlParser().parseText(networkParams)
			try {
				def paramsList = netParams.'**'.findAll {param->param.'@name'}.'@name'
				if (paramsList.size() > 0) {
					build += "<networkParams>"
					for (i in 0..paramsList.size()-1) {
                        def name = netParams.'**'.find {param->param.'@name' == paramsList[i]}.'@name'
						def value= netParams.'**'.find {param->param.'@name' == paramsList[i]}.text()
						build += "<${name}>${value}</${name}>"
					}
					build += "</networkParams>"
				}

			} catch (Exception ex) {
				println ' buildParams error - ' + ex.getMessage()
				build = ""
			}
		return build
	}

	def getVlans(xmlInput) {
		def rtn = ""
		if (xmlInput!=null) {
			def vlansList = getListWithElements(xmlInput, 'vlans')
			def vlansListSize = vlansList.size()
			if (vlansListSize > 0) {
				for (i in 0..vlansListSize-1) {
				   rtn += '<vlans>'+vlansList[i]+'</vlans>'
				}
			}
		}
		return rtn


	}

	/**
	 * This method returns the uri value for the vpn bindings.
	 * Return the a list of value of vpn binding in the <related-link> string.
	 * Ex.
	 *   <relationship-list>
	 *      <relationship>
	 *          <related-to>vpn-binding</related-to>
	 *          <related-link>https://aai-app-e2e.test.openecomp.com:8443/aai/v6/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
	 *          <relationship-data>
	 *             <relationship-key>vpn-binding.vpn-id</relationship-key>
	 *             <relationship-value>85f015d0-2e32-4c30-96d2-87a1a27f8017</relationship-value>
	 *          </relationship-data>
	 *       </relationship>
	 *		<relationship>
	 *	        <related-to>vpn-binding</related-to>
	 *			<related-link>https://aai-ext1.test.openecomp.com:8443/aai/v6/network/vpn-bindings/vpn-binding/24a4b507-853a-4a38-99aa-05fcc54be24d/</related-link>
	 *			<relationship-data>
	 *			   <relationship-key>vpn-binding.vpn-id</relationship-key>
	 *			   <relationship-value>24a4b507-853a-4a38-99aa-05fcc54be24d</relationship-value>
	 *		    </relationship-data>
	 *			<related-to-property>
	 *			  <property-key>vpn-binding.vpn-name</property-key>
	 *			  <property-value>oam_protected_net_6_MTN5_msotest1</property-value>
	 *			</related-to-property>
	 *		</relationship>
	 * @param xmlInput the XML document
	 * @return a list of vpn binding values
	 *            ex: ['aai/v6/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', 'aai/v6/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/']
	 *
	 **/
	def getVnfBindingObject(xmlInput) {
		//def rtn = null
		List rtn = []
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "vpn-binding") {
				   	  def relatedLink = utils.getNodeText(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 rtn.add(relatedLink.substring(relatedLink.indexOf("/aai/"), relatedLink.length()))
					  }
				   }
				}
			}
		}
		return rtn
	}
	/**
	 * similar to VNF bindings method
	* @param xmlInput the XML document
	* @return a list of network policy values
	*            ex: ['aai/v$/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg', 'aai/v$/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg']
	*
	**/
	def getNetworkPolicyObject(xmlInput) {
		//def rtn = null
		List rtn = []
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "network-policy") {
					  def relatedLink = utils.getNodeText(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 rtn.add(relatedLink.substring(relatedLink.indexOf("/aai/"), relatedLink.length()))
					  }
				   }
				}
			}
		}
		return rtn
	}

	/**
	 * similar to network policymethod
	* @param xmlInput the XML document
	* @return a list of network policy values
	*            ex: ['aai/v$/network/route-table-references/route-table-reference/refFQDN1', 'aai/v$/network/route-table-references/route-table-reference/refFQDN2']
	*
	**/
	def getNetworkTableRefObject(xmlInput) {
		//def rtn = null
		List rtn = []
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "route-table-reference") {
					  def relatedLink = utils.getNodeText1(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 rtn.add(relatedLink.substring(relatedLink.indexOf("/aai/"), relatedLink.length()))
					  }
				   }
				}
			}
		}
		return rtn
	}

	/**
	 * similar to network policymethod
	* @param xmlInput the XML document
	* @return a list of IDs for related VNF instances
	*
	**/
	def getRelatedVnfIdList(xmlInput) {
		//def rtn = null
		List rtn = []
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "generic-vnf") {
					  def relatedLink = utils.getNodeText1(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 if (relatedLink.substring(relatedLink.indexOf("/generic-vnf/")+13, relatedLink.length()).contains('/')) {
							 rtn.add(relatedLink.substring(relatedLink.indexOf("/generic-vnf/")+13, relatedLink.length()-1))
						 } else {
						     rtn.add(relatedLink.substring(relatedLink.indexOf("/generic-vnf/")+13, relatedLink.length()))
						 }
					  }
				   }
				}
			}
		}
		return rtn
	}

	/**
	 * similar to network policymethod
	* @param xmlInput the XML document
	* @return a list of IDs for related Network instances
	*
	**/
	def getRelatedNetworkIdList(xmlInput) {
		//def rtn = null
		List rtn = []
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "l3-network") {
					  def relatedLink = utils.getNodeText1(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 if (relatedLink.substring(relatedLink.indexOf("/l3-network/")+12, relatedLink.length()).contains('/')) {
							 rtn.add(relatedLink.substring(relatedLink.indexOf("/l3-network/")+12, relatedLink.length()-1))
						 } else {
						     rtn.add(relatedLink.substring(relatedLink.indexOf("/l3-network/")+12, relatedLink.length()))
						 }
					  }
				   }
				}
			}
		}
		return rtn
	}

	def isVfRelationshipExist(xmlInput) {
		Boolean rtn = false
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "vf-module") {
					     rtn = true
				   }
				}
			}
		}
		return rtn

	}

	def getCloudRegion(xmlInput) {
		String lcpCloudRegion = ""
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "cloud-region") {
					  def relatedLink = utils.getNodeText1(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 lcpCloudRegion = relatedLink.substring(relatedLink.indexOf("/att-aic/")+9, relatedLink.length())
						 if (lcpCloudRegion.contains('/')) {
							 lcpCloudRegion = relatedLink.substring(relatedLink.indexOf("/att-aic/")+9, relatedLink.length()-1)
						 }
					  }
				   }
				}
			}
		}
		return lcpCloudRegion
	}

	def getTenantId(xmlInput) {
		String tenantId = ""
		if (xmlInput!=null) {
			def relationshipList = getListWithElements(xmlInput, 'relationship')
			def relationshipListSize = relationshipList.size()
			if (relationshipListSize > 0) {
				for (i in 0..relationshipListSize-1) {
				   def relationshipXml = XmlUtil.serialize(relationshipList[i])
				   if (utils.getNodeText(relationshipXml, 'related-to') == "tenant") {
					  def relatedLink = utils.getNodeText1(relationshipXml, 'related-link')
					  if (relatedLink != null || relatedLink != "") {
						 tenantId = relatedLink.substring(relatedLink.indexOf("/tenant/")+8, relatedLink.length())
						 if (tenantId.contains('/')) {
							 tenantId = relatedLink.substring(relatedLink.indexOf("/tenant/")+8, relatedLink.length()-1)
						 }
					  }
				   }
				}
			}
		}
		return tenantId
	}

	def isInstanceValueMatch(linkResource, globalSubscriberId, serviceType) {
		Boolean rtn = false
		try {
			String globalSubscriberIdLink = linkResource.substring(linkResource.indexOf("/customer/")+10, linkResource.indexOf("/service-subscriptions"))
			String serviceTypeLink = linkResource.substring(linkResource.indexOf("/service-subscription/")+22, linkResource.indexOf("/service-instances"))
			if (globalSubscriberIdLink == globalSubscriberId) {
					rtn = true
			} else {
				if (serviceTypeLink == serviceType) {
					rtn = true
				}
			}

		} catch (Exception ex) {
		    println 'Exception - ' + ex.getMessage()
			return false
		}
        return rtn
	}

	def getListWithElements(xmlInput, groupName) {
		def rtn = ""
		if (xmlInput != null) {
			def relationshipData = new XmlSlurper().parseText(xmlInput)
			rtn = relationshipData.'**'.findAll {it.name() == groupName}
		}
		return rtn

	}

	// build network single elements
	def buildNetworkElements(l3Network, createNetworkContrailResponse, networkList) {
		def replaceNetworkId = ""
		def replaceNeutronNetworkId = ""
		def replaceContrailNetworkFqdn = ""
		if (l3Network != null && createNetworkContrailResponse != null) {
			if (utils.nodeExists(l3Network, 'heat-stack-id')) {
				replaceNetworkId = utils.getNodeText(l3Network, 'heat-stack-id')
			} else {
				if (utils.nodeExists(createNetworkContrailResponse, 'networkStackId')) {
					replaceNetworkId = utils.getNodeText(createNetworkContrailResponse, 'networkStackId')
				}
			}
			if (utils.nodeExists(l3Network, 'neutron-network-id')) {
				replaceNeutronNetworkId = utils.getNodeText(l3Network, 'neutron-network-id')
			} else {
				if (utils.nodeExists(createNetworkContrailResponse, 'neutronNetworkId')) {
					replaceNeutronNetworkId = utils.getNodeText(createNetworkContrailResponse, 'neutronNetworkId')
				}
			}
			if (utils.nodeExists(l3Network, 'contrail-network-fqdn')) {
				replaceContrailNetworkFqdn = utils.getNodeText(l3Network, 'contrail-network-fqdn')
			} else {
				if (utils.nodeExists(createNetworkContrailResponse, 'networkFqdn')) {
					replaceContrailNetworkFqdn = utils.getNodeText(createNetworkContrailResponse, 'networkFqdn')
				}
			}
		}

		String var = ""
		def xmlNetwork = ""
		if (l3Network != null) {
			for (element in networkList) {
				def xml= new XmlSlurper().parseText(l3Network)
				var = xml.'**'.find {it.name() == element}
				if (var == null) {
					if (element=="orchestration-status") {
						if (var.toString() == 'pending-create' || var.toString() == 'PendingCreate') {
							xmlNetwork += "<"+element+">"+"Created"+"</"+element+">"
					    } else { //pending-update or PendingUpdate
							xmlNetwork += "<"+element+">"+"Active"+"</"+element+">"
					    }
					}	
					if (element=="heat-stack-id") {
						if (replaceNetworkId != "") {
							xmlNetwork += "<"+element+">"+replaceNetworkId+"</"+element+">"
						}
					}
					if (element=="neutron-network-id") {
						if (replaceNeutronNetworkId != "") {
							xmlNetwork += "<"+element+">"+replaceNeutronNetworkId+"</"+element+">"
						}
					}
					if (element=="contrail-network-fqdn") {
						if (replaceContrailNetworkFqdn != "") {
							xmlNetwork += "<"+element+">"+replaceContrailNetworkFqdn+"</"+element+">"
						}
					}

				} else {
			   		if (element=="orchestration-status") {
					   if (element=="orchestration-status") {
						  if (var.toString() == 'pending-create' || var.toString() == 'PendingCreate') {
						      xmlNetwork += "<"+element+">"+"Created"+"</"+element+">"
						  } else { //pending-update or PendingUpdate
							  xmlNetwork += "<"+element+">"+"Active"+"</"+element+">"
						  }
					   }   
				    } else {
			    	    xmlNetwork += "<"+element+">"+var.toString()+"</"+element+">"
					}
				}	
			 }
		}
		return xmlNetwork
	}

	def buildSubnets(requeryIdAAIResponse, createNetworkResponse) {
		def rebuildingSubnets = ""
		if (requeryIdAAIResponse != null && utils.nodeExists(requeryIdAAIResponse, 'subnets')) {
			def subnetIdMapValue = ""
			def subnetsGroup = utils.getNodeXml(requeryIdAAIResponse, "subnets", false)
			def subnetsData = new XmlSlurper().parseText(subnetsGroup)
			rebuildingSubnets += "<subnets>"
			try {
				def subnets = subnetsData.'**'.findAll {it.name() == "subnet"}
				def subnetsSize = subnets.size()
				for (i in 0..subnetsSize-1) {
				   def subnet = subnets[i]
				   def subnetXml = XmlUtil.serialize(subnet)
				   def orchestrationStatus = utils.getNodeText1(subnetXml, "orchestration-status")
				   if (orchestrationStatus == "PendingDelete" || orchestrationStatus == "pending-delete") {
					   // skip, do not include in processing, remove!!!
				   } else {
				      def subnetList = ["subnet-id", "neutron-subnet-id", "gateway-address", "network-start-address", "cidr-mask", "ip-version", "orchestration-status", "dhcp-enabled", "dhcp-start", "dhcp-end", "resource-version", "subnet-name"]
				      rebuildingSubnets += buildSubNetworkElements(subnetXml, createNetworkResponse, subnetList, "subnet")
				   }	  
				}
				if (utils.nodeExists(subnetsData, 'relationship')) {
					rebuildingSubnets = rebuildRelationship(requeryIdAAIResponse)
				}

			} catch (Exception ex) {
				// error
			} finally {
				rebuildingSubnets += "</subnets>"
			}
		}
		return rebuildingSubnets
	}

	def buildSubnets(queryIdResponse) {
		def rebuildingSubnets = ""
		def subnetsData = new XmlSlurper().parseText(queryIdResponse)
		//rebuildingSubnets += "<subnets>"
		try {
			def subnets = subnetsData.'**'.findAll {it.name() == "subnet"}
			def subnetsSize = subnets.size()
			for (i in 0..subnetsSize-1) {
			   def subnet = subnets[i]
			   def subnetXml = XmlUtil.serialize(subnet)
			   def orchestrationStatus = utils.getNodeText1(subnetXml, "orchestration-status")
			   if (orchestrationStatus == "pending-delete" || orchestrationStatus == "PendingDelete") {
				   // skip, do not include in processing, remove!!!
			   } else {
			   	  	def subnetList = ["dhcp-start", "dhcp-end", "network-start-address", "cidr-mask", "dhcp-enabled", "gateway-address", "ip-version", "subnet-id", "subnet-name"]
					rebuildingSubnets += buildSubNetworkElements(subnetXml, subnetList, "subnets")
			   		//rebuildingSubnets += buildSubNetworkElements(subnetXml, subnetList, "")
			   }	
			}
		} catch (Exception ex) {
		   //
		} finally {
		  //rebuildingSubnets += "</subnets>"
		}
		return rebuildingSubnets
	}


	// build subnet sub-network single elements
	def buildSubNetworkElements(subnetXml, createNetworkResponse, elementList, parentName) {
		String var = ""
		def xmlBuild = ""
		if (parentName != "") {
		   xmlBuild += "<"+parentName+">"
		}
		if (subnetXml != null) {
			for (element in elementList) {
			  def xml= new XmlSlurper().parseText(subnetXml)
			  var = xml.'**'.find {it.name() == element}
			  if (var != null) {
			  	 if (element=="orchestration-status") {
					if(var.toString() == 'pending-create' || var.toString() == 'PendingCreate') {   
						xmlBuild += "<"+element+">"+"Created"+"</"+element+">"
					} else { // pending-update or PendingUpdate'
					   xmlBuild += "<"+element+">"+"Active"+"</"+element+">"
					}
				 } else { // "subnet-id", "neutron-subnet-id"
					 if (element=="subnet-id") {
						 if (utils.nodeExists(createNetworkResponse, "subnetMap")) {
							 xmlBuild += "<"+element+">"+var.toString()+"</"+element+">"
							 String neutronSubnetId = extractNeutSubId(createNetworkResponse, var.toString())
							 xmlBuild += "<neutron-subnet-id>"+neutronSubnetId+"</neutron-subnet-id>"
						 }
					 } else {
					     if (element=="neutron-subnet-id") {
                               // skip
						 } else {
						    xmlBuild += "<"+element+">"+var.toString()+"</"+element+">"
						 }
					 }
				 }
			  }
			}

		}
		if (parentName != "") {
		   xmlBuild += "</"+parentName+">"
		}
		return xmlBuild
	}

	// build subnet sub-network single elements
	def buildSubNetworkElements(subnetXml, elementList, parentName) {
		def var = ""
		def xmlBuild = ""
		if (parentName != "") {
			xmlBuild += "<"+parentName+">"
		 }
		if (subnetXml != null) {
		    def networkStartAddress = ""
			for (element in elementList) {
				def xml= new XmlSlurper().parseText(subnetXml)
				var = xml.'**'.find {it.name() == element}
				if (element == "dhcp-start") {
					xmlBuild += "<allocationPools>"
					if (var.toString() == 'null') {
						xmlBuild += "<start>"+""+"</start>"
					} else {
						xmlBuild += "<start>"+var.toString()+"</start>"
					}
				}
				if (element == "dhcp-end") {
					if (var.toString() == 'null') {
						xmlBuild += "<end>"+""+"</end>"
					} else {
						xmlBuild += "<end>"+var.toString()+"</end>"
					}
					xmlBuild += "</allocationPools>"
				}
				if (element == "network-start-address" || element == "cidr-mask") {
					if (element == "network-start-address") {
						networkStartAddress = var.toString()
					}
					if (element == "cidr-mask") {
						xmlBuild += "<cidr>"+networkStartAddress+"/"+var.toString()+"</cidr>"
					}
				}
				if (element == "dhcp-enabled") {
					xmlBuild += "<enableDHCP>"+var.toString()+"</enableDHCP>"
				}
				if (element == "gateway-address") {
					xmlBuild += "<gatewayIp>"+var.toString()+"</gatewayIp>"
				}
				if (element == "ip-version") {
					String ipVersion = getIpvVersion(var.toString())
					xmlBuild += "<ipVersion>"+ipVersion+"</ipVersion>"
				}
				if (element == "subnet-id") {
					xmlBuild += "<subnetId>"+var.toString()+"</subnetId>"
				}
				if ((element == "subnet-name") && (var != null)) {
					xmlBuild += "<subnetName>"+var.toString()+"</subnetName>"
				}
			}
		}
		if (parentName != "") {
			xmlBuild += "</"+parentName+">"
		 }
		return xmlBuild
	}

	// rebuild ctag-assignments
	def rebuildCtagAssignments(xmlInput) {
		def rebuildingCtagAssignments = ""
		if (xmlInput!=null) {
			def ctagAssignmentsData = new XmlSlurper().parseText(xmlInput)
			rebuildingCtagAssignments += "<ctag-assignments>"
			def ctagAssignments = ctagAssignmentsData.'**'.findAll {it.name() == "ctag-assignment"}
			def ctagAssignmentsSize = ctagAssignments.size()
			for (i in 0..ctagAssignmentsSize-1) {
				def ctagAssignment = ctagAssignments[i]
				def ctagAssignmentXml = XmlUtil.serialize(ctagAssignment)
				rebuildingCtagAssignments += "<ctag-assignment>"
				List elementList = ["vlan-id-inner", "resource-version"]
				rebuildingCtagAssignments +=  buildXMLElements(ctagAssignmentXml, ""      , "", elementList)
				if (utils.nodeExists(ctagAssignmentXml, 'relationship')) {
					rebuildingCtagAssignments += rebuildRelationship(ctagAssignmentXml)
				}
				rebuildingCtagAssignments += "</ctag-assignment>"
			}
			rebuildingCtagAssignments += "</ctag-assignments>"
		}
		return rebuildingCtagAssignments
	}

	// rebuild 'relationship-list'
	def rebuildRelationship(xmlInput) {
		def rebuildingSubnets = ""
		if (xmlInput!=null) {
			def subnetsData = new XmlSlurper().parseText(xmlInput)
			rebuildingSubnets += "<relationship-list>"
			def relationships = subnetsData.'**'.findAll {it.name() == "relationship"}
			def relationshipsSize = relationships.size()
			for (i in 0..relationshipsSize-1) {
				def relationship = relationships[i]
				def relationshipXml = XmlUtil.serialize(relationship)
				rebuildingSubnets += "<relationship>"
				def relationshipList = ["related-to", "related-link"]
				rebuildingSubnets += buildSubNetworkElements(relationshipXml, "", relationshipList, "")
				if (utils.nodeExists(relationshipXml, 'relationship-data')) {
					def relationshipDataXmlData = new XmlSlurper().parseText(relationshipXml)
					def relationshipsData = relationshipDataXmlData.'**'.findAll {it.name() == "relationship-data"}
					def relationshipsDataSize = relationshipsData.size()
					for (j in 0..relationshipsDataSize-1) {
						def relationshipData = relationshipsData[j]
						def relationshipDataXml = XmlUtil.serialize(relationshipData)
						def relationshipDataList =  ["relationship-key", "relationship-value"]
						rebuildingSubnets += buildXMLElements(relationshipDataXml, "", "relationship-data", relationshipDataList)
					}
				}
				if (utils.nodeExists(relationshipXml, 'related-to-property')) {
					def relationshipDataXmlData = new XmlSlurper().parseText(relationshipXml)
					def relationshipsData = relationshipDataXmlData.'**'.findAll {it.name() == "related-to-property"}
					def relationshipsDataSize = relationshipsData.size()
					for (j in 0..relationshipsDataSize-1) {
						def relationshipData = relationshipsData[j]
						def relationshipDataXml = XmlUtil.serialize(relationshipData)
						def relationshipDataList =  ["property-key", "property-value"]
						rebuildingSubnets += buildXMLElements(relationshipDataXml, "", "related-to-property", relationshipDataList)
					}
				}

				rebuildingSubnets += "</relationship>"
			}
			rebuildingSubnets += "</relationship-list>"
		}
		return rebuildingSubnets
	}

	def buildVlans(queryIdResponse) {
		def rebuildingSubnets = "<vlans>"
		def subnetsData = new XmlSlurper().parseText(queryIdResponse)

		try {
			def subnets = subnetsData.'**'.findAll {it.name() == "segmentation-assignments"}
			def subnetsSize = subnets.size()
			for (i in 0..subnetsSize-1) {
			   def subnet = subnets[i]
			   def subnetXml = XmlUtil.serialize(subnet)

			   String vlan = utils.getNodeText1(subnetXml, "segmentation-id")
			   if (i>0){
				   rebuildingSubnets += ","
			   }
			   rebuildingSubnets += vlan
			}
		} catch (Exception ex) {
		   //
		} finally {
		  //rebuildingSubnets += "</subnets>"
		rebuildingSubnets += "</vlans>"
		}
		return rebuildingSubnets
	}

	/* Utility code to rebuild xml/elements in a list:
	 * rebuild xml with 1) unbounded groups of elements; or
	 *                  2) one group of elements; or
	 *                  3) just one or more elements (in a list as argument)
	 * @param xmlInput the XML document
	 * @param parentName the parent name  (ex: 'inputs')
	 * @param childrenName the chilrendName (ex: 'entry' as unbounded/occurs>1)
	 * @param elementList the element list of children (ex: 'key', 'value')
	 * @return a string of rebuild xml
	 *
	 * Ex 1: xmlInput:
	 *    <ws:inputs>
	 *       <ws:entry>
	 *          <ws:key>name</ws:key>
	 *          <ws:value>Edward</ws:value>
	 *       </ws:entry>
	 *       <ws:entry>
	 *          <ws:key>age</ws:key>
	 *          <ws:value>30</ws:value>
	 *       </ws:entry>
	 *       <ws:entry>
	 *          <ws:key>age</ws:key>
	 *          <ws:value>30</ws:value>
	 *       </ws:entry>
	 *    <ws:/inputs>
	 * Usage:
	 * List elementList = ["key", "value"]
	 * String rebuild =  buildXMLElements(xmlInput, "inputs", "entry", elementList)
	 *
	 * Ex 2: xmlInput // no parent tag
	 *   <ws:sdnc-request-header>
	 *    <ws:svc-request-id>fec8ec88-151a-45c9-ad60-8233e0fc8ff2</ws:svc-request-id>
	 *    <ws:svc-notification-url>https://msojra.mtsnj.aic.cip.openecomp.com:8443/adapters/rest/SDNCNotify</ws:svc-notification-url>
	 *    <ws:svc-action>assign</ws:svc-action>
	 *   </ws:sdnc-request-header>
	 * Usage:
	 * List elementList = ["svc-request-id", "svc-notification-url", "svc-action"]
	 * String rebuild =  buildXMLElements(xmlInput, ""      , "sdnc-request-header", elementList)  // no parent tag
	 *
	 * Ex 3: xmlInput // elements one after another (with no parent & children tag)
	 * <ws:test-id>myTestid</ws:test-id>
	 * <ws:test-user>myUser</ws:test-user>
	 * Usage:
	 * List elementList = ["test-id", "test-user"]
	 * String rebuild =  buildXMLElements(xmlInput, ""      , "", elementList)
	 *
	 */
	def buildXMLElements(xmlInput, parentName, childrenName, elementList) {
		def varChildren = ""
		def var = ""
		def xmlBuildUnbounded = ""
		if (parentName!="") {xmlBuildUnbounded += "<"+parentName+">" +'\n'}
		if (xmlInput != null) {
			def xml= new XmlSlurper().parseText(xmlInput)
			if (childrenName!="") {
				varChildren = xml.'**'.findAll {it.name() == childrenName}
				for (i in 0..varChildren.size()-1) {
					xmlBuildUnbounded += "<"+childrenName+">" +'\n'
					for (element in elementList) {
						var = varChildren[i].'*'.find {it.name() == element}
					   if (var != null) {
						  xmlBuildUnbounded += "<"+element+">"+var.toString()+"</"+element+">" +'\n'
					   }
					}
					xmlBuildUnbounded += "</"+childrenName+">" +'\n'
				}
			} else {
				for (element in elementList) {
					var = xml.'*'.find {it.name() == element}
					if (var != null) {
						xmlBuildUnbounded += "<"+element+">"+var.toString()+"</"+element+">" +'\n'
					}
				}
			}

		}
		if (parentName!="") {xmlBuildUnbounded += "</"+parentName+">" +'\n'}
		return xmlBuildUnbounded
	 }

	def getFirstNodeXml(xmlInput, element){
		def nodeAsText = ""
		def nodeToSerialize =  ""
		if (xmlInput != null) {
			def fxml= new XmlSlurper().parseText(xmlInput)
			if (utils.nodeExists(xmlInput, "payload")) {
				nodeToSerialize = fxml.'payload'.'l3-network'.'*'.find {it.name() == element}
				if (nodeToSerialize!=null) {
					nodeAsText = XmlUtil.serialize(nodeToSerialize)
				} else {
				    nodeAsText = ""
				}

			} else {
				nodeToSerialize = fxml.'*'.find {it.name() == element}
				if (nodeToSerialize!=null) {
					nodeAsText = XmlUtil.serialize(nodeToSerialize)
				} else {
					nodeAsText = ""
				}

			}
		}
		return nodeAsText

	}

//TODO: This method still needs to be tested before using.
	/**
	 *
	 * This method is similar to the gennetwork:ContrailNetworUpdateCompletedObject
	 * BPEL method.  It extracts all of the required subnet information
	 * for each subnet listed with an orch status equal to the one provided
	 * and puts the corresponding infomation with the appropriate node for
	 * updating aai. The method sets the orch status for each subnet to active
	 *
	 * @param subnetsXml the entire subnets xml
	 * @param requestInput the request in the process
	 * @param queryIdResponse the response of REST AAI query by Id
	 * @param queryVpnBindingResponse the response of REST AAI query by vpn binding
	 * @param routeCollection the collection of vpnBinding's 'global-route-target'
	 * @return String request
	 */
	public String networkUpdateSubnetInfo(String subnetsXml, String networkResponseXml){

			String subnets = ""
			StringBuilder sb = new StringBuilder()
			InputSource source = new InputSource(new StringReader(subnetsXml))
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
			docFactory.setNamespaceAware(true)
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
			Document xml = docBuilder.parse(source)
			NodeList nodeList = xml.getElementsByTagNameNS("*", "subnet")
			for (int x = 0; x < nodeList.getLength(); x++) {
				Node node = nodeList.item(x)
				String subnet = ""
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node
					String subnetOrchStatus = eElement.getElementsByTagNameNS("*", "orchestration-status").item(0).getTextContent()
					if(subnetOrchStatus.equals("pending-create")){

						String subnetId = eElement.getElementsByTagNameNS("*", "subnet-id").item(0).getTextContent()
						def netAddress = eElement.getElementsByTagNameNS("*", "network-start-address").item(0).getTextContent()
						def mask = eElement.getElementsByTagNameNS("*", "cidr-mask").item(0).getTextContent()
						def dhcpEnabledSubnet = eElement.getElementsByTagNameNS("*", "dhcp-enabled").item(0).getTextContent()
						def gatewayAddress = eElement.getElementsByTagNameNS("*", "gateway-address").item(0).getTextContent()
						def ipVersion = eElement.getElementsByTagNameNS("*", "ip-version").item(0).getTextContent()
						def relationshipList =  eElement.getElementsByTagNameNS("*", "relationship-list").item(0).getTextContent() //TODO: test this
						String neutronSubnetId = extractNeutSubId(networkResponseXml, subnetId)
						subnet =
						"""<subnet>
					<subnetId>${subnetId}</subnetId>
					<neutron-subnet-id>${neutronSubnetId}</neutron-subnet-id>
					<gateway-address>${gatewayAddress}</gateway-address>
					<network-start-address>${netAddress}</network-start-address>
					<cidr-mask>${mask}</cidr-mask>
					<ip-Version>${ipVersion}</ip-Version>
					<orchestration-status>active</orchestration-status>
					<dhcp-enabled>${dhcpEnabledSubnet}</dhcp-enabled>
					<relationship-list>${relationshipList}</relationship-list>
					</subnet>"""

					}else if(subnetOrchStatus.equals("pending-delete")){
						StringWriter writer = new StringWriter()
						Transformer transformer = TransformerFactory.newInstance().newTransformer()
						transformer.transform(new DOMSource(node), new StreamResult(writer))
						subnet = writer.toString()

					}else{
						subnet = ""
					}
				}
				subnets = sb.append(subnet)
			}

			subnets = utils.removeXmlPreamble(subnets)

			String subnetsList =
			"""<subnets>
			${subnets}
			</subnets>"""

			return subnetsList
	}


	/**
	 *
	 * This method extracts the "value" node text for the the given subnet Id.
	 *
	 * @param String inputSource - xml that contains the subnet id key and value
	 * @param String subnetId - for which you want the value of

	 * @return String value - node text of node named value associated with the given subnet id
	 */
	public String extractNeutSubId(String inputSource, String subnetId){

				String value = ""
				InputSource source = new InputSource(new StringReader(inputSource))
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
				docFactory.setNamespaceAware(true)
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
				Document xml = docBuilder.parse(source)
				NodeList nodeList = xml.getElementsByTagNameNS("*", "entry")
				for (int x = 0; x < nodeList.getLength(); x++) {
					Node node = nodeList.item(x)
					String subnet = ""
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) node
						String key = eElement.getElementsByTagNameNS("*", "key").item(0).getTextContent()
						if(key.equals(subnetId)){
							value = eElement.getElementsByTagNameNS("*", "value").item(0).getTextContent()
						}
					}
				}
				return value
			}

	public boolean isRollbackEnabled (Execution execution, String payloadXml) {

		def rollbackEnabled = false
		def rollbackValueSet = false
		if (utils.nodeExists(payloadXml, "backout-on-failure")) {
			String backoutValue = utils.getNodeText1(payloadXml, "backout-on-failure")
			if (backoutValue != null && !backoutValue.isEmpty()) {
				if (backoutValue.equalsIgnoreCase("false")) {
					rollbackEnabled = false
				}
				else {
					rollbackEnabled = true
				}
				rollbackValueSet = true
			}
		}

		if (!rollbackValueSet) {
			if (execution.getVariable("URN_mso_rollback") != null) {
			    rollbackEnabled = execution.getVariable("URN_mso_rollback").toBoolean()
			}
		}
		return rollbackEnabled
	}
	
	
	/**
	 * This method extracts the version for the the given ip-version.
	 *
	 * @param String ipvVersion - IP protocols version (ex: ipv4 or ipv6)
	 * @return String version - digit version (ex: 4 or 6)
	 */
	
	public String getIpvVersion (String ipvVersion) {
		
		String version = ""
		if (ipvVersion.isNumber()) {
			version = ipvVersion
		} else {
			version = ipvVersion.substring(ipvVersion.indexOf("ipv")+3)
		}
		return version
	}
}
