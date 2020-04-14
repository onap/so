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

import com.squareup.okhttp.Credentials;
import java.util.Collections;
import org.apache.logging.log4j.util.Strings;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.onap.so.adapters.vevnfm.provider.AuthorizationHeadersProvider;
<<<<<<< HEAD   (8fe8b7 Merge "Correcting ControllerExecutionBB Id" into frankfurt)
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
=======
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.model.SubscriptionsFilter;
>>>>>>> CHANGE (028879 Refactor SOL003 Adapter to organize its modules)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    @Value("${vevnfmadapter.endpoint}")
    private String endpoint;

    @Value("${vnfm.notification}")
    private String notification;

    @Value("${spring.security.usercredentials[0].username}")
    private String username;

    @Value("${spring.security.usercredentials[0].openpass}")
    private String openpass;

    @Autowired
    private AuthorizationHeadersProvider headersProvider;

    @Autowired
    private SubscribeSender sender;

    private static String getAuthorization(final EsrSystemInfo info) {
        if (info == null) {
            return null;
        }

        final String userName = info.getUserName();

        if (Strings.isBlank(userName)) {
            return null;
        }

        final String password = info.getPassword();
        return Credentials.basic(userName, password);
    }

    public String subscribe(final EsrSystemInfo info) throws VeVnfmException {
        try {
            headersProvider.addAuthorization(getAuthorization(info));
            final LccnSubscriptionRequest request = createRequest();
            return sender.send(info, request);
        } catch (Exception e) {
            throw new VeVnfmException(e);
        } finally {
            headersProvider.removeAuthorization();
        }
    }

    public boolean checkSubscription(final EsrSystemInfo info, final String id) throws VeVnfmException {
        try {
            return sender.check(info, id);
        } catch (Exception e) {
            throw new VeVnfmException(e);
        }
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
        return endpoint + notification;
    }
}
