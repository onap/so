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

package org.openecomp.mso.bpmn.common.workflow.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterResponse;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCCallbackAdapterPortType;

/**
 * Implementation of SDNCAdapterCallbackService.
 */
@WebService(serviceName="SDNCAdapterCallbackService", targetNamespace="http://org.openecomp/workflow/sdnc/adapter/schema/v1")
public class SDNCAdapterCallbackServiceImpl extends AbstractCallbackService implements SDNCCallbackAdapterPortType {

	private final String logMarker = "[SDNC-CALLBACK]";

	@Context WebServiceContext wsContext;

	@WebMethod(operationName = "SDNCAdapterCallback")
    @WebResult(name = "SDNCAdapterResponse", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1", partName = "SDNCAdapterCallbackResponse")
    public SDNCAdapterResponse sdncAdapterCallback(
            @WebParam(name = "SDNCAdapterCallbackRequest", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1", partName = "SDNCAdapterCallbackRequest")
            SDNCAdapterCallbackRequest sdncAdapterCallbackRequest) {

		String method = "sdncAdapterCallback";
		Object message = sdncAdapterCallbackRequest;
		String messageEventName = "sdncAdapterCallbackRequest";
		String messageVariable = "sdncAdapterCallbackRequest";
		String correlationVariable = "SDNCA_requestId";
		String correlationValue = sdncAdapterCallbackRequest.getCallbackHeader().getRequestId();

		MsoLogger.setServiceName("MSO." + method);
		MsoLogger.setLogContext(correlationValue, "N/A");

		CallbackResult result = handleCallback(method, message, messageEventName,
			messageVariable, correlationVariable, correlationValue, logMarker);

		if (result instanceof CallbackError) {
			return new SDNCAdapterErrorResponse(((CallbackError)result).getErrorMessage());
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