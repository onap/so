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
import com.woorea.openstack.glance.Glance;
import com.woorea.openstack.glance.model.Image;
import com.woorea.openstack.glance.model.Images;

@Component
public class GlanceClientImpl extends MsoCommonUtils {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(GlanceClientImpl.class);

    /**
     * Gets the glance client.
     *
     * @param cloudSite the cloud site
     * @param tenantId the tenant id
     * @return the glance client
     * @throws MsoException the mso exception
     */
    private Glance getGlanceClient(String cloudSiteId, String tenantId) throws MsoException {
        KeystoneAuthHolder keystone = getKeystoneAuthHolder(cloudSiteId, tenantId, "image");
        Glance glanceClient = new Glance(keystone.getServiceUrl() + "/v2.0/");
        glanceClient.token(keystone.getId());
        return glanceClient;
    }


    /**
     * Query images
     *
     * 
     * @param cloudSiteId the cloud site id
     * @param tenantId the tenant id
     * @param limit limits the number of records returned
     * @param visibility visibility in the image in openstack
     * @param marker the last viewed record
     * @param name the image names
     * @return the list of images in openstack
     * @throws MsoCloudSiteNotFound the mso cloud site not found
     * @throws GlanceClientException the glance client exception
     */
    public Images queryImages(String cloudSiteId, String tenantId, int limit, String visibility, String marker,
            String name) throws MsoCloudSiteNotFound, GlanceClientException {
        try {
            String encodedName = null;
            if (name != null) {
                encodedName = "in:\"" + name + "\"";
            }
            Glance glanceClient = getGlanceClient(cloudSiteId, tenantId);
            // list is set to false, otherwise an invalid URL is appended
            OpenStackRequest<Images> request = glanceClient.images().list(false).queryParam("visibility", visibility)
                    .queryParam("limit", limit).queryParam("marker", marker).queryParam("name", encodedName);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            logger.error("Error building Glance Client", e);
            throw new GlanceClientException("Error building Glance Client", e);
        }
    }

    public Image queryImage(String cloudSiteId, String tenantId, String imageId)
            throws GlanceClientException {
        try {
            Glance glanceClient = getGlanceClient(cloudSiteId, tenantId);
            // list is set to false, otherwise an invalid URL is appended
            OpenStackRequest<Image> request = glanceClient.images().show(imageId);
            return executeAndRecordOpenstackRequest(request, false);
        } catch (MsoException e) {
            logger.error("Error building Glance Client", e);
            throw new GlanceClientException("Error building Glance Client", e);
        }
    }

}
