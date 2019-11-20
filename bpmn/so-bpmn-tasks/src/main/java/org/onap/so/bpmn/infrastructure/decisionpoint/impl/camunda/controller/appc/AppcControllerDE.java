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
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.LcmControllerDE;
import org.springframework.stereotype.Component;

/**
 * This class is created to demonstrate how to support {@link DelegateExecution} API based APPC controller.
 *
 * Function wise, it's similar as {@ref AppcClient} groovy code.
 */
@Component
public class AppcControllerDE extends LcmControllerDE {

    private static final int APPC_DELEGATE_EXECUTION_ERROR_CODE = 1102;

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("appc");
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    /**
     * This method is left empty intentionally. If you are planning to use the Appc Controller, please implement here.
     *
     * You can use the {@ref ApplicationControllerAction}, {@ref ApplicationControllerOrchestrator},
     * {@ref ApplicationControllerClient} or create your own Appc Client proxy.
     */
    @Override
    protected int callLcmClient(ControllerContext<DelegateExecution> context) {
        return 0;
    }

    @Override
    protected int getErrorCode() {
        return APPC_DELEGATE_EXECUTION_ERROR_CODE;
    }
}
