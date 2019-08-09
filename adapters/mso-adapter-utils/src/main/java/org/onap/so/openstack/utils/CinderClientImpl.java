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
import org.onap.so.openstack.exceptions.MsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.Volumes;



@Component
public class CinderClientImpl extends MsoCommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CinderClientImpl.class);

    /**
     * Gets the Cinder client.
     *
     * @param cloudSiteId the cloud site
     * @param tenantId the tenant id
     * @return the glance client
     * @throws MsoException the mso exception
     */
    private Cinder getCinderClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "volumev2");
        Cinder cinderClient = new Cinder(keystone.getServiceUrl());
        cinderClient.token(keystone.getId());
        return cinderClient;
    }


    /**
     * Query images
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param marker the last viewed record
     * @return the list of images in openstack
     * @throws CinderClientException the glance client exception
     */
    public Volumes queryVolumes(String cloudSiteId, String tenantId, int limit, String marker)
            throws CinderClientException {
        try {
            Cinder cinderClient = getCinderClient(cloudSiteId, tenantId);
            // list is set to false, otherwise an invalid URL is appended
            OpenStackRequest<Volumes> request =
                    cinderClient.volumes().list(false).queryParam("limit", limit).queryParam("marker", marker);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            String errorMsg = "Error building Cinder Client";
            logger.error(errorMsg, e);
            throw new CinderClientException(errorMsg, e);
        }
    }


    public Volume queryVolume(String cloudSiteId, String tenantId, String volumeId) throws CinderClientException {
        try {
            Cinder cinderClient = getCinderClient(cloudSiteId, tenantId);
            // list is set to false, otherwise an invalid URL is appended
            OpenStackRequest<Volume> request = cinderClient.volumes().show(volumeId);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            String errorMsg = "Error building Cinder Client";
            logger.error(errorMsg, e);
            throw new CinderClientException(errorMsg, e);
        }
    }

}
