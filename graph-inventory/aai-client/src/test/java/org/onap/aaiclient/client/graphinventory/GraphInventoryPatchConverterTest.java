/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(MockitoJUnitRunner.class)
public class GraphInventoryPatchConverterTest {

    private ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();

    @Test
    public void convertObjectToPatchFormatTest()
            throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
        GraphInventoryPatchConverter validator = new GraphInventoryPatchConverter();
        GenericVnf vnf = new GenericVnf();
        vnf.setIpv4Loopback0Address("");
        String result = validator.marshallObjectToPatchFormat(vnf);
        GenericVnf resultObj = mapper.readValue(result.toString(), GenericVnf.class);
        assertTrue("expect object to become a String to prevent double marshalling", result instanceof String);
        assertNull("expect null because of custom mapper", resultObj.getIpv4Loopback0Address());

    }

    @Test
    public void convertStringToPatchFormatTest()
            throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
        GraphInventoryPatchConverter validator = new GraphInventoryPatchConverter();
        String payload = "{\"ipv4-loopback0-address\":\"\"}";
        String result = validator.marshallObjectToPatchFormat(payload);

        assertEquals("expect no change", payload, result);
    }

    @Test
    public void convertStringToPatchFormatNull_Test()
            throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
        GraphInventoryPatchConverter validator = new GraphInventoryPatchConverter();
        String payload = "{\"ipv4-loopback0-address\": null}";
        String result = validator.marshallObjectToPatchFormat(payload);
        System.out.println(result);
        assertEquals("expect no change", payload, result);
    }

    @Test
    public void convertMapToPatchFormatTest()
            throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
        GraphInventoryPatchConverter validator = new GraphInventoryPatchConverter();
        HashMap<String, String> map = new HashMap<>();
        map.put("ipv4-loopback0-address", "");
        map.put("ipv4-loopback1-address", "192.168.1.1");
        String result = validator.marshallObjectToPatchFormat(map);

        assertEquals("expect string", "{\"ipv4-loopback1-address\":\"192.168.1.1\"}", result);
    }

    @Test
    public void hasComplexObjectTest() {
        GraphInventoryPatchConverter validator = new GraphInventoryPatchConverter();
        String hasNesting = "{ \"hello\" : \"world\", \"nested\" : { \"key\" : \"value\" } }";
        String noNesting = "{ \"hello\" : \"world\" }";
        String arrayCase =
                "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"nestedComplex\" : [{\"key\" : \"value\"}]}";
        String empty = "{}";
        String arrayCaseSimpleOnly = "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"]}";
        String relationshipListCaseNesting =
                "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}], \"nested\" : { \"key\" : \"value\" }}";
        String relationshipListCase =
                "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}]}";
        String nothing = "";

        assertTrue("expect has nesting", validator.hasComplexObject(hasNesting));
        assertFalse("expect no nesting", validator.hasComplexObject(noNesting));
        assertTrue("expect has nesting", validator.hasComplexObject(arrayCase));
        assertFalse("expect no nesting", validator.hasComplexObject(empty));
        assertFalse("expect no nesting", validator.hasComplexObject(arrayCaseSimpleOnly));
        assertFalse("expect no nesting", validator.hasComplexObject(relationshipListCase));
        assertTrue("expect has nesting", validator.hasComplexObject(relationshipListCaseNesting));
        assertFalse("expect no nesting", validator.hasComplexObject(nothing));
    }

}
