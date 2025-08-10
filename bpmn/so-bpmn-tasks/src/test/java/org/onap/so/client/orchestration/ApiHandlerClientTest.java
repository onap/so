package org.onap.so.client.orchestration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;


@TestPropertySource(properties = {"mso.adapters.apihandler.endpoint=http://localhost:",
        "mso.adapters.apihandler.auth=Basic dGVzdDp0ZXN0Cg=="})
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiHandlerClientTest {

    @InjectMocks
    private ApiHandlerClient client;

    @Mock
    protected RestTemplate restTemplate;

    private ServiceInstancesRequest buildRequest(String incomingRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ServiceInstancesRequest request = null;
        try {
            request = mapper.readValue(incomingRequest, ServiceInstancesRequest.class);
            System.out.println("Java Object: " + request);
        } catch (Exception e) {
            System.out.println("Caught Exception " + e.getMessage());
            Assert.fail("Could not build ServiceInstancesRequest object");
        }
        return request;
    }

    @Test
    public void createServiceInstanceTest() {
        String incomingRequest =
                "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"owningEntity\":{\"owningEntityId\":\"038d99af-0427-42c2-9d15-971b99b9b489\",\"owningEntityName\":\"PACKETCORE\"},\"project\":{\"projectName\":\"{someprojectname}\"},\"subscriberInfo\":{\"globalSubscriberId\":\"{somesubscriberid}\"},\"requestInfo\":{\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"source\":\"VID\",\"suppressRollback\":true,\"requestorId\":\"xxxxxx\"},\"requestParameters\":{\"subscriptionServiceType\":\"VMX\",\"aLaCarte\":false,\"userParams\":[{\"service\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelType\":\"vnf\",\"modelName\":\"2016-73_MOW\",\"modelVersionId\":\"7f40c192-f63c-463e-ba94-286933b895f8\",\"modelCustomizationName\":\"2016-73_MOW\",\"modelCustomizationId\":\"ab153b6e-c364-44c0-bef6-1f2982117f04\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"platform\":{\"platformName\":\"test\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"someValue\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceParams\":[],\"vfModules\":[{\"modelInfo\":{\"modelType\":\"vfModule\",\"modelName\":\"201673MowtestBvL\",\"modelVersionId\":\"4c75f813-fa91-45a4-89d0-790ff5f1ae79\",\"modelCustomizationId\":\"a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f\"},\"instanceName\":\"vfModule1\",\"instanceParams\":[{\"vmx_int_net_len\":\"24\"}]}]}]}}}]}}}";
        ServiceInstancesRequest request = buildRequest(incomingRequest);
        ResponseEntity<ServiceInstancesResponse> responseEntity =
                new ResponseEntity<ServiceInstancesResponse>(new ServiceInstancesResponse(), HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(), ArgumentMatchers.<Class<ServiceInstancesResponse>>any()))
                .thenReturn(responseEntity);

        try {
            ServiceInstancesResponse response = client.createServiceInstance(request);
            assertThat(response, instanceOf(ServiceInstancesResponse.class));
        } catch (ApiHandlerClientException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createServiceInstanceErrorTest() {
        String incomingRequest =
                "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"owningEntity\":{\"owningEntityId\":\"038d99af-0427-42c2-9d15-971b99b9b489\",\"owningEntityName\":\"PACKETCORE\"},\"project\":{\"projectName\":\"{someprojectname}\"},\"subscriberInfo\":{\"globalSubscriberId\":\"{somesubscriberid}\"},\"requestInfo\":{\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"source\":\"VID\",\"suppressRollback\":true,\"requestorId\":\"xxxxxx\"},\"requestParameters\":{\"subscriptionServiceType\":\"VMX\",\"aLaCarte\":false,\"userParams\":[{\"service\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelType\":\"vnf\",\"modelName\":\"2016-73_MOW\",\"modelVersionId\":\"7f40c192-f63c-463e-ba94-286933b895f8\",\"modelCustomizationName\":\"2016-73_MOW\",\"modelCustomizationId\":\"ab153b6e-c364-44c0-bef6-1f2982117f04\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"platform\":{\"platformName\":\"test\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"someValue\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceParams\":[],\"vfModules\":[{\"modelInfo\":{\"modelType\":\"vfModule\",\"modelName\":\"201673MowtestBvL\",\"modelVersionId\":\"4c75f813-fa91-45a4-89d0-790ff5f1ae79\",\"modelCustomizationId\":\"a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f\"},\"instanceName\":\"vfModule1\",\"instanceParams\":[{\"vmx_int_net_len\":\"24\"}]}]}]}}}]}}}";
        ServiceInstancesRequest request = buildRequest(incomingRequest);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(), ArgumentMatchers.<Class<ServiceInstancesResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        try {
            client.createServiceInstance(request);
            Assert.fail("ApiHandlerClientException exception was not thrown");
        } catch (ApiHandlerClientException e) {
            assertEquals("Failed sending service createInstance request to api-handler. Error: ", e.getMessage());
        }
    }

    @Test
    public void deleteServiceInstanceTest() {
        String incomingRequest =
                "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"owningEntity\":{\"owningEntityId\":\"038d99af-0427-42c2-9d15-971b99b9b489\",\"owningEntityName\":\"PACKETCORE\"},\"project\":{\"projectName\":\"{someprojectname}\"},\"subscriberInfo\":{\"globalSubscriberId\":\"{somesubscriberid}\"},\"requestInfo\":{\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"source\":\"VID\",\"suppressRollback\":true,\"requestorId\":\"xxxxxx\"},\"requestParameters\":{\"subscriptionServiceType\":\"VMX\",\"aLaCarte\":false,\"userParams\":[{\"service\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelType\":\"vnf\",\"modelName\":\"2016-73_MOW\",\"modelVersionId\":\"7f40c192-f63c-463e-ba94-286933b895f8\",\"modelCustomizationName\":\"2016-73_MOW\",\"modelCustomizationId\":\"ab153b6e-c364-44c0-bef6-1f2982117f04\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"platform\":{\"platformName\":\"test\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"someValue\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceParams\":[],\"vfModules\":[{\"modelInfo\":{\"modelType\":\"vfModule\",\"modelName\":\"201673MowtestBvL\",\"modelVersionId\":\"4c75f813-fa91-45a4-89d0-790ff5f1ae79\",\"modelCustomizationId\":\"a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f\"},\"instanceName\":\"vfModule1\",\"instanceParams\":[{\"vmx_int_net_len\":\"24\"}]}]}]}}}]}}}";
        ServiceInstancesRequest request = buildRequest(incomingRequest);
        ResponseEntity<ServiceInstancesResponse> responseEntity =
                new ResponseEntity<ServiceInstancesResponse>(new ServiceInstancesResponse(), HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(), ArgumentMatchers.<Class<ServiceInstancesResponse>>any()))
                .thenReturn(responseEntity);

        try {
            ServiceInstancesResponse response = client.deleteServiceInstance(request);
            assertThat(response, instanceOf(ServiceInstancesResponse.class));
        } catch (ApiHandlerClientException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteServiceInstanceErrorTest() {
        String incomingRequest =
                "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"owningEntity\":{\"owningEntityId\":\"038d99af-0427-42c2-9d15-971b99b9b489\",\"owningEntityName\":\"PACKETCORE\"},\"project\":{\"projectName\":\"{someprojectname}\"},\"subscriberInfo\":{\"globalSubscriberId\":\"{somesubscriberid}\"},\"requestInfo\":{\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"source\":\"VID\",\"suppressRollback\":true,\"requestorId\":\"xxxxxx\"},\"requestParameters\":{\"subscriptionServiceType\":\"VMX\",\"aLaCarte\":false,\"userParams\":[{\"service\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantId\":\"5d48acb5-097d-4982-aeb2-f4a3bd87d31b\",\"modelVersionId\":\"3c40d244-808e-42ca-b09a-256d83d19d0a\",\"modelName\":\"MOWvMXBV1Service\",\"modelVersion\":\"10.0\"},\"instanceParams\":[],\"resources\":{\"vnfs\":[{\"modelInfo\":{\"modelType\":\"vnf\",\"modelName\":\"2016-73_MOW\",\"modelVersionId\":\"7f40c192-f63c-463e-ba94-286933b895f8\",\"modelCustomizationName\":\"2016-73_MOW\",\"modelCustomizationId\":\"ab153b6e-c364-44c0-bef6-1f2982117f04\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"88a6ca3ee0394ade9403f075db23167e\"},\"platform\":{\"platformName\":\"test\"},\"lineOfBusiness\":{\"lineOfBusinessName\":\"someValue\"},\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"instanceParams\":[],\"vfModules\":[{\"modelInfo\":{\"modelType\":\"vfModule\",\"modelName\":\"201673MowtestBvL\",\"modelVersionId\":\"4c75f813-fa91-45a4-89d0-790ff5f1ae79\",\"modelCustomizationId\":\"a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f\"},\"instanceName\":\"vfModule1\",\"instanceParams\":[{\"vmx_int_net_len\":\"24\"}]}]}]}}}]}}}";
        ServiceInstancesRequest request = buildRequest(incomingRequest);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(), ArgumentMatchers.<Class<ServiceInstancesResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        try {
            client.deleteServiceInstance(request);
            Assert.fail("ApiHandlerClientException exception was not thrown");
        } catch (ApiHandlerClientException e) {
            assertEquals("Failed sending service deleteInstance request to api-handler. Error: ", e.getMessage());
        }
    }

}
