/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.workflowmessage;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Sends asynchronous messages to the BPMN WorkflowMessage service.
 */
public class BPRestCallback {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	/**
	 * Sends a message to the BPMN workflow message service. The URL path is
	 * constructed using the specified message type and correlator.
	 * @param workflowMessageUrl the base BPMN WorkflowMessage URL
	 * @param messageType the message type
	 * @param correlator the message correlator
	 * @param contentType the value for the HTTP Content-Type header (possibly null)
	 * @param message the content (possibly null)
	 * @return true if the message was consumed successfully by the endpoint
	 */
	public boolean send(String workflowMessageUrl, String messageType,
			String correlator, ContentType contentType, String message) {
		LOGGER.debug(getClass().getSimpleName() + ".send("
			+ "workflowMessageUrl=" + workflowMessageUrl
			+ " messageType=" + messageType
			+ " correlator=" + correlator
			+ " contentType=" + contentType
			+ " message=" + message
			+ ")");

		while (workflowMessageUrl.endsWith("/")) {
			workflowMessageUrl = workflowMessageUrl.substring(0, workflowMessageUrl.length()-1);
		}

		String endpoint = workflowMessageUrl + "/" + WMAdapterUtils.encodeURLPathSegment(messageType)
			+ "/" + WMAdapterUtils.encodeURLPathSegment(correlator);

		return send(endpoint, contentType, message);
	}

	/**
	 * Sends a message to the BPMN workflow message service. The specified URL
	 * must have the message type and correlator already embedded in it.
	 * @param url the endpoint URL
	 * @param message the content (possibly null)
	 * @param contentType the value for the HTTP Content-Type header (possibly null)
	 * @return true if the message was consumed successfully by the endpoint
	 */
	public boolean send(String url, ContentType contentType, String message) {
		LOGGER.debug(getClass().getSimpleName() + ".send("
			+ "url=" + url
			+ " contentType=" + contentType
			+ " message=" + message
			+ ")");

		LOGGER.info(MessageEnum.RA_CALLBACK_BPEL, message == null ? "[no content]" : message, "Camunda", "");

		HttpPost method = null;
		HttpResponse httpResponse = null;

		try {
			// TODO: configurable timeout?
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
				method.setEntity(new StringEntity(message, contentType));
			}

			boolean error = false;

			try {
				// AAF Integration, disabled for now due to the constrains from other party
				// String userCredentials = CredentialConstants.getDecryptedCredential(WMAdapterConstants.DEFAULT_BPEL_AUTH);
				// Once AAF enabled, the credential shall be get by triggering the CredentialConstants.getDecryptedCredential -- remove line
				String  userCredentials = WMAdapterProperties.getEncryptedProperty(WMAdapterConstants.BPEL_AUTH_PROP,
					WMAdapterConstants.DEFAULT_BPEL_AUTH, WMAdapterConstants.ENCRYPTION_KEY);
				String authorization = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
				method.setHeader("Authorization", authorization);
			} catch (Exception e) {
				LOGGER.error(MessageEnum.RA_SET_CALLBACK_AUTH_EXC, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError,
					"Unable to set authorization in callback request", e);
				ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL,
					"Unable to set authorization in callback request: " + e.getMessage());
				error = true;
			}

			if (!error) {
				httpResponse = client.execute(method);

				@SuppressWarnings("unused")
				String responseContent;

				if (httpResponse.getEntity() != null) {
					responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				}

				if (httpResponse.getStatusLine().getStatusCode() >= 300) {
					String msg = "Received error response to callback request: " + httpResponse.getStatusLine();
					LOGGER.error(MessageEnum.RA_CALLBACK_BPEL_EXC, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, msg);
					ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, msg);
				}

			}

			return true;
		} catch (Exception e) {
			LOGGER.error(MessageEnum.RA_CALLBACK_BPEL_EXC, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError,
				"Error sending callback request", e);
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL,
				"Error sending callback request: " + e.getMessage());
			return false;
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
					httpResponse = null;
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
				}
			}

			if (method != null) {
				try {
					method.reset();
					method = null;
				} catch (Exception e) {
					LOGGER.debug("Exception :",e);
				}
			}

			LOGGER.info(MessageEnum.RA_CALLBACK_BPEL_COMPLETE, "Camunda", "");
		}
	}
}
