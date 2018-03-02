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

package org.openecomp.mso.asdc.installer.heat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.openecomp.sdc.api.notification.IArtifactInfo;

import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.installer.ASDCElementInfo;
import org.openecomp.mso.asdc.installer.BigDecimalVersion;
import org.openecomp.mso.asdc.installer.IVfResourceInstaller;
import org.openecomp.mso.asdc.installer.VfModuleArtifact;
import org.openecomp.mso.asdc.installer.VfModuleStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.util.YamlEditor;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceToAllottedResources;
import org.openecomp.mso.db.catalog.beans.ServiceToNetworks;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class VfResourceInstaller implements IVfResourceInstaller {

	private MsoLogger logger;

	public VfResourceInstaller() {
		logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.ASDC);
	}

	@Override
	public boolean isResourceAlreadyDeployed(VfResourceStructure vfResourceStruct)
			throws ArtifactInstallerException {
	
		boolean status = false;
		VfResourceStructure vfResourceStructure = (VfResourceStructure)vfResourceStruct;

        	try(CatalogDatabase db = CatalogDatabase.getInstance()) {

			String resourceType = vfResourceStruct.getResourceInstance().getResourceType();
			String category = vfResourceStruct.getResourceInstance().getCategory();

			// Check for duplicate VF Module that is not an Allotted Resource
			if(resourceType.equals("VF") && !category.equalsIgnoreCase("Allotted Resource")){
				logger.info(MessageEnum.ASDC_CHECK_HEAT_TEMPLATE, "VNFResource",
					VfResourceInstaller.createVNFName(vfResourceStructure),
					BigDecimalVersion.castAndCheckNotificationVersionToString(
							vfResourceStructure.getNotification().getServiceVersion()), "", "");

				VnfResource vnfResource = db.getVnfResource(
					VfResourceInstaller.createVNFName(vfResourceStructure),
					BigDecimalVersion.castAndCheckNotificationVersionToString(
							vfResourceStructure.getNotification().getServiceVersion()));

			if (vnfResource != null) {
				status = true;

			}

			}

			// Check dup for VF Allotted Resource
			if(resourceType.equals("VF") && category.equalsIgnoreCase("Allotted Resource")){
				logger.info(MessageEnum.ASDC_CHECK_HEAT_TEMPLATE, "AllottedResource",
						vfResourceStruct.getResourceInstance().getResourceInstanceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()), "", "");

				List<AllottedResourceCustomization> allottedResources = db.getAllAllottedResourcesByServiceModelUuid(vfResourceStruct.getNotification().getServiceUUID());

				if(allottedResources != null && allottedResources.size() > 0){
					for(AllottedResourceCustomization allottedResource : allottedResources){

						String existingAllottedResource = allottedResource.getModelCustomizationUuid();
						String notificationAllottedResource = vfResourceStruct.getResourceInstance().getResourceCustomizationUUID();

						if(existingAllottedResource.equals(notificationAllottedResource)){
							status=true;
							break;
						}
					}

				}
			}

			// Check Network for duplicates
			if(resourceType.equals("VL")){
				logger.info(MessageEnum.ASDC_CHECK_HEAT_TEMPLATE, "NetworkResource",
						vfResourceStruct.getResourceInstance().getResourceInstanceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()), "", "");

				List<NetworkResourceCustomization> networkResources = db.getAllNetworksByServiceModelUuid(vfResourceStruct.getNotification().getServiceUUID());

				if(networkResources != null && networkResources.size() > 0){
					for(NetworkResourceCustomization networkResource : networkResources){

						String existingNetworkResource = networkResource.getModelCustomizationUuid();
						String notificationNetworkResource = vfResourceStruct.getResourceInstance().getResourceCustomizationUUID();

						if(existingNetworkResource.equals(notificationNetworkResource)){
							status=true;
							break;
						}
					}

				}
			}

			if (status) {
				logger.info(MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED_DETAIL,
						vfResourceStructure.getResourceInstance().getResourceInstanceName(),
						vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
						vfResourceStructure.getNotification().getServiceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()),
						vfResourceStructure.getNotification().getServiceUUID(),
						vfResourceStructure.getResourceInstance().getResourceName(),"", "");
			} else {
				logger.info(MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL,
						vfResourceStructure.getResourceInstance().getResourceInstanceName(),
						vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
						vfResourceStructure.getNotification().getServiceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()),
						vfResourceStructure.getNotification().getServiceUUID(),
						vfResourceStructure.getResourceInstance().getResourceName(),"", "");
			}

			return status;

		} catch (Exception e) {
			logger.error(MessageEnum.ASDC_ARTIFACT_CHECK_EXC, "", "", MsoLogger.ErrorCode.SchemaError, "Exception - isResourceAlreadyDeployed");
			throw new ArtifactInstallerException("Exception caught during checking existence of the VNF Resource.", e);
		}
	}

	@Override
	public void installTheResource(VfResourceStructure vfResourceStruct) throws ArtifactInstallerException {

		// 1. Add the DB object list (Hashed) to be created from the HashMap
		// UUID
		// The DB objects will be stored in each VfModuleArtifact objects
		// Those objects could be reused by different VfModule

		VfResourceStructure vfResourceStructure = (VfResourceStructure)vfResourceStruct;

		for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

			switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
			case ASDCConfiguration.HEAT:
			case ASDCConfiguration.HEAT_VOL:
			case ASDCConfiguration.HEAT_NESTED:
				VfResourceInstaller.createHeatTemplateFromArtifact(vfResourceStructure, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ENV:
				VfResourceInstaller.createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ARTIFACT:
				VfResourceInstaller.createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_NET:
			case ASDCConfiguration.OTHER:
				logger.warn(MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT, vfModuleArtifact.getArtifactInfo().getArtifactType()+"(Artifact Name:"+vfModuleArtifact.getArtifactInfo().getArtifactName()+")", "", "", MsoLogger.ErrorCode.DataError, "Artifact type not supported");
				break;
			default:
				break;

			}
		}

		// in case of deployment failure, use a string that will represent the type of artifact that failed...
		List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();

		CatalogDatabase catalogDB = CatalogDatabase.getInstance();
		// 2. Create the VFModules/VNFResource objects by linking them to the
		// objects created before and store them in Resource/module structure
		// Opening a DB transaction, starting from here
		try {

			VfResourceInstaller.createService(vfResourceStructure);

			String resourceType = vfResourceStructure.getResourceInstance().getResourceType();
			String resourceCategory = vfResourceStructure.getResourceInstance().getCategory();

			if("VF".equals(resourceType)){

				if(resourceCategory.equalsIgnoreCase("Allotted Resource")){
					VfResourceInstaller.createAllottedResourceCustomization(vfResourceStructure);
					catalogDB.saveAllottedResourceCustomization(vfResourceStructure.getCatalogResourceCustomization());
				} else {
					VfResourceInstaller.createVnfResource(vfResourceStructure);
					catalogDB.saveOrUpdateVnfResource(vfResourceStructure.getCatalogVnfResource());
				}
			}

			if("VL".equals(resourceType)){
				VfResourceInstaller.createNetworkResourceCustomization(vfResourceStructure);
				catalogDB.saveNetworkResourceCustomization(vfResourceStructure.getCatalogNetworkResourceCustomization());
			}

			// Add this one for logging
			artifactListForLogging.add(ASDCElementInfo.createElementFromVfResourceStructure(vfResourceStructure));

			//catalogDB.saveOrUpdateVnfResource(vfResourceStructure.getCatalogVnfResource());
			catalogDB.saveService(vfResourceStructure.getCatalogService());

			// Now that the service has been added we can populate the Service_to_AllottedResources table
			if("VF".equals(resourceType) && "Allotted Resource".equalsIgnoreCase(resourceCategory)){
				catalogDB.saveServiceToAllottedResources(vfResourceStructure.getCatalogServiceToAllottedResources());
			}

			// Now that the service has been added we can populate the Service_to_Network table
			if("VL".equals(resourceType)){
				catalogDB.saveServiceToNetworks(vfResourceStructure.getCatalogServiceToNetworks());
			}

			for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {

				// Here we set the right db structure according to the Catalog
				// DB

				// We expect only one MAIN HEAT per VFMODULE
				// we can also obtain from it the Env ArtifactInfo, that's why
				// we
				// get the Main IArtifactInfo

				HeatTemplate heatMainTemplate = null;
				HeatEnvironment heatEnv = null;

				HeatTemplate heatVolumeTemplate = null;
				HeatEnvironment heatVolumeEnv = null;

				if (vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT)) {
					IArtifactInfo mainEnvArtifactInfo = vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT)
							.get(0).getArtifactInfo().getGeneratedArtifact();

					// MAIN HEAT
					heatMainTemplate = (HeatTemplate) vfModuleStructure.getArtifactsMap()
							.get(ASDCConfiguration.HEAT).get(0).getCatalogObject();

					// Add this one for logging
					artifactListForLogging.add(ASDCElementInfo
							.createElementFromVfArtifactInfo(vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).getArtifactInfo()));

					catalogDB.saveHeatTemplate(heatMainTemplate, heatMainTemplate.getParameters());
					// Indicate we have deployed it in the DB
					vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).incrementDeployedInDB();


					// VOLUME HEAT
					// We expect only one VOL HEAT per VFMODULE
					// we can also obtain from it the Env ArtifactInfo, that's why
					// we get the Volume IArtifactInfo

					if (vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_VOL)) {
						IArtifactInfo volEnvArtifactInfo = vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0)
								.getArtifactInfo().getGeneratedArtifact();

						heatVolumeTemplate = (HeatTemplate) vfModuleStructure.getArtifactsMap()
								.get(ASDCConfiguration.HEAT_VOL).get(0).getCatalogObject();

						// Add this one for logging
						artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo()));

						catalogDB.saveHeatTemplate(heatVolumeTemplate, heatVolumeTemplate.getParameters());
						// Indicate we have deployed it in the DB
						vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).incrementDeployedInDB();

						if (volEnvArtifactInfo != null) {
							heatVolumeEnv = (HeatEnvironment) vfResourceStructure.getArtifactsMapByUUID()
									.get(volEnvArtifactInfo.getArtifactUUID()).getCatalogObject();

							// Add this one for logging
							artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(volEnvArtifactInfo));

							catalogDB.saveHeatEnvironment(heatVolumeEnv);
							// Indicate we have deployed it in the DB
							vfResourceStructure.getArtifactsMapByUUID().get(volEnvArtifactInfo.getArtifactUUID()).incrementDeployedInDB();
						}

					}

					// NESTED HEAT
					// Here we expect many HEAT_NESTED template to be there
					// check first if we really have nested heat templates
					if (vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_NESTED)) {
						for (VfModuleArtifact heatNestedArtifact : vfModuleStructure.getArtifactsMap()
								.get(ASDCConfiguration.HEAT_NESTED)) {

							// Check if this nested is well referenced by the MAIN HEAT
							String parentArtifactType = VfResourceInstaller.identifyParentOfNestedTemplate(vfModuleStructure,heatNestedArtifact);
							HeatTemplate heatNestedTemplate = (HeatTemplate) heatNestedArtifact.getCatalogObject();

							if (parentArtifactType != null) {

								switch (parentArtifactType) {
									case ASDCConfiguration.HEAT:

										// Add this one for logging
										artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(heatNestedArtifact.getArtifactInfo()));

										catalogDB.saveNestedHeatTemplate (heatMainTemplate.getArtifactUuid(), heatNestedTemplate, heatNestedTemplate.getTemplateName());
										// Indicate we have deployed it in the DB
										heatNestedArtifact.incrementDeployedInDB();
										break;
									case ASDCConfiguration.HEAT_VOL:

										// Add this one for logging
										artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(heatNestedArtifact.getArtifactInfo()));
										catalogDB.saveNestedHeatTemplate (heatVolumeTemplate.getArtifactUuid(), heatNestedTemplate, heatNestedTemplate.getTemplateName());
										// Indicate we have deployed it in the DB
										heatNestedArtifact.incrementDeployedInDB();
										break;

									default:
										break;

								}
							} else { // Assume it belongs to HEAT MAIN
								// Add this one for logging
								artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(heatNestedArtifact.getArtifactInfo()));

								catalogDB.saveNestedHeatTemplate (heatMainTemplate.getArtifactUuid(), heatNestedTemplate, heatNestedTemplate.getTemplateName());
								// Indicate we have deployed it in the DB
								heatNestedArtifact.incrementDeployedInDB();
							}
						}
					}

					if (mainEnvArtifactInfo != null) {
						heatEnv = (HeatEnvironment) vfResourceStructure.getArtifactsMapByUUID()
								.get(mainEnvArtifactInfo.getArtifactUUID()).getCatalogObject();

						// Add this one for logging
						artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(mainEnvArtifactInfo));

						catalogDB.saveHeatEnvironment(heatEnv);
						// Indicate we have deployed it in the DB
						vfResourceStructure.getArtifactsMapByUUID().get(mainEnvArtifactInfo.getArtifactUUID()).incrementDeployedInDB();
					}

				}


				// here we expect one VFModule to be there
