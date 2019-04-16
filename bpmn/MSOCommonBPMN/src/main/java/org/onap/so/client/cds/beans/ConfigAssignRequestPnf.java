/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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

package org.onap.so.client.cds.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"config-assign-properties", "resolution-key"})

public class ConfigAssignRequestPnf {
    @JsonProperty("resolution-key")
    private String resolutionKey;
    @JsonProperty("config-assign-properties")
    private ConfigAssignPropertiesForPnf configAssignPropertiesForPnf;

    public String getResolutionKey() {
        return resolutionKey;
    }

    public void setResolutionKey(String resolutionKey) {
        this.resolutionKey = resolutionKey;
    }

    public ConfigAssignPropertiesForPnf getConfigAssignPropertiesForPnf() {
        return configAssignPropertiesForPnf;
    }

    public void setConfigAssignPropertiesForPnf(ConfigAssignPropertiesForPnf configAssignPropertiesForPnf) {
        this.configAssignPropertiesForPnf = configAssignPropertiesForPnf;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{\"config-assign-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"config-assign-properties\":").append(configAssignPropertiesForPnf.toString());
        sb.append('}');
        sb.append('}');

        return sb.toString();
    }

}
