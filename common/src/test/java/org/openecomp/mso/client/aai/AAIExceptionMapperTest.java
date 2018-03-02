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

package org.openecomp.mso.client.aai;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.client.aai.entities.AAIError;

public class AAIExceptionMapperTest {

	@Mock private AAIError errorObj;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	@Test
	public void nestedReplace() {
		String error = "Error %1 on %2";
		List<String> list = Arrays.asList("PUT", "hello %1");
		AAIErrorFormatter formatter = new AAIErrorFormatter(errorObj);
		String result = formatter.fillInTemplate(error, list);
		assertEquals("equal", "Error PUT on hello PUT", result);
		
	}
	
	@Test
	public void noReplace() {
		String error = "Error";
		List<String> list = new ArrayList<>();
		AAIErrorFormatter formatter = new AAIErrorFormatter(errorObj);
		String result = formatter.fillInTemplate(error, list);
		assertEquals("equal", "Error", result);
		
	}
	
}
