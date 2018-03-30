/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.openecomp.mso.rest;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

public class APIResponseTest {
	
	@Test
	public void test() throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("firstName", "firstName1");
		jsonObject.put("lastName", "lastName1");
		String jsonObjectAsString= jsonObject.toString();
		HttpEntity entity = new StringEntity(jsonObjectAsString, ContentType.APPLICATION_JSON);
		ProtocolVersion ver = new ProtocolVersion("HTTP", 1, 1);		
		HttpResponse response = new BasicHttpResponse(ver, 1, "Ok");  
		response.setStatusLine(ver, 200);
		response.setEntity(entity);
		response.setHeader("name", "value");
		response.setStatusCode(200);
		APIResponse apiResponse = new APIResponse(response);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonObject.toString(), apiResponse.getResponseBodyAsString());
		assertEquals("value", apiResponse.getFirstHeader("name"));
		assertEquals(1, apiResponse.getAllHeaders().length);
		assertEquals(49, apiResponse.getResponseBodyAsByteArray().length);
	}
}