/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoServiceInstanceFoundExceptionTest {
	private static final String MESSAGE = "message";
	private static final Throwable CAUSE = new Throwable();
	private NoServiceInstanceFoundException NoObjectFoundException;
	
	@Test
	public void defaultConstructorTest() {
		NoObjectFoundException = new NoServiceInstanceFoundException();
		assertEquals(null, NoObjectFoundException.getMessage());
		assertEquals(null, NoObjectFoundException.getCause());
	}
	
	@Test
	public void messageConstructorTest() {
		NoObjectFoundException = new NoServiceInstanceFoundException(MESSAGE);
		assertEquals(MESSAGE, NoObjectFoundException.getMessage());
		assertEquals(null, NoObjectFoundException.getCause());
	}
	
	@Test
	public void causeConstructorTest() {
		NoObjectFoundException = new NoServiceInstanceFoundException(CAUSE);
		assertEquals(CAUSE.toString(), NoObjectFoundException.getMessage()); 
		assertEquals(CAUSE, NoObjectFoundException.getCause());
	}
	
	@Test
	public void messageAndCauseConstructorTest() {
		NoObjectFoundException = new NoServiceInstanceFoundException(MESSAGE, CAUSE);
		assertEquals(MESSAGE, NoObjectFoundException.getMessage());
		assertEquals(CAUSE, NoObjectFoundException.getCause());
	}
	
	@Test
	public void messageAndCauseAndFlagsConstructorTest() {
		NoObjectFoundException = new NoServiceInstanceFoundException(MESSAGE, CAUSE, true, true);
		assertEquals(MESSAGE, NoObjectFoundException.getMessage());
		assertEquals(CAUSE, NoObjectFoundException.getCause());
	}
}
