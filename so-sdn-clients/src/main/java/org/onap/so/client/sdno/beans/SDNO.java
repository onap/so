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

package org.onap.so.client.sdno.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"operation", "nodeLoc", "nodeType", "body"})
public class SDNO implements Serializable {

    @JsonProperty("operation")
    private String operation;
    @JsonProperty("nodeLoc")
    private String nodeLoc;
    @JsonProperty("nodeType")
    private String nodeType;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private static final long serialVersionUID = -5303297382564282650L;

    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    @JsonProperty("operation")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @JsonProperty("nodeLoc")
    public String getNodeLoc() {
        return nodeLoc;
    }

    @JsonProperty("nodeLoc")
    public void setNodeLoc(String nodeLoc) {
        this.nodeLoc = nodeLoc;
    }

    public SDNO withNodeLoc(String nodeLoc) {
        this.nodeLoc = nodeLoc;
        return this;
    }

    public SDNO withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    @JsonProperty("nodeType")
    public String getNodeType() {
        return nodeType;
    }

    @JsonProperty("nodeType")
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public SDNO withNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    public SDNO withBody(Body body) {
        this.body = body;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void setAdditionalProperties(Map<String, Object> map) {
        this.additionalProperties = map;
    }

    public SDNO SDNO(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
