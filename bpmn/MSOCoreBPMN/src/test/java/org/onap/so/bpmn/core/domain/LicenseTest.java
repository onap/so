/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.core.domain;


import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;



public class LicenseTest {

    private License license = new License();
    List<String> entitlementPoolList = new ArrayList<String>();
    private List<String> licenseKeyGroupList = new ArrayList<String>();

    Long serialVersionUID = 333L;

    @Test
    public void testLicense() {
        license.setEntitlementPoolList(entitlementPoolList);
        license.setLicenseKeyGroupList(licenseKeyGroupList);
        license.addLicenseKeyGroup("licenseKeyGroupUuid");
        assertEquals(license.getEntitlementPoolList(), entitlementPoolList);
        assertEquals(license.getLicenseKeyGroupList(), licenseKeyGroupList);
        assert (license.getEntitlementPoolListAsString() != null);
        assert (license.getLicenseKeyGroupListAsString() != null);
        license.addEntitlementPool("entitlementPoolUuid");


    }

}
