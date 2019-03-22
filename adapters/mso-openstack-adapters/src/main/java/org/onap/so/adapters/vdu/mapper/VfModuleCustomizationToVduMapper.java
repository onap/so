/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.vdu.mapper;

import java.util.List;

import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.springframework.stereotype.Component;

@Component
public class VfModuleCustomizationToVduMapper {

	public VduModelInfo mapVfModuleCustomizationToVdu(VfModuleCustomization vfModuleCustom)
	{
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUUID());
		vduModel.setModelUUID(vfModuleCustom.getVfModule().getModelUUID());
		vduModel.setModelInvariantUUID(vfModuleCustom.getVfModule().getModelInvariantUUID());

		// Map the cloud templates, attached files, and environment file
		mapCloudTemplates(vfModuleCustom.getVfModule().getModuleHeatTemplate(), vduModel);
		mapCloudFiles(vfModuleCustom,vduModel);
		mapEnvironment(vfModuleCustom.getHeatEnvironment(), vduModel);

		return vduModel;
	}

	public VduModelInfo mapVfModuleCustVolumeToVdu(VfModuleCustomization vfModuleCustom)
	{
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUUID());
		vduModel.setModelUUID(vfModuleCustom.getVfModule().getModelUUID());
		vduModel.setModelInvariantUUID(vfModuleCustom.getVfModule().getModelInvariantUUID());

		// Map the cloud templates, attached files, and environment file
		mapCloudTemplates(vfModuleCustom.getVfModule().getVolumeHeatTemplate(), vduModel);
		mapCloudFiles(vfModuleCustom,vduModel);
		mapEnvironment(vfModuleCustom.getVolumeHeatEnv(), vduModel);

		return vduModel;
	}

	private void mapCloudTemplates(HeatTemplate heatTemplate, VduModelInfo vduModel) {
		// TODO:  These catalog objects will be refactored to be non-Heat-specific

		List<VduArtifact> vduArtifacts = vduModel.getArtifacts();

		// Main template.  Also set the VDU timeout based on the main template.
		vduArtifacts.add(mapHeatTemplateToVduArtifact(heatTemplate, ArtifactType.MAIN_TEMPLATE));
		vduModel.setTimeoutMinutes(heatTemplate.getTimeoutMinutes());

		// Nested templates
		List<HeatTemplate> childTemplates = heatTemplate.getChildTemplates();
		if (childTemplates != null) {
			for(HeatTemplate childTemplate : childTemplates){
				vduArtifacts.add(mapHeatTemplateToVduArtifact(childTemplate, ArtifactType.NESTED_TEMPLATE));
			}
		}
	}

	private VduArtifact mapHeatTemplateToVduArtifact(HeatTemplate heatTemplate, ArtifactType artifactType) {
		VduArtifact vduArtifact = new VduArtifact();
		vduArtifact.setName(heatTemplate.getTemplateName());
		vduArtifact.setContent(heatTemplate.getHeatTemplate().getBytes());
		vduArtifact.setType(artifactType);
		return vduArtifact;
	}

	private void mapCloudFiles(VfModuleCustomization vfModuleCustom, VduModelInfo vduModel) {
		// TODO:  These catalog objects will be refactored to be non-Heat-specific

		List<VduArtifact> vduArtifacts = vduModel.getArtifacts();

		// Attached Files
		List<HeatFiles> heatFiles = vfModuleCustom.getVfModule().getHeatFiles();
		if (heatFiles != null) {
			for(HeatFiles file : heatFiles){
				vduArtifacts.add(mapCloudFileToVduArtifact(file, ArtifactType.TEXT_FILE));
			}
		}
	}

	private VduArtifact mapCloudFileToVduArtifact(HeatFiles heatFile, ArtifactType artifactType) {
		VduArtifact vduArtifact = new VduArtifact();
		vduArtifact.setName(heatFile.getFileName());
		vduArtifact.setContent(heatFile.getFileBody().getBytes());
		vduArtifact.setType(artifactType);
		return vduArtifact;
	}

	private void mapEnvironment(HeatEnvironment heatEnvironment, VduModelInfo vduModel) {
		// TODO:  These catalog objects will be refactored to be non-Heat-specific
		if (heatEnvironment != null) {
			List<VduArtifact> vduArtifacts = vduModel.getArtifacts();
			vduArtifacts.add(mapEnvironmentFileToVduArtifact(heatEnvironment));
		}
	}

	private VduArtifact mapEnvironmentFileToVduArtifact(HeatEnvironment heatEnv) {
		VduArtifact vduArtifact = new VduArtifact();
		vduArtifact.setName(heatEnv.getName());
		vduArtifact.setContent(heatEnv.getEnvironment().getBytes());
		vduArtifact.setType(ArtifactType.ENVIRONMENT);
		return vduArtifact;
	}

}
