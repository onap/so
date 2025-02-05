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

package org.onap.so.bpmn.infrastructure.validations;

import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.validation.PreBuildingBlockValidator;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.listener.Skip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Skip
public class CloudRegionOrchestrationValidator implements PreBuildingBlockValidator {

    private static Logger logger = LoggerFactory.getLogger(CloudRegionOrchestrationValidator.class);
    private final Pattern pattern = Pattern.compile(
            "(?:Activate|Assign|Create|Deactivate|Delete|Unassign|Update)(?:Network|Vnf|VfModule|VolumeGroup|FabricConfiguration)BB");

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    @Override
    public boolean shouldRunFor(String bbName) {
        return pattern.matcher(bbName).find();
    }

    @Override
    public Optional<String> validate(BuildingBlockExecution execution) {
        String msg = null;
        try {
            CloudRegion cloudRegion = execution.getGeneralBuildingBlock().getCloudRegion();
            if (Boolean.TRUE.equals(cloudRegion.getOrchestrationDisabled())) {
                msg = String.format(
                        "Error: The request has failed due to orchestration currently disabled for the target cloud region %s for cloud owner %s",
                        cloudRegion.getLcpCloudRegionId(), cloudRegion.getCloudOwner());
                logger.error(msg);
                return Optional.ofNullable(msg);
            }
        } catch (Exception e) {
            logger.error("failed to validate", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e, ONAPComponents.SO);
        }
        return Optional.empty();
    }

}
