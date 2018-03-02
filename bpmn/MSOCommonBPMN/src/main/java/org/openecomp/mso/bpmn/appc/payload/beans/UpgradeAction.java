package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"configuration-parameters"
})
public class UpgradeAction {

@JsonProperty("configuration-parameters")
private ConfigurationParametersUpgrade configurationParameters;

@JsonProperty("configuration-parameters")
public ConfigurationParametersUpgrade getConfigurationParameters() {
return configurationParameters;
}

@JsonProperty("configuration-parameters")
public void setConfigurationParameters(ConfigurationParametersUpgrade configurationParameters) {
this.configurationParameters = configurationParameters;
}
}