/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.activity.DeployActivitySpecs;
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
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.DistributionStatus;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.asdc.util.ZipParser;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class ASDCController {

    protected static final Logger logger = LoggerFactory.getLogger(ASDCController.class);
    protected static final String MSO = "SO";
    protected boolean isAsdcClientAutoManaged = false;
    protected String controllerName;
    protected int nbOfNotificationsOngoing = 0;

    private static final String UNKNOWN = "Unknown";
    private static final String UUID_PARAM = "(UUID:";
    private static final ObjectMapper mapper;
    private ASDCControllerStatus controllerStatus = ASDCControllerStatus.STOPPED;

    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.setSerializationInclusion(Include.NON_ABSENT);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Autowired
    private ToscaResourceInstaller toscaInstaller;

    @Autowired
    private WatchdogDistributionStatusRepository wdsRepo;

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;

    @Autowired
    private ASDCConfiguration asdcConfig;

    @Autowired
    private ASDCStatusCallBack asdcStatusCallBack;

    @Autowired
    private ASDCNotificationCallBack asdcNotificationCallBack;

    private IDistributionClient distributionClient;


    @Autowired
    private WatchdogDistribution wd;

    @Autowired
    DeployActivitySpecs deployActivitySpecs;

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

    public ASDCConfiguration getASDCConfiguration() {
        return asdcConfig;
    }

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
                changeOnStatusIDLE(newControllerStatus);

                break;
            default:
                this.controllerStatus = newControllerStatus;
                break;

        }
    }

    private void changeOnStatusIDLE(ASDCControllerStatus newControllerStatus) {
        if (this.nbOfNotificationsOngoing > 1) {
            --this.nbOfNotificationsOngoing;
        } else {
            this.nbOfNotificationsOngoing = 0;
            this.controllerStatus = newControllerStatus;
        }
    }

    public ASDCControllerStatus getControllerStatus() {
        return this.controllerStatus;
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
     *         attempt is done when already initialized
     * @throws ASDCParametersException If there is an issue with the parameters provided
     * @throws IOException In case of issues when trying to load the key file
     */
    public void initASDC() throws ASDCControllerException {
        logger.debug("Initialize the ASDC Controller");
        if (!isStopped()) {
            throw new ASDCControllerException("The controller is already initialized, call the closeASDC method first");
        }

        if (asdcConfig != null) {
            asdcConfig.setAsdcControllerName(controllerName);
        }

        if (this.distributionClient == null) {
            distributionClient = DistributionClientFactory.createDistributionClient();
        }

        IDistributionClientResult result =
                this.distributionClient.init(asdcConfig, asdcNotificationCallBack, asdcStatusCallBack);
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            String endEvent =
                    "ASDC distribution client init failed with reason:" + result.getDistributionMessageResult();
            logger.debug(endEvent);
            this.changeControllerStatus(ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException("Initialization of the ASDC Controller failed with reason: "
                    + result.getDistributionMessageResult());
        }

        result = this.distributionClient.start();
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            String endEvent =
                    "ASDC distribution client start failed with reason:" + result.getDistributionMessageResult();
            logger.debug(endEvent);
            this.changeControllerStatus(ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException(
                    "Startup of the ASDC Controller failed with reason: " + result.getDistributionMessageResult());
        }

        this.changeControllerStatus(ASDCControllerStatus.IDLE);
        logger.info(LoggingAnchor.THREE, MessageEnum.ASDC_INIT_ASDC_CLIENT_SUC.toString(), "ASDC",
                "changeControllerStatus");
    }

    /**
     * @return true if controller is stopped
     */
    public boolean isStopped() {
        return this.getControllerStatus() == ASDCControllerStatus.STOPPED;
    }

    /**
     * This method closes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be closed because it's currently
     *         BUSY in processing notifications.
     */
    public void closeASDC() throws ASDCControllerException {

        if (isBusy()) {
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

    /**
     * @return true if controller is currently processing notification
     */
    public boolean isBusy() {
        return this.getControllerStatus() == ASDCControllerStatus.BUSY;
    }

    protected boolean checkResourceAlreadyDeployed(ResourceStructure resource, boolean serviceDeployed)
            throws ArtifactInstallerException {


        if (toscaInstaller.isResourceAlreadyDeployed(resource, serviceDeployed)) {
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_ALREADY_EXIST.toString(),
                    resource.getResourceInstance().getResourceInstanceName(),
                    resource.getResourceInstance().getResourceUUID(), resource.getResourceInstance().getResourceName());

            this.sendDeployNotificationsForResource(resource, DistributionStatusEnum.ALREADY_DOWNLOADED, null);
            this.sendDeployNotificationsForResource(resource, DistributionStatusEnum.ALREADY_DEPLOYED, null);

            return true;
        } else {
            return false;
        }
    }

    protected void notifyErrorToAsdc(INotificationData iNotif, ToscaResourceStructure toscaResourceStructure,
            DistributionStatusEnum deployStatus, VfResourceStructure resourceStructure, String errorMessage) {
        // do csar lever first
        this.sendCsarDeployNotification(resourceStructure, toscaResourceStructure, deployStatus, errorMessage);
        // at resource level
        for (IResourceInstance resource : iNotif.getResources()) {
            resourceStructure = new VfResourceStructure(iNotif, resource);
            errorMessage = String.format("Resource with UUID: %s already exists", resource.getResourceUUID());
            this.sendCsarDeployNotification(resourceStructure, toscaResourceStructure, deployStatus, errorMessage);
        }
    }

    protected boolean isCsarAlreadyDeployed(INotificationData iNotif, ToscaResourceStructure toscaResourceStructure) {
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
            notifyErrorToAsdc(iNotif, toscaResourceStructure, deployStatus, resourceStructure, errorMessage);
        }

        return csarAlreadyDeployed;
    }

    protected IDistributionClientDownloadResult downloadTheArtifact(IArtifactInfo artifact, String distributionId)
            throws ASDCDownloadException {

        logger.info("Trying to download the artifact UUID: {} from URL: {}", artifact.getArtifactUUID(),
                artifact.getArtifactURL());
        IDistributionClientDownloadResult downloadResult;

        try {
            downloadResult = distributionClient.download(artifact);
            if (null == downloadResult) {
                logger.info(LoggingAnchor.TWO, MessageEnum.ASDC_ARTIFACT_NULL.toString(), artifact.getArtifactUUID());
                return downloadResult;
            }
        } catch (RuntimeException e) {
            logger.debug("Not able to download the artifact due to an exception: " + artifact.getArtifactURL());
            this.sendASDCNotification(NotificationType.DOWNLOAD, artifact.getArtifactURL(), asdcConfig.getConsumerID(),
                    distributionId, DistributionStatusEnum.DOWNLOAD_ERROR, e.getMessage(), System.currentTimeMillis());

            throw new ASDCDownloadException("Exception caught when downloading the artifact", e);
        }

        if (DistributionActionResultEnum.SUCCESS.equals(downloadResult.getDistributionActionResult())) {
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_DOWNLOAD_SUC.toString(),
                    artifact.getArtifactURL(), artifact.getArtifactUUID(), downloadResult.getArtifactPayload().length);

        } else {
            logger.error(LoggingAnchor.SEVEN, MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                    artifact.getArtifactName(), artifact.getArtifactURL(), artifact.getArtifactUUID(),
                    downloadResult.getDistributionMessageResult(), ErrorCode.DataError.getValue(),
                    "ASDC artifact download fail");

            this.sendASDCNotification(NotificationType.DOWNLOAD, artifact.getArtifactURL(), asdcConfig.getConsumerID(),
                    distributionId, DistributionStatusEnum.DOWNLOAD_ERROR,
                    downloadResult.getDistributionMessageResult(), System.currentTimeMillis());

            throw new ASDCDownloadException("Artifact " + artifact.getArtifactName()
                    + " could not be downloaded from ASDC URL " + artifact.getArtifactURL() + UUID_PARAM
                    + artifact.getArtifactUUID() + ")" + System.lineSeparator() + "Error message is "
                    + downloadResult.getDistributionMessageResult() + System.lineSeparator());

        }

        this.sendASDCNotification(NotificationType.DOWNLOAD, artifact.getArtifactURL(), asdcConfig.getConsumerID(),
                distributionId, DistributionStatusEnum.DOWNLOAD_OK, null, System.currentTimeMillis());
        return downloadResult;

    }

    private void writeArtifactToFile(IArtifactInfo artifact, IDistributionClientDownloadResult resultArtifact) {

        String filePath =
                Paths.get(getMsoConfigPath(), "ASDC", artifact.getArtifactVersion(), artifact.getArtifactName())
                        .normalize().toString();

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
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF.toString(),
                    "***WRITE FILE ARTIFACT NAME", "ASDC", artifact.getArtifactName());
            outFile.write(payloadBytes, 0, payloadBytes.length);
        } catch (Exception e) {
            logger.debug("Exception :", e);
            logger.error(LoggingAnchor.SEVEN, MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL.toString(),
                    artifact.getArtifactName(), artifact.getArtifactURL(), artifact.getArtifactUUID(),
                    resultArtifact.getDistributionMessageResult(), ErrorCode.DataError.getValue(),
                    "ASDC write to file failed");
        }

    }


    protected void sendDeployNotificationsForResource(ResourceStructure resourceStructure,
            DistributionStatusEnum distribStatus, String errorReason) {

        for (IArtifactInfo artifactInfo : resourceStructure.getResourceInstance().getArtifacts()) {

            if ((DistributionStatusEnum.DEPLOY_OK.equals(distribStatus)
                    && !("OTHER").equalsIgnoreCase(artifactInfo.getArtifactType())
                    && !resourceStructure.isAlreadyDeployed())
                    // This could be NULL if the artifact is a VF module artifact, this won't be present in the MAP
                    && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()) != null
                    && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID())
                            .getDeployedInDb() == 0) {
                this.sendASDCNotification(NotificationType.DEPLOY, artifactInfo.getArtifactURL(),
                        asdcConfig.getConsumerID(), resourceStructure.getNotification().getDistributionID(),
                        DistributionStatusEnum.DEPLOY_ERROR,
                        "The artifact has not been used by the modules defined in the resource",
                        System.currentTimeMillis());
            } else {
                this.sendASDCNotification(NotificationType.DEPLOY, artifactInfo.getArtifactURL(),
                        asdcConfig.getConsumerID(), resourceStructure.getNotification().getDistributionID(),
                        distribStatus, errorReason, System.currentTimeMillis());
            }
        }
    }

    protected void sendCsarDeployNotification(ResourceStructure resourceStructure,
            ToscaResourceStructure toscaResourceStructure, DistributionStatusEnum statusEnum, String errorReason) {

        IArtifactInfo csarArtifact = toscaResourceStructure.getToscaArtifact();

        this.sendASDCNotification(NotificationType.DEPLOY, csarArtifact.getArtifactURL(), asdcConfig.getConsumerID(),
                resourceStructure.getNotification().getDistributionID(), statusEnum, errorReason,
                System.currentTimeMillis());

    }

    protected void deployResourceStructure(ResourceStructure resourceStructure,
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
            sendDeployNotificationsForResource(resourceStructure, DistributionStatusEnum.DEPLOY_ERROR, e.getMessage());
            throw e;
        }

        if (resourceStructure.isDeployedSuccessfully() || toscaResourceStructure.isDeployedSuccessfully()) {
            logger.info(LoggingAnchor.SIX, MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC.toString(),
                    resourceStructure.getResourceInstance().getResourceName(),
                    resourceStructure.getResourceInstance().getResourceUUID(), resourceStructure.getNumberOfResources(),
                    "ASDC", "deployResourceStructure");
            sendDeployNotificationsForResource(resourceStructure, DistributionStatusEnum.DEPLOY_OK, null);
        }

    }


    private enum NotificationType {
        DOWNLOAD, DEPLOY
    }

    protected void sendASDCNotification(NotificationType notificationType, String artifactURL, String consumerID,
            String distributionID, DistributionStatusEnum status, String errorReason, long timestamp) {

        String event = "Sending " + notificationType.name() + "(" + status.name() + ")"
                + " notification to ASDC for artifact:" + artifactURL;

        if (errorReason != null) {
            event = event + "(" + errorReason + ")";
        }
        logger.info(LoggingAnchor.SIX, MessageEnum.ASDC_SEND_NOTIF_ASDC.toString(), notificationType.name(),
                status.name(), artifactURL, "ASDC", "sendASDCNotification");
        logger.debug(event);


        try {
            IDistributionStatusMessage message =
                    new DistributionStatusMessage(artifactURL, consumerID, distributionID, status, timestamp);
            if (errorReason != null) {
                sendNotificationWithMessageAndErrorReason(notificationType, errorReason, message);
            } else {
                sendNotificationWithMessage(notificationType, message);
            }
        } catch (RuntimeException e) {
            logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                    "sendASDCNotification", ErrorCode.SchemaError.getValue(), "RuntimeException - sendASDCNotification",
                    e);
        }
    }

    private void sendNotificationWithMessage(NotificationType notificationType, IDistributionStatusMessage message) {
        switch (notificationType) {
            case DOWNLOAD:
                this.distributionClient.sendDownloadStatus(message);
                break;
            case DEPLOY:
                this.distributionClient.sendDeploymentStatus(message);
                break;
            default:
                break;
        }
    }

    private void sendNotificationWithMessageAndErrorReason(NotificationType notificationType, String errorReason,
            IDistributionStatusMessage message) {
        switch (notificationType) {
            case DOWNLOAD:
                this.distributionClient.sendDownloadStatus(message, errorReason);
                break;
            case DEPLOY:
                this.distributionClient.sendDeploymentStatus(message, errorReason);
                break;
            default:
                break;
        }
    }

    protected void sendFinalDistributionStatus(String distributionID, DistributionStatusEnum status,
            String errorReason) {

        logger.debug("Enter sendFinalDistributionStatus with DistributionID " + distributionID + " and Status of "
                + status.name() + " and ErrorReason " + errorReason);

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
            logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                    "sendASDCNotification", ErrorCode.SchemaError.getValue(), "RuntimeException - sendASDCNotification",
                    e);
        }
    }

    private Optional<String> getNotificationJson(INotificationData iNotif) {
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
        logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_RECEIVE_CALLBACK_NOTIF.toString(), noOfArtifacts,
                iNotif.getServiceUUID(), "ASDC");
        try {

            if (iNotif.getDistributionID() != null && !iNotif.getDistributionID().isEmpty()) {
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, iNotif.getDistributionID());
            }
            logger.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF.toString(), iNotif.getServiceUUID(),
                    "ASDC", "treatNotification");

            this.changeControllerStatus(ASDCControllerStatus.BUSY);
            Optional<String> notificationMessage = getNotificationJson(iNotif);
            toscaInstaller.processWatchdog(iNotif.getDistributionID(), iNotif.getServiceUUID(), notificationMessage,
                    asdcConfig.getConsumerID());

            // Process only the Resource artifacts in MSO
            this.processResourceNotification(iNotif);

            // ********************************************************************************************************
            // Start Watchdog loop and wait for all components to complete before reporting final status back.
            // **If timer expires first then we will report a Distribution Error back to ASDC
            // ********************************************************************************************************
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

                if (distributionStatus != null
                        && !distributionStatus.equalsIgnoreCase(DistributionStatus.INCOMPLETE.name())) {

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
                logger.debug("Timeout of {} seconds was reached before all components reported status",
                        watchDogTimeout);
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

            wd.updateCatalogDBStatus(iNotif.getServiceInvariantUUID(), overallStatus);

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

            logger.debug(
                    "OptimisticLockingFailure for DistributionId: {} Another process "
                            + "has already altered this distribution, so not going to process it on this site.",
                    iNotif.getDistributionID());
            logger.error(LoggingAnchor.FIVE, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                    "Database concurrency exception: ", "ASDC", "treatNotification",
                    ErrorCode.BusinessProcessError.getValue(), "RuntimeException in treatNotification", e);

        } catch (Exception e) {
            logger.error("", MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                    "Unexpected exception caught during the notification processing", "ASDC", "treatNotification",
                    ErrorCode.SchemaError.getValue(), "RuntimeException in treatNotification", e);

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
        DistributionStatusEnum deployStatus = DistributionStatusEnum.DEPLOY_OK;
        String errorMessage = null;
        boolean serviceDeployed = false;

        try {
            this.processCsarServiceArtifacts(iNotif, toscaResourceStructure);
            if (isCsarAlreadyDeployed(iNotif, toscaResourceStructure)) {
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

                    if (!this.checkResourceAlreadyDeployed(resourceStructure, serviceDeployed)) {
                        logger.debug("Processing Resource Type: " + resourceType + " and Model UUID: "
                                + resourceStructure.getResourceInstance().getResourceUUID());


                        if ("VF".equals(resourceType)) {
                            hasVFResource = true;
                            for (IArtifactInfo artifact : resource.getArtifacts()) {
                                IDistributionClientDownloadResult resultArtifact =
                                        this.downloadTheArtifact(artifact, iNotif.getDistributionID());
                                if (resultArtifact == null) {
                                    continue;
                                }

                                if (ASDCConfiguration.VF_MODULES_METADATA.equals(artifact.getArtifactType())) {
                                    logger.debug("VF_MODULE_ARTIFACT: "
                                            + new String(resultArtifact.getArtifactPayload(), StandardCharsets.UTF_8));
                                    logger.debug(ASDCNotificationLogging
                                            .dumpVfModuleMetaDataList(((VfResourceStructure) resourceStructure)
                                                    .decodeVfModuleArtifact(resultArtifact.getArtifactPayload())));
                                }
                                if (!ASDCConfiguration.WORKFLOW.equals(artifact.getArtifactType())) {
                                    resourceStructure.addArtifactToStructure(distributionClient, artifact,
                                            resultArtifact);
                                } else {
                                    writeArtifactToFile(artifact, resultArtifact);
                                    logger.debug(
                                            "Adding workflow artifact to structure: " + artifact.getArtifactName());
                                    resourceStructure.addWorkflowArtifactToStructure(artifact, resultArtifact);
                                }

                            }

                            // Deploy VF resource and artifacts
                            logger.debug("Preparing to deploy Service: {}", iNotif.getServiceUUID());


                            this.deployResourceStructure(resourceStructure, toscaResourceStructure);
                            serviceDeployed = true;
                        }
                    }

                } catch (ArtifactInstallerException e) {
                    deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
                    errorMessage = e.getMessage();
                    logger.error("Exception occurred", e);
                }

                if (!hasVFResource) {

                    logger.debug("No resources found for Service: " + iNotif.getServiceUUID());

                    logger.debug("Preparing to deploy Service: {}", iNotif.getServiceUUID());
                    try {
                        this.deployResourceStructure(resourceStructure, toscaResourceStructure);
                        serviceDeployed = true;
                    } catch (ArtifactInstallerException e) {
                        deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
                        errorMessage = e.getMessage();
                        logger.error("Exception occurred", e);
                    }
                }
            }

            this.sendCsarDeployNotification(resourceStructure, toscaResourceStructure, deployStatus, errorMessage);

        } catch (ASDCDownloadException | UnsupportedEncodingException e) {
            logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception caught during Installation of artifact", "ASDC", "processResourceNotification",
                    ErrorCode.BusinessProcessError.getValue(), "Exception in processResourceNotification", e);
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

    private String getMsoConfigPath() {
        String msoConfigPath = System.getProperty("mso.config.path");
        if (msoConfigPath == null) {
            logger.info("Unable to find the system property mso.config.path, use the default configuration");
            msoConfigPath = asdcConfig.getPropertyOrNull("mso.config.defaultpath");
        }
        if (msoConfigPath == null) {
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

                    IDistributionClientDownloadResult resultArtifact =
                            this.downloadTheArtifact(artifact, iNotif.getDistributionID());

                    writeArtifactToFile(artifact, resultArtifact);

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
                            this.downloadTheArtifact(artifact, iNotif.getDistributionID());

                    writeArtifactToFile(artifact, resultArtifact);

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
                            this.downloadTheArtifact(artifact, iNotif.getDistributionID());

                    writeArtifactToFile(artifact, resultArtifact);

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


    /**
     * @return the address of the ASDC we are connected to.
     */
    public String getAddress() {
        if (asdcConfig != null) {
            return asdcConfig.getSdcAddress();
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
                    logger.debug("Ready to parse this serviceArtifactUUID:  " + artifactInfo.getArtifactUUID());
                    String filePath = Paths.get(getMsoConfigPath(), "ASDC", artifactInfo.getArtifactVersion(),
                            artifactInfo.getArtifactName()).normalize().toString();
                    ZipParser zipParserInstance = ZipParser.getInstance();
                    artifactContent = zipParserInstance.parseJsonForZip(filePath);
                    logger.debug(
                            "serviceArtifact parsing success! serviceArtifactUUID: " + artifactInfo.getArtifactUUID());

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
            logger.error("serviceArtifact parse failure for service uuid:  "
                    + serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        } catch (Exception e) {
            logger.error("error NSST process resource failure ", e);
        }
    }
}
