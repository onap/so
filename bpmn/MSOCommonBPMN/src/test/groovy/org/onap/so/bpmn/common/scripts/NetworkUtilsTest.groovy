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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import static org.junit.Assert.*;
import org.junit.Test;

class NetworkUtilsTest {

	String volumeRequestXml = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id/>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure></backout-on-failure>
   </volume-inputs>
   <volume-params>
      <param name="vnf_name">STMTN5MMSC20</param>
      <param name="vnf_name2">US1117MTSNJVBR0246</param>
    </volume-params>
</volume-request>"""


	@Test
	public void testIsRollbackEnabled() {
		
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.rollback")).thenReturn(true)

		NetworkUtils networkUtils = new NetworkUtils()
		def rollbackEnabled = networkUtils.isRollbackEnabled(mockExecution, volumeRequestXml)
		
		assertEquals(true, rollbackEnabled)

	}
	
	@Test
	public void testIsRollbackEnabled2() {
		
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.rollback")).thenReturn(false)

		NetworkUtils networkUtils = new NetworkUtils()
		def rollbackEnabled = networkUtils.isRollbackEnabled(mockExecution, volumeRequestXml)
		
		assertEquals(false, rollbackEnabled)

	}

	@Test
	public void testGetIpvVersion() {
		
		NetworkUtils networkUtils = new NetworkUtils()
		println "test: ipv4"
		String version4 = networkUtils.getIpvVersion("ipv4")
		assertEquals("4", version4)
		println "test: ipv6"
		String version6 = networkUtils.getIpvVersion("ipv6")
		assertEquals("6", version6)
		println "test: 4"
		String versionDigit4 = networkUtils.getIpvVersion("4")
		assertEquals("4", versionDigit4)

	}
	
}
