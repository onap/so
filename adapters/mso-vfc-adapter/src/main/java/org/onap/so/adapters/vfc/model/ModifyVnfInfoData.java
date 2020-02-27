/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import java.util.Map;

public class ModifyVnfInfoData {
    private String vnfInstanceId;
    private String vnfInstanceName;
    private String vnfInstanceDescription;
    private String vnfPkgId;
    private Map<String, Object> vnfConfigurableProperties;
    private Map<String, Object> Metadata;
    private Map<String, Object> Extensions;

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getVnfInstanceDescription() {
        return vnfInstanceDescription;
    }

    public void setVnfInstanceDescription(String vnfInstanceDescription) {
        this.vnfInstanceDescription = vnfInstanceDescription;
    }

    public String getVnfPkgId() {
        return vnfPkgId;
    }

    public void setVnfPkgId(String vnfPkgId) {
        this.vnfPkgId = vnfPkgId;
    }

    public Map<String, Object> getVnfConfigurableProperties() {
        return vnfConfigurableProperties;
    }

    public void setVnfConfigurableProperties(Map<String, Object> vnfConfigurableProperties) {
        this.vnfConfigurableProperties = vnfConfigurableProperties;
    }

    public Map<String, Object> getMetadata() {
        return Metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        Metadata = metadata;
    }

    public Map<String, Object> getExtensions() {
        return Extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        Extensions = extensions;
    }
}
