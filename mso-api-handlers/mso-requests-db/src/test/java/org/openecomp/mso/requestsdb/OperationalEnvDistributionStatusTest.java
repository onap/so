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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

public class OperationalEnvDistributionStatusTest {

	OperationalEnvDistributionStatus _operationalEnvDistributionStatus;
	
	protected String _distributionId;
	protected String _operationalEnvId;
	protected String _serviceModelVersionId;
	protected String _requestId;
	protected String _distributionIdStatus;
	protected String _distributionIdErrorReason;
	protected Timestamp _createTime;
	protected Timestamp _modifyTime;

	public OperationalEnvDistributionStatusTest() {
	}
	
    @Before
	public void setUp() {
    	_operationalEnvDistributionStatus = mock(OperationalEnvDistributionStatus.class);
    	_distributionId = "12abad84e7ff";
    	_operationalEnvId = "28122015552391";
    	_serviceModelVersionId = "28122015552391-aa";
    	_requestId = "1234";
    	_distributionIdStatus = "SENT";
    	_distributionIdErrorReason = "Fail";  	
    	_createTime = new Timestamp (System.currentTimeMillis());
    	_modifyTime = new Timestamp (System.currentTimeMillis());
    	
    	when(_operationalEnvDistributionStatus.getDistributionId()).thenReturn(_distributionId);
    	when(_operationalEnvDistributionStatus.getOperationalEnvId()).thenReturn(_operationalEnvId);
    	when(_operationalEnvDistributionStatus.getServiceModelVersionId()).thenReturn(_serviceModelVersionId);
    	when(_operationalEnvDistributionStatus.getRequestId()).thenReturn(_requestId);
    	when(_operationalEnvDistributionStatus.getDistributionIdStatus()).thenReturn(_distributionIdStatus);
    	when(_operationalEnvDistributionStatus.getDistributionIdErrorReason()).thenReturn(_distributionIdErrorReason);
    	when(_operationalEnvDistributionStatus.getCreateTime()).thenReturn(_createTime);
    	when(_operationalEnvDistributionStatus.getModifyTime()).thenReturn(_modifyTime);
	}
    

	@After
	public void tearDown() {
		_operationalEnvDistributionStatus = null;
	}
	
	/**
	 * Test of getDistributionId method
	 */
	@Test
	public void testGetDistributionId() {
		_operationalEnvDistributionStatus.setDistributionId(_distributionId);
		assertEquals(_operationalEnvDistributionStatus.getDistributionId(),_distributionId);

	}

	/**
	 * Test setDistributionId  method
	 */
	@Test
	public void testSetDistributionId() {
		_operationalEnvDistributionStatus.setDistributionId(_distributionId);
		verify(_operationalEnvDistributionStatus).setDistributionId(_distributionId);
	}
	
	/**
	 * Test of getOperationalEnvId method
	 */
	@Test
	public void testGetOperationalEnvId() {
		_operationalEnvDistributionStatus.setOperationalEnvId(_operationalEnvId);
		assertEquals(_operationalEnvDistributionStatus.getOperationalEnvId(),_operationalEnvId);

	}

	/**
	 * Test setOperationalEnvId method
	 */
	@Test
	public void testSetOperationalEnvId() {
		_operationalEnvDistributionStatus.setOperationalEnvId(_operationalEnvId);
		verify(_operationalEnvDistributionStatus).setOperationalEnvId(_operationalEnvId);
	}
	
	/**
	 * Test of getServiceModelVersionId method
	 */
	@Test
	public void testGetServiceModelVersionId() {
		_operationalEnvDistributionStatus.setServiceModelVersionId(_serviceModelVersionId);
		assertEquals(_operationalEnvDistributionStatus.getServiceModelVersionId(),_serviceModelVersionId);

	}

	/**
	 * Test setServiceModelVersionId method
	 */
	@Test
	public void testSetServiceModelVersionId() {
		_operationalEnvDistributionStatus.setServiceModelVersionId(_serviceModelVersionId);
		verify(_operationalEnvDistributionStatus).setServiceModelVersionId(_serviceModelVersionId);
	}
	
	/**
	 * Test of getRequestId method
	 */
	@Test
	public void testGetRequestId() {
		_operationalEnvDistributionStatus.setRequestId(_requestId);
		assertEquals(_operationalEnvDistributionStatus.getRequestId(),_requestId);

	}

	/**
	 * Test setRequestId method
	 */
	@Test
	public void testSetRequestId() {
		_operationalEnvDistributionStatus.setRequestId(_requestId);
		verify(_operationalEnvDistributionStatus).setRequestId(_requestId);
	}
	
	/**
	 * Test of getDistributionIdStatus method
	 */
	@Test
	public void testGetDistributionIdStatus() {
		_operationalEnvDistributionStatus.setDistributionIdStatus(_distributionIdStatus);
		assertEquals(_operationalEnvDistributionStatus.getDistributionIdStatus(),_distributionIdStatus);

	}

	/**
	 * Test setDistributionIdStatus method
	 */
	@Test
	public void testSetDistributionIdStatus() {
		_operationalEnvDistributionStatus.setDistributionIdStatus(_distributionIdStatus);
		verify(_operationalEnvDistributionStatus).setDistributionIdStatus(_distributionIdStatus);
	}
	
	/**
	 * Test of getDistributionIdErrorReason method
	 */
	@Test
	public void testGetDistributionIdErrorReason() {
		_operationalEnvDistributionStatus.setDistributionIdErrorReason(_distributionIdErrorReason);
		assertEquals(_operationalEnvDistributionStatus.getDistributionIdErrorReason(),_distributionIdErrorReason);

	}

	/**
	 * Test setDistributionIdErrorReason method
	 */
	@Test
	public void testSetDistributionIdErrorReason() {
		_operationalEnvDistributionStatus.setDistributionIdErrorReason(_distributionIdErrorReason);
		verify(_operationalEnvDistributionStatus).setDistributionIdErrorReason(_distributionIdErrorReason);
	}
	
	/**
	 * Test of getCreateTime method
	 */
	@Test
	public void testGetCreateTime() {
		_operationalEnvDistributionStatus.setCreateTime(_createTime);
		System.out.println("CreateTime : " + _createTime);
		assertEquals(_operationalEnvDistributionStatus.getCreateTime(),_createTime);

	}

	/**
	 * Test setCreateTime method
	 */
	@Test
	public void testSetCreateTime() {
		_operationalEnvDistributionStatus.setCreateTime(_createTime);
		verify(_operationalEnvDistributionStatus).setCreateTime(_createTime);
	}
	
	/**
	 * Test of getModifyTime method
	 */
	@Test
	public void testGetModifyTime() {
		_operationalEnvDistributionStatus.setModifyTime(_modifyTime);
		System.out.println("ModifyTime : " + _modifyTime);
		assertEquals(_operationalEnvDistributionStatus.getModifyTime(),_modifyTime);

	}

	/**
	 * Test setModifyTime method
	 */
	@Test
	public void testSetModifyTime() {
		_operationalEnvDistributionStatus.setModifyTime(_modifyTime);
		verify(_operationalEnvDistributionStatus).setModifyTime(_modifyTime);
	}
	
	
}
