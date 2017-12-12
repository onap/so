/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.workflowmessage;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.entity.ContentType;

import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

/**
 * Workflow Message Adapter interface added in 1707.  Supports delivery of
 * callbacks from external systems to waiting BPMN workflows.
 */
@Path("/")
public class WMAdapterRest {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	@HEAD
	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck(@QueryParam("requestId") String requestId) {
		long startTime = System.currentTimeMillis();
		MsoLogger.setServiceName("Healthcheck");
		UUIDChecker.verifyOldUUID(requestId, LOGGER);
		HealthCheckUtils healthCheck = new HealthCheckUtils();

		if (!healthCheck.siteStatusCheck(LOGGER)) {
			return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}

		if (!healthCheck.configFileCheck(LOGGER, startTime, WMAdapterConstants.MSO_PROPERTIES_ID)) {
			return HealthCheckUtils.NOT_STARTED_RESPONSE;
		}

		LOGGER.debug("healthcheck - Successful");
		return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
	}

	/**
	 * Receives a message from a remote system.
	 * @param content the message content
	 */
	@POST
	@Path("/message/{messageType}/{correlator}")
	@Consumes("*/*")
	@Produces({MediaType.TEXT_PLAIN})
	public Response receiveWorkflowMessage(
			@HeaderParam("Content-Type") String contentTypeHeader,
			@PathParam("messageType") String messageType,
			@PathParam("correlator") String correlator,
			String content) {

		String path= "workflow/" + messageType + "/" + correlator;
		LOGGER.info(MessageEnum.RA_RECEIVE_WORKFLOW_MESSAGE, content, "WorkflowMessageAdapter", path);

		long startTime = System.currentTimeMillis();

		ContentType contentType = null;

		if (contentTypeHeader != null) {
			try {
				contentType = ContentType.parse(contentTypeHeader);
			} catch (Exception e) {
				// If we don't get a valid one, we handle it below.
				LOGGER.debug("Exception :",e);
			}
		}

		if (contentType == null && content != null) {
			String error = "Missing or Invalid Content-Type";
			LOGGER.error(MessageEnum.RA_PARSING_REQUEST_ERROR, error, "WorkflowMessageAdapter", path,
				MsoLogger.ErrorCode.DataError, "Bad Request");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return Response.status(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE).entity(error).build();
		}

		String bpUrl = WMAdapterProperties.getProperty(WMAdapterConstants.BPEL_URL_PROP, null);

		if (bpUrl == null) {
			String error = "Missing configuration for: " + WMAdapterConstants.BPEL_URL_PROP;
			LOGGER.error(MessageEnum.RA_CONFIG_EXC, error, "WorkflowMessageAdapter", path,
				MsoLogger.ErrorCode.DataError, "Configuration Error");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(error).build();
		}

		long bpStartTime = System.currentTimeMillis();
		BPRestCallback callback = new BPRestCallback();
		boolean callbackSuccess = callback.send(bpUrl, messageType, correlator, contentType, content);

		if (callbackSuccess) {
			LOGGER.recordMetricEvent(bpStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Sent notification", "BPMN", bpUrl, null);
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
		} else {
			LOGGER.recordMetricEvent(bpStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
				"Failed to send notification", "BPMN", bpUrl, null);
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
				"Failed to send notification");
		}

		return Response.status(204).build();
	}
}
