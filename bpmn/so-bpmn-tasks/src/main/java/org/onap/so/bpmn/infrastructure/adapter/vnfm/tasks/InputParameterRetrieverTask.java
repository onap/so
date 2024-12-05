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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParameter;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParametersProvider;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.NullInputParameter;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This class retrieve input parameters
 * 
 * @author waqas.ikram@est.tech
 */
@Component
public class InputParameterRetrieverTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputParameterRetrieverTask.class);

    private final InputParametersProvider<GenericVnf> sdncInputParametersProvider;

    private final ExtractPojosForBB extractPojosForBB;

    private final InputParametersProvider<Map<String, Object>> userParamInputParametersProvider;

    /*
     * @Autowired public InputParameterRetrieverTask(final InputParametersProvider<GenericVnf> inputParametersProvider,
     * final InputParametersProvider<Map<String, Object>> userParamInputParametersProvider, final ExtractPojosForBB
     * extractPojosForBB) { this.sdncInputParametersProvider = inputParametersProvider;
     * this.userParamInputParametersProvider = userParamInputParametersProvider; this.extractPojosForBB =
     * extractPojosForBB; }
     */

    @Autowired
    public InputParameterRetrieverTask(
            @Qualifier("sdncInputParametersProvider") final InputParametersProvider<GenericVnf> sdncInputParametersProvider,
            @Qualifier("userParamInputParametersProvider") final InputParametersProvider<Map<String, Object>> userParamInputParametersProvider,
            final ExtractPojosForBB extractPojosForBB) {

        this.sdncInputParametersProvider = sdncInputParametersProvider;
        this.userParamInputParametersProvider = userParamInputParametersProvider;
        this.extractPojosForBB = extractPojosForBB;
    }

    public void getInputParameters(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getInputParameters  ...");

            final InputParameter inputParameter = new InputParameter();

            final InputParameter userParamsInputParameter = getUserParamsInputParameter(execution);
            final InputParameter sdncInputParameter = getSdncInputParameter(execution);

            inputParameter.putAdditionalParams(sdncInputParameter.getAdditionalParams());
            inputParameter.addExtVirtualLinks(sdncInputParameter.getExtVirtualLinks());

            inputParameter.putAdditionalParams(userParamsInputParameter.getAdditionalParams());
            inputParameter.addExtVirtualLinks(userParamsInputParameter.getExtVirtualLinks());

            LOGGER.debug("inputParameter: {}", inputParameter);
            execution.setVariable(INPUT_PARAMETER, inputParameter);

            LOGGER.debug("Finished executing getInputParameters ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to get input parameters", exception);
            execution.setVariable(INPUT_PARAMETER, NullInputParameter.NULL_INSTANCE);
        }
    }

    private InputParameter getSdncInputParameter(final BuildingBlockExecution execution)
            throws BBObjectNotFoundException {
        final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);
        return sdncInputParametersProvider.getInputParameter(vnf);
    }

    private InputParameter getUserParamsInputParameter(final BuildingBlockExecution execution) {
        final GeneralBuildingBlock generalBuildingBlock = execution.getGeneralBuildingBlock();

        if (generalBuildingBlock != null && generalBuildingBlock.getRequestContext() != null
                && generalBuildingBlock.getRequestContext().getRequestParameters() != null) {

            final List<Map<String, Object>> userParams =
                    generalBuildingBlock.getRequestContext().getRequestParameters().getUserParams();
            if (userParams != null) {
                final Map<String, Object> params = new HashMap<>();

                userParams.stream().forEach(obj -> {
                    params.putAll(obj);
                });
                LOGGER.info("User params found : {}", params);
                if (userParams != null && !userParams.isEmpty()) {
                    return userParamInputParametersProvider.getInputParameter(params);
                }
            }

        }
        LOGGER.warn("No input parameters found in userparams ...");
        return NullInputParameter.NULL_INSTANCE;
    }

}
