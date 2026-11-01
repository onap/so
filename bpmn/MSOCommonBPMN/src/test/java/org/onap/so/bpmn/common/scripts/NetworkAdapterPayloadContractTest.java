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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Test;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.SegmentationAssignment;
import org.onap.aai.domain.yang.SegmentationAssignments;
import org.onap.aai.domain.yang.Subnet;
import org.onap.aai.domain.yang.Subnets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

public class NetworkAdapterPayloadContractTest {
    private final NetworkUtils script = new NetworkUtils(null);
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private static final String REQUEST = "<request><tenant-id>tenant &amp; SO</tenant-id>"
            + "<service-instance-id>service &amp; SO</service-instance-id>"
            + "<modelCustomizationId>custom &amp; SO</modelCustomizationId></request>";

    @Test
    public void createAndUpdateEscapeIdentityAndKeepExplicitFalseFlags() throws Exception {
        for (boolean update : Arrays.asList(false, true)) {
            L3Network network = network();
            Document payload = xml(request(update, REQUEST, network, "VID"));
            assertEquals(update ? "updateNetworkRequest" : "createNetworkRequest",
                    payload.getDocumentElement().getLocalName());
            assertText(payload, "cloudSiteId", "region & SO");
            assertText(payload, "tenantId", "tenant & SO");
            assertText(payload, "networkId", "network & id");
            assertText(payload, "networkName", "network Gr\u00fc\u00dfe & SO");
            assertText(payload, "modelCustomizationUuid", "custom & SO");
            assertText(payload, "physicalNetworkName", "physical & SO");
            assertText(payload, "requestId", "request & SO");
            assertText(payload, "serviceInstanceId", "service & SO");
            assertText(payload, "messageId", "message & SO");
            assertText(payload, "networkTechnology", "CONTRAIL");
            if (update) {
                assertText(payload, "networkStackId", "stack & SO");
            }
            assertText(payload, "shared", "false");
            assertText(payload, "external", "true");
            assertText(payload, "backout", "false");
            assertText(payload, "skipAAI", "true");
            assertText(payload, "failIfExists", "false");
            assertText(payload, "notificationUrl", "");
            assertText(payload, "vlans", "");
            assertEquals("network & id", network.getNetworkId());
            assertEquals("Contrail", network.getNetworkTechnology());
        }
    }

    @Test
    public void portalRequestsUseExplicitParameterFlagsInsteadOfAaiFlags() throws Exception {
        String request = REQUEST.replace("</request>", "<network-params><param name=\"shared\">true</param>"
                + "<param name=\"external\">false</param></network-params></request>");
        for (boolean update : Arrays.asList(false, true)) {
            Document payload = xml(request(update, request, network(), "PORTAL"));
            Element contrail = (Element) payload.getElementsByTagNameNS("*", "contrailNetwork").item(0);
            assertEquals("true", contrail.getElementsByTagNameNS("*", "shared").item(0).getTextContent());
            assertEquals("false", contrail.getElementsByTagNameNS("*", "external").item(0).getTextContent());
        }
    }

    @Test
    public void absentVidFlagsDefaultToFalse() throws Exception {
        L3Network network = network();
        network.setIsSharedNetwork(null);
        network.setIsExternalNetwork(null);
        for (boolean update : Arrays.asList(false, true)) {
            Document payload = xml(request(update, REQUEST, network, "VID"));
            assertText(payload, "shared", "false");
            assertText(payload, "external", "false");
        }
    }

    @Test
    public void nonContrailRequestsOmitTheContrailBlock() throws Exception {
        L3Network network = network();
        network.setNetworkTechnology("AIC_SR_IOV");
        Document payload = xml(request(false, REQUEST, network, "VID"));
        assertText(payload, "networkTechnology", "AIC_SR_IOV");
        assertEquals(0, payload.getElementsByTagNameNS("*", "contrailNetwork").getLength());
    }

    @Test
    public void parameterTransformationPreservesEscapedUnicodeEmptyFalseAndZero() throws Exception {
        Document parameters = xml((String) script
                .buildParams("<network-params>" + "<param name=\"label\">Gr\u00fc\u00dfe &amp; &lt;SO&gt;</param>"
                        + "<param name=\"empty\"></param><param name=\"enabled\">false</param>"
                        + "<param name=\"count\">0</param></network-params>"));
        assertText(parameters, "label", "Gr\u00fc\u00dfe & <SO>");
        assertText(parameters, "empty", "");
        assertText(parameters, "enabled", "false");
        assertText(parameters, "count", "0");
        assertEquals("", script.buildParams("<network-params/>"));
    }

    @Test
    public void missingRequestOrInventoryDoesNotProduceAPartialPayload() {
        assertNull(script.CreateNetworkRequestV2(execution, "request", "message", null, network(), "", "", "", "region",
                false, "VID"));
        assertNull(script.UpdateNetworkRequestV2(execution, "request", "message", REQUEST, null, "", "", "", "region",
                false, "VID"));
        assertNull(script.CreateNetworkRequestV2(execution, "request", "message", REQUEST, null, "", "", "", "region",
                false, "VID"));
        assertNull(script.UpdateNetworkRequestV2(execution, "request", "message", null, network(), "", "", "", "region",
                false, "VID"));
    }

