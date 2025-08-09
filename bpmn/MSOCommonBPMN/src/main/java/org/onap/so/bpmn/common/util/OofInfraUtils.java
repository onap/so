/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018. Intel Corp. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
package org.onap.so.bpmn.common.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class OofInfraUtils {

    private static final Logger logger = LoggerFactory.getLogger(OofInfraUtils.class);

    /**
     * This method creates a cloudsite in catalog database.
     *
     * @param cloudSite
     *
     * @return void
     */
    public void createCloudSite(CloudSite cloudSite, DelegateExecution execution) {
        String endpoint = UrnPropertiesReader.getVariable("mso.catalog.db.spring.endpoint", execution);
        String auth = UrnPropertiesReader.getVariable("mso.db.auth", execution);
        try {
            CloudSite getCloudsite;

            CatalogDbClient client = new CatalogDbClient(endpoint, auth);

            getCloudsite = Optional.ofNullable(client.getCloudSite(cloudSite.getId(), endpoint + "/cloudSite/"))
                    .orElse(new CloudSite());
            if (!cloudSite.getId().equals(getCloudsite.getId())) {
                client.postOofHomingCloudSite(cloudSite);
                logger.debug("Did not findd cloudsite : {}", cloudSite.getId());
                logger.debug("Will create cloudSite: {}", cloudSite.toString());
            } else {
                logger.debug("Found cloudsite : {}", cloudSite.getId());
                logger.debug("Will not create cloudSite: {}", cloudSite.toString());
            }
        } catch (Exception e) {
            logger.debug("Error looking up or creating cloudsite : {}", cloudSite.getId());
            logger.debug("CloudSite Lookup/Creation Error: {}", e);
        }


    }

    /**
     * This method creates a HomingInstance in catalog database.
     *
     * @param homingInstance
     *
     * @return void
     */
    public void createHomingInstance(HomingInstance homingInstance, DelegateExecution execution) {
        String endpoint = UrnPropertiesReader.getVariable("mso.catalog.db.spring.endpoint", execution);
        String auth = UrnPropertiesReader.getVariable("mso.db.auth", execution);

        CatalogDbClient client = new CatalogDbClient(endpoint, auth);
        try {
            client.postHomingInstance(homingInstance);
        } catch (Exception exception) {
            logger.debug("Could not create HomingInstance : {}", homingInstance.getServiceInstanceId());
            logger.debug("HomingInstance Creation Error: {}", exception);
        }

    }

    /**
     * This method gets a HomingInstance in catalog database.
     *
     * @param serviceInstanceId
     *
     * @return HomingInstance
     */
    public HomingInstance getHomingInstance(String serviceInstanceId, DelegateExecution execution) {
        String endpoint = UrnPropertiesReader.getVariable("mso.catalog.db.spring.endpoint", execution);
        String auth = UrnPropertiesReader.getVariable("mso.db.auth", execution);

        CatalogDbClient client = new CatalogDbClient(endpoint, auth);
        try {
            return client.getHomingInstance(serviceInstanceId, endpoint + "/homingInstance/");
        } catch (Exception exception) {
            logger.debug("Could not get HomingInstance for serviceInstanceId : {}", serviceInstanceId);
            logger.debug("Get HomingInstance Error: {}", exception);
        }
        return null;
    }
}
