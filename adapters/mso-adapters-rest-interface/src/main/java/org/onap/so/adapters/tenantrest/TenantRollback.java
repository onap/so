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

package org.onap.so.adapters.tenantrest;



import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.entity.MsoRequest;

/**
 * Javabean representing the rollback criteria following a "Create Tenant" operation. This structure can be passed back
 * to the "Rollback Tenant" operation to undo the effects of the create.
 *
 *
 */

@XmlRootElement(name = "rollbackTenantRequest")
public class TenantRollback extends TenantRequestCommon {
    private static final long serialVersionUID = -4540810517355635993L;
    private String tenantId;
    private String cloudId;
    private boolean tenantCreated = false;
    private MsoRequest msoRequest;

    public TenantRollback() {}

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public boolean getTenantCreated() {
        return tenantCreated;
    }

    public void setTenantCreated(boolean tenantCreated) {
        this.tenantCreated = tenantCreated;
    }

    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    public void setMsoRequest(MsoRequest msoRequest) {
        this.msoRequest = msoRequest;
    }

    @Override
    public String toString() {
        return "VnfRollback: cloud=" + cloudId + ", tenant=" + tenantId + ", tenantCreated=" + tenantCreated;
    }
}
