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

package org.openecomp.mso.asdc.healthcheck;


import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJsonProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.utils.UUIDChecker;


@Path("/")
	public class HealthCheckHandler {
		
		private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.ASDC);
		private static final String MSO_PROP_ASDC = "MSO_PROP_ASDC";
		private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

		private static final String SUC_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
		private static final String NOT_FOUND = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Application Not Ready</title></head><body>Application Not Ready. Properties file missing or invalid or database Connection failed</body></html>";

		private static final Response OK_RESPONSE = Response.status (HttpStatus.SC_OK).entity (SUC_HTML).build ();
		private static final Response NOK_RESPONSE = Response.status (HttpStatus.SC_SERVICE_UNAVAILABLE).entity (NOT_FOUND).build ();

		@HEAD
	    @GET
	    @Path("/healthcheck")
	    @Produces("text/html")
	    public Response healthcheck (@QueryParam("requestId") String requestId) {
			long startTime = System.currentTimeMillis ();
			MsoLogger.setServiceName ("Healthcheck");
			UUIDChecker.verifyOldUUID(requestId, msoLogger);
			HealthCheckUtils healthCheck = new HealthCheckUtils ();
			if (!healthCheck.siteStatusCheck(msoLogger)) {
				return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
			}

			MsoJsonProperties props = loadMsoProperties ();
			if (props == null) {
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Application Not Ready");
				return HealthCheckUtils.NOT_STARTED_RESPONSE;
			}

			if (!healthCheck.catalogDBCheck (msoLogger, startTime)) {
				return HealthCheckUtils.NOT_STARTED_RESPONSE;
			}
			msoLogger.debug("healthcheck - Successful");
			return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
	    }

		private MsoJsonProperties loadMsoProperties () {
			MsoJsonProperties msoProperties;
			try {
				msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_PROP_ASDC);
			} catch (Exception e) {
				msoLogger.error (MessageEnum.ASDC_PROPERTIES_NOT_FOUND, MSO_PROP_ASDC, "", "", MsoLogger.ErrorCode.DataError, "Exception - getMsoJsonProperties", e);
				return null;
			}

			if (msoProperties !=null && msoProperties.getJsonRootNode().elements().hasNext()) {
				return msoProperties;
			} else {
				msoLogger.error (MessageEnum.ASDC_PROPERTIES_NOT_FOUND , MSO_PROP_ASDC, "", "", MsoLogger.ErrorCode.DataError, "ASDC properties not found");
				return  null;
			}
		}
}
