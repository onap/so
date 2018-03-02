package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"vnf_name",
"operations_timeout"
})
public class ConfigurationParametersQuiesce {

@JsonProperty("vnf_name")
private String vnfName;
@JsonProperty("operations_timeout")
private String operationsTimeout;

@JsonProperty("vnf_name")
public String getVnfName() {
return vnfName;
}

@JsonProperty("vnf_name")
public void setVnfName(String vnfName) {
this.vnfName = vnfName;
}

@JsonProperty("operations_timeout")
public String getOperationsTimeout() {
return operationsTimeout;
}

@JsonProperty("operations_timeout")
public void setOperationsTimeout(String operationsTimeout) {
this.operationsTimeout = operationsTimeout;
}

}