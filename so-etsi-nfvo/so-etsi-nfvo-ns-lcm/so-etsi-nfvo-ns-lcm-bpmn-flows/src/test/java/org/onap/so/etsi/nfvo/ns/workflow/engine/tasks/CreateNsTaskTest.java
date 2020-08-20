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
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_REST_TEMPLATE_BEAN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.WorkflowQueryService;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance.NsStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class CreateNsTaskTest extends BaseTest {
    private static final String NSD_ID = UUID.randomUUID().toString();
    private static final String NS_NAME = "CreateNetworkService-" + NSD_ID;
    private static final String CREATE_NS_WORKFLOW_NAME = "CreateNs";

    @Autowired
    @Qualifier(ETSI_CATALOG_REST_TEMPLATE_BEAN)
    private RestTemplate restTemplate;

    @Autowired
    private GsonProvider gsonProvider;

    @Autowired
    private JobExecutorService objUnderTest;

    @Autowired
    private WorkflowQueryService workflowQueryService;

    private MockRestServiceServer mockRestServiceServer;

    private Gson gson;

    @Before
    public void before() {
        wireMockServer.resetAll();
        final MockRestServiceServer.MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);
        builder.ignoreExpectOrder(true);
        mockRestServiceServer = builder.build();
        gson = gsonProvider.getGson();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));
    }

    @After
    public void after() {
        wireMockServer.resetAll();
        mockRestServiceServer.reset();
    }

    @Test
    public void testCreateNsWorkflow_SuccessfullCase() throws InterruptedException {
        final CreateNsRequest createNsRequest = getCreateNsRequest();

        mockEtsiCatalogEndpoints();
        mockAAIEndpoints(createNsRequest);

        final NsInstancesNsInstance nsResponse =
                objUnderTest.runCreateNsJob(createNsRequest, GLOBAL_CUSTOMER_ID, SERVICE_TYPE);
        assertNotNull(nsResponse);
        assertNotNull(nsResponse.getId());

        final Optional<NfvoJob> optional = getJobByResourceId(createNsRequest.getNsdId());
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();

        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));

        mockRestServiceServer.verify();
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());
        assertTrue(databaseServiceProvider.isNsInstExists(createNsRequest.getNsName()));

        final NfvoJob actualJob = optional.get();
        assertEquals(JobStatusEnum.FINISHED, actualJob.getStatus());

        assertEquals(NS_NAME, nsResponse.getNsInstanceName());
        assertEquals(NsStateEnum.NOT_INSTANTIATED, nsResponse.getNsState());

        final HistoricVariableInstance doesNsPackageExistsVar =
                getVariable(nfvoJob.getProcessInstanceId(), "doesNsPackageExists");
        assertNotNull(doesNsPackageExistsVar);
        assertTrue((boolean) doesNsPackageExistsVar.getValue());

        final HistoricVariableInstance doesNsInstanceExistsVar =
                getVariable(nfvoJob.getProcessInstanceId(), "doesNsInstanceExists");
        assertNotNull(doesNsInstanceExistsVar);
        assertFalse((boolean) doesNsInstanceExistsVar.getValue());

    }

    @Test
    public void testCreateNsWorkflow_FailsToGetNsPackage() throws InterruptedException {
        final String nsdId = UUID.randomUUID().toString();
        final String nsdName = NS_NAME + "-" + System.currentTimeMillis();
        final CreateNsRequest createNsRequest = getCreateNsRequest(nsdId, nsdName);

        mockRestServiceServer.expect(requestTo(ETSI_CATALOG_URL + "/nsd/v1/ns_descriptors/" + nsdId))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        try {
            objUnderTest.runCreateNsJob(createNsRequest, GLOBAL_CUSTOMER_ID, SERVICE_TYPE);
            fail("runCreateNsJob should throw exception");
        } catch (final Exception exception) {
            assertEquals(NsRequestProcessingException.class, exception.getClass());
        }

        final Optional<NfvoJob> optional = getJobByResourceId(createNsRequest.getNsdId());
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();
        assertEquals(JobStatusEnum.ERROR, nfvoJob.getStatus());

        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));

        mockRestServiceServer.verify();
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());


        final HistoricVariableInstance nsResponseVariable =
                getVariable(nfvoJob.getProcessInstanceId(), CamundaVariableNameConstants.CREATE_NS_RESPONSE_PARAM_NAME);
        assertNull(nsResponseVariable);

        final Optional<InlineResponse400> problemDetailsOptional =
                workflowQueryService.getProblemDetails(nfvoJob.getProcessInstanceId());
        assertTrue(problemDetailsOptional.isPresent());

        final InlineResponse400 problemDetails = problemDetailsOptional.get();
        assertNotNull(problemDetails);
        assertNotNull(problemDetails.getDetail());

        final HistoricVariableInstance doesNsPackageExistsVar =
                getVariable(nfvoJob.getProcessInstanceId(), "doesNsPackageExists");
        assertNotNull(doesNsPackageExistsVar);
        assertFalse((boolean) doesNsPackageExistsVar.getValue());
        assertEquals("Unexpected exception occured while getting ns package using nsdId: " + nsdId,
                problemDetails.getDetail());
    }

    @Test
    public void testCreateNsWorkflow_FailsToFindJobUsingJobId() throws InterruptedException {
        final String nsdId = UUID.randomUUID().toString();
        final String nsdName = NS_NAME + "-" + System.currentTimeMillis();
        final CreateNsRequest createNsRequest = getCreateNsRequest(nsdId, nsdName);

        final String randomJobId = UUID.randomUUID().toString();
        final ProcessInstance processInstance =
                executeWorkflow(CREATE_NS_WORKFLOW_NAME, randomJobId, getVariables(randomJobId, createNsRequest));
        assertTrue(waitForProcessInstanceToFinish(processInstance.getProcessInstanceId()));

        mockRestServiceServer.verify();
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(processInstance.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final HistoricVariableInstance nsResponseVariable = getVariable(processInstance.getProcessInstanceId(),
                CamundaVariableNameConstants.CREATE_NS_RESPONSE_PARAM_NAME);

        assertNull(nsResponseVariable);

        final HistoricVariableInstance workflowExceptionVariable = getVariable(processInstance.getProcessInstanceId(),
                CamundaVariableNameConstants.CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME);

        final InlineResponse400 problemDetails = (InlineResponse400) workflowExceptionVariable.getValue();
        assertNotNull(problemDetails);
        assertNotNull(problemDetails.getDetail());
        assertEquals("Unable to find job using job id: " + randomJobId, problemDetails.getDetail());

    }

    @Test
    public void testCreateNsWorkflow_NsInstanceExistsInDb() throws InterruptedException {
        final String nsdId = UUID.randomUUID().toString();
        final String nsdName = NS_NAME + "-" + System.currentTimeMillis();
        final CreateNsRequest createNsRequest = getCreateNsRequest(nsdId, nsdName);

        databaseServiceProvider.saveNfvoNsInst(new NfvoNsInst().nsInstId(nsdId).name(createNsRequest.getNsName())
                .nsPackageId(UUID.randomUUID().toString()).nsdId(nsdId).nsdInvariantId(nsdId)
                .description(createNsRequest.getNsDescription()).status(State.INSTANTIATED)
                .statusUpdatedTime(LocalDateTime.now()).globalCustomerId(GLOBAL_CUSTOMER_ID).serviceType(SERVICE_TYPE));

        mockEtsiCatalogEndpoints(nsdId);

        try {
            objUnderTest.runCreateNsJob(createNsRequest, GLOBAL_CUSTOMER_ID, SERVICE_TYPE);
            fail("runCreateNsJob should throw exception");
        } catch (final Exception exception) {
            assertEquals(NsRequestProcessingException.class, exception.getClass());
        }

        final Optional<NfvoJob> optional = getJobByResourceId(createNsRequest.getNsdId());
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();
        assertEquals(JobStatusEnum.ERROR, nfvoJob.getStatus());

        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));

        mockRestServiceServer.verify();
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());
        assertTrue(databaseServiceProvider.isNsInstExists(createNsRequest.getNsName()));

        final HistoricVariableInstance historicVariableInstance =
                getVariable(nfvoJob.getProcessInstanceId(), CamundaVariableNameConstants.CREATE_NS_RESPONSE_PARAM_NAME);

        assertNull(historicVariableInstance);

        final Optional<InlineResponse400> problemDetailsOptional =
                workflowQueryService.getProblemDetails(nfvoJob.getProcessInstanceId());

        final InlineResponse400 problemDetails = problemDetailsOptional.get();
        assertNotNull(problemDetails);
        assertNotNull(problemDetails.getDetail());
        assertTrue(problemDetails.getDetail().startsWith("Ns Instance already exists in database"));

        final HistoricVariableInstance doesNsInstanceExistsVar =
                getVariable(nfvoJob.getProcessInstanceId(), "doesNsInstanceExists");
        assertNotNull(doesNsInstanceExistsVar);
        assertTrue((boolean) doesNsInstanceExistsVar.getValue());

    }

    @Test
    public void testCreateNsWorkflow_FailToCreateResouceInAai() throws InterruptedException {
        final String nsdId = UUID.randomUUID().toString();
        final String nsdName = NS_NAME + "-" + System.currentTimeMillis();
        final CreateNsRequest createNsRequest = getCreateNsRequest(nsdId, nsdName);

        mockEtsiCatalogEndpoints(nsdId);

        final String modelEndpoint = getAiaServiceInstancelEndPoint(createNsRequest);
        wireMockServer.stubFor(put(urlMatching(modelEndpoint)).willReturn(WireMock.serverError()));
        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(WireMock.serverError()));

        try {
            objUnderTest.runCreateNsJob(createNsRequest, GLOBAL_CUSTOMER_ID, SERVICE_TYPE);
            fail("runCreateNsJob should throw exception");
        } catch (final Exception exception) {
            assertEquals(NsRequestProcessingException.class, exception.getClass());
        }
        final Optional<NfvoJob> optional = getJobByResourceId(createNsRequest.getNsdId());
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();
        assertEquals(JobStatusEnum.ERROR, nfvoJob.getStatus());

        mockRestServiceServer.verify();
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());
        assertTrue(databaseServiceProvider.isNsInstExists(createNsRequest.getNsName()));

        final Optional<InlineResponse400> problemDetailsOptional =
                workflowQueryService.getProblemDetails(nfvoJob.getProcessInstanceId());

        final InlineResponse400 problemDetails = problemDetailsOptional.get();
        assertNotNull(problemDetails);
        assertEquals("Unable to Create Service Instance in AAI", problemDetails.getDetail());

    }

    private void mockAAIEndpoints(final CreateNsRequest createNsRequest) {
        final String modelEndpoint = getAiaServiceInstancelEndPoint(createNsRequest);

        wireMockServer.stubFor(put(urlMatching(modelEndpoint)).willReturn(ok()));
        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(notFound()));
    }

    private String getAiaServiceInstancelEndPoint(final CreateNsRequest createNsRequest) {
        return "/aai/v[0-9]+/business/customers/customer/" + GLOBAL_CUSTOMER_ID
                + "/service-subscriptions/service-subscription/" + SERVICE_TYPE
                + "/service-instances/service-instance/.*";
    }

    private void mockEtsiCatalogEndpoints(final String nsdId) {
        mockRestServiceServer.expect(requestTo(ETSI_CATALOG_URL + "/nsd/v1/ns_descriptors/" + nsdId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(getNSPackageModel(NSD_ID)), MediaType.APPLICATION_JSON));
    }

    private void mockEtsiCatalogEndpoints() {
        mockEtsiCatalogEndpoints(NSD_ID);;
    }

    private NsdInfo getNSPackageModel(final String nsdId) {
        return new NsdInfo().id(nsdId).nsdId(nsdId).nsdInvariantId(NSD_INVARIANT_ID).nsdName("vcpe").nsdDesigner("ONAP")
                .vnfPkgIds(Arrays.asList(GLOBAL_CUSTOMER_ID));
    }

    private CreateNsRequest getCreateNsRequest() {
        return getCreateNsRequest(NSD_ID, NS_NAME);
    }

    private CreateNsRequest getCreateNsRequest(final String nsdId, final String nsName) {
        return new CreateNsRequest().nsdId(nsdId).nsName(nsName);
    }

    private Map<String, Object> getVariables(final String jobId, final CreateNsRequest createNsRequest) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaVariableNameConstants.JOB_ID_PARAM_NAME, jobId);
        variables.put(CamundaVariableNameConstants.CREATE_NS_REQUEST_PARAM_NAME, createNsRequest);
        variables.put(CamundaVariableNameConstants.GLOBAL_CUSTOMER_ID_PARAM_NAME, GLOBAL_CUSTOMER_ID);
        variables.put(CamundaVariableNameConstants.SERVICE_TYPE_PARAM_NAME, SERVICE_TYPE);

        return variables;
    }

}
