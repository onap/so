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

package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CnSliceProfile {

    private List<String> snssaiList;

    private String sliceProfileId;

    private List<String> plmnIdList;

    private PerfReq perfReq;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int maxNumberofUEs;

    private List<String> coverageAreaTAList;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int latency;

    private UeMobilityLevel ueMobilityLevel;

    private ResourceSharingLevel resourceSharingLevel;

    public String getSliceProfileId() {
        return sliceProfileId;
    }

    public void setSliceProfileId(String sliceProfileId) {
        this.sliceProfileId = sliceProfileId;
    }

    public List<String> getPlmnIdList() {
        return plmnIdList;
    }

    public void setPlmnIdList(List<String> plmnIdList) {
        this.plmnIdList = plmnIdList;
    }

    public PerfReq getPerfReq() {
        return perfReq;
    }

    public void setPerfReq(PerfReq perfReq) {
        this.perfReq = perfReq;
    }

    public int getMaxNumberofUEs() {
        return maxNumberofUEs;
    }

    public void setMaxNumberofUEs(int maxNumberofUEs) {
        this.maxNumberofUEs = maxNumberofUEs;
    }

    public List<String> getCoverageAreaTAList() {
        return coverageAreaTAList;
    }

    public void setCoverageAreaTAList(List<String> coverageAreaTAList) {
        this.coverageAreaTAList = coverageAreaTAList;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public UeMobilityLevel getUeMobilityLevel() {
        return ueMobilityLevel;
    }

    public void setUeMobilityLevel(UeMobilityLevel ueMobilityLevel) {
        this.ueMobilityLevel = ueMobilityLevel;
    }

    public ResourceSharingLevel getResourceSharingLevel() {
        return resourceSharingLevel;
    }

    public void setResourceSharingLevel(ResourceSharingLevel resourceSharingLevel) {
        this.resourceSharingLevel = resourceSharingLevel;
    }

    public List<String> getSnssaiList() {
        return snssaiList;
    }

    public void setSnssaiList(List<String> snssaiList) {
        this.snssaiList = snssaiList;
    }
}
