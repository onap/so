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
import java.util.List;

public class InstantiatedVnfInfo {
    @NotNull
    private String flavourId;
    @NotNull
    private String vnfState;
    private List<VnfScaleInfo> vnfScaleInfos;
    @NotNull
    private List<ExtCpInfo> extCpInfo;
    private List<ExtVirtualLinkInfo> extVirtualLinkInfo;
    private List<ExtManagedVirtualLinkInfo> extManagedVirtualLinkInfo;
    // Defination of MonitoringParameter is not there in ETSI document
    // considering as String
    private List<String> monitoringParameters;
    private String localizationLanguage;

    private List<VnfcResourceInfo> vnfcResourceInfo;
    // Defination of VirtualStorageResourceInfo is not there in ETSI document
    // considering as String
    private List<String> virtualStorageResourceInfo;

    public String getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(String flavourId) {
        this.flavourId = flavourId;
    }

    public String getVnfState() {
        return vnfState;
    }

    public void setVnfState(String vnfState) {
        this.vnfState = vnfState;
    }

    public List<VnfScaleInfo> getVnfScaleInfos() {
        return vnfScaleInfos;
    }

    public void setVnfScaleInfos(List<VnfScaleInfo> vnfScaleInfos) {
        this.vnfScaleInfos = vnfScaleInfos;
    }

    public List<ExtCpInfo> getExtCpInfo() {
        return extCpInfo;
    }

    public void setExtCpInfo(List<ExtCpInfo> extCpInfo) {
        this.extCpInfo = extCpInfo;
    }

    public List<ExtVirtualLinkInfo> getExtVirtualLinkInfo() {
        return extVirtualLinkInfo;
    }

    public void setExtVirtualLinkInfo(List<ExtVirtualLinkInfo> extVirtualLinkInfo) {
        this.extVirtualLinkInfo = extVirtualLinkInfo;
    }

    public List<ExtManagedVirtualLinkInfo> getExtManagedVirtualLinkInfo() {
        return extManagedVirtualLinkInfo;
    }

    public void setExtManagedVirtualLinkInfo(List<ExtManagedVirtualLinkInfo> extManagedVirtualLinkInfo) {
        this.extManagedVirtualLinkInfo = extManagedVirtualLinkInfo;
    }

    public List<String> getMonitoringParameters() {
        return monitoringParameters;
    }

    public void setMonitoringParameters(List<String> monitoringParameters) {
        this.monitoringParameters = monitoringParameters;
    }

    public String getLocalizationLanguage() {
        return localizationLanguage;
    }

    public void setLocalizationLanguage(String localizationLanguage) {
        this.localizationLanguage = localizationLanguage;
    }

    public List<VnfcResourceInfo> getVnfcResourceInfo() {
        return vnfcResourceInfo;
    }

    public void setVnfcResourceInfo(List<VnfcResourceInfo> vnfcResourceInfo) {
        this.vnfcResourceInfo = vnfcResourceInfo;
    }

    public List<String> getVirtualStorageResourceInfo() {
        return virtualStorageResourceInfo;
    }

    public void setVirtualStorageResourceInfo(List<String> virtualStorageResourceInfo) {
        this.virtualStorageResourceInfo = virtualStorageResourceInfo;
    }
}
