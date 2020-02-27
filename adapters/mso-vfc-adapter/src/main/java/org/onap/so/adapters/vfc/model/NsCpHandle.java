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

public class NsCpHandle {
    private String vnfInstanceId;
    private String vnfExtCpInstanceId;
    private String pnfInfoId;
    private String pnfExtCpInstanceId;
    private String nsInstanceId;
    private String nsSapInstanceId;

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getVnfExtCpInstanceId() {
        return vnfExtCpInstanceId;
    }

    public void setVnfExtCpInstanceId(String vnfExtCpInstanceId) {
        this.vnfExtCpInstanceId = vnfExtCpInstanceId;
    }

    public String getPnfInfoId() {
        return pnfInfoId;
    }

    public void setPnfInfoId(String pnfInfoId) {
        this.pnfInfoId = pnfInfoId;
    }

    public String getPnfExtCpInstanceId() {
        return pnfExtCpInstanceId;
    }

    public void setPnfExtCpInstanceId(String pnfExtCpInstanceId) {
        this.pnfExtCpInstanceId = pnfExtCpInstanceId;
    }

    public String getNsInstanceId() {
        return nsInstanceId;
    }

    public void setNsInstanceId(String nsInstanceId) {
        this.nsInstanceId = nsInstanceId;
    }

    public String getNsSapInstanceId() {
        return nsSapInstanceId;
    }

    public void setNsSapInstanceId(String nsSapInstanceId) {
        this.nsSapInstanceId = nsSapInstanceId;
    }
}
