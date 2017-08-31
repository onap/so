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

/**
 * Input Parameter For Each Domain<br>
 * <p>
 * </p>
 * 
 * @version GSO 0.5 2017/1/7
 */
public class NSResourceInputParameter {

    private NsOperationKey nsOperationKey;

    private String subServiceName;

    private String subServiceDesc;

    private NsParameters nsParameters;

    /**
     * @return Returns the subServiceName.
     */
    public String getSubServiceName() {
        return subServiceName;
    }

    /**
     * @param subServiceName The subServiceName to set.
     */
    public void setSubServiceName(String subServiceName) {
        this.subServiceName = subServiceName;
    }

    /**
     * @return Returns the subServiceDesc.
     */
    public String getSubServiceDesc() {
        return subServiceDesc;
    }

    /**
     * @param subServiceDesc The subServiceDesc to set.
     */
    public void setSubServiceDesc(String subServiceDesc) {
        this.subServiceDesc = subServiceDesc;
    }

    /**
     * @return Returns the nsParameters.
     */
    public NsParameters getNsParameters() {
        return nsParameters;
    }

    /**
     * @param nsParameters The nsParameters to set.
     */
    public void setNsParameters(NsParameters nsParameters) {
        this.nsParameters = nsParameters;
    }

    public NsOperationKey getNsOperationKey() {
        return nsOperationKey;
    }

    public void setNsOperationKey(NsOperationKey nsOperationKey) {
        this.nsOperationKey = nsOperationKey;
    }

}
