package org.openecomp.mso.client.restproperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AaiPropertiesConfiguration {

	@Value("${aai.endpoint}")
	private String endpoint;

	@Value("${aai.auth}")
	private String auth;

	public String getEndpoint() {
		return endpoint;
	}

	public String getAuth() {
		return auth;
	}
}
