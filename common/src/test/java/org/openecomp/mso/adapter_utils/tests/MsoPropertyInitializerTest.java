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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.properties.MsoPropertyInitializer;

public class MsoPropertyInitializerTest {

	public static final String ASDC_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.json").toString().substring(5);
	public static ServletContextEvent servletContextEvent = Mockito.mock(ServletContextEvent.class);
	public static ServletContext servletContext = Mockito.mock(ServletContext.class);
    public MsoPropertyInitializer msoPropInitializer = new MsoPropertyInitializer();
	    
	@BeforeClass
	public static final void prepareBeforeClass() throws MsoPropertiesException {

		Mockito.when(servletContextEvent.getServletContext()).thenReturn(servletContext);
	}
	
	@Before
	public final void preparebeforeEachTest() throws MsoPropertiesException {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
	
	}
		
	@Test
	public void testContextInitialized() throws MsoPropertiesException {
		Mockito.when(servletContext.getInitParameter("mso.configuration")).thenReturn("MSO_PROP_ASDC="+ASDC_PROP);
		msoPropInitializer.contextInitialized(servletContextEvent);
		
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		assertNotNull(msoPropertiesFactory.getMsoJsonProperties("MSO_PROP_ASDC"));
		assertFalse("{}".equals(msoPropertiesFactory.getMsoJsonProperties("MSO_PROP_ASDC").getJsonRootNode().toString()));
		assertTrue(msoPropertiesFactory.getMsoJsonProperties("MSO_PROP_ASDC").getJsonRootNode().get("asdc-connections")!= null);
	}
	
	@Test
	public void testContextInitializedFailure() throws MsoPropertiesException {
		Mockito.when(servletContext.getInitParameter("mso.configuration")).thenReturn("MSO_PROP_ASDC="+"Does_not_exist.json");
		msoPropInitializer.contextInitialized(servletContextEvent);
		
		// No exception should be raised, log instead
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
				
		assertTrue("{}".equals(msoPropertiesFactory.getMsoJsonProperties("MSO_PROP_ASDC").getJsonRootNode().toString()));
		assertTrue(msoPropertiesFactory.getMsoJsonProperties("MSO_PROP_ASDC").getJsonRootNode().get("asdc-connections")== null);
		
		
	
	}
	
}
