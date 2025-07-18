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

package org.onap.aaiclient.client.graphinventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventorySingleResourceUri;
import org.onap.so.jsonpath.JsonPathUtil;
import com.ctc.wstx.shaded.msv_core.util.Uri;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GraphInventoryRelationships<Wrapper extends GraphInventoryResultWrapper<?>, Uri extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, Type extends GraphInventoryObjectType> {

    protected final ObjectMapper mapper;
    protected Map<String, Object> map;
    protected final String jsonBody;

    public GraphInventoryRelationships(String json) {
        this.jsonBody = json;
        this.mapper = new GraphInventoryCommonObjectMapperProvider().getMapper();
        try {
            this.map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            this.map = new HashMap<>();
        }
    }

    public List<Wrapper> getByType(GraphInventoryObjectName type) {

        return this.getAll(Optional.of(type));
    }

    public List<Wrapper> getByType(GraphInventoryObjectName type, UnaryOperator<Uri> func) {

        return this.getAll(Optional.of(type), func);
    }

    public List<Wrapper> getAll() {

        return this.getAll(Optional.empty());
    }


    public List<String> getRelatedLinks() {
        return this.getRelatedLinks(Optional.empty());
    }

    public List<String> getRelatedLinks(GraphInventoryObjectName type) {
        return this.getRelatedLinks(Optional.of(type));
    }

    public List<Uri> getRelatedUris() {
        return this.getRelatedUris(x -> true);
    }

    public List<Uri> getRelatedUris(GraphInventoryObjectName type) {
        return this.getRelatedUris(x -> type.typeName().equals(x));
    }

    protected List<Uri> getRelatedUris(Predicate<String> p) {
        List<Uri> result = new ArrayList<>();
        if (map.containsKey("relationship")) {
            List<Map<String, Object>> relationships = (List<Map<String, Object>>) map.get("relationship");
            for (Map<String, Object> relationship : relationships) {
                final String relatedTo = (String) relationship.get("related-to");
                if (p.test(relatedTo)) {
                    Type type;
                    final String relatedLink = (String) relationship.get("related-link");
                    type = fromTypeName(relatedTo, relatedLink);

                    result.add(createUri(type, relatedLink));
                }
            }
        }
        return result;
    }



    protected List<Wrapper> getAll(final Optional<GraphInventoryObjectName> type) {
        return getAll(type, UnaryOperator.identity());
    }

    protected List<Wrapper> getAll(final Optional<GraphInventoryObjectName> type, UnaryOperator<Uri> func) {
        List<Uri> relatedLinks;
        if (type.isPresent()) {
            relatedLinks = this.getRelatedUris(type.get());
        } else {
            relatedLinks = this.getRelatedUris();
        }
        ArrayList<Wrapper> result = new ArrayList<>();
        for (Uri link : relatedLinks) {
            result.add(this.get(func.apply(link)));
        }
        return result;
    }

    protected abstract Wrapper get(Uri uri);

    protected abstract Uri createUri(Type type, String relatedLink);

    protected abstract Type fromTypeName(String name, String uri);

    protected List<String> getRelatedLinks(Optional<GraphInventoryObjectName> type) {
        String matcher = "";
        if (type.isPresent()) {
            matcher = "[?(@.related-to=='" + type.get().typeName() + "')]";
        }
        return JsonPathUtil.getInstance().locateResultList(this.jsonBody,
                String.format("$.relationship%s.related-link", matcher));
    }

    public String getJson() {
        return this.jsonBody;
    }
}
