/*- 
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
 * ================================================================================ 
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved. 
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

 
package org.openecomp.mso.bpmn.infrastructure;

public class StubResponseAAITest extends WorkflowTest{

	@Test
	public void testStubResponseAAIforNullAndDefaultInputs()
	{
		try{
			StubResponseAAI.MockPutTunnelXConnect(null, null, null, null, null);
			StubResponseAAI.MockGetAllottedResource(null, null, null, null, null);
			StubResponseAAI.MockPutAllottedResource(null, null, null, null);
			StubResponseAAI.MockPutAllottedResource_500(null, null, null, null);
			StubResponseAAI.MockDeleteAllottedResource(null, null, null, null, null);
			StubResponseAAI.MockPatchAllottedResource(null, null, null, null);
			StubResponseAAI.MockQueryAllottedResourceById(null, null);
			StubResponseAAI.MockGetServiceInstance(null, null, null, null);
			StubResponseAAI.MockGetServiceInstance_404(null, null, null);
			StubResponseAAI.MockGetServiceInstance_500(null, null, null);
			StubResponseAAI.MockGetServiceInstance_500(null, null, null, null);
			StubResponseAAI.MockNodeQueryServiceInstanceByName(null, null);
			StubResponseAAI.MockNodeQueryServiceInstanceByName_404(null);
			StubResponseAAI.MockNodeQueryServiceInstanceByName_500(null);
			StubResponseAAI.MockNodeQueryServiceInstanceById(null, null);
			StubResponseAAI.MockNodeQueryServiceInstanceById_404(null);
			StubResponseAAI.MockNodeQueryServiceInstanceById_500(null);
			StubResponseAAI.MockDeleteServiceInstance(null, null, null, null);
			StubResponseAAI.MockGetServiceInstance(null, null, null, null, 0);
			StubResponseAAI.MockGetServiceInstance(null, null, null, 0);
			StubResponseAAI.MockDeleteServiceInstance(null, null, null, null, 0);
			StubResponseAAI.MockDeleteServiceInstance(null, null, null, 0);
			StubResponseAAI.MockDeleteServiceInstance_404(null, null, null, null);
			StubResponseAAI.MockDeleteServiceInstance_500(null, null, null, null);
			StubResponseAAI.MockPutServiceInstance(null, null, null, null);
			StubResponseAAI.MockPutServiceInstance_500(null, null, null);
			StubResponseAAI.MockGetServiceSubscription(null, null, null);
			StubResponseAAI.MockDeleteServiceSubscription(null, null, 0);
			StubResponseAAI.MockDeleteServiceInstanceId(null, null, null);
			StubResponseAAI.MockPutServiceSubscription(null, null);
			StubResponseAAI.MockGetServiceSubscription(null, null, 0);
			StubResponseAAI.MockGetCustomer(null, null);
			StubResponseAAI.MockDeleteCustomer(null);
			StubResponseAAI.MockPutCustomer(null);
			StubResponseAAI.MockPutCustomer_500(null);
			StubResponseAAI.MockGetGenericVnfById(null, null);
			StubResponseAAI.MockGetGenericVnfById(null, null, 0);
			StubResponseAAI.MockGetGenericVnfByIdWithPriority(null, 0, null);
			StubResponseAAI.MockGetGenericVnfByIdWithPriority(null, null, 0, null, 0);
			StubResponseAAI.MockGetGenericVnfByIdWithDepth(null, 0, null);
			StubResponseAAI.MockGetGenericVnfById_404(null);
			StubResponseAAI.MockGetGenericVnfById_500(null);
			StubResponseAAI.MockGetGenericVnfByName(null, null);
			StubResponseAAI.MockGetGenericVnfByNameWithDepth(null, 0, null);
			StubResponseAAI.MockGetGenericVnfByName_404(null);
			StubResponseAAI.MockDeleteGenericVnf(null, null);
			StubResponseAAI.MockDeleteGenericVnf(null, null, 0);
			StubResponseAAI.MockDeleteGenericVnf_500(null, null);
			StubResponseAAI.MockPutGenericVnf(null);
			StubResponseAAI.MockPutGenericVnf(null, null, 0);
			StubResponseAAI.MockPutGenericVnf(null, 0);
			StubResponseAAI.MockPutGenericVnf_Bad(null, 0);
			StubResponseAAI.MockPatchGenericVnf(null);
			StubResponseAAI.MockGetVceById(null, null);
			StubResponseAAI.MockGetVceByName(null, null);
			StubResponseAAI.MockDeleteVce(null, null, 0);
			StubResponseAAI.MockPutVce(null);
			StubResponseAAI.MockGetGenericVceByNameWithDepth(null, 0, null);
			StubResponseAAI.MockGetVceGenericQuery(null, 0, 0, null);
			StubResponseAAI.MockGetTenantGenericQuery(null, null, null);
			StubResponseAAI.MockGetTenant(null, null);
			StubResponseAAI.MockGetNetwork(null, null, 0);
			StubResponseAAI.MockGetNetworkByIdWithDepth(null, null, null);
			StubResponseAAI.MockGetNetworkCloudRegion(null, null);
			StubResponseAAI.MockGetNetworkByName(null, null);
			StubResponseAAI.MockGetNetworkByName_404(null, null);
			StubResponseAAI.MockGetNetworkCloudRegion_404(null);
			StubResponseAAI.MockPutNetwork(null, 0, null);
			StubResponseAAI.MockPutNetwork(null, null, 0);
			StubResponseAAI.MockGetNetworkName(null, null, 0);
			StubResponseAAI.MockGetNetworkVpnBinding(null, null);
			StubResponseAAI.MockGetNetworkPolicy(null, null);
			StubResponseAAI.MockGetNetworkVpnBinding(null, null, 0);
			StubResponseAAI.MockGetNetworkPolicy(null, null, 0);
			StubResponseAAI.MockGetNetworkTableReference(null, null);
			StubResponseAAI.MockPutNetworkIdWithDepth(null, null, null);
			StubResponseAAI.MockGetNetworkPolicyfqdn(null, null, 0);
			StubResponseAAI.MockGetNetworkRouteTable(null, null, 0);
			StubResponseAAI.MockPatchVfModuleId(null, null);
			StubResponseAAI.MockVNFAdapterRestVfModule();
			StubResponseAAI.MockDBUpdateVfModule();
			StubResponseAAI.MockSDNCAdapterVfModule();
			StubResponseAAI.MockAAIVfModule();
			StubResponseAAI.MockGetCloudRegion(null, 0, null);
			StubResponseAAI.MockGetVolumeGroupById(null, null, null);
			StubResponseAAI.MockPutVolumeGroupById(null, null, null, 0);
			StubResponseAAI.MockGetVolumeGroupByName(null, null, null, 0);
			StubResponseAAI.MockDeleteVolumeGroupById(null, null, null, 0);
			StubResponseAAI.MockGetVolumeGroupByName_404(null, null);
			StubResponseAAI.MockDeleteVolumeGroup(null, null, null);
			StubResponseAAI.MockGetVfModuleId(null, null, null, 0);
			StubResponseAAI.MockGetVfModuleByNameWithDepth(null, null, 0, null, 0);
			StubResponseAAI.MockGetVfModuleIdNoResponse(null, null, null);
			StubResponseAAI.MockPutVfModuleIdNoResponse(null, null, null);
			StubResponseAAI.MockPutVfModuleId(null, null);
			StubResponseAAI.MockPutVfModuleId(null, null, 0);
			StubResponseAAI.MockDeleteVfModuleId(null, null, null, 0);
			StubResponseAAI.MockAAIVfModuleBadPatch(null, 0);
			StubResponseAAI.MockGetPserverByVnfId(null, null, 0);
			StubResponseAAI.MockGetGenericVnfsByVnfId(null, null, 0);
			StubResponseAAI.MockSetInMaintFlagByVnfId(null, 0);
			StubResponseAAI.MockGetVceById();
			StubResponseAAI.MockGetVceByName();
			StubResponseAAI.MockPutVce();
			StubResponseAAI.MockDeleteVce();
			StubResponseAAI.MockDeleteVce_404();
			StubResponseAAI.MockDeleteServiceSubscription();
			StubResponseAAI.MockGetServiceSubscription();
			StubResponseAAI.MockGetServiceSubscription_200Empty();
			StubResponseAAI.MockGetServiceSubscription_404();
			StubResponseAAI.MockGENPSIPutServiceInstance();
			StubResponseAAI.MockGENPSIPutServiceSubscription();
			StubResponseAAI.MockGENPSIPutServiceInstance_get500();
			StubResponseAAI.MockGetGenericVnfById();
			StubResponseAAI.MockGetGenericVnfById_404();
			StubResponseAAI.MockGetGenericVnfByName();
			StubResponseAAI.MockGetGenericVnfByName_hasRelationships();
			StubResponseAAI.MockGetGenericVnfById_hasRelationships();
			StubResponseAAI.MockGetGenericVnfById_500();
			StubResponseAAI.MockGetGenericVnfByName_404();
			StubResponseAAI.MockPutGenericVnf();
			StubResponseAAI.MockPutGenericVnf_400();
			StubResponseAAI.MockDeleteGenericVnf();
			StubResponseAAI.MockDeleteGenericVnf_404();
			StubResponseAAI.MockDeleteGenericVnf_500();
			StubResponseAAI.MockDeleteGenericVnf_412();
		}
		catch(Exception ex)
		{
			
			System.err.println(ex);
		}
		
	}
}
