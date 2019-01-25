/*
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
package org.onap.so.bpmn.vcpe.scripts;

import static org.apache.commons.lang3.StringUtils.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.*
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.springframework.web.util.UriUtils;

import groovy.json.*



/**
 * This groovy class supports the <class>CreateVcpeResCustService.bpmn</class> process.
 *
 * @author ek1439
 *
 */
public class CreateVcpeResCustService extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateVcpeResCustService.class);

    private static final String DebugFlag = "isDebugLogEnabled"

    String Prefix = "CVRCS_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    VidUtils vidUtils = new VidUtils()

    /**
     * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
     * @param execution
     */
    private InitializeProcessVariables(DelegateExecution execution) {
        /* Initialize all the process variables in this block */

        execution.setVariable("createVcpeServiceRequest", "")
        execution.setVariable("globalSubscriberId", "")
        execution.setVariable("serviceInstanceName", "")
        execution.setVariable("msoRequestId", "")
        execution.setVariable(Prefix + "VnfsCreatedCount", 0)
        execution.setVariable("productFamilyId", "")
        execution.setVariable("brgWanMacAddress", "")
        execution.setVariable("customerLocation", "")
        execution.setVariable("homingService", "")
        execution.setVariable("cloudOwner", "")
        execution.setVariable("cloudRegionId", "")
        execution.setVariable("homingModelIds", "")

        //TODO
        execution.setVariable("sdncVersion", "1707")
    }

    // **************************************************
    //     Pre or Prepare Request Section
    // **************************************************
    /**
     * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
     * @param execution
     */
    public void preProcessRequest(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable(DebugFlag)
        execution.setVariable("prefix", Prefix)

        msoLogger.trace("Inside preProcessRequest CreateVcpeResCustService Request ")

        try {
            // initialize flow variables
            InitializeProcessVariables(execution)

            //Config Inputs
            String aaiDistDelay = UrnPropertiesReader.getVariable("aai.workflowAaiDistributionDelay", execution)
            if (isBlank(aaiDistDelay)) {
                String msg = "workflowAaiDistributionDelay is null"
                msoLogger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("aaiDistDelay", aaiDistDelay)
            msoLogger.debug("AAI distribution delay: " + aaiDistDelay)

            // check for incoming json message/input
            String createVcpeServiceRequest = execution.getVariable("bpmnRequest")
            msoLogger.debug(createVcpeServiceRequest)
            execution.setVariable("createVcpeServiceRequest", createVcpeServiceRequest);
            println 'createVcpeServiceRequest - ' + createVcpeServiceRequest

            // extract requestId
            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            if ((serviceInstanceId == null) || (serviceInstanceId.isEmpty())) {
                serviceInstanceId = UUID.randomUUID().toString()
                msoLogger.debug(" Generated new Service Instance: " + serviceInstanceId)
            } else {
                msoLogger.debug("Using provided Service Instance ID: " + serviceInstanceId)
            }

            serviceInstanceId = UriUtils.encode(serviceInstanceId, "UTF-8")
            execution.setVariable("serviceInstanceId", serviceInstanceId)
            utils.log("DEBUG", "Incoming serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)

            String serviceInstanceName = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.instanceName")
            execution.setVariable("serviceInstanceName", serviceInstanceName)
            utils.log("DEBUG", "Incoming serviceInstanceName is: " + serviceInstanceName, isDebugEnabled)

            String requestAction = execution.getVariable("requestAction")
            execution.setVariable("requestAction", requestAction)

            setBasicDBAuthHeader(execution, isDebugEnabled)

            String source = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.source")
            if ((source == null) || (source.isEmpty())) {
                source = "VID"
            }
            execution.setVariable("source", source)

            // extract globalSubscriberId
            String globalSubscriberId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.subscriberInfo.globalSubscriberId")

            // verify element global-customer-id is sent from JSON input, throw exception if missing
            if ((globalSubscriberId == null) || (globalSubscriberId.isEmpty())) {
                String dataErrorMessage = " Element 'globalSubscriberId' is missing. "
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
                execution.setVariable("globalCustomerId", globalSubscriberId)
            }

            // extract subscriptionServiceType
            String subscriptionServiceType = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestParameters.subscriptionServiceType")
            execution.setVariable("subscriptionServiceType", subscriptionServiceType)
            msoLogger.debug("Incoming subscriptionServiceType is: " + subscriptionServiceType)

            String suppressRollback = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.suppressRollback")
            execution.setVariable("disableRollback", suppressRollback)
            msoLogger.debug("Incoming Suppress/Disable Rollback is: " + suppressRollback)

            String productFamilyId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.productFamilyId")
            execution.setVariable("productFamilyId", productFamilyId)
            msoLogger.debug("Incoming productFamilyId is: " + productFamilyId)

            String subscriberInfo = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.subscriberInfo")
            execution.setVariable("subscriberInfo", subscriberInfo)
            msoLogger.debug("Incoming subscriberInfo is: " + subscriberInfo)

            // extract cloud configuration - if underscore "_" is present treat as vimId else it's a cloudRegion
            String vimId = jsonUtil.getJsonValue(createVcpeServiceRequest,
                    "requestDetails.cloudConfiguration.lcpCloudRegionId")
            if (vimId.contains("_") && vimId.split("_").length == 2 ) {
                def cloudRegion = vimId.split("_")
                def cloudOwner = cloudRegion[0]
                def cloudRegionId = cloudRegion[1]
                execution.setVariable("cloudOwner", cloudOwner)
                msoLogger.debug("cloudOwner: " + cloudOwner)
                execution.setVariable("cloudRegionId", cloudRegionId)
                msoLogger.debug("cloudRegionId: " + cloudRegionId)
            } else {
                msoLogger.debug("vimId is not present - setting  cloudRegion/cloudOwner from request.")
                String cloudOwner = jsonUtil.getJsonValue(createVcpeServiceRequest,
                        "requestDetails.cloudConfiguration.cloudOwner")
                if (!cloudOwner?.empty && cloudOwner != "")
                {
                    execution.setVariable("cloudOwner", cloudOwner)
                    msoLogger.debug("cloudOwner: " + cloudOwner)
                }
                def cloudRegionId = vimId
                execution.setVariable("cloudRegionId", cloudRegionId)
                msoLogger.debug("cloudRegionId: " + cloudRegionId)
            }
            /*
            * Extracting User Parameters from incoming Request and converting into a Map
            */
            def jsonSlurper = new JsonSlurper()

            Map reqMap = jsonSlurper.parseText(createVcpeServiceRequest)

            //InputParams
            def userParams = reqMap.requestDetails?.requestParameters?.userParams

            Map<String, String> inputMap = [:]
            if (userParams) {
                userParams.each {
                    userParam ->
                        if ("Customer_Location".equals(userParam?.name)) {
                            Map<String, String> customerMap = [:]
                            userParam.value.each {
                                param ->
                                    inputMap.put(param.key, param.value)
                                    customerMap.put(param.key, param.value)
                                    }
                            execution.setVariable("customerLocation", customerMap)
                        }
                        if ("Homing_Model_Ids".equals(userParam?.name)) {
                            utils.log("DEBUG", "Homing_Model_Ids: " + userParam.value.toString() + "  ---- Type is:" +
                                    userParam.value.getClass() , isDebugEnabled)
                            def modelIdLst = []
                            userParam.value.each {
                                param ->
                                    def valueMap = [:]
                                    param.each {
                                        entry ->
                                            valueMap.put(entry.key, entry.value)
                                    }
                                    modelIdLst.add(valueMap)
                                    utils.log("DEBUG", "Param: " + param.toString() + "  ---- Type is:" +
                                            param.getClass() , isDebugEnabled)
                                    }
                            execution.setVariable("homingModelIds", modelIdLst)
                        }
                        if ("BRG_WAN_MAC_Address".equals(userParam?.name)) {
                            execution.setVariable("brgWanMacAddress", userParam.value)
                            inputMap.put("BRG_WAN_MAC_Address", userParam.value)
                        }
                        if ("Homing_Solution".equals(userParam?.name)) {
                                    execution.setVariable("homingService", userParam.value)
                                    execution.setVariable("callHoming", true)
                                    inputMap.put("Homing_Solution", userParam.value)
                        }
                        if ("Orchestrator".equalsIgnoreCase(userParam?.name)) {
                            execution.setVariable("orchestrator", userParam.value)
                            inputMap.put("orchestrator", userParam.value)
                        }
                        if ("VfModuleNames".equals(userParam?.name)) {
                            utils.log("DEBUG", "VfModuleNames: " + userParam.value.toString(), isDebugEnabled)
                            def vfModuleNames = [:]
                            userParam.value.each {
                                entry ->
                                    String vfModuleModelInvariantUuid = null;
                                    String vfModuleName = null;
                                    entry.each {
                                        param ->
                                            if ("VfModuleModelInvariantUuid".equals(param.key)) {
                                                vfModuleModelInvariantUuid = param.value;
                                            } else if ("VfModuleName".equals(param.key)) {
                                                vfModuleName = param.value;
                                            }
                                    }

                                    if (vfModuleModelInvariantUuid != null && !vfModuleModelInvariantUuid.isEmpty() && vfModuleName != null && !vfModuleName.isEmpty()) {
                                        vfModuleNames.put(vfModuleModelInvariantUuid, vfModuleName)
                                        utils.log("DEBUG", "VfModuleModelInvariantUuid: " + vfModuleModelInvariantUuid + " VfModuleName: " + vfModuleName, isDebugEnabled)
                               	    }
                            }
                            execution.setVariable("vfModuleNames", vfModuleNames)
                        }
                }
            }

            if (execution.getVariable("homingService") == "") {
                // Set Default Homing to OOF if not set
                execution.setVariable("homingService", "oof")
            }

            msoLogger.debug("User Input Parameters map: " + userParams.toString())
            execution.setVariable("serviceInputParams", inputMap) // DOES NOT SEEM TO BE USED

            msoLogger.debug("Incoming brgWanMacAddress is: " + execution.getVariable('brgWanMacAddress'))

            //For Completion Handler & Fallout Handler
            String requestInfo =
                    """<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                    <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                    <action>CREATE</action>
                    <source>${MsoUtils.xmlEscape(source)}</source>
                   </request-info>"""

            execution.setVariable(Prefix + "requestInfo", requestInfo)

            msoLogger.trace("Completed preProcessRequest CreateVcpeResCustService Request ")

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void sendSyncResponse(DelegateExecution execution) {

        msoLogger.trace("Inside sendSyncResponse of CreateVcpeResCustService ")

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String requestId = execution.getVariable("mso-request-id")

            // RESTResponse (for API Handler (APIH) Reply Task)
            String syncResponse = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${
                requestId
            }"}}""".trim()

            msoLogger.debug(" sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
            sendWorkflowResponse(execution, 202, syncResponse)

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    // *******************************
    //
    // *******************************
    public void prepareDecomposeService(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside prepareDecomposeService of CreateVcpeResCustService ")

            String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")

            //serviceModelInfo JSON string will be used as-is for DoCreateServiceInstance BB
            String serviceModelInfo = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.modelInfo")
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            msoLogger.trace("Completed prepareDecomposeService of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    // *******************************
    //
    // *******************************
    public void prepareCreateServiceInstance(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside prepareCreateServiceInstance of CreateVcpeResCustService ")

            /*
             * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
             *      ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
             *      ModelInfo modelInfo = serviceDecomposition.getModelInfo()
             *
             */
            String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
//          String serviceInputParams = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestParameters")
//          execution.setVariable("serviceInputParams", serviceInputParams)


            String serviceInstanceName = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.instanceName")
            execution.setVariable("serviceInstanceName", serviceInstanceName)

            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonStringNoRootName())

            msoLogger.trace("Completed prepareCreateServiceInstance of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void postProcessServiceInstanceCreate(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.postProcessServiceInstanceCreate(' + 'execution=' + execution.getId() + ')'
        msoLogger.trace('Entered ' + method)

        String requestId = execution.getVariable("mso-request-id")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceInstanceName = execution.getVariable("serviceInstanceName")

        try {

            String payload = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
            <soapenv:Header/>
            <soapenv:Body>
            <req:updateInfraRequest>
                <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
                <lastModifiedBy>BPEL</lastModifiedBy>
                <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
                <serviceInstanceName>${MsoUtils.xmlEscape(serviceInstanceName)}</serviceInstanceName>
            </req:updateInfraRequest>
            </soapenv:Body>
            </soapenv:Envelope>
            """
            execution.setVariable(Prefix + "setUpdateDbInstancePayload", payload)
            msoLogger.debug(Prefix + "setUpdateDbInstancePayload: " + payload)
            msoLogger.trace('Exited ' + method)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception e) {
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
        }
    }


    public void processDecomposition(DelegateExecution execution) {

        msoLogger.trace("Inside processDecomposition() of CreateVcpeResCustService ")

        try {

            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            // VNFs
            List<VnfResource> vnfList = serviceDecomposition.getVnfResources()
            filterVnfs(vnfList)
            serviceDecomposition.setVnfResources(vnfList)

            execution.setVariable("vnfList", vnfList)
            execution.setVariable("vnfListString", vnfList.toString())

            String vnfModelInfoString = ""
            if (vnfList != null && vnfList.size() > 0) {
                execution.setVariable(Prefix + "VNFsCount", vnfList.size())
                msoLogger.debug("vnfs to create: " + vnfList.size())
                ModelInfo vnfModelInfo = vnfList[0].getModelInfo()

                vnfModelInfoString = vnfModelInfo.toString()
                String vnfModelInfoWithRoot = vnfModelInfo.toString()
                vnfModelInfoString = jsonUtil.getJsonValue(vnfModelInfoWithRoot, "modelInfo")
            } else {
                execution.setVariable(Prefix + "VNFsCount", 0)
                msoLogger.debug("no vnfs to create based upon serviceDecomposition content")
            }

            execution.setVariable("vnfModelInfo", vnfModelInfoString)
            execution.setVariable("vnfModelInfoString", vnfModelInfoString)
            msoLogger.debug(" vnfModelInfoString :" + vnfModelInfoString)

            msoLogger.trace("Completed processDecomposition() of CreateVcpeResCustService ")
        } catch (Exception ex) {
            sendSyncError(execution)
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. processDecomposition() - " + ex.getMessage()
            msoLogger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    private void filterVnfs(List<VnfResource> vnfList) {
        if (vnfList == null) {
            return
        }

        // remove BRG & TXC from VNF list

        Iterator<VnfResource> it = vnfList.iterator()
        while (it.hasNext()) {
            VnfResource vr = it.next()

            String role = vr.getNfRole()
            if (role == "BRG" || role == "TunnelXConn" || role == "Tunnel XConn") {
                it.remove()
            }
        }
    }


    public void prepareCreateAllottedResourceTXC(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside prepareCreateAllottedResourceTXC of CreateVcpeResCustService ")

            /*
             * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
             *      ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
             *      ModelInfo modelInfo = serviceDecomposition.getModelInfo()
             *
             */
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            //allottedResourceModelInfo
            //allottedResourceRole
            //The model Info parameters are a JSON structure as defined in the Service Instantiation API.
            //It would be sufficient to only include the service model UUID (i.e. the modelVersionId), since this BB will query the full model from the Catalog DB.
            List<AllottedResource> allottedResources = serviceDecomposition.getAllottedResources()
            if (allottedResources != null) {
                Iterator iter = allottedResources.iterator();
                while (iter.hasNext()) {
                    AllottedResource allottedResource = (AllottedResource) iter.next();

                    msoLogger.debug(" getting model info for AllottedResource # :" + allottedResource.toJsonStringNoRootName())
                    msoLogger.debug(" allottedResource.getAllottedResourceType() :" + allottedResource.getAllottedResourceType())
                    if ("TunnelXConn".equalsIgnoreCase(allottedResource.getAllottedResourceType()) || "Tunnel XConn".equalsIgnoreCase(allottedResource.getAllottedResourceType())) {
                        //set create flag to true
                        execution.setVariable("createTXCAR", true)
                        ModelInfo allottedResourceModelInfo = allottedResource.getModelInfo()
                        execution.setVariable("allottedResourceModelInfoTXC", allottedResourceModelInfo.toJsonStringNoRootName())
                        execution.setVariable("allottedResourceRoleTXC", allottedResource.getAllottedResourceRole())
                        execution.setVariable("allottedResourceTypeTXC", allottedResource.getAllottedResourceType())
                        //After decomposition and homing BBs, there should be an allotted resource object in the decomposition that represents the TXC,
                        //and in its homingSolution section should be found the infraServiceInstanceId (i.e. infraServiceInstanceId in TXC Allotted Resource structure) (which the Homing BB would have populated).
                        execution.setVariable("parentServiceInstanceIdTXC", allottedResource.getHomingSolution().getServiceInstanceId())
                    }
                }
            }

            //unit test only
            String allottedResourceId = execution.getVariable("allottedResourceId")
            execution.setVariable("allottedResourceIdTXC", allottedResourceId)
            msoLogger.debug("setting allottedResourceId CreateVcpeResCustService " + allottedResourceId)

            msoLogger.trace("Completed prepareCreateAllottedResourceTXC of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in prepareCreateAllottedResourceTXC flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void prepareCreateAllottedResourceBRG(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside prepareCreateAllottedResourceBRG of CreateVcpeResCustService ")

            /*
             * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
             *      ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
             *      ModelInfo modelInfo = serviceDecomposition.getModelInfo()
             *
             */
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            //allottedResourceModelInfo
            //allottedResourceRole
            //The model Info parameters are a JSON structure as defined in the Service Instantiation API.
            //It would be sufficient to only include the service model UUID (i.e. the modelVersionId), since this BB will query the full model from the Catalog DB.
            List<AllottedResource> allottedResources = serviceDecomposition.getAllottedResources()
            if (allottedResources != null) {
                Iterator iter = allottedResources.iterator();
                while (iter.hasNext()) {
                    AllottedResource allottedResource = (AllottedResource) iter.next();

                    msoLogger.debug(" getting model info for AllottedResource # :" + allottedResource.toJsonStringNoRootName())
                    msoLogger.debug(" allottedResource.getAllottedResourceType() :" + allottedResource.getAllottedResourceType())
                    if ("BRG".equalsIgnoreCase(allottedResource.getAllottedResourceType())) {
                        //set create flag to true
                        execution.setVariable("createBRGAR", true)
                        ModelInfo allottedResourceModelInfo = allottedResource.getModelInfo()
                        execution.setVariable("allottedResourceModelInfoBRG", allottedResourceModelInfo.toJsonStringNoRootName())
                        execution.setVariable("allottedResourceRoleBRG", allottedResource.getAllottedResourceRole())
                        execution.setVariable("allottedResourceTypeBRG", allottedResource.getAllottedResourceType())
                        //After decomposition and homing BBs, there should be an allotted resource object in the decomposition that represents the BRG,
                        //and in its homingSolution section should be found the infraServiceInstanceId (i.e. infraServiceInstanceId in BRG Allotted Resource structure) (which the Homing BB would have populated).
                        execution.setVariable("parentServiceInstanceIdBRG", allottedResource.getHomingSolution().getServiceInstanceId())
                    }
                }
            }

            //unit test only
            String allottedResourceId = execution.getVariable("allottedResourceId")
            execution.setVariable("allottedResourceIdBRG", allottedResourceId)
            msoLogger.debug("setting allottedResourceId CreateVcpeResCustService " + allottedResourceId)

            msoLogger.trace("Completed prepareCreateAllottedResourceBRG of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in prepareCreateAllottedResourceBRG flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    // *******************************
    //     Generate Network request Section
    // *******************************
    public void prepareVnfAndModulesCreate(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside prepareVnfAndModulesCreate of CreateVcpeResCustService ")

            //          String disableRollback = execution.getVariable("disableRollback")
            //          def backoutOnFailure = ""
            //          if(disableRollback != null){
            //              if ( disableRollback == true) {
            //                  backoutOnFailure = "false"
            //              } else if ( disableRollback == false) {
            //                  backoutOnFailure = "true"
            //              }
            //          }
            //failIfExists - optional

            String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
            String productFamilyId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.productFamilyId")
            execution.setVariable("productFamilyId", productFamilyId)
            msoLogger.debug("productFamilyId: " + productFamilyId)

            List<VnfResource> vnfList = execution.getVariable("vnfList")

            Integer vnfsCreatedCount = execution.getVariable(Prefix + "VnfsCreatedCount")
            String vnfModelInfoString = null;

            if (vnfList != null && vnfList.size() > 0) {
                msoLogger.debug("getting model info for vnf # " + vnfsCreatedCount)
                ModelInfo vnfModelInfo1 = vnfList[0].getModelInfo()
                msoLogger.debug("got 0 ")
                ModelInfo vnfModelInfo = vnfList[vnfsCreatedCount.intValue()].getModelInfo()
                vnfModelInfoString = vnfModelInfo.toString()
            } else {
                //TODO: vnfList does not contain data. Need to investigate why ... . Fro VCPE use model stored
                vnfModelInfoString = execution.getVariable("vnfModelInfo")
            }

            msoLogger.debug(" vnfModelInfoString :" + vnfModelInfoString)

            // extract cloud configuration - if underscore "_" is present treat as vimId else it's a cloudRegion
            String vimId = jsonUtil.getJsonValue(createVcpeServiceRequest,
                    "requestDetails.cloudConfiguration.lcpCloudRegionId")
            if (vimId.contains("_") && vimId.split("_").length == 2 )  {
                def cloudRegion = vimId.split("_")
                execution.setVariable("cloudOwner", cloudRegion[0])
                msoLogger.debug("cloudOwner: " + cloudRegion[0])
                execution.setVariable("cloudRegionId", cloudRegion[1])
                msoLogger.debug("cloudRegionId: " + cloudRegion[1])
                execution.setVariable("lcpCloudRegionId", cloudRegion[1])
                msoLogger.debug("lcpCloudRegionId: " + cloudRegion[1])
            } else {
                msoLogger.debug("vimId is not present - setting cloudRegion/cloudOwner from request.")
                String cloudOwner = jsonUtil.getJsonValue(createVcpeServiceRequest,
                        "requestDetails.cloudConfiguration.cloudOwner")
                if (!cloudOwner?.empty && cloudOwner != "")
                {
                    execution.setVariable("cloudOwner", cloudOwner)
                    msoLogger.debug("cloudOwner: " + cloudOwner)
                }
                execution.setVariable("cloudRegionId", vimId)
                msoLogger.debug("cloudRegionId: " + vimId)
                execution.setVariable("lcpCloudRegionId", vimId)
                msoLogger.debug("lcpCloudRegionId: " + vimId)
            }

            String tenantId = jsonUtil.getJsonValue(createVcpeServiceRequest,
                    "requestDetails.cloudConfiguration.tenantId")
            execution.setVariable("tenantId", tenantId)
            msoLogger.debug("tenantId: " + tenantId)

            String sdncVersion = execution.getVariable("sdncVersion")
            msoLogger.debug("sdncVersion: " + sdncVersion)

            msoLogger.trace("Completed prepareVnfAndModulesCreate of CreateVcpeResCustService ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareVnfAndModulesCreate() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    // *******************************
    //     Validate Vnf request Section -> increment count
    // *******************************
    public void validateVnfCreate(DelegateExecution execution) {

        try {
            msoLogger.trace("Inside validateVnfCreate of CreateVcpeResCustService ")

            Integer vnfsCreatedCount = execution.getVariable(Prefix + "VnfsCreatedCount")
            vnfsCreatedCount++

            execution.setVariable(Prefix + "VnfsCreatedCount", vnfsCreatedCount)

            msoLogger.debug(" ***** Completed validateVnfCreate of CreateVcpeResCustService ***** " + " vnf # " + vnfsCreatedCount)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method validateVnfCreate() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    // *****************************************
    //     Prepare Completion request Section
    // *****************************************
    public void postProcessResponse(DelegateExecution execution) {

        msoLogger.trace("Inside postProcessResponse of CreateVcpeResCustService ")

        try {
            String source = execution.getVariable("source")
            String requestId = execution.getVariable("mso-request-id")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            String msoCompletionRequest =
                    """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                    xmlns:ns="http://org.onap/so/request/types/v1">
                            <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                                <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                                <action>CREATE</action>
                                <source>${MsoUtils.xmlEscape(source)}</source>
                            </request-info>
                            <status-message>Service Instance has been created successfully via macro orchestration</status-message>
                            <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
                            <mso-bpel-name>BPMN macro create</mso-bpel-name>
                        </aetgt:MsoCompletionRequest>"""

            // Format Response
            String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

            msoLogger.debug(xmlMsoCompletionRequest)
            execution.setVariable(Prefix + "Success", true)
            execution.setVariable(Prefix + "CompleteMsoProcessRequest", xmlMsoCompletionRequest)
            msoLogger.debug(" SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void preProcessRollback(DelegateExecution execution) {
        msoLogger.trace("preProcessRollback of CreateVcpeResCustService ")
        try {

            Object workflowException = execution.getVariable("WorkflowException");

            if (workflowException instanceof WorkflowException) {
                msoLogger.debug("Prev workflowException: " + workflowException.getErrorMessage())
                execution.setVariable("prevWorkflowException", workflowException);
                //execution.setVariable("WorkflowException", null);
            }
        } catch (BpmnError e) {
            msoLogger.debug("BPMN Error during preProcessRollback")
        } catch (Exception ex) {
            String msg = "Exception in preProcessRollback. " + ex.getMessage()
            msoLogger.debug(msg)
        }
        msoLogger.trace("Exit preProcessRollback of CreateVcpeResCustService ")
    }

    public void postProcessRollback(DelegateExecution execution) {
        msoLogger.trace("postProcessRollback of CreateVcpeResCustService ")
        String msg = ""
        try {
            Object workflowException = execution.getVariable("prevWorkflowException");
            if (workflowException instanceof WorkflowException) {
                msoLogger.debug("Setting prevException to WorkflowException: ")
                execution.setVariable("WorkflowException", workflowException);
            }
        } catch (BpmnError b) {
            msoLogger.debug("BPMN Error during postProcessRollback")
            throw b;
        } catch (Exception ex) {
            msg = "Exception in postProcessRollback. " + ex.getMessage()
            msoLogger.debug(msg)
        }
        msoLogger.trace("Exit postProcessRollback of CreateVcpeResCustService ")
    }

    public void prepareFalloutRequest(DelegateExecution execution) {

        msoLogger.trace("STARTED CreateVcpeResCustService prepareFalloutRequest Process ")

        try {
            WorkflowException wfex = execution.getVariable("WorkflowException")
            msoLogger.debug(" Incoming Workflow Exception: " + wfex.toString())
            String requestInfo = execution.getVariable(Prefix + "requestInfo")
            msoLogger.debug(" Incoming Request Info: " + requestInfo)

            //TODO. hmmm. there is no way to UPDATE error message.
//          String errorMessage = wfex.getErrorMessage()
//          boolean successIndicator = execution.getVariable("DCRESI_rolledBack")
//          if (successIndicator){
//              errorMessage = errorMessage + ". Rollback successful."
//          } else {
//              errorMessage = errorMessage + ". Rollback not completed."
//          }

            String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

            execution.setVariable(Prefix + "falloutRequest", falloutRequest)

        } catch (Exception ex) {
            msoLogger.debug("Error Occured in CreateVcpeResCustService prepareFalloutRequest Process " + ex.getMessage())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVcpeResCustService prepareFalloutRequest Process")
        }
        msoLogger.trace("COMPLETED CreateVcpeResCustService prepareFalloutRequest Process ")
    }


    public void sendSyncError(DelegateExecution execution) {
        execution.setVariable("prefix", Prefix)

        msoLogger.trace("Inside sendSyncError() of CreateVcpeResCustService ")

        try {
            String errorMessage = ""
            def wfe = execution.getVariable("WorkflowException")
            if (wfe instanceof WorkflowException) {
                errorMessage = wfe.getErrorMessage()
            } else {
                errorMessage = "Sending Sync Error."
            }

            String buildworkflowException =
                    """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
                    <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
                    <aetgt:ErrorCode>7000</aetgt:ErrorCode>
                    </aetgt:WorkflowException>"""

            msoLogger.debug(buildworkflowException)
            sendWorkflowResponse(execution, 500, buildworkflowException)
        } catch (Exception ex) {
            msoLogger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
        }
    }

    public void processJavaException(DelegateExecution execution) {
        execution.setVariable("prefix", Prefix)
        try {
            msoLogger.debug("Caught a Java Exception")
            msoLogger.debug("Started processJavaException Method")
            msoLogger.debug("Variables List: " + execution.getVariables())
            execution.setVariable(Prefix + "unexpectedError", "Caught a Java Lang Exception")
            // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
        } catch (BpmnError b) {
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Rethrowing MSOWorkflowException", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
            throw b
        } catch (Exception e) {
            msoLogger.debug("Caught Exception during processJavaException Method: " + e)
            execution.setVariable(Prefix + "unexpectedError", "Exception in processJavaException method")
            // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
        }
        msoLogger.debug("Completed processJavaException Method")
    }
}
