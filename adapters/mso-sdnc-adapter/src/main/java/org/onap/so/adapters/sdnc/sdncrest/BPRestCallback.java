/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.onap.so.logger.MessageEnum;

import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;

/**
 * Sends asynchronous messages to the BPMN WorkflowMessage service.
 */
@Component
public class BPRestCallback {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA,BPRestCallback.class);

	private static final String CAMUNDA="Camunda";
	private static final String MSO_INTERNAL_ERROR="MsoInternalError";
	@Autowired
	private Environment env;

	/**
	 * Sends a message to the BPMN workflow message service. The URL path is
	 * constructed using the specified message type and correlator.
	 * @param workflowMessageUrl the base BPMN WorkflowMessage URL
	 * @param messageType the message type
	 * @param correlator the message correlator
	 * @param message the JSON content
	 * @return true if the message was consumed successfully by the endpoint
	 */
	public boolean send(String workflowMessageUrl, String messageType, String correlator, String message) {
		LOGGER.debug(getClass().getSimpleName() + ".send("
			+ "workflowMessageUrl=" + workflowMessageUrl
			+ " messageType=" + messageType
			+ " correlator=" + correlator
			+ " message=" + message
			+ ")");

		while (workflowMessageUrl.endsWith("/")) {
			workflowMessageUrl = workflowMessageUrl.substring(0, workflowMessageUrl.length()-1);
		}

		String endpoint = workflowMessageUrl + "/" + SDNCAdapterUtils.encodeURLPathSegment(messageType)
			+ "/" + SDNCAdapterUtils.encodeURLPathSegment(correlator);

		return send(endpoint, message);
	}

	/**
	 * Sends a message to the BPMN workflow message service. The specified URL
	 * must have the message type and correlator already embedded in it.
	 * @param url the endpoint URL
	 * @param message the JSON content
	 * @return true if the message was consumed successfully by the endpoint
	 */
	public boolean send(String url, String message) {
		LOGGER.debug(getClass().getSimpleName() + ".send("
			+ "url=" + url
			+ " message=" + message
			+ ")");

		LOGGER.info(MessageEnum.RA_CALLBACK_BPEL, message == null ? "[no content]" : message, CAMUNDA, "");

		HttpPost method = null;
		HttpResponse httpResponse = null;

		try {		
			int timeout = 60 * 1000;

			RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.build();

			HttpClient client = HttpClientBuilder.create().build();
			method = new HttpPost(url);
			method.setConfig(requestConfig);

			if (message != null) {
				method.setEntity(new StringEntity(message, ContentType.APPLICATION_JSON));
			}

			boolean error = false;

			try {	
				String userCredentials = CryptoUtils.decrypt(env.getProperty(Constants.BPEL_AUTH_PROP),
					env.getProperty(Constants.ENCRYPTION_KEY_PROP));
				String authorization = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
				method.setHeader("Authorization", authorization);
				method.setHeader(ONAPLogConstants.Headers.REQUEST_ID,MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
				method.setHeader(ONAPLogConstants.Headers.INVOCATION_ID,MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
				method.setHeader(ONAPLogConstants.Headers.PARTNER_NAME,"SO-SDNCAdapter");
			} catch (Exception e) {
				LOGGER.error(MessageEnum.RA_SET_CALLBACK_AUTH_EXC, CAMUNDA, "", MsoLogger.ErrorCode.BusinessProcesssError,
					"Unable to set authorization in callback request", e);			
				error = true;
			}

			if (!error) {
				httpResponse = client.execute(method);

				@SuppressWarnings("unused")
				String responseContent = null;

				if (httpResponse.getEntity() != null) {
					responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				}

				if (httpResponse.getStatusLine().getStatusCode() >= 300) {
					String msg = "Received error response to callback request: " + httpResponse.getStatusLine();
					LOGGER.error(MessageEnum.RA_CALLBACK_BPEL_EXC, CAMUNDA, "", MsoLogger.ErrorCode.BusinessProcesssError, msg);

				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(MessageEnum.RA_CALLBACK_BPEL_EXC, CAMUNDA, "", MsoLogger.ErrorCode.BusinessProcesssError,
				"Error sending callback request", e);			
			return false;
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
					httpResponse = null;
				} catch (Exception e) {
					LOGGER.debug("Exception:", e);
				}
			}

			if (method != null) {
				try {
					method.reset();
				} catch (Exception e) {
					LOGGER.debug("Exception:", e);
				}
			}
			LOGGER.info(MessageEnum.RA_CALLBACK_BPEL_COMPLETE, CAMUNDA, "","");
		}
	}
}