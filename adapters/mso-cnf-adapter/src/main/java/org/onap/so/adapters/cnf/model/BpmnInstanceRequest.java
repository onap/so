package org.onap.so.adapters.cnf.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class BpmnInstanceRequest {

    @JsonProperty(value = "modelInvariantId")
    private String modelInvariantId;

    @JsonProperty(value = "modelVersionId")
    private String modelVersionId;

    @JsonProperty(value = "k8sRBProfileName")
    private String k8sRBProfileName;

    @JsonProperty(value = "cloudRegionId")
    private String cloudRegionId;

    @JsonProperty(value = "vfModuleUUID")
    private String vfModuleUUID;

    @JsonProperty(value = "labels")
    private Map<String, String> labels;

    @JsonProperty(value = "overrideValues")
    private Map<String, String> overrideValues;

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getK8sRBProfileName() {
        return k8sRBProfileName;
    }

    public void setK8sRBProfileName(String k8sRBProfileName) {
        this.k8sRBProfileName = k8sRBProfileName;
    }

    public String getCloudRegionId() {
        return cloudRegionId;
    }

    public void setCloudRegionId(String cloudRegionId) {
        this.cloudRegionId = cloudRegionId;
    }

    public String getVfModuleUUID() {
        return vfModuleUUID;
    }

    public void setVfModuleUUID(String vfModuleUUID) {
        this.vfModuleUUID = vfModuleUUID;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(Map<String, String> overrideValues) {
        this.overrideValues = overrideValues;
    }

}
