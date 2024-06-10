/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.sdnc.sdncrest;

import javax.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * A temporary interface to support notifications from SNIRO to BPMN. We added this to the SDNC adapter because we
 * didn't have time to develop a SNIRO adapter in 1702.
 */
@Path("/")
@Component
public class SNIROResponse {
    private static final Logger logger = LoggerFactory.getLogger(SNIROResponse.class);


    @Autowired
    private Environment env;

    @Autowired
    private BPRestCallback callback;

    @POST
    @Path("/SDNCNotify/SNIROResponse/{correlator}")
    @Consumes("*/*")
    @Produces({MediaType.TEXT_PLAIN})
    public Response serviceNotification(@PathParam("correlator") String correlator, String content) {
        logger.info(LoggingAnchor.FOUR, MessageEnum.RA_RECEIVE_SDNC_NOTIF.toString(), content, "SDNC",
                "SDNCNotify/SNIROResponse");

        String bpUrl = env.getProperty(Constants.BPEL_REST_URL_PROP, "");

        if (bpUrl == null || ("").equals(bpUrl)) {
            String error = "Missing configuration for: " + Constants.BPEL_REST_URL_PROP;
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_SDNC_MISS_CONFIG_PARAM.toString(),
                    Constants.BPEL_REST_URL_PROP, "SDNC", ErrorCode.DataError.getValue(), "Missing config param");

            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(error).build();
        }
        return Response.status(204).build();
    }
}
