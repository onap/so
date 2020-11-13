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
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CnSliceProfile implements Serializable {
    @Transient
    private static final long serialVersionUID = -3695693380329368745L;
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
}
