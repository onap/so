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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.vnf.VnfVolumeAdapterClientImpl;
import org.onap.so.client.adapter.vnf.mapper.VnfAdapterObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.shazam.shazamcrest.matcher.Matchers;

@RunWith(MockitoJUnitRunner.Silent.class)
public class VnfAdapterVolumeGroupResourcesTest extends TestDataSetup {
    @InjectMocks
    private VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;

    @Mock
    protected VnfAdapterObjectMapper MOCK_vnfAdapterObjectMapper;

    @Mock
    protected VnfVolumeAdapterClientImpl MOCK_vnfVolumeAdapterClient;

    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private GenericVnf genericVnf;
    private VfModule vfModule;
    private VolumeGroup volumeGroup;
    private CloudRegion cloudRegion;
    private OrchestrationContext orchestrationContext;

    @Before
    public void before() {
        requestContext = buildRequestContext();
        serviceInstance = buildServiceInstance();
        genericVnf = buildGenericVnf();
        serviceInstance.getVnfs().add(genericVnf);
        vfModule = buildVfModule();
        genericVnf.getVfModules().add(vfModule);
        volumeGroup = buildVolumeGroup();
        serviceInstance.getVnfs().get(0).getVolumeGroups().add(volumeGroup);
        cloudRegion = buildCloudRegion();
        orchestrationContext = buildOrchestrationContext();
        orchestrationContext.setIsRollbackEnabled(true);
    }

    @Test
    public void test_createVolumeGroup() throws Exception {
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        CreateVolumeGroupRequest createVolumeGroupRequest = new CreateVolumeGroupRequest();
        createVolumeGroupRequest.setCloudSiteId("cloudSiteId");

        CreateVolumeGroupResponse expectedCreateVolumeGroupResponse = new CreateVolumeGroupResponse();
        expectedCreateVolumeGroupResponse.setVolumeGroupStackId("volumeGroupStackId");
        expectedCreateVolumeGroupResponse.setVolumeGroupCreated(true);

        String sdncVfModuleQueryResponse = "sdncVfModuleQueryResponse";

        doReturn(createVolumeGroupRequest).when(MOCK_vnfAdapterObjectMapper).createVolumeGroupRequestMapper(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup,
                sdncVfModuleQueryResponse);

        CreateVolumeGroupRequest actualCreateVolumeGroupResponse =
                vnfAdapterVolumeGroupResources.createVolumeGroupRequest(requestContext, cloudRegion,
                        orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);

        verify(MOCK_vnfAdapterObjectMapper, times(1)).createVolumeGroupRequestMapper(requestContext, cloudRegion,
                orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);

        assertThat(createVolumeGroupRequest, Matchers.sameBeanAs(actualCreateVolumeGroupResponse));
    }

    @Test
    public void test_deleteVolumeGroup() throws Exception {
        DeleteVolumeGroupRequest deleteVolumeGroupRequest = new DeleteVolumeGroupRequest();
        doReturn(deleteVolumeGroupRequest).when(MOCK_vnfAdapterObjectMapper)
                .deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance, volumeGroup);

        DeleteVolumeGroupRequest expectedDeleteVolumeGroupRequest = new DeleteVolumeGroupRequest();
        DeleteVolumeGroupRequest actualDeleteVolumeGroupRequest = vnfAdapterVolumeGroupResources
                .deleteVolumeGroupRequest(requestContext, cloudRegion, serviceInstance, volumeGroup);

        verify(MOCK_vnfAdapterObjectMapper, times(1)).deleteVolumeGroupRequestMapper(requestContext, cloudRegion,
                serviceInstance, volumeGroup);
        assertThat(expectedDeleteVolumeGroupRequest, Matchers.sameBeanAs(actualDeleteVolumeGroupRequest));
    }
}
