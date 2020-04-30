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

package org.onap.aaiclient.client.aai.entities;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomQuery {

    private List<String> start;
    private String query;
    private String gremlin;

    public CustomQuery(List<AAIResourceUri> start) {
        this.setStart(start);
    }

    public CustomQuery(List<AAIResourceUri> start, String query) {
        this.setStart(start);
        this.query = "query/" + query;
    }

    public CustomQuery(String gremlin) throws UnsupportedEncodingException {
        this.gremlin = gremlin;
    }

    public String getGremlin() {
        return gremlin;
    }

    public void setGremlin(String gremlin) {
        this.gremlin = gremlin;
    }

    public List<String> getStart() {
        return start;
    }

    public void setStart(List<AAIResourceUri> start) {
        this.start = this.mapUris(start);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private List<String> mapUris(List<AAIResourceUri> uris) {
        final List<String> result = new ArrayList<>();
        uris.stream().map(item -> item.build().toString()).forEach(result::add);
        return result;
    }
}
