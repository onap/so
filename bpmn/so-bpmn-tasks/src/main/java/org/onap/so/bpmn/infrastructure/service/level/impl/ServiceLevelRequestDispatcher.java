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
import org.onap.aai.domain.yang.Relationship;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_MODEL_INFO;

/**
 * This implementation of {@link JavaDelegate} is used to populate the execution object for Service level upgrade
 */
@Component
public class ServiceLevelRequestDispatcher implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ExceptionBuilder exceptionUtil;

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

        final String modelInvariantId = bpmnRequestDetails.getModelInfo().getModelInvariantId();
        final String modelId = bpmnRequestDetails.getModelInfo().getModelUuid();
        final String modelVersion = bpmnRequestDetails.getModelInfo().getModelVersion();

        if (ServiceLevelConstants.PNF.equalsIgnoreCase(serviceType)) {
            getAndSetPnfNameFromServiceInstance(serviceInstanceId, serviceType, globalSubscriberId, delegateExecution);
        }

        // TODO : handling for vnf

        logger.trace("Completed dispatcher request for ServiceLevelUpgrade.");
    }

    private void getAndSetPnfNameFromServiceInstance(final String serviceInstanceId, final String serviceType,
            final String globalSubscriberId, DelegateExecution delegateExecution) {

        AAIRestClientI restClient = new AAIRestClientImpl();

        Optional<ServiceInstance> optionalSi =
                restClient.getServiceInstanceById(serviceInstanceId, serviceType, globalSubscriberId);

        optionalSi.ifPresentOrElse(serviceInstance -> {
            final List<String> pnfNameList = serviceInstance.getRelationshipList().getRelationship().stream()
                    .filter(x -> x.getRelatedTo().contains("pnf")).flatMap(x -> x.getRelationshipData().stream())
                    .filter(data -> data.getRelationshipKey().contains("pnf.pnf-name"))
                    .map(x -> x.getRelationshipValue()).collect(Collectors.toList());
            if (pnfNameList == null || pnfNameList.size() == 0) {
                logger.warn(
                        "Unable to find the PNF for service instance id: " + serviceInstance.getServiceInstanceId());
                return;
            }
            delegateExecution.setVariable(ServiceLevelConstants.PNF_NAME_LIST, pnfNameList);
            delegateExecution.setVariable(ServiceLevelConstants.PNF_SIZE, pnfNameList.size());
            delegateExecution.setVariable(ServiceLevelConstants.RESOURCE_TYPE, ServiceLevelConstants.PNF);
        }, () -> {
            throwExceptionWithWarn(delegateExecution, "Unable to find the service instance: " + serviceInstanceId);
        });
    }

    private RequestDetails requestVerification(DelegateExecution delegateExecution) throws IOException {
        RequestDetails bpmnRequestDetails = mapper.readValue(JsonUtils.getJsonValue(
                String.valueOf(delegateExecution.getVariable(ServiceLevelConstants.BPMN_REQUEST)), "requestDetails"),
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
        exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ServiceLevelConstants.ERROR_CODE, exceptionMsg);
    }
}
