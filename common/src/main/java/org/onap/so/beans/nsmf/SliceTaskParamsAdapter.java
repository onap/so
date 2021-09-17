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

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Map<String, Object> serviceProfile = new HashMap<>();

    private String suggestNsiId;

    private String suggestNsiName;

    private TemplateInfo NSTInfo = new TemplateInfo();

    private SliceTaskInfo<SliceProfileAdapter> tnBHSliceTaskInfo = new SliceTaskInfo<>();

    private SliceTaskInfo<SliceProfileAdapter> tnMHSliceTaskInfo = new SliceTaskInfo<>();

    private SliceTaskInfo<SliceProfileAdapter> tnFHSliceTaskInfo = new SliceTaskInfo<>();

    private SliceTaskInfo<SliceProfileAdapter> cnSliceTaskInfo = new SliceTaskInfo<>();

    private SliceTaskInfo<SliceProfileAdapter> anSliceTaskInfo = new SliceTaskInfo<>();

    private SliceTaskInfo<SliceProfileAdapter> anNFSliceTaskInfo = new SliceTaskInfo<>();

    /**
     * change T t to {@link Map}
     *
     * @param t input
     * @param <T> Object
     * @return {@link Map}
     */
    private <T> Map<String, Object> bean2Map(T t) {
        Map<String, Object> resMap = new HashMap<>();
        if (t == null) {
            return resMap;
        }

        try {
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                String key = name;
                if (name == null || "".equals(name) || "serialVersionUID".equalsIgnoreCase(name)) {
                    continue;
                }
                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                if (annotation != null && !annotation.value().equals(JsonProperty.USE_DEFAULT_NAME)) {
                    key = annotation.value();
                }

                Method method = t.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
                Object value = method.invoke(t);
                resMap.put(key, value);
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

