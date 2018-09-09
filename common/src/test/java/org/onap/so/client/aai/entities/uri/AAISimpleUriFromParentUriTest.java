/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.so.client.aai.AAIObjectType;

public class AAISimpleUriFromParentUriTest {

	
	@Test
	public void appendChildren() {
		
		AAIResourceUri parentUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "key1", "key2", "key3");
		
		AAIUri uri = new AAISimpleUri(parentUri, AAIObjectType.ALLOTTED_RESOURCE, "key4");
		
		assertEquals("path appended", "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3/allotted-resources/allotted-resource/key4", uri.build().toString());
		
	}
}
