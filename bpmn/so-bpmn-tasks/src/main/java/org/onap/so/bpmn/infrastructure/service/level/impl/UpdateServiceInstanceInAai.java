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
package org.onap.so.bpmn.infrastructure.service.level.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIRestClientI;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_MODEL_INFO;

@Component
public class UpdateServiceInstanceInAai implements JavaDelegate {

    @Autowired
    private ExceptionBuilder exceptionUtil;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String PNF_RESOURCE = "pnf";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    // ERROR CODE for variable not found in the delegation Context
    private static final int ERROR_CODE = 601;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        logger.debug("Running execute block for activity id: {}, name: {}", delegateExecution.getCurrentActivityId(),
                delegateExecution.getCurrentActivityName());

        RequestDetails bpmnRequestDetails = requestVerification(delegateExecution);

        final String serviceInstanceId = String.valueOf(delegateExecution.getVariable(SERVICE_INSTANCE_ID));
        final String serviceType = bpmnRequestDetails.getRequestParameters().getSubscriptionServiceType();
        final String globalSubscriberId = bpmnRequestDetails.getSubscriberInfo().getGlobalSubscriberId();

        final String modelId = bpmnRequestDetails.getModelInfo().getModelUuid();

        if (PNF_RESOURCE.equalsIgnoreCase(serviceType)) {
            getAndSetServiceInstance(serviceInstanceId, serviceType, globalSubscriberId, modelId);
        }

        // TODO : handling for vnf

        logger.trace("Completed updating request for ServiceLevelUpgrade.");
    }

    private void getAndSetServiceInstance(final String serviceInstanceId, final String serviceType,
            final String globalSubscriberId, String modelVersionId) {

        AAIRestClientI restClient = new AAIRestClientImpl();

        Optional<ServiceInstance> optionalSi =
                restClient.getServiceInstanceById(serviceInstanceId, serviceType, globalSubscriberId);

        if (!optionalSi.isPresent()) {
            // throwExceptionWithWarn(delegateExecution, "Unable to find the service instance: " + serviceInstanceId);
        }

        ServiceInstance serviceInstance = optionalSi.get();

        serviceInstance.setModelVersionId(modelVersionId);

        restClient.updateServiceInstance(serviceInstanceId, serviceType, globalSubscriberId, serviceInstance);

    }

    private RequestDetails requestVerification(DelegateExecution delegateExecution) throws IOException {
        RequestDetails bpmnRequestDetails = mapper.readValue(
                JsonUtils.getJsonValue(String.valueOf(delegateExecution.getVariable(BPMN_REQUEST)), "requestDetails"),
                RequestDetails.class);

        throwIfNull(delegateExecution, bpmnRequestDetails.getModelInfo(), SERVICE_MODEL_INFO);
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestInfo(), "RequestInfo");
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestParameters(), "RequestParameters");
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestParameters().getUserParams(), "UserParams");

        return bpmnRequestDetails;
    }

    private void throwIfNull(DelegateExecution delegateExecution, Object obj, String param) {
        if (obj == null) {
            throwExceptionWithWarn(delegateExecution,
                    "Unable to find the parameter: " + param + " in the execution context");
        }
    }

    private void throwExceptionWithWarn(DelegateExecution delegateExecution, String exceptionMsg) {
        logger.warn(exceptionMsg);
        exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, exceptionMsg);
    }

}
