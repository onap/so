/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.tenantisolation;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.client.CacheProperties;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;

public class AaiClientPropertiesImpl implements AAIProperties {

    private String aaiEndpoint;
    private String auth;
    private String key;
    private Long readTimeout;
    private Long connectionTimeout;
    private boolean enableCaching;
    private Long cacheMaxAge;

    public AaiClientPropertiesImpl() {

        ApplicationContext context = SpringContextHelper.getAppContext();
        aaiEndpoint = context.getEnvironment().getProperty("mso.aai.endpoint");
        this.auth = context.getEnvironment().getProperty("aai.auth");
        this.key = context.getEnvironment().getProperty("mso.msoKey");
        this.readTimeout = context.getEnvironment().getProperty("aai.readTimeout", Long.class, 60000L);
        this.connectionTimeout = context.getEnvironment().getProperty("aai.connectionTimeout", Long.class, 60000L);
        this.enableCaching = context.getEnvironment().getProperty("aai.caching.enabled", Boolean.class, false);
        this.cacheMaxAge = context.getEnvironment().getProperty("aai.caching.maxAge", Long.class, 60000L);
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(aaiEndpoint);
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.LATEST;
    }

    @Override
    public String getAuth() {
        return this.auth;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Long getReadTimeout() {
        return this.readTimeout;
    }

    @Override
    public Long getConnectionTimeout() {
        return this.connectionTimeout;
    }

    @Override
    public boolean isCachingEnabled() {
        return this.enableCaching;
    }

    @Override
    public CacheProperties getCacheProperties() {
        return new AAICacheProperties() {
            @Override
            public Long getMaxAge() {
                return cacheMaxAge;
            }
        };
    }
}
