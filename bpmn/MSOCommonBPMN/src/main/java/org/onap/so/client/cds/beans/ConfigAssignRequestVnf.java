
package org.onap.so.client.cds.beans;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"config-assign-properties",
"resolution-key"
})
public class ConfigAssignRequestVnf {
	@JsonProperty("resolution-key")
	private String resolutionKey;
	@JsonProperty("config-assign-properties")
	private ConfigAssignPropertiesForVnf configAssignPropertiesForVnf;
	
	public String getResolutionKey() {
		return resolutionKey;
	}
	
	public void setResolutionKey(String resolutionKey) {
		this.resolutionKey = resolutionKey;
	}
	
	public ConfigAssignPropertiesForVnf getConfigAssignPropertiesForVnf() {
		return configAssignPropertiesForVnf;
	}

	public void setConfigAssignPropertiesForVnf(ConfigAssignPropertiesForVnf configAssignPropertiesForVnf) {
		this.configAssignPropertiesForVnf = configAssignPropertiesForVnf;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{\"config-assign-request\":{");
		sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
		sb.append(", \"config-assign-properties\":").append(configAssignPropertiesForVnf.toString());
		sb.append('}');
		sb.append('}');
		
		return sb.toString();
	}
	
}
