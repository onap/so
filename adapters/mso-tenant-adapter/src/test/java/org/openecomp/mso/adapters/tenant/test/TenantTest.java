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

package org.openecomp.mso.adapters.tenant.test;



import java.util.Map;

import javax.xml.ws.Holder;

import org.openecomp.mso.adapters.tenant.MsoTenantAdapter;
import org.openecomp.mso.adapters.tenant.MsoTenantAdapterImpl;
import org.openecomp.mso.adapters.tenant.exceptions.TenantException;
import org.openecomp.mso.adapters.tenantrest.TenantRollback;

public class TenantTest {
	public static final void main (String args[])
	{
		String cloudId = "MT";
		 cloudId = "AIC_GAMMALAB";
		
		MsoTenantAdapter tenantAdapter = new MsoTenantAdapterImpl();
		
		Holder<String> tenantId = new Holder<>();
		Holder<String> tenantName = new Holder<>();
		Holder<Map<String,String>> tenantMetadata = new Holder<>();
		Holder<Boolean> tenantDeleted = new Holder<>();
		Holder<TenantRollback> rollback = new Holder<>();
		
		try {
			tenantAdapter.queryTenant (cloudId, "934a4ac9c4bd4b8d9d8ab3ef900281b0", null, tenantId, tenantName, tenantMetadata);
			System.out.println ("Got Tenant ID=" + tenantId.value + ", name=" + tenantName.value + ", metadata = " + tenantMetadata.value);
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception: " + e);
			System.exit(1);
		}
/*		
		Map<String,String> metadata = new HashMap<String,String>();
		metadata.put("sdn-global-id", "abc");
		metadata.put("service-type", "gamma");
		
		// Create a new tenant
		try {
			tenantAdapter.createTenant(cloudId, "TEST_META6", metadata, true, tenantId, rollback);
			System.out.println ("Created Tenant ID " + tenantId.value);
		}
		catch (TenantAlreadyExists e) {
			System.out.println ("Create: Tenant already exists: " + "TEST_META6");
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Create: " + e);
			System.exit(1);
		}
		
		// Query the new tenant
		try {
			tenantAdapter.queryTenant (cloudId, "TEST_META6", tenantId, tenantName, tenantMetadata);
			System.out.println ("Queried Tenant ID=" + tenantId.value + ", name=" + tenantName.value + ", metadata = " + tenantMetadata.value);
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Query: " + e);
			System.exit(1);
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {}
		
		// Delete the new tenant
		try {
			tenantAdapter.deleteTenant (cloudId, tenantId.value, tenantDeleted);
			if (tenantDeleted.value)
				System.out.println ("Deleted Tenant " + tenantId.value);
			else
				System.out.println ("Delete: Tenant " + tenantId.value + " does not exist");
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Delete: " + e);
		}
/*
		// Create another new tenant
		try {
			tenantAdapter.createTenant(cloudId, "TEST_MSO2", null, false, tenantId, rollback);
			System.out.println ("Created Tenant ID " + tenantId.value);
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Create: " + e);
		}
		
		// Query the new tenant
		try {
			tenantAdapter.queryTenant (cloudId, "TEST_MSO2", tenantId, tenantName);
			System.out.println ("Queried Tenant ID=" + tenantId.value + ", name=" + tenantName.value);
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Query: " + e);
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {}

		// Rollback the new tenant
		try {
			tenantAdapter.rollbackTenant(rollback.value);
			System.out.println ("Rolled Back Tenant ID " + tenantId.value);
		}
		catch (TenantException e) {
			System.out.println ("Got Tenant Exception on Rollback: " + e);
		}
*/
	}
}
