package org.onap.so.adapters.cnf.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class InstanceEntity {

    @JsonProperty(value = "cloud-region")
    private String cloudRegion;

    @JsonProperty(value = "rb-name")
    private String rbName;

    @JsonProperty(value = "rb-version")
    private String rbVersion;

    @JsonProperty(value = "profile-name")
    private String profileName;

    @JsonProperty(value = "labels")
    private Map<String, String> labels;

    @JsonProperty(value = "override-values")
    private Map<String, String> overrideValues;

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getRbName() {
        return rbName;
    }

    public void setRbName(String rbName) {
        this.rbName = rbName;
    }

    public String getRbVersion() {
        return rbVersion;
    }

    public void setRbVersion(String rbVersion) {
        this.rbVersion = rbVersion;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
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
