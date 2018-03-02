package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.client.grm.beans.Property;
import org.openecomp.mso.client.grm.beans.ServiceEndPointList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateVnfOperationalEnvironmentTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	private CloudOrchestrationRequest request;
	private ServiceEndPointList serviceEndpoints;
	private CreateVnfOperationalEnvironment spyProcess;
	
	@Before
	public void testSetUp() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
		MockitoAnnotations.initMocks(this);
		String jsonRequest = getFileContentsAsString("__files/vnfoperenv/createVnfOperationalEnvironmentRequest.json");
		request = mapper.readValue(jsonRequest, CloudOrchestrationRequest.class);
		String jsonServiceEndpoints = getFileContentsAsString("__files/vnfoperenv/endpoints.json");
		serviceEndpoints = mapper.readValue(jsonServiceEndpoints, ServiceEndPointList.class);
		CreateVnfOperationalEnvironment process  = new CreateVnfOperationalEnvironment(request, "9876543210");
		spyProcess = spy(process);
	}
	
	
	@Test
	public void testGetEcompManagingEnvironmentId() throws Exception { 
		when(spyProcess.getRequest()).thenReturn(request);
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff000", spyProcess.getEcompManagingEnvironmentId());
	}
	
	@Test
	public void testGetTenantContext() throws Exception { 
		when(spyProcess.getRequest()).thenReturn(request);
		assertEquals("Test", spyProcess.getTenantContext());
	}
	
	@Test
	public void testGetEnvironmentName() throws Exception {
		List<Property> props = serviceEndpoints.getServiceEndPointList().get(0).getProperties();
		assertEquals("DEV", spyProcess.getEnvironmentName(props));
	}
	
	@Test 
	public void testBuildServiceNameForVnf() throws Exception {
		when(spyProcess.getRequest()).thenReturn(request);
		assertEquals("Test.VNF_E2E-IST.Inventory", spyProcess.buildServiceNameForVnf("TEST.ECOMP_PSL.Inventory"));
	}
	
	@Test
	public void testGetSearchKey() {
		AAIOperationalEnvironment ecompEnv = new AAIOperationalEnvironment();
		ecompEnv.setTenantContext("Test");
		ecompEnv.setWorkloadContext("ECOMPL_PSL");
		assertEquals("Test.ECOMPL_PSL.*", spyProcess.getSearchKey(ecompEnv));
	}
	
	public String getFileContentsAsString(String fileName) {
		String content = "";
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());
			content = new String(Files.readAllBytes(file.toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
		}
		return content;
	}
	
}
