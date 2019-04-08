/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_VERSION;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_CUSTOMIZATION_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_INSTANCE_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_MODEL_INFO;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SKIP_POST_INSTANTIATION_CONFIGURATION;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link JavaDelegate} is used to check the SO catalogdb for PNF config flag.
 *
 * It queries the PNF resource customization table for the skip_post_instantiation_configuration for required PNF model
 * UUID.
 */
@Component
public class ConfigCheckerDelegate implements JavaDelegate {

    private Logger logger = LoggerFactory.getLogger(ConfigCheckerDelegate.class);

    // ERROR CODE for variable not found in the delegation Context
    private static int ERROR_CODE = 601;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        logger.debug("Running execute block for activity id:{}, name:{}", delegateExecution.getCurrentActivityId(),
                delegateExecution.getCurrentActivityName());

        if (delegateExecution.hasVariable(SERVICE_MODEL_INFO)) {
            String serviceModelInfo = (String) delegateExecution.getVariable(SERVICE_MODEL_INFO);
            String serviceModelUuid = JsonUtils.getJsonValue(serviceModelInfo, MODEL_UUID);
            delegateExecution.setVariable(MODEL_UUID, serviceModelUuid);
            List<PnfResourceCustomization> pnfCustomizations =
                    catalogDbClient.getPnfResourceCustomizationByModelUuid(serviceModelUuid);
            if (pnfCustomizations != null && pnfCustomizations.size() >= 1) {
                PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
                boolean skipPostInstantiationConfiguration = pnfResourceCustomization.isSkipPostInstConf();
                delegateExecution.setVariable(SKIP_POST_INSTANTIATION_CONFIGURATION,
                        skipPostInstantiationConfiguration);
                delegateExecution.setVariable(PRC_BLUEPRINT_NAME, pnfResourceCustomization.getBlueprintName());
                delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, pnfResourceCustomization.getBlueprintVersion());
                delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID,
                        pnfResourceCustomization.getModelCustomizationUUID());
                delegateExecution.setVariable(PRC_INSTANCE_NAME, pnfResourceCustomization.getModelInstanceName());
            } else {
                logger.warn("Unable to find the PNF resource customizations of model service UUID: {}",
                        serviceModelUuid);
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                        "Unable to find the PNF resource customizations of model service UUID:  " + serviceModelUuid);
            }
        } else {
            logger.warn("Unable to find the parameter: {} in the execution context", SERVICE_MODEL_INFO);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find parameter " + SERVICE_MODEL_INFO);
        }
    }
}
