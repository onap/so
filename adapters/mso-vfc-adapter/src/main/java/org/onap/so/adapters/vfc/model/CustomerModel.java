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
 * The Customer Model <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-10-12
 */
public class CustomerModel {

    String globalCustomerId;

    String serviceType;


    /**
     * @return Returns the globalCustomerId.
     */
    public String getGlobalCustomerId() {
        return globalCustomerId;
    }


    /**
     * @param globalCustomerId The globalCustomerId to set.
     */
    public void setGlobalCustomerId(String globalCustomerId) {
        this.globalCustomerId = globalCustomerId;
    }


    /**
     * @return Returns the serviceType.
     */
    public String getServiceType() {
        return serviceType;
    }


    /**
     * @param serviceType The serviceType to set.
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

}
