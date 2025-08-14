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
@JsonPropertyOrder({"valueType", "string", "chars"})
public class Treatments {

    @JsonProperty("valueType")
    private String valueType;
    @JsonProperty("string")
    private String string;
    @JsonProperty("chars")
    private String chars;

    @JsonProperty("valueType")
    public String getValueType() {
        return valueType;
    }

    @JsonProperty("valueType")
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Treatments withValueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

    @JsonProperty("string")
    public String getString() {
        return string;
    }

    @JsonProperty("string")
    public void setString(String string) {
        this.string = string;
    }

    public Treatments withString(String string) {
        this.string = string;
        return this;
    }

    @JsonProperty("chars")
    public String getChars() {
        return chars;
    }

    @JsonProperty("chars")
    public void setChars(String chars) {
        this.chars = chars;
    }

    public Treatments withChars(String chars) {
        this.chars = chars;
        return this;
    }

}
