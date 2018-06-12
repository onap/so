package org.openecomp.mso.asdc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.asdc.client.test.emulators.NotificationDataImpl;

public class NotificationLoggingTest {
	private NotificationDataImpl notificationData;
	
	@Before
	public void before() {		
		notificationData = new NotificationDataImpl();
	}
	
	@Test
	public void logNotificationTest() {
		notificationData.setDistributionID("distributionID");
		notificationData.setServiceVersion("123456");
		notificationData.setServiceUUID("serviceUUID");
		notificationData.setWorkloadContext("workloadContext");
		
		String response = NotificationLogging.logNotification(notificationData);

		assertTrue(response.contains("ASDC Notification"));
		assertTrue(response.contains("ResourcesType not recognized"));
		assertTrue(response.contains("ServiceNameNULL"));
		assertTrue(response.contains("ServiceUUIDserviceUUID"));
		assertTrue(response.contains("ResourcesImplNULL"));
		assertTrue(response.contains("ServiceArtifactsType not recognized"));
		assertTrue(response.contains("ServiceDescriptionNULL"));
		assertTrue(response.contains("DistributionIDdistributionID"));
		assertTrue(response.contains("ServiceInvariantUUIDNULL"));
		assertTrue(response.contains("WorkloadContextworkloadContext"));
	}
	
	@Test
	public void logNotificationNullTest() {
		notificationData = null;
		
		String response = NotificationLogging.logNotification(notificationData);
		
		assertEquals("NULL", response);
	}
}
