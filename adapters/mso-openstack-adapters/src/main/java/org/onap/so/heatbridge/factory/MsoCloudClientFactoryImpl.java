/*
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
package org.onap.so.heatbridge.factory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess.OpenstackAccessBuilder;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientException;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactory;
import org.onap.so.utils.CryptoUtils;

/**
 * This class implements {@link MsoCloudClientFactory}
 * It loads the cloud configuration from SO and uses it to authenticate with keystone.
 * As a result of authentication with keystone, it returns the Openstack client with the auth token so that
 * subsequent API calls to Openstack can be made.
 */
public class MsoCloudClientFactoryImpl implements MsoCloudClientFactory {

    private OpenstackClientFactory openstackClientFactory;

    public MsoCloudClientFactoryImpl(@Nonnull OpenstackClientFactory openstackClientFactory) {
        Objects.requireNonNull(openstackClientFactory, "Null OpenstackClientFactory object");
        this.openstackClientFactory = openstackClientFactory;
    }
    @Override
    public OpenstackClient getOpenstackClient(@Nonnull String url, @Nonnull String msoId, @Nonnull String msoPass, @Nonnull String cloudRegionId, @Nonnull String tenantId) throws
        HeatBridgeException {
        Objects.requireNonNull(url, "Null openstack url!");
        Objects.requireNonNull(msoId, "Null openstack user id!");
        Objects.requireNonNull(msoPass, "Null openstack password!");
        Objects.requireNonNull(cloudRegionId, "Null cloud-region ID!");
        Objects.requireNonNull(tenantId, "Null tenant ID!");
        try {
            //CloudSite cloudSite = cloudConfigFactory.getCloudConfig().getCloudSite(cloudRegionId);
            //Get openstack credentials dynamically for a given cloud
            //String regionId = "tenlab-nfvi";
            //CloudIdentity cloudIdentity = cloudSite.getIdentityService();
            final OpenstackAccess osAccess = new OpenstackAccessBuilder()
                .setBaseUrl(url) // keystone URL
                .setUser(msoId) // keystone username
                .setPassword(CryptoUtils.decryptCloudConfigPassword(msoPass)) // keystone decrypted password
                .setRegion(cloudRegionId) // openstack region
                .setDomainName(HeatBridgeConstants.OS_DEFAULT_DOMAIN_NAME) // hardcode to "default"
                .setTenantId(tenantId) // tenantId
                .build();

            // Identify the Keystone version
            String version = new URL(url).getPath().replace("/", "");
            if (version.equals(HeatBridgeConstants.OS_KEYSTONE_V2_KEY)) {
                return openstackClientFactory.createOpenstackV2Client(osAccess);
            } else if (version.equals(HeatBridgeConstants.OS_KEYSTONE_V3_KEY)) {
                return openstackClientFactory.createOpenstackV3Client(osAccess);
            }
            throw new OpenstackClientException("Unsupported keystone version!");
        } catch (MalformedURLException e) {
            throw new HeatBridgeException("Malformed Keystone Endpoint in SO configuration.", e);
        } catch (OpenstackClientException osClientEx) {
            throw new HeatBridgeException("Client error when authenticating with the Openstack V3.", osClientEx);
        }
    }
}
