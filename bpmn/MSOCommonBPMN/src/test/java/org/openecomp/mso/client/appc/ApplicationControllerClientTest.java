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

package org.openecomp.mso.client.appc;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.CheckLockInput;
import org.onap.appc.client.lcm.model.Status;

public class ApplicationControllerClientTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	public void createRequest_CheckLock_RequestBuilt() {
		ApplicationControllerClient client = new ApplicationControllerClient();
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId("vnfId");
		CheckLockInput checkLockInput = (CheckLockInput) client.createRequest(Action.CheckLock, actionIdentifiers, null,
				"requestId");
		assertEquals(checkLockInput.getAction().name(), "CheckLock");
	}

	@Test
	public void runCommand_liveAppc() {
		ApplicationControllerClient client = new ApplicationControllerClient();
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		//actionIdentifiers.setVnfId("ca522254-2ba4-4fbd-b15b-0ef0d9cfda5f");
		actionIdentifiers.setVnfId("2d2bf10e-81a5-");
		Status status;
		try {
			status = client.runCommand(Action.Lock, actionIdentifiers, null, UUID.randomUUID().toString());
		} catch (ApplicationControllerOrchestratorException e) {
			status = new Status();
			status.setCode(e.getAppcCode());
			status.setMessage(e.getMessage());
		}
		assertEquals("Status of run command is correct", status.getCode(), 306);
	}

	@Test
	public void runCommand_CheckLock_RequestBuilt() {
		ApplicationControllerClient client = new ApplicationControllerClient();
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

	@Test
	public void test_getLCMPropertiesHelper() {
		ApplicationControllerClient client = new ApplicationControllerClient();
		Properties properties = client.getLCMProperties();
		assertEquals(properties.get("topic.write"), "APPC-TEST-AMDOCS1-DEV3");
		assertEquals(properties.get("topic.read.timeout"), "120000");
		assertEquals(properties.get("client.response.timeout"), "120000");
		assertEquals(properties.get("topic.read"), "APPC-TEST-AMDOCS2");
		assertEquals(properties.get("poolMembers"),
				"uebsb93kcdc.it.att.com:3904,uebsb92kcdc.it.att.com:3904,uebsb91kcdc.it.att.com:3904");
		assertEquals(properties.get("client.key"), "iaEMAfjsVsZnraBP");
		assertEquals(properties.get("client.secret"), "wcivUjsjXzmGFBfxMmyJu9dz");
	}

}