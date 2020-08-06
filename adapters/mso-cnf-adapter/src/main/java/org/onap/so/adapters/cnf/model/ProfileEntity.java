package org.onap.so.adapters.cnf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class ProfileEntity {

    @JsonProperty(value = "rb-name")
    private String rbName;

    @JsonProperty(value = "rb-version")
    private String rbVersion;

    @JsonProperty(value = "profile-name")
    private String profileName;

    @JsonProperty(value = "release-name")
    private String releaseName;

    @JsonProperty(value = "namespace")
    private String nameSpace;

    @JsonProperty(value = "kubernetes-version")
    private String kubernetesVersion;

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

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getKubernetesVersion() {
        return kubernetesVersion;
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }
}
