package org.openecomp.mso.client.grm.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "major", "minor", "patch" })
public class Version {

	@JsonProperty("major")
	private Integer major;
	@JsonProperty("minor")
	private Integer minor;
	@JsonProperty("patch")
	private String patch;

	@JsonProperty("major")
	public Integer getMajor() {
		return major;
	}

	@JsonProperty("major")
	public void setMajor(Integer major) {
		this.major = major;
	}

	@JsonProperty("minor")
	public Integer getMinor() {
		return minor;
	}

	@JsonProperty("minor")
	public void setMinor(Integer minor) {
		this.minor = minor;
	}

	@JsonProperty("patch")
	public String getPatch() {
		return patch;
	}

	@JsonProperty("patch")
	public void setPatch(String patch) {
		this.patch = patch;
	}
}