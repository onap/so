package org.onap.so.bpmn.infrastructure.service.composition;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.MockControllerBB;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.ApiHandlerClient;
import org.onap.so.client.orchestration.ApiHandlerClientException;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_PAYLOAD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CreateChildServiceBB.class, MockControllerBB.class, ExceptionBuilder.class})
public class CreateChildServiceBBTest {

    @Autowired
    private CreateChildServiceBB createChildServiceBB;

    @MockBean
    private BuildingBlockExecution execution;

    @MockBean
    private ApiHandlerClient apiHandlerClient;

    @MockBean
    private ExceptionBuilder exceptionBuilder;

    @Before
    public void setUp() throws IOException, ApiHandlerClientException {
        String incomingRequest =
                "{\"requestDetails\":{\"subscriberInfo\":{\"globalSubscriberId\":\"ubuntu-customer\"},\"requestInfo\":{\"suppressRollback\":false,\"instanceName\":\"LcmDemo\",\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"requestorId\":\"portal\",\"source\":\"postman\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"requestParameters\":{\"subscriptionServiceType\":\"ubuntu\",\"userParams\":[{\"Homing_Solution\":\"none\"},{\"service\":{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}],\"services\":[{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelCustomizationId\":\"cs1-svc-modelCustomizationId\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service1-instanceName\"},{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelCustomizationId\":\"cs1-svc-modelCustomizationId\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service2-instanceName\"}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}],\"aLaCarte\":false},\"project\":{\"projectName\":\"Project-UbuntuDemo\"},\"owningEntity\":{\"owningEntityId\":\"33a8b609-1cfe-4d19-8dc2-5b95b921de1e\",\"owningEntityName\":\"demo\"},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ServiceInstancesRequest request = mapper.readValue(incomingRequest, ServiceInstancesRequest.class);
        RequestContext context = new RequestContext();
        RequestParameters parameters = new RequestParameters();
        parameters.setUserParams(request.getRequestDetails().getRequestParameters().getUserParams());
        context.setRequestParameters(parameters);
        context.setProductFamilyId("FamilyId");
        context.setSource("source");
        context.setRequestorId("RequestOrId");

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setCloudOwner("CloudOwner");
        cloudRegion.setLcpCloudRegionId("my-region-id");
        cloudRegion.setTenantId("tenant-id");

        Customer customer = new Customer();
        customer.setGlobalCustomerId("GlobalCustomerId");
        customer.setSubscriberName("SubscriberName");

        ServiceInstance serviceInstance = new ServiceInstance();
        OwningEntity owningEntity = new OwningEntity();
        owningEntity.setOwningEntityId("owningEntityId");
        owningEntity.setOwningEntityName("owningEntityName");
        serviceInstance.setOwningEntity(owningEntity);

        Project project = new Project();
        project.setProjectName("projectName");
        serviceInstance.setProject(project);

        serviceInstance.setServiceInstanceId("serviceInstanceId");

        GeneralBuildingBlock gbb = new GeneralBuildingBlock();
        gbb.setCloudRegion(cloudRegion);
        gbb.setCustomer(customer);
        gbb.setRequestContext(context);
        gbb.setServiceInstance(serviceInstance);

        Map<ResourceKey, String> map = new HashMap<>();
        map.put(ResourceKey.CHILD_SERVICE_INSTANCE_NAME, "service1-instanceName");

        when(execution.getGeneralBuildingBlock()).thenReturn(gbb);
        when(execution.getLookupMap()).thenReturn(map);

        ServiceInstancesResponse response = new ServiceInstancesResponse();
        response.setRequestReferences(new RequestReferences());
        response.getRequestReferences().setInstanceId("instanceId");
        response.getRequestReferences().setRequestId("requestId");

        when(apiHandlerClient.createServiceInstance(any())).thenReturn(response);
    }

    @Test
    public void buildRequestTest() {
        assertDoesNotThrow(() -> createChildServiceBB.buildRequest(execution));
    }

    @Test
    public void sendRequestTest() throws Exception {
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails details = new RequestDetails();
        details.setRequestInfo(new RequestInfo());
        details.getRequestInfo().setCorrelator("correlator");
        sir.setRequestDetails(details);

        when(execution.getVariable(CHILD_SVC_REQ_PAYLOAD)).thenReturn(sir);
        createChildServiceBB.sendRequest(execution);
        verify(execution).setVariable(CHILD_SVC_REQ_ID, "requestId");
        verify(execution).setVariable(CHILD_SVC_INSTANCE_ID, "instanceId");
        verify(execution).setVariable(CHILD_SVC_REQ_CORRELATION_ID, "correlator");
    }

    @Test
    public void handleFailureTest() {
        createChildServiceBB.handleFailure(execution);
        verify(exceptionBuilder).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), anyInt(),
                anyString(), any(ONAPComponentsList.class));
    }
}
