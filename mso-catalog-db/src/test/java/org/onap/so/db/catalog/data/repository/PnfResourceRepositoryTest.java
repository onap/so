/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.data.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class PnfResourceRepositoryTest extends BaseTest {

    @Autowired
    private PnfResourceRepository pnfResourceRepository;

    @Test
    public void findResourceById_validUuid_expectedOutput() throws Exception {

        PnfResource pnfResource = pnfResourceRepository.findById("ff2ae348-214a-11e7-93ae-92361f002680")
            .orElseThrow(() -> new NoEntityFoundException("Cannot Find Entity"));
        checkPnfResource(pnfResource);
    }

    private void checkPnfResource(PnfResource pnfResource) {
        assertEquals("PNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002680", pnfResource.getModelUUID());
        assertEquals("PNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002680",
            pnfResource.getModelInvariantUUID());
        assertEquals("PNFResource modelVersion", "1.0", pnfResource.getModelVersion());
        assertEquals("PNFResource orchestration mode", "", pnfResource.getOrchestrationMode());
    }
}