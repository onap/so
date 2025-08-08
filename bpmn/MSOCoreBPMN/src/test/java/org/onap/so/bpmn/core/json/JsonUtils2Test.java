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


import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.bpmn.core.xml.XmlTool;
import org.onap.so.exceptions.ValidationException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

/**
 * @version 1.0
 */
public class JsonUtils2Test {

    private static final String EOL = "\n";
    private static final String XML_REQ = "<vnf-request xmlns=\"http://org.onap/so/infra/vnf-request/v1\">" + EOL
            + "  <request-info>" + EOL + "    <request-id>DEV-VF-0021</request-id>" + EOL
            + "    <action>CREATE_VF_MODULE</action>" + EOL + "    <source>PORTAL</source>" + EOL + "  </request-info>"
            + EOL + "  <vnf-inputs>" + EOL + "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL
            + "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL + "    <vnf-type>asc_heat-int</vnf-type>" + EOL
            + "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL
            + "    <vf-module-model-name>STMTN5MMSC21-MMSC::model-1-0</vf-module-model-name>" + EOL
            + "    <is-base-module>true</is-base-module>" + EOL
            + "    <persona-model-id>00000000-0000-0000-0000-000000000000</persona-model-id>" + EOL
            + "    <persona-model-version>1.0</persona-model-version>" + EOL
            + "    <vnf-persona-model-id>999999999-0000-0000-0000-000000000000</vnf-persona-model-id>" + EOL
            + "    <vnf-persona-model-version>1.5</vnf-persona-model-version>" + EOL
            + "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL
            + "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL
            + "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL
            + "    <orchestration-status>pending-delete</orchestration-status>" + EOL
            + "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL
            + "    <asdc-service-model-version>1</asdc-service-model-version>" + EOL + "  </vnf-inputs>" + EOL
            + "  <vnf-params xmlns:tns=\"http://org.onap/so/infra/vnf-request/v1\">" + EOL
            + "    <param name=\"network\">network1111</param>" + EOL + "    <param name=\"server\">server1111</param>"
            + EOL + "  </vnf-params> " + EOL + "</vnf-request>" + EOL;

    private static final String XML_REQ_NO_ATTRS = "<vnf-request xmlns=\"http://org.onap/so/infra/vnf-request/v1\">"
            + EOL + "  <request-info>" + EOL + "    <action>DELETE_VF_MODULE</action>" + EOL
            + "    <source>PORTAL</source>" + EOL + "  </request-info>" + EOL + "  <vnf-inputs>" + EOL
            + "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL
            + "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL + "    <vnf-type>asc_heat-int</vnf-type>" + EOL
            + "    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL
            + "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL
            + "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL
            + "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL
            + "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL
            + "    <orchestration-status>pending-delete</orchestration-status>" + EOL
            + "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL + "  </vnf-inputs>" + EOL
            + "  <vnf-params xmlns:tns=\"http://org.onap/so/infra/vnf-request/v1\"/>" + EOL + "</vnf-request>" + EOL;

