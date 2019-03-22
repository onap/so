/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.catalogdb.catalogrest;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.client.CatalogDbClientPortChanger;
import org.onap.so.db.catalog.data.repository.CvnfcCustomizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.beans.BeanUtils;

public class CvnfcCatalogDbQueryTest extends CatalogDbAdapterBaseTest {

    @Autowired
    private CvnfcCustomizationRepository cvnfcCustomizationRepository;
    
	private static final Logger logger = LoggerFactory.getLogger(CvnfcCatalogDbQueryTest.class);

	@LocalServerPort
	private int port;
	boolean isInitialized;

	@Autowired
	CatalogDbClientPortChanger client;

	@Before
	public void initialize(){
		client.wiremockPort= String.valueOf(port);
	}
	
	@Test
	public void cVnfcTest() {
		
    	CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization("dadc2c8c-2bab-11e9-b210-d663bd873d93");
   	
		List<CvnfcCustomization> foundCvnfcCustomization = client.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671","cb82ffd8-252a-11e7-93ae-92361f002671");
		assertNotNull(foundCvnfcCustomization);
		assertTrue(foundCvnfcCustomization.size() > 0);
		CvnfcCustomization found = foundCvnfcCustomization.get(0);
		
		CvnfcCustomization templateCvnfcCustomization = new CvnfcCustomization();
		BeanUtils.copyProperties(found, templateCvnfcCustomization, "vnfVfmoduleCvnfcConfigurationCustomization", "vfModuleCustomization", "vnfcCustomization", "vnfResourceCustomization");
		
        assertThat(cvnfcCustomization, sameBeanAs(templateCvnfcCustomization)
        		.ignoring("id")
        		.ignoring("created")
        		.ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
        		.ignoring("vnfResourceCusteModelCustomizationUUID"));
	}
	
	@Test
	public void cVnfcByCustomizationUUID_Test() {
		
    	CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization("dadc2c8c-2bab-11e9-b210-d663bd873d93");
   	
		CvnfcCustomization found = client.getCvnfcCustomizationByCustomizationUUID("dadc2c8c-2bab-11e9-b210-d663bd873d93");
		assertNotNull(found);
		
		CvnfcCustomization templateCvnfcCustomization = new CvnfcCustomization();
		BeanUtils.copyProperties(found, templateCvnfcCustomization, "vnfVfmoduleCvnfcConfigurationCustomization", "vfModuleCustomization", "vnfcCustomization", "vnfResourceCustomization");
		
        assertThat(cvnfcCustomization, sameBeanAs(templateCvnfcCustomization)
        		.ignoring("id")
        		.ignoring("created")
        		.ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
        		.ignoring("vnfResourceCusteModelCustomizationUUID"));
	}

	
    protected CvnfcCustomization setUpCvnfcCustomization(String id){
    	CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
    	cvnfcCustomization.setModelCustomizationUUID(id);
    	cvnfcCustomization.setModelInstanceName("testModelInstanceName");
    	cvnfcCustomization.setModelUUID("b25735fe-9b37-11e8-98d0-529269fb1459");
    	cvnfcCustomization.setModelInvariantUUID("ba7e6ef0-9b37-11e8-98d0-529269fb1459");
    	cvnfcCustomization.setModelVersion("testModelVersion");
    	cvnfcCustomization.setModelName("testModelName");
    	cvnfcCustomization.setToscaNodeType("testToscaNodeType");
    	cvnfcCustomization.setDescription("testCvnfcCustomzationDescription");
    	cvnfcCustomization.setNfcFunction("testNfcFunction");
    	cvnfcCustomization.setNfcNamingCode("testNfcNamingCode");
    	return cvnfcCustomization;
    }
    
    protected VnfcCustomization setUpVnfcCustomization(){
    	VnfcCustomization vnfcCustomization = new VnfcCustomization();
    	vnfcCustomization.setModelInstanceName("testVnfcCustomizationModelInstanceName");
    	vnfcCustomization.setModelUUID("321228a4-9f15-11e8-98d0-529269fb1459");
    	vnfcCustomization.setModelInvariantUUID("c0659136-9f15-11e8-98d0-529269fb1459");
    	vnfcCustomization.setModelVersion("testModelVersion");
    	vnfcCustomization.setModelName("testModelName");
    	vnfcCustomization.setToscaNodeType("testToscaModelType");
    	vnfcCustomization.setDescription("testVnfcCustomizationDescription");
    	return vnfcCustomization;
    }
}