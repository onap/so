/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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



import java.util.HashMap;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.entity.MsoRequest;

@XmlRootElement(name = "createTenantRequest")
public class CreateTenantRequest extends TenantRequestCommon {
    private String cloudSiteId;
    private String tenantName;
    private Boolean failIfExists;
    private Boolean backout;
    private Map<String, String> metadata = new HashMap<>();
    private MsoRequest msoRequest = new MsoRequest();

    public CreateTenantRequest() {
        // empty constructor
    }

    public String getCloudSiteId() {
        return cloudSiteId;
    }

    public void setCloudSiteId(String cloudSiteId) {
        this.cloudSiteId = cloudSiteId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Boolean getFailIfExists() {
        return failIfExists;
    }

    public void setFailIfExists(Boolean failIfExists) {
        this.failIfExists = failIfExists;
    }

    public Boolean getBackout() {
        return backout;
    }

    public void setBackout(Boolean backout) {
        this.backout = backout;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    public void setMsoRequest(MsoRequest msoRequest) {
        this.msoRequest = msoRequest;
    }

    @Override
    public String toString() {
        return "CreateTenantRequest [cloudSiteId=" + cloudSiteId + ", tenantName=" + tenantName + ", failIfExists="
                + failIfExists + ", backout=" + backout + ", metadata=" + metadata + "]";
    }
}
