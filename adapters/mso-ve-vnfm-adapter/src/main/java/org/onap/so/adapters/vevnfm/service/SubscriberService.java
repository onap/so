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

package org.onap.so.adapters.vevnfm.service;

import java.util.Collections;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.subscription.SubscribeSender;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    private static final char COLON = ':';

    @Value("${system.url}")
    private String systemUrl;

    @Value("${server.port}")
    private String serverPort;

    @Value("${notification.url}")
    private String notificationUrl;

    @Value("${spring.security.usercredentials[0].username}")
    private String username;

    @Value("${spring.security.usercredentials[0].openpass}")
    private String openpass;

    @Autowired
    private SubscribeSender sender;

    public boolean subscribe(final EsrSystemInfo info) {
        final LccnSubscriptionRequest request = createRequest();
        return sender.send(info, request);
    }

    private LccnSubscriptionRequest createRequest() {
        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();
        request.callbackUri(getCallbackUri());
        final SubscriptionsAuthenticationParamsBasic paramsBasic = new SubscriptionsAuthenticationParamsBasic();
        final SubscriptionsAuthentication authentication = new SubscriptionsAuthentication();
        paramsBasic.setUserName(username);
        paramsBasic.setPassword(openpass);
        authentication.setAuthType(Collections.singletonList(SubscriptionsAuthentication.AuthTypeEnum.BASIC));
        authentication.setParamsBasic(paramsBasic);
        request.authentication(authentication);

        return request;
    }

    private String getCallbackUri() {
        return systemUrl + COLON + serverPort + notificationUrl;
    }
}
