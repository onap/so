package org.onap.so.adapters.appc.orchestrator.client.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestParameters {

    @JsonProperty("vnf-host-ip-address")
    private String vnfHostIpAddress;
    @JsonProperty("vf-module-id")
    private String vfModuleId;
    @JsonProperty("vnf-name")
    private String vnfName;
    @JsonProperty("host-ip-address")
    private String hostIpAddress;

    public String getVnfHostIpAddress() {
        return vnfHostIpAddress;
    }

    public void setVnfHostIpAddress(String vnfHostIpAddress) {
        this.vnfHostIpAddress = vnfHostIpAddress;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getHostIpAddress() {
        return hostIpAddress;
    }

    public void setHostIpAddress(String hostIpAddress) {
        this.hostIpAddress = hostIpAddress;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestParametersConfigScaleOut{");
        sb.append("vnf-host-ip-address=").append(vnfHostIpAddress);
        sb.append(", vf-module-id='").append(vfModuleId);
        sb.append('}');
        return sb.toString();
    }

}
