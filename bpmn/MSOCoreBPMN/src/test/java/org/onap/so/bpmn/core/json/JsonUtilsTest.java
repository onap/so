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

package org.onap.so.bpmn.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.onap.so.exceptions.ValidationException;

public class JsonUtilsTest {

    @Mock
    public DelegateExecution execution;
    @Mock
    public Execution mockEexecution;
    private final String fileLocation = "src/test/resources/json-examples/";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void jsonStringToMapTest() throws IOException {

        JsonUtils utils = new JsonUtils();
        String response = this.getJson("SDNCServiceResponseExample.json");
        String entry = JsonUtils.getJsonValue(response, "SDNCServiceResponse.params");
        Map<String, String> map = utils.jsonStringToMap(execution, entry);
        assertEquals(map.get("e2e-vpn-key"), "my-key");
    }

    @Test
    public void entryArrayToMapTest() throws IOException {
        JsonUtils utils = new JsonUtils();
        String response = this.getJson("SNIROExample.json");
        String entry = JsonUtils.getJsonValue(response, "solutionInfo.placementInfo");
        JSONArray arr = new JSONArray(entry);
        JSONObject homingDataJson = arr.getJSONObject(0);
        JSONArray assignmentInfo = homingDataJson.getJSONArray("assignmentInfo");
        Map<String, String> map =
                utils.entryArrayToMap(execution, assignmentInfo.toString(), "variableName", "variableValue");
        assertEquals(map.get("cloudOwner"), "CloudOwner");
    }

    @Test
    public void entryArrayToMapStringTest() throws IOException {
        JsonUtils utils = new JsonUtils();
        String response = this.getJson("SNIROExample.json");
        String entry = JsonUtils.getJsonValue(response, "solutionInfo.placementInfo");
        JSONArray arr = new JSONArray(entry);
        JSONObject homingDataJson = arr.getJSONObject(0);
        JSONArray assignmentInfo = homingDataJson.getJSONArray("assignmentInfo");
        Map<String, String> map = utils.entryArrayToMap(assignmentInfo.toString(), "variableName", "variableValue");
        assertEquals(map.get("cloudOwner"), "CloudOwner");
    }

    @Test
    public void entryArrayToMapStringTestOof() throws IOException {
        JsonUtils utils = new JsonUtils();
        String response = this.getJson("OofExample.json");
        String entry = JsonUtils.getJsonValue(response, "solutions.placementSolutions");
        JSONArray arr = new JSONArray(entry);
        JSONArray arr2 = arr.getJSONArray(0);
        JSONObject homingDataJson = arr2.getJSONObject(0);
        JSONArray assignmentInfo = homingDataJson.getJSONArray("assignmentInfo");
        Map<String, String> map = utils.entryArrayToMap(assignmentInfo.toString(), "key", "value");
        assertEquals(map.get("cloudOwner"), "HPA-cloud");
    }

