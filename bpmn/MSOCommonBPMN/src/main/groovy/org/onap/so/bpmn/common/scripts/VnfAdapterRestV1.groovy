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

package org.onap.so.bpmn.common.scripts

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger



class VnfAdapterRestV1 extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VnfAdapterRestV1.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()

	// VNF Response Processing
	public void preProcessRequest (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		def prefix="VNFREST_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)

		try {
			String request = validateRequest(execution, "mso-request-id")

			// Get the request type (the name of the root element) from the request

			Node root = new XmlParser().parseText(request)
			String requestType = root.name()
			execution.setVariable(prefix + 'requestType', requestType)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'requestType = ' + requestType)

			msoLogger.debug('VnfAdapterRestV1, request: ' + request)
			// Get the messageId from the request

			String messageId = getChildText(root, 'messageId')

			if ('rollbackVolumeGroupRequest'.equals(requestType)) {
				messageId = getMessageIdForVolumeGroupRollback(root)
			}
			
			if (messageId == null || messageId.isEmpty()) {
				String msg = getProcessKey(execution) + ': no messageId in ' + requestType
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable('VNFAResponse_CORRELATOR', messageId)
			msoLogger.debug(getProcessKey(execution) + ': VNFAResponse_CORRELATOR = ' + messageId)

			// Get the notificationUrl from the request

			String notificationUrl = getChildText(root, 'notificationUrl')

			if (notificationUrl == null || notificationUrl.isEmpty()) {
				String msg = getProcessKey(execution) + ': no notificationUrl in ' + requestType
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + 'notificationUrl', notificationUrl)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'notificationUrl = ' + notificationUrl)

			// Determine the VnfAdapter endpoint

			String vnfAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.vnf.rest.endpoint", execution)

			if (vnfAdapterEndpoint == null || vnfAdapterEndpoint.isEmpty()) {
				String msg = getProcessKey(execution) + ': mso:adapters:vnf:rest:endpoint URN mapping is not defined'
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			while (vnfAdapterEndpoint.endsWith('/')) {
				vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, vnfAdapterEndpoint.length()-1)
			}

			String vnfAdapterMethod = null
			String vnfAdapterUrl = null
			String vnfAdapterRequest = request

			if ('createVfModuleRequest'.equals(requestType)) {
				String vnfId = getChildText(root, 'vnfId')

				if (vnfId == null || vnfId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'POST'
				vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') + '/vf-modules'

			} else if ('updateVfModuleRequest'.equals(requestType)) {
				String vnfId = getChildText(root, 'vnfId')

				if (vnfId == null || vnfId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				String vfModuleId = getChildText(root, 'vfModuleId')

				if (vfModuleId == null || vfModuleId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'PUT'
				vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') +
					'/vf-modules/' + URLEncoder.encode(vfModuleId, 'UTF-8')

			} else if ('deleteVfModuleRequest'.equals(requestType)) {
				String vnfId = getChildText(root, 'vnfId')

				if (vnfId == null || vnfId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				String vfModuleId = getChildText(root, 'vfModuleId')

				if (vfModuleId == null || vfModuleId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'DELETE'
				vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') +
					'/vf-modules/' + URLEncoder.encode(vfModuleId, 'UTF-8')

			} else if ('rollbackVfModuleRequest'.equals(requestType)) {
				Node vfModuleRollbackNode = getChild(root, 'vfModuleRollback')

				if (vfModuleRollbackNode == null) {
					String msg = getProcessKey(execution) + ': no vfModuleRollback in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				String vnfId = getChildText(vfModuleRollbackNode, 'vnfId')

				if (vnfId == null || vnfId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				String vfModuleId = getChildText(vfModuleRollbackNode, 'vfModuleId')

				if (vfModuleId == null || vfModuleId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'DELETE'
				vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') +
					'/vf-modules/' + URLEncoder.encode(vfModuleId, 'UTF-8') + '/rollback'

			} else if ('createVolumeGroupRequest'.equals(requestType)) {
				vnfAdapterMethod = 'POST'
				if (vnfAdapterEndpoint.endsWith('v1/vnfs')) {
					vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, (vnfAdapterEndpoint.length()-'/vnfs'.length()))
				}
				vnfAdapterUrl = vnfAdapterEndpoint + '/volume-groups'

			} else if ('updateVolumeGroupRequest'.equals(requestType)) {
				String volumeGroupId = getChildText(root, 'volumeGroupId')

				if (volumeGroupId == null || volumeGroupId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no volumeGroupId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'PUT'
				if (vnfAdapterEndpoint.endsWith('v1/vnfs')) {
					vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, (vnfAdapterEndpoint.length()-'/vnfs'.length()))
				}
				vnfAdapterUrl = vnfAdapterEndpoint + '/volume-groups/' + URLEncoder.encode(volumeGroupId, 'UTF-8')

			} else if ('deleteVolumeGroupRequest'.equals(requestType)) {
				String volumeGroupId = getChildText(root, 'volumeGroupId')

				if (volumeGroupId == null || volumeGroupId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no volumeGroupId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'DELETE'
				if (vnfAdapterEndpoint.endsWith('v1/vnfs')) {
					vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, (vnfAdapterEndpoint.length()-'/vnfs'.length()))
				}
				vnfAdapterUrl = vnfAdapterEndpoint + '/volume-groups/' + URLEncoder.encode(volumeGroupId, 'UTF-8')

			} else if ('rollbackVolumeGroupRequest'.equals(requestType)) {
				String volumeGroupId = getVolumeGroupIdFromRollbackRequest(root)

				if (volumeGroupId == null || volumeGroupId.isEmpty()) {
					String msg = getProcessKey(execution) + ': no volumeGroupId in ' + requestType
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				vnfAdapterMethod = 'DELETE'
				if (vnfAdapterEndpoint.endsWith('v1/vnfs')) {
					vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, (vnfAdapterEndpoint.length()-'/vnfs'.length()))
				}
				vnfAdapterUrl = vnfAdapterEndpoint + '/volume-groups/' + URLEncoder.encode(volumeGroupId, 'UTF-8')  + '/rollback'

			} else {
				String msg = getProcessKey(execution) + ': Unsupported request type: ' + requestType
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + 'vnfAdapterMethod', vnfAdapterMethod)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterMethod = ' + vnfAdapterMethod)
			execution.setVariable(prefix + 'vnfAdapterUrl', vnfAdapterUrl)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterUrl = ' + vnfAdapterUrl)
			execution.setVariable(prefix + 'vnfAdapterRequest', vnfAdapterRequest)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterRequest = \n' + vnfAdapterRequest)

			// Get the Basic Auth credentials for the VnfAdapter

			String basicAuthValue = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

			if (basicAuthValue == null || basicAuthValue.isEmpty()) {
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			} else {
				try {
					def encodedString = utils.getBasicAuth(basicAuthValue, UrnPropertiesReader.getVariable("mso.msoKey", execution))
					execution.setVariable(prefix + 'basicAuthHeaderValue', encodedString)
				} catch (IOException ex) {
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, getProcessKey(execution) + ": Unable to encode BasicAuth credentials for VnfAdapter", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				}
			}

		} catch (BpmnError e) {
			msoLogger.debug(" Rethrowing MSOWorkflowException")
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		}
	}
	
	public String getVolumeGroupIdFromRollbackRequest(Node root) {
		return root.'volumeGroupRollback'.'volumeGroupId'.text()
	}

	public String getMessageIdForVolumeGroupRollback(Node root) {
		return root.'volumeGroupRollback'.'messageId'.text()
	}
	
	/**
	 * This method is used instead of an HTTP Connector task because the
	 * connector does not allow DELETE with a body.
	 */
	public void sendRequestToVnfAdapter(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendRequestToVnfAdapter(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		String prefix = execution.getVariable('prefix')

		try {
			String vnfAdapterMethod = execution.getVariable(prefix + 'vnfAdapterMethod')
			String vnfAdapterUrl = execution.getVariable(prefix + 'vnfAdapterUrl')
			String vnfAdapterRequest = execution.getVariable(prefix + 'vnfAdapterRequest')

			RESTConfig config = new RESTConfig(vnfAdapterUrl)
			RESTClient client = new RESTClient(config).
				addHeader("Content-Type", "application/xml").
				addAuthorizationHeader(execution.getVariable(prefix + "basicAuthHeaderValue"));

			APIResponse response;

			if ("GET".equals(vnfAdapterMethod)) {
				response = client.httpGet()
			} else if ("PUT".equals(vnfAdapterMethod)) {
				response = client.httpPut(vnfAdapterRequest)
			} else if ("POST".equals(vnfAdapterMethod)) {
				response = client.httpPost(vnfAdapterRequest)
			} else if ("DELETE".equals(vnfAdapterMethod)) {
				response = client.httpDelete(vnfAdapterRequest)
			} else {
				String msg = 'Unsupported HTTP method "' + vnfAdapterMethod + '" in ' + method + ": " + e
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + "vnfAdapterStatusCode", response.getStatusCode())
			execution.setVariable(prefix + "vnfAdapterResponse", response.getResponseBodyAsString())
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		}
	}

	public void processCallback(DelegateExecution execution){
		def method = getClass().getSimpleName() + '.processCallback(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		String callback = execution.getVariable('VNFAResponse_MESSAGE')

		try {
			msoLogger.debug(getProcessKey(execution) + ": received callback:\n" + callback)

			// The XML callback is available to the calling flow in any case,
			// even if a WorkflowException is generated.
			execution.setVariable(getProcessKey(execution) + 'Response', callback)
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead.
			execution.setVariable("WorkflowResponse", callback)

			Node root = new XmlParser().parseText(callback)
			if (root.name().endsWith('Exception')) {
				vnfAdapterWorkflowException(execution, callback)
			}
		} catch (Exception e) {
			e.printStackTrace()
			callback = callback == null || String.valueOf(callback).isEmpty() ? "NONE" : callback
			String msg = "Received error from VnfAdapter: " + callback
			msoLogger.debug(getProcessKey(execution) + ': ' + msg)
			exceptionUtil.buildWorkflowException(execution, 7020, msg)
		}
	}

	/**
	 * Tries to parse the response as XML to extract the information to create
	 * a WorkflowException.  If the response cannot be parsed, a more generic
	 * WorkflowException is created.
	 */
	public void vnfAdapterWorkflowException(DelegateExecution execution, Object response) {
		try {
			Node root = new XmlParser().parseText(response)
			String category = getChildText(root, "category")
			category = category == null || category.isEmpty() ? "" : " category='" + category + "'"
			String message = getChildText(root, "message")
			message = message == null || message.isEmpty() ? "" : " message='" + message + "'"
			String rolledBack = getChildText(root, "rolledBack")
			rolledBack = rolledBack == null || rolledBack.isEmpty() ? "" : " rolledBack='" + rolledBack + "'"
			exceptionUtil.buildWorkflowException(execution, 7020, "Received " + root.name() +
				" from VnfAdapter:" + category + message + rolledBack);
		} catch (Exception e) {
			response = response == null || String.valueOf(response).isEmpty() ? "NONE" : response
			exceptionUtil.buildWorkflowException(execution, 7020, "Received error from VnfAdapter: " + response)
		}
	}

	/**
	 * Gets the named child of the specified node.
	 * @param node the node
	 * @param name the child name
	 * @return the child node, or null if no such child exists
	 */
	private Node getChild(Node node, String name) {
		for (Node child : node.children()) {
			if (child.name() == name) {
				return child
			}
		}
		return null
	}

	/**
	 * Gets the text of the named child of the specified node.
	 * @param node the node
	 * @param name the child name
	 * @return the child node text, or null if no such child exists
	 */
	private String getChildText(Node node, String name) {
		Node child = getChild(node, name)
		return child == null ? null : child.text()
	}
}
