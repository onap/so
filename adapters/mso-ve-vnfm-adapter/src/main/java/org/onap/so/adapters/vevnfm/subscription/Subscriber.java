/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.subscription;

import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Subscriber {

    private static final char COLON = ':';

    @Value("${notification.url}")
    private String notificationUrl;

    @Value("${server.port}")
    private String serverPort;

    @Value("${system.url}")
    private String systemUrl;

    @Autowired
    private SubscribeSender sender;

    public boolean subscribe(final Map vnfm) {
        final LccnSubscriptionRequest request = createRequest();
        return sender.send(vnfm, request);
    }

    private LccnSubscriptionRequest createRequest() {
        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();
        request.callbackUri(getCallbackUri());
        final SubscriptionsAuthenticationParamsBasic paramsBasic = new SubscriptionsAuthenticationParamsBasic();
        final SubscriptionsAuthentication authentication = new SubscriptionsAuthentication();
        authentication.setParamsBasic(paramsBasic);
        request.authentication(authentication);

        return request;
    }

    private String getCallbackUri() {
        return systemUrl + COLON + serverPort + notificationUrl;
    }
}
