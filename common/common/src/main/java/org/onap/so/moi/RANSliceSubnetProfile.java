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
import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"coverageAreaTAList", "dLLatency", "uLLatency", "resourceSharingLevel", "serviceType",
        "maxNumberofUEs"})
@Generated("jsonschema2pojo")
public class RANSliceSubnetProfile {

    @JsonProperty("coverageAreaTAList")
    private Integer coverageAreaTAList;
    @JsonProperty("latency")
    private Integer latency;
    @JsonProperty("dLLatency")
    private Integer dLLatency;
    @JsonProperty("uLLatency")
    private Integer uLLatency;
    @JsonProperty("resourceSharingLevel")
    private String resourceSharingLevel;
    @JsonProperty("serviceType")
    private String serviceType;
    @JsonProperty("maxNumberofUEs")
    private Integer maxNumberofUEs;
    @JsonProperty("areaTrafficCapDL")
    private Integer areaTrafficCapDL;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("coverageAreaTAList")
    public Integer getCoverageAreaTAList() {
        return coverageAreaTAList;
    }

    @JsonProperty("coverageAreaTAList")
    public void setCoverageAreaTAList(Integer coverageAreaTAList) {
        this.coverageAreaTAList = coverageAreaTAList;
    }

    @JsonProperty("dLLatency")
    public Integer getdLLatency() {
        return dLLatency;
    }

    @JsonProperty("dLLatency")
    public void setdLLatency(Integer dLLatency) {
        this.dLLatency = dLLatency;
    }

    @JsonProperty("uLLatency")
    public Integer getuLLatency() {
        return uLLatency;
    }

    @JsonProperty("uLLatency")
    public void setuLLatency(Integer uLLatency) {
        this.uLLatency = uLLatency;
    }

    @JsonProperty("resourceSharingLevel")
    public String getResourceSharingLevel() {
        return resourceSharingLevel;
    }

    @JsonProperty("resourceSharingLevel")
    public void setResourceSharingLevel(String resourceSharingLevel) {
        this.resourceSharingLevel = resourceSharingLevel;
    }

    @JsonProperty("serviceType")
    public String getServiceType() {
        return serviceType;
    }

    @JsonProperty("serviceType")
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty("maxNumberofUEs")
    public Integer getMaxNumberofUEs() {
        return maxNumberofUEs;
    }

    @JsonProperty("maxNumberofUEs")
    public void setMaxNumberofUEs(Integer maxNumberofUEs) {
        this.maxNumberofUEs = maxNumberofUEs;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("areaTrafficCapDL")
    public Integer getAreaTrafficCapDL() {
        return this.areaTrafficCapDL;
    }

    @JsonProperty("latency")
    public Integer getLatency() {
        return latency;
    }

    @JsonProperty("latency")
    public void setLatency(Integer latency) {
        this.latency = latency;
    }

    @JsonProperty("areaTrafficCapDL")
    public void setAreaTrafficCapDL(Integer areaTrafficCapDL) {
        this.areaTrafficCapDL = areaTrafficCapDL;
    }

}
