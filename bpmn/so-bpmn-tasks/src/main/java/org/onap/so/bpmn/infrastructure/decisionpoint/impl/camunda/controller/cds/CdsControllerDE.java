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
package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.cds;

import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@ref ControllerRunnable} is used for Self service, i.e, blueprint based Controller.
 */
@Component
public class CdsControllerDE implements ControllerRunnable<DelegateExecution> {

    @Autowired(required = false)
    private List<ControllerPreparable<DelegateExecution>> prepareList;

    @Autowired
    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        return "cds".equalsIgnoreCase(context.getControllerActor())
                && "pnf".equalsIgnoreCase(context.getControllerScope())
                && ("config-assign".equalsIgnoreCase(context.getControllerAction())
                        || "config-deploy".equalsIgnoreCase(context.getControllerAction())); // legacy behavior
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> context) {
        prepareList.stream().filter(prepare -> prepare.understand(context))
                .forEach(prepare -> prepare.prepare(context));
    }

    @Override
    public void run(ControllerContext<DelegateExecution> context) {
        DelegateExecution execution = context.getExecution();
        abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(execution);
        abstractCDSProcessingBBUtils.sendRequestToCDSClient(execution);
    }
}
