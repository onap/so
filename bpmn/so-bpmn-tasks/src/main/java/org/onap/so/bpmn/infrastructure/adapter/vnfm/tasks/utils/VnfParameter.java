/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is used to deserialize vnf-parameters from vnf-preload-list/{vnf-name}/{vnf-type} response
 * 
 * @author waqas.ikram@est.tech
 */
public class VnfParameter {

    @JsonProperty("vnf-parameter-name")
    private String name;

    @JsonProperty("vnf-parameter-value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int nameResult = prime + ((name == null) ? 0 : name.hashCode());
        int valueResult = prime + ((value == null) ? 0 : value.hashCode());
        return Objects.hash(nameResult, valueResult);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VnfParameter) {
            VnfParameter other = (VnfParameter) obj;
            return Objects.equals(name, other.name) && Objects.equals(value, other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return "VnfParameter [name=" + name + ", value=" + value + "]";
    }
}
