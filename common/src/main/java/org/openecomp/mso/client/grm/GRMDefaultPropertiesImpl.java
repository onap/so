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

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

public class GRMDefaultPropertiesImpl implements GRMProperties {
	
	public GRMDefaultPropertiesImpl() {
	}

	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL("http://localhost:28090");
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}

	@Override
	public String getDefaultVersion() {
		return "v1";
	}

	@Override
	public String getUsername() {
		return "gmruser";
	}

	@Override
	public String getPassword() {
		return "cGFzc3dvcmQ=";
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON;
	}

}
