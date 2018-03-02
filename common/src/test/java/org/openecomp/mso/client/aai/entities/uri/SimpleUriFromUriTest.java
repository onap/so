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

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;

public class SimpleUriFromUriTest {

	
	
	@Test
	public void removeHost() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("https://aai-conexus-e2e.test.att.com:8443/aai/v9/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("root and version removed", "/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
	@Test
	public void noChange() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("no change", "/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
	
	@Test
	public void encodingPreserved() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("encoding preserved", "/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
}
