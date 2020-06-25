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

/*-
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */
package org.onap.so.heatbridge.openstack.factory;

import com.google.common.base.Preconditions;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientException;
import org.onap.so.heatbridge.openstack.api.OpenstackV2ClientImpl;
import org.onap.so.heatbridge.openstack.api.OpenstackV3ClientImpl;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.model.common.Identifier;

public class OpenstackClientFactoryImpl implements OpenstackClientFactory {

    @Override
    public OpenstackClient createOpenstackV3Client(OpenstackAccess osAccess) throws OpenstackClientException {
        Preconditions.checkNotNull(osAccess.getUrl(), "Keystone-v3 Auth: endpoint not set.");
        Preconditions.checkNotNull(osAccess.getUser(), "Keystone-v3 Auth: username not set.");
        Preconditions.checkNotNull(osAccess.getPassword(), "Keystone-v3 Auth: password not set.");
        Preconditions.checkNotNull(osAccess.getDomainNameIdentifier(), "Keystone-v3 Auth: domain not set.");
        Preconditions.checkNotNull(osAccess.getRegion(), "Keystone-v3 Auth: region not set.");
        Preconditions.checkNotNull(osAccess.getTenantId(), "Keystone-v3 Auth: tenant-id not set.");

        OSClientV3 client;
        try {
            OSFactory.enableHttpLoggingFilter(true);
            client = OSFactory.builderV3().endpoint(osAccess.getUrl())
                    .credentials(osAccess.getUser(), osAccess.getPassword(), osAccess.getDomainNameIdentifier())
                    .scopeToProject(Identifier.byId(osAccess.getTenantId()), osAccess.getProjectNameIdentifier())
                    .authenticate().useRegion(osAccess.getRegion());
            return new OpenstackV3ClientImpl(client);
        } catch (AuthenticationException exception) {
            throw new OpenstackClientException("Failed to authenticate with Keystone-v3: " + osAccess.getUrl(),
                    exception);
        }
    }

    @Override
    public OpenstackClient createOpenstackV2Client(OpenstackAccess osAccess) throws OpenstackClientException {
        Preconditions.checkNotNull(osAccess.getUrl(), "Keystone-v2 Auth: endpoint not set.");
        Preconditions.checkNotNull(osAccess.getUser(), "Keystone-v2 Auth: username not set.");
        Preconditions.checkNotNull(osAccess.getPassword(), "Keystone-v2 Auth: password not set.");
        Preconditions.checkNotNull(osAccess.getTenantId(), "Keystone-v2 Auth: tenant-id not set.");
        Preconditions.checkNotNull(osAccess.getRegion(), "Keystone-v2 Auth: region not set.");

        OSClientV2 client;
        try {
            OSFactory.enableHttpLoggingFilter(true);
            client = OSFactory.builderV2().endpoint(osAccess.getUrl())
                    .credentials(osAccess.getUser(), osAccess.getPassword()).tenantId(osAccess.getTenantId())
                    .authenticate().useRegion(osAccess.getRegion());
            return new OpenstackV2ClientImpl(client);
        } catch (AuthenticationException exception) {
            throw new OpenstackClientException("Failed to authenticate with Keystone-v2.0: " + osAccess.getUrl(),
                    exception);
        }
    }
}
