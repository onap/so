package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/22.
 */
public class OutputEntity {
    @JsonProperty("svc-request-id")
    private String svcRequestId;

    @JsonProperty("response-code")
    private String responseCode;

    @JsonProperty("response-message")
    private String responseMessage;

    @JsonProperty("ack-final-indicator")
    private String ackFinalIndicator;

    @JsonProperty("network-response-information")
    private NetworkResponseInformationEntity networkResponseInformation;

    @JsonProperty("service-response-information")
    private ServiceResponseInformationEntity serviceResponseInformation;

    public String getSvcRequestId() {
        return svcRequestId;
    }

    public void setSvcRequestId(String svcRequestId) {
        this.svcRequestId = svcRequestId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAckFinalIndicator() {
        return ackFinalIndicator;
    }

    public void setAckFinalIndicator(String ackFinalIndicator) {
        this.ackFinalIndicator = ackFinalIndicator;
    }

    public NetworkResponseInformationEntity getNetworkResponseInformation() {
        return networkResponseInformation;
    }

    public void setNetworkResponseInformation(NetworkResponseInformationEntity networkResponseInformation) {
        this.networkResponseInformation = networkResponseInformation;
    }

    public ServiceResponseInformationEntity getServiceResponseInformation() {
        return serviceResponseInformation;
    }

    public void setServiceResponseInformation(ServiceResponseInformationEntity serviceResponseInformation) {
        this.serviceResponseInformation = serviceResponseInformation;
    }
}
