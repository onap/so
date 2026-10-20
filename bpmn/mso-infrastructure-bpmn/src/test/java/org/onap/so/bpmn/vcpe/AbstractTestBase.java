/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
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

package org.onap.so.bpmn.vcpe;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.After;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.mock.FileUtil;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.infrastructure.FlakyTests;
import groovy.lang.Category;


@Category(FlakyTests.class)
public class AbstractTestBase extends BaseIntegrationTest {
    private final List<String> ownedInstances = new ArrayList<>();


    public static final String CUST = "SDN-ETHERNET-INTERNET";
    public static final String SVC = "123456789";
    public static final String INST = "MIS%252F1604%252F0026%252FSW_INTERNET";
    public static final String PARENT_INST = "MIS%252F1604%252F0027%252FSW_INTERNET";
    public static final String ARID = "arId-1";
    public static final String ARVERS = "1490627351232";

    public static final String DEC_INST = "MIS%2F1604%2F0026%2FSW_INTERNET";
    public static final String DEC_PARENT_INST = "MIS%2F1604%2F0027%2FSW_INTERNET";

    public static final String VAR_SUCCESS_IND = "SuccessIndicator";
    public static final String VAR_WFEX = "SavedWorkflowException1";
    public static final String VAR_RESP_CODE = "CMSO_ResponseCode";
    public static final String VAR_COMP_REQ = "CompleteMsoProcessRequest";

    protected void stubServiceInstanceLookup(String instanceId, String fixture) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        NodeList links = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(FileUtil.readResourceFile("__files/" + fixture))))
                .getElementsByTagNameNS("*", "resource-link");
        JSONArray results = new JSONArray();
        for (int i = 0; i < links.getLength(); i++) {
            String link = links.item(i).getTextContent().trim();
            results.put(new JSONObject().put("resource-type", "service-instance").put("resource-link", link));
            String resolvedPath = URI.create(link).getRawPath().replaceFirst("^/aai/v[0-9]+", "");
            wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+" + Pattern.quote(resolvedPath)))
                    .withQueryParam("format", equalTo("count")).willReturn(aResponse().withStatus(200)));
        }
        String encoded = UriUtils.encodePathSegment(instanceId, StandardCharsets.UTF_8);
        wireMockServer.stubFor(
                get(urlPathMatching("/aai/v[0-9]+/nodes/service-instances/service-instance/" + Pattern.quote(encoded)))
                        .withQueryParam("format", equalTo("pathed"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody(new JSONObject().put("results", results).toString())));
    }

    protected void stubAllottedResourceRelationshipsAndUpdates() {
        String resource = "/aai/v[0-9]+" + Pattern.quote(allottedResourcePath());
        wireMockServer.stubFor(put(urlPathMatching(resource + "/relationship-list/relationship"))
                .withRequestBody(matchingJsonPath("$['related-link']", equalTo(serviceInstancePath(INST))))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(post(urlPathMatching(resource)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                .willReturn(aResponse().withStatus(200)));
    }

    protected void assertCreatedAllottedResource() {
        String resource = "/aai/v[0-9]+" + Pattern.quote(allottedResourcePath());
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(resource))
                .withRequestBody(matchingJsonPath("$['id']", equalTo(ARID)))
                .withRequestBody(
                        matchingJsonPath("$['model-invariant-id']", equalTo("ff5256d2-5a33-55df-13ab-12abad84e7ff")))
                .withRequestBody(
                        matchingJsonPath("$['model-version-id']", equalTo("fe6478e5-ea33-3346-ac12-ab121484a3fe"))));
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(resource + "/relationship-list/relationship"))
                .withRequestBody(matchingJsonPath("$['related-link']", equalTo(serviceInstancePath(INST)))));
    }

    private String serviceInstancePath(String id) {
        return "/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC
                + "/service-instances/service-instance/" + id;
    }

    private String allottedResourcePath() {
        return serviceInstancePath(PARENT_INST) + "/allotted-resources/allotted-resource/" + ARID;
    }

    @Override
    protected String invokeSubProcess(String key, Map<String, Object> inputs) {
        String id = super.invokeSubProcess(key, inputs);
        ownedInstances.add(id);
        return id;
    }

    @After
    public void removeOwnedInstances() {
        for (String id : ownedInstances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "Test cleanup");
            }
        }
        ownedInstances.clear();
    }

    protected void assertActiveResourceWasReused(String processId) {
        assertEquals("An active allotted resource must take the reuse route", Boolean.TRUE,
                runtimeService.getVariable(processId, "foundActiveAR"));
        String resource = "/aai/v[0-9]+" + Pattern.quote(allottedResourcePath());
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(resource)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(resource + "/relationship-list/relationship")));
    }

    protected void stubControllerResponse(int status) {
        wireMockServer.stubFor(post(urlPathMatching(".*/SDNCAdapter"))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
    }
}
