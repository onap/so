/*
 *  Copyright (C) 2019 Verizon. All Rights Reserved
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import javax.validation.constraints.NotNull;
import java.util.List;

public class VnffgInfo {
    @NotNull
    private String id;
    @NotNull
    private String vnffgdId;
    @NotNull
    private List<String> vnfInstanceId;
    private String pnfInfoId;
    @NotNull
    private List<String> nsVirtualLinkInfoId;
    @NotNull
    private List<NsCpHandle> nsCpHandle;
    @NotNull
    private List<NfpInfo> nfpInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVnffgdId() {
        return vnffgdId;
    }

    public void setVnffgdId(String vnffgdId) {
        this.vnffgdId = vnffgdId;
    }

    public List<String> getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(List<String> vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getPnfInfoId() {
        return pnfInfoId;
    }

    public void setPnfInfoId(String pnfInfoId) {
        this.pnfInfoId = pnfInfoId;
    }

    public List<String> getNsVirtualLinkInfoId() {
        return nsVirtualLinkInfoId;
    }

    public void setNsVirtualLinkInfoId(List<String> nsVirtualLinkInfoId) {
        this.nsVirtualLinkInfoId = nsVirtualLinkInfoId;
    }

    public List<NsCpHandle> getNsCpHandle() {
        return nsCpHandle;
    }

    public void setNsCpHandle(List<NsCpHandle> nsCpHandle) {
        this.nsCpHandle = nsCpHandle;
    }

    public List<NfpInfo> getNfpInfo() {
        return nfpInfo;
    }

    public void setNfpInfo(List<NfpInfo> nfpInfo) {
        this.nfpInfo = nfpInfo;
    }
}
