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

package org.onap.so.adapters.nwrest;



import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.entity.MsoRequest;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("deleteNetworkRequest")
@XmlRootElement(name = "deleteNetworkRequest")
public class DeleteNetworkRequest extends NetworkRequestCommon {

    /**
     * 
     */
    private static final long serialVersionUID = -2445072708572065058L;
    private String cloudSiteId;
    private String tenantId;
    private String networkId;
    private String networkStackId;
    private String networkType;
    private String modelCustomizationUuid;
    private MsoRequest msoRequest = new MsoRequest();

    public DeleteNetworkRequest() {
        // empty default constructor
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

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkStackId() {
        return networkStackId;
    }

    public void setNetworkStackId(String networkStackId) {
        this.networkStackId = networkStackId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getModelCustomizationUuid() {
        return this.modelCustomizationUuid;
    }

    public void setModelCustomizationUuid(String modelCustomizationUuid) {
        this.modelCustomizationUuid = modelCustomizationUuid;
    }

    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    public void setMsoRequest(MsoRequest msoRequest) {
        this.msoRequest = msoRequest;
    }
}
