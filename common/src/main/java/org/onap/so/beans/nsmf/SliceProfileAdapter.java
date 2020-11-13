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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import java.io.Serializable;
import java.util.Arrays;

@Data
@ToString
public class SliceProfileAdapter implements Serializable {

    private static final long serialVersionUID = -6412175980642584804L;

    @JsonProperty(value = "sliceProfileId", required = true)
    private String sliceProfileId;

    @JsonProperty(value = "sNSSAI", required = true)
    private String sNSSAI = "";

    @JsonProperty(value = "sNSSAIList", required = true)
    private String sNSSAIList = "";

    @JsonProperty(value = "pLMNIdList", required = true)
    private String pLMNIdList = "";

    @JsonProperty(value = "perfReq", required = true)
    private AnPerfReq perfReq;

    @JsonProperty(value = "maxNumberOfUEs")
    private long maxNumberofUEs;

    @JsonProperty(value = "maxNumberofUEs")
    private long maxNumberOfUEs;

    @JsonProperty(value = "coverageAreaTAList")
    private String coverageAreaTAList = "";

    @JsonProperty(value = "latency")
    private int latency;

    @JsonProperty(value = "uEMobilityLevel")
    private UeMobilityLevel uEMobilityLevel;

    @JsonProperty(value = "resourceSharingLevel")
    private ResourceSharingLevel resourceSharingLevel;

    @JsonProperty(value = "maxBandwidth")
    private String bandwidth;

    @JsonProperty(value = "bandwidth")
    private String maxBandwidth;

    @JsonProperty(value = "sST")
    private String sST;

    @JsonProperty(value = "activityFactor")
    private String activityFactor;

    @JsonProperty(value = "survivalTime")
    private String survivalTime;

    public void setMaxNumberofUEs(long maxNumberofUEs) {
        this.maxNumberofUEs = maxNumberofUEs;
        this.setMaxNumberOfUEs(maxNumberofUEs);
    }

    public void setMaxNumberOfUEs(long maxNumberOfUEs) {
        this.maxNumberOfUEs = maxNumberOfUEs;
        this.setMaxNumberofUEs(maxNumberOfUEs);
    }

    public void setsNSSAI(String snssai) {
        this.setsNSSAIList(snssai);
        this.sNSSAI = snssai;
    }

    public void setsNSSAIList(String snssailist) {
        this.sNSSAIList = snssailist;
        this.setsNSSAI(snssailist);
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
        this.setMaxBandwidth(bandwidth);
    }

    public void setMaxBandwidth(String maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
        this.setBandwidth(maxBandwidth);
    }

    public AnSliceProfile trans2AnProfile() {
        AnSliceProfile anSliceProfile = new AnSliceProfile();
        BeanUtils.copyProperties(this, anSliceProfile);
        anSliceProfile.setSNSSAIList(Arrays.asList(this.sNSSAI.split("\\|")));
        anSliceProfile.setPLMNIdList(Arrays.asList(this.pLMNIdList.split("\\|")));

        String[] areas = this.coverageAreaTAList.split("\\|");
        Integer[] areasRes = new Integer[areas.length];
        for (int i = 0; i < areas.length; i++) {
            areasRes[i] = str2Code(areas[i]);
        }
        anSliceProfile.setCoverageAreaTAList(Arrays.asList(areasRes));
        return anSliceProfile;
    }

    private Integer str2Code(String area) {
        return area.hashCode() >> 16;
    }

    public CnSliceProfile trans2CnProfile() {
        CnSliceProfile cnSliceProfile = new CnSliceProfile();
        BeanUtils.copyProperties(this, cnSliceProfile);
        cnSliceProfile.setSnssaiList(Arrays.asList(this.sNSSAI.split("\\|")));
        cnSliceProfile.setCoverageAreaTAList(Arrays.asList(this.coverageAreaTAList.split("\\|")));
        cnSliceProfile.setPlmnIdList(Arrays.asList(this.pLMNIdList.split("\\|")));
        return cnSliceProfile;
    }

    public TnSliceProfile trans2TnProfile() {
        TnSliceProfile tnSliceProfile = new TnSliceProfile();
        BeanUtils.copyProperties(this, tnSliceProfile);
        tnSliceProfile.setSNSSAIList(Arrays.asList(this.sNSSAI.split("\\|")));
        tnSliceProfile.setPLMNIdList(Arrays.asList(this.pLMNIdList.split("\\|")));
        return tnSliceProfile;
    }
}
