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

import com.google.common.base.CaseFormat;

public enum AAIObjectPlurals implements AAIObjectName, AAIObjectUriTemplate, AAIObjectUriPartial {

	GENERIC_VNF(AAINamespaceConstants.NETWORK, "/generic-vnfs"),
	PSERVER(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers");
	
	private final String uriTemplate;
	private final String partialUri;
	private AAIObjectPlurals(String parentUri, String partialUri) {
		this.uriTemplate = parentUri + partialUri;
		this.partialUri = partialUri;
	}
	
	@Override
	public String toString() {
		return this.uriTemplate();
	}

	@Override
	public String uriTemplate() {
		return this.uriTemplate;
	}

	@Override
	public String partialUri() {
		return this.partialUri;
	}

	@Override
	public String typeName() {
		return this.typeName(CaseFormat.LOWER_HYPHEN);
	}
	@Override
	public String typeName(CaseFormat format) {
		String enumName = this.name();
		if (this.equals(AAIObjectType.DEFAULT_CLOUD_REGION) || this.equals(AAIObjectType.DEFAULT_TENANT)) {
			enumName = enumName.replace("DEFAULT_", "");
		}
		
		return CaseFormat.UPPER_UNDERSCORE.to(format, enumName);
	}
}
