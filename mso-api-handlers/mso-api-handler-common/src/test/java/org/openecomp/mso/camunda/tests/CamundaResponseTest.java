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

import org.junit.Test;
import org.openecomp.mso.apihandler.camundabeans.CamundaResponse;
import org.openecomp.mso.utils.RootIgnoringObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class implements test methods of Camunda Beans.
 * 
 *
 */
public class CamundaResponseTest {

	@Test
	public final void testDeserializationWithoutRootElement() throws Exception {

		ObjectMapper mapper = new RootIgnoringObjectMapper<CamundaResponse>(CamundaResponse.class);

		String content = "{"
			+ "\"messageCode\":202"
			+ ",\"message\":\"Successfully started the process\""
			+ ",\"content\":\"<xml>xml</xml>\""
			+ ",\"processInstanceId\":\"4d3b3201a7ce\""
			+ ",\"variables\":null"
			+ "}";

		CamundaResponse response = mapper.readValue(content, CamundaResponse.class);

		assertEquals(
			"CamundaResponse[processInstanceId=4d3b3201a7ce,messageCode=202,message=Successfully started the process,variables=null,content=<xml>xml</xml>]",
			response.toString());
	}

	@Test
	public final void testDeserializationWithRootElement() throws Exception {

		ObjectMapper mapper = new RootIgnoringObjectMapper<CamundaResponse>(CamundaResponse.class);

		String content = "{\"WorkflowResponse\":{"
			+ "\"messageCode\":202"
			+ ",\"message\":\"Successfully started the process\""
			+ ",\"content\":\"<xml>xml</xml>\""
			+ ",\"processInstanceId\":\"4d3b3201a7ce\""
			+ ",\"variables\":null"
			+ "}}";

		CamundaResponse response = mapper.readValue(content, CamundaResponse.class);

		assertEquals(
			"CamundaResponse[processInstanceId=4d3b3201a7ce,messageCode=202,message=Successfully started the process,variables=null,content=<xml>xml</xml>]",
			response.toString());
	}
}