/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.msb.sdk.discovery.common.RouteException
import org.json.JSONObject
import static org.apache.commons.lang3.StringUtils.*
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.ErrorCode
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.springframework.web.util.UriUtils
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.LogicalLink
import org.onap.so.bpmn.core.domain.PInterface
import org.onap.so.bpmn.core.domain.EsrThirdpartySdnc
import org.onap.so.bpmn.core.domain.ThirdpartySdncMap
import org.onap.aai.domain.yang.LogicalLink
import org.onap.aai.domain.yang.LogicalLinks
import org.onap.aai.domain.yang.PInterface
import org.onap.aai.domain.yang.PInterfaces
import org.onap.aai.domain.yang.Pnf
import org.onap.aai.domain.yang.Relationship
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.entities.Relationships
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.core.json.DecomposeJsonUtil
import org.onap.so.bpmn.infrastructure.aai.OpticalAAIRestClientImpl
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.*

/**
 * This groovy class supports the <class>DecomposeOpticalService.bpmn</class> process.
 *
 * @author
 *
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - serviceInstanceId
 * @param - serviceModelInfo
 * @param - requestParameters (may be null)
 *
 * Outputs:
 * @param - rollbackData (null)
 * @param - rolledBack (null)
 * @param - WorkflowException
 * @param - serviceDecomposition
 *
 */
