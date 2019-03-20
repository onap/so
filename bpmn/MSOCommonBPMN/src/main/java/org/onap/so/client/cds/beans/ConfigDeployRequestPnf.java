package org.onap.so.client.cds.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"config-deploy-properties",
"resolution-key"
})
public class ConfigDeployRequestPnf {
	@JsonProperty("resolution-key")
	private String resolutionKey;

	@JsonProperty("config-deploy-properties")
	private ConfigDeployPropertiesForPnf configDeployPropertiesForPnf;

	public String getResolutionKey() {
		return resolutionKey;
	}

	public void setResolutionKey(String resolutionKey) {
		this.resolutionKey = resolutionKey;
	}

	public ConfigDeployPropertiesForPnf getConfigDeployPropertiesForPnf() {
		return configDeployPropertiesForPnf;
	}

	public void setConfigDeployPropertiesForPnf(ConfigDeployPropertiesForPnf configDeployPropertiesForPnf) {
		this.configDeployPropertiesForPnf = configDeployPropertiesForPnf;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{\"config-deploy-request\":{");
		sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
		sb.append(", \"config-deploy-properties\":").append(configDeployPropertiesForPnf.toString());
		sb.append('}');
		sb.append('}');

		return sb.toString();
	}

}
