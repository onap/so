/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.Constants.NS_WORKFLOW_ENGINE;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.Constants.TENANT_ID;
import static org.slf4j.LoggerFactory.getLogger;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
@Order(Ordering.DEFAULT_ORDER + 1)
public class CamundaCustomConfiguration extends AbstractCamundaConfiguration {
    private static final Logger logger = getLogger(CamundaCustomConfiguration.class);

    @Override
    public void preInit(final SpringProcessEngineConfiguration processEngineConfiguration) {
        logger.info("Setting DeploymentTenantId to {} and DeploymentName to {}", TENANT_ID, NS_WORKFLOW_ENGINE);
        processEngineConfiguration.setDeploymentTenantId(TENANT_ID);
        processEngineConfiguration.setDeploymentName(NS_WORKFLOW_ENGINE);
        super.preInit(processEngineConfiguration);
    }

}
