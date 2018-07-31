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

package org.onap.so.bpmn.common.workflow.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackError;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackResult;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Generalized REST interface that injects a message event into a waiting BPMN process.
 * Examples:
 * <pre>
 *     /WorkflowMessage/SDNCAResponse/6d10d075-100c-42d0-9d84-a52432681cae-1478486185286
 *     /WorkflowMessage/SDNCAEvent/USOSTCDALTX0101UJZZ01
 * </pre>
 */
@Path("/")
@Api(description = "Provides a generic service to inject messages into a waiting BPMN Proccess")
@Component
public class WorkflowMessageResource{
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, WorkflowMessageResource.class);
	private static final String LOGMARKER = "[WORKFLOW-MESSAGE]";
	
	@Autowired
	CallbackHandlerService callback;
	
	@POST
	@Path("/WorkflowMessage/{messageType}/{correlator}")
	@ApiOperation(
	        value = "Workflow message correlator",
	        notes = ""
	    )
	@Consumes("*/*")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deliver(
			@HeaderParam("Content-Type") String contentType,
			@PathParam("messageType") String messageType,
			@PathParam("correlator") String correlator,
			String message) {

		String method = "receiveWorkflowMessage";
		MsoLogger.setServiceName("MSO." + method);
		MsoLogger.setLogContext(correlator, "N/A");

		LOGGER.debug(LOGMARKER + " Received workflow message"
			+ " type='" + messageType + "'"
			+ " correlator='" + correlator + "'"
			+ (contentType == null ? "" : " contentType='" + contentType + "'")
			+ " message=" + System.lineSeparator() + message);

		if (messageType == null || messageType.isEmpty()) {
			String msg = "Missing message type";
			LOGGER.debug(LOGMARKER + " " + msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.DataError, LOGMARKER + ":" + msg);
			return Response.status(400).entity(msg).build();
		}

		if (correlator == null || correlator.isEmpty()) {
			String msg = "Missing correlator";
			LOGGER.debug(LOGMARKER + " " + msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.DataError, LOGMARKER + ":" + msg);
			return Response.status(400).entity(msg).build();
		}

		String messageEventName = "WorkflowMessage";
		String messageVariable = messageType + "_MESSAGE";
		String correlationVariable = messageType + "_CORRELATOR";
		String correlationValue = correlator;
		String contentTypeVariable = messageType + "_CONTENT_TYPE";

		Map<String, Object> variables = new HashMap<>();

		if (contentType != null) {
			variables.put(contentTypeVariable, contentType);
		}

		CallbackResult result = callback.handleCallback(method, message, messageEventName,
			messageVariable, correlationVariable, correlationValue, LOGMARKER, variables);

		if (result instanceof CallbackError) {
			return Response.status(500).entity(((CallbackError)result).getErrorMessage()).build();
		} else {
			return Response.status(204).build();
		}
	}
}