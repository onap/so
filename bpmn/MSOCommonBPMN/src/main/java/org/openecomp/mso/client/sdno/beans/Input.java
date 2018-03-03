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

package org.openecomp.mso.client.sdno.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "request-healthdiagnostic",
    "request-hd-custom"
})
public class Input implements Serializable
{

    @JsonProperty("request-healthdiagnostic")
    private RequestHealthDiagnostic RequestHealthDiagnostic;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 7155546785389227528L;

    @JsonProperty("request-healthdiagnostic")
    public RequestHealthDiagnostic getRequestHealthDiagnostic() {
        return RequestHealthDiagnostic;
    }

    @JsonProperty("request-healthdiagnostic")
    public void setRequestHealthDiagnostic(RequestHealthDiagnostic RequestHealthDiagnostic) {
        this.RequestHealthDiagnostic = RequestHealthDiagnostic;
    }

    public Input withRequestHealthDiagnostic(RequestHealthDiagnostic RequestHealthDiagnostic) {
        this.RequestHealthDiagnostic = RequestHealthDiagnostic;
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

    public Input withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
