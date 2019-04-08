/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.appc.payload.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigScaleOutPayload {

    @JsonProperty("request-parameters")
    private RequestParametersConfigScaleOut requestParameters;
    @JsonProperty("configuration-parameters")
    private Map<String, String> configurationParameters = new HashMap<>();

    public RequestParametersConfigScaleOut getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(RequestParametersConfigScaleOut requestParameters) {
        this.requestParameters = requestParameters;
    }

    public Map<String, String> getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(Map<String, String> configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigScaleOutPayload{");
        sb.append("requestParameters=").append(requestParameters);
        sb.append(", configurationParameters='").append(configurationParameters);
        sb.append('}');
        return sb.toString();
    }
}
