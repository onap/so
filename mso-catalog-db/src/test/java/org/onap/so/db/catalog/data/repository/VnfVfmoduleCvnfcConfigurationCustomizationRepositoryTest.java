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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

public class VnfVfmoduleCvnfcConfigurationCustomizationRepositoryTest extends BaseTest {
    @Autowired
    private VnfVfmoduleCvnfcConfigurationCustomizationRepository vnfVfmoduleCvnfcConfigurationCustomizationRepository;
    @Autowired
    private CvnfcCustomizationRepository cvnfcCustomizationRepository;

    @Test
    public void findAllTest() throws Exception {
        List<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomizationList = vnfVfmoduleCvnfcConfigurationCustomizationRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(vnfVfmoduleCvnfcConfigurationCustomizationList));
        
        VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization = vnfVfmoduleCvnfcConfigurationCustomizationRepository.findOne(1);
        Assert.assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getConfigurationFunction().equalsIgnoreCase("testConfigurationFunction"));
    }
    
    @Test
    @Transactional
    public void createAndGetTest() throws Exception {
    	
    	VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization = new VnfVfmoduleCvnfcConfigurationCustomization();
    	vnfVfmoduleCvnfcConfigurationCustomization.setModelCustomizationUUID("cf9f6efc-9f14-11e8-98d0-529269fb1459");
    	vnfVfmoduleCvnfcConfigurationCustomization.setModelInstanceName("testModelInstanceName");
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationType("testConfigurationType");
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationRole("testConfigurationRole");
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationFunction("testConfigurationFunction");
    	vnfVfmoduleCvnfcConfigurationCustomization.setPolicyName("testPolicyName");

    	ConfigurationResource configurationResource = new ConfigurationResource();
    	configurationResource.setModelUUID("98b42780-9f13-11e8-98d0-529269fb1459");
    	configurationResource.setModelInvariantUUID("c9338d1a-9f13-11e8-98d0-529269fb1459");
    	configurationResource.setModelVersion("testModelVertsion");
    	configurationResource.setModelName("testModelName");
    	configurationResource.setToscaNodeType("testToscaNodeType");
    	configurationResource.setDescription("testConfigurationDescription");
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(configurationResource);
    	
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
    	
    	List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList();
    	vnfResourceCustomizations.add(vnfResourceCustomization);
    	vnfResource.setVnfResourceCustomizations(vnfResourceCustomizations);
    	vnfResourceCustomization.setVnfResources(vnfResource);
    	
    	cvnfcCustomization.setVnfResourceCustomization(vnfResourceCustomization);
    	
    	VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
    	vnfcCustomization.setModelCustomizationUUID("0aa015ea-9ff3-11e8-98d0-529269fb1459");
    	cvnfcCustomization.setVnfcCustomization(vnfcCustomization);
    	
    	cvnfcCustomizationRepository.save(cvnfcCustomization);
    	
    	vnfVfmoduleCvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);
    	vnfVfmoduleCvnfcConfigurationCustomization.setVfModuleCustomization(vfModuleCustomization);
    	vnfVfmoduleCvnfcConfigurationCustomization.setVnfResourceCustomization(vnfResourceCustomization);
    	
    	vnfVfmoduleCvnfcConfigurationCustomizationRepository.save(vnfVfmoduleCvnfcConfigurationCustomization);
    	
    	VnfVfmoduleCvnfcConfigurationCustomization foundVnfVfmoduleCvnfcConfigurationCustomization = vnfVfmoduleCvnfcConfigurationCustomizationRepository.findOne(1);
       
        
    	if(foundVnfVfmoduleCvnfcConfigurationCustomization == null)
    		fail("should not be null");
    }
}
