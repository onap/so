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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.L3Network
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.logging.filter.base.ErrorCode;
import org.springframework.web.util.UriUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory




/**
 * @version 1.0
 *
 */
class SDNCAdapterUtils {
    private static final Logger logger = LoggerFactory.getLogger( SDNCAdapterUtils.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	MsoUtils  msoUtils = new MsoUtils()

	public SDNCAdapterUtils() {
	}

	String SDNCAdapterFeatureRequest(DelegateExecution execution, String requestName, String action, String callbackURL, String serviceOperation, String timeoutValueInMinutes) {

		def prefix = execution.getVariable('prefix')
		def request = AbstractServiceTaskProcessor.getVariable(execution, requestName)
		def requestInformation = msoUtils.getNodeXml(request, 'request-information', false)
		def serviceInformation = msoUtils.getNodeXml(request, 'service-information', false)
		def featureInformation = msoUtils.getNodeXml(request, 'feature-information', false)
		def featureParameters = msoUtils.getNodeXml(request, 'feature-parameters', false)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		def nnsRequestInformation = msoUtils.removeXmlNamespaces(requestInformation)
		def nnsServiceInformation = msoUtils.removeXmlNamespaces(serviceInformation)
		def nnsFeatureInformation = msoUtils.removeXmlNamespaces(featureInformation)
		def nnsFeatureParameters = msoUtils.removeXmlNamespaces(featureParameters)

		String sdncAdapterFeatureRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsFeatureInformation}
						${nnsFeatureParameters}
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${MsoUtils.xmlEscape(timeoutValueInMinutes)}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterFeatureRequest = msoUtils.removeXmlPreamble(msoUtils.formatXml(sdncAdapterFeatureRequest))
		return sdncAdapterFeatureRequest
	}

	String SDNCAdapterActivateVnfRequest(DelegateExecution execution, String action, String callbackURL, String serviceOperation, String msoAction, String timeoutValueInMinutes) {

		def prefix = execution.getVariable('prefix')
		def request = AbstractServiceTaskProcessor.getVariable(execution, prefix+'Request')
		def requestInformation = msoUtils.getNodeXml(request, 'request-information', false)
		def serviceInformation = msoUtils.getNodeXml(request, 'service-information', false)
		def vnfInformationList = msoUtils.getNodeXml(request, 'vnf-information-list', false)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		def nnsRequestInformation = msoUtils.removeXmlNamespaces(requestInformation)
		def nnsServiceInformation = msoUtils.removeXmlNamespaces(serviceInformation)
		def nnsVnfInformationList = msoUtils.removeXmlNamespaces(vnfInformationList)

		String sdncAdapterActivateVnfRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:sdncadapterworkflow="http://openecomp.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${MsoUtils.xmlEscape(msoAction)}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${nnsServiceInformation}
						${nnsVnfInformationList}
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${MsoUtils.xmlEscape(timeoutValueInMinutes)}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterActivateVnfRequest = msoUtils.removeXmlPreamble(msoUtils.formatXml(sdncAdapterActivateVnfRequest))
		return sdncAdapterActivateVnfRequest
	}

