/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import static org.apache.commons.lang3.StringUtils.*
import javax.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.workflow.service.ServicePluginFactory
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

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
 */
public class DoCreateE2EServiceInstance extends AbstractServiceTaskProcessor {
  private static final Logger logger = LoggerFactory.getLogger( DoCreateE2EServiceInstance.class)
	private static final ObjectMapper mapper

	static {
		mapper = new ObjectMapper()
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
	}


	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		logger.trace("preProcessRequest ")

		try {
			execution.setVariable("prefix", Prefix)
			//Inputs
			//requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			logger.info(" ***** globalSubscriberId *****" + globalSubscriberId)
			//requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
			String serviceType = execution.getVariable("serviceType")
			logger.info(" ***** serviceType *****" + serviceType)
			//requestDetails.requestParameters. for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (productFamilyId == null) {
				execution.setVariable("productFamilyId", "")
			}

			String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			logger.info("SDNC Callback URL: " + sdncCallbackUrl)

			//requestDetails.modelInfo.for AAI PUT servieInstanceData
			//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String uuiRequest = execution.getVariable("uuiRequest")
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
			String serviceModelName = jsonUtil.getJsonValue(uuiRequest, "service.parameters.templateName")
			execution.setVariable("serviceModelName", serviceModelName)
			//aai serviceType and Role can be setted as fixed value now.
			String aaiServiceType = "E2E Service"
			String aaiServiceRole = "E2E Service"

			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("modelUuid", modelUuid)

			//AAI PUT
			String oStatus = execution.getVariable("initialStatus") ?: ""
			if ("TRANSPORT".equalsIgnoreCase(serviceType))
			{
				oStatus = "Created"
			}

