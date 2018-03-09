/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.asdc.client.tests;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * THis class tests the ASDC Controller by using the ASDC Mock CLient
 * 
 *
 */
public class ASDCConfigurationTest {
	
	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	public final String ASDC_PROP = ASDCConfigurationTest.class.getClassLoader().getResource("mso.json").toString().substring(5);
	public final String ASDC_PROP2 = ASDCConfigurationTest.class.getClassLoader().getResource("mso2.json").toString().substring(5);
	public final String ASDC_PROP3 = ASDCConfigurationTest.class.getClassLoader().getResource("mso3.json").toString().substring(5);
	public final String ASDC_PROP_BAD = ASDCConfigurationTest.class.getClassLoader().getResource("mso-bad.json").toString().substring(5);
	public final String ASDC_PROP_WITH_NULL = ASDCConfigurationTest.class.getClassLoader().getResource("mso-with-NULL.json").toString().substring(5);
	public final String ASDC_PROP_DOUBLE_CONFIG = ASDCConfigurationTest.class.getClassLoader().getResource("mso-two-configs.json").toString().substring(5);
	public final String ASDC_PROP4_WITH_TLS = ASDCConfigurationTest.class.getClassLoader().getResource("mso4-with-TLS.json").toString().substring(5);
	
	@BeforeClass
	public static final void prepareBeforeAllTests() {
		msoPropertiesFactory.removeAllMsoProperties();
	}
	
	@Before
	public final void prepareBeforeEachTest () throws MsoPropertiesException {
		msoPropertiesFactory.initializeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
	}
	
	@After
	public final void cleanAfterEachTest () {
		msoPropertiesFactory.removeAllMsoProperties();
	}
	
	@Test
	public final void testTheInit() throws ASDCParametersException, IOException {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");
		assertNotNull(asdcConfig.getUser());
		assertTrue("User".equals(asdcConfig.getUser()));
		
		assertNotNull(asdcConfig.getPassword());
		assertTrue("ThePassword".equals(asdcConfig.getPassword()));
		
		assertNotNull(asdcConfig.getConsumerGroup());
		assertTrue("consumerGroup".equals(asdcConfig.getConsumerGroup()));
		
		assertNotNull(asdcConfig.getConsumerID());
		assertTrue("consumerId".equals(asdcConfig.getConsumerID()));
		
		assertNotNull(asdcConfig.getEnvironmentName());
		assertTrue("environmentName".equals(asdcConfig.getEnvironmentName()));
		
		assertNotNull(asdcConfig.getAsdcAddress());
		assertTrue("hostname".equals(asdcConfig.getAsdcAddress()));
		
		assertNotNull(asdcConfig.getPollingInterval());
		assertTrue(asdcConfig.getPollingInterval() == 10);
		
		assertNotNull(asdcConfig.getPollingTimeout());
		assertTrue(asdcConfig.getPollingTimeout() == 30);
		
		assertNotNull(asdcConfig.getRelevantArtifactTypes());
		assertTrue(asdcConfig.getRelevantArtifactTypes().size() == ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());
		
		assertFalse(asdcConfig.activateServerTLSAuth());
		
	}
	
	@Test
	public final void testAllParametersMethod() throws ASDCParametersException, IOException {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");
		
		// No exception should be raised
		asdcConfig.testAllParameters();
	}

