/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
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

import org.json.JSONObject
import org.json.XML;

import static org.apache.commons.lang3.StringUtils.*;


import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*


/**
 * This groovy class supports the <class>CreateDeviceResource.bpmn</class> process.
 * flow for Device Resource Create
 */
public class CreateDeviceResource extends AbstractServiceTaskProcessor {

    String Prefix="CREDEVRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    private static final Logger logger = LoggerFactory.getLogger( CreateDeviceResource.class);

    public void preProcessRequest(DelegateExecution execution){
        logger.info(" ***** Started preProcessRequest *****")
        String msg = ""
        try {

            //get bpmn inputs from resource request.
            String requestId = execution.getVariable("mso-request-id")
            String requestAction = execution.getVariable("requestAction")
            logger.info("The requestAction is: " + requestAction)
            String recipeParamsFromRequest = execution.getVariable("recipeParams")
            logger.info("The recipeParams is: " + recipeParamsFromRequest)
            String resourceInput = execution.getVariable("resourceInput")
            logger.info("The resourceInput is: " + resourceInput)
            //Get ResourceInput Object
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
            execution.setVariable(Prefix + "ResourceInput", resourceInputObj)

            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String inputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            JSONObject inputParameters = new JSONObject(inputParametersJson)
            execution.setVariable(Prefix + "ResourceRequestInputs", inputParameters)

//            String incomingRequest = resourceInputObj.getRequestsInputs()
//            String serviceParameters = JsonUtils.getJsonValue(incomingRequest, "service.parameters")
//            String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs")
//            JSONObject serviceInputParameters = new JSONObject(requestInputs)
//            execution.setVariable(Prefix + "ServiceParameters", serviceInputParameters)

            //Deal with recipeParams
            String recipeParamsFromWf = execution.getVariable("recipeParamXsd")
            String resourceName = resourceInputObj.getResourceInstanceName()
            if (isBlank(resourceName)) {
                msg = "Input resourceName is null"
                logger.error(msg)
            }
            execution.setVariable("resourceName", resourceName)
            logger.info("resourceName:" + resourceName)

            String resourceModelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid()
            if (isBlank(resourceModelInvariantUuid)) {
                msg = "Input resourceModelInvariantUuid is null"
                logger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelInvariantUuid", resourceModelInvariantUuid)
            logger.info("resourceModelInvariantUuid:" + resourceModelInvariantUuid)

            String resourceModelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
            if (isBlank(resourceModelUuid)) {
                msg = "Input resourceModelUuid is null"
                logger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelUuid", resourceModelUuid)
            logger.info("resourceModelUuid:" + resourceModelUuid)

            String resourceModelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
            if (isBlank(resourceModelCustomizationUuid)) {
                msg = "Input resourceModelCustomizationUuid is null"
                logger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelCustomizationUuid", resourceModelCustomizationUuid)
            logger.info("resourceModelCustomizationUuid:" + resourceModelCustomizationUuid)

            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)

        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
//            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

	public void checkDevType(DelegateExecution execution){
		logger.info(" ***** Started checkDevType *****")
		try {

			JSONObject resourceInputParameters = execution.getVariable(Prefix + "ResourceRequestInputs")
			String devType = resourceInputParameters.get("device_class")

			if(StringUtils.isBlank(devType)) {
				devType = "OTHER"
			}

			execution.setVariable("device_class", devType)

		} catch (Exception ex){
			String msg = "Exception in checkDevType " + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
		logger.info(" ***** Started prepareUpdateProgress *****")
		ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")
		String operType = resourceInputObj.getOperationType()
		String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
		String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
		String modelName = resourceInputObj.getResourceModelInfo().getModelName()
		String operationId = resourceInputObj.getOperationId()
		String progress = execution.getVariable("progress")
		String status = execution.getVariable("status")
		String statusDescription = execution.getVariable("statusDescription")

		String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${operType}</operType>
                               <operationId>${operationId}</operationId>
                               <progress>${progress}</progress>
                               <resourceTemplateUUID>${resourceCustomizationUuid}</resourceTemplateUUID>
                               <serviceId>${ServiceInstanceId}</serviceId>
                               <status>${status}</status>
                               <statusDescription>${statusDescription}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>"""

		setProgressUpdateVariables(execution, body)
		logger.info(" ***** Exit prepareUpdateProgress *****")
	}

	private void getVNFTemplatefromSDC(DelegateExecution execution){
		logger.info(" ***** Started getVNFTemplatefromSDC *****")
		try {
			// To do


		} catch (Exception ex){
			String msg = "Exception in getVNFTemplatefromSDC " + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

    public void prepareVnfAndModulesCreate(DelegateExecution execution) {
        try {
            logger.trace("Inside prepareVnfAndModulesCreate of CreateDeviceResource ")
            
            JSONObject resourceInputParameters = execution.getVariable(Prefix + "ResourceRequestInputs")
            String devType = resourceInputParameters.get("device_type")
            String devVersion = resourceInputParameters.get("device_version")
            
            //get vnf model from SDC
            getVNFTemplatefromSDC(execution)
	    
            VnfResource vnf = execution.getVariable("vnfResource")
            Integer vnfsCreatedCount = execution.getVariable(Prefix + "VnfsCreatedCount")
            String vnfModelInfoString = null;

            if (vnf != null) {
                logger.debug("getting model info for vnf # " + vnfsCreatedCount)
                ModelInfo vnfModelInfo = vnf.getModelInfo()
                vnfModelInfoString = vnfModelInfo.toString()
            } else {
                logger.debug("vnf is null")
                vnfModelInfoString = execution.getVariable("vnfModelInfo")
            }

            logger.debug(" vnfModelInfoString :" + vnfModelInfoString)

            // extract cloud configuration
//            String vimId = jsonUtil.getJsonValue(createVcpeServiceRequest,
//                    "requestDetails.cloudConfiguration.lcpCloudRegionId")
//            def cloudRegion = vimId.split("_")
//            execution.setVariable("cloudOwner", cloudRegion[0])
//            logger.debug("cloudOwner: "+ cloudRegion[0])
//            execution.setVariable("cloudRegionId", cloudRegion[1])
//            logger.debug("cloudRegionId: "+ cloudRegion[1])
//            execution.setVariable("lcpCloudRegionId", cloudRegion[1])
//            logger.debug("lcpCloudRegionId: "+ cloudRegion[1])
//            String tenantId = jsonUtil.getJsonValue(createVcpeServiceRequest,
//                    "requestDetails.cloudConfiguration.tenantId")
//            execution.setVariable("tenantId", tenantId)
//            logger.debug("tenantId: " + tenantId)
            

           execution.setVariable("cloudOwner", "")

           execution.setVariable("cloudRegionId", "")
       
           execution.setVariable("lcpCloudRegionId", "")
       
           execution.setVariable("tenantId", "")       

            String sdncVersion = execution.getVariable("sdncVersion")
            logger.debug("sdncVersion: " + sdncVersion)

            logger.trace("Completed prepareVnfAndModulesCreate of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateDeviceResource flow. Unexpected Error from method prepareVnfAndModulesCreate() - " + ex.getMessage()
            logger.debug(exceptionMessage)
        }
    }

    // *******************************
    //     Validate Vnf request Section -> increment count
    // *******************************
    public void validateVnfCreate(DelegateExecution execution) {

        try {
            logger.trace("Inside validateVnfCreate of CreateDeviceResource ")

            //Update Relationship between VNF to Device
            addVNFAAIRelationShip(execution)

            Integer vnfsCreatedCount = execution.getVariable(Prefix + "VnfsCreatedCount")
            vnfsCreatedCount++

            execution.setVariable(Prefix + "VnfsCreatedCount", vnfsCreatedCount)

            logger.debug(" ***** Completed validateVnfCreate of CreateDeviceResource ***** " + " vnf # " + vnfsCreatedCount)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateDeviceResource flow. Unexpected Error from method validateVnfCreate() - " + ex.getMessage()
            logger.debug(exceptionMessage)
        }
    }

    public void addVNFAAIRelationShip(DelegateExecution execution) {

        try {
            logger.trace("Inside addVNFAAIRelationShip of CreateDeviceResource ")



            logger.debug(" ***** Completed addVNFAAIRelationShip of CreateDeviceResource ***** ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateDeviceResource flow. Unexpected Error from method addVNFAAIRelationShip() - " + ex.getMessage()
            logger.debug(exceptionMessage)
        }
    }

	public void sendSyncResponse (DelegateExecution execution) {
		logger.debug(" *** sendSyncResponse *** ")

		try {
			String operationStatus = "finished"
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			logger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(" ***** Exit sendSyncResopnse *****")
	}
}
