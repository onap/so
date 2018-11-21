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

package org.onap.so.bpmn.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object that stores data for rollbacks. Data is organized by type. A type is simply a string
 * identifier. Multiple types of data may be stored in the same object for separate rollback
 * operations.
 */
public class RollbackData implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final Map<String, Map<String, Serializable>> dictionary = new HashMap<>();

    /**
     * Returns true if the specified type is stored in this object.
     *
     * @param type the data type
     */
    public boolean hasType(final String type) {
        return dictionary.containsKey(type);
    }

    /**
     * Stores a single item.
     *
     * @param type the data type
     * @param key the key
     * @param value the value
     */
    public void put(final String type, final String key, final String value) {
        final Map<String, Serializable> mapForType = dictionary.computeIfAbsent(type, k -> new HashMap<>());

        mapForType.put(key, value);
    }

    /**
     * Gets a single item.
     *
     * @param type the data type
     * @param key the key
     * @return the item or null if there is no item for the specified type and key
     */
    public Serializable get(final String type, final String key) {
        final Map<String, Serializable> mapForType = dictionary.get(type);

        if (mapForType == null) {
            return null;
        }

        return mapForType.get(key);
    }

    /**
     * Gets a map containing all items associated with the specified data type.
     *
     * @param type the data type
     * @return a map, or null if there are no items associated with the specified data type
     */
    public Map<String, Serializable> get(final String type) {
        return dictionary.get(type);
    }

    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        return dictionary.entrySet().stream().map(entry -> entry.getKey() + entry.getValue())
                .collect(Collectors.joining(",", "[", "]"));
    }
}
