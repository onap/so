/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.sdc.api.consumer.IStatusCallback;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ASDCStatusCallBack implements IStatusCallback {

    @Autowired
    private ToscaResourceInstaller toscaInstaller;

    protected static final Logger logger = LoggerFactory.getLogger(ASDCStatusCallBack.class);

    @Autowired
    private WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;

    @Override
    public void activateCallback(IStatusData iStatus) {
        String event = "Receive a callback componentStatus in ASDC, for componentName: " + iStatus.getComponentName()
                + " and status of " + iStatus.getStatus() + " distributionID of " + iStatus.getDistributionID();

        try {

            if (iStatus.getStatus() == null) {
                return;
            }
            if (!iStatus.getStatus().equals(DistributionStatusEnum.COMPONENT_DONE_OK)
                    && !iStatus.getStatus().equals(DistributionStatusEnum.COMPONENT_DONE_ERROR)) {
                return;
            }
            WatchdogDistributionStatus watchdogDistributionStatus =
                    watchdogDistributionStatusRepository.findById(iStatus.getDistributionID()).orElseGet(() -> null);
            if (watchdogDistributionStatus == null) {
                watchdogDistributionStatus = new WatchdogDistributionStatus();
                watchdogDistributionStatus.setDistributionId(iStatus.getDistributionID());
                watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
            }
            logger.debug(event);
            toscaInstaller.installTheComponentStatus(iStatus);

        } catch (ArtifactInstallerException e) {
            logger.error("Error in ASDCStatusCallback {}", e.getMessage(), e);
            logger.debug("Error in ASDCStatusCallback {}", e.getMessage());
        }
    }
}
