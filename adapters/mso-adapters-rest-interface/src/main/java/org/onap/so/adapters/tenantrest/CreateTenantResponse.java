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



import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "createTenantResponse")
public class CreateTenantResponse implements Serializable {
    private static final long serialVersionUID = -456155026754759682L;
    private String cloudSiteId;
    private String tenantId;
    private Boolean tenantCreated;
    private TenantRollback tenantRollback = new TenantRollback();

    public CreateTenantResponse() {}

    public CreateTenantResponse(String cloudSiteId, String tenantId, Boolean tenantCreated,
            TenantRollback tenantRollback) {
        this.cloudSiteId = cloudSiteId;
        this.tenantId = tenantId;
        this.tenantCreated = tenantCreated;
        this.tenantRollback = tenantRollback;
    }

    public String getCloudSiteId() {
        return cloudSiteId;
    }

    public void setCloudSiteId(String cloudSiteId) {
        this.cloudSiteId = cloudSiteId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getTenantCreated() {
        return tenantCreated;
    }

    public void setTenantCreated(Boolean tenantCreated) {
        this.tenantCreated = tenantCreated;
    }

    public TenantRollback getTenantRollback() {
        return tenantRollback;
    }

    public void setTenantRollback(TenantRollback tenantRollback) {
        this.tenantRollback = tenantRollback;
    }

    @Override
    public String toString() {
        return "CreateTenantResponse [cloudSiteId=" + cloudSiteId + ", tenantId=" + tenantId + ", tenantCreated="
                + tenantCreated + ", tenantRollback=" + tenantRollback.toString() + "]";
    }
}
