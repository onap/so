package org.openecomp.mso.client.sdno.beans;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"client-name",
"code",
"processing-host",
"request-id",
"status"
})
public class ResultInfo {

@JsonProperty("client-name")
private String clientName;
@JsonProperty("code")
private String code;
@JsonProperty("processing-host")
private String processingHost;
@JsonProperty("request-id")
private String requestId;
@JsonProperty("status")
private String status;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("client-name")
public String getClientName() {
return clientName;
}

@JsonProperty("client-name")
public void setClientName(String clientName) {
this.clientName = clientName;
}

@JsonProperty("code")
public String getCode() {
return code;
}

@JsonProperty("code")
public void setCode(String code) {
this.code = code;
}

@JsonProperty("processing-host")
public String getProcessingHost() {
return processingHost;
}

@JsonProperty("processing-host")
public void setProcessingHost(String processingHost) {
this.processingHost = processingHost;
}

@JsonProperty("request-id")
public String getRequestId() {
return requestId;
}

@JsonProperty("request-id")
public void setRequestId(String requestId) {
this.requestId = requestId;
}

@JsonProperty("status")
public String getStatus() {
return status;
}

@JsonProperty("status")
public void setStatus(String status) {
this.status = status;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
