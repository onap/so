/*-
d * ============LICENSE_START=======================================================
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

package org.onap.so.asdc.client;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.asdc.client.exceptions.ASDCParametersException;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.IVfResourceInstaller;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ResourceType;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.bpmn.BpmnInstaller;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.DistributionStatus;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ASDCController {

    protected static final Logger logger = LoggerFactory.getLogger(ASDCController.class);

    protected boolean isAsdcClientAutoManaged = false;

    protected String controllerName;

    private ASDCControllerStatus controllerStatus = ASDCControllerStatus.STOPPED;

    protected int nbOfNotificationsOngoing = 0;

    @Autowired
    private ToscaResourceInstaller toscaInstaller;

    @Autowired
    private BpmnInstaller bpmnInstaller;

    @Autowired
    private WatchdogDistributionStatusRepository wdsRepo;

    @Autowired
    private ASDCConfiguration asdcConfig;

    @Autowired
    private ASDCStatusCallBack asdcStatusCallBack;

    @Autowired
    private ASDCNotificationCallBack asdcNotificationCallBack;

    private IDistributionClient distributionClient;

    private static final String UUID_PARAM = "(UUID:";

    @Autowired
    private WatchdogDistribution wd;

    public int getNbOfNotificationsOngoing() {
        return nbOfNotificationsOngoing;
    }

    public IDistributionClient getDistributionClient() {
        return distributionClient;
    }

    public void setDistributionClient(IDistributionClient distributionClient) {
        this.distributionClient = distributionClient;
    }

    protected void changeControllerStatus(ASDCControllerStatus newControllerStatus) {
        switch (newControllerStatus) {

            case BUSY:
                ++this.nbOfNotificationsOngoing;
                this.controllerStatus = newControllerStatus;
                break;

            case IDLE:
                if (this.nbOfNotificationsOngoing > 1) {
                    --this.nbOfNotificationsOngoing;
                } else {
                    this.nbOfNotificationsOngoing = 0;
                    this.controllerStatus = newControllerStatus;
                }

                break;
            default:
                this.controllerStatus = newControllerStatus;
                break;

        }
    }

    public ASDCControllerStatus getControllerStatus() {
        return this.controllerStatus;
    }

    public ASDCController() {
        this("");
    }

    public ASDCController(String controllerConfigName) {
        isAsdcClientAutoManaged = true;
        this.controllerName = controllerConfigName;
    }

    public ASDCController(String controllerConfigName, IDistributionClient asdcClient,
        IVfResourceInstaller resourceinstaller) {
        distributionClient = asdcClient;
    }

    public ASDCController(String controllerConfigName, IDistributionClient asdcClient) {
        distributionClient = asdcClient;
        this.controllerName = controllerConfigName;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    /**
     * This method initializes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be instantiated or if an init
     * attempt is done when already initialized
     * @throws ASDCParametersException If there is an issue with the parameters provided
     * @throws IOException In case of issues when trying to load the key file
     */
    public void initASDC() throws ASDCControllerException {
        String event = "Initialize the ASDC Controller";
        logger.debug(event);
        if (this.getControllerStatus() != ASDCControllerStatus.STOPPED) {
            String endEvent = "The controller is already initialized, call the closeASDC method first";
            throw new ASDCControllerException(endEvent);
        }

        if (asdcConfig != null) {
            asdcConfig.setAsdcControllerName(controllerName);
        }

        if (this.distributionClient == null) {
            distributionClient = DistributionClientFactory.createDistributionClient();
        }

        IDistributionClientResult result = this.distributionClient.init(asdcConfig,
            asdcNotificationCallBack, asdcStatusCallBack);
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            String endEvent = "ASDC distribution client init failed with reason:"
                + result.getDistributionMessageResult();
            logger.debug(endEvent);
            this.changeControllerStatus(ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException("Initialization of the ASDC Controller failed with reason: "
                + result.getDistributionMessageResult());
        }

        result = this.distributionClient.start();
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            String endEvent = "ASDC distribution client start failed with reason:"
                + result.getDistributionMessageResult();
            logger.debug(endEvent);
            this.changeControllerStatus(ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException("Startup of the ASDC Controller failed with reason: "
                + result.getDistributionMessageResult());
        }

        this.changeControllerStatus(ASDCControllerStatus.IDLE);
        logger.info("{} {} {}", MessageEnum.ASDC_INIT_ASDC_CLIENT_SUC.toString(), "ASDC", "changeControllerStatus");
    }

    /**
     * This method closes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be closed because it's currently
     * BUSY in processing notifications.
     */
    public void closeASDC() throws ASDCControllerException {

        if (this.getControllerStatus() == ASDCControllerStatus.BUSY) {
            throw new ASDCControllerException("Cannot close the ASDC controller as it's currently in BUSY state");
        }
        if (this.distributionClient != null) {
            this.distributionClient.stop();
            // If auto managed we can set it to Null, ASDCController controls it.
            // In the other case the client of this class has specified it, so we can't reset it
            if (isAsdcClientAutoManaged) {
                // Next init will initialize it with a new ASDC Client
                this.distributionClient = null;
            }

        }
        this.changeControllerStatus(ASDCControllerStatus.STOPPED);
    }

    private boolean checkResourceAlreadyDeployed(VfResourceStructure resource) throws ArtifactInstallerException {

        if (toscaInstaller.isResourceAlreadyDeployed(resource)) {
            logger.info("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_ALREADY_EXIST.toString(),
                resource.getResourceInstance().getResourceInstanceName(),
                resource.getResourceInstance().getResourceUUID(),
                resource.getResourceInstance().getResourceName());

            this.sendDeployNotificationsForResource(resource, DistributionStatusEnum.ALREADY_DOWNLOADED, null);
            this.sendDeployNotificationsForResource(resource, DistributionStatusEnum.ALREADY_DEPLOYED, null);

            return true;
        } else {
            return false;
        }
    }


    protected IDistributionClientDownloadResult downloadTheArtifact(IArtifactInfo artifact,
        String distributionId) throws ASDCDownloadException {

        logger.info("Trying to download the artifact UUID: {} from URL: {}", artifact.getArtifactUUID(),
            artifact.getArtifactURL());
        IDistributionClientDownloadResult downloadResult;

        try {
            downloadResult = distributionClient.download(artifact);
            if (null == downloadResult) {
                logger.info("{} {}", MessageEnum.ASDC_ARTIFACT_NULL.toString(), artifact.getArtifactUUID());
                return downloadResult;
            }
        } catch (RuntimeException e) {
            logger.debug("Not able to download the artifact due to an exception: " + artifact.getArtifactURL());
            this.sendASDCNotification(NotificationType.DOWNLOAD,
                artifact.getArtifactURL(),
                asdcConfig.getConsumerID(),
                distributionId,
                DistributionStatusEnum.DOWNLOAD_ERROR,
                e.getMessage(),
                System.currentTimeMillis());

            throw new ASDCDownloadException("Exception caught when downloading the artifact", e);
        }

        if (DistributionActionResultEnum.SUCCESS.equals(downloadResult.getDistributionActionResult())) {
            logger.info("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_DOWNLOAD_SUC.toString(), artifact.getArtifactURL(),
                artifact.getArtifactUUID(), String.valueOf(downloadResult.getArtifactPayload().length));

        } else {
            logger.error("{} {} {} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                artifact.getArtifactName(), artifact.getArtifactURL(), artifact.getArtifactUUID(),
                downloadResult.getDistributionMessageResult(), MsoLogger.ErrorCode.DataError.getValue(),
                "ASDC artifact download fail");

            this.sendASDCNotification(NotificationType.DOWNLOAD,
                artifact.getArtifactURL(),
                asdcConfig.getConsumerID(),
                distributionId,
                DistributionStatusEnum.DOWNLOAD_ERROR,
                downloadResult.getDistributionMessageResult(),
                System.currentTimeMillis());

            throw new ASDCDownloadException("Artifact " + artifact.getArtifactName()
                + " could not be downloaded from ASDC URL "
                + artifact.getArtifactURL()
                + UUID_PARAM
                + artifact.getArtifactUUID()
                + ")"
                + System.lineSeparator()
                + "Error message is "
                + downloadResult.getDistributionMessageResult()
                + System.lineSeparator());

        }

        this.sendASDCNotification(NotificationType.DOWNLOAD,
            artifact.getArtifactURL(),
            asdcConfig.getConsumerID(),
            distributionId,
            DistributionStatusEnum.DOWNLOAD_OK,
            null,
            System.currentTimeMillis());
        return downloadResult;

    }

    private void writeArtifactToFile(IArtifactInfo artifact, IDistributionClientDownloadResult resultArtifact) {

        String filePath = Paths
            .get(getMsoConfigPath(), "ASDC", artifact.getArtifactVersion(), artifact.getArtifactName()).normalize()
            .toString();

        logger.info("Trying to write artifact UUID: {}, URL: {} to file: {}", artifact.getArtifactUUID(),
            artifact.getArtifactURL(), filePath);

        // make parent directory
        File file = new File(filePath);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }

        byte[] payloadBytes = resultArtifact.getArtifactPayload();

        try (FileOutputStream outFile = new FileOutputStream(filePath)) {
            logger.info("{} {} {} {}", MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF.toString(), "***WRITE FILE ARTIFACT NAME",
                "ASDC", artifact.getArtifactName());
            outFile.write(payloadBytes, 0, payloadBytes.length);
        } catch (Exception e) {
            logger.debug("Exception :", e);
            logger.error("{} {} {} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                artifact.getArtifactName(), artifact.getArtifactURL(), artifact.getArtifactUUID(),
                resultArtifact.getDistributionMessageResult(), MsoLogger.ErrorCode.DataError.getValue(),
                "ASDC write to file failed");
        }

    }


    protected void sendDeployNotificationsForResource(ResourceStructure resourceStructure,
        DistributionStatusEnum distribStatus, String errorReason) {

        for (IArtifactInfo artifactInfo : resourceStructure.getResourceInstance().getArtifacts()) {

            if ((DistributionStatusEnum.DEPLOY_OK.equals(distribStatus) && !artifactInfo.getArtifactType()
                .equalsIgnoreCase("OTHER") && !resourceStructure.isAlreadyDeployed())
                // This could be NULL if the artifact is a VF module artifact, this won't be present in the MAP
                && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()) != null
                && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()).getDeployedInDb()
                == 0) {
                this.sendASDCNotification(NotificationType.DEPLOY,
                    artifactInfo.getArtifactURL(),
                    asdcConfig.getConsumerID(),
                    resourceStructure.getNotification().getDistributionID(),
                    DistributionStatusEnum.DEPLOY_ERROR,
                    "The artifact has not been used by the modules defined in the resource",
                    System.currentTimeMillis());
            } else {
                this.sendASDCNotification(NotificationType.DEPLOY,
                    artifactInfo.getArtifactURL(),
                    asdcConfig.getConsumerID(),
                    resourceStructure.getNotification().getDistributionID(),
                    distribStatus,
                    errorReason,
                    System.currentTimeMillis());
            }
        }
    }

    protected void sendCsarDeployNotification(INotificationData iNotif, ResourceStructure resourceStructure,
        ToscaResourceStructure toscaResourceStructure, boolean deploySuccessful, String errorReason) {

        IArtifactInfo csarArtifact = toscaResourceStructure.getToscaArtifact();

        if (deploySuccessful) {

            this.sendASDCNotification(NotificationType.DEPLOY,
                csarArtifact.getArtifactURL(),
                asdcConfig.getConsumerID(),
                resourceStructure.getNotification().getDistributionID(),
                DistributionStatusEnum.DEPLOY_OK,
                errorReason,
                System.currentTimeMillis());

        } else {

            this.sendASDCNotification(NotificationType.DEPLOY,
                csarArtifact.getArtifactURL(),
                asdcConfig.getConsumerID(),
                resourceStructure.getNotification().getDistributionID(),
                DistributionStatusEnum.DEPLOY_ERROR,
                errorReason,
                System.currentTimeMillis());

        }
    }

    protected void deployResourceStructure(ResourceStructure resourceStructure,
        ToscaResourceStructure toscaResourceStructure) throws ArtifactInstallerException {

        logger.info("{} {} {} {}", MessageEnum.ASDC_START_DEPLOY_ARTIFACT.toString(),
            resourceStructure.getResourceInstance().getResourceInstanceName(),
            resourceStructure.getResourceInstance().getResourceUUID(), "ASDC");
        try {
            resourceStructure.prepareInstall();
            toscaInstaller.installTheResource(toscaResourceStructure, resourceStructure);

        } catch (ArtifactInstallerException e) {
            logger.info("{} {} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                resourceStructure.getResourceInstance().getResourceName(),
                resourceStructure.getResourceInstance().getResourceUUID(),
                String.valueOf(resourceStructure.getNumberOfResources()), "ASDC", "deployResourceStructure");
            sendDeployNotificationsForResource(resourceStructure, DistributionStatusEnum.DEPLOY_ERROR, e.getMessage());
            throw e;
        }

        if (resourceStructure.isDeployedSuccessfully() || toscaResourceStructure.isDeployedSuccessfully()) {
            logger.info("{} {} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC.toString(),
                resourceStructure.getResourceInstance().getResourceName(),
                resourceStructure.getResourceInstance().getResourceUUID(),
                String.valueOf(resourceStructure.getNumberOfResources()), "ASDC", "deployResourceStructure");
            sendDeployNotificationsForResource(resourceStructure, DistributionStatusEnum.DEPLOY_OK, null);
        }

    }


    private enum NotificationType {
        DOWNLOAD, DEPLOY
    }

    protected void sendASDCNotification(NotificationType notificationType,
        String artifactURL,
        String consumerID,
        String distributionID,
        DistributionStatusEnum status,
        String errorReason,
        long timestamp) {

        String event = "Sending " + notificationType.name()
            + "("
            + status.name()
            + ")"
            + " notification to ASDC for artifact:"
            + artifactURL;

        if (errorReason != null) {
            event = event + "(" + errorReason + ")";
        }
        logger.info("{} {} {} {} {} {}", MessageEnum.ASDC_SEND_NOTIF_ASDC.toString(), notificationType.name(),
            status.name(), artifactURL, "ASDC", "sendASDCNotification");
        logger.debug(event);

        String action = "";
        try {
            IDistributionStatusMessage message = new DistributionStatusMessage(artifactURL,
                consumerID,
                distributionID,
                status,
                timestamp);

            switch (notificationType) {
                case DOWNLOAD:
                    if (errorReason != null) {
                        this.distributionClient.sendDownloadStatus(message, errorReason);
                    } else {
                        this.distributionClient.sendDownloadStatus(message);
                    }
                    action = "sendDownloadStatus";
                    break;
                case DEPLOY:
                    if (errorReason != null) {
                        this.distributionClient.sendDeploymentStatus(message, errorReason);
                    } else {
                        this.distributionClient.sendDeploymentStatus(message);
                    }
                    action = "sendDeploymentdStatus";
                    break;
                default:
                    break;
            }
        } catch (RuntimeException e) {
            logger.warn("{} {} {} {} {}", MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                "sendASDCNotification", MsoLogger.ErrorCode.SchemaError.getValue(),
                "RuntimeException - sendASDCNotification", e);
        }
    }

    protected void sendFinalDistributionStatus(
        String distributionID,
        DistributionStatusEnum status,
        String errorReason) {

        logger.debug(
            "Enter sendFinalDistributionStatus with DistributionID " + distributionID + " and Status of " + status
                .name() + " and ErrorReason " + errorReason);

        long subStarttime = System.currentTimeMillis();
        try {

            IFinalDistrStatusMessage finalDistribution = new FinalDistributionStatusMessage(distributionID, status,
                subStarttime, asdcConfig.getConsumerID());

            if (errorReason == null) {
                this.distributionClient.sendFinalDistrStatus(finalDistribution);
            } else {
                this.distributionClient.sendFinalDistrStatus(finalDistribution, errorReason);
            }


        } catch (RuntimeException e) {
            logger.debug("Exception caught in sendFinalDistributionStatus {}", e.getMessage());
            logger.warn("{} {} {} {} {}", MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                "sendASDCNotification",
                MsoLogger.ErrorCode.SchemaError.getValue(), "RuntimeException - sendASDCNotification", e);
        }
    }

    private Optional<String> getNotificationJson(INotificationData iNotif) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.setSerializationInclusion(Include.NON_ABSENT);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Optional<String> returnValue = Optional.empty();
        try {
            returnValue = Optional.of(mapper.writeValueAsString(iNotif));
        } catch (JsonProcessingException e) {
            logger.error("Error converting incoming ASDC notification to JSON", e);
        }
        return returnValue;
    }

    public void treatNotification(INotificationData iNotif) {

        int noOfArtifacts = 0;

        for (IResourceInstance resource : iNotif.getResources()) {
            noOfArtifacts += resource.getArtifacts().size();
        }
        logger.info("{} {} {} {}", MessageEnum.ASDC_RECEIVE_CALLBACK_NOTIF.toString(), String.valueOf(noOfArtifacts),
            iNotif.getServiceUUID(), "ASDC");
        try {
            logger.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));
            logger
                .info("{} {} {} {}", MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF.toString(), iNotif.getServiceUUID(), "ASDC",
                    "treatNotification");

            this.changeControllerStatus(ASDCControllerStatus.BUSY);
            Optional<String> notificationMessage = getNotificationJson(iNotif);
            toscaInstaller.processWatchdog(iNotif.getDistributionID(), iNotif.getServiceUUID(), notificationMessage,
                asdcConfig.getConsumerID());

            // Process only the Resource artifacts in MSO
            this.processResourceNotification(iNotif);

            //********************************************************************************************************
            //Start Watchdog loop and wait for all components to complete before reporting final status back.
            // **If timer expires first then we will report a Distribution Error back to ASDC
            //********************************************************************************************************
            long initialStartTime = System.currentTimeMillis();
            boolean componentsComplete = false;
            String distributionStatus = null;
            String watchdogError = null;
            String overallStatus = null;
            int watchDogTimeout = asdcConfig.getWatchDogTimeout() * 1000;
            boolean isDeploySuccess = false;

            while (!componentsComplete && (System.currentTimeMillis() - initialStartTime) < watchDogTimeout) {

                try {
                    distributionStatus = wd.getOverallDistributionStatus(iNotif.getDistributionID());
                    Thread.sleep(watchDogTimeout / 10);
                } catch (Exception e) {
                    logger.debug("Exception in Watchdog Loop {}", e.getMessage());
                    Thread.sleep(watchDogTimeout / 10);
                }

                if (distributionStatus != null && !distributionStatus
                    .equalsIgnoreCase(DistributionStatus.INCOMPLETE.name())) {

                    if (distributionStatus.equalsIgnoreCase(DistributionStatus.SUCCESS.name())) {
                        isDeploySuccess = true;
                        overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name();
                    } else {
                        overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
                    }
                    componentsComplete = true;
                }
            }

            if (!componentsComplete) {
                logger
                    .debug("Timeout of {} seconds was reached before all components reported status", watchDogTimeout);
                watchdogError = "Timeout occurred while waiting for all components to report status";
                overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
            }

            if (distributionStatus == null) {
                overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
                logger.debug("DistributionStatus is null for DistributionId: {}", iNotif.getDistributionID());
            }

            try {
                wd.executePatchAAI(iNotif.getDistributionID(), iNotif.getServiceInvariantUUID(), overallStatus);
                logger.debug("A&AI Updated succefully with Distribution Status!");
            } catch (Exception e) {
                logger.debug("Exception in Watchdog executePatchAAI(): {}", e.getMessage());
                watchdogError = "Error calling A&AI " + e.getMessage();
                if (e.getCause() != null) {
                    logger.debug("Exception caused by: {}", e.getCause().getMessage());
                }
            }

            if (isDeploySuccess && watchdogError == null) {
                sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK,
                    null);
                WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
                wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.toString());
                wdsRepo.save(wds);
            } else {
                sendFinalDistributionStatus(iNotif.getDistributionID(),
                    DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, watchdogError);
                WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
                wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.toString());
                wdsRepo.save(wds);
            }


        } catch (ObjectOptimisticLockingFailureException e) {

            logger.debug("OptimisticLockingFailure for DistributionId: {} Another process "
                    + "has already altered this distribution, so not going to process it on this site.",
                iNotif.getDistributionID());
            logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                "Database concurrency exception: ", "ASDC", "treatNotification",
                MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "RuntimeException in treatNotification", e);

        } catch (Exception e) {
            logger.error("", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                "Unexpected exception caught during the notification processing", "ASDC",
                "treatNotification", MsoLogger.ErrorCode.SchemaError.getValue(),
                "RuntimeException in treatNotification",
                e);

            try {
                wd.executePatchAAI(iNotif.getDistributionID(), iNotif.getServiceInvariantUUID(),
                    DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name());
                logger.debug("A&AI Updated succefully with Distribution Status of {}",
                    DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name());
            } catch (Exception aaiException) {
                logger.debug("Exception in executePatchAAI(): {}", aaiException);
                if (aaiException.getCause() != null) {
                    logger.debug("Exception caused by: {}", aaiException.getCause().getMessage());
                }
            }

            sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR,
                e.getMessage());

            WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
            wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.toString());
            wdsRepo.save(wds);

        } finally {
            this.changeControllerStatus(ASDCControllerStatus.IDLE);
        }
    }

    protected void processResourceNotification(INotificationData iNotif) {
        // For each artifact, create a structure describing the VFModule in a ordered flat level
        ResourceStructure resourceStructure = null;
        String msoConfigPath = getMsoConfigPath();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure(msoConfigPath);
        boolean deploySuccessful = true;
        String errorMessage = null;

        try {
            this.processCsarServiceArtifacts(iNotif, toscaResourceStructure);
            IArtifactInfo iArtifact = toscaResourceStructure.getToscaArtifact();
            String filePath =
                msoConfigPath + "/ASDC/" + iArtifact.getArtifactVersion() + "/" + iArtifact
                    .getArtifactName();
            File csarFile = new File(filePath);
            String csarFilePath = csarFile.getAbsolutePath();
            if (bpmnInstaller.containsWorkflows(csarFilePath)) {
                bpmnInstaller.installBpmn(csarFilePath);
            }

            for (IResourceInstance resource : iNotif.getResources()) {

                String resourceType = resource.getResourceType();
                String category = resource.getCategory();

                logger.info("Processing Resource Type: {}, Model UUID: {}", resourceType, resource.getResourceUUID());

                if ("VF".equals(resourceType) && !"Allotted Resource".equalsIgnoreCase(category)) {
                    resourceStructure = new VfResourceStructure(iNotif, resource);
                } else if ("PNF".equals(resourceType)) {
                    resourceStructure = new PnfResourceStructure(iNotif, resource);
                } else {
                    // There are cases where the Service has no VF resources, those are handled here
                    logger.info("No resources found for Service: {}", iNotif.getServiceUUID());
                    resourceStructure = new VfResourceStructure(iNotif, new ResourceInstance());
                    resourceStructure.setResourceType(ResourceType.OTHER);
                }

                for (IArtifactInfo artifact : resource.getArtifacts()) {
                    IDistributionClientDownloadResult resultArtifact = this.downloadTheArtifact(artifact,
                        iNotif.getDistributionID());
                    if (resultArtifact != null) {
                        resourceStructure.addArtifactToStructure(distributionClient, artifact, resultArtifact);
                    }
                }

                //Deploy VF resource and artifacts
                logger.debug("Preparing to deploy Service: {}", iNotif.getServiceUUID());
                try {
                    this.deployResourceStructure(resourceStructure, toscaResourceStructure);
                } catch (ArtifactInstallerException e) {
                    deploySuccessful = false;
                    errorMessage = e.getMessage();
                    logger.error("Exception occurred", e);
                }

                this.sendCsarDeployNotification(iNotif, resourceStructure, toscaResourceStructure, deploySuccessful,
                    errorMessage);
            }


        } catch (ASDCDownloadException | UnsupportedEncodingException e) {
            logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                "Exception caught during Installation of artifact", "ASDC", "processResourceNotification",
                MsoLogger.ErrorCode.BusinessProcesssError.getValue(), "Exception in processResourceNotification", e);
        }
    }

    private String getMsoConfigPath() {
        String msoConfigPath = System.getProperty("mso.config.path");
        if (msoConfigPath == null) {
            logger.info("Unable to find the system property mso.config.path, use the default configuration");
            msoConfigPath = asdcConfig.getPropertyOrNull("mso.config.defaultpath");
        }
        if ( msoConfigPath == null){
            logger.info("Unable to find the property: {} from configuration.", "mso.config.defaultpath");
            msoConfigPath = "";
        }
        logger.info("MSO config path is: {}", msoConfigPath);
        return msoConfigPath;
    }

    protected void processCsarServiceArtifacts(INotificationData iNotif,
        ToscaResourceStructure toscaResourceStructure) {

        List<IArtifactInfo> serviceArtifacts = iNotif.getServiceArtifacts();

        for (IArtifactInfo artifact : serviceArtifacts) {

            if (artifact.getArtifactType().equals(ASDCConfiguration.TOSCA_CSAR)) {

                try {

                    toscaResourceStructure.setToscaArtifact(artifact);

                    IDistributionClientDownloadResult resultArtifact = this
                        .downloadTheArtifact(artifact, iNotif.getDistributionID());

                    writeArtifactToFile(artifact, resultArtifact);

                    toscaResourceStructure.updateResourceStructure(artifact);

                    toscaResourceStructure.setServiceVersion(iNotif.getServiceVersion());

                    logger.debug(ASDCNotificationLogging.dumpCSARNotification(iNotif, toscaResourceStructure));


                } catch (Exception e) {
                    logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                        "Exception caught during processCsarServiceArtifacts", "ASDC", "processCsarServiceArtifacts",
                        MsoLogger.ErrorCode.BusinessProcesssError.getValue(),
                        "Exception in processCsarServiceArtifacts", e);
                }
            } else if (artifact.getArtifactType().equals(ASDCConfiguration.WORKFLOWS)) {

                try {

                    IDistributionClientDownloadResult resultArtifact = this
                        .downloadTheArtifact(artifact, iNotif.getDistributionID());

                    writeArtifactToFile(artifact, resultArtifact);

                    toscaResourceStructure.setToscaArtifact(artifact);

                    logger.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));


                } catch (Exception e) {
                    logger.info("Whats the error {}", e.getMessage());
                    logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                        "Exception caught during processCsarServiceArtifacts", "ASDC", "processCsarServiceArtifacts",
                        MsoLogger.ErrorCode.BusinessProcesssError.getValue(),
                        "Exception in processCsarServiceArtifacts",
                        e);
                }
            }


        }
    }

    private static final String UNKNOWN = "Unknown";

    /**
     * @return the address of the ASDC we are connected to.
     */
    public String getAddress() {
        if (asdcConfig != null) {
            return asdcConfig.getAsdcAddress();
        }
        return UNKNOWN;
    }

    /**
     * @return the environment name of the ASDC we are connected to.
     */
    public String getEnvironment() {
        if (asdcConfig != null) {
            return asdcConfig.getEnvironmentName();
        }
        return UNKNOWN;
    }
}
