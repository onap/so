package org.onap.so.bpmn.infrastructure.service.composition;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ChildServiceRequestBuilderTest {

    private BuildingBlockExecution mockExecution;
    private List<Map<String, Object>> userParamsExpected;

    @Before
    public void setUp() throws IOException {
        String incomingRequest =
                "{\"requestDetails\":{\"subscriberInfo\":{\"globalSubscriberId\":\"ubuntu-customer\"},\"requestInfo\":{\"suppressRollback\":false,\"instanceName\":\"LcmDemo\",\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"requestorId\":\"portal\",\"source\":\"postman\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"requestParameters\":{\"subscriptionServiceType\":\"ubuntu\",\"userParams\":[{\"Homing_Solution\":\"none\"},{\"service\":{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}],\"services\":[{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelCustomizationId\":\"cs1-svc-modelCustomizationId\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service1-instanceName\"},{\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelName\":\"UbuntuSriovVF\",\"modelVersionId\":\"5b5d07f0-7449-4eec-95eb-531ddef18240\",\"modelInvariantUuid\":\"9ed17b82-11f3-44cc-a86f-32739360617e\",\"modelVersion\":\"1.0\",\"modelCustomizationId\":\"ae139d3d-b2ae-462c-b09d-c85bdc2e3073\",\"modelInstanceName\":\"UbuntuSriovVF0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"123\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"cloudOwner\"},\"platform\":{\"platformName\":\"openstack\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"wireless\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceName\":\"vnf-instanceName\",\"instanceParams\":[{\"vnf-name\":\"vnf-vnf-name\",\"vnf_name\":\"UbuntuVNFName\"}],\"vfModules\":[{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..base..module-0\",\"modelVersionId\":\"f4ef24dd-7a4e-4eef-83b7-c58f2f3e36e4\",\"modelInvariantUuid\":\"719aab1e-c551-46e0-87e8-a78dcd7891da\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"dea421a8-c1b8-4044-8ca1-58c30de3b315\"},\"instanceName\":\"lcm-demo-network-1\",\"instanceParams\":[{\"name\":\"lcm-demo-network-1\",\"cidr\":\"10.10.10.0/24\"}]},{\"modelInfo\":{\"modelName\":\"Ubuntusriovvf..ubuntu-vf-module..module-1\",\"modelVersionId\":\"112f2de4-4f09-4567-9de1-2d271cb6e164\",\"modelInvariantUuid\":\"ba6d2e11-4e82-4bb8-9d52-a2962a263a09\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"7bc2649e-b96b-44ec-adfe-4a6167f3034e\"},\"instanceName\":\"lcm-demo-ubuntu-1\",\"instanceParams\":[{\"name\":\"lcm-demo-ubuntu-1\",\"network_mgmt\":\"networkMgmt\",\"key_name\":\"demo\",\"network_name\":\"lcm-demo-network-1\",\"image_name\":\"imageName\",\"flavor_name\":\"m1.small\"}]}]}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelCustomizationId\":\"cs1-svc-modelCustomizationId\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service2-instanceName\"}]},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}],\"aLaCarte\":false},\"project\":{\"projectName\":\"Project-UbuntuDemo\"},\"owningEntity\":{\"owningEntityId\":\"33a8b609-1cfe-4d19-8dc2-5b95b921de1e\",\"owningEntityName\":\"demo\"},\"modelInfo\":{\"modelVersion\":\"2.0\",\"modelVersionId\":\"5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ServiceInstancesRequest request = mapper.readValue(incomingRequest, ServiceInstancesRequest.class);
        userParamsExpected = request.getRequestDetails().getRequestParameters().getUserParams();
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
        mockExecution = mock(BuildingBlockExecution.class);
        doReturn(gbb).when(mockExecution).getGeneralBuildingBlock();
    }

    @Test
    public void childServiceRequestBuilderTest() {

        ServiceInstancesRequest sir = ChildServiceRequestBuilder.getInstance(mockExecution, "service1-instanceName")
                .setParentRequestId(mockExecution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId())
                .setCorrelationId(UUID.randomUUID().toString()).setChildSvcInstanceId("childInstanceId").build();

        Assert.assertEquals("childInstanceId", sir.getServiceInstanceId());

        // modelInfo
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        Assert.assertEquals("a316f8fa-c886-483f-801b-6663e35b836c", modelInfo.getModelInvariantId());
        Assert.assertEquals(ModelType.service, modelInfo.getModelType());
        Assert.assertEquals("GuilinLcmSVC", modelInfo.getModelName());
        Assert.assertEquals("2.0", modelInfo.getModelVersion());
        Assert.assertEquals("cs1-svc-modelCustomizationId", modelInfo.getModelCustomizationId());
        Assert.assertEquals("5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52", modelInfo.getModelVersionId());
        Assert.assertEquals("5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52", modelInfo.getModelUuid());
        Assert.assertEquals("a316f8fa-c886-483f-801b-6663e35b836c", modelInfo.getModelInvariantUuid());

        // requestInfo
        RequestInfo requestInfo = sir.getRequestDetails().getRequestInfo();
        Assert.assertNotNull(requestInfo.getCorrelator());
        Assert.assertEquals("FamilyId", requestInfo.getProductFamilyId());
        Assert.assertEquals("source", requestInfo.getSource());
        Assert.assertEquals("service1-instanceName", requestInfo.getInstanceName());
        Assert.assertEquals(false, requestInfo.getSuppressRollback());
        Assert.assertEquals("RequestOrId", requestInfo.getRequestorId());

        RelatedInstanceList[] relatedInstanceList = sir.getRequestDetails().getRelatedInstanceList();
        Assert.assertEquals(1, relatedInstanceList.length);
        RelatedInstance relatedInstance = relatedInstanceList[0].getRelatedInstance();
        Assert.assertEquals("serviceInstanceId", relatedInstance.getInstanceId());
        Assert.assertEquals(InstanceDirection.source, relatedInstance.getInstanceDirection());

        ModelInfo parentModel = relatedInstance.getModelInfo();
        Assert.assertEquals("a316f8fa-c886-483f-801b-6663e35b836c", parentModel.getModelInvariantId());
        Assert.assertEquals(ModelType.service, parentModel.getModelType());
        Assert.assertEquals("GuilinLcmSVC", parentModel.getModelName());
        Assert.assertEquals("2.0", parentModel.getModelVersion());
        Assert.assertEquals("5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52", parentModel.getModelVersionId());
        Assert.assertEquals("5bc2b6b3-c9bb-49a1-89c8-4dac5b236d52", parentModel.getModelUuid());
        Assert.assertEquals("a316f8fa-c886-483f-801b-6663e35b836c", parentModel.getModelInvariantUuid());

        SubscriberInfo subsciberInfo = sir.getRequestDetails().getSubscriberInfo();
        Assert.assertEquals("GlobalCustomerId", subsciberInfo.getGlobalSubscriberId());
        Assert.assertEquals("SubscriberName", subsciberInfo.getSubscriberName());

        CloudConfiguration cloudConfiguration = sir.getRequestDetails().getCloudConfiguration();
        Assert.assertEquals("tenant-id", cloudConfiguration.getTenantId());
        Assert.assertEquals("CloudOwner", cloudConfiguration.getCloudOwner());
        Assert.assertEquals("my-region-id", cloudConfiguration.getLcpCloudRegionId());

        org.onap.so.serviceinstancebeans.RequestParameters requestParameters =
                sir.getRequestDetails().getRequestParameters();
        Assert.assertEquals(2, requestParameters.getUserParams().size());
        Assert.assertEquals(userParamsExpected.get(0), requestParameters.getUserParams().get(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String expectedChildService =
                    mapper.writeValueAsString(getChildService(userParamsExpected, "service1-instanceName"));
            String actualChildService =
                    mapper.writeValueAsString(requestParameters.getUserParams().get(1).get("service"));
            Assert.assertEquals(expectedChildService, actualChildService);
        } catch (Exception e) {
            Assert.fail();
        }

        org.onap.so.serviceinstancebeans.Project project = sir.getRequestDetails().getProject();
        Assert.assertEquals("projectName", project.getProjectName());

        org.onap.so.serviceinstancebeans.OwningEntity owningEntity = sir.getRequestDetails().getOwningEntity();
        Assert.assertEquals("owningEntityId", owningEntity.getOwningEntityId());
        Assert.assertEquals("owningEntityName", owningEntity.getOwningEntityName());
    }

    @Test
    public void childServiceCloudConfigurationRequestBuilderTest() throws IOException {
        String incomingRequest =
                "{\"requestDetails\":{\"subscriberInfo\":{\"globalSubscriberId\":\"ubuntu-customer\"},\"requestInfo\":{\"suppressRollback\":false,\"instanceName\":\"onap-test-parent-child-service\",\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"requestorId\":\"portal\",\"source\":\"postman\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"215\",\"tenantId\":\"e2710e84063b421fab08189818761d55\",\"cloudOwner\":\"iaas\"},\"requestParameters\":{\"subscriptionServiceType\":\"ubuntu\",\"userParams\":[{\"Homing_Solution\":\"none\"},{\"service\":{\"instanceParams\":[],\"resources\":{\"services\":[{\"instanceParams\":[],\"resources\":{},\"modelInfo\":{\"modelVersion\":\"3.0\",\"modelVersionId\":\"872efe6d-c787-4caf-b6f8-3234a7bfc5cf\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service1-instanceName-child\",\"cloudConfiguration\":{\"lcpCloudRegionId\":\"test\",\"tenantId\":\"1234567890\",\"cloudOwner\":\"demo\"}},{\"instanceParams\":[],\"resources\":{},\"modelInfo\":{\"modelVersion\":\"3.0\",\"modelVersionId\":\"872efe6d-c787-4caf-b6f8-3234a7bfc5cf\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"},\"instanceName\":\"service2-instanceName-child\"}]},\"modelInfo\":{\"modelVersion\":\"3.0\",\"modelVersionId\":\"872efe6d-c787-4caf-b6f8-3234a7bfc5cf\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}],\"aLaCarte\":false},\"project\":{\"projectName\":\"Project-UbuntuDemo\"},\"owningEntity\":{\"owningEntityId\":\"33a8b609-1cfe-4d19-8dc2-5b95b921de1e\",\"owningEntityName\":\"seb\"},\"modelInfo\":{\"modelVersion\":\"3.0\",\"modelVersionId\":\"872efe6d-c787-4caf-b6f8-3234a7bfc5cf\",\"modelInvariantId\":\"a316f8fa-c886-483f-801b-6663e35b836c\",\"modelName\":\"GuilinLcmSVC\",\"modelType\":\"service\"}}}";

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

        ServiceInstance serviceInstance = new ServiceInstance();
        OwningEntity owningEntity = new OwningEntity();
        owningEntity.setOwningEntityId("owningEntityId");
        owningEntity.setOwningEntityName("owningEntityName");
        serviceInstance.setOwningEntity(owningEntity);

        GeneralBuildingBlock gbb = new GeneralBuildingBlock();
        gbb.setCloudRegion(cloudRegion);
        gbb.setRequestContext(context);
        gbb.setServiceInstance(serviceInstance);
        mockExecution = mock(BuildingBlockExecution.class);
        doReturn(gbb).when(mockExecution).getGeneralBuildingBlock();

        ServiceInstancesRequest sir = ChildServiceRequestBuilder
                .getInstance(mockExecution, "service1-instanceName-child")
                .setParentRequestId(mockExecution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId())
                .setCorrelationId(UUID.randomUUID().toString()).setChildSvcInstanceId("childInstanceId").build();

        Assert.assertEquals("childInstanceId", sir.getServiceInstanceId());

        CloudConfiguration cloudConfiguration = sir.getRequestDetails().getCloudConfiguration();
        Assert.assertEquals("1234567890", cloudConfiguration.getTenantId());
        Assert.assertEquals("demo", cloudConfiguration.getCloudOwner());
        Assert.assertEquals("test", cloudConfiguration.getLcpCloudRegionId());
    }

    private Service getChildService(List<Map<String, Object>> userParams, String serviceInstanceName)
            throws IOException {
        String USERPARAMSERVICE = "service";
        for (Map<String, Object> params : userParams) {
            if (params.containsKey(USERPARAMSERVICE)) {
                ObjectMapper obj = new ObjectMapper();
                String input = obj.writeValueAsString(params.get(USERPARAMSERVICE));
                Service validate = obj.readValue(input, Service.class);
                if (validate.getResources().getServices() != null) {
                    for (Service service : validate.getResources().getServices()) {
                        if (serviceInstanceName.equals(service.getInstanceName())) {
                            return service;
                        }
                    }
                }
            }
        }
        return null;
    }


}
