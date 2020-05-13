/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SliceTaskParams implements Serializable {

    private static final long serialVersionUID = -4389946152970978423L;

    private String serviceId;

    private String serviceName;

    private String nstId;

    private String nstName;

    private String tnScriptName;

    private String anScriptName;

    private String cnScriptName;

    private Map<String, Object> serviceProfile;

    private String suggestNsiId;

    private String suggestNsiName;

    private Map<String, Object> sliceProfileTn;

    private Map<String, Object> sliceProfileCn;

    private Map<String, Object> sliceProfileAn;

    private String tnSuggestNssiId;

    private String tnSuggestNssiName;

    private String tnProgress;

    private String tnStatus;

    private String tnStatusDescription;

    private String cnSuggestNssiId;

    private String cnSuggestNssiName;

    private String cnProgress;

    private String cnStatus;

    private String cnStatusDescription;

    private String anSuggestNssiId;

    private String anSuggestNssiName;

    private String anProgress;

    private String anStatus;

    private String anStatusDescription;

    public SliceTaskParams() {
        this.serviceProfile = new HashMap<>();
        this.sliceProfileAn = new HashMap<>();
        this.sliceProfileCn = new HashMap<>();
        this.sliceProfileTn = new HashMap<>();
    }

    public String getNstId() {
        return nstId;
    }

    public void setNstId(String nstId) {
        this.nstId = nstId;
    }

    public String getNstName() {
        return nstName;
    }

    public void setNstName(String nstName) {
        this.nstName = nstName;
    }

    public String getTnScriptName() {
        return tnScriptName;
    }

    public void setTnScriptName(String tnScriptName) {
        this.tnScriptName = tnScriptName;
    }

    public String getAnScriptName() {
        return anScriptName;
    }

    public void setAnScriptName(String anScriptName) {
        this.anScriptName = anScriptName;
    }

    public String getCnScriptName() {
        return cnScriptName;
    }

    public void setCnScriptName(String cnScriptName) {
        this.cnScriptName = cnScriptName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, Object> getServiceProfile() {
        return serviceProfile;
    }

    public void setServiceProfile(Map<String, Object> serviceProfile) {
        this.serviceProfile = serviceProfile;
    }

    public String getSuggestNsiId() {
        return suggestNsiId;
    }

    public void setSuggestNsiId(String suggestNsiId) {
        this.suggestNsiId = suggestNsiId;
    }

    public String getSuggestNsiName() {
        return suggestNsiName;
    }

    public void setSuggestNsiName(String suggestNsiName) {
        this.suggestNsiName = suggestNsiName;
    }

    public Map<String, Object> getSliceProfileTn() {
        return sliceProfileTn;
    }

    public void setSliceProfileTn(Map<String, Object> sliceProfileTn) {
        this.sliceProfileTn = sliceProfileTn;
    }

    public Map<String, Object> getSliceProfileCn() {
        return sliceProfileCn;
    }

    public void setSliceProfileCn(Map<String, Object> sliceProfileCn) {
        this.sliceProfileCn = sliceProfileCn;
    }

    public Map<String, Object> getSliceProfileAn() {
        return sliceProfileAn;
    }

    public void setSliceProfileAn(Map<String, Object> sliceProfileAn) {
        this.sliceProfileAn = sliceProfileAn;
    }

    public String getTnSuggestNssiId() {
        return tnSuggestNssiId;
    }

    public void setTnSuggestNssiId(String tnSuggestNssiId) {
        this.tnSuggestNssiId = tnSuggestNssiId;
    }

    public String getTnSuggestNssiName() {
        return tnSuggestNssiName;
    }

    public void setTnSuggestNssiName(String tnSuggestNssiName) {
        this.tnSuggestNssiName = tnSuggestNssiName;
    }

    public String getTnProgress() {
        return tnProgress;
    }

    public void setTnProgress(String tnProgress) {
        this.tnProgress = tnProgress;
    }

    public String getTnStatus() {
        return tnStatus;
    }

    public void setTnStatus(String tnStatus) {
        this.tnStatus = tnStatus;
    }

    public String getTnStatusDescription() {
        return tnStatusDescription;
    }

    public void setTnStatusDescription(String tnStatusDescription) {
        this.tnStatusDescription = tnStatusDescription;
    }

    public String getCnSuggestNssiId() {
        return cnSuggestNssiId;
    }

    public void setCnSuggestNssiId(String cnSuggestNssiId) {
        this.cnSuggestNssiId = cnSuggestNssiId;
    }

    public String getCnSuggestNssiName() {
        return cnSuggestNssiName;
    }

    public void setCnSuggestNssiName(String cnSuggestNssiName) {
        this.cnSuggestNssiName = cnSuggestNssiName;
    }

    public String getCnProgress() {
        return cnProgress;
    }

    public void setCnProgress(String cnProgress) {
        this.cnProgress = cnProgress;
    }

    public String getCnStatus() {
        return cnStatus;
    }

    public void setCnStatus(String cnStatus) {
        this.cnStatus = cnStatus;
    }

    public String getCnStatusDescription() {
        return cnStatusDescription;
    }

    public void setCnStatusDescription(String cnStatusDescription) {
        this.cnStatusDescription = cnStatusDescription;
    }

    public String getAnSuggestNssiId() {
        return anSuggestNssiId;
    }

    public void setAnSuggestNssiId(String anSuggestNssiId) {
        this.anSuggestNssiId = anSuggestNssiId;
    }

    public String getAnSuggestNssiName() {
        return anSuggestNssiName;
    }

    public void setAnSuggestNssiName(String anSuggestNssiName) {
        this.anSuggestNssiName = anSuggestNssiName;
    }

    public String getAnProgress() {
        return anProgress;
    }

    public void setAnProgress(String anProgress) {
        this.anProgress = anProgress;
    }

    public String getAnStatus() {
        return anStatus;
    }

    public void setAnStatus(String anStatus) {
        this.anStatus = anStatus;
    }

    public String getAnStatusDescription() {
        return anStatusDescription;
    }

    public void setAnStatusDescription(String anStatusDescription) {
        this.anStatusDescription = anStatusDescription;
    }

    public String convertToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ServiceId", serviceId);
        jsonObject.addProperty("ServiceName", serviceName);
        jsonObject.addProperty("NSTId", nstId);
        jsonObject.addProperty("NSTName", nstName);
        jsonObject.addProperty("TN.ScriptName", tnScriptName);
        jsonObject.addProperty("AN.ScriptName", anScriptName);
        jsonObject.addProperty("CN.ScriptName", cnScriptName);
        for (Map.Entry<String, Object> entry : serviceProfile.entrySet()) {
            jsonObject.addProperty("ServiceProfile." + entry.getKey(), entry.getValue().toString());
        }
        jsonObject.addProperty("suggestNSIId", suggestNsiId);
        jsonObject.addProperty("suggestNSIName", suggestNsiName);
        for (Map.Entry<String, Object> entry : sliceProfileTn.entrySet()) {
            jsonObject.addProperty("SliceProfile.TN." + entry.getKey(), entry.getValue().toString());
        }
        for (Map.Entry<String, Object> entry : sliceProfileCn.entrySet()) {
            jsonObject.addProperty("SliceProfile.CN." + entry.getKey(), entry.getValue().toString());
        }
        for (Map.Entry<String, Object> entry : sliceProfileAn.entrySet()) {
            jsonObject.addProperty("SliceProfile.AN." + entry.getKey(), entry.getValue().toString());
        }
        jsonObject.addProperty("TN.SuggestNSSIId", tnSuggestNssiId);
        jsonObject.addProperty("TN.SuggestNSSIName", tnSuggestNssiName);
        jsonObject.addProperty("TN.progress", tnProgress);
        jsonObject.addProperty("TN.status", tnStatus);
        jsonObject.addProperty("TN.statusDescription", tnStatusDescription);
        jsonObject.addProperty("CN.SuggestNSSIId", cnSuggestNssiId);
        jsonObject.addProperty("CN.SuggestNSSIName", cnSuggestNssiName);
        jsonObject.addProperty("CN.progress", cnProgress);
        jsonObject.addProperty("CN.status", cnStatus);
        jsonObject.addProperty("CN.statusDescription", cnStatusDescription);
        jsonObject.addProperty("AN.SuggestNSSIId", anSuggestNssiId);
        jsonObject.addProperty("AN.SuggestNSSIName", anSuggestNssiName);
        jsonObject.addProperty("AN.progress", anProgress);
        jsonObject.addProperty("AN.status", anStatus);
        jsonObject.addProperty("AN.statusDescription", anStatusDescription);

        return jsonObject.toString();
    }

    public void convertFromJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> paramMap = (Map<String, String>) mapper.readValue(jsonString, Map.class);
        this.setServiceId(paramMap.get("ServiceId"));
        this.setServiceName(paramMap.get("ServiceName"));
        this.setNstId(paramMap.get("NSTId"));
        this.setNstName(paramMap.get("NSTName"));
        this.setTnScriptName(paramMap.get("TN.ScriptName"));
        this.setAnScriptName(paramMap.get("AN.ScriptName"));
        this.setCnScriptName(paramMap.get("CN.ScriptName"));
        Map<String, Object> serviceProfileMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("ServiceProfile.")) {
                serviceProfileMap.put(entry.getKey().replaceFirst("^ServiceProfile.", ""), entry.getValue());
            }
        }
        this.setServiceProfile(serviceProfileMap);
        this.setSuggestNsiId(paramMap.get("suggestNSIId"));
        this.setSuggestNsiName(paramMap.get("suggestNSIName"));
        Map<String, Object> sliceProfileTnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("SliceProfile.TN.")) {
                sliceProfileTnMap.put(entry.getKey().replaceFirst("^SliceProfile.TN.", ""), entry.getValue());
            }
        }
        this.setSliceProfileTn(sliceProfileTnMap);
        Map<String, Object> sliceProfileCnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("SliceProfile.CN.")) {
                sliceProfileCnMap.put(entry.getKey().replaceFirst("^SliceProfile.CN.", ""), entry.getValue());
            }
        }
        this.setSliceProfileCn(sliceProfileCnMap);
        Map<String, Object> sliceProfileAnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("SliceProfile.AN.")) {
                sliceProfileAnMap.put(entry.getKey().replaceFirst("^SliceProfile.AN.", ""), entry.getValue());
            }
        }
        this.setSliceProfileAn(sliceProfileAnMap);
        this.setTnSuggestNssiId(paramMap.get("TN.SuggestNSSIId"));
        this.setTnSuggestNssiName(paramMap.get("TN.SuggestNSSIName"));
        this.setTnProgress(paramMap.get("TN.progress"));
        this.setTnStatus(paramMap.get("TN.status"));
        this.setTnStatusDescription(paramMap.get("TN.statusDescription"));
        this.setCnSuggestNssiId(paramMap.get("CN.SuggestNSSIId"));
        this.setCnSuggestNssiName(paramMap.get("CN.SuggestNSSIName"));
        this.setCnProgress(paramMap.get("CN.progress"));
        this.setCnStatus(paramMap.get("CN.status"));
        this.setCnStatusDescription(paramMap.get("CN.statusDescription"));
        this.setAnSuggestNssiId(paramMap.get("AN.SuggestNSSIId"));
        this.setAnSuggestNssiName(paramMap.get("AN.SuggestNSSIName"));
        this.setAnProgress(paramMap.get("AN.progress"));
        this.setAnStatus(paramMap.get("AN.status"));
        this.setAnStatusDescription(paramMap.get("AN.statusDescription"));
    }
}

