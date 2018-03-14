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

import org.junit.Test;
import org.openecomp.mso.apihandler.camundabeans.CamundaResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class implements test methods of Camunda Beans.
 * 
 *
 */
public class CamundaResponseTest {

	@Test
	public final void testDeserialization() throws JsonGenerationException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		
		String responseBody = "{ \"response\": \"<xml>xml</xml>\","+
				  "\"messageCode\": 200,"+
				  "\"message\": \"Successfully started the process\"," +
				  "\"processInstanceID\":null,\"variables\":null}";
	
		CamundaResponse response = mapper.readValue(responseBody, CamundaResponse.class);
		assertEquals(response.toString(), "CamundaResponse [response=<xml>xml</xml>, messageCode=200, message=Successfully started the process]");

	}

}
