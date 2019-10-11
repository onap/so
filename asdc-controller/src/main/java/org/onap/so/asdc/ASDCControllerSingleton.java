/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc;

import java.security.SecureRandom;
import javax.annotation.PreDestroy;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.asdc.client.ASDCController;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.ScheduledTasksMDCSetup;
import org.onap.so.utils.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Profile("!test")
public class ASDCControllerSingleton {

    private static final Logger logger = LoggerFactory.getLogger(ASDCControllerSingleton.class);
    private final ASDCController asdcController;

    @Autowired
    private ScheduledTasksMDCSetup scheduledMDCSetup;

    @Autowired
    public ASDCControllerSingleton(final ASDCController asdcController) {
        this.asdcController = asdcController;
    }

    @Scheduled(fixedRate = 50000)
    public void periodicControllerTask() {
        scheduledMDCSetup.mdcSetup(Components.ASDC_CONTROLLER, "periodicControllerTask");
        try {
            final int randomNumber = new SecureRandom().nextInt(Integer.MAX_VALUE);
            asdcController.setControllerName("mso-controller" + randomNumber);
            if (asdcController.isStopped()) {
                logger.info("{} not running will try to initialize it, currrent status: {}",
                        asdcController.getClass().getName(), asdcController.getControllerStatus());
                asdcController.initASDC();
            }
        } catch (final ASDCControllerException controllerException) {
            scheduledMDCSetup.errorMDCSetup(ErrorCode.UnknownError, controllerException.getMessage());
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
            logger.error("Exception occurred", controllerException);
        }
        scheduledMDCSetup.exitAndClearMDC();
    }

    @PreDestroy
    private void terminate() {
        try {
            asdcController.closeASDC();
        } catch (final ASDCControllerException controllerException) {
            logger.error("Exception occurred", controllerException);
        }
    }

}
