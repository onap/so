/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.workflow.engine.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_REST_TEMPLATE_BEAN;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterConfiguration.SOL003_ADAPTER_REST_TEMPLATE_BEAN;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.hamcrest.text.MatchesPattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStatusRetrievalStatusEnum;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.VnfPkgInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.NetworkServiceDescriptor;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.OperationStateEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class InstantiateNsTaskTest extends BaseTest {

    private static final String TENANT_ID = "6ca8680feba04dad9254f423c6e28e1c";
    private static final String CLOUD_REGION = "regionOne";
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String UUID_REGEX =
            "[0-9a-zA-Z]{8}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{4}\\-[0-9a-zA-Z]{12}";
    private static final String VCPE_VBRGEMU_VNFD_ID = "b1bb0ce7-2222-4fa7-95ed-4840d70a1102";
    private static final String VCPE_VBNG = "b1bb0ce7-2222-4fa7-95ed-4840d70a1101";
    private static final String VCPE_INFRA_VNFD_ID = "b1bb0ce7-2222-4fa7-95ed-4840d70a1100";
    private static final String VGMUX_VNFD_ID = "0408f076-e6c0-4c82-9940-272fddbb82de";
    private static final String VGW_VNFD_ID = "3fca3543-07f5-492f-812c-ed462e4f94f4";
    private static final String[] VCPE_VNFDS =
            new String[] {VGW_VNFD_ID, VGMUX_VNFD_ID, VCPE_INFRA_VNFD_ID, VCPE_VBNG, VCPE_VBRGEMU_VNFD_ID};
    private static final String SAMPLE_NSD_FILE = "src/test/resources/ns.csar";
    private static final String NS_NAME = "InstantiateNetworkService";

    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Autowired
    @Qualifier(ETSI_CATALOG_REST_TEMPLATE_BEAN)
    private RestTemplate etsiCatalogRestTemplate;

    @Autowired
    @Qualifier(SOL003_ADAPTER_REST_TEMPLATE_BEAN)
    private RestTemplate sol003AdapterRestTemplate;

    private MockRestServiceServer mockEtsiCatalogRestServiceServer;

    private MockRestServiceServer mockSol003AdapterRestServiceServer;

    @Autowired
    private JobExecutorService objUnderTest;

    @Autowired
    private GsonProvider gsonProvider;

    private Gson gson;

    private static final Map<String, String> VNFD_ID_TO_VNFPKG_ID_MAPPING = new HashMap<>();
    static {
        for (final String vnfd : VCPE_VNFDS) {
            VNFD_ID_TO_VNFPKG_ID_MAPPING.put(vnfd, UUID.randomUUID().toString());
        }
    }

    @Before
    public void before() {
        wireMockServer.resetAll();
        gson = gsonProvider.getGson();

        mockEtsiCatalogRestServiceServer =
                MockRestServiceServer.bindTo(etsiCatalogRestTemplate).ignoreExpectOrder(true).build();
        mockSol003AdapterRestServiceServer =
                MockRestServiceServer.bindTo(sol003AdapterRestTemplate).ignoreExpectOrder(true).build();

        etsiCatalogRestTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));
        sol003AdapterRestTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));

    }

    @After
    public void after() {
        wireMockServer.resetAll();
        mockEtsiCatalogRestServiceServer.reset();
    }

    @Test
    public void testInstantiateNsWorkflow_JustUpdateStatus_SuccessfullCase() throws InterruptedException, IOException {
        final String nsdId = UUID.randomUUID().toString();
        final String nsdName = NS_NAME + "-" + System.currentTimeMillis();

        final NfvoNsInst newNfvoNsInst = new NfvoNsInst().nsInstId(nsdId).name(nsdName)
                .nsPackageId(UUID.randomUUID().toString()).nsPackageId(nsdId).nsdId(nsdId).nsdInvariantId(nsdId)
                .status(State.NOT_INSTANTIATED).statusUpdatedTime(LocalDateTime.now());

        databaseServiceProvider.saveNfvoNsInst(newNfvoNsInst);

        mockSol003AdapterEndpoints();
        mockAAIEndpoints(nsdId);
        mockEtsiCatalogEndpoints(nsdId);

        final String nsLcmOpOccId =
                objUnderTest.runInstantiateNsJob(newNfvoNsInst.getNsInstId(), getInstantiateNsRequest());

        final Optional<NfvoJob> optional = getJobByResourceId(newNfvoNsInst.getNsInstId());
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();

        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        // check if value in database has updated
        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsdId);
        final NfvoNsInst nfvoNsInst = optionalNfvoNsInst.get();
        assertEquals(State.INSTANTIATED, nfvoNsInst.getStatus());

        final HistoricVariableInstance historicVariableInstance =
                getVariable(nfvoJob.getProcessInstanceId(), NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME);
        assertNotNull(historicVariableInstance);
        final NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) historicVariableInstance.getValue();
        assertNotNull(nsd);

        assertEquals(VNFD_ID_TO_VNFPKG_ID_MAPPING.size(), nsd.getVnfs().size());

        final List<NfvoNfInst> nfvoNfInsts = databaseServiceProvider.getNfvoNfInstByNsInstId(nsdId);
        assertNotNull(nsd);
        assertEquals(VNFD_ID_TO_VNFPKG_ID_MAPPING.size(), nfvoNfInsts.size());

        final Optional<NsLcmOpOcc> optionalNsLcmOpOcc = databaseServiceProvider.getNsLcmOpOcc(nsLcmOpOccId);
        assertTrue(optionalNsLcmOpOcc.isPresent());

        assertEquals(OperationStateEnum.COMPLETED, optionalNsLcmOpOcc.get().getOperationState());

        final Map<String, NfvoNfInst> nfvoNfInstsMap =
                nfvoNfInsts.stream().collect(Collectors.toMap(NfvoNfInst::getVnfdId, nfvoNfInst -> nfvoNfInst));

        for (final Entry<String, String> entry : VNFD_ID_TO_VNFPKG_ID_MAPPING.entrySet()) {
            assertTrue(nfvoNfInstsMap.containsKey(entry.getKey()));
            assertEquals(State.INSTANTIATED, nfvoNfInstsMap.get(entry.getKey()).getStatus());

        }

    }

    private void mockSol003AdapterEndpoints() {
        mockSol003AdapterRestServiceServer
                .expect(times(VNFD_ID_TO_VNFPKG_ID_MAPPING.size()),
                        requestTo(MatchesPattern.matchesPattern(SOL003_ADAPTER_ENDPOINT_URL + "/vnfs/.*")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(new CreateVnfResponse().jobId(UUID.randomUUID().toString())),
                        MediaType.APPLICATION_JSON));

        mockSol003AdapterRestServiceServer
                .expect(times(VNFD_ID_TO_VNFPKG_ID_MAPPING.size()),
                        requestTo(MatchesPattern.matchesPattern(SOL003_ADAPTER_ENDPOINT_URL + "/jobs/.*")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(
                        new org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse().operationState(
                                org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum.COMPLETED)
                                .operationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND)),
                        MediaType.APPLICATION_JSON));
    }

    private void mockEtsiCatalogEndpoints(final String nsdId) throws IOException {
        mockEtsiCatalogRestServiceServer.expect(requestTo(ETSI_CATALOG_URL + "/nsd/v1/ns_descriptors/" + nsdId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(getNSPackageModel(nsdId)), MediaType.APPLICATION_JSON));
        mockEtsiCatalogRestServiceServer
                .expect(requestTo(ETSI_CATALOG_URL + "/nsd/v1/ns_descriptors/" + nsdId + "/nsd_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
                        getFileContent(getAbsolutePath(SAMPLE_NSD_FILE)), MediaType.APPLICATION_OCTET_STREAM));

        for (final Entry<String, String> entry : VNFD_ID_TO_VNFPKG_ID_MAPPING.entrySet()) {
            mockEtsiCatalogRestServiceServer
                    .expect(requestTo(ETSI_CATALOG_URL + "/vnfpkgm/v1/vnf_packages/" + entry.getValue()))
                    .andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
                            gson.toJson(getVnfPkgInfo(entry.getKey(), entry.getValue())), MediaType.APPLICATION_JSON));
        }

    }

    private VnfPkgInfo getVnfPkgInfo(final String vnfdId, final String vnfPkgId) {
        return new VnfPkgInfo().id(vnfPkgId).vnfdId(vnfdId);
    }

    private InstantiateNsRequest getInstantiateNsRequest() {
        final Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("vim_id", CLOUD_OWNER + "_" + CLOUD_REGION + "_" + TENANT_ID);

        final InstantiateNsRequest instantiateNsRequest = new InstantiateNsRequest().nsFlavourId("default");

        for (final Entry<String, String> entry : VNFD_ID_TO_VNFPKG_ID_MAPPING.entrySet()) {
            instantiateNsRequest
                    .addAdditionalParamsForVnfItem(new NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf()
                            .vnfProfileId(entry.getValue()).additionalParams(additionalParams));
        }

        return instantiateNsRequest;
    }

    private NsdInfo getNSPackageModel(final String nsdId) {
        return new NsdInfo().id(nsdId).nsdId(nsdId).nsdInvariantId(NSD_INVARIANT_ID).nsdName("vcpe").nsdDesigner("ONAP")
                .vnfPkgIds(new ArrayList<>(VNFD_ID_TO_VNFPKG_ID_MAPPING.values()));
    }

    private byte[] getFileContent(final String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }


    private String getAbsolutePath(final String path) {
        return new File(path).getAbsolutePath();
    }

    private void mockAAIEndpoints(final String nsdId) {
        final String modelEndpoint = "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + UUID_REGEX;
        wireMockServer.stubFor(
                get(urlMatching(modelEndpoint + "\\?resultIndex=0&resultSize=1&format=count")).willReturn(notFound()));

        wireMockServer.stubFor(put(urlMatching(modelEndpoint)).willReturn(ok()));
        wireMockServer.stubFor(put(urlMatching(modelEndpoint + "/relationship-list/relationship")).willReturn(ok()));

        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok())
                .willReturn(okJson("{\"orchestration-status\": \"Created\"}")));

        final String resourceType = "service-instance";
        final String resourceLink = "/aai/v20/business/customers/customer/" + GLOBAL_CUSTOMER_ID
                + "/service-subscriptions/service-subscription/NetworkService/service-instances/service-instance/"
                + nsdId;

        final String body = "{\n" + "  \"results\": [{\n" + "    \"resource-type\": \"" + resourceType + "\",\n"
                + "    \"resource-link\": \"" + resourceLink + "\"\n" + "  }]\n" + "}";

        wireMockServer.stubFor(
                get(urlMatching("/aai/v[0-9]+/nodes/service-instances/service-instance/" + nsdId + "\\?format=pathed"))
                        .willReturn(okJson(body)));

        wireMockServer
                .stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/" + CLOUD_OWNER
                        + "/" + CLOUD_REGION + "/tenants/tenant/" + TENANT_ID + "/relationship-list/relationship"))
                                .willReturn(ok()));

    }

}
