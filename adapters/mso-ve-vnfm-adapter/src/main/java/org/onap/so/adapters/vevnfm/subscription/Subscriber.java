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

import java.util.Arrays;

@Component
public class Subscriber {

    private static final char COLON = ':';

    @Value("${notification.url}")
    private String notificationUrl;

    @Value("${server.port}")
    private String serverPort;

    @Value("${my.address}")
    private String myAddress;

    @Value("${my.auth-type}")
    private String myAuthType;

    @Value("${my.user}")
    private String myUser;

    @Value("${my.password}")
    private String myPassword;

    @Autowired
    private SubscribeSender sender;

    public boolean subscribe() {
        final LccnSubscriptionRequest request = createRequest();
        return sender.send(request);
    }

    private LccnSubscriptionRequest createRequest() {
        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();
        request.callbackUri(getCallbackUri());

        final SubscriptionsAuthenticationParamsBasic paramsBasic = new SubscriptionsAuthenticationParamsBasic();
        paramsBasic.setUserName(myUser);
        paramsBasic.setPassword(myPassword);

        final SubscriptionsAuthentication authentication = new SubscriptionsAuthentication();
        authentication.setAuthType(Arrays.asList(SubscriptionsAuthentication.AuthTypeEnum.fromValue(myAuthType)));
        authentication.setParamsBasic(paramsBasic);
        request.authentication(authentication);

        return request;
    }

    private String getCallbackUri() {
        return myAddress + COLON + serverPort + notificationUrl;
    }
}
