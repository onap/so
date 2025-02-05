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

import org.onap.so.logger.LoggingAnchor
import org.onap.aai.domain.yang.NetworkPolicies
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.so.logging.filter.base.ErrorCode

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource

/* Subflow for Delete VF Module. When no DoDeleteVfModuleRequest is specified on input,
 * functions as a building block subflow
 * Inputs for building block interface:
 * @param - requestId
 * @param - isDebugLogEnabled
 * @param - vnfId
 * @param - vfModuleId
 * @param - serviceInstanceId
 * @param - vfModuleName O
 * @param - vfModuleModelInfo
 * @param - cloudConfiguration*
 * @param - sdncVersion ("1610")
 * @param - retainResources
 * @param - aLaCarte
 *
 * Outputs:
 * @param - WorkflowException
 *
 */
public class DoDeleteVfModule extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteVfModule.class);

    def Prefix="DoDVfMod_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    public void initProcessVariables(DelegateExecution execution) {
        execution.setVariable("prefix",Prefix)
        execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", null)
        execution.setVariable("DoDVfMod_oamManagementV4Address", null)
        execution.setVariable("DoDVfMod_oamManagementV6Address", null)
    }

    // parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
    // and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
    public void preProcessRequest(DelegateExecution execution) {

        initProcessVariables(execution)

        try {
            def xml = execution.getVariable("DoDeleteVfModuleRequest")
            String vnfId = ""
            String vfModuleId = ""

            if (xml == null || xml.isEmpty()) {
                // Building Block-type request

                // Set mso-request-id to request-id for VNF Adapter interface
                String requestId = execution.getVariable("requestId")
                execution.setVariable("mso-request-id", requestId)

                String cloudConfiguration = execution.getVariable("cloudConfiguration")
                String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")
                String tenantId = jsonUtil.getJsonValue(cloudConfiguration, "tenantId")
                execution.setVariable("tenantId", tenantId)
                String cloudSiteId = jsonUtil.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
                execution.setVariable("cloudSiteId", cloudSiteId)
                String cloudOwner = jsonUtil.getJsonValue(cloudConfiguration, "cloudOwner")
                execution.setVariable("cloudOwner", cloudOwner)
                // Source is HARDCODED
                String source = "VID"
                execution.setVariable("source", source)
                // SrvInstId is hardcoded to empty
                execution.setVariable("srvInstId", "")
                // ServiceId is hardcoded to empty
                execution.setVariable("serviceId", "")
                String serviceInstanceId = execution.getVariable("serviceInstanceId")
                vnfId = execution.getVariable("vnfId")
                vfModuleId = execution.getVariable("vfModuleId")
                if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
                    execution.setVariable(Prefix + "serviceInstanceIdToSdnc", vfModuleId)
                }
                else {
                    execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceId)
                }
                //vfModuleModelName
                def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
                execution.setVariable("vfModuleModelName", vfModuleModelName)
                // retainResources
                def retainResources = execution.getVariable("retainResources")
                if (retainResources == null) {
                    retainResources  = false
                }
                execution.setVariable("retainResources", retainResources)
            }
            else {

                logger.debug("DoDeleteVfModule Request: " + xml)

                logger.debug("input request xml: " + xml)

                vnfId = utils.getNodeText(xml,"vnf-id")
                execution.setVariable("vnfId", vnfId)
                vfModuleId = utils.getNodeText(xml,"vf-module-id")
                execution.setVariable("vfModuleId", vfModuleId)
                def srvInstId = execution.getVariable("mso-service-instance-id")
                execution.setVariable("srvInstId", srvInstId)
                String requestId = ""
                try {
                    requestId = execution.getVariable("mso-request-id")
                } catch (Exception ex) {
                    requestId = utils.getNodeText(xml, "request-id")
                }
                execution.setVariable("requestId", requestId)
                String source = utils.getNodeText(xml, "source")
                execution.setVariable("source", source)
                String serviceId = utils.getNodeText(xml, "service-id")
                execution.setVariable("serviceId", serviceId)
                String tenantId = utils.getNodeText(xml, "tenant-id")
                execution.setVariable("tenantId", tenantId)

                String serviceInstanceIdToSdnc = ""
                if (xml.contains("service-instance-id")) {
                    serviceInstanceIdToSdnc = utils.getNodeText(xml, "service-instance-id")
                } else {
                    serviceInstanceIdToSdnc = vfModuleId
                }
                execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceIdToSdnc)
                String vfModuleName = utils.getNodeText(xml, "vf-module-name")
                execution.setVariable("vfModuleName", vfModuleName)
                String vfModuleModelName = utils.getNodeText(xml, "vf-module-model-name")
                execution.setVariable("vfModuleModelName", vfModuleModelName)
                String cloudSiteId = utils.getNodeText(xml, "aic-cloud-region")
                execution.setVariable("cloudSiteId", cloudSiteId)
                String cloudOwner = utils.getNodeText(xml, "cloud-owner")
                execution.setVariable("cloudOwner", cloudOwner)
            }

            // formulate the request for PrepareUpdateAAIVfModule
            String request = """<PrepareUpdateAAIVfModuleRequest>
									<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
									<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
            logger.debug("PrepareUpdateAAIVfModuleRequest :" + request)
            logger.debug("UpdateAAIVfModule Request: " + request)
            execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
            execution.setVariable("vfModuleFromAAI", null)
        }catch(BpmnError b){
            throw b
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in PreProcess method!")
        }
    }

    // build a SDNC vnf-topology-operation request for the specified action
    // (note: the action passed is expected to be 'changedelete' or 'delete')
    public void prepSDNCAdapterRequest(DelegateExecution execution, String action) {


        String uuid = execution.getVariable('testReqId') // for junits
        if(uuid==null){
            uuid = execution.getVariable("requestId") + "-" +  	System.currentTimeMillis()
        }

        def srvInstId = execution.getVariable("srvInstId")
        def callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
        String requestId = execution.getVariable("requestId")
        String source = execution.getVariable("source")
        String serviceId = execution.getVariable("serviceId")
        String vnfId = execution.getVariable("vnfId")
        String tenantId = execution.getVariable("tenantId")
        String vfModuleId = execution.getVariable("vfModuleId")
        String serviceInstanceIdToSdnc = execution.getVariable(Prefix + "serviceInstanceIdToSdnc")
        String vfModuleName = execution.getVariable("vfModuleName")
        // Get vfModuleName from AAI response if it was not specified on the request
        if (vfModuleName == null || vfModuleName.isEmpty()) {
            if (execution.getVariable("vfModuleFromAAI") != null) {
                org.onap.aai.domain.yang.VfModule vfModuleFromAAI = execution.getVariable("vfModuleFromAAI")
                vfModuleName = vfModuleFromAAI.getVfModuleName()
            }
        }
        String vfModuleModelName = execution.getVariable("vfModuleModelName")
        String cloudSiteId = execution.getVariable("cloudSiteId")
        boolean retainResources = execution.getVariable("retainResources")
        String requestSubActionString = ""
        if (retainResources) {
            requestSubActionString = "<request-sub-action>RetainResource</request-sub-action>"
        }
        String request = """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
						      <sdncadapter:RequestHeader>
						         <sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
						         <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(vfModuleId)}</sdncadapter:SvcInstanceId>
						         <sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						         <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						         <sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
						      </sdncadapter:RequestHeader>
						      <sdncadapterworkflow:SDNCRequestData>
						         <request-information>
						            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						            <request-action>DisconnectVNFRequest</request-action>
									${requestSubActionString}
						            <source>${MsoUtils.xmlEscape(source)}</source>
						            <notification-url/>
						            <order-number/>
						            <order-version/>
						         </request-information>
						         <service-information>
						            <service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
									<service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
						            <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceIdToSdnc)}</service-instance-id>
						            <subscriber-name>notsurewecare</subscriber-name>
						         </service-information>
						         <vnf-request-information>
						         	<vnf-id>${MsoUtils.xmlEscape(vfModuleId)}</vnf-id>
									<vnf-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vnf-type>
                                    <vnf-name>${MsoUtils.xmlEscape(vfModuleName)}</vnf-name>
									<generic-vnf-id>${MsoUtils.xmlEscape(vnfId)}</generic-vnf-id>
                                    <generic-vnf-name></generic-vnf-name>
									<generic-vnf-type></generic-vnf-type>
									<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
									<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

        logger.debug("sdncAdapterWorkflowRequest: " + request)
        logger.debug("DoDeleteVfModule - SDNCAdapterWorkflowRequest: " + request)
        execution.setVariable("sdncAdapterWorkflowRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing VnfAdapterDeleteV1 request
    public void prepVNFAdapterRequest(DelegateExecution execution) {

        def requestId = UUID.randomUUID().toString()
        def origRequestId = execution.getVariable('requestId')
        def srvInstId = execution.getVariable("serviceInstanceId")
        def aicCloudRegion = execution.getVariable("cloudSiteId")
        def cloudOwner = execution.getVariable("cloudOwner")
        def vnfId = execution.getVariable("vnfId")
        def vfModuleId = execution.getVariable("vfModuleId")
        def vfModuleStackId = execution.getVariable('DoDVfMod_heatStackId')
        def tenantId = execution.getVariable("tenantId")
        def messageId = execution.getVariable('requestId') + '-' +
                System.currentTimeMillis()
        def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
        def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
        if ('true'.equals(useQualifiedHostName)) {
            notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
        }

        String request = """
			<deleteVfModuleRequest>
			    <cloudSiteId>${MsoUtils.xmlEscape(aicCloudRegion)}</cloudSiteId>
			    <cloudOwner>${MsoUtils.xmlEscape(cloudOwner)}</cloudOwner>
			    <tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
			    <vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
			    <vfModuleId>${MsoUtils.xmlEscape(vfModuleId)}</vfModuleId>
			    <vfModuleStackId>${MsoUtils.xmlEscape(vfModuleStackId)}</vfModuleStackId>
			    <skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(origRequestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(srvInstId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</deleteVfModuleRequest>
			""" as String

        logger.debug("deleteVfModuleRequest: " + request)
        execution.setVariable("vnfAdapterTaskRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing UpdateAAIVfModuleRequest request
    public void prepUpdateAAIVfModule(DelegateExecution execution) {

        def vnfId = execution.getVariable("vnfId")
        def vfModuleId = execution.getVariable("vfModuleId")
        // formulate the request for UpdateAAIVfModule
        String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
        logger.debug("UpdateAAIVfModuleRequest: " + request)
        execution.setVariable("UpdateAAIVfModuleRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing DeleteAAIVfModuleRequest request
    public void prepDeleteAAIVfModule(DelegateExecution execution) {


        def vnfId = execution.getVariable("vnfId")
        def vfModuleId = execution.getVariable("vfModuleId")
        // formulate the request for UpdateAAIVfModule
        String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
        logger.debug("DeleteAAIVfModuleRequest :" + request)
        logger.debug("DeleteAAIVfModuleRequest: " + request)
        execution.setVariable("DeleteAAIVfModuleRequest", request)
    }

    // generates a WorkflowException if
    //		-
    public void handleDoDeleteVfModuleFailure(DelegateExecution execution) {
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                "AAI error occurred deleting the Generic Vnf: " + execution.getVariable("DoDVfMod_deleteGenericVnfResponse"),
                "BPMN", ErrorCode.UnknownError.getValue(), "Exception");
        String processKey = getProcessKey(execution);
        WorkflowException exception = new WorkflowException(processKey, 5000,
                execution.getVariable("DoDVfMod_deleteGenericVnfResponse"))
        execution.setVariable("WorkflowException", exception)
    }

    public void sdncValidateResponse(DelegateExecution execution, String response){

        execution.setVariable("prefix",Prefix)

        WorkflowException workflowException = execution.getVariable("WorkflowException")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

        if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
            logger.debug("Successfully Validated SDNC Response")
        }else{
            throw new BpmnError("MSOWorkflowException")
        }
    }

    public void postProcessVNFAdapterRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
                'execution=' + execution.getId() +
                ')'

        logger.trace('Entered ' + method)
        execution.setVariable("prefix",Prefix)
        try{
            logger.trace("STARTED postProcessVNFAdapterRequest Process")

            String vnfResponse = execution.getVariable("DoDVfMod_doDeleteVfModuleResponse")
            logger.debug("VNF Adapter Response is: " + vnfResponse)
            logger.debug("deleteVnfAResponse is: \n"  + vnfResponse)

            if(vnfResponse != null){

                if(vnfResponse.contains("deleteVfModuleResponse")){
                    logger.debug("Received a Good Response from VNF Adapter for DELETE_VF_MODULE Call.")
                    execution.setVariable("DoDVfMod_vnfVfModuleDeleteCompleted", true)

                    // Parse vnfOutputs for contrail network polcy FQDNs
                    if (vnfResponse.contains("vfModuleOutputs")) {
                        def vfModuleOutputsXml = utils.getNodeXml(vnfResponse, "vfModuleOutputs")
                        InputSource source = new InputSource(new StringReader(vfModuleOutputsXml));
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        docFactory.setNamespaceAware(true)
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
                        Document outputsXml = docBuilder.parse(source)

                        NodeList entries = outputsXml.getElementsByTagNameNS("*", "entry")
                        List contrailNetworkPolicyFqdnList = []
                        for (int i = 0; i< entries.getLength(); i++) {
                            Node node = entries.item(i)
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node
                                String key = element.getElementsByTagNameNS("*", "key").item(0).getTextContent()
                                if (key.endsWith("contrail_network_policy_fqdn")) {
                                    String contrailNetworkPolicyFqdn = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
                                    logger.debug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn)
                                    contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
                                }
                                else if (key.equals("oam_management_v4_address")) {
                                    String oamManagementV4Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
                                    logger.debug("Obtained oamManagementV4Address: " + oamManagementV4Address)
                                    execution.setVariable(Prefix + "oamManagementV4Address", oamManagementV4Address)
                                }
                                else if (key.equals("oam_management_v6_address")) {
                                    String oamManagementV6Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
                                    logger.debug("Obtained oamManagementV6Address: " + oamManagementV6Address)
                                    execution.setVariable(Prefix + "oamManagementV6Address", oamManagementV6Address)
                                }

                            }
                        }
                        if (!contrailNetworkPolicyFqdnList.isEmpty()) {
                            logger.debug("Setting the fqdn list")
                            execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
                        }
                    }
                }else{
                    logger.debug("Received a BAD Response from VNF Adapter for DELETE_VF_MODULE Call.")
                    exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
                }
            }else{
                logger.debug("Response from VNF Adapter is Null for DELETE_VF_MODULE Call.")
                exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
            }

        }catch(BpmnError b){
            throw b
        }catch(Exception e){
            logger.debug("Internal Error Occured in PostProcess Method")
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
        }
        logger.trace("COMPLETED postProcessVnfAdapterResponse Process")
    }

    public void deleteNetworkPoliciesFromAAI(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
                'execution=' + execution.getId() +
                ')'

        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED deleteNetworkPoliciesFromAAI ")

        try {
            // get variables
            List fqdnList = execution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")
            if (fqdnList == null) {
                logger.debug("No network policies to delete")
                return
            }
            int fqdnCount = fqdnList.size()

            execution.setVariable("DoDVfMod_networkPolicyFqdnCount", fqdnCount)
            logger.debug("DoDVfMod_networkPolicyFqdnCount - " + fqdnCount)

            if (fqdnCount > 0) {
                // AII loop call over contrail network policy fqdn list
                for (i in 0..fqdnCount-1) {
                    String fqdn = fqdnList[i]
                    // Query AAI for this network policy FQDN
					AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
                    uri.queryParam("network-policy-fqdn", fqdn)
                    try {
                        Optional<NetworkPolicies> networkPolicies = getAAIClient().get(NetworkPolicies.class, uri)
                        if (networkPolicies.isPresent() && !networkPolicies.get().getNetworkPolicy().isEmpty()) {
                            // This network policy FQDN exists in AAI - need to delete it now
                            NetworkPolicy networkPolicy = networkPolicies.get().getNetworkPolicy().get(0)
                            execution.setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 200)
                            // Retrieve the network policy id for this FQDN
                            def networkPolicyId = networkPolicy.getNetworkPolicyId()
                            logger.debug("Deleting network-policy with network-policy-id " + networkPolicyId)
                            try {
                                AAIResourceUri delUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicyId))
                                getAAIClient().delete(delUri)
                                execution.setVariable("DoDVfMod_aaiDeleteNetworkPolicyReturnCode", 200)
                            } catch (Exception e) {
                                execution.setVariable("DoDVfMod_aaiDeleteNetworkPolicyReturnCode", 500)
                                String delErrorMessage = "Unable to delete network-policy to AAI deleteNetworkPoliciesFromAAI - " + e.getMessage()
                                logger.debug(delErrorMessage)
                                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, delErrorMessage)
                            }
                        } else {
                            execution.setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 404)
                            // This network policy FQDN is not in AAI. No need to delete.
                            logger.debug("The return code is: " + 404)
                            logger.debug("This network policy FQDN is not in AAI: " + fqdn)
                            logger.debug("Network policy FQDN is not in AAI")
                        }
                    }catch(Exception e ) {
                        // aai all errors
                        String dataErrorMessage = "Unexpected Response from deleteNetworkPoliciesFromAAI - " + e.getMessage()
                        logger.debug(dataErrorMessage)
                    }
                } // end loop
            } else {
                logger.debug("No contrail network policies to query/create")
            }
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoDeletVfModule flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }

    }

    /**
     * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
     *
     * @param execution The flow's execution instance.
     */
    public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
                'execution=' + execution.getId() +
                ')'

        logger.trace('Entered ' + method)

        try {
            def vnfId = execution.getVariable('vnfId')
            def oamManagementV4Address = execution.getVariable(Prefix + 'oamManagementV4Address')
            def oamManagementV6Address = execution.getVariable(Prefix + 'oamManagementV6Address')
            def ipv4OamAddressElement = ''
            def managementV6AddressElement = ''

            if (oamManagementV4Address != null) {
                ipv4OamAddressElement = '<ipv4-oam-address>' + 'DELETE' + '</ipv4-oam-address>'
            }

            if (oamManagementV6Address != null) {
                managementV6AddressElement = '<management-v6-address>' + 'DELETE' + '</management-v6-address>'
            }


            String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
            updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
            execution.setVariable(Prefix + 'updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
            logger.debug("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
            logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


            logger.trace('Exited ' + method)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    'Caught exception in ' + method, "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
        }
    }

    /**
     * Using the vnfId and vfModuleId provided in the inputs,
     * query AAI to get the corresponding VF Module info.
     * A 200 response is expected with the VF Module info in the response body,
     * Will determine VF Module's orchestration status if one exists
     *
     * @param execution The flow's execution instance.
     */
    public void queryAAIVfModuleForStatus(DelegateExecution execution) {

        def method = getClass().getSimpleName() + '.queryAAIVfModuleForStatus(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        execution.setVariable(Prefix + 'orchestrationStatus', '')

        try {
            def vnfId = execution.getVariable('vnfId')
            def vfModuleId = execution.getVariable('vfModuleId')

            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))

            try {
                Optional<org.onap.aai.domain.yang.VfModule> vfModule = getAAIClient().get(org.onap.aai.domain.yang.VfModule.class, uri);
                // Retrieve VF Module info and its orchestration status; if not found, do nothing
                if (vfModule.isPresent()) {
                    execution.setVariable(Prefix + 'queryAAIVfModuleForStatusResponseCode', 200)
                    execution.setVariable(Prefix + 'queryAAIVfModuleForStatusResponse', vfModule.get())
                    def orchestrationStatus = vfModule.get().getOrchestrationStatus()
                    execution.setVariable(Prefix + "orchestrationStatus", orchestrationStatus)
                    logger.debug("Received orchestration status from A&AI: " + orchestrationStatus)
                }
            } catch (Exception ex) {
                logger.debug('Exception occurred while executing AAI GET: {}', ex.getMessage(), ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
            }
            logger.trace('Exited ' + method)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    'Caught exception in ' + method, "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModuleForStatus(): ' + e.getMessage())
        }
    }





}
