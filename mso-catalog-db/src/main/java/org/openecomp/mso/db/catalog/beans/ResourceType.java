package org.openecomp.mso.db.catalog.beans;

public enum ResourceType {
	SERVICE("Service"),
	VNF("Vnf"),
	VOLUME_GROUP("VolumeGroup"),
	VF_MODULE("VfModule"),
	NETWORK("Network"),
	CUSTOM("Custom");
	
	private final String name;
	
	private ResourceType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
