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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;

@RunWith(MockitoJUnitRunner.class)
public class QueryAllottedResourceCustomizationTest {

    @Test
    public void JSON2_Test() {
        List<AllottedResourceCustomization> paramList;
        paramList = new ArrayList<>();
        AllottedResourceCustomization d1 = new AllottedResourceCustomization();
        d1.setModelInstanceName("0cb9b26a-9820-48a7-86e5-16c510e993d9");
        d1.setModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
        paramList.add(d1);

        QueryAllottedResourceCustomization qarcObj = new QueryAllottedResourceCustomization(paramList);
        String ret = qarcObj.JSON2(true, true);
        System.out.println(ret);
        ret = "{" + ret + "}";

        JsonReader reader = Json.createReader(new StringReader(ret.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        JsonArray jArray = respObj.getJsonArray("serviceAllottedResources");
        assertEquals(jArray.size(), 1);

        assertEquals(jArray.getJsonObject(0).getJsonObject("modelInfo").getString("modelInstanceName"), "0cb9b26a-9820-48a7-86e5-16c510e993d9");
        assertEquals(jArray.getJsonObject(0).getJsonObject("modelInfo").getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
    }

}
