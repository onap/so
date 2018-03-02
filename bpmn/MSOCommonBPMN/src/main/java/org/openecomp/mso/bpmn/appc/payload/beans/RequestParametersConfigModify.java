package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"vnf-host-ip-address"
})
public class RequestParametersConfigModify {

@JsonProperty("vnf-host-ip-address")
private String vnfHostIpAddress;

@JsonProperty("vnf-host-ip-address")
public String getVnfHostIpAddress() {
return vnfHostIpAddress;
}

@JsonProperty("vnf-host-ip-address")
public void setVnfHostIpAddress(String vnfHostIpAddress) {
this.vnfHostIpAddress = vnfHostIpAddress;
}

}