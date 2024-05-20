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

package org.onap.so.adapters.vnfrest;


import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.entity.MsoRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("VolumeGroupRollback")
@XmlRootElement(name = "VolumeGroupRollback")
public class VolumeGroupRollback {
    // “volumeGroupRollback”: {
    // “volumeGroupId”: “<A&AI_ VOLUME_GROUP_ID>”,
    // “volumeGroupStackId”: “<VOLUME_GROUP _STACK_ID>”,
    // “tenantId”: “<TENANT_ID>”,
    // "cloudOwnerId"" "<CLOUD_OWNER>",
    // “cloudSiteId”: “<CLOUD_CLLI>”,
    // “volumeGroupCreated”: TRUE|FALSE,
    // “msoRequest”: {
    // “requestId”: “<REQUEST_ID>”,
    // “serviceInstanceId”: “<SERVICE_INSTANCE_ID>”
    // }
    // },

    private String volumeGroupId;
    private String volumeGroupStackId;
    private String tenantId;
    private String cloudOwnerId;
    private String cloudSiteId;
    private boolean volumeGroupCreated = false;
    private MsoRequest msoRequest;
    private String messageId;

    public VolumeGroupRollback() {}

    public VolumeGroupRollback(VolumeGroupRollback vrb, String volumeGroupStackId, String messageId) {
        this.volumeGroupId = vrb.getVolumeGroupId();
        this.volumeGroupStackId = volumeGroupStackId;
        this.tenantId = vrb.getTenantId();
        this.cloudOwnerId = vrb.getCloudOwnerId();
        this.cloudSiteId = vrb.getCloudSiteId();
        this.volumeGroupCreated = vrb.isVolumeGroupCreated();
        this.msoRequest = vrb.getMsoRequest();
        this.messageId = messageId;
    }

    public VolumeGroupRollback(String volumeGroupId, String volumeGroupStackId, boolean volumeGroupCreated,
            String tenantId, String cloudOwnerId, String cloudSiteId, MsoRequest msoRequest, String messageId) {
        super();
        this.volumeGroupId = volumeGroupId;
        this.volumeGroupStackId = volumeGroupStackId;
        this.volumeGroupCreated = volumeGroupCreated;
        this.tenantId = tenantId;
        this.cloudOwnerId = cloudOwnerId;
        this.cloudSiteId = cloudSiteId;
        this.msoRequest = msoRequest;
        this.messageId = messageId;
    }

    public String getVolumeGroupId() {
        return volumeGroupId;
    }

    public void setVolumeGroupId(String volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    public String getVolumeGroupStackId() {
        return volumeGroupStackId;
    }

    public void setVolumeGroupStackId(String volumeGroupStackId) {
        this.volumeGroupStackId = volumeGroupStackId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCloudSiteId() {
        return cloudSiteId;
    }

    public void setCloudSiteId(String cloudSiteId) {
        this.cloudSiteId = cloudSiteId;
    }

    public String getCloudOwnerId() {
        return cloudOwnerId;
    }

    public void setCloudOwnerId(String cloudOwnerId) {
        this.cloudOwnerId = cloudOwnerId;
    }

    public boolean isVolumeGroupCreated() {
        return volumeGroupCreated;
    }

    public void setVolumeGroupCreated(boolean volumeGroupCreated) {
        this.volumeGroupCreated = volumeGroupCreated;
    }

    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    public void setMsoRequest(MsoRequest msoRequest) {
        this.msoRequest = msoRequest;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
