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

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VFModuleTest {
    @Autowired
    private VFModuleRepository vfModuleRepo;

    @Test
    public void VFModule_Versioned_LookUp() {
        VfModule latestModule = vfModuleRepo.findFirstByModelNameOrderByModelVersionDesc("vSAMP10aDEV::PCM::module-1");
        assertEquals("066de97e-253e-11e7-93ae-92361f002675", latestModule.getModelUUID());
    }

    @Test
    public void VFModule_Versioned_LookUp_LIst() {
        List<VfModule> moduleList =
                vfModuleRepo.findByModelInvariantUUIDOrderByModelVersionDesc("64efd51a-2544-11e7-93ae-92361f002671");
        assertEquals("066de97e-253e-11e7-93ae-92361f002675", moduleList.get(0).getModelUUID());
        assertEquals("066de97e-253e-11e7-93ae-92361f002674", moduleList.get(1).getModelUUID());
        assertEquals("066de97e-253e-11e7-93ae-92361f002673", moduleList.get(2).getModelUUID());
    }
}
