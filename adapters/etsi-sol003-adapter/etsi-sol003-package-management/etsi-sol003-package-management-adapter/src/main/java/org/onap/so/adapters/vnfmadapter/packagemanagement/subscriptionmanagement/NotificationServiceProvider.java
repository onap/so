/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement;

import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsAuthentication.AuthTypeEnum;

/**
 * Interface which lays out requirements for a Notification Service Provider
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
public interface NotificationServiceProvider {

    /**
     * Method to send a notification to a uri, given the subscription authentication
     * 
     * @param notification The notification to send
     * @param subscriptionsAuthentication Object containing the authentication details
     * @param callbackUri The uri to send the notification to
     * @return true if notification is delivered successfully, otherwise false
     */
    boolean send(final Object notification, final SubscriptionsAuthentication subscriptionsAuthentication,
            final String callbackUri);

    /**
     * Method to get the supported authorization type of the service provider
     * 
     * @return the supported AuthTypeEnum
     */
    AuthTypeEnum getAuthType();

}
