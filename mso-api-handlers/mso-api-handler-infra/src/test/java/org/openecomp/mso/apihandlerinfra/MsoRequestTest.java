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

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;

import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceInstancesRequest;

public class MsoRequestTest {



	@Test
	public void testParseOrchestration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
			ObjectMapper mapper = new ObjectMapper();
			String requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"}}}";
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parseOrchestration(sir);
			assertEquals(msoRequest.getRequestInfo().getSource(),"VID");
			assertEquals(msoRequest.getRequestInfo().getRequestorId(),"zz9999");

	}

	@Test(expected = ValidationException.class)
	public void testParseOrchestrationFailure() throws JsonParseException, JsonMappingException, IOException, ValidationException{
			ObjectMapper mapper = new ObjectMapper();
			String requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\"}}}";
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parseOrchestration(sir);

	}

	@Test
	public void testParseV3VnfCreate() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3VnfCreate.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.createInstance, "v3");
			assertEquals(msoRequest.getRequestInfo().getSource(),"VID");
			assertFalse(msoRequest.getALaCarteFlag());
			assertEquals(msoRequest.getReqVersion(),3);
			boolean testIsALaCarteSet = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters().isaLaCarteSet();
			assertFalse(testIsALaCarteSet);

	}

	@Test(expected = ValidationException.class)
	public void testParseV3VolumeGroupFail() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3VolumeGroupBad.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.updateInstance, "v3");

	}

	@Test
	public void testParseV3UpdateNetwork() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3UpdateNetwork.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.updateInstance, "v3");

	}

	@Test(expected = ValidationException.class)
	public void testParseV3UpdateNetworkFail() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3UpdateNetworkBad.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.updateInstance, "v3");

	}

	@Test
	public void testParseV3DeleteNetwork() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3DeleteNetwork.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.deleteInstance, "v3");
	}

	@Test
	public void testParseV3ServiceInstanceDelete() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON1, requestJSON2;
		 try {
			  requestJSON1 = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3DeleteServiceInstance.json"));
			  requestJSON2 = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3DeleteServiceInstanceALaCarte.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON1, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.deleteInstance, "v3");
			boolean testIsALaCarteSet = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters().isaLaCarteSet();
			assertTrue(testIsALaCarteSet);
			assertFalse(msoRequest.getALaCarteFlag());
			sir  = mapper.readValue(requestJSON2, ServiceInstancesRequest.class);
			msoRequest = new MsoRequest ("12345");
			msoRequest.parse(sir, instanceIdMap, Action.deleteInstance, "v3");
			testIsALaCarteSet = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters().isaLaCarteSet();
			assertTrue(testIsALaCarteSet);
			assertTrue(msoRequest.getALaCarteFlag());

	}

	@Test(expected = ValidationException.class)
	public void testParseV3ServiceInstanceCreateFail() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON2;
		 try {
			  requestJSON2 = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3DeleteServiceInstanceALaCarte.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON2, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.createInstance, "v3");

	}

	@Test(expected = ValidationException.class)
	public void testParseV3ServiceInstanceDeleteMacroFail() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v3DeleteServiceInstanceBad.json"));

	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
			ObjectMapper mapper = new ObjectMapper();
			 HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.deleteInstance, "v3");

	}

	@Test
	public void testVfModuleV4UsePreLoad() throws JsonParseException, JsonMappingException, IOException, ValidationException {
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v4CreateVfModule.json"));
	           
	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
		 
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			instanceIdMap.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.createInstance, "v4");
			
			
			
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v4CreateVfModuleNoCustomizationId.json"));
	           
	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
		 
			mapper = new ObjectMapper();
			instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			instanceIdMap.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.createInstance, "v4");
	}
	
	@Test(expected = ValidationException.class)
	public void testV4UsePreLoadMissingModelCustomizationId() throws JsonParseException, JsonMappingException, IOException, ValidationException {
		String requestJSON;
		 try {
			  requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/v4CreateVfModuleMissingModelCustomizationId.json"));
	           
	        } catch (IOException e) {
	            fail ("Exception caught");
	            e.printStackTrace ();
	            return;
	        }
		 
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<>();
			instanceIdMap.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			instanceIdMap.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
			ServiceInstancesRequest sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
			MsoRequest msoRequest = new MsoRequest ("1234");
			msoRequest.parse(sir, instanceIdMap, Action.createInstance, "v4");
	}
}
