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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;
import org.openecomp.mso.asdc.client.ASDCConfiguration;

/**
 * A class representing a generic element whose information can be used for example to log artifacts, resource... data.
 */
public class ASDCElementInfo {

	/**
	 * A default, empty instance used in case a source element was not correctly provided.
	 */
	public static final ASDCElementInfo EMPTY_INSTANCE = new ASDCElementInfo();

	/**
	 * Used to define the other possible ASDC Element types (usually in addition to existing artifact types, etc.).<br/>
	 * <br/>
	 * Possible types allowed:<br/>
	 * <ul>
	 * <li>{@link ASDCElementTypeEnum#VNF_RESOURCE}</li>
	 * <ul>
	 */
	public static enum ASDCElementTypeEnum {
		/**
		 * The type VNF_RESOURCE. Represents a VNF_RESOURCE element.
		 */
		VNF_RESOURCE
	};

	/**
	 * The map of element information fields useful for logging. The complete contents of this list will be concatenated.
	 */
	private final Map<String, String> elementInfoMap = new HashMap<>();

	/**
	 * The type of this element.
	 */
	private final String type;

	private ASDCElementInfo () {
		// Private parameterless constructor. Not visible, only used for EMPTY_INSTANCE.
		this.type = "";
	}

	/**
	 * Artifact-type based constructor. Requires a valid artifact type.
	 * @param artifactType The artifact type
	 */
	private ASDCElementInfo (String artifactType) {
		// We need the exact type name here...
		this.type = artifactType;
	}

	/**
	 * 'Other element type'-based constructor. Requires a valid element type.
	 * @param elementType An ASDCElementTypeEnum entry. This will usually contain enumerated types not in the existing
	 */
	private ASDCElementInfo (ASDCElementTypeEnum elementType) {
		// We need the exact type name here...
		this.type = elementType.name();
	}

	/**
	 * Add an information entry (name, UUID, etc.) from an artifact/resource/..., once a at time.
	 *
	 * @param key The key (name) of the information entry (Artifact UUID, Resource Name, etc.)
	 * @param value The value bound to the information entry.
	 */
	public final void addElementInfo(String key, String value) {
		if ((key != null) && (value != null)) {
			this.getElementInfoMap().put(key, value);
		}
	}

	/**
	 * Returns an aggregated, formatted list of the expected details about an ASDC element.
	 * (non-Javadoc)
	 * @return An aggregated list of element information entries, comma-separated.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> aggregatedElements = new ArrayList<>();
		for (Entry<String, String> entry : this.getElementInfoMap().entrySet()) {
			aggregatedElements.add(entry.getKey() + ": " + entry.getValue());
		}
		sb.append(aggregatedElements.size() > 0 ? aggregatedElements.get(0) : "");
		if (aggregatedElements.size() > 1) {
			for (int i = 1; i < aggregatedElements.size(); ++i) {
				sb.append (", ");
				sb.append(aggregatedElements.get(i));
			}
		}
		return sb.toString();
	}

	/**
	 * The type that was defined at creation time. This is typically VNF_RESOURCE, VF_MODULE_METADATA, HEAT_ENV, etc.
	 * @return The type of this element information type. This will usually be either an ArtifactTypeEnum entry name or an ASDCElementTypeEnum entry name.
	 * @see ASDCElementInfo.ASDCElementTypeEnum
	 */
	public String getType() {
		return type;
	}

	/**
	 * Provides the map of all element information entries for this type.
	 * @return A map of all element information entries which will be used by the toString() method.
	 * @see ASDCElementInfo#toString()
	 */
	protected Map<String, String> getElementInfoMap() {
		return elementInfoMap;
	}

	/**
	 * Create an ASDCElementInfo object from a VNF Resource.<br/>
	 * <br/>
	 * <b>Used information:</b><br/>
	 * <ul>
	 * <li>Resource Instance Name</li>
	 * <li>Resource Instance UUID</li>
	 * </ul>
	 *
	 * @param vfResourceStructure The VfResourceStructure to use as source of information (see {@link VfResourceStructure}).
	 * @return an ASDCElementInfo using the information held in the VNF Resource.
	 */
	public static final ASDCElementInfo createElementFromVfResourceStructure (VfResourceStructure vfResourceStructure) {
		if (vfResourceStructure == null) {
			return EMPTY_INSTANCE;
		}
		ASDCElementInfo elementInfo = new ASDCElementInfo(ASDCElementTypeEnum.VNF_RESOURCE);
		IResourceInstance resourceInstance = vfResourceStructure.getResourceInstance();
		elementInfo.addElementInfo("Resource Instance Name", resourceInstance.getResourceInstanceName());
		elementInfo.addElementInfo("Resource Instance Invariant UUID", resourceInstance.getResourceInvariantUUID());
		return elementInfo;
	}

	/**
	 * Create an ASDCElementInfo object from a VF Module.<br/>
	 * <br/>
	 * <b>Used information:</b><br/>
	 * <ul>
	 * <li>Module Model Name</li>
	 * <li>Module Model UUID</li>
	 * </ul>
	 *
	 * @param vfModuleStructure The VfModuleStructure to use as source of information (see {@link VfModuleStructure}).
	 * @return an ASDCElementInfo using the information held in the VF Module.
	 */
	public static final ASDCElementInfo createElementFromVfModuleStructure (VfModuleStructure vfModuleStructure) {
		if (vfModuleStructure == null) {
			return EMPTY_INSTANCE;
		}
		ASDCElementInfo elementInfo = new ASDCElementInfo(ASDCConfiguration.VF_MODULES_METADATA);
		IVfModuleData moduleMetadata = vfModuleStructure.getVfModuleMetadata();
		elementInfo.addElementInfo("Module Model Name", moduleMetadata.getVfModuleModelName());
		elementInfo.addElementInfo("Module Model Invariant UUID", moduleMetadata.getVfModuleModelInvariantUUID());
		return elementInfo;
	}

	/**
	 * Create an ASDCElementInfo object from an IArtfiactInfo instance.<br/>
	 * <br/>
	 * <b>Used information:</b><br/>
	 * <ul>
	 * <li>IArtifactInfo Name</li>
	 * <li>IArtifactInfo UUID</li>
	 * </ul>
	 *
	 * @param artifactInfo The VfModuleStructure to use as source of information (see {@link IArtifactInfo}).
	 * @return an ASDCElementInfo using the information held in the IArtifactInfo instance.
	 */
	public static final ASDCElementInfo createElementFromVfArtifactInfo (IArtifactInfo artifactInfo) {
		if (artifactInfo == null) {
			return EMPTY_INSTANCE;
		}
		ASDCElementInfo elementInfo = new ASDCElementInfo(artifactInfo.getArtifactType());
		elementInfo.addElementInfo(elementInfo.getType() + " Name", artifactInfo.getArtifactName());
		elementInfo.addElementInfo(elementInfo.getType() + " UUID", artifactInfo.getArtifactUUID());
		return elementInfo;
	}
}
