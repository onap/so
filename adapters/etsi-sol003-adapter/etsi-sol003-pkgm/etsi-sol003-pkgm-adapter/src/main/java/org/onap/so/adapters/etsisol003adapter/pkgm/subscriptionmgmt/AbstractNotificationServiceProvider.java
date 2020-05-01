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
package org.onap.so.adapters.etsisol003adapter.pkgm.subscriptionmgmt;

import static org.onap.so.adapters.etsisol003adapter.pkgm.extclients.vnfm.VnfmHttpServiceProviderConfiguration.VNFM_ADAPTER_HTTP_SERVICE_PROVIDER_BEAN;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A base class that can be extended by classes for providing notification services. Provides common methods that will
 * be useful to those classes.
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
public abstract class AbstractNotificationServiceProvider {

    @Autowired
    @Qualifier(VNFM_ADAPTER_HTTP_SERVICE_PROVIDER_BEAN)
    private HttpRestServiceProvider httpRestServiceProvider;

    protected HttpRestServiceProvider getHttpRestServiceProvider() {
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
