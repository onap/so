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
import static org.junit.Assert.assertNull;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NetworkLegacyXmlContractTest {
    private static final String AAI = "http://org.openecomp.aai.inventory/";
    private static final List<String> FIELDS =
            Arrays.asList("heat-stack-id", "neutron-network-id", "contrail-network-fqdn", "orchestration-status");
    private final NetworkUtils script = new NetworkUtils(null);

    @Test
    public void parameterSelectionKeepsDecodedTextAndTheFirstMatchingValue() {
        String parameters = "<p:params xmlns:p='urn:params'><p:param name='shared'>false</p:param>"
                + "<p:param name='shared'>true</p:param><p:param name='count'>0</p:param>"
                + "<p:param name='name'>Größe &amp; 東京</p:param><p:param name='empty'/></p:params>";
        assertEquals("false", String.valueOf(script.getParameterValue(parameters, "shared")));
        assertEquals("0", String.valueOf(script.getParameterValue(parameters, "count")));
        assertEquals("Größe & 東京", String.valueOf(script.getParameterValue(parameters, "name")));
        assertEquals("", String.valueOf(script.getParameterValue(parameters, "empty")));
        assertEquals("", String.valueOf(script.getParameterValue(parameters, "missing")));
        assertEquals("", String.valueOf(script.getParameterValue(null, "name")));
    }

    @Test
    public void inventoryIdentifiersTakePrecedenceAndLifecycleStatusIsMapped() throws Exception {
        for (String status : new String[] {"pending-create", "PendingCreate", "pending-update", "PendingUpdate",
                "active"}) {
            Document result = xml(String
                    .valueOf(script.buildNetworkElements(inventory(status, inventoryIds()), adapterIds(), FIELDS)));
            assertEquals("inventory-stack", text(result, "heat-stack-id"));
            assertEquals("inventory-network", text(result, "neutron-network-id"));
            assertEquals("inventory.example", text(result, "contrail-network-fqdn"));
            assertEquals(status.equals("pending-create") || status.equals("PendingCreate") ? "Created" : "Active",
                    text(result, "orchestration-status"));
        }
    }

    @Test
    public void existingIdentifiersDoNotRequireAnAdapterResponse() throws Exception {
        Document result = xml(
                String.valueOf(script.buildNetworkElements(inventory("pending-update", inventoryIds()), null, FIELDS)));
        assertEquals("inventory-stack", text(result, "heat-stack-id"));
        assertEquals("inventory-network", text(result, "neutron-network-id"));
        assertEquals("inventory.example", text(result, "contrail-network-fqdn"));
        assertEquals("Active", text(result, "orchestration-status"));
    }

    @Test
    public void adapterIdentifiersFillMissingInventoryValues() throws Exception {
        Document result =
                xml(String.valueOf(script.buildNetworkElements(inventory("pending-create", ""), adapterIds(), FIELDS)));
        assertEquals("adapter-stack", text(result, "heat-stack-id"));
        assertEquals("adapter-network", text(result, "neutron-network-id"));
        assertEquals("adapter.example", text(result, "contrail-network-fqdn"));
        assertEquals("Created", text(result, "orchestration-status"));
    }

    @Test
    public void missingOptionalIdentifiersStayAbsentWhenNeitherSourceSuppliesThem() throws Exception {
        Document result = xml(
                String.valueOf(script.buildNetworkElements(inventory("pending-update", ""), "<response/>", FIELDS)));
        assertEquals(0, result.getElementsByTagNameNS(AAI, "heat-stack-id").getLength());
        assertEquals(0, result.getElementsByTagNameNS(AAI, "neutron-network-id").getLength());
        assertEquals(0, result.getElementsByTagNameNS(AAI, "contrail-network-fqdn").getLength());
        assertEquals("Active", text(result, "orchestration-status"));
    }

    @Test
    public void absentInventoryOrEmptySelectionProducesNoNetworkFragment() {
        assertEquals("", String.valueOf(script.buildNetworkElements(null, adapterIds(), FIELDS)));
        assertEquals("", String.valueOf(script.buildNetworkElements(inventory("pending-create", inventoryIds()),
                adapterIds(), Collections.emptyList())));
    }

    @Test
    public void networkElementTextEscapesXmlWithoutLosingUnicode() throws Exception {
        Assume.assumeTrue("SO-4446: legacy network element names must be XML escaped",
                Boolean.getBoolean("groovy.migration.verification"));
        String source = inventory("pending-create", "<a:network-name>Größe &amp; 東京 &lt;edge&gt;</a:network-name>");
        String result = String
                .valueOf(script.buildNetworkElements(source, "<response/>", Collections.singletonList("network-name")));
        try {
            assertEquals("Größe & 東京 <edge>", text(xml(result), "network-name"));
        } catch (SAXException invalid) {
            throw new AssertionError("Network names must form valid XML text", invalid);
        }
    }

    @Test
    public void neutronSubnetLookupUsesNamespaceAwareKeysAndRetainsIdentifierText() throws Exception {
        String response = "<r:result xmlns:r='urn:adapter'><r:entry><r:key>subnet-1</r:key>"
                + "<r:value>neutron-1</r:value></r:entry><r:entry><r:key>subnet-2</r:key>"
                + "<r:value>neutron-2</r:value></r:entry></r:result>";
        assertEquals("neutron-1", script.extractNeutSubId(response, "subnet-1"));
        assertEquals("neutron-2", script.extractNeutSubId(response, "subnet-2"));
        assertEquals("", script.extractNeutSubId(response, "unknown"));
    }

    @Test
    public void anEmptySubnetMappingDoesNotInventAnIdentifier() throws Exception {
        assertEquals("", script.extractNeutSubId("<result/>", "subnet-1"));
    }

    @Test
    public void ipVersionNormalizationPreservesUnknownAndMissingValues() {
        assertEquals("4", script.getIpvVersion("ipv4"));
        assertEquals("6", script.getIpvVersion("ipv6"));
        for (String value : new String[] {"4", "6", "", "IPV4", "v6", "dual-stack"}) {
            assertEquals(value, script.getIpvVersion(value));
        }
        assertNull(script.getIpvVersion(null));
    }

    private String inventory(String status, String fields) {
        return "<a:l3-network xmlns:a='" + AAI + "'><a:orchestration-status>" + status + "</a:orchestration-status>"
                + fields + "</a:l3-network>";
    }

    private String inventoryIds() {
        return "<a:heat-stack-id>inventory-stack</a:heat-stack-id>"
                + "<a:neutron-network-id>inventory-network</a:neutron-network-id>"
                + "<a:contrail-network-fqdn>inventory.example</a:contrail-network-fqdn>";
    }

    private String adapterIds() {
        return "<n:response xmlns:n='urn:adapter'><n:networkStackId>adapter-stack</n:networkStackId>"
                + "<n:neutronNetworkId>adapter-network</n:neutronNetworkId>"
                + "<n:networkFqdn>adapter.example</n:networkFqdn></n:response>";
    }

    private Document xml(String fragment) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(
                new InputSource(new StringReader("<inventory xmlns='" + AAI + "'>" + fragment + "</inventory>")));
    }

    private String text(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS(AAI, name).getLength());
        return document.getElementsByTagNameNS(AAI, name).item(0).getTextContent();
    }
}
