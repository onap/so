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
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
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

@Component
public class ToscaResourceInstaller {

    protected static final String MSO = "SO";

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
    protected ServiceModelInstaller serviceModelInstaller;

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
            serviceModelInstaller.createToscaCsar(toscaResourceStruct);
            Service service = serviceModelInstaller.createService(toscaResourceStruct, resourceStruct);

            serviceModelInstaller.processResourceSequence(toscaResourceStruct, service);
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
            serviceModelInstaller.createToscaCsar(toscaResourceStruct);
            serviceModelInstaller.createService(toscaResourceStruct, vfResourceStruct);
            Service service = toscaResourceStruct.getCatalogService();
            serviceModelInstaller.createServiceInfo(toscaResourceStruct, service);

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

            serviceModelInstaller.processResourceSequence(toscaResourceStruct, service);
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

    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installNsstService(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct,
            String artifactContent) {
        serviceModelInstaller.createToscaCsar(toscaResourceStruct);
        serviceModelInstaller.createService(toscaResourceStruct, vfResourceStruct);
        Service service = toscaResourceStruct.getCatalogService();
        serviceModelInstaller.createServiceInfo(toscaResourceStruct, service);
        serviceModelInstaller.createServiceArtifact(service, vfResourceStruct, artifactContent);
        serviceRepo.save(service);
    }
}
