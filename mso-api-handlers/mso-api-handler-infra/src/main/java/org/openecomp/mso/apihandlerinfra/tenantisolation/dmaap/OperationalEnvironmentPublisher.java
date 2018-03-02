package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.openecomp.mso.client.dmaap.DmaapPublisher;

public class OperationalEnvironmentPublisher extends DmaapPublisher {

	
	public OperationalEnvironmentPublisher() throws FileNotFoundException, IOException {
		super();
	}
	
	@Override
	public String getUserName() {

		return this.msoProperties.get("so.operational-environment.dmaap.username");
	}

	@Override
	public String getPassword() {

		return this.msoProperties.get("so.operational-environment.dmaap.password");
	}

	@Override
	public String getTopic() {
		
		return this.msoProperties.get("so.operational-environment.publisher.topic");
	}

	@Override
	public Optional<String> getHost() {
		return Optional.ofNullable(this.msoProperties.get("so.operational-environment.dmaap.host"));
	}
}