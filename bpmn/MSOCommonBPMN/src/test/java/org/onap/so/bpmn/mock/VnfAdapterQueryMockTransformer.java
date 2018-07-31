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

package org.onap.so.bpmn.mock;


import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.HttpClient;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.TargetEntity;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * Please describe the VnfAdapterQueryMockTransformer.java class
 *
 */


public class VnfAdapterQueryMockTransformer extends ResponseDefinitionTransformer{
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VnfAdapterQueryMockTransformer.class);
	
	private String notifyCallbackResponse;
	private String ackResponse;
	private String messageId;

	public VnfAdapterQueryMockTransformer() {
		notifyCallbackResponse = FileUtil.readResourceFile("__files/vnfAdapterMocks/vnfQuerySimResponse.xml");
	}
	
	public VnfAdapterQueryMockTransformer(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public String getName() {
		return "vnf-adapter-query-transformer";
	}
	
	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
			FileSource fileSource, Parameters parameters) {

		String requestBody = request.getBodyAsString();

		String notficationUrl = requestBody.substring(requestBody.indexOf("<notificationUrl>")+17, requestBody.indexOf("</notificationUrl>"));
		String messageId = requestBody.substring(requestBody.indexOf("<messageId>")+11, requestBody.indexOf("</messageId>"));
	//	String updatedResponse = notifyCallbackResponse.replace("b1a82ce6-7f5c-45fd-9273-acaf88fc2137", messageId);
	
		String responseMessageId = "";
		String updatedResponse = "";
		
	//	if (ackResponse == null) {
			//System.err.println("file:" + responseDefinition.getBodyFileName());
		//	ackResponse = FileUtil.readResourceFile("__files/" + responseDefinition.getBodyFileName());
		//}

		
		try {
			// try supplied response file (if any)
			System.out.println(" Supplied fileName: " + responseDefinition.getBodyFileName());
		    ackResponse = FileUtil.readResourceFile("__files/" + responseDefinition.getBodyFileName());
			notifyCallbackResponse = ackResponse;
			responseMessageId = ackResponse.substring(ackResponse.indexOf("<messageId>")+11, ackResponse.indexOf("</messageId>"));
		    updatedResponse = ackResponse.replace(responseMessageId, messageId); 
		} catch (Exception ex) {
			LOGGER.debug("Exception :",ex);
			System.out.println(" ******* Use default response file in '__files/vnfAdapterMocks/vnfQuerySimResponse.xml'");
		    responseMessageId = notifyCallbackResponse.substring(notifyCallbackResponse.indexOf("<messageId>")+11, notifyCallbackResponse.indexOf("</messageId>"));
			updatedResponse = notifyCallbackResponse.replace(responseMessageId, messageId);
		}
		
		System.out.println("response (mock) messageId       : " + responseMessageId);		
		System.out.println("request  (replacement) messageId: " + messageId);
		
		System.out.println("vnf Response (before):" + notifyCallbackResponse);
		System.out.println("vnf Response (after):" + updatedResponse);
		
		
		Object vnfDelay = MockResource.getMockProperties().get("vnf_delay");
		int delay = 300;
		if (vnfDelay != null) {
			delay = Integer.parseInt(vnfDelay.toString());
		}

		//Kick off callback thread
		
		//System.out.println("notficationUrl" + notficationUrl);
		//System.out.println("updatedResponse" + updatedResponse);
		System.out.println("VnfAdapterQueryMockTransformer notficationUrl: " + notficationUrl + ":delay: " + delay);
		CallbackResponseThread callbackResponseThread = new CallbackResponseThread(notficationUrl,updatedResponse, delay);
		System.out.println("Inside Callback" );
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

		public void run () {
			try {
				//Delay sending callback response
				sleep(delay);
			} catch (InterruptedException e1) {
				LOGGER.debug("Exception :",e1);
			}

			try {
				HttpClient client = new HttpClient(UriBuilder.fromUri(callbackUrl).build().toURL(), "text/xml", TargetEntity.VNF_ADAPTER);
				client.post(payLoad);
			} catch (Exception e) {
				LOGGER.debug("Exception :",e);
			}
		}

	}


}
