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

package org.onap.so.adapters.vevnfm.aai;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;

public class AaiPropertiesExt implements AAIProperties {

    private static final String MSO = "MSO";

    private final String endpoint;
    private final String encryptedBasicAuth;
    private final String encryptionKey;

    public AaiPropertiesExt() {
        final ApplicationContext context = SpringContextHelper.getAppContext();
        final ConfigProperties configProperties = context.getBean(ConfigProperties.class);
        this.endpoint = configProperties.getAaiEndpoint();
        this.encryptedBasicAuth = configProperties.getAaiAuth();
        this.encryptionKey = configProperties.getMsoKey();
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(endpoint);
    }

    @Override
    public String getSystemName() {
        return MSO;
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.V15;
    }

    @Override
    public String getAuth() {
        return encryptedBasicAuth;
    }

    @Override
    public String getKey() {
        return encryptionKey;
    }
}
