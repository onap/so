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

public class OperationalEnvServiceModelStatusTest {
	
	OperationalEnvServiceModelStatus _operationalEnvServiceModelStatus;
	
	protected String _requestId;
	protected String _operationalEnvId;
	protected String _serviceModelVersionId;
	protected String _serviceModelVersionDistrStatus;
	protected String _recoveryAction;
	private int _retryCount;
	private String _workloadContext;
	protected Timestamp _createTime;
	protected Timestamp _modifyTime;

	
	public OperationalEnvServiceModelStatusTest() {
	}
	
    @Before
	public void setUp() {
    	_operationalEnvServiceModelStatus = mock(OperationalEnvServiceModelStatus.class);
    	_requestId = "1234";
    	_operationalEnvId = "28122015552391";
    	_serviceModelVersionId = "28122015552391-aa";
    	_serviceModelVersionDistrStatus = "SENT";
    	_recoveryAction = "Retry";  	
    	_retryCount = 0;
    	_workloadContext = "VNF_E2E-IST";
    	_createTime = new Timestamp (System.currentTimeMillis());
    	_modifyTime = new Timestamp (System.currentTimeMillis());
    	
    	when(_operationalEnvServiceModelStatus.getRequestId()).thenReturn(_requestId);
    	when(_operationalEnvServiceModelStatus.getOperationalEnvId()).thenReturn(_operationalEnvId);
    	when(_operationalEnvServiceModelStatus.getServiceModelVersionId()).thenReturn(_serviceModelVersionId);
    	when(_operationalEnvServiceModelStatus.getServiceModelVersionDistrStatus()).thenReturn(_serviceModelVersionDistrStatus);
    	when(_operationalEnvServiceModelStatus.getRecoveryAction()).thenReturn(_recoveryAction);
    	when(_operationalEnvServiceModelStatus.getRetryCount()).thenReturn(_retryCount);
    	when(_operationalEnvServiceModelStatus.getWorkloadContext()).thenReturn(_workloadContext);
    	when(_operationalEnvServiceModelStatus.getCreateTime()).thenReturn(_createTime);
    	when(_operationalEnvServiceModelStatus.getModifyTime()).thenReturn(_modifyTime);
	}
    

	@After
	public void tearDown() {
		_operationalEnvServiceModelStatus = null;
	}
	
	/**
	 * Test of getRequestId method
	 */
	@Test
	public void testGetRequestId() {
		_operationalEnvServiceModelStatus.setRequestId(_requestId);
		assertEquals(_operationalEnvServiceModelStatus.getRequestId(),_requestId);

	}

	/**
	 * Test setRequestId  method
	 */
	@Test
	public void testSetRequestId() {
		_operationalEnvServiceModelStatus.setRequestId(_requestId);
		verify(_operationalEnvServiceModelStatus).setRequestId(_requestId);
	}

	/**
	 * Test of getOperationalEnvId method
	 */
	@Test
	public void testGetOperationalEnvId() {
		_operationalEnvServiceModelStatus.setOperationalEnvId(_operationalEnvId);
		assertEquals(_operationalEnvServiceModelStatus.getOperationalEnvId(),_operationalEnvId);

	}

	/**
	 * Test setOperationalEnvId method
	 */
	@Test
	public void testSetOperationalEnvId() {
		_operationalEnvServiceModelStatus.setOperationalEnvId(_operationalEnvId);
		verify(_operationalEnvServiceModelStatus).setOperationalEnvId(_operationalEnvId);
	}
	
	/**
	 * Test of getServiceModelVersionId method
	 */
	@Test
	public void testGetServiceModelVersionId() {
		_operationalEnvServiceModelStatus.setServiceModelVersionId(_serviceModelVersionId);
		assertEquals(_operationalEnvServiceModelStatus.getServiceModelVersionId(),_serviceModelVersionId);

	}

