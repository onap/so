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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;

import static org.slf4j.LoggerFactory.getLogger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobAction;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class BaseTest {
    protected static final String ETSI_CATALOG_URL = "http://modeling-etsicatalog.onap:8806/api";
    protected static final String SOL003_ADAPTER_ENDPOINT_URL = "https://so-vnfm-adapter.onap:9092/so/vnfm-adapter/v1";
    protected static final String GLOBAL_CUSTOMER_ID = UUID.randomUUID().toString();
    protected static final String NSD_INVARIANT_ID = UUID.randomUUID().toString();
    protected static final String SERVICE_TYPE = "NetworkService";
    protected static final Logger logger = getLogger(BaseTest.class);

    private static final long TIME_OUT_IN_SECONDS = 60;
    private static final int SLEEP_TIME_IN_SECONDS = 5;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    protected DatabaseServiceProvider databaseServiceProvider;

    @Autowired
    protected WireMockServer wireMockServer;

    public NfvoJob createNewNfvoJob(final String jobAction, final String nsdId, final String nsName) {
        final NfvoJob newJob = new NfvoJob().startTime(LocalDateTime.now()).jobType("NS").jobAction(JobAction.CREATE)
                .status(JobStatusEnum.STARTING).resourceId(nsdId).resourceName(nsName);
        databaseServiceProvider.addJob(newJob);
        return newJob;
    }

    public Optional<NfvoJob> getNfvoJob(final String jobId) {
        return databaseServiceProvider.getJob(jobId);
    }

    public Optional<NfvoJob> getJobByResourceId(final String resourceId) {
        return databaseServiceProvider.getJobByResourceId(resourceId);
    }

    public ProcessInstance executeWorkflow(final String processDefinitionKey, final String businessKey,
            final Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    public HistoricProcessInstance getHistoricProcessInstance(final String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    public HistoricVariableInstance getVariable(final String processInstanceId, final String name) {
        return historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                .variableName(name).singleResult();
    }

    public List<HistoricVariableInstance> getVariables(final String processInstanceId) {
        return historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
    }

    public boolean waitForProcessInstanceToFinish(final String processInstanceId) throws InterruptedException {
        final long startTimeInMillis = System.currentTimeMillis();
        final long timeOutTime = startTimeInMillis + TimeUnit.SECONDS.toMillis(TIME_OUT_IN_SECONDS);
        while (timeOutTime > System.currentTimeMillis()) {

            if (isProcessEndedByProcessInstanceId(processInstanceId)) {
                logger.info("processInstanceId: {} is finished", processInstanceId);
                return true;
            }
            logger.info("processInstanceId: {} is still running", processInstanceId);
            logger.info("Process instance {} not finished yet, will try again in {} seconds", processInstanceId,
                    SLEEP_TIME_IN_SECONDS);
            TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);
        }
        logger.warn("Timeout {} process didn't finished ", processInstanceId);
        return false;
    }


    public boolean isProcessEndedByProcessInstanceId(final String processInstanceId) {
        final HistoricProcessInstance processInstance = getHistoricProcessInstance(processInstanceId);
        return processInstance != null
                && !HistoricProcessInstance.STATE_ACTIVE.equalsIgnoreCase(processInstance.getState());
    }

}
