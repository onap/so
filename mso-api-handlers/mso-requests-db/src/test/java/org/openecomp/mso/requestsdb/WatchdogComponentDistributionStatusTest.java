package org.openecomp.mso.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WatchdogComponentDistributionStatusTest {
		
	WatchdogComponentDistributionStatus _watchdogComponentDistributionStatus;
	
	protected String _distributionId;
	protected String _componentName;
	protected String _componentDistributionStatus;
	protected Timestamp _createTime;
	protected Timestamp _modifyTime;

	public WatchdogComponentDistributionStatusTest() {
	}
	
    @Before
	public void setUp() {
    	_watchdogComponentDistributionStatus = mock(WatchdogComponentDistributionStatus.class);
    	_distributionId = "12abad84e7ff";
    	_componentName = "MSO";
    	_componentDistributionStatus = "SENT";	
    	_createTime = new Timestamp (System.currentTimeMillis());
    	_modifyTime = new Timestamp (System.currentTimeMillis());
    	
    	when(_watchdogComponentDistributionStatus.getDistributionId()).thenReturn(_distributionId);
    	when(_watchdogComponentDistributionStatus.getComponentName()).thenReturn(_componentName);
    	when(_watchdogComponentDistributionStatus.getComponentDistributionStatus()).thenReturn(_componentDistributionStatus);
    	when(_watchdogComponentDistributionStatus.getCreateTime()).thenReturn(_createTime);
    	when(_watchdogComponentDistributionStatus.getModifyTime()).thenReturn(_modifyTime);
	}
    
	@After
	public void tearDown() {
		_watchdogComponentDistributionStatus = null;
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetDistributionId() {
		_watchdogComponentDistributionStatus.setDistributionId(_distributionId);
		assertEquals(_watchdogComponentDistributionStatus.getDistributionId(),_distributionId);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetDistributionId() {
		_watchdogComponentDistributionStatus.setDistributionId(_distributionId);
		verify(_watchdogComponentDistributionStatus).setDistributionId(_distributionId);
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetComponentName() {
		_watchdogComponentDistributionStatus.setComponentName(_componentName);
		assertEquals(_watchdogComponentDistributionStatus.getComponentName(),_componentName);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetComponentName() {
		_watchdogComponentDistributionStatus.setComponentName(_componentName);
		verify(_watchdogComponentDistributionStatus).setComponentName(_componentName);
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetComponentDistributionStatus() {
		_watchdogComponentDistributionStatus.setComponentDistributionStatus(_componentDistributionStatus);
		assertEquals(_watchdogComponentDistributionStatus.getComponentDistributionStatus(),_componentDistributionStatus);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetComponentDistributionStatus() {
		_watchdogComponentDistributionStatus.setComponentDistributionStatus(_componentDistributionStatus);
		verify(_watchdogComponentDistributionStatus).setComponentDistributionStatus(_componentDistributionStatus);
	}
	
	/**
	 * Test of getCreateTime method
	 */
	@Test
	public void testGetCreateTime() {
		_watchdogComponentDistributionStatus.setCreateTime(_createTime);
		System.out.println("CreateTime : " + _createTime);
		assertEquals(_watchdogComponentDistributionStatus.getCreateTime(),_createTime);

	}

	/**
	 * Test setCreateTime method
	 */
	@Test
	public void testSetCreateTime() {
		_watchdogComponentDistributionStatus.setCreateTime(_createTime);
		verify(_watchdogComponentDistributionStatus).setCreateTime(_createTime);
	}
	
	/**
	 * Test of getModifyTime method
	 */
	@Test
	public void testGetModifyTime() {
		_watchdogComponentDistributionStatus.setModifyTime(_modifyTime);
		System.out.println("ModifyTime : " + _modifyTime);
		assertEquals(_watchdogComponentDistributionStatus.getModifyTime(),_modifyTime);

	}

	/**
	 * Test setModifyTime method
	 */
	@Test
	public void testSetModifyTime() {
		_watchdogComponentDistributionStatus.setModifyTime(_modifyTime);
		verify(_watchdogComponentDistributionStatus).setModifyTime(_modifyTime);
	}
	
}
