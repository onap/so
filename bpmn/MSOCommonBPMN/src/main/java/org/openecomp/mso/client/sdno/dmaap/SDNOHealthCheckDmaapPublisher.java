package org.openecomp.mso.client.sdno.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openecomp.mso.client.dmaap.DmaapPublisher;

public class SDNOHealthCheckDmaapPublisher extends DmaapPublisher {
	
	public SDNOHealthCheckDmaapPublisher() throws FileNotFoundException, IOException {
		super();
	}
	
	@Override
	public String getUserName() {
		return msoProperties.get("sdno.health-check.dmaap.username");
	}

	@Override
	public String getPassword() {
		return msoProperties.get("sdno.health-check.dmaap.password");
	}

	@Override
	public String getTopic() {
		return msoProperties.get("sdno.health-check.dmaap.publisher.topic");
	}
	

}
