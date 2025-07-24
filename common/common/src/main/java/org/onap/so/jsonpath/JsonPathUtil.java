/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.jsonpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

public class JsonPathUtil {


    private final Configuration conf;
    private final Configuration pathListConf;

    private JsonPathUtil() {
        conf = Configuration.defaultConfiguration().jsonProvider(new JacksonJsonNodeJsonProvider())
                .addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
        pathListConf = Configuration.defaultConfiguration().addOptions(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS,
                Option.ALWAYS_RETURN_LIST);
    }

    private static class Helper {
        private static final JsonPathUtil INSTANCE = new JsonPathUtil();
    }

    public static JsonPathUtil getInstance() {
        return Helper.INSTANCE;
    }

    public boolean pathExists(String json, String jsonPath) {
        return JsonPath.using(conf).parse(json).<ArrayNode>read(jsonPath).size() != 0;
    }

    public Optional<String> locateResult(String json, String jsonPath) {
        final ArrayNode result = JsonPath.using(conf).parse(json).read(jsonPath);
        if (result.size() == 0) {
            return Optional.empty();
        } else {
            if (result.get(0).isValueNode()) {
                return Optional.of(result.get(0).asText());
            } else {
                return Optional.of(result.get(0).toString());
            }

        }
    }

    public List<String> locateResultList(String json, String jsonPath) {
        final ArrayNode resultNodes = JsonPath.using(conf).parse(json).read(jsonPath);
        final ArrayList<String> result = new ArrayList<>();

        for (JsonNode node : resultNodes) {
            if (node.isValueNode()) {
                result.add(node.asText());
            } else {
                result.add(node.toString());
            }

        }
        return result;
    }

    public List<String> getPathList(String json, String jsonPath) {
        return JsonPath.using(pathListConf).parse(json).read(jsonPath);
    }
}
