package org.openecomp.mso.client.ruby.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.openecomp.mso.client.dmaap.DmaapPublisher;

public class RubyCreateTicketRequestPublisher extends DmaapPublisher{
	public RubyCreateTicketRequestPublisher() throws FileNotFoundException, IOException {
		super();
	}
	
	@Override
	public String getUserName() {
		return msoProperties.get("ruby.create-ticket-request.dmaap.username");
	}

	@Override
	public String getPassword() {
		return msoProperties.get("ruby.create-ticket-request.dmaap.password");
	}

	@Override
	public String getTopic() {
		return msoProperties.get("ruby.create-ticket-request.publisher.topic");
	}

	@Override
	public Optional<String> getHost() {
		return Optional.ofNullable(msoProperties.get("ruby.create-ticket-request.publisher.host"));
	}
	
}


