
package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"configuration-parameters"
})
public class QuiesceTrafficAction {

@JsonProperty("configuration-parameters")
private ConfigurationParametersQuiesce configurationParameters;

@JsonProperty("configuration-parameters")
public ConfigurationParametersQuiesce getConfigurationParameters() {
return configurationParameters;
}

@JsonProperty("configuration-parameters")
public void setConfigurationParameters(ConfigurationParametersQuiesce configurationParameters) {
this.configurationParameters = configurationParameters;
}
}