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

package org.openecomp.mso.client.restproperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.client.aai.AAIProperties;
import org.openecomp.mso.client.aai.AAIVersion;

public class AAIPropertiesImpl implements AAIProperties {

	final Map<String, String> props;

	public AAIPropertiesImpl() {
		this.props = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");

	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.get("aai.endpoint"));
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

	@Override
	public AAIVersion getDefaultVersion() {
		return AAIVersion.LATEST;
	}

	@Override
	public String getAuth() {
		return props.get("aai.auth");
	}

	@Override
	public String getKey() {
		return props.get("mso.msoKey");
	}
}
