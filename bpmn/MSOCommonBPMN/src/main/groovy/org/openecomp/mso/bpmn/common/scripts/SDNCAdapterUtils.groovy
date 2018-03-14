/*-
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

package org.openecomp.mso.bpmn.common.scripts;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.springframework.web.util.UriUtils


/**
 * @version 1.0
 *
 */
class SDNCAdapterUtils {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	private AbstractServiceTaskProcessor taskProcessor

	public SDNCAdapterUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	String SDNCAdapterFeatureRequest(DelegateExecution execution, String requestName, String action, String callbackURL, String serviceOperation, String timeoutValueInMinutes) {
		def utils=new MsoUtils()

		def prefix = execution.getVariable('prefix')
		def request = taskProcessor.getVariable(execution, requestName)
		def requestInformation = utils.getNodeXml(request, 'request-information', false)
		def serviceInformation = utils.getNodeXml(request, 'service-information', false)
		def featureInformation = utils.getNodeXml(request, 'feature-information', false)
		def featureParameters = utils.getNodeXml(request, 'feature-parameters', false)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		def nnsRequestInformation = utils.removeXmlNamespaces(requestInformation)
		def nnsServiceInformation = utils.removeXmlNamespaces(serviceInformation)
		def nnsFeatureInformation = utils.removeXmlNamespaces(featureInformation)
		def nnsFeatureParameters = utils.removeXmlNamespaces(featureParameters)

		String sdncAdapterFeatureRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsFeatureInformation}
						${nnsFeatureParameters}
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${timeoutValueInMinutes}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterFeatureRequest = utils.removeXmlPreamble(utils.formatXML(sdncAdapterFeatureRequest))
		return sdncAdapterFeatureRequest
	}

	String SDNCAdapterActivateVnfRequest(DelegateExecution execution, String action, String callbackURL, String serviceOperation, String msoAction, String timeoutValueInMinutes) {
		def utils=new MsoUtils()

		def prefix = execution.getVariable('prefix')
		def request = taskProcessor.getVariable(execution, prefix+'Request')
		def requestInformation = utils.getNodeXml(request, 'request-information', false)
		def serviceInformation = utils.getNodeXml(request, 'service-information', false)
		def vnfInformationList = utils.getNodeXml(request, 'vnf-information-list', false)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		def nnsRequestInformation = utils.removeXmlNamespaces(requestInformation)
		def nnsServiceInformation = utils.removeXmlNamespaces(serviceInformation)
		def nnsVnfInformationList = utils.removeXmlNamespaces(vnfInformationList)

		String sdncAdapterActivateVnfRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${msoAction}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsVnfInformationList}
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${timeoutValueInMinutes}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterActivateVnfRequest = utils.removeXmlPreamble(utils.formatXML(sdncAdapterActivateVnfRequest))
		return sdncAdapterActivateVnfRequest
	}

	String SDNCAdapterL3ToHigherLayerRequest(DelegateExecution execution, String action, String callbackURL, String serviceOperation, String timeoutValueInMinutes) {
		def utils=new MsoUtils()

		def prefix = execution.getVariable('prefix')
		def request = taskProcessor.getVariable(execution, prefix+'Request')

		def requestInformation = """<request-information>
										<request-id>${execution.getVariable("mso-request-id")}</request-id>
										<request-action>torepl</request-action>
										<source>${execution.getVariable(prefix+"source")}</source>
										<notification-url>${execution.getVariable(prefix+"notificationUrl")}</notification-url>
									</request-information>"""

		// Change the value of the 'request-information'.'request-action' element
		def xml = new XmlSlurper().parseText(requestInformation)
		if("assign".equalsIgnoreCase(action)){
			xml.'request-action'.replaceBody('createTrinityBonding')
		}else if("activate".equalsIgnoreCase(action)){
			xml.'request-action'.replaceBody('activateTrinityBonding')
		}else if("delete".equalsIgnoreCase(action)){
			xml.'request-action'.replaceBody('deleteTrinityBonding')
		}
		requestInformation = utils.removeXmlPreamble(groovy.xml.XmlUtil.serialize(xml))
		def nnsRequestInformation = utils.removeXmlNamespaces(requestInformation)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		//Build Service Information
		// Send serviceName from CANOPI to sdnc for service-type
		def serviceInformation = """<service-information>
									      <service-type>${execution.getVariable(prefix+"serviceName")}</service-type>
									      <service-instance-id>${svcInstanceId}</service-instance-id>
									      <subscriber-name>${execution.getVariable(prefix+"subscriberName")}</subscriber-name>
									      <subscriber-global-id>${execution.getVariable(prefix+"subscriberGlobalId")}</subscriber-global-id>
									</service-information>"""

		//Build Additional Information - vpn or vni
		// Send serviceType from CANOPI to SDNC for nbnc-request-information service-type
		def service = execution.getVariable(prefix+"serviceType")
		def customerId = execution.getVariable(prefix+"customerId")
		def vpnId = execution.getVariable(prefix+"vpnId")
		def vpnRt = execution.getVariable(prefix+"vpnRt")
		def vpnService = execution.getVariable(prefix+"vpnService")
		def vpnRegion = execution.getVariable(prefix+"vpnRegion")
		def additionalInfo = ""
		if("assign".equalsIgnoreCase(action)){
			additionalInfo = """<vpn-data-list>
									<vpn-id>${vpnId}</vpn-id>
									<vpn-rt>${vpnRt}</vpn-rt>
									<vpn-service>${vpnService}</vpn-service>
									<vpn-region>${vpnRegion}</vpn-region>
							 	</vpn-data-list>"""
		}else if("activate".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action)){
			def vniId = execution.getVariable(prefix+'vniId')
			additionalInfo = "<vni-id>${vniId}</vni-id>"
		}

		//Set Interface Status
		def interfaceStatus = "DISABLE"
		if("activate".equalsIgnoreCase(action)){
			interfaceStatus = "ENABLE"
		}

		//Build SDNC Adapter Request
		String sdncAdapterL3ToHLRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${serviceInformation}
		                <nbnc-request-information>
							<service-type>${service}</service-type>
							<customer-id>${customerId}</customer-id>
							<interface-status>${interfaceStatus}</interface-status>
							${additionalInfo}
						</nbnc-request-information>
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${timeoutValueInMinutes}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterL3ToHLRequest = utils.removeXmlPreamble(utils.formatXML(sdncAdapterL3ToHLRequest))

		return sdncAdapterL3ToHLRequest
	}



	private void SDNCAdapterActivateRequest(DelegateExecution execution, String resultVar, String svcAction,
			String svcOperation, String additionalData) {
			def utils=new MsoUtils()

			def prefix = execution.getVariable('prefix')
			def request = taskProcessor.getVariable(execution, prefix+'Request')
			def requestInformation = utils.getNodeXml(request, 'request-information', false)
			def serviceInformation = utils.getNodeXml(request, 'service-information', false)
			def serviceParameters = utils.getNodeXml(request, 'service-parameters', false)

			def requestId = execution.getVariable('testReqId') // for junits
			if(requestId==null){
				requestId = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}

			def svcInstanceId = execution.getVariable("mso-service-instance-id")
			def msoAction = 'gammainternet'

			def timeoutInMinutes = execution.getVariable('URN_mso_sdnc_timeout_firewall_minutes')

			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (callbackUrl == null || callbackUrl.trim() == "") {
				logError('mso:workflow:sdncadapter:callback URN is not set')
				workflowException(execution, 'Internal Error', 9999) // TODO: what message and error code?
			}

			def l2HomingInformation = utils.getNodeXml(serviceParameters, 'l2-homing-information', false)
			def internetEvcAccessInformation = utils.getNodeXml(serviceParameters, 'internet-evc-access-information', false)
			def vrLan = utils.getNodeXml(serviceParameters, 'vr-lan', false)
			def upceVmsServiceInformation = utils.getNodeXml(serviceParameters, 'ucpe-vms-service-information', false)


			def nnsRequestInformation = utils.removeXmlNamespaces(requestInformation)
			def nnsServiceInformation = utils.removeXmlNamespaces(serviceInformation)
			def nnsl2HomingInformation = utils.removeXmlNamespaces(l2HomingInformation)
			def nnsInternetEvcAccessInformation = utils.removeXmlNamespaces(internetEvcAccessInformation)
			def nnsVrLan = utils.removeXmlNamespaces(vrLan)
			def nnsUpceVmsServiceInformation = utils.removeXmlNamespaces(upceVmsServiceInformation)

			if (additionalData == null) {
				additionalData = ""
			}

			boolean isAic3 = execution.getVariable("isAic3")
			
			if(isAic3) {
				nnsl2HomingInformation = updateHomingInfo(nnsl2HomingInformation, "AIC3.0")
			}
			else {
				nnsl2HomingInformation = updateHomingInfo(nnsl2HomingInformation, "AIC2.X")
			}
									
			String content = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${svcAction}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${svcOperation}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${msoAction}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${additionalData}
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsl2HomingInformation}
						${nnsInternetEvcAccessInformation}
						${nnsVrLan}
						${nnsUpceVmsServiceInformation}
					</sdncadapterworkflow:SDNCRequestData>
						<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${timeoutInMinutes}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""

			content = utils.removeXmlPreamble(utils.formatXML(content))
			execution.setVariable(resultVar, content)
	}

	/**
	 * Builds an SDNC "reserve" request and stores it in the specified execution
	 * variable. 
	 * @param execution the execution
	 * @param action the type of action: reserve, turnup, etc
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void sdncReservePrep(DelegateExecution execution, String action, String resultVar) {
		sdncReservePrep(execution, action, resultVar, false)
	}

	/**
	 * Builds an SDNC "reserve" request and stores it in the specified execution
	 * variable.
	 * @param execution the execution
	 * @param action the type of action: reserve, turnup, etc
	 * @param resultVar the execution variable in which the result will be stored
	 * @param isAic3 boolean to indicate whether request is for AIC3.0
	 */
	public void sdncReservePrep(DelegateExecution execution, String action, String resultVar, boolean isAic3) {
		sdncPrep(execution, resultVar, action , 'service-configuration-operation', null, isAic3, this.taskProcessor)
	}

	/**
	 * Builds a basic SDNC request and stores it in the specified execution variable.
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 * @param svcAction the svcAction element value
	 * @param svcOperation the svcOperation element value
	 * @param additionalData additional XML content to be inserted into the
	 *        RequestData element (may be null)
	 */
	public void sdncPrep(DelegateExecution execution, String resultVar, String svcAction,
		String svcOperation, String additionalData, AbstractServiceTaskProcessor taskProcessor) {
		sdncPrep(execution, resultVar, svcAction, svcOperation, additionalData, false, taskProcessor)
	}

	/**
	 * Builds a basic SDNC request and stores it in the specified execution variable.
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 * @param svcAction the svcAction element value
	 * @param svcOperation the svcOperation element value
	 * @param additionalData additional XML content to be inserted into the RequestData element (may be null)
	 * @param isAic3 boolean to indicate whether request is for AIC3.0
	 */
	public void sdncPrep(DelegateExecution execution, String resultVar, String svcAction,
			String svcOperation, String additionalData, boolean isAic3, AbstractServiceTaskProcessor taskProcessor) {
		def method = getClass().getSimpleName() + '.sdncPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			', svcAction=' + svcAction +
			', svcOperation=' + svcOperation +
			', additionalData=' + (additionalData == null ? "no" : "yes") +
			')'

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)
		MsoUtils utils = taskProcessor.utils
		try {
			def prefix = execution.getVariable('prefix')
			def request = taskProcessor.getVariable(execution, prefix+'Request')
			def requestInformation = utils.getNodeXml(request, 'request-information', false)
			def serviceInformation = utils.getNodeXml(request, 'service-information', false)
			def serviceParameters = utils.getChildNodes(request, 'service-parameters')
			def requestAction = utils.getNodeText1(request, 'request-action')

			def timeoutInMinutes = execution.getVariable('URN_mso_sdnc_timeout_firewall_minutes')

			def requestId = execution.getVariable('testReqId') // for junits
			if(requestId==null){
				requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
			}

			def svcInstanceId = execution.getVariable("mso-service-instance-id")
			def msoAction = 'gammainternet'

			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (callbackUrl == null || callbackUrl.trim() == "") {
				taskProcessor.logError('mso:workflow:sdncadapter:callback URN is not set')
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")
			}

			def l2HomingInformation = utils.getNodeXml(request, 'l2-homing-information', false)
			def internetEvcAccessInformation = utils.getNodeXml(request, 'internet-evc-access-information', false)
			def vrLan = utils.getNodeXml(request, 'vr-lan', false)
			def upceVmsServiceInfo = utils.getNodeXml(request, 'ucpe-vms-service-information', false)
			def vnfInformationList = utils.getNodeXml(request, 'vnf-information-list', false)

			def nnsRequestInformation = utils.removeXmlNamespaces(requestInformation)
			def nnsServiceInformation = utils.removeXmlNamespaces(serviceInformation)
			def nnsl2HomingInformation = utils.removeXmlNamespaces(l2HomingInformation)
			def nnsInternetEvcAccessInformation = utils.removeXmlNamespaces(internetEvcAccessInformation)
			def nnsVrLan = utils.removeXmlNamespaces(vrLan)
			def nnsUpceVmsServiceInfo = utils.removeXmlNamespaces(upceVmsServiceInfo)
			def nnsVnfInformationList = utils.removeXmlNamespaces(vrLan)
			def nnsinternetSvcChangeDetails = ""

			if(requestAction!=null && requestAction.equals("ChangeLayer3ServiceProvRequest")){
				def internetSvcChangeDetails = utils.removeXmlNamespaces(serviceParameters)
				nnsinternetSvcChangeDetails = """<internet-service-change-details>
							${internetSvcChangeDetails}
						</internet-service-change-details>"""
			}

			if (additionalData == null) {
				additionalData = ""
			}
			
			if(isAic3) {
				nnsl2HomingInformation = updateHomingInfo(nnsl2HomingInformation, "AIC3.0")
			}
			else {
				nnsl2HomingInformation = updateHomingInfo(nnsl2HomingInformation, "AIC2.X")
			}


			String content = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${svcAction}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${svcOperation}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${msoAction}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${additionalData}
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsl2HomingInformation}
						${nnsInternetEvcAccessInformation}
						${nnsVrLan}
						${nnsUpceVmsServiceInfo}
						${nnsVnfInformationList}
						${nnsinternetSvcChangeDetails}
					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""

			content = utils.removeXmlPreamble(utils.formatXML(content))
			execution.setVariable(resultVar, content)
			taskProcessor.logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)

			taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			taskProcessor.logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error")
		}
	}
	
	public String updateHomingInfo(String homingInfo, String aicVersion) {
		String newHomingInfo
		if(homingInfo == null || homingInfo.trim().length() == 0) {
			newHomingInfo = "<l2-homing-information><aic-version>" + aicVersion + "</aic-version></l2-homing-information>"
		}
		else {
			newHomingInfo = homingInfo.substring(0, homingInfo.indexOf("</l2-homing-information>")) + "<aic-version>" + aicVersion + "</aic-version></l2-homing-information>"
		}
	}
	
	/**
	 * Builds a topology SDNC request and return String request.
	 * As V2 will use 1607-style request, region instead of aic clli code
	 * @param execution, the execution
	 * @param requestXML, the incoming request for the flow
	 * @param serviceInstanceId, the serviceIntance (if available)
	 * @param callbackUrl, the call back url
	 * @param action, the action element value
	 * @param requestAction the svcOperation element value
	 * @param cloudRegionId the aai's cloud-region-id
	 * @param networkId the aai's network-id
	 * @param additionalData additional XML content to be inserted into the
	 *   RequestData element (may be null)
	 */
	 public String sdncTopologyRequestV2 (DelegateExecution execution, String requestXML, String serviceInstanceId, String callbackUrl, String action, String requestAction, String cloudRegionId, networkId, String queryAAIResponse, String additionalData) {
		 def utils=new MsoUtils()

		 // SNDC is expecting request Id for header as unique each call.
		 String hdrRequestId = ""
		 String testHdrRequestId = execution.getVariable("testMessageId")  // for test purposes.
		 if (testHdrRequestId == null) {
			 hdrRequestId = UUID.randomUUID()  // generate unique
		 } else {
			 hdrRequestId = testHdrRequestId
		 }
		 
		 String requestId = ""
		 try {
			 requestId = execution.getVariable("mso-request-id")
		 } catch (Exception ex) {
			 requestId = utils.getNodeText1(requestXML, "request-id")
		 }

		 String aicCloudRegion = cloudRegionId
		 String tenantId = ""
		 if (utils.nodeExists(requestXML, "tenant-id")) {
			 tenantId = utils.getNodeText1(requestXML, "tenant-id")
		 }
		 String networkType = ""
		 if (utils.nodeExists(requestXML, "network-type")) {
			 networkType = utils.getNodeText1(requestXML, "network-type")
		 }

		 // Replace/Use the value of network-type from aai query (vs input) during Delete Network flows.
		 if (queryAAIResponse != null) {
		     networkType = utils.getNodeText1(queryAAIResponse, "network-type")
		 }

		 String serviceId = ""
		 if (utils.nodeExists(requestXML, "service-id")) {
			 serviceId = utils.getNodeText1(requestXML, "service-id")
		 }
		 String networkName = ""
		 if (utils.nodeExists(requestXML, "network-name")) {
			 networkName = utils.getNodeText1(requestXML, "network-name")
		 }
		 String source = ""
		 if (utils.nodeExists(requestXML, "source")) {
			 source = utils.getNodeText1(requestXML, "source")
		 }

		 // get resourceLink from subflow execution variable
		 String serviceType = ""
		 String subscriberName = ""
		 String siRelatedLink = execution.getVariable("GENGSI_siResourceLink")
		 if (siRelatedLink != null) {
			 // get service type
			 int serviceStart = siRelatedLink.indexOf("service-subscription/")
			 int serviceEnd = siRelatedLink.indexOf("/service-instances/")
			 serviceType = siRelatedLink.substring(serviceStart + 21, serviceEnd)
			 serviceType = UriUtils.decode(serviceType,"UTF-8")
			 // get subscriber name
			 int subscriberNameStart = siRelatedLink.indexOf("customers/customer/")
			 int subscriberNameEnd = siRelatedLink.indexOf("/service-subscriptions/")
		     subscriberName = siRelatedLink.substring(subscriberNameStart + 19, subscriberNameEnd)
			 subscriberName = UriUtils.decode(subscriberName,"UTF-8")
		 }

		 String content =
			"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
		                                  xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
		                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1"
		                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1">
					   <sdncadapter:RequestHeader>
						  <sdncadapter:RequestId>${hdrRequestId}</sdncadapter:RequestId>
						  <sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
						  <sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						  <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
						  <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
					   </sdncadapter:RequestHeader>
					   <sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					            <request-id>${requestId}</request-id>
					            <request-action>${requestAction}</request-action>
					            <source>${source}</source>
					            <notification-url></notification-url>
					            <order-number></order-number>
					            <order-version></order-version>
					         </request-information>
					         <service-information>
					            <service-id>${serviceId}</service-id>
								<service-type>${serviceType}</service-type>
					            <service-instance-id>${serviceInstanceId}</service-instance-id>
					            <subscriber-name>${subscriberName}</subscriber-name>
					         </service-information>
					         <network-request-information>
					            <network-id>${networkId}</network-id>
					            <network-type>${networkType}</network-type>
					            <network-name>${networkName}</network-name>
					            <tenant>${tenantId}</tenant>
					            <aic-cloud-region>${aicCloudRegion}</aic-cloud-region>
					         </network-request-information>
					   </sdncadapterworkflow:SDNCRequestData>
				   </aetgt:SDNCAdapterWorkflowRequest>""".trim()

			return content
	 }

	 /**
	  * Builds a topology SDNC request and return String request.
	  * As V2 will use 1607-style request, region instead of aic clli code
	  * @param execution, the execution
	  * @param requestXML, the incoming request for the flow
	  * @param serviceInstanceId, the serviceIntance (if available)
	  * @param callbackUrl, the call back url
	  * @param action, the action element value
	  * @param requestAction the svcOperation element value
	  * @param cloudRegionId the aai's cloud-region-id
	  * @param networkId the aai's network-id
	  * @param additionalData additional XML content to be inserted into the
	  *   RequestData element (may be null)
	  */
	  public String sdncTopologyRequestRsrc (DelegateExecution execution, String requestXML, String serviceInstanceId, String callbackUrl, String action, String requestAction, String cloudRegionId, networkId, String additionalData) {
		  def utils=new MsoUtils()

		  // SNDC is expecting request Id for header as unique each call.
		  String hdrRequestId = ""
		  String testHdrRequestId = execution.getVariable("testMessageId")  // for test purposes.
		  if (testHdrRequestId == null) {
		      hdrRequestId = UUID.randomUUID()  // generate unique
		  } else {
		      hdrRequestId = testHdrRequestId
		  }

		  String requestId = ""
		  String testRequestId = execution.getVariable("testMessageId")  // for test purposes.
		  if (testRequestId == null) {
			  requestId = execution.getVariable("mso-request-id")
			  if (requestId == null) {
				  requestId = execution.getVariable("msoRequestId")
			  }		
		  } else {
		  	  requestId = testRequestId
		  }		  
		  
		  String aicCloudRegion = cloudRegionId
		  String tenantId = ""
		  if (utils.nodeExists(requestXML, "tenant-id")) {
			  tenantId = utils.getNodeText1(requestXML, "tenant-id")
		  }
		  String networkType = ""
		  if (utils.nodeExists(requestXML, "network-type")) {
			  networkType = utils.getNodeText1(requestXML, "network-type")
		  }

		  String subscriptionServiceType = ""
		  if (utils.nodeExists(requestXML, "subscription-service-type")) {
			  subscriptionServiceType = utils.getNodeText1(requestXML, "subscription-service-type")
		  }

		  String globalCustomerId = ""
		  if (utils.nodeExists(requestXML, "global-customer-id")) {
			  globalCustomerId = utils.getNodeText1(requestXML, "global-customer-id")
		  }

		  String serviceId = ""
		  if (utils.nodeExists(requestXML, "service-id")) {
			  serviceId = utils.getNodeText1(requestXML, "service-id")
		  }
		  String networkName = ""
		  if (utils.nodeExists(requestXML, "network-name")) {
			  networkName = utils.getNodeText1(requestXML, "network-name")
		  }
		  String source = ""
		  if (utils.nodeExists(requestXML, "source")) {
			  source = utils.getNodeText1(requestXML, "source")
		  }

		  // get resourceLink from subflow execution variable
		  String serviceType = ""
		  String subscriberName = ""
		  String siRelatedLink = execution.getVariable("GENGSI_siResourceLink")
		  if (siRelatedLink != null) {
			  // get service type
			  int serviceStart = siRelatedLink.indexOf("service-subscription/")
			  int serviceEnd = siRelatedLink.indexOf("/service-instances/")
			  serviceType = siRelatedLink.substring(serviceStart + 21, serviceEnd)
			  serviceType = UriUtils.decode(serviceType,"UTF-8")
			  // get subscriber name
			  int subscriberNameStart = siRelatedLink.indexOf("customers/customer/")
			  int subscriberNameEnd = siRelatedLink.indexOf("/service-subscriptions/")
			  subscriberName = siRelatedLink.substring(subscriberNameStart + 19, subscriberNameEnd)
			  subscriberName = UriUtils.decode(subscriberName,"UTF-8")
		  }

		  // network-information from 'networkModelInfo' // verify the DB Catalog response
		  String networkModelInfo = utils.getNodeXml(requestXML, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
		  String modelInvariantUuid = utils.getNodeText1(networkModelInfo, "modelInvariantUuid") !=null ?
		                              utils.getNodeText1(networkModelInfo, "modelInvariantUuid") : ""
		  String modelCustomizationUuid = utils.getNodeText1(networkModelInfo, "modelCustomizationUuid")  !=null ?
		                                  utils.getNodeText1(networkModelInfo, "modelCustomizationUuid")  : ""
		  String modelUuid = utils.getNodeText1(networkModelInfo, "modelUuid") !=null ?
		                     utils.getNodeText1(networkModelInfo, "modelUuid") : ""
		  String modelVersion = utils.getNodeText1(networkModelInfo, "modelVersion") !=null ?
		  	                    utils.getNodeText1(networkModelInfo, "modelVersion") : ""
		  String modelName = utils.getNodeText1(networkModelInfo, "modelName") !=null ?
		                     utils.getNodeText1(networkModelInfo, "modelName") : ""

		 // service-information from 'networkModelInfo' // verify the DB Catalog response
		 String serviceModelInfo = utils.getNodeXml(requestXML, "serviceModelInfo", false).replace("tag0:","").replace(":tag0","")
		 String serviceModelInvariantUuid = utils.getNodeText1(serviceModelInfo, "modelInvariantUuid")  !=null ?
										    utils.getNodeText1(serviceModelInfo, "modelInvariantUuid")  : ""
		 String serviceModelUuid = utils.getNodeText1(serviceModelInfo, "modelUuid") !=null ?
							       utils.getNodeText1(serviceModelInfo, "modelUuid") : ""
		 String serviceModelVersion = utils.getNodeText1(serviceModelInfo, "modelVersion") !=null ?
							          utils.getNodeText1(serviceModelInfo, "modelVersion") : ""
		 String serviceModelName = utils.getNodeText1(serviceModelInfo, "modelName") !=null ?
						           utils.getNodeText1(serviceModelInfo, "modelName") : ""


		  String content =
		  """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
   				                                    xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1" 
                                                    xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
					   <sdncadapter:RequestHeader>
						  <sdncadapter:RequestId>${hdrRequestId}</sdncadapter:RequestId>
						  <sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
						  <sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						  <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
						  <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                          <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
					   </sdncadapter:RequestHeader>
   					   <sdncadapterworkflow:SDNCRequestData>
						   <request-information>
						      <request-id>${requestId}</request-id>
						      <request-action>${requestAction}</request-action>
						      <source>${source}</source>
						      <notification-url></notification-url>
						      <order-number></order-number>
						      <order-version></order-version>
						   </request-information>
						   <service-information>
						      <service-id>${serviceId}</service-id>
						      <subscription-service-type>${subscriptionServiceType}</subscription-service-type>
							  <ecomp-model-information>
								   <model-invariant-uuid>${serviceModelInvariantUuid}</model-invariant-uuid>
								   <model-uuid>${serviceModelUuid}</model-uuid>
								   <model-version>${serviceModelVersion}</model-version>
								   <model-name>${serviceModelName}</model-name>
                              </ecomp-model-information>
						      <service-instance-id>${serviceInstanceId}</service-instance-id>
						      <global-customer-id>${globalCustomerId}</global-customer-id>
						      <subscriber-name>${subscriberName}</subscriber-name>
						   </service-information>
						   <network-information>
						      <network-id>${networkId}</network-id>
						      <network-type>${networkType}</network-type>
							  <ecomp-model-information>
								   <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
								   <model-customization-uuid>${modelCustomizationUuid}</model-customization-uuid>
								   <model-uuid>${modelUuid}</model-uuid>
								   <model-version>${modelVersion}</model-version>
								   <model-name>${modelName}</model-name>
							  </ecomp-model-information>
						   </network-information>
						   <network-request-input>
						     <network-name>${networkName}</network-name>
					         <tenant>${tenantId}</tenant>
						     <aic-cloud-region>${aicCloudRegion}</aic-cloud-region>
						     <aic-clli></aic-clli>
						     <network-input-parameters/>
						   </network-request-input>
				      </sdncadapterworkflow:SDNCRequestData>
                   </aetgt:SDNCAdapterWorkflowRequest>""".trim()

			 return content
	  }

			/**
			 * Validates a workflow response.
			 * @param execution the execution
			 * @param responseVar the execution variable in which the response is stored
			 * @param workflowException the WorkflowException Object returned from sdnc call
			 */
	public void validateSDNCResponse(DelegateExecution execution, String response, WorkflowException workflowException, boolean successIndicator){
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.utils.log("DEBUG", "SDNC Response is: " + response, isDebugLogEnabled)
		taskProcessor.utils.log("DEBUG", "SuccessIndicator is: " + successIndicator, isDebugLogEnabled)

		try {
			def prefix = execution.getVariable('prefix')
			execution.setVariable(prefix+'sdncResponseSuccess', false)
			taskProcessor.utils.log("DEBUG", "Response" + ' = ' + (response == null ? "" : System.lineSeparator()) + response, isDebugLogEnabled)

			if (successIndicator){
				if (response == null || response.trim().equals("")) {
					taskProcessor.utils.log("DEBUG", response + ' is empty');
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "SDNCAdapter Workflow Response is Empty")
				}else{

					// we need to peer into the request data for error
					def String sdncAdapterWorkflowResponse = taskProcessor.utils.getNodeXml(response, 'response-data', false)
					def String decodedXml = decodeXML(sdncAdapterWorkflowResponse).replace('<?xml version="1.0" encoding="UTF-8"?>', "")

					// change '&' to "&amp; (if present as data, ex: subscriber-name = 'FOUR SEASONS HEATING & COOLING_8310006378683'
					decodedXml = decodedXml.replace("&", "&amp;")

					taskProcessor.utils.log("DEBUG","decodedXml:\n" + decodedXml, isDebugLogEnabled)

					int requestDataResponseCode = 200
					def String requestDataResponseMessage = ''

					try{
						if (taskProcessor.utils.nodeExists(decodedXml, "response-message")) {
							requestDataResponseMessage = taskProcessor.utils.getNodeText(decodedXml, "response-message")
						} else if (taskProcessor.utils.nodeExists(decodedXml, "ResponseMessage")) {
							requestDataResponseMessage = taskProcessor.utils.getNodeText(decodedXml, "ResponseMessage")
						}
					}catch(Exception e){
						taskProcessor.utils.log("DEBUG", 'Error caught while decoding resposne ' + e.getMessage(), isDebugLogEnabled)
					}

					if(taskProcessor.utils.nodeExists(decodedXml, "response-code")) {
						taskProcessor.utils.log("DEBUG","response-code node Exist ", isDebugLogEnabled)
						String code = taskProcessor.utils.getNodeText1(decodedXml, "response-code")
						if(code.isEmpty() || code.equals("")){
							// if response-code is blank then Success
							taskProcessor.utils.log("DEBUG","response-code node is empty", isDebugLogEnabled)
							requestDataResponseCode = 0
						}else{
							requestDataResponseCode  = code.toInteger()
							taskProcessor.utils.log("DEBUG","response-code is: " + requestDataResponseCode, isDebugLogEnabled)
						}
					}else if(taskProcessor.utils.nodeExists(decodedXml, "ResponseCode")){
						taskProcessor.utils.log("DEBUG","ResponseCode node Exist ", isDebugLogEnabled)
						String code = taskProcessor.utils.getNodeText1(decodedXml, "ResponseCode")
						if(code.isEmpty() || code.equals("")){
							// if ResponseCode blank then Success
							taskProcessor.utils.log("DEBUG","ResponseCode node is empty", isDebugLogEnabled)
							requestDataResponseCode = 0
						}else{
							requestDataResponseCode  = code.toInteger()
							taskProcessor.utils.log("DEBUG","ResponseCode is: " + requestDataResponseCode, isDebugLogEnabled)
						}
					}else{
						taskProcessor.utils.log("DEBUG","A Response Code DOES NOT Exist.", isDebugLogEnabled)
						// if a response code does not exist then Success
						requestDataResponseCode = 0
					}
					try{

						execution.setVariable(prefix+'sdncRequestDataResponseCode', requestDataResponseCode.toString())
						// if a response code is 0 or 2XX then Success
						if ((requestDataResponseCode >= 200 && requestDataResponseCode <= 299) || requestDataResponseCode == 0) {
							execution.setVariable(prefix+'sdncResponseSuccess', true)
							taskProcessor.utils.log("DEBUG", "Setting sdncResponseSuccess to True ", isDebugLogEnabled)
							taskProcessor.utils.log("DEBUG", "Exited ValidateSDNCResponse Method", isDebugLogEnabled)
						}else{
							ExceptionUtil exceptionUtil = new ExceptionUtil()
							String convertedCode = exceptionUtil.MapSDNCResponseCodeToErrorCode(requestDataResponseCode.toString())
							int convertedCodeInt = Integer.parseInt(convertedCode)
							exceptionUtil.buildAndThrowWorkflowException(execution, convertedCodeInt, "Received error from SDN-C: " + requestDataResponseMessage)
						}

					}catch(Exception e){
						//TODO proper handling of new, non numerical response codes in 1607 and new error handling for common API
						requestDataResponseCode = 500
					}

					taskProcessor.utils.log("DEBUG", "SDNC callback response-code: " + requestDataResponseCode, isDebugLogEnabled)
					taskProcessor.utils.log("DEBUG", "SDNC callback response-message: " + requestDataResponseMessage, isDebugLogEnabled)
				}

			}else {
				taskProcessor.utils.log("DEBUG", 'SDNCAdapter Subflow did NOT complete Successfully.  SuccessIndicator is False. ')
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}else{
					//TODO : what error code and error message use here
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - SDNCAdapter Subflow did NOT complete successfully.")
				}
			}

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			taskProcessor.utils.log("DEBUG", 'END of Validate SDNC Response', isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, 'Internal Error- Unable to validate SDNC Response ');
		}
	}

	/**
			 * Validates a workflow response.
			 * @param execution the execution
			 * @param responseVar the execution variable in which the response is stored
			 * @param responseCodeVar the execution variable in which the response code is stored
			 * @param errorResponseVar the execution variable in which the error response is stored
			 */
			public void validateL3BondingSDNCResp(DelegateExecution execution, String response, WorkflowException workflowException, boolean success) {
				def method = getClass().getSimpleName() + '.validateL3BondingSDNCResp(' +
					'execution=' + execution.getId() +
					', response=' + response +
					')'
				def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
				taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)
				def prefix = execution.getVariable('prefix')
				TrinityExceptionUtil trinityExceptionUtil = new TrinityExceptionUtil()

				try {
					execution.setVariable(prefix+'sdncResponseSuccess', false)

					taskProcessor.utils.log("sdncAdapter Success Indicator is: " + success, isDebugLogEnabled)
					if (success) {

						// we need to look inside the request data for error
						def String callbackRequestData = taskProcessor.utils.getNodeXml(response, 'RequestData', false)
						def String decodedXml = decodeXML(callbackRequestData)
						taskProcessor.utils.log("DEBUG","decodedXml:\n" + decodedXml, isDebugLogEnabled)

						def requestDataResponseCode = '200'
						def requestDataResponseMessage = ''
						int intDataResponseCode = 200

						if (taskProcessor.utils.nodeExists(decodedXml, "response-code")) {

							requestDataResponseCode  = ((String) taskProcessor.utils.getNodeText(decodedXml, "response-code"))
							if (taskProcessor.utils.nodeExists(decodedXml, "response-message")) {
								requestDataResponseMessage  = taskProcessor.utils.getNodeText(decodedXml, "response-message")
							}
						}else if(taskProcessor.utils.nodeExists(decodedXml, "ResponseCode")){
							requestDataResponseCode  = ((String) taskProcessor.utils.getNodeText1(decodedXml, "ResponseCode")).toInteger()
						}else if(taskProcessor.utils.nodeExists(response, "ResponseCode")){
							requestDataResponseCode  = ((String) taskProcessor.utils.getNodeText1(response, "ResponseCode")).toInteger()
							requestDataResponseMessage  = taskProcessor.utils.getNodeText(response, "ResponseMessage")
						}

						taskProcessor.utils.log("DEBUG", "SDNC callback response-code: " + requestDataResponseCode, isDebugLogEnabled)
						taskProcessor.utils.log("DEBUG", "SDNC callback response-message: " + requestDataResponseMessage, isDebugLogEnabled)

						// Get the AAI Status to determine if rollback is needed on ASSIGN
						def aai_status = ''
						if (taskProcessor.utils.nodeExists(decodedXml, "aai-status")) {
							aai_status = ((String) taskProcessor.utils.getNodeText(decodedXml, "aai-status"))
							taskProcessor.utils.log("DEBUG", "SDNC sent AAI STATUS code: " + aai_status, isDebugLogEnabled)
						}
						if (aai_status != null && !aai_status.equals("")) {
							execution.setVariable(prefix+"AaiStatus",aai_status)
							taskProcessor.utils.log("DEBUG", "Set variable " + prefix + "AaiStatus: " + execution.getVariable(prefix+"AaiStatus"), isDebugLogEnabled)
						}

						// Get the result string to determine if rollback is needed on ASSIGN in Add Bonding flow only
						def sdncResult = ''
						if (taskProcessor.utils.nodeExists(decodedXml, "result")) {
							sdncResult = ((String) taskProcessor.utils.getNodeText(decodedXml, "result"))
							taskProcessor.utils.log("DEBUG", "SDNC sent result: " + sdncResult, isDebugLogEnabled)
						}
						if (sdncResult != null && !sdncResult.equals("")) {
							execution.setVariable(prefix+"SdncResult",sdncResult)
							taskProcessor.utils.log("DEBUG", "Set variable " + prefix + "SdncResult: " + execution.getVariable(prefix+"SdncResult"), isDebugLogEnabled)
						}

						try{
							intDataResponseCode = Integer.parseInt(String.valueOf(requestDataResponseCode))
						}catch(Exception e){
							intDataResponseCode = 400
						}

						taskProcessor.utils.log("DEBUG", "intDataResponseCode " + intDataResponseCode , isDebugLogEnabled)

						// if response-code is not Success (200, 201, etc) we need to throw an exception
						if ((intDataResponseCode < 200 || intDataResponseCode > 299) && intDataResponseCode != 0) {
							execution.setVariable(prefix+'ResponseCode', intDataResponseCode)
							execution.setVariable("L3HLAB_rollback", true)
							def msg = trinityExceptionUtil.mapSDNCAdapterExceptionToErrorResponse(response, execution)
							exceptionUtil.buildAndThrowWorkflowException(execution, intDataResponseCode, "Received error from SDN-C: " + msg)

						}
					}else {
						taskProcessor.logWarn('sdncAdapter did not complete successfully, sdncAdapter Success Indicator was false ')
						execution.setVariable("L3HLAB_rollback", true)
						def msg = trinityExceptionUtil.mapSDNCAdapterExceptionToErrorResponse(response, execution)
						exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, msg)
					}

					if (response == null || response.trim().equals("")) {
						taskProcessor.logWarn('sdncAdapter workflow response is empty');
						execution.setVariable("L3HLAB_rollback", true)
						def msg = trinityExceptionUtil.buildException("Exception occurred while validating SDNC response " , execution)
						exceptionUtil.buildAndThrowWorkflowException(execution, intResponseCode, msg)
					}

					execution.setVariable(prefix+'sdncResponseSuccess', true)
					taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
				} catch (BpmnError e) {
					throw e;
				} catch (Exception e) {
					taskProcessor.logError('Caught exception in ' + method, e)
					execution.setVariable(prefix+"ResponseCode",400)
					execution.setVariable("L3HLAB_rollback", true)
					def msg = trinityExceptionUtil.buildException("Exception occurred while validating SDNC response: " + e.getMessage(), execution)
					exceptionUtil.buildAndThrowWorkflowException(execution, 400, msg)
				}
			}
			
	public String modelInfoToEcompModelInformation(String jsonModelInfo) {
		String modelInvariantUuid = jsonUtil.getJsonValue(jsonModelInfo, "modelInvariantUuid")
		String modelUuid = jsonUtil.getJsonValue(jsonModelInfo, "modelUuid")
		if (modelUuid == null) {
			modelUuid = ""
		}
		String modelCustomizationUuid = jsonUtil.getJsonValue(jsonModelInfo, "modelCustomizationUuid")
		String modelCustomizationString = ""
		if (modelCustomizationUuid != null) {
			modelCustomizationString = "<model-customization-uuid>${modelCustomizationUuid}</model-customization-uuid>"
		}			
		String modelVersion = jsonUtil.getJsonValue(jsonModelInfo, "modelVersion")
		if (modelVersion == null) {
			modelVersion = ""
		}		
		String modelName = jsonUtil.getJsonValue(jsonModelInfo, "modelName")
		String ecompModelInformation =
				"""<ecomp-model-information>
						<model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
						<model-uuid>${modelUuid}</model-uuid>
						${modelCustomizationString}
						<model-version>${modelVersion}</model-version>
						<model-name>${modelName}</model-name>
				</ecomp-model-information>"""

		return ecompModelInformation		
	} 

	/**
	 * Decode XML - replace &amp; &lt; and &gt; with '&', '<' and '>'
	 * @param xml - the xml to be decoded
	 */
	private String decodeXML(String xml) {
		def String decodedXml = xml.replaceAll("&amp;", "&")
		decodedXml = decodedXml.replaceAll("&lt;", "<")
		decodedXml = decodedXml.replaceAll("&gt;", ">")
	}
	
	

}
