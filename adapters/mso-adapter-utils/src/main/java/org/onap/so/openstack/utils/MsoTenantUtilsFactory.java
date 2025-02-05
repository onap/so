/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

@Component
public class MsoTenantUtilsFactory {

    protected static Logger logger = LoggerFactory.getLogger(MsoTenantUtilsFactory.class);
    @Autowired
    protected CloudConfig cloudConfig;
    @Autowired
    @Lazy
    protected MsoKeystoneUtils keystoneUtils;
    @Autowired
    @Lazy
    protected MsoKeystoneV3Utils keystoneV3Utils;

    // based on Cloud IdentityServerType returns ORM or KEYSTONE Utils
    public MsoTenantUtils getTenantUtils(String cloudSiteId) throws MsoCloudSiteNotFound {
        CloudSite cloudSite =
                cloudConfig.getCloudSite(cloudSiteId).orElseThrow(() -> new MsoCloudSiteNotFound(cloudSiteId));

        return getTenantUtilsByServerType(cloudSite.getIdentityService().getIdentityServerType());
    }

    public MsoTenantUtils getTenantUtilsByServerType(ServerType serverType) {

        MsoTenantUtils tenantU = null;
        if (ServerType.KEYSTONE.equals(serverType)) {
            tenantU = keystoneUtils;
        } else if (ServerType.KEYSTONE_V3.equals(serverType)) {
            tenantU = keystoneV3Utils;
        }
        return tenantU;
    }
}
