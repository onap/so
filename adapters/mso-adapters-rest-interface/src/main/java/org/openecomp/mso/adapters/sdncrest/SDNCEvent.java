/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.sdncrest;

import org.openecomp.mso.adapters.json.MapDeserializer;
import org.openecomp.mso.adapters.json.MapSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.openecomp.mso.logger.MsoLogger;

// NOTE: the JAXB (XML) annotations are required with JBoss AS7 and RESTEasy,
//       even though we are using JSON exclusively.  The @NoJackson annotation
//       is also required in this environment.

/**
 * SDNC adapter success response for "agnostic" API services. Note that the
 * map of response parameters is represented this way in JSON:
 * <pre>
 * "params": {
 *   "entry": [
 *     {"key": "P1", "value": "V1"},
 *     {"key": "P2", "value": "V2"},
 *     ...
 *     {"key": "PN", "value": "VN"}
 *   ]
 * }
 * </pre>
 */
@JsonRootName("SDNCEvent")
@JsonSerialize(include= Inclusion.NON_NULL)
@XmlRootElement(name = "SDNCEvent")
@NoJackson
public class SDNCEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

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
		this.eventCorrelatorType =  eventCorrelatorType;
		this.eventCorrelator =  eventCorrelator;
	}

	public SDNCEvent() {
	}

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
	@JsonDeserialize(using = MapDeserializer.class)
	@XmlElement(name = "params")
	public Map<String, String> getParams() {
		return params;
	}

	@JsonProperty("params")
	@JsonSerialize(using = MapSerializer.class, include= Inclusion.NON_NULL)
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
			mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
		    LOGGER.debug("Exception:", e);
			throw new UnsupportedOperationException("Cannot convert "
				+ getClass().getSimpleName() + " to JSON", e);
		}
	}
}