/*				VfResourceInstaller.createVfModule(vfModuleStructure,heatMainTemplate, heatVolumeTemplate, heatEnv, heatVolumeEnv);
				VfModule vfModule = vfModuleStructure.getCatalogVfModule();

				// Add this one for logging
				artifactListForLogging.add(ASDCElementInfo.createElementFromVfModuleStructure(vfModuleStructure));

				catalogDB.saveOrUpdateVfModule(vfModule);

				// Here we expect many HEAT_TEMPLATE files to be there
				if (vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_ARTIFACT)) {
					for (VfModuleArtifact heatArtifact : vfModuleStructure.getArtifactsMap()
							.get(ASDCConfiguration.HEAT_ARTIFACT)) {

						HeatFiles heatFile = (HeatFiles) heatArtifact.getCatalogObject();

						// Add this one for logging
						artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(heatArtifact.getArtifactInfo()));


						catalogDB.saveVfModuleToHeatFiles (vfModule.getArtifactUuid(), heatFile);
						// Indicate we will deploy it in the DB
						heatArtifact.incrementDeployedInDB();
					}
				} */

			}

			catalogDB.commit();
			vfResourceStructure.setSuccessfulDeployment();

		} catch (Exception e) {

			Throwable dbExceptionToCapture = e;
			while (!(dbExceptionToCapture instanceof ConstraintViolationException || dbExceptionToCapture instanceof LockAcquisitionException)
					&& (dbExceptionToCapture.getCause() != null)) {
				dbExceptionToCapture = dbExceptionToCapture.getCause();
			}

			if (dbExceptionToCapture instanceof ConstraintViolationException || dbExceptionToCapture instanceof LockAcquisitionException) {
				logger.warn(MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED, vfResourceStructure.getResourceInstance().getResourceName(),
						vfResourceStructure.getNotification().getServiceVersion(), "", "", MsoLogger.ErrorCode.DataError, "Exception - ASCDC Artifact already deployed", e);
			} else {
				String endEvent = "Exception caught during installation of " + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.";
				String elementToLog = (artifactListForLogging.size() > 0 ? artifactListForLogging.get(artifactListForLogging.size()-1).toString() : "No element listed");
				logger.error(MessageEnum.ASDC_ARTIFACT_INSTALL_EXC, elementToLog, "", "", MsoLogger.ErrorCode.DataError, "Exception caught during installation of " + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback", e);
				catalogDB.rollback();
				throw new ArtifactInstallerException(
						"Exception caught during installation of " + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.", e);
			}

		} finally {
			catalogDB.close();
			// Debug log the whole collection...
			logger.debug(artifactListForLogging.toString());
		}

	}

	private static String identifyParentOfNestedTemplate(VfModuleStructure vfModuleStructure,VfModuleArtifact heatNestedArtifact) {

		if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT) != null
				&& vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).getArtifactInfo().getRelatedArtifacts() != null) {
			for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0)
					.getArtifactInfo().getRelatedArtifacts()) {
				if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
					return ASDCConfiguration.HEAT;
				}

			}
		}

		if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL) != null
				&& vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo().getRelatedArtifacts() != null) {
			for (IArtifactInfo unknownArtifact:vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo().getRelatedArtifacts()) {
				if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
					return ASDCConfiguration.HEAT_VOL;
				}

			}
		}

		// Does not belong to anything
		return null;

	}

	private static void createVnfResource(VfResourceStructure vfResourceStructure) {
		VnfResource vnfResource = new VnfResource();

		vnfResource.setModelUuid(vfResourceStructure.getResourceInstance().getResourceUUID());
		vnfResource.setDescription(vfResourceStructure.getNotification().getServiceDescription());

		vnfResource.setOrchestrationMode("HEAT");
		// Set the version but Version is stored into ASDC_SERVICE_MODEL_VERSION
		vnfResource.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfResourceStructure.getNotification().getServiceVersion()));
