/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.sdnc.lcm.beans.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ipaddressV4Oam", "playbookName", "oldSwVersion", "targetSwVersion", "ruleName", "additionalData"})
public class UpgradePreCheckPayload {

    @JsonProperty(value = "ipaddress-v4-oam", required = true)
    private String ipaddressV4Oam;

    @JsonProperty(value = "playbook-name")
    private String playbookName;

    @JsonProperty(value = "oldSwVersion", required = true)
    private String oldSwVersion;

    @JsonProperty(value = "targetSwVersion", required = true)
    private String targetSwVersion;

    @JsonProperty(value = "ruleName", required = true)
    private String ruleName;

    @JsonProperty(value = "additionalData")
    private String additionalData;

    public String getIpaddressV4Oam() {
        return ipaddressV4Oam;
    }

    public void setIpaddressV4Oam(String value) {
        this.ipaddressV4Oam = value;
    }

    public String getPlaybookName() {
        return playbookName;
    }

    public void setPlaybookName(String value) {
        this.playbookName = value;
    }

    public String getOldSwVersion() {
        return oldSwVersion;
    }

    public void setOldSwVersion(String value) {
        this.oldSwVersion = value;
    }

    public String getTargetSwVersion() {
        return targetSwVersion;
    }

    public void setTargetSwVersion(String value) {
        this.targetSwVersion = value;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String value) {
        this.ruleName = value;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String value) {
        this.additionalData = value;
    }
}