	/**
	 * Test setServiceModelVersionId method
	 */
	@Test
	public void testSetServiceModelVersionId() {
		_operationalEnvServiceModelStatus.setServiceModelVersionId(_serviceModelVersionId);
		verify(_operationalEnvServiceModelStatus).setServiceModelVersionId(_serviceModelVersionId);
	}
	
	/**
	 * Test of getServiceModelVersionId method
	 */
	@Test
	public void testGetServiceModelVersionDistrStatus() {
		_operationalEnvServiceModelStatus.setServiceModelVersionDistrStatus(_serviceModelVersionDistrStatus);
		assertEquals(_operationalEnvServiceModelStatus.getServiceModelVersionDistrStatus(),_serviceModelVersionDistrStatus);

	}

	/**
	 * Test setServiceModelVersionId method
	 */
	@Test
	public void testSetServiceModelVersionDistrStatus() {
		_operationalEnvServiceModelStatus.setServiceModelVersionDistrStatus(_serviceModelVersionDistrStatus);
		verify(_operationalEnvServiceModelStatus).setServiceModelVersionDistrStatus(_serviceModelVersionDistrStatus);
	}
	
	/**
	 * Test of getOperationalEnvId method
	 */
	@Test
	public void testGetRecoveryAction() {
		_operationalEnvServiceModelStatus.setRecoveryAction(_recoveryAction);
		assertEquals(_operationalEnvServiceModelStatus.getRecoveryAction(),_recoveryAction);

	}

	/**
	 * Test setOperationalEnvId method
	 */
	@Test
	public void testSetRecoveryAction() {
		_operationalEnvServiceModelStatus.setRecoveryAction(_recoveryAction);
		verify(_operationalEnvServiceModelStatus).setRecoveryAction(_recoveryAction);
	}
	
	/**
	 * Test of getOperationalEnvId method
	 */
	@Test
	public void testGetRetryCount() {
		_operationalEnvServiceModelStatus.setRetryCount(_retryCount);
		assertEquals(_operationalEnvServiceModelStatus.getRetryCount(),_retryCount);

	}

	/**
	 * Test setOperationalEnvId method
	 */
	@Test
	public void testSetRetryCount() {
		_operationalEnvServiceModelStatus.setRetryCount(_retryCount);
		verify(_operationalEnvServiceModelStatus).setRetryCount(_retryCount);
	}
	
	/**
	 * Test of getOperationalEnvId method
	 */
	@Test
	public void testGetWorkloadContext() {
		_operationalEnvServiceModelStatus.setWorkloadContext(_workloadContext);
		assertEquals(_operationalEnvServiceModelStatus.getWorkloadContext(),_workloadContext);

	}

	/**
	 * Test setOperationalEnvId method
	 */
	@Test
	public void testSetWorkloadContext() {
		_operationalEnvServiceModelStatus.setWorkloadContext(_workloadContext);
		verify(_operationalEnvServiceModelStatus).setWorkloadContext(_workloadContext);
	}
	
	/**
	 * Test of getCreateTime method
	 */
	@Test
	public void testGetCreateTime() {
		_operationalEnvServiceModelStatus.setCreateTime(_createTime);
		assertEquals(_operationalEnvServiceModelStatus.getCreateTime(),_createTime);

	}

	/**
	 * Test setCreateTime method
	 */
	@Test
	public void testSetCreateTime() {
		_operationalEnvServiceModelStatus.setCreateTime(_createTime);
		verify(_operationalEnvServiceModelStatus).setCreateTime(_createTime);
	}
	
	/**
	 * Test of getModifyTime method
	 */
	@Test
	public void testGetModifyTime() {
		_operationalEnvServiceModelStatus.setModifyTime(_modifyTime);
		assertEquals(_operationalEnvServiceModelStatus.getModifyTime(),_modifyTime);

	}

	/**
	 * Test setModifyTime method
	 */
	@Test
	public void testSetModifyTime() {
		_operationalEnvServiceModelStatus.setModifyTime(_modifyTime);
		verify(_operationalEnvServiceModelStatus).setModifyTime(_modifyTime);
	}
	
}
