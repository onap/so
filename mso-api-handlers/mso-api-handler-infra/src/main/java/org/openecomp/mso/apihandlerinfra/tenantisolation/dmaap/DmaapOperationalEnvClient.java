package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import java.io.IOException;


import org.openecomp.mso.client.dmaap.DmaapPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DmaapOperationalEnvClient {

	
	protected String buildRequest(String operationalEnvironmentId, String operationalEnvironmentName, String operationalEnvironmentType, String tenantContext, String workloadContext, String action ) 
					throws JsonProcessingException {
		final CreateEcompOperationEnvironmentBean operationalEnv = new CreateEcompOperationEnvironmentBean();
		operationalEnv.withOperationalEnvironmentId(operationalEnvironmentId)
	    	   .withOperationalEnvironmentName(operationalEnvironmentName)		
			   .withOperationalEnvironmentType(operationalEnvironmentType)
			   .withTenantContext(tenantContext)
			   .withWorkloadContext(workloadContext)
			   .withaction(action);
		 	
		return this.getJson(operationalEnv);
		
	}

	protected String getJson(CreateEcompOperationEnvironmentBean obj) throws JsonProcessingException {
		
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);
		
	}
	
	protected DmaapPublisher getPublisher() throws IOException {
		return new OperationalEnvironmentPublisher();
	}
	
	public void dmaapPublishOperationalEnvRequest(String operationalEnvironmentId, String operationalEnvironmentName, String operationalEnvironmentType, 
					String tenantContext, String workloadContext, String action ) throws Exception {
		
		String request = this.buildRequest(operationalEnvironmentId, operationalEnvironmentName, operationalEnvironmentType, tenantContext, workloadContext, action);
		final DmaapPublisher publisher = this.getPublisher();
		publisher.send(request);
		
	}
	
}
