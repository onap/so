/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.generalobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("license")
public class License implements Serializable {

    private static final long serialVersionUID = 2345786874755685318L;

    @JsonProperty("entitlement-pool-uuids")
    private List<String> entitlementPoolUuids = new ArrayList<>();
    @JsonProperty("license-key-group-uuids")
    private List<String> licenseKeyGroupUuids = new ArrayList<>();


    public List<String> getEntitlementPoolUuids() {
        return entitlementPoolUuids;
    }

    public void setEntitlementPoolUuids(List<String> entitlementPoolUuids) {
        this.entitlementPoolUuids = entitlementPoolUuids;
    }

    public List<String> getLicenseKeyGroupUuids() {
        return licenseKeyGroupUuids;
    }

    public void setLicenseKeyGroupUuids(List<String> licenseKeyGroupUuids) {
        this.licenseKeyGroupUuids = licenseKeyGroupUuids;
    }

}
