/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.workflow.service;

import io.swagger.annotations.Api;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import org.onap.so.bpmn.common.adapter.vnf.SDNCResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@Path("/mdonsAsync")
@Api(value = "/mdonsAsync", description = "Send async response to BPMN")
public class OpticalAsyncResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(OpticalAsyncResponseHandler.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/updateStatus")
    public String processAsyncResponse(@RequestBody SDNCResponse respose) {

        logger.debug(respose.toString());
        return "success";

    }

}