    @Test
    public void namespacedModelInfoOverridesLegacyValuesForCreateAndUpdate() throws Exception {
        String input = "<request xmlns='urn:onap:test:network'><tenant-id>tenant &amp; SO</tenant-id>"
                + "<service-instance-id>service &amp; SO</service-instance-id>"
                + "<modelCustomizationId>legacy-model</modelCustomizationId><networkModelInfo>"
                + "<modelName>model &lt;one&gt;</modelName>"
                + "<modelCustomizationUuid>model &amp; \u96ea</modelCustomizationUuid></networkModelInfo></request>";
        for (boolean update : Arrays.asList(false, true)) {
            L3Network network = network();
            network.setNetworkType("inventory-type");
            Document payload = xml(request(update, input, network, "VID"));
            assertText(payload, "networkType", "model <one>");
            assertText(payload, "modelCustomizationUuid", "model & \u96ea");
            assertText(payload, "tenantId", "tenant & SO");
            assertText(payload, "serviceInstanceId", "service & SO");
            assertEquals("inventory-type", network.getNetworkType());
        }
    }

    @Test
    public void supportedContrailSpellingsSelectTheSameAdapterTechnology() throws Exception {
        for (String technology : Arrays.asList("Contrail", "contrail", "CONTRAIL")) {
            L3Network network = network();
            network.setNetworkTechnology(technology);
            Document payload = xml(request(false, REQUEST, network, "VID"));
            assertText(payload, "networkTechnology", "CONTRAIL");
            assertEquals(1, payload.getElementsByTagNameNS("*", "contrailNetwork").getLength());
            assertEquals(technology, network.getNetworkTechnology());
        }
    }

    @Test
    public void absentEmptyAndLiteralNullStacksSelectForceUpdate() throws Exception {
        for (String stack : Arrays.asList(null, "", "null")) {
            L3Network network = network();
            network.setHeatStackId(stack);
            Document payload = xml(request(true, REQUEST, network, "VID"));
            assertText(payload, "networkStackId", "force_update");
            assertEquals(stack, network.getHeatStackId());
        }
    }

    @Test
    public void vlanAssignmentsKeepOrderAndZeroWhileAbsentCollectionsStayEmpty() throws Exception {
        for (boolean update : Arrays.asList(false, true)) {
            L3Network network = network();
            SegmentationAssignments assignments = new SegmentationAssignments();
            for (String id : Arrays.asList("101", "0")) {
                SegmentationAssignment assignment = new SegmentationAssignment();
                assignment.setSegmentationId(id);
                assignments.getSegmentationAssignment().add(assignment);
            }
            network.setSegmentationAssignments(assignments);
            assertText(xml(request(update, REQUEST, network, "VID")), "vlans", "101,0");
            assertEquals(2, assignments.getSegmentationAssignment().size());
            assertEquals("101", assignments.getSegmentationAssignment().get(0).getSegmentationId());
            assertEquals("0", assignments.getSegmentationAssignment().get(1).getSegmentationId());
            network.setSegmentationAssignments(new SegmentationAssignments());
            assertText(xml(request(update, REQUEST, network, "VID")), "vlans", "");
            network.setSegmentationAssignments(null);
            assertText(xml(request(update, REQUEST, network, "VID")), "vlans", "");
        }
    }

    @Test
    public void subnetNamesEscapeXmlMetacharactersWithoutLosingUnicode() throws Exception {
        Assume.assumeTrue("SO-4446: a subnet name must produce valid escaped network-adapter XML",
                Boolean.getBoolean("groovy.migration.verification"));
        L3Network network = network();
        Subnet subnet = subnet("active-1", "active");
        subnet.setSubnetName("subnet Gr\u00fc\u00dfe & <SO>");
        Subnets subnets = new Subnets();
        subnets.getSubnet().add(subnet);
        network.setSubnets(subnets);
        try {
            Document payload = xml(request(false, REQUEST, network, "VID"));
            assertText(payload, "subnetName", "subnet Gr\u00fc\u00dfe & <SO>");
        } catch (SAXParseException invalidXml) {
            fail("A subnet name must be escaped in the network-adapter request: " + invalidXml.getMessage());
        }
    }

    private String request(boolean update, String input, L3Network network, String source) {
        if (update) {
            return (String) script.UpdateNetworkRequestV2(execution, "request & SO", "message & SO", input, network, "",
                    "", "", "region & SO", false, source);
        }
        return (String) script.CreateNetworkRequestV2(execution, "request & SO", "message & SO", input, network, "", "",
                "", "region & SO", false, source);
    }

    private L3Network network() {
        L3Network network = new L3Network();
        network.setNetworkId("network & id");
        network.setNetworkName("network Gr\u00fc\u00dfe & SO");
        network.setNetworkTechnology("Contrail");
        network.setPhysicalNetworkName("physical & SO");
        network.setHeatStackId("stack & SO");
        network.setIsSharedNetwork(Boolean.FALSE);
        network.setIsExternalNetwork(Boolean.TRUE);
        return network;
    }

    private Subnet subnet(String id, String status) {
        Subnet subnet = new Subnet();
        subnet.setSubnetId(id);
        subnet.setSubnetName("subnet-" + id);
        subnet.setOrchestrationStatus(status);
        subnet.setNetworkStartAddress("10.0.0.0");
        subnet.setCidrMask("24");
        subnet.setIpVersion("ipv4");
        subnet.setDhcpEnabled(Boolean.FALSE);
        subnet.setGatewayAddress("10.0.0.1");
        return subnet;
    }

    private void assertText(Document document, String name, String expected) {
        assertEquals(name, 1, document.getElementsByTagNameNS("*", name).getLength());
        assertEquals(name, expected, document.getElementsByTagNameNS("*", name).item(0).getTextContent());
    }

    private Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
