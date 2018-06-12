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

package org.openecomp.mso.adapters.sdnc.sdncrest;

import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.adapters.sdncrest.SDNCEvent;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceError;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * SDNC REST adapter interface added in 1702 to support the SDNC "agnostic" API.
 */
@Component
@Path("/")
@Api(value = "/", description = "Root of SDNCAdapterRest")
public class SDNCAdapterRest {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, SDNCAdapterRest.class);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
	 
	public static final Response HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
	            .entity (CHECK_HTML)
	            .build ();

	@Autowired
	private Environment env;
	
	
	@Autowired
	private SDNCServiceRequestTask sdncServiceTask;
	
	@Autowired
	private BPRestCallback bpRestCallback;
	
	@GET
	@Path("/v1/sdnc/healthcheck")
	@ApiOperation(value="Healthcheck for SDNCAdapter",response = Response.class)
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck(@QueryParam("requestId") String requestId) {		
		MsoLogger.setServiceName("Healthcheck");
		UUIDChecker.verifyOldUUID(requestId, LOGGER);
		return HEALTH_CHECK_RESPONSE;
	}
	
	@HEAD
	@Path("/v1/sdnc/healthcheck")
	@ApiOperation(value="Healthcheck for SDNCAdapter",response = Response.class)
	@Produces(MediaType.TEXT_HTML)
	public Response headHealthcheck(@QueryParam("requestId") String requestId) {
		MsoLogger.setServiceName("Healthcheck");
		UUIDChecker.verifyOldUUID(requestId, LOGGER);
		return HEALTH_CHECK_RESPONSE;
	}

	/**
	 * Processes an SDNCServiceRequest (a request for "agnostic" API services) from BP.
	 * @param request the request
	 * @param msoRequestId the request ID for the top-level MSO flow (used for logging only)
	 * @param msoServiceInstanceId the top-level service-instance-id (used for logging only)
	 */
	@POST
	@Path("/v1/sdnc")
	@ApiOperation(value="Processes an SDNCServiceRequest from BP",response = Response.class)
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response service(
			SDNCServiceRequest request,
			@HeaderParam(value="mso-request-id") String msoRequestId,
			@HeaderParam(value="mso-service-instance-id") String msoServiceInstanceId) {

		MsoLogger.setLogContext(msoRequestId, msoServiceInstanceId);

		try {
			LOGGER.debug(getClass().getSimpleName() + ".service(request)"
				+ " entered with request: " + request.toJson());

			sdncServiceTask.runRequest(request, msoRequestId,msoServiceInstanceId, "/attservices");
	    
	    	// Send sync response to caller
    		LOGGER.debug(getClass().getSimpleName() + ".service(request)"
    			+ " exited successfully");
	    	return Response.status(HttpStatus.SC_ACCEPTED).build();
		} catch (Exception e) {
			String msg = "Caught " + e.getClass().getSimpleName() + " in 'service' method";
			LOGGER.error(MessageEnum.RA_SEND_REQUEST_SDNC_ERR, "SDNC", "", MsoLogger.ErrorCode.BusinessProcesssError, msg, e);
    		LOGGER.debug(getClass().getSimpleName() + ".service(request)"
    			+ " exited with error: " + msg);
			SDNCServiceError error = new SDNCServiceError(request.getSdncRequestId(),
				String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR), e.toString(), "Y");
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<SDNCServiceError>(error){})
				.build();
		}
	}

	/**
	 * Processes a notification from SDNC for "agnostic" API services.
	 * Note that the "myurl" configuration property specifies the path
	 * up to and including /SDNCNotify. The /services part of the path
	 * is added by this class.
	 * @param content the notification content
	 */
	@POST
	@Path("/SDNCNotify/attservices")
	@ApiOperation(value="Processes a notification from SDNC for agnostic API services",response = Response.class)
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public Response serviceNotification(String content) {
		LOGGER.info(MessageEnum.RA_RECEIVE_SDNC_NOTIF, content, "SDNC", "SDNCNotify/services");

		long startTime = System.currentTimeMillis();

		try {
			// Because the format of a notification is exactly the same as that of
			// a synchronous response, we can use the same code to parse it.
			SDNCResponseCommon response = SDNCServiceRequestConnector.parseResponseContent(content);

			String bpUrl = env.getProperty(Constants.BPEL_REST_URL_PROP, "");

			if (bpUrl == null || bpUrl.equals("")) {
				String error = "Missing configuration for: " + Constants.BPEL_REST_URL_PROP;
				LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, Constants.BPEL_REST_URL_PROP, "SDNC", "",
					MsoLogger.ErrorCode.DataError, "Missing config param");
				ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
				return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(error).build();
			}

			long bpStartTime = System.currentTimeMillis();		
			boolean callbackSuccess = bpRestCallback.send(bpUrl, "SDNCAResponse", response.getSdncRequestId(), response.toJson());

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

			return Response.ok().build();
		} catch (ParseException e) {
			LOGGER.error(e);
			LOGGER.error(MessageEnum.RA_PARSING_REQUEST_ERROR, "SDNC", "SDNCNotify/services",
				MsoLogger.ErrorCode.SchemaError, e.getMessage());
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
				MsoLogger.ResponseCode.SchemaError, e.getMessage());
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(e.getMessage()).build();
		}catch (Exception e) {
				LOGGER.error(e);
				LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.SchemaError, e.getMessage());
				return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(e.getMessage()).build();
			}
	}

	/**
	 * Processes an event notification from SDNC.
	 * Note that the "myurl" configuration property specifies the path
	 * up to and including /SDNCNotify. The /activate part of the path
	 * is added by this class.
	 * Used By UCPE callbacks
	 * @param content the notification content
	 */
	@POST
	@Path("/SDNCNotify/event")
	@ApiOperation(value="Processes an event notification from SDNC",response = Response.class)
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public Response eventNotification(String content) {
		LOGGER.info(MessageEnum.RA_RECEIVE_SDNC_NOTIF, content, "SDNC", "SDNCNotify/event");

		long startTime = System.currentTimeMillis(); 

		try {
			SDNCEvent event = SDNCEventParser.parse(content);

			String bpUrl = env.getProperty(Constants.BPEL_REST_URL_PROP, "");

			if (bpUrl == null || bpUrl.equals("")) {
				String error = "Missing configuration for: " + Constants.BPEL_REST_URL_PROP;
				LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, Constants.BPEL_REST_URL_PROP, "SDNC", "",
					MsoLogger.ErrorCode.DataError, "Missing config param");
				ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
				return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(error).build();
			}

			long bpStartTime = System.currentTimeMillis();		
			boolean callbackSuccess = bpRestCallback.send(bpUrl, "SDNCAEvent", event.getEventCorrelator(), event.toJson());

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

			return Response.ok().build();
		} catch (ParseException e) {
			LOGGER.error(MessageEnum.RA_PARSING_REQUEST_ERROR, "SDNC", "SDNCNotify/event",
				MsoLogger.ErrorCode.SchemaError, e.getMessage());
			LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
				MsoLogger.ResponseCode.SchemaError, e.getMessage());
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}