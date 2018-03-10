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

package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner

import com.github.tomakehurst.wiremock.junit.WireMockRule

@RunWith(MockitoJUnitRunner.class)
@Ignore // No Junits exists in this class to run
class UpdateVfModuleVolumeInfraV1Test {
	
	String xml = """
	<relationship-list>
		<relationship>
			<related-to>tenant</related-to>
			<related-link>https://aai-ext1.test.com:8443/aai/v7/cloud-infrastructure/cloud-regions/cloud-region/att-aic/mdt1/tenants/tenant/fba1bd1e195a404cacb9ce17a9b2b421/</related-link>
			<relationship-data>
				<relationship-key>tenant.tenant-id</relationship-key>
				<relationship-value>fba1bd1e195a404cacb9ce17a9b2b421</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>att-aic</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>mdt1</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>tenant.tenant-name</property-key>
				<property-value>ECOMP_MDT1</property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>vf-module</related-to>
			<related-link>https://aai-ext1.test.com:8443/aai/v7/cloud-infrastructure/cloud-regions/cloud-region/att-aic/mdt1/tenants/tenant/fba1bd1e195a404cacb9ce17a9b2b421/</related-link>
			<relationship-data>
				<relationship-key>vf-module.vf-module-ids</relationship-key>
				<relationship-value>fba1bd1e195a404cacb9ce17a9b2b421</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>att-aic</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>mdt1</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>vf-module.vf-module-name</property-key>
				<property-value>ECOMP_MDT1</property-value>
			</related-to-property>
		</relationship>		
	</relationship-list>
"""
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(28090)
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
		
	}
}
