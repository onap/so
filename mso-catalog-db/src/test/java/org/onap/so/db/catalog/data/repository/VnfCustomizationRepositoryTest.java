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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;


public class VnfCustomizationRepositoryTest extends BaseTest {

    @Autowired
    private VnfCustomizationRepository vnfCustomizationRepository;

    @Test
    public void findByModelCustomizationUUID_ValidUuid_ExpectedOutput() throws Exception {
        List<VnfResourceCustomization> vnfCustomizationList = vnfCustomizationRepository
            .findByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671");
        assertFalse(CollectionUtils.isEmpty(vnfCustomizationList));
        assertEquals("output contains one entity", 1, vnfCustomizationList.size());

        checkVnfResourceCustomization(vnfCustomizationList.get(0));
    }

    @Test
    public void findOneByModelCustomizationUUID_ValidUuid_ExpectedOutput() throws Exception {
        VnfResourceCustomization vnfResourceCustomization = vnfCustomizationRepository
            .findOneByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671");
        checkVnfResourceCustomization(vnfResourceCustomization);
    }

    @Test
    public void findByModelInstanceNameAndVnfResources_ValidNameAndUuid_ExpectedOutput() throws Exception {
        VnfResourceCustomization vnfResourceCustomization = vnfCustomizationRepository
            .findByModelInstanceNameAndVnfResources("vSAMP10a 1", "ff2ae348-214a-11e7-93ae-92361f002671");
        checkVnfResourceCustomization(vnfResourceCustomization);
    }

    private void checkVnfResourceCustomization(VnfResourceCustomization vnfResourceCustomization) {
        assertEquals("modelInstanceName", "vSAMP10a 1", vnfResourceCustomization.getModelInstanceName());
        assertEquals("blueprintName", "test_configuration_restconf", vnfResourceCustomization.getBlueprintName());
        assertEquals("blueprintVersion", "1.0.0", vnfResourceCustomization.getBlueprintVersion());
        VnfResource vnfResource = vnfResourceCustomization.getVnfResources();
        assertNotNull(vnfResource);

        assertEquals("VNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002671", vnfResource.getModelUUID());
        assertEquals("VNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002671",
            vnfResource.getModelInvariantUUID());
        assertEquals("VNFResource modelVersion", "1.0", vnfResource.getModelVersion());
        assertEquals("VNFResource heat template", "HEAT", vnfResource.getOrchestrationMode());
    }
}