public class DecomposeOpticalService extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DecomposeOpticalService.class);

    String Prefix="DDMS_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
    JsonUtils jsonUtils = new JsonUtils()

    public void preProcessRequest (DelegateExecution execution) {
        String msg = ""
        logger.info("preProcessRequest of DecomposeOpticalService ")
        setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))
        try {
            execution.setVariable("prefix", Prefix)
            // check for required input
            String uuiRequest = execution.getVariable("uuiRequest")
            String requestInput = jsonUtils.getJsonValue(uuiRequest, "service.parameters.requestInputs")
            Map<String, String> requestInputObject = ResourceRequestBuilder.getJsonObject(requestInput, Map.class)
            def serviceInvariantUuid = jsonUtils.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
            def serviceUuid = jsonUtils.getJsonValue(uuiRequest, "service.serviceUuid")
            def serviceInstanceId = execution.getVariable("serviceInstanceId")
            def globalSubscriberId = jsonUtils.getJsonValue(uuiRequest, "service.globalSubscriberId")
            def serviceType = jsonUtils.getJsonValue(uuiRequest, "service.serviceType")
            def serviceLayer = jsonUtils.getJsonValue(uuiRequest, "service.serviceType")
            def name = requestInputObject.get("name")
            def codingFunc = requestInputObject.get("uni_coding_func")
            def protocol = requestInputObject.get("uni_client_proto")
            def dueDate = requestInputObject.get("due_date")
            def endDate = requestInputObject.get("end_date")
            String uniId = requestInputObject.get("uni_id")
			String[] uni = uniId.split(" ")
			def uniIdPort = uni[0]
            def enniId = requestInputObject.get("enni_id")
			String[] enni = enniId.split(" ")
			def enniIdPort = enni[0]
            def nni1IdPort = ""
            def nni2IdPort = ""
            execution.setVariable("globalCustomerId", globalSubscriberId)
			execution.setVariable("serviceInstanceName", name)
			execution.setVariable("serviceLayer", serviceLayer)
            execution.setVariable("uniIdPort", uniIdPort)
            execution.setVariable("enniIdPort", enniIdPort)
            execution.setVariable("dueDate", dueDate)
            execution.setVariable("protocol", protocol)
            execution.setVariable("codingFunc", codingFunc)
            
            String[] str = codingFunc.split("G")
			String rate = str[0]
            execution.setVariable("rate", rate)
			
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit preProcessRequest of DecomposeOpticalService ")
    }

    public void actuallyDecomposeService (DelegateExecution execution) {
	        String msg = ""
	        logger.info("actuallyDecomposeService of DecomposeService ")

            //Fetch Controller from AAI for uniId
            OpticalAAIRestClientImpl qc = new OpticalAAIRestClientImpl()
            List<org.onap.so.bpmn.core.domain.PInterface> pifList = new ArrayList<PInterface>()
            def uniIdPort = execution.getVariable("uniIdPort")
            def enniIdPort = execution.getVariable("enniIdPort")

            EsrThirdpartySdnc uniCont = qc.getDomainControllerByIf(execution, uniIdPort)
            def uniControllerId = uniCont.getThirdpartySdncId() 
            execution.setVariable("uniController", uniCont)
            execution.setVariable("uniDomainType", uniCont.getDomainType())
            org.onap.so.bpmn.core.domain.PInterface uniObj = qc.getInterfaceDetails(execution, uniIdPort)
			execution.setVariable("uniPort", uniObj)
            org.onap.so.bpmn.core.domain.PInterface nni1Obj = new org.onap.so.bpmn.core.domain.PInterface()

            //Fetch Controller from AAI for enniId
            EsrThirdpartySdnc enniCont = qc.getDomainControllerByIf(execution, enniIdPort)
            def enniControllerId = enniCont.getThirdpartySdncId()
            execution.setVariable("enniController", enniCont)
            execution.setVariable("enniDomainType", enniCont.getDomainType())
            org.onap.so.bpmn.core.domain.PInterface enniObj = qc.getInterfaceDetails(execution, enniIdPort)
			execution.setVariable("enniPort", enniObj)
            org.onap.so.bpmn.core.domain.PInterface nni2Obj = new org.onap.so.bpmn.core.domain.PInterface()
            
			if (uniControllerId.equals(enniControllerId)){
				execution.setVariable("isMultiDomain", "false")
				execution.setVariable("domainType", execution.getVariable("uniDomainType"))
				execution.setVariable("serviceRate", execution.getVariable("rate"))
				execution.setVariable("aEndPortId", uniIdPort)
				execution.setVariable("aEndPortName", uniObj.getPortDescription())
				execution.setVariable("zEndPortId", enniIdPort)
				execution.setVariable("zEndPortName", enniObj.getPortDescription())
			}else if (!uniControllerId.equals(enniControllerId)){
				execution.setVariable("isMultiDomain", "true")
				decomposeMultiDomains(execution, uniControllerId)
            }else {
				  msg = "Exception in actuallyDecomposeService Optical "
				  exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
        logger.info("Exit actuallyDecomposeService of DecomposeOpticalService ")
    }

	public void decomposeMultiDomains(DelegateExecution execution, String uniControllerId){
		
		OpticalAAIRestClientImpl qc = new OpticalAAIRestClientImpl()
		org.onap.so.bpmn.core.domain.LogicalLink idl = qc.getInterDomainLink(execution, uniControllerId)
		
		Map<String, Object> tpInfo = getTPsfromAAI(execution)
		
		if (validatePInterface(execution, uniObj)){
			execution.setVariable("domainType", execution.getVariable("uniDomainType"))
			execution.setVariable("serviceRate", execution.getVariable("rate"))
			execution.setVariable("aEndPortId", uniIdPort)
			execution.setVariable("aEndPortName", uniObj.getPortDescription())
		}
		if (validatePInterface(execution, enniObj)){
			execution.setVariable("zEndPortId", enniIdPort)
			execution.setVariable("zEndPortName", enniObj.getPortDescription())
		}
	}
	
	public Map getTPsfromAAI(DelegateExecution execution) {
		Map<String, Object> tpInfo = new HashMap<>()

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.LOGICAL_LINK)
		AAIResourcesClient client = new AAIResourcesClient()
		Optional<org.onap.aai.domain.yang.LogicalLinks> result = client.get(org.onap.aai.domain.yang.LogicalLinks.class, uri)

		if (result.isPresent()) {
			org.onap.aai.domain.yang.LogicalLinks links = result.get();
			org.onap.aai.domain.yang.LogicalLink link = selectLogicalLink(links.getLogicalLink())

			if (link != null) {
				AAIResultWrapper wrapper = new AAIResultWrapper(link)
				Optional<Relationships> optRelationships = wrapper.getRelationships()
				List<AAIResourceUri> pInterfaces = new ArrayList<>()
				if (optRelationships.isPresent()) {
					Relationships relationships = optRelationships.get()
					if (relationships.getRelatedAAIUris(AAIObjectType.P_INTERFACE_PNF).isEmpty()) {
						return null
					}
					pInterfaces.addAll(relationships.getRelatedAAIUris(AAIObjectType.P_INTERFACE_PNF))
					logger.info("Get info for pInterface0 :" + pInterfaces.toString())
						// find remote p interface
						AAIResourceUri localTP = null
						AAIResourceUri remoteTP = null

						AAIResourceUri pInterface0 = pInterfaces.get(0)
						logger.info("Get info for pInterface0 :" + pInterface0.toString())
						Optional<org.onap.aai.domain.yang.PInterfaces> piftemp = client.get(org.onap.aai.domain.yang.PInterfaces.class, uri)
						org.onap.aai.domain.yang.PInterfaces pifs = piftemp.get()
						List<org.onap.aai.domain.yang.PInterface> pint = pifs.getPInterface()
						logger.info("Get info for pInterface0 :" + pint.toString())
						org.onap.aai.domain.yang.PInterface pif = pint.get(0)
						logger.info("Get info for pInterface0 :" + pif.toString())

						if (getDomainControllerByIf(execution, pif.getInterfaceName()).equalsIgnoreCase(execution.getVariable("uniController"))) {
							remoteTP = pInterfaces.get(0)
							localTP = pInterfaces.get(1)
						} else {
							localTP = pInterfaces.get(0)
							remoteTP = pInterfaces.get(1)
						}

						if (localTP != null && remoteTP != null) {
							// give local tp
							String tpUrl = localTP.build().toString()
							String localNodeId = tpUrl.split("/")[4]
							tpInfo.put("local-access-node-id", localNodeId)

							logger.info("Get info for local TP :" + localNodeId)
							Optional<org.onap.aai.domain.yang.Pnf> optLocalPnf = client.get(org.onap.aai.domain.yang.Pnf.class,
									AAIUriFactory.createResourceUri(AAIObjectType.PNF, localNodeId))

							if (optLocalPnf.isPresent()) {
								org.onap.aai.domain.yang.Pnf localPnf = optLocalPnf.get()
							}
							String ltpIdStr = tpUrl.substring(tpUrl.lastIndexOf("/") + 1)
							if (ltpIdStr.contains("-")) {
								tpInfo.put("local-access-ltp-id", ltpIdStr.substring(ltpIdStr.lastIndexOf("-") + 1))
							}

							// give remote tp
							tpUrl = remoteTP.build().toString()
							org.onap.aai.domain.yang.PInterface intfRemote = client.get(PInterface.class, remoteTP).get()

							String remoteNodeId = tpUrl.split("/")[4]
							tpInfo.put("remote-access-node-id", remoteNodeId)

							logger.info("Get info for remote TP:" + remoteNodeId);

							String[] networkRefRemote = intfRemote.getNetworkRef().split("-")
							Optional<org.onap.aai.domain.yang.Pnf> optRemotePnf = client.get(org.onap.aai.domain.yang.Pnf.class,
									AAIUriFactory.createResourceUri(AAIObjectType.PNF, remoteNodeId))

							if (optRemotePnf.isPresent()) {
								org.onap.aai.domain.yang.Pnf remotePnf = optRemotePnf.get()
							}

							String ltpIdStrR = tpUrl.substring(tpUrl.lastIndexOf("/") + 1)
							if (ltpIdStrR.contains("-")) {
								tpInfo.put("remote-access-ltp-id", ltpIdStrR.substring(ltpIdStr.lastIndexOf("-") + 1))
							}
						}
				}
			}
		}
		return tpInfo;
	}
	
	private org.onap.aai.domain.yang.LogicalLink selectLogicalLink(List<org.onap.aai.domain.yang.LogicalLink> logicalLinks) {
				for (org.onap.aai.domain.yang.LogicalLink link : logicalLinks) {
					for (Relationship relationship : link.getRelationshipList().getRelationship()) {
						if (relationship.getRelatedTo().equals("p-interface")
								&& link.getLinkType().equals("inter-domain")) {
								//&& link.getOperationalStatus().equalsIgnoreCase("up")) {
							logger.info("linkname:" + link.getLinkName() + " is matching with allowed list")
							return link;
						}
					}
				}
			logger.error("There is no matching logical link for inter-domain type")
			return null
	}
	
    public boolean validatePInterface (DelegateExecution execution, PInterface pif) { 
        int rate = execution.getVariable("rate")
        int pifSpeed = Integer.parseInt(pif.getSpeedValue()) 
        Boolean pifInMaint = pif.getInMaint()
        if(pifInMaint){
            return true
        } else if(pifSpeed >= rate){
            return true
        }
        else{
		   sendWorkflowResponse(execution, 403, "Required bandwidth not available!")
		   return false
        }
    }
     
    public PInterface getPInterfacebyLink (DelegateExecution execution, String link) {
        Optional<PInterface> pif = Optional.empty()
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.P_INTERFACE_PNF, UriBuilder.fromPath(link).build())
            pif= getAAIClient().get(PInterface.class,uri);
            if(pif.isPresent()) {
                setExecutionVariables(execution,pif.get(),uri)
            }
            else{
                logger.debug("GET PInterface received a Not Found (404) Response")
				sendWorkflowResponse(execution, 404, "GET PInterface received a Not Found (404) Response")
            }
        }catch(Exception e){
            logger.debug(" Error encountered within GetPInterface" + e.getMessage())
			sendWorkflowResponse(execution, 2500, "Error encountered within GetPInterface")
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetPInterface" + e.getMessage())
        }
        return PInterface
    }

    public void prepareInitDomainServiceOperationStatus(DelegateExecution execution){
    logger.info("start prepareInitServiceOperationStatus")
    try{
        String serviceId = UUID.randomUUID().toString()
        logger.info("Domain service instance id: " + serviceId)
        String operationId = UUID.randomUUID().toString()
		execution.setVariable("domainServiceInstanceId", serviceId)
        execution.setVariable("currentDomainIndex", 0)
        String operationType = "CREATE"
        String userId = ""
        String result = "processing"
        String progress = "0"
        String reason = ""
        String accessServiceId = execution.getVariable("serviceInstanceId")
		String accessServiceName = execution.getVariable("serviceInstanceName")
		String domain = execution.getVariable("domainType")
		String ss = execution.getVariable("serviceType")
        String serviceName = accessServiceName + "_" + ss + "_" + domain
		execution.setVariable("domainServiceInstanceName", serviceName)
        logger.info("Access service id : " + accessServiceId )
        String operationContent = "Prepare service creation"
        logger.debug("Generated new operation for Service Instance serviceId: " + serviceId + " operationId: " + operationId)
        serviceId = UriUtils.encode(serviceId, "UTF-8")
        execution.setVariable("domainOperationId", operationId)
        execution.setVariable("operationType", operationType)

        def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint", execution)
        execution.setVariable("MDONS_dbAdapterEndpoint", dbAdapterEndpoint)

            String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
			    <ns:initServiceOperationStatusWithAccessService xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <serviceName>${MsoUtils.xmlEscape(serviceName)}</serviceName>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                            <accessServiceId>${MsoUtils.xmlEscape(accessServiceId)}</accessServiceId>
                        </ns:initServiceOperationStatusWithAccessService>
                    </soapenv:Body>
                </soapenv:Envelope>"""

        payload = utils.formatXml(payload)
        execution.setVariable("MDONS_domainServiceRequest", payload)
        logger.debug("Domain service create entry in requestDb Request: " + payload)

    }catch(Exception e){
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                "Exception Occured Processing prepareInitDomainServiceOperationStatus.", "BPMN",
                ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
        execution.setVariable("MDONS_ErrorResponse", "Error Occurred during prepareInitDomainServiceOperationStatus Method:\n" + e.getMessage())
    }
    logger.info("finished prepareInitDomainServiceOperationStatus")
    }

	void getCurrentDomain(DelegateExecution execution){
		if(execution.getVariable("isMultiDomain") == "true"){
		def currentIndex = execution.getVariable("currentDomainIndex")
		List<ThirdpartySdncMap> domainList = execution.getVariable("sdncList")
		String currentDomain = domainList.get(currentIndex).getEsrThirdpartySdnc().getDomainType()
		execution.setVariable("domainType", currentDomain)
		logger.info("Now we deal with domain:" + currentDomain)
		}
	}
		
	void parseNextDomainService(DelegateExecution execution){
		logger.info("Start parseNextDomainService Process ")
		String serviceCreationStatus = execution.getVariable("Optical_Service_Status");
		if(serviceCreationStatus == "SUCCESS") {
			execution.setVariable("Optical_Service_Creation_failed", "false");
			if(execution.getVariable("isMultiDomain") == "false"){
				execution.setVariable("allDomainsFinished", "true")
			}else{
			def currentIndex = execution.getVariable("currentDomainIndex")
			def nextIndex =  currentIndex + 1
			execution.setVariable("currentDomainIndex", nextIndex)
			List<ThirdpartySdncMap> domainList = execution.getVariable("sdncList")
			if(nextIndex >= domainList.size()){
				execution.setVariable("allDomainsFinished", "true")
			}else{
				execution.setVariable("allDomainsFinished", "false")
				}
			}
		}else {
			execution.setVariable("Optical_Service_Creation_failed","true");
		}
		logger.info("allDomainsFinished value is" + execution.getVariable("allDomainsFinished"))
		logger.info("COMPLETED parseNextDomainService Process ")
	}
	
	public void getAccessServiceDetails(DelegateExecution execution) {
		try {
		String accessServiceId = execution.getVariable("serviceInstanceId")
		
		def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint",execution)
		execution.setVariable("MDONS_dbAdapterEndpoint", dbAdapterEndpoint)
		
		String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:getControllerServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
							<accessServiceId>${MsoUtils.xmlEscape(accessServiceId)}</accessServiceId>
                        </ns:getControllerServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

		payload = utils.formatXml(payload)
		execution.setVariable("MDONS_getAccessServiceReq", payload)
		
		HttpPost httpPost = new HttpPost(requestsdbEndPoint);
		httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
		httpPost.addHeader("Content-type", "application/soap+xml");
		httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
		String result = httpPost(requestsdbEndPoint, httpPost);
		
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing prepareInitDomainServiceOperationStatus.", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			execution.setVariable("MDONS_ErrorResponse", "Error Occurred during prepareInitDomainServiceOperationStatus Method:\n" + e.getMessage())
		}
	}
}   
