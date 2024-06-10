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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.L3Network
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.RouteTableReference
import org.onap.aai.domain.yang.RouteTarget
import org.onap.aai.domain.yang.Subnet
import org.onap.aai.domain.yang.VpnBinding
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
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
 * This groovy class supports the <class>DoUpdateNetworkInstance.bpmn</class> process.
 *
 */
public class DoUpdateNetworkInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoUpdateNetworkInstance.class);

    String Prefix="UPDNETI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    VidUtils vidUtils = new VidUtils(this)
    NetworkUtils networkUtils = new NetworkUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    /**
     * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstance.bpmn</class> process.
     * @param execution
     */
    public InitializeProcessVariables(DelegateExecution execution){
        /* Initialize all the process variables in this block */

        execution.setVariable(Prefix + "messageId", "")
        execution.setVariable("BasicAuthHeaderValuePO", "")
        execution.setVariable("BasicAuthHeaderValueSDNC", "")
        execution.setVariable(Prefix + "networkRequest", "")
        execution.setVariable(Prefix + "networkInputs", "")
        execution.setVariable(Prefix + "networkOutputs", "")
        execution.setVariable(Prefix + "requestId", "")
        execution.setVariable(Prefix + "source", "")
        execution.setVariable(Prefix + "networkId", "")

        execution.setVariable(Prefix + "isPONR", false)    // Point-of-no-return, means, rollback is not needed

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

        execution.setVariable(Prefix + "updateNetworkRequest", "")
        execution.setVariable(Prefix + "updateNetworkResponse", "")
        execution.setVariable(Prefix + "rollbackNetworkRequest", "")
        execution.setVariable(Prefix + "networkReturnCode", "")
        execution.setVariable(Prefix + "isNetworkRollbackNeeded", false)

        execution.setVariable(Prefix + "changeAssignSDNCRequest", "")
        execution.setVariable(Prefix + "changeAssignSDNCResponse", "")
        execution.setVariable(Prefix + "rollbackSDNCRequest", "")
        execution.setVariable(Prefix + "sdncReturnCode", "")
        execution.setVariable(Prefix + "isSdncRollbackNeeded", false)
        execution.setVariable(Prefix + "sdncResponseSuccess", false)

        execution.setVariable(Prefix + "isVnfBindingPresent", false)
        execution.setVariable(Prefix + "Success", false)
        execution.setVariable(Prefix + "serviceInstanceId", "")

        execution.setVariable(Prefix + "isException", false)

    }

    // **************************************************
    //     Pre or Prepare Request Section
    // **************************************************
    /**
     * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstance.bpmn</class> process.
     * @param execution
     */
    public void preProcessRequest (DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside preProcessRequest DoUpdateNetworkInstance Request ")

        try {
            // initialize flow variables
            InitializeProcessVariables(execution)

            // GET Incoming request & validate 3 kinds of format.
            execution.setVariable("action", "UPDATE")
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
            logger.debug(networkRequest)
            execution.setVariable(Prefix + "networkRequest", networkRequest)
            logger.debug(" network-request - " + '\n' + networkRequest)

            // validate 'disableRollback'  (aka, 'suppressRollback')
            boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
            execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
            logger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)

            String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
            execution.setVariable(Prefix + "networkInputs", networkInputs)
            logger.debug(Prefix + "networkInputs - " + '\n' + networkInputs)

            // prepare messageId
            String messageId = execution.getVariable(Prefix + "messageId")  // for testing
            if (messageId == null || messageId == "") {
                messageId = UUID.randomUUID()
                logger.debug(" UPDNETI_messageId, random generated: " + messageId)
            } else {
                logger.debug(" UPDNETI_messageId, pre-assigned: " + messageId)
            }
            execution.setVariable(Prefix + "messageId", messageId)

            String source = utils.getNodeText(networkRequest, "source")
            execution.setVariable(Prefix + "source", source)
            logger.debug(Prefix + "source - " + source)

            String networkId = ""
            if (utils.nodeExists(networkRequest, "network-id")) {
                networkId = utils.getNodeText(networkRequest, "network-id")
                if (networkId == 'null' || networkId == "") {
                    sendSyncError(execution)
                    // missing value of networkId
                    String dataErrorMessage = "Variable 'network-id' value/element is missing."
                    logger.debug(" Invalid Request - " + dataErrorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

                }
            }

            String lcpCloudRegion = ""
            if (utils.nodeExists(networkRequest, "aic-cloud-region")) {
                lcpCloudRegion = utils.getNodeText(networkRequest, "aic-cloud-region")
                if ((lcpCloudRegion == 'null') || (lcpCloudRegion == "")) {
                    sendSyncError(execution)
                    String dataErrorMessage = "requestDetails has missing 'aic-cloud-region' value/element."
                    logger.debug(" Invalid Request - " + dataErrorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                }
            }

            String serviceInstanceId = ""
            if (utils.nodeExists(networkRequest, "service-instance-id")) {
                serviceInstanceId = utils.getNodeText(networkRequest, "service-instance-id")
                if ((serviceInstanceId == 'null') || (lcpCloudRegion == "")) {
                    sendSyncError(execution)
                    String dataErrorMessage = "Variable 'serviceInstanceId' value/element is missing."
                    logger.debug(" Invalid Request - " + dataErrorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                }
            }

            // PO Authorization Info / headers Authorization=
            String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)


            try {
                def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey", execution))
                execution.setVariable("BasicAuthHeaderValuePO",encodedString)
                execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

            } catch (IOException ex) {
                String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - "
                String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
                logger.debug(dataErrorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
            }

            // Set variables for Generic Get Sub Flow use
            execution.setVariable(Prefix + "serviceInstanceId", serviceInstanceId)
            logger.debug(Prefix + "serviceInstanceId - " + serviceInstanceId)

            logger.debug(" Url for SDNC adapter: " + UrnPropertiesReader.getVariable("mso.adapters.sdnc.endpoint",execution))

            String sdncVersion = execution.getVariable("sdncVersion")
            logger.debug("sdncVersion? : " + sdncVersion)

            // build 'networkOutputs'
            networkId = utils.getNodeText(networkRequest, "network-id")
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

        } catch (Exception ex){
            sendSyncError(execution)
            // caught exception
            String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
    }

    /**
     * Gets the service instance uri from aai
     *
     */
    public void getServiceInstance(DelegateExecution execution) {
        logger.trace("getServiceInstance ")
        try {
            String serviceInstanceId = execution.getVariable('serviceInstanceId')

            AAIResourcesClient resourceClient = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))

            if(!resourceClient.exists(uri)){
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Service Instance not found in aai")
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

    public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAICloudRegion of DoUpdateNetworkInstance ***** " )

        try {
            String networkInputs  = execution.getVariable(Prefix + "networkInputs")
            String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")

            // Prepare AA&I url
            String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
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
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkId(DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkId of DoUpdateNetworkInstance ***** " )

        try {
            // get variables
            String networkRequest = execution.getVariable(Prefix + "networkRequest")
            String networkId   = utils.getNodeText(networkRequest, "network-id")
            execution.setVariable(Prefix + "networkId", networkId)

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ONE)
            AAIResultWrapper network = client.get(uri, NotFoundException.class)

            execution.setVariable(Prefix + "aaiIdReturnCode", "200")

            execution.setVariable(Prefix + "queryIdAAIResponse", network)


        } catch (NotFoundException e) {
            String dataErrorMessage = "Response Error from QueryAAINetworkId is 404 (Not Found)."
            logger.debug(" AAI Query Failed. " + dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTReQueryAAINetworkId(DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTReQueryAAINetworkId of DoUpdateNetworkInstance ***** " )

        try {
            // get variables
            String networkRequest = execution.getVariable(Prefix + "networkRequest")
            String networkId   = utils.getNodeText(networkRequest, "network-id")

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ONE)
            AAIResultWrapper network = client.get(uri, NotFoundException.class)

            execution.setVariable(Prefix + "aaiRequeryIdReturnCode", "200")
            execution.setVariable(Prefix + "requeryIdAAIResponse", network)

            L3Network net = network.asBean(L3Network.class).get()
            String netId = net.getNetworkId()
            String netName = net.getNetworkName()
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
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkVpnBinding(DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkVpnBinding of DoUpdateNetworkInstance ***** " )

        try {

            // get variables
            AAIResultWrapper queryIdAAIResponse = execution.getVariable(Prefix + "requeryIdAAIResponse")
            if(queryIdAAIResponse.getRelationships().isPresent()){
                List<AAIResourceUri> uris = queryIdAAIResponse.getRelationships().get().getRelatedUris(Types.VPN_BINDING)

                logger.debug(Prefix + "vpnCount - " + uris.size())

                if (uris.size() > 0) {
                    String routeTargets = ""
                    for(AAIResourceUri u : uris) {

                        AAIResourcesClient client = new AAIResourcesClient()
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
                    logger.debug(" UPDNETI_routeCollection - " + '\n' + routeTargets)

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
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkPolicy(DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkPolicy of DoUpdateNetworkInstance ***** " )

        try {
            // get variables
            AAIResultWrapper queryIdAAIResponse = execution.getVariable(Prefix + "requeryIdAAIResponse")
            if(queryIdAAIResponse.getRelationships().isPresent()){
                List<AAIResourceUri> uris = queryIdAAIResponse.getRelationships().get().getRelatedUris(Types.NETWORK_POLICY)

                execution.setVariable(Prefix + "networkPolicyCount", uris.size())
                logger.debug(Prefix + "networkPolicyCount - " + uris.size())

                if (uris.size() > 0) {

                    String networkPolicies = ""
                    // AII loop call using list vpnBindings
                    for(AAIResourceUri u : uris) {

                        AAIResourcesClient client = new AAIResourcesClient()
                        NetworkPolicy p = client.get(u, NotFoundException.class).asBean(NetworkPolicy.class).get()

                        execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")

                        String networkPolicy  = p.getNetworkPolicyFqdn()
                        networkPolicies += "<policyFqdns>" + networkPolicy + "</policyFqdns>" + '\n'

                    } // end loop

                    execution.setVariable(Prefix + "networkCollection", networkPolicies)
                    logger.debug(" UPDNETI_networkCollection - " + '\n' + networkPolicies)

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
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTQueryAAINetworkTableRef(DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.debug(" ***** Inside callRESTQueryAAINetworkTableRef of DoUpdateNetworkInstance ***** " )

        try {
            AAIResultWrapper queryIdAAIResponse = execution.getVariable(Prefix + "requeryIdAAIResponse")
            if(queryIdAAIResponse.getRelationships().isPresent()){
                List<AAIResourceUri> uris = queryIdAAIResponse.getRelationships().get().getRelatedUris(Types.ROUTE_TABLE_REFERENCE)

                execution.setVariable(Prefix + "networkTableRefCount", uris.size())
                logger.debug(Prefix + "networkTableRefCount - " + uris.size())

                logger.debug(" UPDNETI_networkTableRefCount - " + uris.size())
                if (uris.size() > 0) {

                    execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")

                    // AII loop call using list vpnBindings
                    String networkTableRefs = ""
                    for(AAIResourceUri u : uris) {

                        AAIResourcesClient client = new AAIResourcesClient()
                        RouteTableReference rt = client.get(u, NotFoundException.class).asBean(RouteTableReference.class).get()

                        String networkTableRef  = rt.getRouteTableReferenceFqdn()
                        networkTableRefs += "<routeTableFqdns>" + networkTableRef + "</routeTableFqdns>" + '\n'

                    } // end loop

                    execution.setVariable(Prefix + "tableRefCollection", networkTableRefs)
                    logger.debug(" UPDNETI_tableRefCollection - " + '\n' + networkTableRefs)

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
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void callRESTUpdateContrailAAINetwork(DelegateExecution execution) {
        execution.setVariable("prefix", Prefix)
        logger.debug(" ***** Inside callRESTUpdateContrailAAINetwork of DoUpdateNetworkInstance ***** " )
        try {
            // get variables
            String networkRequest = execution.getVariable(Prefix + "networkRequest")
            String networkId   = utils.getNodeText(networkRequest, "network-id")
            AAIResultWrapper requeryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse")
            String updateNetworkResponse   = execution.getVariable(Prefix + "updateNetworkResponse")

            L3Network oldL3Network = requeryIdAAIResponse.asBean(L3Network.class).get()
            L3Network l3Network = new L3Network()
            if (oldL3Network.getHeatStackId() != null) {
            } else {
                if (utils.nodeExists(updateNetworkResponse, 'networkStackId')) {
                    l3Network.setHeatStackId(utils.getNodeText(updateNetworkResponse, 'networkStackId'))
                }
            }
            if (oldL3Network.getNeutronNetworkId() != null) {
            } else {
                if (utils.nodeExists(updateNetworkResponse, 'neutronNetworkId')) {
                    l3Network.setNeutronNetworkId(utils.getNodeText(updateNetworkResponse, 'neutronNetworkId'))
                }
            }
            if (oldL3Network.getContrailNetworkFqdn() != null) {
            } else {
                if (utils.nodeExists(updateNetworkResponse, 'networkFqdn')) {
                    l3Network.setContrailNetworkFqdn(utils.getNodeText(updateNetworkResponse, 'networkFqdn'))
                }
            }

            String status = utils.getNodeText(updateNetworkResponse, 'orchestration-status')
            if(status.equals("pending-create") || status.equals("PendingCreate")){
                l3Network.setOrchestrationStatus("Created")
            }else{
                l3Network.setOrchestrationStatus("Active")
            }

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId))
            client.update(uri, l3Network)

            List<Subnet> subnets = oldL3Network.getSubnets().getSubnet()
            for(Subnet s:subnets){
                String subnetOrchStatus = s.getOrchestrationStatus()
                String subnetId = s.getSubnetId()

                Subnet subnet = new Subnet()
                String neutronSubnetId = networkUtils.extractNeutSubId(updateNetworkResponse, subnetId)
                subnet.setNeutronSubnetId(neutronSubnetId)
                if(subnetOrchStatus.equals("pending-create") || subnetOrchStatus.equals("PendingCreate") ){
                    subnet.setOrchestrationStatus("Created")
                }else{
                    subnet.setOrchestrationStatus("Active")
                }

                AAIResourceUri subUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId).subnet(subnetId))
                client.update(subUri, subnet)
            }

            execution.setVariable(Prefix + "isPONR", true)

        } catch (BpmnError e) {
            throw e;
        } catch (NotFoundException e) {
            String dataErrorMessage = " Response Error from UpdateContrailAAINetwork is 404 (Not Found)."
            logger.debug(dataErrorMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareUpdateNetworkRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareUpdateNetworkRequest of DoUpdateNetworkInstance ")

        try {

            // get variables
            String requestId = execution.getVariable(Prefix + "requestId")
            String messageId = execution.getVariable(Prefix + "messageId")
            String source    = execution.getVariable(Prefix + "source")

            String requestInput = execution.getVariable(Prefix + "networkRequest")
            L3Network queryIdResponse = execution.getVariable(Prefix + "requeryIdAAIResponse")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionPo")
            String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")

            // Prepare Network request
            String routeCollection = execution.getVariable(Prefix + "routeCollection")
            String policyCollection = execution.getVariable(Prefix + "networkCollection")
            String tableCollection = execution.getVariable(Prefix + "tableRefCollection")
            String updateNetworkRequest = networkUtils.UpdateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyCollection, tableCollection, cloudRegionId, backoutOnFailure, source )
            // Format Response
            String buildUpdateNetworkRequestAsString = utils.formatXml(updateNetworkRequest)
            buildUpdateNetworkRequestAsString = buildUpdateNetworkRequestAsString.replace(":w1aac13n0", "").replace("w1aac13n0:", "")
            logger.debug(buildUpdateNetworkRequestAsString)

            execution.setVariable(Prefix + "updateNetworkRequest", buildUpdateNetworkRequestAsString)
            logger.debug(" UPDNETI_updateNetworkRequest - " + "\n" +  buildUpdateNetworkRequestAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareUpdateNetworkRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareSDNCRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareSDNCRequest of DoUpdateNetworkInstance ")

        try {
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

            String networkId = ""
            if (utils.nodeExists(updateNetworkInput, "network-id")) {
                networkId = utils.getNodeText(updateNetworkInput, "network-id")
            }
            if (networkId == null) {networkId = ""}

            String serviceInstanceId = utils.getNodeText(updateNetworkInput, "service-instance-id")

            AAIResultWrapper wrapper = execution.getVariable(Prefix + "queryIdAAIResponse")
            L3Network queryAAIResponse = wrapper.asBean(L3Network.class).get()

            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "changeassign", "NetworkActivateRequest", cloudRegionId, networkId, queryAAIResponse, null)

            String sndcTopologyUpdateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            logger.debug(sndcTopologyUpdateRequesAsString)
            execution.setVariable(Prefix + "changeAssignSDNCRequest", sndcTopologyUpdateRequesAsString)
            logger.debug(" UPDNETI_changeAssignSDNCRequest - " + "\n" +  sndcTopologyUpdateRequesAsString)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void validateSDNCResponse (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside validateSDNCResponse of DoUpdateNetworkInstance ")

        String response = execution.getVariable(Prefix + "changeAssignSDNCResponse")
        WorkflowException workflowException = null
        try {
            workflowException = execution.getVariable(Prefix + "WorkflowException")
            //execution.setVariable("WorkflowException", workflowException)
        } catch (Exception ex) {
            logger.debug(" Sdnc 'WorkflowException' object is empty or null. ")
        }

        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
        // reset variable
        String changeAssignSDNCResponseDecodeXml = execution.getVariable(Prefix + "changeAssignSDNCResponse")
        changeAssignSDNCResponseDecodeXml = changeAssignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
        execution.setVariable(Prefix + "changeAssignSDNCResponse", changeAssignSDNCResponseDecodeXml)

        if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
            execution.setVariable(Prefix + "isSdncRollbackNeeded", true)
            logger.debug("Successfully Validated SDNC Response")

        } else {
            logger.debug("Did NOT Successfully Validated SDNC Response")
            throw new BpmnError("MSOWorkflowException")
        }

    }


    public void postProcessResponse (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside postProcessResponse of DoUpdateNetworkInstance ")

        try {
            logger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
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
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }


    }

    public void prepareSDNCRollbackRequest (DelegateExecution execution) {

        execution.setVariable("prefix", Prefix)

        logger.trace("Inside prepareSDNCRollbackRequest of DoUpdateNetworkInstance ")

        try {
            // for some reason the WorkflowException object is null after the sdnc rollback call task, need to save WorkflowException.
            execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
            // get variables
            String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
            String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
            String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
            String changeAssignSDNCResponse = execution.getVariable(Prefix + "changeAssignSDNCResponse")
            String networkId = utils.getNodeText(changeAssignSDNCResponse, "network-id")

            String serviceInstanceId = utils.getNodeText(updateNetworkInput, "service-instance-id")

            // 2. prepare rollback topology via SDNC Adapter SUBFLOW call
            String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null, null)
            String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
            execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
            logger.debug(" Preparing request for SDNC Topology assign's rollback/compensation . . . - " + "\n" +  sndcTopologyRollbackRequestAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareRollbackData() of DoUpdateNetworkInstance ")

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
            execution.setVariable("rollbackData", rollbackData)
            logger.debug("** rollbackData : " + rollbackData)

            execution.setVariable("WorkflowException", execution.getVariable(Prefix + "WorkflowException"))
            logger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void prepareSuccessRollbackData(DelegateExecution execution) {

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside prepareSuccessRollbackData() of DoUpdateNetworkInstance ")

        try {

            if (execution.getVariable("sdncVersion") != '1610') {
                // skip: 1702 for 'changeassign' or equivalent not yet defined in SNDC, so no rollback.
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
            execution.setVariable("rollbackData", rollbackData)

            logger.debug("** 'rollbackData' for Full Rollback : " + rollbackData)
            execution.setVariable("WorkflowException", null)


        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void setExceptionFlag(DelegateExecution execution){

        execution.setVariable("prefix",Prefix)

        logger.trace("Inside setExceptionFlag() of DoUpdateNetworkInstance ")

        try {

            execution.setVariable(Prefix + "isException", true)

            if (execution.getVariable("SavedWorkflowException1") != null) {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
            } else {
                execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
            }
            logger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))

        } catch(Exception ex){
            String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
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
            execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - "  + Prefix)  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

        }catch(Exception e){
            logger.debug("Caught Exception during processJavaException Method: " + e)
            execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
            exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
        }
        logger.debug("Completed processJavaException Method")
    }

}
