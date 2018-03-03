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

package org.openecomp.mso.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceMarcoHolderTest {

	@Test
	public void JSON2_Test()
	{
		Service svc = new Service();
		svc.setModelUUID("0cb9b26a-9820-48a7-86e5-16c510e993d9");
		svc.setModelName("Testing Model One");
	    ArrayList<NetworkResourceCustomization> paramList;    		
        paramList = new ArrayList<NetworkResourceCustomization>();
        NetworkResourceCustomization d1 = new NetworkResourceCustomization();
        d1.setNetworkResourceModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
        paramList.add(d1);
		ServiceMacroHolder ret = new ServiceMacroHolder(svc);
		ret.setNetworkResourceCustomization(paramList);
		QueryServiceMacroHolder holder = new QueryServiceMacroHolder(ret);
		String s = holder.JSON2(true, true); 
		
		// System.out.println(s);

		// prepare to inspect response
        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        JsonObject obj = respObj.getJsonObject("serviceResources");
        JsonObject obj2 = obj.getJsonObject("modelInfo");
        String modelName = obj2.getString("modelName");
        String modelUuid = obj2.getString("modelUuid");
        
    	assertTrue(modelName.equals("Testing Model One"));
    	assertTrue(modelUuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
    	// end		
	}
}
