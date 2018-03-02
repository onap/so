package org.openecomp.mso.client.grm.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "major" })
public class VersionLookup implements Serializable {

	private static final long serialVersionUID = 3802602253627725770L;

	@JsonProperty("major")
	private Integer major;
	
	@JsonProperty("major")
	public Integer getMajor() {
		return major;
	}

	@JsonProperty("major")
	public void setMajor(Integer major) {
		this.major = major;
	}
}