/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.adapter.vnf;


import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class SDNCResponse {

    @JsonProperty("request-id")
    private String requestId;
    @JsonProperty("response-message")
    private String reponseMessage;
    @JsonProperty("response-code")
    private String responseCode;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getReponseMessage() {
        return reponseMessage;
    }

    public void setReponseMessage(String reponseMessage) {
        this.reponseMessage = reponseMessage;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "SDNCResponse [requestId=" + requestId + ", reponseMessage=" + reponseMessage + ", responseCode="
                + responseCode + "]";
    }


}
