/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Optional;
import org.junit.Test;

public class XmlToolTest {

    private String response = "<Response>good</Response>";
    private String responseModified = "<Response>veryGood</Response>";
    private String encodeResponse = "&lt;Response&gt;good&lt;/Response&gt;";
    private String encodeResponseNamespace =
            "<Response xmlns:ns2=\"http://ecomp.att.com/mso/request/types/v1\">good</Response>";
    private String attribute = "<Response>good&\"bad\"</Response>";
    private String updatedAttribute = "&lt;Response&gt;good&amp;&quot;bad&quot;&lt;/Response&gt;";

    private String content = "<dummy><configuration-event>" + "<event-type>test</event-type>"
            + "<event-correlator-type>test</event-correlator-type>" + "<event-correlator>123</event-correlator>"
            + "<event-parameters><event-parameter>" + "<tag-name>test</tag-name>"
            + "<tag-value>test</tag-value></event-parameter></event-parameters>" + "</configuration-event></dummy>";

    @Test
    public void test() throws Exception {
        Object xmlMessage = new String(response);
        String xmlResponse = XmlTool.normalize(xmlMessage);
        assertEquals(response, xmlResponse.toString());
        String xmlEncode = XmlTool.encode(xmlMessage);
        assertEquals(encodeResponse, xmlEncode.toString());
        Optional<String> optXml = XmlTool.modifyElement(response, "Response", "veryGood");
        Object obj1 = new String(optXml.get().toString());
        String noPreamble = XmlTool.removePreamble(obj1);
        assertEquals(responseModified, noPreamble.toString());
        Object obj2 = new String(encodeResponseNamespace);
        String noNamespace = XmlTool.removeNamespaces(obj2);
        assertEquals(response, noNamespace.toString());
        Object obj3 = new String(attribute);

        assertEquals(null, XmlTool.normalize(null));
        assertEquals(null, XmlTool.encode(null));
        assertEquals(null, XmlTool.removePreamble(null));
        assertEquals(null, XmlTool.removeNamespaces(null));
        assertEquals(Optional.empty(), XmlTool.modifyElement(null, "Response", "veryGood"));
    }

    @Test
    public void normalizeTest() throws Exception {
        String response = XmlTool.normalize(content);
        assertNotNull(response);
    }

    @Test
    public void modifyElementTest() throws Exception {
        String response = XmlTool.modifyElement(content, "event-type", "uCPE-VMS").get();
        assertNotNull(response);
    }
}
