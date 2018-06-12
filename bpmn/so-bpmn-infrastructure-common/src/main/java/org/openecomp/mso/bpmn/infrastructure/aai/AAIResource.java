package org.openecomp.mso.bpmn.infrastructure.aai;

import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.springframework.stereotype.Component;

@Component
public abstract class AAIResource {	
	private AAIResourcesClient aaiClient;

	public AAIResourcesClient getAaiClient() {
		if(aaiClient == null)
			return new AAIResourcesClient();
		else
			return aaiClient;
	}

	public void setAaiClient(AAIResourcesClient aaiClient) {
		this.aaiClient = aaiClient;
	}
}
