/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import javax.ws.rs.NotFoundException

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils;
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.bpmn.infrastructure.workflow.service.ServicePluginFactory
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils;

import groovy.json.*



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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateE2EServiceInstance.class);


	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils cutils = new CatalogDbUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("preProcessRequest ")

		try {
			execution.setVariable("prefix", Prefix)
			//Inputs
			//requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			msoLogger.info(" ***** globalSubscriberId *****" + globalSubscriberId)
			//requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
			String serviceType = execution.getVariable("serviceType")
			msoLogger.info(" ***** serviceType *****" + serviceType)
			//requestDetails.requestParameters. for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (productFamilyId == null) {
				execution.setVariable("productFamilyId", "")
			}

			String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			msoLogger.info("SDNC Callback URL: " + sdncCallbackUrl)

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

			String statusLine = isBlank(oStatus) ? "" : "<orchestration-status>${MsoUtils.xmlEscape(oStatus)}</orchestration-status>"

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			String serviceInstanceData =
					"""<service-instance xmlns=\"${namespace}\">
			        <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			        <service-instance-name>${MsoUtils.xmlEscape(serviceInstanceName)}</service-instance-name>
					<service-type>${MsoUtils.xmlEscape(aaiServiceType)}</service-type>
					<service-role>${MsoUtils.xmlEscape(aaiServiceRole)}</service-role>
					${statusLine}
				    <model-invariant-id>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-id>
				    <model-version-id>${MsoUtils.xmlEscape(modelUuid)}</model-version-id>
					</service-instance>""".trim()
			execution.setVariable("serviceInstanceData", serviceInstanceData)
			msoLogger.debug(serviceInstanceData)
			msoLogger.info(" aai_uri " + aai_uri + " namespace:" + namespace)
			msoLogger.info(" 'payload' to create Service Instance in AAI - " + "\n" + serviceInstanceData)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest ")
	}

   public void prepareDecomposeService(DelegateExecution execution) {
        try {
            msoLogger.trace("Inside prepareDecomposeService of create generic e2e service ")
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            msoLogger.trace("Completed prepareDecomposeService of  create generic e2e service ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
     }

    public void processDecomposition(DelegateExecution execution) {
        msoLogger.trace("Inside processDecomposition() of  create generic e2e service flow ")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            msoLogger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void doServicePreOperation(DelegateExecution execution){
       //we need a service plugin platform here.
    	ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
    	String uuiRequest = execution.getVariable("uuiRequest")
    	String newUuiRequest = ServicePluginFactory.getInstance().preProcessService(serviceDecomposition, uuiRequest);
    	execution.setVariable("uuiRequest", newUuiRequest)
    }

    public void doServiceHoming(DelegateExecution execution) {
    	//we need a service plugin platform here.
    	ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
    	String uuiRequest = execution.getVariable("uuiRequest")
    	String newUuiRequest = ServicePluginFactory.getInstance().doServiceHoming(serviceDecomposition, uuiRequest);
    	execution.setVariable("uuiRequest", newUuiRequest)
    }

	public void postProcessAAIGET(DelegateExecution execution) {
		msoLogger.trace("postProcessAAIGET ")
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				msoLogger.info("Error getting Service-instance from AAI", + serviceInstanceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				msoLogger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
					msoLogger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					msoLogger.info("Found Service-instance in AAI")
					msg = "ServiceInstance already exists in AAI:" + serviceInstanceName
					msoLogger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET. " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIGET ")
	}

	//TODO use create if not exist
	public void postProcessAAIPUT(DelegateExecution execution) {
		msoLogger.trace("postProcessAAIPUT ")
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENPS_SuccessIndicator")
			if(!succInAAI){
				msoLogger.info("Error putting Service-instance in AAI", + serviceInstanceId)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				msoLogger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
			}
			else
			{
				//start rollback set up
				RollbackData rollbackData = new RollbackData()
				def disableRollback = execution.getVariable("disableRollback")
				rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
				rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
				rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", serviceInstanceId)
				rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
				rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
				execution.setVariable("rollbackData", rollbackData)
			}

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIDEL. " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIPUT ")
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
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, serviceInstanceId)
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)

			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			execution.setVariable("serviceInstanceName", si.get().getServiceInstanceName())

		}catch(BpmnError e) {
			throw e;
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void postProcessAAIGET2(DelegateExecution execution) {
		msoLogger.trace("postProcessAAIGET2 ")
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				msoLogger.info("Error getting Service-instance from AAI in postProcessAAIGET2", + serviceInstanceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				msoLogger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET2 GENGS_SuccessIndicator:" + succInAAI
					msoLogger.info(msg)
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
						msoLogger.info("Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"))
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET2 " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIGET2 ")
	}

	public void preProcessRollback (DelegateExecution execution) {
		msoLogger.trace("preProcessRollback ")
		try {

			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				msoLogger.info("Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			msoLogger.info("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			msoLogger.info(msg)
		}
		msoLogger.trace("Exit preProcessRollback ")
	}

	public void postProcessRollback (DelegateExecution execution) {
		msoLogger.trace("postProcessRollback ")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				msoLogger.info("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			msoLogger.info("BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			msoLogger.info(msg)
		}
		msoLogger.trace("Exit postProcessRollback ")
	}

	public void preInitResourcesOperStatus(DelegateExecution execution){
        msoLogger.trace("STARTED preInitResourcesOperStatus Process ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            msoLogger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            List<Resource>  resourceList = serviceDecomposition.getServiceResources()

            for(Resource resource : resourceList){
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
            }

            def dbAdapterEndpoint = "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter"
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            msoLogger.info("DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
            msoLogger.info("Outgoing initResourceOperationStatus: \n" + payload)
            msoLogger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preInitResourcesOperStatus.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        msoLogger.trace("COMPLETED preInitResourcesOperStatus Process ")
	}

	// if site location is in local Operator, create all resources in local ONAP; 
	// if site location is in 3rd Operator, only process sp-partner to create all resources in 3rd ONAP
	public void doProcessSiteLocation(DelegateExecution execution){
		msoLogger.trace("======== Start doProcessSiteLocation Process ======== ")
		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		String uuiRequest = execution.getVariable("uuiRequest")
		uuiRequest = ServicePluginFactory.getInstance().doProcessSiteLocation(serviceDecomposition, uuiRequest);
		execution.setVariable("uuiRequest", uuiRequest)
		execution.setVariable("serviceDecomposition", serviceDecomposition)
		
		msoLogger.trace("======== COMPLETED doProcessSiteLocation Process ======== ")
	}
	
	// Allocate cross link TPs(terminal points) for sotn network only
	public void doTPResourcesAllocation(DelegateExecution execution){
		msoLogger.trace("======== Start doTPResourcesAllocation Process ======== ")
		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		String uuiRequest = execution.getVariable("uuiRequest")
		uuiRequest = ServicePluginFactory.getInstance().doTPResourcesAllocation(execution, uuiRequest);
		execution.setVariable("uuiRequest", uuiRequest)
		msoLogger.trace("======== COMPLETED doTPResourcesAllocation Process ======== ")
	}

	// prepare input param for using DoCreateResources.bpmn
	public void preProcessForAddResource(DelegateExecution execution) {
		msoLogger.trace("STARTED preProcessForAddResource Process ")

		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		List<Resource> addResourceList = serviceDecomposition.getServiceResources()
		execution.setVariable("addResourceList", addResourceList)

		msoLogger.trace("COMPLETED preProcessForAddResource Process ")
	}

	public void postProcessForAddResource(DelegateExecution execution) {
		// do nothing now

	}

}
