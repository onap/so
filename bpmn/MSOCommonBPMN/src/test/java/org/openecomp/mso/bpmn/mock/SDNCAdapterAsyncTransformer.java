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
 * Simulates SDNC Adapter Callback response
 *
 */
public class SDNCAdapterAsyncTransformer extends ResponseTransformer {

	private String syncResponse;
	private String callbackResponseWrapper;
	
	public SDNCAdapterAsyncTransformer() {
		syncResponse = FileUtil.readResourceFile("__files/StandardSDNCSynchResponse.xml");
		callbackResponseWrapper = FileUtil.readResourceFile("__files/sdncCallbackSoapWrapper.xml");
	}

	public String name() {
		return "sdnc-adapter-vf-module-assign";
	}

	/**
	 * Grab the incoming request xml,extract the request id and replace the stub response with the incoming request id
	 * so that callback response can be correlated
	 * 
	 * Mock Resource can be used to add dynamic properties. If sdnc_delay is not in the list by default waits for 300ms before
	 * the callback response is sent
	 */
	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
			FileSource fileSource) {
		
		String requestBody = request.getBodyAsString();
		
		String callbackUrl = requestBody.substring(requestBody.indexOf("<sdncadapter:CallbackUrl>")+25, requestBody.indexOf("</sdncadapter:CallbackUrl>"));
		String requestId = requestBody.substring(requestBody.indexOf("<sdncadapter:RequestId>")+23, requestBody.indexOf("</sdncadapter:RequestId>"));

		System.out.println("responseDefinition: " + responseDefinition);

		// For this mock, the mapped response body is the Async callback (since the sync response is generic for all requests)
		String sdncResponse = responseDefinition.getBody();
		System.out.println("sdncResponse:" + sdncResponse);

		if (sdncResponse == null) {
			// Body wasn't specified.  Check for a body file
			String bodyFileName = responseDefinition.getBodyFileName();
			System.out.println("bodyFileName" + bodyFileName);
			if (bodyFileName != null) {
				System.out.println("fileSource Class: " + fileSource.getClass().getName());
				BinaryFile bodyFile = fileSource.getBinaryFileNamed(bodyFileName);
				byte[] responseFile = bodyFile.readContents();
				sdncResponse = new String(responseFile);
				System.out.println("sdncResponse(2):" + sdncResponse);
			}
		}
		
		// Transform the SDNC response to escape < and >
		sdncResponse = sdncResponse.replaceAll ("<", "&lt;");
		sdncResponse = sdncResponse.replaceAll (">", "&gt;");
		
		// Next substitute the SDNC response into the callbackResponse (SOAP wrapper).
		// Also, replace the request ID wherever it appears
		String callbackResponse = callbackResponseWrapper.replace("SDNC_RESPONSE_DATA", sdncResponse).replaceAll("SDNC_REQUEST_ID", requestId);
		
		Object sdncDelay = MockResource.getMockProperties().get("sdnc_delay");
		int delay = 2000;
		if (sdncDelay != null) {
			delay = Integer.parseInt(sdncDelay.toString());
		}
		
		//Kick off callback thread
		System.out.println("callback Url:" + callbackUrl + ":delay:" + delay);
		CallbackResponseThread calbackResponseThread = new CallbackResponseThread(callbackUrl,callbackResponse, delay);
		calbackResponseThread.start();
		
		//return 200 OK with empty body
		return ResponseDefinitionBuilder
                .like(responseDefinition).but()
                .withStatus(200).withBody(syncResponse).withHeader("Content-Type", "text/xml")
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
