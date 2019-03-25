/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.INPUT_PARAMETER;
import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParameter;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParametersProvider;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.NullInputParameter;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class retrieve input parameters
 * 
 * @author waqas.ikram@est.tech
 */
@Component
public class InputParameterRetrieverTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputParameterRetrieverTask.class);

    private final InputParametersProvider inputParametersProvider;

    private final ExtractPojosForBB extractPojosForBB;

    @Autowired
    public InputParameterRetrieverTask(final InputParametersProvider inputParametersProvider,
            final ExtractPojosForBB extractPojosForBB) {
        this.inputParametersProvider = inputParametersProvider;
        this.extractPojosForBB = extractPojosForBB;
    }

    public void getInputParameters(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getInputParameters  ...");

            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);
            final InputParameter inputParameter = inputParametersProvider.getInputParameter(vnf);

            LOGGER.debug("inputParameter: {}", inputParameter);
            execution.setVariable(INPUT_PARAMETER, inputParameter);

            LOGGER.debug("Finished executing getInputParameters ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke create and instantiation request", exception);
            execution.setVariable(INPUT_PARAMETER, NullInputParameter.NULL_INSTANCE);
        }
    }

}
