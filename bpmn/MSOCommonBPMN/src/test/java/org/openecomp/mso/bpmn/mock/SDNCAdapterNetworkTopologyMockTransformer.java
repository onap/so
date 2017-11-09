/*
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
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
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import org.openecomp.mso.logger.MsoLogger;

public class SDNCAdapterNetworkTopologyMockTransformer extends ResponseTransformer {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	
	private String callbackResponse;
	private String requestId;
	
	public SDNCAdapterNetworkTopologyMockTransformer() {
		callbackResponse = ""; // FileUtil.readResourceFile("__files/sdncDeleteNetworkTopologySimResponse.xml");
	}

	public SDNCAdapterNetworkTopologyMockTransformer(String requestId) {
		this.requestId = requestId;
	}
	
	public String name() {
		return "network-topology-operation-transformer";
	}

	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource fileSource) {
		String requestBody = request.getBodyAsString();
		
		String callbackUrl = requestBody.substring(requestBody.indexOf("<sdncadapter:CallbackUrl>")+25, requestBody.indexOf("</sdncadapter:CallbackUrl>"));
		String requestId = requestBody.substring(requestBody.indexOf("<sdncadapter:RequestId>")+23, requestBody.indexOf("</sdncadapter:RequestId>"));
		System.out.println("request callbackUrl : " + callbackUrl);
		System.out.println("request requestId : " + requestId);
		
		System.out.println("file path/name : " + responseDefinition.getBodyFileName());
		callbackResponse = FileUtil.readResourceFile("__files/" + responseDefinition.getBodyFileName());		
		// extract Response responseRequestId
		String responseRequestId = callbackResponse.substring(callbackResponse.indexOf("<RequestId>")+11, callbackResponse.indexOf("</RequestId>"));
		System.out.println("response requestId: " + responseRequestId);		
		System.out.println("callbackResponse (before): " + callbackResponse);
		callbackResponse = callbackResponse.replace(responseRequestId, requestId);				
		if (this.requestId != null) {
			callbackResponse = callbackResponse.replace(this.requestId, requestId);
		} else {
			callbackResponse = callbackResponse.replace(responseRequestId, requestId);
		}	
		System.out.println("callbackResponse (after):" + callbackResponse);		

		Object sdncDelay = MockResource.getMockProperties().get("sdnc_delay");
		int delay = 300;
		if (sdncDelay != null) {
			delay = Integer.parseInt(sdncDelay.toString());
		}
		
		//Kick off callback thread
		System.out.println("(NetworkTopologyMockTransformer) callback Url:" + callbackUrl + ":delay:" + delay);
		CallbackResponseThread calbackResponseThread = new CallbackResponseThread(callbackUrl,callbackResponse, delay);
		calbackResponseThread.start();
		
		//return 200 OK with body
		return ResponseDefinitionBuilder
                .like(responseDefinition).but()
                .withStatus(200).withBody(callbackResponse).withHeader("Content-Type", "text/xml")
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
				// TODO Auto-generated catch block
				LOGGER.debug("Exception :",e1);
			}
			LOGGER.debug("Sending callback response to url: " + callbackUrl);
			ClientRequest request = new ClientRequest(callbackUrl);
			request.body("text/xml", payLoad);
			//System.err.println(payLoad);
			try {
				ClientResponse result = request.post();
				LOGGER.debug("Successfully posted callback? Status: " + result.getStatus());
			} catch (Exception e) {
				// TODO Auto-generated catch block
			    LOGGER.debug("catch error in - request.post() ");
				LOGGER.debug("Exception :",e);
			}
		}
		
	}
}
