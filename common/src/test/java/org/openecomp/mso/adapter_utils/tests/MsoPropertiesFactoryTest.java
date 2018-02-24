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

package org.openecomp.mso.adapter_utils.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoJsonProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class implements test methods of the MsoPropertiesFactory features.
 *
 *
 */
public class MsoPropertiesFactoryTest {

	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	public static final String MSO_JAVA_PROP_ID = "TEST_JAVA_PROP";
	public static final String MSO_JSON_PROP_ID = "TEST_JSON_PROP";
	public static final String PATH_MSO_JAVA_PROP1 = MsoJavaProperties.class.getClassLoader().getResource("mso.properties")
			.toString().substring(5);
	public static final String PATH_MSO_JAVA_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.properties")
			.toString().substring(5);
	public static final String PATH_MSO_JSON_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.json")
			.toString().substring(5);
	public static final String PATH_MSO_JSON_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.json")
			.toString().substring(5);
	public static final String PATH_MSO_JSON_PROP_BAD = MsoJavaProperties.class.getClassLoader().getResource("mso-bad.json")
			.toString().substring(5);

	@BeforeClass
	public static final void prepareBeforeAllTests() {
		msoPropertiesFactory.removeAllMsoProperties();
	}
	/**
	 * This method is called before any test occurs. It creates a fake tree from
	 * scratch
	 *
	 * @throws MsoPropertiesException
	 */
	@Before
	public final void prepareBeforeEachTest() throws MsoPropertiesException {
	    
		msoPropertiesFactory.initializeMsoProperties(MSO_JAVA_PROP_ID, PATH_MSO_JAVA_PROP1);
		msoPropertiesFactory.initializeMsoProperties(MSO_JSON_PROP_ID, PATH_MSO_JSON_PROP);
	}
	
	@After
	public final void cleanAfterEachTest() throws MsoPropertiesException {
		msoPropertiesFactory.removeAllMsoProperties ();
	}

	@Test 
	public final void testNotRecognizedFile() {
		try {
			msoPropertiesFactory.initializeMsoProperties("BAD_FILE", "new_file.toto");

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Unable to load the MSO properties file because format is not recognized (only .json or .properties): new_file.toto").equals(ep.getMessage()));
		}
	}
	
