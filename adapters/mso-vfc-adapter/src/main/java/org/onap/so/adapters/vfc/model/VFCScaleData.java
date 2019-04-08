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

import java.util.ArrayList;
import java.util.List;

/**
 * Object totally matches required VFC input json format JsonUtil.marshal will convert this Object to string
 *
 * added on 2018/01/30 by Qihui Zhao from CMCC
 */

public class VFCScaleData {

    private String nsInstanceId;

    private String scaleType;

    private List<ScaleNsData> scaleNsData = new ArrayList<>();

    /**
     * @return Returns the nsInstanceId.
     */
    public String getNsInstanceId() {
        return nsInstanceId;
    }

    /**
     * @param nsInstanceId The nsInstanceId to set.
     */
    public void setNsInstanceId(String nsInstanceId) {
        this.nsInstanceId = nsInstanceId;
    }

    /**
     * @return Returns the scale Type.
     */
    public String getScaleType() {
        return scaleType;
    }

    /**
     * @param scaleType The scaleType to set.
     */
    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    /**
     * @return Returns the scaleNsDate.
     */
    public List<ScaleNsData> getScaleNsData() {
        return scaleNsData;
    }

    /**
     * The scaleNsData to set.
     */
    public void setScaleNsData(List<ScaleNsByStepsData> scaleNsByStepsData) {
        ScaleNsData scaleNsDataObj = new ScaleNsData();
        scaleNsDataObj.setScaleNsByStepsData(scaleNsByStepsData);

        this.scaleNsData.add(scaleNsDataObj);
    }
}
