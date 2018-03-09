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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openecomp.appc.client.lcm.model.Action;
import org.openecomp.appc.client.lcm.model.ActionIdentifiers;
import org.openecomp.appc.client.lcm.model.CheckLockInput;
import org.openecomp.appc.client.lcm.model.Flags;
import org.openecomp.appc.client.lcm.model.Status;

public class ApplicationControllerClientTest {

	private static ApplicationControllerClient client;
	private static ApplicationControllerSupport support;

	@BeforeClass
	public static void beforeClass() {
		client = new ApplicationControllerClient();
		support = new ApplicationControllerSupport();
		client.appCSupport = support;
		System.setProperty("mso.config.path", "src/test/resources");

	}

	@AfterClass
	public static void afterClass() throws Exception {
		client.shutdownclient();
	}

	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void createRequest_CheckLock_RequestBuilt() throws Exception {

		org.springframework.test.util.ReflectionTestUtils.setField(support, "lcmModelPackage",
				"org.openecomp.appc.client.lcm.model");
		Flags flags = new Flags();
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId("vnfId");
		CheckLockInput checkLockInput = (CheckLockInput) client.createRequest(Action.CheckLock, actionIdentifiers,
				flags, null, "requestId");
		assertEquals(checkLockInput.getAction().name(), "CheckLock");
	}

	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void runCommand_liveAppc() throws Exception {
		org.springframework.test.util.ReflectionTestUtils.setField(support, "lcmModelPackage",
				"org.openecomp.appc.client.lcm.model");
		Flags flags = new Flags();
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId("ca522254-2ba4-4fbd-b15b-0ef0d9cfda5f");

		// CheckLockInput checkLockInput = (CheckLockInput)
		// client.createRequest(Action.CheckLock,actionIdentifiers,flags,null,"requestId");
		Status status = client.runCommand(Action.Lock, actionIdentifiers, flags, null, UUID.randomUUID().toString());
		assertEquals("Status of run command is correct", status.getCode(), 306);
	}

	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void runCommand_CheckLock_RequestBuilt() throws Exception {
		org.springframework.test.util.ReflectionTestUtils.setField(support, "lcmModelPackage",
				"org.openecomp.appc.client.lcm.model");
		Flags flags = new Flags();
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId("fusion-vpp-vnf-001");
		Status status = client.runCommand(Action.CheckLock, actionIdentifiers, flags, null, "requestId");
		assertEquals("Status of run command is correct", status.getCode(), 400);
	}
}
