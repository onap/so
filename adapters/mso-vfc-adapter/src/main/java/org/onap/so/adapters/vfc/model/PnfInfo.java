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

public class PnfInfo {

    @NotNull
    private String pnfId;
    @NotNull
    private String pnfName;
    @NotNull
    private String pnfdId;
    @NotNull
    private String pnfdInfoId;
    @NotNull
    private String pnfProfileId;
    private List<PnfExtCpData> cpData;

    /***
     *
     * @return id of pnf
     */
    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public String getPnfdId() {
        return pnfdId;
    }

    public void setPnfdId(String pnfdId) {
        this.pnfdId = pnfdId;
    }

    public String getPnfProfileId() {
        return pnfProfileId;
    }

    public void setPnfProfileId(String pnfProfileId) {
        this.pnfProfileId = pnfProfileId;
    }

    public List<PnfExtCpData> getCpData() {
        return cpData;
    }

    public void setCpData(List<PnfExtCpData> cpData) {
        this.cpData = cpData;
    }
}
