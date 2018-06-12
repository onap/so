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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
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
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
@RunWith(MockitoJUnitRunner.class)
public class AAICollectionResourcesTest extends BuildingBlockTestDataSetup{
	
	@InjectMocks
	private AAICollectionResources aaiCollectionResources = new AAICollectionResources();
	
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;
    
    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;
    
    @Mock
    protected InjectionHelper MOCK_injectionHelper;
	
	private Collection networkCollection;
	
	@Before
	public void before() {
		networkCollection = buildCollection();
		doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
	}
	
	@Test
	public void createCollectionTest() throws Exception {
		networkCollection.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		doReturn(new org.onap.aai.domain.yang.Collection()).when(MOCK_aaiObjectMapper).mapCollection(networkCollection);
		
		aaiCollectionResources.createCollection(networkCollection);
		
		assertEquals(OrchestrationStatus.INVENTORIED, networkCollection.getOrchestrationStatus());
		verify(MOCK_aaiResourcesClient, times(1)).create(eq(AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId())), isA(org.onap.aai.domain.yang.Collection.class));
	}
	
	@Test
	public void updateCollectionTest() throws Exception {
		doReturn(new org.onap.aai.domain.yang.Collection()).when(MOCK_aaiObjectMapper).mapCollection(networkCollection);
		aaiCollectionResources.updateCollection(networkCollection);
		verify(MOCK_aaiResourcesClient, times(1)).update(eq(AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId())), isA(org.onap.aai.domain.yang.Collection.class));
	}
	
	@Test
	public void deleteCollectionTest() throws Exception {
		aaiCollectionResources.deleteCollection(networkCollection);
		verify(MOCK_aaiResourcesClient, times(1)).delete(eq(AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId())));
	}
	
}
