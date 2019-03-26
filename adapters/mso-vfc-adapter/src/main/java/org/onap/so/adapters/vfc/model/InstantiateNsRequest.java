/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import java.util.List;
import java.util.Map;

public class InstantiateNsRequest {

    private String nsFlavourId;
    private List<SapData> sapData;
    private List<AddPnfData> addpnfData;
    private List<VnfInstanceData> vnfInstanceData;
    private List<String> nestedNsInstanceId;
    private List<VnfLocationConstraint> localizationLanguage;
    private Map<String, Object> aditionalParamsForNs;
    private List<ParamsForVnf> additionalParamsForVnf;
    private String startTime;
    private String nsInstantiationLevelId;
    private List<AffinityOrAntiAffinityRule> additionalAffinityOrAntiAffiniityRule;

    /***
     *
     * @return nsFlavourId
     */
    public String getNsFlavourId() {
        return nsFlavourId;
    }

    /***
     *
     * @param nsFlavourId
     */
    public void setNsFlavourId(String nsFlavourId) {
        this.nsFlavourId = nsFlavourId;
    }

    /***
     *
     * @return
     */
    public List<SapData> getSapData() {
        return sapData;
    }

    /***
     *
     * @param sapData
     */
    public void setSapData(List<SapData> sapData) {
        this.sapData = sapData;
    }

    /***
     *
     * @return
     */
    public List<AddPnfData> getAddpnfData() {
        return addpnfData;
    }

    /***
     *
     * @param addpnfData
     */
    public void setAddpnfData(List<AddPnfData> addpnfData) {
        this.addpnfData = addpnfData;
    }

    /***
     *
     * @return
     */
    public List<VnfInstanceData> getVnfInstanceData() {
        return vnfInstanceData;
    }

    /***
     *
     * @param vnfInstanceData
     */
    public void setVnfInstanceData(List<VnfInstanceData> vnfInstanceData) {
        this.vnfInstanceData = vnfInstanceData;
    }

    /***
     *
     * @return
     */
    public List<String> getNestedNsInstanceId() {
        return nestedNsInstanceId;
    }

    /***
     *
     * @param nestedNsInstanceId
     */
    public void setNestedNsInstanceId(List<String> nestedNsInstanceId) {
        this.nestedNsInstanceId = nestedNsInstanceId;
    }

    /***
     *
     * @return
     */
    public List<VnfLocationConstraint> getLocalizationLanguage() {
        return localizationLanguage;
    }

    /***
     *
     * @param localizationLanguage
     */
    public void setLocalizationLanguage(List<VnfLocationConstraint> localizationLanguage) {
        this.localizationLanguage = localizationLanguage;
    }

    /***
     *
     * @return
     */
    public Map<String, Object> getAditionalParamsForNs() {
        return aditionalParamsForNs;
    }

    /***
     *
     * @param aditionalParamsForNs
     */
    public void setAditionalParamsForNs(Map<String, Object> aditionalParamsForNs) {
        this.aditionalParamsForNs = aditionalParamsForNs;
    }

    /***
     *
     * @return
     */
    public List<ParamsForVnf> getAdditionalParamsForVnf() {
        return additionalParamsForVnf;
    }

    /***
     *
     * @param additionalParamsForVnf
     */
    public void setAdditionalParamsForVnf(List<ParamsForVnf> additionalParamsForVnf) {
        this.additionalParamsForVnf = additionalParamsForVnf;
    }

    /***
     *
     * @return
     */
    public String getStartTime() {
        return startTime;
    }

    /***
     *
     * @param startTime
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /***
     *
     * @return
     */
    public String getNsInstantiationLevelId() {
        return nsInstantiationLevelId;
    }

    /***
     *
     * @param nsInstantiationLevelId
     */
    public void setNsInstantiationLevelId(String nsInstantiationLevelId) {
        this.nsInstantiationLevelId = nsInstantiationLevelId;
    }

    /***
     *
     * @return
     */
    public List<AffinityOrAntiAffinityRule> getAdditionalAffinityOrAntiAffiniityRule() {
        return additionalAffinityOrAntiAffiniityRule;
    }

    /***
     *
     * @param additionalAffinityOrAntiAffiniityRule
     */
    public void setAdditionalAffinityOrAntiAffiniityRule(
            List<AffinityOrAntiAffinityRule> additionalAffinityOrAntiAffiniityRule) {
        this.additionalAffinityOrAntiAffiniityRule = additionalAffinityOrAntiAffiniityRule;
    }
}