//		vnfResource.setVnfType(VfResourceInstaller.createVNFName(vfResourceStructure));
		vnfResource.setModelVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfResourceStructure.getResourceInstance().getResourceVersion()));

		vnfResource.setModelInvariantUuid(vfResourceStructure.getResourceInstance().getResourceInvariantUUID());
		vnfResource.setModelVersion(vfResourceStructure.getResourceInstance().getResourceVersion());
//		vnfResource.setModelCustomizationName(vfResourceStructure.getResourceInstance().getResourceInstanceName());
//		vnfResource.setModelCustomizationUuid(vfResourceStructure.getResourceInstance().getResourceCustomizationUUID());
		vnfResource.setModelName(vfResourceStructure.getResourceInstance().getResourceName());
//		vnfResource.setServiceModelInvariantUUID(vfResourceStructure.getNotification().getServiceInvariantUUID());
		
		//vnfResource.setCreated(getCurrentTimeStamp());

		vfResourceStructure.setCatalogVnfResource(vnfResource);
	}

	private static void createNetworkResourceCustomization(VfResourceStructure vfResourceStructure) {
		NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();

		networkResourceCustomization.setModelCustomizationUuid(vfResourceStructure.getResourceInstance().getResourceCustomizationUUID().trim());
//		networkResourceCustomization.setModelName(vfResourceStructure.getResourceInstance().getResourceName().trim());
		networkResourceCustomization.setModelInstanceName(vfResourceStructure.getResourceInstance().getResourceInstanceName().trim());
//		networkResourceCustomization.setModelInvariantUuid(vfResourceStructure.getResourceInstance().getResourceInvariantUUID().trim());
//		networkResourceCustomization.setModelUuid(vfResourceStructure.getResourceInstance().getResourceUUID().trim());
//		networkResourceCustomization.setModelVersion(vfResourceStructure.getResourceInstance().getResourceVersion().trim());
		//networkResourceCustomization.setCreated(getCurrentTimeStamp());

		vfResourceStructure.setCatalogNetworkResourceCustomization(networkResourceCustomization);

		ServiceToNetworks serviceNetworks = new ServiceToNetworks();
		serviceNetworks.setNetworkModelCustomizationUuid(networkResourceCustomization.getModelCustomizationUuid());
		serviceNetworks.setServiceModelUuid(vfResourceStructure.getNotification().getServiceUUID());

		vfResourceStructure.setCatalogServiceToNetworks(serviceNetworks);

	}

	private static void createAllottedResourceCustomization(VfResourceStructure vfResourceStructure) {
		AllottedResourceCustomization resourceCustomization = new AllottedResourceCustomization();

		resourceCustomization.setModelCustomizationUuid(vfResourceStructure.getResourceInstance().getResourceCustomizationUUID().trim());
//		resourceCustomization.setModelName(vfResourceStructure.getResourceInstance().getResourceName().trim());
		resourceCustomization.setModelInstanceName(vfResourceStructure.getResourceInstance().getResourceInstanceName().trim());
//		resourceCustomization.setModelInvariantUuid(vfResourceStructure.getResourceInstance().getResourceInvariantUUID().trim());
//		resourceCustomization.setModelUuid(vfResourceStructure.getResourceInstance().getResourceUUID().trim());
		resourceCustomization.setVersion(vfResourceStructure.getResourceInstance().getResourceVersion().trim());
		//resourceCustomization.setCreated(getCurrentTimeStamp());

		vfResourceStructure.setCatalogResourceCustomization(resourceCustomization);

		ServiceToAllottedResources serviceAllottedResources = new ServiceToAllottedResources();
		serviceAllottedResources.setArModelCustomizationUuid(resourceCustomization.getModelCustomizationUuid());
		serviceAllottedResources.setServiceModelUuid(vfResourceStructure.getNotification().getServiceUUID());

		vfResourceStructure.setCatalogServiceToAllottedResources(serviceAllottedResources);

	}

	private static void createVfModule(VfModuleStructure vfModuleStructure,HeatTemplate heatMain, HeatTemplate heatVolume,HeatEnvironment heatEnv, HeatEnvironment heatVolumeEnv) {
		VfModule vfModule = new VfModule();
//		vfModule.setType(createVfModuleName(vfModuleStructure));
//		vfModule.setAsdcUuid(vfModuleStructure.getVfModuleMetadata().getVfModuleModelUUID());
		vfModule.setDescription(vfModuleStructure.getVfModuleMetadata().getVfModuleModelDescription());

		if (vfModuleStructure.getVfModuleMetadata().isBase()) {
			vfModule.setIsBase(1);
		} else {
			vfModule.setIsBase(0);
		}

//		vfModule.setModelCustomizationUuid(vfModuleStructure.getVfModuleMetadata().getVfModuleModelCustomizationUUID()); 
		vfModule.setModelInvariantUuid(vfModuleStructure.getVfModuleMetadata().getVfModuleModelInvariantUUID());
		vfModule.setModelName(vfModuleStructure.getVfModuleMetadata().getVfModuleModelName());

		vfModule.setVersion(BigDecimalVersion.castAndCheckNotificationVersionToString(vfModuleStructure.getParentVfResource().getNotification().getServiceVersion()));
		vfModule.setModelVersion(BigDecimalVersion.castAndCheckNotificationVersionToString(
				vfModuleStructure.getVfModuleMetadata().getVfModuleModelVersion()));

		Map<String,String> map = vfModuleStructure.getVfModuleMetadata().getProperties();

		if(map != null){

			if(map.get("vf_module_label") != null){
//				vfModule.setLabel(map.get("vf_module_label"));
			}
			if(map.get("initial_count") != null && map.get("initial_count").length() > 0){
//				vfModule.setInitialCount(Integer.parseInt(map.get("initial_count")));
			}
			if(map.get("min_vf_module_instances") != null && map.get("min_vf_module_instances").length() > 0){
//				vfModule.setMinInstances(Integer.parseInt(map.get("min_vf_module_instances")));
			}
			if(map.get("max_vf_module_instances") != null && map.get("max_vf_module_instances").length() > 0){
//				vfModule.setMaxInstances(Integer.parseInt(map.get("max_vf_module_instances")));
			}

		}

		vfModuleStructure.setCatalogVfModule(vfModule);

		VfResourceInstaller.createVfModuleLinks(vfModule, vfModuleStructure.getParentVfResource().getCatalogVnfResource(), heatMain,heatVolume, heatEnv,heatVolumeEnv);
	}

	private static void createVfModuleLinks(VfModule vfModule, VnfResource vnfResource, HeatTemplate heatMain,
			HeatTemplate heatVolume, HeatEnvironment heatEnv, HeatEnvironment heatVolumeEnv) {

		if (heatMain !=null) {
//			vfModule.setTemplateId(heatMain.getId());
		}
		if (heatEnv != null) {
//			vfModule.setEnvironmentId(heatEnv.getId());
		}
		if (heatVolume != null) {
//			vfModule.setVolTemplateId(heatVolume.getId());
		}
		if (heatVolumeEnv != null) {
//			vfModule.setVolEnvironmentId(heatVolumeEnv.getId());
		}

//		vfModule.setVnfResourceId(vnfResource.getId());

	}


	private static Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {

		// Scan the payload downloadResult and extract the HeatTemplate
		// parameters
		YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
		return yamlEditor.getParameterList(artifactUUID);

	}


	public static String verifyTheFilePrefixInArtifacts(String filebody, VfResourceStructure vfResourceStructure, List<String> listTypes) {
		String newFileBody = filebody;
		for (VfModuleArtifact moduleArtifact:vfResourceStructure.getArtifactsMapByUUID().values()) {

			if (listTypes.contains(moduleArtifact.getArtifactInfo().getArtifactType())) {

				newFileBody = verifyTheFilePrefixInString(newFileBody,moduleArtifact.getArtifactInfo().getArtifactName());
			}
		}
		return newFileBody;
	}

	public static String verifyTheFilePrefixInString(final String body, final String filenameToVerify) {

		String needlePrefix = "file:///";
		String prefixedFilenameToVerify = needlePrefix+filenameToVerify;

		if ((body == null) || (body.length() == 0) || (filenameToVerify == null) || (filenameToVerify.length() == 0)) {
			return body;
		}

		StringBuilder sb = new StringBuilder(body.length());

		int currentIndex = 0;
		int startIndex = 0;

		while (currentIndex != -1) {
			startIndex = currentIndex;
			currentIndex = body.indexOf(prefixedFilenameToVerify, startIndex);

			if (currentIndex == -1) {
				break;
			}

			// We append from the startIndex up to currentIndex (start of File Name)
			sb.append(body.substring(startIndex, currentIndex));
			sb.append(filenameToVerify);

			currentIndex += prefixedFilenameToVerify.length();
		}

		sb.append(body.substring(startIndex));

		return sb.toString();
	}

	private static void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,
			VfModuleArtifact vfModuleArtifact) {
		HeatTemplate heatTemplate = new HeatTemplate();

		// TODO Set the label
//		heatTemplate.setAsdcLabel("label");
		// Use the ResourceName of the ASDC template because the HEAT could be
		// reused
//		heatTemplate.setAsdcResourceName(vfResourceStructure.getResourceInstance().getResourceName());
		heatTemplate.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

		List<String> typeList = new ArrayList<>();
		typeList.add(ASDCConfiguration.HEAT_NESTED);
		typeList.add(ASDCConfiguration.HEAT_ARTIFACT);

		heatTemplate.setTemplateBody(verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(),vfResourceStructure,typeList));
		heatTemplate.setTemplateName(vfModuleArtifact.getArtifactInfo().getArtifactName());

		if (vfModuleArtifact.getArtifactInfo().getArtifactTimeout() != null) {
			heatTemplate.setTimeoutMinutes(vfModuleArtifact.getArtifactInfo().getArtifactTimeout());
		} else {
			heatTemplate.setTimeoutMinutes(240);
		}

		heatTemplate.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatTemplate.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
		heatTemplate.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

		if(vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null){
			heatTemplate.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatTemplate.setArtifactChecksum("MANUAL_RECORD");
		}

		Set<HeatTemplateParam> heatParam = VfResourceInstaller
				.extractHeatTemplateParameters(vfModuleArtifact.getResult(), vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatTemplate.setParameters(heatParam);
		//heatTemplate.setCreated(getCurrentTimeStamp());

		vfModuleArtifact.setCatalogObject(heatTemplate);
	}

	private static void createHeatEnvFromArtifact(VfResourceStructure vfResourceStructure,
			VfModuleArtifact vfModuleArtifact) {
		HeatEnvironment heatEnvironment = new HeatEnvironment();

		heatEnvironment.setName(vfModuleArtifact.getArtifactInfo().getArtifactName());
		// TODO Set the label
//		heatEnvironment.setAsdcLabel("Label");

		List<String> typeList = new ArrayList<>();
		typeList.add(ASDCConfiguration.HEAT);
		typeList.add(ASDCConfiguration.HEAT_VOL);

		heatEnvironment.setEnvironment(verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(),vfResourceStructure,typeList));
