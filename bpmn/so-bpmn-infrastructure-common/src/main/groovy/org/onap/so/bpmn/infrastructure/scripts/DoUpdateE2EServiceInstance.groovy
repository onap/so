/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.springframework.web.util.UriUtils;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.*


/**
 * This groovy class supports the <class>DoUpdateE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - serviceType
 * @param - serviceInstanceId
 * @param - serviceInstanceName
 * @param - serviceModelInfo
 * @param - productFamilyId
 * @param - uuiRequest
 * @param - serviceDecomposition_Target
 * @param - serviceDecomposition_Original
 * @param - addResourceList
 * @param - delResourceList
 *
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException
 */
public class DoUpdateE2EServiceInstance extends AbstractServiceTaskProcessor {
	private static final Logger logger = LoggerFactory.getLogger( DoUpdateE2EServiceInstance.class);

	String Prefix="DUPDSI_"
	private static final String DebugFlag = "isDebugEnabled"

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** Enter DoUpdateE2EServiceInstance preProcessRequest *****")

		String msg = ""

		try {
			execution.setVariable("prefix", Prefix)
			//Inputs
			//for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			logger.info(" ***** globalSubscriberId *****" + globalSubscriberId)

			//for AAI PUT & SDNC assignTopology
			String serviceType = execution.getVariable("serviceType")
			logger.info(" ***** serviceType *****" + serviceType)

			//for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			//Generated in parent for AAI
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)){
				msg = "Input serviceInstanceId is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			String serviceInstanceName = execution.getVariable("serviceInstanceName")

			// user params
			String uuiRequest = execution.getVariable("uuiRequest")

			// target model Invariant uuid
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			logger.info( "modelInvariantUuid: " + modelInvariantUuid)

			// target model uuid
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
			execution.setVariable("modelUuid", modelUuid)

			logger.info("modelUuid: " + modelUuid)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info( "======== COMPLETED preProcessRequest Process ======== ")
	}


	public void preInitResourcesOperStatus(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        logger.info( " ======== STARTED preInitResourcesOperStatus Process ======== ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            logger.info( "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

			List<Resource> resourceList = new ArrayList<String>()
			List<Resource> addResourceList =  execution.getVariable("addResourceList")
			List<Resource> delResourceList =  execution.getVariable("delResourceList")
			resourceList.addAll(addResourceList)
			resourceList.addAll(delResourceList)
			for(Resource resource : resourceList){
				resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
			}

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.info( "DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
			logger.info( "Outgoing initResourceOperationStatus: \n" + payload)

        }catch(Exception e){
            logger.info( "Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        logger.info( "======== COMPLETED preInitResourcesOperStatus Process ======== ")
    }


    public void preProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** preProcessForAddResource ***** ")

	    execution.setVariable("operationType", "create")

		execution.setVariable("hasResourcetoAdd", false)
		List<Resource> addResourceList =  execution.getVariable("addResourceList")
		if(addResourceList != null && !addResourceList.isEmpty()) {
			execution.setVariable("hasResourcetoAdd", true)
		}

		logger.info(" *** Exit preProcessForAddResource *** ")
    }

    public void postProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** postProcessForAddResource ***** ")

		execution.setVariable("operationType", "update")

		logger.info(" *** Exit postProcessForAddResource *** ")
    }

	public void preProcessForDeleteResource(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** preProcessForDeleteResource ***** ")

		execution.setVariable("operationType", "delete")

		def  hasResourcetoDelete = false
		List<Resource> delResourceList =  execution.getVariable("delResourceList")
		if(delResourceList != null && !delResourceList.isEmpty()) {
			hasResourcetoDelete = true
		}
		execution.setVariable("hasResourcetoDelete", hasResourcetoDelete)

		if(hasResourcetoDelete) {
			def jsonSlurper = new JsonSlurper()
			String serviceRelationShip = execution.getVariable("serviceRelationShip")
			List relationShipList =  jsonSlurper.parseText(serviceRelationShip)

			//Set the real resource instance id to the decomosed resource list
			for(Resource resource: delResourceList){
				//reset the resource instance id , because in the decompose flow ,its a random one.
				resource.setResourceId("");
				//match the resource-instance-name and the model name
				if (relationShipList != null) {
					relationShipList.each {
						if(StringUtils.containsIgnoreCase(it.resourceType, resource.getModelInfo().getModelName())){
							resource.setResourceId(it.resourceInstanceId);
						}
					}
				}
			}
		}

		execution.setVariable("deleteResourceList", delResourceList)

		logger.info(" *** Exit preProcessForDeleteResource *** ")
	}

    public void postProcessForDeleteResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** postProcessForDeleteResource ***** ")

		execution.setVariable("operationType", "update")

		logger.info(" *** Exit postProcessForDeleteResource *** ")
    }

	public void preProcessAAIPUT(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.info("Entered " + method)

		String serviceInstanceVersion = execution.getVariable("serviceInstanceVersion")

		//requestDetails.modelInfo.for AAI PUT servieInstanceData
		//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData
		String serviceInstanceName = execution.getVariable("serviceInstanceName")
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		//aai serviceType and Role can be setted as fixed value now.
		String aaiServiceType = "E2E Service"
		String aaiServiceRole = "E2E Service"
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelUuid = execution.getVariable("modelUuid")

		org.onap.aai.domain.yang.ServiceInstance si = new org.onap.aai.domain.yang.ServiceInstance()
		si.setServiceInstanceId(serviceInstanceId)
		si.setServiceInstanceName(serviceInstanceName)
		si.setServiceType(aaiServiceType)
		si.setServiceRole(aaiServiceRole)
		si.setModelInvariantId(modelInvariantUuid)
		si.setModelVersionId(modelUuid)

		execution.setVariable("serviceInstanceData", si)

		logger.info( "Exited " + method)
	}

	public void updateServiceInstance(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** createServiceInstance ***** ")
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		try {
			org.onap.aai.domain.yang.ServiceInstance si = execution.getVariable("serviceInstanceData")

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
			client.update(uri, si)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			RollbackData rollbackData = new RollbackData()
			def disableRollback = execution.getVariable("disableRollback")
			rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
			rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
			rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", serviceInstanceId)
			rollbackData.put("SERVICEINSTANCE", "serviceType", execution.getVariable("serviceType"))
			rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
			execution.setVariable("rollbackData", rollbackData)

			msg = "Exception in DoCreateServiceInstance.createServiceInstance. " + ex.getMessage()
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info(" *** Exit createServiceInstance *** ")
	}

	public void preProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** preProcessRollback ***** ")
		try {

			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				logger.info( "Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			logger.info( "BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			logger.info( msg)
		}
		logger.info(" *** Exit preProcessRollback *** ")
	}

	public void postProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info(" ***** postProcessRollback ***** ")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				logger.info( "Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			logger.info( "BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			logger.info( msg)
		}
		logger.info(" *** Exit postProcessRollback *** ")
	}


	public void postConfigRequest(execution){
	    //now do noting
	}


}

