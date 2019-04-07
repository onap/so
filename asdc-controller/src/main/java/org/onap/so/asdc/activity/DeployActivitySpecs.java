/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.activity;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeployActivitySpecs {
    @Autowired
    private ActivitySpecsActions activitySpecsActions;

    @Autowired
    private Environment env;

    private static final String SDC_ENDPOINT = "mso.asdc.config.activity.endpoint";

    protected static final Logger logger = LoggerFactory.getLogger(DeployActivitySpecs.class);

    public void deployActivities() throws Exception {
        String hostname = this.env.getProperty(SDC_ENDPOINT);
        List<ActivitySpec> activitySpecs = new ArrayList<ActivitySpec>();

        // Initialize activitySpecs from Catalog DB

        for (ActivitySpec activitySpec : activitySpecs) {
            String activitySpecId = activitySpecsActions.createActivitySpec(hostname, activitySpec);
            if (activitySpecId != null) {
                logger.info("{} {}", "Successfully created activitySpec", activitySpec.getName());
                boolean certificationResult = activitySpecsActions.certifyActivitySpec(hostname, activitySpecId);
                if (certificationResult) {
                    logger.info("{} {}", "Successfully certified activitySpec", activitySpec.getName());
                } else {
                    logger.info("{} {}", "Failed to certify activitySpec", activitySpec.getName());
                }
            } else {
                logger.info("{} {}", "Failed to create activitySpec", activitySpec.getName());
            }
        }
    }
}
