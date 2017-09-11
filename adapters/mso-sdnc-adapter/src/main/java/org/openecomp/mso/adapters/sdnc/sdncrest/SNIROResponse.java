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

import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A temporary interface to support notifications from SNIRO to BPMN.
 * We added this to the SDNC adapter because we didn't have time to
 * develop a SNIRO adapter in 1702.
 */
@Path("/")
public class SNIROResponse {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	@POST
	@Path("/SDNCNotify/SNIROResponse/{correlator}")
	@Consumes("*/*")
	@Produces({MediaType.TEXT_PLAIN})
	public Response serviceNotification(@PathParam("correlator") String correlator, String content) {
		LOGGER.info(MessageEnum.RA_RECEIVE_SDNC_NOTIF, content, "SDNC", "SDNCNotify/SNIROResponse");

		long startTime = System.currentTimeMillis();

		String bpUrl = SDNCAdapterProperties.getProperty(Constants.BPEL_REST_URL_PROP, null);

		if (bpUrl == null) {
			String error = "Missing configuration for: " + Constants.BPEL_REST_URL_PROP;
			LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, Constants.BPEL_REST_URL_PROP, "SDNC", "",
				MsoLogger.ErrorCode.DataError, "Missing config param");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(error).build();
		}

		long bpStartTime = System.currentTimeMillis();
		BPRestCallback callback = new BPRestCallback();
		boolean callbackSuccess = callback.send(bpUrl, "SNIROResponse", correlator, content);

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