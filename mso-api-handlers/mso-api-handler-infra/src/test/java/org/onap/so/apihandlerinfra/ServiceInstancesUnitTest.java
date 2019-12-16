package org.onap.so.apihandlerinfra;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.infra.rest.BpmnRequestBuilder;
import org.onap.so.apihandlerinfra.infra.rest.exception.CloudConfigurationNotFoundException;
import org.onap.so.apihandlerinfra.vnfbeans.ModelType;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.CloudConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstancesUnitTest {

    @Mock
    private BpmnRequestBuilder bpmnRequestBuilder;

    @Spy
    @InjectMocks
    private ServiceInstances serviceInstances;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void getCloudConfigurationOnReplaceVnfTest() throws ApiException {
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setTenantId("tenantId");
        cloudConfiguration.setLcpCloudRegionId("lcpCloudRegionId");
        String requestScope = ModelType.vnf.toString();
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("vnfInstanceId", "17c10d8e-48f4-4ee6-b162-a801943df6d6");
        InfraActiveRequests currentActiveRequest = new InfraActiveRequests();

        doReturn(cloudConfiguration).when(bpmnRequestBuilder)
                .mapCloudConfigurationVnf("17c10d8e-48f4-4ee6-b162-a801943df6d6");
        CloudConfiguration result =
                serviceInstances.getCloudConfigurationOnReplace(requestScope, instanceIdMap, currentActiveRequest);

        assertEquals(cloudConfiguration, result);
    }

    @Test
    public void getCloudConfigurationOnReplaceVfModuleTest() throws ApiException {
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setTenantId("tenantId");
        cloudConfiguration.setLcpCloudRegionId("lcpCloudRegionId");
        String requestScope = ModelType.vfModule.toString();
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("vnfInstanceId", "17c10d8e-48f4-4ee6-b162-a801943df6d6");
        instanceIdMap.put("vfModuleInstanceId", "17c10d8e-48f4-4ee6-b162-a801943df6d8");
        InfraActiveRequests currentActiveRequest = new InfraActiveRequests();

        doReturn(cloudConfiguration).when(bpmnRequestBuilder).getCloudConfigurationVfModuleReplace(
                "17c10d8e-48f4-4ee6-b162-a801943df6d6", "17c10d8e-48f4-4ee6-b162-a801943df6d8");
        CloudConfiguration result =
                serviceInstances.getCloudConfigurationOnReplace(requestScope, instanceIdMap, currentActiveRequest);

        assertEquals(cloudConfiguration, result);
    }

    @Test
    public void getCloudConfigurationReturnsNullTest() throws ApiException {
        String requestScope = ModelType.vfModule.toString();
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("vnfInstanceId", "17c10d8e-48f4-4ee6-b162-a801943df6d6");
        instanceIdMap.put("vfModuleInstanceId", "17c10d8e-48f4-4ee6-b162-a801943df6d8");
        InfraActiveRequests currentActiveRequest = new InfraActiveRequests();

        doReturn(null).when(bpmnRequestBuilder).getCloudConfigurationVfModuleReplace(
                "17c10d8e-48f4-4ee6-b162-a801943df6d6", "17c10d8e-48f4-4ee6-b162-a801943df6d8");
        thrown.expect(CloudConfigurationNotFoundException.class);
        thrown.expectMessage("CloudConfiguration not found during autofill for replace request.");

        serviceInstances.getCloudConfigurationOnReplace(requestScope, instanceIdMap, currentActiveRequest);
    }

    @Test
    public void setCloudConfigurationCurrentActiveRequestTest() {
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setTenantId("tenantId");
        cloudConfiguration.setLcpCloudRegionId("lcpCloudRegionId");

        InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
        serviceInstances.setCloudConfigurationCurrentActiveRequest(cloudConfiguration, currentActiveRequest);

        assertEquals("tenantId", currentActiveRequest.getTenantId());
        assertEquals("lcpCloudRegionId", currentActiveRequest.getCloudRegion());
    }
}
