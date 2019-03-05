package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;

import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tenant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8704478668505531590L;
	@Id
	@JsonProperty("tenant-id")
	private String tenantId;
	@JsonProperty("tenant-name")
	private String tenantName;
	@JsonProperty("tenant-context")
	private String tenantContext;
	
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Tenant)) {
			return false;
		}
		Tenant castOther = (Tenant) other;
		return new EqualsBuilder().append(tenantId, castOther.tenantId).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(tenantId).toHashCode();
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public String getTenantContext() {
		return tenantContext;
	}
	public void setTenantContext(String tenantContext) {
		this.tenantContext = tenantContext;
	}

	
}