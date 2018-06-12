package org.openecomp.mso.asdc.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.asdc.BaseTest;
import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ASDCControllerTest extends BaseTest {
	@Autowired
	private ASDCController asdcController;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void initASDCExceptionTest() throws Exception {
		expectedException.expect(ASDCControllerException.class);
		
		asdcController.changeControllerStatus(ASDCControllerStatus.IDLE);
		
		try {
			asdcController.initASDC();
		} finally {
			asdcController.closeASDC();
		}
	}
}
