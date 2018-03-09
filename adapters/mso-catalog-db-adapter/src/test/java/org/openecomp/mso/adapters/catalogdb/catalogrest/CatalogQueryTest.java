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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;

import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class CatalogQueryTest {

	
	@Test
	public void catalogQuerySetTemplateImpl_Test(){
		CatalogQuery mockCatalogQuery = Mockito.mock(CatalogQuery.class);
		Mockito.doCallRealMethod().when(mockCatalogQuery).setTemplate(Mockito.anyString(), Mockito.anyMapOf(String.class, String.class));
		
		Map<String,String> valueMap = new HashMap<>();
		valueMap.put("somekey", "somevalue");
		
		String ret = mockCatalogQuery.setTemplate("<somekey>", valueMap);
		
		assertTrue(ret.equalsIgnoreCase("somevalue"));
	}
	
	@Test
	public void smartToJson_Test()
	{
        List<VnfResourceCustomization> paramList = new ArrayList<>();
        VnfResourceCustomization d1 = new VnfResourceCustomization();
        d1.setModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
        d1.setModelInstanceName("RG_6-26_mog11 0");
        d1.setVersion("v1");
        paramList.add(d1);
        
        QueryServiceVnfs qryResp = new QueryServiceVnfs(paramList);
        QueryServiceVnfs mockCatalogQuery = Mockito.spy(qryResp);
        Mockito.doCallRealMethod().when(mockCatalogQuery).smartToJSON();
		String ret = qryResp.smartToJSON();
		// System.out.println(ret);
		
        JsonReader reader = Json.createReader(new StringReader(ret.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
		assertEquals(jArray.size(), 1);
		assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
	}	
	
	@Test
	public void toJsonString_Test()
	{
		CatalogQueryExtendedTest mockCatalogQuery = Mockito.mock(CatalogQueryExtendedTest.class);
		Mockito.doCallRealMethod().when(mockCatalogQuery).JSON2(Mockito.anyBoolean(), Mockito.anyBoolean());
		String ret = mockCatalogQuery.JSON2(true, true);
		
		// System.out.println(ret);
		
        JsonReader reader = Json.createReader(new StringReader(ret.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
		assertEquals(jArray.size(), 1);
		assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
		assertEquals(jArray.getJsonObject(0).getString("version"), "v2");
	}	
	
	class CatalogQueryExtendedTest extends CatalogQuery{
		@Override
		public String JSON2(boolean isArray, boolean isEmbed) {
			return "{\"serviceVnfs\":[{\"version\":\"v2\",\"modelCustomizationUuid\":\"16ea3e56-a8ce-4ad7-8edd-4d2eae095391\",\"modelInstanceName\":\"ciVFOnboarded-FNAT-aab06c41 1\",\"created\":null,\"vnfResourceModelUuid\":null,\"vnfResourceModelUUID\":null,\"minInstances\":null,\"maxInstances\":null,\"availabilityZoneMaxCount\":null,\"vnfResource\":null,\"nfFunction\":null,\"nfType\":null,\"nfRole\":null,\"nfNamingCode\":null,\"multiStageDesign\":null,\"vfModuleCustomizations\":null,\"serviceResourceCustomizations\":null,\"creationTimestamp\":null}]}";
		}
		
	}	
	
}


