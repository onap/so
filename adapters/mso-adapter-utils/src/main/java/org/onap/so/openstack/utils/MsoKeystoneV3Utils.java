/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.Map;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.stereotype.Component;

@Component
public class MsoKeystoneV3Utils extends MsoTenantUtils {

    @Override
    public String createTenant(String tenantName, String cloudSiteId, Map<String, String> metadata, boolean backout)
            throws MsoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MsoTenant queryTenant(String tenantId, String cloudSiteId) throws MsoException, MsoCloudSiteNotFound {
        throw new UnsupportedOperationException();
    }

    @Override
    public MsoTenant queryTenantByName(String tenantName, String cloudSiteId)
            throws MsoException, MsoCloudSiteNotFound {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteTenant(String tenantId, String cloudSiteId) throws MsoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKeystoneUrl(String regionId, CloudIdentity cloudIdentity) throws MsoException {
        return cloudIdentity.getIdentityUrl();
    }

}
