/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.sdncrest;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// NOTE: the JAXB (XML) annotations are required with JBoss AS7 and RESTEasy,
// even though we are using JSON exclusively. The @NoJackson annotation
// is also required in this environment.

/**
 * Map<String, String> elements when marshalled to XML produce a list of
 * <entry><key>${MsoUtils.xmlEscape(key)}</key><value>${MsoUtils.xmlEscape(value)}</value></entry> elements. When
 * marshalling to JSON they create a list of "${key}" : "${value}" pairs with no extra wrappers.
 * </pre>
 */
@JsonRootName("SDNCEvent")
@JsonInclude(Include.NON_NULL)
@XmlRootElement(name = "SDNCEvent")
public class SDNCEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SDNCEvent.class);

    // Event type
    private String eventType;

    // Event correlator type
    private String eventCorrelatorType;

    // Event correlator value.
    private String eventCorrelator;

    // Map of response parameters (possibly none).
    private Map<String, String> params = null;

    public SDNCEvent(String eventType, String eventCorrelatorType, String eventCorrelator) {
        this.eventType = eventType;
        this.eventCorrelatorType = eventCorrelatorType;
        this.eventCorrelator = eventCorrelator;
    }

    public SDNCEvent() {}

    @JsonProperty("eventType")
    @XmlElement(name = "eventType")
    public String getEventType() {
        return eventType;
    }

    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @JsonProperty("eventCorrelatorType")
    @XmlElement(name = "eventCorrelatorType")
    public String getEventCorrelatorType() {
        return eventCorrelatorType;
    }

    @JsonProperty("eventCorrelatorType")
    public void setEventCorrelatorType(String eventCorrelatorType) {
        this.eventCorrelatorType = eventCorrelatorType;
    }

    @JsonProperty("eventCorrelator")
    @XmlElement(name = "eventCorrelator")
    public String getEventCorrelator() {
        return eventCorrelator;
    }

    @JsonProperty("eventCorrelator")
    public void setEventCorrelator(String eventCorrelator) {
        this.eventCorrelator = eventCorrelator;
    }

    @JsonProperty("params")
    @XmlElement(name = "params")
    public Map<String, String> getParams() {
        return params;
    }

    @JsonProperty("params")
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParam(String name, String value) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(name, value);
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            mapper.setSerializationInclusion(Include.NON_NULL);
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            logger.debug("Exception:", e);
            throw new UnsupportedOperationException("Cannot convert " + getClass().getSimpleName() + " to JSON", e);
        }
    }
}
