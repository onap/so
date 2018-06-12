package org.openecomp.mso.bpmn.buildingblock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.mock.FileUtil;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.Candidate;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.CandidateType;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestParameters;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.ServiceProxy;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.sniro.beans.SniroManagerRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@Ignore
public class SniroHomingV2Test extends BaseTest{

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8446));


	private ServiceInstance serviceInstance;

	private RequestContext requestContext;

	private Customer customer;
	ObjectMapper mapper = new ObjectMapper();

	private static final String RESOURCE_PATH = "__files/BuildingBlocks/SniroHoming/";


    String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"corys cool\", \"requestStatus\": \"accepted\"}";

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
		params.setSubscriptionServiceType("iptollfree");
		requestContext.setRequestParameters(params);
	}

	public void beforeVpnBondingLink(String id){
		VpnBondingLink bondingLink = new VpnBondingLink();
		bondingLink.setVpnBondingLinkId("testVpnBondingId" + id);
		bondingLink.getServiceProxies().add(setServiceProxy("1", "transport"));
		ServiceProxy sp2 = setServiceProxy("2", "infrastructure");
		Candidate requiredCandidate = new Candidate();
		requiredCandidate.setCandidateType(CandidateType.VNF_ID);
		List<String> c = new ArrayList<String>();
		c.add("testVnfId");
		requiredCandidate.setCandidates(c);
		sp2.addRequiredCandidates(requiredCandidate);
		bondingLink.getServiceProxies().add(sp2);
		serviceInstance.getVpnBondingLinks().add(bondingLink);

	}

	public void beforeAllottedResource(){
		serviceInstance.getAllottedResources().add(setAllottedResource("1"));
		serviceInstance.getAllottedResources().add(setAllottedResource("2"));
		serviceInstance.getAllottedResources().add(setAllottedResource("3"));
	}

	public void beforeVnf(){
		serviceInstance.getVnfs().add(setGenericVnf());
	}

	@Ignore
    @Test(expected = Test.None.class)
	public void testCallSniro_success_1VpnLink() throws BadResponseException, IOException{
    	beforeVpnBondingLink("1");

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		String request = FileUtil.readResourceFile(RESOURCE_PATH + "SniroManagerRequest1Vpn.json");

		ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
		verify(sniroClient, times(1)).postDemands(argument.capture());
		assertEquals(request, argument.getValue().toJsonString());
	}

	@Test
	@Ignore
	public void testCallSniro_success_3VpnLink() throws JsonProcessingException, BadResponseException{
    	beforeVpnBondingLink("1");
    	beforeVpnBondingLink("2");
    	beforeVpnBondingLink("3");

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		String request = FileUtil.readResourceFile(RESOURCE_PATH + "SniroManagerRequest3Vpn.json");

		ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
		verify(sniroClient, times(1)).postDemands(argument.capture());
		assertEquals(request, argument.getValue().toJsonString());
	}

	@Test
	@Ignore
	public void testCallSniro_success_3Allotteds() throws BadResponseException, JsonProcessingException{
		beforeAllottedResource();

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
		verify(sniroClient, times(1)).postDemands(argument.capture());
		//TODO	assertEquals(request, argument.getValue().toJsonString());
	}

	@Test
	@Ignore
	public void testCallSniro_success_1Vnf() throws JsonProcessingException, BadResponseException{
		beforeVnf();

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		ArgumentCaptor<SniroManagerRequest> argument = ArgumentCaptor.forClass(SniroManagerRequest.class);
		verify(sniroClient, times(1)).postDemands(argument.capture());
		//TODO assertEquals(request, argument.getValue().toJsonString());
	}

	@Test
	@Ignore
	public void testCallSniro_success_3Allotteds1Vnf() throws JsonProcessingException, BadResponseException{
		beforeAllottedResource();
		beforeVnf();

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		verify(sniroClient, times(1)).postDemands(isA(SniroManagerRequest.class));
	}

	@Test(expected = Test.None.class)
	public void testProcessSolution_success_1VpnLink_1Solution(){
    	beforeVpnBondingLink("1");

		JSONObject asyncResponse = new JSONObject();
		asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState", "completed");
		JSONArray solution1 = new JSONArray();
		solution1.put(new JSONObject().put("serviceResourceId", "testProxyId1").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId1")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId1"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testProxyId2").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId2")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName2"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "secondaryPnfName").put("value", "testSecondaryPnfName2")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId2"))));

		asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1)).put("licenseSolutions", new JSONArray()));

		sniroHoming.processSolution(execution, asyncResponse.toString());

		ServiceInstance si = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

		assertFalse(si.getVpnBondingLinks().isEmpty());
		VpnBondingLink link = si.getVpnBondingLinks().get(0);
		assertNotNull(link);
		assertFalse(link.getServiceProxies().isEmpty());

		assertEquals("testServiceInstanceId1", link.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
		assertNotNull(link.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
		assertEquals("testVnfHostName1", link.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

		assertEquals("testServiceInstanceId2", link.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
		assertNotNull(link.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
		assertFalse(link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
		assertEquals("testPrimaryPnfName2", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
		assertEquals("primary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
		assertEquals("testSecondaryPnfName2", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
		assertEquals("secondary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());
	}

	@Test
	public void testProcessSolution_success_1VpnLink_2Solutions(){
    	beforeVpnBondingLink("1");

		JSONObject asyncResponse = new JSONObject();
		asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState", "completed");
		JSONArray solution1 = new JSONArray();
		solution1.put(new JSONObject().put("serviceResourceId", "testProxyId1").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId1")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId1"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testProxyId2").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId2")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName2"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "secondaryPnfName").put("value", "testSecondaryPnfName2")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId2"))));

		JSONArray solution2 = new JSONArray();
		solution2.put(new JSONObject().put("serviceResourceId", "testProxyId1").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId3")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName3"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli3")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId3")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId3"))));
		solution2.put(new JSONObject().put("serviceResourceId", "testProxyId2").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId4")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "False"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "primaryPnfName").put("value", "testPrimaryPnfName4"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli4")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "secondaryPnfName").put("value", "testSecondaryPnfName4")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testSloudRegionId4"))));

		asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1).put(solution2)).put("licenseSolutions", new JSONArray()));

		sniroHoming.processSolution(execution, asyncResponse.toString());

		ServiceInstance si = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

		assertFalse(si.getVpnBondingLinks().isEmpty());
		VpnBondingLink link = si.getVpnBondingLinks().get(0);
		VpnBondingLink link2 = si.getVpnBondingLinks().get(1);
		assertNotNull(link);
		assertFalse(link.getServiceProxies().isEmpty());

		assertEquals("testServiceInstanceId1", link.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
		assertNotNull(link.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
		assertEquals("testVnfHostName1", link.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

		assertEquals("testServiceInstanceId2", link.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
		assertNotNull(link.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
		assertFalse(link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
		assertEquals("testPrimaryPnfName2", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
		assertEquals("primary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
		assertEquals("testSecondaryPnfName2", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
		assertEquals("secondary", link.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());

		assertNotNull(link2);
		assertFalse(link2.getServiceProxies().isEmpty());

		assertEquals("testServiceInstanceId3", link2.getServiceProxy("testProxyId1").getServiceInstance().getServiceInstanceId());
		assertNotNull(link2.getServiceProxy("testProxyId1").getServiceInstance().getSolutionInfo());
		assertEquals("testVnfHostName3", link2.getServiceProxy("testProxyId1").getServiceInstance().getVnfs().get(0).getVnfName());

		assertEquals("testServiceInstanceId4", link2.getServiceProxy("testProxyId2").getServiceInstance().getServiceInstanceId());
		assertNotNull(link2.getServiceProxy("testProxyId2").getServiceInstance().getSolutionInfo());
		assertFalse(link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().isEmpty());
		assertEquals("testPrimaryPnfName4", link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getPnfName());
		assertEquals("primary", link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(0).getRole());
		assertEquals("testSecondaryPnfName4", link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getPnfName());
		assertEquals("secondary", link2.getServiceProxy("testProxyId2").getServiceInstance().getPnfs().get(1).getRole());

	}

	@Test
	public void testProcessSolution_success_3VpnLink_2Solutions(){
		//TODO
	}

	@Test
	public void testProcessSolution_success_3Allotteds_1Solution(){
		beforeAllottedResource();

		JSONObject asyncResponse = new JSONObject();
		asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState", "completed");
		JSONArray solution1 = new JSONArray();
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId1").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId1")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId1"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId2").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId2")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName2"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId2"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId3").put("inventoryType", "cloud").put("solution", new JSONObject()
				.put("identifierType", "cloudRegionId").put("identifiers", new JSONArray().put("testCloudRegionId3")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))));

		asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1)).put("licenseSolutions", new JSONArray()));

		sniroHoming.processSolution(execution, asyncResponse.toString());

		ServiceInstance si = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

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
		assertEquals("testCloudRegionId3", ar3.getParentServiceInstance().getSolutionInfo().getTargetedCloudRegion().getLcpCloudRegionId());
	}

	@Test
	@Ignore
	public void testProcessSolution_success_3Allotteds1Vnf_1Solution(){
		beforeVnf();
		beforeAllottedResource();

		JSONObject asyncResponse = new JSONObject();
		asyncResponse.put("transactionId", "testRequestId").put("requestId", "testRequestId").put("requestState", "completed");
		JSONArray solution1 = new JSONArray();
		JSONArray licenseSolution = new JSONArray();
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId1").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId1")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName1"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli1")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId1"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId2").put("inventoryType", "service").put("solution", new JSONObject()
				.put("identifierType", "serviceInstanceId").put("identifiers", new JSONArray().put("testServiceInstanceId2")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "vnfHostName").put("value", "testVnfHostName2"))
						.put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2")).put(new JSONObject().put("key", "aicVersion").put("value", "3"))
						.put(new JSONObject().put("key", "vnfId").put("value", "testVnfId1")).put(new JSONObject().put("key", "cloudRegionId").put("value", "testCloudRegionId2"))));
		solution1.put(new JSONObject().put("serviceResourceId", "testAllottedResourceId3").put("inventoryType", "cloud").put("solution", new JSONObject()
				.put("identifierType", "cloudRegionId").put("identifiers", new JSONArray().put("testCloudRegionId3")))
				.put("assignmentInfo", new JSONArray().put(new JSONObject().put("key", "isRehome").put("value", "True"))
						.put(new JSONObject().put("key", "cloudOwner").put("value", "aic")).put(new JSONObject().put("key", "aicClli").put("value", "testAicClli2"))
						.put(new JSONObject().put("key", "aicVersion").put("value", "3"))));

		licenseSolution.put(
				new JSONObject().put("serviceResourceId", "testVnfId").put("entitlementPoolUUID", new JSONArray().put("f1d563e8-e714-4393-8f99-cc480144a05e").put("j1d563e8-e714-4393-8f99-cc480144a05e"))
						.put("licenseKeyGroupUUID", new JSONArray().put("s1d563e8-e714-4393-8f99-cc480144a05e").put("b1d563e8-e714-4393-8f99-cc480144a05e")));

		asyncResponse.put("solutions", new JSONObject().put("placementSolutions", new JSONArray().put(solution1)).put("licenseSolutions", licenseSolution));

		sniroHoming.processSolution(execution, asyncResponse.toString());

		ServiceInstance si = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription().getServiceInstances().get(0);

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
		assertEquals("testCloudRegionId3", ar3.getParentServiceInstance().getSolutionInfo().getTargetedCloudRegion().getLcpCloudRegionId());

		GenericVnf vnf = si.getVnfs().get(0);
		assertNotNull(vnf);
		assertEquals(2, vnf.getLicense().getEntitlementPoolUuids().size());
		assertEquals("s1d563e8-e714-4393-8f99-cc480144a05e", vnf.getLicense().getLicenseKeyGroupUuids().get(0));

	}

	@Test

	public void testProcessSolution_success_1Vnf_1Solution(){

	}

	@Test
	public void testProcessSolution_success_1Vnf_2Solutions(){

	}

	@Test(expected = BpmnError.class)
	public void testCallSniro_error_0Resources() throws BadResponseException, JsonProcessingException{

		sniroHoming.callSniro(execution);

		verify(sniroClient, times(0)).postDemands(isA(SniroManagerRequest.class));
	}

	@Test(expected = BpmnError.class)
	public void testCallSniro_error_badResponse() throws BadResponseException, JsonProcessingException{
		beforeAllottedResource();

		mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";
		wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
				.willReturn(aResponse().withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(mockResponse)));

		sniroHoming.callSniro(execution);

		verify(sniroClient, times(1)).postDemands(isA(SniroManagerRequest.class));
	}

}
