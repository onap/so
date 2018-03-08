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

package org.openecomp.mso.global_tests.logging;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openecomp.mso.filesearching.LogFileSearching;
import org.openecomp.mso.global_tests.ArquillianPackagerForITCases;

@RunWith(Arquillian.class)
public class LogsCheckerITCase {


    @BeforeClass
    public static void waitBeforeStart() throws InterruptedException {
        System.out.println("Executing " + LogsCheckerITCase.class.getName());

    }

    @Deployment(name = "log-check", testable = true)
    public static Archive<?> createAsdcControllerWarDeployment() throws Exception {
        // Any war could be used here, we just take that one randomly
        // Be careful some WAR does not work when being injected in JBOSS, probably due to Servlet conflict
        System.out.println("Deploying ASDC Controller WAR for log checker");
        WebArchive warArchive = (WebArchive) ArquillianPackagerForITCases.createPackageFromExistingOne("../../asdc-controller/target/", "asdc-controller*.war", "asdc-controller.war");

        JavaArchive testclasses = ShrinkWrap.create(JavaArchive.class, "testClasses.jar");

        testclasses.addPackage("org.openecomp.mso.filesearching");

        warArchive.addAsLibraries(testclasses);

        Testable.archiveToTest(warArchive);
        return warArchive;
    }

    @Before
    public void beforeEachTest() {
        LogFileSearching.initFile("/tmp/mso-log-checker.log");
    }

    @After
    public void afterEachTest() {
        LogFileSearching.closeFile();
    }

    @Test
    @OperateOnDeployment("log-check")
    public void testJbossServerLog() throws IOException {

        File serverLogs = new File("/opt/jboss/standalone/log");
        //File serverLogs = new File("/tmp/jbosslogs/server.log");

        assertFalse(LogFileSearching.searchInDirectoryForCommonIssues(null, serverLogs));

    }

    @Test
    @OperateOnDeployment("log-check")
    public void testMSOLog() throws IOException {
        //File serverLogs = new File("/opt/app/mso/jboss-eap-6.2/standalone/log/server.log");
        File msoLogs = new File("/var/log/ecomp/MSO");

        assertFalse(LogFileSearching.searchInDirectoryForCommonIssues(null, msoLogs));

    }


}
