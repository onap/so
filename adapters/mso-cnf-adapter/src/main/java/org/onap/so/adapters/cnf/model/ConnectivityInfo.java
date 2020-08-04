package org.onap.so.adapters.cnf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class ConnectivityInfo {

    @JsonProperty(value = "cloud-region")
    private String cloudRegion;

    @JsonProperty(value = "cloud-owner")
    private String cloudOwner;

    @JsonProperty(value = "kubeconfig")
    private String kubeconfig;

    @JsonProperty(value = "other-connectivity-list")
    private OtherConnectivityListEntity otherConnectivityListEntity;

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public OtherConnectivityListEntity getOtherConnectivityListEntity() {
        return otherConnectivityListEntity;
    }

    public void setOtherConnectivityListEntity(OtherConnectivityListEntity otherConnectivityListEntity) {
        this.otherConnectivityListEntity = otherConnectivityListEntity;
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

    public void setKubeconfig(String kubeconfig) {
        this.kubeconfig = kubeconfig;
    }

}
