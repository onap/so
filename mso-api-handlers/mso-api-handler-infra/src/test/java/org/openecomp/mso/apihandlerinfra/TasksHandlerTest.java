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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class TasksHandlerTest {
	
	TasksHandler handler = new TasksHandler();
	
	@Test
	public void queryFiltersTest() throws ParseException{
		Response resp = handler.queryFilters("10020", "399495", "test", "nfRole", "buildingBlockName", "originalRequestDate", "originalRequestorId", "v2");
		assertTrue(resp.getEntity().toString() != null);
	}

}
