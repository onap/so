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
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ServiceTest {

    @Autowired
    private ServiceRepository serviceRepo;

    @Test
    public void Find_LatestService_Test() {
        Service latestVersionService =
                serviceRepo.findFirstByModelNameOrderByModelVersionDesc("MSOTADevInfra_vSAMP10a_Service");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002675", latestVersionService.getModelUUID());
    }


    @Test
    public void Find_LatestService_Test_2() {
        Service latestVersionService =
                serviceRepo.findByModelNameOrderByModelVersionDesc("MSOTADevInfra_vSAMP10a_Service");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002675", latestVersionService.getModelUUID());
    }

    @Test
    public void Find_LatestService_Test_Invariant_UUID() {
        List<Service> latestVersionService =
                serviceRepo.findByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002671");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002675", latestVersionService.get(0).getModelUUID());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002674", latestVersionService.get(1).getModelUUID());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002673", latestVersionService.get(2).getModelUUID());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002672", latestVersionService.get(3).getModelUUID());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002671", latestVersionService.get(4).getModelUUID());
    }

    @Test
    public void Find_LatestService_Test_4() {
        Service latestVersionService =
                serviceRepo.findOneByModelUUIDOrderByModelVersionDesc("5df8b6de-2083-11e7-93ae-92361f002671");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002671", latestVersionService.getModelUUID());
    }

    @Test
    public void Find_LatestService_Test_5() {
        Service latestVersionService = serviceRepo
                .findFirstByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002671");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002675", latestVersionService.getModelUUID());
    }


    @Test
    public void findByModelNameOrderByModelVersionDesc_ValidModelName_ExpectedOutput() {
        Service latestVersionService = serviceRepo.findByModelNameOrderByModelVersionDesc("PNF_routing_service");
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002676", latestVersionService.getModelUUID());
    }

    @Test
    public void findByModelInvariantUUIDOrderByModelVersionDesc_ValidInvariantUuid_ExpectedOutput() {
        List<Service> services =
                serviceRepo.findByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002676");
        assertEquals("One PNF service found", 1, services.size());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002676", services.get(0).getModelUUID());
    }
}
