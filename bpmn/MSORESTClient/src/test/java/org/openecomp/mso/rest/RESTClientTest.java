/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient;
import org.openecomp.mso.rest.RESTException;

/**
 * @version 1.0
 *
 */
public class RESTClientTest {

	@Test
	public void testSimpleHTTP() throws RESTException, ClientProtocolException, IOException {
		HttpClient mockHttpClient = mock(HttpClient.class);
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
		response.setEntity(new StringEntity("test","UTF-8"));
		when(mockHttpClient.execute(Mockito.<HttpUriRequest>any())).thenReturn(response);

		RESTClient restClient = new RESTClient("http://localhost");
		restClient.setUnitTestClient(mockHttpClient);
		APIResponse apiResponse = restClient.get();
		Assert.assertEquals(200, apiResponse.getStatusCode());
		Assert.assertEquals("test", apiResponse.getResponseBodyAsString());
	}
	
	@Test
	public void testSimpleHTTPS() throws RESTException, ClientProtocolException, IOException {
		HttpClient mockHttpClient = mock(HttpClient.class);
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
		response.setEntity(new StringEntity("test","UTF-8"));
		when(mockHttpClient.execute(Mockito.<HttpUriRequest>any())).thenReturn(response);

		RESTClient restClient = new RESTClient("https://localhost");
		restClient.setUnitTestClient(mockHttpClient);
		APIResponse apiResponse = restClient.get();
		Assert.assertEquals(200, apiResponse.getStatusCode());
		Assert.assertEquals("test", apiResponse.getResponseBodyAsString());
	}
	
}
