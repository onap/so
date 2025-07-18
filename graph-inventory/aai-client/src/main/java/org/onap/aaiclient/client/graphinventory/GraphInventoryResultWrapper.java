/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.aaiclient.client.graphinventory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.onap.so.jsonpath.JsonPathUtil;
import org.slf4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GraphInventoryResultWrapper<R extends GraphInventoryRelationships<?, ?, ?>>
        implements Serializable {

    private static final long serialVersionUID = 5895841925807816727L;
    protected final String jsonBody;
    protected final ObjectMapper mapper;
    private final transient Logger logger;

    protected GraphInventoryResultWrapper(String json, Logger logger) {
        this.jsonBody = json;
        this.mapper = new GraphInventoryCommonObjectMapperProvider().getMapper();
        this.logger = logger;
    }

    protected GraphInventoryResultWrapper(Object aaiObject, Logger logger) {
        this.mapper = new GraphInventoryCommonObjectMapperProvider().getMapper();
        this.jsonBody = mapObjectToString(aaiObject);
        this.logger = logger;
    }

    protected String mapObjectToString(Object aaiObject) {
        try {
            return mapper.writeValueAsString(aaiObject);
        } catch (JsonProcessingException e) {
            logger.warn("could not parse object into json - defaulting to empty object");
            return "{}";
        }
    }

    public boolean hasRelationshipsTo(GraphInventoryObjectName name) {
        Optional<R> rOpt = this.getRelationships();
        if (rOpt.isPresent()) {
            return rOpt.get().getRelatedLinks(name).size() > 0;
        } else {
            return false;
        }
    }

    public Optional<R> getRelationships() {
        final String path = "$.relationship-list";
        if (isEmpty()) {
            return Optional.empty();
        }
        Optional<String> result = JsonPathUtil.getInstance().locateResult(jsonBody, path);
        if (result.isPresent()) {
            return Optional.of(createRelationships(result.get()));
        } else {
            return Optional.empty();
        }
    }

    protected abstract R createRelationships(String json);

    public String getJson() {
        if (jsonBody == null) {
            return "{}";
        } else {
            return jsonBody;
        }
    }

    public Map<String, Object> asMap() {

        return asBean(new TypeReference<Map<String, Object>>() {}).orElse(new HashMap<>());
    }

    public <T> Optional<T> asBean(Class<T> clazz) {
        if (isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(this.jsonBody, clazz));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public <T> Optional<T> asBean(TypeReference<T> reference) {
        if (isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(this.jsonBody, reference));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        return jsonBody == null;
    }

    @Override
    public String toString() {
        return this.getJson();
    }

}
