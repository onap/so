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
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerfReqEmbb implements Serializable {
    /*
     * Reference 3GPP TS 28.541 V16.5.0, Section 6.4.1.
     */

    private static final long serialVersionUID = 8886635511695277599L;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int expDataRateDL;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int expDataRateUL;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int areaTrafficCapDL;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int areaTrafficCapUL;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int overallUserDensity;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int activityFactor;

    public int getExpDataRateDL() {
        return expDataRateDL;
    }

    public void setExpDataRateDL(int expDataRateDL) {
        this.expDataRateDL = expDataRateDL;
    }

    public int getExpDataRateUL() {
        return expDataRateUL;
    }

    public void setExpDataRateUL(int expDataRateUL) {
        this.expDataRateUL = expDataRateUL;
    }

    public int getAreaTrafficCapDL() {
        return areaTrafficCapDL;
    }

    public void setAreaTrafficCapDL(int areaTrafficCapDL) {
        this.areaTrafficCapDL = areaTrafficCapDL;
    }

    public int getAreaTrafficCapUL() {
        return areaTrafficCapUL;
    }

    public void setAreaTrafficCapUL(int areaTrafficCapUL) {
        this.areaTrafficCapUL = areaTrafficCapUL;
    }

    public int getOverallUserDensity() {
        return overallUserDensity;
    }

    public void setOverallUserDensity(int overallUserDensity) {
        this.overallUserDensity = overallUserDensity;
    }

    public int getActivityFactor() {
        return activityFactor;
    }

    public void setActivityFactor(int activityFactor) {
        this.activityFactor = activityFactor;
    }
}
