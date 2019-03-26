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

public class NsInstance {
    @NotNull
    private String id;
    @NotNull
    private String nsInstanceName;
    @NotNull
    private String nsInstanceDescription;
    @NotNull
    private String nsdId;
    @NotNull
    private String nsdInfoId;
    private String flavourId;
    private List<VnfInstance> vnfInstance;
    private List<PnfInfo> pnfInfo;
    private List<NsVirtualLinkInfo> virtualLinkInfo;
    private List<VnffgInfo> vnffgInfo;
    private List<SapInfo> sapInfo;
    private List<String> nestedNsInstanceId;

    @NotNull
    private enum nsState {
        NOT_INSTANTIATED, INSTANTIATED
    };

    private List<NsScaleInfo> nsScaleStatus;
    private List<AffinityOrAntiAffinityRule> additionalAffinityOrAntiAffinityRule;
    @NotNull
    private NsInstanceLinks _links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNsInstanceName() {
        return nsInstanceName;
    }

    public void setNsInstanceName(String nsInstanceName) {
        this.nsInstanceName = nsInstanceName;
    }

    public String getNsInstanceDescription() {
        return nsInstanceDescription;
    }

    public void setNsInstanceDescription(String nsInstanceDescription) {
        this.nsInstanceDescription = nsInstanceDescription;
    }

    public String getNsdId() {
        return nsdId;
    }

    public void setNsdId(String nsdId) {
        this.nsdId = nsdId;
    }

    public String getNsdInfoId() {
        return nsdInfoId;
    }

    public void setNsdInfoId(String nsdInfoId) {
        this.nsdInfoId = nsdInfoId;
    }

    public String getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(String flavourId) {
        this.flavourId = flavourId;
    }

    public List<VnfInstance> getVnfInstance() {
        return vnfInstance;
    }

    public void setVnfInstance(List<VnfInstance> vnfInstance) {
        this.vnfInstance = vnfInstance;
    }

    public List<PnfInfo> getPnfInfo() {
        return pnfInfo;
    }

    public void setPnfInfo(List<PnfInfo> pnfInfo) {
        this.pnfInfo = pnfInfo;
    }

    public List<NsVirtualLinkInfo> getVirtualLinkInfo() {
        return virtualLinkInfo;
    }

    public void setVirtualLinkInfo(List<NsVirtualLinkInfo> virtualLinkInfo) {
        this.virtualLinkInfo = virtualLinkInfo;
    }

    public List<VnffgInfo> getVnffgInfo() {
        return vnffgInfo;
    }

    public void setVnffgInfo(List<VnffgInfo> vnffgInfo) {
        this.vnffgInfo = vnffgInfo;
    }

    public List<SapInfo> getSapInfo() {
        return sapInfo;
    }

    public void setSapInfo(List<SapInfo> sapInfo) {
        this.sapInfo = sapInfo;
    }

    public List<String> getNestedNsInstanceId() {
        return nestedNsInstanceId;
    }

    public void setNestedNsInstanceId(List<String> nestedNsInstanceId) {
        this.nestedNsInstanceId = nestedNsInstanceId;
    }

    public List<NsScaleInfo> getNsScaleStatus() {
        return nsScaleStatus;
    }

    public void setNsScaleStatus(List<NsScaleInfo> nsScaleStatus) {
        this.nsScaleStatus = nsScaleStatus;
    }

    public List<AffinityOrAntiAffinityRule> getAdditionalAffinityOrAntiAffinityRule() {
        return additionalAffinityOrAntiAffinityRule;
    }

    public void setAdditionalAffinityOrAntiAffinityRule(
            List<AffinityOrAntiAffinityRule> additionalAffinityOrAntiAffinityRule) {
        this.additionalAffinityOrAntiAffinityRule = additionalAffinityOrAntiAffinityRule;
    }

    public NsInstanceLinks get_links() {
        return _links;
    }

    public void set_links(NsInstanceLinks _links) {
        this._links = _links;
    }
}
