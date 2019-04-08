/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.vfc.model;

import java.util.List;

/**
 * aim to wrap List<ScaleNsByStepsData> as a new list then be provided for the usage of vfc json
 *
 * added on 2018/01/30 by Qihui Zhao from CMCC
 */

public class ScaleNsData {

    private List<ScaleNsByStepsData> scaleNsByStepsData;

    /**
     * @return Returns the scaleNsByStepsData.
     */
    public List<ScaleNsByStepsData> getScaleNsByStepsData() {
        return scaleNsByStepsData;
    }

    /**
     * @param scaleNsByStepsData The scaleNsByStepsData to set.
     */
    public void setScaleNsByStepsData(List<ScaleNsByStepsData> scaleNsByStepsData) {
        this.scaleNsByStepsData = scaleNsByStepsData;
    }
}
