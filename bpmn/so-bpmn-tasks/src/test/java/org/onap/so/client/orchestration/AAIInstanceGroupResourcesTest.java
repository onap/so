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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIEdgeLabel;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
@RunWith(MockitoJUnitRunner.class)
public class AAIInstanceGroupResourcesTest extends TestDataSetup{
	
	@InjectMocks
	private AAIInstanceGroupResources aaiInstanceGroupResources = new AAIInstanceGroupResources();
	
	private InstanceGroup instanceGroup;
	private GenericVnf vnf;
	
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;
    
    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;
    
    @Mock
    protected InjectionHelper MOCK_injectionHelper;
	
	@Before
	public void before() {
		instanceGroup = buildInstanceGroup();
		vnf = buildGenericVnf();
		 doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
	}
	
	@Test
	public void createInstanceGroupTest() throws Exception {
		doReturn(new org.onap.aai.domain.yang.InstanceGroup()).when(MOCK_aaiObjectMapper).mapInstanceGroup(instanceGroup);
		aaiInstanceGroupResources.createInstanceGroup(instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())), isA(Optional.class));
	}
	
	@Test
	public void deleteInstanceGroupTest() throws Exception {
		aaiInstanceGroupResources.deleteInstanceGroup(instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).delete(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())));
	}
	
	@Test
	public void connectInstanceGroupTest() throws Exception {
		aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())), eq(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId())));
	}
	
	@Test
	public void connectInstanceGroupWithEdgeTest() throws Exception {
		aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf, AAIEdgeLabel.BELONGS_TO);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())), eq(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId())), eq(AAIEdgeLabel.BELONGS_TO));
	}
	
	@Test
	public void existsTest() throws Exception {
		aaiInstanceGroupResources.exists(instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).exists(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())));
	}
	
}
