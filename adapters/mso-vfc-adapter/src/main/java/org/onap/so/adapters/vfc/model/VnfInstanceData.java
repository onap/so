/***
 * Copyright (C) 2019 Verizon. All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

public class VnfInstanceData {
    private String vnfInstanceId;
    private String vnfProfileId;

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getVnfProfileId() {
        return vnfProfileId;
    }

    public void setVnfProfileId(String vnfProfileId) {
        this.vnfProfileId = vnfProfileId;
    }
}
