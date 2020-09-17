/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

package org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans;


import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for slice subnet capability query
 */
public class QuerySubnetCapability {

    @JsonProperty("subnetTypes")
    private List<SubnetTypes> subnetTypes;

    public List<SubnetTypes> getSubnetTypes() {
        return subnetTypes;
    }

    public void setSubnetTypes(List<SubnetTypes> subnetTypes) {
        this.subnetTypes = subnetTypes;
    }

    @Override
    public String toString() {
        return "QuerySubnetCapability [subnetType=" + subnetTypes + "]";
    }
}
