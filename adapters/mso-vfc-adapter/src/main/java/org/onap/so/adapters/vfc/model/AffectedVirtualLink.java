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

public class AffectedVirtualLink {
    private String nsVirtualLinkInstanceId;
    private String nsVirtualLinkDescId;
    private String vlProfileId;

    private enum changeType {
        ADD, DELETE, MODIFY, ADD_LINK_PORT, REMOVE_LINK_PORT
    };
    private enum changeResult {
        COMPLETED, ROLLED_BACK, FAILED
    }

    public String getNsVirtualLinkInstanceId() {
        return nsVirtualLinkInstanceId;
    }

    public void setNsVirtualLinkInstanceId(String nsVirtualLinkInstanceId) {
        this.nsVirtualLinkInstanceId = nsVirtualLinkInstanceId;
    }

    public String getNsVirtualLinkDescId() {
        return nsVirtualLinkDescId;
    }

    public void setNsVirtualLinkDescId(String nsVirtualLinkDescId) {
        this.nsVirtualLinkDescId = nsVirtualLinkDescId;
    }

    public String getVlProfileId() {
        return vlProfileId;
    }

    public void setVlProfileId(String vlProfileId) {
        this.vlProfileId = vlProfileId;
    }
}
