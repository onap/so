/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.logger.MsoLogger
import org.onap.so.utils.TargetEntity
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource

import javax.ws.rs.core.Response
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoDeleteE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion
 * @param - failNotFound - TODO
 * @param - serviceInputParams - TODO
 *
 * @param - delResourceList
 * @param - serviceRelationShip
 *
 * Outputs:
 * @param - WorkflowException
 *
 * Rollback - Deferred
 */
public class DoDeleteE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DDEESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteE2EServiceInstance.class);


    public void preProcessRequest (DelegateExecution execution) {
        msoLogger.debug(" ***** preProcessRequest *****")
        String msg = ""

        try {
            String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix",Prefix)

            //Inputs
            //requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
            String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
            if (globalSubscriberId == null)
            {
                execution.setVariable("globalSubscriberId", "")
            }

            //requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
            String serviceType = execution.getVariable("serviceType")
            if (serviceType == null)
            {
                execution.setVariable("serviceType", "")
            }

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                msoLogger.info(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sdncCallbackUrl = UrnPropertiesReader.getVariable('URN_mso_workflow_sdncadapter_callback', execution)
            if (isBlank(sdncCallbackUrl)) {
                msg = "URN_mso_workflow_sdncadapter_callback is null"
                msoLogger.info(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
            msoLogger.info("SDNC Callback URL: " + sdncCallbackUrl)

            StringBuilder sbParams = new StringBuilder()
            Map<String, String> paramsMap = execution.getVariable("serviceInputParams")

            if (paramsMap != null) {
                sbParams.append("<service-input-parameters>")
                for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                    String paramsXml
                    String paramName = entry.getKey()
                    String paramValue = entry.getValue()
                    paramsXml =
                            """	<param>
							<name>${MsoUtils.xmlEscape(paramName)}</name>
							<value>${MsoUtils.xmlEscape(paramValue)}</value>
							</param>
							"""
                    sbParams.append(paramsXml)
                }
                sbParams.append("</service-input-parameters>")
            }
            String siParamsXml = sbParams.toString()
            if (siParamsXml == null)
                siParamsXml = ""
            execution.setVariable("siParamsXml", siParamsXml)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug("***** Exit preProcessRequest *****")
    }

    public void postProcessAAIGET(DelegateExecution execution) {
        msoLogger.debug(" ***** postProcessAAIGET ***** ")
        String msg = ""

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
            String serviceType = ""

            if(foundInAAI){
                msoLogger.debug("Found Service-instance in AAI")

                String siData = execution.getVariable("GENGS_service")
                msoLogger.debug("SI Data")
                if (isBlank(siData))
                {
                    msg = "Could not retrive ServiceInstance data from AAI to delete id:" + serviceInstanceId
                    msoLogger.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
                }
                else
                {
                    InputSource source = new InputSource(new StringReader(siData));
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
                    Document serviceXml = docBuilder.parse(source)
                    serviceXml.getDocumentElement().normalize()
                    // get model invariant id
                    // Get Template uuid and version
                    if (utils.nodeExists(siData, "model-invariant-id") && utils.nodeExists(siData, "model-version-id") ) {
                        msoLogger.debug("SI Data model-invariant-id and model-version-id exist")
                        def modelInvariantId  = serviceXml.getElementsByTagName("model-invariant-id").item(0).getTextContent()
                        def modelVersionId  = serviceXml.getElementsByTagName("model-version-id").item(0).getTextContent()

                        // Set Original Template info
                        execution.setVariable("model-invariant-id-original", modelInvariantId)
                        execution.setVariable("model-version-id-original", modelVersionId)
                    }

                    msoLogger.debug("SI Data" + siData)
                    //Confirm there are no related service instances (vnf/network or volume)
                    if (utils.nodeExists(siData, "relationship-list")) {
                        msoLogger.debug("SI Data relationship-list exists")
                        JSONArray jArray = new JSONArray()

                        XmlParser xmlParser = new XmlParser()
                        Node root = xmlParser.parseText(siData)
                        def relation_list = utils.getChildNode(root, 'relationship-list')
                        def relationships = utils.getIdenticalChildren(relation_list, 'relationship')

                        for (def relation: relationships) {
                        	def jObj = getRelationShipData(relation, isDebugEnabled)
                        	jArray.put(jObj)
                        }

                        execution.setVariable("serviceRelationShip", jArray.toString())
                        execution.setVariable("serviceRelationShip", jArray.toString())
                    }
                }
            }else{
                boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
                if(!succInAAI){
                    msoLogger.debug("Error getting Service-instance from AAI :" + serviceInstanceId)
                    WorkflowException workflowException = execution.getVariable("WorkflowException")
                    if(workflowException != null){
                        msoLogger.error("workflowException: " + workflowException)
                        exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
                    }
                    else {
                        msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
                        msoLogger.error(msg)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                    }
                }

                msoLogger.debug("Service-instance NOT found in AAI. Silent Success")
            }
        }catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIGET. " + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" *** Exit postProcessAAIGET *** ")
    }

	private JSONObject getRelationShipData(node, isDebugEnabled){
		JSONObject jObj = new JSONObject()

		def relation  = utils.nodeToString(node)
		def rt  = utils.getNodeText(relation, "related-to")

		def rl  = utils.getNodeText(relation, "related-link")
		msoLogger.debug("ServiceInstance Related NS/Configuration :" + rl)

		def rl_datas = utils.getIdenticalChildren(node, "relationship-data")
		for(def rl_data : rl_datas) {
			def eKey =  utils.getChildNodeText(rl_data, "relationship-key")
			def eValue = utils.getChildNodeText(rl_data, "relationship-value")

			if ((rt == "service-instance" && eKey.equals("service-instance.service-instance-id"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-id")
			)){
				jObj.put("resourceInstanceId", eValue)
			}
			// for sp-partner and others
			else if(eKey.endsWith("-id")){
				jObj.put("resourceInstanceId", eValue)
				String resourceName = rt + eValue;
				jObj.put("resourceType", resourceName)
			}

			jObj.put("resourceLinkUrl", rl)
		}

		def rl_props = utils.getIdenticalChildren(node, "related-to-property")
		for(def rl_prop : rl_props) {
			def eKey =  utils.getChildNodeText(rl_prop, "property-key")
			def eValue = utils.getChildNodeText(rl_prop, "property-value")
			if((rt == "service-instance" && eKey.equals("service-instance.service-instance-name"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-type"))){
				jObj.put("resourceType", eValue)
			}
		}

		msoLogger.debug("Relationship related to Resource:" + jObj.toString())
		return jObj
	}

   public void getCurrentNS(DelegateExecution execution){
       utils.log("INFO", "======== Start getCurrentNS Process ======== ", isDebugEnabled)

       def currentIndex = execution.getVariable("currentNSIndex")
       List<String> nsSequence = execution.getVariable("nsSequence")
       String nsResourceType =  nsSequence.get(currentIndex)

       // GET AAI by Name, not ID, for process convenient
       execution.setVariable("GENGS_type", "service-instance")
       execution.setVariable("GENGS_serviceInstanceId", "")
       execution.setVariable("GENGS_serviceInstanceName", nsResourceType)

       msoLogger.debug("======== COMPLETED getCurrentNS Process ======== ")
   }

    public void prepareDecomposeService(DelegateExecution execution) {
        try {
            msoLogger.debug(" ***** Inside prepareDecomposeService of create generic e2e service ***** ")
            String modelInvariantUuid = execution.getVariable("model-invariant-id-original")
            String modelVersionId = execution.getVariable("model-version-id-original")

            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelVersionId}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            msoLogger.debug(" ***** Completed prepareDecomposeService of  create generic e2e service ***** ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            msoLogger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

	private void generateRelatedResourceInfo(String response, JSONObject jObj){

		def xml = new XmlSlurper().parseText(response)
		def rtn = xml.childNodes()
		while (rtn.hasNext()) {
			groovy.util.slurpersupport.Node node = rtn.next()
			def key = node.name()
			def value = node.text()
			jObj.put(key, value)
		}
	}

	private JSONObject getRelatedResourceInAAI (DelegateExecution execution, JSONObject jObj)
	{
		msoLogger.debug(" ***** Started getRelatedResourceInAAI *****")

        String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
		String urlLink = jObj.get("resourceLinkUrl")
		String serviceAaiPath = "${aai_endpoint}${urlLink}"

		URL url = new URL(serviceAaiPath)
		HttpClient client = new HttpClientFactory().newXmlClient(url, TargetEntity.AAI)


		Response response = client.get()
		int responseCode = response.getStatus()
		execution.setVariable(Prefix + "GeRelatedResourceResponseCode", responseCode)
		msoLogger.debug("  Get RelatedResource code is: " + responseCode)

		String aaiResponse = response.readEntity(String.class)
		execution.setVariable(Prefix + "GetRelatedResourceResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			msoLogger.debug("GET RelatedResource Received a Good Response")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable(Prefix + "FoundIndicator", true)

			generateRelatedResourceInfo(aaiResponse, jObj)

			//get model-invariant-uuid and model-uuid
			String modelInvariantId = ""
			String modelUuid = ""
			String modelCustomizationId = ""
			if(jObj.has("model-invariant-id")) {
				modelInvariantId = jObj.get("model-invariant-id")
				modelUuid = jObj.get("model-version-id")
				modelCustomizationId = jObj.get("model-customization-id")
			}

			jObj.put("modelInvariantId", modelInvariantId)
			jObj.put("modelVersionId", modelUuid)
			jObj.put("modelCustomizationId", modelCustomizationId)
		}
		else {
            String exceptionMessage = "Get RelatedResource Received a Bad Response Code. Response Code is: " + responseCode
			msoLogger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }

		msoLogger.debug(" ***** Exit getRelatedResourceInAAI *****")
		return jObj
	}

    public void postDecomposeService(DelegateExecution execution) {
        msoLogger.debug(" ***** Inside processDecomposition() of  delete generic e2e service flow ***** ")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            // service model info
            execution.setVariable("serviceModelInfo", serviceDecomposition.getModelInfo())

            List<Resource> deleteResourceList = serviceDecomposition.getServiceResources()
            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            def jsonSlurper = new JsonSlurper()
            def jsonOutput = new JsonOutput()

            List relationShipList = null
            if (serviceRelationShip != null) {
                relationShipList = jsonSlurper.parseText(serviceRelationShip)
            }

            List<Resource> deleteRealResourceList = new ArrayList<Resource>()

            //Set the real resource instance id to the decomosed resource list
            //reset the resource instance id , because in the decompose flow ,its a random one.
            //match the resource-instance-name and the model name
            if (relationShipList != null) {
                relationShipList.each {

                    JSONObject obj = getRelatedResourceInAAI(execution, (JSONObject)it)

                    for (Resource resource : deleteResourceList) {

                        String modelName = resource.getModelInfo().getModelName()

                        String modelCustomizationUuid = resource.getModelInfo().getModelCustomizationUuid()
                        if (StringUtils.containsIgnoreCase(obj.get("resourceType"), modelName)) {
                            resource.setResourceId(obj.get("resourceInstanceId"))
                            deleteRealResourceList.add(resource)
                        }
                        else if (modelCustomizationUuid.equals(obj.get("modelCustomizationId"))) {
                            resource.setResourceId(obj.get("resourceInstanceId"))
                            resource.setResourceInstanceName(obj.get("resourceType"))
                            deleteRealResourceList.add(resource)
                        }
                    }
                }
            }

            // only delete real existing resources
            execution.setVariable("deleteResourceList", deleteRealResourceList)
            
            boolean isDeleteResourceListValid = false
            if(deleteRealResourceList.size() > 0) {
                isDeleteResourceListValid = true
            }
            execution.setVariable("isDeleteResourceListValid", isDeleteResourceListValid)

            msoLogger.debug("delete resource list : " + deleteRealResourceList)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            msoLogger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        utils.log("DEBUG", " ***** exit processDecomposition() of  delete generic e2e service flow ***** ", isDebugEnabled)
    }

    public void preInitResourcesOperStatus(DelegateExecution execution){
        msoLogger.debug(" ======== STARTED preInitResourcesOperStatus Process ======== ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            msoLogger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            List<Resource> deleteResourceList = execution.getVariable("deleteResourceList")

            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            for(Resource resource : deleteResourceList){
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
            }

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("URN_mso_adapters_openecomp_db_endpoint", dbAdapterEndpoint)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initResourceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <resourceTemplateUUIDs>${MsoUtils.xmlEscape(resourceTemplateUUIDs)}</resourceTemplateUUIDs>
                        </ns:initResourceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_initResOperStatusRequest", payload)
            msoLogger.debug("Outgoing initResourceOperationStatus: \n" + payload)
            msoLogger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            msoLogger.debug("Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        msoLogger.debug("======== COMPLETED preInitResourcesOperStatus Process ======== ")
    }
    
    public void prepareUpdateServiceOperationStatus(DelegateExecution execution){
        msoLogger.debug(" ======== STARTED prepareUpdateServiceOperationStatus Process ======== ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String userId = ""
            String result = execution.getVariable("result")
            String progress = execution.getVariable("progress")
            String reason = ""
            String operationContent = execution.getVariable("operationContent")
            
            serviceId = UriUtils.encode(serviceId,"UTF-8")

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            msoLogger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>DELETE</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            msoLogger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)

        }catch(Exception e){
            msoLogger.error("Exception Occured Processing prepareUpdateServiceOperationStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareUpdateServiceOperationStatus Method:\n" + e.getMessage())
        }
        msoLogger.debug("======== COMPLETED prepareUpdateServiceOperationStatus Process ======== ")
    }

     /**
      * post config request.
      */
     public void postConfigRequest(execution){
         //to do
     }

}
