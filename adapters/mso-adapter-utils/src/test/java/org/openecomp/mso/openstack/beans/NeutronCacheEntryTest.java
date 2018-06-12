package org.openecomp.mso.openstack.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

public class NeutronCacheEntryTest extends BaseTest {
	
	private static final String NEUTRON_URL = "testNeutronUrl";
	private static final String TOKEN = "testToken";
	
	@Test
	public void isExpiredTrueTest() {
		Calendar expires = new GregorianCalendar(2013,0,31);
		NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
		assertTrue(neutronCacheEntry.isExpired());
	}
	
	@Test
	public void isExpiredFalseTest() {
		Calendar expires = new GregorianCalendar(2100,0,31);
		NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
		assertFalse(neutronCacheEntry.isExpired());
	}
	
	@Test
	public void isExpiredNullTest() {
		Calendar expires = null;
		NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
		assertTrue(neutronCacheEntry.isExpired());
	}
}
