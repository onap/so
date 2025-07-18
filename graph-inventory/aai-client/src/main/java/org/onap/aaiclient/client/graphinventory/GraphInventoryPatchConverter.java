/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperPatchProvider;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryPatchDepthExceededException;
import org.onap.so.jsonpath.JsonPathUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GraphInventoryPatchConverter {

    private static final AAICommonObjectMapperProvider standardProvider = new AAICommonObjectMapperProvider();
    private static final AAICommonObjectMapperPatchProvider patchProvider = new AAICommonObjectMapperPatchProvider();
    private static final Pattern LOCATE_COMPLEX_OBJECT =
            Pattern.compile("^((?!relationship-list).)+?\\['[^\\[\\]]+?'\\]$");


    public String convertPatchFormat(Object obj) {
        return validatePatchObject(marshallObjectToPatchFormat(obj));
    }

    public String validatePatchObject(String payload) {
        if (hasComplexObject(payload)) {
            throw new GraphInventoryPatchDepthExceededException(payload);
        }

        return payload;
    }

    /**
     * validates client side that json does not include any complex objects relationship-list is omitted from this
     * validation
     */
    protected boolean hasComplexObject(String json) {
        if (json.isEmpty()) {
            return false;
        }
        String complex = "$.*.*";
        String array = "$.*.*.*";
        List<String> result = JsonPathUtil.getInstance().getPathList(json, complex);
        List<String> result2 = JsonPathUtil.getInstance().getPathList(json, array);

        result.addAll(result2);
        return result.stream().anyMatch(item -> LOCATE_COMPLEX_OBJECT.matcher(item).find());
    }

    protected String marshallObjectToPatchFormat(Object obj) {
        Object value = obj;
        try {
            if (!(obj instanceof Map || obj instanceof String)) {
                value = patchProvider.getMapper().writeValueAsString(obj);
            } else if (obj instanceof Map) {
                value = standardProvider.getMapper().writeValueAsString(obj);
            }
        } catch (JsonProcessingException e) {
            value = "{}";
        }

        return (String) value;
    }
}
