package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class ServiceTopologyOperationInputEntity {
    @JsonProperty("sdnc-request-header")
    private SdncRequestHeaderEntity sdncRequestHeader;

    @JsonProperty("request-information")
    private RequestInformationEntity requestInformation;

    @JsonProperty("service-information")
    private ServiceInformationEntity serviceInformation;

    @JsonProperty("service-request-input")
    private ServiceRequestInputEntity serviceRequestInput;

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

    public ServiceRequestInputEntity getServiceRequestInput() {
        return serviceRequestInput;
    }

    public void setServiceRequestInput(ServiceRequestInputEntity serviceRequestInput) {
        this.serviceRequestInput = serviceRequestInput;
    }
}
