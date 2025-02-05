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

import javax.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.L3Network
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.constants.Defaults
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils
import groovy.json.JsonOutput

public class DoDeleteNetworkInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteNetworkInstance.class);

    String Prefix= "DELNWKI_"
    String groovyClassName = "DoDeleteNetworkInstance"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    VidUtils vidUtils = new VidUtils(this)
    NetworkUtils networkUtils = new NetworkUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    public InitializeProcessVariables(DelegateExecution execution){
        /* Initialize all the process variables in this block */

        execution.setVariable(Prefix + "networkRequest", "")
        execution.setVariable(Prefix + "isSilentSuccess", false)
        execution.setVariable(Prefix + "Success", false)

        execution.setVariable(Prefix + "requestId", "")
        execution.setVariable(Prefix + "source", "")
        execution.setVariable(Prefix + "lcpCloudRegion", "")
        execution.setVariable(Prefix + "networkInputs", "")
        execution.setVariable(Prefix + "tenantId", "")

        execution.setVariable(Prefix + "queryAAIResponse", "")
        execution.setVariable(Prefix + "aaiReturnCode", "")
        execution.setVariable(Prefix + "isAAIGood", false)
        execution.setVariable(Prefix + "isVfRelationshipExist", false)

        // AAI query Cloud Region
        execution.setVariable(Prefix + "queryCloudRegionRequest","")
        execution.setVariable(Prefix + "queryCloudRegionReturnCode","")
        execution.setVariable(Prefix + "queryCloudRegionResponse","")
        execution.setVariable(Prefix + "cloudRegionPo","")
        execution.setVariable(Prefix + "cloudRegionSdnc","")

        execution.setVariable(Prefix + "deleteNetworkRequest", "")
        execution.setVariable(Prefix + "deleteNetworkResponse", "")
        execution.setVariable(Prefix + "networkReturnCode", "")
        execution.setVariable(Prefix + "rollbackNetworkRequest", "")

        execution.setVariable(Prefix + "deleteSDNCRequest", "")
        execution.setVariable(Prefix + "deleteSDNCResponse", "")
        execution.setVariable(Prefix + "sdncReturnCode", "")
        execution.setVariable(Prefix + "sdncResponseSuccess", false)

        execution.setVariable(Prefix + "deactivateSDNCRequest", "")
        execution.setVariable(Prefix + "deactivateSDNCResponse", "")
        execution.setVariable(Prefix + "deactivateSdncReturnCode", "")
        execution.setVariable(Prefix + "isSdncDeactivateRollbackNeeded", "")

        execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", "")
        execution.setVariable(Prefix + "isException", false)


    }

    // **************************************************
    //     Pre or Prepare Request Section
    // **************************************************

    public void preProcessRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside preProcessRequest() of " + groovyClassName + " Request ")

        // initialize flow variables
        InitializeProcessVariables(execution)

        try {
            // get incoming message/input
            execution.setVariable("action", "DELETE")
            String deleteNetwork = execution.getVariable("bpmnRequest")
            if (deleteNetwork != null) {
                if (deleteNetwork.contains("requestDetails")) {
                    // JSON format request is sent, create xml
                    try {
                        def prettyJson = JsonOutput.prettyPrint(deleteNetwork.toString())
                        logger.debug(" Incoming message formatted . . . : " + '\n' + prettyJson)
                        deleteNetwork =  vidUtils.createXmlNetworkRequestInfra(execution, deleteNetwork)

                    } catch (Exception ex) {
                        String dataErrorMessage = " Invalid json format Request - " + ex.getMessage()
                        logger.debug(dataErrorMessage)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                    }
                } else {
                    // XML format request is sent

                }
            } else {
                // vIPR format request is sent, create xml from individual variables
                deleteNetwork = vidUtils.createXmlNetworkRequestInstance(execution)
            }

            deleteNetwork = utils.formatXml(deleteNetwork)
            logger.debug(deleteNetwork)
            execution.setVariable(Prefix + "networkRequest", deleteNetwork)
            logger.debug(Prefix + "networkRequest - " + '\n' + deleteNetwork)

            // validate 'backout-on-failure' to override 'mso.rollback'
            boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, deleteNetwork)
            execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
            logger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)

            String networkInputs = utils.getNodeXml(deleteNetwork, "network-inputs", false).replace("tag0:","").replace(":tag0","")
            execution.setVariable(Prefix + "networkInputs", networkInputs)

            // prepare messageId
            String messageId = execution.getVariable("testMessageId")  // for testing
            if (messageId == null || messageId == "") {
                messageId = UUID.randomUUID()
                logger.debug(Prefix + "messageId, random generated: " + messageId)
            } else {
                logger.debug(Prefix + "messageId, pre-assigned: " + messageId)
            }
            execution.setVariable(Prefix + "messageId", messageId)

            String source = utils.getNodeText(deleteNetwork, "source")
            execution.setVariable(Prefix + "source", source)
            logger.debug(Prefix + "source - " + source)

            String networkId = ""
            if (utils.nodeExists(networkInputs, "network-id")) {
                networkId = utils.getNodeText(networkInputs, "network-id")
                if (networkId == null || networkId == "" || networkId == 'null' ) {
                    sendSyncError(execution)
                    // missing value of network-id
                    String dataErrorMessage = "network-request has missing 'network-id' element/value."
                    logger.debug(" Invalid Request - " + dataErrorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                }
            }

            // lcpCloudRegion or tenantId not sent, will be extracted from query AA&I
            def lcpCloudRegion = null
            if (utils.nodeExists(networkInputs, "aic-cloud-region")) {
                lcpCloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
                if (lcpCloudRegion == 'null') {
                    lcpCloudRegion = null
                }
            }
            execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
            logger.debug("lcpCloudRegion : " + lcpCloudRegion)

            String tenantId = null
            if (utils.nodeExists(networkInputs, "tenant-id")) {
                tenantId = utils.getNodeText(networkInputs, "tenant-id")
                if (tenantId == 'null') {
                    tenantId = null
                }

            }
            execution.setVariable(Prefix + "tenantId", tenantId)
            logger.debug("tenantId : " + tenantId)

            String sdncVersion = execution.getVariable("sdncVersion")
            logger.debug("sdncVersion? : " + sdncVersion)

            // PO Authorization Info / headers Authorization=
            String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

            try {
                def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey", execution))
                execution.setVariable("BasicAuthHeaderValuePO",encodedString)
                execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

            } catch (IOException ex) {
                String dataErrorMessage = " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
                logger.debug(dataErrorMessage )
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            }

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex){
            // caught exception
            String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, PreProcessRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }


    public void callRESTQueryAAI (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAI() of DoDoDeleteNetworkInstance ***** " )

        // get variables
        String networkInputs  = execution.getVariable(Prefix + "networkInputs")
        String networkId   = utils.getNodeText(networkInputs, "network-id")
        networkId = UriUtils.encode(networkId,"UTF-8")
        ExceptionUtil exceptionUtil = new ExceptionUtil()
        Boolean isVfRelationshipExist = false
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ALL)
            Optional<L3Network> l3Network = getAAIClient().get(L3Network.class,uri);
            AAIResultWrapper wrapper = getAAIClient().get(uri);
            Optional<Relationships> relationships = wrapper.getRelationships()

            if (l3Network.isPresent()) {
                execution.setVariable(Prefix + "aaiReturnCode", 200)
                execution.setVariable(Prefix + "queryAAIResponse", l3Network.get())
                execution.setVariable(Prefix + "isAAIGood", true)
                if (relationships.isPresent()){
                    if(!relationships.get().getRelatedUris(Types.VF_MODULE).isEmpty()){
                        execution.setVariable(Prefix + "isVfRelationshipExist", true)
                        isVfRelationshipExist = true
                        String relationshipMessage = "AAI Query Success Response but 'vf-module' relationship exist, not allowed to delete: network Id: " + networkId
                        exceptionUtil.buildWorkflowException(execution, 2500, relationshipMessage)
                    }else{
                        List<AAIResourceUri> tenantURIList = relationships.get().getRelatedUris(Types.TENANT)
                        for(AAIResourceUri tenantURI: tenantURIList){
                            if(execution.getVariable(Prefix + "tenantId") == null) {
                                String tenantId = tenantURI.getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId)
                                execution.setVariable(Prefix + "tenantId", tenantId)
                                logger.debug(" Get AAI getTenantId()  : " + tenantId)
                            }
                        }
                        List<AAIResourceUri> cloudRegionURIList = relationships.get().getRelatedUris(Types.CLOUD_REGION)
                        for(AAIResourceUri tenantURI: cloudRegionURIList){
                            if(execution.getVariable(Prefix + "lcpCloudRegion") == null) {
                                String lcpCloudRegion = tenantURI.getURIKeys().get(AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudRegionId)
                                execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
                                logger.debug(" Get AAI getCloudRegion()  : " + lcpCloudRegion)
                            }
                        }
                    }
                }
                logger.debug(Prefix + "isVfRelationshipExist - " + isVfRelationshipExist)
            } else {
                // not found // empty aai response
                execution.setVariable(Prefix + "aaiReturnCode", 404)
                execution.setVariable(Prefix + "isAAIGood", false)
                execution.setVariable(Prefix + "isSilentSuccess", true)
                logger.debug(" AAI Query is Silent Success")
            }
            logger.debug(" AAI Query call, isAAIGood? : " + execution.getVariable(Prefix + "isAAIGood"))
        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, callRESTQueryAAI() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAICloudRegion of DoDeleteNetworkInstance ***** " )

        try {
            String networkInputs  = execution.getVariable(Prefix + "networkInputs")
            // String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
            String cloudRegion = execution.getVariable(Prefix + "lcpCloudRegion")
            // Prepare AA&I url
            AaiUtil aaiUtil = new AaiUtil(this)

            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion))
            def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

            execution.setVariable(Prefix + "queryCloudRegionRequest", queryCloudRegionRequest)

            String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
            String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

            if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
                execution.setVariable(Prefix + "cloudRegionPo", cloudRegionPo)
                execution.setVariable(Prefix + "cloudRegionSdnc", cloudRegionSdnc)

            } else {
                String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
                logger.debug(dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

            }

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, callRESTQueryAAICloudRegion(). Unexpected Response from AAI - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareNetworkRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareNetworkRequest of DoDeleteNetworkInstance ")
        ExceptionUtil exceptionUtil = new ExceptionUtil()
        try {
            // get variables
            String networkRequest = execution.getVariable(Prefix + "networkRequest")
            String cloudSiteId = execution.getVariable(Prefix + "cloudRegionPo")
            String tenantId = execution.getVariable(Prefix + "tenantId")

            L3Network l3Network = execution.getVariable(Prefix + "queryAAIResponse")
            String networkType = l3Network.getNetworkType()
            String networkId = l3Network.getNetworkId()

            String networkStackId = ""
            networkStackId = l3Network.getHeatStackId()
            if (networkStackId == 'null' || networkStackId == "" || networkStackId == null) {
                networkStackId = "force_delete"
            }

            String requestId = execution.getVariable("msoRequestId")
            if (requestId != null) {
                execution.setVariable("mso-request-id", requestId)
            } else {
                requestId = execution.getVariable("mso-request-id")
            }
            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            // Added new Elements
            String messageId = execution.getVariable(Prefix + "messageId")
            String notificationUrl = ""                                   //TODO - is this coming from URN? What variable/value to use?
            //String notificationUrl = execution.getVariable("URN_?????") //TODO - is this coming from URN? What variable/value to use?

            String modelCustomizationUuid = ""
            if (utils.nodeExists(networkRequest, "networkModelInfo")) {
                String networkModelInfo = utils.getNodeXml(networkRequest, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
                modelCustomizationUuid = utils.getNodeText(networkModelInfo, "modelCustomizationUuid")
            } else {
                modelCustomizationUuid = utils.getNodeText(networkRequest, "modelCustomizationId")
            }

            String deleteNetworkRequest = """
					  <deleteNetworkRequest>
					    <cloudSiteId>${MsoUtils.xmlEscape(cloudSiteId)}</cloudSiteId>
					    <tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
					    <networkId>${MsoUtils.xmlEscape(networkId)}</networkId>
						<networkStackId>${MsoUtils.xmlEscape(networkStackId)}</networkStackId>
					    <networkType>${MsoUtils.xmlEscape(networkType)}</networkType>
						<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
						<skipAAI>true</skipAAI>
					    <msoRequest>
					       <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
					       <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
					    </msoRequest>
						<messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
						<notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
					  </deleteNetworkRequest>
						""".trim()

            logger.debug(Prefix + "deleteNetworkRequest - " + "\n" +  deleteNetworkRequest)
            // Format Response
            String buildDeleteNetworkRequestAsString = utils.formatXml(deleteNetworkRequest)
            logger.debug(buildDeleteNetworkRequestAsString)
            logger.debug(Prefix + "deleteNetworkRequestAsString - " + "\n" +  buildDeleteNetworkRequestAsString)

            String restURL = UrnPropertiesReader.getVariable("mso.adapters.network.rest.endpoint", execution)
            execution.setVariable("mso.adapters.network.rest.endpoint", restURL + "/" + networkId)
            logger.debug("mso.adapters.network.rest.endpoint - " + "\n" +  restURL + "/" + networkId)

            execution.setVariable(Prefix + "deleteNetworkRequest", buildDeleteNetworkRequestAsString)
            logger.debug(Prefix + "deleteNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString)
        }
        catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareNetworkRequest(). Unexpected Response from AAI - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
    }

    /**
     * This method is used instead of an HTTP Connector task because the
     * connector does not allow DELETE with a body.
     */
    public void sendRequestToVnfAdapter(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.sendRequestToVnfAdapter(' +
                'execution=' + execution.getId() +
                ')'

        logger.trace('Entered ' + method)

        try {

            String vnfAdapterUrl = UrnPropertiesReader.getVariable("mso.adapters.network.rest.endpoint",execution)
            String vnfAdapterRequest = execution.getVariable(Prefix + "deleteNetworkRequest")

            URL url = new URL(vnfAdapterUrl)
            HttpClient httpClient = new HttpClientFactory().newXmlClient(url, ONAPComponents.OPENSTACK_ADAPTER)
            httpClient.accept = "application/xml"
            httpClient.addAdditionalHeader("Authorization", execution.getVariable("BasicAuthHeaderValuePO"))
            Response response = httpClient.delete(vnfAdapterRequest)

            execution.setVariable(Prefix + "deleteNetworkResponse", response.readEntity(String.class))
            execution.setVariable(Prefix + "networkReturnCode", response.getStatus())

        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, sendRequestToVnfAdapter() - " + ex.getMessage()
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), exceptionMessage,
                    "BPMN", ErrorCode.UnknownError.getValue(),
                    "Exception is:\n" + ex);
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
    }


    public void prepareSDNCRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareSDNCRequest of DoDeleteNetworkInstance ")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String networkId = ""
            if (utils.nodeExists(deleteNetworkInput, "network-id")) {
                networkId = utils.getNodeText(deleteNetworkInput, "network-id")
            }
            if (networkId == 'null') {networkId = ""}

            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            // get/set 'msoRequestId' and 'mso-request-id'
            String requestId = execution.getVariable("msoRequestId")
            if (requestId != null) {
                execution.setVariable("mso-request-id", requestId)
            } else {
                requestId = execution.getVariable("mso-request-id")
            }
            execution.setVariable(Prefix + "requestId", requestId)
            logger.debug(Prefix + "requestId " + requestId)
            L3Network queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")

            SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            // 1. prepare delete topology via SDNC Adapter SUBFLOW call
            String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "delete", "DisconnectNetworkRequest", cloudRegionId, networkId, queryAAIResponse, null)
            String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
            logger.debug(sndcTopologyDeleteRequesAsString)
            execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
            logger.debug(Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)

        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), exceptionMessage,
                    "BPMN", ErrorCode.UnknownError.getValue(),
                    "Exception is:\n" + ex);
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareRpcSDNCRequest of DoDeleteNetworkInstance ")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String networkId = ""
            if (utils.nodeExists(deleteNetworkInput, "network-id")) {
                networkId = utils.getNodeText(deleteNetworkInput, "network-id")
            }
            if (networkId == 'null') {networkId = ""}

            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            // 1. prepare delete topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "unassign", "DeleteNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            logger.debug(sndcTopologyDeleteRequesAsString)
            execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
            logger.debug(Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)

        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), exceptionMessage,
                    "BPMN", ErrorCode.UnknownError.getValue(),
                    "Exception is:\n" + ex);
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }


    public void prepareRpcSDNCDeactivate(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCDeactivate() of DoDeleteNetworkInstance ")

        try {

            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String networkId = ""
            if (utils.nodeExists(deleteNetworkInput, "network-id")) {
                networkId = utils.getNodeText(deleteNetworkInput, "network-id")
            }
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "deactivate", "DeleteNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            execution.setVariable(Prefix + "deactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
            logger.debug(" Preparing request for RPC SDNC Topology deactivate - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCActivateRollback() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void validateSDNCResponse (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside validateSDNCResponse of DoDeleteNetworkInstance ")

        String response = execution.getVariable(Prefix + "deleteSDNCResponse")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
        WorkflowException workflowException = execution.getVariable("WorkflowException")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
        // reset variable
        String deleteSDNCResponseDecodeXml = execution.getVariable(Prefix + "deleteSDNCResponse")
        deleteSDNCResponseDecodeXml = deleteSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
        execution.setVariable(Prefix + "deleteSDNCResponse", deleteSDNCResponseDecodeXml)

        if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
            execution.setVariable(Prefix + "isSdncRollbackNeeded", true)      //
            execution.setVariable(Prefix + "isPONR", true)
            logger.debug("Successfully Validated SDNC Response")
        } else {
            logger.debug("Did NOT Successfully Validated SDNC Response")
            throw new BpmnError("MSOWorkflowException")
        }

    }

    public void validateRpcSDNCDeactivateResponse (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside validateRpcSDNCDeactivateResponse() of DoDeleteNetworkInstance ")

        String response = execution.getVariable(Prefix + "deactivateSDNCResponse")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
        WorkflowException workflowException = execution.getVariable("WorkflowException")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
        // reset variable
        String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "deactivateSDNCResponse")
        assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
        execution.setVariable(Prefix + "deactivateSDNCResponse", assignSDNCResponseDecodeXml)

        if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
            execution.setVariable(Prefix + "isSdncDeactivateRollbackNeeded", true)
            logger.debug("Successfully Validated Rpc SDNC Activate Response")

        } else {
            logger.debug("Did NOT Successfully Validated Rpc SDNC Deactivate Response")
            throw new BpmnError("MSOWorkflowException")
        }

    }

    public void prepareRpcSDNCDeactivateRollback(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCDeactivateRollback() of DoDeleteNetworkInstance ")

        try {

            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String deactivateSDNCResponse = execution.getVariable(Prefix + "deactivateSDNCResponse")
            String networkId = utils.getNodeText(deactivateSDNCResponse, "network-id")
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            // 2. prepare rollback topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "activate", "CreateNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
            logger.debug(" Preparing request for RPC SDNC Topology 'activate-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCDeactivateRollback() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRollbackData() of DoDeleteNetworkInstance ")

        try {

            Map<String, String> rollbackData = new HashMap<String, String>();
            String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
            if (rollbackNetworkRequest != null) {
                if (rollbackNetworkRequest != "") {
                    rollbackData.put("rollbackNetworkRequest", execution.getVariable(Prefix + "rollbackNetworkRequest"))
                }
            }
            String rollbackDeactivateSDNCRequest = execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest")
            if (rollbackDeactivateSDNCRequest != null) {
                if (rollbackDeactivateSDNCRequest != "") {
                    rollbackData.put("rollbackDeactivateSDNCRequest", execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest"))
                }
            }
            execution.setVariable("rollbackData", rollbackData)
            logger.debug("** rollbackData : " + rollbackData)

            execution.setVariable("WorkflowException", execution.getVariable("WorkflowException"))
            logger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void postProcessResponse (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside postProcessResponse of DoDeleteNetworkInstance ")

        try {

            logger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
            if (execution.getVariable(Prefix + "isException") == false) {
                execution.setVariable(Prefix + "Success", true)
                execution.setVariable("WorkflowException", null)
                if (execution.getVariable(Prefix + "isSilentSuccess") == true) {
                    execution.setVariable("rolledBack", false)
                } else {
                    execution.setVariable("rolledBack", true)
                }
                prepareSuccessRollbackData(execution) // populate rollbackData

            } else {
                execution.setVariable(Prefix + "Success", false)
                execution.setVariable("rollbackData", null)
                String exceptionMessage = " Exception encountered in MSO Bpmn. "
                if (execution.getVariable("workflowException") != null) {  // Output of Rollback flow.
                    logger.debug(" ***** workflowException: " + execution.getVariable("workflowException"))
                    WorkflowException wfex = execution.getVariable("workflowException")
                    exceptionMessage = wfex.getErrorMessage()
                } else {
                    if (execution.getVariable(Prefix + "WorkflowException") != null) {
                        WorkflowException pwfex = execution.getVariable(Prefix + "WorkflowException")
                        exceptionMessage = pwfex.getErrorMessage()
                    } else {
                        if (execution.getVariable("WorkflowException") != null) {
                            WorkflowException pwfex = execution.getVariable("WorkflowException")
                            exceptionMessage = pwfex.getErrorMessage()
                        }
                    }
                }

                // going to the Main flow: a-la-carte or macro
                logger.debug(" ***** postProcessResponse(), BAD !!!")
                exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
                throw new BpmnError("MSOWorkflowException")

            }

        } catch(BpmnError b){
            logger.debug("Rethrowing MSOWorkflowException")
            throw b

        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, postProcessResponse() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
            throw new BpmnError("MSOWorkflowException")

        }

    }

    public void prepareSuccessRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareSuccessRollbackData() of DoDeleteNetworkInstance ")

        try {

            if (execution.getVariable("sdncVersion") != '1610') {
                prepareRpcSDNCDeactivateRollback(execution)
                prepareRpcSDNCUnassignRollback(execution)
            } else {
                prepareSDNCRollback(execution)
            }

            Map<String, String> rollbackData = new HashMap<String, String>();
            String rollbackSDNCRequest = execution.getVariable(Prefix + "rollbackSDNCRequest")
            if (rollbackSDNCRequest != null) {
                if (rollbackSDNCRequest != "") {
                    rollbackData.put("rollbackSDNCRequest", execution.getVariable(Prefix + "rollbackSDNCRequest"))
                }
            }
            String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
            if (rollbackNetworkRequest != null) {
                if (rollbackNetworkRequest != "") {
                    rollbackData.put("rollbackNetworkRequest", execution.getVariable(Prefix + "rollbackNetworkRequest"))
                }
            }
            String rollbackDeactivateSDNCRequest = execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest")
            if (rollbackDeactivateSDNCRequest != null) {
                if (rollbackDeactivateSDNCRequest != "") {
                    rollbackData.put("rollbackDeactivateSDNCRequest", execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest"))
                }
            }
            execution.setVariable("rollbackData", rollbackData)

            logger.debug("** rollbackData : " + rollbackData)
            execution.setVariable("WorkflowException", null)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCUnassignRollback(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCUnassignRollbac() of DoDeleteNetworkInstance ")

        try {

            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String deleteSDNCResponse = execution.getVariable(Prefix + "deleteSDNCResponse")
            String networkId = utils.getNodeText(deleteSDNCResponse, "network-id")
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            // 1. prepare delete topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "assign", "CreateNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            logger.debug(sndcTopologyDeleteRequesAsString)
            execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
            logger.debug(Prefix + "rollbackSDNCRequest" + "\n" +  sndcTopologyDeleteRequesAsString)
            logger.debug(" Preparing request for RPC SDNC Topology 'assign-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCUnassignRollback() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareSDNCRollback (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareSDNCRollback of DoDeleteNetworkInstance ")

        try {

            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String networkId = ""
            if (utils.nodeExists(deleteNetworkInput, "network-id")) {
                networkId = utils.getNodeText(deleteNetworkInput, "network-id")
            }
            if (networkId == 'null') {networkId = ""}

            String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

            // get/set 'msoRequestId' and 'mso-request-id'
            String requestId = execution.getVariable("msoRequestId")
            if (requestId != null) {
                execution.setVariable("mso-request-id", requestId)
            } else {
                requestId = execution.getVariable("mso-request-id")
            }
            execution.setVariable(Prefix + "requestId", requestId)

            L3Network queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")

            SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            // 1. prepare delete topology via SDNC Adapter SUBFLOW call
            String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "rollback", "DisconnectNetworkRequest", cloudRegionId, networkId, queryAAIResponse, null)
            String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
            logger.debug(sndcTopologyDeleteRequesAsString)
            execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
            logger.debug(Prefix + "rollbackSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)
            logger.debug(" Preparing request for RPC SDNC Topology 'rollback-DisconnectNetworkRequest' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString)


        } catch (Exception ex) {
            // caught exception
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRollback() - " + ex.getMessage()
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), exceptionMessage,
                    "BPMN", ErrorCode.UnknownError.getValue(),
                    "Exception is:\n" + ex);
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void setExceptionFlag(DelegateExecution execution){

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside setExceptionFlag() of DoDeleteNetworkInstance ")

        try {

            execution.setVariable(Prefix + "isException", true)

            if (execution.getVariable("SavedWorkflowException1") != null) {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
            } else {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
            }
            logger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))

        } catch(Exception ex){
            String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
        }

    }


    // *******************************
    //     Build Error Section
    // *******************************

    public void processJavaException(DelegateExecution execution){

        execution.setVariable("prefix",Prefix)
        try{
            logger.debug("Caught a Java Exception")
            logger.debug("Started processJavaException Method")
            logger.debug("Variables List: " + execution.getVariables())
            execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

        }catch(Exception e){
            logger.debug("Caught Exception during processJavaException Method: " + e)
            execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
        }
        logger.debug("Completed processJavaException Method of " + Prefix)
    }

}
