package org.onap.so.adapters.requestsdb.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RequestsDbClientTest {

    @Autowired
    private RequestDbClientPortChanger requestsDbClient;

    private InfraActiveRequests infraActiveRequests;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        requestsDbClient.setPort(port);

        infraActiveRequests = new InfraActiveRequests();
        infraActiveRequests.setRequestId(UUID.randomUUID().toString());
        infraActiveRequests.setOperationalEnvId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceName("serviceInstanceNameTest");
        infraActiveRequests.setVnfId(UUID.randomUUID().toString());
        infraActiveRequests.setVnfName("vnfInstanceNameTest");
        infraActiveRequests.setVfModuleId(UUID.randomUUID().toString());
        infraActiveRequests.setVfModuleName("vfModuleInstanceNameTest");
        infraActiveRequests.setVolumeGroupId(UUID.randomUUID().toString());
        infraActiveRequests.setVolumeGroupName("volumeGroupInstanceNameTest");
        infraActiveRequests.setNetworkId(UUID.randomUUID().toString());
        infraActiveRequests.setNetworkName("networkInstanceNameTest");
        infraActiveRequests.setConfigurationId(UUID.randomUUID().toString());
        infraActiveRequests.setConfigurationName("configurationInstanceNameTest");
        infraActiveRequests.setAicCloudRegion("1");
        infraActiveRequests.setTenantId(UUID.randomUUID().toString());
        infraActiveRequests.setRequestScope("operationalEnvironment");
        infraActiveRequests.setRequestorId(UUID.randomUUID().toString());
        infraActiveRequests.setSource("sourceTest");
        infraActiveRequests.setOperationalEnvName(UUID.randomUUID().toString());
        infraActiveRequests.setRequestStatus("IN_PROGRESS");
        infraActiveRequests.setAction("create");
        infraActiveRequests.setRequestAction("someaction");
        requestsDbClient.save(infraActiveRequests);
    }

    private void verifyOperationStatus(OperationStatus request,OperationStatus response){
        assertThat(request, sameBeanAs(response).ignoring("operateAt").ignoring("finishedAt"));
   }

    private void verifyInfraActiveRequests(InfraActiveRequests infraActiveRequestsResponse) {
        assertThat(infraActiveRequestsResponse, sameBeanAs(infraActiveRequests).ignoring("modifyTime").ignoring("log"));
    }

    @Test
    public void getCloudOrchestrationFiltersFromInfraActiveTest() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());
        requestMap.put("operationalEnvironmentName", infraActiveRequests.getOperationalEnvName());
        requestMap.put("resourceType", "operationalEnvironment");

        List<InfraActiveRequests> iarr = requestsDbClient.getCloudOrchestrationFiltersFromInfraActive(requestMap);

        assertEquals(1, iarr.size());
        InfraActiveRequests infraActiveRequestsResponse = iarr.get(0);
        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }


    @Test
    public void checkVnfIdStatusTest() {
        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.checkVnfIdStatus(infraActiveRequests.getOperationalEnvId());
        verifyInfraActiveRequests(infraActiveRequestsResponse);
        assertNull(requestsDbClient.checkVnfIdStatus(UUID.randomUUID().toString()));
    }

    @Test
    public void checkInstanceNameDuplicateTest() {
        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.checkInstanceNameDuplicate(null,infraActiveRequests.getOperationalEnvName(),infraActiveRequests.getRequestScope());

        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }

    @Test
    public void checkInstanceNameDuplicateViaTest() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());

        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.checkInstanceNameDuplicate((HashMap<String, String>)requestMap,null,infraActiveRequests.getRequestScope());

        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }

    @Test
    public void getOrchestrationFiltersFromInfraActiveTest() {
        Map<String, List<String>> requestMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceId());
        requestMap.put("serviceInstanceId", values);

        values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceName());
        requestMap.put("serviceInstanceName", values);

        List<InfraActiveRequests> iaar = requestsDbClient.getOrchestrationFiltersFromInfraActive(requestMap);

        assertEquals(1, iaar.size());
        InfraActiveRequests infraActiveRequestsResponse = iaar.get(0);

        verifyInfraActiveRequests(infraActiveRequestsResponse);
        values = new ArrayList<>();
        values.add("EQUALS");
        values.add(UUID.randomUUID().toString());
        requestMap.put("serviceInstanceName", values);
        requestsDbClient.getOrchestrationFiltersFromInfraActive(requestMap);
    }

    @Test
    public void getInfraActiveRequestbyRequestIdTest(){
        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.getInfraActiveRequestbyRequestId(infraActiveRequests.getRequestId());
        verifyInfraActiveRequests(infraActiveRequestsResponse);       
        infraActiveRequestsResponse = requestsDbClient.getInfraActiveRequestbyRequestId(infraActiveRequests.getRequestId());
        
        assertNull(requestsDbClient.getInfraActiveRequestbyRequestId(UUID.randomUUID().toString()));
    }
    

    @Test
    public void getOneByServiceIdAndOperationIdTest(){
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setProgress("IN_PROGRESS");
        operationStatus.setResult("FAILED");
        operationStatus.setServiceId(UUID.randomUUID().toString());
        operationStatus.setOperationContent("operation-content");
        operationStatus.setOperation("operation");
        operationStatus.setOperationId(UUID.randomUUID().toString());
        operationStatus.setReason("reason-test");
        operationStatus.setUserId(UUID.randomUUID().toString());
        operationStatus.setServiceName("test-service");
        requestsDbClient.save(operationStatus);

        OperationStatus operationStatusResponse = requestsDbClient.getOneByServiceIdAndOperationId(operationStatus.getServiceId(),operationStatus.getOperationId());

        verifyOperationStatus(operationStatus,operationStatusResponse);

        assertNull(requestsDbClient.getOneByServiceIdAndOperationId(UUID.randomUUID().toString(),operationStatus.getOperationId()));
    }
}