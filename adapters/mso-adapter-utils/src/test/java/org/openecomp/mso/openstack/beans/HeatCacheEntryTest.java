package org.openecomp.mso.openstack.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

public class HeatCacheEntryTest extends BaseTest {
	
	private static final String HEAT_URL = "testHeatUrl";
	private static final String TOKEN = "testToken";

	@Test
	public void getHeatClientTest() {
		Calendar expires = new GregorianCalendar(2013,0,31);
		HeatCacheEntry heatCacheEntry = new HeatCacheEntry(HEAT_URL, TOKEN, expires);
		assertNotNull(heatCacheEntry.getHeatClient());
	}
	
	@Test
	public void isExpiredTrueTest() {
		Calendar expires = new GregorianCalendar(2013,0,31);
		HeatCacheEntry heatCacheEntry = new HeatCacheEntry(HEAT_URL, TOKEN, expires);
		assertTrue(heatCacheEntry.isExpired());
	}
	
	@Test
	public void isExpiredFalseTest() {
		Calendar expires = new GregorianCalendar(2100,0,31);
		HeatCacheEntry heatCacheEntry = new HeatCacheEntry(HEAT_URL, TOKEN, expires);
		assertFalse(heatCacheEntry.isExpired());
	}
	
	@Test
	public void isExpiredNullTest() {
		Calendar expires = null;
		HeatCacheEntry heatCacheEntry = new HeatCacheEntry(HEAT_URL, TOKEN, expires);
		assertTrue(heatCacheEntry.isExpired());
	}
}
