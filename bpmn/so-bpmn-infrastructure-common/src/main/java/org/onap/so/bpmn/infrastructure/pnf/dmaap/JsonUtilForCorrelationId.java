/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

package org.onap.so.bpmn.infrastructure.pnf.dmaap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

public final class JsonUtilForCorrelationId {

    private static final String JSON_CORRELATION_ID_FIELD_NAME = "correlationId";

    static List<String> parseJsonToGelAllCorrelationId(String json) {
        JsonElement je = new JsonParser().parse(json);
        JsonArray array = je.getAsJsonArray();
        List<String> list = new ArrayList<>();
        Spliterator<JsonElement> spliterator = array.spliterator();
        spliterator.forEachRemaining(jsonElement -> {
            handleEscapedCharacters(jsonElement)
                    .ifPresent(jsonObject -> getCorrelationId(jsonObject)
                            .ifPresent(correlationId -> list.add(correlationId)));
        });
        return list;
    }

    private static Optional<JsonObject> handleEscapedCharacters(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            return Optional.ofNullable(jsonElement.getAsJsonObject());
        }
        return Optional.ofNullable(new JsonParser().parse(jsonElement.getAsString()).getAsJsonObject());
    }

    private static Optional<String> getCorrelationId(JsonObject jsonObject) {
        if (jsonObject.has(JSON_CORRELATION_ID_FIELD_NAME)) {
            return Optional.ofNullable(jsonObject.get(JSON_CORRELATION_ID_FIELD_NAME).getAsString());
        }
        return Optional.empty();
    }
}
