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
package org.openecomp.mso.adapters.sdncrest;

import org.openecomp.mso.adapters.json.MapDeserializer;
import org.openecomp.mso.adapters.json.MapSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

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
@JsonRootName("SDNCServiceResponse")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "SDNCServiceResponse")
@NoJackson
public class SDNCServiceResponse extends SDNCResponseCommon implements Serializable {
	private static final long serialVersionUID = 1L;

	// Map of response parameters (possibly none).
	private Map<String, String> params = null;

	public SDNCServiceResponse(String sdncRequestId, String responseCode,
			String responseMessage, String ackFinalIndicator) {
		super(sdncRequestId, responseCode, responseMessage, ackFinalIndicator);
	}

	public SDNCServiceResponse() {
	}

	@JsonProperty("params")
	@JsonDeserialize(using = MapDeserializer.class)
	@XmlElement(name = "params")
	public Map<String, String> getParams() {
		return params;
	}

	@JsonProperty("params")
	@JsonSerialize(using = MapSerializer.class, include=JsonSerialize.Inclusion.NON_NULL)
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public void addParam(String name, String value) {
		if (params == null) {
			params = new LinkedHashMap<>();
		}
		params.put(name, value);
	}
}
