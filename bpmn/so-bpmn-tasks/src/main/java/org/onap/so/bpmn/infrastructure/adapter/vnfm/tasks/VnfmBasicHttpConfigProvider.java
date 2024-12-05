/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNFM_ADAPTER_DEFAULT_AUTH;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNFM_ADAPTER_DEFAULT_URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Provides VNFM adapter {@link java.net.URI} and basic authorization values
 * 
 * @author waqas.ikram@est.tech
 */
@Configuration
// @Primary
@ConfigurationProperties(prefix = "so.vnfm.adapter")
public class VnfmBasicHttpConfigProvider {

    private String url = VNFM_ADAPTER_DEFAULT_URL;

    private String auth = VNFM_ADAPTER_DEFAULT_AUTH;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * @return the auth
     */
    public String getAuth() {
        return auth;
    }

    /**
     * @param auth the auth to set
     */
    public void setAuth(final String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "EtsiVnfmAdapter [url=" + url + ", auth=" + auth + "]";
    }

}
