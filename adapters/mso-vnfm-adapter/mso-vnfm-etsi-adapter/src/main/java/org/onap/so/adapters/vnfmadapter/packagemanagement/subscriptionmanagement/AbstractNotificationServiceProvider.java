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

import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

/**
 * A base class that can be extended by classes for providing notification services. Provides common methods that will
 * be useful to those classes.
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
public abstract class AbstractNotificationServiceProvider {

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate restTemplate;

    protected HttpRestServiceProvider getHttpRestServiceProvider(final HttpHeadersProvider httpHeadersProvider) {
        final HttpRestServiceProvider httpRestServiceProvider =
                new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider);
        return httpRestServiceProvider;
    }

    protected BasicHttpHeadersProvider getBasicHttpHeadersProviderWithBasicAuth(final String username,
            final String password) {
        final byte[] encodedAuth = getBasicAuth(username, password);
        final String authHeader = "Basic " + new String(encodedAuth);
        return new BasicHttpHeadersProvider(authHeader);
    }

    protected byte[] getBasicAuth(final String username, final String password) {
        final String auth = username + ":" + password;
        return Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    }

}
