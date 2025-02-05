/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;
import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This class performs CNF Delete
 *
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@Component
public class CnfDeleteTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnfInstantiateTask.class);
    private final ExceptionBuilder exceptionUtil;
    private final CnfmHttpServiceProvider cnfmHttpServiceProvider;
    private final ExtractPojosForBB extractPojosForBB;
    private static final String MONITOR_JOB_NAME = "MonitorJobName";
    private static final String AS_INSTANCE_ID = "asInstanceid";
    private static final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    private static final String TERMINATE_AS_REQUEST_OBJECT = "TerminateAsRequest";

    @Autowired
    public CnfDeleteTask(
            @Qualifier("CnfmHttpServiceProviderImpl") final CnfmHttpServiceProvider cnfmHttpServiceProvider,
            final ExceptionBuilder exceptionUtil, ExtractPojosForBB extractPojosForBB) {
        this.cnfmHttpServiceProvider = cnfmHttpServiceProvider;
        this.exceptionUtil = exceptionUtil;
        this.extractPojosForBB = extractPojosForBB;
    }

    public void createTerminateAsRequest(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing createTerminateAsRequest task  ...");

            final TerminateAsRequest terminateAsRequest = new TerminateAsRequest();
            terminateAsRequest.setTerminationType(TerminateAsRequest.TerminationTypeEnum.GRACEFUL);
            terminateAsRequest.setGracefulTerminationTimeout(0);
            terminateAsRequest.setAdditionalParams(new HashMap<>());

            LOGGER.debug("Adding TerminateAsRequest to execution {}", terminateAsRequest);

            execution.setVariable(TERMINATE_AS_REQUEST_OBJECT, terminateAsRequest);
            LOGGER.debug("Finished executing terminateAsRequest task ...");

        } catch (final Exception exception) {
            LOGGER.error("Unable to create TerminateAsRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2001, exception);
        }
    }

    public void invokeCnfmToTerminateAsInstance(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing TerminateAsInstance task  ...");

            final TerminateAsRequest terminateAsRequest = execution.getVariable(TERMINATE_AS_REQUEST_OBJECT);
            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);
            final String asInstanceId = vnf.getVnfId();

            Optional<URI> terminateStatusCheck =
                    cnfmHttpServiceProvider.invokeTerminateAsRequest(asInstanceId, terminateAsRequest);
            execution.setVariable(CNFM_REQUEST_STATUS_CHECK_URL,
                    terminateStatusCheck.orElseThrow(() -> new NoSuchElementException("Status check url Not found")));
            execution.setVariable(MONITOR_JOB_NAME, "Terminate");
            LOGGER.debug("Successfully invoked CNFM terminate AS request: {}", asInstanceId);

        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke CNFM TerminateAsRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2002, exception);
        }
    }

    public void invokeCnfmToDeleteAsInstance(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing DeleteAsInstance task  ...");

            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);
            final String asInstanceId = vnf.getVnfId();

            Optional<Boolean> response = cnfmHttpServiceProvider.invokeDeleteAsRequest(asInstanceId);
            if (Boolean.TRUE.equals(response
                    .orElseThrow(() -> new BpmnError("Unable to complete DeleteAsRequest of ID: " + asInstanceId)))) {
                LOGGER.debug("Successfully invoked CNFM delete AS request with ID: {}", asInstanceId);
            }

        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke CNFM DeleteAsRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2003, exception);
        }
    }
}
