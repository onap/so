/*
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

import javax.validation.constraints.NotNull;
import java.util.Map;

public class VnfInstance {
    @NotNull
    private String id;
    private String vnfInstanceName;
    private String vnfInstanceDescription;
    @NotNull
    private String vnfdId;
    @NotNull
    private String vnfProvider;
    @NotNull
    private String vnfProductName;
    @NotNull
    private String vnfSoftwareVersion;
    @NotNull
    private String vnfdVersion;
    @NotNull
    private String vnfPkgId;
    private Map<String, Object> vnfConfigurableProperties;
    private String vimId;

    private enum instantiationState {
        NOT_INSTANTIATED, INSTANTIATED
    };

    private InstantiatedVnfInfo instantiatedVnfInfo;
    private Map<String, Object> metadata;
    private Map<String, Object> extensions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public String getVnfProvider() {
        return vnfProvider;
    }

    public void setVnfProvider(String vnfProvider) {
        this.vnfProvider = vnfProvider;
    }

    public String getVnfProductName() {
        return vnfProductName;
    }

    public void setVnfProductName(String vnfProductName) {
        this.vnfProductName = vnfProductName;
    }

    public String getVnfSoftwareVersion() {
        return vnfSoftwareVersion;
    }

    public void setVnfSoftwareVersion(String vnfSoftwareVersion) {
        this.vnfSoftwareVersion = vnfSoftwareVersion;
    }

    public String getVnfdVersion() {
        return vnfdVersion;
    }

    public void setVnfdVersion(String vnfdVersion) {
        this.vnfdVersion = vnfdVersion;
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

    public String getVimId() {
        return vimId;
    }

    public void setVimId(String vimId) {
        this.vimId = vimId;
    }

    public InstantiatedVnfInfo getInstantiatedVnfInfo() {
        return instantiatedVnfInfo;
    }

    public void setInstantiatedVnfInfo(InstantiatedVnfInfo instantiatedVnfInfo) {
        this.instantiatedVnfInfo = instantiatedVnfInfo;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
