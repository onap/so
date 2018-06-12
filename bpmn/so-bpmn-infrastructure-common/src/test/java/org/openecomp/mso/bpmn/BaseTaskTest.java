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
package org.openecomp.mso.bpmn;

import org.junit.runner.RunWith;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.infrastructure.MSOInfrastructureApplication;
import org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks.AssignNetworkBBUtils;
import org.openecomp.mso.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.client.orchestration.AAICollectionResources;
import org.openecomp.mso.client.orchestration.AAIInstanceGroupResources;
import org.openecomp.mso.client.orchestration.AAINetworkResources;
import org.openecomp.mso.client.orchestration.AAIServiceInstanceResources;
import org.openecomp.mso.client.orchestration.AAIVfModuleResources;
import org.openecomp.mso.client.orchestration.AAIVnfResources;
import org.openecomp.mso.client.orchestration.AAIVolumeGroupResources;
import org.openecomp.mso.client.orchestration.AAIVpnBindingResources;
import org.openecomp.mso.client.orchestration.NetworkAdapterResources;
import org.openecomp.mso.client.orchestration.SDNCNetworkResources;
import org.openecomp.mso.client.orchestration.SDNCServiceInstanceResources;
import org.openecomp.mso.client.orchestration.SDNCVfModuleResources;
import org.openecomp.mso.client.orchestration.SDNCVnfResources;
import org.openecomp.mso.client.orchestration.VnfAdapterVfModuleResources;
import org.openecomp.mso.client.orchestration.VnfAdapterVolumeGroupResources;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSOInfrastructureApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTaskTest extends BuildingBlockTestDataSetup {
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
}
