/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.taskbeans;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openecomp.mso.apihandlerinfra.tasksbeans.Value;

public class ValueTest {
	Value _valueInstance;
	protected String _value;

	public ValueTest() {
	}

	@Before
	public void setUp() {
		_valueInstance = mock(Value.class);
		_value = "_value";
		when(_valueInstance.getValue()).thenReturn(_value);
	}

	@After
	public void tearDown() {
		_valueInstance = null;
	}

	/**
	 * Test of getValue method
	 */
	@Test
	public void testGetValue() {
		_valueInstance.setValue(_value);
		assertEquals(_valueInstance.getValue(),_value);

	}

	/**
	 * Test setValue
	 */
	@Test
	public void testSetValue() {
		_valueInstance.setValue(_value);
		verify(_valueInstance).setValue(_value);
	}
}
