/*-
 * Copyright (C) 2021 Bell Canada. All rights reserved.
 *
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
 */

package org.onap.so.bpmn.infrastructure.service.composition;

import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.ApiHandlerClient;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Objects;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.*;


@Component
public class DeleteChildServiceBB {

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    @Autowired
    private ApiHandlerClient apiHandlerClient;


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void buildRequest(final BuildingBlockExecution buildingBlockExecution) {
        log.info("Building Delete Service Request");
        Map<ResourceKey, String> lookupMap = buildingBlockExecution.getLookupMap();
        String childSvcInstanceName = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_NAME);
        Objects.requireNonNull(childSvcInstanceName, "Child service instance name is required");
        String childSvcInstanceId = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_ID);

        ServiceInstancesRequest sir =
                ChildServiceRequestBuilder.getInstance(buildingBlockExecution, childSvcInstanceName)
                        .setParentRequestId(
                                buildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId())
                        .setChildSvcInstanceId(childSvcInstanceId).build();
        buildingBlockExecution.setVariable(CHILD_SVC_REQ_PAYLOAD, sir);
    }

    public void sendRequest(final BuildingBlockExecution buildingBlockExecution) {
        try {
            buildingBlockExecution.getLookupMap();
            ServiceInstancesRequest sir = buildingBlockExecution.getVariable(CHILD_SVC_REQ_PAYLOAD);
            log.info("Sending Create Service Request: \n{}", sir.toString());
            buildingBlockExecution.setVariable(CHILD_SVC_REQ_CORRELATION_ID,
                    sir.getRequestDetails().getRequestInfo().getCorrelator());

            ServiceInstancesResponse response = apiHandlerClient.deleteServiceInstance(sir);
            buildingBlockExecution.setVariable(CHILD_SVC_REQ_ID, response.getRequestReferences().getRequestId());
            buildingBlockExecution.setVariable(CHILD_SVC_INSTANCE_ID, response.getRequestReferences().getInstanceId());
        } catch (Exception e) {
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 10003, e.getMessage(),
                    ONAPComponents.SO);
        }
    }


    public void handleFailure(final BuildingBlockExecution buildingBlockExecution) {
        Map<ResourceKey, String> lookupMap = buildingBlockExecution.getLookupMap();
        String childSvcInstanceName = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_NAME);
        String childErrorMessage = buildingBlockExecution.getVariable(CHILD_SVC_REQ_ERROR);
        String errorMessage =
                String.format("Failed deleting child service %:qqs %s", childSvcInstanceName, childErrorMessage);
        exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 10001, errorMessage, ONAPComponents.SO);
    }

}
