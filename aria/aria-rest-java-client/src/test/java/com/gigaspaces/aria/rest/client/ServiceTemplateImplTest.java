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

import java.net.URI;

import org.junit.Test;

public class ServiceTemplateImplTest {

	 private URI uri;
	 private byte[] csar_blob;
	 private ServiceTemplateImpl sti;
	
	@Test
	public void test() {
		sti=new ServiceTemplateImpl("name", uri, "filename", "description");
		ServiceTemplateImpl stid=new ServiceTemplateImpl("name", csar_blob);
		ServiceTemplateImpl std=new ServiceTemplateImpl("name", uri);
		sti.setFilename("filename");
		sti.setId(10);
		sti.setName("name");
		sti.setPath("path");
		assertEquals(10,sti.getId());
		assertEquals("name",sti.getName());
		assertEquals(uri,sti.getURI());
		assertEquals("filename",sti.getFilename());
		assertEquals("description",sti.getDescription());
		assertEquals(csar_blob,stid.getCSARBytes());	
	}
}