    @Test
    public void getJsonRootPropertyTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        assertEquals("SDNCServiceResponse", JsonUtils.getJsonRootProperty(response));
    }

    @Test
    public void getJsonRootProperty_ExceptionTest() throws IOException {
        expectedException.expect(JSONException.class);
        String response = this.getJson("SNIROExample.json");
        String root = JsonUtils.getJsonRootProperty(response);
    }

    @Test
    public void getJsonNodeValueTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        String code = JsonUtils.getJsonNodeValue(response, "SDNCServiceResponse.responseCode");
        assertEquals("200", code);
    }

    @Test
    public void stringArrayToList_jsonStringTest() throws IOException {
        String response = this.getJson("SNIROExample.json");
        String licenseInfo = JsonUtils.getJsonNodeValue(response, "solutionInfo.licenseInfo");
        JsonUtils utils = new JsonUtils();
        List<String> listString = utils.StringArrayToList(licenseInfo);
        assertNotNull(listString.get(0));
    }

    @Test
    public void stringArrayToList_JSONArrayTest() throws IOException {
        String response = this.getJson("SNIROExample.json");
        String licenseInfo = JsonUtils.getJsonNodeValue(response, "solutionInfo.licenseInfo");
        JSONArray jsonArray = new JSONArray(licenseInfo);
        JsonUtils utils = new JsonUtils();
        List<String> listString = utils.StringArrayToList(jsonArray);
        assertNotNull(listString.get(0));
    }

    @Test
    public void stringArrayToList_withExecutionTest() throws IOException {
        String response = this.getJson("SNIROExample.json");
        String licenseInfo = JsonUtils.getJsonNodeValue(response, "solutionInfo.licenseInfo");
        JsonUtils utils = new JsonUtils();
        List<String> listString = utils.StringArrayToList(mockEexecution, licenseInfo);
        assertNotNull(listString.get(0));
    }

    @Test
    public void jsonElementExist_trueTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        boolean isExist = JsonUtils.jsonElementExist(response, "SDNCServiceResponse.responseCode");
        assertEquals(true, isExist);
    }

    @Test
    public void jsonElementExist_falseTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        boolean isExist = JsonUtils.jsonElementExist(response, "SDNCServiceResponse.responseX");
        assertEquals(false, isExist);
    }

    @Test
    public void jsonElementExist_NullTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        boolean isExist = JsonUtils.jsonElementExist(response, null);
        assertEquals(true, isExist);
    }

    @Test
    public void jsonSchemaValidation_ExceptionTest() throws IOException, ValidationException {
        expectedException.expect(ValidationException.class);
        String response = this.getJson("SDNCServiceResponseExample.json");
        String isExist = JsonUtils.jsonSchemaValidation(response, fileLocation);
    }

    @Test
    public void getJsonIntValueTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        int intValue = JsonUtils.getJsonIntValue(response, "SDNCServiceResponse.responseCode");
        assertEquals(0, intValue);
    }

    @Test
    public void getJsonBooleanValueTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        boolean isBoolean = JsonUtils.getJsonBooleanValue(response, "SDNCServiceResponse.responseCode");
        assertEquals(false, isBoolean);
    }

    @Test
    public void prettyJsonTest() throws IOException {
        String response = this.getJson("SNIROExample.json");
        assertNotNull(JsonUtils.prettyJson(response));
        String malformedJson = "{\"name\" \"myName\"}";
        assertNull(JsonUtils.prettyJson(malformedJson));
    }

    @Test
    public void xml2jsonTest() {
        String expectedJson = "{\"name\":\"myName\"}";
        String xml = "<name>myName</name>";
        assertEquals(expectedJson, JsonUtils.xml2json(xml, false));
    }

    @Test
    public void xml2jsonErrorTest() {
        String malformedXml = "<name>myName<name>";
        assertNull(JsonUtils.xml2json(malformedXml));
    }

    @Test
    public void json2xmlTest() {
        String expectedXml = "<name>myName</name>";
        String malformedJson = "{\"name\":\"myName\"}";
        assertEquals(expectedXml, JsonUtils.json2xml(malformedJson, false));
    }

    @Test
    public void json2xmlErrorTest() {
        String malformedJson = "{\"name\" \"myName\"}";
        assertNull(JsonUtils.json2xml(malformedJson));
    }

    @Test
    public void getJsonValueErrorTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        assertNull(JsonUtils.getJsonValue(response, null));
    }

    @Test
    public void getJsonNodeValueErrorTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        assertNull(JsonUtils.getJsonNodeValue(response, null));
    }

    @Test
    public void getJsonIntValueErrorTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        assertEquals(0, JsonUtils.getJsonIntValue(response, null));
    }

    @Test
    public void getJsonBooleanValueErrorTest() throws IOException {
        String response = this.getJson("SDNCServiceResponseExample.json");
        assertEquals(false, JsonUtils.getJsonBooleanValue(response, null));
    }

    @Test
    public void getJsonValueForKeyErrorTest() {
        String malformedJson = "{\"name\" \"myName\"}";
        assertNull(JsonUtils.getJsonValueForKey(malformedJson, "name"));
    }

    @Test
    public void updJsonValueTest() {
        String expectedJson = "{\"name\": \"yourName\"}";
        String json = "{\"name\":\"myName\"}";
        assertEquals(expectedJson, JsonUtils.updJsonValue(json, "name", "yourName"));
    }

    @Test
    public void updJsonValueErrorTest() {
        String expectedJson = "{\"name\" \"myName\"}";
        String json = "{\"name\" \"myName\"}";
        assertEquals(expectedJson, JsonUtils.updJsonValue(json, "name", "yourName"));
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
    }
}
