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


import java.util.HashMap;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.mappers.MapAdapter;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("updateVolumeGroupRequest")
@XmlRootElement(name = "updateVolumeGroupRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateVolumeGroupRequest extends VfRequestCommon {
    private String cloudSiteId;
    private String cloudOwner;
    private String tenantId;
    private String volumeGroupId;
    private String volumeGroupStackId;
    private String vnfType;
    private String vnfVersion;
    private String vfModuleType;
    private String modelCustomizationUuid;
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, Object> volumeGroupParams = new HashMap<>();
    private MsoRequest msoRequest = new MsoRequest();

    public UpdateVolumeGroupRequest() {
        super();
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getVnfVersion() {
        return vnfVersion;
    }

    public void setVnfVersion(String vnfVersion) {
        this.vnfVersion = vnfVersion;
    }

    public String getVfModuleType() {
        return vfModuleType;
    }

    public void setVfModuleType(String vfModuleType) {
        this.vfModuleType = vfModuleType;
    }

    public String getModelCustomizationUuid() {
        return this.modelCustomizationUuid;
    }

    public void setModelCustomizationUuid(String modelCustomizationUuid) {
        this.modelCustomizationUuid = modelCustomizationUuid;
    }

    public Map<String, Object> getVolumeGroupParams() {
        return volumeGroupParams;
    }

    public void setVolumeGroupParams(Map<String, Object> volumeGroupParams) {
        this.volumeGroupParams = volumeGroupParams;
    }

    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    public void setMsoRequest(MsoRequest msoRequest) {
        this.msoRequest = msoRequest;
    }
}
