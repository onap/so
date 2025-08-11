/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.catalog;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseTest {

    protected VnfcCustomization setUpVnfcCustomization() {
        VnfcCustomization vnfcCustomization = new VnfcCustomization();
        vnfcCustomization.setModelInstanceName("testVnfcCustomizationModelInstanceName");
        vnfcCustomization.setModelUUID("321228a4-9f15-11e8-98d0-529269fb1459");
        vnfcCustomization.setModelInvariantUUID("c0659136-9f15-11e8-98d0-529269fb1459");
        vnfcCustomization.setModelVersion("testModelVersion");
        vnfcCustomization.setModelName("testModelName");
        vnfcCustomization.setToscaNodeType("testToscaModelType");
        vnfcCustomization.setDescription("testVnfcCustomizationDescription");
        return vnfcCustomization;
    }

    protected CvnfcCustomization setUpCvnfcCustomization() {
        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
        cvnfcCustomization.setModelInstanceName("cvfncCustomizationTestModelInstanceName");
        cvnfcCustomization.setModelUUID("321228a4-9f15-11e8-98d0-529269fb1459");
        cvnfcCustomization.setModelInvariantUUID("c0659136-9f15-11e8-98d0-529269fb1459");
        cvnfcCustomization.setModelVersion("testModelVersion");
        cvnfcCustomization.setModelName("testModelName");
        cvnfcCustomization.setToscaNodeType("testToscaNodeType");
        cvnfcCustomization.setDescription("description");
        cvnfcCustomization.setNfcFunction("testNfcFunction");
        cvnfcCustomization.setNfcNamingCode("testNfcNamingCode");
        return cvnfcCustomization;
    }

    protected VfModule setUpVfModule() {
        VfModule vFModule = new VfModule();
        vFModule.setModelUUID("cb82ffd8-252a-11e7-93ae-92361f002671");
        vFModule.setModelVersion("testModelVersion");
        vFModule.setModelName("testModelName");
        vFModule.setIsBase(false);
        return vFModule;
    }

    protected VnfResource setUpVnfResource() {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelUUID("cb82ffd8-252a-11e7-93ae-92361f002671");
        vnfResource.setModelInvariantUUID("az82ffd8-252a-11e7-93ae-92361f002677");
        vnfResource.setModelVersion("testModelVersion");
        vnfResource.setOrchestrationMode("HEAT");
        return vnfResource;
    }

    @Test
    public void testNothing() {
        assertTrue(true);
    }
}
