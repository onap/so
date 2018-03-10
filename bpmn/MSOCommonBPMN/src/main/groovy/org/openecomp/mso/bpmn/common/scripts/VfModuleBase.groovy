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

//import groovy.util.Node;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.runtime.Execution
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource


public abstract class VfModuleBase extends AbstractServiceTaskProcessor {
	
	protected XmlParser xmlParser = new XmlParser()
	
	/**
	 * Get the XmlParser.
	 * 
	 * @return the XmlParser.
	 */
	protected XmlParser getXmlParser() {
		return xmlParser
	}
	
	/**
	 * Find the VF Module with specified ID in the specified Generic VF.  If no such
	 * VF Module is found, null is returned.
	 * 
	 * @param genericVnf The Generic VNF in which to search for the specified VF Moduel.
	 * @param vfModuleId The ID of the VF Module for which to search.
	 * @return a VFModule object for the found VF Module or null if no VF Module is found.
	 */
	protected VfModule findVfModule(String genericVnf, String vfModuleId) {
		
		def genericVnfNode = xmlParser.parseText(genericVnf)
		def vfModulesNode = utils.getChildNode(genericVnfNode, 'vf-modules')
		if (vfModulesNode == null) {
			return null
		}
		def vfModuleList = utils.getIdenticalChildren(vfModulesNode, 'vf-module')
		for (vfModuleNode in vfModuleList) {
			def vfModuleIdNode = utils.getChildNode(vfModuleNode, 'vf-module-id')
			if ((vfModuleIdNode != null) && (vfModuleIdNode.text().equals(vfModuleId))) {
				return new VfModule(vfModuleNode, (vfModuleList.size() == 1))
			}
		}
		return null
	}
	
	/**
	 * Transform all '*_network' parameter specifications from the incoming '*-params' root
	 * element to a corresponding list of 'vnf-networks' specifications (typically used when
	 * invoking the VNF Rest Adpater). Each element in '*-params' whose name attribute ends
	 * with '_network' is used to create an 'vnf-networks' element.
	 * 
	 * @param paramsNode A Node representing a '*-params' element.
	 * @return a String of 'vnf-networks' elements, one for each 'param' element whose name
	 * attribute ends with '_network'.
	 */
	protected String transformNetworkParamsToVnfNetworks(String paramsRootXml) {
		if ((paramsRootXml == null) || (paramsRootXml.isEmpty())) {
			return ''
		}
		def String vnfNetworks = ''
		try {
			paramsRootXml = utils.removeXmlNamespaces(paramsRootXml)
			def paramsNode = xmlParser.parseText(paramsRootXml)
			def params = utils.getIdenticalChildren(paramsNode, 'param')
			for (param in params) {
				def String attrName = (String) param.attribute('name')
				if (attrName.endsWith('_network')) {
					def networkRole = attrName.substring(0, (attrName.length()-'_network'.length()))
					def networkName = param.text()
					String vnfNetwork = """
						<vnf-networks>
							<network-role>${networkRole}</network-role>
							<network-name>${networkName}</network-name>
						</vnf-networks>
					"""
					vnfNetworks = vnfNetworks + vnfNetwork
				}
			}
		} catch (Exception e) {
			logWarn('Exception transforming \'_network\' params to vnfNetworks', e)
		}
		return vnfNetworks
	}
	
	/**
	 * Transform the parameter specifications from the incoming '*-params' root element to
	 * a corresponding list of 'entry's (typically used when invoking the VNF Rest Adpater).
	 * Each element in '*-params' is used to create an 'entry' element.
	 *
	 * @param paramsNode A Node representing a '*-params' element.
	 * @return a String of 'entry' elements, one for each 'param' element.
	 */
	protected String transformParamsToEntries(String paramsRootXml) {
		if ((paramsRootXml == null) || (paramsRootXml.isEmpty())) {
			return ''
		}
		def String entries = ''
		try {
			paramsRootXml = utils.removeXmlNamespaces(paramsRootXml)
			def paramsNode = xmlParser.parseText(paramsRootXml)
			def params = utils.getIdenticalChildren(paramsNode, 'param')
			for (param in params) {
				def key = (String) param.attribute('name')
				if (key == null) {
					key = ''
				}
				def value = (String) param.text()
				String entry = """
					<entry>
						<key>${key}</key>
						<value>${value}</value>
					</entry>
				"""
				entries = entries + entry
			}
		} catch (Exception e) {
			logWarn('Exception transforming params to entries', e)
		}
		return entries
	}
	
	/**
	 * Transform the parameter specifications from the incoming '*-params' root element to
	 * a corresponding list of 'entry's (typically used when invoking the VNF Rest Adpater).
	 * Each element in '*-params' is used to create an 'entry' element.
	 *
	 * @param paramsNode A Node representing a '*-params' element.
	 * @return a String of 'entry' elements, one for each 'param' element.
	 */
	protected String transformVolumeParamsToEntries(String paramsRootXml) {
		if ((paramsRootXml == null) || (paramsRootXml.isEmpty())) {
			return ''
		}
		def String entries = ''
		try {
			paramsRootXml = utils.removeXmlNamespaces(paramsRootXml)
			def paramsNode = xmlParser.parseText(paramsRootXml)
			def params = utils.getIdenticalChildren(paramsNode, 'param')
			for (param in params) {
				def key = (String) param.attribute('name')
				if (key == null) {
					key = ''
				}
				if ( !(key in ['vnf_id', 'vnf_name', 'vf_module_id', 'vf_module_name'])) {
					def value = (String) param.text()
					String entry = """
						<entry>
							<key>${key}</key>
							<value>${value}</value>
						</entry>
					"""
					entries = entries + entry
				}
			}
		} catch (Exception e) {
			logWarn('Exception transforming params to entries', e)
		}
		return entries
	}
	
