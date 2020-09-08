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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_NS_REQUEST_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.GLOBAL_CUSTOMER_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.INSTANTIATE_NS_REQUEST_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.JOB_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.OCC_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.SERVICE_TYPE_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.Constants.CREATE_NS_WORKFLOW_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.Constants.INSTANTIATE_NS_WORKFLOW_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobAction.INSTANTIATE;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.ERROR;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED_WITH_ERROR;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.STARTING;
import static org.slf4j.LoggerFactory.getLogger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobAction;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpType;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.OperationStateEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class JobExecutorService {

    private static final Logger logger = getLogger(JobExecutorService.class);

    private static final ImmutableSet<JobStatusEnum> JOB_FINISHED_STATES =
            ImmutableSet.of(FINISHED, ERROR, FINISHED_WITH_ERROR);

    private static final int SLEEP_TIME_IN_SECONDS = 5;

    @Value("${so-etsi-ns-lcm-workflow-engine.requesttimeout.timeoutInSeconds:300}")
    private int timeOutInSeconds;

    private final DatabaseServiceProvider databaseServiceProvider;
    private final WorkflowExecutorService workflowExecutorService;
    private final WorkflowQueryService workflowQueryService;
    private Gson gson;

    @Autowired
    public JobExecutorService(final DatabaseServiceProvider databaseServiceProvider,
            final WorkflowExecutorService workflowExecutorService, final WorkflowQueryService workflowQueryService,
            final GsonProvider gsonProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
        this.workflowExecutorService = workflowExecutorService;
        this.workflowQueryService = workflowQueryService;
        gson = gsonProvider.getGson();
    }

    public NsInstancesNsInstance runCreateNsJob(final CreateNsRequest createNsRequest, final String globalCustomerId,
            final String serviceType) {
        logger.info("Starting 'Create NS' workflow job for request:\n{}", createNsRequest);
        final NfvoJob newJob = new NfvoJob().startTime(LocalDateTime.now()).jobType("NS").jobAction(JobAction.CREATE)
                .resourceId(createNsRequest.getNsdId()).resourceName(createNsRequest.getNsName())
                .status(JobStatusEnum.STARTING).progress(5);
        databaseServiceProvider.addJob(newJob);

        logger.info("New job created in database :\n{}", newJob);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), CREATE_NS_WORKFLOW_NAME,
                getVariables(newJob.getJobId(), createNsRequest, globalCustomerId, serviceType));

        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), JOB_FINISHED_STATES);

        if (immutablePair.getRight() == null) {
            final String message = "Failed to create NS for request: \n" + createNsRequest;
            logger.error(message);
            throw new NsRequestProcessingException(message);
        }
        final JobStatusEnum finalJobStatus = immutablePair.getRight();
        final String processInstanceId = immutablePair.getLeft();

        if (!FINISHED.equals(finalJobStatus)) {

            final Optional<InlineResponse400> optional = workflowQueryService.getProblemDetails(processInstanceId);
            if (optional.isPresent()) {
                final InlineResponse400 problemDetails = optional.get();
                final String message =
                        "Failed to create NS for request: \n" + createNsRequest + " due to \n" + problemDetails;
                logger.error(message);
                throw new NsRequestProcessingException(message, problemDetails);
            }

            final String message = "Received unexpected Job Status: " + finalJobStatus
                    + " Failed to Create NS for request: \n" + createNsRequest;
            logger.error(message);
            throw new NsRequestProcessingException(message);
        }

        logger.debug("Will query for CreateNsResponse using processInstanceId:{}", processInstanceId);
        final Optional<NsInstancesNsInstance> optional = workflowQueryService.getCreateNsResponse(processInstanceId);
        if (optional.isEmpty()) {
            final String message =
                    "Unable to find CreateNsReponse in Camunda History for process instance: " + processInstanceId;
            logger.error(message);
            throw new NsRequestProcessingException(message);
        }
        return optional.get();
    }

    public String runInstantiateNsJob(final String nsInstanceId, final InstantiateNsRequest instantiateNsRequest) {

        final NfvoJob newJob = new NfvoJob().startTime(LocalDateTime.now()).jobType("NS").jobAction(INSTANTIATE)
                .resourceId(nsInstanceId).status(STARTING).progress(0);
        databaseServiceProvider.addJob(newJob);
        logger.info("New job created in database :\n{}", newJob);

        final LocalDateTime currentDateTime = LocalDateTime.now();
        final NsLcmOpOcc newNsLcmOpOcc = new NsLcmOpOcc().id(nsInstanceId).operation(NsLcmOpType.INSTANTIATE)
                .operationState(OperationStateEnum.PROCESSING).stateEnteredTime(currentDateTime)
                .startTime(currentDateTime).isAutoInnovation(false).isCancelPending(false)
                .operationParams(gson.toJson(instantiateNsRequest));
        databaseServiceProvider.addNSLcmOpOcc(newNsLcmOpOcc);
        logger.info("New NSLcmOpOcc created in database :\n{}", newNsLcmOpOcc);

        workflowExecutorService.executeWorkflow(newJob.getJobId(), INSTANTIATE_NS_WORKFLOW_NAME,
                getVariables(nsInstanceId, newJob.getJobId(), newNsLcmOpOcc.getId(), instantiateNsRequest));

        final ImmutableSet<JobStatusEnum> jobFinishedStates =
                ImmutableSet.of(FINISHED, ERROR, FINISHED_WITH_ERROR, IN_PROGRESS);
        final ImmutablePair<String, JobStatusEnum> immutablePair =
                waitForJobToFinish(newJob.getJobId(), jobFinishedStates);

        if (immutablePair.getRight() == null) {
            final String message = "Failed to Instantiate NS for request: \n" + instantiateNsRequest;
            logger.error(message);
            throw new NsRequestProcessingException(message);
        }

        final JobStatusEnum finalJobStatus = immutablePair.getRight();

        if (IN_PROGRESS.equals(finalJobStatus) || FINISHED.equals(finalJobStatus)) {
            logger.info("Instantiation Job status: {}", finalJobStatus);


            return newNsLcmOpOcc.getId();
        }

        final String message = "Received unexpected Job Status: " + finalJobStatus
                + " Failed to instantiate NS for request: \n" + instantiateNsRequest;
        logger.error(message);
        throw new NsRequestProcessingException(message);
    }

    private ImmutablePair<String, JobStatusEnum> waitForJobToFinish(final String jobId,
            final ImmutableSet<JobStatusEnum> jobFinishedStates) {
        try {
            final long startTimeInMillis = System.currentTimeMillis();
            final long timeOutTime = startTimeInMillis + TimeUnit.SECONDS.toMillis(timeOutInSeconds);

            logger.info("Will wait till {} for {} job to finish", Instant.ofEpochMilli(timeOutTime).toString(), jobId);
            JobStatusEnum currentJobStatus = null;
            while (timeOutTime > System.currentTimeMillis()) {

                final Optional<NfvoJob> optional = databaseServiceProvider.getJob(jobId);

                if (optional.isEmpty()) {
                    logger.error("Unable to find Job using jobId: {}", jobId);
                    return ImmutablePair.nullPair();
                }

                final NfvoJob nfvoJob = optional.get();
                currentJobStatus = nfvoJob.getStatus();
                logger.debug("Received job status response: \n ", nfvoJob);
                if (jobFinishedStates.contains(nfvoJob.getStatus())) {
                    logger.info("Job finished \n {}", currentJobStatus);
                    return ImmutablePair.of(nfvoJob.getProcessInstanceId(), currentJobStatus);
                }

                logger.debug("Haven't received one of finish state {} yet, will try again in {} seconds",
                        jobFinishedStates, SLEEP_TIME_IN_SECONDS);
                TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);

            }
            logger.warn("Timeout current job status: {}", currentJobStatus);
            return ImmutablePair.nullPair();
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            logger.error("Sleep was interrupted", interruptedException);
            return ImmutablePair.nullPair();
        }
    }

    private Map<String, Object> getVariables(final String jobId, final CreateNsRequest createNsRequest,
            final String globalCustomerId, final String serviceType) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(JOB_ID_PARAM_NAME, jobId);
        variables.put(CREATE_NS_REQUEST_PARAM_NAME, createNsRequest);
        variables.put(GLOBAL_CUSTOMER_ID_PARAM_NAME, globalCustomerId);
        variables.put(SERVICE_TYPE_PARAM_NAME, serviceType);
        return variables;
    }

    private Map<String, Object> getVariables(final String nsInstanceId, final String jobId, final String occId,
            final InstantiateNsRequest instantiateNsRequest) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(NS_INSTANCE_ID_PARAM_NAME, nsInstanceId);
        variables.put(JOB_ID_PARAM_NAME, jobId);
        variables.put(OCC_ID_PARAM_NAME, occId);
        variables.put(INSTANTIATE_NS_REQUEST_PARAM_NAME, instantiateNsRequest);
        return variables;
    }

}
