package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class RequestInformationEntity {
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getOrderUnmber() {
        return orderUnmber;
    }

    public void setOrderUnmber(String orderUnmber) {
        this.orderUnmber = orderUnmber;
    }

    public String getOrerVersion() {
        return orerVersion;
    }

    public void setOrerVersion(String orerVersion) {
        this.orerVersion = orerVersion;
    }

    @JsonProperty("request-id")
    private String requestId;

    @JsonProperty("request-action")
    private String requestAction;

    @JsonProperty("source")
    private String source;

    @JsonProperty("notification-url")
    private String notificationUrl;

    @JsonProperty("order-number")
    private String orderUnmber;

    @JsonProperty("order-version")
    private String orerVersion;
}
