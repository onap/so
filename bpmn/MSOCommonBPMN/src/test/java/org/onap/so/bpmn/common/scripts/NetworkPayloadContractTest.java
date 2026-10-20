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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.onap.aai.domain.yang.HostRoute;
import org.onap.aai.domain.yang.HostRoutes;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.Subnet;
import org.onap.aai.domain.yang.Subnets;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class NetworkPayloadContractTest {
    private final NetworkUtils utility = new NetworkUtils(null);

    @Test
    public void subnetsAwaitingDeletionAreOmittedWithoutReorderingTheOthers() throws Exception {
        L3Network network = new L3Network();
        Subnets subnets = new Subnets();
        network.setSubnets(subnets);
        subnets.getSubnet().add(subnet("active-1", "active"));
        subnets.getSubnet().add(subnet("delete-1", "pending-delete"));
        subnets.getSubnet().add(subnet("delete-2", "PendingDelete"));
        subnets.getSubnet().add(subnet("active-2", null));
        Document document = xml((String) utility.buildSubnets(network));
        NodeList ids = document.getElementsByTagName("subnetId");
        assertEquals(2, ids.getLength());
        assertEquals("active-1", ids.item(0).getTextContent());
        assertEquals("active-2", ids.item(1).getTextContent());
    }

    @Test
    public void subnetPayloadKeepsFalseDhcpAndDistinguishesEmptyFromOmittedFields() throws Exception {
        L3Network network = new L3Network();
        Subnets subnets = new Subnets();
        network.setSubnets(subnets);
        Subnet subnet = subnet("subnet-1", "active");
        subnets.getSubnet().add(subnet);
        Document document = xml((String) utility.buildSubnets(network));
        assertEquals("10.0.0.0/24", text(document, "cidr"));
        assertEquals("false", text(document, "enableDHCP"));
        assertEquals("10.0.0.1", text(document, "gatewayIp"));
        assertEquals("4", text(document, "ipVersion"));
        assertEquals("", text(document, "start"));
        assertEquals("", text(document, "end"));
        assertEquals(0, document.getElementsByTagName("subnetName").getLength());
        assertEquals(0, document.getElementsByTagName("addrFromStart").getLength());
        assertEquals(0, document.getElementsByTagName("hostRoutes").getLength());
    }

    @Test
    public void ipv6PayloadKeepsAllocationPoolsNamesDirectionAndHostRouteOrder() throws Exception {
        L3Network network = new L3Network();
        Subnets subnets = new Subnets();
        network.setSubnets(subnets);
        Subnet subnet = subnet("ipv6-subnet", "active");
        subnet.setNetworkStartAddress("2001:db8::");
        subnet.setCidrMask("64");
        subnet.setGatewayAddress("2001:db8::1");
        subnet.setIpVersion("ipv6");
        subnet.setDhcpEnabled(true);
        subnet.setDhcpStart("2001:db8::10");
        subnet.setDhcpEnd("2001:db8::20");
        subnet.setSubnetName("Grüße");
        subnet.setIpAssignmentDirection("true");
        HostRoutes routes = new HostRoutes();
        routes.getHostRoute().add(route("2001:db8:1::/64", "2001:db8::2"));
        routes.getHostRoute().add(route("2001:db8:2::/64", "2001:db8::3"));
        subnet.setHostRoutes(routes);
        subnets.getSubnet().add(subnet);
        Document document = xml((String) utility.buildSubnets(network));
        assertEquals("2001:db8::/64", text(document, "cidr"));
        assertEquals("6", text(document, "ipVersion"));
        assertEquals("true", text(document, "enableDHCP"));
        assertEquals("2001:db8::10", text(document, "start"));
        assertEquals("2001:db8::20", text(document, "end"));
        assertEquals("Grüße", text(document, "subnetName"));
        assertEquals("true", text(document, "addrFromStart"));
        NodeList prefixes = document.getElementsByTagName("prefix");
        NodeList hops = document.getElementsByTagName("nextHop");
        assertEquals(2, prefixes.getLength());
        assertEquals(2, hops.getLength());
        assertEquals("2001:db8:1::/64", prefixes.item(0).getTextContent());
        assertEquals("2001:db8:2::/64", prefixes.item(1).getTextContent());
        assertEquals("2001:db8::2", hops.item(0).getTextContent());
        assertEquals("2001:db8::3", hops.item(1).getTextContent());
    }

    @Test
    public void missingAndEmptySubnetCollectionsHaveNoPayloadElements() {
        L3Network network = new L3Network();
        assertEquals("", utility.buildSubnets(network));
        network.setSubnets(new Subnets());
        assertEquals("", utility.buildSubnets(network));
    }

    @Test
    public void requestRollbackPreferenceOverridesTheConfiguredDefault() {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("mso.rollback")).thenReturn(true);
        assertFalse(utility.isRollbackEnabled(execution,
                "<request><backout-on-failure>FALSE</backout-on-failure></request>"));
        when(execution.getVariable("mso.rollback")).thenReturn(false);
        assertTrue(utility.isRollbackEnabled(execution,
                "<request><backout-on-failure>true</backout-on-failure></request>"));
    }

    @Test
    public void missingAndEmptyRollbackPreferencesUseTheTypedConfiguredDefault() {
        DelegateExecution execution = mock(DelegateExecution.class);
        for (Object enabled : new Object[] {true, "true"}) {
            when(execution.getVariable("mso.rollback")).thenReturn(enabled);
            assertTrue(utility.isRollbackEnabled(execution, "<request/>"));
            assertTrue(utility.isRollbackEnabled(execution, "<request><backout-on-failure/></request>"));
        }
        for (Object disabled : new Object[] {false, "false", 0}) {
            when(execution.getVariable("mso.rollback")).thenReturn(disabled);
            assertFalse(utility.isRollbackEnabled(execution, "<request/>"));
            assertFalse(utility.isRollbackEnabled(execution, "<request><backout-on-failure/></request>"));
        }
    }

    private static Subnet subnet(String id, String status) {
        Subnet subnet = new Subnet();
        subnet.setSubnetId(id);
        subnet.setOrchestrationStatus(status);
        subnet.setNetworkStartAddress("10.0.0.0");
        subnet.setCidrMask("24");
        subnet.setGatewayAddress("10.0.0.1");
        subnet.setIpVersion("ipv4");
        subnet.setDhcpEnabled(false);
        return subnet;
    }

    private static HostRoute route(String prefix, String nextHop) {
        HostRoute route = new HostRoute();
        route.setRoutePrefix(prefix);
        route.setNextHop(nextHop);
        return route;
    }

    private static String text(Document document, String name) {
        NodeList nodes = document.getElementsByTagName(name);
        assertEquals(1, nodes.getLength());
        return nodes.item(0).getTextContent();
    }

    private static Document xml(String fragment) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(("<network>" + fragment + "</network>").getBytes(StandardCharsets.UTF_8)));
    }
}