    private static final String XML_ARRAY_REQ =
            "<ucpeInfo>" + EOL + "       <outOfBandManagementModem>BROADBAND</outOfBandManagementModem>" + EOL
                    + "   <internetTopology>IVLAN</internetTopology>" + EOL
                    + "   <ucpeAliasHostName>SHELLUCPE31</ucpeAliasHostName>" + EOL + "   <wanList>" + EOL
                    + "           <wanInfo>" + EOL + "                   <wanType>AVPN</wanType>" + EOL
                    + "                   <interfaceType>1000BASE-T</interfaceType>" + EOL
                    + "                   <transportProviderName>ATT</transportProviderName>" + EOL
                    + "                   <circuitId>BT/SLIR/70911</circuitId>" + EOL
                    + "                   <dualMode>Active</dualMode>" + EOL
                    + "                   <wanPortNumber>WAN1</wanPortNumber>" + EOL
                    + "                   <transportManagementOption>ATT</transportManagementOption>" + EOL
                    + "                   <transportVendorTotalBandwidth>100</transportVendorTotalBandwidth>" + EOL
                    + "                   <mediaType>ELECTRICAL</mediaType>" + EOL + "           </wanInfo>" + EOL
                    + "           <wanInfo>" + EOL + "                   <wanType>AVPN</wanType>" + EOL
                    + "                   <interfaceType>10/100/1000BASE-T</interfaceType>" + EOL
                    + "                   <transportProviderName>ATT</transportProviderName>" + EOL
                    + "                   <circuitId>AS/KRFN/34611</circuitId>" + EOL
                    + "                   <dualMode>Active</dualMode>" + EOL
                    + "                   <wanPortNumber>WAN2</wanPortNumber>" + EOL
                    + "                   <transportManagementOption>ATT</transportManagementOption>" + EOL
                    + "                   <transportVendorTotalBandwidth>10000</transportVendorTotalBandwidth>" + EOL
                    + "                   <mediaType>MMF</mediaType>" + EOL + "           </wanInfo>" + EOL
                    + "   </wanList>" + EOL + "   <ucpeActivationCode>ASD-987-M31</ucpeActivationCode>" + EOL
                    + "   <ucpeHostName>USOSTCDALTX0101UJZZ31</ucpeHostName>" + EOL
                    + "   <ucpePartNumber>FG-VM00*</ucpePartNumber>" + EOL + "</ucpeInfo>";

    // JSON request w/ embedded XML will be read from a file
    private static String jsonReq;
    private static String jsonReqArray;

    @BeforeClass
    public static void initialize() throws Exception {
        jsonReq = readFileToString("src/test/resources/request.json");
        jsonReqArray = readFileToString("src/test/resources/requestArray.json");
    }

    private static String readFileToString(String path) throws IOException {
        File file = new File(path);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void shouldConvertXmlToJsonAndBackToSameXml() {
        // Note: the current version of the JsonUtils.json2xml() method
        // does not support converting the JSONObject representation
        // of XML attributes (JSONArray) back to XML. So this test will
        // only succeed if the original XML does not contain attributes

        // given
        String xmlIn = XmlTool.removeNamespaces(XML_REQ_NO_ATTRS);
        // when
        String json = JsonUtils.xml2json(XML_REQ_NO_ATTRS);
        String xmlOut = JsonUtils.json2xml(json);
        // then
        Diff diffXml = DiffBuilder.compare(xmlIn).withTest(xmlOut).ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).checkForSimilar().build();

        assertFalse(diffXml.hasDifferences());
    }

    @Test
    public void shouldReadValuesForAbsoluteJsonPaths() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        // when, then
        assertEquals("STMTN5MMSC21", JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.vnf-name"));
        assertEquals("CREATE_VF_MODULE", JsonUtils.getJsonValue(json, "vnf-request.request-info.action"));
        assertEquals("1", JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.persona-model-version"));
        assertEquals("1.5", JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.vnf-persona-model-version"));
        assertEquals("true", JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.is-base-module"));
    }

    @Test
    public void shouldReturnValueForJsonKey() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        // when, then
        assertEquals("PORTAL", JsonUtils.getJsonValueForKey(json, "source"));
    }

    @Test
    public void shouldReturnNullForNonexistentJsonNode() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        // when, then
        assertNull(JsonUtils.getJsonValueForKey(json, "nonexistent-node"));
    }

    @Test
    public void shouldReturnNullForNonExistentParameter() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        // when, then
        assertNull(JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.bad"));
    }

    @Test
    public void shouldGetJasonParametersFromArray() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        // when, then
        assertEquals("network", JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name"));
        assertEquals("network1111", JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "content"));
        assertEquals("server", JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name", 1));
        assertEquals("server1111", JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "content", 1));
        assertNull(JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "badParam"));
        assertNull(JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name", 2));
    }

