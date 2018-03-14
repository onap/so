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
 
package org.openecomp.mso.client.dmaap.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.dmaap.Consumer;
import org.openecomp.mso.client.policy.RestClient;

public class RestConsumer implements Consumer {

	private final RestClient client;
	public RestConsumer(Properties properties) {
		PropertiesBean bean = new PropertiesBean(properties);
		client = new DMaaPRestClient(this.createURL(bean), bean.getContentType(), bean.getUsername(), bean.getPassword());
	}
	
	private URL createURL(PropertiesBean properties) {
		try {
			return UriBuilder.fromUri(properties.getHost())
					.path("events").path(properties.getTopic())
					.path(properties.getPartition())
					.path("consumer1")
					.queryParam("timeout",  properties.getTimeout()).build().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Iterable<String> fetch() {
		
		return client.get(new GenericType<List<String>>() {});
	}

}
