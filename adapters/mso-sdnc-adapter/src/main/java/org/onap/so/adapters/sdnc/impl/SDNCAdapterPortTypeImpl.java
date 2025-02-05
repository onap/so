/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.so.adapters.sdnc.impl;

import jakarta.annotation.PostConstruct;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.adapters.sdnc.SDNCAdapterPortType;
import org.onap.so.adapters.sdnc.SDNCAdapterRequest;
import org.onap.so.adapters.sdnc.SDNCAdapterResponse;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// BPEL SDNCAdapter SOAP Web Service implementation
@WebService(serviceName = "SDNCAdapterService", endpointInterface = "org.onap.so.adapters.sdnc.SDNCAdapterPortType",
        targetNamespace = "http://org.onap/workflow/sdnc/adapter/wsdl/v1")
@Component
public class SDNCAdapterPortTypeImpl implements SDNCAdapterPortType {



    private static Logger logger = LoggerFactory.getLogger(SDNCAdapterPortTypeImpl.class);


    @Autowired
    private SDNCRestClient sdncClient;

    @PostConstruct
    public void init() {
        logger.info(LoggingAnchor.THREE, MessageEnum.RA_INIT_SDNC_ADAPTER.toString(), "SDNC", "SDNCAdapterPortType");
    }

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheck() {
        logger.debug("Health check call in SDNC Adapter");
    }


    @Override
    public SDNCAdapterResponse sdncAdapter(SDNCAdapterRequest bpelRequest) {
        String bpelReqId = bpelRequest.getRequestHeader().getRequestId();
        String callbackUrl = bpelRequest.getRequestHeader().getCallbackUrl();
        try {
            sdncClient.executeRequest(bpelRequest);
        } catch (Exception e) {
            String respMsg = "Error sending request to SDNC. Failed to start SDNC Client thread " + e.getMessage();
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_SEND_REQUEST_SDNC_ERR.toString(), "SDNC",
                    ErrorCode.DataError.getValue(), respMsg, e);

            SDNCResponse sdncResp = new SDNCResponse(bpelReqId);
            sdncResp.setRespCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sdncResp.setRespMsg(respMsg);
            sdncClient.sendRespToBpel(callbackUrl, sdncResp);
        }
        return (new SDNCAdapterResponse());
    }
}
