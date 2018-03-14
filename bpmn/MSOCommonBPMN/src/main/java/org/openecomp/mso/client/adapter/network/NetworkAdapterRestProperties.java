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

package org.openecomp.mso.client.adapter.network;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.adapter.vnf.AdapterRestProperties;

public class NetworkAdapterRestProperties implements AdapterRestProperties {

	private final Map<String, String> props;
	
	public NetworkAdapterRestProperties() {
		this.props = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
	}
	
	@Override
	public String getAuth() {
		return props.get("mso.adapters.po.auth");
	}
	@Override
	public String getKey() {
		return props.get("mso.msoKey");
	}
	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.get("mso.adapters.network.rest.endpoint"));
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

}
