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

package org.onap.so.bpmn.infrastructure.scripts;

import jakarta.ws.rs.NotFoundException
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.L3Network
import org.onap.aai.domain.yang.L3Networks
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.RouteTableReference
import org.onap.aai.domain.yang.RouteTarget
import org.onap.aai.domain.yang.Subnet
import org.onap.aai.domain.yang.VpnBinding
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
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
import org.onap.so.constants.Defaults
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.*

/**
 * This groovy class supports the <class>DoCreateNetworkInstance.bpmn</class> process.
 *
 */
public class DoCreateNetworkInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCreateNetworkInstance.class);

    String Prefix="CRENWKI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    VidUtils vidUtils = new VidUtils(this)
    NetworkUtils networkUtils = new NetworkUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    def className = getClass().getSimpleName()

    /**
     * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstance.bpmn</class> process.
     * @param execution
     */
    public InitializeProcessVariables(DelegateExecution execution){
        /* Initialize all the process variables in this block */

        execution.setVariable(Prefix + "networkRequest", "")
        execution.setVariable(Prefix + "rollbackEnabled", null)
        execution.setVariable(Prefix + "networkInputs", "")
        //execution.setVariable(Prefix + "requestId", "")
        execution.setVariable(Prefix + "messageId", "")
        execution.setVariable(Prefix + "source", "")
        execution.setVariable("BasicAuthHeaderValuePO", "")
        execution.setVariable("BasicAuthHeaderValueSDNC", "")
        execution.setVariable(Prefix + "serviceInstanceId","")
        execution.setVariable("GENGS_type", "")
        execution.setVariable(Prefix + "rsrc_endpoint", null)
        execution.setVariable(Prefix + "networkOutputs", "")
        execution.setVariable(Prefix + "networkId","")
        execution.setVariable(Prefix + "networkName","")

        // AAI query Name
        execution.setVariable(Prefix + "queryNameAAIRequest","")
        execution.setVariable(Prefix + "queryNameAAIResponse", "")
        execution.setVariable(Prefix + "aaiNameReturnCode", "")
        execution.setVariable(Prefix + "isAAIqueryNameGood", false)

        // AAI query Cloud Region
        execution.setVariable(Prefix + "queryCloudRegionRequest","")
        execution.setVariable(Prefix + "queryCloudRegionReturnCode","")
        execution.setVariable(Prefix + "queryCloudRegionResponse","")
        execution.setVariable(Prefix + "cloudRegionPo","")
        execution.setVariable(Prefix + "cloudRegionSdnc","")
        execution.setVariable(Prefix + "isCloudRegionGood", false)

        // AAI query Id
        execution.setVariable(Prefix + "queryIdAAIRequest","")
        execution.setVariable(Prefix + "queryIdAAIResponse", "")
        execution.setVariable(Prefix + "aaiIdReturnCode", "")

        // AAI query vpn binding
        execution.setVariable(Prefix + "queryVpnBindingAAIRequest","")
        execution.setVariable(Prefix + "queryVpnBindingAAIResponse", "")
        execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "")
        execution.setVariable(Prefix + "vpnBindings", null)
        execution.setVariable(Prefix + "vpnCount", 0)
        execution.setVariable(Prefix + "routeCollection", "")

        // AAI query network policy
        execution.setVariable(Prefix + "queryNetworkPolicyAAIRequest","")
        execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", "")
        execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "")
        execution.setVariable(Prefix + "networkPolicyUriList", null)
        execution.setVariable(Prefix + "networkPolicyCount", 0)
        execution.setVariable(Prefix + "networkCollection", "")

        // AAI query route table reference
        execution.setVariable(Prefix + "queryNetworkTableRefAAIRequest","")
        execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", "")
        execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "")
        execution.setVariable(Prefix + "networkTableRefUriList", null)
        execution.setVariable(Prefix + "networkTableRefCount", 0)
        execution.setVariable(Prefix + "tableRefCollection", "")

        // AAI requery Id
        execution.setVariable(Prefix + "requeryIdAAIRequest","")
        execution.setVariable(Prefix + "requeryIdAAIResponse", "")
        execution.setVariable(Prefix + "aaiRequeryIdReturnCode", "")

        // AAI update contrail
        execution.setVariable(Prefix + "updateContrailAAIUrlRequest","")
        execution.setVariable(Prefix + "updateContrailAAIPayloadRequest","")
        execution.setVariable(Prefix + "updateContrailAAIResponse", "")
        execution.setVariable(Prefix + "aaiUpdateContrailReturnCode", "")

        execution.setVariable(Prefix + "createNetworkRequest", "")
        execution.setVariable(Prefix + "createNetworkResponse", "")
        execution.setVariable(Prefix + "rollbackNetworkRequest", "")
        //execution.setVariable(Prefix + "rollbackNetworkResponse", "")
        execution.setVariable(Prefix + "networkReturnCode", "")
        //execution.setVariable(Prefix + "rollbackNetworkReturnCode", "")
        execution.setVariable(Prefix + "isNetworkRollbackNeeded", false)

        execution.setVariable(Prefix + "assignSDNCRequest", "")
        execution.setVariable(Prefix + "assignSDNCResponse", "")
        execution.setVariable(Prefix + "rollbackSDNCRequest", "")
        //execution.setVariable(Prefix + "rollbackSDNCResponse", "")
        execution.setVariable(Prefix + "sdncReturnCode", "")
        //execution.setVariable(Prefix + "rollbackSDNCReturnCode", "")
        execution.setVariable(Prefix + "isSdncRollbackNeeded", false)
        execution.setVariable(Prefix + "sdncResponseSuccess", false)

        execution.setVariable(Prefix + "activateSDNCRequest", "")
        execution.setVariable(Prefix + "activateSDNCResponse", "")
        execution.setVariable(Prefix + "rollbackActivateSDNCRequest", "")
        //execution.setVariable(Prefix + "rollbackActivateSDNCResponse", "")
        execution.setVariable(Prefix + "sdncActivateReturnCode", "")
        //execution.setVariable(Prefix + "rollbackActivateSDNCReturnCode", "")
        execution.setVariable(Prefix + "isSdncActivateRollbackNeeded", false)
        execution.setVariable(Prefix + "sdncActivateResponseSuccess", false)

        execution.setVariable(Prefix + "orchestrationStatus", "")
        execution.setVariable(Prefix + "isVnfBindingPresent", false)
        execution.setVariable(Prefix + "Success", false)

        execution.setVariable(Prefix + "isException", false)

    }

    // **************************************************
    //     Pre or Prepare Request Section
    // **************************************************
    /**
     * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstance.bpmn</class> process.
     * @param execution
     */
    public void preProcessRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)
        logger.trace("Inside preProcessRequest() of " + className + ".groovy")

        try {
            // initialize flow variables
            InitializeProcessVariables(execution)

            // GET Incoming request & validate 3 kinds of format.
            execution.setVariable("action", "CREATE")
            String networkRequest = execution.getVariable("bpmnRequest")
            if (networkRequest != null) {
                if (networkRequest.contains("requestDetails")) {
                    // JSON format request is sent, create xml
                    try {
                        def prettyJson = JsonOutput.prettyPrint(networkRequest.toString())
                        logger.debug(" Incoming message formatted . . . : " + '\n' + prettyJson)
                        networkRequest =  vidUtils.createXmlNetworkRequestInfra(execution, networkRequest)

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
                networkRequest = vidUtils.createXmlNetworkRequestInstance(execution)
            }

            networkRequest = utils.formatXml(networkRequest)
            execution.setVariable(Prefix + "networkRequest", networkRequest)
            logger.debug(Prefix + "networkRequest - " + '\n' + networkRequest)

            // validate 'backout-on-failure' to override 'mso.rollback'
            boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
            execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
            logger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)

            String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
            execution.setVariable(Prefix + "networkInputs", networkInputs)
            logger.debug(Prefix + "networkInputs - " + '\n' + networkInputs)

            // prepare messageId
            String messageId = execution.getVariable("testMessageId")  // for testing
            if (messageId == null || messageId == "") {
                messageId = UUID.randomUUID()
                logger.debug(Prefix + "messageId, random generated: " + messageId)
            } else {
                logger.debug(Prefix + "messageId, pre-assigned: " + messageId)
            }
            execution.setVariable(Prefix + "messageId", messageId)

            String source = utils.getNodeText(networkRequest, "source")
            execution.setVariable(Prefix + "source", source)
            logger.debug(Prefix + "source - " + source)

            // validate cloud region
            String lcpCloudRegionId = utils.getNodeText(networkRequest, "aic-cloud-region")
            if ((lcpCloudRegionId == null) || (lcpCloudRegionId == "") || (lcpCloudRegionId == "null")) {
                String dataErrorMessage = "Missing value/element: 'lcpCloudRegionId' or 'cloudConfiguration' or 'aic-cloud-region'."
                logger.debug(" Invalid Request - " + dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            }

            // validate service instance id
            String serviceInstanceId = utils.getNodeText(networkRequest, "service-instance-id")
            if ((serviceInstanceId == null) || (serviceInstanceId == "") || (serviceInstanceId == "null")) {
                String dataErrorMessage = "Missing value/element: 'serviceInstanceId'."
                logger.debug(" Invalid Request - " + dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            }

            // PO Authorization Info / headers Authorization=
            String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)

            try {
                def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey",execution))
                execution.setVariable("BasicAuthHeaderValuePO",encodedString)
                execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

            } catch (IOException ex) {
                String exceptionMessage = "Exception Encountered in DoCreateNetworkInstance, PreProcessRequest() - "
                String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
                logger.debug(dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            }

            // Set variables for Generic Get Sub Flow use
            execution.setVariable(Prefix + "serviceInstanceId", serviceInstanceId)
            logger.debug(Prefix + "serviceInstanceId - " + serviceInstanceId)

            execution.setVariable("GENGS_type", "service-instance")
            logger.debug("GENGS_type - " + "service-instance")
            logger.debug(" Url for SDNC adapter: " + UrnPropertiesReader.getVariable("mso.adapters.sdnc.endpoint",execution))

            String sdncVersion = execution.getVariable("sdncVersion")
            logger.debug("sdncVersion? : " + sdncVersion)

            // build 'networkOutputs'
            String networkId = utils.getNodeText(networkRequest, "network-id")
            if ((networkId == null) || (networkId == "null")) {
                networkId = ""
            }
            String networkName = utils.getNodeText(networkRequest, "network-name")
            if ((networkName == null) || (networkName == "null")) {
                networkName = ""
            }
            String networkOutputs =
                    """<network-outputs>
	                   <network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
	                   <network-name>${MsoUtils.xmlEscape(networkName)}</network-name>
	                 </network-outputs>"""
            execution.setVariable(Prefix + "networkOutputs", networkOutputs)
            logger.debug(Prefix + "networkOutputs - " + '\n' + networkOutputs)
            execution.setVariable(Prefix + "networkId", networkId)
            execution.setVariable(Prefix + "networkName", networkName)

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            sendSyncError(execution)
            // caught exception
            String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    /**
     * Gets the service instance uri from aai
     */
    public void getServiceInstance(DelegateExecution execution) {
        try {
            String serviceInstanceId = execution.getVariable('CRENWKI_serviceInstanceId')

            AAIResourcesClient resourceClient = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))

            if(!resourceClient.exists(uri)){
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
            }else{
                Map<String, String> keys = uri.getURIKeys()
                execution.setVariable("serviceType", keys.get(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType))
                execution.setVariable("subscriberName", keys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId))
            }

        }catch(BpmnError e) {
            throw e;
        }catch (Exception ex){
            String msg = "Exception in getServiceInstance. " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    public void callRESTQueryAAINetworkName (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkName() of DoCreateNetworkInstance ***** " )

        try{
            // get variables
            String networkInputs  = execution.getVariable(Prefix + "networkInputs")
            String networkName   = utils.getNodeText(networkInputs, "network-name")

            AAIResourcesClient client = new AAIResourcesClient()
            AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Networks()).queryParam("network-name", networkName)
            L3Networks networks = client.get(uri, NotFoundException.class).asBean(L3Networks.class).get()
            L3Network network = networks.getL3Network().get(0)

            execution.setVariable(Prefix + "isAAIqueryNameGood", true)
            String orchestrationStatus = network.getOrchestrationStatus()
            execution.setVariable(Prefix + "orchestrationStatus", orchestrationStatus.toUpperCase())
            logger.debug(Prefix + "orchestrationStatus - " + orchestrationStatus.toUpperCase())
            execution.setVariable("orchestrationStatus", orchestrationStatus)

            logger.debug(Prefix + "isAAIqueryNameGood? : " + execution.getVariable(Prefix + "isAAIqueryNameGood"))

        } catch (NotFoundException e) {
            logger.debug(" QueryAAINetworkName return code = '404' (Not Found).  Proceed with the Create !!! ")

        } catch (Exception ex) {
            // try error
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow - callRESTQueryAAINetworkName() -  " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAICloudRegion() of DoCreateNetworkInstance ***** " )

        try {
            String networkInputs  = execution.getVariable(Prefix + "networkInputs")
            String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")

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
                execution.setVariable(Prefix + "isCloudRegionGood", true)

            } else {
                String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
                logger.debug(dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

            }

            logger.debug(" is Cloud Region Good: " + execution.getVariable(Prefix + "isCloudRegionGood"))

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            // try error
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkId(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkId() of DoCreateNetworkInstance ***** " )

        try {
            // get variables
            String networkId = ""
            String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
            if (execution.getVariable("sdncVersion") != "1610") {
                String networkResponseInformation = ""
                try {
                    networkResponseInformation = utils.getNodeXml(assignSDNCResponse, "network-response-information", false).replace("tag0:","").replace(":tag0","")
                    networkId = utils.getNodeText(networkResponseInformation, "instance-id")
                } catch (Exception ex) {
                    String dataErrorMessage = " SNDC Response network validation for 'instance-id' (network-id) failed: Empty <network-response-information>"
                    logger.debug(dataErrorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                }

            } else {
                networkId = utils.getNodeText(assignSDNCResponse, "network-id")
            }
            if (networkId == null || networkId == "null") {
                String dataErrorMessage = "SNDC Response did not contains 'instance-id' or 'network-id' element, or the value is null."
                logger.debug(dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            } else {
                logger.debug(" SNDC Response network validation for 'instance-id' (network-id)' is good: " + networkId)
            }


            execution.setVariable(Prefix + "networkId", networkId)
            String networkName   = utils.getNodeText(assignSDNCResponse, "network-name")
            execution.setVariable(Prefix + "networkName", networkName)

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ONE)
            L3Network network = client.get(uri, NotFoundException.class).asBean(L3Network.class).get()

            execution.setVariable(Prefix + "queryIdAAIResponse", network)

            String netId   = network.getNetworkId()
            execution.setVariable(Prefix + "networkId", netId)
            String netName   = network.getNetworkName()
            execution.setVariable(Prefix + "networkName", netName)

        } catch (NotFoundException e) {
            String dataErrorMessage = "Response Error from QueryAAINetworkId is 404 (Not Found)."
            logger.debug(" AAI Query Failed. " + dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTReQueryAAINetworkId(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTReQueryAAINetworkId() of DoCreateNetworkInstance ***** " )

        try {
            // get variables
            String networkId   = execution.getVariable(Prefix + "networkId")
            String netId = networkId

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ONE)
            L3Network network = client.get(uri, NotFoundException.class).asBean(L3Network.class).get()

            execution.setVariable(Prefix + "aaiRequeryIdReturnCode", "200")
            execution.setVariable(Prefix + "requeryIdAAIResponse", network)

            String netName = network.getNetworkName()
            String networkOutputs =
                    """<network-outputs>
                   <network-id>${MsoUtils.xmlEscape(netId)}</network-id>
                   <network-name>${MsoUtils.xmlEscape(netName)}</network-name>
                 </network-outputs>"""
            execution.setVariable(Prefix + "networkOutputs", networkOutputs)
            logger.debug(" networkOutputs - " + '\n' + networkOutputs)


        } catch (NotFoundException e) {
            String dataErrorMessage = "Response Error from ReQueryAAINetworkId is 404 (Not Found)."
            logger.debug(" AAI ReQuery Failed. - " + dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkVpnBinding(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkVpnBinding() of DoCreateNetworkInstance ***** " )

        try {

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(execution.getVariable(Prefix + "networkId")))
            Optional<Relationships> relationships = client.get(uri, NotFoundException.class).getRelationships()
            if(relationships.isPresent()){
                List<AAIResourceUri> uris = relationships.get().getRelatedUris(Types.VPN_BINDING)

                logger.debug(Prefix + "vpnCount - " + uris.size())

                if (uris.size() > 0) {
                    String routeTargets = ""
                    for(AAIResourceUri u : uris) {

                        AAIResultWrapper wrapper = client.get(u.depth(Depth.TWO), NotFoundException.class)
                        Optional<VpnBinding> binding = wrapper.asBean(VpnBinding.class)

                        String routeTarget = ""
                        String routeRole = ""
                        if(binding.get().getRouteTargets() != null) {
                            List<RouteTarget> targets = binding.get().getRouteTargets().getRouteTarget()
                            for(RouteTarget target : targets) {
                                routeTarget  = target.getGlobalRouteTarget()
                                routeRole  = target.getRouteTargetRole()
                                routeTargets += "<routeTargets>" + '\n' +
                                        " <routeTarget>" + routeTarget + "</routeTarget>" + '\n' +
                                        " <routeTargetRole>" + routeRole + "</routeTargetRole>" + '\n' +
                                        "</routeTargets>" + '\n'
                            }
                        }

                    } // end loop

                    execution.setVariable(Prefix + "routeCollection", routeTargets)
                    logger.debug(Prefix + "routeCollection - " + '\n' + routeTargets)

                } else {
                    // reset return code to success
                    execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
                    AaiUtil aaiUriUtil = new AaiUtil(this)
                    String schemaVersion = aaiUriUtil.getNamespace()
                    String aaiStubResponse =
                            """	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<vpn-binding xmlns="${schemaVersion}">
						      <global-route-target/>
							</vpn-binding>
						</rest:payload>"""
                    String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
                    execution.setVariable(Prefix + "queryVpnBindingAAIResponse", aaiStubResponseAsXml)
                    execution.setVariable(Prefix + "routeCollection", "<routeTargets/>")
                    logger.debug(" No vpnBinding, using this stub as response - " + '\n' + aaiStubResponseAsXml)

                }
            }

        } catch (NotFoundException e) {
            logger.debug("Response Error from AAINetworkVpnBinding is 404 (Not Found).")
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Response Error from AAINetworkVpnBinding is 404 (Not Found).")
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkPolicy(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkPolicy() of DoCreateNetworkInstance ***** " )

        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(execution.getVariable(Prefix + "networkId")))
            Optional<Relationships> relationships = client.get(uri, NotFoundException.class).getRelationships()
            if(relationships.isPresent()){
                List<AAIResourceUri> uris = relationships.get().getRelatedUris(Types.NETWORK_POLICY)

                execution.setVariable(Prefix + "networkPolicyCount", uris.size())
                logger.debug(Prefix + "networkPolicyCount - " + uris.size())

                if (uris.size() > 0) {

                    String networkPolicies = ""
                    // AII loop call using list vpnBindings
                    for(AAIResourceUri u : uris) {

                        NetworkPolicy p = client.get(u, NotFoundException.class).asBean(NetworkPolicy.class).get()

                        execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")

                        String networkPolicy  = p.getNetworkPolicyFqdn()
                        networkPolicies += "<policyFqdns>" + networkPolicy + "</policyFqdns>" + '\n'

                    } // end loop

                    execution.setVariable(Prefix + "networkCollection", networkPolicies)
                    logger.debug(Prefix + "networkCollection - " + '\n' + networkPolicies)

                } else {
                    // reset return code to success
                    execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")
                    AaiUtil aaiUriUtil = new AaiUtil(this)
                    String schemaVersion = aaiUriUtil.getNamespace()
                    String aaiStubResponse =
                            """	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<network-policy xmlns="${schemaVersion}">
							  <network-policy-fqdn/>
                            </network-policy>
						</rest:payload>"""
                    String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
                    execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", aaiStubResponseAsXml)
                    execution.setVariable(Prefix + "networkCollection", "<policyFqdns/>")
                    logger.debug(" No net policies, using this stub as response - " + '\n' + aaiStubResponseAsXml)

                }
            }
        } catch (NotFoundException e) {
            String dataErrorMessage = "Response Error from QueryAAINetworkPolicy is 404 (Not Found)."
            logger.debug(dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkTableRef(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkTableRef() of DoCreateNetworkInstance ***** " )

        try {

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(execution.getVariable(Prefix + "networkId")))
            Optional<Relationships> relationships = client.get(uri, NotFoundException.class).getRelationships()
            if(relationships.isPresent()){
                List<AAIResourceUri> uris = relationships.get().getRelatedUris(Types.ROUTE_TABLE_REFERENCE)

                execution.setVariable(Prefix + "networkTableRefCount", uris.size())
                logger.debug(Prefix + "networkTableRefCount - " + uris.size())


                if (uris.size() > 0) {

                    // AII loop call using list vpnBindings
                    String networkTableRefs = ""
                    for(AAIResourceUri u : uris) {

                        RouteTableReference rt = client.get(u, NotFoundException.class).asBean(RouteTableReference.class).get()

                        String networkTableRef  = rt.getRouteTableReferenceFqdn()
                        networkTableRefs += "<routeTableFqdns>" + networkTableRef + "</routeTableFqdns>" + '\n'


                    } // end loop

                    execution.setVariable(Prefix + "tableRefCollection", networkTableRefs)
                    logger.debug(Prefix + "tableRefCollection - " + '\n' + networkTableRefs)

                } else {
                    // reset return code to success
                    execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")
                    AaiUtil aaiUriUtil = new AaiUtil(this)
                    String schemaVersion = aaiUriUtil.getNamespace()
                    String aaiStubResponse =
                            """	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<route-table-references xmlns="${schemaVersion}">
							  <route-table-reference-fqdn/>
                            </route-table-references>
						</rest:payload>"""
                    String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
                    execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", aaiStubResponseAsXml)
                    execution.setVariable(Prefix + "tableRefCollection", "<routeTableFqdns/>")
                    logger.debug(" No net table references, using this stub as response - " + '\n' + aaiStubResponseAsXml)

                }
            }

        } catch (NotFoundException e) {
            String dataErrorMessage = "Response Error from QueryAAINetworkTableRef is 404 (Not Found)."
            logger.debug(dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }


    public void callRESTUpdateContrailAAINetwork(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace(" ***** Inside callRESTUpdateContrailAAINetwork() of DoCreateNetworkInstance ***** " )

        try {
            // get variables
            String networkId   = execution.getVariable(Prefix + "networkId")
            L3Network requeryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse")
            String createNetworkResponse   = execution.getVariable(Prefix + "createNetworkResponse")

            L3Network l3Network = new L3Network()
            if (StringUtils.isBlank(requeryIdAAIResponse.getHeatStackId())) {
                if (utils.nodeExists(createNetworkResponse, 'networkStackId')) {
                    l3Network.setHeatStackId(utils.getNodeText(createNetworkResponse, 'networkStackId'))
                }
            }
            if (StringUtils.isBlank(requeryIdAAIResponse.getNeutronNetworkId())) {
                if (utils.nodeExists(createNetworkResponse, 'neutronNetworkId')) {
                    l3Network.setNeutronNetworkId(utils.getNodeText(createNetworkResponse, 'neutronNetworkId'))
                }
            }
            if (StringUtils.isBlank(requeryIdAAIResponse.getContrailNetworkFqdn())) {
                if (utils.nodeExists(createNetworkResponse, 'networkFqdn')) {
                    l3Network.setContrailNetworkFqdn(utils.getNodeText(createNetworkResponse, 'networkFqdn'))
                }
            }

            String status = utils.getNodeText(createNetworkResponse, 'orchestration-status')
            if(status.equals("pending-create") || status.equals("PendingCreate")){
                l3Network.setOrchestrationStatus("Created")
            }else{
                l3Network.setOrchestrationStatus("Active")
            }

            logger.debug("Updating l3-network in AAI" )

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId))
            client.update(uri, l3Network)

            if(requeryIdAAIResponse.getSubnets() != null){
                for(Subnet s:requeryIdAAIResponse.getSubnets().getSubnet()){
                    String subnetOrchStatus = s.getOrchestrationStatus()
                    String subnetId = s.getSubnetId()
                    Subnet subnet = new Subnet()
                    subnet.setNeutronSubnetId(networkUtils.extractNeutSubId(createNetworkResponse, subnetId))
                    if(subnetOrchStatus.equals("pending-create") || subnetOrchStatus.equals("PendingCreate") ){
                        subnet.setOrchestrationStatus("Created")
                    }else{
                        subnet.setOrchestrationStatus("Active")
                    }

                    logger.debug("Updating subnet in AAI" )
                    AAIResourceUri subUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId).subnet(subnetId))
                    client.update(subUri, subnet)

                }
            }

            String rollbackEnabled = execution.getVariable(Prefix + "rollbackEnabled")
            if (rollbackEnabled == "true") {
                execution.setVariable(Prefix + "isPONR", false)
            } else {
                execution.setVariable(Prefix + "isPONR", true)
            }
            logger.debug(Prefix + "isPONR" + ": " + execution.getVariable(Prefix + "isPONR"))

        } catch (BpmnError e) {
            throw e;
        } catch (NotFoundException e) {
            String dataErrorMessage = " Response Error from UpdateContrailAAINetwork is 404 (Not Found)."
            logger.debug(dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

    public void prepareCreateNetworkRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareCreateNetworkRequest() of DoCreateNetworkInstance")

        try {

            // get variables
            String requestId = execution.getVariable("msoRequestId")
            if (requestId == null) {
                requestId = execution.getVariable("mso-request-id")
            }
            String messageId = execution.getVariable(Prefix + "messageId")
            String source    = execution.getVariable(Prefix + "source")

            String requestInput = execution.getVariable(Prefix + "networkRequest")
            L3Network queryIdResponse = execution.getVariable(Prefix + "queryIdAAIResponse")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionPo")
            String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")

            // Prepare Network request
            String routeCollection = execution.getVariable(Prefix + "routeCollection")
            String policyCollection = execution.getVariable(Prefix + "networkCollection")
            String tableCollection = execution.getVariable(Prefix + "tableRefCollection")
            String createNetworkRequest = networkUtils.CreateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyCollection, tableCollection, cloudRegionId, backoutOnFailure, source )
            // Format Response
            String buildDeleteNetworkRequestAsString = utils.formatXml(createNetworkRequest)
            buildDeleteNetworkRequestAsString = buildDeleteNetworkRequestAsString.replace(":w1aac13n0", "").replace("w1aac13n0:", "")

            execution.setVariable(Prefix + "createNetworkRequest", buildDeleteNetworkRequestAsString)
            logger.debug(Prefix + "createNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareCreateNetworkRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareSDNCRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareSDNCRequest() of DoCreateNetworkInstance")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

            String networkId = execution.getVariable(Prefix + "networkId")
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // get/set 'msoRequestId' and 'mso-request-id'
            String requestId = execution.getVariable("msoRequestId")
            if (requestId != null) {
                execution.setVariable("mso-request-id", requestId)
            } else {
                requestId = execution.getVariable("mso-request-id")
            }
            execution.setVariable(Prefix + "requestId", requestId)

            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, createNetworkInput, serviceInstanceId, sdncCallback, "assign", "NetworkActivateRequest", cloudRegionId, networkId, null, null)

            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            execution.setVariable(Prefix + "assignSDNCRequest", sndcTopologyCreateRequesAsString)
            logger.debug(Prefix + "assignSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSDNCRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCRequest() of DoCreateNetworkInstance")

        try {
            // get variables

            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

            String networkId = execution.getVariable(Prefix + "networkId")
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "assign", "CreateNetworkInstance", cloudRegionId, networkId, null)

            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            execution.setVariable(Prefix + "assignSDNCRequest", sndcTopologyCreateRequesAsString)
            logger.debug(Prefix + "assignSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCActivateRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCActivateRequest() of DoCreateNetworkInstance")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String networkId = execution.getVariable(Prefix + "networkId")
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "activate", "CreateNetworkInstance", cloudRegionId, networkId, null)

            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            execution.setVariable(Prefix + "activateSDNCRequest", sndcTopologyCreateRequesAsString)
            logger.debug(Prefix + "activateSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCActivateRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }




    // **************************************************
    //     Post or Validate Response Section
    // **************************************************

    public void validateCreateNetworkResponse (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside validateNetworkResponse() of DoCreateNetworkInstance")

        try {
            String networkResponse = execution.getVariable(Prefix + "createNetworkResponse")
            if (networkResponse==null)	{
                networkResponse="" // reset
            }

            execution.setVariable(Prefix + "isNetworkRollbackNeeded", true)
            execution.setVariable(Prefix + "createNetworkResponse", networkResponse)
            logger.debug(" Network Adapter create Success Response - " + "\n" + networkResponse)

            // prepare rollback data
            String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
            rollbackData = rollbackData.replace("rollback>", "networkRollback>")
            String rollbackNetwork =
                    """<rollbackNetworkRequest>
							${rollbackData}
						</rollbackNetworkRequest>"""
            String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
            execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkXml)
            logger.debug(" Network Adapter rollback data - " + "\n" + rollbackNetworkXml)

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. validateCreateNetworkResponse() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }


    }

    public void validateSDNCResponse (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside validateSDNCResponse() of DoCreateNetworkInstance")

        String response = execution.getVariable(Prefix + "assignSDNCResponse")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
        WorkflowException workflowException = execution.getVariable("WorkflowException")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
        // reset variable
        String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "assignSDNCResponse")
        assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
        execution.setVariable(Prefix + "assignSDNCResponse", assignSDNCResponseDecodeXml)

        if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
            execution.setVariable(Prefix + "isSdncRollbackNeeded", true)
            logger.debug("Successfully Validated SDNC Response")

        } else {
            logger.debug("Did NOT Successfully Validated SDNC Response")
            throw new BpmnError("MSOWorkflowException")
        }

    }

    public void validateRpcSDNCActivateResponse (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside validateRpcSDNCActivateResponse() of DoCreateNetworkInstance")

        String response = execution.getVariable(Prefix + "activateSDNCResponse")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
        WorkflowException workflowException = execution.getVariable("WorkflowException")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
        // reset variable
        String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "activateSDNCResponse")
        assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
        execution.setVariable(Prefix + "activateSDNCResponse", assignSDNCResponseDecodeXml)

        if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
            execution.setVariable(Prefix + "isSdncActivateRollbackNeeded", true)
            logger.debug("Successfully Validated Rpc SDNC Activate Response")

        } else {
            logger.debug("Did NOT Successfully Validated Rpc SDNC Activate Response")
            throw new BpmnError("MSOWorkflowException")
        }

    }


    public void prepareSDNCRollbackRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareSDNCRollbackRequest() of DoCreateNetworkInstance")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
            String networkId = execution.getVariable(Prefix + "networkId")
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // 2. prepare rollback topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, createNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null, null)
            String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
            execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
            logger.debug(" Preparing request for SDNC Topology 'rollback-NetworkActivateRequest' rollback . . . - " + "\n" +  sndcTopologyRollbackRequestAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCRollbackRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCRollbackRequest() of DoCreateNetworkInstance")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
            String networkId = execution.getVariable(Prefix + "networkId")
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // 2. prepare rollback topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "unassign", "DeleteNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
            logger.debug(" Preparing request for SDNC Topology 'unassign-DeleteNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCRollbackRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRpcSDNCActivateRollback(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRpcSDNCActivateRollback() of DoCreateNetworkInstance")

        try {

            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String activateSDNCResponse = execution.getVariable(Prefix + "activateSDNCResponse")
            String networkId = execution.getVariable(Prefix + "networkId")
            if (networkId == 'null') {networkId = ""}
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

            // 2. prepare rollback topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "deactivate", "DeleteNetworkInstance", cloudRegionId, networkId, null)
            String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
            execution.setVariable(Prefix + "rollbackActivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
            logger.debug(" Preparing request for RPC SDNC Topology 'deactivate-DeleteNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCActivateRollback() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRollbackData() of DoCreateNetworkInstance")

        try {

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
            String rollbackActivateSDNCRequest = execution.getVariable(Prefix + "rollbackActivateSDNCRequest")
            if (rollbackActivateSDNCRequest != null) {
                if (rollbackActivateSDNCRequest != "") {
                    rollbackData.put("rollbackActivateSDNCRequest", execution.getVariable(Prefix + "rollbackActivateSDNCRequest"))
                }
            }
            execution.setVariable("rollbackData", rollbackData)
            logger.debug("** rollbackData : " + rollbackData)

            execution.setVariable("WorkflowException", execution.getVariable(Prefix + "WorkflowException"))
            logger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void postProcessResponse(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside postProcessResponse() of DoCreateNetworkInstance")

        try {

            //Conditions:
            // 1. Silent Success: execution.getVariable("CRENWKI_orchestrationStatus") == "ACTIVE"
            // 2. Success: execution.getVariable("WorkflowException") == null (NULL)
            // 3. WorkflowException: execution.getVariable("WorkflowException") != null (NOT NULL)

            logger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
            // successful flow
            if (execution.getVariable(Prefix + "isException") == false) {
                // set rollback data
                execution.setVariable("orchestrationStatus", "")
                execution.setVariable("networkId", execution.getVariable(Prefix + "networkId"))
                execution.setVariable("networkName", execution.getVariable(Prefix + "networkName"))
                prepareSuccessRollbackData(execution) // populate rollbackData
                execution.setVariable("WorkflowException", null)
                execution.setVariable(Prefix + "Success", true)
                logger.debug(" ***** postProcessResponse(), GOOD !!!")
            } else {
                // inside sub-flow logic
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
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
            throw new BpmnError("MSOWorkflowException")

        }



    }

    public void prepareSuccessRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareSuccessRollbackData() of DoCreateNetworkInstance")

        try {

            if (execution.getVariable("sdncVersion") != '1610') {
                prepareRpcSDNCRollbackRequest(execution)
                prepareRpcSDNCActivateRollback(execution)
            } else {
                prepareSDNCRollbackRequest(execution)
            }

            Map<String, String> rollbackData = new HashMap<String, String>();
            String rollbackSDNCRequest = execution.getVariable(Prefix + "rollbackSDNCRequest")
            if (rollbackSDNCRequest != null) {
                if (rollbackSDNCRequest != "") {
                    rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
                }
            }
            String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
            if (rollbackNetworkRequest != null) {
                if (rollbackNetworkRequest != "") {
                    rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)
                }
            }
            String rollbackActivateSDNCRequest = execution.getVariable(Prefix + "rollbackActivateSDNCRequest")
            if (rollbackActivateSDNCRequest != null) {
                if (rollbackActivateSDNCRequest != "") {
                    rollbackData.put("rollbackActivateSDNCRequest", rollbackActivateSDNCRequest)
                }
            }
            execution.setVariable("rollbackData", rollbackData)

            logger.debug("** 'rollbackData' for Full Rollback : " + rollbackData)
            execution.setVariable("WorkflowException", null)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void setExceptionFlag(DelegateExecution execution){

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside setExceptionFlag() of DoCreateNetworkInstance")

        try {

            execution.setVariable(Prefix + "isException", true)

            if (execution.getVariable("SavedWorkflowException1") != null) {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
            } else {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
            }
            logger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))

        } catch(Exception ex){
            String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
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
            logger.debug( "Caught a Java Exception in " + Prefix)
            logger.debug("Started processJavaException Method")
            logger.debug("Variables List: " + execution.getVariables())
            execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

        }catch(Exception e){
            logger.debug("Caught Exception during processJavaException Method: " + e)
            execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
        }
        logger.debug( "Completed processJavaException Method in " + Prefix)
    }

}
