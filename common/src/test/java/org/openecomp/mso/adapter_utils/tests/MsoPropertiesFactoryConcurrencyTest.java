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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.mso.properties.AbstractMsoProperties;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * This class implements test methods of the MsoPropertiesFactory features.
 *
 *
 */
public class MsoPropertiesFactoryConcurrencyTest {

	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	public static final String MSO_PROP_ID = "TEST_PROP";
	public static final String PATH_MSO_PROP1 = MsoJavaProperties.class.getClassLoader().getResource("mso.properties")
			.toString().substring(5);
	public static final String PATH_MSO_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.properties")
			.toString().substring(5);

	/**
	 * This method is called before any test occurs. It creates a fake tree from
	 * scratch
	 *
	 * @throws MsoPropertiesException
	 */
	@BeforeClass
	public static final void prepare() throws MsoPropertiesException {
		// it's possible to have it already initialized, as tests are executed in the same JVM
	    msoPropertiesFactory.removeAllMsoProperties ();
		msoPropertiesFactory.initializeMsoProperties(MSO_PROP_ID, PATH_MSO_PROP1);
	}

	private Callable<Integer> taskReload = () -> {
        try {
            if (!msoPropertiesFactory.reloadMsoProperties()) {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace ();
            return 1;
        }
        return 0;
    };

	private Callable<Integer> taskRead = () -> {
        try {
            MsoJavaProperties msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_ID);
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

        } catch (MsoPropertiesException e) {
e.printStackTrace ();
            return 1;
        }
        return 0;
    };

	private Callable<Integer> taskReadAll = () -> {
        try {
            List<AbstractMsoProperties> msoPropertiesList =  msoPropertiesFactory.getAllMsoProperties();
            String property1 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.cloudId", "defaultValue");
            String property2 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.keystoneUrl", "defaultValue");
            String property3 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.msoId", "defaultValue");
            String property4 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.publicNetId", "defaultValue");
            String property5 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("does.not.exist", "defaultValue");
            String property6 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.test", "defaultValue");
            String property7 = ((MsoJavaProperties)msoPropertiesList.get(0)).getProperty("ecomp.mso.cloud.1.boolean", "defaultValue");

            assertEquals(property1, "MT");
            assertEquals(property2, "http://localhost:5000/v2.0");
            assertEquals(property3, "John");
            assertEquals(property4, "FD205490A48D48475607C36B9AD902BF");
            assertEquals(property5, "defaultValue");
            assertEquals(property6, "1234");
            assertEquals(property7, "true");
        } catch (Exception e) {
e.printStackTrace ();
            return 1;
        }
        return 0;
    };

	@Test
	public final void testGetMsoProperties()
			throws MsoPropertiesException, InterruptedException, ExecutionException, FileNotFoundException {

		List<Future<Integer>> list = new ArrayList<Future<Integer>>();
		ExecutorService executor = Executors.newFixedThreadPool(20);

		for (int i = 0; i <= 100000; i++) {

			Future<Integer> futureResult = executor.submit(taskRead);
			list.add(futureResult);

			futureResult = executor.submit(taskReload);
			list.add(futureResult);

			futureResult = executor.submit(taskReadAll);
			list.add(futureResult);
		}
		executor.shutdown();
		while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            ;
        }

		for (Future<Integer> result : list) {
			assertTrue(result.get().equals(0));
		}

	}

}
