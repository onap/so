package org.openecomp.mso.apihandlerinfra.tenantisolation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.TenantIsolationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;

public class TenantIsolationRequestTest {

	@Test
	public void testParseCloudResourceECOMP() throws Exception{
		try {
			String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/ECOMPOperationEnvironmentCreate.json"));
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<String,String>();
			CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
			TenantIsolationRequest request = new TenantIsolationRequest ("1234");
			request.parse(cor, instanceIdMap, Action.create);
			assertNotNull(request.getRequestId());
		} catch(ValidationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseCloudResourceVNF() throws Exception{
		try {
			String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/VNFOperationEnvironmentCreate.json"));
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<String,String>();
			CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
			TenantIsolationRequest request = new TenantIsolationRequest ("1234");
			request.parse(cor, instanceIdMap, Action.create);
			assertNotNull(request.getRequestId());
		} catch(ValidationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ValidationException.class)
	public void testParseCloudResourceVNFInvalid() throws Exception {
		String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/VNFOperationEnvironmentCreateInvalid.json"));
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		TenantIsolationRequest request = new TenantIsolationRequest ("1234");
		request.parse(cor, instanceIdMap, Action.create);
		assertNotNull(request.getRequestId());
	}
	
	@Test
	public void testParseActivateCloudResource() throws Exception{
		try {
			String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/ActivateOperationEnvironment.json"));
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<String,String>();
			CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
			TenantIsolationRequest request = new TenantIsolationRequest ("1234");
			request.parse(cor, instanceIdMap, Action.activate);
			assertNotNull(request.getRequestId());
		} catch(ValidationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected = ValidationException.class)
	public void testParseActivateCloudResourceInvalid() throws Exception{
		String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/ActivateOperationEnvironmentInvalid.json"));
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		TenantIsolationRequest request = new TenantIsolationRequest ("1234");
		request.parse(cor, instanceIdMap, Action.activate);
		assertNotNull(request.getRequestId());
	}
	
	@Test
	public void testParseDeactivateCloudResource() throws Exception{
		try {
			String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/DeactivateOperationEnvironment.json"));
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> instanceIdMap = new HashMap<String,String>();
			CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
			TenantIsolationRequest request = new TenantIsolationRequest ("1234");
			request.parse(cor, instanceIdMap, Action.deactivate);
			assertNotNull(request.getRequestId());
		} catch(ValidationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected= ValidationException.class)
	public void testParseDeactivateCloudResourceInvalid() throws Exception{
		String requestJSON = IOUtils.toString (ClassLoader.class.getResourceAsStream ("/DeactivateOperationEnvironmentInvalid.json"));
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		CloudOrchestrationRequest cor  = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		TenantIsolationRequest request = new TenantIsolationRequest ("1234");
		request.parse(cor, instanceIdMap, Action.deactivate);
		assertNotNull(request.getRequestId());
	}
}