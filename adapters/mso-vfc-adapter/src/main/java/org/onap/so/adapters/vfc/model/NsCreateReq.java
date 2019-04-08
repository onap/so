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
 * Network Service Request<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Sep 2, 2016
 */
public class NsCreateReq {

    CustomerModel context;

    String csarId;

    String nsName;

    String description;


    /**
     * @return Returns the context.
     */
    public CustomerModel getContext() {
        return context;
    }



    /**
     * @param context The context to set.
     */
    public void setContext(CustomerModel context) {
        this.context = context;
    }


    /**
     * @return Returns the csarId.
     */
    public String getCsarId() {
        return csarId;
    }


    /**
     * @param csarId The csarId to set.
     */
    public void setCsarId(String csarId) {
        this.csarId = csarId;
    }

    /**
     * @return Returns the nsName.
     */
    public String getNsName() {
        return nsName;
    }

    /**
     * @param nsName The nsName to set.
     */
    public void setNsName(String nsName) {
        this.nsName = nsName;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
