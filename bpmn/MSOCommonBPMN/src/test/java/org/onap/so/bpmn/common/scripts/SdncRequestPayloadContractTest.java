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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class SdncRequestPayloadContractTest {
    private static final String WORKFLOW = "http://openecomp.com/mso/workflow/schema/v1";
    private static final String ADAPTER = "http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1";
    private static final String CALLBACK = "http://localhost/callback?a=1&tenant=\u96ea";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final SDNCAdapterUtils script = new SDNCAdapterUtils();

    @Before
    public void configureIndependentHeaderAndPayloadInputs() {
        variables.put("prefix", "T_");
        variables.put("T_Request", request("", "feature &amp; &lt;\u96ea&gt;"));
        variables.put("feature-input", request("", "named-feature"));
        variables.put("testReqId", "wire & <\u96ea>");
        variables.put("mso-request-id", "parent-request");
        variables.put("mso-service-instance-id", "header-service & <\u96ea>");
        variables.put("T_source", "source & <\u96ea>");
        variables.put("T_notificationUrl", "http://localhost/notify?a=1&b=two");
        variables.put("T_serviceName", "service-name & <\u96ea>");
        variables.put("T_subscriberName", "subscriber & <\u96ea>");
        variables.put("T_subscriberGlobalId", "subscriber-id");
        variables.put("T_serviceType", "service-type");
        variables.put("T_customerId", "customer & <\u96ea>");
        variables.put("T_vpnId", "vpn & <\u96ea>");
        variables.put("T_vpnRt", "target:123:456");
        variables.put("T_vpnService", "vpn-service");
        variables.put("T_vpnRegion", "region & <\u96ea>");
        variables.put("T_vniId", "vni & <\u96ea>");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
    }

    @Test
    public void featureEnvelopeKeepsNamespaceUrisEscapedHeadersAndAllRequestFragments() throws Exception {
        Document document = feature("T_Request");
        headers(document, "assign");
        assertEquals("feature & <\u96ea>", plain(document, "feature-id"));
        assertEquals("payload-service", plain(document, "service-instance-id"));
        assertEquals("payload-request", plain(document, "request-id"));
        assertEquals("value & <\u96ea>", plain(document, "parameter-value"));
        Element data = (Element) document.getElementsByTagNameNS(WORKFLOW, "SDNCRequestData").item(0);
        Set<String> fragments = new HashSet<>();
        for (Node child = data.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element) {
                assertTrue(fragments.add(child.getLocalName()));
                assertTrue(child.getNamespaceURI() == null || child.getNamespaceURI().isEmpty());
            }
        }
        assertEquals(new HashSet<>(Arrays.asList("request-information", "service-information", "feature-information",
                "feature-parameters")), fragments);
    }

    @Test
    public void featureBuilderUsesTheExplicitRequestVariable() throws Exception {
        Document document = feature("feature-input");
        assertEquals("named-feature", plain(document, "feature-id"));
        assertEquals("header-service & <\u96ea>", adapter(document, "SvcInstanceId"));
    }

    @Test
    public void featureBuilderRemovesSourceDefaultNamespacesWithoutChangingValues() throws Exception {
        variables.put("T_Request", request(" xmlns=\"urn:source-request\"", "namespaced-feature"));
        Document document = feature("T_Request");
        assertEquals("namespaced-feature", plain(document, "feature-id"));
        assertEquals(0, document.getElementsByTagNameNS("urn:source-request", "feature-information").getLength());
        assertEquals("value & <\u96ea>", plain(document, "parameter-value"));
    }

    @Test
    public void activationEnvelopeKeepsItsMsoActionAndVnfInformation() throws Exception {
        Document document = activation();
        headers(document, "activate");
        assertEquals("CREATE & <\u96ea>", adapter(document, "MsoAction"));
        assertEquals("payload-request", plain(document, "request-id"));
        assertEquals("payload-service", plain(document, "service-instance-id"));
        assertEquals(0, document.getElementsByTagName("feature-information").getLength());
    }

    @Test
    public void activationKeepsRepeatedVnfEntriesInTheirInputOrder() throws Exception {
        Document document = activation();
        var entries = document.getElementsByTagName("vnf-id");
        assertEquals(2, entries.getLength());
        assertEquals("first & <\u96ea>", entries.item(0).getTextContent());
        assertEquals("second", entries.item(1).getTextContent());
    }

    @Test
    public void generatedHeaderIdsRetainTheirParentRequestPrefixAcrossAllThreeBuilders() throws Exception {
        variables.remove("testReqId");
        for (Document document : new Document[] {feature("T_Request"), activation(), layer("assign")}) {
            String id = adapter(document, "RequestId");
            assertTrue(id, id.matches("parent-request-[0-9]+"));
            assertTrue(Long.parseLong(id.substring("parent-request-".length())) > 0);
        }
    }

    @Test
    public void higherLayerAssignmentBuildsVpnDataAndDisablesTheInterface() throws Exception {
        Document document = layer("assign");
        layerCommon(document, "assign", "createTrinityBonding", "DISABLE");
        assertEquals("vpn & <\u96ea>", plain(document, "vpn-id"));
        assertEquals("target:123:456", plain(document, "vpn-rt"));
        assertEquals("vpn-service", plain(document, "vpn-service"));
        assertEquals("region & <\u96ea>", plain(document, "vpn-region"));
        assertEquals(0, document.getElementsByTagName("vni-id").getLength());
    }

    @Test
    public void higherLayerActivationUsesVniDataAndEnablesTheInterface() throws Exception {
        Document document = layer("activate");
        layerCommon(document, "activate", "activateTrinityBonding", "ENABLE");
        assertEquals("vni & <\u96ea>", plain(document, "vni-id"));
        assertEquals(0, document.getElementsByTagName("vpn-data-list").getLength());
    }

    @Test
    public void higherLayerDeletionUsesVniDataAndDisablesTheInterface() throws Exception {
        Document document = layer("delete");
        layerCommon(document, "delete", "deleteTrinityBonding", "DISABLE");
        assertEquals("vni & <\u96ea>", plain(document, "vni-id"));
        assertEquals(0, document.getElementsByTagName("vpn-data-list").getLength());
    }

    @Test
    public void supportedHigherLayerActionsSelectTheSameBodyWhenTheirCaseDiffers() throws Exception {
        assertEquals("createTrinityBonding", plain(layer("AsSiGn"), "request-action"));
        assertEquals("activateTrinityBonding", plain(layer("AcTiVaTe"), "request-action"));
        assertEquals("ENABLE", plain(layer("AcTiVaTe"), "interface-status"));
        assertEquals("deleteTrinityBonding", plain(layer("DeLeTe"), "request-action"));
    }

    private void layerCommon(Document document, String action, String requestAction, String status) {
        headers(document, action);
        assertEquals("parent-request", plain(document, "request-id"));
        assertEquals(requestAction, plain(document, "request-action"));
        assertEquals("source & <\u96ea>", plain(document, "source"));
        assertEquals("http://localhost/notify?a=1&b=two", plain(document, "notification-url"));
        assertEquals("service-name & <\u96ea>", plain(document, "service-type"));
        assertEquals("subscriber & <\u96ea>", plain(document, "subscriber-name"));
        assertEquals("subscriber-id", plain(document, "subscriber-global-id"));
        Element additional = (Element) document.getElementsByTagName("nbnc-request-information").item(0);
        assertEquals("service-type", additional.getElementsByTagName("service-type").item(0).getTextContent());
        assertEquals("customer & <\u96ea>", plain(document, "customer-id"));
        assertEquals(status, plain(document, "interface-status"));
    }

    private void headers(Document document, String action) {
        assertEquals(WORKFLOW, document.getDocumentElement().getNamespaceURI());
        assertEquals("SDNCAdapterWorkflowRequest", document.getDocumentElement().getLocalName());
        assertEquals("wire & <\u96ea>", adapter(document, "RequestId"));
        assertEquals("header-service & <\u96ea>", adapter(document, "SvcInstanceId"));
        assertEquals(action, adapter(document, "SvcAction"));
        assertEquals("operation & <\u96ea>", adapter(document, "SvcOperation"));
        assertEquals(CALLBACK, adapter(document, "CallbackUrl"));
        assertEquals("7",
                document.getElementsByTagNameNS(WORKFLOW, "SDNCTimeOutValueInMinutes").item(0).getTextContent());
    }

    private Document feature(String key) throws Exception {
        return parse(script.SDNCAdapterFeatureRequest(execution, key, "assign", CALLBACK, "operation & <\u96ea>", "7"));
    }

    private Document activation() throws Exception {
        return parse(script.SDNCAdapterActivateVnfRequest(execution, "activate", CALLBACK, "operation & <\u96ea>",
                "CREATE & <\u96ea>", "7"));
    }

    private Document layer(String action) throws Exception {
        return parse(
                script.SDNCAdapterL3ToHigherLayerRequest(execution, action, CALLBACK, "operation & <\u96ea>", "7"));
    }

    private Document parse(String xml) throws Exception {
        assertFalse(xml.isEmpty());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private String adapter(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS(ADAPTER, name).getLength());
        return document.getElementsByTagNameNS(ADAPTER, name).item(0).getTextContent();
    }

    private String plain(Document document, String name) {
        return document.getElementsByTagName(name).item(0).getTextContent();
    }

    private String request(String namespace, String feature) {
        return "<request" + namespace + "><request-information><request-id>payload-request</request-id>"
                + "</request-information><service-information><service-instance-id>payload-service</service-instance-id>"
                + "</service-information><feature-information><feature-id>" + feature + "</feature-id>"
                + "</feature-information><feature-parameters><parameter-value>value &amp; &lt;\u96ea&gt;</parameter-value>"
                + "</feature-parameters><vnf-information-list><vnf-information><vnf-id>first &amp; &lt;\u96ea&gt;</vnf-id>"
                + "</vnf-information><vnf-information><vnf-id>second</vnf-id></vnf-information></vnf-information-list></request>";
    }
}
