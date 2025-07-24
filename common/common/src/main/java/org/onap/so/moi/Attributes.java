/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2023 DTAG Intellectual Property. All rights reserved.
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
package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sliceProfileList"})
public class Attributes {

    @JsonProperty("sliceProfileList")
    private List<SliceProfile> sliceProfileList = null;

    @JsonProperty("operationalState")
    private String operationalState;

    @JsonProperty("administrativeState")
    private String administrativeState;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sliceProfileList")
    public List<SliceProfile> getSliceProfileList() {
        return sliceProfileList;
    }

    @JsonProperty("sliceProfileList")
    public void setSliceProfileList(List<SliceProfile> sliceProfileList) {
        this.sliceProfileList = sliceProfileList;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("operationalState")
    public String getOperationalState() {
        return operationalState;
    }

    @JsonProperty("operationalState")
    public void setOperationalState(String operationalState) {
        this.operationalState = operationalState;
    }

    @JsonProperty("administrativeState")
    public String getAdministrativeState() {
        return administrativeState;
    }

    @JsonProperty("administrativeState")
    public void setAdministrativeState(String administrativeState) {
        this.administrativeState = administrativeState;
    }
}
