
package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"request-parameters",
"configuration-parameters"
})
public class HealthCheckAction {

@JsonProperty("request-parameters")
private RequestParametersHealthCheck requestParameters;
@JsonProperty("configuration-parameters")
private ConfigurationParametersHealthCheck configurationParameters;

@JsonProperty("request-parameters")
public RequestParametersHealthCheck getRequestParameters() {
return requestParameters;
}

@JsonProperty("request-parameters")
public void setRequestParameters(RequestParametersHealthCheck requestParameters) {
this.requestParameters = requestParameters;
}

@JsonProperty("configuration-parameters")
public ConfigurationParametersHealthCheck getConfigurationParameters() {
return configurationParameters;
}

@JsonProperty("configuration-parameters")
public void setConfigurationParameters(ConfigurationParametersHealthCheck configurationParameters) {
this.configurationParameters = configurationParameters;
}
}