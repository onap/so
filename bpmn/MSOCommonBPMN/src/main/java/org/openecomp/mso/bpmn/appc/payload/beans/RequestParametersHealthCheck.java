package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"vnf-name"
})
public class RequestParametersHealthCheck {

@JsonProperty("vnf-name")
private String vnfName;


@JsonProperty("vnf-name")
public String getVnfName() {
return vnfName;
}

@JsonProperty("vnf-name")
public void setVnfName(String vnfName) {
this.vnfName = vnfName;
}

}