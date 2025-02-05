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

package org.onap.so.asdc.client;

import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.notification.INotificationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Inner class for Notification callback
 *
 *
 */
@Component
public final class ASDCNotificationCallBack implements INotificationCallback {

    @Autowired
    @Lazy
    private ASDCController asdcController;

    protected static final Logger logger = LoggerFactory.getLogger(ASDCNotificationCallBack.class);

    /**
     * This method can be called multiple times at the same moment. The controller must be thread safe !
     */
    @Override
    public void activateCallback(INotificationData iNotif) {
        String event = "Receive a callback notification in ASDC, nb of resources: " + iNotif.getResources().size();
        logger.debug(event);
        asdcController.treatNotification(iNotif);
    }
}
