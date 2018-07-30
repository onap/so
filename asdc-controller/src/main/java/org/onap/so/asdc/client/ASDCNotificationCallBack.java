/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Inner class for Notification callback
 *
 *
 */
@Component
public final class ASDCNotificationCallBack implements INotificationCallback {

	@Autowired
    private ASDCController asdcController;
    
    protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,ASDCNotificationCallBack.class);

    /**
     * This method can be called multiple times at the same moment.
     * The controller must be thread safe !
     */
    @Override
    public void activateCallback (INotificationData iNotif) {
        long startTime = System.currentTimeMillis ();
        UUIDChecker.generateUUID (LOGGER);
        MsoLogger.setServiceName ("NotificationHandler");
        MsoLogger.setLogContext (iNotif.getDistributionID (), iNotif.getServiceUUID ());
        String event = "Receive a callback notification in ASDC, nb of resources: " + iNotif.getResources ().size ();
        LOGGER.debug(event);
        asdcController.treatNotification (iNotif);
        LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Completed the treatment of the notification");
    }
}
