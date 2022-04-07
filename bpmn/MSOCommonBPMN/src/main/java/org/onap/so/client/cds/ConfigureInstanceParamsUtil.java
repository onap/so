/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Bell Canada
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

package org.onap.so.client.cds;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

public final class ConfigureInstanceParamsUtil {

    private static final Gson gson = new Gson();

    public static void applyParamsToObject(List<Map<String, String>> instanceParamsList, JsonObject jsonObject) {
        instanceParamsList.stream().flatMap(instanceParamsMap -> instanceParamsMap.entrySet().stream())
                .forEachOrdered(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue()));
    }

    public static void applyJsonParamsToObject(List<Map<String, Object>> instanceParamsList, JsonObject jsonObject) {
        instanceParamsList.stream().flatMap(instanceParamsMap -> instanceParamsMap.entrySet().stream())
                .forEachOrdered(entry -> jsonObject.add(entry.getKey(), gson.toJsonTree(entry.getValue())));
    }

}
