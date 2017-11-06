package org.openecomp.mso.client.policy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.RestProperties;

public class PolicyRestProperties implements RestProperties {

	
	final Map<String, String> props;
	public PolicyRestProperties() {
		this.props = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");

	}
	@Override
	public URL getEndpoint() {
		try {
			return new URL(props.getOrDefault("policy.endpoint", ""));
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}
	
	public String getClientAuth() {
		return props.get("policy.client.auth");
	}
	
	public String getAuth() {
		return props.get("policy.auth");
	}
	
	public String getEnvironment() {
		return props.get("policy.environment");
	}

}
