package org.openecomp.mso.client.sdnc.beans;

public enum SDNCSvcOperation {

	VF_MODULE_TOPOLOGY_OPERATION("vf-module-topology-operation"),
	NETWORK_TOPOLOGY_OPERATION("network-topology-operation"),
	VNF_TOPOLOGY_OPERATION("vnf-topology-operation"),
	CONTRAIL_ROUTE_TOPOLOGY_OPERATION("contrail-route-topology-operation"),
	SECURITY_ZONE_TOPOLOGY_OPERATION("security-zone-topology-operation"),
	PORT_MIRROR_TOPOLOGY_OPERATION("port-mirror-topology-operation"),
	SERVICE_TOPOLOGY_OPERATION("service-topology-operation");
	
	private final String name;
	
	private SDNCSvcOperation(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
