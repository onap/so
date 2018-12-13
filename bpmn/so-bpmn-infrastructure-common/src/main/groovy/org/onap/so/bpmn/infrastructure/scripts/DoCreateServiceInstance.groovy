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

package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.OwningEntity
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.aai.groovyflows.AAICreateResources
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MsoLogger

/**
 * This groovy class supports the <class>DoCreateServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - subscriptionServiceType
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceModelInfo
 * @param - productFamilyId
 * @param - disableRollback
 * @param - failExists - TODO
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion ("1610")
 * @param - serviceDecomposition - Decomposition for R1710
 * (if macro provides serviceDecompsition then serviceModelInfo, serviceInstanceId & serviceInstanceName will be ignored)
 *
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException
 * @param - serviceInstanceName - (GET from AAI if null in input)
 *
 * This BB processes Macros(except TRANSPORT all sent to sdnc) and Alacartes(sdncSvcs && nonSdncSvcs)
 */
public class DoCreateServiceInstance extends AbstractServiceTaskProcessor {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateServiceInstance.class);
	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		msoLogger.trace("preProcessRequest")

		try {
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("prefix", Prefix)
			
			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData == null) {
				rollbackData = new RollbackData()
			}
			execution.setVariable("rollbackData", rollbackData)

			setBasicDBAuthHeader(execution, isDebugEnabled)
			//Inputs
			//requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId

			//requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

			//requestDetails.requestParameters. for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (productFamilyId == null) {
				execution.setVariable("productFamilyId", "")
			}

			String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "mso.workflow.sdncadapter.callback is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			msoLogger.debug("SDNC Callback URL: " + sdncCallbackUrl)

			//requestDetails.modelInfo.for AAI PUT servieInstanceData & SDNC assignTopology
			String modelInvariantUuid = ""
			String modelVersion = ""
			String modelUuid = ""
			String modelName = ""
			String serviceInstanceName = ""
			//Generated in parent.for AAI PUT
			String serviceInstanceId = ""
			String serviceType = ""
			String serviceRole = ""

			ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
			if (serviceDecomp != null)
			{
				serviceType = serviceDecomp.getServiceType() ?: ""
				msoLogger.debug("serviceType:" + serviceType)
				serviceRole = serviceDecomp.getServiceRole() ?: ""

				ServiceInstance serviceInstance = serviceDecomp.getServiceInstance()
				if (serviceInstance != null)
				{
					serviceInstanceId = serviceInstance.getInstanceId() ?: ""
					serviceInstanceName = serviceInstance.getInstanceName() ?: ""
					execution.setVariable("serviceInstanceId", serviceInstanceId)
					execution.setVariable("serviceInstanceName", serviceInstanceName)
				}

				ModelInfo modelInfo = serviceDecomp.getModelInfo()
				if (modelInfo != null)
				{
					modelInvariantUuid = modelInfo.getModelInvariantUuid() ?: ""
					modelVersion = modelInfo.getModelVersion() ?: ""
					modelUuid = modelInfo.getModelUuid() ?: ""
					modelName = modelInfo.getModelName() ?: ""
				}
				else
				{
					msg = "Input serviceModelInfo is null"
					msoLogger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
			}
			else
			{
				//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData & SDNC assignToplology
				serviceInstanceName = execution.getVariable("serviceInstanceName") ?: ""
				serviceInstanceId = execution.getVariable("serviceInstanceId") ?: ""

				String serviceModelInfo = execution.getVariable("serviceModelInfo")
				if (isBlank(serviceModelInfo)) {
					msg = "Input serviceModelInfo is null"
					msoLogger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid") ?: ""
				modelVersion = jsonUtil.getJsonValue(serviceModelInfo, "modelVersion") ?: ""
				modelUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelUuid") ?: ""
				modelName = jsonUtil.getJsonValue(serviceModelInfo, "modelName") ?: ""
				//modelCustomizationUuid NA for SI

			}

			execution.setVariable("serviceType", serviceType)
			execution.setVariable("serviceRole", serviceRole)
			execution.setVariable("serviceInstanceName", serviceInstanceName)

			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("modelVersion", modelVersion)
			execution.setVariable("modelUuid", modelUuid)
			execution.setVariable("modelName", modelName)

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
					//alacarte non-sdnc svcs must provide name (sdnc provides name for rest)
					if (isBlank(execution.getVariable("serviceInstanceName" )))
					{
						msg = "Input serviceInstanceName must be provided for alacarte"
						msoLogger.debug(msg)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
					}
				}
			}

			msoLogger.debug("isSdncService: " + isSdncService)
			msoLogger.debug("Send To SDNC: " + execution.getVariable("sendToSDNC"))
			msoLogger.debug("Service Type: " + execution.getVariable("serviceType"))

			//macro may provide name and alacarte-portm may provide name
			execution.setVariable("checkAAI", false)
			if (!isBlank(execution.getVariable("serviceInstanceName" )))
			{
				execution.setVariable("checkAAI", true)
			}

			if (isBlank(serviceInstanceId)){
				msg = "Input serviceInstanceId is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}


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
		msoLogger.trace("Exit preProcessRequest")
	}

	public void getAAICustomerById (DelegateExecution execution) {
		// https://{aaiEP}/aai/v8/business/customers/customer/{globalCustomerId}
		try {

			String globalCustomerId = execution.getVariable("globalSubscriberId") //VID to AAI name map
			msoLogger.debug(" ***** getAAICustomerById ***** globalCustomerId:" + globalCustomerId)

			AAIUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalCustomerId)
			if(!getAAIClient().exists(uri)){
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "GlobalCustomerId:" + globalCustomerId + " not found (404) in AAI")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception in getAAICustomerById. " + ex.getMessage())
		}
		msoLogger.trace("Exit getAAICustomerById")

	}

	public void putServiceInstance(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("putServiceInstance")
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		try {

			String serviceType = execution.getVariable("serviceType")
			//AAI PUT
			String oStatus = execution.getVariable("initialStatus") ?: "Active"
			if ("TRANSPORT".equalsIgnoreCase(serviceType))
			{
				oStatus = "Created"
			}

			//QUERY CATALOG DB AND GET WORKLOAD / ENVIRONMENT CONTEXT
			String environmentContext = ""
			String workloadContext =""
			String modelInvariantUuid = execution.getVariable("modelInvariantUuid")

			try{
				 String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution,modelInvariantUuid )

				 msoLogger.debug("JSON IS: "+json)

				 environmentContext = jsonUtil.getJsonValue(json, "serviceResources.environmentContext") ?: ""
				 workloadContext = jsonUtil.getJsonValue(json, "serviceResources.workloadContext") ?: ""
				 msoLogger.debug("Env Context is: "+ environmentContext)
				 msoLogger.debug("Workload Context is: "+ workloadContext)
			}catch(BpmnError e){
				throw e
			} catch (Exception ex){
				msg = "Exception in preProcessRequest " + ex.getMessage()
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}

			org.onap.aai.domain.yang.ServiceInstance si = new org.onap.aai.domain.yang.ServiceInstance()
			si.setServiceInstanceName(execution.getVariable("serviceInstanceName"))
			si.setServiceType(serviceType)
			si.setServiceRole(execution.getVariable("serviceRole"))
			si.setOrchestrationStatus(oStatus)
			si.setModelInvariantId(modelInvariantUuid)
			si.setModelVersionId(execution.getVariable("modelUuid"))
			si.setEnvironmentContext(environmentContext)
			si.setWorkloadContext(workloadContext)

			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), serviceInstanceId)
			client.create(uri, si)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			//start rollback set up
			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData == null) {
				rollbackData = new RollbackData()
			}			
			def disableRollback = execution.getVariable("disableRollback")
			rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
			rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
			rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", serviceInstanceId)
			rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
			rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
			execution.setVariable("rollbackData", rollbackData)

			msg = "Exception in DoCreateServiceInstance.putServiceInstance. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit putServiceInstance")
	}

	public void preProcessSDNCAssignRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		msoLogger.trace("preProcessSDNCAssignRequest")

		try {
			def serviceInstanceId = execution.getVariable("serviceInstanceId")
			def serviceInstanceName = execution.getVariable("serviceInstanceName")
			def callbackURL = execution.getVariable("sdncCallbackUrl")
			def requestId = execution.getVariable("msoRequestId")
			def serviceId = execution.getVariable("productFamilyId")
			def subscriptionServiceType = execution.getVariable("subscriptionServiceType")
			def globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			def msoAction = ""

			def modelInvariantUuid = execution.getVariable("modelInvariantUuid")
			def modelVersion = execution.getVariable("modelVersion")
			def modelUuid = execution.getVariable("modelUuid")
			def modelName = execution.getVariable("modelName")

			def sdncRequestId = UUID.randomUUID().toString()

			def siParamsXml = execution.getVariable("siParamsXml")

			// special URL for SDNW, msoAction helps set diff url in SDNCA
			if("TRANSPORT".equalsIgnoreCase(execution.getVariable("serviceType")))
			{
				msoAction = "TRANSPORT"
			}

			String sdncAssignRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${MsoUtils.xmlEscape(sdncRequestId)}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>assign</sdncadapter:SvcAction>
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
						<request-action>CreateServiceInstance</request-action>
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

			msoLogger.debug("sdncAssignRequest:\n" + sdncAssignRequest)
			sdncAssignRequest = utils.formatXml(sdncAssignRequest)
			execution.setVariable("sdncAssignRequest", sdncAssignRequest)
			msoLogger.debug("sdncAssignRequest:  " + sdncAssignRequest)

			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncDelete = sdncAssignRequest.replace(">assign<", ">delete<").replace(">CreateServiceInstance<", ">DeleteServiceInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def sdncRequestId3 = UUID.randomUUID().toString()
			String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId2}<", ">${sdncRequestId3}<")
			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData != null) {
				rollbackData.put("SERVICEINSTANCE", "sdncDeactivate", sdncDeactivate)
				rollbackData.put("SERVICEINSTANCE", "sdncDelete", sdncDelete)
				execution.setVariable("rollbackData", rollbackData)		

				msoLogger.debug("rollbackData:\n" + rollbackData.toString())
			}

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCAssignRequest. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessSDNCAssignRequest")
	}

	public void postProcessSDNCAssign (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("postProcessSDNCAssign")
		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			msoLogger.debug("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

			String response = execution.getVariable("sdncAdapterResponse")
			msoLogger.debug("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msoLogger.debug("Good response from SDNC Adapter for service-instance  topology assign: \n" + response)

				def rollbackData = execution.getVariable("rollbackData")
				if (rollbackData != null) {
					rollbackData.put("SERVICEINSTANCE", "rollbackSDNC", "true")
					execution.setVariable("rollbackData", rollbackData)
				}

			}else{
				msoLogger.debug("Bad Response from SDNC Adapter for service-instance assign")
				throw new BpmnError("MSOWorkflowException")
			}

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNCAssign. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessSDNCAssign")
	}

	public void postProcessAAIGET2(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("postProcessAAIGET2")
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				msoLogger.debug("Error getting Service-instance from AAI in postProcessAAIGET2", + serviceInstanceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				msoLogger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET2 GENGS_SuccessIndicator:" + succInAAI
					msoLogger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					String aaiService = execution.getVariable("GENGS_service")
					if (!isBlank(aaiService) && (utils.nodeExists(aaiService, "service-instance-name"))) {
						execution.setVariable("serviceInstanceName",  utils.getNodeText(aaiService, "service-instance-name"))
						msoLogger.debug("Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"))
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET2 " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIGET2")
	}

	public void preProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("preProcessRollback")
		try {

			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				msoLogger.debug("Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			msoLogger.debug("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			msoLogger.debug(msg)
		}
		msoLogger.trace("Exit preProcessRollback")
	}

	public void postProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("postProcessRollback")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				msoLogger.debug("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			msoLogger.debug("BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			msoLogger.debug(msg)
		}
		msoLogger.trace("Exit postProcessRollback")
	}

	public void createProject(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("createProject")

		String bpmnRequest = execution.getVariable("requestJson")
		String projectName = jsonUtil.getJsonValue(bpmnRequest, "requestDetails.project.projectName")
		String serviceInstance = execution.getVariable("serviceInstanceId")

		msoLogger.debug("BPMN REQUEST IS: "+ bpmnRequest)
		msoLogger.debug("PROJECT NAME: " + projectName)
		msoLogger.debug("Service Instance: " + serviceInstance)

		if(projectName == null||projectName.equals("")){
			msoLogger.debug("Project Name was not found in input. Skipping task...")
		}else{
			try{
				AAICreateResources aaiCR = new AAICreateResources()
				aaiCR.createAAIProject(projectName, serviceInstance)
			}catch(Exception ex){
				String msg = "Exception in createProject. " + ex.getMessage();
				msoLogger.debug(msg)
				msoLogger.error(ex);
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		msoLogger.trace("Exit createProject")
	}

	public void createOwningEntity(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.trace("createOwningEntity")
		String msg = "";
		String bpmnRequest = execution.getVariable("requestJson")
		String owningEntityId = jsonUtil.getJsonValue(bpmnRequest, "requestDetails.owningEntity.owningEntityId")
		String owningEntityName = jsonUtil.getJsonValue(bpmnRequest,"requestDetails.owningEntity.owningEntityName");
		String serviceInstance = execution.getVariable("serviceInstanceId")

		msoLogger.debug("owningEntity: " + owningEntityId)
		msoLogger.debug("OwningEntityName: "+ owningEntityName)
		msoLogger.debug("Service Instance: " + serviceInstance)

		try{
			AAICreateResources aaiCR = new AAICreateResources()
			if(owningEntityId==null||owningEntityId.equals("")){
				msg = "Exception in createOwningEntity. OwningEntityId is null in input.";
				throw new IllegalStateException();
			}else{
				if(aaiCR.existsOwningEntity(owningEntityId)){
					aaiCR.connectOwningEntityandServiceInstance(owningEntityId,serviceInstance)
				}else{
					if(owningEntityName==null||owningEntityName.equals("")){
						msg = "Exception in createOwningEntity. Can't create an owningEntity without an owningEntityName in input.";
						throw new IllegalStateException();
					}else{
						Optional<OwningEntity> owningEntity = aaiCR.getOwningEntityNames(owningEntityName);
						if(owningEntity.isPresent()){
							msg = "Exception in createOwningEntity. Can't create OwningEntity as name already exists in AAI associated with a different owning-entity-id (name must be unique).";
							throw new IllegalStateException();
						} else {
							aaiCR.createAAIOwningEntity(owningEntityId, owningEntityName, serviceInstance)
						}
					}
				}
			}
		}catch(Exception ex){
			msoLogger.debug(msg)
			msoLogger.error(ex);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit createOwningEntity")
	}

	// *******************************
	//     Build Error Section
	// *******************************

	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try{
			msoLogger.debug("Caught a Java Exception in DoCreateServiceInstance")
			msoLogger.debug("Started processJavaException Method")
			msoLogger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception in DoCreateServiceInstance")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception in DoCreateServiceInstance")

		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		msoLogger.trace("Completed processJavaException Method in DoCreateServiceInstance")
	}

}
