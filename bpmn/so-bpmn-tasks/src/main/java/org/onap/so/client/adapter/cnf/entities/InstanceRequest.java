
package org.onap.so.client.adapter.cnf.entities;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"rb-name", "rb-version", "profile-name", "release-name", "cloud-region", "labels",
        "override-values"})
public class InstanceRequest {

    @JsonProperty("rb-name")
    private String rbName;
    @JsonProperty("rb-version")
    private String rbVersion;
    @JsonProperty("profile-name")
    private String profileName;
    @JsonProperty("release-name")
    private String releaseName;
    @JsonProperty("cloud-region")
    private String cloudRegion;
    @JsonProperty("labels")
    private Labels labels;
    @JsonProperty(value = "override-values")
    private Map<String, String> overrideValues;

    @JsonProperty("rb-name")
    public String getRbName() {
        return rbName;
    }

    @JsonProperty("rb-name")
    public void setRbName(String rbName) {
        this.rbName = rbName;
    }

    @JsonProperty("rb-version")
    public String getRbVersion() {
        return rbVersion;
    }

    @JsonProperty("rb-version")
    public void setRbVersion(String rbVersion) {
        this.rbVersion = rbVersion;
    }

    @JsonProperty("profile-name")
    public String getProfileName() {
        return profileName;
    }

    @JsonProperty("profile-name")
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @JsonProperty("cloud-region")
    public String getCloudRegion() {
        return cloudRegion;
    }

    @JsonProperty("cloud-region")
    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    @JsonProperty("labels")
    public Labels getLabels() {
        return labels;
    }

    @JsonProperty("labels")
    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public Map<String, String> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(Map<String, String> overrideValues) {
        this.overrideValues = overrideValues;
    }

}
