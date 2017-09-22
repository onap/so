package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class SdncRequestHeaderEntity {
    @JsonProperty("svc-request-id")
    private String svcRequestId;

    @JsonProperty("svc-action")
    private String svcAction;

    @JsonProperty("svc-notification-url")
    private String svcNotificationUrl;

    public String getSvcRequestId() {
        return svcRequestId;
    }

    public void setSvcRequestId(String svcRequestId) {
        this.svcRequestId = svcRequestId;
    }

    public String getSvcAction() {
        return svcAction;
    }

    public void setSvcAction(String svcAction) {
        this.svcAction = svcAction;
    }

    public String getSvcNotificationUrl() {
        return svcNotificationUrl;
    }

    public void setSvcNotificationUrl(String svcNotificationUrl) {
        this.svcNotificationUrl = svcNotificationUrl;
    }

}
