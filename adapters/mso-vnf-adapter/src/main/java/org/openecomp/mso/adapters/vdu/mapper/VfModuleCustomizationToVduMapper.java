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

	public VduModelInfo mapVfModuleCustomizationToVdu(VfModuleCustomization vfModuleCustom) throws Exception {
		CatalogDatabase db = CatalogDatabase.getInstance();
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUuid());
		try {
			// Map the cloud templates, attached files, and environment file
			mapCloudTemplates(
					db.getHeatTemplateByArtifactUuid(vfModuleCustom.getVfModule().getHeatTemplateArtifactUUId()),
					vduModel);
			mapCloudFiles(vfModuleCustom, vduModel);
			mapEnvironment(db.getHeatEnvironmentByArtifactUuid(vfModuleCustom.getHeatEnvironmentArtifactUuid()),
					vduModel);
		} catch (Exception e) {
			LOGGER.debug("unhandled exception in mapVfModuleCustomizationToVdu", e);
			throw new Exception("Exception during mapVfModuleCustomizationToVdu " + e.getMessage());
		} finally {
			// Make sure DB session is closed
			db.close();
		}

		return vduModel;
	}

	public VduModelInfo mapVfModuleCustVolumeToVdu(VfModuleCustomization vfModuleCustom) throws Exception {
		CatalogDatabase db = CatalogDatabase.getInstance();
		VduModelInfo vduModel = new VduModelInfo();
		vduModel.setModelCustomizationUUID(vfModuleCustom.getModelCustomizationUuid());
		try {
			// Map the cloud templates, attached files, and environment file
			mapCloudTemplates(
					db.getHeatTemplateByArtifactUuid(vfModuleCustom.getVfModule().getVolHeatTemplateArtifactUUId()),
					vduModel);
			mapCloudFiles(vfModuleCustom, vduModel);
			mapEnvironment(db.getHeatEnvironmentByArtifactUuid(vfModuleCustom.getVolEnvironmentArtifactUuid()),
					vduModel);
		} catch (Exception e) {
			LOGGER.debug("unhandled exception in mapVfModuleCustVolumeToVdu", e);
			throw new Exception("Exception during mapVfModuleCustVolumeToVdu " + e.getMessage());
		} finally {
			// Make sure DB session is closed
			db.close();
		}

		return vduModel;
	}

	private void mapCloudTemplates(HeatTemplate heatTemplate, VduModelInfo vduModel) throws Exception {
		// TODO: These catalog objects will be refactored to be
		// non-Heat-specific
		CatalogDatabase db = CatalogDatabase.getInstance();
		try {
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
			
		} catch (Exception e) {
			LOGGER.debug("unhandled exception in mapCloudTemplates", e);
			throw new Exception("Exception during mapCloudTemplates " + e.getMessage());
		} finally {
			// Make sure DB session is closed
			db.close();
		}
	}

	private VduArtifact mapHeatTemplateToVduArtifact(HeatTemplate heatTemplate, ArtifactType artifactType) {
		VduArtifact vduArtifact = new VduArtifact();
		vduArtifact.setName(heatTemplate.getTemplateName());
		vduArtifact.setContent(heatTemplate.getHeatTemplate().getBytes());
		vduArtifact.setType(artifactType);
		return vduArtifact;
	}

	private void mapCloudFiles(VfModuleCustomization vfModuleCustom, VduModelInfo vduModel) throws Exception {
		// TODO: These catalog objects will be refactored to be
		// non-Heat-specific
		CatalogDatabase db = CatalogDatabase.getInstance();

		try{
			Map <String, HeatFiles> heatFiles = db.getHeatFilesForVfModule(vfModuleCustom.getVfModuleModelUuid());
			if (heatFiles != null) {
				for (HeatFiles heatFile: heatFiles.values()) {
					mapCloudFileToVduArtifact(heatFile, ArtifactType.TEXT_FILE);
				}
			}
		} catch (Exception e) {
			LOGGER.debug("unhandled exception in mapCloudFiles", e);
			throw new Exception("Exception during mapCloudFiles " + e.getMessage());
		} finally {
			// Make sure DB session is closed
			db.close();
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