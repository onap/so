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

/**
 * <br>
 * <p>
 * </p>
 * request model for instatiate
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class NsInstantiateReq extends NsParameters {

    String nsInstanceId;

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

}
