/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.beans.nsmf;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Model class for slice subnet capability query
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuerySubnetCapability {

    @JsonProperty("subnetTypes")
    private List<String> subnetTypes;

    public List<String> getSubnetTypes() {
        return subnetTypes;
    }

    public void setSubnetTypes(List<String> subnetTypes) {
        this.subnetTypes = subnetTypes;
    }

    @Override
    public String toString() {
        return "QuerySubnetCapability [subnetType=" + subnetTypes + "]";
    }
}