//		heatEnvironment.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatEnvironment.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatEnvironment.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
//		heatEnvironment.setAsdcResourceName(VfResourceInstaller.createVNFName(vfResourceStructure));
		heatEnvironment.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

		if(vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null){
			heatEnvironment.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else{
			heatEnvironment.setArtifactChecksum("MANUAL_RECORD");
		}
		//heatEnvironment.setCreated(getCurrentTimeStamp());

		vfModuleArtifact.setCatalogObject(heatEnvironment);

	}

	private static void createHeatFileFromArtifact(VfResourceStructure vfResourceStructure,
			VfModuleArtifact vfModuleArtifact) {

		HeatFiles heatFile = new HeatFiles();
		// TODO Set the label
//		heatFile.setAsdcLabel("Label");
		heatFile.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatFile.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatFile.setFileBody(vfModuleArtifact.getResult());
		heatFile.setFileName(vfModuleArtifact.getArtifactInfo().getArtifactName());
		heatFile.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
		//heatFile.setCreated(getCurrentTimeStamp());

//		heatFile.setAsdcResourceName(vfResourceStructure.getResourceInstance().getResourceName());

		if(vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null){
			heatFile.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatFile.setArtifactChecksum("MANUAL_RECORD");
		}

		vfModuleArtifact.setCatalogObject(heatFile);

	}

	private static void createService(VfResourceStructure vfResourceStructure) {

		Service service = new Service();
		service.setDescription(vfResourceStructure.getNotification().getServiceDescription());
		service.setModelName(vfResourceStructure.getNotification().getServiceName());
		service.setModelUUID(vfResourceStructure.getNotification().getServiceUUID());
		service.setVersion(vfResourceStructure.getNotification().getServiceVersion());
		service.setModelInvariantUUID(vfResourceStructure.getNotification().getServiceInvariantUUID());
		//service.setCreated(getCurrentTimeStamp());

		vfResourceStructure.setCatalogService(service);
	}


	private static String createVNFName(VfResourceStructure vfResourceStructure) {

		return vfResourceStructure.getNotification().getServiceName() + "/" + vfResourceStructure.getResourceInstance().getResourceInstanceName();
	}

	private static String createVfModuleName(VfModuleStructure vfModuleStructure) {

		return createVNFName(vfModuleStructure.getParentVfResource())+"::"+vfModuleStructure.getVfModuleMetadata().getVfModuleModelName();
	}


	private static Timestamp getCurrentTimeStamp() {

		return new Timestamp(new Date().getTime());
	}


}
