package org.onap.so.adapters.cnf.service;

import javax.ws.rs.InternalServerErrorException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
public class CnfAdapterServiceTest {

    @InjectMocks
    CnfAdapterService cnfAdapterService;

    @Mock
    ResponseEntity<InstanceResponse> createInstanceResponse;

    @Mock
    ResponseEntity<InstanceMiniResponseList> instacneMiniResponseList;

    @Mock
    ResponseEntity<InstanceStatusResponse> instanceStatusResponse;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void healthCheckTest() throws Exception {

        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);

        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any())).thenReturn(response);

        ResponseEntity<String> actualResponse = cnfAdapterService.healthCheck();
        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

    }

    @Test
    public void createInstanceTest() throws Exception {

        ResponseEntity<InstanceResponse> response = new ResponseEntity<InstanceResponse>(HttpStatus.OK);
        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        bpmnInstanceRequest.setK8sRBProfileName("k8sRBProfileName");
        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<InstanceResponse>>any())).thenReturn(response);

        ResponseEntity<InstanceResponse> actualResponse = cnfAdapterService.createInstance(bpmnInstanceRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(actualResponse.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void createInstanceExceptionTest() throws Exception {

        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        ResponseEntity<InstanceResponse> response = cnfAdapterService.createInstance(bpmnInstanceRequest);
        Assert.assertNull(response);

    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {

        ResponseEntity<InstanceResponse> response = new ResponseEntity<InstanceResponse>(HttpStatus.OK);
        String instanceId = "123";
        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<InstanceResponse>>any())).thenReturn(response);

        ResponseEntity<InstanceResponse> actualResponse = cnfAdapterService.getInstanceByInstanceId(instanceId);
        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {

        ResponseEntity<InstanceStatusResponse> response = new ResponseEntity<InstanceStatusResponse>(HttpStatus.OK);
        String instanceId = "123";
        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<InstanceStatusResponse>>any())).thenReturn(response);

        ResponseEntity<InstanceStatusResponse> actualResponse =
                cnfAdapterService.getInstanceStatusByInstanceId(instanceId);
        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {

        ResponseEntity<InstanceMiniResponseList> response = new ResponseEntity<InstanceMiniResponseList>(HttpStatus.OK);
        String rbName = "xyz";
        String rbVersion = "v1";
        String profileName = "p1";

        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<InstanceMiniResponseList>>any())).thenReturn(response);

        ResponseEntity<InstanceMiniResponseList> actualResponse =
                cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {

        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        String instanceId = "123";
        Mockito.when(restTemplate.exchange(Matchers.anyString(), Matchers.any(HttpMethod.class),
                Matchers.<HttpEntity<?>>any(), Matchers.<Class<String>>any())).thenReturn(response);

        ResponseEntity<String> actualResponse = cnfAdapterService.deleteInstanceByInstanceId(instanceId);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

}
