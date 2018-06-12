package org.openecomp.mso.client.restproperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:urn.properties")
public class PolicyPropertiesConfiguration {
	@Value("${policy.endpoint}")
	private String policyEndpoint;
	@Value("${policy.client.auth}")
	private String policyClientAuth;
	@Value("${policy.auth}")
	private String policyAuth;
	@Value("${policy.environment}")
	private String policyEnvironment;
	@Value("${policy.default.disposition}")
	private String policyDefaultDisposition;

	public String getPolicyEndpoint() {
		return policyEndpoint;
	}

	public String getPolicyClientAuth() {
		return policyClientAuth;
	}

	public String getPolicyAuth() {
		return policyAuth;
	}

	public String getPolicyEnvironment() {
		return policyEnvironment;
	}

	public String getPolicyDefaultDisposition() {
		return policyDefaultDisposition;
	}
}
