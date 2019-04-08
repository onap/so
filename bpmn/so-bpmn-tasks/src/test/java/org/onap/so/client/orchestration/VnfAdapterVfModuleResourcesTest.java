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

package org.onap.so.client.orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.client.adapter.vnf.mapper.VnfAdapterVfModuleObjectMapper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class VnfAdapterVfModuleResourcesTest extends TestDataSetup {
    @InjectMocks
    private VnfAdapterVfModuleResources vnfAdapterVfModuleResources = new VnfAdapterVfModuleResources();

    @Mock
    protected VnfAdapterVfModuleObjectMapper MOCK_vnfAdapterVfModuleObjectMapper;

    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private ModelInfoServiceInstance modelInfoServiceInstance;
    private GenericVnf genericVnf;
    private VfModule vfModule;
    private VolumeGroup volumeGroup;
    private ModelInfoVfModule modelInfoVfModule;
    private CloudRegion cloudRegion;
    private OrchestrationContext orchestrationContext;
    private CreateVfModuleRequest createVfModuleRequest;
    private String sdncVnfQueryResponse;
    private String sdncVfModuleQueryResponse;
    private DeleteVfModuleRequest deleteVfModuleRequest;

    @Before
    public void before() {
        requestContext = buildRequestContext();

        serviceInstance = buildServiceInstance();

        genericVnf = buildGenericVnf();

        vfModule = buildVfModule();

        cloudRegion = buildCloudRegion();

        orchestrationContext = buildOrchestrationContext();
        orchestrationContext.setIsRollbackEnabled(true);

        sdncVnfQueryResponse = new String();
        sdncVfModuleQueryResponse = new String();

        createVfModuleRequest = new CreateVfModuleRequest();
        createVfModuleRequest.setCloudSiteId("cloudSiteId");

        deleteVfModuleRequest = new DeleteVfModuleRequest();
        deleteVfModuleRequest.setCloudSiteId("cloudSiteId");
    }

    @Test
    public void test_createVfModule() throws Exception {
        doReturn(createVfModuleRequest).when(MOCK_vnfAdapterVfModuleObjectMapper).createVfModuleRequestMapper(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, vfModule, null,
                sdncVnfQueryResponse, sdncVfModuleQueryResponse);

        CreateVfModuleRequest actualCreateVfModuleRequest =
                vnfAdapterVfModuleResources.createVfModuleRequest(requestContext, cloudRegion, orchestrationContext,
                        serviceInstance, genericVnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);

        verify(MOCK_vnfAdapterVfModuleObjectMapper, times(1)).createVfModuleRequestMapper(requestContext, cloudRegion,
                orchestrationContext, serviceInstance, genericVnf, vfModule, null, sdncVnfQueryResponse,
                sdncVfModuleQueryResponse);

        assertNotNull(createVfModuleRequest);
        assertNotNull(actualCreateVfModuleRequest);
        assertEquals(createVfModuleRequest, actualCreateVfModuleRequest);
    }

    @Test
    public void test_createVfModuleWithVolumeGroup() throws Exception {
        volumeGroup = buildVolumeGroup();
        doReturn(createVfModuleRequest).when(MOCK_vnfAdapterVfModuleObjectMapper).createVfModuleRequestMapper(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, vfModule, volumeGroup,
                sdncVnfQueryResponse, sdncVfModuleQueryResponse);

        CreateVfModuleRequest actualCreateVfModuleRequest = vnfAdapterVfModuleResources.createVfModuleRequest(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, vfModule, volumeGroup,
                sdncVnfQueryResponse, sdncVfModuleQueryResponse);

        verify(MOCK_vnfAdapterVfModuleObjectMapper, times(1)).createVfModuleRequestMapper(requestContext, cloudRegion,
                orchestrationContext, serviceInstance, genericVnf, vfModule, volumeGroup, sdncVnfQueryResponse,
                sdncVfModuleQueryResponse);

        assertNotNull(createVfModuleRequest);
        assertNotNull(actualCreateVfModuleRequest);
        assertEquals(createVfModuleRequest, actualCreateVfModuleRequest);
    }

    @Test
    public void test_deleteVfModule() throws Exception {
        doReturn(deleteVfModuleRequest).when(MOCK_vnfAdapterVfModuleObjectMapper).deleteVfModuleRequestMapper(
                isA(RequestContext.class), isA(CloudRegion.class), isA(ServiceInstance.class), isA(GenericVnf.class),
                isA(VfModule.class));

        DeleteVfModuleRequest actualDeleteVfModuleRequest = vnfAdapterVfModuleResources
                .deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);

        verify(MOCK_vnfAdapterVfModuleObjectMapper, times(1)).deleteVfModuleRequestMapper(requestContext, cloudRegion,
                serviceInstance, genericVnf, vfModule);
        assertEquals(deleteVfModuleRequest, actualDeleteVfModuleRequest);
    }
}
