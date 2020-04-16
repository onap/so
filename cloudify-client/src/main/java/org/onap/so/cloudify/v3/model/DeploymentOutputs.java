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

package org.onap.so.cloudify.v3.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonRootName("outputs")
public class DeploymentOutputs implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("deployment_id")
    private String deploymentId;

    @JsonProperty("outputs")
    private Map<String, Object> outputs = null;

    public Map<String, Object> getOutputs() {
        return this.outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /*
     * Return an output as a Json-mapped Object of the provided type. This is useful for json-object outputs.
     */
    public <T> T getMapValue(Map<String, Object> map, String key, Class<T> type) {

        ObjectMapper mapper = new ObjectMapper();

        if (map.containsKey(key)) {
            try {
                String s = mapper.writeValueAsString(map.get(key));
                return (mapper.readValue(s, type));
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DeploymentOutputs{" + "deploymentId='" + deploymentId + '\'' + ", outputs='" + outputs + '\'' + '}';
    }

}
