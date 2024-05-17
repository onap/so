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

package org.onap.so.bpmn.common.scripts

import org.junit.Assert
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.core.WorkflowException
@RunWith(MockitoJUnitRunner.class)
import org.junit.Test



class ExceptionUtilTest {


	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}

	@Test
	public void testMapErrorCode5010(){
		String msg = "Connect to njcdtl20ew2988:8070  failed: Connection refused: connect"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals("5010",util.MapErrorCode(msg))
	}

	@Test
	public void testMapErrorCode5020(){
		String msg = "Connection timed out"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals("5020",util.MapErrorCode(msg))
	}
	
	@Test
	public void testGetErrorCode() {
		ExceptionUtil eUtil = new ExceptionUtil()
		WorkflowException we = new WorkflowException("CreateVCEV2", 400, "SDNC Test Error")
		int errCd = eUtil.getErrorCode(we)
		Assert.assertEquals(400, errCd)
	}
	
	@Test
	public void testGetErrorCodeWithNullWfe() {
		ExceptionUtil eUtil = new ExceptionUtil()
		int errCd = eUtil.getErrorCode(null)
		Assert.assertEquals(2500, errCd)
	}
	
	@Test
	public void testGetErrorMessage() {
		ExceptionUtil eUtil = new ExceptionUtil()
		WorkflowException we = new WorkflowException("CreateVCEV2", 400, "SDNC Test Error")
		String errMsg = eUtil.getErrorMessage(we, "CreateVCEV2")
		Assert.assertEquals("SDNC Test Error", errMsg)
	}
	
	@Test
	public void testGetErrorMessageWithNullWfe() {
		ExceptionUtil eUtil = new ExceptionUtil()
		String errMsg = eUtil.getErrorMessage(null, "CreateVCEV2")
		Assert.assertEquals("Unexpected error encountered in CreateVCEV2", errMsg)
	}
	
}
