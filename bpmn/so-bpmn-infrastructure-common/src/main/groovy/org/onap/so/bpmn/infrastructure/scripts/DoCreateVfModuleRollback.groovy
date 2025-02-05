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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.NetworkPolicies
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.ws.rs.NotFoundException


public class DoCreateVfModuleRollback extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DoCreateVfModuleRollback.class);

    def Prefix="DCVFMR_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    public void initProcessVariables(DelegateExecution execution) {
        execution.setVariable("prefix",Prefix)
    }

    // parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
    // and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
    public void preProcessRequest(DelegateExecution execution) {


        initProcessVariables(execution)

        try {

            execution.setVariable("rolledBack", null)
            execution.setVariable("rollbackError", null)

            def rollbackData = execution.getVariable("rollbackData")
            logger.debug("RollbackData:" + rollbackData)

            if (rollbackData != null) {
                String vnfId = rollbackData.get("VFMODULE", "vnfid")
                execution.setVariable("DCVFMR_vnfId", vnfId)
                String vfModuleId = rollbackData.get("VFMODULE", "vfmoduleid")
                execution.setVariable("DCVFMR_vfModuleId", vfModuleId)
                String source = rollbackData.get("VFMODULE", "source")
                execution.setVariable("DCVFMR_source", source)
                String serviceInstanceId = rollbackData.get("VFMODULE", "serviceInstanceId")
                execution.setVariable("DCVFMR_serviceInstanceId", serviceInstanceId)
                String serviceId = rollbackData.get("VFMODULE", "service-id")
                execution.setVariable("DCVFMR_serviceId", serviceId)
                String vnfType = rollbackData.get("VFMODULE", "vnftype")
                execution.setVariable("DCVFMR_vnfType", vnfType)
                String vnfName = rollbackData.get("VFMODULE", "vnfname")
                execution.setVariable("DCVFMR_vnfName", vnfName)
                String tenantId = rollbackData.get("VFMODULE", "tenantid")
                execution.setVariable("DCVFMR_tenantId", tenantId)
                String vfModuleName = rollbackData.get("VFMODULE", "vfmodulename")
                execution.setVariable("DCVFMR_vfModuleName", vfModuleName)
                String vfModuleModelName = rollbackData.get("VFMODULE", "vfmodulemodelname")
                execution.setVariable("DCVFMR_vfModuleModelName", vfModuleModelName)
                String cloudSiteId = rollbackData.get("VFMODULE", "aiccloudregion")
                execution.setVariable("DCVFMR_cloudSiteId", cloudSiteId)
                String cloudOwner = rollbackData.get("VFMODULE", "cloudowner")
                execution.setVariable("DCVFMR_cloudOwner", cloudOwner)
                String heatStackId = rollbackData.get("VFMODULE", "heatstackid")
                execution.setVariable("DCVFMR_heatStackId", heatStackId)
                String requestId = rollbackData.get("VFMODULE", "msorequestid")
                execution.setVariable("DCVFMR_requestId", requestId)
                // Set mso-request-id to request-id for VNF Adapter interface
                execution.setVariable("mso-request-id", requestId)
                List createdNetworkPolicyFqdnList = []
                int i = 0
                while (i < 100) {
                    String fqdn = rollbackData.get("VFMODULE", "contrailNetworkPolicyFqdn" + i)
                    if (fqdn == null) {
                        break
                    }
                    createdNetworkPolicyFqdnList.add(fqdn)
                    logger.debug("got fqdn # " + i + ": " + fqdn)
                    i = i + 1

                }

                execution.setVariable("DCVFMR_createdNetworkPolicyFqdnList", createdNetworkPolicyFqdnList)
                String oamManagementV4Address = rollbackData.get("VFMODULE", "oamManagementV4Address")
                execution.setVariable("DCVFMR_oamManagementV4Address", oamManagementV4Address)
                String oamManagementV6Address = rollbackData.get("VFMODULE", "oamManagementV6Address")
                execution.setVariable("DCVFMR_oamManagementV6Address", oamManagementV6Address)
                //String serviceInstanceId = rollbackData.get("VFMODULE", "msoserviceinstanceid")
                //execution.setVariable("DCVFMR_serviceInstanceId", serviceInstanceId)
                execution.setVariable("DCVFMR_rollbackPrepareUpdateVfModule", rollbackData.get("VFMODULE", "rollbackPrepareUpdateVfModule"))
                execution.setVariable("DCVFMR_rollbackUpdateAAIVfModule", rollbackData.get("VFMODULE", "rollbackUpdateAAIVfModule"))
                execution.setVariable("DCVFMR_rollbackVnfAdapterCreate", rollbackData.get("VFMODULE", "rollbackVnfAdapterCreate"))
                execution.setVariable("DCVFMR_rollbackSDNCRequestAssign", rollbackData.get("VFMODULE", "rollbackSDNCRequestAssign"))
                execution.setVariable("DCVFMR_rollbackSDNCRequestActivate", rollbackData.get("VFMODULE", "rollbackSDNCRequestActivate"))
                execution.setVariable("DCVFMR_rollbackCreateAAIVfModule", rollbackData.get("VFMODULE", "rollbackCreateAAIVfModule"))
                execution.setVariable("DCVFMR_rollbackCreateNetworkPoliciesAAI", rollbackData.get("VFMODULE", "rollbackCreateNetworkPoliciesAAI"))
                execution.setVariable("DCVFMR_rollbackUpdateVnfAAI", rollbackData.get("VFMODULE", "rollbackUpdateVnfAAI"))

                // formulate the request for PrepareUpdateAAIVfModule
                String request = """<PrepareUpdateAAIVfModuleRequest>
									<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
									<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
                logger.debug("PrepareUpdateAAIVfModuleRequest :" + request)
                execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
            } else {
                execution.setVariable("skipRollback", true)
            }

            if (execution.getVariable("disableRollback").equals("true" )) {
                execution.setVariable("skipRollback", true)
            }

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            def msg = "Exception in DoCreateVfModuleRollback preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    // build a SDNC vnf-topology-operation request for the specified action
    // (note: the action passed is expected to be 'changedelete' or 'delete')
    public void prepSDNCAdapterRequest(DelegateExecution execution) {

        String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")

        String uuid = execution.getVariable('testReqId') // for junits
        if(uuid==null){
            uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
        }

        def callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)

        String source = execution.getVariable("DCVFMR_source")
        String serviceId = execution.getVariable("DCVFMR_serviceId")
        String vnfId = execution.getVariable("DCVFMR_vnfId")
        String vnfType = execution.getVariable("DCVFMR_vnfType")
        String vnfName = execution.getVariable("DCVFMR_vnfName")
        String tenantId = execution.getVariable("DCVFMR_tenantId")
        String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        String vfModuleName = execution.getVariable("DCVFMR_vfModuleName")
        String vfModuleModelName = execution.getVariable("DCVFMR_vfModuleModelName")
        String cloudSiteId = execution.getVariable("DCVFMR_cloudSiteId")
        String requestId = execution.getVariable("DCVFMR_requestId")

        String serviceInstanceIdToSdnc = ""
        if (srvInstId != null && !srvInstId.isEmpty()) {
            serviceInstanceIdToSdnc = srvInstId
        } else {
            serviceInstanceIdToSdnc = vfModuleId
        }

        def doSDNCActivateRollback = execution.getVariable("DCVFMR_rollbackSDNCRequestActivate")
        def doSDNCAssignRollback = execution.getVariable("DCVFMR_rollbackSDNCRequestAssign")

        def action = ""
        def requestAction = ""

        if (doSDNCActivateRollback.equals("true")) {
            action = "delete"
            requestAction = "DisconnectVNFRequest"
        }
        else if (doSDNCAssignRollback.equals("true")) {
            action = "rollback"
            requestAction = "VNFActivateRequest"
        }
        else
            return


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
						            <request-action>${MsoUtils.xmlEscape(requestAction)}</request-action>
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
                                    <generic-vnf-name>${MsoUtils.xmlEscape(vnfName)}</generic-vnf-name>
									<generic-vnf-type>${MsoUtils.xmlEscape(vnfType)}</generic-vnf-type>
									<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
									<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

        logger.debug("sdncAdapterWorkflowRequest: " + request)
        execution.setVariable("sdncAdapterWorkflowRequest", request)
    }

    public void preProcessSDNCDeactivateRequest(DelegateExecution execution){

        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED preProcessSDNCDeactivateRequest")

        def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")

        try{
            //Build SDNC Request

            String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

            deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
            execution.setVariable("DCVFMR_deactivateSDNCRequest", deactivateSDNCRequest)
            logger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing preProcessSDNCDeactivateRequest.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preProcessSDNCDeactivateRequest")
    }

    public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED preProcessSDNCUnassignRequest Process")
        try{
            String serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")

            String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")

            execution.setVariable("DCVFMR_unassignSDNCRequest", unassignSDNCRequest)
            logger.debug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest)

        }catch(Exception e){
            logger.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED  preProcessSDNCUnassignRequest Process")
    }

    public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){

        String uuid = execution.getVariable('testReqId') // for junits
        if(uuid==null){
            uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
        }
        def callbackURL = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
        def requestId = execution.getVariable("DCVFMR_requestId")
        def serviceId = execution.getVariable("DCVFMR_serviceId")
        def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
        def vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        def source = execution.getVariable("DCVFMR_source")
        def vnfId = execution.getVariable("DCVFMR_vnfId")

        def sdncVersion = execution.getVariable("sdncVersion")

        String sdncRequest =
                """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>DeleteVfModuleInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>
			<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type/>
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
		</vf-module-information>
		<vf-module-request-input/>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

        logger.debug("sdncRequest:  " + sdncRequest)
        return sdncRequest
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing VnfAdapterDeleteV1 request
    public void prepVNFAdapterRequest(DelegateExecution execution) {

        String requestId = UUID.randomUUID().toString()
        String origRequestId = execution.getVariable("DCVFMR_requestId")
        String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")
        String aicCloudRegion = execution.getVariable("DCVFMR_cloudSiteId")
        String cloudOwner = execution.getVariable("DCVFMR_cloudOwner")
        String vnfId = execution.getVariable("DCVFMR_vnfId")
        String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        String vfModuleStackId = execution.getVariable("DCVFMR_heatStackId")
        String tenantId = execution.getVariable("DCVFMR_tenantId")
        def messageId = execution.getVariable('mso-request-id') + '-' +
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

        execution.setVariable("vnfAdapterTaskRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing UpdateAAIVfModuleRequest request
    public void prepUpdateAAIVfModule(DelegateExecution execution) {

        String vnfId = execution.getVariable("DCVFMR_vnfId")
        String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        // formulate the request for UpdateAAIVfModule
        String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
        logger.debug("UpdateAAIVfModuleRequest :" + request)
        execution.setVariable("UpdateAAIVfModuleRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing UpdateAAIVfModuleRequest request
    public void prepUpdateAAIVfModuleToAssigned(DelegateExecution execution) {

        String vnfId = execution.getVariable("DCVFMR_vnfId")
        String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        // formulate the request for UpdateAAIVfModule
        String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id></heat-stack-id>
								<orchestration-status>Assigned</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
        logger.debug("UpdateAAIVfModuleRequest :" + request)
        execution.setVariable("UpdateAAIVfModuleRequest", request)
    }

    // parse the incoming DELETE_VF_MODULE request
    // and formulate the outgoing DeleteAAIVfModuleRequest request
    public void prepDeleteAAIVfModule(DelegateExecution execution) {

        String vnfId = execution.getVariable("DCVFMR_vnfId")
        String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
        // formulate the request for UpdateAAIVfModule
        String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
        logger.debug("DeleteAAIVfModuleRequest :" + request)
        execution.setVariable("DeleteAAIVfModuleRequest", request)
    }

    // generates a WorkflowException if
    //		-
    public void handleDoDeleteVfModuleFailure(DelegateExecution execution) {

        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                "AAI error occurred deleting the Generic Vnf" + execution.getVariable("DoDVfMod_deleteGenericVnfResponse"),
                "BPMN", ErrorCode.UnknownError.getValue());
        String processKey = getProcessKey(execution);
        exceptionUtil.buildWorkflowException(execution, 5000, "Failure in DoDeleteVfModule")

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

    public void deleteNetworkPoliciesFromAAI(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED deleteNetworkPoliciesFromAAI")

        try {
            // get variables
            List fqdnList = execution.getVariable(Prefix + "createdNetworkPolicyFqdnList")
            if (fqdnList == null) {
                logger.debug("No network policies to delete")
                return
            }
            int fqdnCount = fqdnList.size()

            execution.setVariable(Prefix + "networkPolicyFqdnCount", fqdnCount)
            logger.debug("networkPolicyFqdnCount - " + fqdnCount)

            AaiUtil aaiUriUtil = new AaiUtil(this)

            if (fqdnCount > 0) {
                // AII loop call over contrail network policy fqdn list
                for (i in 0..fqdnCount-1) {

                    int counting = i+1
                    String fqdn = fqdnList[i]

                    try {
                        // Query AAI for this network policy FQDN
                        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
                        uri.queryParam("network-policy-fqdn", fqdn)
                        Optional<NetworkPolicies> networkPolicies = getAAIClient().get(NetworkPolicies.class, uri)

                        if (networkPolicies.isPresent() && !networkPolicies.get().getNetworkPolicy().isEmpty()) {
                            execution.setVariable(Prefix + "aaiQueryNetworkPolicyByFqdnReturnCode", 200)
                            NetworkPolicy networkPolicy = networkPolicies.get().getNetworkPolicy().get(0)

                            try{
                                AAIResourceUri delUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicy.getNetworkPolicyId()))
                                getAAIClient().delete(delUri)
                                execution.setVariable(Prefix + "aaiDeleteNetworkPolicyReturnCode", 200)
                                logger.debug("AAI delete network policy Response Code, NetworkPolicy #" + counting + " : " + 200)
                                logger.debug("The return code from deleting network policy is: " + 200)
                                // This network policy was deleted from AAI successfully
                                logger.debug(" DelAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : ")
                            }catch(NotFoundException ne){
                                // This network policy FQDN is not in AAI. No need to delete.
                                execution.setVariable(Prefix + "aaiDeleteNetworkPolicyReturnCode", 404)
                                logger.debug("The return code is: " + 404)
                                logger.debug("This network policy FQDN is not in AAI: " + fqdn)
                            }catch(Exception e){
                                // aai all errors
                                String delErrorMessage = "Unable to delete network-policy to AAI deleteNetworkPoliciesFromAAI - " + e.getMessage()
                                logger.debug(delErrorMessage)
                                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, delErrorMessage)
                            }
                        } else {
                            // This network policy FQDN is not in AAI. No need to delete.
                            logger.debug("This network policy FQDN is not in AAI: " + fqdn)
                        }
                    }catch (BpmnError e){
                        throw e
                    }
                    catch (Exception e) {
                        // aai all errors
                        String dataErrorMessage = "Unexpected Response from deleteNetworkPoliciesFromAAI - " + e.getMessage()
                        logger.debug(dataErrorMessage)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
                    }
                } // end loop


            } else {
                logger.debug("No contrail network policies to query/create")

            }

        } catch (BpmnError e) {
            throw e;

        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in DoCreateVfModuleRollback flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }

    }


    /**
     * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
     *
     * @param execution The flow's execution instance.
     */
    public void preProcessUpdateAAIGenericVnf(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessUpdateAAIGenericVnf((' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            def vnfId = execution.getVariable('DCVFMR_vnfId')
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
            logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


            logger.trace('Exited ' + method)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    'Caught exception in ' + method, "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessUpdateAAIGenericVnf((): ' + e.getMessage())
        }
    }

    public void setSuccessfulRollbackStatus (DelegateExecution execution){

        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED setSuccessfulRollbackStatus")

        try{
            // Set rolledBack to true, rollbackError to null
            execution.setVariable("rolledBack", true)
            execution.setVariable("rollbackError", null)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing setSuccessfulRollbackStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setSuccessfulRollbackStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED setSuccessfulRollbackStatus")
    }

    public void setFailedRollbackStatus (DelegateExecution execution){

        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED setFailedRollbackStatus")

        try{
            // Set rolledBack to false, rollbackError to actual value, rollbackData to null
            execution.setVariable("rolledBack", false)
            execution.setVariable("rollbackError", 'Caught exception in DoCreateVfModuleRollback')
            execution.setVariable("rollbackData", null)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing setFailedRollbackStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setFailedRollbackStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED setFailedRollbackStatus")
    }
}
