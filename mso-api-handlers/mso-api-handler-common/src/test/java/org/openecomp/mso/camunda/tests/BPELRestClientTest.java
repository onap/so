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

package org.openecomp.mso.camunda.tests;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.openecomp.mso.apihandler.common.CommonConstants;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.properties.MsoJavaProperties;


/**
 * This class implements test methods of Camunda Beans.
 * 
 *
 */
public class BPELRestClientTest {



	@Mock
	private HttpClient mockHttpClient;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void tesBPELPost() throws JsonGenerationException,
	JsonMappingException, IOException {


		String responseBody ="<layer3activate:service-response xmlns:layer3activate=\"http://org.openecomp/mso/request/layer3serviceactivate/schema/v1\""
												+ "xmlns:reqtype=\"http://org.openecomp/mso/request/types/v1\""
												+ "xmlns:aetgt=\"http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd\""
												+ "xmlns:types=\"http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd\">"
												+ "<reqtype:request-id>req5</reqtype:request-id>"
												+ "<reqtype:request-action>Layer3ServiceActivateRequest</reqtype:request-action>"
												+ "<reqtype:source>OMX</reqtype:source>"
												+ "<reqtype:ack-final-indicator>n</reqtype:ack-final-indicator>"
												+ "</layer3activate:service-response>";
		
		HttpResponse mockResponse = createResponse(200, responseBody);
		mockHttpClient = Mockito.mock(HttpClient.class);
		Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class)))
		.thenReturn(mockResponse);

		String reqXML = "<xml>test</xml>";
		String orchestrationURI = "/active-bpel/services/REST/MsoLayer3ServiceActivate";

		MsoJavaProperties props = new MsoJavaProperties();
		props.setProperty(CommonConstants.BPEL_URL, "http://localhost:8089");
		props.setProperty("bpelAuth", "786864AA53D0DCD881AED1154230C0C3058D58B9339D2EFB6193A0F0D82530E1");

		RequestClient requestClient = RequestClientFactory.getRequestClient(orchestrationURI, props);
		requestClient.setClient(mockHttpClient);
		HttpResponse response = requestClient.post(reqXML, "reqId", "timeout", "version", null, null);


		int statusCode = response.getStatusLine().getStatusCode();
		assertEquals(requestClient.getType(), CommonConstants.BPEL);
		assertEquals(statusCode, HttpStatus.SC_OK);


	}

	private HttpResponse createResponse(int respStatus, 
			String respBody) {
		HttpResponse response = new BasicHttpResponse(
				new BasicStatusLine(
						new ProtocolVersion("HTTP", 1, 1), respStatus, ""));
		response.setStatusCode(respStatus);
		try {
			response.setEntity(new StringEntity(respBody));
			response.setHeader("Content-Type", "text/xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}





}
