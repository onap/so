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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NetworkTest {
	@Autowired
	private NetworkResourceRepository networkRepo;
	
	@Test
	public void BuildingBlockDetailSingleLookupValidationTest() {
		NetworkResource latestNetwork = networkRepo.findFirstByModelNameOrderByModelVersionDesc("CONTRAIL30_GNDIRECT");
		assertEquals("10b36f65-f4e6-4be6-ae49-9596dc1c47fz",latestNetwork.getModelUUID());
	}
}
