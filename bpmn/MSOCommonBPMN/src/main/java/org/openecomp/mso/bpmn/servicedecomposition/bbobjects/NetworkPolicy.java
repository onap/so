package org.openecomp.mso.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openecomp.mso.bpmn.servicedecomposition.ShallowCopy;

public class NetworkPolicy implements Serializable, ShallowCopy<NetworkPolicy>{

	private static final long serialVersionUID = 8925599588239522447L;
	
	@Id
	@JsonProperty("network-policy-id")
	private String networkPolicyId;
	@JsonProperty("network-policy-fqdn")
	private String networkPolicyFqdn;
	@JsonProperty("heat-stack-id")
	private String heatStackId;
	@JsonProperty("resource-version")
	private String resourceVersion;
	
	public String getNetworkPolicyId() {
		return this.networkPolicyId;
	}
	
	public void setNetworkPolicyId(String networkPolicyId) {
		this.networkPolicyId = networkPolicyId;
	}
	
	public String getNetworkPolicyFqdn() {
		return this.networkPolicyFqdn;
	}
	
	public void setNetworkPolicyFqdn(String networkPolicyFqdn) {
		this.networkPolicyFqdn = networkPolicyFqdn;
	}
	
	public String getHeatStackId() {
		return this.heatStackId;
	}
	
	public void setHeatStackId(String heatStackId) {
		this.heatStackId = heatStackId;
	}
	
	public String getResourceVersion() {
		return this.resourceVersion;
	}
	
	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof NetworkPolicy)) {
			return false;
		}
		NetworkPolicy castOther = (NetworkPolicy) other;
		return new EqualsBuilder().append(networkPolicyId, castOther.networkPolicyId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(networkPolicyId).toHashCode();
	}
}
