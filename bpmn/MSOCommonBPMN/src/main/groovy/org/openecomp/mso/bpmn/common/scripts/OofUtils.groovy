package org.openecomp.mso.bpmn.common.scripts

import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ResourceInstance
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.apache.commons.lang3.StringUtils

import static org.openecomp.mso.bpmn.common.scripts.GenericUtils.*

class OofUtils {

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    private AbstractServiceTaskProcessor utils

    public MsoUtils msoUtils = new MsoUtils()

    public OofUtils(AbstractServiceTaskProcessor taskProcessor) {
        this.utils = taskProcessor
    }

    /**
     * This method builds the service-agnostic
     * OOF json request to get a homing solution
     * and license solution
     *
     * @param execution
     * @param requestId
     * @param decomposition - ServiceDecomposition object
     * @param subscriber - Subscriber information
     * @param homingParams - Homing/Request parameters (remove??)
     *
     * @return request - OOF v1 payload - https://wiki.onap.org/pages/viewpage.action?pageId=25435066
     */
    public String buildRequest(DelegateExecution execution, String requestId, ServiceDecomposition decomposition,
                               String cloudOwner, String cloudRegionId, Map customerLocation) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", "Started Building OOF Request", isDebugEnabled)
        def callbackUrl = utils.createWorkflowMessageAdapterCallbackURL(execution, "oofResponse", requestId)
        def transactionId = requestId
        //ServiceInstance Info
        ServiceInstance serviceInstance = decomposition.getServiceInstance()
        def serviceInstanceId = ""
        def serviceInstanceName = ""
        if (serviceInstance == null) {
            utils.log("DEBUG", "Unable to obtain Service Instance Id, ServiceInstance Object is null", isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - Unable to " +
                    "obtain Service Instance Id, ServiceInstance Object is null")
        } else {
            serviceInstanceId = serviceInstance.getInstanceId()
            serviceInstanceName = serviceInstance.getInstanceName()
        }
        //Model Info
        ModelInfo model = decomposition.getModelInfo()
        String modelType = model.getModelType()
        String modelInvariantId = model.getModelInvariantUuid()
        String modelVersionId = model.getModelUuid()
        String modelName = model.getModelName()
        String modelVersion = model.getModelVersion()

        //Demands
        String placementDemands = ""
        StringBuilder sb = new StringBuilder()
        List<Resource> resourceList = decomposition.getServiceAllottedResources()
        List<VnfResource> vnfResourceList = decomposition.getServiceVnfs()

        // TODO: We should include both alloted resources and service resources in the placementDeamnds- not one or the other.
        if (resourceList.isEmpty() || resourceList == null) {
            utils.log("DEBUG", "Allotted Resources List is empty - will try to get service VNFs instead.", isDebugEnabled)
            resourceList = decomposition.getServiceVnfs()
        }

        if (resourceList.isEmpty() || resourceList == null) {
            utils.log("DEBUG", "Resources List is Empty", isDebugEnabled)
        } else {
            for (Resource resource : resourceList) {
                ModelInfo resourceModelInfo = resource.getModelInfo()
                ResourceInstance resourceInstance = resource.getResourceInstance()
                def resourceInstanceType = resource.getResourceType()
                def serviceResourceId = resource.getResourceId()
                //TODO - resourceId versus instanceId - should be what is put in AAI, whatever we put here will be what is in response, used to correlate
                def resourceModuleName = resourceModelInfo.getModelInstanceName()
                def resouceModelInvariantId = resourceModelInfo.getModelInvariantUuid()
                def resouceModelName = resourceModelInfo.getModelName()
                def resouceModelVersion = resourceModelInfo.getModelVersion()
                def resouceModelVersionId = resourceModelInfo.getModelUuid()
                def resouceModelType = resourceModelInfo.getModelType()
                def tenantId = execution.getTenantId()


                // TODO add existing and excluded candidates to request
                //                "existingCandidates": [
                //                        {
                //                            "identifier_type": "service_instance_id",
                //                            "cloud_owner": "",
                //                            "identifiers": ["gjhd-098-fhd-987"]
                //                        }
                //                ],
                //                "excludedCandidates": [
                //                        {
                //                            "identifier_type": "service_instance_id",
                //                            "cloud_owner": "",
                //                            "identifiers": ["gjhd-098-fhd-987"]
                //                        },
                //                        {
                //                            "identifier_type": "vim_id",
                //                            "cloud_owner": "vmware",
                //                            "identifiers": ["NYMDT67"]
                //                        }
                //                ],

                String demand =
                        """{
                        "resourceModuleName": "${resourceModuleName}",
                        "serviceResourceId": "${serviceResourceId}",
                        "tenantId": "${tenantId}",
                        "resourceModelInfo": {
                            "modelInvariantId": "${resouceModelInvariantId}",
                            "modelVersionId": "${resouceModelVersionId}",
                            "modelName": "${resouceModelName}",
                            "modelType": "${resouceModelType}"
                            "modelVersion": "${resouceModelVersion}",
                            "modelCustomizationName": "",
                        },
                        
                        "requiredCandidates": [
                          {
                            "identifier_type": "vim_id",
                            "cloud_owner": "${cloudOwner}",
                            "identifiers": ["${cloudRegionId}"]
                          }
                        ]
                        },"""

                placementDemands = sb.append(demand)
            }
            placementDemands = placementDemands.substring(0, placementDemands.length() - 1)
        }

