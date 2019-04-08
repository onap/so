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

package org.onap.so.adapters.valet.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.io.Serializable;

/*
 * This class represents the body of a Create response on a Valet Placement API call
 */
public class ValetCreateResponse implements Serializable {
    private static final long serialVersionUID = 768026109321305392L;

    @JsonProperty("status")
    private ValetStatus status;
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    public ValetCreateResponse() {
        super();
    }

    public ValetStatus getStatus() {
        return this.status;
    }

    public void setStatus(ValetStatus status) {
        this.status = status;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ValetCreateResponse)) {
            return false;
        }
        ValetCreateResponse vcr = (ValetCreateResponse) o;
        return Objects.equals(status, vcr.status) && Objects.equals(parameters, vcr.parameters);
    }
}
