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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@ToString
public class AnSliceProfile implements Serializable {
    /*
     * Reference 3GPP TS 28.541 V16.5.0.
     */

    private static final long serialVersionUID = -3057342171549542794L;

    @JsonProperty(value = "sliceProfileId", required = true)
    private String sliceProfileId;

    @JsonProperty(value = "snssaiList", required = true)
    private List<String> sNSSAIList;

    @JsonProperty(value = "pLMNIdList", required = true)
    private List<String> pLMNIdList;

    @JsonProperty(value = "perfReq", required = true)
    private PerfReq perfReq;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty(value = "maxNumberofUEs")
    private int maxNumberOfUEs;

    @JsonProperty(value = "coverageAreaTAList")
    private List<Integer> coverageAreaTAList;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty(value = "latency")
    private int latency;

    @JsonProperty(value = "uEMobilityLevel")
    private UeMobilityLevel ueMobilityLevel;

    @JsonProperty(value = "resourceSharingLevel")
    private ResourceSharingLevel resourceSharingLevel;

    @JsonProperty(value = "maxNumberofPDUSession")
    private int maxNumberOfPDUSession;

}
