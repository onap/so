/**
 * Copyright (C) 2020 Huawei, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.monitoring.rest.api;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"warFile", "savetoDB", "modelName", "modelVersionId", "operation", "orchestrationFlow",
        "modelType"})
public class ServiceRecipe {
    @JsonProperty("warFile")
    private String warFile;
    @JsonProperty("savetoDB")
    private String savetoDB;
    @JsonProperty("modelName")
    private String modelName;
    @JsonProperty("modelVersionId")
    private String modelVersionId;
    @JsonProperty("operation")
    private String operation;
    @JsonProperty("orchestrationFlow")
    private String orchestrationFlow;
    @JsonProperty("modelType")
    private String modelType;

    public String getWarFile() {
        return warFile;
    }

    public void setWarFile(String warFile) {
        this.warFile = warFile;
    }

    public String getSavetoDB() {
        return savetoDB;
    }

    public void setSavetoDB(String savetoDB) {
        this.savetoDB = savetoDB;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOrchestrationFlow() {
        return orchestrationFlow;
    }

    public void setOrchestrationFlow(String orchestrationFlow) {
        this.orchestrationFlow = orchestrationFlow;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}


