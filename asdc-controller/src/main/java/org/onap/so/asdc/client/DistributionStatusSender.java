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

import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sends download/deploy/final distribution status notifications back to SDC (ASDC). Extracted from ASDCController so
 * the status-reporting concern is isolated and unit-testable; the distribution client is passed per call because it is
 * set at runtime on the controller.
 */
@Component
public class DistributionStatusSender {

    private static final Logger logger = LoggerFactory.getLogger(DistributionStatusSender.class);

    @Autowired
    private ASDCConfiguration asdcConfig;

    public enum NotificationType {
        DOWNLOAD, DEPLOY
    }

    public void sendDeployNotificationsForResource(IDistributionClient client, ResourceStructure resourceStructure,
            DistributionStatusEnum distribStatus, String errorReason) {

        for (IArtifactInfo artifactInfo : resourceStructure.getResourceInstance().getArtifacts()) {

            if ((DistributionStatusEnum.DEPLOY_OK.equals(distribStatus)
                    && !("OTHER").equalsIgnoreCase(artifactInfo.getArtifactType())
                    && !resourceStructure.isAlreadyDeployed())
                    // This could be NULL if the artifact is a VF module artifact, this won't be present in the MAP
                    && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()) != null
                    && resourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID())
                            .getDeployedInDb() == 0) {
                this.sendASDCNotification(client, NotificationType.DEPLOY, artifactInfo.getArtifactURL(),
                        resourceStructure.getNotification().getDistributionID(), DistributionStatusEnum.DEPLOY_ERROR,
                        "The artifact has not been used by the modules defined in the resource",
                        System.currentTimeMillis());
            } else {
                this.sendASDCNotification(client, NotificationType.DEPLOY, artifactInfo.getArtifactURL(),
                        resourceStructure.getNotification().getDistributionID(), distribStatus, errorReason,
                        System.currentTimeMillis());
            }
        }
    }

    public void sendCsarDeployNotification(IDistributionClient client, ResourceStructure resourceStructure,
            ToscaResourceStructure toscaResourceStructure, DistributionStatusEnum statusEnum, String errorReason) {

        IArtifactInfo csarArtifact = toscaResourceStructure.getToscaArtifact();

        this.sendASDCNotification(client, NotificationType.DEPLOY, csarArtifact.getArtifactURL(),
                resourceStructure.getNotification().getDistributionID(), statusEnum, errorReason,
                System.currentTimeMillis());

    }

    public void sendASDCNotification(IDistributionClient client, NotificationType notificationType, String artifactURL,
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
            IDistributionStatusMessage message = new DistributionStatusMessage(artifactURL, asdcConfig.getConsumerID(),
                    distributionID, status, timestamp);
            if (errorReason != null) {
                sendNotificationWithMessageAndErrorReason(client, notificationType, errorReason, message);
            } else {
                sendNotificationWithMessage(client, notificationType, message);
            }
        } catch (RuntimeException e) {
            logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                    "sendASDCNotification", ErrorCode.SchemaError.getValue(), "RuntimeException - sendASDCNotification",
                    e);
        }
    }

    private void sendNotificationWithMessage(IDistributionClient client, NotificationType notificationType,
            IDistributionStatusMessage message) {
        switch (notificationType) {
            case DOWNLOAD:
                client.sendDownloadStatus(message);
                break;
            case DEPLOY:
                client.sendDeploymentStatus(message);
                break;
            default:
                break;
        }
    }

    private void sendNotificationWithMessageAndErrorReason(IDistributionClient client,
            NotificationType notificationType, String errorReason, IDistributionStatusMessage message) {
        switch (notificationType) {
            case DOWNLOAD:
                client.sendDownloadStatus(message, errorReason);
                break;
            case DEPLOY:
                client.sendDeploymentStatus(message, errorReason);
                break;
            default:
                break;
        }
    }

    public void sendFinalDistributionStatus(IDistributionClient client, String distributionID,
            DistributionStatusEnum status, String errorReason) {

        logger.debug("Enter sendFinalDistributionStatus with DistributionID {} and Status of {} and ErrorReason {}",
                distributionID, status.name(), errorReason);

        long subStarttime = System.currentTimeMillis();
        try {

            IFinalDistrStatusMessage finalDistribution = new FinalDistributionStatusMessage(distributionID, status,
                    subStarttime, asdcConfig.getConsumerID());

            if (errorReason == null) {
                client.sendFinalDistrStatus(finalDistribution);
            } else {
                client.sendFinalDistrStatus(finalDistribution, errorReason);
            }


        } catch (RuntimeException e) {
            logger.debug("Exception caught in sendFinalDistributionStatus {}", e.getMessage());
            logger.warn(LoggingAnchor.FIVE, MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC.toString(), "ASDC",
                    "sendASDCNotification", ErrorCode.SchemaError.getValue(), "RuntimeException - sendASDCNotification",
                    e);
        }
    }
}
