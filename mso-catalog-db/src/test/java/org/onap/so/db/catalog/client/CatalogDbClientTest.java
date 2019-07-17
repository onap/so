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
package org.onap.so.db.catalog.client;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import javax.persistence.EntityNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;

public class CatalogDbClientTest {

    private CatalogDbClient catalogDbClient;

    @Before
    public void init() {
        catalogDbClient = new CatalogDbClient();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public final void testFindVnfResourceCustomizationInListNullInList() {
        thrown.expect(EntityNotFoundException.class);
        String vnfCustomizationUUID = "a123";
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        vrc.setModelCustomizationUUID("z789J");
        VnfResourceCustomization vrc2 = new VnfResourceCustomization();
        vrc2.setModelCustomizationUUID(null);
        ArrayList<VnfResourceCustomization> vrcs = new ArrayList<VnfResourceCustomization>();
        vrcs.add(vrc);
        vrcs.add(vrc2);
        catalogDbClient.findVnfResourceCustomizationInList(vnfCustomizationUUID, vrcs);
    }

    @Test
    public final void testFindVnfResourceCustomizationInListNullString() {
        thrown.expect(EntityNotFoundException.class);
        thrown.expectMessage("a NULL UUID was provided in query to search for VnfResourceCustomization");
        String vnfCustomizationUUID = null;
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        vrc.setModelCustomizationUUID("z789J");
        VnfResourceCustomization vrc2 = new VnfResourceCustomization();
        vrc2.setModelCustomizationUUID("a123");
        ArrayList<VnfResourceCustomization> vrcs = new ArrayList<VnfResourceCustomization>();
        vrcs.add(vrc);
        vrcs.add(vrc2);
        catalogDbClient.findVnfResourceCustomizationInList(vnfCustomizationUUID, vrcs);
    }

    @Test
    public final void testFindVnfResourceCustomizationInListNoNulls() {
        String vnfCustomizationUUID = "a123";
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        vrc.setModelCustomizationUUID("z789J");
        VnfResourceCustomization vrc2 = new VnfResourceCustomization();
        vrc2.setModelCustomizationUUID("a123");
        ArrayList<VnfResourceCustomization> vrcs = new ArrayList<VnfResourceCustomization>();
        vrcs.add(vrc);
        vrcs.add(vrc2);
        VnfResourceCustomization aVrc = catalogDbClient.findVnfResourceCustomizationInList(vnfCustomizationUUID, vrcs);
        assertTrue(aVrc.getModelCustomizationUUID().equals("a123"));
    }

    @Test
    public final void testFindVfModuleCustomizationInListNullInList() {
        thrown.expect(EntityNotFoundException.class);
        String vfModuleCustomizationUUID = "a123";
        VfModuleCustomization vmc = new VfModuleCustomization();
        vmc.setModelCustomizationUUID("z789J");
        VfModuleCustomization vmc2 = new VfModuleCustomization();
        vmc2.setModelCustomizationUUID(null);
        ArrayList<VfModuleCustomization> vmcs = new ArrayList<VfModuleCustomization>();
        vmcs.add(vmc);
        vmcs.add(vmc2);
        catalogDbClient.findVfModuleCustomizationInList(vfModuleCustomizationUUID, vmcs);
    }

    @Test
    public final void testFindVfModuleCustomizationInListNullString() {
        thrown.expect(EntityNotFoundException.class);
        thrown.expectMessage("a NULL UUID was provided in query to search for VfModuleCustomization");
        String vfModuleCustomizationUUID = null;
        VfModuleCustomization vmc = new VfModuleCustomization();
        vmc.setModelCustomizationUUID("z789J");
        VfModuleCustomization vmc2 = new VfModuleCustomization();
        vmc2.setModelCustomizationUUID("a123");
        ArrayList<VfModuleCustomization> vmcs = new ArrayList<VfModuleCustomization>();
        vmcs.add(vmc);
        vmcs.add(vmc2);
        catalogDbClient.findVfModuleCustomizationInList(vfModuleCustomizationUUID, vmcs);
    }

    @Test
    public final void testFindVfModuleCustomizationInListNoNulls() {
        String vfModuleCustomizationUUID = "a123";
        VfModuleCustomization vmc = new VfModuleCustomization();
        vmc.setModelCustomizationUUID("z789J");
        VfModuleCustomization vmc2 = new VfModuleCustomization();
        vmc2.setModelCustomizationUUID("a123");
        ArrayList<VfModuleCustomization> vmcs = new ArrayList<VfModuleCustomization>();
        vmcs.add(vmc);
        vmcs.add(vmc2);
        VfModuleCustomization aVmc = catalogDbClient.findVfModuleCustomizationInList(vfModuleCustomizationUUID, vmcs);
        assertTrue(aVmc.getModelCustomizationUUID().equals("a123"));
    }

    @Test
    public final void testFindCvnfcCustomizationInListNullInList() {
        thrown.expect(EntityNotFoundException.class);
        String cvnfcCustomizationUuid = "a123";
        CvnfcCustomization cvnfc = new CvnfcCustomization();
        cvnfc.setModelCustomizationUUID("z789J");
        CvnfcCustomization cvnfc2 = new CvnfcCustomization();
        cvnfc2.setModelCustomizationUUID(null);
        ArrayList<CvnfcCustomization> cvnfcs = new ArrayList<CvnfcCustomization>();
        cvnfcs.add(cvnfc);
        cvnfcs.add(cvnfc2);
        catalogDbClient.findCvnfcCustomizationInAList(cvnfcCustomizationUuid, cvnfcs);
    }

    @Test
    public final void testFindCvnfcCustomizationInListNullString() {
        thrown.expect(EntityNotFoundException.class);
        thrown.expectMessage("a NULL UUID was provided in query to search for CvnfcCustomization");
        String cvnfcCustomizationUuid = null;
        CvnfcCustomization cvnfc = new CvnfcCustomization();
        cvnfc.setModelCustomizationUUID("z789J");
        CvnfcCustomization cvnfc2 = new CvnfcCustomization();
        cvnfc2.setModelCustomizationUUID("a123");
        ArrayList<CvnfcCustomization> cvnfcs = new ArrayList<CvnfcCustomization>();
        cvnfcs.add(cvnfc);
        cvnfcs.add(cvnfc2);
        catalogDbClient.findCvnfcCustomizationInAList(cvnfcCustomizationUuid, cvnfcs);
    }

    @Test
    public final void testFindCvnfcCustomizationInListNoNulls() {
        String cvnfcCustomizationUuid = "a123";
        CvnfcCustomization cvnfc = new CvnfcCustomization();
        cvnfc.setModelCustomizationUUID("z789J");
        CvnfcCustomization cvnfc2 = new CvnfcCustomization();
        cvnfc2.setModelCustomizationUUID("a123");
        ArrayList<CvnfcCustomization> cvnfcs = new ArrayList<CvnfcCustomization>();
        cvnfcs.add(cvnfc);
        cvnfcs.add(cvnfc2);
        CvnfcCustomization aCvnfc = catalogDbClient.findCvnfcCustomizationInAList(cvnfcCustomizationUuid, cvnfcs);
        assertTrue(aCvnfc.getModelCustomizationUUID().equals("a123"));
    }

}
