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


import java.util.Optional;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.activity.DeployActivitySpecs;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.installer.IVfResourceInstaller;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
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
    protected boolean isAsdcClientAutoManaged = false;
    protected String controllerName;

    private static final String UNKNOWN = "Unknown";
    private final ControllerState state = new ControllerState();

    @Autowired
    private ToscaResourceInstaller toscaInstaller;

    @Autowired
    private WatchdogDistributionStatusRepository wdsRepo;

    @Autowired
    private ASDCConfiguration asdcConfig;

    @Autowired
    private ASDCStatusCallBack asdcStatusCallBack;

    @Autowired
    private ASDCNotificationCallBack asdcNotificationCallBack;

    @Autowired
    private DistributionStatusSender statusSender;

    @Autowired
    private NotificationJsonMapper jsonMapper;

    @Autowired
    private ArtifactDownloader artifactDownloader;

    @Autowired
    private ResourceInstaller resourceInstaller;

    @Autowired
    private WatchdogStatusWaiter watchdogWaiter;

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
        return state.getNbOfNotificationsOngoing();
    }

    public IDistributionClient getDistributionClient() {
        return distributionClient;
    }

    public void setDistributionClient(IDistributionClient distributionClient) {
        this.distributionClient = distributionClient;
    }

    protected void changeControllerStatus(ASDCControllerStatus newControllerStatus) {
        state.changeControllerStatus(newControllerStatus);
    }

    public ASDCControllerStatus getControllerStatus() {
        return state.getControllerStatus();
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
        return state.isStopped();
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
        return state.isBusy();
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
            Optional<String> notificationMessage = jsonMapper.toJson(iNotif);
            toscaInstaller.processWatchdog(iNotif.getDistributionID(), iNotif.getServiceUUID(), notificationMessage,
                    asdcConfig.getConsumerID());

            // Process only the Resource artifacts in MSO
            resourceInstaller.processResourceNotification(distributionClient, iNotif);

            // ********************************************************************************************************
            // Wait for all components to complete before reporting final status back.
            // **If the timer expires first then we will report a Distribution Error back to ASDC
            // ********************************************************************************************************
            WatchdogStatusResult watchdogResult = watchdogWaiter.waitForComponents(iNotif.getDistributionID());
            String overallStatus = watchdogResult.getOverallStatus();
            String watchdogError = watchdogResult.getWatchdogError();
            boolean isDeploySuccess = watchdogResult.isDeploySuccess();

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
                statusSender.sendFinalDistributionStatus(distributionClient, iNotif.getDistributionID(),
                        DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK, null);
                WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
                wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.toString());
                wdsRepo.save(wds);
            } else {
                statusSender.sendFinalDistributionStatus(distributionClient, iNotif.getDistributionID(),
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

            statusSender.sendFinalDistributionStatus(distributionClient, iNotif.getDistributionID(),
                    DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, e.getMessage());

            WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
            wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.toString());
            wdsRepo.save(wds);

        } finally {
            this.changeControllerStatus(ASDCControllerStatus.IDLE);
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
}
