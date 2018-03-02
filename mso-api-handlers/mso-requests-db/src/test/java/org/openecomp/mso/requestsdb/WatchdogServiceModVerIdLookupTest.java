package org.openecomp.mso.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WatchdogServiceModVerIdLookupTest {
	
	WatchdogServiceModVerIdLookup _watchdogServiceModVerIdLookup;
	
	protected String _distributionId;
	protected String _serviceModelVersionId;
	protected Timestamp _createTime;
	
	public WatchdogServiceModVerIdLookupTest() {
	}
	
    @Before
	public void setUp() {
    	_watchdogServiceModVerIdLookup = mock(WatchdogServiceModVerIdLookup.class);
    	_serviceModelVersionId = "12abad84e7ff";
    	_createTime = new Timestamp (System.currentTimeMillis());
    	
    	when(_watchdogServiceModVerIdLookup.getDistributionId()).thenReturn(_distributionId);
    	when(_watchdogServiceModVerIdLookup.getServiceModelVersionId()).thenReturn(_serviceModelVersionId);
    	when(_watchdogServiceModVerIdLookup.getCreateTime()).thenReturn(_createTime);
	}
    
	@After
	public void tearDown() {
		_watchdogServiceModVerIdLookup = null;
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetDistributionId() {
		_watchdogServiceModVerIdLookup.setDistributionId(_distributionId);
		assertEquals(_watchdogServiceModVerIdLookup.getDistributionId(),_distributionId);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetDistributionId() {
		_watchdogServiceModVerIdLookup.setDistributionId(_distributionId);
		verify(_watchdogServiceModVerIdLookup).setDistributionId(_distributionId);
	}
	
	/**
	 * Test of getServiceModelVersionId method
	 */
	@Test
	public void testGetServiceModelVersionId() {
		_watchdogServiceModVerIdLookup.setServiceModelVersionId(_serviceModelVersionId);
		assertEquals(_watchdogServiceModVerIdLookup.getServiceModelVersionId(),_serviceModelVersionId);

	}

	/**
	 * Test setServiceModelVersionId  method
	 */
	@Test
	public void testSetServiceModelVersionId() {
		_watchdogServiceModVerIdLookup.setServiceModelVersionId(_serviceModelVersionId);
		verify(_watchdogServiceModVerIdLookup).setServiceModelVersionId(_serviceModelVersionId);
	}
	
	/**
	 * Test of getCreateTime method
	 */
	@Test
	public void testGetCreateTime() {
		_watchdogServiceModVerIdLookup.setCreateTime(_createTime);
		assertEquals(_watchdogServiceModVerIdLookup.getCreateTime(),_createTime);

	}

	/**
	 * Test setCreateTime method
	 */
	@Test
	public void testSetCreateTime() {
		_watchdogServiceModVerIdLookup.setCreateTime(_createTime);
		verify(_watchdogServiceModVerIdLookup).setCreateTime(_createTime);
	}
	
}