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

public class AffectedPnf {
    private String pnfid;
    private String pnfdid;
    private String pnfProfileId;
    private String pnfName;
    private String cpInstanceId;

    private enum changeType {
        ADD, REMOVE, MODIFY
    }
    private enum changeResult {
        COMPLETED, ROLLED_BACK, FAILED
    }

    public String getPnfid() {
        return pnfid;
    }

    public void setPnfid(String pnfid) {
        this.pnfid = pnfid;
    }

    public String getPnfdid() {
        return pnfdid;
    }

    public void setPnfdid(String pnfdid) {
        this.pnfdid = pnfdid;
    }

    public String getPnfProfileId() {
        return pnfProfileId;
    }

    public void setPnfProfileId(String pnfProfileId) {
        this.pnfProfileId = pnfProfileId;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public String getCpInstanceId() {
        return cpInstanceId;
    }

    public void setCpInstanceId(String cpInstanceId) {
        this.cpInstanceId = cpInstanceId;
    }
}
