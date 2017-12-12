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

package org.openecomp.mso.adapters.vnf;


import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.utils.UUIDChecker;


@Path("/")
public class HealthCheckHandler {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    private static final String MSO_PROP_VNF_ADAPTER = "MSO_PROP_VNF_ADAPTER";

	@HEAD
	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck (@QueryParam("requestId") String requestId) {
		long startTime = System.currentTimeMillis ();
		MsoLogger.setServiceName ("Healthcheck");
		UUIDChecker.verifyOldUUID(requestId, msoLogger);
		HealthCheckUtils healthCheck = new HealthCheckUtils ();
		if (!healthCheck.siteStatusCheck(msoLogger)) {
			return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}

		if (!healthCheck.configFileCheck(msoLogger, startTime, MSO_PROP_VNF_ADAPTER)) {
			return HealthCheckUtils.NOT_STARTED_RESPONSE;
		}

		if (!healthCheck.catalogDBCheck (msoLogger, startTime)) {
			return HealthCheckUtils.NOT_STARTED_RESPONSE;
		}
		msoLogger.debug("healthcheck - Successful");
		return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
	}
}