			org.onap.aai.domain.yang.ServiceInstance si = new org.onap.aai.domain.yang.ServiceInstance()
			si.setServiceInstanceName(serviceInstanceName)
			si.setServiceType(aaiServiceType)
			si.setServiceRole(aaiServiceRole)
			si.setOrchestrationStatus(oStatus)
			si.setModelInvariantId(modelInvariantUuid)
			si.setModelVersionId(modelUuid)
			si.setInputParameters(uuiRequest)
			execution.setVariable("serviceInstanceData", si)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest ")
	}

   public void prepareDecomposeService(DelegateExecution execution) {
        try {
            logger.trace("Inside prepareDecomposeService of create generic e2e service ")
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            logger.trace("Completed prepareDecomposeService of  create generic e2e service ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
     }

    public void processDecomposition(DelegateExecution execution) {
        logger.trace("Inside processDecomposition() of  create generic e2e service flow ")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void doServicePreOperation(DelegateExecution execution){
       //we need a service plugin platform here.
    	ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
    	String uuiRequest = execution.getVariable("uuiRequest")
    	String newUuiRequest = ServicePluginFactory.getInstance().preProcessService(serviceDecomposition, uuiRequest)
    	execution.setVariable("uuiRequest", newUuiRequest)
    }

    public void doServiceHoming(DelegateExecution execution) {
    	//we need a service plugin platform here.
    	ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
    	String uuiRequest = execution.getVariable("uuiRequest")
    	String newUuiRequest = ServicePluginFactory.getInstance().doServiceHoming(serviceDecomposition, uuiRequest)
    	execution.setVariable("uuiRequest", newUuiRequest)
    }

	public void postProcessAAIGET(DelegateExecution execution) {
		logger.trace("postProcessAAIGET ")
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				logger.info("Error getting Service-instance from AAI", + serviceInstanceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				logger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
					logger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					logger.info("Found Service-instance in AAI")
					msg = "ServiceInstance already exists in AAI:" + serviceInstanceName
					logger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit postProcessAAIGET ")
	}

	//TODO use create if not exist
	public void createServiceInstance(DelegateExecution execution) {
		logger.trace("createServiceInstance ")
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		try {
			org.onap.aai.domain.yang.ServiceInstance si = execution.getVariable("serviceInstanceData")

			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(serviceInstanceId))
			client.create(uri, si)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			//start rollback set up
			RollbackData rollbackData = new RollbackData()
			def disableRollback = execution.getVariable("disableRollback")
			rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
			rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
			rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", serviceInstanceId)
			rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
			rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
			execution.setVariable("rollbackData", rollbackData)

			msg = "Exception in DoCreateServiceInstance.createServiceInstance. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit createServiceInstance ")
	}

	public void createCustomRelationship(DelegateExecution execution) {
		logger.trace("createCustomRelationship ")
		String msg = ""
		try {
			String uuiRequest = execution.getVariable("uuiRequest")
			String  vpnName =  isNeedProcessCustomRelationship(uuiRequest)

			if(null != vpnName){
				logger.debug("fetching resource-link information for the given sotnVpnName:"+vpnName)
				// fetch the service instance to link the relationship
				AAIResourcesClient client = new AAIResourcesClient()
				AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.NODES_QUERY).queryParam("search-node-type","service-instance").queryParam("filter","service-instance-name:EQUALS:"+vpnName)
				AAIResultWrapper aaiResult = client.get(uri,NotFoundException.class)
				Map<String, Object> result = aaiResult.asMap()
				List<Object> resources =
						(List<Object>) result.getOrDefault("result-data", Collections.emptyList())
				if(resources.size()>0) {
					String relationshipUrl = ((Map<String, Object>) resources.get(0)).get("resource-link")

					final Relationship body = new Relationship()
					body.setRelatedLink(relationshipUrl)

					createRelationShipInAAI(execution, body)
				} else {
					logger.warn("No resource-link found for the given sotnVpnName:"+vpnName)
				}

			} else {
				logger.error("VPNName not found in request input")
			}



		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {

			msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit createCustomRelationship ")
	}

	private void createRelationShipInAAI(DelegateExecution execution, final Relationship relationship){
		logger.trace("createRelationShipInAAI ")
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(serviceInstanceId)).relationshipAPI()
			client.create(uri, relationship)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {

			msg = "Exception in DoCreateE2EServiceInstance.createRelationShipInAAI. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit createRelationShipInAAI ")

	}

	private String isNeedProcessCustomRelationship(String uuiRequest) {
		String requestInput = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs")
		Map<String, String> requestInputObject = getJsonObject(requestInput, Map.class)
		if (requestInputObject == null) {
			return null
		}

		Optional<Map.Entry> firstKey =
		requestInputObject.entrySet()
				.stream()
				.filter({entry -> entry.getKey().toString().contains("_sotnVpnName")})
				.findFirst()
		if (firstKey.isPresent()) {
			return firstKey.get().getValue()
		}

		return null
	}

	private static <T> T getJsonObject(String jsonstr, Class<T> type) {
		try {
			return mapper.readValue(jsonstr, type)
		} catch (IOException e) {
			logger.error("{} {} fail to unMarshal json", MessageEnum.RA_NS_EXC.toString(),
					ErrorCode.BusinessProcessError.getValue(), e)
		}
		return null
	}


	/**
	 * Gets the service instance and its relationships from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')
			String globalSubscriberId = execution.getVariable('globalSubscriberId')
			String serviceType = execution.getVariable('subscriptionServiceType')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)

			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			execution.setVariable("serviceInstanceName", si.get().getServiceInstanceName())

		}catch(BpmnError e) {
			throw e
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void postProcessAAIGET2(DelegateExecution execution) {
		logger.trace("postProcessAAIGET2 ")
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				logger.info("Error getting Service-instance from AAI in postProcessAAIGET2", + serviceInstanceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				logger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET2 GENGS_SuccessIndicator:" + succInAAI
					logger.info(msg)
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
						logger.info("Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"))
					}
				}
			}
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET2 " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit postProcessAAIGET2 ")
	}

	public void preProcessRollback (DelegateExecution execution) {
		logger.trace("preProcessRollback ")
		try {

			Object workflowException = execution.getVariable("WorkflowException")

			if (workflowException instanceof WorkflowException) {
				logger.info("Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException)
				//execution.setVariable("WorkflowException", null)
			}
		} catch (BpmnError e) {
			logger.info("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			logger.info(msg)
		}
		logger.trace("Exit preProcessRollback ")
	}

	public void postProcessRollback (DelegateExecution execution) {
		logger.trace("postProcessRollback ")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException")
			if (workflowException instanceof WorkflowException) {
				logger.info("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException)
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			logger.info("BPMN Error during postProcessRollback")
			throw b
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			logger.info(msg)
		}
		logger.trace("Exit postProcessRollback ")
	}

	public void preInitResourcesOperStatus(DelegateExecution execution){
        logger.trace("STARTED preInitResourcesOperStatus Process ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            logger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            List<Resource>  resourceList = serviceDecomposition.getServiceResources()

            for(Resource resource : resourceList){
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
            }

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint")
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.info("DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
            logger.info("Outgoing initResourceOperationStatus: \n" + payload)
            logger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preInitResourcesOperStatus.", "BPMN",
					ErrorCode.UnknownError.getValue(), e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preInitResourcesOperStatus Process ")
	}

	// if site location is in local Operator, create all resources in local ONAP
	// if site location is in 3rd Operator, only process sp-partner to create all resources in 3rd ONAP
	public void doProcessSiteLocation(DelegateExecution execution){
		logger.trace("======== Start doProcessSiteLocation Process ======== ")
		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		String uuiRequest = execution.getVariable("uuiRequest")
		uuiRequest = ServicePluginFactory.getInstance().doProcessSiteLocation(serviceDecomposition, uuiRequest)
		execution.setVariable("uuiRequest", uuiRequest)
		execution.setVariable("serviceDecomposition", serviceDecomposition)

		logger.trace("======== COMPLETED doProcessSiteLocation Process ======== ")
	}

	// Allocate cross link TPs(terminal points) for sotn network only
	public void doTPResourcesAllocation(DelegateExecution execution){
		logger.trace("======== Start doTPResourcesAllocation Process ======== ")
		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		String uuiRequest = execution.getVariable("uuiRequest")
		uuiRequest = ServicePluginFactory.getInstance().doTPResourcesAllocation(execution, uuiRequest)
		execution.setVariable("uuiRequest", uuiRequest)
		logger.trace("======== COMPLETED doTPResourcesAllocation Process ======== ")
	}

	// prepare input param for using DoCreateResources.bpmn
	public void preProcessForAddResource(DelegateExecution execution) {
		logger.trace("STARTED preProcessForAddResource Process ")

		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		List<Resource> addResourceList = serviceDecomposition.getServiceResources()
		execution.setVariable("addResourceList", addResourceList)

		boolean isCreateResourceListValid = true
		if (addResourceList == null || addResourceList.isEmpty()) {
			isCreateResourceListValid = false
		}

		execution.setVariable("isCreateResourceListValid", isCreateResourceListValid)

		logger.trace("COMPLETED preProcessForAddResource Process ")
	}

	public void postProcessForAddResource(DelegateExecution execution) {
		// do nothing now

	}

}
