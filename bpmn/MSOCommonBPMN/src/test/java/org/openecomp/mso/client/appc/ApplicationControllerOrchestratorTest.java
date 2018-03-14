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

import java.util.Optional;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.Status;

public class ApplicationControllerOrchestratorTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	@Ignore
	public void vnfCommandTest() {
		ApplicationControllerOrchestrator client = new ApplicationControllerOrchestrator();
		Status status;
		try {
			status = client.vnfCommand(Action.Lock, UUID.randomUUID().toString(),
					"3ffdee3c-94d2-45fe-904d-fc1efa0f8b59", Optional.of(""));
		} catch (ApplicationControllerOrchestratorException e) {
			status = new Status();
			status.setCode(e.getAppcCode());
			status.setMessage(e.getMessage());
		}
		assertEquals("Status of vnfCommand is correct", status.getCode(), 306);
	}

	@Test
	@Ignore
	public void vnfCommandTest1() {
		ApplicationControllerOrchestrator client = new ApplicationControllerOrchestrator();
		Status status;
		try {
			status = client.vnfCommand(Action.Unlock, UUID.randomUUID().toString(),
					"ca522254-2ba4-4fbd-b15b-0ef0d9cfda5f", Optional.of(""));
		} catch (ApplicationControllerOrchestratorException e) {
			status = new Status();
			status.setCode(e.getAppcCode());
			status.setMessage(e.getMessage());
		}
		assertEquals("Status of vnfCommand is correct", status.getCode(), 306);
	}
}