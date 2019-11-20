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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller;

import java.util.List;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class should be extended by LCM api based controllers.
 */
public abstract class LcmControllerBB implements ControllerRunnable<BuildingBlockExecution> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired(required = false)
    protected List<ControllerPreparable<BuildingBlockExecution>> prepareList;

    @Autowired
    protected ApplicationControllerAction client;

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        prepareList.stream().filter(prepare -> prepare.understand(context))
                .forEach(prepare -> prepare.prepare(context));
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        callLcmClient(context);
    }

    /**
     * This method is used to execute the LCM action by calling LCM client, appc Client or SDNC client.
     *
     * @return error code
     */
    protected abstract int callLcmClient(ControllerContext<BuildingBlockExecution> context);

    protected abstract int getErrorCode();
}
