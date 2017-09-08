package org.openecomp.mso.bpmn.core.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRootName;


/**
 * Encapsulates Network resource data set
 *
 */
@JsonRootName("networkResource")
public class NetworkResource extends Resource {

	private static final long serialVersionUID = 1L;
	/*
	 * set resourceType for this object
	 */
	public NetworkResource(){
		resourceType = ResourceType.NETWORK;
		setResourceId(UUID.randomUUID().toString());
	}
	/*
	 * fields specific to Network resource type
	 */
	private String networkType;
	private String networkRole;
	private String networkTechnology;
	private String networkScope;
	
	/*
	 * GET and SET
	 */
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public String getNetworkRole() {
		return networkRole;
	}
	public void setNetworkRole(String networkRole) {
		this.networkRole = networkRole;
	}
	public String getNetworkTechnology() {
		return networkTechnology;
	}
	public void setNetworkTechnology(String networkTechnology) {
		this.networkTechnology = networkTechnology;
	}
	public String getNetworkScope() {
		return networkScope;
	}
	public void setNetworkScope(String networkScope) {
		this.networkScope = networkScope;
	}
}