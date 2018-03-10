/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SdncRequestHeaderEntity {
    @JsonProperty("GENERIC-RESOURCE-API:svc-request-id")
    private String svcRequestId;

    @JsonProperty("GENERIC-RESOURCE-API:svc-action")
    private String svcAction;

    @JsonProperty("GENERIC-RESOURCE-API:svc-notification-url")
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
