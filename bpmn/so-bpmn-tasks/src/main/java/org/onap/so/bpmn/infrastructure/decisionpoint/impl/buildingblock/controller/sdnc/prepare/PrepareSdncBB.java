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
package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.sdnc.prepare;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This abstract class should be extended by all the SDNC based {@ControllerPreparable}. It defines some common
 * behavior.
 */
public abstract class PrepareSdncBB implements ControllerPreparable<BuildingBlockExecution> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected ExtractPojosForBB extractPojosForBB;

    public boolean understand(final ControllerContext<BuildingBlockExecution> controllerContext) {
        return controllerContext.getControllerActor().equalsIgnoreCase("sdnc");
    }
}
