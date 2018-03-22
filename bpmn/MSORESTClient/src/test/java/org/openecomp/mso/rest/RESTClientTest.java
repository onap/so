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

package org.openecomp.mso.rest;

import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.message.AbstractHttpMessage;
import org.junit.Test;

public class RESTClientTest {

	@Test
	public void test()throws Exception{
		APIResponse apr=mock(APIResponse.class);
		HttpResponse response=mock(HttpResponse.class);
		AbstractHttpMessage httpMsg=mock(AbstractHttpMessage.class);
		RESTClient cle=mock(RESTClient.class);
		RESTClient rcl=new RESTClient("URL");
		RESTClient rcle=new RESTClient("URL", "10.5.3.126", 5020);
		LinkedHashMap<String, List<String>> headers=new LinkedHashMap<>() ;
		List<String>list=new ArrayList<>();
		list.add("value");
		headers.put("name", list);
		rcle.setHeader("name", "value");
		rcle.setParameter("name", "value");
		assert(rcle.getHeaders()!=null);
		assert(rcle.getParameters()!=null);
	}
}