	@Test
	public final void testTheRefreshConfigFalseCase() throws ASDCParametersException, IOException {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");
		
		// No update should be done as we use the mso.properties located in the resource folder for testing
		assertFalse(asdcConfig.hasASDCConfigChanged());
		assertFalse(asdcConfig.refreshASDCConfig());
		
		assertNotNull(asdcConfig.getUser());
		assertTrue("User".equals(asdcConfig.getUser()));
		
		assertNotNull(asdcConfig.getPassword());
		assertTrue("ThePassword".equals(asdcConfig.getPassword()));
		
		assertNotNull(asdcConfig.getConsumerGroup());
		assertTrue("consumerGroup".equals(asdcConfig.getConsumerGroup()));
		
		assertNotNull(asdcConfig.getConsumerID());
		assertTrue("consumerId".equals(asdcConfig.getConsumerID()));
		
		assertNotNull(asdcConfig.getEnvironmentName());
		assertTrue("environmentName".equals(asdcConfig.getEnvironmentName()));
		
		assertNotNull(asdcConfig.getAsdcAddress());
		assertTrue("hostname".equals(asdcConfig.getAsdcAddress()));
		
		assertNotNull(asdcConfig.getPollingInterval());
		assertTrue(asdcConfig.getPollingInterval() == 10);
		
		assertNotNull(asdcConfig.getPollingTimeout());
		assertTrue(asdcConfig.getPollingTimeout() == 30);
		
		assertNotNull(asdcConfig.getRelevantArtifactTypes());
		assertTrue(asdcConfig.getRelevantArtifactTypes().size() == ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());
		
		msoPropertiesFactory.removeAllMsoProperties();
		
		try {
			asdcConfig.refreshASDCConfig();
			fail("Should have thrown an ASDCParametersException because config does not exist anymore!");
		} catch (ASDCParametersException e) {
			assertTrue(e.getMessage().contains(("mso.asdc.json not initialized properly, ASDC config cannot be reloaded")));
		}
		
		try {
			asdcConfig.hasASDCConfigChanged();
			fail("Should have thrown an ASDCParametersException because config does not exist anymore!");
		} catch (ASDCParametersException e) {
			assertTrue(e.getMessage().contains(("mso.asdc.json not initialized properly, ASDC config cannot be read")));
		}
		
	}	
	
	
	@Test
	public final void testToChangeTheFileAndRefresh () throws ASDCParametersException, IOException, MsoPropertiesException  {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");

		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP2);
		msoPropertiesFactory.reloadMsoProperties();
		
		// SHould be the same file untouched just a different file name, there should be no difference between them
		// In a normal case a different Filename should force the system to reload the config but not here as we have changed the filename by reflection
		assertFalse(asdcConfig.hasASDCConfigChanged());
		assertFalse(asdcConfig.refreshASDCConfig());

		assertNotNull(asdcConfig.getUser());
		assertTrue("User".equals(asdcConfig.getUser()));

		assertNotNull(asdcConfig.getPassword());
		assertTrue("ThePassword".equals(asdcConfig.getPassword()));

		assertNotNull(asdcConfig.getConsumerGroup());
		assertTrue("consumerGroup".equals(asdcConfig.getConsumerGroup()));

		assertNotNull(asdcConfig.getConsumerID());
		assertTrue("consumerId".equals(asdcConfig.getConsumerID()));

		assertNotNull(asdcConfig.getEnvironmentName());
		assertTrue("environmentName".equals(asdcConfig.getEnvironmentName()));

		assertNotNull(asdcConfig.getAsdcAddress());
		assertTrue("hostname".equals(asdcConfig.getAsdcAddress()));

		assertNotNull(asdcConfig.getPollingInterval());
		assertTrue(asdcConfig.getPollingInterval() == 10);

		assertNotNull(asdcConfig.getPollingTimeout());
		assertTrue(asdcConfig.getPollingTimeout() == 30);

		assertNotNull(asdcConfig.getRelevantArtifactTypes());
		assertTrue(asdcConfig.getRelevantArtifactTypes().size() == ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());

