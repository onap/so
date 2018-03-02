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