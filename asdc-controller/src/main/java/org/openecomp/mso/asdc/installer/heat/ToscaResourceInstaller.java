/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.Comparator;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
//import org.openecomp.generic.tosca.parser.model.Metadata;
//import org.openecomp.generic.tosca.parser.model.NodeTemplate;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;
import org.openecomp.sdc.tosca.parser.impl.SdcPropertyNames;
import org.openecomp.sdc.toscaparser.api.Group;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.Property;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.sdc.toscaparser.api.parameters.Input;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.installer.ASDCElementInfo;
import org.openecomp.mso.asdc.installer.BigDecimalVersion;
import org.openecomp.mso.asdc.installer.IVfModuleData;
import org.openecomp.mso.asdc.installer.IVfResourceInstaller;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;
import org.openecomp.mso.asdc.installer.VfModuleArtifact;
import org.openecomp.mso.asdc.installer.VfModuleStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.util.YamlEditor;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceToAllottedResources;
import org.openecomp.mso.db.catalog.beans.ServiceToNetworks;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfResCustomToVfModuleCustom;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class ToscaResourceInstaller {// implements IVfResourceInstaller {

	private MsoLogger logger;
	
	private static final Pattern lastDigit = Pattern.compile("(\\d+)$");

	public ToscaResourceInstaller()  {
		logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.ASDC);
	}

	//@Override
	public boolean isResourceAlreadyDeployed(VfResourceStructure vfResourceStruct)
			throws ArtifactInstallerException {
		boolean status = false;
		VfResourceStructure vfResourceStructure = (VfResourceStructure)vfResourceStruct;
		try {
		    status = vfResourceStructure.isDeployedSuccessfully();
		} catch (RuntimeException e) {
		    status = false;
		}
		
		try {
			
			String serviceUUID = vfResourceStruct.getNotification().getServiceUUID();			

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

	//@Override
	public void installTheResource(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct) throws ArtifactInstallerException {
		
		logger.debug("installTheResource is called");
		
		VfResourceStructure vfResourceStructure = (VfResourceStructure)vfResourceStruct;

		for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

			switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
			case ASDCConfiguration.HEAT:
			case ASDCConfiguration.HEAT_VOL:
			case ASDCConfiguration.HEAT_NESTED:
				ToscaResourceInstaller.createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ENV:
				ToscaResourceInstaller.createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ARTIFACT:
				ToscaResourceInstaller.createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact, toscaResourceStruct);
				break;
			case ASDCConfiguration.HEAT_NET:
			case ASDCConfiguration.OTHER:
				logger.warn(MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT, vfModuleArtifact.getArtifactInfo().getArtifactType()+"(Artifact Name:"+vfModuleArtifact.getArtifactInfo().getArtifactName()+")", "", "", MsoLogger.ErrorCode.DataError, "Artifact type not supported");
				break;
			default:
				break;

			}
		}
		// Those objects could be reused by different VfModule
		

		
		// PCLO: in case of deployment failure, use a string that will represent the type of artifact that failed...
		List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();
		
		CatalogDatabase catalogDB = CatalogDatabase.getInstance();
		// 2. Create the VFModules/VNFResource objects by linking them to the
		// objects created before and store them in Resource/module structure
		// Opening a DB transaction, starting from here
		try {

			
			createToscaCsar(toscaResourceStruct);
			
			catalogDB.saveToscaCsar(toscaResourceStruct.getCatalogToscaCsar());
			
			ToscaResourceInstaller.createService(toscaResourceStruct);
			
			catalogDB.saveService(toscaResourceStruct.getCatalogService());

			
			/* VNF POPULATION
			 * ************************************************************************************************************
			 */
			
            // Ingest (VNF) Data - 1707
            List<NodeTemplate> vfNodeTemplatesList = toscaResourceStruct.getSdcCsarHelper().getServiceVfList();
            int outerLoop = 0;
            logger.debug("**vfMondeTEmplatesList.size()=" + vfNodeTemplatesList.size());
            for(NodeTemplate nodeTemplate : vfNodeTemplatesList) {
                logger.debug("nodeTemplate outerLoop=" + outerLoop++);
                // extract VF metadata

                Metadata metadata = nodeTemplate.getMetaData();

                String vfCustomizationUUID = toscaResourceStruct.getSdcCsarHelper().getMetadataPropertyValue(metadata,
                        SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
                logger.debug("vfCustomizationUUID=" + vfCustomizationUUID);

                // extract VF metadata
                createVnfResource(nodeTemplate, toscaResourceStruct);

                // check for duplicate record already in the database
                VnfResource vnfResource =
                        catalogDB.getVnfResource(toscaResourceStruct.getCatalogVnfResource().getModelName(),
                                BigDecimalVersion.castAndCheckNotificationVersionToString(
                                        toscaResourceStruct.getCatalogVnfResource().getVersion()));

                if(vnfResource != null) {
                    toscaResourceStruct.setVnfAlreadyInstalled(true);
                }

                if(!toscaResourceStruct.isVnfAlreadyInstalled()) {

                    catalogDB.saveOrUpdateVnfResource(toscaResourceStruct.getCatalogVnfResource());

                }

                boolean saveVnfCustomization = catalogDB
                        .saveVnfResourceCustomization(toscaResourceStruct.getCatalogVnfResourceCustomization());

                if(saveVnfCustomization) {
                    catalogDB.saveServiceToResourceCustomization(
                            toscaResourceStruct.getCatalogVfServiceToResourceCustomization());
                }

                /*
                 * HEAT TABLE POPULATION
                 * *********************************************************************************
                 * **********************
                 */

                int nextLoop = 0;
                for(VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {
                    logger.debug("vfResourceStructure.getVfMOduleStructure() loop, nextLoop = " + nextLoop++);
                    logger.debug("vfModuleStructure:" + vfModuleStructure.toString());

                    // Here we set the right db structure according to the Catalog
                    // DB

                    // We expect only one MAIN HEAT per VFMODULE
                    // we can also obtain from it the Env ArtifactInfo, that's why
                    // we
                    // get the Main IArtifactInfo

                    HeatTemplate heatMainTemplate = null;
                    HeatEnvironment heatEnv;

                    HeatTemplate heatVolumeTemplate = null;
                    HeatEnvironment heatVolumeEnv;

                    IVfModuleData vfMetadata = vfModuleStructure.getVfModuleMetadata();

                    if(vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT)) {

                        List<VfModuleArtifact> artifacts =
                                vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT);
                        logger.debug("there are " + artifacts.size() + " artifacts");
                        IArtifactInfo mainEnvArtifactInfo = null;
                        for(VfModuleArtifact vfma : artifacts) {
                            logger.debug("vmfa=" + vfma.toString());
                            mainEnvArtifactInfo = vfma.getArtifactInfo().getGeneratedArtifact();

                            // MAIN HEAT
                            heatMainTemplate = (HeatTemplate)vfma.getCatalogObject();

                            // Set HeatTemplateArtifactUUID to use later when setting the VfModule
                            // and NetworkResource
                            toscaResourceStruct.setHeatTemplateUUID(heatMainTemplate.getArtifactUuid());

                            // Add this one for logging
                            artifactListForLogging
                                    .add(ASDCElementInfo.createElementFromVfArtifactInfo(vfma.getArtifactInfo()));

                            catalogDB.saveHeatTemplate(heatMainTemplate, heatMainTemplate.getParameters());
                            // Indicate we have deployed it in the DB
                            vfma.incrementDeployedInDB();
                        }

                        // VOLUME HEAT
                        // We expect only one VOL HEAT per VFMODULE
                        // we can also obtain from it the Env ArtifactInfo, that's why
                        // we get the Volume IArtifactInfo

                        if(vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_VOL)) {
                            IArtifactInfo volEnvArtifactInfo = vfModuleStructure.getArtifactsMap()
                                    .get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo().getGeneratedArtifact();

                            heatVolumeTemplate = (HeatTemplate)vfModuleStructure.getArtifactsMap()
                                    .get(ASDCConfiguration.HEAT_VOL).get(0).getCatalogObject();

                            // Set VolHeatTemplate ArtifactUUID to use later when setting the
                            // VfModule
                            toscaResourceStruct.setVolHeatTemplateUUID(heatVolumeTemplate.getArtifactUuid());

                            // Add this one for logging
                            artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(vfModuleStructure
                                    .getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo()));

                            catalogDB.saveHeatTemplate(heatVolumeTemplate, heatVolumeTemplate.getParameters());
                            // Indicate we have deployed it in the DB
                            vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0)
                                    .incrementDeployedInDB();

                            if(volEnvArtifactInfo != null) {
                                heatVolumeEnv = (HeatEnvironment)vfResourceStructure.getArtifactsMapByUUID()
                                        .get(volEnvArtifactInfo.getArtifactUUID()).getCatalogObject();

                                // Set VolHeatTemplate ArtifactUUID to use later when setting the
                                // VfModule
                                toscaResourceStruct.setVolHeatEnvTemplateUUID(heatVolumeEnv.getArtifactUuid());

                                // Add this one for logging
                                artifactListForLogging
                                        .add(ASDCElementInfo.createElementFromVfArtifactInfo(volEnvArtifactInfo));

                                catalogDB.saveHeatEnvironment(heatVolumeEnv);
                                // Indicate we have deployed it in the DB
                                vfResourceStructure.getArtifactsMapByUUID().get(volEnvArtifactInfo.getArtifactUUID())
                                        .incrementDeployedInDB();
                            }

                        }

                        // NESTED HEAT
                        // Here we expect many HEAT_NESTED template to be there
                        // XXX FIX BY PCLO: Defect# -36643 -US666034 - check first if we really have
                        // nested heat templates
                        if(vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_NESTED)) {
                            for(VfModuleArtifact heatNestedArtifact : vfModuleStructure.getArtifactsMap()
                                    .get(ASDCConfiguration.HEAT_NESTED)) {

                                // Check if this nested is well referenced by the MAIN HEAT
                                String parentArtifactType = ToscaResourceInstaller
                                        .identifyParentOfNestedTemplate(vfModuleStructure, heatNestedArtifact);
                                HeatTemplate heatNestedTemplate = (HeatTemplate)heatNestedArtifact.getCatalogObject();

                                if(parentArtifactType != null) {

                                    switch(parentArtifactType) {
                                        case ASDCConfiguration.HEAT:

                                            // Add this one for logging
                                            artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(
                                                    heatNestedArtifact.getArtifactInfo()));

                                            catalogDB.saveNestedHeatTemplate(heatMainTemplate.getArtifactUuid(),
                                                    heatNestedTemplate, heatNestedTemplate.getTemplateName());
                                            // Indicate we have deployed it in the DB
                                            heatNestedArtifact.incrementDeployedInDB();
                                            break;
                                        case ASDCConfiguration.HEAT_VOL:

                                            // Add this one for logging
                                            artifactListForLogging.add(ASDCElementInfo.createElementFromVfArtifactInfo(
                                                    heatNestedArtifact.getArtifactInfo()));
                                            catalogDB.saveNestedHeatTemplate(heatVolumeTemplate.getArtifactUuid(),
                                                    heatNestedTemplate, heatNestedTemplate.getTemplateName());
                                            // Indicate we have deployed it in the DB
                                            heatNestedArtifact.incrementDeployedInDB();
                                            break;

                                        default:
                                            break;

                                    }
                                } else { // Assume it belongs to HEAT MAIN
                                    // Add this one for logging
                                    artifactListForLogging.add(ASDCElementInfo
                                            .createElementFromVfArtifactInfo(heatNestedArtifact.getArtifactInfo()));

                                    catalogDB.saveNestedHeatTemplate(heatMainTemplate.getArtifactUuid(),
                                            heatNestedTemplate, heatNestedTemplate.getTemplateName());
                                    // Indicate we have deployed it in the DB
                                    heatNestedArtifact.incrementDeployedInDB();
                                }
                            }
                        }

                        if(mainEnvArtifactInfo != null) {
                            heatEnv = (HeatEnvironment)vfResourceStructure.getArtifactsMapByUUID()
                                    .get(mainEnvArtifactInfo.getArtifactUUID()).getCatalogObject();

                            // Set HeatEnvironmentArtifactUUID to use later when setting the
                            // VfModule
                            toscaResourceStruct.setEnvHeatTemplateUUID(heatEnv.getArtifactUuid());

                            // Add this one for logging
                            artifactListForLogging
                                    .add(ASDCElementInfo.createElementFromVfArtifactInfo(mainEnvArtifactInfo));

                            catalogDB.saveHeatEnvironment(heatEnv);
                            // Indicate we have deployed it in the DB
                            vfResourceStructure.getArtifactsMapByUUID().get(mainEnvArtifactInfo.getArtifactUUID())
                                    .incrementDeployedInDB();
                        }

                        // here we expect one VFModule to be there
                        // VfResourceInstaller.createVfModule(vfModuleStructure,heatMainTemplate,
                        // heatVolumeTemplate, heatEnv, heatVolumeEnv);
                        // VfModule vfModule = vfModuleStructure.getCatalogVfModule();

                        // Add this one for logging
                        // artifactListForLogging.add(ASDCElementInfo.createElementFromVfModuleStructure(vfModuleStructure));

                        // catalogDB.saveOrUpdateVfModule(vfModule);

                        List<org.openecomp.sdc.toscaparser.api.Group> vfGroups =
                                toscaResourceStruct.getSdcCsarHelper().getVfModulesByVf(vfCustomizationUUID);
                        logger.debug("vfGroups:" + vfGroups.toString());

                        Collections.sort(vfGroups, (group1, group2) -> {

                            // Field name1Field = group1.class.getDeclaredField("name");
                            // name1Field.setAccessible(true);
                            String thisName = group1.getName(); // (String)
                                                                // name1Field.get(group1);
                            String thatName = group2.getName(); // (String)
                                                                // name1Field.get(group2);

                            Matcher m = lastDigit.matcher(thisName);
                            Matcher m2 = lastDigit.matcher(thatName);

                            String thisDigit = "0";
                            String thatDigit = "0";
                            if(m.find()) {
                                thisDigit = m.group();
                            } else {
                                return -1;
                            }
                            if(m2.find()) {
                                thatDigit = m2.group();
                            } else {
                                return 1;
                            }

                            return new Integer(thisDigit).compareTo(new Integer(thatDigit));

                        });

                        logger.debug("vfGroupsAfter:" + vfGroups.toString());

                        for(Group group : vfGroups) {

                            // boolean saveVFModule = createVFModule(group, nodeTemplate,
                            // toscaResourceStruct, vfMetadata);
                            if(vfMetadata.getVfModuleModelCustomizationUUID() == null) {
                                logger.debug("NULL 1");
                            } else {
                                logger.debug("vfMetadata.getMCU=" + vfMetadata.getVfModuleModelCustomizationUUID());
                            }
                            if(group.getMetadata() == null) {
                                logger.debug("NULL 3");
                            } else {
                                logger.debug("group.getMetadata().getValue() = "
                                        + group.getMetadata().getValue("vfModuleModelCustomizationUUID"));
                            }
                            if(vfMetadata.getVfModuleModelCustomizationUUID()
                                    .equals(group.getMetadata().getValue("vfModuleModelCustomizationUUID"))) {
                                logger.debug("Found a match at " + vfMetadata.getVfModuleModelCustomizationUUID());
                                createVFModule(group, nodeTemplate, toscaResourceStruct, vfResourceStructure,
                                        vfMetadata);

                                catalogDB.saveOrUpdateVfModule(toscaResourceStruct.getCatalogVfModule());

                                catalogDB.saveOrUpdateVfModuleCustomization(
                                        toscaResourceStruct.getCatalogVfModuleCustomization());

                                catalogDB.saveVnfResourceToVfModuleCustomization(
                                        toscaResourceStruct.getCatalogVnfResourceCustomization(),
                                        toscaResourceStruct.getCatalogVfModuleCustomization());

                            } else {
                                if(toscaResourceStruct.getCatalogVfModuleCustomization() != null) {
                                    logger.debug("No match for " + toscaResourceStruct.getCatalogVfModuleCustomization()
                                            .getModelCustomizationUuid());
                                } else {
                                    logger.debug("No match for vfModuleModelCustomizationUUID");
                                }
                            }

                        }

                    } // Commented out to process VFModules each time

                    // Here we expect many HEAT_TEMPLATE files to be there
                    if(vfModuleStructure.getArtifactsMap().containsKey(ASDCConfiguration.HEAT_ARTIFACT)) {
                        for(VfModuleArtifact heatArtifact : vfModuleStructure.getArtifactsMap()
                                .get(ASDCConfiguration.HEAT_ARTIFACT)) {

                            HeatFiles heatFile = (HeatFiles)heatArtifact.getCatalogObject();

                            // Add this one for logging
                            artifactListForLogging.add(
                                    ASDCElementInfo.createElementFromVfArtifactInfo(heatArtifact.getArtifactInfo()));

                            if(toscaResourceStruct.getCatalogVfModule() != null && heatFile != null) {
                                catalogDB.saveVfModuleToHeatFiles(
                                        toscaResourceStruct.getCatalogVfModule().getModelUUID(), heatFile);
                            }
                            // Indicate we will deploy it in the DB
                            heatArtifact.incrementDeployedInDB();
                        }
                    }

                }

            }
				
				/* END OF HEAT TABLE POPULATION
				 * ***************************************************************************************************
				 */
            
            
          //  throw new ArtifactInstallerException("TESTING");
            
            
            List<NodeTemplate> allottedResourceList = toscaResourceStruct.getSdcCsarHelper().getAllottedResources();
        	
        		if(allottedResourceList != null){
        		
        			for(NodeTemplate allottedNode : allottedResourceList){
					
        				createAllottedResource(allottedNode, toscaResourceStruct);
					
        				catalogDB.saveAllottedResource(toscaResourceStruct.getAllottedResource());
					
        				catalogDB.saveAllottedResourceCustomization(toscaResourceStruct.getCatalogAllottedResourceCustomization());
        				
        				catalogDB.saveServiceToResourceCustomization(toscaResourceStruct.getCatalogAllottedServiceToResourceCustomization());
					
        			}
        		}
        		

        	List<NodeTemplate> nodeTemplatesVLList = toscaResourceStruct.getSdcCsarHelper().getServiceVlList();
				
        		if(nodeTemplatesVLList != null){
				
        			for(NodeTemplate vlNode : nodeTemplatesVLList){
        				
        				String networkResourceModelName = vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim();

        				List<TempNetworkHeatTemplateLookup> networkHeatTemplateLookup = catalogDB.getTempNetworkHeatTemplateLookup(networkResourceModelName);
        				
        				if(networkHeatTemplateLookup != null && networkHeatTemplateLookup.size() > 0 ){
					
        					createNetworkResource(vlNode, toscaResourceStruct, networkHeatTemplateLookup.get(0));
        				
        				} else {
                            logger.info(MessageEnum.ASDC_GENERAL_INFO,
                                    "No NetworkResourceName found in TempNetworkHeatTemplateLookup for" + networkResourceModelName, "ASDC",
                                    "createVfModuleStructures");
                            createNetworkResource(vlNode, toscaResourceStruct, null);
        				}
					
										
		   		 		catalogDB.saveNetworkResource(toscaResourceStruct.getCatalogNetworkResource());
						
		   		 		catalogDB.saveNetworkResourceCustomization(toscaResourceStruct.getCatalogNetworkResourceCustomization());
						
						catalogDB.saveServiceToResourceCustomization(toscaResourceStruct.getCatalogVlServiceToResourceCustomization());

        			}
        		} 	
            
		
        	   //createServiceToResourceCustomization(toscaResourceStruct.getCatalogService(), toscaResourceStruct.getCatalogVnfResourceCustomization(), toscaResourceStruct);
        	   
           // catalogDB.saveToscaCsar(toscaResourceStruct.getCatalogToscaCsar());
         	
 
			catalogDB.commit();	
			vfResourceStructure.setSuccessfulDeployment();
			
		}catch(Exception e){
			logger.debug("Exception :",e);
			
			Throwable dbExceptionToCapture = e;
			while (!(dbExceptionToCapture instanceof ConstraintViolationException || dbExceptionToCapture instanceof LockAcquisitionException)
					&& (dbExceptionToCapture.getCause() != null)) {
				dbExceptionToCapture = dbExceptionToCapture.getCause();
			}

			if (dbExceptionToCapture instanceof ConstraintViolationException || dbExceptionToCapture instanceof LockAcquisitionException) {
				logger.warn(MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED, vfResourceStructure.getResourceInstance().getResourceName(),
						vfResourceStructure.getNotification().getServiceVersion(), "", "", MsoLogger.ErrorCode.DataError, "Exception - ASCDC Artifact already deployed", e);
			} else {
			    String elementToLog = (artifactListForLogging.size() > 0 ? artifactListForLogging.get(artifactListForLogging.size()-1).toString() : "No element listed");
				logger.error(MessageEnum.ASDC_ARTIFACT_INSTALL_EXC, elementToLog, "", "", MsoLogger.ErrorCode.DataError, "Exception caught during installation of " + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback", e);
				catalogDB.rollback();
				throw new ArtifactInstallerException(
						"Exception caught during installation of " + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.", e);
			}

		} finally {
			catalogDB.close();
		}
				
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
	
	
	private static void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,ToscaResourceStructure toscaResourceStruct,
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
		
		//toscaResourceStruct.setHeatFilesUUID(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

		heatTemplate.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatTemplate.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
		heatTemplate.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		
		if(vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null){
			heatTemplate.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatTemplate.setArtifactChecksum("MANUAL_RECORD");
		}
		
		Set<HeatTemplateParam> heatParam = ToscaResourceInstaller
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
			VfModuleArtifact vfModuleArtifact, ToscaResourceStructure toscaResourceStruct) {

		HeatFiles heatFile = new HeatFiles();
		// TODO Set the label
//		heatFile.setAsdcLabel("Label");
		heatFile.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatFile.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatFile.setFileBody(vfModuleArtifact.getResult());
		heatFile.setFileName(vfModuleArtifact.getArtifactInfo().getArtifactName());
		heatFile.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
		
		toscaResourceStruct.setHeatFilesUUID(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		//heatFile.setCreated(getCurrentTimeStamp());

//		heatFile.setAsdcResourceName(vfResourceStructure.getResourceInstance().getResourceName());
		
		
		if(vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null){
			heatFile.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatFile.setArtifactChecksum("MANUAL_RECORD");
		}
		
		vfModuleArtifact.setCatalogObject(heatFile);
		
	}

	private static void createService(ToscaResourceStructure toscaResourceStructure) {
		
		toscaResourceStructure.getServiceMetadata();
		
		Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();
			
		Service service = new Service();
		
		//  Service	
		if(serviceMetadata != null){	
			
			if(toscaResourceStructure.getServiceVersion() != null){
				service.setVersion(toscaResourceStructure.getServiceVersion());
			}
			
			service.setServiceType(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(serviceMetadata, "serviceType"));
			service.setServiceRole(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(serviceMetadata, "serviceRole"));
			
			service.setDescription(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
			service.setModelName(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			service.setModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			//service.setVersion(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
			service.setModelInvariantUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
			service.setCategory(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
			service.setToscaCsarArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
			//service.setCreated(getCurrentTimeStamp());
		}
				
		toscaResourceStructure.setCatalogService(service);
	}
	
	private static void createToscaCsar(ToscaResourceStructure toscaResourceStructure) {
		
		ToscaCsar toscaCsar = new ToscaCsar();
		if(toscaResourceStructure.getToscaArtifact().getArtifactChecksum() != null){
			toscaCsar.setArtifactChecksum(toscaResourceStructure.getToscaArtifact().getArtifactChecksum());
		} else {
			toscaCsar.setArtifactChecksum("MANUAL_RECORD");
		}
		toscaCsar.setArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
		toscaCsar.setName(toscaResourceStructure.getToscaArtifact().getArtifactName());
		toscaCsar.setVersion(toscaResourceStructure.getToscaArtifact().getArtifactVersion());
		toscaCsar.setDescription(toscaResourceStructure.getToscaArtifact().getArtifactDescription());
		toscaCsar.setUrl(toscaResourceStructure.getToscaArtifact().getArtifactURL());
				
		toscaResourceStructure.setCatalogToscaCsar(toscaCsar);
	}
	
	private static void createNetworkResource(NodeTemplate networkNodeTemplate, ToscaResourceStructure toscaResourceStructure, TempNetworkHeatTemplateLookup networkHeatTemplateLookup) {
		NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
		
		NetworkResource networkResource = new NetworkResource();
		
		String providerNetwork = toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);
		
		if("true".equalsIgnoreCase(providerNetwork)){
			networkResource.setNeutronNetworkType("PROVIDER");
		} else {
			networkResource.setNeutronNetworkType("BASIC");
		}
		
		networkResource.setModelName(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
		
		networkResource.setModelInvariantUUID(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
		networkResource.setModelUUID(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
		networkResource.setModelVersion(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
		
		networkResource.setAicVersionMax(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
		String aicVersionMin = networkHeatTemplateLookup != null ? networkHeatTemplateLookup.getAicVersionMin() : "2.5";
		networkResource.setAicVersionMin(aicVersionMin);
		networkResource.setToscaNodeType(networkNodeTemplate.getType());
		networkResource.setDescription(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
		networkResource.setOrchestrationMode("HEAT");
		networkResource.setCategory(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
		networkResource.setSubCategory(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));
		//for tosca NS ,there is no heat for network VL
		String heatTemplateArtifactUUID = networkHeatTemplateLookup != null ? networkHeatTemplateLookup.getHeatTemplateArtifactUuid() : "null";
		networkResource.setHeatTemplateArtifactUUID(heatTemplateArtifactUUID);
			
		toscaResourceStructure.setCatalogNetworkResource(networkResource); 
		
		networkResourceCustomization.setModelInstanceName(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
		networkResourceCustomization.setModelCustomizationUuid(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
		networkResourceCustomization.setNetworkResourceModelUuid(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
		
				
		networkResourceCustomization.setNetworkTechnology(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY)).trim());
		networkResourceCustomization.setNetworkType(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE)).trim());
		networkResourceCustomization.setNetworkRole(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE)).trim());
		networkResourceCustomization.setNetworkScope(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE)).trim());
			
		toscaResourceStructure.setCatalogNetworkResourceCustomization(networkResourceCustomization);
		
		ServiceToResourceCustomization serviceToResourceCustomization = new ServiceToResourceCustomization();
		serviceToResourceCustomization.setServiceModelUUID(toscaResourceStructure.getCatalogService().getModelUUID());
		serviceToResourceCustomization.setResourceModelCustomizationUUID(testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
		serviceToResourceCustomization.setModelType("network");

		toscaResourceStructure.setCatalogVlServiceToResourceCustomization(serviceToResourceCustomization);
		
		
	}
	
	private static void createVFModule(Group group, NodeTemplate nodeTemplate, ToscaResourceStructure toscaResourceStructure, VfResourceStructure vfResourceStructure, IVfModuleData vfModuleData) {
		VfModule vfModule = new VfModule();
		
		Metadata vfMetadata = group.getMetadata();
		
		String vfModuleModelUUID = vfModuleData.getVfModuleModelUUID();
		
		
		if(vfModuleModelUUID != null && vfModuleModelUUID.contains(".")){
			vfModuleModelUUID = vfModuleModelUUID.substring(0, vfModuleModelUUID.indexOf("."));
		}
					
			
			for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()){
				
				String vfModelUUID = vfModuleStructure.getVfModuleMetadata().getVfModuleModelUUID();
												
				if(vfModelUUID != null && vfModelUUID.equalsIgnoreCase(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID))){
					
					List<String> artifacts = vfModuleStructure.getVfModuleMetadata().getArtifacts();
					
					for(String artifact: artifacts){

						
						for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {
							
							
							if(artifact.equals(vfModuleArtifact.getArtifactInfo().getArtifactUUID())){
								//if(vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ARTIFACT)){
								if(vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT)){
									vfModule.setHeatTemplateArtifactUUId(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
								}
								
								if(vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_VOL)){
									vfModule.setVolHeatTemplateArtifactUUId(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
									
								}
							}
							
						}
						
						
					}
					
	
				}
				
				
			}
							
			vfModule.setModelInvariantUuid(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID)));
			vfModule.setModelName(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME)));
			vfModule.setModelUUID(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)));
			vfModule.setVersion(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION)));
			vfModule.setDescription(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
			//vfModule.setHeatTemplateArtifactUUId(toscaResourceStructure.getHeatTemplateUUID());
			//vfModule.setVolHeatTemplateArtifactUUId(toscaResourceStructure.getVolHeatTemplateUUID());
						
			vfModule.setVnfResourceModelUUId(toscaResourceStructure.getCatalogVnfResource().getModelUuid());		
			
			String vfModuleType = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE);
			if(vfModuleType != null && "Base".equalsIgnoreCase(vfModuleType)){
				vfModule.setIsBase(1);	
			}else {
				vfModule.setIsBase(0);
			}
			

			VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
			vfModuleCustomization.setModelCustomizationUuid(vfModuleData.getVfModuleModelCustomizationUUID());
		
			
			vfModuleCustomization.setVfModuleModelUuid(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));
			
			vfModuleCustomization.setHeatEnvironmentArtifactUuid(toscaResourceStructure.getEnvHeatTemplateUUID());
			

			vfModuleCustomization.setVolEnvironmentArtifactUuid(toscaResourceStructure.getVolHeatEnvTemplateUUID());
			
			String initialCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT);
			if(initialCount != null && initialCount.length() > 0){
				vfModuleCustomization.setInitialCount(Integer.valueOf(initialCount));
			}
		
			vfModuleCustomization.setInitialCount(Integer.valueOf(toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT)));
			
			String availabilityZoneCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);
			if(availabilityZoneCount != null && availabilityZoneCount.length() > 0){
				vfModuleCustomization.setAvailabilityZoneCount(Integer.valueOf(availabilityZoneCount));
			}
			

			vfModuleCustomization.setLabel(toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_VFMODULELABEL));
			
			String maxInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_MAXVFMODULEINSTANCES);
			if(maxInstances != null && maxInstances.length() > 0){
				vfModuleCustomization.setMaxInstances(Integer.valueOf(maxInstances));
			}
			
			String minInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_MINVFMODULEINSTANCES);
			if(minInstances != null && minInstances.length() > 0){
				vfModuleCustomization.setMinInstances(Integer.valueOf(minInstances));
			}
	
			toscaResourceStructure.setCatalogVfModule(vfModule); 
		
			toscaResourceStructure.setCatalogVfModuleCustomization(vfModuleCustomization);
			
	}
	
	private static void createVnfResourceToVfModuleCustomization(VnfResourceCustomization vnfResourceCustomization, 
			                                                     VfModuleCustomization vfModuleCustomization, 
			                                                     ToscaResourceStructure toscaResourceStructure) {
		
		VnfResCustomToVfModuleCustom vnfResCustomToVfModuleCustom = new VnfResCustomToVfModuleCustom();
		
		vnfResCustomToVfModuleCustom.setVfModuleCustModelCustomizationUuid(vfModuleCustomization.getModelCustomizationUuid());
		vnfResCustomToVfModuleCustom.setVnfResourceCustModelCustomizationUuid(vnfResourceCustomization.getModelCustomizationUuid());
		
		toscaResourceStructure.setCatalogVnfResCustomToVfModuleCustom(vnfResCustomToVfModuleCustom);
		

			
	}
	
	
	private static void createTempNetworkHeatTemplateLookup(NetworkResource networkResource, 
            VfModule vfModule, 
            ToscaResourceStructure toscaResourceStructure) {
		
			TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup = new TempNetworkHeatTemplateLookup();

			tempNetworkHeatTemplateLookup.setNetworkResourceModelName(networkResource.getModelName());
			tempNetworkHeatTemplateLookup.setHeatTemplateArtifactUuid(vfModule.getHeatTemplateArtifactUUId());
			tempNetworkHeatTemplateLookup.setAicVersionMin("1");

			toscaResourceStructure.setCatalogTempNetworkHeatTemplateLookup(tempNetworkHeatTemplateLookup);



	}
	
	private static void createVnfResource(NodeTemplate vfNodeTemplate, ToscaResourceStructure toscaResourceStructure) {
		VnfResource vnfResource = new VnfResource();
		
		
		//toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT)
		
		vnfResource.setModelInvariantUuid(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID).trim()));
		vnfResource.setModelName(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
		vnfResource.setModelUuid(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));

		vnfResource.setVersion(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION).trim()));
		vnfResource.setDescription(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION).trim()));
		vnfResource.setOrchestrationMode("HEAT");
		vnfResource.setToscaNodeType(testNull(vfNodeTemplate.getType()));
		vnfResource.setAicVersionMax(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES).trim()));
		vnfResource.setAicVersionMin(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES).trim()));
        // vnfResource.setHeatTemplateArtifactUUId(toscaResourceStructure.getHeatTemplateUUID());
        vnfResource.setCategory(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        vnfResource.setSubCategory(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));
        // vfNodeTemplate.getProperties()
        toscaResourceStructure.setCatalogVnfResource(vnfResource);

		VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
		vnfResourceCustomization.setModelCustomizationUuid(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
		vnfResourceCustomization.setModelInstanceName(vfNodeTemplate.getName());
		
		vnfResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)).trim());
		vnfResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFCODE)).trim());
		vnfResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)).trim());
		vnfResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)).trim());
		
		
		vnfResourceCustomization.setVnfResourceModelUuid(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
		vnfResourceCustomization.setAvailabilityZoneMaxCount(Integer.getInteger(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT).trim()));

		vnfResourceCustomization.setMaxInstances(Integer.getInteger(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES).trim()));
		vnfResourceCustomization.setMinInstances(Integer.getInteger(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES).trim()));


		
		toscaResourceStructure.setCatalogVnfResourceCustomization(vnfResourceCustomization);
		
		
		ServiceToResourceCustomization serviceToResourceCustomization = new ServiceToResourceCustomization();
		serviceToResourceCustomization.setServiceModelUUID(toscaResourceStructure.getCatalogService().getModelUUID());
		serviceToResourceCustomization.setResourceModelCustomizationUUID(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim());
		serviceToResourceCustomization.setModelType("vnf");

		toscaResourceStructure.setCatalogVfServiceToResourceCustomization(serviceToResourceCustomization);
		
		
	}
		
	private static void createAllottedResource(NodeTemplate nodeTemplate, ToscaResourceStructure toscaResourceStructure) {
		AllottedResource allottedResource = new AllottedResource();
		
		allottedResource.setModelUuid(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
		allottedResource.setModelInvariantUuid(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID).trim()));
		allottedResource.setModelName(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
		allottedResource.setModelVersion(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION).trim()));
		allottedResource.setToscaNodeType(testNull(nodeTemplate.getType()));
		
		toscaResourceStructure.setAllottedResource(allottedResource);
		
		AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
		allottedResourceCustomization.setModelCustomizationUuid(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
		allottedResourceCustomization.setModelInstanceName(nodeTemplate.getName());
		allottedResourceCustomization.setArModelUuid(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
		
		allottedResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)).trim());
		allottedResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFCODE)).trim());
		allottedResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)).trim());
		allottedResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)).trim());
		
		toscaResourceStructure.setCatalogAllottedResourceCustomization(allottedResourceCustomization);
		
		ServiceToResourceCustomization serviceToResourceCustomization = new ServiceToResourceCustomization();
		serviceToResourceCustomization.setServiceModelUUID(toscaResourceStructure.getCatalogService().getModelUUID());
		serviceToResourceCustomization.setResourceModelCustomizationUUID(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
		serviceToResourceCustomization.setModelType("allottedResource");

		toscaResourceStructure.setCatalogAllottedServiceToResourceCustomization(serviceToResourceCustomization);
		
	}
	
	private static Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {

		// Scan the payload downloadResult and extract the HeatTemplate
		// parameters
		YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
		return yamlEditor.getParameterList(artifactUUID);

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
	
	private static String testNull(Object object) {
		if (object == null) {
			return "";
		} else if ("null".equals(object)) {
			return null;
		}else if (object instanceof Integer) {
			return object.toString();
		} else if (object instanceof String) {
			return (String)object;
		} else {
			return "Type not recognized";
		}
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