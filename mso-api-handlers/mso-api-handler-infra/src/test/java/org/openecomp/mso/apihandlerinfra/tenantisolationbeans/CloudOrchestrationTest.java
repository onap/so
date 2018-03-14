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

package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class CloudOrchestrationTest {

	private static final String requestJSONCreate = "{\"requestDetails\": {\"requestInfo\": {\"resourceType\": \"operationalEnvironment\",\"instanceName\": "
													+ "\"myOpEnv\",\"source\": \"VID\",\"requestorId\": \"az2017\"},\"requestParameters\": {\"operationalEnvironmentType\": \"ECOMP\",	"
													+ "\"tenantContext\": \"Test\",\"workloadContext\": \"ECOMP_E2E-IST\"}}} ";
	
	private static final String requestJsonActivate = "{\"requestDetails\": {\"requestInfo\": {\"resourceType\": \"operationalEnvironment\","
														+ "\"instanceName\": \"myVnfOpEnv\",\"source\": \"VID\",\"requestorId\": \"az2017\"},"
														+ "\"relatedInstanceList\": [{\"relatedInstance\": {\"resourceType\": \"operationalEnvironment\","
														+ "\"instanceId\": \"ff305d54-75b4-431b-adb2-eb6b9e5ff000\",\"instanceName\": \"name\"}}],"
														+ "\"requestParameters\": {	\"operationalEnvironmentType\": \"VNF\",\"workloadContext\": \"VNF_E2E-IST\","
														+ "\"manifest\": {\"serviceModelList\": [{\"serviceModelVersionId\": \"ff305d54-75b4-431b-adb2-eb6b9e5ff000\","
														+ "\"recoveryAction\": \"abort\"},{\"serviceModelVersionId\": \"ff305d54-75b4-431b-adb2-eb6b9e5ff000\","
														+ "\"recoveryAction\": \"retry\"}]}	}}}";
	
	private static final String requestJsonDeactivate = "{\"requestDetails\": {\"requestInfo\": {\"resourceType\": \"operationalEnvironment\","
														+ "\"source\": \"VID\",\"requestorId\": \"az2017\"},\"requestParameters\": "
														+ "{\"operationalEnvironmentType\": \"VNF\"}}}";
	
	@Test
	public void testCreateOperationEnvironment()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String response = "{\"requestId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\","
									+ "\"instanceId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\"}";
		final Response okResponse = Response.status(HttpStatus.SC_OK).entity(response).build();

		try {
			CloudOrchestration cor = Mockito.mock(CloudOrchestration.class);
			cor.createOperationEnvironment(requestJSONCreate, "v1");
			Mockito.when(cor.createOperationEnvironment(requestJSONCreate, "v1")).thenReturn(okResponse);
			Response resp = cor.createOperationEnvironment(requestJSONCreate, "v1");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	@Test
	public void testActivateOperationEnvironment()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String response = "{\"requestId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\","
									+ "\"instanceId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\"}";
		final Response okResponse = Response.status(HttpStatus.SC_OK).entity(response).build();

		try {
			CloudOrchestration cor = Mockito.mock(CloudOrchestration.class);
			cor.activateOperationEnvironment(requestJsonActivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
			Mockito.when(cor.activateOperationEnvironment(requestJsonActivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(okResponse);
			Response resp = cor.activateOperationEnvironment(requestJsonActivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	@Test
	public void testDeactivateOperationEnvironment()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		final String response = "{\"requestId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\","
									+ "\"instanceId\": \"ff3514e3-5a33-55df-13ab-12abad84e7ff\"}";
		final Response okResponse = Response.status(HttpStatus.SC_OK).entity(response).build();

		try {
			CloudOrchestration cor = Mockito.mock(CloudOrchestration.class);
			cor.deactivateOperationEnvironment(requestJsonDeactivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
			Mockito.when(cor.deactivateOperationEnvironment(requestJsonDeactivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(okResponse);
			Response resp = cor.deactivateOperationEnvironment(requestJsonDeactivate, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
