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

package org.onap.so.heatbridge.openstack.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.onap.so.openstack.utils.CinderClientImpl;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.NeutronClientImpl;
import org.onap.so.openstack.utils.NovaClientImpl;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Image;
import com.woorea.openstack.nova.model.Server;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Subnet;

public class OpenstackClientImpl implements OpenstackClient {

    private static final int NOT_FOUND = 404;

    private final NovaClientImpl novaClient;
    private final NeutronClientImpl neutronClient;
    private final CinderClientImpl cinderClient;
    private final MsoHeatUtils heatUtils;
    private final String cloudSiteId;
    private final String tenantId;

    public OpenstackClientImpl(NovaClientImpl novaClient, NeutronClientImpl neutronClient,
            CinderClientImpl cinderClient, MsoHeatUtils heatUtils, String cloudSiteId, String tenantId) {
        this.novaClient = novaClient;
        this.neutronClient = neutronClient;
        this.cinderClient = cinderClient;
        this.heatUtils = heatUtils;
        this.cloudSiteId = cloudSiteId;
        this.tenantId = tenantId;
    }

    @Override
    public Server getServerById(String serverId) {
        return query("server " + serverId, () -> novaClient.queryServerById(cloudSiteId, tenantId, serverId));
    }

    @Override
    public Image getImageById(String imageId) {
        return queryOrNullIfNotFound("image " + imageId,
                () -> novaClient.queryImageById(cloudSiteId, tenantId, imageId));
    }

    @Override
    public Flavor getFlavorById(String flavorId) {
        return queryOrNullIfNotFound("flavor " + flavorId,
                () -> novaClient.queryFlavorById(cloudSiteId, tenantId, flavorId));
    }

    @Override
    public Port getPortById(String portId) {
        return query("port " + portId, () -> neutronClient.queryPortById(cloudSiteId, tenantId, portId));
    }

    @Override
    public List<Resource> getStackBasedResources(String stackId, int nestingDepth) {
        return query("resources of stack " + stackId,
                () -> heatUtils.queryStackResources(cloudSiteId, tenantId, stackId, nestingDepth)).getList().stream()
                        .filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Network getNetworkById(String networkId) {
        return query("network " + networkId, () -> neutronClient.queryNetworkById(cloudSiteId, tenantId, networkId));
    }

    @Override
    public Subnet getSubnetById(String subnetId) {
        return query("subnet " + subnetId, () -> neutronClient.querySubnetById(cloudSiteId, tenantId, subnetId));
    }

    @Override
    public Volume getVolumeById(String volumeId) {
        return query("volume " + volumeId, () -> cinderClient.queryVolume(cloudSiteId, tenantId, volumeId));
    }

    @FunctionalInterface
    private interface Query<T> {
        T run() throws Exception;
    }

    private static <T> T query(String target, Query<T> query) {
        try {
            return query.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackClientException("Failed to query " + target, e);
        }
    }

    private static <T> T queryOrNullIfNotFound(String target, Query<T> query) {
        try {
            return query(target, query);
        } catch (OpenStackResponseException e) {
            if (e.getStatus() == NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }
}
