package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenstackConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("username")
	String username;

	@JsonProperty("password")
	String password;

	@JsonProperty("tenant_name")
	String tenantName;

	@JsonProperty("auth_url")
	String authUrl;

	@JsonProperty("region")
	String region;

	// NOTE:  Not supporting "custom_configuration"

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	
}
