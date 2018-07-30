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

import static org.apache.commons.lang3.StringUtils.*;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MsoLogger
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import groovy.json.*

/**
 * This groovy class supports the <class>DoDeleteServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceModelInfo - O
 * @param - productFamilyId
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion
 * @param - failNotFound - TODO
 * @param - serviceInputParams - TODO
 *
 * Outputs:
 * @param - WorkflowException
 *
 * Rollback - Deferred
 */
public class DoDeleteServiceInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteServiceInstance.class);

	String Prefix="DDELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {

		msoLogger.trace("preProcessRequest ")
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
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
			if (subscriptionServiceType == null)
			{
				execution.setVariable("subscriptionServiceType", "")
			}

			//Generated in parent for AAI PUT
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)){
				msg = "Input serviceInstanceId is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback',execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "mso.workflow.sdncadapter.callback is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			msoLogger.debug("SDNC Callback URL: " + sdncCallbackUrl)

			StringBuilder sbParams = new StringBuilder()
			Map<String, String> paramsMap = execution.getVariable("serviceInputParams")
			if (paramsMap != null)
			{
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
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest ")
	}

	public void preProcessSDNCDelete (DelegateExecution execution) {

		msoLogger.trace("preProcessSDNCDelete ")
		String msg = ""

		try {
			def serviceInstanceId = execution.getVariable("serviceInstanceId") ?: ""
			def serviceInstanceName = execution.getVariable("serviceInstanceName") ?: ""
			def callbackURL = execution.getVariable("sdncCallbackUrl") ?: ""
			def requestId = execution.getVariable("msoRequestId") ?: ""
			def serviceId = execution.getVariable("productFamilyId") ?: ""
			def subscriptionServiceType = execution.getVariable("subscriptionServiceType") ?: ""
			def globalSubscriberId = execution.getVariable("globalSubscriberId") ?: "" //globalCustomerId

			String serviceModelInfo = execution.getVariable("serviceModelInfo") ?: ""
			def modelInvariantUuid = ""
			def modelVersion = ""
			def modelUuid = ""
			def modelName = ""
			if (!isBlank(serviceModelInfo))
			{
				modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid") ?: ""
				modelVersion = jsonUtil.getJsonValue(serviceModelInfo, "modelVersion") ?: ""
				modelUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelUuid") ?: ""
				modelName = jsonUtil.getJsonValue(serviceModelInfo, "modelName") ?: ""

			}

			def siParamsXml = execution.getVariable("siParamsXml") ?: ""
			def msoAction = ""
			// special URL for SDNW, msoAction helps set diff url in SDNCA
			if("TRANSPORT".equalsIgnoreCase(execution.getVariable("serviceType")))
			{
				msoAction = "TRANSPORT"
			}

			def sdncRequestId = UUID.randomUUID().toString()

			String sdncDelete =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${MsoUtils.xmlEscape(sdncRequestId)}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>delete</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
							<sdncadapter:MsoAction>${MsoUtils.xmlEscape(msoAction)}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
						<request-action>DeleteServiceInstance</request-action>
					</request-information>
					<service-information>
						<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
						<subscription-service-type>${MsoUtils.xmlEscape(subscriptionServiceType)}</subscription-service-type>
						<onap-model-information>
					         <model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
					         <model-uuid>${MsoUtils.xmlEscape(modelUuid)}</model-uuid>
					         <model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
					         <model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
					    </onap-model-information>
						<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
					</service-information>
					<service-request-input>
						<service-instance-name>${MsoUtils.xmlEscape(serviceInstanceName)}</service-instance-name>
						${siParamsXml}
					</service-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			sdncDelete = utils.formatXml(sdncDelete)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			execution.setVariable("sdncDelete", sdncDelete)
			execution.setVariable("sdncDeactivate", sdncDeactivate)
			msoLogger.debug("sdncDeactivate:\n" + sdncDeactivate)
			msoLogger.debug("sdncDelete:\n" + sdncDelete)

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
		}
		msoLogger.debug(" *****Exit preProcessSDNCDelete *****")
	}

	public void postProcessSDNCDelete(DelegateExecution execution, String response, String method) {


		msoLogger.trace("postProcessSDNC " + method + " ")
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			msoLogger.debug("SDNCResponse: " + response)
			msoLogger.debug("workflowException: " + workflowException)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msoLogger.debug("Good response from SDNC Adapter for service-instance " + method + "response:\n" + response)

			}else{
				msg = "Bad Response from SDNC Adapter for service-instance " + method
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 3500, msg)
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNC " + method + " Exception:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessSDNC " + method + " ")
	}

	public void postProcessAAIGET(DelegateExecution execution) {

		msoLogger.trace("postProcessAAIGET ")
		String msg = ""

		try {

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
			String serviceType = ""

			if(foundInAAI == true){
				msoLogger.debug("Found Service-instance in AAI")

				//Extract GlobalSubscriberId
				String siRelatedLink = execution.getVariable("GENGS_siResourceLink")
				if (isBlank(siRelatedLink))
				{
					msg = "Could not retrive ServiceInstance data from AAI to delete id:" + serviceInstanceId
					msoLogger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				else
				{
					msoLogger.debug("Found Service-instance in AAI. link: " + siRelatedLink)
					String  globalSubscriberId = execution.getVariable("globalSubscriberId")
					if(isBlank(globalSubscriberId)){
						int custStart = siRelatedLink.indexOf("customer/")
						int custEnd = siRelatedLink.indexOf("/service-subscriptions")
						globalSubscriberId = siRelatedLink.substring(custStart + 9, custEnd)
						execution.setVariable("globalSubscriberId", globalSubscriberId)
					}

					//Extract Service Type if not provided on request
					String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
					if(isBlank(subscriptionServiceType)){
						int serviceStart = siRelatedLink.indexOf("service-subscription/")
						int serviceEnd = siRelatedLink.indexOf("/service-instances/")
						String serviceTypeEncoded = siRelatedLink.substring(serviceStart + 21, serviceEnd)
						subscriptionServiceType = UriUtils.decode(serviceTypeEncoded, "UTF-8")
						execution.setVariable("subscriptionServiceType", subscriptionServiceType)
					}

					if (isBlank(globalSubscriberId) || isBlank(subscriptionServiceType))
					{
						msg = "Could not retrive global-customer-id & subscription-service-type from AAI to delete id:" + serviceInstanceId
						msoLogger.debug(msg)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
					}
				}

				String siData = execution.getVariable("GENGS_service")
				msoLogger.debug("SI Data")
				if (isBlank(siData))
				{
					msg = "Could not retrive ServiceInstance data from AAI to delete id:" + serviceInstanceId
					msoLogger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				else
				{
					msoLogger.debug("SI Data" + siData)
					serviceType = utils.getNodeText(siData,"service-type")
					execution.setVariable("serviceType", serviceType)
					execution.setVariable("serviceRole", utils.getNodeText(siData,"service-role"))
					String orchestrationStatus =  utils.getNodeText(siData,"orchestration-status")

					//Confirm there are no related service instances (vnf/network or volume)
					if (utils.nodeExists(siData, "relationship-list")) {
						msoLogger.debug("SI Data relationship-list exists:")
						InputSource source = new InputSource(new StringReader(siData));
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
						Document serviceXml = docBuilder.parse(source)

						NodeList nodeList = serviceXml.getElementsByTagName("relationship")
						for (int x = 0; x < nodeList.getLength(); x++) {
							Node node = nodeList.item(x)
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) node
								def e = eElement.getElementsByTagName("related-to").item(0).getTextContent()
								if(e.equals("generic-vnf") || e.equals("l3-network") || e.equals("allotted-resource") ){
									msoLogger.debug("ServiceInstance still has relationship(s) to generic-vnfs, l3-networks or allotted-resources")
									execution.setVariable("siInUse", true)
									//there are relationship dependencies to this Service Instance
									msg = " Stopped deleting Service Instance, it has dependencies. Service instance id: " + serviceInstanceId
									msoLogger.debug(msg)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
								}else{
									msoLogger.debug("Relationship NOT related to OpenStack")
								}
							}
						}
					}

					if ("TRANSPORT".equalsIgnoreCase(serviceType))
					{
						if ("PendingDelete".equals(orchestrationStatus))
						{
							execution.setVariable("skipDeactivate", true)
						}
						else
						{
							msg = "ServiceInstance of type TRANSPORT must in PendingDelete status to allow Delete. Orchestration-status:" + orchestrationStatus
							msoLogger.debug(msg)
							exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
						}

					}

					//alacarte SIs are NOT sent to sdnc. exceptions are listed in config variable
					String svcTypes = UrnPropertiesReader.getVariable("sdnc.si.svc.types",execution) ?: ""
					msoLogger.debug("SDNC SI serviceTypes:" + svcTypes)
					List<String> svcList = Arrays.asList(svcTypes.split("\\s*,\\s*"));
					boolean isSdncService= false
					for (String listEntry : svcList){
						if (listEntry.equalsIgnoreCase(serviceType)){
							isSdncService = true
							break;
						}
					}

					//All Macros are sent to SDNC, TRANSPORT(Macro) is sent to SDNW
					//Alacartes are sent to SDNC if they are listed in config variable above
					execution.setVariable("sendToSDNC", true)
					if(execution.getVariable("sdncVersion").equals("1610")) //alacarte
					{
						if(!isSdncService){
							execution.setVariable("sendToSDNC", false)
						}
					}

					msoLogger.debug("isSdncService: " + isSdncService)
					msoLogger.debug("Send To SDNC: " + execution.getVariable("sendToSDNC"))
					msoLogger.debug("Service Type: " + execution.getVariable("serviceType"))

				}
			}else{
				boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
				if(succInAAI != true){
					msoLogger.debug("Error getting Service-instance from AAI", + serviceInstanceId)
					WorkflowException workflowException = execution.getVariable("WorkflowException")
					msoLogger.debug("workflowException: " + workflowException)
					if(workflowException != null){
						exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
					}
					else
					{
						msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
						msoLogger.debug(msg)
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
					}
				}

				msoLogger.debug("Service-instance NOT found in AAI. Silent Success")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteServiceInstance.postProcessAAIGET. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIGET ")
	}

	/**
	 * Deletes the service instance in aai
	 */
	public void deleteServiceInstance(DelegateExecution execution) {
		msoLogger.trace("Entered deleteServiceInstance")
		try {
			String globalCustId = execution.getVariable("globalSubscriberId")
			String serviceType = execution.getVariable("subscriptionServiceType")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			AAIResourcesClient resourceClient = new AAIResourcesClient();
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustId, serviceType, serviceInstanceId)
			resourceClient.delete(serviceInstanceUri)

			msoLogger.trace("Exited deleteServiceInstance")
		}catch(Exception e){
			msoLogger.debug("Error occured within deleteServiceInstance method: " + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during deleteServiceInstance from aai")
		}
	}

}
