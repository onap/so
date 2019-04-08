/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class NsParameters {

    private List<LocationConstraint> locationConstraints;

    private Map<String, Object> additionalParamForNs = new HashMap<>();

    /**
     * @return Returns the locationConstraints.
     */
    public List<LocationConstraint> getLocationConstraints() {
        return locationConstraints;
    }

    /**
     * @param locationConstraints The locationConstraints to set.
     */
    public void setLocationConstraints(List<LocationConstraint> locationConstraints) {
        this.locationConstraints = locationConstraints;
    }


    /**
     * @return Returns the additionalParamForNs.
     */
    public Map<String, Object> getAdditionalParamForNs() {
        return additionalParamForNs;
    }


    /**
     * @param additionalParamForNs The additionalParamForNs to set.
     */
    public void setAdditionalParamForNs(Map<String, Object> additionalParamForNs) {
        this.additionalParamForNs = additionalParamForNs;
    }
}
