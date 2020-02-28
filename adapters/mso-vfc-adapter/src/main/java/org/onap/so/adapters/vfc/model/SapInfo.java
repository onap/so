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

public class SapInfo {
    @NotNull
    private String id;
    @NotNull
    private String sapdId;
    @NotNull
    private String sapName;
    @NotNull
    private String description;
    @NotNull
    private List<CpProtocolInfo> sapProtocolInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSapdId() {
        return sapdId;
    }

    public void setSapdId(String sapdId) {
        this.sapdId = sapdId;
    }

    public String getSapName() {
        return sapName;
    }

    public void setSapName(String sapName) {
        this.sapName = sapName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CpProtocolInfo> getSapProtocolInfo() {
        return sapProtocolInfo;
    }

    public void setSapProtocolInfo(List<CpProtocolInfo> sapProtocolInfo) {
        this.sapProtocolInfo = sapProtocolInfo;
    }
}
