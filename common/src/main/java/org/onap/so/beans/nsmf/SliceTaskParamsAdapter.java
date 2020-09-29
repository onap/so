/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import lombok.*;
import org.onap.so.beans.nsmf.oof.TemplateInfo;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SliceTaskParamsAdapter implements Serializable {

    private static final long serialVersionUID = -7785578865170503301L;

    private String serviceId;

    private String serviceName;

    private String nstId;

    private String nstName;

    private Map<String, Object> serviceProfile;

    private String suggestNsiId;

    private String suggestNsiName;

    private TemplateInfo NSTInfo;

    private SliceTaskInfo<TnSliceProfile> tnBHSliceTaskInfo;

    private SliceTaskInfo<TnSliceProfile> tnMHSliceTaskInfo;

    private SliceTaskInfo<TnSliceProfile> tnFHSliceTaskInfo;

    private SliceTaskInfo<CnSliceProfile> cnSliceTaskInfo;

    private SliceTaskInfo<AnSliceProfile> anSliceTaskInfo;

    @SuppressWarnings("unchecked")
    public void convertFromJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> paramMap = (Map<String, String>) mapper.readValue(jsonString, Map.class);

        this.setServiceName(paramMap.get("ServiceName"));
        this.setServiceId(paramMap.get("ServiceId"));
        this.setNstId(paramMap.get("NSTId"));
        this.setNstName(paramMap.get("NSTName"));
        this.setSuggestNsiId(paramMap.get("suggestNSIId"));
        this.setSuggestNsiName(paramMap.get("suggestNSIName"));

        this.setServiceProfile(replaceHeader(paramMap, "ServiceProfile."));

        TnSliceProfile tnBHSliceProfile = mapper.readValue(
                mapper.writeValueAsString(replaceHeader(paramMap, "SliceProfile.TN.BH.")), TnSliceProfile.class);
        this.tnBHSliceTaskInfo.setSliceProfile(tnBHSliceProfile);

        TnSliceProfile tnMHSliceProfile = mapper.readValue(
                mapper.writeValueAsString(replaceHeader(paramMap, "SliceProfile.TN.MH.")), TnSliceProfile.class);
        this.tnMHSliceTaskInfo.setSliceProfile(tnMHSliceProfile);

        TnSliceProfile tnFHSliceProfile = mapper.readValue(
                mapper.writeValueAsString(replaceHeader(paramMap, "SliceProfile.TN.FH.")), TnSliceProfile.class);
        this.tnFHSliceTaskInfo.setSliceProfile(tnFHSliceProfile);

        CnSliceProfile cnSliceProfile = mapper.readValue(
                mapper.writeValueAsString(replaceHeader(paramMap, "SliceProfile.CN.")), CnSliceProfile.class);
        this.cnSliceTaskInfo.setSliceProfile(cnSliceProfile);

        AnSliceProfile anSliceProfile = mapper.readValue(
                mapper.writeValueAsString(replaceHeader(paramMap, "SliceProfile.AN.")), AnSliceProfile.class);
        this.anSliceTaskInfo.setSliceProfile(anSliceProfile);

        this.tnBHSliceTaskInfo.setSuggestNssiId(paramMap.get("TN.BH.SuggestNSSIId"));
        this.tnBHSliceTaskInfo.setSuggestNssiName(paramMap.get("TN.BH.SuggestNSSIName"));
        this.tnBHSliceTaskInfo.setProgress(paramMap.get("TN.BH.progress"));
        this.tnBHSliceTaskInfo.setStatus(paramMap.get("TN.BH.status"));
        this.tnBHSliceTaskInfo.setStatusDescription(paramMap.get("TN.BH.statusDescription"));
        this.tnBHSliceTaskInfo.setScriptName(paramMap.get("TN.BH.ScriptName"));

        this.tnMHSliceTaskInfo.setSuggestNssiId(paramMap.get("TN.MH.SuggestNSSIId"));
        this.tnMHSliceTaskInfo.setSuggestNssiName(paramMap.get("TN.MH.SuggestNSSIName"));
        this.tnMHSliceTaskInfo.setProgress(paramMap.get("TN.MH.progress"));
        this.tnMHSliceTaskInfo.setStatus(paramMap.get("TN.MH.status"));
        this.tnMHSliceTaskInfo.setStatusDescription(paramMap.get("TN.MH.statusDescription"));
        this.tnMHSliceTaskInfo.setScriptName(paramMap.get("TN.MH.ScriptName"));

        this.tnFHSliceTaskInfo.setSuggestNssiId(paramMap.get("TN.FH.SuggestNSSIId"));
        this.tnFHSliceTaskInfo.setSuggestNssiName(paramMap.get("TN.FH.SuggestNSSIName"));
        this.tnFHSliceTaskInfo.setProgress(paramMap.get("TN.FH.progress"));
        this.tnFHSliceTaskInfo.setStatus(paramMap.get("TN.FH.status"));
        this.tnFHSliceTaskInfo.setStatusDescription(paramMap.get("TN.FH.statusDescription"));
        this.tnFHSliceTaskInfo.setScriptName(paramMap.get("TN.FH.ScriptName"));

        this.cnSliceTaskInfo.setSuggestNssiId(paramMap.get("CN.SuggestNSSIId"));
        this.cnSliceTaskInfo.setSuggestNssiName(paramMap.get("CN.SuggestNSSIName"));
        this.cnSliceTaskInfo.setProgress(paramMap.get("CN.progress"));
        this.cnSliceTaskInfo.setStatus(paramMap.get("CN.status"));
        this.cnSliceTaskInfo.setStatusDescription(paramMap.get("CN.statusDescription"));
        this.cnSliceTaskInfo.setScriptName(paramMap.get("CN.ScriptName"));

        this.anSliceTaskInfo.setSuggestNssiId(paramMap.get("AN.SuggestNSSIId"));
        this.anSliceTaskInfo.setSuggestNssiName(paramMap.get("AN.SuggestNSSIName"));
        this.anSliceTaskInfo.setProgress(paramMap.get("AN.progress"));
        this.anSliceTaskInfo.setStatus(paramMap.get("AN.status"));
        this.anSliceTaskInfo.setStatusDescription(paramMap.get("AN.statusDescription"));
        this.anSliceTaskInfo.setScriptName(paramMap.get("AN.ScriptName"));
    }

    public String convertToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ServiceId", serviceId);
        jsonObject.addProperty("ServiceName", serviceName);
        jsonObject.addProperty("NSTId", nstId);
        jsonObject.addProperty("NSTName", nstName);
        jsonObject.addProperty("suggestNSIId", suggestNsiId);
        jsonObject.addProperty("suggestNSIName", suggestNsiName);

        for (Map.Entry<String, Object> entry : serviceProfile.entrySet()) {
            jsonObject.addProperty("ServiceProfile." + entry.getKey(), entry.getValue().toString());
        }

        Map<String, Object> sliceProfileAn = bean2Map(anSliceTaskInfo.getSliceProfile());

        for (Map.Entry<String, Object> entry : sliceProfileAn.entrySet()) {
            jsonObject.addProperty("SliceProfile.AN." + entry.getKey(), entry.getValue().toString());
        }

        Map<String, Object> sliceProfileCn = bean2Map(cnSliceTaskInfo.getSliceProfile());
        for (Map.Entry<String, Object> entry : sliceProfileCn.entrySet()) {
            jsonObject.addProperty("SliceProfile.CN." + entry.getKey(), entry.getValue().toString());
        }

        Map<String, Object> sliceProfileTnBH = bean2Map(tnBHSliceTaskInfo.getSliceProfile());
        for (Map.Entry<String, Object> entry : sliceProfileTnBH.entrySet()) {
            jsonObject.addProperty("SliceProfile.TN.BH." + entry.getKey(), entry.getValue().toString());
        }

        Map<String, Object> sliceProfileTnMH = bean2Map(tnMHSliceTaskInfo.getSliceProfile());
        for (Map.Entry<String, Object> entry : sliceProfileTnMH.entrySet()) {
            jsonObject.addProperty("SliceProfile.TN.MH." + entry.getKey(), entry.getValue().toString());
        }

        Map<String, Object> sliceProfileTnFH = bean2Map(tnFHSliceTaskInfo.getSliceProfile());
        for (Map.Entry<String, Object> entry : sliceProfileTnFH.entrySet()) {
            jsonObject.addProperty("SliceProfile.TN.FH." + entry.getKey(), entry.getValue().toString());
        }

        jsonObject.addProperty("TN.BH.SuggestNSSIId", tnBHSliceTaskInfo.getSuggestNssiId());
        jsonObject.addProperty("TN.BH.SuggestNSSIName", tnBHSliceTaskInfo.getSuggestNssiName());
        jsonObject.addProperty("TN.BH.progress", tnBHSliceTaskInfo.getProgress());
        jsonObject.addProperty("TN.BH.status", tnBHSliceTaskInfo.getStatus());
        jsonObject.addProperty("TN.BH.statusDescription", tnBHSliceTaskInfo.getStatusDescription());
        jsonObject.addProperty("TN.BH.ScriptName", tnBHSliceTaskInfo.getScriptName());


        jsonObject.addProperty("TN.MH.SuggestNSSIId", tnMHSliceTaskInfo.getSuggestNssiId());
        jsonObject.addProperty("TN.MH.SuggestNSSIName", tnMHSliceTaskInfo.getSuggestNssiName());
        jsonObject.addProperty("TN.MH.progress", tnMHSliceTaskInfo.getProgress());
        jsonObject.addProperty("TN.MH.status", tnMHSliceTaskInfo.getStatus());
        jsonObject.addProperty("TN.MH.statusDescription", tnMHSliceTaskInfo.getStatusDescription());
        jsonObject.addProperty("TN.MH.ScriptName", tnMHSliceTaskInfo.getScriptName());


        jsonObject.addProperty("TN.FH.SuggestNSSIId", tnFHSliceTaskInfo.getSuggestNssiId());
        jsonObject.addProperty("TN.FH.SuggestNSSIName", tnFHSliceTaskInfo.getSuggestNssiName());
        jsonObject.addProperty("TN.FH.progress", tnFHSliceTaskInfo.getProgress());
        jsonObject.addProperty("TN.FH.status", tnFHSliceTaskInfo.getStatus());
        jsonObject.addProperty("TN.FH.statusDescription", tnFHSliceTaskInfo.getStatusDescription());
        jsonObject.addProperty("TN.FH.ScriptName", tnFHSliceTaskInfo.getScriptName());


        jsonObject.addProperty("CN.SuggestNSSIId", cnSliceTaskInfo.getSuggestNssiId());
        jsonObject.addProperty("CN.SuggestNSSIName", cnSliceTaskInfo.getSuggestNssiName());
        jsonObject.addProperty("CN.progress", cnSliceTaskInfo.getProgress());
        jsonObject.addProperty("CN.status", cnSliceTaskInfo.getStatus());
        jsonObject.addProperty("CN.statusDescription", cnSliceTaskInfo.getStatusDescription());
        jsonObject.addProperty("CN.ScriptName", cnSliceTaskInfo.getScriptName());


        jsonObject.addProperty("AN.SuggestNSSIId", anSliceTaskInfo.getSuggestNssiId());
        jsonObject.addProperty("AN.SuggestNSSIName", anSliceTaskInfo.getSuggestNssiName());
        jsonObject.addProperty("AN.progress", anSliceTaskInfo.getProgress());
        jsonObject.addProperty("AN.status", anSliceTaskInfo.getStatus());
        jsonObject.addProperty("AN.statusDescription", anSliceTaskInfo.getStatusDescription());
        jsonObject.addProperty("AN.ScriptName", anSliceTaskInfo.getScriptName());

        return jsonObject.toString();
    }

    /**
     * change T t to {@link Map}
     * 
     * @param t input
     * @param <T> Object
     * @return {@link Map}
     */
    private <T> Map<String, Object> bean2Map(T t) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                Method method = t.getClass().getMethod("get" + name);
                Object value = method.invoke(t);
                resMap.put(name, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resMap;
    }

    /**
     * replace of slice profile
     * 
     * @param paramMap params map
     * @param header starts of key
     * @return Map
     */
    private Map<String, Object> replaceHeader(Map<String, String> paramMap, String header) {
        Map<String, Object> sliceProfileMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith(header)) {
                sliceProfileMap.put(entry.getKey().replaceFirst("^" + header, ""), entry.getValue());
            }
        }
        return sliceProfileMap;
    }
}
