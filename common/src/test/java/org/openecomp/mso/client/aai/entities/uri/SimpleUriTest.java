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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;

public class SimpleUriTest {

	
	
	@Test
	public void relatedToTestPlural() {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
		uri.relatedTo(AAIObjectPlurals.PSERVER);
		String uriOutput = uri.build().toString();
		assertEquals(true, uriOutput.contains("related-to"));
	}
	
	@Test
	public void relatedToTestSingular() {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
		uri.relatedTo(AAIObjectType.PSERVER, "test2");
		String uriOutput = uri.build().toString();
		assertEquals(true, uriOutput.contains("related-to"));
	}
	
	@Test
	public void cloneTest() {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
		AAIResourceUri cloned = uri.clone();
		Map<String,String> keys = cloned.getURIKeys();
		assertThat(keys.values(), contains("test1"));
	}
	
	@Test
	public void getKeysTest() {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, "cloud1", "cloud2", "tenant1", "vserver1");
		Map<String,String> keys = uri.getURIKeys();
		System.out.println(keys);
		System.out.println(uri.build());
		assertEquals("vserver1", keys.get("vserver-id"));
	}
}
