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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.appc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link ControllerRunnable} is created to demonstrate how to support {@link DelegateExecution}
 * API based APPC controller.
 *
 * Function wise, it's similar as {@ref AppcClient} groovy code.
 */
@Component
public class AppcControllerDE implements ControllerRunnable<DelegateExecution> {

    private static final Logger logger = LoggerFactory.getLogger(AppcControllerDE.class);

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("appc")
                || context.getControllerActor().equalsIgnoreCase("sdnc");
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> context) {

    }

    /**
     * This method is left empty intentionally. If you are planning to use the Appc Controller, please implement here.
     *
     * You can use the {@ref ApplicationControllerAction}, {@ref ApplicationControllerOrchestrator},
     * {@ref ApplicationControllerClient} or create your own Appc Client proxy.
     */
    @Override
    public void run(final ControllerContext<DelegateExecution> context) {}
}
