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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@ToString
public class SliceProfileAdapter implements Serializable {

    private static final long serialVersionUID = -6412175980642584804L;

    @JsonProperty(value = "sliceProfileId")
    private String sliceProfileId;

    @JsonProperty(value = "sNSSAI")
    private String sNSSAIList = "";

    @JsonProperty(value = "pLMNIdList")
    private String pLMNIdList = "";

    @JsonProperty(value = "maxNumberofUEs")
    private int maxNumberOfUEs;

    @JsonProperty(value = "coverageAreaTAList")
    private String coverageAreaTAList = "";

    @JsonProperty(value = "latency")
    private int latency;

    @JsonProperty(value = "uEMobilityLevel")
    private String ueMobilityLevel;

    @JsonProperty(value = "resourceSharingLevel")
    private String resourceSharingLevel;

    @JsonProperty(value = "maxBandwidth")
    private int maxBandwidth;

    @JsonProperty(value = "sST")
    private String sST = "embb";

    @JsonProperty(value = "activityFactor")
    private int activityFactor;

    @JsonProperty(value = "survivalTime")
    private String survivalTime;

    @JsonProperty(value = "expDataRateUL")
    private int expDataRateUL;

    @JsonProperty(value = "expDataRateDL")
    private int expDataRateDL;

    @JsonProperty(value = "areaTrafficCapUL")
    private int areaTrafficCapUL;

    @JsonProperty(value = "areaTrafficCapDL")
    private int areaTrafficCapDL;

    @JsonProperty(value = "jitter")
    private int jitter;

    @JsonProperty(value = "csAvailabilityTarget")
    private float csAvailabilityTarget;

    @JsonProperty(value = "expDataRate")
    private int expDataRate;

    @JsonProperty(value = "maxNumberofPDUSession")
    private int maxNumberOfPDUSession;

    @JsonProperty(value = "overallUserDensity")
    private int overallUserDensity;

    @JsonProperty(value = "cSReliabilityMeanTime")
    private String csReliabilityMeanTime;

    @JsonProperty(value = "msgSizeByte")
    private String msgSizeByte;

    @JsonProperty(value = "transferIntervalTarget")
    private String transferIntervalTarget;

    @JsonProperty(value = "ipAddress")
    private String ipAddress;

    @JsonProperty(value = "logicInterfaceId")
    private String logicInterfaceId;

    @JsonProperty(value = "nextHopInfo")
    private String nextHopInfo;

    public AnSliceProfile trans2AnProfile() {
        AnSliceProfile anSliceProfile = new AnSliceProfile();
        BeanUtils.copyProperties(this, anSliceProfile);
        anSliceProfile.setSNSSAIList(Arrays.asList(this.sNSSAIList.split("\\|")));
        anSliceProfile.setPLMNIdList(Arrays.asList(this.pLMNIdList.split("\\|")));

        String[] areas = this.coverageAreaTAList.split("\\|");
        Integer[] areasRes = new Integer[areas.length];
        for (int i = 0; i < areas.length; i++) {
            areasRes[i] = str2Code(areas[i]);
        }
        anSliceProfile.setCoverageAreaTAList(Arrays.asList(areasRes));

        anSliceProfile.setUeMobilityLevel(UeMobilityLevel.fromString(this.ueMobilityLevel));
        anSliceProfile.setResourceSharingLevel(ResourceSharingLevel.fromString(this.resourceSharingLevel));
        anSliceProfile.setMaxNumberOfPDUSession(this.maxNumberOfPDUSession);
        anSliceProfile.setPerfReq(generatePerfReq());

        return anSliceProfile;
    }

    private Integer str2Code(String area) {
        return area.hashCode() >> 16;
    }

    public CnSliceProfile trans2CnProfile() {
        CnSliceProfile cnSliceProfile = new CnSliceProfile();
        BeanUtils.copyProperties(this, cnSliceProfile);
        cnSliceProfile.setSnssaiList(Arrays.asList(this.sNSSAIList.split("\\|")));
        cnSliceProfile.setCoverageAreaTAList(Arrays.asList(this.coverageAreaTAList.split("\\|")));
        cnSliceProfile.setPLMNIdList(Arrays.asList(this.pLMNIdList.split("\\|")));
        cnSliceProfile.setResourceSharingLevel(ResourceSharingLevel.fromString(this.resourceSharingLevel));

        cnSliceProfile.setPerfReq(generatePerfReq());
        return cnSliceProfile;
    }

    private PerfReq generatePerfReq() {
        PerfReq perfReq = new PerfReq();
        if ("embb".equalsIgnoreCase(sST)) {
            List<PerfReqEmbb> perfReqEmbbs = new ArrayList<>();
            PerfReqEmbb perfReqEmbb = new PerfReqEmbb();
            BeanUtils.copyProperties(this, perfReqEmbb);
            perfReqEmbbs.add(perfReqEmbb);
            perfReq.setPerfReqEmbbList(perfReqEmbbs);
        } else if ("urllc".equalsIgnoreCase(sST)) {
            List<PerfReqUrllc> perfReqUrllcs = new ArrayList<>();
            PerfReqUrllc perfReqUrllc = new PerfReqUrllc();
            BeanUtils.copyProperties(this, perfReqUrllc);
            perfReqUrllcs.add(perfReqUrllc);
            perfReq.setPerfReqUrllcList(perfReqUrllcs);
        }
        return perfReq;
    }

    public TnSliceProfile trans2TnProfile() {
        TnSliceProfile tnSliceProfile = new TnSliceProfile();
        BeanUtils.copyProperties(this, tnSliceProfile);
        tnSliceProfile.setSNSSAIList(Arrays.asList(this.sNSSAIList.split("\\|")));
        tnSliceProfile.setPLMNIdList(Arrays.asList(this.pLMNIdList.split("\\|")));
        tnSliceProfile.setResourceSharingLevel(ResourceSharingLevel.fromString(this.resourceSharingLevel));
        return tnSliceProfile;
    }
}
