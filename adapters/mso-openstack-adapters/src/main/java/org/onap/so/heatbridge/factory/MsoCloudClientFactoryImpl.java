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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge.factory;

import java.util.Objects;
import jakarta.annotation.Nonnull;
import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess.OpenstackAccessBuilder;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientException;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactory;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements {@link MsoCloudClientFactory} It loads the cloud configuration from SO and uses it to
 * authenticate with keystone. As a result of authentication with keystone, it returns the Openstack client with the
 * auth token so that subsequent API calls to Openstack can be made.
 */
public class MsoCloudClientFactoryImpl implements MsoCloudClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(MsoCloudClientFactoryImpl.class);

    private OpenstackClientFactory openstackClientFactory;

    public MsoCloudClientFactoryImpl(@Nonnull OpenstackClientFactory openstackClientFactory) {
        Objects.requireNonNull(openstackClientFactory, "Null OpenstackClientFactory object");
        this.openstackClientFactory = openstackClientFactory;
    }

    @Override
    public OpenstackClient getOpenstackClient(@Nonnull String url, @Nonnull String msoId, @Nonnull String msoPass,
            @Nonnull String regionId, @Nonnull String tenantId, @Nonnull String keystoneVersion, String userDomainName,
            String projectDomainName) throws HeatBridgeException {
        Objects.requireNonNull(url, "Null openstack url!");
        Objects.requireNonNull(msoId, "Null openstack user id!");
        Objects.requireNonNull(msoPass, "Null openstack password!");
        Objects.requireNonNull(regionId, "Null regionId ID!");
        Objects.requireNonNull(tenantId, "Null tenant ID!");
        Objects.requireNonNull(keystoneVersion, "Null keystone version");
        if (userDomainName == null) {
            userDomainName = HeatBridgeConstants.OS_DEFAULT_DOMAIN_NAME;
        }
        if (projectDomainName == null) {
            projectDomainName = HeatBridgeConstants.OS_DEFAULT_DOMAIN_NAME;
        }
        try {
            final OpenstackAccess osAccess = new OpenstackAccessBuilder().setBaseUrl(url) // keystone URL
                    .setUser(msoId) // keystone username
                    .setPassword(CryptoUtils.decryptCloudConfigPassword(msoPass)) // keystone decrypted password
                    .setRegion(regionId) // openstack region
                    .setDomainName(userDomainName).setProjectName(projectDomainName).setTenantId(tenantId) // tenantId
                    .build();

            // Identify the Keystone version
            if (keystoneVersion.equals(HeatBridgeConstants.OS_KEYSTONE_V2_KEY)) {
                return openstackClientFactory.createOpenstackV2Client(osAccess);
            } else if (keystoneVersion.equals(HeatBridgeConstants.OS_KEYSTONE_V3_KEY)) {
                return openstackClientFactory.createOpenstackV3Client(osAccess);
            }
            throw new OpenstackClientException("Unsupported keystone version! " + keystoneVersion);
        } catch (OpenstackClientException osClientEx) {
            logger.error("Error creating OS Client", osClientEx);
            throw new HeatBridgeException("Client error when authenticating with the Openstack", osClientEx);
        }
    }
}
