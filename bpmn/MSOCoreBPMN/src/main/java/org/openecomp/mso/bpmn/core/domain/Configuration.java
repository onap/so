package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Stores configuration information and modeled off
 * of the AAI configuration object
 *
 */
@JsonRootName("configuration")
public class Configuration extends JsonWrapper implements Serializable{

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String type;
	private String orchestrationStatus;
	private String tunnelBandwidth;
	private String vendorAllowedMaxBandwidth;
	private String resourceVersion;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getOrchestrationStatus() {
		return orchestrationStatus;
	}
	public void setOrchestrationStatus(String orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}
	public String getTunnelBandwidth() {
		return tunnelBandwidth;
	}
	public void setTunnelBandwidth(String tunnelBandwidth) {
		this.tunnelBandwidth = tunnelBandwidth;
	}
	public String getVendorAllowedMaxBandwidth() {
		return vendorAllowedMaxBandwidth;
	}
	public void setVendorAllowedMaxBandwidth(String vendorAllowedMaxBandwidth) {
		this.vendorAllowedMaxBandwidth = vendorAllowedMaxBandwidth;
	}
	public String getResourceVersion() {
		return resourceVersion;
	}
	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}


}
