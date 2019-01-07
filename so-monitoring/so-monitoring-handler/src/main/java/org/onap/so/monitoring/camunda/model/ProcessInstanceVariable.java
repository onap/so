/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.camunda.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author waqas.ikram@ericsson.com
 */
public class ProcessInstanceVariable {

    private String name;
    private Object value;
    private String type;

    public ProcessInstanceVariable() {}

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessInstanceVariable) {
            final ProcessInstanceVariable other = (ProcessInstanceVariable) obj;
            return isEqual(name, other.name) && isEqual(value, other.value) && isEqual(type, other.type);
        }

        return false;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "ProcessInstance [name=" + name + ", value=" + value + ", type=" + type + "]";
    }
}
