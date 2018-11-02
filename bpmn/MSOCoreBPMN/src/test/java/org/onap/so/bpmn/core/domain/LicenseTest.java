/*
* ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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
package org.onap.so.bpmn.core.domain;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito.Then;
import org.mockito.InjectMocks;
import org.mockito.Mock;

//@PrepareForTest({License.class})
public class LicenseTest {
	
	//@Mock
	private License license= new License();
	//@InjectMocks
	//private LicenseTest licenceTest;
	List<String> entitlementPoolList = new ArrayList<String>();
	private List<String> licenseKeyGroupList = new ArrayList<String>();
	//JSONArray array = new JSONArray(entitlementPoolList);
	//JSONArray array1 = new JSONArray(licenseKeyGroupList);
	//@PrepareForTest({License.class})
	Long serialVersionUID = 333L;
	
	@Test
	public void testLicense() {
		license.setEntitlementPoolList(entitlementPoolList);
		license.setLicenseKeyGroupList(licenseKeyGroupList);
		//license.addEntitlementPool("entitlementPoolUuid");
		license.addLicenseKeyGroup("licenseKeyGroupUuid");
		assertEquals(license.getEntitlementPoolList(), entitlementPoolList);
		assertEquals(license.getLicenseKeyGroupList(), licenseKeyGroupList);
		assert(license.getEntitlementPoolListAsString()!= null);
		assert(license.getLicenseKeyGroupListAsString()!=null);
		license.addEntitlementPool("entitlementPoolUuid");
		//assertEquals(license.getSerialversionuid(), serialVersionUID);
		//assertArrayEquals(license.getSerialversionuid(), serialVersionUID);
		//assert
	/*
		Mockito.when(License.getSerialversionuid()).thenReturn(getserial());
		assertEquals(License.getSerialversionuid(),"abc");*/
		
	}
	// @Before 
	// public void mocksetUp() {
//      Long serialVersionUID = 333L;
//	      expect (license.getSerialversionuid()).andReturn(serialVersionUID);
//	   }
	
	/*private Long getserial() {
		
		return abc;
	}*/

}
