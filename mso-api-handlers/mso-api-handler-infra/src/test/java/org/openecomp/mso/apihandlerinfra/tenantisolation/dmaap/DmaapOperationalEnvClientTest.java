package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;

import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.CreateEcompOperationEnvironmentBean;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.DmaapOperationalEnvClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DmaapOperationalEnvClientTest {
	
	private final String fileLocation = "src/test/resources/org/openecomp/mso/client/asdc/create-ecompoe/";
	private static final String operationalEnvironmentId = "28122015552391";
	private static final String operationalEnvironmentName = "OpEnv-name";
	private static final String operationalEnvironmentType = "VNF";
	private static final String tenantContext = "Test";
	private static final String workloadContext = "VNF_E2E-IST";
	private static final String action = "Create";
	
	
	@Test
	public void verifyCreateEcompOperationEnvironmentRequest() throws IOException, ParseException{
		String content = this.getJson("ecomp-openv-request.json");
		ObjectMapper mapper = new ObjectMapper();
		CreateEcompOperationEnvironmentBean expected = mapper.readValue(content, CreateEcompOperationEnvironmentBean.class);
		DmaapOperationalEnvClient client = new DmaapOperationalEnvClient();
		DmaapOperationalEnvClient spy = spy(client);
		
		String actual = spy.buildRequest(operationalEnvironmentId, operationalEnvironmentName, operationalEnvironmentType, 
				tenantContext, workloadContext, action);
		
		assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);
	}
	
	
	private String getJson(String filename) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
	}
	
}
	
