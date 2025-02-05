/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.google.common.base.Optional;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 */
@Component
public class EtsiVnfDeleteTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiVnfDeleteTask.class);
    private final ExtractPojosForBB extractPojosForBB;
    private final ExceptionBuilder exceptionUtil;
    private final VnfmAdapterServiceProvider vnfmAdapterServiceProvider;

    @Autowired
    public EtsiVnfDeleteTask(final ExceptionBuilder exceptionUtil, final ExtractPojosForBB extractPojosForBB,
            @Qualifier("VnfmAdapterServiceProviderImpl") final VnfmAdapterServiceProvider vnfmAdapterServiceProvider) {
        this.exceptionUtil = exceptionUtil;
        this.extractPojosForBB = extractPojosForBB;
        this.vnfmAdapterServiceProvider = vnfmAdapterServiceProvider;
    }

    /**
     * Invoke VNFM adapter to delete the VNF
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void invokeVnfmAdapter(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing invokeVnfmAdapter  ...");
            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);

            final Optional<DeleteVnfResponse> response = vnfmAdapterServiceProvider.invokeDeleteRequest(vnf.getVnfId());

            if (!response.isPresent()) {
                final String errorMessage = "Unexpected error while processing delete request";
                LOGGER.error(errorMessage);
                exceptionUtil.buildAndThrowWorkflowException(execution, 1211, errorMessage);
            }

            final DeleteVnfResponse vnfResponse = response.get();

            LOGGER.debug("Vnf delete response: {}", vnfResponse);
            execution.setVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME, vnfResponse);

            LOGGER.debug("Finished executing invokeVnfmAdapter ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke delete request", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1212, exception);
        }
    }
}