	@Test
	public final void testDoubleInit() {

		try {
			msoPropertiesFactory.initializeMsoProperties(MSO_JAVA_PROP_ID, PATH_MSO_JAVA_PROP1);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("The factory contains already an instance of this mso properties: "+PATH_MSO_JAVA_PROP1).equals(ep.getMessage()));
		}


	}

	/**
	 * This method implements a test for the getMsoJavaProperties method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetMsoJavaProperties() throws MsoPropertiesException {
		assertNotNull(msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID));
		assertTrue(msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID).size()==8);
		
		try {
			msoPropertiesFactory.getMsoJavaProperties(MSO_JSON_PROP_ID);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties is not JAVA_PROP properties type:" + MSO_JSON_PROP_ID).equals(ep.getMessage()));
		}
		
		try {
			msoPropertiesFactory.getMsoJavaProperties("DUMB_PROP");

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+"DUMB_PROP").equals(ep.getMessage()));
		}

	}

	/**
	 * This method test the MsoJavaProperties Set, equals and hascode
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testSetMsoJavaProperties() throws MsoPropertiesException  {
		MsoJavaProperties msoPropChanged = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		msoPropChanged.setProperty("testos", "testos");
		assertNotNull(msoPropChanged.getProperty("testos", null));
						
		// Check no modification occurred on cache one
		MsoJavaProperties msoPropCache = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		assertNull(msoPropCache.getProperty("testos", null));
		assertFalse(msoPropChanged.hashCode() != msoPropCache.hashCode());
		
		assertFalse(msoPropChanged.equals(null));
		assertFalse(msoPropChanged.equals(msoPropCache));
		assertFalse(msoPropChanged.equals(Boolean.TRUE));
		
		assertTrue(msoPropChanged.equals(msoPropChanged));
	}
	
	
	/**
	 * This method implements a test for the testGetMsoJsonProperties method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetMsoJsonProperties() throws MsoPropertiesException {
		assertNotNull(msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID));

		try {
			msoPropertiesFactory.getMsoJsonProperties(MSO_JAVA_PROP_ID);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties is not JSON_PROP properties type:" + MSO_JAVA_PROP_ID).equals(ep.getMessage()));
		}
		
		try {
			msoPropertiesFactory.getMsoJsonProperties("DUMB_PROP");

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+"DUMB_PROP").equals(ep.getMessage()));
		}

	}
	
	/**
	 * This method implements a test for the testGetAllMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetAllMsoProperties() throws MsoPropertiesException {
		assertNotNull(msoPropertiesFactory.getAllMsoProperties().size()==2);

	}

	/**
	 * This method implements a test for the testGetAllMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testToString() throws MsoPropertiesException {
		String dump = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID).toString();
		assertTrue(dump != null && !dump.isEmpty());
		
		dump = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID).toString();
		assertTrue(dump != null && !dump.isEmpty());

	}
	
	/**
	 * This method implements a test for the getProperty of JAVA_PROP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetProperties() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);

		String property1 = msoProperties.getProperty("ecomp.mso.cloud.1.cloudId", "defaultValue");
		String property2 = msoProperties.getProperty("ecomp.mso.cloud.1.keystoneUrl", "defaultValue");
		String property3 = msoProperties.getProperty("ecomp.mso.cloud.1.msoId", "defaultValue");
		String property4 = msoProperties.getProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue");
		String property5 = msoProperties.getProperty("does.not.exist", "defaultValue");
		String property6 = msoProperties.getProperty("ecomp.mso.cloud.1.test", "defaultValue");
		String property7 = msoProperties.getProperty("ecomp.mso.cloud.1.boolean", "defaultValue");

		assertEquals(property1, "MT");
		assertEquals(property2, "http://localhost:5000/v2.0");
		assertEquals(property3, "John");
		assertEquals(property4, "FD205490A48D48475607C36B9AD902BF");
		assertEquals(property5, "defaultValue");
		assertEquals(property6, "1234");
		assertEquals(property7, "true");
	}

	/**
	 * This method implements a test for the getIntProperty JAVA_RPOP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetIntProperties() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		int property1 = msoProperties.getIntProperty("ecomp.mso.cloud.1.test", 345);
		int property2 = msoProperties.getIntProperty("ecomp.mso.cloud.1.publicNetId", 345);
		int property3 = msoProperties.getIntProperty("does.not.exist", 345);
		assertEquals(property1, 1234);
		assertEquals(property2, 345);
		assertEquals(property3, 345);
	}

	/**
	 * This method implements a test for the getBooleanProperty JAVA_RPOP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetBooleanProperty() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		boolean property1 = msoProperties.getBooleanProperty("ecomp.mso.cloud.1.boolean", false);
		boolean property2 = msoProperties.getBooleanProperty("ecomp.mso.cloud.1.publicNetId", false);
		boolean property3NotThere = msoProperties.getBooleanProperty("ecomp.mso.cloud.1.publicNetIdBad", true);
		
		assertEquals(property1, true);
		assertEquals(property2, false);
		assertEquals(property3NotThere, true);
	}

	/**
	 * This method implements a test for the getEncryptedProperty JAVA_RPOP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetEncryptedProperty() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		String property1 = msoProperties.getEncryptedProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue",
				"aa3871669d893c7fb8abbcda31b88b4f");
		String property2 = msoProperties.getEncryptedProperty("test", "defaultValue",
				"aa3871669d893c7fb8abbcda31b88b4f");

		
		String property3Wrong = msoProperties.getEncryptedProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue",
				"aa3871669d893c7fb8abbcda31b88b4");

		
		assertEquals(property1, "changeme");
		assertEquals(property2, "defaultValue");
		assertEquals(property3Wrong, "defaultValue");
	}
	
	/**
	 * This method implements a test for the getEncryptedProperty JAVA_RPOP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testencryptProperty()  {

		assertTrue("FD205490A48D48475607C36B9AD902BF"
				.contains(msoPropertiesFactory.encryptProperty("changeme", "aa3871669d893c7fb8abbcda31b88b4f").getEntity().toString()));

	
		assertTrue("Invalid AES key length: 15 bytes".contains(msoPropertiesFactory.encryptProperty("changeme", "aa3871669d893c7fb8abbcda31b88b4").getEntity().toString()));

	}
	
	/**
	 * This method implements a test for the getJSON JSON_RPOP type method.
	 *
	 * @throws MsoPropertiesException
	 */
	@Test
	public final void testGetJsonNode() throws MsoPropertiesException {
		MsoJsonProperties msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);
		
		JsonNode propNode = msoProperties.getJsonRootNode();
		assertNotNull(propNode);
		assertFalse(propNode.toString().isEmpty());
		assertTrue(propNode.isContainerNode());
		assertNotNull(propNode.path("asdc-connections").path("asdc-controller1"));
		assertNotNull(propNode.path("asdc-connections").path("asdc-controller2"));
		
	}

	/**
	 * This method implements a test for the reloadMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 *
	 */
	@Test
	public final void testReloadJavaMsoProperties() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);

		// Do some additional test on propertiesHaveChanged method
		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, null));
		
		// Change path with bad one
		try {
			msoPropertiesFactory.changeMsoPropertiesFilePath("DO_NOT_EXIST", PATH_MSO_JAVA_PROP2);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:DO_NOT_EXIST").equals(ep.getMessage()));
		} 
				
		
		// Change path with right one
		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JAVA_PROP_ID, PATH_MSO_JAVA_PROP2);
		assertTrue(PATH_MSO_JAVA_PROP2.equals(msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID).getPropertiesFileName()));
		
		assertTrue(msoPropertiesFactory.reloadMsoProperties());
		assertFalse(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));
		// Do a second time as timer value is set to 2
		assertTrue(msoPropertiesFactory.reloadMsoProperties());
		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));

		// Get the new one
		msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		String property1 = msoProperties.getProperty("ecomp.mso.cloud.1.cloudId", "defaultValue");
		String property2 = msoProperties.getProperty("ecomp.mso.cloud.1.keystoneUrl", "defaultValue");
		String property3 = msoProperties.getProperty("ecomp.mso.cloud.1.msoId", "defaultValue");
		String property4 = msoProperties.getProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue");
		String property5 = msoProperties.getProperty("does.not.exist", "defaultValue");
		String property6 = msoProperties.getProperty("ecomp.mso.cloud.1.test", "defaultValue");
		String property7 = msoProperties.getProperty("ecomp.mso.cloud.1.boolean", "defaultValue");

		assertEquals(property1, "MT2");
		assertEquals(property2, "defaultValue");
		assertEquals(property3, "defaultValue");
		assertEquals(property4, "defaultValue");
		assertEquals(property5, "defaultValue");
		assertEquals(property6, "defaultValue");
		assertEquals(property7, "defaultValue");
		
		// Additional test on propertiesHaveChanged
		msoPropertiesFactory.removeAllMsoProperties();

		// Do some additional test on propertiesHaveChanged method
		try {
			msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, null);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+MSO_JAVA_PROP_ID).equals(ep.getMessage()));
		} 
		
		try {
			msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+MSO_JAVA_PROP_ID).equals(ep.getMessage()));
		} 

	}

	/**
	 * This method implements a test for the reloadMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 *
	 */
	@Test
	public final void testReloadMoreThanAMinuteMsoProperties() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);

		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JAVA_PROP_ID, PATH_MSO_JAVA_PROP2);

		// Simulate 2 minutes
		msoPropertiesFactory.reloadMsoProperties();
		assertFalse(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));
		msoPropertiesFactory.reloadMsoProperties();

		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));

		// Get the new one
		msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		String property1 = msoProperties.getProperty("ecomp.mso.cloud.1.cloudId", "defaultValue");
		String property2 = msoProperties.getProperty("ecomp.mso.cloud.1.keystoneUrl", "defaultValue");
		String property3 = msoProperties.getProperty("ecomp.mso.cloud.1.msoId", "defaultValue");
		String property4 = msoProperties.getProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue");
		String property5 = msoProperties.getProperty("does.not.exist", "defaultValue");
		String property6 = msoProperties.getProperty("ecomp.mso.cloud.1.test", "defaultValue");
		String property7 = msoProperties.getProperty("ecomp.mso.cloud.1.boolean", "defaultValue");

		assertEquals(property1, "MT2");
		assertEquals(property2, "defaultValue");
		assertEquals(property3, "defaultValue");
		assertEquals(property4, "defaultValue");
		assertEquals(property5, "defaultValue");
		assertEquals(property6, "defaultValue");
		assertEquals(property7, "defaultValue");


	}

	/**
	 * This method implements a test for the reloadMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 *
	 */
	@Test
	public final void testReloadBadMsoProperties() throws MsoPropertiesException {
		MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);

		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JAVA_PROP_ID, "file-does-not-exist.properties");
		msoPropertiesFactory.reloadMsoProperties();
		assertFalse(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));
		// Reload it a second time as initial timer parameter was set to 2 
		msoPropertiesFactory.reloadMsoProperties();

		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JAVA_PROP_ID, msoProperties));

		// Get the new one
		msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);
		String property1 = msoProperties.getProperty("ecomp.mso.cloud.1.cloudId", "defaultValue");
		String property2 = msoProperties.getProperty("ecomp.mso.cloud.1.keystoneUrl", "defaultValue");
		String property3 = msoProperties.getProperty("ecomp.mso.cloud.1.msoId", "defaultValue");
		String property4 = msoProperties.getProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue");
		String property5 = msoProperties.getProperty("does.not.exist", "defaultValue");
		String property6 = msoProperties.getProperty("ecomp.mso.cloud.1.test", "defaultValue");
		String property7 = msoProperties.getProperty("ecomp.mso.cloud.1.boolean", "defaultValue");

		assertEquals(property1, "defaultValue");
		assertEquals(property2, "defaultValue");
		assertEquals(property3, "defaultValue");
		assertEquals(property4, "defaultValue");
		assertEquals(property5, "defaultValue");
		assertEquals(property6, "defaultValue");
		assertEquals(property7, "defaultValue");

	}

	/**
	 * This method implements a test for the reloadMsoProperties method.
	 *
	 * @throws MsoPropertiesException
	 *
	 */
	@Test
	public final void testReloadBadMsoJsonProperties() throws MsoPropertiesException {
		// Load a bad JSON file
		MsoJsonProperties msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);

		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JSON_PROP_ID, PATH_MSO_JSON_PROP_BAD);

		msoPropertiesFactory.reloadMsoProperties();
		assertFalse(msoPropertiesFactory.propertiesHaveChanged(MSO_JSON_PROP_ID, msoProperties));
		// Reload it a second time as initial timer parameter was set to 2 
		msoPropertiesFactory.reloadMsoProperties();

		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JSON_PROP_ID, msoProperties));

		// Get the new one
		msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);
		assertNotNull(msoProperties);
		assertNotNull(msoProperties.getJsonRootNode());
		assertTrue(msoProperties.getJsonRootNode().size() == 0);
		
	}
	
	@Test
	public final void testRemoveMsoProperties() throws MsoPropertiesException {
		try {
			msoPropertiesFactory.removeMsoProperties("DUMB_PROP");

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+"DUMB_PROP").equals(ep.getMessage()));
		}

		msoPropertiesFactory.removeMsoProperties(MSO_JAVA_PROP_ID);

		try {
			msoPropertiesFactory.getMsoJavaProperties(MSO_JAVA_PROP_ID);

			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Mso properties not found in cache:"+MSO_JAVA_PROP_ID).equals(ep.getMessage()));
		}

	}
	
	@Test
	public final void testInitializeWithNonExistingPropertiesFile () throws MsoPropertiesException  {
		try {
			msoPropertiesFactory.initializeMsoProperties("NEW_BAD_FILE", "no_file.properties");
			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Unable to load the MSO properties file because it has not been found:no_file.properties").equals(ep.getMessage()));
		}
		
		// empty object should be returned as no config has been loaded, anyway no exception should be raised because the config ID must be loaded in cache
		// This is there for automatic reload attempt
		assertTrue(msoPropertiesFactory.getMsoJavaProperties("NEW_BAD_FILE").size()==0);
	}
	
	
	@Test
	public final void testInitializeWithNonExistingJsonFile () throws MsoPropertiesException  {
		try {
			msoPropertiesFactory.initializeMsoProperties("NEW_BAD_FILE", "no_file.json");
			fail ("MsoPropertiesException should have been raised");
		} catch (MsoPropertiesException ep) {
			assertTrue(("Unable to load the MSO properties file because it has not been found:no_file.json").equals(ep.getMessage()));
		}
		
		// empty object should be returned as no config has been loaded, anyway no exception should be raised because the config ID must be loaded in cache
		// This is there for automatic reload attempt
		assertTrue(msoPropertiesFactory.getMsoJsonProperties("NEW_BAD_FILE").getJsonRootNode()!=null);
		assertTrue("{}".equals(msoPropertiesFactory.getMsoJsonProperties("NEW_BAD_FILE").getJsonRootNode().toString()));
	}
	
	@Test
	public final void testShowProperties() {
		assertTrue(msoPropertiesFactory.showProperties().getEntity().toString().contains("/target/test-classes/mso.json(Timer:2mins)"));
		assertTrue(msoPropertiesFactory.showProperties().getEntity().toString().contains("asdc-controller1"));
		assertTrue(msoPropertiesFactory.showProperties().getEntity().toString().contains("/target/test-classes/mso.properties(Timer:2mins):"));
		assertTrue(msoPropertiesFactory.showProperties().getEntity().toString().contains("ecomp.mso.cloud.1.keystoneUrl"));
		
	}

	@Test 
	public final void testGetEncryptedPropertyJson() throws MsoPropertiesException {
		MsoJsonProperties msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);
		assertTrue("ThePassword".equals(msoProperties.getEncryptedProperty(msoProperties.getJsonRootNode().get("asdc-connections").get("asdc-controller1").get("asdcPassword"),"defautlvalue","566B754875657232314F5548556D3665")));
		
		assertTrue("defautlvalue".equals(msoProperties.getEncryptedProperty(msoProperties.getJsonRootNode().get("asdc-connections").get("asdc-controller1").get("asdcPassword"),"defautlvalue","566B754875657232314F5548556D366")));
		
		
	}
	
	@Test
	public final void testHashcodeAndEqualsMsoJsonProperties() throws MsoPropertiesException {
	
		MsoJsonProperties msoProperties = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);

		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JSON_PROP_ID, PATH_MSO_JSON_PROP2);

		msoPropertiesFactory.reloadMsoProperties();
		assertFalse(msoPropertiesFactory.propertiesHaveChanged(MSO_JSON_PROP_ID, msoProperties));
		// Reload it a second time as initial timer parameter was set to 2 
		msoPropertiesFactory.reloadMsoProperties();
		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JSON_PROP_ID, msoProperties));
		
		// Get the new one
		MsoJsonProperties msoProperties2 = msoPropertiesFactory.getMsoJsonProperties(MSO_JSON_PROP_ID);
		assertFalse(msoProperties.hashCode()==msoProperties2.hashCode());
		
		assertFalse(msoProperties.equals(msoProperties2));
		assertTrue(msoProperties.equals(msoProperties));
		assertFalse(msoProperties.equals(null));
		assertFalse(msoProperties.toString().isEmpty());

		// Test a reload with timer set to 1 in PATH_MSO_JSON_PROP2
		msoPropertiesFactory.changeMsoPropertiesFilePath(MSO_JSON_PROP_ID, PATH_MSO_JSON_PROP);

		msoPropertiesFactory.reloadMsoProperties();
		assertTrue(msoPropertiesFactory.propertiesHaveChanged(MSO_JSON_PROP_ID, msoProperties2));
	
	}
	
}
