/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.openstack.mappers;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapElements {
    private static final Logger logger = LoggerFactory.getLogger(MapElements.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @XmlElement
    public String key;
    @XmlElement
    public Object value;

    public MapElements() {
        // Required by JAXB
    }

    public MapElements(String key, Object value) {
        this.key = key;
        // this is required to handle marshalling raw json
        // always write values as strings for XML
        if (value != null) {
            if (value instanceof List || value instanceof Map) {
                try {
                    this.value = mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    logger.warn("could not marshal value to json, calling toString", e);
                    this.value = value.toString();
                }
            } else {
                this.value = value;
            }
        } else {
            this.value = value;
        }
    }
}
