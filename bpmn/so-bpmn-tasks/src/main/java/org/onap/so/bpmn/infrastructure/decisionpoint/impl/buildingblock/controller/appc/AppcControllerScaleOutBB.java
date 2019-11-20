/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.appc;

import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.ConfigurationScaleOut;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCQueryTasks;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This component is used to execute the ScaleOut use case for VNF against APPC controller.
 */
@Component
public class AppcControllerScaleOutBB implements ControllerRunnable<BuildingBlockExecution> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurationScaleOut configurationScaleOut;

    @Autowired
    private SDNCQueryTasks sdncQueryTasks;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return (context.getControllerActor().equalsIgnoreCase("appc")
                || context.getControllerActor().equalsIgnoreCase("sdnc"))
                && context.getControllerAction().equals("ConfigScaleOut");
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        try {
            sdncQueryTasks.queryVfModule(context.getExecution());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(context.getExecution(), 9020, e, ONAPComponents.SO);
        }
        configurationScaleOut.setParamsForConfigurationScaleOut(context.getExecution());
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        configurationScaleOut.callAppcClient(context.getExecution());
    }
}
