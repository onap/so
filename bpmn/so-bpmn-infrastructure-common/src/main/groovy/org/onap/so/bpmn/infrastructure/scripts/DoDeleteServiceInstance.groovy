/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils;

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
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteServiceInstance.class);

	String Prefix="DDELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {

		logger.trace("preProcessRequest ")
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
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback',execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "mso.workflow.sdncadapter.callback is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			logger.debug("SDNC Callback URL: " + sdncCallbackUrl)

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
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest ")
	}

	public void preProcessSDNCDelete (DelegateExecution execution) {

		logger.trace("preProcessSDNCDelete ")
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
			logger.debug("sdncDeactivate:\n" + sdncDeactivate)
			logger.debug("sdncDelete:\n" + sdncDelete)

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
		}
		logger.debug(" *****Exit preProcessSDNCDelete *****")
	}

	public void postProcessSDNCDelete(DelegateExecution execution, String response, String method) {


		logger.trace("postProcessSDNC " + method + " ")
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			logger.debug("SDNCResponse: " + response)
			logger.debug("workflowException: " + workflowException)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				logger.debug("Good response from SDNC Adapter for service-instance " + method + "response:\n" + response)

			}else{
				msg = "Bad Response from SDNC Adapter for service-instance " + method
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 3500, msg)
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNC " + method + " Exception:" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit postProcessSDNC " + method + " ")
	}

	/**
	 * Gets the service instance uri from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		logger.trace("getServiceInstance ")
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			if(resourceClient.exists(uri)){				
				execution.setVariable("GENGS_FoundIndicator", true)
				execution.setVariable("GENGS_siResourceLink", uri.build().toString())
				Map<String, String> keys = uri.getURIKeys()
				String  globalSubscriberId = execution.getVariable(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId)
				if(isBlank(globalSubscriberId)){
					globalSubscriberId = keys.get("global-customer-id")
					execution.setVariable("globalSubscriberId", globalSubscriberId)
				}

				//Extract Service Type if not provided on request
				String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
				if(isBlank(subscriptionServiceType)){
					String serviceTypeEncoded = keys.get("service-type") //TODO will this produce as already decoded?
					subscriptionServiceType = UriUtils.decode(serviceTypeEncoded, "UTF-8")
					execution.setVariable("subscriptionServiceType", subscriptionServiceType)
				}

				AAIResultWrapper wrapper = resourceClient.get(uri)
				if(wrapper.getRelationships().isPresent()){
					List<AAIResourceUri> uriList = wrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.ALLOTTED_RESOURCE)
					uriList.addAll(wrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.GENERIC_VNF))
					uriList.addAll(wrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.L3_NETWORK))

					if(uriList.isEmpty()){
						Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
						String orchestrationStatus = si.get().getOrchestrationStatus()
						String serviceType = si.get().getServiceType()
						execution.setVariable("serviceType", serviceType)
						execution.setVariable("serviceRole", si.get().getServiceRole())

						if("TRANSPORT".equalsIgnoreCase(serviceType)){
							if("PendingDelete".equals(orchestrationStatus)){
								execution.setVariable("skipDeactivate", true)
							}else{
								exceptionUtil.buildAndThrowWorkflowException(execution, 500, "ServiceInstance of type TRANSPORT must in PendingDelete status to allow Delete. Orchestration-status: " + orchestrationStatus)
							}
						}

						String svcTypes = UrnPropertiesReader.getVariable("sdnc.si.svc.types",execution) ?: ""
						List<String> svcList = Arrays.asList(svcTypes.split("\\s*,\\s*"));
						boolean isSdncService= false
						for(String listEntry : svcList){
							if(listEntry.equalsIgnoreCase(serviceType)){
								isSdncService = true
								break;
							}
						}
						execution.setVariable("sendToSDNC", true)
						if(execution.getVariable("sdncVersion").equals("1610")){
							if(!isSdncService){
								execution.setVariable("sendToSDNC", false)
							}
						}

					}else{
						execution.setVariable("siInUse", true)
						logger.debug("Stopped deleting Service Instance, it has dependencies")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Stopped deleting Service Instance, it has dependencies")
					}
				}
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "ServiceInstance not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch (Exception ex){
			String msg = "Exception in getServiceInstance. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	/**
	 * Deletes the service instance in aai
	 */
	public void deleteServiceInstance(DelegateExecution execution) {
		logger.trace("Entered deleteServiceInstance")
		try {
			String globalCustId = execution.getVariable("globalSubscriberId")
			String serviceType = execution.getVariable("subscriptionServiceType")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			AAIResourcesClient resourceClient = new AAIResourcesClient();
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustId, serviceType, serviceInstanceId)
			resourceClient.delete(serviceInstanceUri)

			logger.trace("Exited deleteServiceInstance")
		}catch(Exception e){
			logger.debug("Error occured within deleteServiceInstance method: " + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during deleteServiceInstance from aai")
		}
	}

}
