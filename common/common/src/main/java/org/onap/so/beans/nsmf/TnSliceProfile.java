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
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TnSliceProfile implements Serializable {

    private static final long serialVersionUID = 3767943556195823439L;

    private String sliceProfileId;

    @JsonProperty(value = "maxBandwidth")
    private int maxBandwidth;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int latency;

    @JsonProperty(value = "snssaiList", required = true)
    private List<String> sNSSAIList;

    @JsonProperty(value = "pLMNIdList", required = true)
    private List<String> pLMNIdList;

    @JsonProperty(value = "jitter")
    private int jitter;

    @JsonProperty(value = "domainType")
    private String domainType;

    @JsonProperty(value = "resourceSharingLevel")
    private ResourceSharingLevel resourceSharingLevel;
}
