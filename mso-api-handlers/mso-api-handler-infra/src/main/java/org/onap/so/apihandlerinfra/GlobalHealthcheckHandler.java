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

package org.onap.so.apihandlerinfra;

import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;



@Component
@Path("/globalhealthcheck")
@Api(value="/globalhealthcheck",description="APIH Infra Global Health Check")
public class GlobalHealthcheckHandler {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH,GlobalHealthcheckHandler.class);
    private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";

	public static final Response HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
            .entity (CHECK_HTML)
            .build ();
	
    @GET
    @Produces("text/html")
	@ApiOperation(value="Performing global health check",response=Response.class)
    @Transactional
    public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn, 
    									@Context ContainerRequestContext requestContext) {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("GlobalHealthcheck");
        // Generated RequestId
        String requestId = requestContext.getProperty("requestId").toString();
        MsoLogger.setLogContext(requestId, null);
        msoLogger.info(MessageEnum.APIH_GENERATED_REQUEST_ID, requestId, "", "");
        return HEALTH_CHECK_RESPONSE;
    } 
}
