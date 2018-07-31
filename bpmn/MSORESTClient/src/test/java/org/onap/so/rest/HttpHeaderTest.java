/*
* ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

package org.onap.so.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpHeaderTest {

	@Test
	public void test() {
		HttpHeader hth= new HttpHeader("name", "value");
		assertEquals("name", hth.getName());
		assertEquals("value", hth.getValue());
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testException() {
		HttpHeader httpHeader = new HttpHeader(null, "value");  //null
	}	
}
