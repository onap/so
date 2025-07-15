/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.aaiclient.spring.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aai")
public class AAIConfigurationProperties implements AAIProperties {

    /**
     * Default URI used when the configured URI is {@code null}.
     */
    private static final URL DEFAULT_URI;
    private static final String DEFAULT_API_VERSION;

    static {
        try {
            DEFAULT_URI = new URL("http://localhost:8080");
            DEFAULT_API_VERSION = "v24";
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL endpoint;
    private String systemName;
    @Pattern(regexp = "^v[0-9]+$")
    private String apiVersion;
    private String auth;
    private String key;

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return (this.endpoint != null) ? this.endpoint : DEFAULT_URI;
    }

    @Override
    public String getSystemName() {
        return this.systemName;
    }

    @Override
    public AAIVersion getDefaultVersion() {
        String version = (this.apiVersion != null) ? this.apiVersion : DEFAULT_API_VERSION;
        return AAIVersion.valueOf(version.toUpperCase());
    }

    public void setDefaultVersion(String defaultVersion) {
        this.apiVersion = defaultVersion;
    }

    @Override
    public String getAuth() {
        return this.auth;
    }

    @Override
    public String getKey() {
        return this.key;
    }

}
