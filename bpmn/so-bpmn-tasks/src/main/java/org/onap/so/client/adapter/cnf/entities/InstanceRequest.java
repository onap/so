
package org.onap.so.client.adapter.cnf.entities;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstanceRequest {

    @JsonProperty("modelInvariantId")
    private String modelInvariantId;
    @JsonProperty("modelVersionId")
    private String modelVersionId;
    @JsonProperty("modelCustomizationId")
    private String modelCustomizationId;
    @JsonProperty("k8sRBProfileName")
    private String profileName;
    @JsonProperty("k8sRBInstanceReleaseName")
    private String releaseName;
    @JsonProperty("k8sRBInstanceStatusCheck")
    private Boolean statusCheck = false;
    @JsonProperty("vfModuleUUID")
    private String vfModuleUUID;
    @JsonProperty("cloudRegionId")
    private String cloudRegion;
    @JsonProperty("labels")
    private Map<String, String> labels;
    @JsonProperty("override-values")
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

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Boolean getStatusCheck() {
        return statusCheck;
    }

    public void setStatusCheck(Boolean statusCheck) {
        this.statusCheck = statusCheck;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getVfModuleUUID() {
        return vfModuleUUID;
    }

    public void setVfModuleUUID(String vfModuleUUID) {
        this.vfModuleUUID = vfModuleUUID;
    }

    public Map<String, String> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(Map<String, String> overrideValues) {
        this.overrideValues = overrideValues;
    }

}
