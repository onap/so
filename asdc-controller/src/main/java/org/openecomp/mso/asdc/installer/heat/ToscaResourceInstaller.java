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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.tosca.parser.impl.SdcTypes;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.CapabilityAssignments;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.openecomp.mso.apihandler.beans.avpnbondingbeans.AVPNServiceNames;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.installer.ASDCElementInfo;
import org.openecomp.mso.asdc.installer.BigDecimalVersion;
import org.openecomp.mso.asdc.installer.IVfModuleData;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;
import org.openecomp.mso.asdc.installer.VfModuleArtifact;
import org.openecomp.mso.asdc.installer.VfModuleStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.util.YamlEditor;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.ConfigurationResource;
import org.openecomp.mso.db.catalog.beans.ConfigurationResourceCustomization;
import org.openecomp.mso.db.catalog.beans.ExternalServiceToInternalService;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.InstanceGroupType;
import org.openecomp.mso.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkInstanceGroup;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceProxyResource;
import org.openecomp.mso.db.catalog.beans.ServiceProxyResourceCustomization;
import org.openecomp.mso.db.catalog.beans.SubType;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VFCInstanceGroup;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.AllottedResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.CollectionResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.CollectionResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.ConfigurationResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.ExternalServiceToInternalServiceRepository;
import org.openecomp.mso.db.catalog.data.repository.HeatTemplateRepository;
import org.openecomp.mso.db.catalog.data.repository.InstanceGroupRepository;
import org.openecomp.mso.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.NetworkResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.ServiceProxyResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.ServiceRepository;
import org.openecomp.mso.db.catalog.data.repository.TempNetworkHeatTemplateRepository;
import org.openecomp.mso.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.VFModuleRepository;
import org.openecomp.mso.db.catalog.data.repository.VnfCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.VnfResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;
import org.openecomp.mso.db.request.beans.WatchdogComponentDistributionStatus;
import org.openecomp.mso.db.request.beans.WatchdogDistributionStatus;
import org.openecomp.mso.db.request.beans.WatchdogServiceModVerIdLookup;
import org.openecomp.mso.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.openecomp.mso.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.openecomp.mso.db.request.data.repository.WatchdogServiceModVerIdLookupRepository;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ToscaResourceInstaller {

	private static final String ALLOTTED_RESOURCE = "Allotted Resource";

	private static final String MULTI_STAGE_DESIGN = "multi_stage_design";

	private static final String SCALABLE = "scalable";

	private static final String BASIC = "BASIC";

	private static final String PROVIDER = "PROVIDER";

	private static final String HEAT = "HEAT";

	private static final String MANUAL_RECORD = "MANUAL_RECORD";

	private static final String MSO = "SO";

	public static final String IP_FLEXIBLE_REACH = "IP-FLEXIBLE-REACH";
	public static final String IP_TOLL_FREE = "IP-TOLL-FREE";
	public static final String COLLABORATE = "COLLABORATE";
	public static final String SERVICE_TYPE_BONDING = "BONDING";

	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private InstanceGroupRepository instanceGroupRepo;
	
	@Autowired
	private ServiceProxyResourceCustomizationRepository serviceProxyCustomizationRepo;
	
	@Autowired
	private CollectionResourceRepository collectionRepo;
	
	@Autowired
	private CollectionResourceCustomizationRepository collectionCustomizationRepo;
	
	@Autowired
	private ConfigurationResourceCustomizationRepository configCustomizationRepo;

	@Autowired
	private VnfResourceRepository vnfRepo;

	@Autowired
	private VnfCustomizationRepository vnfCustomizationRepo;
	
	@Autowired
	private VFModuleRepository vfModuleRepo;

	@Autowired
	private VFModuleCustomizationRepository vfModuleCustomizationRepo;	
	
	@Autowired
	private VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo;	

	@Autowired
	private AllottedResourceRepository allottedRepo;

	@Autowired
	private AllottedResourceCustomizationRepository allottedCustomizationRepo;

	@Autowired
	private NetworkResourceRepository networkRepo;
	 
	@Autowired
	private HeatTemplateRepository heatRepo;

	@Autowired
	private NetworkResourceCustomizationRepository networkCustomizationRepo;

	@Autowired
	private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
	@Autowired
	private WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;
	@Autowired
	private WatchdogServiceModVerIdLookupRepository watchdogModVerIdLookupRepository;	
	
	@Autowired
	private TempNetworkHeatTemplateRepository tempNetworkLookupRepo;
	
	@Autowired
	private ExternalServiceToInternalServiceRepository externalServiceToInternalServiceRepository;
	
	protected static final MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,ToscaResourceInstaller.class);

	public boolean isResourceAlreadyDeployed(VfResourceStructure vfResourceStruct) throws ArtifactInstallerException {
		boolean status = false;
		VfResourceStructure vfResourceStructure = vfResourceStruct;
		try {
		    status = vfResourceStructure.isDeployedSuccessfully();
		} catch (RuntimeException e) {
		    status = false;
		}
		try {					
			Service existingService = serviceRepo.findOneByModelUUID(vfResourceStructure.getNotification().getServiceUUID()); 
			if(existingService != null)
				status = true;
			if (status) {
				logger.info(vfResourceStructure.getResourceInstance().getResourceInstanceName(),
						vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
						vfResourceStructure.getNotification().getServiceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()),
						vfResourceStructure.getNotification().getServiceUUID(),
						vfResourceStructure.getResourceInstance().getResourceName(), "", "");
				WatchdogComponentDistributionStatus wdStatus = new WatchdogComponentDistributionStatus(vfResourceStruct.getNotification().getDistributionID(), MSO);
				wdStatus.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
				watchdogCDStatusRepository.saveAndFlush(wdStatus);
			} else {			
				logger.info(vfResourceStructure.getResourceInstance().getResourceInstanceName(),
						vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
						vfResourceStructure.getNotification().getServiceName(),
						BigDecimalVersion.castAndCheckNotificationVersionToString(
								vfResourceStructure.getNotification().getServiceVersion()),
						vfResourceStructure.getNotification().getServiceUUID(),
						vfResourceStructure.getResourceInstance().getResourceName(), "", "");
			}
			return status;
		} catch (Exception e) {
			logger.error(MessageEnum.ASDC_ARTIFACT_CHECK_EXC, "", "", MsoLogger.ErrorCode.SchemaError,
					"Exception - isResourceAlreadyDeployed");
			throw new ArtifactInstallerException("Exception caught during checking existence of the VNF Resource.", e);
		}
	}

	public void installTheComponentStatus(IStatusData iStatus) throws ArtifactInstallerException {
		logger.debug("Entering installTheComponentStatus for distributionId " + iStatus.getDistributionID()
				+ " and ComponentName " + iStatus.getComponentName());

		try {
			WatchdogComponentDistributionStatus cdStatus = new WatchdogComponentDistributionStatus(iStatus.getDistributionID(),
					iStatus.getComponentName());
			cdStatus.setComponentDistributionStatus(iStatus.getStatus().toString());
			watchdogCDStatusRepository.save(cdStatus);

		} catch (Exception e) {
			logger.debug("Exception caught in installTheComponentStatus " + e.getMessage());
			throw new ArtifactInstallerException("Exception caught in installTheComponentStatus " + e.getMessage());
		}
	}

	@Transactional(rollbackFor = { ArtifactInstallerException.class })
	public void installTheResource(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct)
			throws ArtifactInstallerException {		
		VfResourceStructure vfResourceStructure = vfResourceStruct;
		extractHeatInformation(toscaResourceStruct, vfResourceStructure);	

		// PCLO: in case of deployment failure, use a string that will represent
		// the type of artifact that failed...
		List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();
		try {
			createToscaCsar(toscaResourceStruct);			
			createService(toscaResourceStruct, vfResourceStruct);			
			Service service = toscaResourceStruct.getCatalogService();				
			List<NodeTemplate> vfNodeTemplatesList = toscaResourceStruct.getSdcCsarHelper().getServiceVfList();
			
			processAvpnService(service);
			for (NodeTemplate nodeTemplate : vfNodeTemplatesList) {	
				
				Metadata metadata = nodeTemplate.getMetaData();		
				String serviceType = toscaResourceStruct.getCatalogService().getServiceType();
				processFlexware(toscaResourceStruct, service, nodeTemplate, serviceType);
				String vfCustomizationCategory = toscaResourceStruct.getSdcCsarHelper()
						.getMetadataPropertyValue(metadata, SdcPropertyNames.PROPERTY_NAME_CATEGORY);
				processVfModules(toscaResourceStruct, vfResourceStructure, service, nodeTemplate, metadata,
						vfCustomizationCategory);
			}

			List<NodeTemplate> allottedResourceList = toscaResourceStruct.getSdcCsarHelper().getAllottedResources();
			processAllottedResources(toscaResourceStruct, service, allottedResourceList);
			processNetworks(toscaResourceStruct, service);	
			// process Network Collections
			processNetworkCollections(toscaResourceStruct, service);
			// Process Service Proxy & Configuration
			processServiceProxyAndConfiguration(toscaResourceStruct, service);
			
			serviceRepo.save(service);
			
			WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(vfResourceStruct.getNotification().getDistributionID(), MSO);
			status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
			watchdogCDStatusRepository.save(status);

			toscaResourceStruct.setSuccessfulDeployment();

		} catch (Exception e) {
			logger.debug("Exception :", e);
			WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(vfResourceStruct.getNotification().getDistributionID(), MSO);
			status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_ERROR.name());
			watchdogCDStatusRepository.save(status);
			Throwable dbExceptionToCapture = e;
			while (!(dbExceptionToCapture instanceof ConstraintViolationException
					|| dbExceptionToCapture instanceof LockAcquisitionException)
					&& (dbExceptionToCapture.getCause() != null)) {
				dbExceptionToCapture = dbExceptionToCapture.getCause();
			}

			if (dbExceptionToCapture instanceof ConstraintViolationException
					|| dbExceptionToCapture instanceof LockAcquisitionException) {
				logger.warn(MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED,
						vfResourceStructure.getResourceInstance().getResourceName(),
						vfResourceStructure.getNotification().getServiceVersion(), "", MsoLogger.ErrorCode.DataError, "Exception - ASCDC Artifact already deployed", e);
			} else {
				String elementToLog = (!artifactListForLogging.isEmpty()
						? artifactListForLogging.get(artifactListForLogging.size() - 1).toString()
						: "No element listed");
				logger.error(MessageEnum.ASDC_ARTIFACT_INSTALL_EXC, elementToLog, "", "", MsoLogger.ErrorCode.DataError,
						"Exception caught during installation of "
								+ vfResourceStructure.getResourceInstance().getResourceName()
								+ ". Transaction rollback",
						e);
				throw new ArtifactInstallerException("Exception caught during installation of "
						+ vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.", e);
			}
		}
	}

	private void processNetworks(ToscaResourceStructure toscaResourceStruct, Service service)
			throws ArtifactInstallerException {
		List<NodeTemplate> nodeTemplatesVLList = toscaResourceStruct.getSdcCsarHelper().getServiceVlList();

		if (nodeTemplatesVLList != null) {
			for (NodeTemplate vlNode : nodeTemplatesVLList) {
				String networkResourceModelName = vlNode.getMetaData()
						.getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
				
				TempNetworkHeatTemplateLookup tempNetworkLookUp = tempNetworkLookupRepo.findFirstBynetworkResourceModelName(networkResourceModelName);
				
				if (tempNetworkLookUp != null ) {					
						HeatTemplate heatTemplate =  heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
						if (heatTemplate != null ) {
						NetworkResourceCustomization networkCustomization = createNetwork(vlNode, toscaResourceStruct, heatTemplate,tempNetworkLookUp.getAicVersionMax(),
							tempNetworkLookUp.getAicVersionMin(),service);
						service.getNetworkCustomizations()
						.add(networkCustomization);
					}
					else{
						throw new ArtifactInstallerException(					
							"No HeatTemplate found for artifactUUID: "
									+ tempNetworkLookUp.getHeatTemplateArtifactUuid());
					}
				} else {
					throw new ArtifactInstallerException(
							"No NetworkResourceName found in TempNetworkHeatTemplateLookup for "
									+ networkResourceModelName);
				}					
				
			}
		}
	}

	private void processAllottedResources(ToscaResourceStructure toscaResourceStruct, Service service,
			List<NodeTemplate> allottedResourceList) {
		if (allottedResourceList != null) {
			for (NodeTemplate allottedNode : allottedResourceList) {									
				service.getAllottedCustomizations()
						.add(createAllottedResource(allottedNode, toscaResourceStruct, service));				
			}
		}
	}
	
	private void processServiceProxyAndConfiguration(ToscaResourceStructure toscaResourceStruct, Service service) {
		
		List<NodeTemplate> serviceProxyResourceList = toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.SERVICE_PROXY);
		
		List<NodeTemplate> configurationNodeTemplatesList = toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.CONFIGURATION);
		
		if (serviceProxyResourceList != null) {
			for (NodeTemplate spNode : serviceProxyResourceList) {
				createServiceProxy(spNode, service, toscaResourceStruct);
				serviceProxyCustomizationRepo.saveAndFlush(toscaResourceStruct.getCatalogServiceProxyResourceCustomization());	

				for (NodeTemplate configNode : configurationNodeTemplatesList) {
					
						List<RequirementAssignment> requirementsList = toscaResourceStruct.getSdcCsarHelper().getRequirementsOf(configNode).getAll();
						for (RequirementAssignment requirement :  requirementsList) {
							if (requirement.getNodeTemplateName().equals(spNode.getName())) {
								createConfiguration(configNode, toscaResourceStruct, toscaResourceStruct.getCatalogServiceProxyResourceCustomization());
								configCustomizationRepo.saveAndFlush(toscaResourceStruct.getCatalogConfigurationResourceCustomization());	
								break;
							}
						}
				}
	
			}
		}
	}
	
	private void processNetworkCollections(ToscaResourceStructure toscaResourceStruct, Service service) {
		
		List<NodeTemplate> networkCollectionList = toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.CR);
		
		if (networkCollectionList != null) {
			for (NodeTemplate crNode : networkCollectionList) {	
				
				createNetworkCollection(crNode, toscaResourceStruct, service);
				collectionRepo.saveAndFlush(toscaResourceStruct.getCatalogCollectionResource());
				
				List<NetworkInstanceGroup> networkInstanceGroupList = toscaResourceStruct.getCatalogNetworkInstanceGroup();
				for(NetworkInstanceGroup networkInstanceGroup : networkInstanceGroupList){
					instanceGroupRepo.saveAndFlush(networkInstanceGroup);
				}
	
			}
		}
	}

	private void processVfModules(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStructure,
			Service service, NodeTemplate nodeTemplate, Metadata metadata, String vfCustomizationCategory) throws Exception {
		if (!vfCustomizationCategory.equalsIgnoreCase(ALLOTTED_RESOURCE)) 
		{

			String vfCustomizationUUID = toscaResourceStruct.getSdcCsarHelper()
					.getMetadataPropertyValue(metadata, SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
			logger.debug("vfCustomizationUUID=" + vfCustomizationUUID);					
			
			VnfResourceCustomization vnfResource = createVnfResource(nodeTemplate, toscaResourceStruct, service);	
			
			for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {			
				logger.debug("vfModuleStructure:" + vfModuleStructure.toString());
				List<org.onap.sdc.toscaparser.api.Group> vfGroups = toscaResourceStruct
						.getSdcCsarHelper().getVfModulesByVf(vfCustomizationUUID);
				IVfModuleData vfMetadata = vfModuleStructure.getVfModuleMetadata();		
				Optional<org.onap.sdc.toscaparser.api.Group> matchingObject = vfGroups.stream().
					    filter(group -> group.getMetadata().getValue("vfModuleModelCustomizationUUID").equals(vfMetadata.getVfModuleModelCustomizationUUID())).
					    findFirst();
				if(matchingObject.isPresent()){
					VfModuleCustomization vfModuleCustomization = createVFModuleResource(matchingObject.get(), nodeTemplate, toscaResourceStruct, vfResourceStructure,vfMetadata, vnfResource);
					vfModuleCustomization.getVfModule().setVnfResources(vnfResource.getVnfResources());
				}else
					throw (new Exception("Cannot find matching VFModule Customization"));
				
			}
			service.getVnfCustomizations().add(vnfResource);
		}
	}

	private void processFlexware(ToscaResourceStructure toscaResourceStruct, Service service, NodeTemplate nodeTemplate,
			String serviceType) {
		if (serviceType != null && serviceType.equalsIgnoreCase("Flexware")) {

			createVnfResource(nodeTemplate, toscaResourceStruct, service);
			String modelName = toscaResourceStruct.getVnfResourceCustomization().getVnfResources().getModelName();
			
			String modelVersion = BigDecimalVersion.castAndCheckNotificationVersionToString(
					toscaResourceStruct.getCatalogVnfResourceCustomization().getVnfResources().getModelVersion());
			// check for duplicate record already in the database
			VnfResource vnfResource = vnfRepo.findByModelNameAndModelVersion(modelName, modelVersion);

			if (vnfResource != null) {
				toscaResourceStruct.setVnfAlreadyInstalled(true);
			}

			vnfCustomizationRepo.saveAndFlush(toscaResourceStruct.getCatalogVnfResourceCustomization());					
		}
	}

	private void processAvpnService(Service service) {
		String serviceType = service.getServiceType();
		String serviceRole = service.getServiceRole();
		String serviceName = "";
		if (serviceType != null && serviceType.equalsIgnoreCase(SERVICE_TYPE_BONDING)) {
			if(serviceRole.equalsIgnoreCase(COLLABORATE)){
				serviceName = AVPNServiceNames.AVPN_BONDING_TO_COLLABORATE.getServiceName();
			}else if(serviceRole.equalsIgnoreCase(IP_FLEXIBLE_REACH)){
				serviceName = AVPNServiceNames.AVPN_BONDING_TO_IP_FLEX_REACH.getServiceName();
			}else if(serviceRole.equalsIgnoreCase(IP_TOLL_FREE)){
				serviceName = AVPNServiceNames.AVPN_BONDING_TO_IP_TOLL_FREE.getServiceName();
			}else {
				return;
			}
		
			ExternalServiceToInternalService serviceToInternalService;
		
			// check for duplicate record already in the database
			serviceToInternalService = externalServiceToInternalServiceRepository.findByServiceNameAndSubscriptionServiceType(serviceName, serviceRole);
			
			if(serviceToInternalService != null){
				serviceToInternalService.setServiceModelUUID(service.getModelUUID());
			}else{
				serviceToInternalService = new ExternalServiceToInternalService();
				serviceToInternalService.setServiceName(serviceName);
				serviceToInternalService.setSubscriptionServiceType(serviceRole);
				serviceToInternalService.setServiceModelUUID(service.getModelUUID());	
			}
			externalServiceToInternalServiceRepository.save(serviceToInternalService);
		}
	}

	public void processWatchdog(String distributionId, String servideUUID) {
		WatchdogServiceModVerIdLookup modVerIdLookup = new WatchdogServiceModVerIdLookup(distributionId,servideUUID);
		watchdogModVerIdLookupRepository.saveAndFlush(modVerIdLookup);
		
		WatchdogDistributionStatus distributionStatus = new WatchdogDistributionStatus(distributionId);
		watchdogDistributionStatusRepository.saveAndFlush(distributionStatus);
	}

	private void extractHeatInformation(ToscaResourceStructure toscaResourceStruct,
			VfResourceStructure vfResourceStructure) {
		for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

			switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
			case ASDCConfiguration.HEAT:
			case ASDCConfiguration.HEAT_NESTED:
				createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct,
						vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_VOL:
				createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct,
						vfModuleArtifact);
				VfModuleArtifact envModuleArtifact = getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
				createHeatEnvFromArtifact(vfResourceStructure, envModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ENV:
				createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
				break;
			case ASDCConfiguration.HEAT_ARTIFACT:
				createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact,
						toscaResourceStruct);
				break;
			case ASDCConfiguration.HEAT_NET:
			case ASDCConfiguration.OTHER:
				logger.warn(MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT,
						vfModuleArtifact.getArtifactInfo().getArtifactType() + "(Artifact Name:"
								+ vfModuleArtifact.getArtifactInfo().getArtifactName() + ")",
						"", "", MsoLogger.ErrorCode.DataError, "Artifact type not supported");
				break;
			default:
				break;

			}
		}
	}

	protected VfModuleArtifact getHeatEnvArtifactFromGeneratedArtifact(VfResourceStructure vfResourceStructure,
			VfModuleArtifact vfModuleArtifact) {
		String artifactName = vfModuleArtifact.getArtifactInfo().getArtifactName();
		artifactName = artifactName.substring(0, artifactName.indexOf('.'));
		for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {
			if (moduleArtifact.getArtifactInfo().getArtifactName().contains(artifactName)
					&& moduleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
				return moduleArtifact;
			}
		}
		return null;
	}

	public String verifyTheFilePrefixInArtifacts(String filebody, VfResourceStructure vfResourceStructure,
			List<String> listTypes) {
		String newFileBody = filebody;
		for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

			if (listTypes.contains(moduleArtifact.getArtifactInfo().getArtifactType())) {

				newFileBody = verifyTheFilePrefixInString(newFileBody,
						moduleArtifact.getArtifactInfo().getArtifactName());
			}
		}
		return newFileBody;
	}

	public String verifyTheFilePrefixInString(final String body, final String filenameToVerify) {

		String needlePrefix = "file:///";
		String prefixedFilenameToVerify = needlePrefix + filenameToVerify;

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
			// We append from the startIndex up to currentIndex (start of File
			// Name)
			sb.append(body.substring(startIndex, currentIndex));
			sb.append(filenameToVerify);

			currentIndex += prefixedFilenameToVerify.length();
		}

		sb.append(body.substring(startIndex));

		return sb.toString();
	}

	private void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,
			ToscaResourceStructure toscaResourceStruct, VfModuleArtifact vfModuleArtifact) {
		HeatTemplate heatTemplate = new HeatTemplate();
		List<String> typeList = new ArrayList<>();
		typeList.add(ASDCConfiguration.HEAT_NESTED);
		typeList.add(ASDCConfiguration.HEAT_ARTIFACT);

		heatTemplate.setTemplateBody(
				verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
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

		if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
			heatTemplate.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatTemplate.setArtifactChecksum(MANUAL_RECORD);
		}

		Set<HeatTemplateParam> heatParam = extractHeatTemplateParameters(
				vfModuleArtifact.getResult(), vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatTemplate.setParameters(heatParam);	
		vfModuleArtifact.setHeatTemplate(heatTemplate);
	}

	private void createHeatEnvFromArtifact(VfResourceStructure vfResourceStructure,
			VfModuleArtifact vfModuleArtifact) {
		HeatEnvironment heatEnvironment = new HeatEnvironment();
		heatEnvironment.setName(vfModuleArtifact.getArtifactInfo().getArtifactName());
		List<String> typeList = new ArrayList<>();
		typeList.add(ASDCConfiguration.HEAT);
		typeList.add(ASDCConfiguration.HEAT_VOL);
		heatEnvironment.setEnvironment(
				verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
		heatEnvironment.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatEnvironment.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));	
		heatEnvironment.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

		if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
			heatEnvironment.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatEnvironment.setArtifactChecksum(MANUAL_RECORD);
		}		
		vfModuleArtifact.setHeatEnvironment(heatEnvironment);
	}

	private void createHeatFileFromArtifact(VfResourceStructure vfResourceStructure,
		VfModuleArtifact vfModuleArtifact, ToscaResourceStructure toscaResourceStruct) {
		
		HeatFiles heatFile = new HeatFiles();	
		heatFile.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		heatFile.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
		heatFile.setFileBody(vfModuleArtifact.getResult());
		heatFile.setFileName(vfModuleArtifact.getArtifactInfo().getArtifactName());
		heatFile.setVersion(BigDecimalVersion
				.castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
		toscaResourceStruct.setHeatFilesUUID(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
		if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
			heatFile.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
		} else {
			heatFile.setArtifactChecksum(MANUAL_RECORD);
		}
		vfModuleArtifact.setHeatFiles(heatFile);
	}

	private Service createService(ToscaResourceStructure toscaResourceStructure,
			VfResourceStructure vfResourceStructure) {

		toscaResourceStructure.getServiceMetadata();

		Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();

		Service service = new Service();

		if (serviceMetadata != null) {

			if (toscaResourceStructure.getServiceVersion() != null) {
				service.setModelVersion(toscaResourceStructure.getServiceVersion());
			}

			service.setServiceType(serviceMetadata.getValue("serviceType"));
			service.setServiceRole(serviceMetadata.getValue("serviceRole"));

			service.setDescription(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
			service.setModelName(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			service.setModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			service.setEnvironmentContext(serviceMetadata.getValue("environmentContext"));

			if (vfResourceStructure != null) 
				service.setWorkloadContext(vfResourceStructure.getNotification().getWorkloadContext());
						
			service.setModelInvariantUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
			service.setCsar(toscaResourceStructure.getCatalogToscaCsar());			
		}
		
		
		toscaResourceStructure.setCatalogService(service); 
		return service;
	}
	
	private ServiceProxyResourceCustomization createServiceProxy(NodeTemplate nodeTemplate, Service service, ToscaResourceStructure toscaResourceStructure) {

		Metadata spMetadata = nodeTemplate.getMetaData();
		
		ServiceProxyResource spResource = new ServiceProxyResource();
		
		spResource.setModelName(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
		spResource.setModelInvariantUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
		spResource.setModelUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
		spResource.setModelVersion(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
		spResource.setDescription(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));	
		
		ServiceProxyResourceCustomization spCustomizationResource = new ServiceProxyResourceCustomization();
		
		Set<ServiceProxyResourceCustomization> serviceProxyCustomizationSet = new HashSet<>();
		
		spCustomizationResource.setModelCustomizationUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
		spCustomizationResource.setModelInstanceName(nodeTemplate.getName());
		spCustomizationResource.setToscaNodeType(nodeTemplate.getType());
		spCustomizationResource.setSourceService(service);
		spCustomizationResource.setServiceProxyResource(spResource);
		spCustomizationResource.setToscaNodeType(nodeTemplate.getType());
		spCustomizationResource.setServiceProxyResource(spResource);
		serviceProxyCustomizationSet.add(spCustomizationResource);

		
		spResource.setServiceProxyCustomization(serviceProxyCustomizationSet); 		
		
		toscaResourceStructure.setCatalogServiceProxyResource(spResource);
		
		toscaResourceStructure.setCatalogServiceProxyResourceCustomization(spCustomizationResource);
		
		return spCustomizationResource;
	}
	
	private ConfigurationResourceCustomization createConfiguration(NodeTemplate nodeTemplate, ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization) {

		Metadata metadata = nodeTemplate.getMetaData();
		
		ConfigurationResource configResource = new ConfigurationResource();
		
		configResource.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
		configResource.setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
		configResource.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
		configResource.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
		configResource.setDescription(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
		configResource.setToscaNodeType(nodeTemplate.getType());
		
		ConfigurationResourceCustomization configCustomizationResource = new ConfigurationResourceCustomization();
		
		Set<ConfigurationResourceCustomization> configResourceCustomizationSet = new HashSet<>();
		
		configCustomizationResource.setModelCustomizationUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
		configCustomizationResource.setModelInstanceName(nodeTemplate.getName());
		
		configCustomizationResource.setNfFunction(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
		configCustomizationResource.setNfRole(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE));
		configCustomizationResource.setNfType(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE));
		configCustomizationResource.setServiceProxyResourceCustomization(spResourceCustomization);
		configCustomizationResource.setConfigResourceCustomization(configCustomizationResource);
		configCustomizationResource.setConfigurationResource(configResource);
		configResourceCustomizationSet.add(configCustomizationResource);

		configResource.setConfigurationResourceCustomization(configResourceCustomizationSet); 	
		
		toscaResourceStructure.setCatalogConfigurationResource(configResource);
		
		toscaResourceStructure.setCatalogConfigurationResourceCustomization(configCustomizationResource);
		
		return configCustomizationResource;
	}


	private void createToscaCsar(ToscaResourceStructure toscaResourceStructure) {
		ToscaCsar toscaCsar = new ToscaCsar();
		if (toscaResourceStructure.getToscaArtifact().getArtifactChecksum() != null) {
			toscaCsar.setArtifactChecksum(toscaResourceStructure.getToscaArtifact().getArtifactChecksum());
		} else {
			toscaCsar.setArtifactChecksum(MANUAL_RECORD);
		}
		toscaCsar.setArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
		toscaCsar.setName(toscaResourceStructure.getToscaArtifact().getArtifactName());
		toscaCsar.setVersion(toscaResourceStructure.getToscaArtifact().getArtifactVersion());
		toscaCsar.setDescription(toscaResourceStructure.getToscaArtifact().getArtifactDescription());
		toscaCsar.setUrl(toscaResourceStructure.getToscaArtifact().getArtifactURL());

		toscaResourceStructure.setCatalogToscaCsar(toscaCsar);
	}

	private  NetworkResourceCustomization createNetwork(NodeTemplate networkNodeTemplate,
			ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin,Service service) {
		
		NetworkResourceCustomization networkResourceCustomization=networkCustomizationRepo.findOneByModelCustomizationUUID(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
				if(networkResourceCustomization==null){
			networkResourceCustomization = createNetworkResourceCustomization(networkNodeTemplate,
					toscaResourceStructure);
					
			NetworkResource networkResource = findExistingNetworkResource(service,
					networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
					if(networkResource == null)
				networkResource = createNetworkResource(networkNodeTemplate, toscaResourceStructure, heatTemplate,
						aicMax, aicMin);

					networkResource.addNetworkResourceCustomization(networkResourceCustomization);		
					networkResourceCustomization.setNetworkResource(networkResource);
				}
		return networkResourceCustomization;
	}
	
	private  NetworkResource findExistingNetworkResource(Service service, String modelUUID) {
		NetworkResource networkResource = null;
		for(NetworkResourceCustomization networkCustom : service.getNetworkCustomizations()){
			if (networkCustom.getNetworkResource() != null
					&& networkCustom.getNetworkResource().getModelUUID().equals(modelUUID)) {
					networkResource = networkCustom.getNetworkResource();
			}
		}
		if(networkResource==null)
			networkResource = networkRepo.findResourceByModelUUID(modelUUID);
		
		return networkResource;
	}
	
	private NetworkResourceCustomization createNetworkResourceCustomization(NodeTemplate networkNodeTemplate,
			ToscaResourceStructure toscaResourceStructure) {
		NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
		networkResourceCustomization.setModelInstanceName(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
		networkResourceCustomization.setModelCustomizationUUID(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));		

		networkResourceCustomization.setNetworkTechnology(
				testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
						SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY)));
		networkResourceCustomization.setNetworkType(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE)));
		networkResourceCustomization.setNetworkRole(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE)));
		networkResourceCustomization.setNetworkScope(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE)));
		return networkResourceCustomization;
	}

	private NetworkResource createNetworkResource(NodeTemplate networkNodeTemplate,
			ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin) {
		NetworkResource networkResource = new NetworkResource();
		String providerNetwork = toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(
				networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

		if ("true".equalsIgnoreCase(providerNetwork)) {
			networkResource.setNeutronNetworkType(PROVIDER);
		} else {
			networkResource.setNeutronNetworkType(BASIC);
		}

		networkResource.setModelName(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));

		networkResource.setModelInvariantUUID(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
		networkResource.setModelUUID(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
		networkResource.setModelVersion(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));

		networkResource.setAicVersionMax(aicMax);		
		networkResource.setAicVersionMin(aicMin);
		networkResource.setToscaNodeType(networkNodeTemplate.getType());
		networkResource.setDescription(
				testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
		networkResource.setOrchestrationMode(HEAT);	
		networkResource.setHeatTemplate(heatTemplate); 
		return networkResource;
	}
	
	private  CollectionNetworkResourceCustomization createNetworkCollection(NodeTemplate networkNodeTemplate,
			ToscaResourceStructure toscaResourceStructure, Service service) {

		CollectionNetworkResourceCustomization collectionNetworkResourceCustomization = new CollectionNetworkResourceCustomization();

		// CollectionResource existingCollectionResource =
		// collectionRepo.findResourceByModelUUID(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
		
		
		//**** Build Object to populate Collection_Resource table
		CollectionResource collectionResource = new CollectionResource();

		//if(existingCollectionResource == null){
		collectionResource
				.setModelName(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
		collectionResource.setModelInvariantUUID(
				networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
		collectionResource
				.setModelUUID(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
		collectionResource
				.setModelVersion(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
		collectionResource
				.setDescription(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
		collectionResource.setToscaNodeType(networkNodeTemplate.getType());

		toscaResourceStructure.setCatalogCollectionResource(collectionResource);
		
	//	}
		
		//**** Build object to populate Collection_Resource_Customization table
		NetworkCollectionResourceCustomization ncfc = new NetworkCollectionResourceCustomization();
		
		ncfc.setFunction(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
				"cr_function"));
		ncfc.setRole(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
				"cr_role"));
		ncfc.setType(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
				"cr_type"));

		ncfc.setModelInstanceName(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
		ncfc.setModelCustomizationUUID(
				networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
		
		Set<CollectionNetworkResourceCustomization> networkResourceCustomizationSet = new HashSet<>();
		networkResourceCustomizationSet.add(collectionNetworkResourceCustomization);

		ncfc.setNetworkResourceCustomization(networkResourceCustomizationSet);

		//if(existingCollectionResource != null){
		//	ncfc.setCollectionResource(existingCollectionResource);
		//} else {
			ncfc.setCollectionResource(collectionResource);
		//}
		
		//*** Build object to populate the Instance_Group table
		List<Group> groupList = toscaResourceStructure.getSdcCsarHelper()
				.getGroupsOfOriginOfNodeTemplateByToscaGroupType(networkNodeTemplate,
						"org.openecomp.groups.NetworkCollection");
		
		List<NetworkInstanceGroup> networkInstanceGroupList = new ArrayList<>();

		List<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationList = new ArrayList<CollectionResourceInstanceGroupCustomization>();

		for (Group group : groupList) { 
			
			NetworkInstanceGroup networkInstanceGroup = new NetworkInstanceGroup();
			Metadata instanceMetadata = group.getMetadata();
			networkInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			networkInstanceGroup
					.setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
			networkInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			networkInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
			networkInstanceGroup.setToscaNodeType(networkNodeTemplate.getType());
			networkInstanceGroup.setRole(SubType.SUB_INTERFACE.toString()); // Set
																			// Role
			networkInstanceGroup.setType(InstanceGroupType.L3_NETWORK); // Set
																		// type
			networkInstanceGroup.setCollectionResource(collectionResource);
		
			// ****Build object to populate
			// Collection_Resource_Instance_Group_Customization table
			CollectionResourceInstanceGroupCustomization crInstanceGroupCustomization = new CollectionResourceInstanceGroupCustomization();
			crInstanceGroupCustomization.setInstanceGroup(networkInstanceGroup);
			crInstanceGroupCustomization.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			crInstanceGroupCustomization.setModelCustomizationUUID(
					networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
		
			if (toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
					instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
							+ "_subinterface_network_quantity") != null) {
				crInstanceGroupCustomization.setSubInterfaceNetworkQuantity(
						Integer.getInteger(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(
								networkNodeTemplate, instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
										+ "_subinterface_network_quantity")));
			}
		
			crInstanceGroupCustomization.setDescription(
					toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
							instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
									+ "_network_collection_description"));
			crInstanceGroupCustomization.setFunction(
					toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
							instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
									+ "_network_collection_function"));
			crInstanceGroupCustomization.setCollectionResourceCust(ncfc);
			collectionResourceInstanceGroupCustomizationList.add(crInstanceGroupCustomization);

			networkInstanceGroup
					.setCollectionInstanceGroupCustomizations(collectionResourceInstanceGroupCustomizationList);

			networkInstanceGroupList.add(networkInstanceGroup);
		}

		toscaResourceStructure.setCatalogNetworkInstanceGroup(networkInstanceGroupList);

		List<NodeTemplate> vlNodeList = toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplateBySdcType(networkNodeTemplate, SdcTypes.VL);
		
		List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizationList = new ArrayList<>();
		
		//*****Build object to populate the NetworkResource table
		NetworkResource networkResource = new NetworkResource();
		
		for(NodeTemplate vlNodeTemplate : vlNodeList){

			String providerNetwork = toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(
					vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

			if ("true".equalsIgnoreCase(providerNetwork)) {
				networkResource.setNeutronNetworkType(PROVIDER);
			} else {
				networkResource.setNeutronNetworkType(BASIC);
			}

			networkResource.setModelName(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

			networkResource.setModelInvariantUUID(
					vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
			networkResource.setModelUUID(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			networkResource
					.setModelVersion(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));

			networkResource.setAicVersionMax(
					vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));
			
			TempNetworkHeatTemplateLookup tempNetworkLookUp = tempNetworkLookupRepo.findFirstBynetworkResourceModelName(
					vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			
			if (tempNetworkLookUp != null ) {	
					
				HeatTemplate heatTemplate = heatRepo
						.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
					networkResource.setHeatTemplate(heatTemplate);
					
					networkResource.setAicVersionMin(tempNetworkLookUp.getAicVersionMin());
					
			}

			networkResource.setToscaNodeType(vlNodeTemplate.getType());
			networkResource
					.setDescription(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
			networkResource.setOrchestrationMode(HEAT);
			
			// Build object to populate the
			// Collection_Network_Resource_Customization table
			collectionNetworkResourceCustomization
					.setModelInstanceName(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			collectionNetworkResourceCustomization.setModelCustomizationUUID(
					vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

			collectionNetworkResourceCustomization.setNetworkTechnology(
					toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vlNodeTemplate,
							SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
			collectionNetworkResourceCustomization.setNetworkType(toscaResourceStructure.getSdcCsarHelper()
					.getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
			collectionNetworkResourceCustomization.setNetworkRole(toscaResourceStructure.getSdcCsarHelper()
					.getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
			collectionNetworkResourceCustomization.setNetworkScope(toscaResourceStructure.getSdcCsarHelper()
					.getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
			collectionNetworkResourceCustomization.setNetworkResource(networkResource);
			collectionNetworkResourceCustomization.setNetworkResourceCustomization(ncfc);
			
			collectionNetworkResourceCustomizationList.add(collectionNetworkResourceCustomization);

		}
		
		return collectionNetworkResourceCustomization;
	}
	
	protected VnfcInstanceGroupCustomization createVNFCInstanceGroup(NodeTemplate vnfcNodeTemplate, Group group,
			VnfResourceCustomization vnfResourceCustomization) {

			Metadata instanceMetadata = group.getMetadata();
			// Populate InstanceGroup
			VFCInstanceGroup vfcInstanceGroup = new VFCInstanceGroup();
			
			vfcInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
			vfcInstanceGroup.setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
			vfcInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			vfcInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
			vfcInstanceGroup.setToscaNodeType(vnfcNodeTemplate.getType());
			vfcInstanceGroup.setRole("SUB-INTERFACE");   // Set Role
			vfcInstanceGroup.setType(InstanceGroupType.VNFC);  // Set type	
			
			//Populate VNFCInstanceGroupCustomization
			VnfcInstanceGroupCustomization vfcInstanceGroupCustom = new VnfcInstanceGroupCustomization();
			
			vfcInstanceGroupCustom.setModelCustomizationUUID(vnfResourceCustomization.getModelCustomizationUUID());
			vfcInstanceGroupCustom.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			vfcInstanceGroupCustom.setDescription(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
			vfcInstanceGroupCustom.setFunction("FUNCTION");
			vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);
			vfcInstanceGroupCustom.setVnfResourceCust(vnfResourceCustomization);		
			
		return vfcInstanceGroupCustom;

	}
	
	private VFCInstanceGroup findExistingVnfcInstanceGroup(VnfResourceCustomization vnfResourceCustomization,
			String modelUUID) {
		VFCInstanceGroup vfcInstanceGroup = null;
		List<VnfcInstanceGroupCustomization> vnfInstanceGroupCustomizations = vnfResourceCustomization
				.getVnfcInstanceGroupCustomizations();
		if(vnfInstanceGroupCustomizations != null){
			for (VnfcInstanceGroupCustomization vnfcInstanceGroupCustom : vnfResourceCustomization
					.getVnfcInstanceGroupCustomizations()) {
				if (vnfcInstanceGroupCustom.getInstanceGroup() != null
						&& vnfcInstanceGroupCustom.getInstanceGroup().getModelUUID().equals(modelUUID)) {
					vfcInstanceGroup = (VFCInstanceGroup)vnfcInstanceGroupCustom.getInstanceGroup();
				}
			}
		}
		if(vfcInstanceGroup==null)
			vfcInstanceGroup = (VFCInstanceGroup)instanceGroupRepo.findResourceByModelUUID(modelUUID);
		
		return vfcInstanceGroup;
	}
	
	protected VfModuleCustomization createVFModuleResource(Group group, NodeTemplate nodeTemplate,
			ToscaResourceStructure toscaResourceStructure, VfResourceStructure vfResourceStructure,
			IVfModuleData vfModuleData, VnfResourceCustomization vnfResource) {
		VfModuleCustomization vfModuleCustomization = findExistingVfModuleCustomization(vnfResource,
				vfModuleData.getVfModuleModelCustomizationUUID());
		if(vfModuleCustomization == null){		
			VfModule vfModule = findExistingVfModule(vnfResource,
					nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));
			Metadata vfMetadata = group.getMetadata();
			if(vfModule==null)
				vfModule=createVfModule(group, toscaResourceStructure, vfModuleData, vfMetadata);
			
			vfModuleCustomization = createVfModuleCustomzation(group, toscaResourceStructure, vfModule, vfModuleData);
			setHeatInformationForVfModule(toscaResourceStructure, vfResourceStructure, vfModule, vfModuleCustomization,
					vfMetadata);
			vfModuleCustomization.setVfModule(vfModule);
			vfModule.getVfModuleCustomization().add(vfModuleCustomization);
			vnfResource.getVfModuleCustomizations().add(vfModuleCustomization);
		} else{
			vfResourceStructure.setAlreadyDeployed(true);
		}
		return vfModuleCustomization;
	}
	
	private VfModuleCustomization findExistingVfModuleCustomization(VnfResourceCustomization vnfResource,
			String vfModuleModelCustomizationUUID) {
		VfModuleCustomization vfModuleCustomization = null;
		for(VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()){
			if(vfModuleCustom.getModelCustomizationUUID().equalsIgnoreCase(vfModuleModelCustomizationUUID)){
				vfModuleCustomization = vfModuleCustom;
			}
		}
		if(vfModuleCustomization==null)
			vfModuleCustomization = vfModuleCustomizationRepo
					.findByModelCustomizationUUID(vfModuleModelCustomizationUUID);
		
		return vfModuleCustomization;
	}

	private VfModule findExistingVfModule(VnfResourceCustomization vnfResource, String modelUUID) {
		VfModule vfModule = null;
		for(VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()){
			if(vfModuleCustom.getVfModule() != null && vfModuleCustom.getVfModule().getModelUUID().equals(modelUUID)){
				vfModule = vfModuleCustom.getVfModule();
			}
		}
		if(vfModule==null)
			vfModule = vfModuleRepo.findByModelUUID(modelUUID);
		
		return vfModule;
	}

	protected VfModuleCustomization createVfModuleCustomzation(Group group,
			ToscaResourceStructure toscaResourceStructure, VfModule vfModule, IVfModuleData vfModuleData) {
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		
		vfModuleCustomization.setModelCustomizationUUID(vfModuleData.getVfModuleModelCustomizationUUID());

		vfModuleCustomization.setVfModule(vfModule);

		String initialCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT);
		if (initialCount != null && initialCount.length() > 0) {
			vfModuleCustomization.setInitialCount(Integer.valueOf(initialCount));
		}

		vfModuleCustomization.setInitialCount(Integer.valueOf(toscaResourceStructure.getSdcCsarHelper()
				.getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT)));

		String availabilityZoneCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);
		if (availabilityZoneCount != null && availabilityZoneCount.length() > 0) {
			vfModuleCustomization.setAvailabilityZoneCount(Integer.valueOf(availabilityZoneCount));
		}

		vfModuleCustomization.setLabel(toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_VFMODULELABEL));

		String maxInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_MAXVFMODULEINSTANCES);
		if (maxInstances != null && maxInstances.length() > 0) {
			vfModuleCustomization.setMaxInstances(Integer.valueOf(maxInstances));
		}

		String minInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_MINVFMODULEINSTANCES);
		if (minInstances != null && minInstances.length() > 0) {
			vfModuleCustomization.setMinInstances(Integer.valueOf(minInstances));
		}
		return vfModuleCustomization;
	}

	protected VfModule createVfModule(Group group, ToscaResourceStructure toscaResourceStructure,
			IVfModuleData vfModuleData, Metadata vfMetadata) {
		VfModule vfModule = new VfModule();
		String vfModuleModelUUID = vfModuleData.getVfModuleModelUUID();

		if(vfModuleModelUUID == null) {
			vfModuleModelUUID = testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
					SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));
		} else if (vfModuleModelUUID.indexOf('.') > -1) {
			vfModuleModelUUID = vfModuleModelUUID.substring(0, vfModuleModelUUID.indexOf('.'));
		}

		vfModule.setModelInvariantUUID(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID)));
		vfModule.setModelName(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
				SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME)));
		vfModule.setModelUUID(vfModuleModelUUID);
		vfModule.setModelVersion(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
				SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION)));
		vfModule.setDescription(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
				SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));

		String vfModuleType = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
				SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE);
		if (vfModuleType != null && "Base".equalsIgnoreCase(vfModuleType)) {
			vfModule.setIsBase(true);
		} else {
			vfModule.setIsBase(false);
		}
		return vfModule;
	}

	protected void setHeatInformationForVfModule(ToscaResourceStructure toscaResourceStructure,
			VfResourceStructure vfResourceStructure, VfModule vfModule, VfModuleCustomization vfModuleCustomization,
			Metadata vfMetadata) {
		Optional<VfModuleStructure> matchingObject = vfResourceStructure.getVfModuleStructure().stream()
				.filter(vfModuleStruct -> vfModuleStruct.getVfModuleMetadata().getVfModuleModelUUID()
						.equalsIgnoreCase(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
								SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)))
				.findFirst();

		if (matchingObject.isPresent()) {
			List<HeatFiles> heatFilesList = new ArrayList<>();
			List<HeatTemplate> childNestedHeatTemplates = new ArrayList<HeatTemplate>();
			HeatTemplate parentHeatTemplate = new HeatTemplate();
			String parentArtifactType = null;
			Set<String> artifacts = new HashSet<>(matchingObject.get().getVfModuleMetadata().getArtifacts());
			for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {
				if (artifacts.contains(vfModuleArtifact.getArtifactInfo().getArtifactUUID())) {
					checkVfModuleArtifactType(vfModule, vfModuleCustomization, heatFilesList, vfModuleArtifact,
							childNestedHeatTemplates, parentHeatTemplate, vfResourceStructure);
				}
				
				if(vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)){
					parentArtifactType = identifyParentOfNestedTemplate(matchingObject.get(), vfModuleArtifact);
				}
			}
			if(!heatFilesList.isEmpty()){
				vfModule.setHeatFiles(heatFilesList);
			}
			
			if(!childNestedHeatTemplates.isEmpty()){
			
				if(parentArtifactType != null && parentArtifactType.equalsIgnoreCase(ASDCConfiguration.HEAT_VOL)){
					vfModule.getVolumeHeatTemplate().setChildTemplates(childNestedHeatTemplates);
				} else {
					vfModule.getModuleHeatTemplate().setChildTemplates(childNestedHeatTemplates);
				}  
			}
		}
	}

	protected void checkVfModuleArtifactType(VfModule vfModule, VfModuleCustomization vfModuleCustomization,
			List<HeatFiles> heatFilesList, VfModuleArtifact vfModuleArtifact, List<HeatTemplate> nestedHeatTemplates,
			HeatTemplate parentHeatTemplate, VfResourceStructure vfResourceStructure) {
		if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT)) {
			vfModuleArtifact.incrementDeployedInDB();
			vfModule.setModuleHeatTemplate(vfModuleArtifact.getHeatTemplate());
		} else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_VOL)) {
			vfModule.setVolumeHeatTemplate(vfModuleArtifact.getHeatTemplate());
			VfModuleArtifact volVfModuleArtifact = this.getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
			vfModuleCustomization.setVolumeHeatEnv(volVfModuleArtifact.getHeatEnvironment());
			vfModuleArtifact.incrementDeployedInDB();
		} else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
			vfModuleCustomization.setHeatEnvironment(vfModuleArtifact.getHeatEnvironment());
			vfModuleArtifact.incrementDeployedInDB();
		} else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ARTIFACT)) {
			heatFilesList.add(vfModuleArtifact.getHeatFiles());							
			vfModuleArtifact.incrementDeployedInDB();
		} else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
			nestedHeatTemplates.add(vfModuleArtifact.getHeatTemplate());				
			vfModuleArtifact.incrementDeployedInDB();
		}
	}

	private VnfResourceCustomization createVnfResource(NodeTemplate vfNodeTemplate,
			ToscaResourceStructure toscaResourceStructure, Service service) {
		VnfResourceCustomization vnfResourceCustomization = vnfCustomizationRepo.findOneByModelCustomizationUUID(
				vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
		if(vnfResourceCustomization == null){		
			VnfResource vnfResource = findExistingVnfResource(service,
					vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			
			if(vnfResource==null)
				vnfResource=createVnfResource(vfNodeTemplate);
			
			vnfResourceCustomization = createVnfResourceCustomization(vfNodeTemplate, toscaResourceStructure,
					vnfResource);
			vnfResourceCustomization.setVnfResources(vnfResource);
			vnfResource.getVnfResourceCustomizations().add(vnfResourceCustomization);
			
			// Fetch VNFC Instance Group Info				
			List<Group> groupList = toscaResourceStructure.getSdcCsarHelper()
					.getGroupsOfOriginOfNodeTemplateByToscaGroupType(vfNodeTemplate,
							"org.openecomp.groups.VfcInstanceGroup");
				
			for (Group group : groupList) { 
				
				VFCInstanceGroup vfcInstanceGroup = findExistingVnfcInstanceGroup(vnfResourceCustomization,
						group.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
				if(vfcInstanceGroup == null){
					VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = createVNFCInstanceGroup(
							vfNodeTemplate, group, vnfResourceCustomization);
				
					vnfcInstanceGroupCustomizationRepo.saveAndFlush(vnfcInstanceGroupCustomization);
				}
			}
			
		}
		return vnfResourceCustomization;
	}
	
	private VnfResource findExistingVnfResource(Service service, String modelUUID) {
		VnfResource vnfResource = null;
		for(VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()){
			if (vnfResourceCustom.getVnfResources() != null
					&& vnfResourceCustom.getVnfResources().getModelUUID().equals(modelUUID)) {
				vnfResource = vnfResourceCustom.getVnfResources();
			}
		}
		if(vnfResource==null)
			vnfResource = vnfRepo.findResourceByModelUUID(modelUUID);
		
		return vnfResource;
	}

	private VnfResourceCustomization createVnfResourceCustomization(NodeTemplate vfNodeTemplate,
			ToscaResourceStructure toscaResourceStructure, VnfResource vnfResource) {
		VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
		vnfResourceCustomization.setModelCustomizationUUID(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
		vnfResourceCustomization.setModelInstanceName(vfNodeTemplate.getName());

		vnfResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
		vnfResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(vfNodeTemplate, "nf_naming_code")));
		vnfResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)));
		vnfResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)));

		vnfResourceCustomization.setMultiStageDesign(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(vfNodeTemplate, MULTI_STAGE_DESIGN));

		vnfResourceCustomization.setVnfResources(vnfResource);
		vnfResourceCustomization.setAvailabilityZoneMaxCount(Integer.getInteger(
				vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT)));	

		CapabilityAssignments vnfCustomizationCapability = toscaResourceStructure.getSdcCsarHelper()
				.getCapabilitiesOf(vfNodeTemplate);

		if (vnfCustomizationCapability != null) {
			CapabilityAssignment capAssign = vnfCustomizationCapability.getCapabilityByName(SCALABLE);

			if (capAssign != null) {
				vnfResourceCustomization.setMinInstances(Integer.getInteger(toscaResourceStructure.getSdcCsarHelper()
						.getCapabilityPropertyLeafValue(capAssign, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
				vnfResourceCustomization.setMaxInstances(Integer.getInteger(toscaResourceStructure.getSdcCsarHelper()
						.getCapabilityPropertyLeafValue(capAssign, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
			}

		}
		
		toscaResourceStructure.setCatalogVnfResourceCustomization(vnfResourceCustomization);
		
		return vnfResourceCustomization;
	}

	private VnfResource createVnfResource(NodeTemplate vfNodeTemplate) {
		VnfResource vnfResource = new VnfResource();
		vnfResource.setModelInvariantUUID(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
		vnfResource.setModelName(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
		vnfResource.setModelUUID(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));

		vnfResource.setModelVersion(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
		vnfResource.setDescription(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
		vnfResource.setOrchestrationMode(HEAT);
		vnfResource.setToscaNodeType(testNull(vfNodeTemplate.getType()));
		vnfResource.setAicVersionMax(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
		vnfResource.setAicVersionMin(
				testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
		
		return vnfResource;
	}

	private AllottedResourceCustomization createAllottedResource(NodeTemplate nodeTemplate,
			ToscaResourceStructure toscaResourceStructure, Service service) {
		AllottedResourceCustomization allottedResourceCustomization = allottedCustomizationRepo
				.findOneByModelCustomizationUUID(
				nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
			
		if(allottedResourceCustomization == null){			
			AllottedResource allottedResource = findExistingAllottedResource(service,
					nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
			
			if(allottedResource==null)
				allottedResource=createAR(nodeTemplate);
			
			toscaResourceStructure.setAllottedResource(allottedResource);			
			allottedResourceCustomization = createAllottedResourceCustomization(nodeTemplate, toscaResourceStructure);
			allottedResourceCustomization.setAllottedResource(allottedResource);
			allottedResource.getAllotedResourceCustomization().add(allottedResourceCustomization);
		}
		return allottedResourceCustomization;
	}
	
	private AllottedResource findExistingAllottedResource(Service service, String modelUUID) {
		AllottedResource allottedResource = null;
		for(AllottedResourceCustomization allottedResourceCustom : service.getAllottedCustomizations()){
			if (allottedResourceCustom.getAllottedResource() != null
					&& allottedResourceCustom.getAllottedResource().getModelUUID().equals(modelUUID)) {
				allottedResource = allottedResourceCustom.getAllottedResource();
			}
		}
		if(allottedResource==null)
			allottedResource = allottedRepo.findResourceByModelUUID(modelUUID);
		
		return allottedResource;
	}
	
	private AllottedResourceCustomization createAllottedResourceCustomization(NodeTemplate nodeTemplate,
			ToscaResourceStructure toscaResourceStructure) {
		AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
		allottedResourceCustomization.setModelCustomizationUUID(
				testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
		allottedResourceCustomization.setModelInstanceName(nodeTemplate.getName());
		

		allottedResourceCustomization.setProvidingServiceModelInvariantUUID(
				toscaResourceStructure.getCatalogService().getModelInvariantUUID());
		allottedResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
		allottedResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(nodeTemplate, "nf_naming_code")));
		allottedResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)));
		allottedResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper()
				.getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)));

		CapabilityAssignments arCustomizationCapability = toscaResourceStructure.getSdcCsarHelper()
				.getCapabilitiesOf(nodeTemplate);

		if (arCustomizationCapability != null) {
			CapabilityAssignment capAssign = arCustomizationCapability.getCapabilityByName(SCALABLE);

			if (capAssign != null) {
				allottedResourceCustomization.setMinInstances(
						Integer.getInteger(toscaResourceStructure.getSdcCsarHelper().getCapabilityPropertyLeafValue(
								capAssign, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
				allottedResourceCustomization.setMaxInstances(
						Integer.getInteger(toscaResourceStructure.getSdcCsarHelper().getCapabilityPropertyLeafValue(
								capAssign, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
			}
		}
		return allottedResourceCustomization;
	}

	private AllottedResource createAR(NodeTemplate nodeTemplate) {
		AllottedResource allottedResource = new AllottedResource();
		allottedResource
		.setModelUUID(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
		allottedResource.setModelInvariantUUID(
				testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
		allottedResource
		.setModelName(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
		allottedResource
		.setModelVersion(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
		allottedResource.setToscaNodeType(testNull(nodeTemplate.getType()));
		allottedResource.setSubcategory(
				testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
		allottedResource
		.setDescription(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
		return allottedResource;
	}

	private Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {
		// Scan the payload downloadResult and extract the HeatTemplate
		// parameters
		YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
		return yamlEditor.getParameterList(artifactUUID);
	}	

	private String testNull(Object object) {

		if (object == null) {
			return null;
		} else if (object.equals("NULL")) {
			return null;
		} else if (object instanceof Integer) {
			return object.toString();
		} else if (object instanceof String) {
			return (String) object;
		} else {
			return "Type not recognized";
		}
	}
	
	private static String identifyParentOfNestedTemplate(VfModuleStructure vfModuleStructure,
			VfModuleArtifact heatNestedArtifact) {

		if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT) != null && vfModuleStructure
				.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).getArtifactInfo().getRelatedArtifacts() != null) {
			for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0)
					.getArtifactInfo().getRelatedArtifacts()) {
				if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
					return ASDCConfiguration.HEAT;
				}

			}
		} 
		
		if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL) != null 
				&& vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo()
						.getRelatedArtifacts() != null) {
			for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL)
					.get(0).getArtifactInfo().getRelatedArtifacts()) {
				if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
					return ASDCConfiguration.HEAT_VOL;
				}
			
			}
		}
		
		// Does not belong to anything
		return null;
			
	}
	
	private static String createVNFName(VfResourceStructure vfResourceStructure) {

		return vfResourceStructure.getNotification().getServiceName() + "/"
				+ vfResourceStructure.getResourceInstance().getResourceInstanceName();
	}

	private static String createVfModuleName(VfModuleStructure vfModuleStructure) {
		
		return createVNFName(vfModuleStructure.getParentVfResource()) + "::"
				+ vfModuleStructure.getVfModuleMetadata().getVfModuleModelName();
	}
	
	
	private static Timestamp getCurrentTimeStamp() {
		
		return new Timestamp(new Date().getTime());
	}

}