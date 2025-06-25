/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package org.onap.so.bpmn.common.scripts

import javax.ws.rs.core.Response

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.oof.adapter.beans.payload.OofRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper


import static org.onap.so.bpmn.common.scripts.GenericUtils.*


class DoHandleOofRequest extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	private static final Logger logger = LoggerFactory.getLogger(DoHandleOofRequest.class)
	private static final ObjectMapper mapper = new ObjectMapper()

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("In Preprocess Oof Request Handler")
		String apiPath = execution.getVariable("apiPath")
		if (isBlank(apiPath)) {
			String msg = "Cannot process OOF adapter call : API PATH is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		//msoRequestId is used for correlation
		String requestId = execution.getVariable("correlator")
		if (isBlank(requestId)) {
			String msg = "Cannot process OOF adapter call : correlator is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String messageType = execution.getVariable("messageType")
		if (isBlank(messageType)) {
			String msg = "Cannot process OOF adapter call : messageType is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String timeout = execution.getVariable("timeout")
		if (isBlank(timeout)) {
			timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
			if (isBlank(timeout)) {
				logger.debug("Setting OOF timeout to default : PT30M")
				timeout = "PT30M"
			}
		}

		Object requestDetails = execution.getVariable("oofRequest")
		OofRequest oofRequestPayload = new OofRequest()
		oofRequestPayload.setApiPath(apiPath)
		oofRequestPayload.setRequestDetails(requestDetails)
		String requestJson = mapper.writeValueAsString(oofRequestPayload)
		execution.setVariable("oofRequestPayload", requestJson)
	}

	void callOofAdapter(DelegateExecution execution) {
		logger.debug("Start callOofAdapter")
		String requestId = execution.getVariable("msoRequestId")
		String oofAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.oof.endpoint", execution)
		String basicAuthCred = execution.getVariable("BasicAuthHeaderValue")
		URL requestUrl = new URL(oofAdapterEndpoint)
		String oofRequest = execution.getVariable("oofRequestPayload")
		logger.debug("oofRequest : " + oofRequest)
		HttpClient httpClient = new HttpClientFactory().newJsonClient(requestUrl, ONAPComponents.EXTERNAL)
		Response httpResponse = httpClient.post(oofRequest)
		int responseCode = httpResponse.getStatus()
		logger.debug("OOF sync response code is: " + responseCode)
        if(responseCode < 200 || responseCode >= 300){
			exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
		}
	}

}
