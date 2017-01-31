/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.gamma.workflow.service;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Listens for REST notifications from the VNF Adapter and injects each one
 * into a waiting BPMN processes.
 */
@Path("/vnfAdapterRestNotify")
public class VnfAdapterRestNotifyResource {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static final String LOGMARKER = "[VNF-REST-NOTIFY]";

	private ProcessEngineServices pes4junit = null;
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response notify(String content) {
		LOGGER.debug(LOGMARKER + " Received VNF Adapter REST Notification:"
			+ System.lineSeparator() + content);

		String messageId = null;
		long startTime = System.currentTimeMillis();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource(new StringReader(content));
			Document doc = builder.parse(source);
			doc.normalize();

			Element rootElement = doc.getDocumentElement();
			NodeList childList = rootElement.getChildNodes();

			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) childNode;

					String childElementName = childElement.getLocalName();
					if (childElementName == null) {
						childElementName = childElement.getNodeName();
					}

					if ("messageId".equals(childElementName)) {
						messageId = childElement.getTextContent();
					}
				}
			}
		} catch (Exception e) {
			String msg = "Failed to parse VNF Adapter REST Notification: " + e;
			LOGGER.debug(LOGMARKER + " " + msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, LOGMARKER + ":" + msg, e);
			
			return Response.status(400).entity(e).build();
		}

		if (messageId == null || messageId.isEmpty()) {
			String msg = "No messageId in VNF Adapter REST Notification";
			LOGGER.debug(LOGMARKER + " " + msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, LOGMARKER + ":" + msg);
			
			return Response.status(400).entity(msg).build();
		}

		MsoLogger.setServiceName("MSO." + "vnfAdapterRestNotify");
		MsoLogger.setLogContext(messageId, "N/A");

		LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO vnfAdapterRestNotify ");

		try {
			ProcessEngineServices pes = getProcessEngineServices();
			RuntimeService runtimeService = pes.getRuntimeService();

			if (!isReadyforCorrelation(runtimeService, "VNFREST_messageId", messageId, "vnfAdapterRestCallbackMessage")) {
				String msg = "No process is waiting to receive vnfAdapterRestCallbackMessage with VNFREST_messageId='" + messageId + "'";
				LOGGER.debug(LOGMARKER + " " + msg);
				LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, LOGMARKER + ":" + msg);
				
				LOGGER.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
						LOGMARKER + "Call to MSO vnfAdapterRestNotify ", "BPMN", MsoLogger.getServiceName(), "vnfAdapterRestNotify");
				
				LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO VnfAdapterNotifyService ");
				
				
				return Response.status(500).entity(msg).build();
			}

			Map<String,Object> variables = new HashMap<String,Object>();
			variables.put("VNFREST_messageId", messageId);
			variables.put("VNFREST_callback", content);

			runtimeService.createMessageCorrelation("vnfAdapterRestCallbackMessage").setVariables(variables)
				  .processInstanceVariableEquals("VNFREST_messageId", messageId).correlate();

			LOGGER.debug(LOGMARKER + " Completed processing of VNF Adapter REST Notification");
		} catch (MismatchingMessageCorrelationException e) {
			LOGGER.debug(LOGMARKER + "[CORM] correlation id mismatch");
			String msg = "vnfAdapterRestNotify received a notification with messageId='"
				+ messageId + "' but it could not be correlated to any active process - ignoring the request";
			LOGGER.debug(msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, LOGMARKER, e);
			
			LOGGER.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.InternalError, 
					LOGMARKER + "Completed vnfAdapterRestNotify with error ", "BPMN", MsoLogger.getServiceName(), "vnfAdapterRestNotify");
			
			LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.InternalError, "Completed vnfAdapterRestNotify with error ");
			
			return Response.status(500).entity(msg).build();
		}
		LOGGER.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				LOGMARKER + "Completed vnfAdapterRestNotify", "BPMN", MsoLogger.getServiceName(), "vnfAdapterRestNotify");
		
		LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Completed vnfAdapterRestNotify");
		
		return Response.status(204).build();
	}
	
	private boolean isReadyforCorrelation(RuntimeService runtimeService,
			String correlationVariable, String correlationValue, String messageName) {
		long waitingInstances = runtimeService.createExecutionQuery()
			.messageEventSubscriptionName(messageName)
			.processVariableValueEquals(correlationVariable, correlationValue)
			.count();

		int retries = 50;
		while (waitingInstances == 0 && retries > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, LOGMARKER, e);
				
				return false;
			}

			waitingInstances = runtimeService.createExecutionQuery()
				.messageEventSubscriptionName(messageName)
				.processVariableValueEquals(correlationVariable, correlationValue)
				.count();

			retries--;
		}
		
		return waitingInstances != 0;
	}
	
	private ProcessEngineServices getProcessEngineServices() {
		if (pes4junit == null) {
			return BpmPlatform.getDefaultProcessEngine();
		} else {
			return pes4junit;
		}
	}

	public void setProcessEngineServices4junit(ProcessEngineServices pes) {
		pes4junit = pes;
	}
}
