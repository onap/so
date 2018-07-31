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

package org.onap.so.client.adapter.vnf;

import java.net.MalformedURLException;
import java.net.URL;

import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.adapter.rest.AdapterRestProperties;


public class VnfVolumeAdapterRestProperties implements AdapterRestProperties {

	protected static final String authProp = "mso.adapters.po.auth";
	protected static final String keyProp = "mso.msoKey";
	protected static final String endpointProp = "mso.adapters.volume-groups.rest.endpoint";

	public VnfVolumeAdapterRestProperties() {
	}
	
	@Override
	public String getAuth() {
		return UrnPropertiesReader.getVariable(authProp);
	}
	@Override
	public String getKey() {
		return UrnPropertiesReader.getVariable(keyProp);
	}
	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(UrnPropertiesReader.getVariable(endpointProp));
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

}
