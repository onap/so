/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.homingobjects.Candidate;
import org.onap.so.bpmn.servicedecomposition.homingobjects.CandidateType;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SniroHomingV2IT extends BaseIntegrationTest {

    private ServiceInstance serviceInstance;

    private RequestContext requestContext;

    private Customer customer;
    ObjectMapper mapper = new ObjectMapper();

    private static final String RESOURCE_PATH = "__files/BuildingBlocks/SniroHoming/";


    String mockResponse =
            "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"corys cool\", \"requestStatus\": \"accepted\"}";

    @Before
    public void before() {
        serviceInstance = setServiceInstance();
        customer = setCustomer();
        customer.setGlobalCustomerId("testCustomerId");
        customer.setSubscriberName("testCustomerName");
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);

        requestContext = setRequestContext();
        requestContext.setMsoRequestId("testRequestId");
        RequestParameters params = new RequestParameters();
        params.setaLaCarte(false);
        params.setSubscriptionServiceType("testSubscriptionServiceType");
        requestContext.setRequestParameters(params);
    }

    public void beforeVpnBondingLink(String id) {
        VpnBondingLink bondingLink = new VpnBondingLink();
        bondingLink.setVpnBondingLinkId("testVpnBondingId" + id);
        bondingLink.getServiceProxies().add(setServiceProxy("1", "transport"));
        ServiceProxy sp2 = setServiceProxy("2", "infrastructure");
        Candidate requiredCandidate = new Candidate();
        requiredCandidate.setIdentifierType(CandidateType.VNF_ID);
        List<String> c = new ArrayList<String>();
        c.add("testVnfId");
        requiredCandidate.setIdentifiers(c);
        sp2.addRequiredCandidates(requiredCandidate);
        bondingLink.getServiceProxies().add(sp2);
        serviceInstance.getVpnBondingLinks().add(bondingLink);

    }

    public void beforeAllottedResource() {
        serviceInstance.getAllottedResources().add(setAllottedResource("1"));
        serviceInstance.getAllottedResources().add(setAllottedResource("2"));
        serviceInstance.getAllottedResources().add(setAllottedResource("3"));
    }

    public void beforeServiceProxy() {
        ServiceProxy sp = setServiceProxy("1", "infrastructure");
        Candidate filteringAttributes = new Candidate();
        filteringAttributes.setIdentifierType(CandidateType.CLOUD_REGION_ID);
        List<String> c = new ArrayList<String>();
        c.add("testCloudRegionId");
        filteringAttributes.setCloudOwner("att");
        filteringAttributes.setIdentifiers(c);
        sp.getFilteringAttributes().add(filteringAttributes);
        serviceInstance.getServiceProxies().add(sp);
    }

    public void beforeVnf() {
        setGenericVnf();
    }

    @Test(expected = Test.None.class)
    public void testCallSniro_success_1VpnLink() throws BadResponseException {
        beforeVpnBondingLink("1");

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        String request = readResourceFile(RESOURCE_PATH + "SniroManagerRequest1Vpn.json");
        request = request.replace("28080", wireMockPort);

        ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
        verify(sniroClient, times(1)).postDemands(argument.capture());
        assertEquals(request, argument.getValue().toJsonString());
    }

    @Test
    public void testCallSniro_success_3VpnLink() throws BadResponseException {
        beforeVpnBondingLink("1");
        beforeVpnBondingLink("2");
        beforeVpnBondingLink("3");

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        String request = readResourceFile(RESOURCE_PATH + "SniroManagerRequest3Vpn.json");
        request = request.replace("28080", wireMockPort);

        ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
        verify(sniroClient, times(1)).postDemands(argument.capture());
        assertEquals(request, argument.getValue().toJsonString());
    }

    @Test
    public void testCallSniro_success_3Allotteds() throws BadResponseException {
        beforeAllottedResource();

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        String request = readResourceFile(RESOURCE_PATH + "SniroManagerRequest3AR.json");
        request = request.replace("28080", wireMockPort);

        ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
        verify(sniroClient, times(1)).postDemands(argument.capture());
        assertEquals(request, argument.getValue().toJsonString());
    }

    @Test
    public void testCallSniro_success_1Vnf() throws BadResponseException {
        beforeVnf();

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
        verify(sniroClient, times(1)).postDemands(argument.capture());
        // TODO assertEquals(request, argument.getValue().toJsonString());
    }

    @Test
    public void testCallSniro_success_3Allotteds1Vnf() throws BadResponseException {
        beforeAllottedResource();
        beforeVnf();

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        verify(sniroClient, times(1)).postDemands(isA(SniroManagerRequest.class));
    }

    @Test
    public void testCallSniro_success_1ServiceProxy() throws BadResponseException {
        beforeServiceProxy();

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        String request = readResourceFile(RESOURCE_PATH + "SniroManagerRequest1SP.json");
        request = request.replace("28080", wireMockPort);

        ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
        verify(sniroClient, times(1)).postDemands(argument.capture());
        assertEquals(request, argument.getValue().toJsonString());
    }

    @Test(expected = Test.None.class)
    public void testProcessSolution_success_1VpnLink_1Solution() {
        beforeVpnBondingLink("1");

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray solution1 = new JSONArray();
        solution1.put(new JSONObject().put("serviceResourceId", "testProxyId1")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId1")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId1"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testProxyId2")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId2")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName2"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "secondaryPnfName").put("value",
                                        "testSecondaryPnfName2"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId2"))));

        asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1))
                .put("licenseSolutions", new JSONArray()));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        assertFalse(si.getVpnBondingLinks().isEmpty());
        VpnBondingLink link = si.getVpnBondingLinks().get(0);
        assertNotNull(link);
        assertFalse(link.getServiceProxies().isEmpty());

        assertEquals("testServiceInstanceId1",
                link.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
        assertNotNull(link.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName1",
                link.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

        assertEquals("testServiceInstanceId2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
        assertNotNull(link.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
        assertFalse(link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
        assertEquals("testPrimaryPnfName2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
        assertEquals("primary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
        assertEquals("testSecondaryPnfName2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
        assertEquals("secondary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());
    }

    @Test
    public void testProcessSolution_success_1VpnLink_2Solutions() {
        beforeVpnBondingLink("1");

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray solution1 = new JSONArray();
        solution1.put(new JSONObject().put("serviceResourceId", "testProxyId1")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId1")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId1"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testProxyId2")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId2")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName2"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "secondaryPnfName").put("value",
                                        "testSecondaryPnfName2"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId2"))));

        JSONArray solution2 = new JSONArray();
        solution2.put(new JSONObject().put("serviceResourceId", "testProxyId1")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId3")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName3"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli3"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId3"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId3"))));
        solution2.put(new JSONObject().put("serviceResourceId", "testProxyId2")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId4")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName4"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli4"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "secondaryPnfName").put("value",
                                        "testSecondaryPnfName4"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId4"))));

        asyncResponse.put("solutions",
                new JSONObject().put("placementSolutions", new JSONArray().put(solution1).put(solution2))
                        .put("licenseSolutions", new JSONArray()));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        assertFalse(si.getVpnBondingLinks().isEmpty());
        VpnBondingLink link = si.getVpnBondingLinks().get(0);
        VpnBondingLink link2 = si.getVpnBondingLinks().get(1);
        assertNotNull(link);
        assertFalse(link.getServiceProxies().isEmpty());

        assertEquals("testServiceInstanceId1",
                link.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
        assertNotNull(link.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName1",
                link.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

        assertEquals("testServiceInstanceId2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
        assertNotNull(link.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
        assertFalse(link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
        assertEquals("testPrimaryPnfName2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
        assertEquals("primary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
        assertEquals("testSecondaryPnfName2",
                link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
        assertEquals("secondary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());

        assertNotNull(link2);
        assertFalse(link2.getServiceProxies().isEmpty());

        assertEquals("testServiceInstanceId3",
                link2.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
        assertNotNull(link2.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName3",
                link2.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

        assertEquals("testServiceInstanceId4",
                link2.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
        assertNotNull(link2.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
        assertFalse(link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
        assertEquals("testPrimaryPnfName4",
                link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
        assertEquals("primary", link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
        assertEquals("testSecondaryPnfName4",
                link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
        assertEquals("secondary",
                link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());

    }

    @Test
    @Ignore
    public void testProcessSolution_success_3VpnLink_2Solutions() {
        // TODO
    }

    @Test
    public void testProcessSolution_success_3Allotteds_1Solution() {
        beforeAllottedResource();

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray solution1 = new JSONArray();
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId1")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId1")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId1"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId2")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId2")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName2"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId2"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId3")
                .put("solution",
                        new JSONObject().put("identifierType", "cloudRegionId").put("identifiers",
                                new JSONArray().put("testCloudRegionId3")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))));

        asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1))
                .put("licenseSolutions", new JSONArray()));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        assertFalse(si.getAllottedResources().isEmpty());
        AllottedResource ar = si.getAllottedResources().get(0);
        assertNotNull(ar);
        assertEquals("testServiceInstanceId1", ar.getParentServiceInstance().getServiceInstanceId());
        assertNotNull(ar.getParentServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName1", ar.getParentServiceInstance().getVnfs().get(0).getVnfName());

        AllottedResource ar2 = si.getAllottedResources().get(1);
        assertNotNull(ar2);
        assertEquals("testServiceInstanceId2", ar2.getParentServiceInstance().getServiceInstanceId());
        assertNotNull(ar2.getParentServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName2", ar2.getParentServiceInstance().getVnfs().get(0).getVnfName());

        AllottedResource ar3 = si.getAllottedResources().get(2);
        assertNotNull(ar3);
        assertNotNull(ar3.getParentServiceInstance().getSolutionInfo());
        assertEquals("testCloudRegionId3",
                ar3.getParentServiceInstance().getSolutionInfo().getTargetedCloudRegion().getLcpCloudRegionId());
    }

    @Test
    public void testProcessSolution_success_3Allotteds1Vnf_1Solution() {
        beforeVnf();
        beforeAllottedResource();

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray solution1 = new JSONArray();
        JSONArray licenseSolution = new JSONArray();
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId1")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId1")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId1"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId2")
                .put("solution",
                        new JSONObject().put("identifierType", "serviceInstanceId").put("identifiers",
                                new JSONArray().put("testServiceInstanceId2")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName2"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                .put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1"))
                                .put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId2"))));
        solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId3")
                .put("solution",
                        new JSONObject().put("identifierType", "cloudRegionId").put("identifiers",
                                new JSONArray().put("testCloudRegionId3")))
                .put("assignmentInfo",
                        new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
                                .put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
                                .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
                                .put(new JSONObject().put("key", "aicVersion").put("value", "3"))));

        licenseSolution.put(new JSONObject().put("serviceResourceId", "testVnfId1")
                .put("entitlementPoolUUID",
                        new JSONArray().put("f1d563e8-e714-4393-8f99-cc480144a05e")
                                .put("j1d563e8-e714-4393-8f99-cc480144a05e"))
                .put("licenseKeyGroupUUID", new JSONArray().put("s1d563e8-e714-4393-8f99-cc480144a05e")
                        .put("b1d563e8-e714-4393-8f99-cc480144a05e")));

        asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1))
                .put("licenseSolutions", licenseSolution));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        assertFalse(si.getAllottedResources().isEmpty());
        AllottedResource ar = si.getAllottedResources().get(0);
        assertNotNull(ar);
        assertEquals("testServiceInstanceId1", ar.getParentServiceInstance().getServiceInstanceId());
        assertNotNull(ar.getParentServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName1", ar.getParentServiceInstance().getVnfs().get(0).getVnfName());

        AllottedResource ar2 = si.getAllottedResources().get(1);
        assertNotNull(ar2);
        assertEquals("testServiceInstanceId2", ar2.getParentServiceInstance().getServiceInstanceId());
        assertNotNull(ar2.getParentServiceInstance().getSolutionInfo());
        assertEquals("testVnfHostName2", ar2.getParentServiceInstance().getVnfs().get(0).getVnfName());

        AllottedResource ar3 = si.getAllottedResources().get(2);
        assertNotNull(ar3);
        assertNotNull(ar3.getParentServiceInstance().getSolutionInfo());
        assertEquals("testCloudRegionId3",
                ar3.getParentServiceInstance().getSolutionInfo().getTargetedCloudRegion().getLcpCloudRegionId());

        GenericVnf vnf = si.getVnfs().get(0);
        assertNotNull(vnf);
        assertNotNull(vnf.getLicense());
        assertEquals(2, vnf.getLicense().getEntitlementPoolUuids().size());
        assertEquals("s1d563e8-e714-4393-8f99-cc480144a05e", vnf.getLicense().getLicenseKeyGroupUuids().get(0));

    }

    @Test
    public void testProcessSolution_success_1Vnf_1Solution() {
        beforeVnf();

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray licenseSolution = new JSONArray();

        licenseSolution.put(new JSONObject().put("serviceResourceId", "testVnfId1")
                .put("entitlementPoolUUID",
                        new JSONArray().put("f1d563e8-e714-4393-8f99-cc480144a05e")
                                .put("j1d563e8-e714-4393-8f99-cc480144a05e"))
                .put("licenseKeyGroupUUID", new JSONArray().put("s1d563e8-e714-4393-8f99-cc480144a05e")
                        .put("b1d563e8-e714-4393-8f99-cc480144a05e")));

        asyncResponse.put("solutions", new JSONObject().put("licenseSolutions", licenseSolution));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        GenericVnf vnf = si.getVnfs().get(0);
        assertNotNull(vnf);
        assertNotNull(vnf.getLicense());
        assertEquals(2, vnf.getLicense().getEntitlementPoolUuids().size());
        assertEquals(2, vnf.getLicense().getLicenseKeyGroupUuids().size());
        assertEquals("f1d563e8-e714-4393-8f99-cc480144a05e", vnf.getLicense().getEntitlementPoolUuids().get(0));
        assertEquals("s1d563e8-e714-4393-8f99-cc480144a05e", vnf.getLicense().getLicenseKeyGroupUuids().get(0));
    }

    @Test
    public void testProcessSolution_success_1ServiceProxy_1Solutions() {
        beforeServiceProxy();

        JSONObject asyncResponse = new JSONObject();
        asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState",
                "completed");
        JSONArray solution1 = new JSONArray();
        solution1
                .put(new JSONObject()
                        .put("serviceResourceId", "testProxyId1").put(
                                "solution",
                                new JSONObject()
                                        .put("identifierType", "serviceInstanceId")
                                        .put("identifiers", new JSONArray().put("testServiceInstanceId1")))
                        .put("assignmentInfo",
                                new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
                                        .put(new JSONObject().put("key", "cloudOwner").put("value", ""))
                                        .put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1"))
                                        .put(new JSONObject().put("key", "aicVersion").put("value", "3"))
                                        .put(new JSONObject().put("key", "cloudRegionId").put("value", ""))
                                        .put(new JSONObject().put("key", "primaryPnfName").put("value",
                                                "testPrimaryPnfName"))
                                        .put(new JSONObject().put("key", "secondaryPnfName").put("value",
                                                "testSecondaryPnfName"))));

        asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1))
                .put("licenseSolutions", new JSONArray()));

        sniroHoming.processSolution(execution, asyncResponse.toString());

        ServiceInstance si =
                execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

        ServiceProxy sp = si.getServiceProxies().get(0);
        assertNotNull(sp);
        assertNotNull(sp.getServiceInstance());

        assertEquals("testServiceInstanceId1", sp.getServiceInstance().getServiceInstanceId());
        assertNotNull(sp.getServiceInstance().getSolutionInfo());

        assertFalse(sp.getServiceInstance().getPnfs().isEmpty());
        assertEquals("testPrimaryPnfName", sp.getServiceInstance().getPnfs().get(0).getPnfName());
        assertEquals("primary", sp.getServiceInstance().getPnfs().get(0).getRole());
        assertEquals("testSecondaryPnfName", sp.getServiceInstance().getPnfs().get(1).getPnfName());
        assertEquals("secondary", sp.getServiceInstance().getPnfs().get(1).getRole());
    }


    @Test(expected = BpmnError.class)
    public void testCallSniro_error_0Resources() throws BadResponseException {

        sniroHoming.callSniro(execution);

        verify(sniroClient, times(0)).postDemands(isA(SniroManagerRequest.class));
    }

    @Test(expected = BpmnError.class)
    public void testCallSniro_error_badResponse() throws BadResponseException {
        beforeAllottedResource();

        mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";
        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        sniroHoming.callSniro(execution);

        verify(sniroClient, times(1)).postDemands(isA(SniroManagerRequest.class));
    }

}
