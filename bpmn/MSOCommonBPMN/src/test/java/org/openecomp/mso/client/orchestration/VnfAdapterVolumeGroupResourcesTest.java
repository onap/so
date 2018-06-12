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

package org.openecomp.mso.client.orchestration;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.adapter.vnf.VnfVolumeAdapterClientImpl;
import org.openecomp.mso.client.adapter.vnf.mapper.VnfAdapterObjectMapper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.shazam.shazamcrest.matcher.Matchers;

@RunWith(MockitoJUnitRunner.class)
public class VnfAdapterVolumeGroupResourcesTest  extends BuildingBlockTestDataSetup {
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
		
		doReturn(createVolumeGroupRequest).when(MOCK_vnfAdapterObjectMapper).createVolumeGroupRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf,  volumeGroup, sdncVfModuleQueryResponse);

		CreateVolumeGroupRequest actualCreateVolumeGroupResponse = vnfAdapterVolumeGroupResources.createVolumeGroupRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf,  volumeGroup, sdncVfModuleQueryResponse);
		
		verify(MOCK_vnfAdapterObjectMapper, times(1)).createVolumeGroupRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
		
		assertThat(createVolumeGroupRequest, Matchers.sameBeanAs(actualCreateVolumeGroupResponse));
	}
	
	@Test
	public void test_deleteVolumeGroup() throws Exception {
		DeleteVolumeGroupRequest deleteVolumeGroupRequest = new DeleteVolumeGroupRequest();
		doReturn(deleteVolumeGroupRequest).when(MOCK_vnfAdapterObjectMapper).deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance, volumeGroup);
	
		DeleteVolumeGroupResponse expectedDeleteVolumeGroupResponse = new DeleteVolumeGroupResponse();
		doReturn(expectedDeleteVolumeGroupResponse).when(MOCK_vnfVolumeAdapterClient).deleteVNFVolumes(volumeGroup.getVolumeGroupId(), deleteVolumeGroupRequest);
		
		DeleteVolumeGroupResponse actualDeleteVolumeGroupResponse = vnfAdapterVolumeGroupResources.deleteVolumeGroup(requestContext, cloudRegion, serviceInstance, volumeGroup);
		
		verify(MOCK_vnfVolumeAdapterClient, times(1)).deleteVNFVolumes(volumeGroup.getVolumeGroupId(), deleteVolumeGroupRequest);
		verify(MOCK_vnfAdapterObjectMapper, times(1)).deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance, volumeGroup);
		assertThat(expectedDeleteVolumeGroupResponse, Matchers.sameBeanAs(actualDeleteVolumeGroupResponse));
	}
}
