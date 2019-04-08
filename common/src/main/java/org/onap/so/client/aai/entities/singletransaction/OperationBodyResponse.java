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

package org.onap.so.client.aai.entities.singletransaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"action", "uri", "response-status-code", "response-body"})
public class OperationBodyResponse {

    @JsonProperty("action")
    public String action;
    @JsonProperty("uri")
    public String uri;
    @JsonProperty("response-status-code")
    public Integer responseStatusCode;
    @JsonProperty("response-body")
    public Object responseBody;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("response-status-code")
    public Integer getResponseStatusCode() {
        return responseStatusCode;
    }

    @JsonProperty("response-status-code")
    public void setResponseStatusCode(Integer responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    @JsonProperty("response-body")
    public Object getResponseBody() {
        return responseBody;
    }

    @JsonProperty("response-body")
    public void getResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }
}
