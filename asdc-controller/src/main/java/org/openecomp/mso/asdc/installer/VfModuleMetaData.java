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

package org.openecomp.mso.asdc.installer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class VfModuleMetaData implements IVfModuleData {

	@JsonProperty("artifacts")
	private List<String> artifacts;

	@JsonProperty("properties")
	//private List<Map<String, Object>> properties = new ArrayList<>();
	private Map<String,String> properties = new HashMap<>();

	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();

	@Override
	public List<String> getArtifacts() {
		return artifacts;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String getVfModuleModelDescription() {
		return (String)attributesMap.get("vfModuleModelDescription");
	}

	@Override
	public String getVfModuleModelInvariantUUID() {
		return (String)attributesMap.get("vfModuleModelInvariantUUID");
	}

	public String getVfModuleModelCustomizationUUID() {
		return (String)attributesMap.get("vfModuleModelCustomizationUUID");
	}

	@Override
	public String getVfModuleModelName() {
		return (String)attributesMap.get("vfModuleModelName");
	}

	@Override
	public String getVfModuleModelUUID() {
		return (String)attributesMap.get("vfModuleModelUUID");
	}

	@Override
	public String getVfModuleModelVersion() {
		return (String)attributesMap.get("vfModuleModelVersion");
	}

	@Override
	public boolean isBase() {
		return (boolean)attributesMap.get("isBase");
	}



	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}

}
