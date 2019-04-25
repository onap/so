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

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.Input;
import org.onap.so.asdc.activity.beans.Output;
import org.onap.so.db.catalog.beans.ActivitySpecActivitySpecCategories;
import org.onap.so.db.catalog.beans.ActivitySpecActivitySpecParameters;
import org.onap.so.db.catalog.beans.ActivitySpecParameters;
import org.onap.so.db.catalog.data.repository.ActivitySpecRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeployActivitySpecs {
    @Autowired
    private ActivitySpecsActions activitySpecsActions;

    @Autowired
    private Environment env;

    @Autowired
    private ActivitySpecRepository activitySpecRepository;

    private static final String SDC_ENDPOINT = "mso.asdc.config.activity.endpoint";
    private static final String DIRECTION_INPUT = "input";
    private static final String DIRECTION_OUTPUT = "output";

    protected static final Logger logger = LoggerFactory.getLogger(DeployActivitySpecs.class);


    public void deployActivities() throws Exception {
        String hostname = env.getProperty(SDC_ENDPOINT);
        if (hostname == null || hostname.isEmpty()) {
            return;
        }
        List<org.onap.so.db.catalog.beans.ActivitySpec> activitySpecsFromCatalog = activitySpecRepository.findAll();
        for (org.onap.so.db.catalog.beans.ActivitySpec activitySpecFromCatalog : activitySpecsFromCatalog) {
            ActivitySpec activitySpec = mapActivitySpecFromCatalogToSdc(activitySpecFromCatalog);
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

    public ActivitySpec mapActivitySpecFromCatalogToSdc(
            org.onap.so.db.catalog.beans.ActivitySpec activitySpecFromCatalog) {
        ActivitySpec activitySpec = new ActivitySpec();
        activitySpec.setName(activitySpecFromCatalog.getName());
        activitySpec.setDescription(activitySpecFromCatalog.getDescription());
        mapCategoryList(activitySpecFromCatalog.getActivitySpecActivitySpecCategories(), activitySpec);
        mapInputsAndOutputs(activitySpecFromCatalog.getActivitySpecActivitySpecParameters(), activitySpec);
        return activitySpec;
    }

    private void mapCategoryList(List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories,
            ActivitySpec activitySpec) {
        if (activitySpecActivitySpecCategories == null || activitySpecActivitySpecCategories.size() == 0) {
            return;
        }
        List<String> categoryList = new ArrayList<>();
        for (ActivitySpecActivitySpecCategories activitySpecCat : activitySpecActivitySpecCategories) {
            if (activitySpecCat != null) {
                if (activitySpecCat.getActivitySpecCategories() != null) {
                    categoryList.add(activitySpecCat.getActivitySpecCategories().getName());
                }
            }
        }
        activitySpec.setCategoryList(categoryList);
    }

    private void mapInputsAndOutputs(List<ActivitySpecActivitySpecParameters> activitySpecActivitySpecParameters,
            ActivitySpec activitySpec) {
        if (activitySpecActivitySpecParameters == null || activitySpecActivitySpecParameters.size() == 0) {
            return;
        }
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        for (ActivitySpecActivitySpecParameters activitySpecParam : activitySpecActivitySpecParameters) {
            if (activitySpecParam != null) {
                if (activitySpecParam.getActivitySpecParameters() != null) {
                    ActivitySpecParameters activitySpecParameters = activitySpecParam.getActivitySpecParameters();
                    if (activitySpecParameters != null) {
                        if (activitySpecParameters.getDirection().equals(DIRECTION_INPUT)) {
                            Input input = new Input();
                            input.setName(activitySpecParameters.getName());
                            input.setType(activitySpecParameters.getType());
                            inputs.add(input);
                        } else if (activitySpecParameters.getDirection().equals(DIRECTION_OUTPUT)) {
                            Output output = new Output();
                            output.setName(activitySpecParameters.getName());
                            output.setType(activitySpecParameters.getType());
                            outputs.add(output);
                        }
                    }
                }
            }
        }
        activitySpec.setInputs(inputs);
        activitySpec.setOutputs(outputs);
        return;
    }
}
