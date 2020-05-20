/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import org.onap.so.cloud.authentication.KeystoneAuthHolder;
import org.onap.so.openstack.exceptions.MsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import com.woorea.openstack.nova.Nova;


@Component
public class NovaClient extends MsoCommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(NovaClient.class);

    /**
     * Gets the Nova client
     *
     * @param cloudSiteId id of the cloud site
     * @param tenantId the tenant id
     * @return the Nova client
     * @throws MsoException the mso exception
     */
    @Cacheable(value = "novaClient")
    public Nova getNovaClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "compute");
        Nova novaClient = new Nova(keystone.getServiceUrl());
        novaClient.token(keystone.getId());
        return novaClient;
    }
}
