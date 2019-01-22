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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIVnfResourcesTest extends TestDataSetup {

	private GenericVnf genericVnf;

	private ServiceInstance serviceInstance;
	
	private CloudRegion cloudRegion;
	
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;

	@Mock
	protected AAIObjectMapper MOCK_aaiObjectMapper;

	@Mock
	protected InjectionHelper MOCK_injectionHelper;
	
	@InjectMocks
	AAIVnfResources aaiVnfResources = new AAIVnfResources();

	@Before
	public void before() {
		serviceInstance = buildServiceInstance();
		genericVnf = buildGenericVnf();
		cloudRegion = buildCloudRegion();
		 doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
	}

	@Test
	public void createVnfandConnectServiceInstanceTest() {
		doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(MOCK_aaiObjectMapper).mapVnf(genericVnf);
		doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class), any(Optional.class));
		doNothing().when(MOCK_aaiResourcesClient).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
		genericVnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

		aaiVnfResources.createVnfandConnectServiceInstance(genericVnf, serviceInstance);

		assertEquals(OrchestrationStatus.INVENTORIED, genericVnf.getOrchestrationStatus());
		verify(MOCK_aaiObjectMapper, times(1)).mapVnf(genericVnf);
		verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
	}

	@Test
	public void createPlatformandConnectVnfTest() {
		Platform platform = new Platform();
		platform.setPlatformName("a123");
		doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class),isA(AAIResourceUri.class));
		doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class), any(Optional.class));
		aaiVnfResources.createPlatformandConnectVnf(platform, genericVnf);
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class),isA(AAIResourceUri.class));
	}
	
	@Test
	public void createLineOfBusinessandConnectVnfTest() {
		LineOfBusiness lob = new LineOfBusiness();
		lob.setLineOfBusinessName("a123");
		doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class),isA(AAIResourceUri.class));
		doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class), any(Optional.class));
		aaiVnfResources.createLineOfBusinessandConnectVnf(lob, genericVnf);
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class),isA(AAIResourceUri.class));
	}
	
	@Test
	public void deleteVnfTest() {
		doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));

		aaiVnfResources.deleteVnf(genericVnf);

		verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
	}

	@Test
	public void updateOrchestrationStatusVnfTest() {
		doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Vnf.class));

		aaiVnfResources.updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

		verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

		assertEquals(OrchestrationStatus.ACTIVE, genericVnf.getOrchestrationStatus());
	}

	@Test
	public void updateObjectVnfTest() {
		doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(MOCK_aaiObjectMapper).mapVnf(genericVnf);
		doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.GenericVnf.class));

		aaiVnfResources.updateObjectVnf(genericVnf);

		verify(MOCK_aaiObjectMapper, times(1)).mapVnf(genericVnf);
		verify(MOCK_aaiResourcesClient, times(1)).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.GenericVnf.class));
	}

	@Test
	public void getGenericVnfTest () {
		Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
		vnf.get().setVnfId("vnfId");
		doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),isA(AAIResourceUri.class));
		aaiVnfResources.getGenericVnf("vnfId");
		verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),isA(AAIResourceUri.class));
	}

	@Test
	public void checkInMaintFlagTest () {
		Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
		vnf.get().setVnfId("vnfId");
		vnf.get().setInMaint(true);
		doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),isA(AAIResourceUri.class));
		boolean inMaintFlag = aaiVnfResources.checkInMaintFlag("vnfId");
		verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),isA(AAIResourceUri.class));
		assertEquals(inMaintFlag, true);
	}
	
	@Test
	public void connectVnfToTenantTest() throws Exception {
		aaiVnfResources.connectVnfToTenant(genericVnf, cloudRegion);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.TENANT, 
				cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), cloudRegion.getTenantId())), 
				eq(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnf.getVnfId())));
	}
	
	@Test
	public void connectVnfToCloudRegionTest() throws Exception {
		aaiVnfResources.connectVnfToCloudRegion(genericVnf, cloudRegion);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnf.getVnfId())),
				eq(AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, 
						cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId())));
	}
}
