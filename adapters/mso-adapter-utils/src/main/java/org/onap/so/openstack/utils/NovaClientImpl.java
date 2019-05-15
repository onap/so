/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Flavors;
import com.woorea.openstack.nova.model.HostAggregate;
import com.woorea.openstack.nova.model.HostAggregates;
import com.woorea.openstack.nova.model.QuotaSet;


@Component
public class NovaClientImpl extends MsoCommonUtils {


    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(NovaClientImpl.class);

    /**
     * Gets the Nova client
     *
     * @param cloudSiteId id of the cloud site
     * @param tenantId the tenant id
     * @return the Nova client
     * @throws MsoException the mso exception
     */
    private Nova getNovaClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "compute");
        Nova novaClient = new Nova(keystone.getServiceUrl());
        novaClient.token(keystone.getId());
        return novaClient;
    }


    /**
     * Query Networks
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @param name of the newtork
     * @param id of the network
     * @return the list of networks in openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public Flavors queryFlavors(String cloudSiteId, String tenantId, int limit, String marker)
            throws MsoCloudSiteNotFound, NovaClientException {
        try {
            Nova novaClient = getNovaClient(cloudSiteId, tenantId);
            OpenStackRequest<Flavors> request =
                    novaClient.flavors().list(false).queryParam("limit", limit).queryParam("marker", marker);
            return executeAndRecordOpenstackRequest(request);
        } catch (MsoException e) {
            logger.error("Error building Nova Client", e);
            throw new NovaClientException("Error building Nova Client", e);
        }

    }

    /**
     * Query Networks
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param id of the network
     * @return the the flavor from openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public Flavor queryFlavorById(String cloudSiteId, String tenantId, String id)
            throws MsoCloudSiteNotFound, NovaClientException {
        try {
            Nova novaClient = getNovaClient(cloudSiteId, tenantId);
            novaClient = getNovaClient(cloudSiteId, tenantId);
            OpenStackRequest<Flavor> request = novaClient.flavors().show(id);
            return executeAndRecordOpenstackRequest(request);
        } catch (MsoException e) {
            logger.error("Error building Nova Client", e);
            throw new NovaClientException("Error building Nova Client", e);
        }
    }

    /**
     * Query Host Aggregates
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @return the list of host aggregates found in openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public HostAggregates queryHostAggregates(String cloudSiteId, String tenantId, int limit, String marker)
            throws MsoCloudSiteNotFound, NovaClientException {
        try {
            Nova novaClient = getNovaClient(cloudSiteId, tenantId);
            OpenStackRequest<HostAggregates> request =
                    novaClient.aggregates().list().queryParam("limit", limit).queryParam("marker", marker);
            return executeAndRecordOpenstackRequest(request);
        } catch (MsoException e) {
            logger.error("Error building Nova Client", e);
            throw new NovaClientException("Error building Nova Client", e);
        }
    }

    /**
     * Query Host Aggregate
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @return a host aggregate
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public HostAggregate queryHostAggregateById(String cloudSiteId, String tenantId, String id)
            throws MsoCloudSiteNotFound, NovaClientException {
        try {
            Nova novaClient = getNovaClient(cloudSiteId, tenantId);
            OpenStackRequest<HostAggregate> request = novaClient.aggregates().showAggregate(id);
            return executeAndRecordOpenstackRequest(request);
        } catch (MsoException e) {
            logger.error("Error building Nova Client", e);
            throw new NovaClientException("Error building Nova Client", e);
        }
    }

    /**
     * Query OS Quota Set
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @return a host aggregate
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public QuotaSet queryOSQuotaSet(String cloudSiteId, String tenantId)
            throws MsoCloudSiteNotFound, NovaClientException {
        try {
            Nova novaClient = getNovaClient(cloudSiteId, tenantId);
            OpenStackRequest<QuotaSet> request = novaClient.quotaSets().showQuota(tenantId);
            return executeAndRecordOpenstackRequest(request);
        } catch (MsoException e) {
            logger.error("Error building Nova Client", e);
            throw new NovaClientException("Error building Nova Client", e);
        }
    }

}
