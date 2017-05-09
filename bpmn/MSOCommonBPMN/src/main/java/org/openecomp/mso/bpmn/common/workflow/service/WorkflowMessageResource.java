package org.openecomp.mso.bpmn.common.workflow.service;

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

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Generalized REST interface that injects a message event into a waiting BPMN process.
 * Examples:
 * <pre>
 *     /WorkflowMessage/SDNCAResponse/6d10d075-100c-42d0-9d84-a52432681cae-1478486185286
 *     /WorkflowMessage/SDNCAEvent/USOSTCDALTX0101UJZZ01
 * </pre>
 */
@Path("/")
public class WorkflowMessageResource extends AbstractCallbackService {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static final String LOGMARKER = "[WORKFLOW-MESSAGE]";
	
	@POST
	@Path("/WorkflowMessage/{messageType}/{correlator}")
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

		Map<String, Object> variables = new HashMap<String, Object>();

		if (contentType != null) {
			variables.put(contentTypeVariable, contentType);
		}

		CallbackResult result = handleCallback(method, message, messageEventName,
			messageVariable, correlationVariable, correlationValue, LOGMARKER, variables);

		if (result instanceof CallbackError) {
			return Response.status(500).entity(((CallbackError)result).getErrorMessage()).build();
		} else {
			return Response.status(204).build();
		}
	}
}