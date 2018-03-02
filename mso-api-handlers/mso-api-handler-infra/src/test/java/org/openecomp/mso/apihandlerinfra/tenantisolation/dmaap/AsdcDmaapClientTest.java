package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.CreateEcompOperationEnvironmentBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AsdcDmaapClientTest {
	
	private final String fileLocation = "src/test/resources/org/openecomp/mso/client/asdc/create-ecompoe/";
		
	private static final String operationalEnvironmentId =  "28122015552391";
	private static final String operationalEnvironmentName = "Operational Environment Name";
	private static final String operationalEnvironmentType = "ECOMP";
	private static final String tenantContext = "TEST";
	private static final String workloadContext = "ECOMP_E2E-IST";
	private static final String action = "Create" ;	
	

	
	@Test
	public void verifyasdcCreateoeRequest() throws IOException, ParseException{
		
		ObjectMapper mapper = new ObjectMapper();
	
		String expected = "{\"operationalEnvironmentId\":\"28122015552391\",\"operationalEnvironmentName\":\"Operational Environment Name\",\"operationalEnvironmentType\":\"ECOMP\",\"tenantContext\":\"TEST\",\"workloadContext\":\"ECOMP_E2E-IST\",\"action\":\"Create\"}";
	
		
		CreateEcompOperationEnvironmentBean cBean = new CreateEcompOperationEnvironmentBean();
		cBean.setOperationalEnvironmentId(operationalEnvironmentId);
		cBean.setoperationalEnvironmentName(operationalEnvironmentName);
		cBean.setoperationalEnvironmentType(operationalEnvironmentType);
		cBean.settenantContext(tenantContext);
		cBean.setworkloadContext(workloadContext);
		cBean.setaction(action);
		
		String actual = mapper.writeValueAsString(cBean);
		
		assertEquals("payloads are equal", expected, actual);
	}
	
	
	
}
