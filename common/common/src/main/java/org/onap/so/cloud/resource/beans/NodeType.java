package org.onap.so.cloud.resource.beans;

public enum NodeType {
    BROWNFIELD("BROWNFIELD", "OVS", "bond1"), GREENFIELD("GREENFIELD", "OVS-DPDK", "bond0");

    private final String nodeType;
    private final String networkTech;
    private final String interfaceName;

    private NodeType(String s, String n, String h) {
        this.nodeType = s;
        this.networkTech = n;
        this.interfaceName = h;
    }

    public String getNetworkTechnologyName() {
        return networkTech;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public String toString() {
        return this.nodeType;
    }
}
