/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.namingservice.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.namingservice.NamingRequestObject;
import org.onap.so.client.namingservice.NamingServiceConstants;

public class NamingServiceCreateTasksTest extends BaseTaskTest {
    @InjectMocks
    private NamingServiceCreateTasks namingServiceCreateTasks;

    @Mock
    private BBInputSetupUtils MOCK_bbInputSetupUtils;

    private InstanceGroup instanceGroup;
    private ServiceInstance serviceInstance;

    @Before
    public void before() throws BBObjectNotFoundException {
        instanceGroup = setInstanceGroup();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.INSTANCE_GROUP_ID)))
                .thenReturn(instanceGroup);
        serviceInstance = setServiceInstance();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
    }

    @Test
    public void createInstanceGroupTest() throws Exception {
        String policyInstanceName = "policyInstanceName";
        String nfNamingCode = "nfNamingCode";
        String generatedName = "generatedInstanceGroupName";
        execution.setVariable(policyInstanceName, policyInstanceName);
        execution.setVariable(nfNamingCode, nfNamingCode);
        doReturn(generatedName).when(namingServiceResources).generateInstanceGroupName(instanceGroup,
                policyInstanceName, nfNamingCode);

        namingServiceCreateTasks.createInstanceGroupName(execution);
        verify(namingServiceResources, times(1)).generateInstanceGroupName(instanceGroup, policyInstanceName,
                nfNamingCode);
        assertEquals(instanceGroup.getInstanceGroupName(), generatedName);
    }

    @Test
    public void createInstanceGroupExceptionTest() throws Exception {
        expectedException.expect(BBObjectNotFoundException.class);
        lookupKeyMap.put(ResourceKey.INSTANCE_GROUP_ID, "notfound");
        doThrow(BBObjectNotFoundException.class).when(extractPojosForBB).extractByKey(any(),
                ArgumentMatchers.eq(ResourceKey.INSTANCE_GROUP_ID));
        String policyInstanceName = "policyInstanceName";
        String nfNamingCode = "nfNamingCode";
        execution.setVariable(policyInstanceName, policyInstanceName);
        execution.setVariable(nfNamingCode, nfNamingCode);
        doReturn("").when(namingServiceResources).generateInstanceGroupName(instanceGroup, policyInstanceName,
                nfNamingCode);
        namingServiceCreateTasks.createInstanceGroupName(execution);
        verify(namingServiceResources, times(1)).generateInstanceGroupName(instanceGroup, policyInstanceName,
                nfNamingCode);
    }

    @Test
    public void createWanTransportServiceNameTest() throws Exception {
        String generatedName = "generatedWanTransportServiceName";
        doReturn(generatedName).when(namingServiceResources).generateServiceInstanceName(any());
        NamingServiceCreateTasks spy = Mockito.spy(namingServiceCreateTasks);
        ArgumentCaptor<NamingRequestObject> captor = ArgumentCaptor.forClass(NamingRequestObject.class);

        spy.createWanTransportServiceName(execution);
        verify(namingServiceResources, times(1)).generateServiceInstanceName(captor.capture());
        assertTrue(captor.getAllValues().get(0).getExternalKeyValue().startsWith("testServiceInstanceId"));
        assertEquals(captor.getAllValues().get(0).getNamingTypeValue(), NamingServiceConstants.NAMING_TYPE_SERVICE);
        assertEquals(captor.getAllValues().get(0).getResourceNameValue(),
                NamingServiceConstants.RESOURCE_NAME_SERVICE_INSTANCE_NAME);
        assertEquals(captor.getAllValues().get(0).getPolicyInstanceNameValue(), null);
        assertTrue(captor.getAllValues().get(0).getServiceModelNameValue().startsWith("testModelName"));
        assertTrue(captor.getAllValues().get(0).getModelVersionValue().startsWith("testModelVersion"));
        assertEquals(generatedName, serviceInstance.getServiceInstanceName());
    }

    @Test
    public void createVpnBondingServiceNameTest() throws Exception {
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.NETWORK_ID)))
                .thenReturn(buildL3Network());
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VPN_ID)))
                .thenReturn(buildVpnBinding());
        ArgumentCaptor<NamingRequestObject> captor = ArgumentCaptor.forClass(NamingRequestObject.class);
        String generatedName = "generatedWanTransportServiceName";
        doReturn(generatedName).when(namingServiceResources).generateServiceInstanceName(any());
        namingServiceCreateTasks.createVpnBondingServiceName(execution);
        verify(namingServiceResources, times(1)).generateServiceInstanceName(captor.capture());
        assertTrue(captor.getAllValues().get(0).getExternalKeyValue().startsWith("testServiceInstanceId"));
        assertEquals(captor.getAllValues().get(0).getPolicyInstanceNameValue(), null);
        assertEquals(captor.getAllValues().get(0).getNamingTypeValue(), NamingServiceConstants.NAMING_TYPE_SERVICE);
        assertTrue(captor.getAllValues().get(0).getServiceModelNameValue().startsWith("testModelName"));
        assertTrue(captor.getAllValues().get(0).getModelVersionValue().startsWith("testModelVersion"));
        assertTrue(captor.getAllValues().get(0).getNetworkNameValue().startsWith("testNetworkName"));
        assertTrue(captor.getAllValues().get(0).getVpnNameValue().startsWith("testVpnName"));
        assertEquals(captor.getAllValues().get(0).getResourceNameValue(),
                NamingServiceConstants.RESOURCE_NAME_SERVICE_INSTANCE_NAME);
        assertEquals(generatedName, serviceInstance.getServiceInstanceName());
    }
}
