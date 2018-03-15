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

package com.gigaspaces.aria.rest.client;

import static org.junit.Assert.*;
import org.junit.Test;

public class NodeTemplateImplTest {

	private NodeTemplateImpl nti;
	
	@Test
	public void test() {	
		nti=new NodeTemplateImpl(12, null, "desc", 12, "tname");
        assertEquals(12,nti.getId());
        assertEquals(12,nti.getServiceTemplateId());
        assertEquals(null,nti.getName());
        assertEquals("desc",nti.getDescription());
        assertEquals("tname",nti.getTypeName());
	}
}
