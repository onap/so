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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.onap.so.cloud.authentication.KeystoneAuthHolder;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Subnets;



@Component
public class NeutronClientImpl extends MsoCommonUtils {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(NeutronClientImpl.class);

    /**
     * Gets the Neutron client, using old object named Quantum, now renamed Neutron
     *
     * @param cloudSite the cloud site
     * @param tenantId the tenant id
     * @return the Neutron client
     * @throws MsoException the mso exception
     */
    private Quantum getNeutronClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "network");
        Quantum neutronClient = new Quantum(keystone.getServiceUrl() + "/v2.0/");
        neutronClient.token(keystone.getId());
        return neutronClient;
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
    public Networks queryNetworks(String cloudSiteId, String tenantId, int limit, String marker, String name, String id)
            throws MsoCloudSiteNotFound, NeutronClientException {
        try {
            String encodedName = null;
            if (name != null) {
                try {
                    encodedName = URLEncoder.encode(name, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("error encoding query parameter: {}", encodedName);
                }
            }
            Quantum neutronClient = getNeutronClient(cloudSiteId, tenantId);
            OpenStackRequest<Networks> request = neutronClient.networks().list().queryParam("id", id)
                    .queryParam("limit", limit).queryParam("marker", marker).queryParam("name", encodedName);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            logger.error("Error building Neutron Client", e);
            throw new NeutronClientException("Error building Neutron Client", e);
        }
    }


    /**
     * Query Networks
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @param name of the subnet
     * @param id of the subnet
     * @return the list of subnets in openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public Subnets querySubnets(String cloudSiteId, String tenantId, int limit, String marker, String name, String id)
            throws MsoCloudSiteNotFound, NeutronClientException {
        try {
            Quantum neutronClient = getNeutronClient(cloudSiteId, tenantId);
            OpenStackRequest<Subnets> request = neutronClient.subnets().list().queryParam("id", id)
                    .queryParam("limit", limit).queryParam("marker", marker).queryParam("name", name);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            logger.error("Error building Neutron Client", e);
            throw new NeutronClientException("Error building Neutron Client", e);
        }
    }

    /**
     * Query Networks
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @param name of the subnet
     * @param id of the subnet
     * @return the list of subnets in openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws NeutronClientException if the client cannot be built this is thrown
     */
    public Port queryPortById(String cloudSiteId, String tenantId, String id)
            throws MsoCloudSiteNotFound, NeutronClientException {
        try {
            Quantum neutronClient = getNeutronClient(cloudSiteId, tenantId);
            OpenStackRequest<Port> request = neutronClient.ports().show(id);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            logger.error("Error building Neutron Client", e);
            throw new NeutronClientException("Error building Neutron Client", e);
        }
    }

}
