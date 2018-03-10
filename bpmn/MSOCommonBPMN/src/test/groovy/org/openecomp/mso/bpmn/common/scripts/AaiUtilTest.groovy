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

package org.openecomp.mso.bpmn.common.scripts

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.openecomp.mso.rest.HttpHeader
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.junit.Before
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Test
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition

@RunWith(MockitoJUnitRunner.class)
class AaiUtilTest extends MsoGroovyTest {
	
	@Test
	public void testGetVersionDefault() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('8', version)
	}
	
	@Test
	public void testGetVersionResourceSpecific() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_l3_network_version")).thenReturn('7')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('7', version)
	}

	@Test
	public void testGetVersionFlowSpecific() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_custom_CreateAAIVfModule_aai_version")).thenReturn('6')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_l3_network_version")).thenReturn('7')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('6', version)
	}

	@Test(expected=java.lang.Exception.class)
	public void testGetVersionNotDefined() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
	}
	
	@Test
	public void testGetUriDefaultVersion() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn('/aai/v8/network/l3-networks/l3-network')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getUri(mockExecution, 'l3-network')
		assertEquals('/aai/v8/network/l3-networks/l3-network', uri)
	}
	
	@Test
	public void testGetUriFlowAndResourceSpecific() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_CreateAAIVfModule_aai_l3_network_uri")).thenReturn('/aai/v6/network/l3-networks/l3-network')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn('/aai/v8/network/l3-networks/l3-network')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getUri(mockExecution, 'l3-network')
		assertEquals('/aai/v6/network/l3-networks/l3-network', uri)
	}
	
	@Test
	public void testGetNetworkGenericVnfEndpoint() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_generic_vnf_uri")).thenReturn('/aai/v8/network/generic-vnfs/generic-vnf')
		when(mockExecution.getVariable('URN_aai_endpoint')).thenReturn('http://localhost:28090')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def endpoint = aaiUtil.getNetworkGenericVnfEndpoint(mockExecution)
		assertEquals('http://localhost:28090/aai/v8/network/generic-vnfs/generic-vnf', endpoint)
	}
	
	@Test
	public void testGetNetworkGenericVnfUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_generic_vnf_uri")).thenReturn('/aai/v8/network/generic-vnfs/generic-vnf')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkGenericVnfUri(mockExecution)
		assertEquals('/aai/v8/network/generic-vnfs/generic-vnf', uri)
	}
	
	@Test
	public void testGetNetworkVpnBindingUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn('/aai/v8/network/vpn-bindings/vpn-binding')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkVpnBindingUri(mockExecution)
		assertEquals('/aai/v8/network/vpn-bindings/vpn-binding', uri)
	}

	@Test
	public void testGetNetworkPolicyUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_network_policy_uri")).thenReturn('/aai/v8/network/network-policies/network-policy')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkPolicyUri(mockExecution)
		assertEquals('/aai/v8/network/network-policies/network-policy', uri)
	}
	
	@Test
	public void testGetNetworkTableReferencesUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_route_table_reference_uri")).thenReturn('/aai/v8/network/route-table-references/route-table-reference')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkTableReferencesUri(mockExecution)
		assertEquals('/aai/v8/network/route-table-references/route-table-reference', uri)
	}
	
	@Test
	public void testGetNetworkVceUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vce_uri")).thenReturn('/aai/v8/network/vces/vce')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkVceUri(mockExecution)
		assertEquals('/aai/v8/network/vces/vce', uri)
	}
	
	@Test
	public void testGetNetworkL3NetworkUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn('/aai/v8/network/l3-networks/l3-network')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkL3NetworkUri(mockExecution)
		assertEquals('/aai/v8/network/l3-networks/l3-network', uri)
	}
	
	@Test
	public void testGetBusinessCustomerUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_customer_uri")).thenReturn('/aai/v8/business/customers/customer')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getBusinessCustomerUri(mockExecution)
		assertEquals('/aai/v8/business/customers/customer', uri)
	}
	
	@Test
	public void testGetCloudInfrastructureCloudRegionEndpoint() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn('/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic')
		when(mockExecution.getVariable('URN_aai_endpoint')).thenReturn('http://localhost:28090')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(mockExecution)
		assertEquals('http://localhost:28090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic', uri)
	}
	
	@Test
	public void testGetCloudInfrastructureCloudRegionUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn('/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getCloudInfrastructureCloudRegionUri(mockExecution)
		assertEquals('/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic', uri)
	}
	
	@Test
	public void testGetCloudInfrastructureTenantUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_tenant_uri")).thenReturn('/aai/v8/cloud-infrastructure/tenants/tenant')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getCloudInfrastructureTenantUri(mockExecution)
		assertEquals('/aai/v8/cloud-infrastructure/tenants/tenant', uri)
	}
	
	@Test
	public void testGetSearchNodesQueryUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_nodes_query_uri")).thenReturn('/aai/v8/search/nodes-query')
		when(mockExecution.getVariable('URN_aai_endpoint')).thenReturn('http://localhost:28090')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')

		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getSearchNodesQueryUri(mockExecution)
		assertEquals('/aai/v8/search/nodes-query', uri)
	}
	
	@Test
	public void testGetSearchNodesQueryEndpoint() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_nodes_query_uri")).thenReturn('/aai/v8/search/nodes-query')
		when(mockExecution.getVariable('URN_aai_endpoint')).thenReturn('http://localhost:28090')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getSearchNodesQueryEndpoint(mockExecution)
		assertEquals('http://localhost:28090/aai/v8/search/nodes-query', uri)
	}
	
	@Test
	public void testGetSearchGenericQueryUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('8')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_generic_query_uri")).thenReturn('/aai/v8/search/generic-query')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getSearchGenericQueryUri(mockExecution)
		assertEquals('/aai/v8/search/generic-query', uri)
	}
	
	@Test
	public void testGetNamespaceFromUri() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('6')
		when(mockExecution.getVariable("URN_mso_workflow_default_aai_v6_l3_network_uri")).thenReturn('/aai/v6/network/l3-networks/l3-network')
		when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.getNetworkL3NetworkUri(mockExecution)  // Required to populate the namespace in the class
		def ns = aaiUtil.getNamespaceFromUri('/aai/v6/search/generic-query')
		assertEquals('http://org.openecomp.aai.inventory/v6', ns)
	}
	
	@Test
	public void testGetNamespaceFromUri2() {
		   ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('10')
		   when(mockExecution.getVariable("URN_mso_workflow_default_aai_v10_l3_network_uri")).thenReturn('/aai/v10/network/l3-networks/l3-network')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   CreateAAIVfModule myproc = new CreateAAIVfModule()
		   AaiUtil aaiUtil = new AaiUtil(myproc)
		   def uri = aaiUtil.getNetworkL3NetworkUri(mockExecution)  // Required to populate the namespace in the class
		   def ns = aaiUtil.getNamespaceFromUri('/aai/v10/search/generic-query')
		   assertEquals('http://org.openecomp.aai.inventory/v10', ns)
	}
	
	@Test
	public void testGetNamespaceFromUri3() {
		   ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('100')
		   when(mockExecution.getVariable("URN_mso_workflow_default_aai_v100_l3_network_uri")).thenReturn('/aai/v100/network/l3-networks/l3-network')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   CreateAAIVfModule myproc = new CreateAAIVfModule()
		   AaiUtil aaiUtil = new AaiUtil(myproc)
		   def uri = aaiUtil.getNetworkL3NetworkUri(mockExecution)  // Required to populate the namespace in the class
		   def ns = aaiUtil.getNamespaceFromUri('/aai/v100/search/generic-query')
		   assertEquals('http://org.openecomp.aai.inventory/v100', ns)
	}

	@Test
	public void testGetNamespaceFromUri_twoArguments() {  // (execution, uri)
		   ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn('10')
		   when(mockExecution.getVariable("URN_mso_workflow_default_aai_v10_l3_network_uri")).thenReturn('/aai/v10/network/l3-networks/l3-network')
		   when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		   //
		   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		   CreateAAIVfModule myproc = new CreateAAIVfModule()
		   AaiUtil aaiUtil = new AaiUtil(myproc)
		   def ns = aaiUtil.getNamespaceFromUri(mockExecution,'/aai/v10/search/generic-query')
		   assertEquals('http://org.openecomp.aai.inventory/v10', ns)
	}
}
