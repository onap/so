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
package org.openecomp.mso.bpmn.core.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class AllottedResourceTest {
	private AllottedResource ar = new AllottedResource();
	TunnelConnect tc = new TunnelConnect();

	@Test
	public void testAllottedResource() {
		ar.setAllottedResourceType("allottedResourceType");
		ar.setAllottedResourceRole("allottedResourceRole");
		ar.setProvidingServiceModelName("providingServiceModelName");
		ar.setProvidingServiceModelInvariantUuid("providingServiceModelInvariantUuid");
		ar.setProvidingServiceModelUuid("providingServiceModelUuid");
		ar.setNfFunction("nfFunction");
		ar.setNfType("nfType");
		ar.setNfRole("nfRole");
		ar.setNfNamingCode("nfNamingCode");
		ar.setOrchestrationStatus("orchestrationStatus");
		ar.setTunnelConnect(tc);
		assertEquals(ar.getAllottedResourceType(), "allottedResourceType");
		assertEquals(ar.getAllottedResourceRole(), "allottedResourceRole");
		assertEquals(ar.getProvidingServiceModelName(), "providingServiceModelName");
		assertEquals(ar.getProvidingServiceModelInvariantUuid(), "providingServiceModelInvariantUuid");
		assertEquals(ar.getProvidingServiceModelUuid(), "providingServiceModelUuid");
		assertEquals(ar.getNfFunction(), "nfFunction");
		assertEquals(ar.getNfType(), "nfType");
		assertEquals(ar.getNfRole(), "nfRole");
		assertEquals(ar.getNfNamingCode(), "nfNamingCode");
		assertEquals(ar.getOrchestrationStatus(), "orchestrationStatus");
		assertEquals(ar.getTunnelConnect(), tc);
		
	}

}
