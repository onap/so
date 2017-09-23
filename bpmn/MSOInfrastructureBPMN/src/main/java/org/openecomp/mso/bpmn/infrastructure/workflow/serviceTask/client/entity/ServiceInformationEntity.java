package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class ServiceInformationEntity {
    @JsonProperty("service-id")
    private String serviceId;

    @JsonProperty("subscription-service-type")
    private String subscriptionServiceType;

    @JsonProperty("ecomp-model-information")
    private EcompModelInformationEntity ecompModelInformation;

    @JsonProperty("service-instance-id")
    private String serviceInstanceId;

    @JsonProperty("global-customer-id")
    private String globalCustomerId;

    @JsonProperty("subscriber-name")
    private String subscriberName;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public EcompModelInformationEntity getEcompModelInformation() {
        return ecompModelInformation;
    }

    public void setEcompModelInformation(EcompModelInformationEntity ecompModelInformation) {
        this.ecompModelInformation = ecompModelInformation;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getGlobalCustomerId() {
        return globalCustomerId;
    }

    public void setGlobalCustomerId(String globalCustomerId) {
        this.globalCustomerId = globalCustomerId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }
}
