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
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.beans.BeanUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CvnfcCatalogDbQueryTest {

    @Autowired
    private CvnfcCustomizationRepository cvnfcCustomizationRepository;
    
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CvnfcCatalogDbQueryTest.class);

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
		BeanUtils.copyProperties(found, templateCvnfcCustomization, "vnfVfmoduleCvnfcConfigurationCustomization");
		
        assertThat(cvnfcCustomization, sameBeanAs(templateCvnfcCustomization)
        		.ignoring("id")
        		.ignoring("created")
        		.ignoring("vnfVfmoduleCvnfcConfigurationCustomization")
        		.ignoring("vnfResourceCusteModelCustomizationUUID"));
	}
	
	@Test
	public void getLinkedVnfVfmoduleCvnfcConfigurationCustomizationTest() {
		
    	CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization("0c042562-2bac-11e9-b210-d663bd873d93");
    	
    	VnfcCustomization vnfcCustomization = setUpVnfcCustomization();
    	vnfcCustomization.setModelCustomizationUUID("d95d704a-9ff2-11e8-98d0-529269fb1459");
    	cvnfcCustomization.setVnfcCustomization(vnfcCustomization);
    	
		ConfigurationResource configurationResource = new ConfigurationResource();
		configurationResource.setToscaNodeType("FabricConfiguration");
		configurationResource.setModelInvariantUUID("modelInvariantUUID");
		configurationResource.setModelUUID("modelUUID");
		configurationResource.setModelName("modelName");
		configurationResource.setModelVersion("modelVersion");
		configurationResource.setDescription("description");
		configurationResource.setToscaNodeType("toscaNodeTypeFC");
		
		VnfResource vnfResource = new VnfResource();
		vnfResource.setModelUUID("6f19c5fa-2b19-11e9-b210-d663bd873d93");
		vnfResource.setModelVersion("modelVersion");
		vnfResource.setOrchestrationMode("orchestrationMode");
		
		VfModule vfModule = new VfModule();
		vfModule.setModelUUID("98aa2a6e-2b18-11e9-b210-d663bd873d93");
		vfModule.setModelInvariantUUID("9fe57860-2b18-11e9-b210-d663bd873d93");
		vfModule.setIsBase(true);
		vfModule.setModelName("modelName");
		vfModule.setModelVersion("modelVersion");
		vfModule.setVnfResources(vnfResource);
		
    	VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
    	vfModuleCustomization.setModelCustomizationUUID("bdbf984a-2b16-11e9-b210-d663bd873d93");
    	vfModuleCustomization.setVfModule(vfModule);
    	cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);
    	
    	VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
    	vnfResourceCustomization.setModelCustomizationUUID("6912dd02-2b16-11e9-b210-d663bd873d93"); 
    	vnfResourceCustomization.setModelInstanceName("testModelInstanceName");
    	vnfResourceCustomization.setVnfResources(vnfResource);
    	cvnfcCustomization.setVnfResourceCustomization(vnfResourceCustomization);
    	
    	VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization = new VnfVfmoduleCvnfcConfigurationCustomization();
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationFunction("configurationFunction");
    	vnfVfmoduleCvnfcConfigurationCustomization.setModelCustomizationUUID("64627fec-2b1b-11e9-b210-d663bd873d93");
    	vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(configurationResource);
    	vnfVfmoduleCvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);
    	vnfVfmoduleCvnfcConfigurationCustomization.setModelInstanceName("modelInstanceName");
    	vnfVfmoduleCvnfcConfigurationCustomization.setVfModuleCustomization(vfModuleCustomization);
    	vnfVfmoduleCvnfcConfigurationCustomization.setVnfResourceCustomization(vnfResourceCustomization);
    	
    	Set<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomizationSet = new HashSet<VnfVfmoduleCvnfcConfigurationCustomization>();
    	vnfVfmoduleCvnfcConfigurationCustomizationSet.add(vnfVfmoduleCvnfcConfigurationCustomization);
    	cvnfcCustomization.setVnfVfmoduleCvnfcConfigurationCustomization(vnfVfmoduleCvnfcConfigurationCustomizationSet);
    	
    	vnfVfmoduleCvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);
    	
    	cvnfcCustomizationRepository.save(cvnfcCustomization);
   	
		List<CvnfcCustomization> foundCvnfcCustomization = client.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID("6912dd02-2b16-11e9-b210-d663bd873d93","bdbf984a-2b16-11e9-b210-d663bd873d93");
		assertNotNull(foundCvnfcCustomization);
		assertTrue(foundCvnfcCustomization.size() > 0);
		CvnfcCustomization found = foundCvnfcCustomization.get(0);

        Set<VnfVfmoduleCvnfcConfigurationCustomization>  vnfVfmoduleCvnfcConfigurationCustomizations =  found.getVnfVfmoduleCvnfcConfigurationCustomization();
        if (vnfVfmoduleCvnfcConfigurationCustomizations.size() > 0){
        	for(VnfVfmoduleCvnfcConfigurationCustomization customization : vnfVfmoduleCvnfcConfigurationCustomizations) {
        		Assert.assertTrue(customization.getConfigurationResource().getToscaNodeType().equalsIgnoreCase("toscaNodeTypeFC"));
        	}
        } else {
        	Assert.fail("No linked VnfVfmoduleCvnfcConfigurationCustomization found for CvnfcCustomization");
        }
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
