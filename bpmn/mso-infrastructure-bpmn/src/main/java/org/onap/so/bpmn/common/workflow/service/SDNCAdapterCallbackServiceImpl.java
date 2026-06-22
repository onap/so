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

package org.onap.so.bpmn.common.workflow.service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.ws.rs.core.Context;
import jakarta.xml.ws.WebServiceContext;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterResponse;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCCallbackAdapterPortType;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackError;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of SDNCAdapterCallbackService.
 */
@WebService(serviceName = "SDNCAdapterCallbackService",
        targetNamespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
@Service
public class SDNCAdapterCallbackServiceImpl extends ProcessEngineAwareService implements SDNCCallbackAdapterPortType {

    private final static String logMarker = "[SDNC-CALLBACK]";

    @Context
    WebServiceContext wsContext;

    @Autowired
    CallbackHandlerService callback;

    @WebMethod(operationName = "SDNCAdapterCallback")
    @WebResult(name = "SDNCAdapterResponse", targetNamespace = "http://org.onap/workflow/sdnc/adapter/schema/v1",
            partName = "SDNCAdapterCallbackResponse")
    public SDNCAdapterResponse sdncAdapterCallback(@WebParam(name = "SDNCAdapterCallbackRequest",
            targetNamespace = "http://org.onap/workflow/sdnc/adapter/schema/v1",
            partName = "SDNCAdapterCallbackRequest") SDNCAdapterCallbackRequest sdncAdapterCallbackRequest) {

        String method = "sdncAdapterCallback";
        Object message = sdncAdapterCallbackRequest;
        String messageEventName = "sdncAdapterCallbackRequest";
        String messageVariable = "sdncAdapterCallbackRequest";
        String correlationVariable = "SDNCA_requestId";
        String correlationValue = sdncAdapterCallbackRequest.getCallbackHeader().getRequestId();

        CallbackResult result = callback.handleCallback(method, message, messageEventName, messageVariable,
                correlationVariable, correlationValue, logMarker);

        if (result instanceof CallbackError) {
            return new SDNCAdapterErrorResponse(((CallbackError) result).getErrorMessage());
        } else {
            return new SDNCAdapterResponse();
        }
    }

    // This subclass allows unit tests to extract the error
    public class SDNCAdapterErrorResponse extends SDNCAdapterResponse {
        private String error;

        public SDNCAdapterErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
