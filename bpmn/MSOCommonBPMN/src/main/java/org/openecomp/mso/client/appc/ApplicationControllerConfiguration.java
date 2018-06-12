package org.openecomp.mso.client.appc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class ApplicationControllerConfiguration {
	@Value("${appc.client.topic.read.name}")
	private String readTopic;

	@Value("${appc.client.topic.read.timeout}")
	private String readTimeout;

	@Value("${appc.client.response.timeout}")
	private String responseTimeout;

	@Value("${appc.client.topic.write}")
	private String write;

	@Value("${appc.client.poolMembers}")
	private String poolMembers;

	@Value("${appc.client.key}")
	private String clientKey;

	@Value("${appc.client.secret}")
	private String clientSecret;

	@Value("${appc.client.service}")
	private String service;

	public String getClientKey() {
		return clientKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getPoolMembers() {
		return poolMembers;
	}
	
	public String getReadTimeout() {
		return readTimeout;
	}

	public String getResponseTimeout() {
		return responseTimeout;
	}

	public String getReadTopic() {
		return readTopic;
	}

	public String getService() {
		return service;
	}

	public String getWrite() {
		return write;
	}
}

