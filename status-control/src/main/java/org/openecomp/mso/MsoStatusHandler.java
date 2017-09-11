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

package org.openecomp.mso;


import org.openecomp.mso.utils.UUIDChecker;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/")
public class MsoStatusHandler {
    private MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    @POST
    @Path("/setStatus/{siteName}")
    @Produces("text/plain")
    public Response setSiteStatus (@DefaultValue("true") @QueryParam("enable") Boolean enable,
                                   @PathParam("siteName") String siteName) {
        long startTime = System.currentTimeMillis();
        // Set logger parameters
        UUIDChecker.generateUUID (logger);
        MsoLogger.setServiceName ("SetSiteStatus");


        if (null == siteName) {
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Not able to find the site name attribute in the config file");
            return Response.status (Response.Status.INTERNAL_SERVER_ERROR).entity ("Exception: not able to find the site name attribute in the config file").build ();
        }

        // Query DB for the value
        try {
            (RequestsDatabase.getInstance()).updateSiteStatus(siteName, enable);

        } catch (Exception e) {
            logger.error (MessageEnum.GENERAL_EXCEPTION, "", "setSiteStatus", MsoLogger.ErrorCode.DataError, "Failed to set site status", e);
            logger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while updating site status");
            return Response.status (Response.Status.INTERNAL_SERVER_ERROR).entity ("Exception while updating site status").build ();
        }
        logger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        return Response.status (Response.Status.OK).entity ("Site status successfully updated to " + enable).build ();
    }
}
