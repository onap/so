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
import org.onap.so.openstack.beans.VnfRollback;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("VfModuleRollback")
@XmlRootElement(name = "VfModuleRollback")
public class VfModuleRollback {
    private String vnfId;
    private String vfModuleId;
    private String vfModuleStackId;
    private boolean vfModuleCreated = false;
    private String tenantId;
    private String cloudOwner;
    private String cloudSiteId;
    private MsoRequest msoRequest;
    private String messageId;
    private String mode = "HEAT"; // default

    public VfModuleRollback() {}

    public VfModuleRollback(VnfRollback vrb, String vfModuleId, String vfModuleStackId, String messageId) {
        this.vnfId = vrb.getVnfId();
        this.vfModuleCreated = vrb.getVnfCreated();
        this.tenantId = vrb.getTenantId();
        this.cloudOwner = vrb.getCloudOwner();
        this.cloudSiteId = vrb.getCloudSiteId();
        this.msoRequest = vrb.getMsoRequest();
        this.mode = vrb.getMode();
        this.vfModuleId = vfModuleId;
        this.vfModuleStackId = vfModuleStackId;
        this.messageId = messageId;
    }

    public VfModuleRollback(String vnfId, String vfModuleId, String vfModuleStackId, boolean vfModuleCreated,
            String tenantId, String cloudOwner, String cloudSiteId, MsoRequest msoRequest, String messageId) {
        super();
        this.vnfId = vnfId;
        this.vfModuleId = vfModuleId;
        this.vfModuleStackId = vfModuleStackId;
        this.vfModuleCreated = vfModuleCreated;
        this.tenantId = tenantId;
        this.cloudOwner = cloudOwner;
        this.cloudSiteId = cloudSiteId;
        this.msoRequest = msoRequest;
        this.messageId = messageId;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getVfModuleStackId() {
        return vfModuleStackId;
    }

    public void setVfModuleStackId(String vfModuleStackId) {
        this.vfModuleStackId = vfModuleStackId;
    }

    public boolean isVfModuleCreated() {
        return vfModuleCreated;
    }

    public void setVfModuleCreated(boolean vfModuleCreated) {
        this.vfModuleCreated = vfModuleCreated;
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

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
