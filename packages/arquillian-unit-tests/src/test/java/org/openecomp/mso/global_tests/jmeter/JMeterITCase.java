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

package org.openecomp.mso.global_tests.jmeter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openecomp.mso.global_tests.ArquillianPackagerForITCases;

@RunWith(Arquillian.class)
public class JMeterITCase {
	
	@Deployment(name="mso-api-handler-infra",testable=false)
	public static Archive<?> createMsoApiHandlerInfraWarDeployment () {
		System.out.println("Deploying ApiHandler Infra WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../mso-api-handlers/mso-api-handler-infra/target/", "mso-api-handler-infra*.war", "mso-api-handler-infra.war");
	}
	
	@Deployment(name="mso-vnf-adapter",testable=false)
	public static Archive<?> createMsoVnfAdapterWarDeployment () {
		System.out.println("Deploying VNF Adapter WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-vnf-adapter/target/", "mso-vnf-adapter*.war", "mso-vnf-adapter.war");
	}
	
	@Deployment(name="mso-tenant-adapter",testable=false)
	public static Archive<?> createMsoTenantAdapterWarDeployment () {
		System.out.println("Deploying Tenant Adapter WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-tenant-adapter/target/", "mso-tenant-adapter*.war", "mso-tenant-adapter.war");
	}
	
	@Deployment(name="mso-sdnc-adapter",testable=false)
	public static Archive<?> createMsoSdncAdapterWarDeployment () {
		System.out.println("Deploying SDNC Adapter WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-sdnc-adapter/target/", "mso-sdnc-adapter*.war", "mso-sdnc-adapter.war");
	}
	
	@Deployment(name="mso-network-adapter",testable=false)
	public static Archive<?> createMsoNetworkAdapterWarDeployment () {
		System.out.println("Deploying Network Adapter WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-network-adapter/target/", "mso-network-adapter*.war", "mso-network-adapter.war");
	}
	
	@Deployment(name="mso-requests-db-adapter",testable=false)
	public static Archive<?> createMsoRequestsDbAdapterWarDeployment () {
		System.out.println("Deploying Requests DB Adapter WAR on default server");
		return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-requests-db-adapter/target/", "mso-requests-db-adapter*.war", "mso-requests-db-adapter.war");
	}
	
	@Deployment(name="asdc-controller",testable=true)
	public static Archive<?> createAsdcControllerWarDeployment () {
		System.out.println("Deploying ASDC Controller WAR with additional resources on default server");
		
		WebArchive warArchive = (WebArchive)ArquillianPackagerForITCases.createPackageFromExistingOne("../../asdc-controller/target/", "asdc-controller*.war", "asdc-controller.war");
		
		// Add the current test class
        JavaArchive testclasses = ShrinkWrap.create (JavaArchive.class, "testClasses.jar");
		testclasses.addClasses(JMeterITCase.class);
				
		warArchive.addAsLibraries(testclasses);
				
		// BE CAREFUL a settings.xml file must be located in ${home.user}/.m2/settings.xml
		warArchive.addAsLibraries(Maven.resolver()
				.resolve("org.mockito:mockito-all:1.10.19")
                                        .withoutTransitivity ()
                                        .asFile ());

		// Take one war randomly to make arquilian happy

		Testable.archiveToTest(warArchive);

		
		return warArchive;
	}
	
  
    @BeforeClass
    public static void waitBeforeStart () throws InterruptedException {
        System.out.println ("Executing " + JMeterITCase.class.getName ());
 
    }

    @Test
	@RunAsClient()
	public void testJMeter() throws IOException  {
		  // JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();


        // Initialize Properties, logging, locale, etc.
        JMeterUtils.loadJMeterProperties("/tmp/apache-jmeter-2.13/bin/jmeter.properties");
        JMeterUtils.setJMeterHome("/tmp/apache-jmeter-2.13");
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();
        
        // Initialize JMeter SaveService
        SaveService.loadProperties();

        // Load existing .jmx Test Plan
    
        FileInputStream in = new FileInputStream("./src/test/resources/JMeter/MSO-Perf.jmx");
        HashTree testPlanTree = SaveService.loadTree(in);
        testPlanTree.getTree("test variables");
        in.close();

        // Run JMeter Test
        jmeter.configure(testPlanTree);
        jmeter.run();
	}

    @AfterClass
    public static void afterArquillianTest() {
    	try {
			Files.move (Paths.get ("./jmeter.log"),
					Paths.get ("./target/surefire-reports/jmeter.log"),
					StandardCopyOption.REPLACE_EXISTING);
			
		/*	Files.move (Paths.get ("./mso-perf.jtl"),
					Paths.get ("./target/surefire-reports/mso-perf.log"),
					StandardCopyOption.REPLACE_EXISTING);*/

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
