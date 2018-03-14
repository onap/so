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

package org.openecomp.mso.bpmn.mock;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * 
 * Simulates VNF Adapter Asynch Callback response.
 * This should work for any of the operations.
 * 
 * This transformer uses the mapped message as the asynchronous response.
 * By definition, the async API sends a 202 (with no body) in the sync response.
 *
 */
public class VnfAdapterAsyncTransformer extends ResponseTransformer {

	public VnfAdapterAsyncTransformer() {
	}

	public String name() {
		return "vnf-adapter-async";
	}

	/**
	 * Grab the incoming request, extract properties to be copied to the response
	 * (request id, vnf id, vf module ID, message ID).  Then fetch the actual response
	 * body from its FileSource, make the replacements.
	 * 
	 * The sync response is an empty 202 response.
	 * The transformed mapped response file is sent asynchronously after a delay.
	 * 
	 * Mock Resource can be used to add dynamic properties. If vnf_delay is not in the list by
	 * default waits for 5s before the callback response is sent
	 */
	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
			FileSource fileSource) {
		
		String requestBody = request.getBodyAsString();
		
		// Note: Should recognize both XML and JSON.  But current BPMN uses XML.
		String notificationUrl = requestBody.substring(requestBody.indexOf("<notificationUrl>")+17, requestBody.indexOf("</notificationUrl>"));
		
		String vnfId = requestBody.substring(requestBody.indexOf("<vnfId>")+7, requestBody.indexOf("</vnfId>"));
		String vfModuleId = requestBody.substring(requestBody.indexOf("<vfModuleId>")+12, requestBody.indexOf("</vfModuleId>"));
		String messageId = requestBody.substring(requestBody.indexOf("<messageId>")+11, requestBody.indexOf("</messageId>"));
		String requestId = requestBody.substring(requestBody.indexOf("<requestId>")+11, requestBody.indexOf("</requestId>"));

		System.out.println("responseDefinition: " + responseDefinition);

		// For this mock, the mapped response body is the Async callback (since the sync response is generic for all requests)
		String vnfResponse = responseDefinition.getBody();
		System.out.println("VNF Response:" + vnfResponse);

		if (vnfResponse == null) {
			// Body wasn't specified.  Check for a body file
			String bodyFileName = responseDefinition.getBodyFileName();
			System.out.println("bodyFileName" + bodyFileName);
			if (bodyFileName != null) {
				System.out.println("fileSource Class: " + fileSource.getClass().getName());
				BinaryFile bodyFile = fileSource.getBinaryFileNamed(bodyFileName);
				byte[] responseFile = bodyFile.readContents();
				vnfResponse = new String(responseFile);
				System.out.println("vnfResponse(2):" + vnfResponse);
			}
		}
		
		// Transform the SDNC response to escape < and >
		vnfResponse = vnfResponse.replaceAll ("VNF_ID", vnfId);
		vnfResponse = vnfResponse.replaceAll ("VF_MODULE_ID", vfModuleId);
		vnfResponse = vnfResponse.replaceAll ("REQUEST_ID", requestId);
		vnfResponse = vnfResponse.replaceAll ("MESSAGE_ID", messageId);
		
		Object vnfDelay = MockResource.getMockProperties().get("vnf_delay");
		int delay = 5000;
		if (vnfDelay != null) {
			delay = Integer.parseInt(vnfDelay.toString());
		}
		
		//Kick off callback thread
		System.out.println("notification Url:" + notificationUrl + ":delay:" + delay);
		CallbackResponseThread calbackResponseThread = new CallbackResponseThread(notificationUrl,vnfResponse, delay);
		calbackResponseThread.start();
		
		//return 200 OK with empty body
		return ResponseDefinitionBuilder
                .like(responseDefinition).but()
                .withStatus(202).withBody("").withHeader("Content-Type", "text/xml")
                .build();
	}

	@Override
	public boolean applyGlobally() {
	    return false;
	}
	
	/**
	 * 
	 * Callback response thread which sends the callback response asynchronously
	 *
	 */
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Sending callback response:" + callbackUrl);
			ClientRequest request = new ClientRequest(callbackUrl);
			request.body("text/xml", payLoad);
			System.err.println(payLoad);
			try {
				ClientResponse result = request.post();
				//System.err.println("Successfully posted callback:" + result.getStatus());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
