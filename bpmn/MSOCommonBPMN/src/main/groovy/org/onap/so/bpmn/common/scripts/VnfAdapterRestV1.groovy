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

import org.onap.so.logger.LoggingAnchor
import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ErrorCode

import javax.ws.rs.core.Response
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.HttpClient
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.logging.filter.base.ONAPComponents;
import java.util.UUID
import org.onap.so.utils.Components


@Deprecated //Use vnfAdapterRestV2
class VnfAdapterRestV1 extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( VnfAdapterRestV1.class);


    ExceptionUtil exceptionUtil = new ExceptionUtil()

    // VNF Response Processing
    public void preProcessRequest (DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessRequest(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        def prefix="VNFREST_"
        execution.setVariable("prefix", prefix)
        setSuccessIndicator(execution, false)

        try {
            String request = validateRequest(execution, "mso-request-id")

            // Get the request type (the name of the root element) from the request

            Node root = new XmlParser().parseText(request)
            String requestType = root.name()
            execution.setVariable(prefix + 'requestType', requestType)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'requestType = ' + requestType)

            logger.debug('VnfAdapterRestV1, request: ' + request)
            // Get the messageId from the request

            String messageId = getChildText(root, 'messageId')

            if ('rollbackVolumeGroupRequest'.equals(requestType)) {
                messageId = getMessageIdForVolumeGroupRollback(root)
            }

            if (messageId == null || messageId.isEmpty()) {
                String msg = getProcessKey(execution) + ': no messageId in ' + requestType
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
            }

            execution.setVariable('VNFAResponse_CORRELATOR', messageId)
            logger.debug(getProcessKey(execution) + ': VNFAResponse_CORRELATOR = ' + messageId)

            // Get the notificationUrl from the request

            String notificationUrl = getChildText(root, 'notificationUrl')

            if (notificationUrl == null || notificationUrl.isEmpty()) {
                String msg = getProcessKey(execution) + ': no notificationUrl in ' + requestType
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
            }

            execution.setVariable(prefix + 'notificationUrl', notificationUrl)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'notificationUrl = ' + notificationUrl)

            // Determine the VnfAdapter endpoint

            String vnfAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.vnf.rest.endpoint", execution)

            if (vnfAdapterEndpoint == null || vnfAdapterEndpoint.isEmpty()) {
                String msg = getProcessKey(execution) + ': mso:adapters:vnf:rest:endpoint URN mapping is not defined'
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
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
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                vnfAdapterMethod = 'POST'
                vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') + '/vf-modules'

            } else if ('updateVfModuleRequest'.equals(requestType)) {
                String vnfId = getChildText(root, 'vnfId')

                if (vnfId == null || vnfId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                String vfModuleId = getChildText(root, 'vfModuleId')

                if (vfModuleId == null || vfModuleId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                vnfAdapterMethod = 'PUT'
                vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') +
                        '/vf-modules/' + URLEncoder.encode(vfModuleId, 'UTF-8')

            } else if ('deleteVfModuleRequest'.equals(requestType)) {
                String vnfId = getChildText(root, 'vnfId')

                if (vnfId == null || vnfId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                String vfModuleId = getChildText(root, 'vfModuleId')

                if (vfModuleId == null || vfModuleId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                vnfAdapterMethod = 'DELETE'
                vnfAdapterUrl = vnfAdapterEndpoint + '/' + URLEncoder.encode(vnfId, 'UTF-8') +
                        '/vf-modules/' + URLEncoder.encode(vfModuleId, 'UTF-8')

            } else if ('rollbackVfModuleRequest'.equals(requestType)) {
                Node vfModuleRollbackNode = getChild(root, 'vfModuleRollback')

                if (vfModuleRollbackNode == null) {
                    String msg = getProcessKey(execution) + ': no vfModuleRollback in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                String vnfId = getChildText(vfModuleRollbackNode, 'vnfId')

                if (vnfId == null || vnfId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vnfId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                String vfModuleId = getChildText(vfModuleRollbackNode, 'vfModuleId')

                if (vfModuleId == null || vfModuleId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no vfModuleId in ' + requestType
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
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
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
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
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
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
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue());
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
                }

                vnfAdapterMethod = 'DELETE'
                if (vnfAdapterEndpoint.endsWith('v1/vnfs')) {
                    vnfAdapterEndpoint = vnfAdapterEndpoint.substring(0, (vnfAdapterEndpoint.length()-'/vnfs'.length()))
                }
                vnfAdapterUrl = vnfAdapterEndpoint + '/volume-groups/' + URLEncoder.encode(volumeGroupId, 'UTF-8')  + '/rollback'

            } else {
                String msg = getProcessKey(execution) + ': Unsupported request type: ' + requestType
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
            }

            execution.setVariable(prefix + 'vnfAdapterMethod', vnfAdapterMethod)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterMethod = ' + vnfAdapterMethod)
            execution.setVariable(prefix + 'vnfAdapterUrl', vnfAdapterUrl)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterUrl = ' + vnfAdapterUrl)
            execution.setVariable(prefix + 'vnfAdapterRequest', vnfAdapterRequest)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'vnfAdapterRequest = \n' + vnfAdapterRequest)

            // Get the Basic Auth credentials for the VnfAdapter

            String basicAuthValue = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

            if (basicAuthValue == null || basicAuthValue.isEmpty()) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                        getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined", "BPMN",
                        ErrorCode.UnknownError.getValue());
            } else {
                try {
                    def encodedString = utils.getBasicAuth(basicAuthValue, UrnPropertiesReader.getVariable("mso.msoKey", execution))
                    execution.setVariable(prefix + 'basicAuthHeaderValue', encodedString)
                } catch (IOException ex) {
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                            getProcessKey(execution) + ": Unable to encode BasicAuth credentials for VnfAdapter",
                            "BPMN", ErrorCode.UnknownError.getValue(), ex);
                }
            }

        } catch (BpmnError e) {
            logger.debug(" Rethrowing MSOWorkflowException")
            throw e
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
            logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
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
        logger.trace('Entered ' + method)

        String prefix = execution.getVariable('prefix')

        try {
            String vnfAdapterMethod = execution.getVariable(prefix + 'vnfAdapterMethod')
            String vnfAdapterUrl = execution.getVariable(prefix + 'vnfAdapterUrl')
            String vnfAdapterRequest = execution.getVariable(prefix + 'vnfAdapterRequest')

            URL url = new URL(vnfAdapterUrl);

			HttpClient httpClient = new HttpClientFactory().newXmlClient(url, ONAPComponents.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Authorization", execution.getVariable(prefix + "basicAuthHeaderValue"))

            httpClient.addAdditionalHeader("X-ONAP-RequestID", execution.getVariable("mso-request-id"))
            httpClient.addAdditionalHeader("X-ONAP-InvocationID", UUID.randomUUID().toString())
            httpClient.addAdditionalHeader("X-ONAP-PartnerName", "SO-VNFAdapter")
            Response response;

            if ("GET".equals(vnfAdapterMethod)) {
                response = httpClient.get()
            } else if ("PUT".equals(vnfAdapterMethod)) {
                response = httpClient.put(vnfAdapterRequest)
            } else if ("POST".equals(vnfAdapterMethod)) {
                response = httpClient.post(vnfAdapterRequest)
            } else if ("DELETE".equals(vnfAdapterMethod)) {
                response = httpClient.delete(vnfAdapterRequest)
            } else {
                String msg = 'Unsupported HTTP method "' + vnfAdapterMethod + '" in ' + method + ": " + e
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
            }

            execution.setVariable(prefix + "vnfAdapterStatusCode", response.getStatus())
            if(response.hasEntity()){
                execution.setVariable(prefix + "vnfAdapterResponse", response.readEntity(String.class))
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg, ONAPComponents.SO)
        }
    }

    public void processCallback(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.processCallback(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        String callback = execution.getVariable('VNFAResponse_MESSAGE')

        try {
            logger.debug(getProcessKey(execution) + ": received callback:\n" + callback)

            // The XML callback is available to the calling flow in any case,
            // even if a WorkflowException is generated.
            execution.setVariable(getProcessKey(execution) + 'Response', callback)
            // TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead.
            execution.setVariable("WorkflowResponse", callback)

            callback = utils.removeXmlPreamble(callback)

            Node root = new XmlParser().parseText(callback)
            if (root.name().endsWith('Exception')) {
                vnfAdapterWorkflowException(execution, callback)
            }
        } catch (Exception e) {
            logger.debug("Error encountered within VnfAdapterRest ProcessCallback method: {}", e.getMessage(), e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7020, "Error encountered within VnfAdapterRest ProcessCallback method", ONAPComponents.SO)
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
				" from VnfAdapter:" + category + message + rolledBack, Components.OPENSTACK);
        } catch (Exception e) {
            response = response == null || String.valueOf(response).isEmpty() ? "NONE" : response
			exceptionUtil.buildWorkflowException(execution, 7020, "Received error from VnfAdapter: " + response, Components.OPENSTACK)
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

    public Logger getLogger() {
        return logger;
    }
}
