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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.db.catalog.beans.VfModule;

public final class VfModuleStructure {

	private final IVfModuleData vfModuleMetadata;

	private final VfResourceStructure parentVfResource;

	private VfModule catalogVfModule;
	/**
	 * The list of artifact existing in this resource hashed by artifactType.
	 */
	private final Map<String, List<VfModuleArtifact>> artifactsMap;

	public VfModuleStructure(VfResourceStructure vfParentResource,IVfModuleData vfmoduleMetadata) throws ArtifactInstallerException {

		vfModuleMetadata = vfmoduleMetadata;
		parentVfResource = vfParentResource;

		artifactsMap = new HashMap<String, List<VfModuleArtifact>>();

		for (String artifactUUID:this.vfModuleMetadata.getArtifacts()) {
			if (vfParentResource.getArtifactsMapByUUID().containsKey(artifactUUID)) {
				this.addToStructure(vfParentResource.getArtifactsMapByUUID().get(artifactUUID));
			} else {
				throw new ArtifactInstallerException("Artifact (UUID:"+artifactUUID+ ") referenced in the VFModule UUID list has not been downloaded, cancelling the Resource deployment");
			}
		}
	}

	private void addToStructure(VfModuleArtifact vfModuleArtifact) {

		if (artifactsMap.containsKey(vfModuleArtifact.getArtifactInfo().getArtifactType())) {
			artifactsMap.get(vfModuleArtifact.getArtifactInfo().getArtifactType()).add(vfModuleArtifact);

		} else {
			List<VfModuleArtifact> nestedList = new LinkedList<VfModuleArtifact>();
			nestedList.add(vfModuleArtifact);

			artifactsMap.put(vfModuleArtifact.getArtifactInfo().getArtifactType(), nestedList);
		}
	}

	public List<VfModuleArtifact> getOrderedArtifactList() {

		List <VfModuleArtifact> artifactsList = new LinkedList <VfModuleArtifact>();

		artifactsList.addAll(artifactsMap.get(ASDCConfiguration.HEAT));
		artifactsList.addAll(artifactsMap.get(ASDCConfiguration.HEAT_ENV));
		artifactsList.addAll(artifactsMap.get(ASDCConfiguration.HEAT_VOL));

		artifactsList.addAll((artifactsMap.get(ASDCConfiguration.HEAT_NESTED)));

		artifactsList.addAll((artifactsMap.get(ASDCConfiguration.HEAT_ARTIFACT)));

		artifactsList.addAll(artifactsMap.get(ASDCConfiguration.HEAT_VOL));

		return null;
	}

	public IVfModuleData getVfModuleMetadata() {
		return vfModuleMetadata;
	}

	public VfResourceStructure getParentVfResource() {
		return parentVfResource;
	}

	public Map<String, List<VfModuleArtifact>> getArtifactsMap() {
		return artifactsMap;
	}


	public VfModule getCatalogVfModule() {
		return catalogVfModule;
	}

	public void setCatalogVfModule(VfModule catalogVfModule) {
		this.catalogVfModule = catalogVfModule;
	}


}
