/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MsoXmlContractTest {
    private static final String NS = "urn:onap:so:test";
    private final MsoUtils utility = new MsoUtils();

    @Test
    public void textLookupDistinguishesAbsentEmptyAndEscapedContent() {
        String input = "<r:root xmlns:r='" + NS + "'><r:name>Grüße &amp; &lt;SO&gt;</r:name><r:empty/></r:root>";
        assertEquals("Grüße & <SO>", utility.getNodeText(input, "name"));
        assertEquals("", utility.getNodeText(input, "empty"));
        assertNull(utility.getNodeText(input, "absent"));
        assertNull(utility.getNodeText(null, "name"));
    }

    @Test
    public void repeatedTextRetainsDocumentOrderAndSingletonCardinality() {
        String input = "<root><item>first</item><item/><item>Grüße</item><only>one</only></root>";
        assertEquals(Arrays.asList("first", "", "Grüße"), utility.getMultNodes(input, "item"));
        assertEquals(Collections.singletonList("one"), utility.getMultNodes(input, "only"));
        assertEquals(Collections.emptyList(), utility.getMultNodes(input, "absent"));
        assertNull(utility.getMultNodes(null, "item"));
    }

    @Test
    public void extractingContainerPreservesNamespacesEscapingAndOptionalNodes() throws Exception {
        String input = "<r:root xmlns:r='" + NS + "'><r:payload><r:item key='a&amp;b'>"
                + "Grüße &amp; &lt;SO&gt;</r:item><r:item/><r:enabled>false</r:enabled>"
                + "<r:count>0</r:count></r:payload></r:root>";
        String output = (String) utility.getNodeXml(input, "payload", false);
        assertFalse(output.startsWith("<?xml"));
        Document document = xml(output);
        assertEquals(NS, document.getDocumentElement().getNamespaceURI());
        assertEquals("payload", document.getDocumentElement().getLocalName());
        assertEquals(2, document.getElementsByTagNameNS(NS, "item").getLength());
        Element item = (Element) document.getElementsByTagNameNS(NS, "item").item(0);
        assertEquals("a&b", item.getAttribute("key"));
        assertEquals("Grüße & <SO>", item.getTextContent());
        assertEquals("", document.getElementsByTagNameNS(NS, "item").item(1).getTextContent());
        assertEquals("false", document.getElementsByTagNameNS(NS, "enabled").item(0).getTextContent());
        assertEquals("0", document.getElementsByTagNameNS(NS, "count").item(0).getTextContent());
        assertEquals(0, document.getElementsByTagNameNS(NS, "optional").getLength());
    }

    @Test
    public void missingXmlNodeReturnsEmptyString() {
        assertEquals("", utility.getNodeXml("<root/>", "absent", false));
    }

    @Test
    public void childExtractionKeepsAllSiblingsWithTheirNamespaces() throws Exception {
        String input = "<root xmlns:r='" + NS + "'><r:payload><r:item>one</r:item>"
                + "<r:item>two &amp; three</r:item></r:payload></root>";
        Document document = xml("<result>" + utility.getChildNodes(input, "payload") + "</result>");
        assertEquals(2, document.getElementsByTagNameNS(NS, "item").getLength());
        assertEquals("one", document.getElementsByTagNameNS(NS, "item").item(0).getTextContent());
        assertEquals("two & three", document.getElementsByTagNameNS(NS, "item").item(1).getTextContent());
        assertEquals("", utility.getChildNodes(input, "absent"));
    }

    @Test
    public void escapingRoundTripsTextWithoutConvertingBooleanOrZeroToEmpty() throws Exception {
        for (Object value : Arrays.asList("Grüße & <SO> \"quoted\"", false, 0, "")) {
            Document document = xml("<value>" + MsoUtils.xmlEscape(value) + "</value>");
            assertEquals(String.valueOf(value), document.getDocumentElement().getTextContent());
        }
    }

    @Test
    public void nodePresenceDistinguishesEmptyFromMissing() {
        assertTrue((Boolean) utility.nodeExists("<root><empty/></root>", "empty"));
        assertFalse((Boolean) utility.nodeExists("<root/>", "empty"));
    }

    private static Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
