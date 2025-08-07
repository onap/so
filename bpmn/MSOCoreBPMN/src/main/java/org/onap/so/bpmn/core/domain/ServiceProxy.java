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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"modelInfo", "toscaNodeType", "description", "sourceModelUuid"})
@JsonRootName("serviceProxy")
public class ServiceProxy extends JsonWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("modelInfo")
    private ModelInfo modelInfo;
    @JsonProperty("toscaNodeType")
    private String toscaNodeType;
    @JsonProperty("description")
    private String description;
    @JsonProperty("sourceModelUuid")
    private String sourceModelUuid;

    @JsonProperty("modelInfo")
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    @JsonProperty("modelInfo")
    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    @JsonProperty("toscaNodeType")
    public String getToscaNodeType() {
        return toscaNodeType;
    }

    @JsonProperty("toscaNodeType")
    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("sourceModelUuid")
    public String getSourceModelUuid() {
        return sourceModelUuid;
    }

    @JsonProperty("sourceModelUuid")
    public void setSourceModelUuid(String sourceModelUuid) {
        this.sourceModelUuid = sourceModelUuid;
    }
}
