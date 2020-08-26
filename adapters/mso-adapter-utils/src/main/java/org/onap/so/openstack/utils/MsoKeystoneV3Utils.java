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
import org.onap.so.cloud.authentication.AuthenticationMethodFactory;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.woorea.openstack.keystone.v3.model.Token;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.v3.Keystone;
import com.woorea.openstack.keystone.v3.api.TokensResource.Authenticate;
import com.woorea.openstack.keystone.v3.model.Authentication;
import com.woorea.openstack.keystone.v3.model.Authentication.Identity;

@Component
public class MsoKeystoneV3Utils extends MsoTenantUtils {

    @Autowired
    private AuthenticationMethodFactory authenticationMethodFactory;

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

    public Token getKeystoneToken(CloudSite cloudSite) throws MsoException {
        try {
            CloudIdentity cloudIdentity = cloudSite.getIdentityService();

            Keystone keystone = new Keystone(cloudIdentity.getIdentityUrl());

            Authentication auth = authenticationMethodFactory.getAuthenticationForV3(cloudIdentity);

            Authenticate authenticate = keystone.tokens().authenticate(auth);
            return executeAndRecordOpenstackRequest(authenticate);

        } catch (OpenStackResponseException e) {
            throw keystoneErrorToMsoException(e, "TokenAuth");
        } catch (OpenStackConnectException e) {
            throw keystoneErrorToMsoException(e, "TokenAuth");
        }
    }

}
