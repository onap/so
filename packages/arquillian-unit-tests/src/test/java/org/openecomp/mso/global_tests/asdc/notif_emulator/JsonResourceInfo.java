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

package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IResourceInstance;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class JsonResourceInfo implements IResourceInstance {

	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();

	@JsonProperty("artifacts")
	@JsonDeserialize(using=JsonArtifactInfoDeserializer.class)
	private List<IArtifactInfo> artifacts;

	public JsonResourceInfo() {

	}

	@Override
	public List<IArtifactInfo> getArtifacts() {
		return artifacts;
	}

	@Override
	public String getResourceInstanceName() {
		return (String)attributesMap.get("resourceInstanceName");
	}

	@Override
	public String getResourceInvariantUUID() {
		return (String)attributesMap.get("resourceInvariantUUID");
	}

	@Override
	public String getResourceName() {
		return (String)attributesMap.get("resourceName");
	}

	@Override
	public String getResourceType() {
		return (String)attributesMap.get("resourceType");
	}

	@Override
	public String getResourceUUID() {
		return (String)attributesMap.get("resourceUUID");
	}

	@Override
	public String getResourceVersion() {
		return (String)attributesMap.get("resourceVersion");
	}

	@Override
	public String getResourceCustomizationUUID() {
		return (String)attributesMap.get("resourceCustomizationUUID");
	}

	@Override
	public String getSubcategory() {
		return (String)attributesMap.get("subCategory");
	}

	@Override
	public String getCategory() {
		return (String)attributesMap.get("category");
	}

	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}
}