    @Test
    public void shouldAddJsonValue() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        String key = "vnf-request.request-info.comment";
        String value = "Some comment";
        // when
        String jsonUpd = JsonUtils.addJsonValue(json, key, value);
        // then
        String extractedValue = JsonUtils.getJsonValue(jsonUpd, key);
        assertEquals(value, extractedValue);
    }

    @Test
    public void shouldIgnoreAddIfFieldAlreadyExists() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        String key = "vnf-request.vnf-inputs.vnf-name";
        String newValue = "STMTN5MMSC22";
        // when
        String jsonUpd = JsonUtils.addJsonValue(json, key, newValue);
        // then
        String extractedValue = JsonUtils.getJsonValue(jsonUpd, key);
        assertNotEquals(newValue, extractedValue);
    }

    @Test
    public void shouldUpdateValueInJson() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        String key = "vnf-request.vnf-inputs.vnf-name";
        String newValue = "STMTN5MMSC22";
        String oldValue = JsonUtils.getJsonValue(json, key);
        // when
        String jsonUpd = JsonUtils.updJsonValue(json, key, newValue);
        // then
        String extractedValue = JsonUtils.getJsonValue(jsonUpd, key);
        assertEquals(newValue, extractedValue);
    }

    @Test
    public void shouldDeleteValue() throws Exception {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        String key = "vnf-request.vnf-inputs.vnf-name";
        // when
        String jsonUpd = JsonUtils.delJsonValue(json, key);
        // then
        String extractedValue = JsonUtils.getJsonValue(jsonUpd, key);
        assertNull(extractedValue);
        JSONObject jsonObj = new JSONObject(json);
        Integer intValue = JsonUtils.getJsonIntValueForKey(jsonObj, "persona-model-version");
        Assert.assertTrue(intValue == 1);
        Boolean boolValue = JsonUtils.getJsonBooleanValueForKey(jsonObj, "is-base-module");
        Assert.assertTrue(boolValue);
    }

    @Test
    public void shouldReturnOriginalJsonWhenTryingToRemoveNonexistentField() {
        // given
        String json = JsonUtils.xml2json(XML_REQ);
        String key = "vnf-request.vnf-inputs.does-not-exist";
        // when
        String jsonUpd = JsonUtils.delJsonValue(json, key);
        // then
        assertEquals(json, jsonUpd);
    }

    @Test
    public void shouldConvertXmlToJsonAndBackToSameXmlExtractedFromTheRequest() throws Exception {
        // given
        String value = JsonUtils.getJsonValue(jsonReq, "variables.bpmnRequest.value");
        String xmlReq = XmlTool.removeNamespaces(XmlTool.normalize(value));
        // when
        String json = JsonUtils.xml2json(xmlReq);
        String xmlOut = JsonUtils.json2xml(json);
        // then
        Diff diffXml = DiffBuilder.compare(xmlReq).withTest(xmlOut).ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).checkForSimilar().build();
        assertFalse(diffXml.hasDifferences());
    }

    @Test
    public void shouldConvertJsonContainingArrayToXml() {
        // when
        String jsonParm = JsonUtils.getJsonNodeValue(jsonReqArray, "requestDetails.requestParameters.ucpeInfo");
        String xmlOut = JsonUtils.json2xml(jsonParm);
        // then
        Diff diffXml = DiffBuilder.compare(XML_ARRAY_REQ).withTest(xmlOut).ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).checkForSimilar().build();
        assertFalse(diffXml.hasDifferences());
    }

    @Test
    // Tests the jsonSchemaValidation() method
    public void testJsonSchemaValidation() {
        try {
            String myReqArray = jsonReqArray;
            String result = JsonUtils.jsonSchemaValidation(myReqArray, "src/test/resources/requestSchema.json");
            System.out.println("Schema Validation Result: " + result);
            Assert.assertTrue(result.contains("success"));
            // remove a required parameter from the JSON doc so that validation fails
            myReqArray = JsonUtils.delJsonValue(myReqArray, "requestDetails.requestParameters.ucpeInfo.ucpeHostName");
            result = JsonUtils.jsonSchemaValidation(myReqArray, "src/test/resources/requestSchema.json");
            System.out.println("Schema Validation Result: " + result);
            Assert.assertTrue(result.contains("failure"));
            Assert.assertTrue(result.contains("error: object has missing required properties ([\"ucpeHostName\"])"));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
