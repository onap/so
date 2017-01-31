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

import groovy.util.Node;
import groovy.xml.XmlUtil

import java.text.SimpleDateFormat

import org.apache.commons.codec.binary.Base64



import org.apache.commons.codec.binary.Base64
import org.camunda.bpm.engine.runtime.Execution;
import org.slf4j.MDC;
import org.w3c.dom.Element
//import org.w3c.dom.NodeList

import org.openecomp.mso.bpmn.core.BPMNLogger
import org.openecomp.mso.logger.MessageEnum
import org.openecomp.mso.logger.MsoLogger
import org.openecomp.mso.utils.CryptoUtils
import org.openecomp.mso.bpmn.core.xml.XmlTool

class MsoUtils {
	def initializeEndPoints(execution){
		// use this placeholder to initialize end points, if called independently, this need to be set
		execution.setVariable("AAIEndPoint","http://localhost:8080/SoapUIMocks")			
	}
	def getNodeText(xmlInput,element){
		def rtn=null
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			rtn= xml.'**'.find{node->node.name()==element}.text()
		}
		return rtn
	}
	def getMultNodes(xmlInput, element){
		def nodes=null
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			nodes = xml.'**'.findAll{ node-> node.name() == element }*.text()
		}
		return nodes
	}
	def getNodeText1(xmlInput,element){
		def rtn=null
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			rtn= xml.'**'.find{node->node.name()==element}
			if (rtn != null){
				rtn=rtn.text()
			}
		}
		return rtn
	}	
	def getNodeXml(xmlInput,element){
		return getNodeXml(xmlInput, element, true)
	}
	def getNodeXml(xmlInput,element,incPreamble){
		def fxml= new XmlSlurper().parseText(xmlInput)
		def nodeToSerialize = fxml.'**'.find {it.name() == element}
		if(nodeToSerialize==null){
			return ""
		}
		def nodeAsText = XmlUtil.serialize(nodeToSerialize)
		if (!incPreamble) {
			nodeAsText = removeXmlPreamble(nodeAsText)
		}
		return nodeAsText		
	}
	def nodeExists(xmlInput,element){
		try {
			def fxml= new XmlSlurper().parseText(xmlInput)
			def nodeToSerialize = fxml.'**'.find {it.name() == element}
			return nodeToSerialize!=null
		} catch(Exception e) {
			return false
		}
	}

	
	/***** Utilities when using XmlParser *****/
	
	/**
	 * Convert a Node into a String by deserializing it and formatting it.
	 *
	 * @param node Node to be converted.
	 * @return the Node as a String.
	 */
	def String nodeToString(Node node) {
		def String nodeAsString = groovy.xml.XmlUtil.serialize(node)
		nodeAsString = removeXmlPreamble(nodeAsString)
		return formatXml(nodeAsString)
	}
	
	/**
	 * Get the specified child Node of the specified parent. If there are
	 * multiple children of the same name, only the first one is returned.
	 * If there are no children with the specified name, 'null' is returned.
	 * 
	 * @param parent Parent Node in which to find a child.
	 * @param childNodeName Name of the child Node to get.
	 * @return the (first) child Node with the specified name or 'null'
	 * if a child Node with the specified name does not exist.
	 */
	def Node getChildNode(Node parent, String childNodeName) {
		def NodeList nodeList = getIdenticalChildren(parent, childNodeName)
		if (nodeList.size() == 0) {
			return null
		} else {
			return nodeList.get(0)
		}
	}
	
	/**
	 * Get the textual value of the specified child Node of the specified parent.
	 * If there are no children with the specified name, 'null' is returned.
	 * 
	 * @param parent Parent Node in which to find a child.
	 * @param childNodeName Name of the child Node whose value to get.
	 * @return the textual value of child Node with the specified name or 'null'
	 * if a child Node with the specified name does not exist.
	 */
	def String getChildNodeText(Node parent, String childNodeName) {
		def Node childNode = getChildNode(parent, childNodeName)
		if (childNode == null) {
			return null
		} else {
			return childNode.text()
		}
	}
	
	/**
	 * Get all of the child nodes from the specified parent that have the
	 * specified name.  The returned NodeList could be empty.
	 * 
	 * @param parent Parent Node in which to find children.
	 * @param childNodeName Name of the children to get.
	 * @return a NodeList of all the children from the parent with the specified
	 * name. The list could be empty.
	 */
	def NodeList getIdenticalChildren(Node parent, String childNodeName) {
		return (NodeList) parent.get(childNodeName)
	}

	/***** End of Utilities when using XmlParser *****/

	
	/** these are covered under the common function above**/
	def getSubscriberName(xmlInput,element){
		def rtn=null
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			rtn= xml.'**'.find{node->node.name()==element}.text()
		} 
		return rtn
	}
	def getTenantInformation(xmlInput,element){
		def xml= new XmlSlurper().parseText(xmlInput)
		def nodeToSerialize = xml.'**'.find {it.name() == 'service-information'}
		def nodeAsText = XmlUtil.serialize(nodeToSerialize)
		return nodeAsText
	}
	def getServiceInstanceId(xmlInput,element){
		def xml= new XmlSlurper().parseText(xmlInput)
		return ( xml.'**'.find{node->node.name()==element}.text() )
	}
	//for aai tenant url
	def searchResourceLink(xmlInput, resourceType){
		def fxml= new XmlSlurper().parseText(xmlInput)
		def element = fxml.'**'.find {it.'resource-type' == resourceType}
		return (element == null) ? null : element.'resource-link'.text()
	}

	def searchMetaData(xmlInput, searchName, searchValue){
		def fxml= new XmlSlurper().parseText(xmlInput)
		def ret = fxml.'**'.find {it.metaname.text() == searchName && it.metaval.text() == searchValue}
		if(ret != null){
			return ret.parent().parent()
		}
		return ret
	}
	
	def searchMetaDataNode(fxml, searchName, searchValue){
		def ret = fxml.'**'.find {it.metaname.text() == searchName && it.metaval.text() == searchValue}
		if(ret != null){
			return ret.parent().parent()
		}
		return ret
	}
	
	// for Trinity L3 delete bonding
	def getPBGFList(isDebugLogEnabled, xmlInput){
		xmlInput = xmlInput.replaceAll("&amp;", "&")
		xmlInput = xmlInput.replaceAll("&lt;", "<")
		xmlInput = xmlInput.replaceAll("&gt;", ">")
		log("DEBUG", "getPBGFList: xmlInput " + xmlInput,isDebugLogEnabled)
		ArrayList myNodes = new ArrayList()
		if(nodeExists(xmlInput,"nbnc-response-information")){
		def respInfo=getNodeXml(xmlInput,"nbnc-response-information", false)
		if(respInfo!=null){
			def fxml= new XmlSlurper().parseText(respInfo)
			def myNode
			fxml.'virtual-datacenter-list'.each { vdc ->
				//we only want to add one BFG per VDC
				log("DEBUG", "vdc " + vdc.text(),isDebugLogEnabled)
				myNode = vdc.'**'.find {it.'border-element-tangibility'.text() =~ /PHYSICAL/}
				log("DEBUG", "myNode " + myNode.text(),isDebugLogEnabled)
				myNodes.add(XmlUtil.serialize(myNode))
			}
		}
				
			return myNodes
		}else{
			return null
		}
	}
	
	def getPBGFList(xmlInput){
		getPBGFList("false", xmlInput)
	}
	
	def String decodeXML(xml) {
		def String decodedXml = xml.replaceAll("&amp;", "&")
		decodedXml = decodedXml.replaceAll("&lt;", "<")
		decodedXml = decodedXml.replaceAll("&gt;", ">")
	}
	
	def getMetaVal(node, name){
		try{
			return node.'**'.find {it.metaname.text() == name}.metaval.text()
		}catch(Exception e){
			return null
		}
	}
	def getRelationshipVal(node, name){
		try{
			return node.'**'.find {it.'relationship-key'.text() == name}.'relationship-value'.text()
		}catch(Exception e){
			return null
		}
	}

	
	def log(logmode,logtxt,isDebugLogEnabled="false"){
		MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
		if ("INFO"==logmode) {
			msoLogger.info(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, logtxt);
		} else if ("WARN"==logmode) {
			msoLogger.warn (MessageEnum.BPMN_GENERAL_WARNING, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, logtxt);
		} else if ("ERROR"==logmode) {
		    msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, logtxt);
			
		} else {
			BPMNLogger.debug(isDebugLogEnabled, logtxt);
		}
	}
	
	def logContext(requestId, serviceInstanceId){
	    MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
		msoLogger.setLogContext(requestId, serviceInstanceId);
	}
	
	def logMetrics(elapsedTime, logtxt){
		MsoLogger metricsLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
		
		metricsLogger.recordMetricEvent (elapsedTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
			logtxt, "BPMN", MsoLogger.getServiceName(), null);
	}
	
	def logAudit(logtxt){
		MsoLogger auditLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
		long startTime = System.currentTimeMillis();
		
		auditLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logtxt);
	}
	
	// headers: header - name-value
	def getHeaderNameValue(xmlInput, nameAttribute){
		def rtn=null
		if(xmlInput!=null){
			def xml= new XmlSlurper().parseText(xmlInput)
			rtn= xml.'**'.find {header->header.'@name'.text() == nameAttribute}.'@value'
		}
		return rtn
	}
	
	/**
	 * Gets the children of the specified element.
	 */
	public String getChildNodes(xmlInput, element) {
		def xml= new XmlSlurper().parseText(xmlInput)
		def thisElement = xml.'**'.find {it.name() == element}
		StringBuilder out = new StringBuilder()
		if (thisElement != null) {
			thisElement.children().each() {
				String nodeAsText = removeXmlPreamble(XmlUtil.serialize(it))
				if (out.length() > 0) {
					out.append(System.lineSeparator())
				}
				out.append(nodeAsText)
			}
		}
		return out.toString();
	}
	
	/**
	 * Encodes a value so it can be used inside an XML text element.
	 * @param s the string to encode
	 * @return the encoded string
	 */
	public String xmlEncode(Object value) {
		return XmlTool.encode(value)
	}
	
	/**
	 * Encodes a value so it can be used inside an XML attribute.
	 * @param s the string to encode
	 * @return the encoded string
	 */
	public String xmlEncodeAttr(Object value) {
		return XmlTool.encodeAttr(value)
	}
	
	/**
	 * Decodes XML entities in a string value
	 * @param value a value with embedded XML entities
	 * @return the decoded string
	 */
	public String xmlDecode(Object value) {
		return XmlTool.decode(value)
	}
	
	/**
	 * Removes the preamble, if present, from an XML document.
	 * Also, for historical reasons, this also trims leading and trailing
	 * whitespace from the resulting document.  TODO: remove the trimming
	 * and fix unit tests that depend on EXACT xml format.
	 * @param xml the XML document
	 * @return a possibly modified document
	 */
	public String removeXmlPreamble(def xml) {
		if (xml == null) {
			return null
		}

		return XmlTool.removePreamble(xml).trim()
	}

	/**
	 * Removes namespaces and namespace declarations from an XML document.
	 * @param xml the XML document
	 * @return a possibly modified document
	 */
	public String removeXmlNamespaces(def xml) {
		return XmlTool.removeNamespaces(xml);
	}

	/**
	 * Use formatXml instead.  Note: this method inserts an XML preamble.
	 */
	@Deprecated
	def formatXML(xmlInput) {
		def parseXml = null
		def formatXml = null
		if (xmlInput !=null) {
			parseXml = new XmlParser().parseText(xmlInput)
			formatXml = XmlUtil.serialize(parseXml)
		}
	}

	/**
	 * Reformats an XML document. The result will not contain an XML preamble
	 * or a trailing newline.
	 * @param xml the XML document
	 * @return a reformatted document
	 */
	public String formatXml(def xml) {
		return XmlTool.normalize(xml);
	}
	
	// build single elements
	def buildElements(xmlInput, elementList, parentName) {
		String var = ""
		def xmlBuild = ""
		if (parentName != "") {
		   xmlBuild += "<tns2:"+parentName+">"
		}   
		if (xmlInput != null) {
			   for (element in elementList) {
			      def xml= new XmlSlurper().parseText(xmlInput)
			      var = xml.'**'.find {it.name() == element}
				  if (var != null) {
			         xmlBuild += "<tns2:"+element+">"+var.toString()+"</tns2:"+element+">"
				  } 
			   }
		}
		if (parentName != "") {
		   xmlBuild += "</tns2:"+parentName+">"
		}   
		return xmlBuild
	}
	
	// build the Unbounded elements
	def buildElementsUnbounded(xmlInput, elementList, parentName) {
		def varParents = ""
		def var = ""
		def xmlBuildUnbounded = ""
		if (xmlInput != null) {
			def xml= new XmlSlurper().parseText(xmlInput)
			varParents = xml.'**'.findAll {it.name() == parentName}
			//println " Unbounded ${parentName} - varParent.Size() - " + varParents.size()
			for (i in 0..varParents.size()-1) {
				if (parentName != "") {
					xmlBuildUnbounded += "<tns2:"+parentName+">"
				 }
				for (element in elementList) {
					var = varParents[i].'*'.find {it.name() == element}
				   if (var != null) {
					  xmlBuildUnbounded += "<tns2:"+element+">"+var.toString()+"</tns2:"+element+">"
					  //println " i = " + i + ", element: " + element + " = " + var.toString()
				   }
				}
				if (parentName != "") {
					xmlBuildUnbounded += "</tns2:"+parentName+">"
				 }
			}
		}
		return xmlBuildUnbounded
	 }
	
	// Build internet-evc-access-information
	def buildInternetEvcAccessInformation(xmlInput) {
		def elementsInternetEvcAccessInformationList = ["internet-evc-speed-value", "internet-evc-speed-units", "ip-version"]
		def rebuildInternetEvcAccess = ''
		if (xmlInput != null) {
			rebuildInternetEvcAccess = buildElements(xmlInput, elementsInternetEvcAccessInformationList, "internet-evc-access-information")
		}
		return rebuildInternetEvcAccess
	}
	
    // Build internet-service-change-details
	def buildInternetServiceChangeDetails(xmlInput) {
		def rebuildInternetServiceChangeDetails = ""
		if (xmlInput != null) {
		    try { // optional
				def internetServiceChangeDetails = getNodeXml(xmlInput, "internet-service-change-details").drop(38).trim()
				rebuildInternetServiceChangeDetails = "<tns:internet-service-change-details>"
				rebuildInternetServiceChangeDetails += buildElements(internetServiceChangeDetails, ["internet-evc-speed-value"], "")
				rebuildInternetServiceChangeDetails += buildElements(internetServiceChangeDetails, ["internet-evc-speed-units"], "")
				def tProvidedV4LanPublicPrefixesChangesList = ["request-index", "v4-next-hop-address", "v4-lan-public-prefix", "v4-lan-public-prefix-length"]
				rebuildInternetServiceChangeDetails += buildElementsUnbounded(internetServiceChangeDetails, tProvidedV4LanPublicPrefixesChangesList, "t-provided-v4-lan-public-prefixes")
				try { // optional
				  def tProvidedV6LanPublicPrefixesChangesList = ["request-index", "v6-next-hop-address", "v6-lan-public-prefix", "v6-lan-public-prefix-length"]
				  rebuildInternetServiceChangeDetails += buildElementsUnbounded(internetServiceChangeDetails, tProvidedV6LanPublicPrefixesChangesList, "t-provided-v6-lan-public-prefixes")
				} catch (Exception e) {
					log("ERROR"," Optional - Exception INTERNET-SERVICE-CHANGE-DETAILS 't-provided-v6-lan-public-prefixes ")
				}
				rebuildInternetServiceChangeDetails += "</tns:internet-service-change-details>"
			} catch (Exception e) {
				log("ERROR", " Optional - Exception INTERNET-SERVICE-CHANGE-DETAILS 'internet-service-change-details' ")
			}
		}
	    return rebuildInternetServiceChangeDetails
	}		
 	
	// Build vr-lan 
	def buildVrLan(xmlInput) {
		
		def rebuildVrLan = ''
		if (xmlInput != null) {
			
			rebuildVrLan = "<tns2:vr-lan>"
			def vrLan = getNodeXml(xmlInput, "vr-lan").drop(38).trim()
			rebuildVrLan += buildElements(vrLan, ["routing-protocol"], "")
			
			// vr-lan-interface
			def rebuildVrLanInterface = "<tns2:vr-lan-interface>"
			def vrLanInterface = getNodeXml(vrLan, "vr-lan-interface").drop(38).trim()
			rebuildVrLanInterface += buildVrLanInterfacePartial(vrLanInterface)
			
			 // dhcp
			 def dhcp = getNodeXml(vrLan, "dhcp").drop(38).trim()
			 def rebuildDhcp = buildDhcp(dhcp)
			 rebuildVrLanInterface += rebuildDhcp
			 
			 // pat
			 def pat = getNodeXml(vrLan, "pat").drop(38).trim()
			 def rebuildPat = buildPat(pat)
			 rebuildVrLanInterface += rebuildPat
			 
			 // nat
			 def rebuildNat = ""
			 try { // optional
				def nat = getNodeXml(vrLan, "nat").drop(38).trim()
				rebuildNat = buildNat(nat)
			 } catch (Exception e) {
				 log("ERROR", " Optional - Exception 'nat' ")
			 }
			 rebuildVrLanInterface += rebuildNat
										
			 // firewall-lite
			 def firewallLite = getNodeXml(vrLan, "firewall-lite").drop(38).trim()
			 def rebuildFirewallLite = buildFirewallLite(firewallLite)
			 rebuildVrLanInterface += rebuildFirewallLite
			 
			 // static-routes
			 def rebuildStaticRoutes = ""
			 try { // optional 
				 def staticRoutes = getNodeXml(vrLan, "static-routes").drop(38).trim()
				 rebuildStaticRoutes = buildStaticRoutes(staticRoutes)
			} catch (Exception e) {
				 log("ERROR", " Optional - Exception 'static-routes' ")
			}
			rebuildVrLanInterface += rebuildStaticRoutes
			 
		   rebuildVrLan += rebuildVrLanInterface
		   rebuildVrLan += "</tns2:vr-lan-interface>"
		   rebuildVrLan += "</tns2:vr-lan>"
			
		}
		log("DEBUG", " rebuildVrLan - " + rebuildVrLan)
		return rebuildVrLan 		
	}
	
	// Build vr-lan-interface
	def buildVrLanInterfacePartial(xmlInput) {
		def rebuildingVrLanInterface = ''
		if (xmlInput != null) {
			def vrLanInterfaceList = ["vr-designation", "v4-vr-lan-prefix", "v4-vr-lan-address", "v4-vr-lan-prefix-length", "v6-vr-lan-prefix", "v6-vr-lan-address", "v6-vr-lan-prefix-length", "v4-vce-loopback-address", "v6-vce-wan-address"]
			rebuildingVrLanInterface += buildElements(xmlInput, vrLanInterfaceList, "")
			rebuildingVrLanInterface += "<tns2:v4-public-lan-prefixes>"
			try { // optional
				def tProvidedV4LanPublicPrefixes = getNodeXml(xmlInput, "v4-public-lan-prefixes").drop(38).trim()
				def tProvidedV4LanPublicPrefixesList = ["request-index", "v4-next-hop-address", "v4-lan-public-prefix", "v4-lan-public-prefix-length" ]
				rebuildingVrLanInterface += buildElementsUnbounded(xmlInput, tProvidedV4LanPublicPrefixesList, "t-provided-v4-lan-public-prefixes")
			} catch (Exception ex) {
				log("ERROR", " Optional - Exception VR-LAN INTERFACE 'v4-public-lan-prefixes' ")
			}
			rebuildingVrLanInterface += "</tns2:v4-public-lan-prefixes>"
			rebuildingVrLanInterface += "<tns2:v6-public-lan-prefixes>"
			try { // optional
				def tProvidedV6LanPublicPrefixes = getNodeXml(xmlInput, "v6-public-lan-prefixes").drop(38).trim()
				def tProvidedV6LanPublicPrefixesList = ["request-index", "v6-next-hop-address", "v6-lan-public-prefix", "v6-lan-public-prefix-length" ]
				rebuildingVrLanInterface += buildElementsUnbounded(xmlInput, tProvidedV6LanPublicPrefixesList, "t-provided-v6-lan-public-prefixes")
			} catch (Exception e) {
				log("ERROR", " Optional - Exception VR-LAN INTERFACE 'v6-public-lan-prefixes' ")
			}
			rebuildingVrLanInterface += "</tns2:v6-public-lan-prefixes>"
		}
		log("DEBUG", " rebuildingVrLanInterface - " + rebuildingVrLanInterface)
		return rebuildingVrLanInterface
	}
	
	// Build dhcp
	def buildDhcp(xmlInput) {
		def rebuildingDhcp = ''
		if (xmlInput != null) {
			def dhcpData = new XmlSlurper().parseText(xmlInput)
			rebuildingDhcp = "<tns2:dhcp>"
			def dhcpList1 = ["v4-dhcp-server-enabled", "v6-dhcp-server-enabled", "use-v4-default-pool", "v4-dhcp-default-pool-prefix", "v4-dhcp-default-pool-prefix-length"]
			rebuildingDhcp += buildElements(xmlInput, dhcpList1, "")
			try { // optional
				def excludedV4DhcpAddressesFromDefaultPoolList = ["excluded-v4-address"]
				rebuildingDhcp += buildElementsUnbounded(xmlInput, excludedV4DhcpAddressesFromDefaultPoolList, "excluded-v4-dhcp-addresses-from-default-pool")
			} catch (Exception e) {
				log("ERROR", " Optional - Exception DHCP 'excluded-v4-dhcp-addresses-from-default-pool' ")
			}
			try { // optional
				def v4DhcpPools = dhcpData.'**'.findAll {it.name() == "v4-dhcp-pools"}
				def v4DhcpPoolsSize = v4DhcpPools.size()
				// println " v4DhcpPoolsSize = " + v4DhcpPools.size()
				for (i in 0..v4DhcpPoolsSize-1) {
					def v4DhcpPool = v4DhcpPools[i]
					def v4DhcpPoolXml = XmlUtil.serialize(v4DhcpPool)
					rebuildingDhcp += "<tns2:v4-dhcp-pools>"
					def v4DhcpPoolsList1 = ["v4-dhcp-pool-prefix", "v4-dhcp-pool-prefix-length" ]
					rebuildingDhcp += buildElements(v4DhcpPoolXml, v4DhcpPoolsList1, "")
					try { // optional
					   def excludedV4AddressesList = ["excluded-v4-address"]
					   rebuildingDhcp += buildElementsUnbounded(v4DhcpPoolXml, excludedV4AddressesList, "excluded-v4-addresses")
					} catch (Exception e) {
					   log("ERROR", " Optional - Exception DHCP 'excluded-v4-addresses' ")
					}
					def v4DhcpPoolsList2 = ["v4-dhcp-relay-gateway-address", "v4-dhcp-relay-next-hop-address"]
					rebuildingDhcp += buildElements(v4DhcpPoolXml, v4DhcpPoolsList2, "")
					rebuildingDhcp += "</tns2:v4-dhcp-pools>"
				 }
			 } catch (Exception e) {
				  log("ERROR"," Optional - Exception DHCP 'v4-dhcp-pools' ")
			 }
			 def dhcpList2 = ["use-v6-default-pool", "v6-dhcp-default-pool-prefix", "v6-dhcp-default-pool-prefix-length"]
			 rebuildingDhcp += buildElements(xmlInput, dhcpList2, "")
			 try { // optional
				 def excludedV6DhcpAddressesFromDdefaultPoolList = ["excluded-v6-address"]
				 rebuildingDhcp += buildElementsUnbounded(xmlInput, excludedV6DhcpAddressesFromDdefaultPoolList, "excluded-v6-dhcp-addresses-from-default-pool")
			 } catch (Exception e) {
			   log("ERROR", " Optional - Exception DHCP 'excluded-v6-dhcp-addresses-from-default-pool' ")
			 }
			 try { // optional
				 def v6DhcpPools = dhcpData.'**'.findAll {it.name() == "v6-dhcp-pools"}
				 def v6DhcpPoolsSize = v6DhcpPools.size()
				 //println " v6DhcpPoolsSize = " + v6DhcpPools.size()
				 for (i in 0..v6DhcpPoolsSize-1) {
					def v6DhcpPool = v6DhcpPools[i]
					def v6DhcpPoolXml = XmlUtil.serialize(v6DhcpPool)
					rebuildingDhcp += "<tns2:v6-dhcp-pools>"
					def v6DhcpPoolsList1 = ["v6-dhcp-pool-prefix", "v6-dhcp-pool-prefix-length"]
					rebuildingDhcp += buildElements(v6DhcpPoolXml, v6DhcpPoolsList1, "")
					try { // optional
						def excludedV6AddressesList = ["excluded-v6-address"]
						rebuildingDhcp += buildElementsUnbounded(v6DhcpPoolXml, excludedV6AddressesList, "excluded-v6-addresses")
					} catch (Exception e) {
							 log("ERROR", " Optional - Exception DHCP 'excluded-v6-addresses' ")
					}
					def v6DhcpPoolsList2 = ["v6-dhcp-relay-gateway-address", "v6-dhcp-relay-next-hop-address"]
					rebuildingDhcp += buildElements(v6DhcpPoolXml, v6DhcpPoolsList2, "")
					rebuildingDhcp += "</tns2:v6-dhcp-pools>"
				 }
			 } catch (Exception e) {
				 log("ERROR", " Optional - Exception DHCP 'v6-dhcp-pools' ")
			 }
			 rebuildingDhcp += "</tns2:dhcp>"
		}	 
		log("DEBUG", " rebuildingDhcp - " + rebuildingDhcp)
		return rebuildingDhcp
	}	

	// Build pat
	def buildPat(xmlInput) {
		 def rebuildingPat = ''
		 if (xmlInput != null) {
			 rebuildingPat = "<tns2:pat>"
			 def patList = ["v4-pat-enabled", "use-v4-default-pool", "v4-pat-default-pool-prefix", "v4-pat-default-pool-prefix-length"]
			 rebuildingPat += buildElements(xmlInput, patList, "")
			 try { // optional
				 def v4PatPools = getNodeXml(xmlInput, "v4-pat-pools").drop(38).trim()
				 def v4PatPoolsList = ["v4-pat-pool-prefix", "v4-pat-pool-prefix-length", "v4-pat-pool-next-hop-address"]
				 rebuildingPat += buildElementsUnbounded(xmlInput, v4PatPoolsList, "v4-pat-pools")
			 } catch (Exception e) {
				log("ERROR", " Optional - Exception 'v4-pat-pool-next-hop-address' ")
			 }
			 rebuildingPat += "</tns2:pat>"
		 }
		 log("DEBUG", " rebuildingPat - " + rebuildingPat)
	     return rebuildingPat
    }
	
	// Build nat
	def buildNat(xmlInput) {
		def rebuildingNat = ''
		if (xmlInput != null) {
			rebuildingNat = "<tns2:nat>"
			rebuildingNat += buildElements(xmlInput, ["v4-nat-enabled"], "")
			try { // optional
			 def v4NatMappingEntries = getNodeXml(xmlInput, "v4-nat-mapping-entries").drop(38).trim()
			 def v4NatMappingEntriesList = ["v4-nat-internal", "v4-nat-next-hop-address", "v4-nat-external"]
			 rebuildingNat += buildElementsUnbounded(xmlInput, v4NatMappingEntriesList, "v4-nat-mapping-entries")
			} catch (Exception e) {
			   log("ERROR", " Optional - Exception 'v4-nat-external' ")
			}
			rebuildingNat += "</tns2:nat>"
		}
		log("DEBUG", " rebuildingNat - " + rebuildingNat)
	    return rebuildingNat
	}				
	
	// Build firewall-lite
	def buildFirewallLite(xmlInput) {
		def rebuildingFirewallLite = ''
		
		if (xmlInput != null) {
			
			def firewallLiteData = new XmlSlurper().parseText(xmlInput)
			rebuildingFirewallLite = "<tns2:firewall-lite>"
			def firewallLiteList = ["stateful-firewall-lite-v4-enabled", "stateful-firewall-lite-v6-enabled"]
			rebuildingFirewallLite += buildElements(xmlInput, firewallLiteList, "")
			
			 try { // optional
				 def v4FirewallPacketFilters = firewallLiteData.'**'.findAll {it.name() == "v4-firewall-packet-filters"}
				 def v4FirewallPacketFiltersSize = v4FirewallPacketFilters.size()
				 //println " v4FirewallPacketFiltersSize = " + v4FirewallPacketFilters.size()
				 for (i in 0..v4FirewallPacketFiltersSize-1) {
			       def v4FirewallPacketFilter = v4FirewallPacketFilters[i]
			       def v4FirewallPacketFilterXml = XmlUtil.serialize(v4FirewallPacketFilter)
				   rebuildingFirewallLite += "<tns2:v4-firewall-packet-filters>"
				   def v4FirewallPacketFiltersList = ["v4-firewall-prefix", "v4-firewall-prefix-length", "allow-icmp-ping"]
				   rebuildingFirewallLite += buildElements(v4FirewallPacketFilterXml, v4FirewallPacketFiltersList, "")
				   try {  // optional
			          def udpPortsList = ["port-number"]
					  rebuildingFirewallLite += buildElementsUnbounded(v4FirewallPacketFilterXml, udpPortsList, "udp-ports")
				   } catch (Exception e) {
					  log("ERROR", " Optional - Exception FIREWALL-LITE v4 'udp-ports' ")
				   }
				   try {  // optional
					  def tcpPortsList =  ["port-number"]
					  rebuildingFirewallLite += buildElementsUnbounded(v4FirewallPacketFilterXml, tcpPortsList, "tcp-ports")
				   } catch (Exception e) {
				      log("ERROR", " Optional - Exception FIREWALL-LITE v4 'tcp-ports' ")
				   }
				   rebuildingFirewallLite += "</tns2:v4-firewall-packet-filters>"
				 }
			 } catch (Exception e) {
				 log("ERROR", " Optional - Exception FIREWALL-LITE 'v4-firewall-packet-filters' ")
			 }
			 
			 try { // optional
				 def v6FirewallPacketFilters = firewallLiteData.'**'.findAll {it.name() == "v6-firewall-packet-filters"}
				 def v6FirewallPacketFiltersSize = v6FirewallPacketFilters.size()
				 //println " v6FirewallPacketFiltersSize = " + v6FirewallPacketFilters.size()
				 for (i in 0..v6FirewallPacketFiltersSize-1) {
					def v6FirewallPacketFilter = v6FirewallPacketFilters[i]
					def v6FirewallPacketFilterXml = XmlUtil.serialize(v6FirewallPacketFilter)
					rebuildingFirewallLite += "<tns2:v6-firewall-packet-filters>"
					def v6FirewallPacketFiltersList = ["v6-firewall-prefix", "v6-firewall-prefix-length", "allow-icmp-ping"]
					rebuildingFirewallLite += buildElements(v6FirewallPacketFilterXml, v6FirewallPacketFiltersList, "")
					try { // optional
						def udpPortsList = ["port-number"]
						rebuildingFirewallLite += buildElementsUnbounded(v6FirewallPacketFilterXml, udpPortsList, "udp-ports")
					} catch (Exception e) {
				      log("ERROR", " Optional - Exception FIREWALL-LITE v6 'udp-ports' ")
					}
					try { // optional
						def tcpPortsList =  ["port-number"]
						rebuildingFirewallLite += buildElementsUnbounded(v6FirewallPacketFilterXml, tcpPortsList, "tcp-ports")
					} catch (Exception e) {
				    	log("ERROR", " Optional - Exception FIREWALL-LITE v6 'tcp-ports' ")
					}
			       rebuildingFirewallLite += "</tns2:v6-firewall-packet-filters>"
				 }
			 } catch (Exception e) {
				 log("ERROR", " Optional - Exception FIREWALL-LITE 'v6-firewall-packet-filters' ")
			 }
			 rebuildingFirewallLite+= "</tns2:firewall-lite>"
		}
		log("DEBUG", " rebuildingFirewallLite - " + rebuildingFirewallLite)
		return rebuildingFirewallLite
     }
	
	def buildStaticRoutes(xmlInput) {
		def rebuildingStaticRoutes = ''
		if (xmlInput != null) {
			rebuildingStaticRoutes = "<tns2:static-routes>"
			def v4StaticRouteslist = ["v4-static-route-prefix","v4-static-route-prefix-length", "v4-next-hop-address"]
			rebuildingStaticRoutes += buildElementsUnbounded(xmlInput, v4StaticRouteslist, "v4-static-routes")
			def v6StaticRouteslist = ["v6-static-route-prefix","v6-static-route-prefix-length", "v6-next-hop-address"]
			rebuildingStaticRoutes += buildElementsUnbounded(xmlInput, v6StaticRouteslist, "v6-static-routes")
			rebuildingStaticRoutes += "</tns2:static-routes>"
		}
		log("DEBUG", " rebuildingStaticRoutes - " + rebuildingStaticRoutes)
		return rebuildingStaticRoutes
	}
	
	public String generateCurrentTimeInUtc(){
		final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());
		return utcTime;
	}
	
	public String generateCurrentTimeInGMT(){
		final  SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy h:m:s z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String utcTime = sdf.format(new Date());
		return utcTime;
	}
	

	/**
	 * @param encryptedAuth: encrypted credentials from urn properties
	 * @param msoKey: key to use to decrypt from urn properties
	 * @return base 64 encoded basic auth credentials
	 */
	def getBasicAuth(encryptedAuth, msoKey){
		try {
			def auth = decrypt(encryptedAuth, msoKey)
			byte[] encoded = Base64.encodeBase64(auth.getBytes())
			String encodedString = new String(encoded)
			encodedString = "Basic " + encodedString
			return encodedString
		} catch (Exception ex) {
			log("ERROR", "Unable to encode basic auth")
			throw ex
		}
	}
	
	def encrypt(toEncrypt, msokey){
		try {
			String result = CryptoUtils.encrypt(toEncrypt, msokey);
			return result
		}
		catch (Exception e) {
			log("ERROR", "Failed to encrypt credentials")
		}
	}
	
	def decrypt(toDecrypt, msokey){
		try {
			String result = CryptoUtils.decrypt(toDecrypt, msokey);
			return result
		}
		catch (Exception e) {
			log("ERROR", "Failed to decrypt credentials")
			throw e
		}
	}
	
	/**
	 * Return URL with qualified host name (if any) or urn mapping
	 * @param  String url from urn mapping
	 * @return String url with qualified host name
	 */
	public String getQualifiedHostNameForCallback(String urnCallbackUrl) {
		def callbackUrlToUse = urnCallbackUrl
		try{
			//swap host name with qualified host name from the jboss properties
			def qualifiedHostName = System.getProperty("jboss.qualified.host.name")
			if(qualifiedHostName!=null){
				log("DEBUG", "qualifiedHostName:\n" + qualifiedHostName)
				callbackUrlToUse = callbackUrlToUse.replaceAll("(http://)(.*)(:8080*)", {orig, first, torepl, last -> "${first}${qualifiedHostName}${last}"})
			}
		}catch(Exception e){
				log("DEBUG", "unable to grab qualified host name, using what's in urn properties for callbackurl. Exception was: " + e.printStackTrace())
		}
		return callbackUrlToUse
		
	}
	
	/**
	 * Retrieves text context of the element if the element exists, returns empty string otherwise
	 * @param com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl element to parse
	 * param String tagName tagName
	 * @return String text content of the element
	 */
	 public String getElementText(Element element, String tagName) {
	 	String text = ""
	 	org.w3c.dom.NodeList nodeList = element.getElementsByTagNameNS("*", tagName)
	 	if (nodeList != null && nodeList.length > 0) {
	 		text = nodeList.item(0).getTextContent()
	 	}
	 	return text
	 }

}
