package org.openecomp.mso.cloud;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CloudifyManagerTest {
	
	private CloudifyManager cloudifyManager = new CloudifyManager();
	private static final String ID = "testId";
	private static final String CLOUDIFY_URL = "testCloudifyUrl";
	private static final String USERNAME = "testUsername";
	private static final String PASSWORD = "testPassword";
	private static final String VERSION = "testVersion";
	
	@Test
	public void cloneTest() {
		cloudifyManager.setId(ID);
		cloudifyManager.setCloudifyUrl(CLOUDIFY_URL);
		cloudifyManager.setUsername(USERNAME);
		cloudifyManager.setPassword(PASSWORD);
		cloudifyManager.setVersion(VERSION);
		
		CloudifyManager clone = cloudifyManager.clone();
		assertEquals(cloudifyManager, clone);
	}
}