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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.HostRoute
import org.onap.aai.domain.yang.L3Network
import org.onap.aai.domain.yang.SegmentationAssignment
import org.onap.aai.domain.yang.Subnet
import org.onap.aai.domain.yang.Subnets
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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
	def CreateNetworkRequestV2(execution, requestId, messageId, requestInput,L3Network queryIdResponse, routeCollection, policyFqdns, tableCollection, cloudRegionId, backoutOnFailure, source) {
		String createNetworkRequest = null
		if(requestInput!=null && queryIdResponse!=null) {
				String serviceInstanceId = ""
				String sharedValue = ""
				String externalValue = ""

				if (source == "VID") {
					sharedValue = queryIdResponse.isIsSharedNetwork() != null ? queryIdResponse.isIsSharedNetwork() : "false"
					externalValue = queryIdResponse.isIsExternalNetwork() != null ? queryIdResponse.isIsExternalNetwork() : "false"
					serviceInstanceId = utils.getNodeText(requestInput, "service-instance-id")

				} else { // source = 'PORTAL'
					sharedValue = getParameterValue(requestInput, "shared")
					externalValue = getParameterValue(requestInput, "external")
					serviceInstanceId = utils.getNodeText(requestInput, "service-instance-id") != null ? utils.getNodeText(requestInput, "service-instance-id") : ""
				}

				String networkParams = ""
				if (utils.nodeExists(requestInput, "network-params")) {
					String netParams = utils.getNodeXml(requestInput, "network-params", false).replace("tag0:","").replace(":tag0","")
					networkParams = buildParams(netParams)
				}

				String failIfExists = "false"
				// requestInput
				String cloudRegion = cloudRegionId
				String tenantId = utils.getNodeText(requestInput, "tenant-id")

				String networkType = ""
				String modelCustomizationUuid = ""
				if (utils.nodeExists(requestInput, "networkModelInfo")) {
					String networkModelInfo = utils.getNodeXml(requestInput, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
					networkType = utils.getNodeText(networkModelInfo, "modelName")
					modelCustomizationUuid = utils.getNodeText(networkModelInfo, "modelCustomizationUuid")
				} else {
					networkType = queryIdResponse.getNetworkType()
					modelCustomizationUuid = utils.getNodeText(requestInput, "modelCustomizationId")
				}

				// queryIdResponse
				String networkName = queryIdResponse.getNetworkName()
				String networkId = queryIdResponse.getNetworkId()
				String networkTechnology = queryIdResponse.getNetworkTechnology()

				// contrailNetwork - networkTechnology = 'Contrail' vs. 'AIC_SR_IOV')
				String contrailNetwork = ""
				if (networkTechnology.contains('Contrail') || networkTechnology.contains('contrail') || networkTechnology.contains('CONTRAIL')) {
					contrailNetwork = """<contrailNetwork>
					                       <shared>${MsoUtils.xmlEscape(sharedValue)}</shared>
					                       <external>${MsoUtils.xmlEscape(externalValue)}</external>
					                       ${routeCollection}
					                       ${policyFqdns}
					                       ${tableCollection}
				                         </contrailNetwork>"""
					networkTechnology = "CONTRAIL"    // replace
			    }

				// rebuild subnets
				String subnets = ""
				if (queryIdResponse.getSubnets() != null) {
					subnets = buildSubnets(queryIdResponse)
				}

				String physicalNetworkName = ""
				physicalNetworkName = queryIdResponse.getPhysicalNetworkName()

				String vlansCollection = buildVlans(queryIdResponse)

				String notificationUrl = ""                                   //TODO - is this coming from URN? What variable/value to use?
				//String notificationUrl = execution.getVariable("URN_?????") //TODO - is this coming from URN? What variable/value to use?

				createNetworkRequest = """
								<createNetworkRequest>
									<cloudSiteId>${MsoUtils.xmlEscape(cloudRegion)}</cloudSiteId>
									<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
									<networkId>${MsoUtils.xmlEscape(networkId)}</networkId>
									<networkName>${MsoUtils.xmlEscape(networkName)}</networkName>
									<networkType>${MsoUtils.xmlEscape(networkType)}</networkType>
									<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
									<networkTechnology>${MsoUtils.xmlEscape(networkTechnology)}</networkTechnology>
									<providerVlanNetwork>
										<physicalNetworkName>${MsoUtils.xmlEscape(physicalNetworkName)}</physicalNetworkName >
										${vlansCollection}
									</providerVlanNetwork>
                                    ${contrailNetwork}
									${subnets}
									<skipAAI>true</skipAAI>
									<backout>${MsoUtils.xmlEscape(backoutOnFailure)}</backout>
									<failIfExists>${MsoUtils.xmlEscape(failIfExists)}</failIfExists>
									${networkParams}
									<msoRequest>
										<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
										<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
			      					</msoRequest>
									<messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
									<notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
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
	def UpdateNetworkRequestV2(execution, requestId, messageId, requestInput, L3Network queryIdResponse, routeCollection, policyFqdns, tableCollection, cloudRegionId, backoutOnFailure, source) {
		String updateNetworkRequest = null
		if(requestInput!=null && queryIdResponse!=null) {
				String serviceInstanceId = ""
				String sharedValue = ""
				String externalValue = ""

				if (source == "VID") {
					sharedValue = queryIdResponse.isIsSharedNetwork() != null ? queryIdResponse.isIsSharedNetwork() : "false"
					externalValue = queryIdResponse.isIsExternalNetwork() != null ? queryIdResponse.isIsExternalNetwork() : "false"
					serviceInstanceId = utils.getNodeText(requestInput, "service-instance-id")

				} else { // source = 'PORTAL'
					sharedValue = getParameterValue(requestInput, "shared")
					externalValue = getParameterValue(requestInput, "external")
					serviceInstanceId = utils.getNodeText(requestInput, "service-instance-id") != null ? utils.getNodeText(requestInput, "service-instance-id") : ""
				}

				String failIfExists = "false"
				// requestInput
				String cloudRegion = cloudRegionId
				String tenantId = utils.getNodeText(requestInput, "tenant-id")

				// queryIdResponse
				String networkName = queryIdResponse.getNetworkName()
				String networkId = queryIdResponse.getNetworkId()

				String networkType = ""
				String modelCustomizationUuid = ""
				if (utils.nodeExists(requestInput, "networkModelInfo")) {
					String networkModelInfo = utils.getNodeXml(requestInput, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
					networkType = utils.getNodeText(networkModelInfo, "modelName")
					modelCustomizationUuid = utils.getNodeText(networkModelInfo, "modelCustomizationUuid")
				} else {
					networkType = queryIdResponse.getNetworkType()
					modelCustomizationUuid = utils.getNodeText(requestInput, "modelCustomizationId")
				}

				// rebuild subnets
				String subnets = ""
				if (queryIdResponse.getSubnets() != null) {
					subnets = buildSubnets(queryIdResponse)
				}

				String networkParams = ""
				if (utils.nodeExists(requestInput, "network-params")) {
					String netParams = utils.getNodeXml(requestInput, "network-params", false).replace("tag0:","").replace(":tag0","")
					networkParams = buildParams(netParams)
				}

				String networkStackId = queryIdResponse.getHeatStackId()
				if (networkStackId == 'null' || networkStackId == "" || networkStackId == null) {
					networkStackId = "force_update"
				}

				String physicalNetworkName = queryIdResponse.getPhysicalNetworkName()
				String vlansCollection = buildVlans(queryIdResponse)

				updateNetworkRequest =
                         """<updateNetworkRequest>
								<cloudSiteId>${MsoUtils.xmlEscape(cloudRegion)}</cloudSiteId>
								<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
								<networkId>${MsoUtils.xmlEscape(networkId)}</networkId>
						        <networkStackId>${MsoUtils.xmlEscape(networkStackId)}</networkStackId>
								<networkName>${MsoUtils.xmlEscape(networkName)}</networkName>
								<networkType>${MsoUtils.xmlEscape(networkType)}</networkType>
								<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
								<networkTypeVersion/>
								<networkTechnology>CONTRAIL</networkTechnology>
								<providerVlanNetwork>
									<physicalNetworkName>${MsoUtils.xmlEscape(physicalNetworkName)}</physicalNetworkName>
									${vlansCollection}
								</providerVlanNetwork>
								<contrailNetwork>
									<shared>${MsoUtils.xmlEscape(sharedValue)}</shared>
									<external>${MsoUtils.xmlEscape(externalValue)}</external>
									${routeCollection}
									${policyFqdns}
									${tableCollection}
								</contrailNetwork>
								${subnets}
								<skipAAI>true</skipAAI>
								<backout>${MsoUtils.xmlEscape(backoutOnFailure)}</backout>
								<failIfExists>${MsoUtils.xmlEscape(failIfExists)}</failIfExists>
									${networkParams}

								<msoRequest>
								  <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
								  <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
								</msoRequest>
						 		<messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
						 		<notificationUrl></notificationUrl>
 							</updateNetworkRequest>""".trim()

		}
		return updateNetworkRequest

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
						build += "<${name}>${MsoUtils.xmlEscape(value)}</${name}>"
					}
					build += "</networkParams>"
				}

			} catch (Exception ex) {
				println ' buildParams error - ' + ex.getMessage()
				build = ""
			}
		return build
	}

	// build network single elements
	@Deprecated //TODO remove if not used anywhere
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

	def buildSubnets(L3Network network) {
		def rebuildingSubnets = ""
		Subnets subnets = network.getSubnets()
		try{
			for(Subnet s : subnets.getSubnet()){
				def orchestrationStatus = s.getOrchestrationStatus()
				if (orchestrationStatus == "pending-delete" || orchestrationStatus == "PendingDelete") {
					// skip, do not include in processing, remove!!!
				} else {
					def subnetList = ["dhcp-start", "dhcp-end", "network-start-address", "cidr-mask", "dhcp-enabled", "gateway-address", "ip-version", "subnet-id", "subnet-name", "ip-assignment-direction", "host-routes"]
					rebuildingSubnets += buildSubNetworkElements(s, subnetList, "subnets")
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
	def buildSubNetworkElements(Subnet subnet, elementList, parentName) {

		def var = ""
		def xmlBuild = ""
		if (parentName != "") {
			xmlBuild += "<"+parentName+">"
		 }
		if (subnet != null) {
		    def networkStartAddress = ""
			for (element in elementList) {
				if (element == "dhcp-start") {
					var = subnet.getDhcpStart()
					xmlBuild += "<allocationPools>"
					if (var == null) {
						xmlBuild += "<start>"+""+"</start>"
					} else {
						xmlBuild += "<start>"+var+"</start>"
					}
				}
				if (element == "dhcp-end") {
					var = subnet.getDhcpEnd()
					if (var == null) {
						xmlBuild += "<end>"+""+"</end>"
					} else {
						xmlBuild += "<end>"+var+"</end>"
					}
					xmlBuild += "</allocationPools>"
				}
				if (element == "network-start-address" || element == "cidr-mask") {
					if (element == "network-start-address") {
						networkStartAddress = subnet.getNetworkStartAddress()
					}
					if (element == "cidr-mask") {
						xmlBuild += "<cidr>"+networkStartAddress+"/"+ subnet.getCidrMask() +"</cidr>"
					}
				}
				if (element == "dhcp-enabled") {
					xmlBuild += "<enableDHCP>"+subnet.isDhcpEnabled()+"</enableDHCP>"
				}
				if (element == "gateway-address") {
					xmlBuild += "<gatewayIp>"+subnet.getGatewayAddress()+"</gatewayIp>"
				}
				if (element == "ip-version") {
					String ipVersion = getIpvVersion(subnet.getIpVersion())
					xmlBuild += "<ipVersion>"+ipVersion+"</ipVersion>"
				}
				if (element == "subnet-id") {
					xmlBuild += "<subnetId>"+subnet.getSubnetId()+"</subnetId>"
				}
				if ((element == "subnet-name") && (subnet.getSubnetName() != null)) {
					xmlBuild += "<subnetName>"+subnet.getSubnetName()+"</subnetName>"
				}
				if ((element == "ip-assignment-direction") && (subnet.getIpAssignmentDirection() != null)) {
					xmlBuild += "<addrFromStart>"+subnet.getIpAssignmentDirection()+"</addrFromStart>"
				}
				if (element == "host-routes") {
					def routes = ""
					if (subnet.getHostRoutes() != null) {
						routes = buildHostRoutes(subnet)
					}
					xmlBuild += routes
				}

			}
		}
		if (parentName != "") {
			xmlBuild += "</"+parentName+">"
		 }
		return xmlBuild
	}

	// rebuild host-routes
	def buildHostRoutes(Subnet subnet) {
		def buildHostRoutes = ""
		List<HostRoute> routes = subnet.getHostRoutes().getHostRoute()
		if(!routes.isEmpty()){
			for(HostRoute route:routes){
				buildHostRoutes += "<hostRoutes>"
				buildHostRoutes += "<prefix>" + route.getRoutePrefix() + "</prefix>"
				buildHostRoutes += "<nextHop>" + route.getNextHop() + "</nextHop>"
				buildHostRoutes += "</hostRoutes>"
			}
		}

		return buildHostRoutes
	}

	private String buildVlans(L3Network queryIdResponse) { // get seg ids in put in vlan tags
		String vlans = "<vlans>"
		if(queryIdResponse.getSegmentationAssignments() != null){
			List<SegmentationAssignment> segmentations = queryIdResponse.getSegmentationAssignments().getSegmentationAssignment()
			if(!segmentations.isEmpty()){
				for(SegmentationAssignment seg:segmentations){
					String vlan = seg.getSegmentationId() + ","
					vlans += vlan
				}
			}
		}

		if(vlans.endsWith(",")){
			vlans = vlans.substring(0, vlans.length() - 1)
		}

		vlans += "</vlans>"

		return vlans
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
			InputSource source = new InputSource(new StringReader(subnetsXml));
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
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
					<subnetId>${MsoUtils.xmlEscape(subnetId)}</subnetId>
					<neutron-subnet-id>${MsoUtils.xmlEscape(neutronSubnetId)}</neutron-subnet-id>
					<gateway-address>${MsoUtils.xmlEscape(gatewayAddress)}</gateway-address>
					<network-start-address>${MsoUtils.xmlEscape(netAddress)}</network-start-address>
					<cidr-mask>${MsoUtils.xmlEscape(mask)}</cidr-mask>
					<ip-Version>${MsoUtils.xmlEscape(ipVersion)}</ip-Version>
					<orchestration-status>active</orchestration-status>
					<dhcp-enabled>${MsoUtils.xmlEscape(dhcpEnabledSubnet)}</dhcp-enabled>
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
				InputSource source = new InputSource(new StringReader(inputSource));
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
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

	public boolean isRollbackEnabled (DelegateExecution execution, String payloadXml) {

		def rollbackEnabled = false
		def rollbackValueSet = false
		if (utils.nodeExists(payloadXml, "backout-on-failure")) {
			String backoutValue = utils.getNodeText(payloadXml, "backout-on-failure")
			if (backoutValue != null && !backoutValue.isEmpty()) {
				if (backoutValue.equalsIgnoreCase("false")) {
					rollbackEnabled = false
				}
				else {
					rollbackEnabled = true
				}
				rollbackValueSet = true;
			}
		}

		if (!rollbackValueSet) {

			if (UrnPropertiesReader.getVariable("mso.rollback", execution) != null) {
			    rollbackEnabled = UrnPropertiesReader.getVariable("mso.rollback", execution).toBoolean()
			}
		}
		return rollbackEnabled
	}


	/**
	 * This method extracts the version for the the given ip-version.
	 *
	 * @param String ipvVersion - IP protocols version (ex: ipv4 or ipv6 or 4 or 6)
	 * @return String version - digit version (ex: 4 or 6)
	 */

	public String getIpvVersion (String ipvVersion) {

		String version = ""
		try {
			if (ipvVersion.isNumber()) {
				version = ipvVersion
			} else {
				version = ipvVersion.substring(ipvVersion.indexOf("ipv")+3)
				if (!version.isNumber()) {
					version = ipvVersion
				}
			}
		} catch (Exception ex) {
			version = ipvVersion
		}
		return version
	}
}
