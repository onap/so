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

package org.onap.so.db.catalog.data.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;

public class CollectionNetworkResourceCustomizationRepositoryTest extends BaseTest {
    @Autowired
    private CollectionNetworkResourceCustomizationRepository cnrcRepo;

    @Test
    public void findAllTest() {
        List<CollectionNetworkResourceCustomization> cnrcList = cnrcRepo.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(cnrcList));
    }

    @Test
    public void findOneByUuidTest() {
        CollectionNetworkResourceCustomization cnrc =
                cnrcRepo.findOneByModelCustomizationUUID("3bdbb104-ffff-483e-9f8b-c095b3d3068c");
        Assert.assertTrue(cnrc != null);
        Assert.assertTrue("ExtVL 01".equals(cnrc.getModelInstanceName()));
    }
}
