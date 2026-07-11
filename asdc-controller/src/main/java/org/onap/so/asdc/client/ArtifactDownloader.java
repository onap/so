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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Downloads SDC (ASDC) artifacts and writes them to the local MSO config path. Extracted from ASDCController so the
 * download and file-IO concern is isolated and unit-testable; the distribution client is passed per call because it is
 * set at runtime on the controller.
 */
@Component
public class ArtifactDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactDownloader.class);
    private static final String UUID_PARAM = "(UUID:";

    @Autowired
    private ASDCConfiguration asdcConfig;

    @Autowired
    private DistributionStatusSender statusSender;

    public IDistributionClientDownloadResult downloadTheArtifact(IDistributionClient client, IArtifactInfo artifact,
            String distributionId) throws ASDCDownloadException {

        logger.info("Trying to download the artifact UUID: {} from URL: {}", artifact.getArtifactUUID(),
                artifact.getArtifactURL());
        IDistributionClientDownloadResult downloadResult;

        try {
            downloadResult = client.download(artifact);
            if (null == downloadResult) {
                logger.info(LoggingAnchor.TWO, MessageEnum.ASDC_ARTIFACT_NULL.toString(), artifact.getArtifactUUID());
                return downloadResult;
            }
        } catch (RuntimeException e) {
            logger.debug("Not able to download the artifact due to an exception: {}", artifact.getArtifactURL());
            statusSender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DOWNLOAD,
                    artifact.getArtifactURL(), distributionId, DistributionStatusEnum.DOWNLOAD_ERROR, e.getMessage(),
                    System.currentTimeMillis());

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

            statusSender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DOWNLOAD,
                    artifact.getArtifactURL(), distributionId, DistributionStatusEnum.DOWNLOAD_ERROR,
                    downloadResult.getDistributionMessageResult(), System.currentTimeMillis());

            throw new ASDCDownloadException("Artifact " + artifact.getArtifactName()
                    + " could not be downloaded from ASDC URL " + artifact.getArtifactURL() + UUID_PARAM
                    + artifact.getArtifactUUID() + ")" + System.lineSeparator() + "Error message is "
                    + downloadResult.getDistributionMessageResult() + System.lineSeparator());

        }

        statusSender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DOWNLOAD,
                artifact.getArtifactURL(), distributionId, DistributionStatusEnum.DOWNLOAD_OK, null,
                System.currentTimeMillis());
        return downloadResult;

    }

    public void writeArtifactToFile(IArtifactInfo artifact, IDistributionClientDownloadResult resultArtifact) {

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

    public String getMsoConfigPath() {
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
}
