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

package org.onap.so.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"integral", "valueType"})
public class Id {

    @JsonProperty("integral")
    private Boolean integral;
    @JsonProperty("valueType")
    private String valueType;

    @JsonProperty("integral")
    public Boolean getIntegral() {
        return integral;
    }

    @JsonProperty("integral")
    public void setIntegral(Boolean integral) {
        this.integral = integral;
    }

    public Id withIntegral(Boolean integral) {
        this.integral = integral;
        return this;
    }

    @JsonProperty("valueType")
    public String getValueType() {
        return valueType;
    }

    @JsonProperty("valueType")
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Id withValueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

}
