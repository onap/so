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
package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.ValidationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ServiceInstancesTest {

	private static final String requestJSONCreate = "{ \"requestDetails\": { \"modelInfo\": { \"modelType\": \"service\", "
			+ "\"modelInvariantId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\","
			+ " \"modelVersionId\": \"fe6985cd-ea33-3346-ac12-ab121484a3fe\", \"modelName\": \"Test\","
			+ " \"modelVersion\": \"1.0\" }, \"cloudConfiguration\": "
			+ "{ \"lcpCloudRegionId\": \"mdt1\", \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" },"
			+ " \"subscriberInfo\": { \"globalSubscriberId\": \"{some subscriber id}\","
			+ " \"subscriberName\": \"{some subscriber name}\" },"
			+ " \"requestInfo\": { \"productFamilyId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\", "
			+ "\"source\": \"VID\", \"suppressRollback\": true, \"requestorId\": \"az2016\" },"
			+ " \"requestParameters\": { \"subscriptionServiceType\": \"MOG\", \"aLaCarte\": false,"
			+ " \"userParams\": [ { \"name\": \"someUserParam\", \"value\": \"someValue\" } ] } } } ";
	
	private static final String requestJSONActivateDeacivate =
			"{ \"requestDetails\": { \"modelInfo\": { \"modelType\": \"service\","
			+ " \"modelInvariantId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\", "
			+ "\"modelVersionId\": \"fe6985cd-ea33-3346-ac12-ab121484a3fe\", "
			+ "\"modelName\": \"Test\", \"modelVersion\": \"1.0\" }, "
			+ "\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"az2016\" }, "
			+ "\"requestParameters\": { \"userParams\": [ { \"name\": \"aic_zone\", "
			+ "\"value\": \"someValue\" } ] } } } ";

	private static final String requestJSONDelete =
			"{ \"requestDetails\": { \"modelInfo\": { \"modelType\":\"network\", "
			+ "\"modelName\":\"CONTRAIL30_BASIC\" }, \"cloudConfiguration\": { \"lcpCloudRegionId\":\"mdt1\", "
			+ "\"tenantId\":\"8b1df54faa3b49078e3416e21370a3ba\" }, "
			+ "\"requestInfo\": { \"source\":\"VID\", \"requestorId\":\"az2016\" } } }";
	
	@Test
	public void testCreateServiceInstance()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title></title></head><body></body></html>";
		final Response SERVICE_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();

		try {
			ServiceInstances sir = Mockito.mock(ServiceInstances.class);
			sir.createServiceInstance(requestJSONCreate, "v3");
			Mockito.when(sir.createServiceInstance(requestJSONCreate, "v3")).thenReturn(SERVICE_RESPONSE);
			Response resp = sir.createServiceInstance(requestJSONCreate, "v3");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testActivateServiceInstance()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title></title></head><body></body></html>";
		final Response SERVICE_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();
		try {
			ServiceInstances sir = Mockito.mock(ServiceInstances.class);
			Mockito.when(sir.activateServiceInstance(requestJSONActivateDeacivate, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc"))
					.thenReturn(SERVICE_RESPONSE);
			Response resp = sir.activateServiceInstance(requestJSONActivateDeacivate, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testDeactivateServiceInstance()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title></title></head><body></body></html>";
		final Response SERVICE_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();
		try {
			ServiceInstances sir = Mockito.mock(ServiceInstances.class);
			Mockito.when(sir.deactivateServiceInstance(requestJSONActivateDeacivate, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc"))
					.thenReturn(SERVICE_RESPONSE);
			Response resp = sir.deactivateServiceInstance(requestJSONActivateDeacivate, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteServiceInstance()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
		final Response SERVICE_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();
		try {
			ServiceInstances sir = Mockito.mock(ServiceInstances.class);
			Mockito.when(sir.deleteServiceInstance(requestJSONDelete, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc"))
					.thenReturn(SERVICE_RESPONSE);
			Response resp = sir.deleteServiceInstance(requestJSONDelete, "v5", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}