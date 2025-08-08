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
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

public class CvnfcCustomizationRepositoryTest extends BaseTest {
    @Autowired
    private CvnfcCustomizationRepository cvnfcCustomizationRepository;


    @Test
    public void findAllTest() {
        List<CvnfcCustomization> cvnfcCustomizationList = cvnfcCustomizationRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(cvnfcCustomizationList));
    }

    @Test
    @Transactional
    public void createAndGetAllTest() {

        CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization();
        cvnfcCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModule vFModule = setUpVfModule();
        VnfResource vnfResource = setUpVnfResource();

        vFModule.setVnfResources(vnfResource);
        vfModuleCustomization.setVfModule(vFModule);
        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
        vnfResourceCustomization.setModelInstanceName("testModelInstanceName");

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        vnfResourceCustomizations.add(vnfResourceCustomization);
        vnfResourceCustomization.setVnfResources(vnfResource);


        VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("d95d704a-9ff2-11e8-98d0-529269fb1459");

        cvnfcCustomizationRepository.save(cvnfcCustomization);

        List<CvnfcCustomization> cvnfcCustomizationList = cvnfcCustomizationRepository.findAll();
        boolean matchFound = false;
        for (CvnfcCustomization foundCvnfcCustomization : cvnfcCustomizationList) {
            if (foundCvnfcCustomization.getDescription().equalsIgnoreCase(cvnfcCustomization.getDescription())) {

                assertThat(cvnfcCustomization,
                        sameBeanAs(foundCvnfcCustomization).ignoring("id").ignoring("created")
                                .ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
                                .ignoring("vnfResourceCusteModelCustomizationUUID"));

                matchFound = true;
                break;
            }
        }
        Assert.assertTrue(matchFound);
    }

    @Test
    @Transactional
    public void createAndGetCvnfcCustomizationListTest() {

        CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization();
        cvnfcCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModule vFModule = setUpVfModule();
        VnfResource vnfResource = setUpVnfResource();

        vFModule.setVnfResources(vnfResource);
        vfModuleCustomization.setVfModule(vFModule);
        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
        vnfResourceCustomization.setModelInstanceName("testModelInstanceName");

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        vnfResourceCustomizations.add(vnfResourceCustomization);
        vnfResourceCustomization.setVnfResources(vnfResource);


        VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("d95d704a-9ff2-11e8-98d0-529269fb1459");

        cvnfcCustomizationRepository.save(cvnfcCustomization);


        List<CvnfcCustomization> cvnfcCustomizationList =
                cvnfcCustomizationRepository.findByModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
        boolean matchFound = false;
        for (CvnfcCustomization foundCvnfcCustomization : cvnfcCustomizationList) {
            if (foundCvnfcCustomization.getDescription().equalsIgnoreCase(cvnfcCustomization.getDescription())) {

                assertThat(cvnfcCustomization,
                        sameBeanAs(foundCvnfcCustomization).ignoring("id").ignoring("created")
                                .ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
                                .ignoring("vnfResourceCusteModelCustomizationUUID"));

                matchFound = true;
                break;
            }
        }
        Assert.assertTrue(matchFound);
    }


    @Test
    @Transactional
    public void createAndGetCvnfcCustomizationTest() {

        CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization();
        cvnfcCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        VfModule vFModule = setUpVfModule();
        VnfResource vnfResource = setUpVnfResource();

        vFModule.setVnfResources(vnfResource);
        vfModuleCustomization.setVfModule(vFModule);
        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
        vnfResourceCustomization.setModelInstanceName("testModelInstanceName");

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        vnfResourceCustomizations.add(vnfResourceCustomization);
        vnfResourceCustomization.setVnfResources(vnfResource);



        VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("d95d704a-9ff2-11e8-98d0-529269fb1459");

        cvnfcCustomizationRepository.save(cvnfcCustomization);

        CvnfcCustomization cvnfcCustomizationList =
                cvnfcCustomizationRepository.findOneByModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");

        assertThat(cvnfcCustomization,
                sameBeanAs(cvnfcCustomizationList).ignoring("id").ignoring("created")
                        .ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
                        .ignoring("vnfResourceCusteModelCustomizationUUID"));

    }



}