        String licenseDemands = ""
        sb = new StringBuilder()
        if (vnfResourceList.isEmpty() || vnfResourceList == null) {
            utils.log("DEBUG", "Vnf Resources List is Empty", isDebugEnabled)
        } else {
            for (VnfResource vnfResource : vnfResourceList) {
                ModelInfo vnfResourceModelInfo = vnfResource.getModelInfo()
                ResourceInstance vnfResourceInstance = vnfResource.getResourceInstance()
                def resourceInstanceType = vnfResource.getResourceType()
                def serviceResourceId = vnfResource.getResourceId()
                def resourceModuleName = vnfResourceModelInfo.getModelInstanceName()
                def resouceModelInvariantId = vnfResourceModelInfo.getModelInvariantUuid()
                def resouceModelName = vnfResourceModelInfo.getModelName()
                def resouceModelVersion = vnfResourceModelInfo.getModelVersion()
                def resouceModelVersionId = vnfResourceModelInfo.getModelUuid()
                def resouceModelType = vnfResourceModelInfo.getModelType()

                // TODO Add Existing Licenses to demand
                //"existingLicenses": {
                //"entitlementPoolUUID": ["87257b49-9602-4ca1-9817-094e52bc873b",
                // "43257b49-9602-4fe5-9337-094e52bc9435"],
                //"licenseKeyGroupUUID": ["87257b49-9602-4ca1-9817-094e52bc873b",
                // "43257b49-9602-4fe5-9337-094e52bc9435"]
                //}

                    String demand =
                        """{
                            "resourceModuleName": "${resourceModuleName}",
                            "serviceResourceId": "${serviceResourceId}",
                            "resourceInstanceType": "${resourceInstanceType}",
                            "resourceModelInfo": {
                                "modelInvariantId": "${resouceModelInvariantId}",
                                "modelVersionId": "${resouceModelVersionId}",
                                "modelName": "${resouceModelName}",
                                "modelType": "${resouceModelType}"
                                "modelVersion": "${resouceModelVersion}",
                                "modelCustomizationName": "",
                                }
                        },"""

                licenseDemands = sb.append(demand)
            }
            licenseDemands = licenseDemands.substring(0, licenseDemands.length() - 1)
        }

        String request =
                """{
                    "requestInfo": {
                        "transactionId": "${transactionId}",
                        "requestId": "${requestId}",
                        "callbackUrl": "${callbackUrl}",
                        "sourceId": "so",
                        "numSolutions": 1,
                        "optimizer": [
                            "placement",
                            "license"
                        ],                    
                        "timeout": 600
                     },
                    "placementInfo": {
                        "requestParameters": { 
                            "customerLatitude": "${customerLocation[customerLatitude]}", 
                            "customerLongitude": "${customerLocation[customerLongitude]}", 
                            "customerName": "${customerLocation[customerName]}" 
                        },
                        "placementDemand": [
                                ${placementDemands}
                            ],
                        "serviceInfo": {
                            "serviceInstanceId": "${serviceInstanceId}",
                            "serviceInstanceName": "${serviceInstanceName}",
                            "modelInfo": {
                                "modelType": "${modelType}",
                                "modelInvariantId": "${modelInvariantId}",
                                "modelVersionId": "${modelVersionId}",
                                "modelName": "${modelName}",
                                "modelVersion": "${modelVersion}"
                                "modelCustomizationName": "",
                                }
                         },
                        "licenseDemands": [
                            ${licenseDemands}
                        ]
                        }
                    }
                }"""

        utils.log("DEBUG", "Completed Building OOF Request", isDebugEnabled)
        return request
    }

    /**
     * This method validates the callback response
     * from OOF. If the response contains an
     * exception the method will build and throw
     * a workflow exception.
     *
     * @param execution
     * @param response - the async callback response from oof
     */
    public void validateCallbackResponse(DelegateExecution execution, String response) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        String placements = ""
        if (isBlank(response)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "OOF Async Callback Response is Empty")
        } else {
            if (JsonUtils.jsonElementExist(response, "solutions.placementSolutions")) {
                placements = jsonUtil.getJsonValue(response, "solutions.placementSolutions")
                if (isBlank(placements) || placements.equalsIgnoreCase("[]")) {
                    String statusMessage = jsonUtil.getJsonValue(response, "statusMessage")
                    if (isBlank(statusMessage)) {
                        utils.log("DEBUG", "Error Occured in Homing: OOF Async Callback Response does " +
                                "not contain placement solution.", isDebugEnabled)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 400,
                                "OOF Async Callback Response does not contain placement solution.")
                    } else {
                        utils.log("DEBUG", "Error Occured in Homing: " + statusMessage, isDebugEnabled)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 400, statusMessage)
                    }
                } else {
                    return
                }
            } else if (JsonUtils.jsonElementExist(response, "requestError") == true) {
                String errorMessage = ""
                if (response.contains("policyException")) {
                    String text = jsonUtil.getJsonValue(response, "requestError.policyException.text")
                    errorMessage = "OOF Async Callback Response contains a Request Error Policy Exception: " + text
                } else if (response.contains("serviceException")) {
                    String text = jsonUtil.getJsonValue(response, "requestError.serviceException.text")
                    errorMessage = "OOF Async Callback Response contains a Request Error Service Exception: " + text
                } else {
                    errorMessage = "OOF Async Callback Response contains a Request Error. Unable to determine the Request Error Exception."
                }
                utils.log("DEBUG", "Error Occured in Homing: " + errorMessage, isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 400, errorMessage)

            } else {
                utils.log("DEBUG", "Error Occured in Homing: Received an Unknown Async Callback Response from OOF.", isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Received an Unknown Async Callback Response from OOF.")
            }
        }

    }


}