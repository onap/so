/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.installer.bpmn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.WorkflowArtifact;
import org.onap.so.db.catalog.beans.ActivitySpec;
import org.onap.so.db.catalog.beans.VnfResourceWorkflow;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.WorkflowActivitySpecSequence;
import org.onap.so.db.catalog.data.repository.ActivitySpecRepository;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowResource {
    protected static final Logger logger = LoggerFactory.getLogger(WorkflowResource.class);

    private static final String pattern = ".*\\\"activity:(.*)\\\" .*";
    private static final String TARGET_RESOURCE_VNF = "vnf";
    private static final String SOURCE_SDC = "sdc";
    private static final String BPMN_SUFFIX = ".bpmn";

    @Autowired
    protected WorkflowRepository workflowRepo;

    @Autowired
    protected ActivitySpecRepository activityRepo;

    @Autowired
    private BpmnInstaller bpmnInstaller;

    public void processWorkflows(VfResourceStructure vfResourceStructure) throws Exception {
        Map<String, WorkflowArtifact> artifactsMapByUUID = vfResourceStructure.getWorkflowArtifactsMapByUUID();
        String vfResourceModelUuid = vfResourceStructure.getResourceInstance().getResourceUUID();
        for (String uuid : artifactsMapByUUID.keySet()) {
            WorkflowArtifact artifactToInstall = artifactsMapByUUID.get(uuid);
            if (isLatestVersionAvailable(artifactsMapByUUID, artifactToInstall)) {
                logger.debug("Installing the BPMN: " + artifactToInstall.getArtifactInfo().getArtifactName());
                deployWorkflowResourceToCamunda(artifactToInstall);
                installWorkflowResource(artifactToInstall, vfResourceModelUuid);
            } else {
                logger.debug("Skipping installing - not the latest version: "
                        + artifactToInstall.getArtifactInfo().getArtifactName());
            }
        }
    }

    protected void deployWorkflowResourceToCamunda(WorkflowArtifact artifact) throws Exception {
        String bpmnName = artifact.getArtifactInfo().getArtifactName();
        String version = artifact.getArtifactInfo().getArtifactVersion();
        logger.debug("BPMN Name: " + bpmnName);
        try {
            HttpResponse response = bpmnInstaller.sendDeploymentRequest(bpmnName, version);
            logger.debug("Response status line: {}", response.getStatusLine());
            logger.debug("Response entity: {}", response.getEntity().toString());
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.debug("Failed deploying BPMN {}", bpmnName);
                logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL.toString(), bpmnName,
                        bpmnName, Integer.toString(response.getStatusLine().getStatusCode()),
                        ErrorCode.DataError.getValue(), "ASDC BPMN deploy failed");
                throw (new Exception("Error from Camunda on deploying the BPMN: " + bpmnName));
            } else {
                logger.debug("Successfully deployed to Camunda: {}", bpmnName);
            }
        } catch (Exception e) {
            logger.debug("Exception :", e);
            logger.error("{} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_NOT_DEPLOYED_DETAIL.toString(), bpmnName,
                    e.getMessage(), ErrorCode.DataError.getValue(), "ASDC BPMN deploy failed");
            throw e;
        }
    }

    protected void installWorkflowResource(WorkflowArtifact artifact, String vfResourceModelUuid) throws Exception {
        IArtifactInfo artifactInfo = artifact.getArtifactInfo();

        Workflow workflow = new Workflow();

        workflow.setArtifactChecksum(artifactInfo.getArtifactChecksum());
        workflow.setArtifactName(artifactInfo.getArtifactName());
        workflow.setArtifactUUID(artifactInfo.getArtifactUUID());
        workflow.setBody(artifact.getResult());
        workflow.setDescription(artifactInfo.getArtifactDescription());
        workflow.setName(getWorkflowNameFromArtifactName(artifactInfo.getArtifactName()));
        workflow.setResourceTarget(TARGET_RESOURCE_VNF);
        workflow.setSource(SOURCE_SDC);
        workflow.setTimeoutMinutes(artifactInfo.getArtifactTimeout());
        workflow.setOperationName(getWorkflowNameFromArtifactName(artifactInfo.getArtifactName()));
        workflow.setVersion(getWorkflowVersionFromArtifactName(artifactInfo.getArtifactName()));

        VnfResourceWorkflow vnfResourceWorkflow = new VnfResourceWorkflow();
        vnfResourceWorkflow.setVnfResourceModelUUID(vfResourceModelUuid);
        List<VnfResourceWorkflow> vnfResourceWorkflows = new ArrayList<VnfResourceWorkflow>();
        vnfResourceWorkflows.add(vnfResourceWorkflow);

        workflow.setVnfResourceWorkflow(vnfResourceWorkflows);

        List<String> activityNames = getActivityNameList(artifact.getResult());
        List<WorkflowActivitySpecSequence> wfss = getWorkflowActivitySpecSequence(activityNames);
        workflow.setWorkflowActivitySpecSequence(wfss);

        workflowRepo.save(workflow);

    }

    protected boolean isLatestVersionAvailable(Map<String, WorkflowArtifact> artifactsMapByUUID,
            WorkflowArtifact artifact) {
        String workflowName = getWorkflowNameFromArtifactName(artifact.getArtifactInfo().getArtifactName());
        Double workflowVersion = getWorkflowVersionFromArtifactName(artifact.getArtifactInfo().getArtifactName());
        if (workflowVersion == null) {
            workflowVersion = 0.0;
        }
        for (WorkflowArtifact artifactInMap : artifactsMapByUUID.values()) {
            Double versionInMap = getWorkflowVersionFromArtifactName(artifactInMap.getArtifactInfo().getArtifactName());
            if (versionInMap == null) {
                versionInMap = 0.0;
            }
            if (workflowName.equals(getWorkflowNameFromArtifactName(artifactInMap.getArtifactInfo().getArtifactName()))
                    && Double.compare(workflowVersion, versionInMap) < 0) {
                return false;
            }
        }
        return true;
    }

    protected List<String> getActivityNameList(String bpmnContent) {
        List<String> activityNameList = new ArrayList<String>();

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(bpmnContent);
        while (m.find()) {
            activityNameList.add(m.group(1));
        }
        return activityNameList;
    }

    protected List<WorkflowActivitySpecSequence> getWorkflowActivitySpecSequence(List<String> activityNames)
            throws Exception {
        if (activityNames == null || activityNames.size() == 0) {
            return null;
        }
        List<WorkflowActivitySpecSequence> workflowActivitySpecs = new ArrayList<WorkflowActivitySpecSequence>();
        for (String activityName : activityNames) {
            ActivitySpec activitySpec = activityRepo.findByName(activityName);
            if (activitySpec != null) {
                WorkflowActivitySpecSequence workflowActivitySpec = new WorkflowActivitySpecSequence();
                workflowActivitySpec.setActivitySpec(activitySpec);
                workflowActivitySpecs.add(workflowActivitySpec);
            }
        }
        return workflowActivitySpecs;
    }

    public String getWorkflowNameFromArtifactName(String artifactName) {
        if (artifactName == null) {
            return null;
        } else {
            if (artifactName.contains(BPMN_SUFFIX)) {
                return artifactName.substring(0, artifactName.lastIndexOf(BPMN_SUFFIX)).split("-")[0];
            } else {
                return artifactName.split("-")[0];
            }
        }
    }

    public Double getWorkflowVersionFromArtifactName(String artifactName) {
        if (artifactName == null) {
            return null;
        } else {
            String[] workflowNameParts = null;
            if (artifactName.contains(BPMN_SUFFIX)) {
                workflowNameParts = artifactName.substring(0, artifactName.lastIndexOf(BPMN_SUFFIX)).split("-");
            } else {
                workflowNameParts = artifactName.split("-");
            }
            if (workflowNameParts.length < 2) {
                return null;
            } else {
                return Double.valueOf(workflowNameParts[1].replaceAll("_", "."));
            }
        }
    }
}
