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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.client.CatalogDbClientPortChanger;
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
		
    	CvnfcCustomization cvnfcCustomization = setUpCvnfcCustomization();
   	
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
	
    protected CvnfcCustomization setUpCvnfcCustomization(){
    	CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
    	cvnfcCustomization.setModelCustomizationUUID("9bcce658-9b37-11e8-98d0-529269fb1459");
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
}
