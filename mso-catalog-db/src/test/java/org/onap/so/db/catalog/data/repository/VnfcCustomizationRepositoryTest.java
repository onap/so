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

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

public class VnfcCustomizationRepositoryTest extends BaseTest {
    @Autowired
    private VnfcCustomizationRepository vnfcCustomizationRepository;

    @Test
    public void findAllTest() throws Exception {
        List<VnfcCustomization> vnfcCustomizationList = vnfcCustomizationRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(vnfcCustomizationList));

        VnfcCustomization vnfcCustomization =
                vnfcCustomizationRepository.findById("9bcce658-9b37-11e8-98d0-529269fb1459")
                        .orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));
        Assert.assertTrue(vnfcCustomization.getDescription().equalsIgnoreCase("testVnfcCustomizationDescription"));
    }

    @Test
    @Transactional
    public void createAndGetTest() throws Exception {

        VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
        vnfcCustomizationRepository.save(vnfcCustomization);

        VnfcCustomization foundVnfcCustomization =
                vnfcCustomizationRepository.findById("cf9f6efc-9f14-11e8-98d0-529269fb1459")
                        .orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));

        assertThat(vnfcCustomization, sameBeanAs(foundVnfcCustomization).ignoring("created"));
    }
}
