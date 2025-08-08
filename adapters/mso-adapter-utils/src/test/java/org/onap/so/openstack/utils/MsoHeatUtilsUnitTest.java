/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.skyscreamer.jsonassert.JSONAssert;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MsoHeatUtilsUnitTest {


    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void convertInputMapTest() throws JsonMappingException, IOException {
        MsoHeatUtils utils = new MsoHeatUtils();

        Map<String, Object> input = new HashMap<>();
        HeatTemplate template = new HeatTemplate();
        template.setArtifactUuid("my-uuid");
        Set<HeatTemplateParam> parameters = template.getParameters();
        HeatTemplateParam paramNum = new HeatTemplateParam();
        paramNum.setParamType("number");
        paramNum.setParamName("my-number");
        input.put("my-number", "3");

        HeatTemplateParam paramString = new HeatTemplateParam();
        paramString.setParamType("string");
        paramString.setParamName("my-string");
        input.put("my-string", "hello");

        HeatTemplateParam paramJson = new HeatTemplateParam();
        paramJson.setParamType("json");
        paramJson.setParamName("my-json");

        HeatTemplateParam paramJsonEscaped = new HeatTemplateParam();
        paramJsonEscaped.setParamType("json");
        paramJsonEscaped.setParamName("my-json-escaped");

        Map<String, Object> jsonMap =
                mapper.readValue(getJson("free-form.json"), new TypeReference<Map<String, Object>>() {});
        input.put("my-json", jsonMap);

        input.put("my-json-escaped", getJson("free-form.json"));

        parameters.add(paramNum);
        parameters.add(paramString);
        parameters.add(paramJson);
        parameters.add(paramJsonEscaped);

        Map<String, Object> output = utils.convertInputMap(input, template);

        assertEquals(3, output.get("my-number"));
        assertEquals("hello", output.get("my-string"));
        assertTrue("expect no change in type", output.get("my-json") instanceof Map);
        assertTrue("expect string to become jsonNode", output.get("my-json-escaped") instanceof JsonNode);

        JSONAssert.assertEquals(getJson("free-form.json"), mapper.writeValueAsString(output.get("my-json-escaped")),
                false);
    }

    @Test
    public final void convertInputMapValuesTest() {
        MsoHeatUtils utils = new MsoHeatUtils();
        Map<String, Object> inputs = new HashMap<>();
        Set<HeatTemplateParam> params = new HashSet<>();
        HeatTemplate ht = new HeatTemplate();
        HeatTemplateParam htp = new HeatTemplateParam();
        htp.setParamName("vnf_name");
        htp.setParamType("string");
        params.add(htp);
        inputs.put("vnf_name", "a_vnf_name");
        htp = new HeatTemplateParam();
        htp.setParamName("image_size");
        htp.setParamType("number");
        params.add(htp);
        inputs.put("image_size", "1024");
        htp = new HeatTemplateParam();
        htp.setParamName("external");
        htp.setParamType("boolean");
        params.add(htp);
        inputs.put("external", "false");
        htp = new HeatTemplateParam();
        htp.setParamName("oam_ips");
        htp.setParamType("comma_delimited_list");
        params.add(htp);
        inputs.put("oam_ips", "a,b");
        htp = new HeatTemplateParam();
        htp.setParamName("oam_prefixes");
        htp.setParamType("json");
        params.add(htp);
        String jsonEscInput = "[{\"prefix\": \"aValue\"}, {\"prefix\": \"aValue2\"}]";
        inputs.put("oam_prefixes", jsonEscInput);
        ht.setParameters(params);

        Map<String, Object> output = utils.convertInputMap(inputs, ht);

        assertEquals("a_vnf_name", output.get("vnf_name"));
        assertEquals(1024, output.get("image_size"));
        assertEquals(false, output.get("external"));
        List<String> cdl = new ArrayList<>();
        cdl.add(0, "a");
        cdl.add(1, "b");
        assertEquals(cdl, output.get("oam_ips"));
        ObjectMapper JSON_MAPPER = new ObjectMapper();
        JsonNode jn = null;
        try {
            jn = JSON_MAPPER.readTree(jsonEscInput);
        } catch (Exception e) {
        }
        assertEquals(jn, output.get("oam_prefixes"));
    }

    @Test
    public final void convertInputMapNullsTest() {
        MsoHeatUtils utils = new MsoHeatUtils();
        Map<String, Object> inputs = new HashMap<>();
        Set<HeatTemplateParam> params = new HashSet<>();
        HeatTemplate ht = new HeatTemplate();
        HeatTemplateParam htp = new HeatTemplateParam();
        htp.setParamName("vnf_name");
        htp.setParamType("string");
        params.add(htp);
        inputs.put("vnf_name", null);
        htp = new HeatTemplateParam();
        htp.setParamName("image_size");
        htp.setParamType("number");
        params.add(htp);
        inputs.put("image_size", null);
        htp = new HeatTemplateParam();
        htp.setParamName("external");
        htp.setParamType("boolean");
        params.add(htp);
        inputs.put("external", null);
        htp = new HeatTemplateParam();
        htp.setParamName("oam_ips");
        htp.setParamType("comma_delimited_list");
        params.add(htp);
        inputs.put("oam_ips", null);
        htp = new HeatTemplateParam();
        htp.setParamName("oam_prefixes");
        htp.setParamType("json");
        params.add(htp);
        inputs.put("oam_prefixes", null);
        ht.setParameters(params);

        Map<String, Object> output = utils.convertInputMap(inputs, ht);

        assertNull(output.get("vnf_name"));
        assertNull(output.get("image_size"));
        assertEquals(false, output.get("external"));
        assertNull(output.get("oam_ips"));
        assertNull(output.get("oam_prefixes"));
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/MsoHeatUtils/" + filename)));
    }

}