	String SDNCAdapterL3ToHigherLayerRequest(DelegateExecution execution, String action, String callbackURL, String serviceOperation, String timeoutValueInMinutes) {

		def prefix = execution.getVariable('prefix')
		def request = AbstractServiceTaskProcessor.getVariable(execution, prefix+'Request')

		String requestInformation = """<request-information>
										<request-id>${MsoUtils.xmlEscape(execution.getVariable("mso-request-id"))}</request-id>
										<request-action>torepl</request-action>
										<source>${MsoUtils.xmlEscape(execution.getVariable(prefix+"source"))}</source>
										<notification-url>${MsoUtils.xmlEscape(execution.getVariable(prefix+"notificationUrl"))}</notification-url>
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
		requestInformation = msoUtils.removeXmlPreamble(groovy.xml.XmlUtil.serialize(xml))
		def nnsRequestInformation = msoUtils.removeXmlNamespaces(requestInformation)

		def requestId = execution.getVariable('testReqId') // for junits
		if(requestId==null){
			requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}

		def svcInstanceId = execution.getVariable("mso-service-instance-id")

		//Build Service Information
		// Send serviceName from CANOPI to sdnc for service-type
		String serviceInformation = """<service-information>
									      <service-type>${MsoUtils.xmlEscape(execution.getVariable(prefix+"serviceName"))}</service-type>
									      <service-instance-id>${MsoUtils.xmlEscape(svcInstanceId)}</service-instance-id>
									      <subscriber-name>${MsoUtils.xmlEscape(execution.getVariable(prefix+"subscriberName"))}</subscriber-name>
									      <subscriber-global-id>${MsoUtils.xmlEscape(execution.getVariable(prefix+"subscriberGlobalId"))}</subscriber-global-id>
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
									<vpn-id>${MsoUtils.xmlEscape(vpnId)}</vpn-id>
									<vpn-rt>${MsoUtils.xmlEscape(vpnRt)}</vpn-rt>
									<vpn-service>${MsoUtils.xmlEscape(vpnService)}</vpn-service>
									<vpn-region>${MsoUtils.xmlEscape(vpnRegion)}</vpn-region>
							 	</vpn-data-list>"""
		}else if("activate".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action)){
			def vniId = execution.getVariable(prefix+'vniId')
			additionalInfo = "<vni-id>${MsoUtils.xmlEscape(vniId)}</vni-id>"
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
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
						${nnsRequestInformation}
						${serviceInformation}
		                <nbnc-request-information>
							<service-type>${MsoUtils.xmlEscape(service)}</service-type>
							<customer-id>${MsoUtils.xmlEscape(customerId)}</customer-id>
							<interface-status>${MsoUtils.xmlEscape(interfaceStatus)}</interface-status>
							${additionalInfo}
						</nbnc-request-information>
					</sdncadapterworkflow:SDNCRequestData>
					<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${MsoUtils.xmlEscape(timeoutValueInMinutes)}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
		sdncAdapterL3ToHLRequest = msoUtils.removeXmlPreamble(msoUtils.formatXml(sdncAdapterL3ToHLRequest))

		return sdncAdapterL3ToHLRequest
	}



	private void SDNCAdapterActivateRequest(DelegateExecution execution, String resultVar, String svcAction,
			String svcOperation, String additionalData) {

			def prefix = execution.getVariable('prefix')
			def request = AbstractServiceTaskProcessor.getVariable(execution, prefix+'Request')
			def requestInformation = msoUtils.getNodeXml(request, 'request-information', false)
			def serviceInformation = msoUtils.getNodeXml(request, 'service-information', false)
			def serviceParameters = msoUtils.getNodeXml(request, 'service-parameters', false)

			def requestId = execution.getVariable('testReqId') // for junits
			if(requestId==null){
				requestId = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}

			def svcInstanceId = execution.getVariable("mso-service-instance-id")
			def msoAction = 'gammainternet'

			def timeoutInMinutes = UrnPropertiesReader.getVariable('mso.sdnc.timeout.firewall.minutes',execution)

			def callbackUrl = (String)UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback',execution)
			if (callbackUrl == null || callbackUrl.trim() == "") {
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
						'mso:workflow:sdncadapter:callback URN is not set', "BPMN",
						ErrorCode.UnknownError.getValue());
				workflowException(execution, 'Internal Error', 9999) // TODO: what message and error code?
			}

			def l2HomingInformation = msoUtils.getNodeXml(serviceParameters, 'l2-homing-information', false)
			def internetEvcAccessInformation = msoUtils.getNodeXml(serviceParameters, 'internet-evc-access-information', false)
			def vrLan = msoUtils.getNodeXml(serviceParameters, 'vr-lan', false)
			def upceVmsServiceInformation = msoUtils.getNodeXml(serviceParameters, 'ucpe-vms-service-information', false)


			def nnsRequestInformation = msoUtils.removeXmlNamespaces(requestInformation)
			def nnsServiceInformation = msoUtils.removeXmlNamespaces(serviceInformation)
			def nnsl2HomingInformation = msoUtils.removeXmlNamespaces(l2HomingInformation)
			def nnsInternetEvcAccessInformation = msoUtils.removeXmlNamespaces(internetEvcAccessInformation)
			def nnsVrLan = msoUtils.removeXmlNamespaces(vrLan)
			def nnsUpceVmsServiceInformation = msoUtils.removeXmlNamespaces(upceVmsServiceInformation)

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
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${MsoUtils.xmlEscape(svcAction)}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(svcOperation)}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${MsoUtils.xmlEscape(msoAction)}</sdncadapter:MsoAction>
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
						<sdncadapterworkflow:SDNCTimeOutValueInMinutes>${MsoUtils.xmlEscape(timeoutInMinutes)}</sdncadapterworkflow:SDNCTimeOutValueInMinutes>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""

			content = msoUtils.removeXmlPreamble(msoUtils.formatXml(content))
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
		sdncPrep(execution, resultVar, action , 'service-configuration-operation', null, isAic3)
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
		String svcOperation, String additionalData) {
		sdncPrep(execution, resultVar, svcAction, svcOperation, additionalData, false)
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
			String svcOperation, String additionalData, boolean isAic3) {
		def method = getClass().getSimpleName() + '.sdncPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			', svcAction=' + svcAction +
			', svcOperation=' + svcOperation +
			', additionalData=' + (additionalData == null ? "no" : "yes") +
			')'

		logger.trace('Entered ' + method)

		try {
			def prefix = execution.getVariable('prefix')
			def request = AbstractServiceTaskProcessor.getVariable(execution, prefix+'Request')
			def requestInformation = msoUtils.getNodeXml(request, 'request-information', false)
			def serviceInformation = msoUtils.getNodeXml(request, 'service-information', false)
			def serviceParameters = msoUtils.getChildNodes(request, 'service-parameters')
			def requestAction = msoUtils.getNodeText(request, 'request-action')

			def timeoutInMinutes = UrnPropertiesReader.getVariable('mso.sdnc.timeout.firewall.minutes',execution)

			def requestId = execution.getVariable('testReqId') // for junits
			if(requestId==null){
				requestId = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
			}

			def svcInstanceId = execution.getVariable("mso-service-instance-id")
			def msoAction = 'gammainternet'

			def callbackUrl = (String)UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback',execution)
			if (callbackUrl == null || callbackUrl.trim() == "") {
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
						'mso:workflow:sdncadapter:callback URN is not set', "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")
			}

			def l2HomingInformation = msoUtils.getNodeXml(request, 'l2-homing-information', false)
			def internetEvcAccessInformation = msoUtils.getNodeXml(request, 'internet-evc-access-information', false)
			def vrLan = msoUtils.getNodeXml(request, 'vr-lan', false)
			def upceVmsServiceInfo = msoUtils.getNodeXml(request, 'ucpe-vms-service-information', false)
			def vnfInformationList = msoUtils.getNodeXml(request, 'vnf-information-list', false)

			def nnsRequestInformation = msoUtils.removeXmlNamespaces(requestInformation)
			def nnsServiceInformation = msoUtils.removeXmlNamespaces(serviceInformation)
			def nnsl2HomingInformation = msoUtils.removeXmlNamespaces(l2HomingInformation)
			def nnsInternetEvcAccessInformation = msoUtils.removeXmlNamespaces(internetEvcAccessInformation)
			def nnsVrLan = msoUtils.removeXmlNamespaces(vrLan)
			def nnsUpceVmsServiceInfo = msoUtils.removeXmlNamespaces(upceVmsServiceInfo)
			def nnsVnfInformationList = msoUtils.removeXmlNamespaces(vrLan)
			def nnsinternetSvcChangeDetails = ""

			if(requestAction!=null && requestAction.equals("ChangeLayer3ServiceProvRequest")){
				def internetSvcChangeDetails = msoUtils.removeXmlNamespaces(serviceParameters)
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
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>${MsoUtils.xmlEscape(svcAction)}</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(svcOperation)}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>${MsoUtils.xmlEscape(msoAction)}</sdncadapter:MsoAction>
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

			content = msoUtils.removeXmlPreamble(msoUtils.formatXml(content))
			execution.setVariable(resultVar, content)
			logger.debug(resultVar + ' = ' + System.lineSeparator() + content)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error")
		}
	}

	public String updateHomingInfo(String homingInfo, String aicVersion) {
		if(homingInfo == null || homingInfo.trim().length() == 0) {
		}
		else {
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
	 public String sdncTopologyRequestV2 (DelegateExecution execution, String requestXML, String serviceInstanceId, String callbackUrl, String action, String requestAction, String cloudRegionId, networkId, L3Network queryAAIResponse, String additionalData) {

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
			 requestId = msoUtils.getNodeText(requestXML, "request-id")
		 }

		 String aicCloudRegion = cloudRegionId
		 String tenantId = ""
		 if (msoUtils.nodeExists(requestXML, "tenant-id")) {
			 tenantId = msoUtils.getNodeText(requestXML, "tenant-id")
		 }
		 String networkType = ""
		 if (msoUtils.nodeExists(requestXML, "network-type")) {
			 networkType = msoUtils.getNodeText(requestXML, "network-type")
		 }

		 // Replace/Use the value of network-type from aai query (vs input) during Delete Network flows.
		 if (queryAAIResponse != null) {
		     networkType = queryAAIResponse.getNetworkType()
		 }

		 String serviceId = ""
		 if (msoUtils.nodeExists(requestXML, "service-id")) {
			 serviceId = msoUtils.getNodeText(requestXML, "service-id")
		 }
		 String networkName = ""
		 // Replace/Use the value of network-name from aai query (vs input) if it was already set in AAI
		 if (queryAAIResponse != null) {
			 networkName = queryAAIResponse.getNetworkName()
		 }
		 if (networkName.isEmpty() && msoUtils.nodeExists(requestXML, "network-name")) {
			 networkName = msoUtils.getNodeText(requestXML, "network-name")
		 }
		 String source = ""
		 if (msoUtils.nodeExists(requestXML, "source")) {
			 source = msoUtils.getNodeText(requestXML, "source")
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
		 }else{
			 serviceType = execution.getVariable("serviceType")
			 subscriberName = execution.getVariable("subscriberName")
		 }

		 String content =
			"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
		                                  xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
		                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
		                                  xmlns:ns5="http://org.onap/so/request/types/v1">
					   <sdncadapter:RequestHeader>
						  <sdncadapter:RequestId>${MsoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
						  <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
						  <sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						  <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
						  <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
					   </sdncadapter:RequestHeader>
					   <sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					            <request-action>${MsoUtils.xmlEscape(requestAction)}</request-action>
					            <source>${MsoUtils.xmlEscape(source)}</source>
					            <notification-url></notification-url>
					            <order-number></order-number>
					            <order-version></order-version>
					         </request-information>
					         <service-information>
					            <service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
								<service-type>${MsoUtils.xmlEscape(serviceType)}</service-type>
					            <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
					            <subscriber-name>${MsoUtils.xmlEscape(subscriberName)}</subscriber-name>
					         </service-information>
					         <network-request-information>
					            <network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
					            <network-type>${MsoUtils.xmlEscape(networkType)}</network-type>
					            <network-name>${MsoUtils.xmlEscape(networkName)}</network-name>
					            <tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
					            <aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
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
		  if (msoUtils.nodeExists(requestXML, "tenant-id")) {
			  tenantId = msoUtils.getNodeText(requestXML, "tenant-id")
		  }
		  String networkType = ""
		  if (msoUtils.nodeExists(requestXML, "network-type")) {
			  networkType = msoUtils.getNodeText(requestXML, "network-type")
		  }

		  String subscriptionServiceType = ""
		  if (msoUtils.nodeExists(requestXML, "subscription-service-type")) {
			  subscriptionServiceType = msoUtils.getNodeText(requestXML, "subscription-service-type")
		  }

		  String globalCustomerId = ""
		  if (msoUtils.nodeExists(requestXML, "global-customer-id")) {
			  globalCustomerId = msoUtils.getNodeText(requestXML, "global-customer-id")
		  }

		  String serviceId = ""
		  if (msoUtils.nodeExists(requestXML, "service-id")) {
			  serviceId = msoUtils.getNodeText(requestXML, "service-id")
		  }
		  String networkName = ""
		  if (msoUtils.nodeExists(requestXML, "network-name")) {
			  networkName = msoUtils.getNodeText(requestXML, "network-name")
		  }
		  String source = ""
		  if (msoUtils.nodeExists(requestXML, "source")) {
			  source = msoUtils.getNodeText(requestXML, "source")
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
		  String networkModelInfo = msoUtils.getNodeXml(requestXML, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
		  String modelInvariantUuid = msoUtils.getNodeText(networkModelInfo, "modelInvariantUuid") !=null ?
				  msoUtils.getNodeText(networkModelInfo, "modelInvariantUuid") : ""
		  String modelCustomizationUuid = msoUtils.getNodeText(networkModelInfo, "modelCustomizationUuid")  !=null ?
				  msoUtils.getNodeText(networkModelInfo, "modelCustomizationUuid")  : ""
		  String modelUuid = msoUtils.getNodeText(networkModelInfo, "modelUuid") !=null ?
				  msoUtils.getNodeText(networkModelInfo, "modelUuid") : ""
		  String modelVersion = msoUtils.getNodeText(networkModelInfo, "modelVersion") !=null ?
				  msoUtils.getNodeText(networkModelInfo, "modelVersion") : ""
		  String modelName = msoUtils.getNodeText(networkModelInfo, "modelName") !=null ?
				  msoUtils.getNodeText(networkModelInfo, "modelName") : ""

		 // service-information from 'networkModelInfo' // verify the DB Catalog response
		 String serviceModelInfo = msoUtils.getNodeXml(requestXML, "serviceModelInfo", false).replace("tag0:","").replace(":tag0","")
		 String serviceModelInvariantUuid = msoUtils.getNodeText(serviceModelInfo, "modelInvariantUuid")  !=null ?
				 msoUtils.getNodeText(serviceModelInfo, "modelInvariantUuid")  : ""
		 String serviceModelUuid = msoUtils.getNodeText(serviceModelInfo, "modelUuid") !=null ?
				 msoUtils.getNodeText(serviceModelInfo, "modelUuid") : ""
		 String serviceModelVersion = msoUtils.getNodeText(serviceModelInfo, "modelVersion") !=null ?
				 msoUtils.getNodeText(serviceModelInfo, "modelVersion") : ""
		 String serviceModelName = msoUtils.getNodeText(serviceModelInfo, "modelName") !=null ?
				 msoUtils.getNodeText(serviceModelInfo, "modelName") : ""


		  String content =
		  """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
   				                                    xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
                                                    xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
					   <sdncadapter:RequestHeader>
						  <sdncadapter:RequestId>${MsoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
						  <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
						  <sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						  <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
						  <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                          <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
					   </sdncadapter:RequestHeader>
   					   <sdncadapterworkflow:SDNCRequestData>
						   <request-information>
						      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						      <request-action>${MsoUtils.xmlEscape(requestAction)}</request-action>
						      <source>${MsoUtils.xmlEscape(source)}</source>
						      <notification-url></notification-url>
						      <order-number></order-number>
						      <order-version></order-version>
						   </request-information>
						   <service-information>
						      <service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
						      <subscription-service-type>${MsoUtils.xmlEscape(subscriptionServiceType)}</subscription-service-type>
							  <onap-model-information>
								   <model-invariant-uuid>${MsoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
								   <model-uuid>${MsoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
								   <model-version>${MsoUtils.xmlEscape(serviceModelVersion)}</model-version>
								   <model-name>${MsoUtils.xmlEscape(serviceModelName)}</model-name>
                              </onap-model-information>
						      <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
						      <global-customer-id>${MsoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
						      <subscriber-name>${MsoUtils.xmlEscape(subscriberName)}</subscriber-name>
						   </service-information>
						   <network-information>
						      <network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
						      <network-type>${MsoUtils.xmlEscape(networkType)}</network-type>
							  <onap-model-information>
								   <model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
								   <model-customization-uuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
								   <model-uuid>${MsoUtils.xmlEscape(modelUuid)}</model-uuid>
								   <model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
								   <model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
							  </onap-model-information>
						   </network-information>
						   <network-request-input>
						     <network-name>${MsoUtils.xmlEscape(networkName)}</network-name>
					         <tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
						     <aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
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
		logger.debug("SDNC Response is: " + response)
		logger.debug("SuccessIndicator is: " + successIndicator)

		try {
			def prefix = execution.getVariable('prefix')
			execution.setVariable(prefix+'sdncResponseSuccess', false)
			logger.debug("Response" + ' = ' + (response == null ? "" : System.lineSeparator()) + response)

			if (successIndicator){
				if (response == null || response.trim().equals("")) {
					logger.debug(response + ' is empty');
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "SDNCAdapter Workflow Response is Empty")
				}else{

					// we need to peer into the request data for error
					def String sdncAdapterWorkflowResponse = msoUtils.getNodeXml(response, 'response-data', false)
					def String decodedXml = sdncAdapterWorkflowResponse.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
					decodedXml = msoUtils.getNodeXml(response, 'RequestData')
					logger.debug("decodedXml:\n" + decodedXml)

					int requestDataResponseCode = 200
					def String requestDataResponseMessage = ''

					try{
						if (msoUtils.nodeExists(decodedXml, "response-message")) {
							requestDataResponseMessage = msoUtils.getNodeText(decodedXml, "response-message")
						} else if (msoUtils.nodeExists(sdncAdapterWorkflowResponse, "ResponseMessage")) {
							requestDataResponseMessage = msoUtils.getNodeText(sdncAdapterWorkflowResponse, "ResponseMessage")
						}
					}catch(Exception e){
						logger.debug('Error caught while decoding resposne ' + e.getMessage())
					}

					if(msoUtils.nodeExists(decodedXml, "response-code")) {
						logger.debug("response-code node Exist ")
						String code = msoUtils.getNodeText(decodedXml, "response-code")
						if(code.isEmpty() || code.equals("")){
							// if response-code is blank then Success
							logger.debug("response-code node is empty")
							requestDataResponseCode = 0
						}else{
							requestDataResponseCode  = code.toInteger()
							logger.debug("response-code is: " + requestDataResponseCode)
						}
					}else if(msoUtils.nodeExists(sdncAdapterWorkflowResponse, "ResponseCode")){
						logger.debug("ResponseCode node Exist ")
						String code = msoUtils.getNodeText(sdncAdapterWorkflowResponse, "ResponseCode")
						if(code.isEmpty() || code.equals("")){
							// if ResponseCode blank then Success
							logger.debug("ResponseCode node is empty")
							requestDataResponseCode = 0
						}else{
							requestDataResponseCode  = code.toInteger()
							logger.debug("ResponseCode is: " + requestDataResponseCode)
						}
					}else{
						logger.debug("A Response Code DOES NOT Exist.")
						// if a response code does not exist then Success
						requestDataResponseCode = 0
					}
					try{

						execution.setVariable(prefix+'sdncRequestDataResponseCode', requestDataResponseCode.toString())
						// if a response code is 0 or 2XX then Success
						if ((requestDataResponseCode >= 200 && requestDataResponseCode <= 299) || requestDataResponseCode == 0) {
							execution.setVariable(prefix+'sdncResponseSuccess', true)
							logger.debug("Setting sdncResponseSuccess to True ")
							logger.debug("Exited ValidateSDNCResponse Method")
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

					logger.debug("SDNC callback response-code: " + requestDataResponseCode)
					logger.debug("SDNC callback response-message: " + requestDataResponseMessage)
				}

			}else {
				logger.debug('SDNCAdapter Subflow did NOT complete Successfully.  SuccessIndicator is False. ')
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
			logger.debug('END of Validate SDNC Response')
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
				logger.trace('Entered ' + method)
				def prefix = execution.getVariable('prefix')
				TrinityExceptionUtil trinityExceptionUtil = new TrinityExceptionUtil()

				try {
					execution.setVariable(prefix+'sdncResponseSuccess', false)

					logger.debug("sdncAdapter Success Indicator is: " + success)
					if (success) {

						// we need to look inside the request data for error
						def String callbackRequestData = msoUtils.getNodeXml(response, 'RequestData', false)
						def String decodedXml = callbackRequestData
						logger.debug("decodedXml:\n" + decodedXml)

						def requestDataResponseCode = '200'
						def requestDataResponseMessage = ''
						int intDataResponseCode = 200

						if (msoUtils.nodeExists(decodedXml, "response-code")) {

							requestDataResponseCode  = ((String) msoUtils.getNodeText(decodedXml, "response-code"))
							if (msoUtils.nodeExists(decodedXml, "response-message")) {
								requestDataResponseMessage  = msoUtils.getNodeText(decodedXml, "response-message")
							}
						}else if(msoUtils.nodeExists(decodedXml, "ResponseCode")){
							requestDataResponseCode  = ((String) msoUtils.getNodeText(decodedXml, "ResponseCode")).toInteger()
						}else if(msoUtils.nodeExists(response, "ResponseCode")){
							requestDataResponseCode  = ((String) msoUtils.getNodeText(response, "ResponseCode")).toInteger()
							requestDataResponseMessage  = msoUtils.getNodeText(response, "ResponseMessage")
						}

						logger.debug("SDNC callback response-code: " + requestDataResponseCode)
						logger.debug("SDNC callback response-message: " + requestDataResponseMessage)

						// Get the AAI Status to determine if rollback is needed on ASSIGN
						def aai_status = ''
						if (msoUtils.nodeExists(decodedXml, "aai-status")) {
							aai_status = ((String) msoUtils.getNodeText(decodedXml, "aai-status"))
							logger.debug("SDNC sent AAI STATUS code: " + aai_status)
						}
						if (aai_status != null && !aai_status.equals("")) {
							execution.setVariable(prefix+"AaiStatus",aai_status)
							logger.debug("Set variable " + prefix + "AaiStatus: " + execution.getVariable(prefix+"AaiStatus"))
						}

						// Get the result string to determine if rollback is needed on ASSIGN in Add Bonding flow only
						def sdncResult = ''
						if (msoUtils.nodeExists(decodedXml, "result")) {
							sdncResult = ((String) msoUtils.getNodeText(decodedXml, "result"))
							logger.debug("SDNC sent result: " + sdncResult)
						}
						if (sdncResult != null && !sdncResult.equals("")) {
							execution.setVariable(prefix+"SdncResult",sdncResult)
							logger.debug("Set variable " + prefix + "SdncResult: " + execution.getVariable(prefix+"SdncResult"))
						}

						try{
							intDataResponseCode = Integer.parseInt(String.valueOf(requestDataResponseCode))
						}catch(Exception e){
							intDataResponseCode = 400
						}

						logger.debug("intDataResponseCode " + intDataResponseCode )

						// if response-code is not Success (200, 201, etc) we need to throw an exception
						if ((intDataResponseCode < 200 || intDataResponseCode > 299) && intDataResponseCode != 0) {
							execution.setVariable(prefix+'ResponseCode', intDataResponseCode)
							execution.setVariable("L3HLAB_rollback", true)
							def msg = trinityExceptionUtil.mapSDNCAdapterExceptionToErrorResponse(response, execution)
							exceptionUtil.buildAndThrowWorkflowException(execution, intDataResponseCode, "Received error from SDN-C: " + msg)

						}
					}else {
						logger.warn(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_WARNING,
								'sdncAdapter did not complete successfully, sdncAdapter Success Indicator was false ',
								"BPMN", ErrorCode.UnknownError,
								'sdncAdapter did not complete successfully, sdncAdapter Success Indicator was false ')
						execution.setVariable("L3HLAB_rollback", true)
						def msg = trinityExceptionUtil.intDataResponseCode(response, execution)
						exceptionUtil.buildAndThrowWorkflowException(execution, intDataResponseCode, msg)
					}

					if (response == null || response.trim().equals("")) {
						logger.warn(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_WARNING,
								'sdncAdapter workflow response is empty', "BPMN",
								ErrorCode.UnknownError, 'sdncAdapter workflow response is empty')
						execution.setVariable("L3HLAB_rollback", true)
						def msg = trinityExceptionUtil.buildException("Exception occurred while validating SDNC response " , execution)
						exceptionUtil.buildAndThrowWorkflowException(execution, intResponseCode, msg)
					}

					execution.setVariable(prefix+'sdncResponseSuccess', true)
					logger.trace('Exited ' + method)
				} catch (BpmnError e) {
					throw e;
				} catch (Exception e) {
					logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught ' +
							'exception in ' + method, "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
			modelCustomizationString = "<model-customization-uuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>"
		}
		String modelVersion = jsonUtil.getJsonValue(jsonModelInfo, "modelVersion")
		if (modelVersion == null) {
			modelVersion = ""
		}
		String modelName = jsonUtil.getJsonValue(jsonModelInfo, "modelName")
		String ecompModelInformation =
				"""<onap-model-information>
						<model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
						<model-uuid>${MsoUtils.xmlEscape(modelUuid)}</model-uuid>
						${modelCustomizationString}
						<model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
						<model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
				</onap-model-information>"""

		return ecompModelInformation
	}
}
