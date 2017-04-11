package org.openecomp.mso.bpmn.common.workflow.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;

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
public class WorkflowMessageResource {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static final String LOGMARKER = "[WORKFLOW-MESSAGE]";

	private ProcessEngineServices pes4junit = null;
	
	@POST
	@Path("/WorkflowMessage/{messageType}/{correlator}")
	@Consumes("*/*")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deliver(@PathParam("messageType") String messageType,
			@PathParam("correlator") String correlator, String message) {

		LOGGER.debug(LOGMARKER + " Received workflow message"
			+ " type='" + messageType + "'"
			+ " correlator='" + correlator + "'"
			+ System.lineSeparator() + message);

		MsoLogger.setServiceName("MSO." + "WorkflowMessage");

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

		String correlatorVariable = messageType + "_CORRELATOR";
		String messageVariable = messageType + "_MESSAGE";

		long startTime = System.currentTimeMillis();

		try {
			RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();

			if (!isReadyforCorrelation(runtimeService, correlatorVariable, correlator)) {
				String msg = "No process is waiting to receive '" + messageType + "' WorkflowMessage with correlator '" + correlator + "'";
				LOGGER.debug(LOGMARKER + " " + msg);
				LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, LOGMARKER + ":" + msg);
				LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BusinessProcesssError, msg, "BPMN", MsoLogger.getServiceName(), messageType);
				LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BusinessProcesssError, msg);
				return Response.status(500).entity(msg).build();
			}

			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put(correlatorVariable, correlator);
			variables.put(messageVariable, message);

			runtimeService.createMessageCorrelation("WorkflowMessage").setVariables(variables)
				.processInstanceVariableEquals(correlatorVariable, correlator).correlate();

			String msg = "Completed delivery of '" + messageType + "' WorkflowMessage with correlator '" + correlator + "'";
			LOGGER.debug(LOGMARKER + msg);
			LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, msg, "BPMN", MsoLogger.getServiceName(), messageType);
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, msg);
			return Response.status(204).build();
		} catch (Exception e) {
			// For example: MismatchingMessageCorrelationException
			String msg = "Caught " + e.getClass().getSimpleName() + " processing '" + messageType + "' WorkflowMessage with " + correlatorVariable + "='" + correlator + "'";
			LOGGER.debug(LOGMARKER + " " + msg);
			LOGGER.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, LOGMARKER + ":" + msg, e);
			LOGGER.recordMetricEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, msg, "BPMN", MsoLogger.getServiceName(), messageType);
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, msg);
			return Response.status(500).entity(msg).build();
		}
	}
	
	private boolean isReadyforCorrelation(RuntimeService runtimeService,
			String correlationVariable, String correlationValue)
			throws InterruptedException {

		long waitingInstances = runtimeService.createExecutionQuery()
			.messageEventSubscriptionName("WorkflowMessage")
			.processVariableValueEquals(correlationVariable, correlationValue)
			.count();

		int retries = 50;
		while (waitingInstances == 0 && retries > 0) {
			Thread.sleep(100);

			waitingInstances = runtimeService.createExecutionQuery()
				.messageEventSubscriptionName("WorkflowMessage")
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