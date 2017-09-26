package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRequestInputEntity {
    @JsonProperty("network-name")
    private String networkName;

    @JsonProperty("tenant")
    private String tenant;

    @JsonProperty("aic-cloud-region")
    private String aicCloudRegion;

    @JsonProperty("aic-clli")
    private String aicClli;

    @JsonProperty("network-input-parameters")
    private NetworkInputPaarametersEntity networkInputPaarameters;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getAicCloudRegion() {
        return aicCloudRegion;
    }

    public void setAicCloudRegion(String aicCloudRegion) {
        this.aicCloudRegion = aicCloudRegion;
    }

    public String getAicClli() {
        return aicClli;
    }

    public void setAicClli(String aicClli) {
        this.aicClli = aicClli;
    }

    public NetworkInputPaarametersEntity getNetworkInputPaarameters() {
        return networkInputPaarameters;
    }

    public void setNetworkInputPaarameters(NetworkInputPaarametersEntity networkInputPaarameters) {
        this.networkInputPaarameters = networkInputPaarameters;
    }
}
