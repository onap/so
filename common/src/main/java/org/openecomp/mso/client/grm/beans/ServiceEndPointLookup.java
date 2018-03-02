package org.openecomp.mso.client.grm.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "serviceEndPoint")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "version"})
public class ServiceEndPointLookup implements Serializable {

	private static final long serialVersionUID = 8867758152519088615L;

	@JsonProperty("name")
	private String name;
	@JsonProperty("version")
	private VersionLookup version;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("version")
	public VersionLookup getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(VersionLookup version) {
		this.version = version;
	}	
}
