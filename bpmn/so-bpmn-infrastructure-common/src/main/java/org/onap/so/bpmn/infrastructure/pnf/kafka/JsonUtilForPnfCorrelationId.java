/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.pnf.kafka;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

public final class JsonUtilForPnfCorrelationId {

    private static final String JSON_PNF_CORRELATION_ID_FIELD_NAME = "correlationId";

    private JsonUtilForPnfCorrelationId() {
        throw new IllegalStateException("Utility class");
    }

    static List<String> parseJsonToGelAllPnfCorrelationId(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> newList = new ArrayList<>();
        list.forEach(je -> handleEscapedCharacters(new JsonParser().parse(je))
                .ifPresent(jsonObject -> getPnfCorrelationId(jsonObject)
                        .ifPresent(pnfCorrelationId -> newList.add(pnfCorrelationId))));
        return newList;
    }

    private static Optional<JsonObject> handleEscapedCharacters(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            return Optional.ofNullable(jsonElement.getAsJsonObject());
        }
        return Optional.ofNullable(new JsonParser().parse(jsonElement.getAsString()).getAsJsonObject());
    }

    private static Optional<String> getPnfCorrelationId(JsonObject jsonObject) {
        if (jsonObject.has(JSON_PNF_CORRELATION_ID_FIELD_NAME)) {
            return Optional.ofNullable(jsonObject.get(JSON_PNF_CORRELATION_ID_FIELD_NAME).getAsString());
        }
        return Optional.empty();
    }
}
