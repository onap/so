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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class VnfcInstanceGroupCustomizationRepositoryTest extends BaseTest {
    @Autowired
    private VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepository;

    @Test
    public void findAllTest() throws Exception {
        List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizationList =
                vnfcInstanceGroupCustomizationRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(vnfcInstanceGroupCustomizationList));

        VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = vnfcInstanceGroupCustomizationRepository
                .findById(1450).orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));
        Assert.assertTrue(vnfcInstanceGroupCustomization.getFunction().equalsIgnoreCase("FUNCTION"));
    }
}
