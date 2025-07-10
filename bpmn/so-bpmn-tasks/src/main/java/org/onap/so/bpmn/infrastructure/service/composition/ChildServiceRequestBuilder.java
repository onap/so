package org.onap.so.bpmn.infrastructure.service.composition;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.OwningEntity;
import org.onap.so.serviceinstancebeans.Project;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import java.io.IOException;
import java.util.Map;

public class ChildServiceRequestBuilder {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final BuildingBlockExecution buildingBlockExecution;
    private Service parent;
    private Service child;
    private ServiceInstancesRequest sir;

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
            String USERPARAMSERVICE = "service";
            for (Map<String, Object> params : buildingBlockExecution.getGeneralBuildingBlock().getRequestContext()
                    .getRequestParameters().getUserParams()) {
                if (params.containsKey(USERPARAMSERVICE)) {
                    String input = mapper.writeValueAsString(params.get(USERPARAMSERVICE));
                    parent = mapper.readValue(input, Service.class);
                    if (parent.getResources().getServices() != null) {
                        for (Service service : parent.getResources().getServices()) {
                            if (service.getInstanceName().equals(childSvcInstanceName)) {
                                child = service;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing context user parameters for parent or child service", e);
        }
        return new ChildServiceRequestBuilder(buildingBlockExecution, parent, child);
    }

    public static ChildServiceRequestBuilder getInstance(final BuildingBlockExecution buildingBlockExecution,
            Service parentInstance, Service childInstance) {
        Service child = null;
        Service parent = null;
        if (childInstance != null) {
            parent = parentInstance;
            child = childInstance;
        }
        return new ChildServiceRequestBuilder(buildingBlockExecution, parent, child);
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

        if (context.getAction().equals("deleteInstance")) {
            sir.setRequestDetails(createRequestDetailsDeleteChild(context));
        } else {
            sir.setRequestDetails(createRequestDetails(context));
        }
        return sir;
    }

    private RequestDetails createRequestDetailsDeleteChild(RequestContext context) {
        RequestDetails details = sir.getRequestDetails();

        details.setRequestParameters(createRequestParameters(context, child));
        details.setRequestInfo(createRequestInfo(context));
        details.setCloudConfiguration(createCloudConfiguration());
        details.setModelInfo(child.getModelInfo());
        details.setSubscriberInfo(createSubscriberInfo());
        details.setRelatedInstanceList(createRelatedInstanceList());

        return details;
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
            if (context.getAction().equals("createInstance")) {
                requestParameters.getUserParams().add(Map.of("service", childService));
            }
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
