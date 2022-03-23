package org.onap.so.bpmn.infrastructure.service.composition;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.OwningEntity;
import org.onap.so.serviceinstancebeans.Project;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ChildServiceRequestBuilder {

    private static AAIResourcesClient aaiClient = new AAIResourcesClient();
    private final BuildingBlockExecution buildingBlockExecution;
    private Service parent;
    private Service child;
    private ServiceInstancesRequest sir;

    private final static Logger LOGGER = LoggerFactory.getLogger(ChildServiceRequestBuilder.class);

    private ChildServiceRequestBuilder(final BuildingBlockExecution buildingBlockExecution, Service parent,
            Service child) {
        this.buildingBlockExecution = buildingBlockExecution;
        this.child = child;
        this.parent = parent;

        this.sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(new RequestInfo());
        this.sir.setRequestDetails(requestDetails);
    }

    public static ChildServiceRequestBuilder getInstance(final BuildingBlockExecution buildingBlockExecution,
            String childSvcInstanceName) {
        Service child = null;
        Service parent = null;
        try {
            if ("CreateChildServiceBB".equalsIgnoreCase(buildingBlockExecution.getFlowToBeCalled())) {
                String USERPARAMSERVICE = "service";
                for (Map<String, Object> params : buildingBlockExecution.getGeneralBuildingBlock().getRequestContext()
                        .getRequestParameters().getUserParams()) {
                    if (params.containsKey(USERPARAMSERVICE)) {
                        ObjectMapper obj = new ObjectMapper();
                        String input = obj.writeValueAsString(params.get(USERPARAMSERVICE));
                        parent = obj.readValue(input, Service.class);
                        if (parent.getResources().getServices() != null) {
                            for (Service service : parent.getResources().getServices()) {
                                if (service.getInstanceName().equals(childSvcInstanceName)) {
                                    child = service;
                                }
                            }
                        }
                    }
                }
            } else if ("DeleteChildServiceBB".equalsIgnoreCase(buildingBlockExecution.getFlowToBeCalled())) {
                String childServiceInstanceId =
                        buildingBlockExecution.getLookupMap().get(ResourceKey.CHILD_SERVICE_INSTANCE_ID);
                String parentServiceInstanceId =
                        buildingBlockExecution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID);

                ServiceInstance serviceInstance =
                        aaiClient.get(ServiceInstance.class,
                                AAIUriFactory.createResourceUri(
                                        AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(childServiceInstanceId))
                                        .depth(Depth.TWO))
                                .orElse(null);

                ServiceInstance parentAAIInstance = aaiClient.get(ServiceInstance.class,
                        AAIUriFactory.createResourceUri(
                                AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(parentServiceInstanceId))
                                .depth(Depth.TWO))
                        .orElse(null);

                if (serviceInstance != null) {
                    parent = serviceInstanceToServiceBeanMapper(parentAAIInstance);
                    child = serviceInstanceToServiceBeanMapper(serviceInstance);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing context user parameters for parent or child service", e);
        }
        return new ChildServiceRequestBuilder(buildingBlockExecution, parent, child);
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

    public ChildServiceRequestBuilder setParentRequestId(String parentRequestId) {
        sir.getRequestDetails().getRequestInfo().setRequestorId(parentRequestId);
        return this;
    }

    public ChildServiceRequestBuilder setCorrelationId(String correlationId) {
        sir.getRequestDetails().getRequestInfo().setCorrelator(correlationId);
        return this;
    }

    public ChildServiceRequestBuilder setChildSvcInstanceId(String childSvcInstanceId) {
        sir.setServiceInstanceId(childSvcInstanceId);
        return this;
    }

    public ServiceInstancesRequest build() {
        RequestContext context = buildingBlockExecution.getGeneralBuildingBlock().getRequestContext();
        sir.setRequestDetails(createRequestDetails(context));
        return sir;
    }

    private RequestDetails createRequestDetails(RequestContext context) {
        RequestDetails details = sir.getRequestDetails();

        details.setRequestParameters(createRequestParameters(context, child));
        details.setRequestInfo(createRequestInfo(context));
        details.setCloudConfiguration(createCloudConfiguration());
        details.setModelInfo(child.getModelInfo());
        details.setSubscriberInfo(createSubscriberInfo());
        details.setOwningEntity(createOwningEntity());
        details.setProject(createProject());
        details.setRelatedInstanceList(createRelatedInstanceList());

        return details;
    }

    private RequestParameters createRequestParameters(RequestContext context, Service childService) {
        RequestParameters requestParameters = new RequestParameters();

        if (!context.getRequestParameters().getUserParams().isEmpty()) {
            requestParameters.getUserParams().add(context.getRequestParameters().getUserParams().get(0));
            requestParameters.getUserParams().add(Map.of("service", childService));
        }
        requestParameters.setSubscriptionServiceType(context.getRequestParameters().getSubscriptionServiceType());
        requestParameters.setaLaCarte(context.getRequestParameters().getALaCarte());
        requestParameters.setPayload(context.getRequestParameters().getPayload());
        requestParameters.setUsePreload(context.getRequestParameters().getUsePreload());

        return requestParameters;
    }

    private RequestInfo createRequestInfo(RequestContext context) {
        RequestInfo info = sir.getRequestDetails().getRequestInfo();
        if (info != null) {
            info.setProductFamilyId(context.getProductFamilyId());
            info.setSource(context.getSource());
            info.setRequestorId(context.getRequestorId());
            info.setInstanceName(child.getInstanceName());
            info.setSuppressRollback(false);
        }
        return info;
    }

    private CloudConfiguration createCloudConfiguration() {
        if (child.getCloudConfiguration() != null) {
            return child.getCloudConfiguration();
        }

        CloudConfiguration cloudConfiguration = null;
        CloudRegion requestCloudConfiguration = buildingBlockExecution.getGeneralBuildingBlock().getCloudRegion();
        if (requestCloudConfiguration != null) {
            cloudConfiguration = new CloudConfiguration();
            cloudConfiguration.setLcpCloudRegionId(requestCloudConfiguration.getLcpCloudRegionId());
            cloudConfiguration.setTenantId(requestCloudConfiguration.getTenantId());
            cloudConfiguration.setCloudOwner(requestCloudConfiguration.getCloudOwner());
        }
        return cloudConfiguration;
    }

    private SubscriberInfo createSubscriberInfo() {
        Customer requestCustomer = buildingBlockExecution.getGeneralBuildingBlock().getCustomer();

        SubscriberInfo subscriberInfo = null;
        if (requestCustomer != null) {
            subscriberInfo = new SubscriberInfo();
            subscriberInfo.setGlobalSubscriberId(requestCustomer.getGlobalCustomerId());
            subscriberInfo.setSubscriberName(requestCustomer.getSubscriberName());
        }

        return subscriberInfo;
    }

    private OwningEntity createOwningEntity() {
        org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity requestOwningEntity =
                buildingBlockExecution.getGeneralBuildingBlock().getServiceInstance().getOwningEntity();
        OwningEntity owningEntity = null;
        if (requestOwningEntity != null) {
            owningEntity = new OwningEntity();
            owningEntity.setOwningEntityId(requestOwningEntity.getOwningEntityId());
            owningEntity.setOwningEntityName(requestOwningEntity.getOwningEntityName());
        }

        return owningEntity;
    }

    private Project createProject() {
        org.onap.so.bpmn.servicedecomposition.bbobjects.Project requestProject =
                buildingBlockExecution.getGeneralBuildingBlock().getServiceInstance().getProject();
        Project project = null;

        if (requestProject != null) {
            project = new Project();
            project.setProjectName(requestProject.getProjectName());
        }
        return project;
    }

    private RelatedInstanceList[] createRelatedInstanceList() {
        RelatedInstance relatedInstance = new RelatedInstance();
        relatedInstance.setModelInfo(parent.getModelInfo());
        relatedInstance.setInstanceId(
                buildingBlockExecution.getGeneralBuildingBlock().getServiceInstance().getServiceInstanceId());
        relatedInstance.setInstanceDirection(InstanceDirection.source);

        RelatedInstanceList relatedInstanceList = new RelatedInstanceList();
        relatedInstanceList.setRelatedInstance(relatedInstance);

        RelatedInstanceList[] relatedInstanceListsArray = new RelatedInstanceList[1];
        relatedInstanceListsArray[0] = relatedInstanceList;

        return relatedInstanceListsArray;
    }
}