	/**
	 * Extract the Tenant Id from the Volume Group information returned by AAI.
	 * 
	 * @param volumeGroupXml Volume Group XML returned by AAI.
	 * @return the Tenant Id extracted from the Volume Group information. 'null' is returned if
	 * the Tenant Id is missing or could not otherwise be extracted.
	 */
	protected String getTenantIdFromVolumeGroup(String volumeGroupXml) {
		def groovy.util.Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def groovy.util.Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def groovy.util.NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (groovy.util.Node relationship in relationships) {
				def groovy.util.Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('tenant'))) {
					def groovy.util.NodeList relationshipDataList = utils.getIdenticalChildren(relationship, 'relationship-data')
					for (groovy.util.Node relationshipData in relationshipDataList) {
						def groovy.util.Node relationshipKey = utils.getChildNode(relationshipData, 'relationship-key')
						if ((relationshipKey != null) && (relationshipKey.text().equals('tenant.tenant-id'))) {
							def groovy.util.Node relationshipValue = utils.getChildNode(relationshipData, 'relationship-value')
							if (relationshipValue != null) {
								return relationshipValue.text()
							}
						}
					}
				}
			}
		}
		return null
	}
	
	
	/*
	 * Parses VNF parameters passed in on the incoming requests and SDNC parameters returned from SDNC get response
	 * and puts them into the format expected by VNF adapter.
	 * @param vnfParamsMap -  map of VNF parameters passed in the request body
	 * @param sdncGetResponse - response string from SDNC GET topology request
	 * @param vnfId
	 * @param vnfName
	 * @param vfModuleId
	 * @param vfModuleName
	 * @param vfModuleIndex - can be null
	 * @return a String of key/value entries for vfModuleParams
	 */
	  
	 
	 protected String buildVfModuleParams(Map<String, String> vnfParamsMap, String sdncGetResponse, String vnfId, String vnfName,
			String vfModuleId, String vfModuleName, String vfModuleIndex) {
			
			//Get SDNC Response Data
			
			String data = utils.getNodeXml(sdncGetResponse, "response-data")
			data = data.replaceAll("&lt;", "<")
			data = data.replaceAll("&gt;", ">")

			String serviceData = utils.getNodeXml(data, "service-data")
			serviceData = utils.removeXmlPreamble(serviceData)
			serviceData = utils.removeXmlNamespaces(serviceData)
			String vnfRequestInfo = utils.getNodeXml(serviceData, "vnf-request-information")
			String oldVnfId = utils.getNodeXml(vnfRequestInfo, "vnf-id")
			oldVnfId = utils.removeXmlPreamble(oldVnfId)
			oldVnfId = utils.removeXmlNamespaces(oldVnfId)
			serviceData = serviceData.replace(oldVnfId, "")
			def vnfId1 = utils.getNodeText1(serviceData, "vnf-id")
			
			Map<String, String> paramsMap = new HashMap<String, String>()
			
			if (vfModuleIndex != null) {
				paramsMap.put("vf_module_index", "${vfModuleIndex}")
			}

			// Add-on data
			paramsMap.put("vnf_id", "${vnfId}")
			paramsMap.put("vnf_name", "${vnfName}")
			paramsMap.put("vf_module_id", "${vfModuleId}")
			paramsMap.put("vf_module_name", "${vfModuleName}")
			
			InputSource source = new InputSource(new StringReader(data))
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
			docFactory.setNamespaceAware(true)
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
			Document responseXml = docBuilder.parse(source)


			// Availability Zones Data
			
			NodeList aZonesList = responseXml.getElementsByTagNameNS("*", "availability-zones")
			String aZonePosition = "0"
			for (int z = 0; z < aZonesList.getLength(); z++) {
				Node node = aZonesList.item(z)
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node
					String aZoneValue = utils.getElementText(eElement, "availability-zone")
					aZonePosition = z.toString()
					paramsMap.put("availability_zone_${aZonePosition}", "${aZoneValue}")
				}
			}

			// VNF Networks Data
			
			StringBuilder sbNet = new StringBuilder()
			
			NodeList vnfNetworkList = responseXml.getElementsByTagNameNS("*", "vnf-networks")
			for (int x = 0; x < vnfNetworkList.getLength(); x++) {
				Node node = vnfNetworkList.item(x)
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node
					String vnfNetworkKey = utils.getElementText(eElement, "network-role")
					String vnfNetworkNeutronIdValue = utils.getElementText(eElement, "neutron-id")
					String vnfNetworkNetNameValue = utils.getElementText(eElement, "network-name")
					String vnfNetworkSubNetIdValue = utils.getElementText(eElement, "subnet-id")
					String vnfNetworkV6SubNetIdValue = utils.getElementText(eElement, "ipv6-subnet-id")
					String vnfNetworkNetFqdnValue = utils.getElementText(eElement, "contrail-network-fqdn")
					paramsMap.put("${vnfNetworkKey}_net_id", "${vnfNetworkNeutronIdValue}")
					paramsMap.put("${vnfNetworkKey}_net_name", "${vnfNetworkNetNameValue}")
					paramsMap.put("${vnfNetworkKey}_subnet_id", "${vnfNetworkSubNetIdValue}")
					paramsMap.put("${vnfNetworkKey}_v6_subnet_id", "${vnfNetworkV6SubNetIdValue}")
					paramsMap.put("${vnfNetworkKey}_net_fqdn", "${vnfNetworkNetFqdnValue}")
					
					NodeList sriovVlanFilterList = eElement.getElementsByTagNameNS("*","sriov-vlan-filter-list")
					StringBuffer sriovFilterBuf = new StringBuffer()
					String values = ""
					for(int i = 0; i < sriovVlanFilterList.getLength(); i++){
						Node node1 = sriovVlanFilterList.item(i)
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement1 = (Element) node1
							String value = utils.getElementText(eElement1, "sriov-vlan-filter")
							if (i != sriovVlanFilterList.getLength() - 1) {
								values = sriovFilterBuf.append(value + ",")
							}
							else {
								values = sriovFilterBuf.append(value)
							}
						}
					}
					if (!values.isEmpty()) {
							paramsMap.put("${vnfNetworkKey}_ATT_VF_VLAN_FILTER", "${values}")
						}
					}
			}

			// VNF-VMS Data
			
			def key
			def value
			def networkKey
			def networkValue
			def floatingIPKey
			def floatingIPKeyValue
			def floatingIPV6Key
			def floatingIPV6KeyValue
			StringBuilder sb = new StringBuilder()

			NodeList vmsList = responseXml.getElementsByTagNameNS("*","vnf-vms")
			for (int x = 0; x < vmsList.getLength(); x++) {
				Node node = vmsList.item(x)
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node
					key = utils.getElementText(eElement, "vm-type")
					String values
					String position = "0"
					StringBuilder sb1 = new StringBuilder()
					NodeList valueList = eElement.getElementsByTagNameNS("*","vm-names")
					NodeList vmNetworksList = eElement.getElementsByTagNameNS("*","vm-networks")
					for(int i = 0; i < valueList.getLength(); i++){
						Node node1 = valueList.item(i)
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement1 = (Element) node1
							value = utils.getElementText(eElement1, "vm-name")
							if (i != valueList.getLength() - 1) {
								values = sb1.append(value + ",")
							}
							else {
								values = sb1.append(value)
							}
							position = i.toString()
							paramsMap.put("${key}_name_${position}", "${value}")
						}
					}
					for(int n = 0; n < vmNetworksList.getLength(); n++){
						String floatingIpKeyValueStr = ""
						String floatingIpV6KeyValueStr = ""
						Node nodeNetworkKey = vmNetworksList.item(n)
						if (nodeNetworkKey.getNodeType() == Node.ELEMENT_NODE) {
							Element eElementNetworkKey = (Element) nodeNetworkKey
							String ipAddressValues
							String ipV6AddressValues
							String networkPosition = "0"
							StringBuilder sb2 = new StringBuilder()
							StringBuilder sb3 = new StringBuilder()
							StringBuilder sb4 = new StringBuilder()
							networkKey = utils.getElementText(eElementNetworkKey, "network-role")
							floatingIPKey = key + '_' + networkKey + '_floating_ip'
							floatingIPKeyValue = utils.getElementText(eElementNetworkKey, "floating-ip")
							if(!floatingIPKeyValue.isEmpty()){
								paramsMap.put("$floatingIPKey", "$floatingIPKeyValue")
							}
							floatingIPV6Key = key + '_' + networkKey + '_floating_v6_ip'
							floatingIPV6KeyValue = utils.getElementText(eElementNetworkKey, "floating-ip-v6")
							if(!floatingIPV6KeyValue.isEmpty()){
								paramsMap.put("$floatingIPV6Key", "$floatingIPV6KeyValue")
							}
							NodeList networkIpsList = eElementNetworkKey.getElementsByTagNameNS("*","network-ips")
							for(int a = 0; a < networkIpsList.getLength(); a++){
								Node ipAddress = networkIpsList.item(a)
								if (ipAddress.getNodeType() == Node.ELEMENT_NODE) {
									Element eElementIpAddress = (Element) ipAddress
									String ipAddressValue = utils.getElementText(eElementIpAddress, "ip-address")
									if (a != networkIpsList.getLength() - 1) {
										ipAddressValues = sb2.append(ipAddressValue + ",")
									}
									else {
										ipAddressValues = sb2.append(ipAddressValue)
									}
									networkPosition = a.toString()
									paramsMap.put("${key}_${networkKey}_ip_${networkPosition}", "${ipAddressValue}")
								}
							}
							
							paramsMap.put("${key}_${networkKey}_ips", "${ipAddressValues}")
							
							NodeList interfaceRoutePrefixesList = eElementNetworkKey.getElementsByTagNameNS("*","interface-route-prefixes")
							String interfaceRoutePrefixValues = sb3.append("[")
							
							for(int a = 0; a < interfaceRoutePrefixesList.getLength(); a++){
								Node interfaceRoutePrefix = interfaceRoutePrefixesList.item(a)
								if (interfaceRoutePrefix.getNodeType() == Node.ELEMENT_NODE) {
									Element eElementInterfaceRoutePrefix = (Element) interfaceRoutePrefix
									String interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix-cidr")
									if (interfaceRoutePrefixValue == null || interfaceRoutePrefixValue.isEmpty()) {
										interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix")
									}
									if (a != interfaceRoutePrefixesList.getLength() - 1) {
										interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}" + ",")
									}
									else {
										interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}")
									}
								}
							}
							interfaceRoutePrefixValues = sb3.append("]")
							if (interfaceRoutePrefixesList.getLength() > 0) {
								paramsMap.put("${key}_${networkKey}_route_prefixes", "${interfaceRoutePrefixValues}")
							}
							
							NodeList networkIpsV6List = eElementNetworkKey.getElementsByTagNameNS("*","network-ips-v6")
							for(int a = 0; a < networkIpsV6List.getLength(); a++){
								Node ipV6Address = networkIpsV6List.item(a)
								if (ipV6Address.getNodeType() == Node.ELEMENT_NODE) {
									Element eElementIpV6Address = (Element) ipV6Address
									String ipV6AddressValue = utils.getElementText(eElementIpV6Address, "ip-address-ipv6")
									if (a != networkIpsV6List.getLength() - 1) {
										ipV6AddressValues = sb4.append(ipV6AddressValue + ",")
									}
									else {
										ipV6AddressValues = sb4.append(ipV6AddressValue)
									}
									networkPosition = a.toString()
									paramsMap.put("${key}_${networkKey}_v6_ip_${networkPosition}", "${ipV6AddressValue}")
								}
							}
							paramsMap.put("${key}_${networkKey}_v6_ips", "${ipV6AddressValues}")
						}
					}
					paramsMap.put("${key}_names", "${values}")
				}
			}
		//SDNC Response Params
			String sdncResponseParams = ""
			List<String> sdncResponseParamsToSkip = ["vnf_id", "vf_module_id", "vnf_name", "vf_module_name"]
			String vnfParamsChildNodes = utils.getChildNodes(data, "vnf-parameters")
			if(vnfParamsChildNodes == null || vnfParamsChildNodes.length() < 1){
				// No SDNC params
			}else{
				NodeList paramsList = responseXml.getElementsByTagNameNS("*", "vnf-parameters")
				for (int z = 0; z < paramsList.getLength(); z++) {
					Node node = paramsList.item(z)
					Element eElement = (Element) node
					String vnfParameterName = utils.getElementText(eElement, "vnf-parameter-name")
					if (!sdncResponseParamsToSkip.contains(vnfParameterName)) {
						String vnfParameterValue = utils.getElementText(eElement, "vnf-parameter-value")
						paramsMap.put("${vnfParameterName}", "${vnfParameterValue}")
					}
				}
			}
			
		// Parameters received from the request should overwrite any parameters received from SDNC
		if (vnfParamsMap != null) {
			for (Map.Entry<String, String> entry : vnfParamsMap.entrySet()) {
				String vnfKey = entry.getKey()
				String vnfValue = entry.getValue()
				paramsMap.put("$vnfKey", "$vnfValue")
			}
		}
		
		StringBuilder sbParams = new StringBuilder()
		def vfModuleParams = ""
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			String paramsXml
			String paramName = entry.getKey()
			String paramValue = entry.getValue()
			paramsXml =
					"""<entry>
						<key>${paramName}</key>
						<value>${paramValue}</value>
					</entry>
					"""

			vfModuleParams = sbParams.append(paramsXml)
		}
		
		return vfModuleParams
		
	}
			
			
			/*
			 * Parses VNF parameters passed in on the incoming requests and SDNC parameters returned from SDNC get response
			 * for both VNF and VF Module
			 * and puts them into the format expected by VNF adapter.
			 * @param vnfParamsMap -  map of VNF parameters passed in the request body
			 * @param vnfSdncGetResponse - response string from SDNC GET VNF topology request
			 * @param vfmoduleSdncGetResponse - response string from SDNC GET VF Module topology request
			 * @param vnfId
			 * @param vnfName
			 * @param vfModuleId
			 * @param vfModuleName
			 * @param vfModuleIndex - can be null
			 * @return a String of key/value entries for vfModuleParams
			 */			  
			 
			 protected String buildVfModuleParamsFromCombinedTopologies(Map<String, String> vnfParamsMap, String vnfSdncGetResponse, String vfmoduleSdncGetResponse, String vnfId, String vnfName,
					String vfModuleId, String vfModuleName, String vfModuleIndex) {
					
					// Set up initial parameters
					
					Map<String, String> paramsMap = new HashMap<String, String>()
					
					if (vfModuleIndex != null) {
						paramsMap.put("vf_module_index", "${vfModuleIndex}")
					}
		
					// Add-on data
					paramsMap.put("vnf_id", "${vnfId}")
					paramsMap.put("vnf_name", "${vnfName}")
					paramsMap.put("vf_module_id", "${vfModuleId}")
					paramsMap.put("vf_module_name", "${vfModuleName}")					
					
					//Get SDNC Response Data for VNF
					
					String vnfData = utils.getNodeXml(vnfSdncGetResponse, "response-data")
					vnfData = vnfData.replaceAll("&lt;", "<")
					vnfData = vnfData.replaceAll("&gt;", ">")
		
					String vnfTopology = utils.getNodeXml(vnfData, "vnf-topology")
					vnfTopology = utils.removeXmlPreamble(vnfTopology)
					vnfTopology = utils.removeXmlNamespaces(vnfTopology)
										
					InputSource sourceVnf = new InputSource(new StringReader(vnfData))
					DocumentBuilderFactory docFactoryVnf = DocumentBuilderFactory.newInstance()
					docFactoryVnf.setNamespaceAware(true)
					DocumentBuilder docBuilderVnf = docFactoryVnf.newDocumentBuilder()
					Document responseXmlVnf = docBuilderVnf.parse(sourceVnf)
				
					// Availability Zones Data
					
					NodeList aZonesList = responseXmlVnf.getElementsByTagNameNS("*", "availability-zones")
					String aZonePosition = "0"
					for (int z = 0; z < aZonesList.getLength(); z++) {
						Node node = aZonesList.item(z)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) node
							String aZoneValue = utils.getElementText(eElement, "availability-zone")
							aZonePosition = z.toString()
							paramsMap.put("availability_zone_${aZonePosition}", "${aZoneValue}")
						}
					}
		
					// VNF Networks Data
					
					StringBuilder sbNet = new StringBuilder()
					
					NodeList vnfNetworkList = responseXmlVnf.getElementsByTagNameNS("*", "vnf-networks")
					for (int x = 0; x < vnfNetworkList.getLength(); x++) {
						Node node = vnfNetworkList.item(x)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) node
							String vnfNetworkKey = utils.getElementText(eElement, "network-role")
							String vnfNetworkNeutronIdValue = utils.getElementText(eElement, "neutron-id")
							String vnfNetworkNetNameValue = utils.getElementText(eElement, "network-name")
							String vnfNetworkSubNetIdValue = utils.getElementText(eElement, "subnet-id")
							String vnfNetworkV6SubNetIdValue = utils.getElementText(eElement, "ipv6-subnet-id")
							String vnfNetworkNetFqdnValue = utils.getElementText(eElement, "contrail-network-fqdn")
							paramsMap.put("${vnfNetworkKey}_net_id", "${vnfNetworkNeutronIdValue}")
							paramsMap.put("${vnfNetworkKey}_net_name", "${vnfNetworkNetNameValue}")
							paramsMap.put("${vnfNetworkKey}_subnet_id", "${vnfNetworkSubNetIdValue}")
							paramsMap.put("${vnfNetworkKey}_v6_subnet_id", "${vnfNetworkV6SubNetIdValue}")
							paramsMap.put("${vnfNetworkKey}_net_fqdn", "${vnfNetworkNetFqdnValue}")
							
							NodeList sriovVlanFilterList = eElement.getElementsByTagNameNS("*","sriov-vlan-filter-list")
							StringBuffer sriovFilterBuf = new StringBuffer()
							String values = ""
							for(int i = 0; i < sriovVlanFilterList.getLength(); i++){
								Node node1 = sriovVlanFilterList.item(i)
								if (node1.getNodeType() == Node.ELEMENT_NODE) {
									Element eElement1 = (Element) node1
									String value = utils.getElementText(eElement1, "sriov-vlan-filter")
									if (i != sriovVlanFilterList.getLength() - 1) {
										values = sriovFilterBuf.append(value + ",")
									}
									else {
										values = sriovFilterBuf.append(value)
									}
								}
							}
							if (!values.isEmpty()) {
									paramsMap.put("${vnfNetworkKey}_ATT_VF_VLAN_FILTER", "${values}")
								}
							}
					}
					
					//Get SDNC Response Data for VF Module
					
					String vfModuleData = utils.getNodeXml(vfmoduleSdncGetResponse, "response-data")
					vfModuleData = vfModuleData.replaceAll("&lt;", "<")
					vfModuleData = vfModuleData.replaceAll("&gt;", ">")
		
					String vfModuleTopology = utils.getNodeXml(vfModuleData, "vf-module-topology")
					vfModuleTopology = utils.removeXmlPreamble(vfModuleTopology)
					vfModuleTopology = utils.removeXmlNamespaces(vfModuleTopology)
					String vfModuleTopologyIdentifier = utils.getNodeXml(vfModuleTopology, "vf-module-topology-identifier")
					
					InputSource sourceVfModule = new InputSource(new StringReader(vfModuleData))
					DocumentBuilderFactory docFactoryVfModule = DocumentBuilderFactory.newInstance()
					docFactoryVfModule.setNamespaceAware(true)
					DocumentBuilder docBuilderVfModule = docFactoryVfModule.newDocumentBuilder()
					Document responseXmlVfModule = docBuilderVfModule.parse(sourceVfModule)
				
					// VMS Data
					
					def key
					def value
					def networkKey
					def networkValue
					def floatingIPKey
					def floatingIPKeyValue
					def floatingIPV6Key
					def floatingIPV6KeyValue
					StringBuilder sb = new StringBuilder()
		
					NodeList vmsList = responseXmlVfModule.getElementsByTagNameNS("*","vm")
					for (int x = 0; x < vmsList.getLength(); x++) {
						Node node = vmsList.item(x)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) node
							key = utils.getElementText(eElement, "vm-type")
							String values
							String position = "0"
							StringBuilder sb1 = new StringBuilder()
							NodeList valueList = eElement.getElementsByTagNameNS("*","vm-names")
							NodeList vmNetworksList = eElement.getElementsByTagNameNS("*","vm-networks")
							for(int i = 0; i < valueList.getLength(); i++){
								Node node1 = valueList.item(i)
								if (node1.getNodeType() == Node.ELEMENT_NODE) {
									Element eElement1 = (Element) node1
									value = utils.getElementText(eElement1, "vm-name")
									if (i != valueList.getLength() - 1) {
										values = sb1.append(value + ",")
									}
									else {
										values = sb1.append(value)
									}
									position = i.toString()
									paramsMap.put("${key}_name_${position}", "${value}")
								}
							}
							for(int n = 0; n < vmNetworksList.getLength(); n++){
								String floatingIpKeyValueStr = ""
								String floatingIpV6KeyValueStr = ""
								Node nodeNetworkKey = vmNetworksList.item(n)
								if (nodeNetworkKey.getNodeType() == Node.ELEMENT_NODE) {
									Element eElementNetworkKey = (Element) nodeNetworkKey
									String ipAddressValues
									String ipV6AddressValues
									String networkPosition = "0"
									StringBuilder sb2 = new StringBuilder()
									StringBuilder sb3 = new StringBuilder()
									StringBuilder sb4 = new StringBuilder()
									networkKey = utils.getElementText(eElementNetworkKey, "network-role")
									floatingIPKey = key + '_' + networkKey + '_floating_ip'
									floatingIPKeyValue = utils.getElementText(eElementNetworkKey, "floating-ip")
									if(!floatingIPKeyValue.isEmpty()){
										paramsMap.put("$floatingIPKey", "$floatingIPKeyValue")
									}
									floatingIPV6Key = key + '_' + networkKey + '_floating_v6_ip'
									floatingIPV6KeyValue = utils.getElementText(eElementNetworkKey, "floating-ip-v6")
									if(!floatingIPV6KeyValue.isEmpty()){
										paramsMap.put("$floatingIPV6Key", "$floatingIPV6KeyValue")
									}
									NodeList networkIpsList = eElementNetworkKey.getElementsByTagNameNS("*","network-ips")
									for(int a = 0; a < networkIpsList.getLength(); a++){
										Node ipAddress = networkIpsList.item(a)
										if (ipAddress.getNodeType() == Node.ELEMENT_NODE) {
											Element eElementIpAddress = (Element) ipAddress
											String ipAddressValue = utils.getElementText(eElementIpAddress, "ip-address")
											if (a != networkIpsList.getLength() - 1) {
												ipAddressValues = sb2.append(ipAddressValue + ",")
											}
											else {
												ipAddressValues = sb2.append(ipAddressValue)
											}
											networkPosition = a.toString()
											paramsMap.put("${key}_${networkKey}_ip_${networkPosition}", "${ipAddressValue}")
										}
									}
									
									paramsMap.put("${key}_${networkKey}_ips", "${ipAddressValues}")
									
									NodeList interfaceRoutePrefixesList = eElementNetworkKey.getElementsByTagNameNS("*","interface-route-prefixes")
									String interfaceRoutePrefixValues = sb3.append("[")
									
									for(int a = 0; a < interfaceRoutePrefixesList.getLength(); a++){
										Node interfaceRoutePrefix = interfaceRoutePrefixesList.item(a)
										if (interfaceRoutePrefix.getNodeType() == Node.ELEMENT_NODE) {
											Element eElementInterfaceRoutePrefix = (Element) interfaceRoutePrefix
											String interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix-cidr")
											if (interfaceRoutePrefixValue == null || interfaceRoutePrefixValue.isEmpty()) {
												interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix")
											}
											if (a != interfaceRoutePrefixesList.getLength() - 1) {
												interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}" + ",")
											}
											else {
												interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}")
											}
										}
									}
									interfaceRoutePrefixValues = sb3.append("]")
									if (interfaceRoutePrefixesList.getLength() > 0) {
										paramsMap.put("${key}_${networkKey}_route_prefixes", "${interfaceRoutePrefixValues}")
									}
									
									NodeList networkIpsV6List = eElementNetworkKey.getElementsByTagNameNS("*","network-ips-v6")
									for(int a = 0; a < networkIpsV6List.getLength(); a++){
										Node ipV6Address = networkIpsV6List.item(a)
										if (ipV6Address.getNodeType() == Node.ELEMENT_NODE) {
											Element eElementIpV6Address = (Element) ipV6Address
											String ipV6AddressValue = utils.getElementText(eElementIpV6Address, "ip-address-ipv6")
											if (a != networkIpsV6List.getLength() - 1) {
												ipV6AddressValues = sb4.append(ipV6AddressValue + ",")
											}
											else {
												ipV6AddressValues = sb4.append(ipV6AddressValue)
											}
											networkPosition = a.toString()
											paramsMap.put("${key}_${networkKey}_v6_ip_${networkPosition}", "${ipV6AddressValue}")
										}
									}
									paramsMap.put("${key}_${networkKey}_v6_ips", "${ipV6AddressValues}")
								}
							}
							paramsMap.put("${key}_names", "${values}")
						}
					}
				//SDNC Response Params
					List<String> sdncResponseParamsToSkip = ["vnf_id", "vf_module_id", "vnf_name", "vf_module_name"]
					
					String vnfParamsChildNodes = utils.getChildNodes(vnfData, "param")
					if(vnfParamsChildNodes == null || vnfParamsChildNodes.length() < 1){
						// No SDNC params for VNF
					}else{
						NodeList paramsList = responseXmlVnf.getElementsByTagNameNS("*", "param")
						for (int z = 0; z < paramsList.getLength(); z++) {
							Node node = paramsList.item(z)
							Element eElement = (Element) node
							String vnfParameterName = utils.getElementText(eElement, "name")
							if (!sdncResponseParamsToSkip.contains(vnfParameterName)) {
								String vnfParameterValue = utils.getElementText(eElement, "value")
								paramsMap.put("${vnfParameterName}", "${vnfParameterValue}")
							}
						}
					}
					
					String vfModuleParamsChildNodes = utils.getChildNodes(vfModuleData, "param")
					if(vfModuleParamsChildNodes == null || vfModuleParamsChildNodes.length() < 1){
						// No SDNC params for VF Module
					}else{
						NodeList paramsList = responseXmlVfModule.getElementsByTagNameNS("*", "param")
						for (int z = 0; z < paramsList.getLength(); z++) {
							Node node = paramsList.item(z)
							Element eElement = (Element) node
							String vnfParameterName = utils.getElementText(eElement, "name")
							if (!sdncResponseParamsToSkip.contains(vnfParameterName)) {
								String vnfParameterValue = utils.getElementText(eElement, "value")
								paramsMap.put("${vnfParameterName}", "${vnfParameterValue}")
							}
						}
					}
					
				// Parameters received from the request should overwrite any parameters received from SDNC
				if (vnfParamsMap != null) {
					for (Map.Entry<String, String> entry : vnfParamsMap.entrySet()) {
						String vnfKey = entry.getKey()
						String vnfValue = entry.getValue()
						paramsMap.put("$vnfKey", "$vnfValue")
					}
				}
				
				StringBuilder sbParams = new StringBuilder()
				def vfModuleParams = ""
				for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
					String paramsXml
					String paramName = entry.getKey()
					String paramValue = entry.getValue()
					paramsXml =
							"""<entry>
						<key>${paramName}</key>
						<value>${paramValue}</value>
					</entry>
					"""
		
					vfModuleParams = sbParams.append(paramsXml)
				}
				
				return vfModuleParams
				
			}
	

	/*
	 * VBNG specific method that parses VNF parameters passed in on the 
	 * incoming requests and SDNC parameters returned from SDNC get response
	 * and puts them into the format expected by VNF adapter.
	 * @param vnfParamsMap -  map of VNF parameters passed in the request body
	 * @param sdncGetResponse - response string from SDNC GET topology request
	 * @param vnfId
	 * @param vnfName
	 * @param vfModuleId
	 * @param vfModuleName
	 * @return a String of key/value entries for vfModuleParams
	 */
	
	protected String buildVfModuleParamsVbng(String vnfParams, String sdncGetResponse, String vnfId, String vnfName,
			String vfModuleId, String vfModuleName) {
				
		//Get SDNC Response Data
				
		String data = utils.getNodeXml(sdncGetResponse, "response-data")
		data = data.replaceAll("&lt;", "<")
		data = data.replaceAll("&gt;", ">")
	
		
	
		// Add-on data
		String vnfInfo =
			"""<entry>
				<key>vnf_id</key>
				<value>${vnfId}</value>
			</entry>
			<entry>
				<key>vnf_name</key>
				<value>${vnfName}</value>
			</entry>
			<entry>
				<key>vf_module_id</key>
				<value>${vfModuleId}</value>
			</entry>
			<entry>
				<key>vf_module_name</key>
				<value>${vfModuleName}</value>
			</entry>"""
	
		utils.logAudit("vnfInfo: " + vnfInfo)
		InputSource source = new InputSource(new StringReader(data))
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
		docFactory.setNamespaceAware(true)
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
		Document responseXml = docBuilder.parse(source)
	
	
		// Availability Zones Data
		String aZones = ""
		StringBuilder sbAZone = new StringBuilder()
		NodeList aZonesList = responseXml.getElementsByTagNameNS("*", "availability-zones")
		String aZonePosition = "0"
		for (int z = 0; z < aZonesList.getLength(); z++) {
			Node node = aZonesList.item(z)
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node
				String aZoneValue = utils.getElementText(eElement, "availability-zone")
				aZonePosition = z.toString()
				String aZoneXml =
					"""<entry>
						<key>availability_zone_${aZonePosition}</key>
						<value>${aZoneValue}</value>
					</entry>"""
				aZones = sbAZone.append(aZoneXml)
			}
		}
	
		// VNF Networks Data
		String vnfNetworkNetId = ""
		String vnfNetworkNetName = ""
		String vnfNetworkSubNetId = ""
		String vnfNetworkV6SubNetId = ""
		String vnfNetworkNetFqdn = ""
		String vnfNetworksSriovVlanFilters = ""
		StringBuilder sbNet = new StringBuilder()
		StringBuilder sbNet2 = new StringBuilder()
		StringBuilder sbNet3 = new StringBuilder()
		StringBuilder sbNet4 = new StringBuilder()
		StringBuilder sbNet5 = new StringBuilder()
		StringBuilder sbNet6 = new StringBuilder()
		NodeList vnfNetworkList = responseXml.getElementsByTagNameNS("*", "vnf-networks")
		for (int x = 0; x < vnfNetworkList.getLength(); x++) {
			Node node = vnfNetworkList.item(x)
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node
				String vnfNetworkKey = utils.getElementText(eElement, "network-role")
				String vnfNetworkNeutronIdValue = utils.getElementText(eElement, "neutron-id")
				String vnfNetworkNetNameValue = utils.getElementText(eElement, "network-name")
				String vnfNetworkSubNetIdValue = utils.getElementText(eElement, "subnet-id")
				String vnfNetworkV6SubNetIdValue = utils.getElementText(eElement, "ipv6-subnet-id")
				String vnfNetworkNetFqdnValue = utils.getElementText(eElement, "contrail-network-fqdn")
				String vnfNetworkNetIdXml =
						"""<entry>
							<key>${vnfNetworkKey}_net_id</key>
							<value>${vnfNetworkNeutronIdValue}</value>
						</entry>"""
				vnfNetworkNetId = sbNet.append(vnfNetworkNetIdXml)
				String vnfNetworkNetNameXml =
						"""<entry>
						<key>${vnfNetworkKey}_net_name</key>
						<value>${vnfNetworkNetNameValue}</value>
				</entry>"""
				vnfNetworkNetName = sbNet2.append(vnfNetworkNetNameXml)
				String vnfNetworkSubNetIdXml =
						"""<entry>
						<key>${vnfNetworkKey}_subnet_id</key>
						<value>${vnfNetworkSubNetIdValue}</value>
				</entry>"""
				vnfNetworkSubNetId = sbNet3.append(vnfNetworkSubNetIdXml)
				String vnfNetworkV6SubNetIdXml =
						"""<entry>
						<key>${vnfNetworkKey}_v6_subnet_id</key>
						<value>${vnfNetworkV6SubNetIdValue}</value>
				</entry>"""
				vnfNetworkV6SubNetId = sbNet5.append(vnfNetworkV6SubNetIdXml)
				String vnfNetworkNetFqdnXml =
						"""<entry>
						<key>${vnfNetworkKey}_net_fqdn</key>
						<value>${vnfNetworkNetFqdnValue}</value>
				</entry>"""
				vnfNetworkNetFqdn = sbNet4.append(vnfNetworkNetFqdnXml)
						
				NodeList sriovVlanFilterList = eElement.getElementsByTagNameNS("*","sriov-vlan-filter-list")
				StringBuffer sriovFilterBuf = new StringBuffer()
				String values = ""
				for(int i = 0; i < sriovVlanFilterList.getLength(); i++){
					Node node1 = sriovVlanFilterList.item(i)
					if (node1.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement1 = (Element) node1
						String value = utils.getElementText(eElement1, "sriov-vlan-filter")
						if (i != sriovVlanFilterList.getLength() - 1) {
							values = sriovFilterBuf.append(value + ",")
						}
						else {
							values = sriovFilterBuf.append(value)
						}
					}
				}
				if (!values.isEmpty()) {
						String vnfNetworkSriovVlanFilterXml =
								"""<entry>
						<key>${vnfNetworkKey}_ATT_VF_VLAN_FILTER</key>
						<value>${values}</value>
					</entry>"""
						vnfNetworksSriovVlanFilters = sbNet6.append(vnfNetworkSriovVlanFilterXml)
				}
			}
		}
	
		// VNF-VMS Data
		String vnfVMS = ""
		String vnfVMSPositions = ""
		String vmNetworks = ""
		String vmNetworksPositions = ""
		String vmNetworksPositionsV6 = ""
		String interfaceRoutePrefixes = ""
		def key
		def value
		def networkKey
		def networkValue
		def floatingIPKey
		def floatingIPKeyValue
		def floatingIPV6Key
		def floatingIPV6KeyValue
		StringBuilder sb = new StringBuilder()
		StringBuilder sbPositions = new StringBuilder()
		StringBuilder sbVmNetworks = new StringBuilder()
		StringBuilder sbNetworksPositions = new StringBuilder()
		StringBuilder sbInterfaceRoutePrefixes = new StringBuilder()
		StringBuilder sbNetworksPositionsV6 = new StringBuilder()

		NodeList vmsList = responseXml.getElementsByTagNameNS("*","vnf-vms")
		for (int x = 0; x < vmsList.getLength(); x++) {
			Node node = vmsList.item(x)
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node
				key = utils.getElementText(eElement, "vm-type")
				String values
				String position = "0"
				StringBuilder sb1 = new StringBuilder()
				NodeList valueList = eElement.getElementsByTagNameNS("*","vm-names")
				NodeList vmNetworksList = eElement.getElementsByTagNameNS("*","vm-networks")
				for(int i = 0; i < valueList.getLength(); i++){
					Node node1 = valueList.item(i)
					if (node1.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement1 = (Element) node1
						value = utils.getElementText(eElement1, "vm-name")
						if (i != valueList.getLength() - 1) {
							values = sb1.append(value + ",")
						}
						else {
							values = sb1.append(value)
						}
						position = i.toString()
						String vnfPositionXml =
								"""<entry>
								<key>${key}_name_${position}</key>
								<value>${value}</value>
							</entry>"""
						vnfVMSPositions = sbPositions.append(vnfPositionXml)
					}
				}
				for(int n = 0; n < vmNetworksList.getLength(); n++){
					String floatingIpKeyValueStr = ""
					String floatingIpV6KeyValueStr = ""
					Node nodeNetworkKey = vmNetworksList.item(n)
					if (nodeNetworkKey.getNodeType() == Node.ELEMENT_NODE) {
						Element eElementNetworkKey = (Element) nodeNetworkKey
						String ipAddressValues
						String ipV6AddressValues
						String networkPosition = "0"
						StringBuilder sb2 = new StringBuilder()
						StringBuilder sb3 = new StringBuilder()
						StringBuilder sb4 = new StringBuilder()
						networkKey = utils.getElementText(eElementNetworkKey, "network-role")
						floatingIPKey = key + '_' + networkKey + '_floating_ip'
						floatingIPKeyValue = utils.getElementText(eElementNetworkKey, "floating-ip")
						if(!floatingIPKeyValue.isEmpty()){
							floatingIpKeyValueStr = """<entry>
								<key>$floatingIPKey</key>
								<value>$floatingIPKeyValue</value>
							</entry>"""
						}
						floatingIPV6Key = key + '_' + networkKey + '_floating_v6_ip'
						floatingIPV6KeyValue = utils.getElementText(eElementNetworkKey, "floating-ip-v6")
						if(!floatingIPV6KeyValue.isEmpty()){
							floatingIpV6KeyValueStr = """<entry>
								<key>$floatingIPV6Key</key>
								<value>$floatingIPV6KeyValue</value>
							</entry>"""
						}
						NodeList networkIpsList = eElementNetworkKey.getElementsByTagNameNS("*","network-ips")
						for(int a = 0; a < networkIpsList.getLength(); a++){
							Node ipAddress = networkIpsList.item(a)
							if (ipAddress.getNodeType() == Node.ELEMENT_NODE) {
								Element eElementIpAddress = (Element) ipAddress
								String ipAddressValue = utils.getElementText(eElementIpAddress, "ip-address")
								if (a != networkIpsList.getLength() - 1) {
									ipAddressValues = sb2.append(ipAddressValue + ",")
								}
								else {
									ipAddressValues = sb2.append(ipAddressValue)
								}
								networkPosition = a.toString()
								String vmNetworksPositionsXml =
												"""<entry>
										<key>${key}_${networkKey}_ip_${networkPosition}</key>
										<value>${ipAddressValue}</value>
									</entry>"""
								vmNetworksPositions = sbNetworksPositions.append(vmNetworksPositionsXml)
							}
						}
						vmNetworksPositions = sbNetworksPositions.append(floatingIpKeyValueStr).append(floatingIpV6KeyValueStr)
							
						String vmNetworksXml =
										"""<entry>
								<key>${key}_${networkKey}_ips</key>
								<value>${ipAddressValues}</value>
							</entry>"""
						vmNetworks = sbVmNetworks.append(vmNetworksXml)
								
						NodeList interfaceRoutePrefixesList = eElementNetworkKey.getElementsByTagNameNS("*","interface-route-prefixes")
						String interfaceRoutePrefixValues = sb3.append("[")
								
						for(int a = 0; a < interfaceRoutePrefixesList.getLength(); a++){
							Node interfaceRoutePrefix = interfaceRoutePrefixesList.item(a)
							if (interfaceRoutePrefix.getNodeType() == Node.ELEMENT_NODE) {
								Element eElementInterfaceRoutePrefix = (Element) interfaceRoutePrefix
								String interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix-cidr")
								if (interfaceRoutePrefixValue == null || interfaceRoutePrefixValue.isEmpty()) {
									interfaceRoutePrefixValue = utils.getElementText(eElementInterfaceRoutePrefix, "interface-route-prefix")
								}
								if (a != interfaceRoutePrefixesList.getLength() - 1) {
									interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}" + ",")
								}
								else {
									interfaceRoutePrefixValues = sb3.append("{\"interface_route_table_routes_route_prefix\": \"" + interfaceRoutePrefixValue + "\"}")
								}
							}
						}
						interfaceRoutePrefixValues = sb3.append("]")
						if (interfaceRoutePrefixesList.getLength() > 0) {
							String interfaceRoutePrefixesXml =
													"""<entry>
											<key>${key}_${networkKey}_route_prefixes</key>
											<value>${interfaceRoutePrefixValues}</value>
										</entry>"""					
							interfaceRoutePrefixes = sbInterfaceRoutePrefixes.append(interfaceRoutePrefixesXml)
						}
							
						NodeList networkIpsV6List = eElementNetworkKey.getElementsByTagNameNS("*","network-ips-v6")
						for(int a = 0; a < networkIpsV6List.getLength(); a++){
							Node ipV6Address = networkIpsV6List.item(a)
							if (ipV6Address.getNodeType() == Node.ELEMENT_NODE) {
								Element eElementIpV6Address = (Element) ipV6Address
								String ipV6AddressValue = utils.getElementText(eElementIpV6Address, "ip-address-ipv6")
								if (a != networkIpsV6List.getLength() - 1) {
									ipV6AddressValues = sb4.append(ipV6AddressValue + ",")
								}
								else {
									ipV6AddressValues = sb4.append(ipV6AddressValue)
								}
								networkPosition = a.toString()
								String vmNetworksPositionsV6Xml =
										"""<entry>
										<key>${key}_${networkKey}_v6_ip_${networkPosition}</key>
										<value>${ipV6AddressValue}</value>
										</entry>"""
								vmNetworksPositionsV6 = sbNetworksPositionsV6.append(vmNetworksPositionsV6Xml)
							}
						}
						String vmNetworksV6Xml =
								"""<entry>
								<key>${key}_${networkKey}_v6_ips</key>
								<value>${ipV6AddressValues}</value>
							</entry>"""
						vmNetworks = sbVmNetworks.append(vmNetworksV6Xml)
					}
				}
				String vnfXml =
								"""<entry>
				<key>${key}_names</key>
				<value>${values}</value>
					</entry>"""
				vnfVMS = sb.append(vnfXml)
			}
		}
	//SDNC Response Params
		String sdncResponseParams = ""
		List<String> sdncResponseParamsToSkip = ["vnf_id", "vf_module_id", "vnf_name", "vf_module_name"]
		String vnfParamsChildNodes = utils.getChildNodes(data, "vnf-parameters")
		if(vnfParamsChildNodes == null || vnfParamsChildNodes.length() < 1){
			// No SDNC params
		}else{
			NodeList paramsList = responseXml.getElementsByTagNameNS("*", "vnf-parameters")
			for (int z = 0; z < paramsList.getLength(); z++) {
				Node node = paramsList.item(z)
				Element eElement = (Element) node
				String vnfParameterName = utils.getElementText(eElement, "vnf-parameter-name")
				if (!sdncResponseParamsToSkip.contains(vnfParameterName)) {
					String vnfParameterValue = utils.getElementText(eElement, "vnf-parameter-value")
					String paraEntry =
						"""<entry>
							<key>${vnfParameterName}</key>
							<value>${vnfParameterValue}</value>
						</entry>"""
					sdncResponseParams = sb.append(paraEntry)
				}
			}
		}
	
	
		def vfModuleParams = """
		${vnfInfo}
		${aZones}
		${vnfNetworkNetId}
		${vnfNetworkNetName}
		${vnfNetworkSubNetId}
		${vnfNetworkV6SubNetId}
		${vnfNetworkNetFqdn}
		${vnfNetworksSriovVlanFilters}
        ${vnfVMS}
        ${vnfVMSPositions}
		${vmNetworks}
		${vmNetworksPositions}
		${vmNetworksPositionsV6}
		${interfaceRoutePrefixes}
		${vnfParams}
		${sdncResponseParams}"""
			
		return vfModuleParams
			
	}
	
}