/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.cloudify.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.PluginAction;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduPlugin;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloudify.base.client.CloudifyBaseException;
import org.onap.so.cloudify.base.client.CloudifyClientTokenProvider;
import org.onap.so.cloudify.base.client.CloudifyConnectException;
import org.onap.so.cloudify.base.client.CloudifyRequest;
import org.onap.so.cloudify.base.client.CloudifyResponseException;
import org.onap.so.cloudify.beans.DeploymentInfo;
import org.onap.so.cloudify.beans.DeploymentInfoBuilder;
import org.onap.so.cloudify.beans.DeploymentStatus;
import org.onap.so.cloudify.exceptions.MsoCloudifyException;
import org.onap.so.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.onap.so.cloudify.exceptions.MsoDeploymentAlreadyExists;
import org.onap.so.cloudify.v3.client.BlueprintsResource.GetBlueprint;
import org.onap.so.cloudify.v3.client.BlueprintsResource.UploadBlueprint;
import org.onap.so.cloudify.v3.client.Cloudify;
import org.onap.so.cloudify.v3.client.DeploymentsResource.CreateDeployment;
import org.onap.so.cloudify.v3.client.DeploymentsResource.DeleteDeployment;
import org.onap.so.cloudify.v3.client.DeploymentsResource.GetDeployment;
import org.onap.so.cloudify.v3.client.DeploymentsResource.GetDeploymentOutputs;
import org.onap.so.cloudify.v3.client.ExecutionsResource.CancelExecution;
import org.onap.so.cloudify.v3.client.ExecutionsResource.GetExecution;
import org.onap.so.cloudify.v3.client.ExecutionsResource.ListExecutions;
import org.onap.so.cloudify.v3.client.ExecutionsResource.StartExecution;
import org.onap.so.cloudify.v3.model.AzureConfig;
import org.onap.so.cloudify.v3.model.Blueprint;
import org.onap.so.cloudify.v3.model.CancelExecutionParams;
import org.onap.so.cloudify.v3.model.CloudifyError;
import org.onap.so.cloudify.v3.model.CreateDeploymentParams;
import org.onap.so.cloudify.v3.model.Deployment;
import org.onap.so.cloudify.v3.model.DeploymentOutputs;
import org.onap.so.cloudify.v3.model.Execution;
import org.onap.so.cloudify.v3.model.Executions;
import org.onap.so.cloudify.v3.model.OpenstackConfig;
import org.onap.so.cloudify.v3.model.StartExecutionParams;
import org.onap.so.config.beans.PoConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.utils.MsoCommonUtils;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MsoCloudifyUtils extends MsoCommonUtils implements VduPlugin {

    private static final String CLOUDIFY = "Cloudify";
    private static final String CREATE_DEPLOYMENT = "CreateDeployment";
    private static final String DELETE_DEPLOYMENT = "DeleteDeployment";
    private static final String TERMINATED = "terminated";
    private static final String CANCELLED = "cancelled";
    private static final String UNINSTALL = "uninstall";
    private static final String UPLOAD_BLUEPRINT = "UPLOAD_BLUEPRINT";

    // Fetch cloud configuration each time (may be cached in CloudConfig class)
    @Autowired
    protected CloudConfig cloudConfig;

    @Autowired
    private Environment environment;

    @Autowired
    private PoConfig poConfig;

    private static final Logger logger = LoggerFactory.getLogger(MsoCloudifyUtils.class);

    // Properties names and variables (with default values)
    protected String createPollIntervalProp = "org.onap.so.adapters.po.pollInterval";
    private String deletePollIntervalProp = "org.onap.so.adapters.po.pollInterval";

    protected String createPollIntervalDefault = "15";
    private String deletePollIntervalDefault = "15";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * Create a new Deployment from a specified blueprint, and install it in the specified cloud location and tenant.
     * The blueprint identifier and parameter map are passed in as arguments, along with the cloud access credentials.
     * The blueprint should have been previously uploaded to Cloudify.
     *
     * It is expected that parameters have been validated and contain at minimum the required parameters for the given
     * template with no extra (undefined) parameters..
     *
     * The deployment ID supplied by the caller must be unique in the scope of the Cloudify tenant (not the Openstack
     * tenant). However, it should also be globally unique, as it will be the identifier for the resource going forward
     * in Inventory. This latter is managed by the higher levels invoking this function.
     *
     * This function executes the "install" workflow on the newly created workflow. Cloudify will be polled for
     * completion unless the client requests otherwise.
     *
     * An error will be thrown if the requested Deployment already exists in the specified Cloudify instance.
     *
     * @param cloudSiteId The cloud (may be a region) in which to create the stack.
     * @param tenantId The Openstack ID of the tenant in which to create the Stack
     * @param deploymentId The identifier (name) of the deployment to create
     * @param blueprintId The blueprint from which to create the deployment.
     * @param inputs A map of key/value inputs
     * @param pollForCompletion Indicator that polling should be handled in Java vs. in the client
     * @param timeoutMinutes Timeout after which the "install" will be cancelled
     * @param backout Flag to delete deployment on install Failure - defaulted to True
     * @return A DeploymentInfo object
     * @throws MsoCloudifyException Thrown if the Cloudify API call returns an exception.
     * @throws MsoIOException Thrown on Cloudify connection errors.
     */

    public DeploymentInfo createAndInstallDeployment(String cloudSiteId, String tenantId, String deploymentId,
            String blueprintId, Map<String, ? extends Object> inputs, boolean pollForCompletion, int timeoutMinutes,
            boolean backout) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        Cloudify cloudify = getCloudifyClient(cloudSite.get());

        logger.debug("Ready to Create Deployment ({}) with input params: {}", deploymentId, inputs);

        // Build up the inputs, including:
        // - from provided "environment" file
        // - passed in by caller
        // - special input for cloud-specific Credentials
        Map<String, Object> expandedInputs = new HashMap<>(inputs);

        String platform = cloudSite.get().getPlatform();
        if (platform == null || platform.isEmpty() || ("OPENSTACK").equalsIgnoreCase(platform)) {
            // Create the Cloudify OpenstackConfig with the credentials
            OpenstackConfig openstackConfig = getOpenstackConfig(cloudSite.get(), tenantId);
            expandedInputs.put("openstack_config", openstackConfig);
        } else if (("AZURE").equalsIgnoreCase(platform)) {
            // Create Cloudify AzureConfig with the credentials
            AzureConfig azureConfig = getAzureConfig(cloudSite.get(), tenantId);
            expandedInputs.put("azure_config", azureConfig);
        }

        // Build up the parameters to create a new deployment
        CreateDeploymentParams deploymentParams = new CreateDeploymentParams();
        deploymentParams.setBlueprintId(blueprintId);
        deploymentParams.setInputs(expandedInputs);

        Deployment deployment = null;
        try {
            CreateDeployment createDeploymentRequest = cloudify.deployments().create(deploymentId, deploymentParams);
            logger.debug(createDeploymentRequest.toString());

            deployment = executeAndRecordCloudifyRequest(createDeploymentRequest);
        } catch (CloudifyResponseException e) {
            // Since this came on the 'Create Deployment' command, nothing was changed
            // in the cloud. Return the error as an exception.
            if (e.getStatus() == 409) {
                // Deployment already exists. Return a specific error for this case
                MsoException me = new MsoDeploymentAlreadyExists(deploymentId, cloudSiteId);
                me.addContext(CREATE_DEPLOYMENT);
                throw me;
            } else {
                // Convert the CloudifyResponseException to an MsoException
                logger.debug("ERROR STATUS = {},\n{}\n{}", e.getStatus(), e.getMessage(), e.getLocalizedMessage());
                MsoException me = cloudifyExceptionToMsoException(e, CREATE_DEPLOYMENT);
                me.setCategory(MsoExceptionCategory.OPENSTACK);
                throw me;
            }
        } catch (CloudifyConnectException e) {
            // Error connecting to Cloudify instance. Convert to an MsoException
            throw cloudifyExceptionToMsoException(e, CREATE_DEPLOYMENT);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException(e, CREATE_DEPLOYMENT);
        }

        /*
         * It can take some time for Cloudify to be ready to execute a workflow on the deployment. Sleep 30 seconds
         * based on observation of behavior in a Cloudify VM instance (delay due to "create_deployment_environment").
         */
        sleep(30000);

        /*
         * Next execute the "install" workflow. Note - this assumes there are no additional parameters required for the
         * workflow.
         */
        int createPollInterval =
                Integer.parseInt(this.environment.getProperty(createPollIntervalProp, createPollIntervalDefault));
        int pollTimeout = (timeoutMinutes * 60) + createPollInterval;

        Execution installWorkflow = null;

        try {
            installWorkflow = executeWorkflow(cloudify, deploymentId, "install", null, pollForCompletion, pollTimeout,
                    createPollInterval);

            if (installWorkflow.getStatus().equals(TERMINATED)) {
                // Success!
                // Create and return a DeploymentInfo structure. Include the Runtime outputs
                return new DeploymentInfoBuilder().withId(deployment.getId())
                        .withDeploymentInputs(deployment.getInputs())
                        .withDeploymentOutputs(getDeploymentOutputs(cloudify, deploymentId).get())
                        .fromExecution(installWorkflow).build();
            } else {
                // The workflow completed with errors. Must try to back it out.
                if (!backout) {
                    logger.warn("{} Deployment installation failed, backout deletion suppressed {} {}",
                            MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue(),
                            "Exception in Deployment Installation, backout suppressed");
                } else {
                    // Poll on delete if we rollback - use same values for now
                    int deletePollInterval = createPollInterval;
                    int deletePollTimeout = pollTimeout;

                    try {
                        // Run the uninstall to undo the install
                        Execution uninstallWorkflow = executeWorkflow(cloudify, deploymentId, UNINSTALL, null,
                                pollForCompletion, deletePollTimeout, deletePollInterval);

                        if (uninstallWorkflow.getStatus().equals(TERMINATED)) {
                            // The uninstall completed. Delete the deployment itself
                            DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
                            executeAndRecordCloudifyRequest(deleteRequest);
                        } else {
                            // Didn't uninstall successfully. Log this error
                            logger.error("{} Create Deployment: Cloudify error rolling back deployment install: {} {}",
                                    MessageEnum.RA_CREATE_STACK_ERR, installWorkflow.getError(),
                                    ErrorCode.BusinessProcessError.getValue());
                        }
                    } catch (Exception e) {
                        // Catch-all for backout errors trying to uninstall/delete
                        // Log this error, and return the original exception
                        logger.error("{} Create Stack: Nested exception rolling back deployment install: {}",
                                MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue(), e);
                    }
                }

                MsoCloudifyException me =
                        new MsoCloudifyException(0, "Workflow Execution Failed", installWorkflow.getError());
                me.addContext(CREATE_DEPLOYMENT);

                throw me;
            }
        } catch (MsoException me) {
            // Install failed. Unless requested otherwise, back out the deployment

            if (!backout) {
                logger.warn("{} Deployment installation failed, backout deletion suppressed {}",
                        MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue());
            } else {
                // Poll on delete if we rollback - use same values for now
                int deletePollInterval = createPollInterval;
                int deletePollTimeout = pollTimeout;

                try {
                    // Run the uninstall to undo the install.
                    // Always try to run it, as it should be idempotent
                    executeWorkflow(cloudify, deploymentId, UNINSTALL, null, pollForCompletion, deletePollTimeout,
                            deletePollInterval);

                    // Delete the deployment itself
                    DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
                    executeAndRecordCloudifyRequest(deleteRequest);
                } catch (Exception e) {
                    // Catch-all for backout errors trying to uninstall/delete
                    // Log this error, and return the original exception
                    logger.error("{} Create Stack: Nested exception rolling back deployment install: {} ",
                            MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue(), e);
                }
            }

            // Propagate the original exception from Stack Query.
            me.addContext(CREATE_DEPLOYMENT);

            throw me;
        }
    }


    /*
     * Get the runtime Outputs of a deployment. Return the Map of tag/value outputs.
     */
    private Optional<Map<String, Object>> getDeploymentOutputs(Cloudify cloudify, String deploymentId)
            throws MsoException {
        // Build and send the Cloudify request
        DeploymentOutputs deploymentOutputs;
        try {
            GetDeploymentOutputs queryDeploymentOutputs = cloudify.deployments().outputsById(deploymentId);
            logger.debug(queryDeploymentOutputs.toString());

            deploymentOutputs = executeAndRecordCloudifyRequest(queryDeploymentOutputs);
            if (deploymentOutputs != null) {
                return Optional.ofNullable(deploymentOutputs.getOutputs());
            } else {
                return Optional.empty();
            }
        } catch (CloudifyConnectException ce) {
            // Couldn't connect to Cloudify
            logger.error("{} QueryDeploymentOutputs: Cloudify connection failure: {} ", MessageEnum.RA_CREATE_STACK_ERR,
                    ErrorCode.BusinessProcessError.getValue(), ce);
            throw new MsoIOException(ce.getMessage(), ce);
        } catch (CloudifyResponseException re) {
            if (re.getStatus() == 404) {
                // No Outputs
                return Optional.empty();
            }
            throw new MsoCloudifyException(re.getStatus(), re.getMessage(), re.getLocalizedMessage(), re);
        } catch (Exception e) {
            // Catch-all
            throw new MsoAdapterException(e.getMessage(), e);
        }
    }

    /*
     * Execute a workflow on a deployment. Handle polling for completion with timeout. Return the final Execution object
     * with status. Throw an exception on Errors. Question - how does the client know whether rollback needs to be done?
     */
    private Execution executeWorkflow(Cloudify cloudify, String deploymentId, String workflowId,
            Map<String, Object> workflowParams, boolean pollForCompletion, int timeout, int pollInterval)
            throws MsoCloudifyException {
        logger.debug("Executing '{}' workflow on deployment '{}'", workflowId, deploymentId);

        StartExecutionParams executeParams = new StartExecutionParams();
        executeParams.setWorkflowId(workflowId);
        executeParams.setDeploymentId(deploymentId);
        executeParams.setParameters(workflowParams);

        Execution execution = null;
        String executionId = null;
        String command = "start";
        Exception savedException = null;

        try {
            StartExecution executionRequest = cloudify.executions().start(executeParams);
            logger.debug(executionRequest.toString());
            execution = executeAndRecordCloudifyRequest(executionRequest);
            executionId = execution.getId();

            if (!pollForCompletion) {
                // Client did not request polling, so just return the Execution object
                return execution;
            }

            // Enter polling loop
            boolean timedOut = false;
            int pollTimeout = timeout;

            String status = execution.getStatus();

            // Create a reusable cloudify query request
            GetExecution queryExecution = cloudify.executions().byId(executionId);
            command = "query";

            while (!timedOut && !(status.equals(TERMINATED) || ("failed").equals(status) || status.equals(CANCELLED))) {
                // workflow is still running; check for timeout
                if (pollTimeout <= 0) {
                    logger.debug("workflow {} timed out on deployment {}", execution.getWorkflowId(),
                            execution.getDeploymentId());
                    timedOut = true;
                    continue;
                }

                sleep(pollInterval * 1000L);

                pollTimeout -= pollInterval;
                logger.debug("pollTimeout remaining: " + pollTimeout);

                execution = queryExecution.execute();
                if (execution != null) {
                    status = execution.getStatus();
                } else {
                    status = TERMINATED;
                }
            }

            // Broke the loop. Check again for a terminal state
            if (status.equals(TERMINATED)) {
                // Success!
                logger.debug("Workflow '{}' completed successfully on deployment '{}'", workflowId, deploymentId);
                return execution;
            } else if (("failed").equals(status)) {
                // Workflow failed. Log it and return the execution object (don't throw exception here)
                logger.error("{} Cloudify workflow failure: {} {} Execute Workflow: Failed: {}",
                        MessageEnum.RA_CREATE_STACK_ERR, execution.getError(),
                        ErrorCode.BusinessProcessError.getValue(), execution.getError());
                return execution;
            } else if (status.equals(CANCELLED)) {
                // Workflow was cancelled, leaving the deployment in an indeterminate state. Log it and return the
                // execution object (don't throw exception here)
                logger.error("{} Cloudify workflow cancelled. Deployment is in an indeterminate state {} {} {}",
                        MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue(),
                        "Execute Workflow cancelled: ", workflowId);
                return execution;
            } else {
                // Can only get here after a timeout
                logger.error("{} Cloudify workflow timeout {} Execute Workflow: Timed Out",
                        MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue());
            }
        } catch (CloudifyConnectException ce) {
            logger.error("{} {} Execute Workflow ({} {}): Cloudify connection failure {} ",
                    MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcessError.getValue(), command, ce);
            savedException = ce;
        } catch (CloudifyResponseException re) {
            logger.error("{} {} Execute Workflow ({}): Cloudify response error {} ", MessageEnum.RA_CREATE_STACK_ERR,
                    ErrorCode.BusinessProcessError.getValue(), command, re.getMessage(), re);
            savedException = re;
        } catch (RuntimeException e) {
            // Catch-all
            logger.error("{} {} Execute Workflow ({}): Internal error {}", MessageEnum.RA_CREATE_STACK_ERR,
                    ErrorCode.BusinessProcessError.getValue(), command, e.getMessage(), e);
            savedException = e;
        }

        // Get to this point ONLY on an error or timeout
        // The cloudify execution is still running (we've not received a terminal status),
        // so try to Cancel it.
        CancelExecutionParams cancelParams = new CancelExecutionParams();
        cancelParams.setAction("cancel");
        // TODO: Use force_cancel?

        Execution cancelExecution = null;

        try {
            CancelExecution cancelRequest = cloudify.executions().cancel(executionId, cancelParams);
            logger.debug(cancelRequest.toString());
            cancelExecution = cancelRequest.execute();

            // Enter polling loop
            boolean timedOut = false;
            int cancelTimeout = timeout; // TODO: For now, just use same timeout

            String status = null;
            if (cancelExecution != null) {
                status = cancelExecution.getStatus();
            }
            // Poll for completion. Create a reusable cloudify query request
            GetExecution queryExecution = cloudify.executions().byId(executionId);

            while (!timedOut && !CANCELLED.equals(status)) {
                // workflow is still running; check for timeout
                if (cancelTimeout <= 0) {
                    logger.debug("Cancel timeout for workflow {} on deployment {}", workflowId, deploymentId);
                    timedOut = true;
                    continue;
                }

                sleep(pollInterval * 1000L);

                cancelTimeout -= pollInterval;
                logger.debug("pollTimeout remaining: {}", cancelTimeout);

                execution = queryExecution.execute();
                if (execution != null) {
                    status = execution.getStatus();
                }
            }

            // Broke the loop. Check again for a terminal state
            if (CANCELLED.equals(status)) {
                // Finished cancelling. Return the original exception
                logger.debug("Cancel workflow {} completed on deployment {}", workflowId, deploymentId);
                throw new MsoCloudifyException(-1, "", "", savedException);
            } else {
                // Can only get here after a timeout
                logger.debug("Cancel workflow {} timeout out on deployment {}", workflowId, deploymentId);
                MsoCloudifyException exception = new MsoCloudifyException(-1, "", "", savedException);
                exception.setPendingWorkflow(true);
                throw exception;
            }
        } catch (Exception e) {
            // Catch-all. Log the message and throw the original exception
            logger.debug("Cancel workflow {} failed for deployment {} :", workflowId, deploymentId, e);
            MsoCloudifyException exception = new MsoCloudifyException(-1, "", "", savedException);
            exception.setPendingWorkflow(true);
            throw exception;
        }
    }



    /**
     * Query for a Cloudify Deployment (by Name). This call will always return a DeploymentInfo object. If the
     * deployment does not exist, an "empty" DeploymentInfo will be returned - containing only the deployment ID and a
     * special status of NOTFOUND.
     *
     * @param tenantId The Openstack ID of the tenant in which to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     */
    public DeploymentInfo queryDeployment(String cloudSiteId, String tenantId, String deploymentId)
            throws MsoException {
        logger.debug("Query Cloudify Deployment: {} in tenant {}", deploymentId, tenantId);

        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        Cloudify cloudify = getCloudifyClient(cloudSite.get());

        // Build and send the Cloudify request
        Deployment deployment = new Deployment();
        try {
            GetDeployment queryDeployment = cloudify.deployments().byId(deploymentId);
            logger.debug(queryDeployment.toString());
            deployment = executeAndRecordCloudifyRequest(queryDeployment);

            // Next look for the latest execution
            ListExecutions listExecutions =
                    cloudify.executions().listFiltered("deployment_id=" + deploymentId, "-created_at");
            Executions executions = listExecutions.execute();

            // If no executions, does this give NOT_FOUND or empty set?
            if (executions == null || executions.getItems().isEmpty()) {
                return new DeploymentInfoBuilder().withId(deployment.getId())
                        .withDeploymentInputs(deployment.getInputs()).build();
            } else {
                return new DeploymentInfoBuilder().withId(deployment.getId())
                        .withDeploymentInputs(deployment.getInputs())
                        .withDeploymentOutputs(getDeploymentOutputs(cloudify, deploymentId).get())
                        .fromExecution(executions.getItems().get(0)).build();
            }
        } catch (CloudifyConnectException ce) {
            // Couldn't connect to Cloudify
            logger.error("{} QueryDeployment: Cloudify connection failure: {} ", MessageEnum.RA_CREATE_STACK_ERR,
                    ErrorCode.BusinessProcessError.getValue(), ce);
            throw new MsoIOException(ce.getMessage(), ce);
        } catch (CloudifyResponseException re) {
            if (re.getStatus() == 404) {
                // Got a NOT FOUND error. React differently based on deployment vs. execution
                if (deployment != null) {
                    // Got NOT_FOUND on the executions. Assume this is a valid "empty" set
                    return new DeploymentInfoBuilder().withId(deployment.getId())
                            .withDeploymentInputs(deployment.getInputs())
                            .withDeploymentOutputs(getDeploymentOutputs(cloudify, deploymentId).get()).build();
                } else {
                    // Deployment not found. Default status of a DeploymentInfo object is NOTFOUND
                    return new DeploymentInfoBuilder().withId(deploymentId).build();
                }
            }
            throw new MsoCloudifyException(re.getStatus(), re.getMessage(), re.getLocalizedMessage(), re);
        } catch (Exception e) {
            // Catch-all
            throw new MsoAdapterException(e.getMessage(), e);
        }
    }


    /**
     * Delete a Cloudify deployment (by ID). If the deployment is not found, it will be considered a successful
     * deletion. The return value is a DeploymentInfo object which contains the last deployment status.
     *
     * There is no rollback from a successful deletion. A deletion failure will also result in an undefined deployment
     * state - the components may or may not have been all or partially deleted, so the resulting deployment must be
     * considered invalid.
     *
     * @param tenantId The Openstack ID of the tenant in which to perform the delete
     * @param cloudSiteId The cloud identifier (may be a region) from which to delete the stack.
     * @return A StackInfo object
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public DeploymentInfo uninstallAndDeleteDeployment(String cloudSiteId, String tenantId, String deploymentId,
            int timeoutMinutes) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        Cloudify cloudify = getCloudifyClient(cloudSite.get());

        logger.debug("Ready to Uninstall/Delete Deployment ({})", deploymentId);

        // Query first to save the trouble if deployment not found
        try {
            GetDeployment queryDeploymentRequest = cloudify.deployments().byId(deploymentId);
            logger.debug(queryDeploymentRequest.toString());

            // deployment = executeAndRecordCloudifyRequest (queryDeploymentRequest);
        } catch (CloudifyResponseException e) {
            // Since this came on the 'Create Deployment' command, nothing was changed
            // in the cloud. Return the error as an exception.
            if (e.getStatus() == 404) {
                // Deployment doesn't exist. Return a "NOTFOUND" DeploymentInfo object
                // TODO: Should return NULL?
                logger.debug("Deployment requested for deletion does not exist: {}", deploymentId);
                return new DeploymentInfoBuilder().withId(deploymentId).withStatus(DeploymentStatus.NOTFOUND).build();
            } else {
                // Convert the CloudifyResponseException to an MsoOpenstackException
                logger.debug("ERROR STATUS = {}, \n {}\n {}\n {}", e.getStatus(), e.getMessage(),
                        e.getLocalizedMessage(), e);
                MsoException me = cloudifyExceptionToMsoException(e, DELETE_DEPLOYMENT);
                me.setCategory(MsoExceptionCategory.INTERNAL);
                throw me;
            }
        } catch (CloudifyConnectException e) {
            // Error connecting to Cloudify instance. Convert to an MsoException
            throw cloudifyExceptionToMsoException(e, DELETE_DEPLOYMENT);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException(e, DELETE_DEPLOYMENT);
        }

        /*
         * Query the outputs before deleting so they can be returned as well
         */
        // DeploymentOutputs outputs = getDeploymentOutputs (cloudify, deploymentId);

        /*
         * Next execute the "uninstall" workflow. Note - this assumes there are no additional parameters required for
         * the workflow.
         */
        // TODO: No deletePollInterval that I'm aware of. Use the create interval
        int deletePollInterval =
                Integer.parseInt(this.environment.getProperty(deletePollIntervalProp, deletePollIntervalDefault));
        int pollTimeout = (timeoutMinutes * 60) + deletePollInterval;

        Execution uninstallWorkflow = null;

        try {
            uninstallWorkflow =
                    executeWorkflow(cloudify, deploymentId, UNINSTALL, null, true, pollTimeout, deletePollInterval);

            if (uninstallWorkflow.getStatus().equals(TERMINATED)) {
                // Successful uninstall.
                logger.debug("Uninstall successful for deployment {}", deploymentId);
            } else {
                // The uninstall workflow completed with an error. Must fail the request, but will
                // leave the deployment in an indeterminate state, as cloud resources may still exist.
                MsoCloudifyException me =
                        new MsoCloudifyException(0, "Uninstall Workflow Failed", uninstallWorkflow.getError());
                me.addContext(DELETE_DEPLOYMENT);

                throw me;
            }
        } catch (MsoException me) {
            // Uninstall workflow has failed.
            // Must fail the deletion... may leave the deployment in an inconclusive state
            me.addContext(DELETE_DEPLOYMENT);

            throw me;
        }

        // At this point, the deployment has been successfully uninstalled.
        // Next step is to delete the deployment itself
        Deployment deployment;
        try {
            DeleteDeployment deleteRequest = cloudify.deployments().deleteByName(deploymentId);
            logger.debug(deleteRequest.toString());

            // The delete request returns the deleted deployment
            deployment = deleteRequest.execute();

        } catch (CloudifyConnectException ce) {
            // Failed to delete. Must fail the request, but will leave the (uninstalled)
            // deployment in Cloudify DB.
            MsoCloudifyException me = new MsoCloudifyException(0, "Deployment Delete Failed", ce.getMessage(), ce);
            me.addContext(DELETE_DEPLOYMENT);

            throw me;
        } catch (CloudifyResponseException re) {
            // Failed to delete. Must fail the request, but will leave the (uninstalled)
            // deployment in the Cloudify DB.
            MsoCloudifyException me = new MsoCloudifyException(re.getStatus(), re.getMessage(), re.getMessage(), re);
            me.addContext(DELETE_DEPLOYMENT);

            throw me;
        } catch (Exception e) {
            // Catch-all
            MsoAdapterException ae = new MsoAdapterException(e.getMessage(), e);
            ae.addContext(DELETE_DEPLOYMENT);

            throw ae;
        }

        // Return the deleted deployment info (with runtime outputs) along with the completed uninstall workflow status
        return new DeploymentInfoBuilder().withId(deployment.getId()).withDeploymentInputs(deployment.getInputs())
                .withDeploymentOutputs(getDeploymentOutputs(cloudify, deploymentId).get())
                .fromExecution(uninstallWorkflow).build();
    }


    /**
     * Check if a blueprint is available for use at a targeted cloud site. This requires checking the Cloudify Manager
     * which is servicing that cloud site to see if the specified blueprint has been loaded.
     *
     * @param cloudSiteId The cloud site where the blueprint is needed
     * @param blueprintId The ID for the blueprint in Cloudify
     */
    public boolean isBlueprintLoaded(String cloudSiteId, String blueprintId) throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        Cloudify cloudify = getCloudifyClient(cloudSite.get());

        GetBlueprint getRequest = cloudify.blueprints().getMetadataById(blueprintId);
        try {
            Blueprint bp = getRequest.execute();
            if (bp != null) {
                logger.debug("Blueprint exists: {}", bp.getId());
                return true;
            } else {
                logger.debug("Null blueprint!");
                return false;
            }
        } catch (CloudifyResponseException ce) {
            if (ce.getStatus() == 404) {
                return false;
            } else {
                throw ce;
            }
        }
    }

    /**
     * Upload a blueprint to the Cloudify Manager that is servicing a Cloud Site. The blueprint currently must be
     * structured as a single directory with all of the required files. One of those files is designated the "main file"
     * for the blueprint. Files are provided as byte arrays, though expect only text files will be distributed from ASDC
     * and stored by MSO.
     *
     * Cloudify requires a single root directory in its blueprint zip files. The requested blueprint ID will also be
     * used as the directory. All of the files will be added to this directory in the zip file.
     */
    public void uploadBlueprint(String cloudSiteId, String blueprintId, String mainFileName,
            Map<String, byte[]> blueprintFiles, boolean failIfExists) throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        Cloudify cloudify = getCloudifyClient(cloudSite.get());

        boolean blueprintUploaded = uploadBlueprint(cloudify, blueprintId, mainFileName, blueprintFiles);

        if (!blueprintUploaded && failIfExists) {
            throw new MsoAdapterException("Blueprint already exists");
        }
    }

    /*
     * Common method to load a blueprint. May be called from
     */
    protected boolean uploadBlueprint(Cloudify cloudify, String blueprintId, String mainFileName,
            Map<String, byte[]> blueprintFiles) throws MsoException {
        // Check if it already exists. If so, return false.
        GetBlueprint getRequest = cloudify.blueprints().getMetadataById(blueprintId);
        try {
            Blueprint bp = getRequest.execute();
            if (bp != null) {
                logger.debug("Blueprint {} already exists.", bp.getId());
                return false;
            } else {
                logger.debug("Null blueprint!");
            }
        } catch (CloudifyResponseException ce) {
            if (ce.getStatus() == 404) {
                // This is the expected result.
                logger.debug("Verified that Blueprint doesn't exist yet");
            } else {
                throw ce;
            }
        }

        // Create a blueprint ZIP file in memory
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(zipBuffer);

        try {
            // Put the root directory
            String rootDir = blueprintId + (blueprintId.endsWith("/") ? "" : "/");
            zipOut.putNextEntry(new ZipEntry(rootDir));
            zipOut.closeEntry();

            for (String fileName : blueprintFiles.keySet()) {
                ZipEntry ze = new ZipEntry(rootDir + fileName);
                zipOut.putNextEntry(ze);
                zipOut.write(blueprintFiles.get(fileName));
                zipOut.closeEntry();
            }
            zipOut.close();
        } catch (IOException e) {
            // Since we're writing to a byte array, this should never happen
        }
        logger.debug("Blueprint zip file size: {}", zipBuffer.size());

        // Ready to upload the blueprint zip

        try (InputStream blueprintStream = new ByteArrayInputStream(zipBuffer.toByteArray())) {
            UploadBlueprint uploadRequest =
                    cloudify.blueprints().uploadFromStream(blueprintId, mainFileName, blueprintStream);
            Blueprint blueprint = uploadRequest.execute();
            logger.debug("Successfully uploaded blueprint {}", blueprint.getId());
        } catch (CloudifyResponseException | CloudifyConnectException e) {
            throw cloudifyExceptionToMsoException(e, UPLOAD_BLUEPRINT);
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException(e, UPLOAD_BLUEPRINT);
        } catch (IOException e) {
            // for try-with-resources
            throw ioExceptionToMsoException(e, UPLOAD_BLUEPRINT);
        }

        return true;
    }



    // ---------------------------------------------------------------
    // PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    /**
     * Get a Cloudify client for the specified cloud site. Everything that is required can be found in the Cloud Config.
     *
     * @param cloudSite
     * @return a Cloudify object
     */
    public Cloudify getCloudifyClient(CloudSite cloudSite) throws MsoException {
        CloudifyManager cloudifyConfig = cloudConfig.getCloudifyManager(cloudSite.getCloudifyId());
        if (cloudifyConfig == null) {
            throw new MsoCloudifyManagerNotFound(cloudSite.getId());
        }

        // Get a Cloudify client
        // Set a Token Provider to fetch tokens from Cloudify itself.
        String cloudifyUrl = cloudifyConfig.getCloudifyUrl();
        Cloudify cloudify = new Cloudify(cloudifyUrl);
        cloudify.setTokenProvider(new CloudifyClientTokenProvider(cloudifyUrl, cloudifyConfig.getUsername(),
                CryptoUtils.decryptCloudConfigPassword(cloudifyConfig.getPassword())));

        return cloudify;
    }


    /*
     * Query for a Cloudify Deployment. This function is needed in several places, so a common method is useful. This
     * method takes an authenticated CloudifyClient (which internally identifies the cloud & tenant to search), and
     * returns a Deployment object if found, Null if not found, or an MsoCloudifyException if the Cloudify API call
     * fails.
     *
     * @param cloudifyClient an authenticated Cloudify client
     *
     * @param deploymentId the deployment to query
     *
     * @return a Deployment object or null if the requested deployment doesn't exist.
     *
     * @throws MsoCloudifyException Thrown if the Cloudify API call returns an exception
     */
    protected Deployment queryDeployment(Cloudify cloudify, String deploymentId) throws MsoException {
        if (deploymentId == null) {
            return null;
        }
        try {
            GetDeployment request = cloudify.deployments().byId(deploymentId);
            return executeAndRecordCloudifyRequest(request);
        } catch (CloudifyResponseException e) {
            if (e.getStatus() == 404) {
                logger.debug("queryDeployment - not found: {}", deploymentId);
                return null;
            } else {
                // Convert the CloudifyResponseException to an MsoCloudifyException
                throw cloudifyExceptionToMsoException(e, "QueryDeployment");
            }
        } catch (CloudifyConnectException e) {
            // Connection to Openstack failed
            throw cloudifyExceptionToMsoException(e, "QueryDeployment");
        }
    }


    public void copyStringOutputsToInputs(Map<String, String> inputs, Map<String, Object> otherStackOutputs,
            boolean overWrite) {
        if (inputs == null || otherStackOutputs == null)
            return;

        for (Map.Entry<String, Object> entry : otherStackOutputs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof JsonNode) {
                // This is a bit of mess - but I think it's the least impacting
                // let's convert it BACK to a string - then it will get converted back later
                try {
                    inputs.put(key, this.convertNode((JsonNode) value));
                } catch (Exception e) {
                    logger.debug("WARNING: unable to convert JsonNode output value for {}", key);
                    // effect here is this value will not have been copied to the inputs - and therefore will error out
                    // downstream
                }
            } else if (value instanceof java.util.LinkedHashMap) {
                logger.debug("LinkedHashMap - this is showing up as a LinkedHashMap instead of JsonNode");
                try {
                    inputs.put(key, JSON_MAPPER.writeValueAsString(value));
                } catch (Exception e) {
                    logger.debug("WARNING: unable to convert LinkedHashMap output value for {}", key);
                }
            } else {
                // just try to cast it - could be an integer or some such
                try {
                    inputs.put(key, (String) value);
                } catch (Exception e) {
                    logger.debug("WARNING: unable to convert output value for {}", key);
                    // effect here is this value will not have been copied to the inputs - and therefore will error out
                    // downstream
                }
            }
        }
        return;
    }

    /*
     * Normalize an input value to an Object, based on the target parameter type. If the type is not recognized, it will
     * just be returned unchanged (as a string).
     */
    public Object convertInputValue(Object inputValue, HeatTemplateParam templateParam) {
        String type = templateParam.getParamType();
        logger.debug("Parameter: {} is of type {}", templateParam.getParamName(), type);

        if (("number").equalsIgnoreCase(type)) {
            try {
                return Integer.valueOf(inputValue.toString());
            } catch (Exception e) {
                logger.debug("Unable to convert {} to an integer!", inputValue);
                return null;
            }
        } else if (("json").equalsIgnoreCase(type)) {
            try {
                if (inputValue instanceof String) {
                    return JSON_MAPPER.readTree(inputValue.toString());
                }
                // will already marshal to json without intervention
                return inputValue;
            } catch (Exception e) {
                logger.debug("Unable to convert {} to a JsonNode!", inputValue);
                return null;
            }
        } else if (("boolean").equalsIgnoreCase(type)) {
            return Boolean.valueOf(inputValue.toString());
        }

        // Nothing else matched. Return the original string
        return inputValue;
    }


    private String convertNode(final JsonNode node) {
        try {
            final Object obj = JSON_MAPPER.treeToValue(node, Object.class);
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (JsonParseException jpe) {
            logger.debug("Error converting json to string {}", jpe);
        } catch (Exception e) {
            logger.debug("Error converting json to string {}", e);
        }
        return "[Error converting json to string]";
    }


    /*
     * Method to execute a Cloudify command and track its execution time. For the metrics log, a category of "Cloudify"
     * is used along with a sub-category that identifies the specific call (using the real cloudify-client classname of
     * the CloudifyRequest<T> parameter).
     */


    protected <T> T executeAndRecordCloudifyRequest(CloudifyRequest<T> request) {

        String requestType;
        if (request.getClass().getEnclosingClass() != null) {
            requestType =
                    request.getClass().getEnclosingClass().getSimpleName() + "." + request.getClass().getSimpleName();
        } else {
            requestType = request.getClass().getSimpleName();
        }

        int retryDelay = poConfig.getRetryDelay();
        int retryCount = poConfig.getRetryCount();
        String retryCodes = poConfig.getRetryCodes();

        // Run the actual command. All exceptions will be propagated
        while (true) {
            try {
                return request.execute();
            } catch (CloudifyResponseException e) {
                boolean retry = false;
                if (retryCodes != null) {
                    int code = e.getStatus();
                    logger.debug("Config values RetryDelay: {} RetryCount:{} RetryCodes:{} ResponseCode:{}", retryDelay,
                            retryCount, retryCodes, code);
                    for (String rCode : retryCodes.split(",")) {
                        try {
                            if (retryCount > 0 && code == Integer.parseInt(rCode)) {
                                retryCount--;
                                retry = true;
                                logger.debug(
                                        "CloudifyResponseException ResponseCode:{} request:{} Retry indicated. Attempts remaining:{}",
                                        code, requestType, retryCount);
                                break;
                            }
                        } catch (NumberFormatException e1) {
                            logger.error("{} No retries. Exception in parsing retry code in config:{} {}",
                                    MessageEnum.RA_CONFIG_EXC, rCode, ErrorCode.SchemaError.getValue());
                            throw e;
                        }
                    }
                }
                if (retry) {
                    sleep(retryDelay * 1000L);
                } else
                    throw e; // exceeded retryCount or code is not retryable
            } catch (CloudifyConnectException e) {
                // Connection to Cloudify failed
                if (retryCount > 0) {
                    retryCount--;
                    logger.debug(" request: {} Retry indicated. Attempts remaining:{}", requestType, retryCount);
                    sleep(retryDelay * 1000L);
                } else
                    throw e;

            }
        }
    }

    /*
     * Convert an Exception on a Cloudify call to an MsoCloudifyException. This method supports
     * CloudifyResponseException and CloudifyConnectException.
     */
    protected MsoException cloudifyExceptionToMsoException(CloudifyBaseException e, String context) {
        MsoException me = null;

        if (e instanceof CloudifyResponseException) {
            CloudifyResponseException re = (CloudifyResponseException) e;

            try {
                // Failed Cloudify calls return an error entity body.
                CloudifyError error = re.getResponse().getErrorEntity(CloudifyError.class);
                logger.error("{} {} {} Exception - Cloudify Error on {}: {}", MessageEnum.RA_CONNECTION_EXCEPTION,
                        CLOUDIFY, ErrorCode.DataError.getValue(), context, error.getErrorCode());
                String fullError = error.getErrorCode() + ": " + error.getMessage();
                logger.debug(fullError);
                me = new MsoCloudifyException(re.getStatus(), re.getMessage(), fullError);
            } catch (Exception e2) {
                // Couldn't parse the body as a "CloudifyError". Report the original HTTP error.
                logger.error("{} {} {} Exception - HTTP Error on {}: {}, {} ", MessageEnum.RA_CONNECTION_EXCEPTION,
                        CLOUDIFY, ErrorCode.DataError.getValue(), context, re.getStatus(), e.getMessage(), e2);
                me = new MsoCloudifyException(re.getStatus(), re.getMessage(), "");
            }

            // Add the context of the error
            me.addContext(context);

            // Generate an alarm for 5XX and higher errors.
            if (re.getStatus() >= 500) {

            }
        } else if (e instanceof CloudifyConnectException) {
            CloudifyConnectException ce = (CloudifyConnectException) e;

            me = new MsoIOException(ce.getMessage());
            me.addContext(context);

            // Generate an alarm for all connection errors.

            logger.error("{} {} {} Cloudify connection error on {}: ", MessageEnum.RA_CONNECTION_EXCEPTION, CLOUDIFY,
                    ErrorCode.DataError.getValue(), context, e);
        }

        return me;
    }



    /*******************************************************************************
     *
     * Methods (and associated utilities) to implement the VduPlugin interface
     *
     *******************************************************************************/

    /**
     * VduPlugin interface for instantiate function.
     *
     * This one is a bit more complex, in that it will first upload the blueprint if needed, then create the Cloudify
     * deployment and execute the install workflow.
     *
     * This implementation also merges any parameters defined in the ENV file with the other other input parameters for
     * any undefined parameters). The basic MsoCloudifyUtils separates blueprint management from deploument actions, but
     * the VduPlugin does not declare blueprint management operations.
     */
    @Override
    public VduInstance instantiateVdu(CloudInfo cloudInfo, String instanceName, Map<String, Object> inputs,
            VduModelInfo vduModel, boolean rollbackOnFailure) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        // Translate the VDU ModelInformation structure to that which is needed for
        // creating and uploading a blueprint. Use the model customization UUID as
        // the blueprint identifier.

        String blueprintId = vduModel.getModelCustomizationUUID();

        try {

            if (!isBlueprintLoaded(cloudSiteId, blueprintId)) {
                logger.debug("Blueprint {} is not loaded.  Will upload it now.", blueprintId);

                // Prepare the blueprint inputs. Need the set of blueprint templates and files,
                // plus the main blueprint name.
                Map<String, byte[]> blueprintFiles = new HashMap<>();
                String mainTemplate = "";

                // Add all of the blueprint artifacts from the VDU model
                List<VduArtifact> vduArtifacts = vduModel.getArtifacts();
                for (VduArtifact vduArtifact : vduArtifacts) {
                    // Add all artifacts to the blueprint, with one exception.
                    // ENVIRONMENT files will be processed later as additional parameters.

                    ArtifactType artifactType = vduArtifact.getType();
                    if (artifactType != ArtifactType.ENVIRONMENT) {
                        blueprintFiles.put(vduArtifact.getName(), vduArtifact.getContent());

                        if (artifactType == ArtifactType.MAIN_TEMPLATE) {
                            mainTemplate = vduArtifact.getName();
                        }
                    }
                }

                // Upload the blueprint package
                uploadBlueprint(cloudSiteId, blueprintId, mainTemplate, blueprintFiles, false);
            }
        } catch (Exception e) {
            throw new VduException("CloudifyUtils (instantiateVDU): blueprint Exception", e);
        }


        // Next, create and install a new deployment based on the blueprint.
        // For Cloudify, the deploymentId is specified by the client. Just use the instance name
        // as the ID.

        try {
            // Query the Cloudify Deployment object and populate a VduInstance
            DeploymentInfo deployment =
                    createAndInstallDeployment(cloudSiteId, tenantId, instanceName, blueprintId, inputs, true, // (poll
                                                                                                               // for
                                                                                                               // completion)
                            vduModel.getTimeoutMinutes(), rollbackOnFailure);

            return deploymentInfoToVduInstance(deployment);
        } catch (Exception e) {
            throw new VduException("CloudifyUtils (instantiateVDU): Create-and-install-deployment Exception", e);
        }
    }


    /**
     * VduPlugin interface for query function.
     */
    @Override
    public VduInstance queryVdu(CloudInfo cloudInfo, String instanceId) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Query the Cloudify Deployment object and populate a VduInstance
            DeploymentInfo deployment = queryDeployment(cloudSiteId, tenantId, instanceId);

            return deploymentInfoToVduInstance(deployment);
        } catch (Exception e) {
            throw new VduException("Query VDU Exception", e);
        }
    }


    /**
     * VduPlugin interface for delete function.
     */
    @Override
    public VduInstance deleteVdu(CloudInfo cloudInfo, String instanceId, int timeoutMinutes) throws VduException {
        String cloudSiteId = cloudInfo.getCloudSiteId();
        String tenantId = cloudInfo.getTenantId();

        try {
            // Uninstall and delete the Cloudify Deployment
            DeploymentInfo deployment = uninstallAndDeleteDeployment(cloudSiteId, tenantId, instanceId, timeoutMinutes);

            // Populate a VduInstance based on the deleted Cloudify Deployment object
            return deploymentInfoToVduInstance(deployment);
        } catch (Exception e) {
            throw new VduException("Delete VDU Exception", e);
        }
    }


    /**
     * VduPlugin interface for update function.
     *
     * Update is currently not supported in the MsoCloudifyUtils implementation. Just return a VduException.
     *
     */
    @Override
    public VduInstance updateVdu(CloudInfo cloudInfo, String instanceId, Map<String, Object> inputs,
            VduModelInfo vduModel, boolean rollbackOnFailure) throws VduException {
        throw new VduException("CloudifyUtils: updateVDU interface not supported");
    }


    /*
     * Convert the local DeploymentInfo object (Cloudify-specific) to a generic VduInstance object
     */
    protected VduInstance deploymentInfoToVduInstance(DeploymentInfo deployment) {
        VduInstance vduInstance = new VduInstance();

        // only one ID in Cloudify, use for both VDU name and ID
        vduInstance.setVduInstanceId(deployment.getId());
        vduInstance.setVduInstanceName(deployment.getId());

        // Copy inputs and outputs
        vduInstance.setInputs(deployment.getInputs());
        vduInstance.setOutputs(deployment.getOutputs());

        // Translate the status elements
        vduInstance.setStatus(deploymentStatusToVduStatus(deployment));

        return vduInstance;
    }

    protected VduStatus deploymentStatusToVduStatus(DeploymentInfo deployment) {
        VduStatus vduStatus = new VduStatus();

        // Determine the status based on last action & status
        // DeploymentInfo object should be enhanced to report a better status internally.
        DeploymentStatus status = deployment.getStatus();

        if (status == null) {
            vduStatus.setState(VduStateType.UNKNOWN);
        } else if (status == DeploymentStatus.NOTFOUND) {
            vduStatus.setState(VduStateType.NOTFOUND);
        } else if (status == DeploymentStatus.INSTALLED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
        } else if (status == DeploymentStatus.CREATED) {
            // Deployment exists but is not installed. This shouldn't really happen,
            // since create + install or uninstall + delete are always done together.
            // But account for it anyway, assuming the operation is still in progress.
            String lastAction = deployment.getLastAction();
            if (lastAction == null)
                vduStatus.setState(VduStateType.INSTANTIATING);
            else
                vduStatus.setState(VduStateType.DELETING);
        } else if (status == DeploymentStatus.FAILED) {
            vduStatus.setState(VduStateType.FAILED);
        } else {
            vduStatus.setState(VduStateType.UNKNOWN);
        }

        vduStatus.setErrorMessage(deployment.getErrorMessage());
        vduStatus.setLastAction(new PluginAction(deployment.getLastAction(), deployment.getActionStatus(),
                deployment.getErrorMessage()));

        return vduStatus;
    }

    /*
     * Return an OpenstackConfig object as expected by Cloudify Openstack Plug-in. Base the values on the CloudSite
     * definition.
     */
    protected OpenstackConfig getOpenstackConfig(CloudSite cloudSite, String tenantId) {
        OpenstackConfig openstackConfig = new OpenstackConfig();
        openstackConfig.setRegion(cloudSite.getRegionId());
        openstackConfig.setAuthUrl(cloudSite.getIdentityService().getIdentityUrl());
        openstackConfig.setUsername(cloudSite.getIdentityService().getMsoId());
        openstackConfig
                .setPassword(CryptoUtils.decryptCloudConfigPassword(cloudSite.getIdentityService().getMsoPass()));
        openstackConfig.setTenantName(tenantId);
        return openstackConfig;
    }

    /*
     * Return an Azure object as expected by Cloudify Azure Plug-in. Base the values on the CloudSite definition.
     */
    protected AzureConfig getAzureConfig(CloudSite cloudSite, String tenantId) {
        AzureConfig azureConfig = new AzureConfig();
        // TODO: Use adminTenant for now, instead of adding another element
        azureConfig.setSubscriptionId(cloudSite.getIdentityService().getAdminTenant());
        azureConfig.setTenantId(tenantId);
        azureConfig.setClientId(cloudSite.getIdentityService().getMsoId());
        azureConfig.setClientSecret(cloudSite.getIdentityService().getMsoPass());
        return azureConfig;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.debug("Thread interrupted while sleeping!", e);
            Thread.currentThread().interrupt();
        }
    }
}
