/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.*;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "serviceInput", "serviceProperties", "serviceArtifact"})
@JsonRootName("serviceInfo")
public class ServiceInfo extends JsonWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("serviceInput")
    private String serviceInput;
    @JsonProperty("serviceProperties")
    private String serviceProperties;
    @JsonProperty("serviceArtifact")
    private List<ServiceArtifact> serviceArtifact = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("serviceInput")
    public String getServiceInput() {
        return serviceInput;
    }

    @JsonProperty("serviceInput")
    public void setServiceInput(String serviceInput) {
        this.serviceInput = serviceInput;
    }

    @JsonProperty("serviceProperties")
    public String getServiceProperties() {
        return serviceProperties;
    }

    @JsonProperty("serviceProperties")
    public void setServiceProperties(String serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @JsonProperty("serviceArtifact")
    public List<ServiceArtifact> getServiceArtifact() {
        return serviceArtifact;
    }

    @JsonProperty("serviceArtifact")
    public void setServiceArtifact(List<ServiceArtifact> serviceArtifact) {
        this.serviceArtifact = serviceArtifact;
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


