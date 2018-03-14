/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */


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