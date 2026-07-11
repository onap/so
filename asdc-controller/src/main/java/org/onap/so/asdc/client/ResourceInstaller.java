/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ResourceType;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.asdc.util.ZipParser;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Orchestrates the installation of the resource and artifact structures contained in an SDC (ASDC) notification.
 * Extracted from ASDCController so the resource-install concern is isolated; the distribution client is passed per call
 * because it is set at runtime on the controller.
 */
@Component
public class ResourceInstaller {

    private static final Logger logger = LoggerFactory.getLogger(ResourceInstaller.class);
    protected static final String MSO = "SO";

    @Autowired
    private ToscaResourceInstaller toscaInstaller;

    @Autowired
    private ArtifactDownloader artifactDownloader;

    @Autowired
    private DistributionStatusSender statusSender;

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;

    public void processResourceNotification(IDistributionClient client, INotificationData iNotif) {
        // For each artifact, create a structure describing the VFModule in a ordered flat level
        ResourceStructure resourceStructure = null;
        String msoConfigPath = artifactDownloader.getMsoConfigPath();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure(msoConfigPath);
        DistributionStatusEnum deployStatus = DistributionStatusEnum.DEPLOY_OK;
        String errorMessage = null;
        boolean serviceDeployed = false;

        try {
            this.processCsarServiceArtifacts(client, iNotif, toscaResourceStructure);
            if (isCsarAlreadyDeployed(client, iNotif, toscaResourceStructure)) {
                return;
            }
            // process NsstResource
            this.processNsstNotification(iNotif, toscaResourceStructure);

            if (iNotif.getResources().isEmpty()) {
                logger.error("Service Model contains no resources.");
                return;
            }

            for (IResourceInstance resource : iNotif.getResources()) {

                String resourceType = resource.getResourceType();
                boolean hasVFResource = false;

                logger.info("Processing Resource Type: {}, Model UUID: {}", resourceType, resource.getResourceUUID());

                resourceStructure = getResourceStructure(iNotif, resource, resourceType);

                try {

                    if (!this.checkResourceAlreadyDeployed(client, resourceStructure, serviceDeployed)) {
                        logger.debug("Processing Resource Type: {} and Model UUID: {}", resourceType,
                                resourceStructure.getResourceInstance().getResourceUUID());


                        if ("VF".equals(resourceType)) {
                            hasVFResource = true;
                            for (IArtifactInfo artifact : resource.getArtifacts()) {
                                IDistributionClientDownloadResult resultArtifact = artifactDownloader
                                        .downloadTheArtifact(client, artifact, iNotif.getDistributionID());
                                if (resultArtifact == null) {
                                    continue;
                                }

                                if (ASDCConfiguration.VF_MODULES_METADATA.equals(artifact.getArtifactType())) {
                                    logger.debug("VF_MODULE_ARTIFACT: {}",
                                            new String(resultArtifact.getArtifactPayload(), StandardCharsets.UTF_8));
                                    logger.debug(ASDCNotificationLogging
                                            .dumpVfModuleMetaDataList(((VfResourceStructure) resourceStructure)
                                                    .decodeVfModuleArtifact(resultArtifact.getArtifactPayload())));
                                }
                                if (!ASDCConfiguration.WORKFLOW.equals(artifact.getArtifactType())) {
                                    resourceStructure.addArtifactToStructure(client, artifact, resultArtifact);
                                } else {
                                    artifactDownloader.writeArtifactToFile(artifact, resultArtifact);
                                    logger.debug("Adding workflow artifact to structure: {}",
                                            artifact.getArtifactName());
                                    resourceStructure.addWorkflowArtifactToStructure(artifact, resultArtifact);
                                }

                            }

                            // Deploy VF resource and artifacts
                            logger.debug("Preparing to deploy Service: {}", iNotif.getServiceUUID());


                            this.deployResourceStructure(client, resourceStructure, toscaResourceStructure);
                            serviceDeployed = true;
                        }
                    }

                } catch (ArtifactInstallerException e) {
                    deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
                    errorMessage = e.getMessage();
                    logger.error("Exception occurred", e);
                }

                if (!hasVFResource) {

                    logger.debug("No resources found for Service: {}", iNotif.getServiceUUID());

                    logger.debug("Preparing to deploy Service: {}", iNotif.getServiceUUID());
                    try {
                        this.deployResourceStructure(client, resourceStructure, toscaResourceStructure);
                        serviceDeployed = true;
                    } catch (ArtifactInstallerException e) {
                        deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
                        errorMessage = e.getMessage();
                        logger.error("Exception occurred", e);
                    }
                }
            }

            statusSender.sendCsarDeployNotification(client, resourceStructure, toscaResourceStructure, deployStatus,
                    errorMessage);

        } catch (ASDCDownloadException | UnsupportedEncodingException e) {
            logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception caught during Installation of artifact", "ASDC", "processResourceNotification",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in processResourceNotification", e);
        }
    }

    protected boolean checkResourceAlreadyDeployed(IDistributionClient client, ResourceStructure resource,
            boolean serviceDeployed) throws ArtifactInstallerException {


        if (toscaInstaller.isResourceAlreadyDeployed(resource, serviceDeployed)) {
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_ALREADY_EXIST.toString(),
                    resource.getResourceInstance().getResourceInstanceName(),
                    resource.getResourceInstance().getResourceUUID(), resource.getResourceInstance().getResourceName());

            statusSender.sendDeployNotificationsForResource(client, resource, DistributionStatusEnum.ALREADY_DOWNLOADED,
                    null);
            statusSender.sendDeployNotificationsForResource(client, resource, DistributionStatusEnum.ALREADY_DEPLOYED,
                    null);

            return true;
        } else {
            return false;
        }
    }

    protected void notifyErrorToAsdc(IDistributionClient client, INotificationData iNotif,
            ToscaResourceStructure toscaResourceStructure, DistributionStatusEnum deployStatus,
            VfResourceStructure resourceStructure, String errorMessage) {
        // do csar lever first
        statusSender.sendCsarDeployNotification(client, resourceStructure, toscaResourceStructure, deployStatus,
                errorMessage);
        // at resource level
        for (IResourceInstance resource : iNotif.getResources()) {
            resourceStructure = new VfResourceStructure(iNotif, resource);
            errorMessage = String.format("Resource with UUID: %s already exists", resource.getResourceUUID());
            statusSender.sendCsarDeployNotification(client, resourceStructure, toscaResourceStructure, deployStatus,
                    errorMessage);
        }
    }

    protected boolean isCsarAlreadyDeployed(IDistributionClient client, INotificationData iNotif,
            ToscaResourceStructure toscaResourceStructure) {
        VfResourceStructure resourceStructure = null;
        String errorMessage = "";
        boolean csarAlreadyDeployed = false;
        DistributionStatusEnum deployStatus = DistributionStatusEnum.DEPLOY_OK;
        WatchdogComponentDistributionStatus wdStatus =
                new WatchdogComponentDistributionStatus(iNotif.getDistributionID(), MSO);
        try {
            csarAlreadyDeployed = toscaInstaller.isCsarAlreadyDeployed(toscaResourceStructure);
            if (csarAlreadyDeployed) {
                deployStatus = DistributionStatusEnum.ALREADY_DEPLOYED;
                resourceStructure = new VfResourceStructure(iNotif, null);
                errorMessage = String.format("Csar with UUID: %s already exists",
                        toscaResourceStructure.getToscaArtifact().getArtifactUUID());
                wdStatus.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
                watchdogCDStatusRepository.saveAndFlush(wdStatus);
                logger.error(errorMessage);
            }
        } catch (ArtifactInstallerException e) {
            deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
            resourceStructure = new VfResourceStructure(iNotif, null);
            errorMessage = e.getMessage();
            wdStatus.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_ERROR.name());
            watchdogCDStatusRepository.saveAndFlush(wdStatus);
            logger.warn("Tosca Checksums don't match, Tosca validation check failed", e);
        }

        if (deployStatus != DistributionStatusEnum.DEPLOY_OK) {
            notifyErrorToAsdc(client, iNotif, toscaResourceStructure, deployStatus, resourceStructure, errorMessage);
        }

        return csarAlreadyDeployed;
    }

    protected void deployResourceStructure(IDistributionClient client, ResourceStructure resourceStructure,
            ToscaResourceStructure toscaResourceStructure) throws ArtifactInstallerException {

        logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_START_DEPLOY_ARTIFACT.toString(),
                resourceStructure.getResourceInstance().getResourceInstanceName(),
                resourceStructure.getResourceInstance().getResourceUUID(), "ASDC");
        try {
            resourceStructure.prepareInstall();
            toscaInstaller.installTheResource(toscaResourceStructure, resourceStructure);

        } catch (ArtifactInstallerException e) {
            logger.info(LoggingAnchor.SIX, MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                    resourceStructure.getResourceInstance().getResourceName(),
                    resourceStructure.getResourceInstance().getResourceUUID(), resourceStructure.getNumberOfResources(),
                    "ASDC", "deployResourceStructure");
            statusSender.sendDeployNotificationsForResource(client, resourceStructure,
                    DistributionStatusEnum.DEPLOY_ERROR, e.getMessage());
            throw e;
        }

        if (resourceStructure.isDeployedSuccessfully() || toscaResourceStructure.isDeployedSuccessfully()) {
            logger.info(LoggingAnchor.SIX, MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC.toString(),
                    resourceStructure.getResourceInstance().getResourceName(),
                    resourceStructure.getResourceInstance().getResourceUUID(), resourceStructure.getNumberOfResources(),
                    "ASDC", "deployResourceStructure");
            statusSender.sendDeployNotificationsForResource(client, resourceStructure, DistributionStatusEnum.DEPLOY_OK,
                    null);
        }

    }

    protected void processCsarServiceArtifacts(IDistributionClient client, INotificationData iNotif,
            ToscaResourceStructure toscaResourceStructure) {

        List<IArtifactInfo> serviceArtifacts = iNotif.getServiceArtifacts();

        for (IArtifactInfo artifact : serviceArtifacts) {

            if (artifact.getArtifactType().equals(ASDCConfiguration.TOSCA_CSAR)) {

                try {

                    toscaResourceStructure.setToscaArtifact(artifact);

                    IDistributionClientDownloadResult resultArtifact =
                            artifactDownloader.downloadTheArtifact(client, artifact, iNotif.getDistributionID());

                    artifactDownloader.writeArtifactToFile(artifact, resultArtifact);

                    toscaResourceStructure.updateResourceStructure(artifact);

                    toscaResourceStructure.setServiceVersion(iNotif.getServiceVersion());

                    logger.debug(ASDCNotificationLogging.dumpCSARNotification(iNotif, toscaResourceStructure));


                } catch (Exception e) {
                    logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                            "Exception caught during processCsarServiceArtifacts", "ASDC",
                            "processCsarServiceArtifacts", ErrorCode.BusinessProcessError.getValue(),
                            "Exception in processCsarServiceArtifacts", e);
                }
            } else if (artifact.getArtifactType().equals(ASDCConfiguration.WORKFLOW)) {

                try {

                    IDistributionClientDownloadResult resultArtifact =
                            artifactDownloader.downloadTheArtifact(client, artifact, iNotif.getDistributionID());

                    artifactDownloader.writeArtifactToFile(artifact, resultArtifact);

                    toscaResourceStructure.setToscaArtifact(artifact);

                    logger.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));


                } catch (Exception e) {
                    logger.info("Whats the error {}", e.getMessage());
                    logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                            "Exception caught during processCsarServiceArtifacts", "ASDC",
                            "processCsarServiceArtifacts", ErrorCode.BusinessProcessError.getValue(),
                            "Exception in processCsarServiceArtifacts", e);
                }
            } else if (artifact.getArtifactType().equals(ASDCConfiguration.OTHER)) {
                try {
                    IDistributionClientDownloadResult resultArtifact =
                            artifactDownloader.downloadTheArtifact(client, artifact, iNotif.getDistributionID());

                    artifactDownloader.writeArtifactToFile(artifact, resultArtifact);

                    toscaResourceStructure.setToscaArtifact(artifact);

                    toscaResourceStructure.setServiceVersion(iNotif.getServiceVersion());

                } catch (ASDCDownloadException e) {
                    logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                            "Exception caught during processCsarServiceArtifacts", "ASDC",
                            "processCsarServiceArtifacts", ErrorCode.BusinessProcessError.getValue(),
                            "Exception in processCsarServiceArtifacts", e);
                }
            }


        }
    }

    private ResourceStructure getResourceStructure(INotificationData iNotif, IResourceInstance resource,
            String resourceType) {
        if ("VF".equals(resourceType)) {
            return new VfResourceStructure(iNotif, resource);
        }
        if ("PNF".equals(resourceType)) {
            return new PnfResourceStructure(iNotif, resource);
        }
        logger.info("No resources found for Service: {}", iNotif.getServiceUUID());
        ResourceStructure resourceStructure = new VfResourceStructure(iNotif, new ResourceInstance());
        resourceStructure.setResourceType(ResourceType.OTHER);
        return resourceStructure;
    }

    private void processNsstNotification(INotificationData iNotif, ToscaResourceStructure toscaResourceStructure) {
        Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();
        try {
            String category = serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);
            boolean isNeedInital = (category.contains("NSST") || category.equalsIgnoreCase("TN Network Requirement"))
                    && iNotif.getResources().isEmpty();
            if (isNeedInital) {
                String artifactContent = null;
                List<IArtifactInfo> serviceArtifacts = iNotif.getServiceArtifacts();
                Optional<IArtifactInfo> artifactOpt = serviceArtifacts.stream()
                        .filter(e -> e.getArtifactType().equalsIgnoreCase("WORKFLOW")).findFirst();
                if (artifactOpt.isPresent()) {
                    IArtifactInfo artifactInfo = artifactOpt.get();
                    logger.debug("Ready to parse this serviceArtifactUUID:  {}", artifactInfo.getArtifactUUID());
                    String filePath = Paths.get(artifactDownloader.getMsoConfigPath(), "ASDC",
                            artifactInfo.getArtifactVersion(), artifactInfo.getArtifactName()).normalize().toString();
                    ZipParser zipParserInstance = ZipParser.getInstance();
                    artifactContent = zipParserInstance.parseJsonForZip(filePath);
                    logger.debug("serviceArtifact parsing success! serviceArtifactUUID: {}",
                            artifactInfo.getArtifactUUID());

                    ResourceStructure resourceStructure = new VfResourceStructure(iNotif, new ResourceInstance());
                    resourceStructure.setResourceType(ResourceType.OTHER);
                    toscaInstaller.installNsstService(toscaResourceStructure, (VfResourceStructure) resourceStructure,
                            artifactContent);
                } else {
                    logger.debug("serviceArtifact is null");
                    toscaInstaller.installNsstService(toscaResourceStructure, null, null);
                }

            }

        } catch (IOException e) {
            logger.error("serviceArtifact parse failure for service uuid:  {}",
                    serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        } catch (Exception e) {
            logger.error("error NSST process resource failure ", e);
        }
    }
}
