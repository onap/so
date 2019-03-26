/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
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

    //ERROR CODE for variable not found in the delegation Context
    private static int ERROR_CODE = 601;
    private static String MODEL_UUID = "modelUuid";

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, Object> variables = delegateExecution.getVariables();
        logger.info("Variables: {}", variables);

        if (delegateExecution.hasVariable(MODEL_UUID)) {
            String serviceModelUuid = (String) delegateExecution.getVariable(MODEL_UUID);
            Service serviceByID = catalogDbClient.getServiceByID(serviceModelUuid);
            if ( serviceByID == null){
                logger.warn("Unable to find the service UUID: {}", serviceModelUuid);
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find the service UUID: " + serviceModelUuid);
            }
            List<PnfResourceCustomization> pnfCustomizations = serviceByID.getPnfCustomizations();
            if (pnfCustomizations != null && pnfCustomizations.size() >= 1) {
                PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
                boolean skipPostInstantiationConfiguration = pnfResourceCustomization.isSkipPostInstConf();
                delegateExecution.setVariable("SkipPostInstantiationConfiguration", skipPostInstantiationConfiguration);
            } else {
                logger
                    .warn("Unable to find the PNF resource customizations of model service UUID: {}", serviceModelUuid);
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find the PNF resource customizations of model service UUID:  " + serviceModelUuid);
            }
        } else {
            logger.warn("Unable to find the parameter: {} in the execution context", MODEL_UUID);
            exceptionUtil
                .buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, "Unable to find parameter " + MODEL_UUID);
        }
    }
}
