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

package org.onap.so.client.dmaapproperties;

import org.onap.so.bpmn.core.UrnPropertiesReader;
import java.util.HashMap;

/**
 * This class is used when Dmaap Properties are to be accessed from application.yaml and it delegates get calls to
 * UrnPropertyReader class for reading the value from active configuration
 * 
 * @param <K> Key for Map Entry
 * @param <V> Value for Map Entry
 */
public class DmaapPropertiesMap<K, V> extends HashMap<K, V> {

    @Override
    public V get(Object key) {
        return (V) UrnPropertiesReader.getVariable((String) key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        String str = UrnPropertiesReader.getVariable((String) key);
        return str == null ? defaultValue : (V) str;
    }


}
