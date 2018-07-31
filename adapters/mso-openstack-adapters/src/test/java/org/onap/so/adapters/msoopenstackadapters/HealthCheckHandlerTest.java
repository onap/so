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

package org.onap.so.adapters.msoopenstackadapters;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.junit.Test;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class HealthCheckHandlerTest extends BaseRestTestUtils {

	@Test
	public void testHealthcheckVnf() throws JSONException {

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/manage/health"),
				HttpMethod.GET, entity, String.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}
	
}
