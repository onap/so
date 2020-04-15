/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.installer;
 
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;

/**
 * Abstract class to represent the resource structure.
 *
 * This structure exists to avoid having issues if the order of the resource artifact of tree structure is not good.
 */
public abstract class ResourceStructure {

    /**
     * Flag to indicate whether the resource is deployed successfully.
     */
    protected boolean isDeployedSuccessfully = false;

    /**
     * Flag to indicate whether the resource is already deployed.
     */
    protected boolean isAlreadyDeployed = false;

    /**
     * The resource type.
     */
    protected ResourceType resourceType;

    /**
     * The Raw notification data.
     */
    protected INotificationData notificationData;

    /**
     * The resource we will try to deploy.
     */
    protected IResourceInstance resourceInstance;

    /**
     * Number of resources provided by the resource structure.
     */
    protected int numberOfResources;

    /**
     * The list of artifacts existing in this resource hashed by UUID.
     */
    protected final Map<String, VfModuleArtifact> artifactsMapByUUID;

    /**
     * The list of workflow artifacts existing in this resource
     */
    protected final Map<String, WorkflowArtifact> workflowArtifactsMapByUUID;

    public ResourceStructure(INotificationData notificationData, IResourceInstance resourceInstance) {
        this.notificationData = notificationData;
        this.resourceInstance = resourceInstance;
        artifactsMapByUUID = new HashMap<>();
        workflowArtifactsMapByUUID = new HashMap<>();
    }

    /**
     * Add artifact to the resource structure.
     *
     * @param distributionClient
     * @param artifactinfo
     * @param clientResult
     * @throws UnsupportedEncodingException
     */
    public abstract void addArtifactToStructure(IDistributionClient distributionClient, IArtifactInfo artifactinfo,
            IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException;

    public abstract void addWorkflowArtifactToStructure(IArtifactInfo artifactinfo,
            IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException;

    /**
     * Prepare the resource for installation.
     *
     * @throws ArtifactInstallerException
     */
    public abstract void prepareInstall() throws ArtifactInstallerException;

    public boolean isDeployedSuccessfully() {
        return isDeployedSuccessfully;
    }

    public void setDeployedSuccessfully(boolean deployedSuccessfully) {
        isDeployedSuccessfully = deployedSuccessfully;
    }

    public boolean isAlreadyDeployed() {
        return isAlreadyDeployed;
    }

    public void setAlreadyDeployed(boolean alreadyDeployed) {
        isAlreadyDeployed = alreadyDeployed;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public INotificationData getNotification() {
        return notificationData;
    }

    public void setNotification(INotificationData notificationData) {
        this.notificationData = notificationData;
    }

    public IResourceInstance getResourceInstance() {
        return resourceInstance;
    }

    public void setResourceInstance(IResourceInstance resourceInstance) {
        this.resourceInstance = resourceInstance;
    }

    public int getNumberOfResources() {
        return numberOfResources;
    }

    public void setNumberOfResources(int numberOfResources) {
        this.numberOfResources = numberOfResources;
    }

    public Map<String, VfModuleArtifact> getArtifactsMapByUUID() {
        return artifactsMapByUUID;
    }

    public Map<String, WorkflowArtifact> getWorkflowArtifactsMapByUUID() {
        return workflowArtifactsMapByUUID;
    }

}
