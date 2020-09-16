
/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2020 Huawei
 * Technologies Co., Ltd. All rights reserved.
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.cnf.rest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceMiniResponse;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.Resource;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class CnfAdapterRestTest {

    @InjectMocks
    CnfAdapterRest cnfAdapterRest;

    @Mock
    CnfAdapterService cnfAdapterService;

    @Mock
    ResponseEntity<InstanceResponse> createInstanceResponse;

    @Mock
    ResponseEntity<InstanceMiniResponseList> instacneMiniResponseList;

    @Mock
    ResponseEntity<InstanceStatusResponse> instanceStatusResponse;

    @Test
    public void healthCheckTest() throws Exception {

        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.healthCheck()).thenReturn(response);
        cnfAdapterRest.healthCheck();
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createInstanceTest() throws Exception {

        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<String, String>();
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        bpmnInstanceRequest.setCloudRegionId("v1");
        bpmnInstanceRequest.setLabels(labels);
        bpmnInstanceRequest.setModelInvariantId("krd");
        bpmnInstanceRequest.setModelVersionId("p1");
        bpmnInstanceRequest.setOverrideValues(overrideValues);
        bpmnInstanceRequest.setVfModuleUUID("20200824");
        List<Resource> resourceList = new ArrayList<Resource>();
        InstanceResponse instanceResponse = new InstanceResponse(HttpStatus.CREATED.toString());
        instanceResponse.setId("123");
        instanceResponse.setNamespace("testNamespace");
        instanceResponse.setRequest(new MulticloudInstanceRequest());
        instanceResponse.setResources(resourceList);
        createInstanceResponse = new ResponseEntity<InstanceResponse>(instanceResponse, HttpStatus.CREATED);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.createInstance(bpmnInstanceRequest)).thenReturn(createInstanceResponse);
        cnfAdapterRest.createInstance(bpmnInstanceRequest);
        Assert.assertNotNull(createInstanceResponse);
        Assert.assertEquals(HttpStatus.CREATED, createInstanceResponse.getStatusCode());
    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        createInstanceResponse = new ResponseEntity<InstanceResponse>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByInstanceId(instanceId)).thenReturn(createInstanceResponse);
        cnfAdapterRest.getInstanceByInstanceId(instanceId);
        Assert.assertNotNull(createInstanceResponse);
        Assert.assertEquals(HttpStatus.OK, createInstanceResponse.getStatusCode());
    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.deleteInstanceByInstanceId(instanceId)).thenReturn(response);
        cnfAdapterRest.deleteInstanceByInstanceId(instanceId);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {

        String instanceId = "123";
        instanceStatusResponse = new ResponseEntity<InstanceStatusResponse>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceStatusByInstanceId(instanceId)).thenReturn(instanceStatusResponse);
        cnfAdapterRest.getInstanceStatusByInstanceId(instanceId);
        Assert.assertNotNull(instanceStatusResponse);
        Assert.assertEquals(HttpStatus.OK, instanceStatusResponse.getStatusCode());
    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {

        String rbName = "xyz";
        String rbVersion = "v1";
        String profileName = "p1";
        InstanceMiniResponse instanceMiniResponse = new InstanceMiniResponse(HttpStatus.OK.toString());
        List<InstanceMiniResponse> instancList = new ArrayList<InstanceMiniResponse>();
        instancList.add(instanceMiniResponse);
        InstanceMiniResponseList instanceMiniRespList = new InstanceMiniResponseList(HttpStatus.OK.toString());
        instanceMiniRespList.setInstancList(instancList);
        instanceMiniRespList.setErrorMsg(HttpStatus.OK.toString());
        ResponseEntity<InstanceMiniResponseList> respone =
                new ResponseEntity<InstanceMiniResponseList>(instanceMiniRespList, HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName))
                .thenReturn(instacneMiniResponseList);
        cnfAdapterRest.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
        Assert.assertNotNull(instacneMiniResponseList);
        Assert.assertEquals(HttpStatus.OK.toString(), instanceMiniRespList.getErrorMsg());
    }

}

