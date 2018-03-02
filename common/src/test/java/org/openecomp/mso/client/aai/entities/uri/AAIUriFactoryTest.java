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

package org.openecomp.mso.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;

public class AAIUriFactoryTest {

	
	
	@Test
	public void automaticallyEncodeUriTemplateValue() {
		
		AAIUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "VIP(VelocitytoIP)");
		
		String expected = "/network/generic-vnfs/generic-vnf/VIP%28VelocitytoIP%29";
		assertEquals(expected, uri.build().toString());
	}
}
