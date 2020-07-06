/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"modelType", "modelInvariantId", "modelVersionId", "modelName", "modelVersion",
        "modelCustomizationName"})
@JsonRootName("modelInfo")
public class ModelInfo implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("modelType")
    private String modelType;
    @JsonProperty("modelInvariantId")
    private String modelInvariantId;
    @JsonProperty("modelVersionId")
    private String modelVersionId;
    @JsonProperty("modelName")
    private String modelName;
    @JsonProperty("modelVersion")
    private String modelVersion;
    @JsonProperty("modelCustomizationName")
    private String modelCustomizationName;

    @JsonProperty("modelType")
    public String getModelType() {
        return modelType;
    }

    @JsonProperty("modelType")
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    @JsonProperty("modelInvariantId")
    public String getModelInvariantId() {
        return modelInvariantId;
    }

    @JsonProperty("modelInvariantId")
    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    @JsonProperty("modelVersionId")
    public String getModelVersionId() {
        return modelVersionId;
    }

    @JsonProperty("modelVersionId")
    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    @JsonProperty("modelName")
    public String getModelName() {
        return modelName;
    }

    @JsonProperty("modelName")
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @JsonProperty("modelVersion")
    public String getModelVersion() {
        return modelVersion;
    }

    @JsonProperty("modelVersion")
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @JsonProperty("modelCustomizationName")
    public String getModelCustomizationName() {
        return modelCustomizationName;
    }

    @JsonProperty("modelCustomizationName")
    public void setModelCustomizationName(String modelCustomizationName) {
        this.modelCustomizationName = modelCustomizationName;
    }

}
