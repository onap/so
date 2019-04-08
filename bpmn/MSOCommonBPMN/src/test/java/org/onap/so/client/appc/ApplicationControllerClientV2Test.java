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

package org.onap.so.client.appc;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.CheckLockInput;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.BaseTest;
import java.util.Properties;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationControllerClientV2Test {

    @Mock
    ApplicationControllerSupport applicationControllerSupport;

    @Mock
    ApplicationControllerConfiguration applicationControllerConfiguration;

    @InjectMocks
    ApplicationControllerClientV2 client;


    @Before
    public void setup() {
        when(applicationControllerConfiguration.getReadTopic()).thenReturn("APPC-TEST-AMDOCS2");
        when(applicationControllerConfiguration.getReadTimeout()).thenReturn("120000");
        when(applicationControllerConfiguration.getResponseTimeout()).thenReturn("120000");
        when(applicationControllerConfiguration.getWrite()).thenReturn("APPC-TEST-AMDOCS1-DEV3");
        when(applicationControllerConfiguration.getService()).thenReturn("ueb");
        when(applicationControllerConfiguration.getPoolMembers())
                .thenReturn("localhost:3904,localhost:3904,localhost:3904");
        when(applicationControllerConfiguration.getClientKey()).thenReturn("iaEMAfjsVsZnraBP");
        when(applicationControllerConfiguration.getClientSecret()).thenReturn("wcivUjsjXzmGFBfxMmyJu9dz");
        // client.buildClient();
    }

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    @Ignore
    @Test
    public void createRequest_CheckLock_RequestBuilt() {
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId("vnfId");
        // when(applicationControllerSupport.getInput(eq(Action.CheckLock.name()))).thenReturn(new CheckLockInput());
        CheckLockInput checkLockInput =
                (CheckLockInput) client.createRequest(Action.CheckLock, actionIdentifiers, null, "requestId");
        assertEquals(checkLockInput.getAction().name(), "CheckLock");
    }

    @Ignore
    @Test
    public void runCommand_liveAppc() {
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        // actionIdentifiers.setVnfId("ca522254-2ba4-4fbd-b15b-0ef0d9cfda5f");
        actionIdentifiers.setVnfId("2d2bf10e-81a5-");
        Status status;
        // when(applicationControllerSupport.getInput(eq(Action.Lock.name()))).thenReturn(new LockInput());
        // when(applicationControllerSupport.getAPIMethod(anyString(),any(),anyBoolean())).thenCallRealMethod();
        try {
            status = client.runCommand(Action.Lock, actionIdentifiers, null, UUID.randomUUID().toString());
        } catch (ApplicationControllerOrchestratorException e) {
            status = new Status();
            status.setCode(e.getAppcCode());
            status.setMessage(e.getMessage());
        }
        assertEquals("Status of run command is correct", status.getCode(), 306);
    }

    @Ignore
    @Test
    public void runCommand_CheckLock_RequestBuilt() {
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId("fusion-vpp-vnf-001");
        Status status;
        try {
            status = client.runCommand(Action.Unlock, actionIdentifiers, null, "requestId");
        } catch (ApplicationControllerOrchestratorException e) {
            status = new Status();
            status.setCode(e.getAppcCode());
            status.setMessage(e.getMessage());
        }
        assertEquals("Status of run command is correct", status.getCode(), 309);
    }

    @Ignore
    @Test
    public void test_getLCMPropertiesHelper() {
        Properties properties = client.getLCMProperties("");
        assertEquals(properties.get("topic.write"), "APPC-TEST-AMDOCS1-DEV3");
        assertEquals(properties.get("topic.read.timeout"), "120000");
        assertEquals(properties.get("client.response.timeout"), "120000");
        assertEquals(properties.get("topic.read"), "APPC-TEST-AMDOCS2");
        assertEquals(properties.get("poolMembers"), "localhost:3904,localhost:3904,localhost:3904");
        assertEquals(properties.get("client.key"), "iaEMAfjsVsZnraBP");
        assertEquals(properties.get("client.secret"), "wcivUjsjXzmGFBfxMmyJu9dz");
    }

}
