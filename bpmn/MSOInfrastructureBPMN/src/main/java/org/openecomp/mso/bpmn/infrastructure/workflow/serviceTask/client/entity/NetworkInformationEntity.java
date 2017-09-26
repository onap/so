package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkInformationEntity {
    @JsonProperty("network-id")
    private String networkId;

    @JsonProperty("network-type")
    private String networkType;

    @JsonProperty("onap-model-information")
    private OnapModelInformationEntity onapModelInformation;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public OnapModelInformationEntity getOnapModelInformation() {
        return onapModelInformation;
    }

    public void setOnapModelInformation(OnapModelInformationEntity onapModelInformation) {
        this.onapModelInformation = onapModelInformation;
    }
}
