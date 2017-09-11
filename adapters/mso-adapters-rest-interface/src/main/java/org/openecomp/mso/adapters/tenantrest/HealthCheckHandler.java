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

package org.openecomp.mso.adapters.tenantrest;



import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import org.openecomp.mso.logger.MsoLogger;

@Path("/")
public class HealthCheckHandler {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
    private static final Response HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
                                                                  .entity (CHECK_HTML)
                                                                  .build ();
    @HEAD
    @GET
    @Path("/healthcheck")
    @Produces("text/html")
    public Response healthcheck () {
    	msoLogger.debug ("Health check call in Tenant Adapter");
        return HEALTH_CHECK_RESPONSE;
    }
}
