package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"request-parameters",
"configuration-parameters"
})
public class ConfigModifyAction {

@JsonProperty("request-parameters")
private RequestParametersConfigModify requestParameters;
@JsonProperty("configuration-parameters")
private ConfigurationParametersConfigModify configurationParameters;

@JsonProperty("request-parameters")
public RequestParametersConfigModify getRequestParameters() {
return requestParameters;
}

@JsonProperty("request-parameters")
public void setRequestParameters(RequestParametersConfigModify requestParameters) {
this.requestParameters = requestParameters;
}

@JsonProperty("configuration-parameters")
public ConfigurationParametersConfigModify getConfigurationParameters() {
return configurationParameters;
}

@JsonProperty("configuration-parameters")
public void setConfigurationParameters(ConfigurationParametersConfigModify configurationParameters) {
this.configurationParameters = configurationParameters;
}

}