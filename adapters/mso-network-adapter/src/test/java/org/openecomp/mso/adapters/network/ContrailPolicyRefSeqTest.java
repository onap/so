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

package org.openecomp.mso.adapters.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContrailPolicyRefSeqTest {
	@Test
	public void ContrailPolicyRefSeqJson_Test()
	{
		ContrailPolicyRefSeq cprs = new ContrailPolicyRefSeq("majorVersion 1","minorVersion 0.02");
		assertTrue(cprs.toString().contains("majorVersion 1"));
		assertTrue(cprs.toString().contains("minorVersion 0.02"));
	}
	
}
