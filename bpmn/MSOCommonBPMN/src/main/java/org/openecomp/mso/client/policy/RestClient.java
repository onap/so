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

package org.openecomp.mso.client.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;


import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.stereotype.Service;

@Service
public abstract class RestClient {
	protected static final String ECOMP_COMPONENT_NAME = "MSO";

	private WebTarget webTarget;

	protected final Map<String, String> headerMap;
	protected final MsoLogger msoLogger;
	protected Map<String, String> properties;
	protected String host;

	protected RestClient(String endpointKey) {
		Logger logger = Logger.getLogger(getClass().getName());
		msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
		
		properties = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
		headerMap = new HashMap<>();
		initializeHeaderMap(headerMap);

		host = this.getHost(endpointKey);

		webTarget = ClientBuilder.newClient().register(logger).register(new LoggingFilter())
				.register(new CommonObjectMapperProvider()).target(host);
	}

	private String getHost(String key) {
		return properties.get(key);
	}

	protected Builder getBuilder() {
		Builder builder = webTarget.request();

		for (Entry<String, String> entry : headerMap.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}
		return builder;
	}

	protected abstract void initializeHeaderMap(Map<String, String> headerMap);
}
