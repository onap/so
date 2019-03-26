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

import java.util.List;

public class PnfExtCpData {
    private String cpInstanceI16;
    private String cpdId;
    private List<CpProtocolData> cpProtocolData;

    public String getCpInstanceI16() {
        return cpInstanceI16;
    }

    public void setCpInstanceI16(String cpInstanceI16) {
        this.cpInstanceI16 = cpInstanceI16;
    }

    public String getCpdId() {
        return cpdId;
    }

    public void setCpdId(String cpdId) {
        this.cpdId = cpdId;
    }

    public List<CpProtocolData> getCpProtocolData() {
        return cpProtocolData;
    }

    public void setCpProtocolData(List<CpProtocolData> cpProtocolData) {
        this.cpProtocolData = cpProtocolData;
    }
}
