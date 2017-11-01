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

package org.openecomp.mso.bpmn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.Diff;

import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.bpmn.core.xml.XmlTool;

/**
 * @version 1.0
 */
public class JsonUtilsTest {

    private static final String EOL = "\n";
    private String xmlReq =
                    "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                    "  <request-info>" + EOL +
                    "    <request-id>DEV-VF-0021</request-id>" + EOL +
                    "    <action>CREATE_VF_MODULE</action>" + EOL +
                    "    <source>PORTAL</source>" + EOL +
                    "  </request-info>" + EOL +
                    "  <vnf-inputs>" + EOL +
                    "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
                    "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
                    "    <vnf-type>asc_heat-int</vnf-type>" + EOL +
                    "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
                    "    <vf-module-model-name>STMTN5MMSC21-MMSC::model-1-0</vf-module-model-name>" + EOL +
                    "    <is-base-module>true</is-base-module>" + EOL +
                    "    <persona-model-id>00000000-0000-0000-0000-000000000000</persona-model-id>" + EOL +
                    "    <persona-model-version>1.0</persona-model-version>" + EOL +
                    "    <vnf-persona-model-id>999999999-0000-0000-0000-000000000000</vnf-persona-model-id>" + EOL +
                    "    <vnf-persona-model-version>1.5</vnf-persona-model-version>" + EOL +
                    "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
                    "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
                    "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
                    "    <orchestration-status>pending-delete</orchestration-status>" + EOL +
                    "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
                    "    <asdc-service-model-version>1</asdc-service-model-version>" + EOL +
                    "  </vnf-inputs>" + EOL +
                    "  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                    "    <param name=\"network\">network1111</param>" + EOL +
                    "    <param name=\"server\">server1111</param>" + EOL +
                    "  </vnf-params> " + EOL +
                    "</vnf-request>" + EOL;
    
    private String xmlReqNoAttrs =
                    "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                    "  <request-info>" + EOL +
                    "    <action>DELETE_VF_MODULE</action>" + EOL +
                    "    <source>PORTAL</source>" + EOL +
                    "  </request-info>" + EOL +
                    "  <vnf-inputs>" + EOL +
                    "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
                    "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
                    "    <vnf-type>asc_heat-int</vnf-type>" + EOL +
                    "    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
                    "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
                    "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
                    "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
                    "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
                    "    <orchestration-status>pending-delete</orchestration-status>" + EOL +
                    "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
                    "  </vnf-inputs>" + EOL +
                    "  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\"/>" + EOL +
                    "</vnf-request>" + EOL;

    private String xmlArrayReq =
                    "<ucpeInfo>" + EOL +
                    "       <outOfBandManagementModem>BROADBAND</outOfBandManagementModem>" + EOL +
                "   <internetTopology>IVLAN</internetTopology>" + EOL +
                "   <ucpeAliasHostName>SHELLUCPE31</ucpeAliasHostName>" + EOL +
                "   <wanList>" + EOL +
                "           <wanInfo>" + EOL +
                "                   <wanType>AVPN</wanType>" + EOL +
                "                   <interfaceType>1000BASE-T</interfaceType>" + EOL +
                "                   <transportProviderName>ATT</transportProviderName>" + EOL +
                "                   <circuitId>BT/SLIR/70911</circuitId>" + EOL +
                "                   <dualMode>Active</dualMode>" + EOL +
                "                   <wanPortNumber>WAN1</wanPortNumber>" + EOL +
                "                   <transportManagementOption>ATT</transportManagementOption>" + EOL +
                "                   <transportVendorTotalBandwidth>100</transportVendorTotalBandwidth>" + EOL +
                "                   <mediaType>ELECTRICAL</mediaType>" + EOL +
                "           </wanInfo>" + EOL +
                "           <wanInfo>" + EOL +
                "                   <wanType>AVPN</wanType>" + EOL +
                "                   <interfaceType>10/100/1000BASE-T</interfaceType>" + EOL +
                "                   <transportProviderName>ATT</transportProviderName>" + EOL +
                "                   <circuitId>AS/KRFN/34611</circuitId>" + EOL +
                "                   <dualMode>Active</dualMode>" + EOL +
                "                   <wanPortNumber>WAN2</wanPortNumber>" + EOL +
                "                   <transportManagementOption>ATT</transportManagementOption>" + EOL +
                "                   <transportVendorTotalBandwidth>10000</transportVendorTotalBandwidth>" + EOL +
                "                   <mediaType>MMF</mediaType>" + EOL +
                "           </wanInfo>" + EOL +
                "   </wanList>" + EOL +
                "   <ucpeActivationCode>ASD-987-M31</ucpeActivationCode>" + EOL +
                "   <ucpeHostName>USOSTCDALTX0101UJZZ31</ucpeHostName>" + EOL +
                "   <ucpePartNumber>FG-VM00*</ucpePartNumber>" + EOL +
                    "</ucpeInfo>";

