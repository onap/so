/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018. Intel Corp. All rights reserved.
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
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

public class OofInfraUtils {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, OofInfraUtils.class);

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
        Optional <CloudSite> optCloudsite = Optional.empty();

        CatalogDbClient client = new CatalogDbClient(endpoint, auth);
        try {
            optCloudsite = Optional.ofNullable(client.getCloudSite(cloudSite.getId(), endpoint + "/cloudSite/"));
        } catch (Exception e) {
            LOGGER.debug("Could not find cloudsite : " + cloudSite.getId());
            LOGGER.debug("Creating cloudSite: " + cloudSite.toString());
        }
        if (optCloudsite.isPresent() && (cloudSite.getId()) != optCloudsite.get().getId()) {
            client.postCloudSite(cloudSite);
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
            LOGGER.debug("Could not create HomingInstance : " + homingInstance.getServiceInstanceId());
            LOGGER.debug("HomingInstance Creation Error: " + exception);
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
            LOGGER.debug("Could not get HomingInstance for serviceInstanceId : " + serviceInstanceId);
            LOGGER.debug("Get HomingInstance Error: " + exception);
        }
        return null;
    }
}
