package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class InputEntity {
    @JsonProperty("sdnc-request-header")
    private SdncRequestHeaderEntity sdncRequestHeader;

    @JsonProperty("request-information")
    private RequestInformationEntity requestInformation;

    @JsonProperty("service-information")
    private ServiceInformationEntity serviceInformation;

    @JsonProperty("network-information")
    private NetworkInformationEntity networkInformation;

    @JsonProperty("network-request-input")
    private NetworkRequestInputEntity networkRequestInput;

    public SdncRequestHeaderEntity getSdncRequestHeader() {
        return sdncRequestHeader;
    }

    public void setSdncRequestHeader(SdncRequestHeaderEntity sdncRequestHeader) {
        this.sdncRequestHeader = sdncRequestHeader;
    }

    public RequestInformationEntity getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(RequestInformationEntity requestInformation) {
        this.requestInformation = requestInformation;
    }

    public ServiceInformationEntity getServiceInformation() {
        return serviceInformation;
    }

    public void setServiceInformation(ServiceInformationEntity serviceInformation) {
        this.serviceInformation = serviceInformation;
    }

    public NetworkInformationEntity getNetworkInformation() {
        return networkInformation;
    }

    public void setNetworkInformation(NetworkInformationEntity networkInformation) {
        this.networkInformation = networkInformation;
    }

    public NetworkRequestInputEntity getNetworkRequestInput() {
        return networkRequestInput;
    }

    public void setNetworkRequestInput(NetworkRequestInputEntity networkRequestInput) {
        this.networkRequestInput = networkRequestInput;
    }
}
