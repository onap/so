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

package org.openecomp.mso.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WatchdogDistributionStatusTest {

	WatchdogDistributionStatus _watchdogDistributionStatus;
	
	protected String _distributionId;
	protected String _distributionIdStatus;
	protected Timestamp _createTime;
	protected Timestamp _modifyTime;
	
	public WatchdogDistributionStatusTest() {
	}
	
    @Before
	public void setUp() {
    	_watchdogDistributionStatus = mock(WatchdogDistributionStatus.class);
    	_distributionId = "12abad84e7ff";
    	_distributionIdStatus = "SENT";	
    	_createTime = new Timestamp (System.currentTimeMillis());
    	_modifyTime = new Timestamp (System.currentTimeMillis());
    	
    	when(_watchdogDistributionStatus.getDistributionId()).thenReturn(_distributionId);
    	when(_watchdogDistributionStatus.getDistributionIdStatus()).thenReturn(_distributionIdStatus);
    	when(_watchdogDistributionStatus.getCreateTime()).thenReturn(_createTime);
    	when(_watchdogDistributionStatus.getModifyTime()).thenReturn(_modifyTime);
	}
    
	@After
	public void tearDown() {
		_watchdogDistributionStatus = null;
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetDistributionId() {
		_watchdogDistributionStatus.setDistributionId(_distributionId);
		assertEquals(_watchdogDistributionStatus.getDistributionId(),_distributionId);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetDistributionId() {
		_watchdogDistributionStatus.setDistributionId(_distributionId);
		verify(_watchdogDistributionStatus).setDistributionId(_distributionId);
	}
	
	/**
	 * Test of getDistributionIdStatus method
	 */
	@Test
	public void testGetComponentDistributionStatus() {
		_watchdogDistributionStatus.setDistributionIdStatus(_distributionIdStatus);
		assertEquals(_watchdogDistributionStatus.getDistributionIdStatus(),_distributionIdStatus);

	}

	/**
	 * Test setDistributionIdStatus  method
	 */
	@Test
	public void testSetComponentDistributionStatus() {
		_watchdogDistributionStatus.setDistributionIdStatus(_distributionIdStatus);
		verify(_watchdogDistributionStatus).setDistributionIdStatus(_distributionIdStatus);
	}
	
	/**
	 * Test of getCreateTime method
	 */
	@Test
	public void testGetCreateTime() {
		_watchdogDistributionStatus.setCreateTime(_createTime);
		assertEquals(_watchdogDistributionStatus.getCreateTime(),_createTime);

	}

	/**
	 * Test setCreateTime method
	 */
	@Test
	public void testSetCreateTime() {
		_watchdogDistributionStatus.setCreateTime(_createTime);
		verify(_watchdogDistributionStatus).setCreateTime(_createTime);
	}
	
	/**
	 * Test of getModifyTime method
	 */
	@Test
	public void testGetModifyTime() {
		_watchdogDistributionStatus.setModifyTime(_modifyTime);
		assertEquals(_watchdogDistributionStatus.getModifyTime(),_modifyTime);

	}

	/**
	 * Test setModifyTime method
	 */
	@Test
	public void testSetModifyTime() {
		_watchdogDistributionStatus.setModifyTime(_modifyTime);
		verify(_watchdogDistributionStatus).setModifyTime(_modifyTime);
	}
}
