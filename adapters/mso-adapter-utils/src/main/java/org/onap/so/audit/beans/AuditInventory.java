/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.audit.beans;

import java.io.Serializable;

public class AuditInventory implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4937350343452380760L;

    private String msoRequestId;

    private String cloudRegion;

    private String cloudOwner;

    private String tenantId;

    private String vfModuleId;

    private String heatStackName;

    private String genericVnfId;

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHeatStackName() {
        return heatStackName;
    }

    public void setHeatStackName(String heatStackName) {
        this.heatStackName = heatStackName;
    }

    public String getGenericVnfId() {
        return genericVnfId;
    }

    public void setGenericVnfId(String genericVnfId) {
        this.genericVnfId = genericVnfId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getMsoRequestId() {
        return msoRequestId;
    }

    public void setMsoRequestId(String msoRequestId) {
        this.msoRequestId = msoRequestId;
    }



}
