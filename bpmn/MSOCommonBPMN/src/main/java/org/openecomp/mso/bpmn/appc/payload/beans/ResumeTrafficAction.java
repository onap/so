package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"configuration-parameters"
})
public class ResumeTrafficAction {

@JsonProperty("configuration-parameters")
private ConfigurationParametersResumeTraffic configurationParameters;

@JsonProperty("configuration-parameters")
public ConfigurationParametersResumeTraffic getConfigurationParameters() {
return configurationParameters;
}

@JsonProperty("configuration-parameters")
public void setConfigurationParameters(ConfigurationParametersResumeTraffic configurationParameters) {
this.configurationParameters = configurationParameters;
}
}