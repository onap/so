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

package org.onap.so.apihandlerinfra.tenantisolation.dmaap;

import java.util.HashMap;
import java.util.Map;

import org.onap.so.client.dmaap.DmaapProperties;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;

public class DmaapPropertiesImpl implements DmaapProperties {

	private final Map<String, String> props  = new HashMap<>();
	private static final String[] propertyNames = {
			"mso.so.operational-environment.dmaap.auth",
			"mso.msoKey",
			"mso.so.operational-environment.publisher.topic",
			"mso.so.operational-environment.dmaap.host"
	};
	public DmaapPropertiesImpl () {
		ApplicationContext context = SpringContextHelper.getAppContext();
		
		for (String name : propertyNames) {
			this.props.put(name, context.getEnvironment().getProperty(name));

		}

	}
	
	@Override
	public Map<String, String> getProperties() {
		
		return this.props;
	}

}
