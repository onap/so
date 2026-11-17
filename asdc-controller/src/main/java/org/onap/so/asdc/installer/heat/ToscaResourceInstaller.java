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


import static org.onap.so.asdc.installer.heat.ToscaEntityHelper.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.api.notification.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ASDCElementInfo;
import org.onap.so.asdc.installer.BigDecimalVersion;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.bpmn.WorkflowResource;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.data.repository.CollectionResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ExternalServiceToInternalServiceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ToscaResourceInstaller {

    protected static final String MSO = "SO";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected ServiceRepository serviceRepo;

    @Autowired
    protected CollectionResourceCustomizationRepository collectionCustomizationRepo;

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
    @Autowired
    protected WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;
    @Autowired
    protected WatchdogServiceModVerIdLookupRepository watchdogModVerIdLookupRepository;

    @Autowired
    protected ExternalServiceToInternalServiceRepository externalServiceToInternalServiceRepository;

    @Autowired
    protected ToscaCsarRepository toscaCsarRepo;

    @Autowired
    protected PnfResourceInstaller pnfResourceInstaller;

    @Autowired
    protected AllottedResourceInstaller allottedResourceInstaller;

    @Autowired
    protected NetworkResourceInstaller networkResourceInstaller;

    @Autowired
    protected ConfigurationInstaller configurationInstaller;

    @Autowired
    protected HeatArtifactInstaller heatArtifactInstaller;

    @Autowired
    protected VnfResourceInstaller vnfResourceInstaller;

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
            pnfResourceInstaller.processPnfResources(toscaResourceStruct, service, resourceStruct);
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
        heatArtifactInstaller.extractHeatInformation(toscaResourceStruct, vfResourceStructure);

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

                vnfResourceInstaller.processVfModules(vfEntityDetails, toscaResourceStruct, vfResourceStructure,
                        service, metadata);
            }

            processResourceSequence(toscaResourceStruct, service);
            allottedResourceInstaller.processAllottedResources(arEntityDetails, toscaResourceStruct, service);
            networkResourceInstaller.processNetworks(toscaResourceStruct, service);
            // process Network Collections
            networkResourceInstaller.processNetworkCollections(toscaResourceStruct, service);
            // Process Service Proxy & Configuration
            configurationInstaller.processServiceProxyAndConfiguration(toscaResourceStruct, service);

            logger.info("Saving Service: {} ", service.getModelName());
            service = serviceRepo.save(service);
            configurationInstaller.correlateConfigCustomResources(service);

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
            logger.debug("serviceInputs: {} {}", serviceInputs);
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
                logger.error("service input could not be deserialized for service uuid:  {}",
                        sdcCsarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
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
                logger.error("serviceProperties could not be deserialized for service uuid:  {}", serviceUUID);
            } catch (Exception ex) {
                logger.error("service properties parsing failed. service uuid:{}", serviceUUID);
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
