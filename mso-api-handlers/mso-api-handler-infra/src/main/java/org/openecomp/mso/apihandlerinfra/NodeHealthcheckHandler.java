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

package org.openecomp.mso.apihandlerinfra;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/nodehealthcheck")
@Api(value="/nodehealthcheck",description="API Handler Infra Node Health Check")
public class NodeHealthcheckHandler {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

	@HEAD
    @GET
    @Produces("text/html")
	@ApiOperation(value="Performing node health check",response=Response.class)
    public Response nodeHealthcheck () {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("NodeHealthcheck");
        // Generate a Request Id
        String requestId = UUIDChecker.generateUUID(msoLogger);
        HealthCheckUtils healthCheck = new HealthCheckUtils ();
        if (!healthCheck.siteStatusCheck (msoLogger)) {
            return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }

        if (healthCheck.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, requestId)) {
            msoLogger.debug("nodeHealthcheck - Successful");
            return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
        } else {
            msoLogger.debug("nodeHealthcheck - At leaset one of the sub-modules is not available.");
            return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }
    }
}
