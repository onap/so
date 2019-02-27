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

package org.onap.so.bpmn.mock;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.utils.TargetEntity;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Please describe the VnfAdapterCreateMockTransformer.java class
 *
 */
public class VnfAdapterCreateMockTransformer extends ResponseDefinitionTransformer {

	private static final Logger logger = LoggerFactory.getLogger(VnfAdapterCreateMockTransformer.class);
	
	private String notifyCallbackResponse;
	private String ackResponse;

	public VnfAdapterCreateMockTransformer() {
		notifyCallbackResponse = FileUtil.readResourceFile("__files/vnfAdapterMocks/vnfCreateSimResponse.xml"); // default response
	}

	@Override
	public String getName() {
		return "vnf-adapter-create-transformer";
	}

	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
			FileSource fileSource, Parameters parameters) {

		String requestBody = request.getBodyAsString();

		String notficationUrl = requestBody.substring(requestBody.indexOf("<notificationUrl>")+17, requestBody.indexOf("</notificationUrl>"));
		String messageId = requestBody.substring(requestBody.indexOf("<messageId>")+11, requestBody.indexOf("</messageId>"));
		String responseMessageId = "";
		String updatedResponse = "";

		try {
			// try supplied response file (if any)
			System.out.println(" Supplied fileName: " + responseDefinition.getBodyFileName());
		    ackResponse = FileUtil.readResourceFile("__files/" + responseDefinition.getBodyFileName());
			notifyCallbackResponse = ackResponse;
			responseMessageId = ackResponse.substring(ackResponse.indexOf("<messageId>")+11, ackResponse.indexOf("</messageId>"));
		    updatedResponse = ackResponse.replace(responseMessageId, messageId);
		} catch (Exception ex) {
			logger.debug("Exception :",ex);
			System.out.println(" ******* Use default response file in '__files/vnfAdapterMocks/vnfCreateSimResponse.xml'");
		    responseMessageId = notifyCallbackResponse.substring(notifyCallbackResponse.indexOf("<messageId>")+11, notifyCallbackResponse.indexOf("</messageId>"));
			updatedResponse = notifyCallbackResponse.replace(responseMessageId, messageId);
		}

		logger.info("response (mock) messageId       : {}", responseMessageId);
		logger.info("request  (replacement) messageId: {}", messageId);

		logger.info("vnf Response (before): {}", notifyCallbackResponse);
		logger.info("vnf Response (after): {}", updatedResponse);

		Object vnfDelay = MockResource.getMockProperties().get("vnf_delay");
		int delay = 300;
		if (vnfDelay != null) {
			delay = Integer.parseInt(vnfDelay.toString());
		}

		//Kick off callback thread
		logger.info("VnfAdapterCreateMockTransformer notficationUrl: {} :delay: {}", notficationUrl, delay);
		CallbackResponseThread callbackResponseThread = new CallbackResponseThread(notficationUrl,updatedResponse, delay);
		callbackResponseThread.start();

		return ResponseDefinitionBuilder
		           .like(responseDefinition).but()
		           .withStatus(200).withBody(updatedResponse).withHeader("Content-Type", "text/xml")
		           .build();

	}

	@Override
	public boolean applyGlobally() {
	    return false;
	}

	private class CallbackResponseThread extends Thread {

		private String callbackUrl;
		private String payLoad;
		private int delay;

		public CallbackResponseThread(String callbackUrl, String payLoad, int delay) {
			this.callbackUrl = callbackUrl;
			this.payLoad = payLoad;
			this.delay = delay;
		}

		@SuppressWarnings("deprecation")
		public void run () {
			try {
				//Delay sending callback response
				sleep(delay);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				logger.debug("Exception :",e1);
			}
			logger.debug("Sending callback response to url: {}", callbackUrl);
			try {
				HttpClient client = new HttpClientFactory().newTextXmlClient(
					UriBuilder.fromUri(callbackUrl).build().toURL(),
					TargetEntity.VNF_ADAPTER);
				Response response = client.post(payLoad);
				logger.debug("Successfully posted callback? Status: {}", response.getStatus());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.debug("catch error in - request.post() ");
				logger.debug("Exception :", e);
			}
		}

	}


}