    // JSON request w/ embedded XML will be read from a file
    private String jsonReq = null;
    private String jsonReqArray = null;
    
    @Before
    public void initialize() {
            File file = new File("src/test/resources/request.json");
            File file2 = new File("src/test/resources/requestArray.json");
            FileInputStream fis = null;

            try {
                    fis = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    CharBuffer cbuf = CharBuffer.allocate((int)file.length()+1);
                    br.read(cbuf);
                    cbuf.flip();
                    jsonReq = cbuf.toString();
                    if (jsonReq != null) {
                            System.out.println("initialize(): json request: " + jsonReq);
                    } else {
                            System.out.println("initialize(): failed to read json request from src/test/resources/request.json");
                    }
                    fis.close();
                    fis = new FileInputStream(file2);
                    br = new BufferedReader(new InputStreamReader(fis));
                    cbuf = CharBuffer.allocate((int)file.length()+1);
                    br.read(cbuf);
                    cbuf.flip();
                    jsonReqArray = cbuf.toString();
                    if (jsonReq != null) {
                            System.out.println("initialize(): json request w/ array: " + jsonReqArray);
                    } else {
                            System.out.println("initialize(): failed to read json request from src/test/resources/request2.json");
                    }
            } catch (IOException e) {
                    e.printStackTrace();
            } finally {
                    try {
                            if (fis != null)
                                    fis.close();
                    } catch (IOException ex) {
                            ex.printStackTrace();
                    }
            }
    }
    
    @After
    public void cleanup(){
    }

    @Test
//  @Ignore
    public void testConversion() {
            // Note: the current version of the JsonUtils.json2xml() method
            // does not support converting the JSONObject representation
            // of XML attributes (JSONArray) back to XML. So this test will
            // only succeed if the original XML does not contain attributes
            
            // save a copy of the xml with the namespaces removed
            String xmlIn = XmlTool.removeNamespaces(xmlReqNoAttrs);
            // strip all the non-data whitespace
            xmlIn = xmlIn.replaceAll(">\\s*<", "><");
            String json = JsonUtils.xml2json(xmlReqNoAttrs);
            System.out.println("testConversion(): xml request to json: " + json);
            String xmlOut = JsonUtils.json2xml(json);
            System.out.println("testConversion(): json request back to xml: " + xmlOut);
            
            // strip all the non-data whitespace
            xmlOut = xmlOut.replaceAll(">\\s*<", "><");

            Diff diffXml;
            try {
                    diffXml = new Diff(xmlIn, xmlOut);
                    Assert.assertTrue(diffXml.similar());
            } catch (SAXException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }
    }

    @Test
//  @Ignore
    public void testRetrieval() {
            String json = JsonUtils.xml2json(xmlReq);
            System.out.println("testRetrieval(): xml request to json: " + json);
            // full JSON path
            String value = JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.vnf-name");
            Assert.assertEquals(value, "STMTN5MMSC21");
            value = JsonUtils.getJsonValue(json, "vnf-request.request-info.action");
            Assert.assertEquals(value, "CREATE_VF_MODULE");
            // retrieving an integer
            value = JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.persona-model-version");
            Assert.assertEquals(value, "1");
            // retrieving a float
            value = JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.vnf-persona-model-version");
            Assert.assertEquals(value, "1.5");
            // retrieving a boolean
            value = JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.is-base-module");
            Assert.assertEquals(value, "true");
            // attempt to retrieve a value for a non-existent field
            value = JsonUtils.getJsonValue(json, "vnf-request.vnf-inputs.bad");
            Assert.assertEquals(value, null);
            // retrieving a parameter/array value (originally a XML attribute)
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name");
            Assert.assertEquals(value, "network");
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "content");
            Assert.assertEquals(value, "network1111");
            // retrieving a parameter/array value by index
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "content", 1);
            Assert.assertEquals(value, "server1111");
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name", 1);
            Assert.assertEquals(value, "server");
            // failure due to invalid parameter name
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "badParam");
            Assert.assertEquals(value, null);
            // failure due to array index out of bounds
            value = JsonUtils.getJsonParamValue(json, "vnf-request.vnf-params.param", "name", 2);
            Assert.assertEquals(value, null);
            // by field name/key
            value = JsonUtils.getJsonValueForKey(json, "source");
            Assert.assertEquals(value, "PORTAL");
            value = JsonUtils.getJsonValueForKey(json, "vnf-module");
            Assert.assertEquals(value, null);       
    }

    @Test
