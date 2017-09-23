package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class EcompModelInformationEntity {
    @JsonProperty("model-invariant-uuid")
    private String modelInvariantUuid;

    @JsonProperty("model-customization-uuid")
    private String modelCustomizationUuid;

    @JsonProperty("model-uuid")
    private String modelUuid;

    @JsonProperty("model-version")
    private String modelVersion;

    @JsonProperty("model-name")
    private String modelName;

    public String getModelInvariantUuid() {
        return modelInvariantUuid;
    }

    public void setModelInvariantUuid(String modelInvariantUuid) {
        this.modelInvariantUuid = modelInvariantUuid;
    }

    public String getModelCustomizationUuid() {
        return modelCustomizationUuid;
    }

    public void setModelCustomizationUuid(String modelCustomizationUuid) {
        this.modelCustomizationUuid = modelCustomizationUuid;
    }

    public String getModelUuid() {
        return modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