		// Set another file that has some attributes changed
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP3);
		msoPropertiesFactory.reloadMsoProperties();
		
		// SHould be the same file untouched just a different file name, so new config
		assertTrue(asdcConfig.hasASDCConfigChanged());
		assertTrue(asdcConfig.refreshASDCConfig());

		assertNotNull(asdcConfig.getUser());
		assertTrue("User".equals(asdcConfig.getUser()));

		assertNotNull(asdcConfig.getPassword());
		assertTrue("ThePassword".equals(asdcConfig.getPassword()));

		assertNotNull(asdcConfig.getConsumerGroup());
		assertTrue("consumerGroup".equals(asdcConfig.getConsumerGroup()));

		assertNotNull(asdcConfig.getConsumerID());
		assertTrue("consumerId".equals(asdcConfig.getConsumerID()));

		assertNotNull(asdcConfig.getEnvironmentName());
		assertTrue("environmentName".equals(asdcConfig.getEnvironmentName()));

		// only this field has been changed
		assertNotNull(asdcConfig.getAsdcAddress());
		assertTrue("hostname1".equals(asdcConfig.getAsdcAddress()));

		assertNotNull(asdcConfig.getPollingInterval());
		assertTrue(asdcConfig.getPollingInterval() == 10);

		assertNotNull(asdcConfig.getPollingTimeout());
		assertTrue(asdcConfig.getPollingTimeout() == 30);

		assertNotNull(asdcConfig.getRelevantArtifactTypes());
		assertTrue(asdcConfig.getRelevantArtifactTypes().size() == ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());


		// reload the good property file for other test cases
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
		msoPropertiesFactory.reloadMsoProperties();

	}
	
	@Test
	public final void testAllParametersCheck () throws ASDCParametersException, IOException, MsoPropertiesException  {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");

		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_BAD);
		msoPropertiesFactory.reloadMsoProperties();
		// SHould be a bad file, it should raise an exception
		try {
			asdcConfig.refreshASDCConfig();
			fail("Should have thrown an ASDCControllerException because one param is missing!");
		} catch (ASDCParametersException e) {
			assertTrue(e.getMessage().contains(("consumerGroup parameter cannot be found in config mso.properties")));
		}


		// reload the good property file for other test cases
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
		msoPropertiesFactory.reloadMsoProperties();
		
		assertTrue(asdcConfig.refreshASDCConfig());
 
	}
	
	@Test
	public final void testConsumerGroupWithNULL () throws MsoPropertiesException, ASDCParametersException, IOException {
		ASDCConfiguration asdcConfig = new ASDCConfiguration("asdc-controller1");
		
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_WITH_NULL);
		msoPropertiesFactory.reloadMsoProperties();

		asdcConfig.refreshASDCConfig();
		assertTrue(asdcConfig.getConsumerGroup()==null);

		// reload the good property file for other test cases
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
		msoPropertiesFactory.reloadMsoProperties();
		
		assertTrue(asdcConfig.refreshASDCConfig());


	}

	@Test
	public final void testGetAllDefinedControllers() throws MsoPropertiesException, ASDCParametersException, IOException {
		List<String> listControllers = ASDCConfiguration.getAllDefinedControllers();
		
		assertTrue(listControllers.size()==1);
		assertTrue("asdc-controller1".equals(listControllers.get(0)));
		
		ASDCConfiguration asdcConfiguration = new ASDCConfiguration("asdc-controller1");
		assertTrue(asdcConfiguration.getAsdcControllerName().equals("asdc-controller1"));
		
		
		// Try to reload a wrong Json file
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_BAD);
		msoPropertiesFactory.reloadMsoProperties();
		
		listControllers = ASDCConfiguration.getAllDefinedControllers();
		assertTrue(listControllers.size()==0);

	}
	
	@Test
	public final void testABadInit() throws MsoPropertiesException {
		msoPropertiesFactory.removeAllMsoProperties();		
		
		try {
			ASDCConfiguration asdcConfiguration = new ASDCConfiguration("asdc-controller1");
			fail("Should have thrown an ASDCParametersException because prop factory is empty!");
		} catch (ASDCParametersException e) {
			assertTrue(e.getMessage().contains(("mso.asdc.json not initialized properly, ASDC config cannot be reloaded")));
		} catch (IOException e) {
			fail("Should have thrown an ASDCParametersException, not IOException because file is corrupted!");
		}
	}
	
	@Test
	public final void testFileDoesNotExist() throws MsoPropertiesException, ASDCParametersException, IOException {
				
			ASDCConfiguration asdcConfiguration = new ASDCConfiguration("asdc-controller1");
		
			msoPropertiesFactory.removeAllMsoProperties();
			
		try {	
			asdcConfiguration.refreshASDCConfig();
			fail("Should have thrown an ASDCParametersException because factory is empty!");
		} catch (ASDCParametersException e) {
			assertTrue(e.getMessage().contains(("mso.asdc.json not initialized properly, ASDC config cannot be reloaded")));
		} 
	}

	@Test
	public final void testWithTLS () throws ASDCParametersException, IOException, MsoPropertiesException {
		ASDCConfiguration asdcConfiguration = new ASDCConfiguration("asdc-controller1");
		
		msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP4_WITH_TLS);
		msoPropertiesFactory.reloadMsoProperties();

		asdcConfiguration.refreshASDCConfig();
		
		assertTrue(asdcConfiguration.activateServerTLSAuth());
		assertTrue("/test".equals(asdcConfiguration.getKeyStorePath()));
		assertTrue("ThePassword".equals(asdcConfiguration.getKeyStorePassword()));
	}
	
}