//  @Ignore
    public void testUpdate() {
            String json = JsonUtils.xml2json(xmlReq);
            System.out.println("testUpdate(): xml request to json: " + json);
            // the add should be successful
            String jsonUpd = JsonUtils.addJsonValue(json, "vnf-request.request-info.comment", "Some comment");
            String value = JsonUtils.getJsonValue(jsonUpd, "vnf-request.request-info.comment");
            Assert.assertEquals(value, "Some comment");
            // the add should be ignored as the field already exists
            jsonUpd = JsonUtils.addJsonValue(jsonUpd, "vnf-request.vnf-inputs.vnf-name", "STMTN5MMSC22");
            value = JsonUtils.getJsonValue(jsonUpd, "vnf-request.vnf-inputs.vnf-name");
            Assert.assertEquals(value, "STMTN5MMSC21");
            // the update should be successful
            jsonUpd = JsonUtils.updJsonValue(jsonUpd, "vnf-request.vnf-inputs.vnf-name", "STMTN5MMSC22");
            value = JsonUtils.getJsonValue(jsonUpd, "vnf-request.vnf-inputs.vnf-name");
            Assert.assertEquals(value, "STMTN5MMSC22");
            // the delete should be successful
            jsonUpd = JsonUtils.delJsonValue(jsonUpd, "vnf-request.request-info.comment");
            value = JsonUtils.getJsonValue(jsonUpd, "vnf-request.request-info.comment");
            Assert.assertEquals(value, null);
            // the delete should fail as field 'vnf-model' does not exist
            String jsonCur = jsonUpd;
            jsonUpd = JsonUtils.delJsonValue(jsonUpd, "vnf-request.vnf-inputs.vnf-module");
            Assert.assertEquals(jsonCur, jsonUpd);          
    }
    
    @Test
//  @Ignore
    public void testEmbededXmlRetrievalConversion() {
            try {
                    // extract the embedded XML from the request
                    String value = JsonUtils.getJsonValue(jsonReq, "variables.bpmnRequest.value");
                    String xmlReq = XmlTool.removeNamespaces(XmlTool.normalize(value));
                    System.out.println("testEmbededXmlRetrievalConversion(): xml payload: " + xmlReq);
                    String json = JsonUtils.xml2json(xmlReq);
                    System.out.println("testEmbededXmlRetrievalConversion(): xml request to json: " + json);
                    String xmlOut = JsonUtils.json2xml(json);
                    System.out.println("testEmbededXmlRetrievalConversion(): json request back to xml: " + xmlOut);
                    Diff diffXml;
                    try {
                            // compare the XML before and after
                            diffXml = new Diff(xmlReq, xmlOut);
                            Assert.assertTrue(diffXml.similar());
                    } catch (SAXException e) {
                            e.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                    
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }

    @Test
//  @Ignore
    // Tests the conversion of a JSON Doc containing a JSON Array to XML
    public void testConversionArray() {
            try {
                    String jsonParm = JsonUtils.getJsonNodeValue(jsonReqArray, "requestDetails.requestParameters.ucpeInfo");
                    System.out.println("testConversionArray(): json value: " + JsonUtils.prettyJson(jsonParm));
                    String xmlOut = JsonUtils.json2xml(jsonParm);
                    System.out.println("testConversionArray(): json parameters to xml: " + xmlOut);
                    // strip all the non-data whitespace
                    xmlOut = xmlOut.replaceAll(">\\s*<", "><");
                    System.out.println("testConversionArray(): XML after removing whitespace:" + xmlOut);
                    String xmlTest = xmlArrayReq.replaceAll(">\\s*<", "><");

                    Diff diffXml;
                    try {
                            diffXml = new Diff(xmlTest, xmlOut);
                          Assert.assertTrue(diffXml.similar());
                    } catch (SAXException e) {
                            e.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }
}
