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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.onap.aai.domain.yang.ComposedResource;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.ApiHandlerClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_ERROR;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_PAYLOAD;



@Component
public class DeleteChildServiceBB {

    private static final String ROLLBACK_TARGET_STATE = "rollbackTargetState";
    private static final String REQUEST_ACTION = "requestAction";
    public static final String CHILD_SVC_REQ_STATUS = "CHILD_SVC_REQ_STATUS";
    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    @Autowired
    private ApiHandlerClient apiHandlerClient;

    private AAIResourcesClient aaiResourcesClient = new AAIResourcesClient();

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public void buildRequest(final BuildingBlockExecution buildingBlockExecution) {
        log.info("Building Delete Service Request");
        Map<ResourceKey, String> lookupMap = buildingBlockExecution.getLookupMap();
        String childSvcInstanceId = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_ID);
        String childServiceInstanceId =
                buildingBlockExecution.getLookupMap().get(ResourceKey.CHILD_SERVICE_INSTANCE_ID);
        String parentServiceInstanceId = buildingBlockExecution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID);
        ServiceInstance childInstanceAAI = aaiResourcesClient.get(ServiceInstance.class,
                AAIClientUriFactory
                        .createResourceUri(
                                AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(childServiceInstanceId))
                        .depth(Depth.TWO))
                .orElse(null);
        ServiceInstance parentInstanceAAI =
                aaiResourcesClient.get(ServiceInstance.class,
                        AAIClientUriFactory.createResourceUri(
                                AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(parentServiceInstanceId))
                                .depth(Depth.TWO))
                        .orElse(null);
        if (childInstanceAAI == null || parentInstanceAAI == null) {
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 10004, "Service AAI request failed",
                    ONAPComponents.SO);
        }
        Service parentInstance = serviceInstanceToServiceBeanMapper(parentInstanceAAI);
        Service childInstance = serviceInstanceToServiceBeanMapper(childInstanceAAI);
        ServiceInstancesRequest sir = ChildServiceRequestBuilder
                .getInstance(buildingBlockExecution, parentInstance, childInstance)
                .setParentRequestId(
                        buildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId())
                .setChildSvcInstanceId(childSvcInstanceId).setCorrelationId(UUID.randomUUID().toString()).build();
        buildingBlockExecution.setVariable(CHILD_SVC_REQ_PAYLOAD, sir);
    }


    public void setRequestAction(final BuildingBlockExecution buildingBlockExecution) {
        String action = buildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getAction();
        buildingBlockExecution.setVariable(REQUEST_ACTION, action);
    }

    public void checkIfChildInstantiated(final BuildingBlockExecution buildingBlockExecution) {
        try {
            Map<ResourceKey, String> lookupMap = buildingBlockExecution.getLookupMap();

            String parentServiceInstanceId = buildingBlockExecution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID);

            String childSvcInstanceName = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_NAME);

            ServiceInstance parentInstanceAAI =
                    aaiResourcesClient.get(ServiceInstance.class,
                            AAIClientUriFactory.createResourceUri(
                                    AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(parentServiceInstanceId))
                                    .depth(Depth.TWO))
                            .orElse(null);



            if (parentInstanceAAI.getComposedResources() != null) {
                List<ComposedResource> composedResources =
                        parentInstanceAAI.getComposedResources().getComposedResource();



                List<List<Relationship>> relationship = new ArrayList<>();
                for (ComposedResource composedResource : composedResources) {
                    if (composedResource.getRelationshipList() != null) {
                        relationship.add(composedResource.getRelationshipList().getRelationship());
                    }
                }

                List<List<RelationshipData>> relationshipData = new ArrayList<>();
                List<List<RelatedToProperty>> relatedToProperties = new ArrayList<>();

                for (int i = 0; i < relationship.size(); i++) {
                    for (Relationship relationshipList : relationship.get(i)) {
                        relatedToProperties.add(relationshipList.getRelatedToProperty());
                        relationshipData.add(relationshipList.getRelationshipData());
                    }
                }


                String childInstanceId = null;
                for (int i = 0; i < relationship.size(); i++) {
                    for (RelatedToProperty relatedToProperty1 : relatedToProperties.get(i)) {
                        if (relatedToProperty1.getPropertyValue().equalsIgnoreCase(childSvcInstanceName)) {
                            for (RelationshipData relationshipData1 : relationshipData.get(i)) {
                                if (relationshipData1.getRelationshipKey()
                                        .equals("service-instance.service-instance-id")) {
                                    childInstanceId = relationshipData1.getRelationshipValue();
                                    break;
                                }
                            }
                        }
                    }
                }
                if (childInstanceId != null) {
                    lookupMap.put(ResourceKey.CHILD_SERVICE_INSTANCE_ID, childInstanceId);
                    buildingBlockExecution.setVariable(ROLLBACK_TARGET_STATE, "Rollback");
                    buildingBlockExecution.setVariable(REQUEST_ACTION, "createInstance");
                } else {
                    buildingBlockExecution.setVariable(REQUEST_ACTION, "createInstance");
                    buildingBlockExecution.setVariable(CHILD_SVC_REQ_STATUS, "fail");
                }
            } else {
                buildingBlockExecution.setVariable(REQUEST_ACTION, "createInstance");
                buildingBlockExecution.setVariable(CHILD_SVC_REQ_STATUS, "fail");
            }
        } catch (Exception e) {
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 10005, e.getMessage(),
                    ONAPComponents.SO);
        }
    }

    public void buildRequestRollBack(final BuildingBlockExecution buildingBlockExecution) {
        try {
            log.info("buildRequestRollBack Create Service Request");
            Map<ResourceKey, String> lookupMap = buildingBlockExecution.getLookupMap();
            String childSvcInstanceName = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_NAME);
            String childSvcInstanceId = lookupMap.get(ResourceKey.CHILD_SERVICE_INSTANCE_ID);
            Objects.requireNonNull(childSvcInstanceName, "Child service instance name is required");

            ServiceInstancesRequest sir = ChildServiceRequestBuilder
                    .getInstance(buildingBlockExecution, childSvcInstanceName)
                    .setParentRequestId(
                            buildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId())
                    .setChildSvcInstanceId(childSvcInstanceId).setCorrelationId(UUID.randomUUID().toString()).build();
            buildingBlockExecution.setVariable(CHILD_SVC_REQ_PAYLOAD, sir);
        } catch (Exception e) {
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 10002, e.getMessage(),
                    ONAPComponents.SO);
        }
    }

    public void sendRequest(final BuildingBlockExecution buildingBlockExecution) {
        try {
            ServiceInstancesRequest sir = buildingBlockExecution.getVariable(CHILD_SVC_REQ_PAYLOAD);
            log.info("Sending Delete Service Request: \n{}", sir.toString());
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

    private static Service serviceInstanceToServiceBeanMapper(ServiceInstance serviceInstance) {
        Service service = new Service();
        service.setInstanceName(service.getInstanceName());
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelId(serviceInstance.getModelVersionId());
        modelInfo.setModelType(ModelType.service);
        modelInfo.setModelVersionId(serviceInstance.getModelVersionId());
        modelInfo.setModelInstanceName(serviceInstance.getServiceInstanceName());
        modelInfo.setModelInvariantId(serviceInstance.getModelInvariantId());
        service.setModelInfo(modelInfo);
        return service;
    }
}
