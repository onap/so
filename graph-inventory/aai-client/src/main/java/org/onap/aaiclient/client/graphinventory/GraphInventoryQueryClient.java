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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.ws.rs.core.GenericType;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.graphinventory.entities.GraphInventoryResultWrapper;
import org.onap.aaiclient.client.graphinventory.entities.Pathed;
import org.onap.aaiclient.client.graphinventory.entities.ResourceAndUrl;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryUri;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GraphInventoryQueryClient<S, I, Wrapper extends GraphInventoryResultWrapper<?>, Type extends GraphInventoryObjectType> {

    private Optional<Depth> depth = Optional.empty();
    private boolean nodesOnly = false;
    private Optional<GraphInventorySubgraphType> subgraph = Optional.empty();
    private GraphInventoryClient client;
    private GraphInventoryCommonObjectMapperProvider mapperProvider = new GraphInventoryCommonObjectMapperProvider();

    public GraphInventoryQueryClient(GraphInventoryClient client) {
        this.client = client;
    }

    protected abstract GraphInventoryUri getQueryUri();

    public String query(Format format, I query) {
        return client.createClient(setupQueryParams(getQueryUri().queryParam("format", format.toString()))).put(query,
                String.class);
    }

    protected <R> List<R> querySingleType(Format format, I query, Class<R> clazz) {
        return client.createClient(setupQueryParams(getQueryUri().queryParam("format", format.toString())))
                .put(query, new GenericType<Results<Object>>() {}).getResult().stream().map(item -> {
                    try {
                        return mapperProvider.getMapper().readValue(mapperProvider.getMapper().writeValueAsString(item),
                                clazz);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("could not map values from json", e);
                    }
                }).collect(Collectors.toList());
    }

    public List<Pathed> queryPathed(I query) {
        return querySingleType(Format.PATHED, query, Pathed.class);
    }

    public List<Id> queryId(I query) {
        return querySingleType(Format.ID, query, Id.class);
    }

    public <R> List<R> querySingleResource(I query, Class<R> clazz) {
        try {
            return getResourceAndUrl(query).stream().map(item -> item.getWrapper().asBean(clazz).get())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("could not map values from json", e);
        }
    }

    public List<ResourceAndUrl<Wrapper>> getResourceAndUrl(I query) throws IOException {
        List<ResourceAndUrl<Wrapper>> result = new ArrayList<>();
        ObjectMapper mapper = mapperProvider.getMapper();
        Results<Map<String, Object>> resultsFromJson = mapper.readValue(query(Format.RESOURCE_AND_URL, query),
                new TypeReference<Results<Map<String, Object>>>() {});
        for (Map<String, Object> m : resultsFromJson.getResult()) {
            for (Entry<String, Object> entrySet : m.entrySet()) {
                if (!entrySet.getKey().equals("url")) {
                    String url = (String) m.get("url");
                    String stringJson = mapper.writeValueAsString(entrySet.getValue());
                    result.add(new ResourceAndUrl<Wrapper>(url, createType(entrySet.getKey(), url),
                            createWrapper(stringJson)));
                }
            }
        }

        return result;
    }

    public abstract Wrapper createWrapper(String json);

    public abstract Type createType(String name, String uri);

    public S depth(Depth depth) {
        this.depth = Optional.of(depth);
        return (S) this;
    }

    public S nodesOnly() {
        this.nodesOnly = true;
        return (S) this;
    }

    public S subgraph(GraphInventorySubgraphType type) {

        subgraph = Optional.of(type);

        return (S) this;
    }

    protected GraphInventoryUri setupQueryParams(GraphInventoryUri uri) {
        GraphInventoryUri clone = uri.clone();
        if (this.depth.isPresent()) {
            clone.queryParam("depth", depth.get().toString());
        }
        if (this.nodesOnly) {
            clone.queryParam("nodesOnly", "");
        }
        if (this.subgraph.isPresent()) {
            clone.queryParam("subgraph", this.subgraph.get().toString());
        }
        return clone;
    }

    public GraphInventoryClient getClient() {
        return this.client;
    }
}
