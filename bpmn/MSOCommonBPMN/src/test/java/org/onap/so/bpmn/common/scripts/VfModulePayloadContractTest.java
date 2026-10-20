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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class VfModulePayloadContractTest {
    private final Processor processor = new Processor();

    @Test
    public void parameterEntriesPreserveNamesEscapingValuesAndRepeatedKeyOrder() throws Exception {
        String parameters = "<p:vnf-params xmlns:p='urn:onap:parameters'>"
                + "<p:param name='Grüße &amp; &lt;key&gt;'>  Grüße &amp; &lt;SO&gt;  </p:param>"
                + "<p:param name='duplicate'>first</p:param><p:param name='duplicate'>second</p:param></p:vnf-params>";

        Document result = xml(processor.transformParamsToEntries(parameters));

        assertEntries(result, new String[][] {{"Grüße & <key>", "  Grüße & <SO>  "}, {"duplicate", "first"},
                {"duplicate", "second"}});
    }

    @Test
    public void falseZeroAndEmptyParameterValuesAreNotDropped() throws Exception {
        String parameters = "<vnf-params><param name='enabled'>false</param><param name='count'>0</param>"
                + "<param name='empty'/></vnf-params>";

        Document result = xml(processor.transformParamsToEntries(parameters));

        assertEntries(result, new String[][] {{"enabled", "false"}, {"count", "0"}, {"empty", ""}});
    }

    @Test
    public void volumeParametersExcludeOnlyTheFourResourceIdentityKeys() throws Exception {
        String parameters = "<v:volume-params xmlns:v='urn:onap:parameters'>"
                + "<v:param name='vnf_id'>vnf-1</v:param><v:param name='vnf_name'>vnf-name</v:param>"
                + "<v:param name='vf_module_id'>module-1</v:param><v:param name='vf_module_name'>module-name</v:param>"
                + "<v:param name='volume_group_id'>volume &amp; Grüße</v:param>"
                + "<v:param name='vf_module_id_suffix'>keep</v:param><v:param name='count'>0</v:param>"
                + "<v:param name='empty'/></v:volume-params>";

        Document result = xml(processor.transformVolumeParamsToEntries(parameters));

        assertEntries(result, new String[][] {{"volume_group_id", "volume & Grüße"}, {"vf_module_id_suffix", "keep"},
                {"count", "0"}, {"empty", ""}});
    }

    @Test
    public void networkParametersPreserveRoleNamesEscapingAndRelativeOrder() throws Exception {
        String parameters = "<p:vnf-params xmlns:p='urn:onap:parameters'>"
                + "<p:param name='oam_network'>Grüße &amp; &lt;network&gt;</p:param>"
                + "<p:param name='oam_network_id'>omit</p:param><p:param name='data_network'>data-1</p:param>"
                + "<p:param name='other'>omit-too</p:param></p:vnf-params>";

        Document result = xml(processor.transformNetworkParamsToVnfNetworks(parameters));

        NodeList networks = result.getElementsByTagName("vnf-networks");
        assertEquals(2, networks.getLength());
        assertElement((Element) networks.item(0), "network-role", "oam");
        assertElement((Element) networks.item(0), "network-name", "Grüße & <network>");
        assertElement((Element) networks.item(1), "network-role", "data");
        assertElement((Element) networks.item(1), "network-name", "data-1");
    }

    @Test
    public void missingEmptyAndNonmatchingCollectionsHaveNoPayloadElements() {
        for (String parameters : new String[] {null, "", "<vnf-params/>"}) {
            assertEquals("", processor.transformParamsToEntries(parameters));
            assertEquals("", processor.transformVolumeParamsToEntries(parameters));
            assertEquals("", processor.transformNetworkParamsToVnfNetworks(parameters));
        }
        assertEquals("", processor.transformNetworkParamsToVnfNetworks(
                "<vnf-params><param name='network_id'>not-a-network-name</param></vnf-params>"));
    }

    @Test
    public void malformedParameterXmlUsesTheDefinedEmptyTransformationFallback() {
        assertEquals("", processor.transformParamsToEntries("<vnf-params>"));
        assertEquals("", processor.transformVolumeParamsToEntries("<volume-params>"));
        assertEquals("", processor.transformNetworkParamsToVnfNetworks("<vnf-params>"));
    }

    @Test
    public void moduleLookupUsesTheExactIdAndDoesNotTreatAMissingIdAsAMatch() {
        String vnf = "<generic-vnf><vf-modules><vf-module>"
                + "<vf-module-name>without-id</vf-module-name></vf-module><vf-module>"
                + "<vf-module-id>module Grüße &amp; SO</vf-module-id><vf-module-name>base</vf-module-name>"
                + "<is-base-vf-module>true</is-base-vf-module></vf-module></vf-modules></generic-vnf>";

        VfModule selected = processor.findVfModule(vnf, "module Grüße & SO");

        assertNotNull(selected);
        assertEquals("module Grüße & SO", selected.getElementText("vf-module-id"));
        assertEquals("base", selected.getElementText("vf-module-name"));
        assertTrue(selected.isBaseVfModule());
        assertFalse(selected.isOnlyVfModule());
        assertNull(processor.findVfModule(vnf, "module Grüße & SO "));
        assertNull(processor.findVfModule(vnf, "absent"));
    }

    @Test
    public void aSingleNonbaseModuleRetainsItsCardinalityAndBooleanClassification() {
        String vnf = "<generic-vnf><vf-modules><vf-module><vf-module-id>module-1</vf-module-id>"
                + "<is-base-vf-module>false</is-base-vf-module></vf-module></vf-modules></generic-vnf>";

        VfModule selected = processor.findVfModule(vnf, "module-1");

        assertNotNull(selected);
        assertTrue(selected.isOnlyVfModule());
        assertFalse(selected.isBaseVfModule());
        assertNull(processor.findVfModule("<generic-vnf/>", "module-1"));
    }

    private static Document xml(String fragment) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(
                ("<parameters>" + fragment + "</parameters>").getBytes(StandardCharsets.UTF_8)));
    }

    private static void assertEntries(Document document, String[][] expected) {
        NodeList entries = document.getElementsByTagName("entry");
        assertEquals(expected.length, entries.getLength());
        for (int index = 0; index < expected.length; index++) {
            assertElement((Element) entries.item(index), "key", expected[index][0]);
            assertElement((Element) entries.item(index), "value", expected[index][1]);
        }
    }

    private static void assertElement(Element parent, String name, String expected) {
        assertEquals(name, 1, parent.getElementsByTagNameNS("", name).getLength());
        assertEquals(expected, parent.getElementsByTagNameNS("", name).item(0).getTextContent());
    }

    private static class Processor extends VfModuleBase {
        @Override
        public void preProcessRequest(DelegateExecution execution) {}
    }
}
