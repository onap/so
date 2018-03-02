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

import org.junit.Test;

public class AAIObjectTypeTest {

	@Test
	public void verifyDefaultCase() {
		assertEquals("default removed for tenant", "tenant", AAIObjectType.DEFAULT_TENANT.typeName());
		assertEquals("default removed for cloud-region", "cloud-region", AAIObjectType.DEFAULT_CLOUD_REGION.typeName());
	}
	
	@Test
	public void verifyRegularCase() {
		assertEquals("default removed for tenant", "allotted-resource", AAIObjectType.ALLOTTED_RESOURCE.typeName());
	}
}
