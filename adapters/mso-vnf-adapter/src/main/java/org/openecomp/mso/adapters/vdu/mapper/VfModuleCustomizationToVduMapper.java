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

package org.openecomp.mso.adapters.vdu.mapper;

import java.util.List;
import java.util.Map;
import org.openecomp.mso.adapters.vdu.VduArtifact;
import org.openecomp.mso.adapters.vdu.VduArtifact.ArtifactType;
import org.openecomp.mso.adapters.vdu.VduModelInfo;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.stereotype.Component;

@Component
public class VfModuleCustomizationToVduMapper {

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

	public VduModelInfo mapVfModuleCustomizationToVdu(VfModuleCustomization vfModuleCustom) {
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUuid());
		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			// Map the cloud templates, attached files, and environment file
			mapCloudTemplates(
					db.getHeatTemplateByArtifactUuid(vfModuleCustom.getVfModule().getHeatTemplateArtifactUUId()),
					vduModel);
			mapCloudFiles(vfModuleCustom, vduModel);
			mapEnvironment(db.getHeatEnvironmentByArtifactUuid(vfModuleCustom.getHeatEnvironmentArtifactUuid()),
					vduModel);
		}
		return vduModel;
	}

	public VduModelInfo mapVfModuleCustVolumeToVdu(VfModuleCustomization vfModuleCustom) {
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUuid());
		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			// Map the cloud templates, attached files, and environment file
			mapCloudTemplates(
					db.getHeatTemplateByArtifactUuid(vfModuleCustom.getVfModule().getVolHeatTemplateArtifactUUId()),
					vduModel);
			mapCloudFiles(vfModuleCustom, vduModel);
			mapEnvironment(db.getHeatEnvironmentByArtifactUuid(vfModuleCustom.getVolEnvironmentArtifactUuid()),
					vduModel);
		}
		return vduModel;
	}

	private void mapCloudTemplates(HeatTemplate heatTemplate, VduModelInfo vduModel) {
		// TODO: These catalog objects will be refactored to be
		// non-Heat-specific
		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			List<VduArtifact> vduArtifacts = vduModel.getArtifacts();

			// Main template. Also set the VDU timeout based on the main
			// template.
			vduArtifacts.add(mapHeatTemplateToVduArtifact(heatTemplate, ArtifactType.MAIN_TEMPLATE));
			vduModel.setTimeoutMinutes(heatTemplate.getTimeoutMinutes());
			// Nested templates
			Map<String,Object> nestedTemplates = db.getNestedTemplates(heatTemplate.getArtifactUuid());
			if (nestedTemplates != null) {
				for (String name : nestedTemplates.keySet()) {
					String body = (String) nestedTemplates.get(name);
					VduArtifact vduArtifact = new VduArtifact(name, body.getBytes(), ArtifactType.NESTED_TEMPLATE);
					vduArtifacts.add(vduArtifact);
				}
			}
		} catch (IllegalArgumentException e) {
			LOGGER.debug("unhandled exception in mapCloudTemplates", e);
			throw new IllegalArgumentException("Exception during mapCloudTemplates " + e.getMessage());
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
		// TODO: These catalog objects will be refactored to be
		// non-Heat-specific
		try (CatalogDatabase db = CatalogDatabase.getInstance()) {
			Map <String, HeatFiles> heatFiles = db.getHeatFilesForVfModule(vfModuleCustom.getVfModuleModelUuid());
			if (heatFiles != null) {
				for (HeatFiles heatFile: heatFiles.values()) {
					vduModel.getArtifacts().add(mapCloudFileToVduArtifact(heatFile, ArtifactType.TEXT_FILE));
				}
			}
		} catch (IllegalArgumentException e) {
			LOGGER.debug("unhandled exception in mapCloudFiles", e);
			throw new IllegalArgumentException("Exception during mapCloudFiles " + e.getMessage());
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
		// TODO: These catalog objects will be refactored to be
		// non-Heat-specific
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