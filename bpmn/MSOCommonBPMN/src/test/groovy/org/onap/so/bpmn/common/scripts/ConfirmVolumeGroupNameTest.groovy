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

package org.onap.so.bpmn.common.scripts

import static org.mockito.Mockito.*

import javax.ws.rs.core.UriBuilder

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory

class ConfirmVolumeGroupNameTest extends MsoGroovyTest {

	@Captor
	ArgumentCaptor<ExecutionEntity> captor=  ArgumentCaptor.forClass(ExecutionEntity.class);

	@Spy
	ConfirmVolumeGroupName confirmVolumeGroupName;

	@Before
	public void init() throws IOException {
		super.init("ConfirmVolumeGroupName")
		MockitoAnnotations.initMocks(this);
		when(confirmVolumeGroupName.getAAIClient()).thenReturn(client)

	}

	@Test
	public void testQueryAAIForVolumeGroupId() {

		AAIResourceUri resourceUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.VOLUME_GROUP, UriBuilder.fromPath('/aai/test/volume-groups/volume-group/testVolumeGroup').build());
		when(mockExecution.getVariable("CVGN_volumeGroupGetEndpoint")).thenReturn(resourceUri)
		VolumeGroup volumeGroup = new VolumeGroup()
		volumeGroup.setVolumeGroupId("Test")
		when(client.get(VolumeGroup.class,resourceUri)).thenReturn(Optional.of(volumeGroup))
		confirmVolumeGroupName.queryAAIForVolumeGroupId(mockExecution)
        Mockito.verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponseCode",200)
        Mockito.verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponse",volumeGroup)
	}

	@Test
	public void testQueryAAIForVolumeGroupId_404() {
		AAIResourceUri resourceUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.VOLUME_GROUP, UriBuilder.fromPath('/aai/test/volume-groups/volume-group/testVolumeGroup').build());
		when(client.get(VolumeGroup.class,  resourceUri)).thenReturn(Optional.empty())
		DelegateExecution execution = new DelegateExecutionFake()
		try {
			execution.setVariable("CVGN_volumeGroupGetEndpoint", resourceUri)
			confirmVolumeGroupName.queryAAIForVolumeGroupId(execution)
		}
		catch(Exception ex){}
		Assert.assertEquals(404, execution.getVariable("CVGN_queryVolumeGroupResponseCode"))
		Assert.assertEquals("Volume Group not Found!", execution.getVariable("CVGN_queryVolumeGroupResponse"))
		
	}
}
