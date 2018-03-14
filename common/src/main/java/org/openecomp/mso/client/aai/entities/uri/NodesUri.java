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

package org.openecomp.mso.client.aai.entities.uri;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;

public class NodesUri extends SimpleUri {

	protected NodesUri(AAIObjectType type, Object... values) {
		super(type, values);
	}
	
	
	@Override
	protected String getTemplate(AAIObjectType type) {
		return "/nodes" + type.partialUri();
	}
	
	@Override
	protected String getTemplate(AAIObjectPlurals type) {
		return "/nodes" + type.partialUri();
	}
}
