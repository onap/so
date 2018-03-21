/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.client.aai.objects;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

public class AAIOwningEntityTest {
	AAIOwningEntity aaiOE= new AAIOwningEntity();
	@Test
	public void test() {
		aaiOE.setOwningEntityId("OwningEntityId");
		aaiOE.setOwningEntityName("OwningEntityName");
		assertEquals(aaiOE.getOwningEntityId(), "OwningEntityId");
		assertEquals(aaiOE.getOwningEntityName(), "OwningEntityName");
		aaiOE.withOwningEntity("OwningEntityId1", "OwningEntityName1");
		assert(aaiOE.getUri()!=null);
		}
}
