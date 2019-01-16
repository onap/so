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
package org.onap.so.monitoring.model;

/**
 * @author waqas.ikram@ericsson.com
 */
import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

public class ProcessInstanceVariableDetail {

    private final String name;
    private final Object value;
    private final String type;

    public ProcessInstanceVariableDetail(final String name, final Object value, final String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessInstanceVariableDetail) {
            final ProcessInstanceVariableDetail other = (ProcessInstanceVariableDetail) obj;

            return isEqual(name, other.name) && isEqual(value, other.value) && isEqual(type, other.type);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ProcessInstanceVariableDetail [name=" + name + ", value=" + value + ", type=" + type + "]";
    }
}
