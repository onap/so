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

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
public class DeleteNsTaskTest extends BaseTest {

    @Autowired
    private JobExecutorService objUnderTest;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        wireMockServer.resetAll();
    }

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    @Test
    public void testRunDeleteNsJob_nsInstanceIdNotInDatabase_throwsException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        assertTrue(databaseServiceProvider.getNfvoNsInst(nsInstanceId).isEmpty());

        expectedException.expect(NsRequestProcessingException.class);
        objUnderTest.runDeleteNsJob(nsInstanceId);
    }

    @Test
    public void testRunDeleteNsJob_nsInstanceStateInstantiated_throwsException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId, State.INSTANTIATED);

        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        assertEquals(State.INSTANTIATED, optionalNfvoNsInst.get().getStatus());

        expectedException.expect(NsRequestProcessingException.class);
        objUnderTest.runDeleteNsJob(nsInstanceId);
    }

    @Test
    public void testRunDeleteNsJob_nsInstanceStateInstantiating_throwsException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId, State.INSTANTIATING);

        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        assertEquals(State.INSTANTIATING, optionalNfvoNsInst.get().getStatus());

        expectedException.expect(NsRequestProcessingException.class);
        objUnderTest.runDeleteNsJob(nsInstanceId);
    }

    @Test
    public void testRunDeleteNsJob_nsInstanceStateTerminating_throwsException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId, State.TERMINATING);

        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        assertEquals(State.TERMINATING, optionalNfvoNsInst.get().getStatus());

        expectedException.expect(NsRequestProcessingException.class);
        objUnderTest.runDeleteNsJob(nsInstanceId);
    }

    @Test
    public void testRunDeleteNsJob_nsInstanceStateFailed_throwsException() {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId, State.FAILED);

        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        assertEquals(State.FAILED, optionalNfvoNsInst.get().getStatus());

        expectedException.expect(NsRequestProcessingException.class);
        objUnderTest.runDeleteNsJob(nsInstanceId);
    }

    @Test
    public void testRunDeleteNsJob_SuccessfulCase() throws InterruptedException {
        final String nsInstanceId = UUID.randomUUID().toString();
        addDummyNsToDatabase(nsInstanceId, State.NOT_INSTANTIATED);
        mockAaiEndpoints();

        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInst.isPresent());
        assertEquals(State.NOT_INSTANTIATED, optionalNfvoNsInst.get().getStatus());

        objUnderTest.runDeleteNsJob(nsInstanceId);

        final Optional<NfvoJob> optional = getJobByResourceId(nsInstanceId);
        assertTrue(optional.isPresent());
        final NfvoJob nfvoJob = optional.get();

        // Confirm Process finishes in STATE_COMPLETED
        assertTrue(waitForProcessInstanceToFinish(nfvoJob.getProcessInstanceId()));
        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(nfvoJob.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        // Confirm NS Instance no longer in database
        final Optional<NfvoNsInst> optionalNfvoNsInstance = databaseServiceProvider.getNfvoNsInst(nsInstanceId);
        assertTrue(optionalNfvoNsInstance.isEmpty());
    }

    private void addDummyNsToDatabase(final String nsInstanceId, final State state) {
        final String nsPackageId = UUID.randomUUID().toString();
        final NfvoNsInst nfvoNsInst = new NfvoNsInst().nsInstId(nsInstanceId).name("nsName").nsPackageId(nsPackageId)
                .nsdId("nsdId").nsdInvariantId("nsdId").status(state).statusUpdatedTime(LocalDateTime.now())
                .globalCustomerId(GLOBAL_CUSTOMER_ID).serviceType(SERVICE_TYPE);
        databaseServiceProvider.saveNfvoNsInst(nfvoNsInst);
    }

    private void mockAaiEndpoints() {
        final String modelEndpoint = getAaiServiceInstanceEndPoint();
        final String resourceVersion = "12345";

        final String body =
                "{\"resource-version\": \"" + resourceVersion + "\",\n\"orchestration-status\": \"Assigned\"}";
        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok()).willReturn(okJson(body)));

        wireMockServer.stubFor(
                delete(urlMatching(modelEndpoint + "\\?resource-version=" + resourceVersion)).willReturn(ok()));
    }
}
