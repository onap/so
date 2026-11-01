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
import static org.junit.Assert.assertThrows;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VfModuleParameterContractTest {
    private static final String DIRECTIVE_TEXT = "quoted \"snow\" \\ line\nGr\u00fc\u00dfe";
    private final ObjectMapper json = new ObjectMapper();
    private final VfModuleBase script = new VfModuleBase() {
        @Override
        public void preProcessRequest(DelegateExecution execution) {}
    };

    @Test
    public void singleTopologyRetainsIdentityContextsAndTheOptionalZeroIndex() throws Exception {
        for (String index : new String[] {null, "0"}) {
            Map<String, String> result = single(null, response("<service-data/>"), index);
            assertEquals("vnf-id", result.get("vnf_id"));
            assertEquals("vnf-name", result.get("vnf_name"));
            assertEquals("module-id", result.get("vf_module_id"));
            assertEquals("module-name", result.get("vf_module_name"));
            assertEquals("environment", result.get("environment_context"));
            assertEquals("workload", result.get("workload_context"));
            assertEquals(index != null, result.containsKey("vf_module_index"));
            if (index != null) {
                assertEquals("0", result.get("vf_module_index"));
            }
            assertEquals("{}", result.get("user_directives"));
        }
    }

    @Test
    public void requestParametersOverrideControllerValuesWithoutChangingTheDirectiveSnapshots() throws Exception {
        Map<String, String> request = new LinkedHashMap<>();
        request.put("choice", "request");
        request.put("count", "0");
        request.put("enabled", "false");
        request.put("oof_directives", "placement");
        String topology = "<service-data>" + parameter("choice", "controller")
                + parameter("vnf_id", "stale-controller-id") + "</service-data>";
        Map<String, String> result = single(request, response(topology), null);
        assertEquals("request", result.get("choice"));
        assertEquals("vnf-id", result.get("vnf_id"));
        assertEquals("0", result.get("count"));
        assertEquals("false", result.get("enabled"));
        assertEquals("placement", result.get("oof_directives"));
        assertEquals("controller", attributes(result.get("sdnc_directives")).get("choice"));
        Map<String, String> user = attributes(result.get("user_directives"));
        assertEquals("request", user.get("choice"));
        assertEquals("0", user.get("count"));
        assertEquals("false", user.get("enabled"));
        assertFalse(user.containsKey("oof_directives"));
        assertEquals(3, user.size());
    }

    @Test
    public void combinedTopologiesUseModuleParametersAndVnfAvailabilityZones() throws Exception {
        String vnf = response("<vnf-topology><availability-zones><availability-zone>zone-a</availability-zone>"
                + "</availability-zones><availability-zones><availability-zone>zone-b</availability-zone>"
                + "</availability-zones></vnf-topology>");
        String module = response("<vf-module-topology><param><name>choice</name><value>controller</value></param>"
                + "<param><name>vf_module_id</name><value>stale-module</value></param></vf-module-topology>");
        Map<String, String> result = combined(Collections.singletonMap("choice", "request"), vnf, module);
        assertEquals("zone-a", result.get("availability_zone_0"));
        assertEquals("zone-b", result.get("availability_zone_1"));
        assertEquals("module-id", result.get("vf_module_id"));
        assertEquals("0", result.get("vf_module_index"));
        assertEquals("request", result.get("choice"));
        assertEquals("controller", attributes(result.get("sdnc_directives")).get("choice"));
        assertEquals("request", attributes(result.get("user_directives")).get("choice"));
    }

    @Test
    public void explicitNetworkTagsTakePrecedenceOverVmRoleMappingsAndFallbackRoles() throws Exception {
        String vm = "<vnf-vms><vm-type>worker</vm-type><vm-networks><network-role>control</network-role>"
                + "<network-role-tag>mapped</network-role-tag></vm-networks></vnf-vms>";
        String networks = network("control", "", "inherited") + network("control", "explicit", "selected")
                + network("plain", "", "fallback");
        Map<String, String> result = single(null, response("<service-data>" + networks + vm + "</service-data>"), null);
        assertEquals("inherited", result.get("mapped_net_id"));
        assertEquals("selected", result.get("explicit_net_id"));
        assertEquals("fallback", result.get("plain_net_id"));
        assertEquals("", result.get("plain_v6_subnet_id"));
        assertEquals("", result.get("plain_net_fqdn"));
        assertFalse(result.containsKey("control_net_id"));
    }

    @Test
    public void vmNamesAddressesFloatingIpsAndRoutesKeepTheirControllerOrdering() throws Exception {
        Map<String, String> result = single(null, response("<service-data>" + vm("vnf-vms") + "</service-data>"), null);
        assertVm(result);
        JsonNode routes = json.readTree(result.get("worker_ctl_route_prefixes"));
        assertEquals(2, routes.size());
        assertEquals("10.1.0.0/16", routes.get(0).path("interface_route_table_routes_route_prefix").asText());
        assertEquals("2001:db8::/64", routes.get(1).path("interface_route_table_routes_route_prefix").asText());
    }

    @Test
    public void combinedTopologiesMapVnfNetworksThroughTheModuleVmRoles() throws Exception {
        Map<String, String> result =
                combined(null, response("<vnf-topology>" + network("control", "", "net-1") + "</vnf-topology>"),
                        response("<vf-module-topology>" + vm("vm") + "</vf-module-topology>"));
        assertEquals("net-1", result.get("ctl_net_id"));
        assertVm(result);
    }

    @Test
    public void namespaceQualifiedTopologiesProduceTheSameNamedParameterValues() throws Exception {
        String body = "<service-data>" + parameter("description", "Gr&#252;&#223;e &amp; &#38634;") + "</service-data>";
        Map<String, String> plain = single(null, response(body), null);
        String namespaced = response(body).replace("<response>", "<response xmlns='urn:sdnc:contract'>");
        Map<String, String> qualified = single(null, namespaced, null);
        assertEquals(plain, qualified);
        assertEquals("Gr\u00fc\u00dfe & \u96ea", qualified.get("description"));
    }

    @Test
    public void malformedTopologyCannotProduceAnApparentlySuccessfulParameterList() {
        assertThrows(Exception.class, () -> single(null, "<response><response-data>", null));
        assertThrows(Exception.class, () -> combined(null, response("<vnf-topology/>"), "<response>"));
    }

    @Test
    public void singleTopologyDirectiveJsonPreservesQuotesBackslashesAndNewlines() throws Exception {
        Assume.assumeTrue("SO-4457: VF-module directives must escape JSON attribute values",
                Boolean.getBoolean("groovy.migration.verification"));
        String value = DIRECTIVE_TEXT;
        Map<String, String> result =
                single(Collections.singletonMap("description", value), response("<service-data/>"), null);
        assertEquals(value, result.get("description"));
        assertEquals(value, attributes(result.get("user_directives")).get("description"));
    }

    @Test
    public void combinedTopologyDirectiveJsonPreservesQuotesBackslashesAndNewlines() throws Exception {
        Assume.assumeTrue("SO-4457: combined VF-module directives must escape JSON attribute values",
                Boolean.getBoolean("groovy.migration.verification"));
        String value = DIRECTIVE_TEXT;
        Map<String, String> result = combined(Collections.singletonMap("description", value),
                response("<vnf-topology/>"), response("<vf-module-topology/>"));
        assertEquals(value, result.get("description"));
        assertEquals(value, attributes(result.get("user_directives")).get("description"));
    }

    @Test
    public void vbngRetainsEscapedIdentityAndCallerParameters() throws Exception {
        Map<String, String> result =
                entries(script.buildVfModuleParamsVbng("<entry><key>custom-request</key><value>false</value></entry>",
                        response("<service-data/>"), "vnf & \u96ea", "vnf-name", "module-id", "module-name"));
        assertEquals("vnf & \u96ea", result.get("vnf_id"));
        assertEquals("vnf-name", result.get("vnf_name"));
        assertEquals("module-id", result.get("vf_module_id"));
        assertEquals("module-name", result.get("vf_module_name"));
        assertEquals("false", result.get("custom-request"));
    }

    @Test
    public void vbngMapsNetworkRolesAndPreservesZoneAndVlanOrdering() throws Exception {
        String network = network("control", "", "net-1").replace("</vnf-networks>",
                "<network-name>Gr&#252;&#223;e &amp; &#38634;</network-name><subnet-id>subnet-1</subnet-id>"
                        + "<ipv6-subnet-id>subnet-6</ipv6-subnet-id><contrail-network-fqdn>domain:net</contrail-network-fqdn>"
                        + "<sriov-vlan-filter-list><sriov-vlan-filter>20</sriov-vlan-filter></sriov-vlan-filter-list>"
                        + "<sriov-vlan-filter-list><sriov-vlan-filter>10</sriov-vlan-filter></sriov-vlan-filter-list>"
                        + "</vnf-networks>");
        String topology = "<service-data><availability-zones><availability-zone>zone-a</availability-zone>"
                + "</availability-zones><availability-zones><availability-zone>zone-b</availability-zone>"
                + "</availability-zones><vm-networks><network-role>control</network-role><network-role-tag>ctl"
                + "</network-role-tag></vm-networks>" + network + "</service-data>";
        Map<String, String> result = entries(script.buildVfModuleParamsVbng("", response(topology), "vnf-id",
                "vnf-name", "module-id", "module-name"));
        assertEquals("zone-a", result.get("availability_zone_0"));
        assertEquals("zone-b", result.get("availability_zone_1"));
        assertEquals("net-1", result.get("ctl_net_id"));
        assertEquals("Gr\u00fc\u00dfe & \u96ea", result.get("ctl_net_name"));
        assertEquals("subnet-1", result.get("ctl_subnet_id"));
        assertEquals("subnet-6", result.get("ctl_v6_subnet_id"));
        assertEquals("domain:net", result.get("ctl_net_fqdn"));
        assertEquals("20,10", result.get("ctl_ATT_VF_VLAN_FILTER"));
    }

    @Test
    public void vbngControllerParametersCannotReplaceReservedResourceIdentities() throws Exception {
        String topology = "<service-data>" + parameter("vnf_id", "stale-vnf") + parameter("vnf_name", "stale-name")
                + parameter("vf_module_id", "stale-module") + parameter("vf_module_name", "stale-module-name")
                + parameter("count", "0") + "</service-data>";
        Map<String, String> result = entries(script.buildVfModuleParamsVbng("", response(topology), "vnf-id",
                "vnf-name", "module-id", "module-name"));
        assertEquals("vnf-id", result.get("vnf_id"));
        assertEquals("vnf-name", result.get("vnf_name"));
        assertEquals("module-id", result.get("vf_module_id"));
        assertEquals("module-name", result.get("vf_module_name"));
        assertEquals("0", result.get("count"));
    }

    @Test
    public void vbngVmNamesAndAddressesMustReachTheAdapterParameterList() throws Exception {
        Assume.assumeTrue("SO-4457: VBNG VM processing references the missing nfVMSPositions property",
                Boolean.getBoolean("groovy.migration.verification"));
        Map<String, String> result = entries(
                script.buildVfModuleParamsVbng("", response("<service-data>" + vm("vnf-vms") + "</service-data>"),
                        "vnf-id", "vnf-name", "module-id", "module-name"));
        assertVm(result);
    }

    @Test
    public void controllerDirectivesPreserveSpecialCharactersInSingleTopologies() throws Exception {
        Assume.assumeTrue("SO-4457: controller directives must escape JSON attribute values",
                Boolean.getBoolean("groovy.migration.verification"));
        String encoded = "quoted &quot;snow&quot; &#92; line&#10;Gr&#252;&#223;e";
        Map<String, String> result =
                single(null, response("<service-data>" + parameter("description", encoded) + "</service-data>"), null);
        assertEquals(DIRECTIVE_TEXT, result.get("description"));
        assertEquals(DIRECTIVE_TEXT, attributes(result.get("sdnc_directives")).get("description"));
    }

    @Test
    public void controllerDirectivesPreserveSpecialCharactersInCombinedTopologies() throws Exception {
        Assume.assumeTrue("SO-4457: combined controller directives must escape JSON attribute values",
                Boolean.getBoolean("groovy.migration.verification"));
        String encoded = "quoted &quot;snow&quot; &#92; line&#10;Gr&#252;&#223;e";
        Map<String, String> result = combined(null, response("<vnf-topology/>"), response("<vf-module-topology>"
                + "<param><name>description</name><value>" + encoded + "</value></param></vf-module-topology>"));
        assertEquals(DIRECTIVE_TEXT, result.get("description"));
        assertEquals(DIRECTIVE_TEXT, attributes(result.get("sdnc_directives")).get("description"));
    }

    @Test
    public void vbngRetainsAddressOrderingWhenTheVmNameListIsEmpty() throws Exception {
        String topology = vm("vnf-vms").replace("<vm-names><vm-name>worker-a</vm-name></vm-names>", "")
                .replace("<vm-names><vm-name>worker-b</vm-name></vm-names>", "");
        Map<String, String> result =
                entries(script.buildVfModuleParamsVbng("", response("<service-data>" + topology + "</service-data>"),
                        "vnf-id", "vnf-name", "module-id", "module-name"));
        assertEquals("192.0.2.1,192.0.2.2", result.get("worker_ctl_ips"));
        assertEquals("192.0.2.1", result.get("worker_ctl_ip_0"));
        assertEquals("192.0.2.2", result.get("worker_ctl_ip_1"));
        assertEquals("2001:db8::1,2001:db8::2", result.get("worker_ctl_v6_ips"));
        assertEquals("2001:db8::1", result.get("worker_ctl_v6_ip_0"));
        assertEquals("2001:db8::2", result.get("worker_ctl_v6_ip_1"));
        assertEquals("192.0.2.44", result.get("worker_ctl_floating_ip"));
        assertEquals("2001:db8::44", result.get("worker_ctl_floating_v6_ip"));
        assertEquals(2, json.readTree(result.get("worker_ctl_route_prefixes")).size());
    }

    @Test
    public void vbngUsesTheNetworkRoleWhenThereIsNoVmTagMapping() throws Exception {
        Map<String, String> result = entries(script.buildVfModuleParamsVbng("",
                response("<service-data>" + network("plain", "", "net-fallback") + "</service-data>"), "vnf-id",
                "vnf-name", "module-id", "module-name"));
        assertEquals("net-fallback", result.get("plain_net_id"));
        assertEquals("", result.get("plain_v6_subnet_id"));
        assertEquals("", result.get("plain_net_fqdn"));
    }

    private Map<String, String> single(Map<String, String> request, String topology, String index) throws Exception {
        return entries(script.buildVfModuleParams(request, topology, "vnf-id", "vnf-name", "module-id", "module-name",
                index, "environment", "workload"));
    }

    private Map<String, String> combined(Map<String, String> request, String vnf, String module) throws Exception {
        return entries(script.buildVfModuleParamsFromCombinedTopologies(request, vnf, module, "vnf-id", "vnf-name",
                "module-id", "module-name", "0", "environment", "workload"));
    }

    private Map<String, String> entries(String fragment) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        NodeList entries = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader("<params>" + fragment + "</params>"))).getDocumentElement()
                .getElementsByTagName("entry");
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String key = entry.getElementsByTagName("key").item(0).getTextContent();
            assertFalse("Duplicate parameter: " + key, result.containsKey(key));
            result.put(key, entry.getElementsByTagName("value").item(0).getTextContent());
        }
        return result;
    }

    private Map<String, String> attributes(String value) throws Exception {
        JsonNode attributes = json.readTree(value).path("attributes");
        assertTrue(attributes.isArray());
        Map<String, String> result = new LinkedHashMap<>();
        for (JsonNode attribute : attributes) {
            assertTrue(attribute.path("attribute_value").isTextual());
            String name = attribute.path("attribute_name").asText();
            assertFalse(result.containsKey(name));
            result.put(name, attribute.path("attribute_value").asText());
        }
        return result;
    }

    private void assertVm(Map<String, String> result) {
        assertEquals("worker-a,worker-b", result.get("worker_names"));
        assertEquals("worker-a", result.get("worker_name_0"));
        assertEquals("worker-b", result.get("worker_name_1"));
        assertEquals("192.0.2.1,192.0.2.2", result.get("worker_ctl_ips"));
        assertEquals("192.0.2.1", result.get("worker_ctl_ip_0"));
        assertEquals("192.0.2.2", result.get("worker_ctl_ip_1"));
        assertEquals("2001:db8::1,2001:db8::2", result.get("worker_ctl_v6_ips"));
        assertEquals("2001:db8::1", result.get("worker_ctl_v6_ip_0"));
        assertEquals("2001:db8::2", result.get("worker_ctl_v6_ip_1"));
        assertEquals("192.0.2.44", result.get("worker_ctl_floating_ip"));
        assertEquals("2001:db8::44", result.get("worker_ctl_floating_v6_ip"));
    }

    private String response(String body) {
        String requestInfo = "<vnf-request-information><vnf-id>requested-vnf</vnf-id></vnf-request-information>";
        body = body.replace("<service-data/>", "<service-data></service-data>").replace("<service-data>",
                "<service-data>" + requestInfo);
        return "<response><response-data>" + body + "</response-data></response>";
    }

    private String parameter(String name, String value) {
        return "<vnf-parameters><vnf-parameter-name>" + name + "</vnf-parameter-name><vnf-parameter-value>" + value
                + "</vnf-parameter-value></vnf-parameters>";
    }

    private String network(String role, String tag, String id) {
        return "<vnf-networks><network-role>" + role + "</network-role><network-role-tag>" + tag
                + "</network-role-tag><neutron-id>" + id + "</neutron-id></vnf-networks>";
    }

    private String vm(String element) {
        return "<" + element + "><vm-type>worker</vm-type><vm-names><vm-name>worker-a</vm-name></vm-names>"
                + "<vm-names><vm-name>worker-b</vm-name></vm-names><vm-networks><network-role>control</network-role>"
                + "<network-role-tag>ctl</network-role-tag><floating-ip>192.0.2.44</floating-ip>"
                + "<floating-ip-v6>2001:db8::44</floating-ip-v6>"
                + "<network-ips><ip-address>192.0.2.1</ip-address></network-ips>"
                + "<network-ips><ip-address>192.0.2.2</ip-address></network-ips>"
                + "<network-ips-v6><ip-address-ipv6>2001:db8::1</ip-address-ipv6></network-ips-v6>"
                + "<network-ips-v6><ip-address-ipv6>2001:db8::2</ip-address-ipv6></network-ips-v6>"
                + "<interface-route-prefixes><interface-route-prefix-cidr>10.1.0.0/16</interface-route-prefix-cidr>"
                + "</interface-route-prefixes><interface-route-prefixes><interface-route-prefix>2001:db8::/64"
                + "</interface-route-prefix></interface-route-prefixes></vm-networks></" + element + ">";
    }
}
