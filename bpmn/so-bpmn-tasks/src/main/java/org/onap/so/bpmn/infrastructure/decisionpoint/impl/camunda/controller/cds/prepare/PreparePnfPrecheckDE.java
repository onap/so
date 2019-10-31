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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.cds.prepare;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.bpmn.infrastructure.pnf.delegate.PreparePreCheckDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class implements {@link ControllerPreparable} interface and is used to set up the context for PNF config-assign
 * action.
 */
@Component
public class PreparePnfPrecheckDE implements ControllerPreparable<DelegateExecution> {

    @Autowired
    private PreparePreCheckDelegate preparePreCheckDelegate;

    @Override
    public boolean understand(ControllerContext<DelegateExecution> controllerContext) {
        return controllerContext.getControllerActor().equalsIgnoreCase("cds")
                && controllerContext.getControllerAction().equalsIgnoreCase("precheck")
                && controllerContext.getControllerScope().equalsIgnoreCase("pnf");
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> controllerContext) {
        preparePreCheckDelegate.execute(controllerContext.getExecution());
    }
}

