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
package org.openecomp.mso.adapters.vfc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class LocationConstraint {

    /**
     * vnf profile id
     */
    private String vnfProfileId;

    /**
     * location constraints: vimId
     */
    private Map<String, String> locationConstraints = new HashMap<>();

    /**
     * @return Returns the vnfProfileId.
     */
    public String getVnfProfileId() {
        return vnfProfileId;
    }

    /**
     * @param vnfProfileId The vnfProfileId to set.
     */
    public void setVnfProfileId(String vnfProfileId) {
        this.vnfProfileId = vnfProfileId;
    }

    /**
     * @return Returns the locationConstraints.
     */
    public Map<String, String> getLocationConstraints() {
        return locationConstraints;
    }

    /**
     * @param locationConstraints The locationConstraints to set.
     */
    public void setLocationConstraints(Map<String, String> locationConstraints) {
        this.locationConstraints = locationConstraints;
    }

}
