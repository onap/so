/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.asdc.installer.heat;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery.EntityQueryBuilder;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery.TopologyTemplateQueryBuilder;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ASDCElementInfo;
import org.onap.so.asdc.installer.BigDecimalVersion;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleArtifact;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.bpmn.WorkflowResource;
import org.onap.so.asdc.util.YamlEditor;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkInstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.SubType;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.CollectionResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.CollectionResourceRepository;
import org.onap.so.db.catalog.data.repository.ConfigurationResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ConfigurationResourceRepository;
import org.onap.so.db.catalog.data.repository.CvnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ExternalServiceToInternalServiceRepository;
import org.onap.so.db.catalog.data.repository.HeatEnvironmentRepository;
import org.onap.so.db.catalog.data.repository.HeatFilesRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.PnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.PnfResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceProxyResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.TempNetworkHeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.data.repository.VnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogServiceModVerIdLookupRepository;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ToscaResourceInstaller {

    protected static final String NODES_VRF_ENTRY = "org.openecomp.nodes.VRFEntry";
    protected static final String VLAN_NETWORK_RECEPTOR = "org.openecomp.nodes.VLANNetworkReceptor";
    protected static final String ALLOTTED_RESOURCE = "Allotted Resource";
    protected static final String MULTI_STAGE_DESIGN = "multi_stage_design";
    protected static final String SCALABLE = "scalable";
    protected static final String BASIC = "BASIC";
    protected static final String PROVIDER = "PROVIDER";
    protected static final String HEAT = "HEAT";
    protected static final String MANUAL_RECORD = "MANUAL_RECORD";
    protected static final String MSO = "SO";
    protected static final String SDNC_MODEL_NAME = "sdnc_model_name";
    protected static final String SDNC_MODEL_VERSION = "sdnc_model_version";
    private static String CUSTOMIZATION_UUID = "customizationUUID";
    protected static final String SKIP_POST_INST_CONF = "skip_post_instantiation_configuration";
    private static final String CONTROLLER_ACTOR = "controller_actor";
    private static final String CDS_MODEL_NAME = "cds_model_name";
    private static final String CDS_MODEL_VERSION = "cds_model_version";
    private static final String DEFAULT_SOFTWARE_VERSION = "default_software_version";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected ServiceRepository serviceRepo;

    @Autowired
    protected InstanceGroupRepository instanceGroupRepo;

    @Autowired
    protected ServiceProxyResourceCustomizationRepository serviceProxyCustomizationRepo;

    @Autowired
    protected CollectionResourceRepository collectionRepo;

    @Autowired
    protected CollectionResourceCustomizationRepository collectionCustomizationRepo;

    @Autowired
    protected ConfigurationResourceCustomizationRepository configCustomizationRepo;

    @Autowired
    protected ConfigurationResourceRepository configRepo;

    @Autowired
    protected VnfResourceRepository vnfRepo;

    @Autowired
    protected VFModuleRepository vfModuleRepo;

    @Autowired
    protected VFModuleCustomizationRepository vfModuleCustomizationRepo;

    @Autowired
    protected VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo;

    @Autowired
    protected VnfcCustomizationRepository vnfcCustomizationRepo;

    @Autowired
    protected CvnfcCustomizationRepository cvnfcCustomizationRepo;

    @Autowired
    protected AllottedResourceRepository allottedRepo;

    @Autowired
    protected AllottedResourceCustomizationRepository allottedCustomizationRepo;

    @Autowired
    protected NetworkResourceRepository networkRepo;

    @Autowired
    protected HeatTemplateRepository heatRepo;

    @Autowired
    protected HeatEnvironmentRepository heatEnvRepo;

    @Autowired
    protected HeatFilesRepository heatFilesRepo;

    @Autowired
    protected NetworkResourceCustomizationRepository networkCustomizationRepo;

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
    @Autowired
    protected WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;
    @Autowired
    protected WatchdogServiceModVerIdLookupRepository watchdogModVerIdLookupRepository;

    @Autowired
    protected TempNetworkHeatTemplateRepository tempNetworkLookupRepo;

    @Autowired
    protected ExternalServiceToInternalServiceRepository externalServiceToInternalServiceRepository;

    @Autowired
    protected ToscaCsarRepository toscaCsarRepo;

    @Autowired
    protected PnfResourceRepository pnfResourceRepository;

    @Autowired
    protected PnfCustomizationRepository pnfCustomizationRepository;

    @Autowired
    protected WorkflowResource workflowResource;

    protected static final Logger logger = LoggerFactory.getLogger(ToscaResourceInstaller.class);

    public boolean isCsarAlreadyDeployed(ToscaResourceStructure toscaResourceStructure)
            throws ArtifactInstallerException {
        boolean deployed = false;
        if (toscaResourceStructure == null) {
            return deployed;
        }

        IArtifactInfo inputToscaCsar = toscaResourceStructure.getToscaArtifact();
        String checkSum = inputToscaCsar.getArtifactChecksum();
        String artifactUuid = inputToscaCsar.getArtifactUUID();

        Optional<ToscaCsar> toscaCsarObj = toscaCsarRepo.findById(artifactUuid);
        if (toscaCsarObj.isPresent()) {
            ToscaCsar toscaCsar = toscaCsarObj.get();
            if (!toscaCsar.getArtifactChecksum().equalsIgnoreCase(checkSum)) {
                String errorMessage =
                        String.format("Csar with UUID: %s already exists.Their checksums don't match", artifactUuid);
                throw new ArtifactInstallerException(errorMessage);
            } else if (toscaCsar.getArtifactChecksum().equalsIgnoreCase(checkSum)) {
                deployed = true;
            }
        }
        return deployed;
    }

    public boolean isResourceAlreadyDeployed(ResourceStructure vfResourceStruct, boolean serviceDeployed)
            throws ArtifactInstallerException {
        boolean status = false;
        ResourceStructure vfResourceStructure = vfResourceStruct;
        try {
            status = vfResourceStructure.isDeployedSuccessfully();
        } catch (RuntimeException e) {
            status = false;
            logger.debug("Exception :", e);
        }
        try {
            Service existingService =
                    serviceRepo.findOneByModelUUID(vfResourceStructure.getNotification().getServiceUUID());
            if (existingService != null && !serviceDeployed)
                status = true;
            if (status) {
                logger.info(vfResourceStructure.getResourceInstance().getResourceInstanceName(),
                        vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
                        vfResourceStructure.getNotification().getServiceName(),
                        BigDecimalVersion.castAndCheckNotificationVersionToString(
                                vfResourceStructure.getNotification().getServiceVersion()),
                        vfResourceStructure.getNotification().getServiceUUID(),
                        vfResourceStructure.getResourceInstance().getResourceName(), "", "");
                WatchdogComponentDistributionStatus wdStatus = new WatchdogComponentDistributionStatus(
                        vfResourceStruct.getNotification().getDistributionID(), MSO);
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
            logger.error(LoggingAnchor.THREE, MessageEnum.ASDC_ARTIFACT_CHECK_EXC.toString(),
                    ErrorCode.SchemaError.getValue(), "Exception - isResourceAlreadyDeployed");
            throw new ArtifactInstallerException("Exception caught during checking existence of the VNF Resource.", e);
        }
    }

    public void installTheComponentStatus(IStatusData iStatus) throws ArtifactInstallerException {
        logger.debug("Entering installTheComponentStatus for distributionId {} and ComponentName {}",
                iStatus.getDistributionID(), iStatus.getComponentName());

        try {
            WatchdogComponentDistributionStatus cdStatus =
                    new WatchdogComponentDistributionStatus(iStatus.getDistributionID(), iStatus.getComponentName());
            cdStatus.setComponentDistributionStatus(iStatus.getStatus().toString());
            watchdogCDStatusRepository.save(cdStatus);

        } catch (Exception e) {
            logger.debug("Exception caught in installTheComponentStatus {}", e.getMessage());
            throw new ArtifactInstallerException("Exception caught in installTheComponentStatus " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installTheResource(ToscaResourceStructure toscaResourceStruct, ResourceStructure resourceStruct)
            throws ArtifactInstallerException {
        if (resourceStruct instanceof VfResourceStructure) {
            installTheVfResource(toscaResourceStruct, (VfResourceStructure) resourceStruct);
        } else if (resourceStruct instanceof PnfResourceStructure) {
            installPnfResource(toscaResourceStruct, (PnfResourceStructure) resourceStruct);
        } else {
            logger.warn("Unrecognized resource type");
        }
    }

    private void installPnfResource(ToscaResourceStructure toscaResourceStruct, PnfResourceStructure resourceStruct)
            throws ArtifactInstallerException {

        // PCLO: in case of deployment failure, use a string that will represent
        // the type of artifact that failed...
        List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();
        try {
            createToscaCsar(toscaResourceStruct);
            Service service = createService(toscaResourceStruct, resourceStruct);

            processResourceSequence(toscaResourceStruct, service);
            processPnfResources(toscaResourceStruct, service, resourceStruct);
            serviceRepo.save(service);

            WatchdogComponentDistributionStatus status =
                    new WatchdogComponentDistributionStatus(resourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
            watchdogCDStatusRepository.save(status);

            toscaResourceStruct.setSuccessfulDeployment();

        } catch (Exception e) {
            logger.debug("Exception :", e);
            WatchdogComponentDistributionStatus status =
                    new WatchdogComponentDistributionStatus(resourceStruct.getNotification().getDistributionID(), MSO);
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
                logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED.toString(),
                        resourceStruct.getResourceInstance().getResourceName(),
                        resourceStruct.getNotification().getServiceVersion(), ErrorCode.DataError.getValue(),
                        "Exception - ASCDC Artifact already deployed", e);
            } else {
                String elementToLog = (!artifactListForLogging.isEmpty()
                        ? artifactListForLogging.get(artifactListForLogging.size() - 1).toString()
                        : "No element listed");
                logger.error(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_INSTALL_EXC.toString(), elementToLog,
                        ErrorCode.DataError.getValue(), "Exception caught during installation of "
                                + resourceStruct.getResourceInstance().getResourceName() + ". Transaction rollback",
                        e);
                throw new ArtifactInstallerException(
                        "Exception caught during installation of "
                                + resourceStruct.getResourceInstance().getResourceName() + ". Transaction rollback.",
                        e);
            }
        }
    }

    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installTheVfResource(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct)
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
            createServiceInfo(toscaResourceStruct, service);

            List<IEntityDetails> vfEntityList = getEntityDetails(toscaResourceStruct,
                    EntityQuery.newBuilder(SdcTypes.VF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

            List<IEntityDetails> arEntityDetails = new ArrayList<IEntityDetails>();

            for (IEntityDetails vfEntityDetails : vfEntityList) {

                Metadata metadata = vfEntityDetails.getMetadata();
                String category = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);

                if (ALLOTTED_RESOURCE.equalsIgnoreCase(category)) {
                    arEntityDetails.add(vfEntityDetails);
                }

                processVfModules(vfEntityDetails, toscaResourceStruct, vfResourceStructure, service, metadata);
            }

            processResourceSequence(toscaResourceStruct, service);
            processAllottedResources(arEntityDetails, toscaResourceStruct, service);
            processNetworks(toscaResourceStruct, service);
            // process Network Collections
            processNetworkCollections(toscaResourceStruct, service);
            // Process Service Proxy & Configuration
            processServiceProxyAndConfiguration(toscaResourceStruct, service);

            logger.info("Saving Service: {} ", service.getModelName());
            service = serviceRepo.save(service);
            correlateConfigCustomResources(service);

            workflowResource.processWorkflows(vfResourceStructure);

            WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(
                    vfResourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
            watchdogCDStatusRepository.save(status);

            toscaResourceStruct.setSuccessfulDeployment();

        } catch (Exception e) {
            logger.debug("Exception :", e);
            WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(
                    vfResourceStruct.getNotification().getDistributionID(), MSO);
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
                logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED.toString(),
                        vfResourceStructure.getResourceInstance().getResourceName(),
                        vfResourceStructure.getNotification().getServiceVersion(), ErrorCode.DataError.getValue(),
                        "Exception - ASCDC Artifact already deployed", e);
            } else {
                String elementToLog = (!artifactListForLogging.isEmpty()
                        ? artifactListForLogging.get(artifactListForLogging.size() - 1).toString()
                        : "No element listed");
                logger.error(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_INSTALL_EXC.toString(), elementToLog,
                        ErrorCode.DataError.getValue(),
                        "Exception caught during installation of "
                                + vfResourceStructure.getResourceInstance().getResourceName()
                                + ". Transaction rollback",
                        e);
                throw new ArtifactInstallerException("Exception caught during installation of "
                        + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.", e);
            }
        }
    }


    List<IEntityDetails> getRequirementList(List<IEntityDetails> resultList, List<IEntityDetails> entityDetails,
            ISdcCsarHelper iSdcCsarHelper) {

        List<IEntityDetails> entities = new ArrayList<>();
        entityDetails.addAll(entityDetails);

        for (IEntityDetails entity : entityDetails) {
            List<RequirementAssignment> requirements = entity.getRequirements();

            for (RequirementAssignment ra : requirements) {
                String reqNode = ra.getNodeTemplateName();
                for (IEntityDetails rEntity : resultList) {
                    if (rEntity.getName().equals(reqNode)) {
                        if (!resultList.contains(entityDetails)) {
                            resultList.add(entity);
                        }
                        if (entities.contains(entityDetails)) {
                            entities.remove(entityDetails);
                        }
                        break;
                    }
                }
            }
        }

        if (!entities.isEmpty()) {
            getRequirementList(resultList, entities, iSdcCsarHelper);
        }

        return resultList;
    }

    // This method retrieve resource sequence from csar file
    void processResourceSequence(ToscaResourceStructure toscaResourceStructure, Service service) {
        List<String> resouceSequence = new ArrayList<>();
        List<IEntityDetails> resultList = new ArrayList<>();

        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();

        List<IEntityDetails> vfEntityList =
                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<IEntityDetails> entities = new ArrayList<>();
        entities.addAll(vfEntityList);

        for (IEntityDetails entityDetails : vfEntityList) {
            List<RequirementAssignment> requirements = entityDetails.getRequirements();

            if (requirements == null || requirements.isEmpty()) {
                resultList.add(entityDetails);
                entities.remove(entityDetails);
            }
        }

        resultList = getRequirementList(resultList, entities, iSdcCsarHelper);

        for (IEntityDetails entity : resultList) {
            String templateName = entity.getMetadata().getValue("name");
            if (!resouceSequence.contains(templateName)) {
                resouceSequence.add(templateName);
            }
        }

        String resourceSeqStr = resouceSequence.stream().collect(Collectors.joining(","));
        service.setResourceOrder(resourceSeqStr);
        logger.debug(" resourceSeq for service uuid {}: {}", service.getModelUUID(), resourceSeqStr);
    }


    // this of temporary solution
    private static String getValue(Object value, List<Input> inputs) {
        String outInput;
        String defaultValue = null;
        if (value instanceof Map) {
            Collection values = ((LinkedHashMap) value).values();
            outInput = (values.size() > 0) ? values.toArray()[0].toString() : "";
        } else if (value instanceof GetInput) {
            String inputName = ((GetInput) value).getInputName();
            Optional<Input> inputOptional =
                    inputs.stream().filter(input -> input.getName().equals(inputName)).findFirst();
            if (inputOptional.isPresent()) {
                Input input = inputOptional.get();
                defaultValue = input.getDefault() != null ? input.getDefault().toString() : "";
            }
            // Gets a value between [ and ]
            String regex = "\\[.*?\\]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value.toString());
            String valueStr = matcher.find() ? matcher.group() : inputName;
            outInput = valueStr + "|" + defaultValue;
        } else {
            outInput = value != null ? value.toString() : "";
        }
        return outInput;
    }

    String getResourceInput(ToscaResourceStructure toscaResourceStructure, String resourceCustomizationUuid)
            throws ArtifactInstallerException {
        Map<String, String> resouceRequest = new HashMap<>();
        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();

        List<IEntityDetails> nodeTemplateEntityList =
                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<IEntityDetails> nodeTemplateOpt = nodeTemplateEntityList.stream()
                .filter(e -> e.getMetadata().getValue(CUSTOMIZATION_UUID).equals(resourceCustomizationUuid))
                .findFirst();
        if (nodeTemplateOpt.isPresent()) {
            IEntityDetails entityDetails = nodeTemplateOpt.get();
            Map<String, Property> resourceProperties = entityDetails.getProperties();

            for (String key : resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);

                String value = getValue(property.getValue(), serInput);
                resouceRequest.put(key, value);
            }
        }

        try {
            String jsonStr = mapper.writeValueAsString(resouceRequest);

            jsonStr = jsonStr.replace("\"", "\\\"");
            logger.debug("resource request for resource customization id {}: {}", resourceCustomizationUuid, jsonStr);
            return jsonStr;
        } catch (JsonProcessingException e) {
            logger.error("resource input could not be deserialized for resource customization id ("
                    + resourceCustomizationUuid + ")");
            throw new ArtifactInstallerException("resource input could not be parsed", e);
        }
    }

    protected void processNetworks(ToscaResourceStructure toscaResourceStruct, Service service)
            throws ArtifactInstallerException {

        List<IEntityDetails> vlEntityList = getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.VL),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (vlEntityList != null) {
            for (IEntityDetails vlEntity : vlEntityList) {
                String networkResourceModelName = vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME);

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(networkResourceModelName);

                if (tempNetworkLookUp != null) {
                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    if (heatTemplate != null) {
                        NetworkResourceCustomization networkCustomization = createNetwork(vlEntity, toscaResourceStruct,
                                heatTemplate, tempNetworkLookUp.getAicVersionMax(),
                                tempNetworkLookUp.getAicVersionMin(), service);
                        // only insert unique entries
                        if (!service.getNetworkCustomizations().contains(networkCustomization)) {
                            service.getNetworkCustomizations().add(networkCustomization);
                        }
                    } else {
                        throw new ArtifactInstallerException("No HeatTemplate found for artifactUUID: "
                                + tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    }
                } else {
                    NetworkResourceCustomization networkCustomization =
                            createNetwork(vlEntity, toscaResourceStruct, null, null, null, service);
                    networkCustomization.setResourceInput(
                            getResourceInput(toscaResourceStruct, networkCustomization.getModelCustomizationUUID()));
                    service.getNetworkCustomizations().add(networkCustomization);
                    logger.debug("No NetworkResourceName found in TempNetworkHeatTemplateLookup for {}",
                            networkResourceModelName);
                }

            }
        }
    }

    protected void processAllottedResources(List<IEntityDetails> arEntityDetails,
            ToscaResourceStructure toscaResourceStruct, Service service) throws ArtifactInstallerException {

        List<IEntityDetails> pnfAREntityList = getEntityDetails(toscaResourceStruct,
                EntityQuery.newBuilder(SdcTypes.PNF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        for (IEntityDetails pnfEntity : pnfAREntityList) {

            Metadata metadata = pnfEntity.getMetadata();
            String category = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);
            if (ALLOTTED_RESOURCE.equalsIgnoreCase(category)) {
                arEntityDetails.add(pnfEntity);
            }

        }

        if (arEntityDetails != null) {
            for (IEntityDetails arEntity : arEntityDetails) {
                AllottedResourceCustomization allottedResource =
                        createAllottedResource(arEntity, toscaResourceStruct, service);
                String resourceInput =
                        getResourceInput(toscaResourceStruct, allottedResource.getModelCustomizationUUID());
                if (!"{}".equals(resourceInput)) {
                    allottedResource.setResourceInput(resourceInput);
                }
                if (!service.getAllottedCustomizations().contains(allottedResource)) {
                    service.getAllottedCustomizations().add(allottedResource);
                }
            }
        }
    }


    protected ConfigurationResource getConfigurationResource(IEntityDetails configEntity) {
        Metadata metadata = configEntity.getMetadata();
        ConfigurationResource configResource = new ConfigurationResource();
        configResource.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(configEntity.getToscaType());
        return configResource;
    }

    protected ConfigurationResourceCustomization getConfigurationResourceCustomization(IEntityDetails configEntity,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service) {
        Metadata metadata = configEntity.getMetadata();

        ConfigurationResource configResource = getConfigurationResource(configEntity);

        ConfigurationResourceCustomization configCustomizationResource = new ConfigurationResourceCustomization();

        Set<ConfigurationResourceCustomization> configResourceCustomizationSet = new HashSet<>();

        configCustomizationResource
                .setModelCustomizationUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        configCustomizationResource.setModelInstanceName(configEntity.getName());

        configCustomizationResource.setFunction(getLeafPropertyValue(configEntity, "function"));
        configCustomizationResource.setRole(getLeafPropertyValue(configEntity, "role"));
        configCustomizationResource.setType(getLeafPropertyValue(configEntity, "type"));

        configCustomizationResource.setServiceProxyResourceCustomization(spResourceCustomization);

        configCustomizationResource.setConfigurationResource(configResource);
        configCustomizationResource.setService(service);
        configResourceCustomizationSet.add(configCustomizationResource);

        configResource.setConfigurationResourceCustomization(configResourceCustomizationSet);

        return configCustomizationResource;
    }

    protected void processServiceProxyAndConfiguration(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<IEntityDetails> spEntityList =
                getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.SERVICE_PROXY),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<IEntityDetails> configEntityList =
                getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.CONFIGURATION),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<ServiceProxyResourceCustomization> serviceProxyList = new ArrayList<>();
        List<ConfigurationResourceCustomization> configurationResourceList = new ArrayList<>();

        ServiceProxyResourceCustomization serviceProxy = null;

        if (spEntityList != null) {
            for (IEntityDetails spEntity : spEntityList) {
                serviceProxy = createServiceProxy(spEntity, service, toscaResourceStruct);
                serviceProxyList.add(serviceProxy);

                for (IEntityDetails configEntity : configEntityList) {

                    List<RequirementAssignment> requirements = configEntity.getRequirements();

                    for (RequirementAssignment requirement : requirements) {

                        if (requirement.getNodeTemplateName().equals(spEntity.getName())) {
                            ConfigurationResourceCustomization configurationResource = createConfiguration(configEntity,
                                    toscaResourceStruct, serviceProxy, service, configurationResourceList);

                            Optional<ConfigurationResourceCustomization> matchingObject =
                                    configurationResourceList.stream()
                                            .filter(configurationResourceCustomization -> configEntity.getMetadata()
                                                    .getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)
                                                    .equals(configurationResource.getModelCustomizationUUID()))
                                            .filter(configurationResourceCustomization -> configurationResourceCustomization
                                                    .getModelInstanceName()
                                                    .equals(configurationResource.getModelInstanceName()))
                                            .findFirst();
                            if (!matchingObject.isPresent()) {
                                configurationResourceList.add(configurationResource);
                            }
                            break;
                        }
                    }
                }

            }
        }

        service.setConfigurationCustomizations(configurationResourceList);
        service.setServiceProxyCustomizations(serviceProxyList);
    }

    /*
     * ConfigurationResourceCustomization objects have their IDs auto incremented in the database. Unless we know their
     * IDs we cannot possibly associate their related records. So these ConfigResourceCustomizations are persisted first
     * and subsequently correlated.
     */

    protected void correlateConfigCustomResources(Service service) {
        /* Assuming that we have only one pair of VRF-VNR */
        ConfigurationResourceCustomization vrfConfigCustomResource = null;
        ConfigurationResourceCustomization vnrConfigCustomResource = null;
        List<ConfigurationResourceCustomization> configCustomList = service.getConfigurationCustomizations();
        for (ConfigurationResourceCustomization configResource : configCustomList) {
            String nodeType = configResource.getConfigurationResource().getToscaNodeType();
            if (NODES_VRF_ENTRY.equalsIgnoreCase(nodeType)) {
                vrfConfigCustomResource = configResource;
            } else if (VLAN_NETWORK_RECEPTOR.equalsIgnoreCase(nodeType)) {
                vnrConfigCustomResource = configResource;
            }
        }

        if (vrfConfigCustomResource != null) {
            vrfConfigCustomResource.setConfigResourceCustomization(vnrConfigCustomResource);
            configCustomizationRepo.save(vrfConfigCustomResource);

        }
        if (vnrConfigCustomResource != null) {
            vnrConfigCustomResource.setConfigResourceCustomization(vrfConfigCustomResource);
            configCustomizationRepo.save(vnrConfigCustomResource);
        }
    }

    protected void processNetworkCollections(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<IEntityDetails> crEntityList = getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.CR),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (crEntityList != null) {
            for (IEntityDetails ncEntity : crEntityList) {

                createNetworkCollection(ncEntity, toscaResourceStruct, service);
                collectionRepo.saveAndFlush(toscaResourceStruct.getCatalogCollectionResource());

                List<NetworkInstanceGroup> networkInstanceGroupList =
                        toscaResourceStruct.getCatalogNetworkInstanceGroup();
                for (NetworkInstanceGroup networkInstanceGroup : networkInstanceGroupList) {
                    instanceGroupRepo.saveAndFlush(networkInstanceGroup);
                }

            }
        }
        service.getCollectionResourceCustomizations()
                .add(toscaResourceStruct.getCatalogCollectionResourceCustomization());
    }



    /**
     * This is used to process the PNF specific resource, including resource and resource_customization.
     * {@link IEntityDetails} based API is used to retrieve information. Please check {@link ISdcCsarHelper} for
     * details.
     */
    protected void processPnfResources(ToscaResourceStructure toscaResourceStruct, Service service,
            PnfResourceStructure resourceStructure) throws Exception {
        logger.info("Processing PNF resource: {}", resourceStructure.getResourceInstance().getResourceUUID());

        ISdcCsarHelper sdcCsarHelper = toscaResourceStruct.getSdcCsarHelper();
        EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.PNF).build();
        TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();

        List<IEntityDetails> entityDetailsList = sdcCsarHelper.getEntity(entityQuery, topologyTemplateQuery, false);
        for (IEntityDetails entityDetails : entityDetailsList) {
            Metadata metadata = entityDetails.getMetadata();
            String customizationUUID = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
            String modelUuid = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
            String notifCustomizationUUID = resourceStructure.getResourceInstance().getResourceCustomizationUUID();
            if (customizationUUID != null && customizationUUID.equals(notifCustomizationUUID)) {
                logger.info("Resource customization UUID: {} is the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);

                if (checkExistingPnfResourceCutomization(customizationUUID)) {
                    logger.info("Resource customization UUID: {} already deployed", customizationUUID);
                } else {
                    PnfResource pnfResource = findExistingPnfResource(service, modelUuid);
                    if (pnfResource == null) {
                        pnfResource = createPnfResource(entityDetails);
                    }
                    PnfResourceCustomization pnfResourceCustomization =
                            createPnfResourceCustomization(entityDetails, pnfResource);
                    pnfResource.getPnfResourceCustomizations().add(pnfResourceCustomization);
                    toscaResourceStruct.setPnfResourceCustomization(pnfResourceCustomization);
                    service.getPnfCustomizations().add(pnfResourceCustomization);
                }
            } else {
                logger.warn(
                        "Resource customization UUID: {} is NOT the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);
            }
        }
    }

    private PnfResource findExistingPnfResource(Service service, String modelUuid) {
        PnfResource pnfResource = null;
        for (PnfResourceCustomization pnfResourceCustomization : service.getPnfCustomizations()) {
            if (pnfResourceCustomization.getPnfResources() != null
                    && pnfResourceCustomization.getPnfResources().getModelUUID().equals(modelUuid)) {
                pnfResource = pnfResourceCustomization.getPnfResources();
            }
        }
        if (pnfResource == null) {
            pnfResource = pnfResourceRepository.findById(modelUuid).orElse(pnfResource);
        }
        return pnfResource;
    }

    private boolean checkExistingPnfResourceCutomization(String customizationUUID) {
        return pnfCustomizationRepository.findById(customizationUUID).isPresent();
    }

    /**
     * Construct the {@link PnfResource} from {@link IEntityDetails} object.
     */
    private PnfResource createPnfResource(IEntityDetails entity) {
        PnfResource pnfResource = new PnfResource();
        Metadata metadata = entity.getMetadata();
        pnfResource.setModelInvariantUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        pnfResource.setModelName(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        pnfResource.setModelUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        pnfResource.setModelVersion(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        pnfResource.setDescription(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        pnfResource.setCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY)));
        pnfResource.setSubCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        pnfResource.setToscaNodeType(entity.getToscaType());
        return pnfResource;
    }

    /**
     * Construct the {@link PnfResourceCustomization} from {@link IEntityDetails} object.
     */
    private PnfResourceCustomization createPnfResourceCustomization(IEntityDetails entityDetails,
            PnfResource pnfResource) {

        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        Metadata metadata = entityDetails.getMetadata();
        Map<String, Property> properties = entityDetails.getProperties();
        pnfResourceCustomization.setModelCustomizationUUID(
                testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        pnfResourceCustomization.setModelInstanceName(entityDetails.getName());
        pnfResourceCustomization
                .setNfFunction(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
        pnfResourceCustomization.setNfNamingCode(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFCODE)));
        pnfResourceCustomization.setNfRole(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFROLE)));
        pnfResourceCustomization.setNfType(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFTYPE)));
        pnfResourceCustomization.setMultiStageDesign(getStringValue(properties.get(MULTI_STAGE_DESIGN)));
        pnfResourceCustomization.setBlueprintName(getStringValue(properties.get(SDNC_MODEL_NAME)));
        pnfResourceCustomization.setBlueprintVersion(getStringValue(properties.get(SDNC_MODEL_VERSION)));
        pnfResourceCustomization.setSkipPostInstConf(getBooleanValue(properties.get(SKIP_POST_INST_CONF)));
        pnfResourceCustomization.setControllerActor(getStringValue(properties.get(CONTROLLER_ACTOR)));
        pnfResourceCustomization.setDefaultSoftwareVersion(extractDefaultSoftwareVersionFromSwVersions(properties));
        pnfResourceCustomization.setPnfResources(pnfResource);
        return pnfResourceCustomization;
    }

    private String extractDefaultSoftwareVersionFromSwVersions(Map<String, Property> properties) {
        final String SOFTWARE_VERSIONS = "software_versions";
        final String EMPTY_STRING = "";
        String defaultSoftwareVersionValue = getStringValue(properties.get(DEFAULT_SOFTWARE_VERSION));
        if (defaultSoftwareVersionValue != null && !defaultSoftwareVersionValue.isEmpty()) {
            return defaultSoftwareVersionValue;
        }
        if (properties.containsKey(SOFTWARE_VERSIONS) && properties.get(SOFTWARE_VERSIONS).getValue() != null) {
            if (properties.get(SOFTWARE_VERSIONS).getValue() instanceof List) {
                List<String> propertyValueList = (List) properties.get(SOFTWARE_VERSIONS).getValue();
                return propertyValueList.get(0);
            } else if (properties.get(SOFTWARE_VERSIONS).getValue() instanceof String) {
                return getStringValue(properties.get(SOFTWARE_VERSIONS));
            }
        }

        return EMPTY_STRING;
    }

    /**
     * Get value from {@link Property} and cast to boolean value. Return true if property is null.
     */
    private boolean getBooleanValue(Property property) {
        if (null == property) {
            return true;
        }
        Object value = property.getValue();
        return new Boolean(String.valueOf(value));
    }

    /**
     * Get value from {@link Property} and cast to String value. Return empty String if property is null value.
     */
    private String getStringValue(Property property) {
        if (null == property) {
            return "";
        }
        Object value = property.getValue();
        return String.valueOf(value);
    }

    protected void processVfModules(IEntityDetails vfEntityDetails, ToscaResourceStructure toscaResourceStruct,
            VfResourceStructure vfResourceStructure, Service service, Metadata metadata) throws Exception {

        String vfCustomizationCategory =
                vfEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);

        logger.debug("VF Category is: {} ", vfCustomizationCategory);

        String vfCustomizationUUID =
                vfEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

        logger.debug("VFCustomizationUUID= {}", vfCustomizationUUID);

        IResourceInstance vfNotificationResource = vfResourceStructure.getResourceInstance();

        // Make sure the VF ResourceCustomizationUUID from the notification and tosca customizations match before
        // comparing their VF Modules UUID's
        logger.debug(
                "Checking if Notification VF ResourceCustomizationUUID: {} matches Tosca VF Customization UUID: {}",
                vfNotificationResource.getResourceCustomizationUUID(), vfCustomizationUUID);

        if (vfCustomizationUUID.equals(vfNotificationResource.getResourceCustomizationUUID())) {

            logger.debug("vfCustomizationUUID: {}  matches vfNotificationResource CustomizationUUID ",
                    vfCustomizationUUID);

            VnfResourceCustomization vnfResource = createVnfResource(vfEntityDetails, toscaResourceStruct, service);

            if (vfResourceStructure.getVfModuleStructure() != null
                    && !vfResourceStructure.getVfModuleStructure().isEmpty()) {
                Set<CvnfcCustomization> existingCvnfcSet = new HashSet<>();
                Set<VnfcCustomization> existingVnfcSet = new HashSet<>();
                List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom = new ArrayList<>();

                for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {

                    logger.debug("vfModuleStructure: {}", vfModuleStructure);

                    List<IEntityDetails> vfModuleEntityList =
                            getEntityDetails(toscaResourceStruct,
                                    EntityQuery.newBuilder("org.openecomp.groups.VfModule"), TopologyTemplateQuery
                                            .newBuilder(SdcTypes.SERVICE).customizationUUID(vfCustomizationUUID),
                                    false);

                    IVfModuleData vfMetadata = vfModuleStructure.getVfModuleMetadata();

                    logger.debug("Comparing Vf_Modules_Metadata CustomizationUUID : "
                            + vfMetadata.getVfModuleModelCustomizationUUID());

                    Optional<IEntityDetails> matchingObject = vfModuleEntityList.stream()
                            .peek(group -> logger.debug("To Csar Group VFModuleModelCustomizationUUID "
                                    + group.getMetadata().getValue("vfModuleModelCustomizationUUID")))
                            .filter(group -> group.getMetadata().getValue("vfModuleModelCustomizationUUID")
                                    .equals(vfMetadata.getVfModuleModelCustomizationUUID()))
                            .findFirst();
                    if (matchingObject.isPresent()) {
                        VfModuleCustomization vfModuleCustomization = createVFModuleResource(matchingObject.get(),
                                toscaResourceStruct, vfResourceStructure, vfMetadata, vnfResource, service,
                                existingCvnfcSet, existingVnfcSet, existingCvnfcConfigurationCustom);
                        vfModuleCustomization.getVfModule().setVnfResources(vnfResource.getVnfResources());
                    } else
                        throw new Exception(
                                "Cannot find matching VFModule Customization in Csar for Vf_Modules_Metadata: "
                                        + vfMetadata.getVfModuleModelCustomizationUUID());

                }
            }

            // Check for VNFC Instance Group info and add it if there is
            List<IEntityDetails> vfcEntityList = getEntityDetails(toscaResourceStruct,
                    EntityQuery.newBuilder("org.openecomp.groups.VfcInstanceGroup"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.VF).customizationUUID(vfCustomizationUUID), false);

            Set<VnfcCustomization> existingVnfcGroupSet = new HashSet<>();

            for (IEntityDetails groupEntity : vfcEntityList) {
                VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = createVNFCInstanceGroup(groupEntity,
                        vfEntityDetails, vnfResource, toscaResourceStruct, existingVnfcGroupSet);
                vnfcInstanceGroupCustomizationRepo.saveAndFlush(vnfcInstanceGroupCustomization);
            }

            List<String> seqResult = processVNFCGroupSequence(toscaResourceStruct, vfcEntityList);
            if (!CollectionUtils.isEmpty(seqResult)) {
                String resultStr = seqResult.stream().collect(Collectors.joining(","));
                vnfResource.setVnfcInstanceGroupOrder(resultStr);
                logger.debug("vnfcGroupOrder result for service uuid {}: {}", service.getModelUUID(), resultStr);
            }
            // add this vnfResource with existing vnfResource for this service
            addVnfCustomization(service, vnfResource);
        } else {
            logger.debug("Notification VF ResourceCustomizationUUID: "
                    + vfNotificationResource.getResourceCustomizationUUID() + " doesn't match "
                    + "Tosca VF Customization UUID: " + vfCustomizationUUID);
        }
    }

    private List<String> processVNFCGroupSequence(ToscaResourceStructure toscaResourceStructure,
            List<IEntityDetails> groupEntityDetails) {
        if (CollectionUtils.isEmpty(groupEntityDetails)) {
            return Collections.emptyList();
        }

        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();
        List<String> strSequence = new ArrayList<>(groupEntityDetails.size());
        List<IEntityDetails> tempEntityList = new ArrayList<>(groupEntityDetails.size());
        List<IEntityDetails> entities = new ArrayList<>();
        tempEntityList.addAll(groupEntityDetails);

        for (IEntityDetails vnfcEntityDetails : groupEntityDetails) {

            List<IEntityDetails> vnfcMemberNodes = vnfcEntityDetails.getMemberNodes();

            boolean hasRequirements = false;
            for (IEntityDetails vnfcDetails : vnfcMemberNodes) {

                List<RequirementAssignment> requirements = vnfcDetails.getRequirements();

                if (requirements != null && !requirements.isEmpty()) {
                    hasRequirements = true;
                    break;
                }
            }

            if (!hasRequirements) {
                strSequence.add(vnfcEntityDetails.getName());
                tempEntityList.remove(vnfcEntityDetails);
                entities.addAll(vnfcMemberNodes);
            }
        }

        getVNFCGroupSequenceList(strSequence, tempEntityList, entities, iSdcCsarHelper);

        return strSequence;

    }

    private void getVNFCGroupSequenceList(List<String> strSequence, List<IEntityDetails> vnfcGroupDetails,
            List<IEntityDetails> vnfcMemberNodes, ISdcCsarHelper iSdcCsarHelper) {
        if (CollectionUtils.isEmpty(vnfcGroupDetails)) {
            return;
        }

        List<IEntityDetails> tempGroupList = new ArrayList<>();
        tempGroupList.addAll(vnfcGroupDetails);

        for (IEntityDetails vnfcGroup : vnfcGroupDetails) {
            List<IEntityDetails> members = vnfcGroup.getMemberNodes();
            for (IEntityDetails memberNode : members) {
                boolean isAllExists = true;


                List<RequirementAssignment> requirements = memberNode.getRequirements();

                if (requirements == null || requirements.isEmpty()) {
                    continue;
                }


                for (RequirementAssignment rqa : requirements) {
                    String name = rqa.getNodeTemplateName();
                    for (IEntityDetails node : vnfcMemberNodes) {
                        if (name.equals(node.getName())) {
                            break;
                        }
                    }

                    isAllExists = false;
                    break;
                }

                if (isAllExists) {
                    strSequence.add(vnfcGroup.getName());
                    tempGroupList.remove(vnfcGroupDetails);
                    vnfcMemberNodes.addAll(vnfcGroupDetails);
                }
            }

            if (!tempGroupList.isEmpty() && tempGroupList.size() < vnfcGroupDetails.size()) {
                getVNFCGroupSequenceList(strSequence, tempGroupList, vnfcMemberNodes, iSdcCsarHelper);
            }
        }
    }

    public void processWatchdog(String distributionId, String servideUUID, Optional<String> distributionNotification,
            String consumerId) {
        WatchdogServiceModVerIdLookup modVerIdLookup =
                new WatchdogServiceModVerIdLookup(distributionId, servideUUID, distributionNotification, consumerId);
        watchdogModVerIdLookupRepository.saveAndFlush(modVerIdLookup);

        try {

            WatchdogDistributionStatus distributionStatus = new WatchdogDistributionStatus(distributionId);
            watchdogDistributionStatusRepository.saveAndFlush(distributionStatus);

        } catch (ObjectOptimisticLockingFailureException e) {
            logger.debug("ObjectOptimisticLockingFailureException in processWatchdog : {} ", e);
            throw e;
        }
    }

    protected void extractHeatInformation(ToscaResourceStructure toscaResourceStruct,
            VfResourceStructure vfResourceStructure) {
        for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

            switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
                case ASDCConfiguration.HEAT:
                case ASDCConfiguration.HEAT_NESTED:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_VOL:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    VfModuleArtifact envModuleArtifact =
                            getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
                    createHeatEnvFromArtifact(vfResourceStructure, envModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ENV:
                    createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ARTIFACT:
                    createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact, toscaResourceStruct);
                    break;
                case ASDCConfiguration.HEAT_NET:
                case ASDCConfiguration.OTHER:
                case ASDCConfiguration.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT:
                    logger.warn(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT.toString(),
                            vfModuleArtifact.getArtifactInfo().getArtifactType() + "(Artifact Name:"
                                    + vfModuleArtifact.getArtifactInfo().getArtifactName() + ")",
                            ErrorCode.DataError.getValue(), "Artifact type not supported");
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

                newFileBody =
                        verifyTheFilePrefixInString(newFileBody, moduleArtifact.getArtifactInfo().getArtifactName());
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

    protected void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,
            ToscaResourceStructure toscaResourceStruct, VfModuleArtifact vfModuleArtifact) {

        HeatTemplate existingHeatTemplate =
                heatRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatTemplate == null) {
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

            Set<HeatTemplateParam> heatParam = extractHeatTemplateParameters(vfModuleArtifact.getResult(),
                    vfModuleArtifact.getArtifactInfo().getArtifactUUID());
            heatTemplate.setParameters(heatParam);
            vfModuleArtifact.setHeatTemplate(heatTemplate);
        } else {
            if (vfModuleArtifact.getArtifactInfo().getArtifactTimeout() != null) {
                existingHeatTemplate.setTimeoutMinutes(vfModuleArtifact.getArtifactInfo().getArtifactTimeout());
            }
            vfModuleArtifact.setHeatTemplate(existingHeatTemplate);
        }
    }

    protected void createHeatEnvFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact) {

        HeatEnvironment existingHeatEnvironment =
                heatEnvRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatEnvironment == null) {
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
        } else {
            vfModuleArtifact.setHeatEnvironment(existingHeatEnvironment);
        }
    }

    protected void createHeatFileFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact, ToscaResourceStructure toscaResourceStruct) {

        HeatFiles existingHeatFiles =
                heatFilesRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatFiles == null) {
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
        } else {
            vfModuleArtifact.setHeatFiles(existingHeatFiles);
        }
    }

    protected Service createService(ToscaResourceStructure toscaResourceStructure,
            ResourceStructure resourceStructure) {

        Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();
        List<Service> services =
                serviceRepo.findByModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        Service service;
        if (!services.isEmpty() && services.size() > 0) {
            service = services.get(0);
        } else {
            service = new Service();
        }

        if (serviceMetadata != null) {

            if (toscaResourceStructure.getServiceVersion() != null) {
                service.setModelVersion(toscaResourceStructure.getServiceVersion());
            }

            service.setServiceType(serviceMetadata.getValue("serviceType"));
            service.setServiceRole(serviceMetadata.getValue("serviceRole"));
            service.setServiceFunction(serviceMetadata.getValue("serviceFunction"));
            service.setCategory(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));

            service.setDescription(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
            service.setModelName(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            service.setModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            service.setEnvironmentContext(serviceMetadata.getValue("environmentContext"));

            if (resourceStructure != null)
                service.setWorkloadContext(resourceStructure.getNotification().getWorkloadContext());

            service.setModelInvariantUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            service.setCsar(toscaResourceStructure.getCatalogToscaCsar());
            service.setNamingPolicy(serviceMetadata.getValue("namingPolicy"));
            String generateNaming = serviceMetadata.getValue("ecompGeneratedNaming");
            Boolean generateNamingValue = null;
            if (generateNaming != null) {
                generateNamingValue = "true".equalsIgnoreCase(generateNaming);
            }
            service.setOnapGeneratedNaming(generateNamingValue);

            List<Input> serviceInputs = toscaResourceStructure.getSdcCsarHelper().getServiceInputs();
            logger.debug("serviceInputs: {} " + serviceInputs);
            if (!serviceInputs.isEmpty()) {
                serviceInputs.forEach(input -> {
                    if (CDS_MODEL_NAME.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setBlueprintName(value);
                    }
                    if (CDS_MODEL_VERSION.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setBlueprintVersion(value);
                    }
                    if (CONTROLLER_ACTOR.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setControllerActor(value);
                    }
                    if (SKIP_POST_INST_CONF.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : "false";
                        service.setSkipPostInstConf(Boolean.valueOf(value));
                    }
                });
            }
        }
        toscaResourceStructure.setCatalogService(service);
        return service;
    }

    protected ServiceProxyResourceCustomization createServiceProxy(IEntityDetails spEntity, Service service,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata spMetadata = spEntity.getMetadata();

        ServiceProxyResourceCustomization spCustomizationResource = new ServiceProxyResourceCustomization();

        Set<ServiceProxyResourceCustomization> serviceProxyCustomizationSet = new HashSet<>();

        spCustomizationResource.setModelName(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        spCustomizationResource
                .setModelInvariantUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        spCustomizationResource.setModelUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        spCustomizationResource.setModelVersion(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        spCustomizationResource.setDescription(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        spCustomizationResource
                .setModelCustomizationUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        spCustomizationResource.setModelInstanceName(spEntity.getName());
        spCustomizationResource.setToscaNodeType(spEntity.getToscaType());

        String sourceServiceUUID = spMetadata.getValue("sourceModelUuid");

        Service sourceService = serviceRepo.findOneByModelUUID(sourceServiceUUID);

        spCustomizationResource.setSourceService(sourceService);
        spCustomizationResource.setToscaNodeType(spEntity.getToscaType());
        serviceProxyCustomizationSet.add(spCustomizationResource);


        toscaResourceStructure.setCatalogServiceProxyResourceCustomization(spCustomizationResource);

        return spCustomizationResource;
    }

    protected ConfigurationResourceCustomization createConfiguration(IEntityDetails configEntity,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service, List<ConfigurationResourceCustomization> configurationResourceList) {

        ConfigurationResourceCustomization configCustomizationResource = getConfigurationResourceCustomization(
                configEntity, toscaResourceStructure, spResourceCustomization, service);

        ConfigurationResource configResource = null;

        ConfigurationResource existingConfigResource = findExistingConfiguration(service,
                configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID), configurationResourceList);

        if (existingConfigResource == null) {
            configResource = getConfigurationResource(configEntity);
        } else {
            configResource = existingConfigResource;
        }

        configCustomizationResource.setConfigurationResource(configResource);

        return configCustomizationResource;
    }

    protected ConfigurationResource createFabricConfiguration(IEntityDetails fabricEntity,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata fabricMetadata = fabricEntity.getMetadata();

        ConfigurationResource configResource = new ConfigurationResource();

        configResource.setModelName(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(fabricEntity.getToscaType());

        return configResource;
    }

    protected void createToscaCsar(ToscaResourceStructure toscaResourceStructure) {
        Optional<ToscaCsar> toscaCsarOpt =
                toscaCsarRepo.findById(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
        ToscaCsar toscaCsar;
        if (!toscaCsarOpt.isPresent()) {
            toscaCsar = new ToscaCsar();
            toscaCsar.setArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
        } else {
            toscaCsar = toscaCsarOpt.get();
        }
        if (toscaResourceStructure.getToscaArtifact().getArtifactChecksum() != null) {
            toscaCsar.setArtifactChecksum(toscaResourceStructure.getToscaArtifact().getArtifactChecksum());
        } else {
            toscaCsar.setArtifactChecksum(MANUAL_RECORD);
        }
        toscaCsar.setName(toscaResourceStructure.getToscaArtifact().getArtifactName());
        toscaCsar.setVersion(toscaResourceStructure.getToscaArtifact().getArtifactVersion());
        toscaCsar.setDescription(toscaResourceStructure.getToscaArtifact().getArtifactDescription());
        toscaCsar.setUrl(toscaResourceStructure.getToscaArtifact().getArtifactURL());

        toscaResourceStructure.setCatalogToscaCsar(toscaCsar);
    }

    protected VnfcCustomization findExistingVfc(Set<VnfcCustomization> vnfcCustomizations, String customizationUUID) {
        VnfcCustomization vnfcCustomization = null;
        for (VnfcCustomization vnfcCustom : vnfcCustomizations) {
            if (vnfcCustom != null && vnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                vnfcCustomization = vnfcCustom;
            }
        }

        if (vnfcCustomization == null)
            vnfcCustomization = vnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);

        return vnfcCustomization;
    }

    protected CvnfcCustomization findExistingCvfc(Set<CvnfcCustomization> cvnfcCustomizations,
            String customizationUUID) {
        CvnfcCustomization cvnfcCustomization = null;
        for (CvnfcCustomization cvnfcCustom : cvnfcCustomizations) {
            if (cvnfcCustom != null && cvnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                cvnfcCustomization = cvnfcCustom;
            }
        }

        if (cvnfcCustomization == null)
            cvnfcCustomization = cvnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);

        return cvnfcCustomization;
    }

    protected NetworkResourceCustomization createNetwork(IEntityDetails networkEntity,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin,
            Service service) {

        NetworkResourceCustomization networkResourceCustomization =
                networkCustomizationRepo.findOneByModelCustomizationUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        boolean networkUUIDsMatch = true;
        // Check to make sure the NetworkResourceUUID on the Customization record matches the NetworkResourceUUID from
        // the distribution.
        // If not we'll update the Customization record with latest from the distribution
        if (networkResourceCustomization != null) {
            String existingNetworkModelUUID = networkResourceCustomization.getNetworkResource().getModelUUID();
            String latestNetworkModelUUID = networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID);

            if (!existingNetworkModelUUID.equals(latestNetworkModelUUID)) {
                networkUUIDsMatch = false;
            }

        }

        if (networkResourceCustomization != null && !networkUUIDsMatch) {

            NetworkResource networkResource =
                    createNetworkResource(networkEntity, toscaResourceStructure, heatTemplate, aicMax, aicMin);

            networkResourceCustomization.setNetworkResource(networkResource);

            networkCustomizationRepo.saveAndFlush(networkResourceCustomization);


        } else if (networkResourceCustomization == null) {
            networkResourceCustomization = createNetworkResourceCustomization(networkEntity, toscaResourceStructure);

            NetworkResource networkResource = findExistingNetworkResource(service,
                    networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            if (networkResource == null)
                networkResource =
                        createNetworkResource(networkEntity, toscaResourceStructure, heatTemplate, aicMax, aicMin);

            networkResource.addNetworkResourceCustomization(networkResourceCustomization);
            networkResourceCustomization.setNetworkResource(networkResource);
        }

        return networkResourceCustomization;
    }

    protected NetworkResource findExistingNetworkResource(Service service, String modelUUID) {
        NetworkResource networkResource = null;
        for (NetworkResourceCustomization networkCustom : service.getNetworkCustomizations()) {
            if (networkCustom.getNetworkResource() != null
                    && networkCustom.getNetworkResource().getModelUUID().equals(modelUUID)) {
                networkResource = networkCustom.getNetworkResource();
            }
        }
        if (networkResource == null)
            networkResource = networkRepo.findResourceByModelUUID(modelUUID);

        return networkResource;
    }

    protected NetworkResourceCustomization createNetworkResourceCustomization(IEntityDetails networkEntity,
            ToscaResourceStructure toscaResourceStructure) {
        NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
        networkResourceCustomization.setModelInstanceName(
                testNull(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        networkResourceCustomization.setModelCustomizationUUID(
                testNull(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));

        networkResourceCustomization.setNetworkTechnology(
                getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));

        networkResourceCustomization
                .setNetworkType(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));

        networkResourceCustomization
                .setNetworkRole(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));

        networkResourceCustomization
                .setNetworkScope(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));

        return networkResourceCustomization;
    }

    protected NetworkResource createNetworkResource(IEntityDetails vlEntity,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin) {
        NetworkResource networkResource = new NetworkResource();
        String providerNetwork =
                getLeafPropertyValue(vlEntity, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

        if ("true".equalsIgnoreCase(providerNetwork)) {
            networkResource.setNeutronNetworkType(PROVIDER);
        } else {
            networkResource.setNeutronNetworkType(BASIC);
        }

        networkResource.setModelName(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));

        networkResource.setModelInvariantUUID(
                testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        networkResource.setModelUUID(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        networkResource
                .setModelVersion(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));

        networkResource.setAicVersionMax(aicMax);
        networkResource.setAicVersionMin(aicMin);
        networkResource.setToscaNodeType(vlEntity.getToscaType());
        networkResource
                .setDescription(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        networkResource.setOrchestrationMode(HEAT);
        networkResource.setHeatTemplate(heatTemplate);
        return networkResource;
    }

    protected CollectionNetworkResourceCustomization createNetworkCollection(IEntityDetails cnrEntity,
            ToscaResourceStructure toscaResourceStructure, Service service) {

        CollectionNetworkResourceCustomization collectionNetworkResourceCustomization =
                new CollectionNetworkResourceCustomization();

        // **** Build Object to populate Collection_Resource table
        CollectionResource collectionResource = new CollectionResource();

        collectionResource.setModelName(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        collectionResource
                .setModelInvariantUUID(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        collectionResource.setModelUUID(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        collectionResource.setModelVersion(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        collectionResource.setDescription(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        collectionResource.setToscaNodeType(cnrEntity.getToscaType());

        toscaResourceStructure.setCatalogCollectionResource(collectionResource);

        // **** Build object to populate Collection_Resource_Customization table
        NetworkCollectionResourceCustomization ncfc = new NetworkCollectionResourceCustomization();

        ncfc.setFunction(getLeafPropertyValue(cnrEntity, "cr_function"));
        ncfc.setRole(getLeafPropertyValue(cnrEntity, "cr_role"));
        ncfc.setType(getLeafPropertyValue(cnrEntity, "cr_type"));

        ncfc.setModelInstanceName(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        ncfc.setModelCustomizationUUID(
                cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        Set<CollectionNetworkResourceCustomization> networkResourceCustomizationSet = new HashSet<>();
        networkResourceCustomizationSet.add(collectionNetworkResourceCustomization);

        ncfc.setNetworkResourceCustomization(networkResourceCustomizationSet);

        ncfc.setCollectionResource(collectionResource);
        toscaResourceStructure.setCatalogCollectionResourceCustomization(ncfc);

        // *** Build object to populate the Instance_Group table
        List<IEntityDetails> ncEntityList =
                getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder("org.openecomp.groups.NetworkCollection"),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);

        List<NetworkInstanceGroup> networkInstanceGroupList = new ArrayList<>();

        List<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationList =
                new ArrayList<>();

        for (IEntityDetails ncGroupEntity : ncEntityList) {

            NetworkInstanceGroup networkInstanceGroup = new NetworkInstanceGroup();
            Metadata instanceMetadata = ncGroupEntity.getMetadata();
            networkInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            networkInstanceGroup
                    .setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            networkInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            networkInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
            networkInstanceGroup.setToscaNodeType(ncGroupEntity.getToscaType());
            networkInstanceGroup.setRole(SubType.SUB_INTERFACE.toString()); // Set
            // Role
            networkInstanceGroup.setType(InstanceGroupType.L3_NETWORK); // Set
            // type
            networkInstanceGroup.setCollectionResource(collectionResource);

            // ****Build object to populate
            // Collection_Resource_Instance_Group_Customization table
            CollectionResourceInstanceGroupCustomization crInstanceGroupCustomization =
                    new CollectionResourceInstanceGroupCustomization();
            crInstanceGroupCustomization.setInstanceGroup(networkInstanceGroup);
            crInstanceGroupCustomization.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            crInstanceGroupCustomization.setModelCustomizationUUID(
                    cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

            // Loop through the template policy to find the subinterface_network_quantity property name. Then extract
            // the value for it.
            List<IEntityDetails> policyEntityList = getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder("org.openecomp.policies.scaling.Fixed"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), true);

            if (policyEntityList != null) {
                for (IEntityDetails policyEntity : policyEntityList) {
                    for (String policyNetworkCollection : policyEntity.getTargets()) {

                        if (policyNetworkCollection.equalsIgnoreCase(ncGroupEntity.getName())) {

                            Map<String, Property> propMap = policyEntity.getProperties();

                            if (propMap.get("quantity") != null) {

                                String quantity = getLeafPropertyValue(cnrEntity,
                                        getPropertyInput(propMap.get("quantity").toString()));

                                if (quantity != null) {
                                    crInstanceGroupCustomization
                                            .setSubInterfaceNetworkQuantity(Integer.parseInt(quantity));
                                }

                            }

                        }
                    }
                }
            }

            crInstanceGroupCustomization.setDescription(
                    getLeafPropertyValue(cnrEntity, instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
                            + "_network_collection_description"));

            crInstanceGroupCustomization.setFunction(getLeafPropertyValue(cnrEntity,
                    instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME) + "_network_collection_function"));

            crInstanceGroupCustomization.setCollectionResourceCust(ncfc);
            collectionResourceInstanceGroupCustomizationList.add(crInstanceGroupCustomization);

            networkInstanceGroup
                    .setCollectionInstanceGroupCustomizations(collectionResourceInstanceGroupCustomizationList);

            networkInstanceGroupList.add(networkInstanceGroup);


            toscaResourceStructure.setCatalogNetworkInstanceGroup(networkInstanceGroupList);

            List<IEntityDetails> networkEntityList =
                    getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(SdcTypes.VL),
                            TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                    cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                            false);

            List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizationList = new ArrayList<>();

            // *****Build object to populate the NetworkResource table
            NetworkResource networkResource = new NetworkResource();

            for (IEntityDetails networkEntity : networkEntityList) {

                String providerNetwork = getLeafPropertyValue(networkEntity,
                        SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

                if ("true".equalsIgnoreCase(providerNetwork)) {
                    networkResource.setNeutronNetworkType(PROVIDER);
                } else {
                    networkResource.setNeutronNetworkType(BASIC);
                }

                networkResource.setModelName(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                networkResource.setModelInvariantUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                networkResource.setModelUUID(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                networkResource
                        .setModelVersion(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));

                networkResource.setAicVersionMax(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(
                                networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                if (tempNetworkLookUp != null) {

                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    networkResource.setHeatTemplate(heatTemplate);

                    networkResource.setAicVersionMin(tempNetworkLookUp.getAicVersionMin());

                }

                networkResource.setToscaNodeType(networkEntity.getToscaType());
                networkResource.setDescription(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                networkResource.setOrchestrationMode(HEAT);

                // Build object to populate the
                // Collection_Network_Resource_Customization table
                for (IEntityDetails networkMemberEntity : ncGroupEntity.getMemberNodes()) {
                    collectionNetworkResourceCustomization.setModelInstanceName(networkMemberEntity.getName());
                }

                collectionNetworkResourceCustomization.setModelCustomizationUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                collectionNetworkResourceCustomization.setNetworkTechnology(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
                collectionNetworkResourceCustomization.setNetworkType(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
                collectionNetworkResourceCustomization.setNetworkRole(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
                collectionNetworkResourceCustomization.setNetworkScope(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
                collectionNetworkResourceCustomization.setInstanceGroup(networkInstanceGroup);
                collectionNetworkResourceCustomization.setNetworkResource(networkResource);
                collectionNetworkResourceCustomization.setNetworkResourceCustomization(ncfc);

                collectionNetworkResourceCustomizationList.add(collectionNetworkResourceCustomization);
            }

        }

        return collectionNetworkResourceCustomization;
    }

    protected VnfcInstanceGroupCustomization createVNFCInstanceGroup(IEntityDetails vfcInstanceEntity,
            IEntityDetails vfEntityDetails, VnfResourceCustomization vnfResourceCustomization,
            ToscaResourceStructure toscaResourceStructure, Set<VnfcCustomization> existingVnfcGroupSet) {

        Metadata instanceMetadata = vfcInstanceEntity.getMetadata();

        InstanceGroup existingInstanceGroup =
                instanceGroupRepo.findByModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

        VFCInstanceGroup vfcInstanceGroup;

        if (existingInstanceGroup == null) {
            // Populate InstanceGroup
            vfcInstanceGroup = new VFCInstanceGroup();
            vfcInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            vfcInstanceGroup
                    .setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            vfcInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            vfcInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
            vfcInstanceGroup.setToscaNodeType(vfcInstanceEntity.getToscaType());
            vfcInstanceGroup.setRole("SUB-INTERFACE"); // Set Role
            vfcInstanceGroup.setType(InstanceGroupType.VNFC); // Set type
        } else {
            vfcInstanceGroup = (VFCInstanceGroup) existingInstanceGroup;
        }

        // Populate VNFCInstanceGroupCustomization
        VnfcInstanceGroupCustomization vfcInstanceGroupCustom = new VnfcInstanceGroupCustomization();

        vfcInstanceGroupCustom.setVnfResourceCust(vnfResourceCustomization);
        vnfResourceCustomization.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);
        vfcInstanceGroup.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setDescription(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        String getInputName = null;

        Map<String, Property> groupProperties = vfcInstanceEntity.getProperties();

        for (String key : groupProperties.keySet()) {
            Property property = groupProperties.get(key);

            String vfcName = property.getName();

            if (vfcName != null) {
                if (vfcName.equals("vfc_instance_group_function")) {

                    String vfcValue = property.getValue().toString();
                    int getInputIndex = vfcValue.indexOf("{get_input=");
                    if (getInputIndex > -1) {
                        getInputName = vfcValue.substring(getInputIndex + 11, vfcValue.length() - 1);
                    }

                }
            }

        }

        List<IEntityDetails> serviceEntityList = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.VF)
                        .customizationUUID(vnfResourceCustomization.getModelCustomizationUUID()),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (serviceEntityList != null && !serviceEntityList.isEmpty()) {
            vfcInstanceGroupCustom.setFunction(getLeafPropertyValue(serviceEntityList.get(0), getInputName));
        }

        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);

        List<Input> inputs = vfEntityDetails.getInputs();

        createVFCInstanceGroupMembers(vfcInstanceGroupCustom, vfcInstanceEntity, inputs, existingVnfcGroupSet);

        return vfcInstanceGroupCustom;
    }

    private void createVFCInstanceGroupMembers(VnfcInstanceGroupCustomization vfcInstanceGroupCustom,
            IEntityDetails vfcModuleEntity, List<Input> inputList, Set<VnfcCustomization> existingVnfcGroupSet) {
        List<IEntityDetails> members = vfcModuleEntity.getMemberNodes();
        if (!CollectionUtils.isEmpty(members)) {
            for (IEntityDetails vfcEntity : members) {

                VnfcCustomization existingVfcGroup = findExistingVfc(existingVnfcGroupSet,
                        vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                if (existingVfcGroup == null) {
                    VnfcCustomization vnfcCustomization = new VnfcCustomization();

                    Metadata metadata = vfcEntity.getMetadata();
                    vnfcCustomization.setModelCustomizationUUID(
                            metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                    vnfcCustomization.setModelInstanceName(vfcEntity.getName());
                    vnfcCustomization.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                    vnfcCustomization
                            .setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                    vnfcCustomization.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                    vnfcCustomization.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                    vnfcCustomization.setToscaNodeType(testNull(vfcEntity.getToscaType()));
                    vnfcCustomization
                            .setDescription(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                    vnfcCustomization.setResourceInput(getVnfcResourceInput(vfcEntity, inputList));
                    vnfcCustomization.setVnfcInstanceGroupCustomization(vfcInstanceGroupCustom);
                    List<VnfcCustomization> vnfcCustomizations = vfcInstanceGroupCustom.getVnfcCustomizations();

                    if (vnfcCustomizations == null) {
                        vnfcCustomizations = new ArrayList<>();
                        vfcInstanceGroupCustom.setVnfcCustomizations(vnfcCustomizations);
                    }
                    vnfcCustomizations.add(vnfcCustomization);

                    existingVnfcGroupSet.add(vnfcCustomization);
                }
            }
        }
    }

    public String getVnfcResourceInput(IEntityDetails vfcEntity, List<Input> inputList) {
        Map<String, String> resouceRequest = new HashMap<>();
        Map<String, Property> vfcTemplateProperties = vfcEntity.getProperties();
        for (String key : vfcTemplateProperties.keySet()) {
            Property property = vfcTemplateProperties.get(key);
            String resourceValue = getValue(property.getValue(), inputList);
            resouceRequest.put(key, resourceValue);
        }

        String resourceCustomizationUuid =
                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(resouceRequest);
            jsonStr = jsonStr.replace("\"", "\\\"");
            logger.debug("vfcResource request for resource customization id {}: {}", resourceCustomizationUuid,
                    jsonStr);
        } catch (JsonProcessingException e) {
            logger.debug("Json Exception: {}", e.getMessage());
            logger.error("Exception occurred", e);
        }

        return jsonStr;
    }

    protected VfModuleCustomization createVFModuleResource(IEntityDetails vfModuleEntityDetails,
            ToscaResourceStructure toscaResourceStructure, VfResourceStructure vfResourceStructure,
            IVfModuleData vfModuleData, VnfResourceCustomization vnfResource, Service service,
            Set<CvnfcCustomization> existingCvnfcSet, Set<VnfcCustomization> existingVnfcSet,
            List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom) {

        VfModuleCustomization vfModuleCustomization =
                findExistingVfModuleCustomization(vnfResource, vfModuleData.getVfModuleModelCustomizationUUID());

        if (vfModuleCustomization == null) {

            VfModule vfModule = findExistingVfModule(vnfResource,
                    vfModuleEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));

            Metadata vfMetadata = vfModuleEntityDetails.getMetadata();
            if (vfModule == null)
                vfModule = createVfModule(vfModuleEntityDetails, toscaResourceStructure, vfModuleData, vfMetadata);

            vfModuleCustomization =
                    createVfModuleCustomization(vfModuleEntityDetails, toscaResourceStructure, vfModule, vfModuleData);
            vfModuleCustomization.setVnfCustomization(vnfResource);
            setHeatInformationForVfModule(toscaResourceStructure, vfResourceStructure, vfModule, vfModuleCustomization,
                    vfMetadata);
            vfModuleCustomization.setVfModule(vfModule);
            vfModule.getVfModuleCustomization().add(vfModuleCustomization);
            vnfResource.getVfModuleCustomizations().add(vfModuleCustomization);
        } else {
            vfResourceStructure.setAlreadyDeployed(true);
        }

        // ******************************************************************************************************************
        // * Extract VFC's and CVFC's then add them to VFModule
        // ******************************************************************************************************************

        List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomizations = new ArrayList<>();
        List<CvnfcCustomization> cvnfcCustomizations = new ArrayList<>();
        Set<VnfcCustomization> vnfcCustomizations = new HashSet<>();

        // Only set the CVNFC if this vfModule group is a member of it.

        List<IEntityDetails> groupMembers = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder("org.openecomp.groups.VfModule")
                        .uUID(vfModuleCustomization.getVfModule().getModelUUID()),
                TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

        String vfModuleMemberName = null;

        // Extract CVFC lists
        List<IEntityDetails> cvnfcEntityList = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.CVFC), TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);


        for (IEntityDetails cvfcEntity : cvnfcEntityList) {
            boolean cvnfcVfModuleNameMatch = false;

            for (IEntityDetails entity : groupMembers) {

                List<IEntityDetails> groupMembersNodes = entity.getMemberNodes();
                for (IEntityDetails groupMember : groupMembersNodes) {

                    vfModuleMemberName = groupMember.getName();

                    if (vfModuleMemberName.equalsIgnoreCase(cvfcEntity.getName())) {
                        cvnfcVfModuleNameMatch = true;
                        break;
                    }

                }
            }


            if (vfModuleMemberName != null && cvnfcVfModuleNameMatch) {

                // Extract associated VFC - Should always be just one
                List<IEntityDetails> vfcEntityList = getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder(SdcTypes.VFC),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CVFC).customizationUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);


                for (IEntityDetails vfcEntity : vfcEntityList) {

                    VnfcCustomization vnfcCustomization = new VnfcCustomization();
                    VnfcCustomization existingVnfcCustomization = null;

                    existingVnfcCustomization = findExistingVfc(existingVnfcSet,
                            vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                    if (existingVnfcCustomization == null) {
                        vnfcCustomization = new VnfcCustomization();
                    } else {
                        vnfcCustomization = existingVnfcCustomization;
                    }

                    // Only Add Abstract VNFC's to our DB, ignore all others
                    if (existingVnfcCustomization == null && vfcEntity.getMetadata()
                            .getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY).equalsIgnoreCase("Abstract")) {

                        vnfcCustomization.setModelCustomizationUUID(
                                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        vnfcCustomization.setModelInstanceName(vfcEntity.getName());
                        vnfcCustomization.setModelInvariantUUID(
                                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        vnfcCustomization
                                .setModelName(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        vnfcCustomization
                                .setModelUUID(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        vnfcCustomization.setModelVersion(
                                testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        vnfcCustomization.setDescription(
                                testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        vnfcCustomization.setToscaNodeType(testNull(vfcEntity.getToscaType()));

                        vnfcCustomizations.add(vnfcCustomization);
                        existingVnfcSet.add(vnfcCustomization);
                    }

                    // This check is needed incase the VFC subcategory is
                    // something other than Abstract. In that case we want to
                    // skip adding that record to our DB.
                    if (vnfcCustomization.getModelCustomizationUUID() != null) {
                        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
                        cvnfcCustomization.setModelCustomizationUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        cvnfcCustomization.setModelInstanceName(cvfcEntity.getName());
                        cvnfcCustomization.setModelInvariantUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        cvnfcCustomization
                                .setModelName(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        cvnfcCustomization
                                .setModelUUID(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        cvnfcCustomization.setModelVersion(
                                testNull(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        cvnfcCustomization.setDescription(testNull(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        cvnfcCustomization.setToscaNodeType(testNull(cvfcEntity.getToscaType()));

                        if (existingVnfcCustomization != null) {
                            cvnfcCustomization.setVnfcCustomization(existingVnfcCustomization);
                        } else {
                            cvnfcCustomization.setVnfcCustomization(vnfcCustomization);
                        }

                        cvnfcCustomization.setNfcFunction(getLeafPropertyValue(cvfcEntity, "nfc_function"));
                        cvnfcCustomization.setNfcNamingCode(getLeafPropertyValue(cvfcEntity, "nfc_naming_code"));

                        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

                        // *****************************************************************************************************************************************
                        // * Extract Fabric Configuration
                        // *****************************************************************************************************************************************

                        List<IEntityDetails> fabricEntityList =
                                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(SdcTypes.CONFIGURATION),
                                        TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

                        for (IEntityDetails fabricEntity : fabricEntityList) {

                            List<RequirementAssignment> requirements = fabricEntity.getRequirements();

                            for (RequirementAssignment requirement : requirements) {

                                if (requirement.getNodeTemplateName().equals(cvfcEntity.getName())) {

                                    ConfigurationResource fabricConfig = null;

                                    ConfigurationResource existingConfig = findExistingConfiguration(
                                            existingCvnfcConfigurationCustom,
                                            fabricEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                                    if (existingConfig == null) {

                                        fabricConfig = createFabricConfiguration(fabricEntity, toscaResourceStructure);

                                    } else {
                                        fabricConfig = existingConfig;
                                    }

                                    CvnfcConfigurationCustomization cvnfcConfigurationCustomization =
                                            createCvnfcConfigurationCustomization(fabricEntity, toscaResourceStructure,
                                                    vnfResource, vfModuleCustomization, cvnfcCustomization,
                                                    fabricConfig, vfModuleMemberName);

                                    cvnfcConfigurationCustomizations.add(cvnfcConfigurationCustomization);

                                    existingCvnfcConfigurationCustom.add(cvnfcConfigurationCustomization);

                                }
                            }

                        }
                        cvnfcCustomization.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizations);
                        cvnfcCustomizations.add(cvnfcCustomization);
                        existingCvnfcSet.add(cvnfcCustomization);

                    }

                }

            }
        }
        vfModuleCustomization.setCvnfcCustomization(cvnfcCustomizations);

        return vfModuleCustomization;
    }

    protected CvnfcConfigurationCustomization createCvnfcConfigurationCustomization(IEntityDetails fabricEntity,
            ToscaResourceStructure toscaResourceStruct, VnfResourceCustomization vnfResource,
            VfModuleCustomization vfModuleCustomization, CvnfcCustomization cvnfcCustomization,
            ConfigurationResource configResource, String vfModuleMemberName) {

        Metadata fabricMetadata = fabricEntity.getMetadata();

        CvnfcConfigurationCustomization cvnfcConfigurationCustomization = new CvnfcConfigurationCustomization();

        cvnfcConfigurationCustomization.setConfigurationResource(configResource);

        cvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);

        cvnfcConfigurationCustomization
                .setModelCustomizationUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        cvnfcConfigurationCustomization.setModelInstanceName(fabricEntity.getName());

        cvnfcConfigurationCustomization.setConfigurationFunction(getLeafPropertyValue(fabricEntity, "function"));
        cvnfcConfigurationCustomization.setConfigurationRole(getLeafPropertyValue(fabricEntity, "role"));
        cvnfcConfigurationCustomization.setConfigurationType(getLeafPropertyValue(fabricEntity, "type"));

        return cvnfcConfigurationCustomization;
    }

    protected ConfigurationResource findExistingConfiguration(
            List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom, String modelUUID) {
        ConfigurationResource configResource = null;
        for (CvnfcConfigurationCustomization cvnfcConfigCustom : existingCvnfcConfigurationCustom) {
            if (cvnfcConfigCustom != null) {
                if (cvnfcConfigCustom.getConfigurationResource().getModelUUID().equals(modelUUID)) {
                    configResource = cvnfcConfigCustom.getConfigurationResource();
                }
            }
        }

        return configResource;
    }

    protected ConfigurationResource findExistingConfiguration(Service service, String modelUUID,
            List<ConfigurationResourceCustomization> configurationResourceList) {
        ConfigurationResource configResource = null;
        for (ConfigurationResourceCustomization configurationResourceCustom : configurationResourceList) {
            if (configurationResourceCustom.getConfigurationResource() != null
                    && configurationResourceCustom.getConfigurationResource().getModelUUID().equals(modelUUID)) {
                configResource = configurationResourceCustom.getConfigurationResource();
            }
        }

        return configResource;
    }

    protected VfModuleCustomization findExistingVfModuleCustomization(VnfResourceCustomization vnfResource,
            String vfModuleModelCustomizationUUID) {
        VfModuleCustomization vfModuleCustomization = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getModelCustomizationUUID().equalsIgnoreCase(vfModuleModelCustomizationUUID)) {
                vfModuleCustomization = vfModuleCustom;
            }
        }
        return vfModuleCustomization;
    }

    protected VfModule findExistingVfModule(VnfResourceCustomization vnfResource, String modelUUID) {
        VfModule vfModule = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getVfModule() != null && vfModuleCustom.getVfModule().getModelUUID().equals(modelUUID)) {
                vfModule = vfModuleCustom.getVfModule();
            }
        }
        if (vfModule == null)
            vfModule = vfModuleRepo.findByModelUUID(modelUUID);

        return vfModule;
    }

    protected VfModuleCustomization createVfModuleCustomization(IEntityDetails vfModuleEntityDetails,
            ToscaResourceStructure toscaResourceStructure, VfModule vfModule, IVfModuleData vfModuleData) {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();

        vfModuleCustomization.setModelCustomizationUUID(vfModuleData.getVfModuleModelCustomizationUUID());

        vfModuleCustomization.setVfModule(vfModule);

        String initialCount = getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT);


        if (initialCount != null && initialCount.length() > 0) {
            vfModuleCustomization.setInitialCount(Integer.valueOf(initialCount));
        }

        String availabilityZoneCount =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);

        if (availabilityZoneCount != null && availabilityZoneCount.length() > 0) {
            vfModuleCustomization.setAvailabilityZoneCount(Integer.valueOf(availabilityZoneCount));
        }

        vfModuleCustomization
                .setLabel(getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_VFMODULELABEL));

        String maxInstances =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_MAXVFMODULEINSTANCES);

        if (maxInstances != null && maxInstances.length() > 0) {
            vfModuleCustomization.setMaxInstances(Integer.valueOf(maxInstances));
        }

        String minInstances =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_MINVFMODULEINSTANCES);

        if (minInstances != null && minInstances.length() > 0) {
            vfModuleCustomization.setMinInstances(Integer.valueOf(minInstances));
        }

        String skipPostInstConfText = getLeafPropertyValue(vfModuleEntityDetails, SKIP_POST_INST_CONF);

        if (skipPostInstConfText != null) {
            vfModuleCustomization.setSkipPostInstConf(
                    Boolean.parseBoolean(getLeafPropertyValue(vfModuleEntityDetails, SKIP_POST_INST_CONF)));
        }

        return vfModuleCustomization;
    }

    protected VfModule createVfModule(IEntityDetails groupEntityDetails, ToscaResourceStructure toscaResourceStructure,
            IVfModuleData vfModuleData, Metadata vfMetadata) {
        VfModule vfModule = new VfModule();
        String vfModuleModelUUID = vfModuleData.getVfModuleModelUUID();

        if (vfModuleModelUUID == null) {

            vfModuleModelUUID = testNull(
                    groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));

        } else if (vfModuleModelUUID.indexOf('.') > -1) {
            vfModuleModelUUID = vfModuleModelUUID.substring(0, vfModuleModelUUID.indexOf('.'));
        }

        vfModule.setModelInvariantUUID(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID));
        vfModule.setModelName(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME));
        vfModule.setModelUUID(vfModuleModelUUID);
        vfModule.setModelVersion(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION));
        vfModule.setDescription(groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        String vfModuleType = getLeafPropertyValue(groupEntityDetails, SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE);

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
                        .equalsIgnoreCase(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)))
                .findFirst();

        if (matchingObject.isPresent()) {
            List<HeatFiles> heatFilesList = new ArrayList<>();
            List<HeatTemplate> volumeHeatChildTemplates = new ArrayList<>();
            List<HeatTemplate> heatChildTemplates = new ArrayList<>();
            HeatTemplate parentHeatTemplate = new HeatTemplate();
            String parentArtifactType = null;
            Set<String> artifacts = new HashSet<>(matchingObject.get().getVfModuleMetadata().getArtifacts());
            for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

                List<HeatTemplate> childNestedHeatTemplates = new ArrayList<>();

                if (artifacts.contains(vfModuleArtifact.getArtifactInfo().getArtifactUUID())) {
                    checkVfModuleArtifactType(vfModule, vfModuleCustomization, heatFilesList, vfModuleArtifact,
                            childNestedHeatTemplates, parentHeatTemplate, vfResourceStructure);
                }

                if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
                    parentArtifactType = identifyParentOfNestedTemplate(matchingObject.get(), vfModuleArtifact);

                    if (!childNestedHeatTemplates.isEmpty()) {

                        if (parentArtifactType != null
                                && parentArtifactType.equalsIgnoreCase(ASDCConfiguration.HEAT_VOL)) {
                            volumeHeatChildTemplates.add(childNestedHeatTemplates.get(0));
                        } else {
                            heatChildTemplates.add(childNestedHeatTemplates.get(0));
                        }
                    }
                }

            }
            if (!heatFilesList.isEmpty()) {
                vfModule.setHeatFiles(heatFilesList);
            }


            // Set all Child Templates related to HEAT_VOLUME
            if (!volumeHeatChildTemplates.isEmpty()) {
                if (vfModule.getVolumeHeatTemplate() != null) {
                    vfModule.getVolumeHeatTemplate().setChildTemplates(volumeHeatChildTemplates);
                } else {
                    logger.debug("VolumeHeatTemplate not set in setHeatInformationForVfModule()");
                }
            }

            // Set all Child Templates related to HEAT
            if (!heatChildTemplates.isEmpty()) {
                if (vfModule.getModuleHeatTemplate() != null) {
                    vfModule.getModuleHeatTemplate().setChildTemplates(heatChildTemplates);
                } else {
                    logger.debug("ModuleHeatTemplate not set in setHeatInformationForVfModule()");
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
            VfModuleArtifact volVfModuleArtifact =
                    this.getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
            vfModuleCustomization.setVolumeHeatEnv(volVfModuleArtifact.getHeatEnvironment());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
            if (vfModuleArtifact.getHeatEnvironment() != null) {
                if (vfModuleArtifact.getHeatEnvironment().getName().contains("volume")) {
                    vfModuleCustomization.setVolumeHeatEnv(vfModuleArtifact.getHeatEnvironment());
                } else {
                    vfModuleCustomization.setHeatEnvironment(vfModuleArtifact.getHeatEnvironment());
                }
            }
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ARTIFACT)) {
            heatFilesList.add(vfModuleArtifact.getHeatFiles());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
            nestedHeatTemplates.add(vfModuleArtifact.getHeatTemplate());
            vfModuleArtifact.incrementDeployedInDB();
        }
    }

    protected VnfResourceCustomization createVnfResource(IEntityDetails entityDetails,
            ToscaResourceStructure toscaResourceStructure, Service service) throws ArtifactInstallerException {
        VnfResourceCustomization vnfResourceCustomization = null;
        if (vnfResourceCustomization == null) {

            VnfResource vnfResource = findExistingVnfResource(service,
                    entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (vnfResource == null) {
                vnfResource = createVnfResource(entityDetails);
            }

            vnfResourceCustomization =
                    createVnfResourceCustomization(entityDetails, toscaResourceStructure, vnfResource);
            vnfResourceCustomization.setVnfResources(vnfResource);
            vnfResourceCustomization.setService(service);

            // setting resource input for vnf customization
            vnfResourceCustomization.setResourceInput(
                    getResourceInput(toscaResourceStructure, vnfResourceCustomization.getModelCustomizationUUID()));

        }
        return vnfResourceCustomization;
    }

    protected VnfResource findExistingVnfResource(Service service, String modelUUID) {
        VnfResource vnfResource = null;
        for (VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()) {
            if (vnfResourceCustom.getVnfResources() != null
                    && vnfResourceCustom.getVnfResources().getModelUUID().equals(modelUUID)) {
                vnfResource = vnfResourceCustom.getVnfResources();
            }
        }
        if (vnfResource == null)
            vnfResource = vnfRepo.findResourceByModelUUID(modelUUID);

        return vnfResource;
    }

    protected VnfResourceCustomization createVnfResourceCustomization(IEntityDetails entityDetails,
            ToscaResourceStructure toscaResourceStructure, VnfResource vnfResource) {
        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID(
                entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        vnfResourceCustomization.setModelInstanceName(entityDetails.getName());
        vnfResourceCustomization
                .setNfFunction(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
        vnfResourceCustomization.setNfNamingCode(getLeafPropertyValue(entityDetails, "nf_naming_code"));
        vnfResourceCustomization.setNfRole(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFROLE));
        vnfResourceCustomization.setNfType(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFTYPE));

        vnfResourceCustomization.setMultiStageDesign(getLeafPropertyValue(entityDetails, MULTI_STAGE_DESIGN));
        vnfResourceCustomization.setBlueprintName(getLeafPropertyValue(entityDetails, SDNC_MODEL_NAME));
        vnfResourceCustomization.setBlueprintVersion(getLeafPropertyValue(entityDetails, SDNC_MODEL_VERSION));

        String skipPostInstConfText = getLeafPropertyValue(entityDetails, SKIP_POST_INST_CONF);

        if (skipPostInstConfText != null) {
            vnfResourceCustomization.setSkipPostInstConf(
                    Boolean.parseBoolean(getLeafPropertyValue(entityDetails, SKIP_POST_INST_CONF)));
        }

        vnfResourceCustomization.setControllerActor(getLeafPropertyValue(entityDetails, CONTROLLER_ACTOR));
        vnfResourceCustomization.setVnfResources(vnfResource);
        vnfResourceCustomization.setAvailabilityZoneMaxCount(Integer.getInteger(
                entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT)));

        List<CapabilityAssignment> capAssignList = entityDetails.getCapabilities();

        if (capAssignList != null) {

            for (CapabilityAssignment capAssign : capAssignList) {


                if (capAssign != null) {

                    String capabilityName = capAssign.getName();

                    if (capabilityName.equalsIgnoreCase(SCALABLE)) {
                        vnfResourceCustomization.setMinInstances(Integer.getInteger(
                                getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                        vnfResourceCustomization.setMaxInstances(Integer.getInteger(
                                getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
                    }
                }

            }
        }

        if (vnfResourceCustomization.getMinInstances() == null && vnfResourceCustomization.getMaxInstances() == null) {
            vnfResourceCustomization.setMinInstances(Integer
                    .getInteger(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
            vnfResourceCustomization.setMaxInstances(Integer
                    .getInteger(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
        }

        toscaResourceStructure.setCatalogVnfResourceCustomization(vnfResourceCustomization);

        return vnfResourceCustomization;
    }

    protected VnfResource createVnfResource(IEntityDetails entityDetails) {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelInvariantUUID(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        vnfResource.setModelName(testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        vnfResource.setModelUUID(testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));

        vnfResource.setModelVersion(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        vnfResource.setDescription(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        vnfResource.setOrchestrationMode(HEAT);
        vnfResource.setToscaNodeType(testNull(entityDetails.getToscaType()));
        vnfResource.setAicVersionMax(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
        vnfResource.setAicVersionMin(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
        vnfResource.setCategory(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        vnfResource.setSubCategory(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));

        return vnfResource;
    }

    protected AllottedResourceCustomization createAllottedResource(IEntityDetails arEntity,
            ToscaResourceStructure toscaResourceStructure, Service service) {
        AllottedResourceCustomization allottedResourceCustomization =
                allottedCustomizationRepo.findOneByModelCustomizationUUID(
                        arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        if (allottedResourceCustomization == null) {
            AllottedResource allottedResource = findExistingAllottedResource(service,
                    arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (allottedResource == null)
                allottedResource = createAR(arEntity);

            toscaResourceStructure.setAllottedResource(allottedResource);
            allottedResourceCustomization = createAllottedResourceCustomization(arEntity, toscaResourceStructure);
            allottedResourceCustomization.setAllottedResource(allottedResource);
            allottedResource.getAllotedResourceCustomization().add(allottedResourceCustomization);
        }
        return allottedResourceCustomization;
    }

    protected AllottedResource findExistingAllottedResource(Service service, String modelUUID) {
        AllottedResource allottedResource = null;
        for (AllottedResourceCustomization allottedResourceCustom : service.getAllottedCustomizations()) {
            if (allottedResourceCustom.getAllottedResource() != null
                    && allottedResourceCustom.getAllottedResource().getModelUUID().equals(modelUUID)) {
                allottedResource = allottedResourceCustom.getAllottedResource();
            }
        }
        if (allottedResource == null)
            allottedResource = allottedRepo.findResourceByModelUUID(modelUUID);

        return allottedResource;
    }

    protected AllottedResourceCustomization createAllottedResourceCustomization(IEntityDetails arEntity,
            ToscaResourceStructure toscaResourceStructure) {
        AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
        allottedResourceCustomization.setModelCustomizationUUID(
                testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        allottedResourceCustomization.setModelInstanceName(arEntity.getName());

        allottedResourceCustomization
                .setNfFunction(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
        allottedResourceCustomization.setNfNamingCode(getLeafPropertyValue(arEntity, "nf_naming_code"));
        allottedResourceCustomization.setNfRole(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFROLE));
        allottedResourceCustomization.setNfType(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFTYPE));

        EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.VFC).build();

        TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.VF)
                .customizationUUID(allottedResourceCustomization.getModelCustomizationUUID()).build();

        List<IEntityDetails> vfcEntities =
                toscaResourceStructure.getSdcCsarHelper().getEntity(entityQuery, topologyTemplateQuery, false);


        if (vfcEntities != null) {
            for (IEntityDetails vfcEntity : vfcEntities) {

                allottedResourceCustomization
                        .setProvidingServiceModelUUID(getLeafPropertyValue(vfcEntity, "providing_service_uuid"));
                allottedResourceCustomization.setProvidingServiceModelInvariantUUID(
                        getLeafPropertyValue(vfcEntity, "providing_service_invariant_uuid"));
                allottedResourceCustomization
                        .setProvidingServiceModelName(getLeafPropertyValue(vfcEntity, "providing_service_name"));
            }
        }

        List<CapabilityAssignment> capAssignmentList = arEntity.getCapabilities();

        if (capAssignmentList != null) {

            for (CapabilityAssignment arCapability : capAssignmentList) {

                if (arCapability != null) {

                    String capabilityName = arCapability.getName();

                    if (capabilityName.equals(SCALABLE)) {

                        allottedResourceCustomization
                                .setMinInstances(Integer.getInteger(getCapabilityLeafPropertyValue(arCapability,
                                        SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                        allottedResourceCustomization
                                .setMinInstances(Integer.getInteger(getCapabilityLeafPropertyValue(arCapability,
                                        SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));

                    }
                }

            }
        }

        return allottedResourceCustomization;
    }

    protected AllottedResource createAR(IEntityDetails arEntity) {
        AllottedResource allottedResource = new AllottedResource();
        allottedResource.setModelUUID(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        allottedResource.setModelInvariantUUID(
                testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        allottedResource.setModelName(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        allottedResource
                .setModelVersion(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        allottedResource.setToscaNodeType(testNull(arEntity.getToscaType()));
        allottedResource
                .setSubcategory(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        allottedResource.setDescription(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        return allottedResource;
    }

    protected Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {
        // Scan the payload downloadResult and extract the HeatTemplate
        // parameters
        YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
        return yamlEditor.getParameterList(artifactUUID);
    }

    protected String testNull(Object object) {

        if (object == null) {
            return null;
        } else if ("NULL".equals(object)) {
            return null;
        } else if (object instanceof Integer) {
            return object.toString();
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return "Type not recognized";
        }
    }

    protected static String identifyParentOfNestedTemplate(VfModuleStructure vfModuleStructure,
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

    protected static String createVNFName(VfResourceStructure vfResourceStructure) {

        return vfResourceStructure.getNotification().getServiceName() + "/"
                + vfResourceStructure.getResourceInstance().getResourceInstanceName();
    }

    protected static String createVfModuleName(VfModuleStructure vfModuleStructure) {

        return createVNFName(vfModuleStructure.getParentVfResource()) + "::"
                + vfModuleStructure.getVfModuleMetadata().getVfModuleModelName();
    }

    public List<IEntityDetails> getEntityDetails(ToscaResourceStructure toscaResourceStruct,
            EntityQueryBuilder entityType, TopologyTemplateQueryBuilder topologyTemplateBuilder, boolean nestedSearch) {

        EntityQuery entityQuery = entityType.build();
        TopologyTemplateQuery topologyTemplateQuery = topologyTemplateBuilder.build();
        List<IEntityDetails> entityDetails =
                toscaResourceStruct.getSdcCsarHelper().getEntity(entityQuery, topologyTemplateQuery, nestedSearch);

        return entityDetails;

    }

    public String getLeafPropertyValue(IEntityDetails entityDetails, String propName) {

        Property leafProperty = entityDetails.getProperties().get(propName);

        if (leafProperty != null && leafProperty.getValue() != null) {
            return leafProperty.getValue().toString();
        }

        return null;
    }

    protected String getCapabilityLeafPropertyValue(CapabilityAssignment capAssign, String propName) {

        Property leafProperty = capAssign.getProperties().get(propName);

        if (leafProperty != null && leafProperty.getValue() != null) {
            return leafProperty.getValue().toString();
        }

        return null;
    }

    protected String getPropertyInput(String propertyName) {

        String inputName = new String();

        if (propertyName != null) {
            int getInputIndex = propertyName.indexOf("{get_input=");
            int getClosingIndex = propertyName.indexOf("}");
            if (getInputIndex > -1) {
                inputName = propertyName.substring(getInputIndex + 11, getClosingIndex);
            }
        }

        return inputName;
    }

    // this method add provided vnfCustomization to service with
    // existing customization available in db.
    private void addVnfCustomization(Service service, VnfResourceCustomization vnfResourceCustomization) {
        List<Service> services = serviceRepo.findByModelUUID(service.getModelUUID());
        if (!services.isEmpty()) {
            // service exist in db
            Service existingService = services.get(0);
            List<VnfResourceCustomization> existingVnfCustomizations = existingService.getVnfCustomizations();
            if (existingService != null) {
                // it is duplicating entries, so added a check
                for (VnfResourceCustomization existingVnfResourceCustomization : existingVnfCustomizations) {
                    if (!service.getVnfCustomizations().contains(existingVnfResourceCustomization)) {
                        service.getVnfCustomizations().add(existingVnfResourceCustomization);
                    }
                }
            }
        }
        service.getVnfCustomizations().add(vnfResourceCustomization);

    }


    protected static Timestamp getCurrentTimeStamp() {

        return new Timestamp(new Date().getTime());
    }

    private String getServiceInput(ToscaResourceStructure toscaResourceStructure) {
        String serviceInput = null;

        List<Object> serviceInputList;
        ISdcCsarHelper sdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();
        List<Input> serviceInputs = sdcCsarHelper.getServiceInputs();
        if (!serviceInputs.isEmpty()) {
            serviceInputList = new ArrayList<>();
            List<Input> filterList;
            filterList = serviceInputs.stream()
                    .filter(input -> !SKIP_POST_INST_CONF.equals(input.getName())
                            && !CDS_MODEL_NAME.equalsIgnoreCase(input.getName())
                            && !CDS_MODEL_VERSION.equalsIgnoreCase(input.getName())
                            && !CONTROLLER_ACTOR.equalsIgnoreCase(input.getName()))
                    .collect(Collectors.toList());

            filterList.forEach(input -> {
                Map<String, Object> serviceInputMap = new HashMap<>();
                serviceInputMap.put("name", input.getName());
                serviceInputMap.put("type", input.getType());
                serviceInputMap.put("default", input.getDefault() == null ? "" : input.getDefault());
                serviceInputMap.put("required", input.isRequired());
                serviceInputList.add(serviceInputMap);

            });
            try {
                serviceInput = mapper.writeValueAsString(serviceInputList);
                serviceInput = serviceInput.replace("\"", "\\\"");
            } catch (JsonProcessingException e) {
                logger.error("service input could not be deserialized for service uuid:  "
                        + sdcCsarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            }
        } else {
            logger.debug("serviceInput is null");
        }
        return serviceInput;
    }

    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installNsstService(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct,
            String artifactContent) {
        createToscaCsar(toscaResourceStruct);
        createService(toscaResourceStruct, vfResourceStruct);
        Service service = toscaResourceStruct.getCatalogService();
        createServiceInfo(toscaResourceStruct, service);
        createServiceArtifact(service, vfResourceStruct, artifactContent);
        serviceRepo.save(service);
    }

    private void createServiceArtifact(Service service, VfResourceStructure vfResourceStruct, String artifactContent) {
        if (null == vfResourceStruct) {
            return;
        }
        List<ServiceArtifact> serviceArtifactList = new ArrayList<>();
        ServiceArtifact serviceArtifact;
        List<IArtifactInfo> artifactInfoList = vfResourceStruct.getNotification().getServiceArtifacts().stream()
                .filter(artifact -> artifact.getArtifactType().equalsIgnoreCase("WORKFLOW"))
                .collect(Collectors.toList());
        for (IArtifactInfo artifactInfo : artifactInfoList) {
            serviceArtifact = new ServiceArtifact();
            serviceArtifact.setArtifactUUID(artifactInfo.getArtifactUUID());
            serviceArtifact.setName(artifactInfo.getArtifactName());
            serviceArtifact.setType("OTHER");
            serviceArtifact.setVersion(artifactInfo.getArtifactVersion());
            serviceArtifact.setDescription(artifactInfo.getArtifactDescription());
            serviceArtifact.setChecksum(artifactInfo.getArtifactChecksum());
            serviceArtifact.setContent(artifactContent);
            serviceArtifact.setService(service);
            serviceArtifactList.add(serviceArtifact);
        }
        service.setServiceArtifactList(serviceArtifactList);
    }

    private void createServiceInfo(ToscaResourceStructure toscaResourceStruct, Service service) {
        if (!service.getServiceInfos().isEmpty()) {
            return;
        }

        List<ServiceInfo> serviceInfos = new ArrayList<>();

        ServiceInfo serviceInfo = new ServiceInfo();
        String serviceInput = getServiceInput(toscaResourceStruct);
        serviceInfo.setServiceInput(serviceInput);

        String serviceProperties = getServiceProperties(toscaResourceStruct);
        serviceInfo.setServiceProperties(serviceProperties);
        serviceInfo.setService(service);
        serviceInfos.add(serviceInfo);

        service.setServiceInfos(serviceInfos);
    }

    private String getServiceProperties(ToscaResourceStructure toscaResourceStruct) {
        String propertiesJson = null;
        ISdcCsarHelper helper = toscaResourceStruct.getSdcCsarHelper();
        String typeName = helper.getServiceSubstitutionMappingsTypeName();
        Optional<NodeTemplate> nodeTemplate = helper.getServiceNodeTemplates().stream().findAny();

        if (nodeTemplate.isPresent()) {
            String serviceUUID = nodeTemplate.get().getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
            LinkedHashMap<String, Object> customDef = nodeTemplate.get().getCustomDef();
            List<Object> serviceProperties = getPropertiesFromCustomDef(customDef, typeName);

            try {
                propertiesJson = mapper.writeValueAsString(serviceProperties);
                propertiesJson = propertiesJson.replace("\"", "\\\"");
            } catch (JsonProcessingException e) {
                logger.error("serviceProperties could not be deserialized for service uuid:  " + serviceUUID);
            } catch (Exception ex) {
                logger.error("service properties parsing failed. service uuid:" + serviceUUID);
            }

        } else {
            logger.debug("ServiceNodeTemplates is null");
        }
        return propertiesJson;
    }

    private static List<Object> getPropertiesFromCustomDef(LinkedHashMap<String, Object> customDef,
            final String typeName) {
        Optional<String> machKey =
                customDef.keySet().stream().filter(key -> key.equalsIgnoreCase(typeName)).findFirst();
        Map<String, Object> servicePropertiesMap;
        List<Object> serviceProperties = new ArrayList<>();

        if (machKey.isPresent()) {
            Object obj = customDef.get(machKey.get());
            if (obj instanceof Map) {
                Object properties = ((HashMap) obj).get("properties");
                if (null != properties) {
                    for (Object propertyName : ((Map) properties).keySet()) {
                        if (propertyName.toString().split("_").length >= 2) {
                            continue;
                        }

                        servicePropertiesMap = new HashMap<>();
                        servicePropertiesMap.put("name", propertyName);
                        Object object = ((Map) properties).get(propertyName);
                        for (Object entry : ((Map) object).entrySet()) {
                            servicePropertiesMap.put((String) ((Map.Entry) entry).getKey(),
                                    ((Map.Entry) entry).getValue());
                        }

                        String type = servicePropertiesMap.get("type").toString();
                        if (type.split("\\.").length >= 2) {
                            List<Object> subProperties = getPropertiesFromCustomDef(customDef, type);
                            if (subProperties.size() > 0) {
                                serviceProperties.addAll(subProperties);
                            }
                            continue;
                        }
                        servicePropertiesMap.remove("description");
                        serviceProperties.add(servicePropertiesMap);
                    }
                }
            }
        }
        return serviceProperties;
    }
}
