/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.graphinventory;

import java.net.URI;

import org.onap.so.client.RestClient;
import org.onap.so.client.RestProperties;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public abstract class GraphInventoryClient {

	private RestProperties props;
	protected GraphInventoryClient(Class<? extends RestProperties> propertiesClass) {
				
		RestProperties props = RestPropertiesLoader.getInstance().getNewImpl(propertiesClass);
		this.props = props;
	}
	protected abstract URI constructPath(GraphInventoryUri uri);
	
	protected abstract RestClient createClient(GraphInventoryUri uri);

	protected <T extends RestProperties> T getRestProperties() {
		if (props == null) {
			throw new IllegalStateException("No RestProperty implementation found on classpath");
		}
		return (T)props;
	}
}
