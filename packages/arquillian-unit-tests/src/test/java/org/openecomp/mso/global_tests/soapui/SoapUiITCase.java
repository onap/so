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

package org.openecomp.mso.global_tests.soapui;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openecomp.mso.global_tests.ArquillianPackagerForITCases;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.tools.SoapUITestCaseRunner;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoapUiITCase {

    private static String jbossHost = System.getProperty("docker.hostname");
    private static String jbossPort = "18080";

    @Deployment(name = "mso-api-handler-infra", testable = false)
    public static Archive<?> createMsoApiHandlerInfraWarDeployment() {
        System.out.println("Deploying ApiHandler Infra WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../mso-api-handlers/mso-api-handler-infra/target/", "mso-api-handler-infra*.war", "mso-api-handler-infra.war");
    }

    @Deployment(name = "mso-vnf-adapter", testable = false)
    public static Archive<?> createMsoVnfAdapterWarDeployment() {
        System.out.println("Deploying VNF Adapter WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-vnf-adapter/target/", "mso-vnf-adapter*.war", "mso-vnf-adapter.war");
    }

    @Deployment(name = "mso-tenant-adapter", testable = false)
    public static Archive<?> createMsoTenantAdapterWarDeployment() {
        System.out.println("Deploying Tenant Adapter WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-tenant-adapter/target/", "mso-tenant-adapter*.war", "mso-tenant-adapter.war");
    }

    @Deployment(name = "mso-sdnc-adapter", testable = false)
    public static Archive<?> createMsoSdncAdapterWarDeployment() {
        System.out.println("Deploying SDNC Adapter WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-sdnc-adapter/target/", "mso-sdnc-adapter*.war", "mso-sdnc-adapter.war");
    }

    @Deployment(name = "mso-network-adapter", testable = false)
    public static Archive<?> createMsoNetworkAdapterWarDeployment() {
        System.out.println("Deploying Network Adapter WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-network-adapter/target/", "mso-network-adapter*.war", "mso-network-adapter.war");
    }

    @Deployment(name = "mso-requests-db-adapter", testable = false)
    public static Archive<?> createMsoRequestsDbAdapterWarDeployment() {
        System.out.println("Deploying Requests DB Adapter WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../adapters/mso-requests-db-adapter/target/", "mso-requests-db-adapter*.war", "mso-requests-db-adapter.war");
    }

    @Deployment(name = "asdc-controller", testable = true)
    public static Archive<?> createAsdcControllerWarDeployment() {
        System.out.println("Deploying ASDC Controller WAR with additional resources on default server");

        WebArchive warArchive = (WebArchive) ArquillianPackagerForITCases.createPackageFromExistingOne("../../asdc-controller/target/", "asdc-controller*.war", "asdc-controller.war");

        // Take one war randomly to make arquilian happy


        return warArchive;
    }

    @Deployment(name = "infrastructure-bpmn", testable = false)
    public static Archive<?> createInfraBPMNDeployment() {
        System.out.println("Deploying Infrastructure BPMN WAR on default server");
        return ArquillianPackagerForITCases.createPackageFromExistingOne("../../bpmn/MSOInfrastructureBPMN/target/",
                "MSOInfrastructureBPMN*.war", "MSOInfrastructureBPMN.war");
    }
/*
    @Deployment(name = "SoapUIMocks", testable = false)
    public static Archive <?> createSoapUIMocksWarDeployment () {

        File file = new File ("src/test/resources/SoapUIMocks.war");

        WebArchive archive = ShrinkWrap.create (WebArchive.class, "SoapUIMocks.war");

        archive.merge ((ShrinkWrap.createFromZipFile (WebArchive.class, file)));

        return archive;
    }*/

    @BeforeClass
    public static void waitBeforeStart() throws InterruptedException {
        Thread.currentThread().sleep(10000);
        System.out.println("Executing " + SoapUiITCase.class.getName());

    }

    @Test
    @RunAsClient
    public void test01Healthcheck() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/Healthcheck-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[7];
        properties[0] = "apihhost=" + jbossHost + ":" + jbossPort;
        properties[1] = "jrahost=" + jbossHost + ":" + jbossPort;
        properties[2] = "userlogin=sitecontrol";
        properties[3] = "userpassword=Domain2.0!";
        properties[4] = "bpmnhost=" + jbossHost + ":" + jbossPort;
        properties[5] = "sitename=mso-docker";
        properties[6] = "enableBpmn=false";
        runner.setProjectProperties(properties);

        try {
            runner.setTestSuite("Healthcheck TestSuite");
            runner.run();
            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI Healthcheck");
        }
    }

    @Test
    @RunAsClient
    public void test02ApiHandlerInfra() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/Local-API-Handler-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[3];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        properties[1] = "user-infraportal=InfraPortalClient";
        properties[2] = "password-infraportal=password1$";

        runner.setProjectProperties(properties);

        try {
            runner.setTestSuite("simple_tests_endpoints");
            runner.run();
            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI ApiHandler Infra");
        }
    }

    @Test
    @RunAsClient
    public void test03StartNetworkAdapter() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/MSONetworkAdapter-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[1];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        runner.setProjectProperties(properties);


        try {
            runner.setTestSuite("MsoNetworkAdapter TestSuite");
            runner.run();

            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI NetworkAdapter");
        }
    }


    @Test
    @RunAsClient
    public void test04StartVnfAdapter() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/MSOVnfAdapter-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[1];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        runner.setProjectProperties(properties);

        try {
            runner.setTestSuite("MsoVnfAdapter TestSuite");
            runner.run();

            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI VnfAdapter");
        }
    }


    @Test
    @RunAsClient
    public void test05StartTenantAdapter() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/MSOTenantAdapter-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[3];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        properties[1] = "user-bpel=BPELClient";
        properties[2] = "password-bpel=password1$";
        runner.setProjectProperties(properties);

        try {
            runner.setTestSuite("MsoTenantAdapter TestSuite");
            runner.run();

            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI TenantAdapter");
        }
    }


    @Test
    @RunAsClient
    public void test06StartRequestDBAdapter() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/MsoRequestDB-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[3];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        properties[1] = "user-infraportal=InfraPortalClient";
        properties[2] = "password-infraportal=password1$";
        runner.setProjectProperties(properties);


        try {
            runner.setTestSuite("MsoRequestsDbAdapterImplPortBinding TestSuite");
            runner.run();

            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI RequestDB adapter");
        }
    }

    @Test
    @RunAsClient
    public void test07MsoConfigEndpoints() {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setSettingsFile("./src/test/resources/SoapUI/soapui-settings.xml");
        runner.setJUnitReport(true);
        runner.setProjectFile("./src/test/resources/SoapUI/MSOConfig-soapui-project.xml");
        runner.setOutputFolder("./target/surefire-reports");
        String[] properties = new String[3];
        properties[0] = "host=" + jbossHost + ":" + jbossPort;
        properties[1] = "user-infraportal=InfraPortalClient";
        properties[2] = "password-infraportal=password1$";
        runner.setProjectProperties(properties);


        try {
            runner.setTestSuite("test_config_endpoints TestSuite");
            runner.run();

            Map<TestAssertion, WsdlTestStepResult> mapResult = runner.getAssertionResults();
            for (Map.Entry<TestAssertion, WsdlTestStepResult> entry : mapResult.entrySet()) {
                assertTrue(entry.getValue().getStatus().equals(TestStepStatus.OK));
            }
            assertTrue(runner.getFailedTests().size() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failure in SOAPUI MSOConfig Endpoints");
        }
    }
}
