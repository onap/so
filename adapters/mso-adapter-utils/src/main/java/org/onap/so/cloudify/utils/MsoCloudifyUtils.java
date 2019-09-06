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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.onap.so.cloudify.client.APIV31;
import org.onap.so.cloudify.client.APIV31.ExecutionStatus;
import org.onap.so.cloudify.client.APIV31.Visibility;
import org.onap.so.cloudify.client.APIV31Impl;
import org.onap.so.cloudify.client.DeploymentV31;
import org.onap.so.cloudify.client.ExecutionV31;
import org.onap.so.cloudify.exceptions.MsoCloudifyException;
import org.onap.so.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.utils.MsoCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MsoCloudifyUtils extends MsoCommonUtils implements VduPlugin {

    private static final String CLOUDIFY = "Cloudify";
    private static final String CREATE_DEPLOYMENT = "CreateDeployment";
    private static final String DELETE_DEPLOYMENT = "DeleteDeployment";
    private static final String TERMINATED_STATUS = "terminated";
    private static final String CANCELLED = "cancelled";
    private static final String INSTALL_WORKFLOW = "install";
    private static final String UNINSTALL_WORKFLOW = "uninstall";
    private static final String UPLOAD_BLUEPRINT = "UPLOAD_BLUEPRINT";

    // Fetch cloud configuration each time (may be cached in CloudConfig class)
    @Autowired
    protected CloudConfig cloudConfig;

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
     * @param cloudSiteId
     *            The cloud (may be a region) in which to create the stack.
     * @param tenantId
     *            The Openstack ID of the tenant in which to create the Stack
     * @param deploymentId
     *            The identifier (name) of the deployment to create
     * @param blueprintId
     *            The blueprint from which to create the deployment.
     * @param inputs
     *            A map of key/value inputs
     * @param pollForCompletion
     *            Indicator that polling should be handled in Java vs. in the client
     * @param timeoutMinutes
     *            Timeout after which the "install" will be cancelled
     * @param backout
     *            Flag to delete deployment on install Failure - defaulted to True
     * @return A DeploymentInfo object
     * @throws MsoCloudifyException
     *             Thrown if the Cloudify API call returns an exception.
     * @throws MsoIOException
     *             Thrown on Cloudify connection errors.
     */

    public DeploymentV31 createAndInstallDeployment(String cloudSiteId, String tenantId, String deploymentId,
            String blueprintId, Map<String, ? extends Object> inputs, boolean pollForCompletion, int timeoutMinutes,
            boolean backout) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        logger.debug("Ready to Create Deployment ({}) with input params: {}", deploymentId, inputs);

        // Build up the inputs, including:
        // - from provided "environment" file
        // - passed in by caller
        // - special input for cloud-specific Credentials
        Map<String, String> expandedInputs = new HashMap<>((HashMap<String, String>) inputs);

        try {
            cloudify.createDeployment(deploymentId, blueprintId, expandedInputs, false, false, Visibility.TENANT);
        } catch (Exception e) {
            MsoException me = new MsoCloudifyException(400, e.getMessage(), null);
            throw me;
        }

        /*
         * Next execute the "install" workflow. Note - this assumes there are no additional parameters required for the
         * workflow.
         */

        try {
            ExecutionV31 installWorkflow = cloudify.runExecution(INSTALL_WORKFLOW, deploymentId, null, false, false,
                    false, null, timeoutMinutes * 60, true);

            if (installWorkflow.getStatus().equals(ExecutionStatus.STATUS_TERMINATED)) {
                // Success!
                return cloudify.getDeployment(deploymentId);
            } else {
                if (installWorkflow.getStatus().equals(ExecutionStatus.STATUS_CANCELLING)) {
                    // cancel failed
                    MsoException me = new MsoCloudifyException(400, "cancel failed", null);
                    throw me;
                }
                // The workflow completed with errors. Must try to back it out.
                if (!backout) {
                    logger.warn("{} Deployment installation failed, backout deletion suppressed {} {}",
                            MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcesssError.getValue(),
                            "Exception in Deployment Installation, backout suppressed");
                } else {
                    ExecutionV31 uninstallWorkflow = cloudify.runExecution(UNINSTALL_WORKFLOW, deploymentId, null,
                            false, false, false, null, 360, false);
                }

                MsoCloudifyException me = new MsoCloudifyException(0, "Workflow Execution Failed",
                        installWorkflow.getError());
                me.addContext(CREATE_DEPLOYMENT);
                throw me;
            }
        } catch (MsoException me) {
            // Install failed. Unless requested otherwise, back out the deployment

            if (!backout) {
                logger.warn("{} Deployment installation failed, backout deletion suppressed {}",
                        MessageEnum.RA_CREATE_STACK_ERR, ErrorCode.BusinessProcesssError.getValue());
            } else {
                ExecutionV31 uninstallWorkflow = cloudify.runExecution(UNINSTALL_WORKFLOW, deploymentId, null, false,
                        false, false, null, 360, false);
            }

            // Propagate the original exception from Stack Query.
            me.addContext(CREATE_DEPLOYMENT);

            throw me;
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
     * @param tenantId
     *            The Openstack ID of the tenant in which to perform the delete
     * @param cloudSiteId
     *            The cloud identifier (may be a region) from which to delete the stack.
     * @return A StackInfo object
     * @throws MsoOpenstackException
     *             Thrown if the Openstack API call returns an exception.
     * @throws MsoCloudSiteNotFound
     */
    public DeploymentV31 uninstallAndDeleteDeployment(String cloudSiteId, String tenantId, String deploymentId,
            int timeoutMinutes) throws MsoException {
        // Obtain the cloud site information where we will create the stack
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }

        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        logger.debug("Ready to Uninstall/Delete Deployment ({})", deploymentId);
        DeploymentV31 deployment = null;
        try {
            deployment = cloudify.getDeployment(deploymentId);
        } catch (Exception e) {
            throw new MsoCloudifyException(400, "Error locating deployment " + deploymentId, null);
        }

        ExecutionV31 exe = cloudify.runExecution(UNINSTALL_WORKFLOW, deploymentId, null, false, false, false, null,
                timeoutMinutes * 60, false);

        if (!exe.getStatus().equals(ExecutionStatus.STATUS_TERMINATED.toString())) {
            throw new MsoCloudifyException(400, "Failed uninstall on deployment " + deploymentId, null);
        }

        try {
            deployment = cloudify.deleteDeployment(deploymentId, false);
        } catch (Exception e) {
            throw new MsoCloudifyException(400, e.getMessage(), null);
        }
        return deployment;

    }

    /**
     * Check if a blueprint is available for use at a targeted cloud site. This requires checking the Cloudify Manager
     * which is servicing that cloud site to see if the specified blueprint has been loaded.
     *
     * @param cloudSiteId
     *            The cloud site where the blueprint is needed
     * @param blueprintId
     *            The ID for the blueprint in Cloudify
     */
    public boolean isBlueprintLoaded(String cloudSiteId, String blueprintId) throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }
        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        try {
            cloudify.getBlueprint(blueprintId);
        } catch (Exception e) {
            return false;
        }

        return true;

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
    public void uploadBlueprint(String cloudSiteId, String blueprintId, String mainFileName, byte[] archive)
            throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }
        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        try {
            cloudify.uploadBlueprint(blueprintId, mainFileName, Visibility.TENANT, archive);
        } catch (Exception e) {
            throw new MsoAdapterException("Blueprint already exists");
        }
    }

    public DeploymentV31 queryDeployment(String cloudSiteId, String deploymentId) throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }
        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        return cloudify.getDeployment(deploymentId);
    }

    public enum DeploymentStatus {
        UNKNOWN("unknown"), CREATED("created"), INSTALLED("installed"), INSTALLING("installing"), FAILED(
                "failed"), UNINSTALLED("uninstalled"), UNINSTALLING("uninstalling"), DELETED("deleted");

        String val;

        DeploymentStatus(String val) {
            this.val = val;
        }

        public String toString() {
            return this.val;
        }
    }

    public enum DeploymentAction {
        INSTALL("install"), CREATE("create"), UNINSTALL("uninstall"), DELETE("delete"), UNKNOWN("unknown");
        private String val;

        DeploymentAction(String val) {
            this.val = val;
        }

        public String toString() {
            return this.val;
        }
    }

    public static class DeploymentState {
        DeploymentStatus status;

        public DeploymentStatus getStatus() {
            return status;
        }

        public void setStatus(DeploymentStatus status) {
            this.status = status;
        }

        public DeploymentAction getAction() {
            return action;
        }

        public void setAction(DeploymentAction action) {
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        DeploymentAction action;
        String message = null;

        public DeploymentState(DeploymentAction action, DeploymentStatus status) {
            this.action = action;
            this.status = status;
        }

        public DeploymentState(DeploymentAction action, DeploymentStatus status, String msg) {
            this.action = action;
            this.status = status;
            this.message = msg;
        }
    }

    public DeploymentState getDeploymentStatus(String cloudSiteId, DeploymentV31 deployment) throws MsoException {
        // Obtain the cloud site information where we will load the blueprint
        Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSite.isPresent()) {
            throw new MsoCloudSiteNotFound(cloudSiteId);
        }
        APIV31 cloudify = getCloudifyClient(cloudSite.get());

        // TODO: logic here is weak. Assumes non-existent deployment was deleted
        try {
            cloudify.getDeployment(deployment.getId());
        } catch (Exception e) {
            return new DeploymentState(DeploymentAction.DELETE, DeploymentStatus.DELETED);
        }

        List<ExecutionV31> elist = cloudify.listExecutions(deployment.getId());
        if (elist == null || elist.size() == 0) {
            return new DeploymentState(DeploymentAction.UNKNOWN, DeploymentStatus.UNKNOWN);
        }

        // evaluate state based on last execution
        ExecutionV31 lastex = null;
        // find last install/uninstall
        for (int i = elist.size() - 1; i >= 0; i--) {
            lastex = elist.get(elist.size() - 1);
            if (lastex.getWorkflow_id().equals("install") || lastex.getWorkflow_id().equals("uninstall"))
                break;
        }
        if (lastex == null) {
            return new DeploymentState(DeploymentAction.CREATE, DeploymentStatus.CREATED);
        }

        String estatus = lastex.getStatus();

        if (lastex.getWorkflow_id().equals("uninstall")) {
            if (estatus.equals(ExecutionStatus.STATUS_TERMINATED)) {
                return new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.UNINSTALLED);
            }
            if (estatus.equals(ExecutionStatus.STATUS_STARTED) || estatus.equals(ExecutionStatus.STATUS_PENDING)) {
                return new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.UNINSTALLING);
            }
            // TODO: there should be more fine grained status here: cancelled mean indeterminate
            if (estatus.equals(ExecutionStatus.STATUS_CANCELLED)) {
                return new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.INSTALLED);
            }
            if (estatus.equals(ExecutionStatus.STATUS_FAILED)) {
                return new DeploymentState(DeploymentAction.UNINSTALL, DeploymentStatus.FAILED, lastex.getError());
            }
            return new DeploymentState(DeploymentAction.UNKNOWN, DeploymentStatus.UNKNOWN);
        }

        if (lastex.getWorkflow_id().equals("install")) {
            if (estatus.equals(ExecutionStatus.STATUS_FAILED)) {
                return new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.FAILED);
            }
            if (estatus.equals(ExecutionStatus.STATUS_CANCELLED)) {
                return new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.FAILED);
            }
            if (estatus.equals(ExecutionStatus.STATUS_TERMINATED)) {
                return new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.INSTALLED);
            }
            if (estatus.equals(ExecutionStatus.STATUS_STARTED) || estatus.equals(ExecutionStatus.STATUS_PENDING)) {
                return new DeploymentState(DeploymentAction.INSTALL, DeploymentStatus.INSTALLING);
            }
            return new DeploymentState(DeploymentAction.UNKNOWN, DeploymentStatus.UNKNOWN);
        }

        return new DeploymentState(DeploymentAction.CREATE, DeploymentStatus.CREATED);
    }

    // ---------------------------------------------------------------
    // PRIVATE FUNCTIONS FOR USE WITHIN THIS CLASS

    /**
     * Get a Cloudify client for the specified cloud site. Everything that is required can be found in the Cloud Config.
     *
     * @param cloudSite
     * @return a Cloudify object
     */
    protected APIV31 getCloudifyClient(CloudSite cloudSite) throws MsoException {
        CloudifyManager cloudifyConfig = cloudConfig.getCloudifyManager(cloudSite.getCloudifyId());
        if (cloudifyConfig == null) {
            throw new MsoCloudifyManagerNotFound(cloudSite.getId());
        }

        // Get a Cloudify client
        // Set a Token Provider to fetch tokens from Cloudify itself.
        String cloudifyUrl = cloudifyConfig.getCloudifyUrl();
        // TODO: Tenant not stored in database, default to default_tenant
        APIV31 cloudify = APIV31Impl.create("default_tenant", cloudifyConfig.getUsername(),
                cloudifyConfig.getPassword(), cloudifyUrl);
        return cloudify;
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
                uploadBlueprint(cloudSiteId, blueprintId, mainTemplate, blueprintFiles.get("archive"));
            }
        } catch (Exception e) {
            throw new VduException("CloudifyUtils (instantiateVDU): blueprint Exception", e);
        }

        // Next, create and install a new deployment based on the blueprint.
        // For Cloudify, the deploymentId is specified by the client. Just use the instance name
        // as the ID.

        try {
            // Query the Cloudify Deployment object and populate a VduInstance
            DeploymentV31 deployment = createAndInstallDeployment(cloudSiteId, tenantId, instanceName, blueprintId,
                    inputs, true, // (poll
                    // for
                    // completion)
                    vduModel.getTimeoutMinutes(), rollbackOnFailure);

            return deploymentToVduInstance(cloudSiteId, deployment);
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

        try {
            // Query the Cloudify Deployment object and populate a VduInstance
            DeploymentV31 deployment = queryDeployment(cloudSiteId, instanceId);

            return deploymentToVduInstance(cloudSiteId, deployment);
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
            DeploymentV31 deployment = uninstallAndDeleteDeployment(cloudSiteId, tenantId, instanceId, timeoutMinutes);

            // Populate a VduInstance based on the deleted Cloudify Deployment object
            return deploymentToVduInstance(cloudSiteId, deployment);
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
    protected VduInstance deploymentToVduInstance(String cloudSiteId, DeploymentV31 deployment) throws MsoException {
        VduInstance vduInstance = new VduInstance();

        // only one ID in Cloudify, use for both VDU name and ID
        vduInstance.setVduInstanceId(deployment.getId());
        vduInstance.setVduInstanceName(deployment.getId());

        // Copy inputs and outputs
        vduInstance.setInputs(deployment.getInputs());
        vduInstance.setOutputs(deployment.getOutputs());

        // Translate the status elements
        vduInstance.setStatus(deploymentStatusToVduStatus(cloudSiteId, deployment));

        return vduInstance;
    }

    protected VduStatus deploymentStatusToVduStatus(String cloudSiteId, DeploymentV31 deployment) throws MsoException {
        VduStatus vduStatus = new VduStatus();

        // Determine the status based on last action & status
        // DeploymentInfo object should be enhanced to report a better status internally.
        DeploymentState status = getDeploymentStatus(cloudSiteId, deployment);

        if (status == null || status.status == DeploymentStatus.UNKNOWN) {
            vduStatus.setState(VduStateType.UNKNOWN);
        } else if (status.status == DeploymentStatus.INSTALLED) {
            vduStatus.setState(VduStateType.INSTANTIATED);
        } else if (status.status == DeploymentStatus.INSTALLING) {
            vduStatus.setState(VduStateType.INSTANTIATING);
        } else if (status.status == DeploymentStatus.UNINSTALLING) {
            vduStatus.setState(VduStateType.DELETING);
        } else if (status.status == DeploymentStatus.FAILED) {
            vduStatus.setState(VduStateType.FAILED);
        } else if (status.status == DeploymentStatus.DELETED) {
            vduStatus.setState(VduStateType.DELETED);
        } else {
            vduStatus.setState(VduStateType.UNKNOWN);
        }

        vduStatus.setErrorMessage(status.message);
        vduStatus.setLastAction(new PluginAction(status.action.toString(), status.status.toString(), status.message));

        return vduStatus;
    }

}
