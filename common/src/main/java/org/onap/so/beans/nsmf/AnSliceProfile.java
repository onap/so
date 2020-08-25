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
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AnSliceProfile {

    private List<String> sNSSAIList;

    private String sliceProfileId;

    private List<String> coverageAreaTAList;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int latency;

    private List<String> pLMNIdList;

    private PerfReq perfReq;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int maxNumberofUEs;

    private UeMobilityLevel uEMobilityLevel;

    private ResourceSharingLevel resourceSharingLevel;

}
