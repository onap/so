package org.openecomp.mso.requestsdb;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WatchdogDistributionStatusDbTest {
	
	private static final String distributionId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
	
	@Test
	public void testUpdateWatchdogDistributionIdStatus() {
		WatchdogDistributionStatusDb wdds = Mockito.mock(WatchdogDistributionStatusDb.class);
		
		doNothing().when(wdds).updateWatchdogDistributionIdStatus("ff3514e3-5a33-55df-13ab-12abad84e7ff", "SENT");
		wdds.updateWatchdogDistributionIdStatus(any(String.class), any(String.class));
		verify(wdds, times(1)).updateWatchdogDistributionIdStatus(any(String.class), any(String.class));
	}	
	
	@Test
	public void testInsertWatchdogDistributionId() {
	
		WatchdogDistributionStatusDb wdds = mock(WatchdogDistributionStatusDb.class);
		
		wdds.insertWatchdogDistributionId(distributionId);		
		doNothing().when(wdds).insertWatchdogDistributionId(any(String.class));       
		verify(wdds, times(1)).insertWatchdogDistributionId(any(String.class));
	
	}

	@Test
	public void testGetWatchdogDistributionIdStatus() {
			
		WatchdogDistributionStatusDb wdds = Mockito.mock(WatchdogDistributionStatusDb.class);
		Mockito.when(wdds.getWatchdogDistributionIdStatus("ff305d54-75b4-431b-adb2-eb6b9e5ff001")).thenReturn("ff3514e3-5a33-55df-13ab-12abad84e7ff");
		String actual = wdds.getWatchdogDistributionIdStatus("ff305d54-75b4-431b-adb2-eb6b9e5ff001");
		assertEquals(actual, distributionId);
		verify(wdds, times(1)).getWatchdogDistributionIdStatus(any(String.class));
	}
	
}
