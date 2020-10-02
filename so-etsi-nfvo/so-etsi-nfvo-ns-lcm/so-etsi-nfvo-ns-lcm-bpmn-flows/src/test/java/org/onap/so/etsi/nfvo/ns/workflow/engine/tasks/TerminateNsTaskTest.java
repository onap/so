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

import com.google.gson.Gson;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.hamcrest.text.MatchesPattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStatusRetrievalStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.OperationStateEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.model.TerminateNsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_REST_TEMPLATE_BEAN;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterConfiguration.SOL003_ADAPTER_REST_TEMPLATE_BEAN;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
public class TerminateNsTaskTest extends BaseTest {

    @Autowired
    @Qualifier(SOL003_ADAPTER_REST_TEMPLATE_BEAN)
    private RestTemplate sol003AdapterRestTemplate;

    private MockRestServiceServer mockSol003AdapterRestServiceServer;

    @Autowired
    private JobExecutorService objUnderTest;

    @Autowired
    private GsonProvider gsonProvider;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Gson gson;

    @Before
    public void before() {
        wireMockServer.resetAll();
        gson = gsonProvider.getGson();
        mockSol003AdapterRestServiceServer =
                MockRestServiceServer.bindTo(sol003AdapterRestTemplate).ignoreExpectOrder(true).build();
        sol003AdapterRestTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));
    }

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    @Test
    public void testRunTerminateNsJob_timeSetInTerminateRequest_throwsNsRequestProcessingException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        final TerminateNsRequest terminateNsRequest = new TerminateNsRequest().terminationTime(LocalDateTime.now());
        final String message = "TerminateNsRequest received with terminateTime: "
                + terminateNsRequest.getTerminationTime()
                + "\nOnly immediate Terminate requests are currently supported \n(i.e., terminateTime field must not be set).";
        expectedException.expect(NsRequestProcessingException.class);
        expectedException.expectMessage(message);
        objUnderTest.runTerminateNsJob(nsInstanceId, terminateNsRequest);
    }

    @Test
    public void testRunTerminateNsJob_NsInstNotInDb_throwsNsRequestProcessingException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        final TerminateNsRequest terminateNsRequest = new TerminateNsRequest();
        final String message = "No matching NS Instance for id: " + nsInstanceId + " found in database.";
        assertThat(databaseServiceProvider.getNfvoNsInst(nsInstanceId)).isEmpty();
        expectedException.expect(NsRequestProcessingException.class);
        expectedException.expectMessage(message);
        objUnderTest.runTerminateNsJob(nsInstanceId, terminateNsRequest);
    }

    @Test
    public void testTerminateNsTask_SuccessfulCase() throws InterruptedException, IOException {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId);
        mockSol003AdapterEndpoints();
        mockAAIEndpoints();

        final String nsLcmOpOccId = objUnderTest.runTerminateNsJob(nsInstanceId, new TerminateNsRequest());

        final Optional<NfvoJob> optional = getJobByResourceId(nsInstanceId);
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();

        // Confirm Process finishes in STATE_COMPLETED
        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        // Confirm NS Instance set to NOT_INSTANTIATED and related NF Instances Deleted
        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        final NfvoNsInst nfvoNsInst = optionalNfvoNsInst.get();
        assertEquals(State.NOT_INSTANTIATED, nfvoNsInst.getStatus());
        final List<NfvoNfInst> nfvoNfInsts = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstanceId);
        assertTrue(nfvoNfInsts.isEmpty());

        // Confirm NS LCM OP OCC Job set to Completed
        final Optional<NsLcmOpOcc> optionalNsLcmOpOcc = databaseServiceProvider.getNsLcmOpOcc(nsLcmOpOccId);
        assertTrue(optionalNsLcmOpOcc.isPresent());
        final NsLcmOpOcc nsLcmOpOcc = optionalNsLcmOpOcc.get();
        assertEquals(OperationStateEnum.COMPLETED, nsLcmOpOcc.getOperationState());
    }

    private void addDummyNsToDatabase(final String nsInstanceId) {
        final String nsPackageId = UUID.randomUUID().toString();
        final NfvoNsInst nfvoNsInst =
                new NfvoNsInst().nsInstId(nsInstanceId).name("nsName").nsPackageId(nsPackageId).nsdId("nsdId")
                        .nsdInvariantId("nsdId").status(State.INSTANTIATED).statusUpdatedTime(LocalDateTime.now());
        databaseServiceProvider.saveNfvoNsInst(nfvoNsInst);
        addDummyNfToDatabase(nfvoNsInst);
        addDummyNfToDatabase(nfvoNsInst);
        addDummyNfToDatabase(nfvoNsInst);
    }

    private void addDummyNfToDatabase(final NfvoNsInst nfvoNsInst) {
        final LocalDateTime localDateTime = LocalDateTime.now();
        final String nfPackageId = UUID.randomUUID().toString();
        final NfvoNfInst nfvoNfInst =
                new NfvoNfInst().status(State.INSTANTIATED).createTime(localDateTime).lastUpdateTime(localDateTime)
                        .name("nfName").vnfdId("vnfdId").packageId(nfPackageId).nfvoNsInst(nfvoNsInst);
        databaseServiceProvider.saveNfvoNfInst(nfvoNfInst);
    }

    private void mockSol003AdapterEndpoints() {
        final int numTimes = 3;

        mockSol003AdapterRestServiceServer
                .expect(times(numTimes),
                        requestTo(MatchesPattern.matchesPattern(SOL003_ADAPTER_ENDPOINT_URL + "/vnfs/.*")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess(gson.toJson(new DeleteVnfResponse().jobId(UUID.randomUUID().toString())),
                        MediaType.APPLICATION_JSON));

        mockSol003AdapterRestServiceServer
                .expect(times(numTimes),
                        requestTo(MatchesPattern.matchesPattern(SOL003_ADAPTER_ENDPOINT_URL + "/jobs/.*")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(
                        new org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse().operationState(
                                org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum.COMPLETED)
                                .operationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND)),
                        MediaType.APPLICATION_JSON));
    }

    private void mockAAIEndpoints() {
        final String modelEndpoint = "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + UUID_REGEX;
        final String resourceVersion = "12345";

        final String body =
                "{\"resource-version\": \"" + resourceVersion + "\",\n\"orchestration-status\": \"Assigned\"}";
        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok()).willReturn(okJson(body)));

        wireMockServer.stubFor(
                delete(urlMatching(modelEndpoint + "\\?resource-version=" + resourceVersion)).willReturn(ok()));
    }

}
