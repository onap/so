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

package org.openecomp.mso.client.grm;

import java.net.URI;
import java.util.Base64;

import javax.ws.rs.core.UriBuilder;
import org.openecomp.mso.client.RestPropertiesLoader;
import org.openecomp.mso.client.policy.RestClient;

public class GRMRestInvoker {
	
	private final RestClient client;
	private final GRMProperties properties;
	
	public GRMRestInvoker(GRMAction action) {
		GRMProperties props = GRMPropertiesLoader.getInstance().getImpl();
		if (props == null) {
			props = new GRMDefaultPropertiesImpl();
		}
		this.properties = props;
		this.client = new GRMRestClient(this.properties, this.createURI(action), this.properties.getUsername(), this.decode(this.properties.getPassword()));
	}
	
	private URI createURI(GRMAction action) {
		return UriBuilder.fromUri("/GRMLWPService")
				.path(this.properties.getDefaultVersion())
				.path("serviceEndPoint")
				.path(action.getAction())
				.build();
	}
	
	private String decode(String cred) {
		try {
			return new String(Base64.getDecoder().decode(cred.getBytes()));
		} 
		catch(IllegalArgumentException iae) {
			return cred;
		}
	}
	
	private RestClient getClient() {
		return this.client;
	}
	
	public void post(Object obj) {
		getClient().post(obj);
	}
	
	public <T> T post(Object obj, Class<T> resultClass) {
		return getClient().post(obj, resultClass);
	}
	
}
