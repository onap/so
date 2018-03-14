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

package org.openecomp.mso.client.aai;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.RestPropertiesLoader;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.defaultproperties.DefaultAAIPropertiesImpl;
import org.openecomp.mso.client.policy.RestClient;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public abstract class AAIClient {

	protected final AAIVersion defaultVersion;
	private static final String AAI_ROOT = "/aai";
	protected final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
	private final AAIProperties properties;
	protected final UUID requestId;
	public AAIClient(UUID requestId) {
		AAIProperties props = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
		if (props == null) {
			metricsLogger.error("No RestProperty.AAIProperties implementation found on classpath");
			props = new DefaultAAIPropertiesImpl();
		}
		this.properties = props;
		this.defaultVersion = props.getDefaultVersion();
		this.requestId = requestId;
	}
	protected URI constructPath(AAIUri uri) {
		
		return UriBuilder.fromUri(AAI_ROOT + "/" + this.getVersion().toString() + uri.build().toString()).build();
	}
	
	protected RestClient createClient(AAIUri uri) {
		return new AAIRestClient(properties, this.getRequestId(), constructPath(uri)).addRequestId(this.getRequestId());

	}
	
	protected UUID getRequestId() {
		return this.requestId;
	}
	protected AAIVersion getVersion() {
		return defaultVersion;
	}
}