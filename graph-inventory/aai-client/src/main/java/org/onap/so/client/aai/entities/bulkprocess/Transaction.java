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

package org.onap.so.client.aai.entities.bulkprocess;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"patch", "patch", "delete"})
public class Transaction {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("put")
    private List<OperationBody> put = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("patch")
    private List<OperationBody> patch = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("delete")
    private List<OperationBody> delete = new ArrayList<>();

    @JsonProperty("put")
    public List<OperationBody> getPut() {
        return put;
    }

    @JsonProperty("put")
    public void setPut(List<OperationBody> put) {
        this.put = put;
    }

    public Transaction withPut(List<OperationBody> put) {
        this.put = put;
        return this;
    }

    @JsonProperty("patch")
    public List<OperationBody> getPatch() {
        return patch;
    }

    @JsonProperty("patch")
    public void setPatch(List<OperationBody> patch) {
        this.patch = patch;
    }

    public Transaction withPatch(List<OperationBody> patch) {
        this.patch = patch;
        return this;
    }

    @JsonProperty("delete")
    public List<OperationBody> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<OperationBody> delete) {
        this.delete = delete;
    }

    public Transaction withDelete(List<OperationBody> delete) {
        this.delete = delete;
        return this;
    }

}
