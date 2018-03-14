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

package org.openecomp.mso.adapters.sdncrest;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;

public class SDNCServiceResponseTest {

	@Mock
	SDNCResponseCommon src;
	
	@InjectMocks
	SDNCServiceResponse ssr;
	
	@Before
	public void init(){
	    MockitoAnnotations.initMocks(this);
	 }
	
	@Test
	public void test() {
		ssr=new SDNCServiceResponse("sdncRequestId", "200",
				"msg", "indicator");
		Map<String, String> mp = new HashMap<>();
		mp.put("name", "value");
		ssr.setParams(mp);
		assert(ssr.getParams().equals(mp));
		assertNotNull(ssr);
	}
}
