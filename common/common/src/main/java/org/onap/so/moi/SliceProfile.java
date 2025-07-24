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
@JsonPropertyOrder({"sliceProfileId", "plmnInfoList", "RANSliceSubnetProfile"})
public class SliceProfile {

    @JsonProperty("sliceProfileId")
    private String sliceProfileId = null;

    @JsonProperty("plmnInfoList")
    private List<PlmnInfo> plmnInfoList = null;

    @JsonProperty("RANSliceSubnetProfile")
    private RANSliceSubnetProfile rANSliceSubnetProfile;

    @JsonProperty("sliceProfileId")
    public String getSliceProfileId() {
        return sliceProfileId;
    }

    @JsonProperty("sliceProfileId")
    public void setSliceProfileId(String sliceProfileId) {
        this.sliceProfileId = sliceProfileId;
    }

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("plmnInfoList")
    public List<PlmnInfo> getPlmnInfoList() {
        return plmnInfoList;
    }

    @JsonProperty("plmnInfoList")
    public void setPlmnInfoList(List<PlmnInfo> plmnInfoList) {
        this.plmnInfoList = plmnInfoList;
    }

    @JsonProperty("RANSliceSubnetProfile")
    public RANSliceSubnetProfile getRANSliceSubnetProfile() {
        return rANSliceSubnetProfile;
    }

    @JsonProperty("RANSliceSubnetProfile")
    public void setrANSliceSubnetProfile(RANSliceSubnetProfile rANSliceSubnetProfile) {
        this.rANSliceSubnetProfile = rANSliceSubnetProfile;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
