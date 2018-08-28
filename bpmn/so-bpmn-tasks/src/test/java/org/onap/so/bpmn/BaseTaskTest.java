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
package org.onap.so.bpmn;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.so.TestApplication;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.AssignNetworkBBUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.db.request.RequestsDbClient;
import org.onap.so.client.orchestration.AAICollectionResources;
import org.onap.so.client.orchestration.AAIInstanceGroupResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.onap.so.client.orchestration.AAIVpnBindingResources;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.onap.so.client.orchestration.SDNCNetworkResources;
import org.onap.so.client.orchestration.SDNCServiceInstanceResources;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.so.client.orchestration.VnfAdapterVfModuleResources;
import org.onap.so.client.orchestration.VnfAdapterVolumeGroupResources;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTaskTest extends TestDataSetup {
	@MockBean
	protected AAIVolumeGroupResources aaiVolumeGroupResources;
	
	@MockBean
	protected AAIServiceInstanceResources aaiServiceInstanceResources;
	
	@MockBean
	protected AAIVnfResources aaiVnfResources;
	
	@MockBean
	protected AAIVfModuleResources aaiVfModuleResources;
	
	@MockBean
	protected AAIVpnBindingResources aaiVpnBindingResources;
	
	@MockBean
	protected AAINetworkResources aaiNetworkResources;
	
	@MockBean
	protected AAICollectionResources aaiCollectionResources;
	
	@MockBean
	protected NetworkAdapterResources networkAdapterResources;
	
	@MockBean
	protected VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;
	
	@MockBean
	protected VnfAdapterVfModuleResources vnfAdapterVfModuleResources;
	
	@MockBean
	protected SDNCVnfResources sdncVnfResources;
	
	@MockBean
	protected SDNCNetworkResources sdncNetworkResources;
	
	@MockBean
	protected SDNCVfModuleResources sdncVfModuleResources;
	
	@MockBean
	protected SDNCServiceInstanceResources sdncServiceInstanceResources;
	
	@MockBean
	protected AssignNetworkBBUtils assignNetworkBBUtils;
	
	@MockBean
	protected NetworkAdapterObjectMapper networkAdapterObjectMapper;
	
	@MockBean
	protected AAIInstanceGroupResources aaiInstanceGroupResources;
	
	@MockBean
	protected CatalogDbClient catalogDbClient;
	
	@MockBean
	protected RequestsDbClient requestsDbClient;
	
	@Mock
	protected BBInputSetupUtils bbSetupUtils;
	
	@Mock
	protected BBInputSetup bbInputSetup;

	@SpyBean
	protected SDNCClient SPY_sdncClient;
	
	@MockBean
	protected ApplicationControllerAction appCClient;
}
