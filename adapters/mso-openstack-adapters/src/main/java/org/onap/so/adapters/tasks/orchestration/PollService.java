/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.tasks.orchestration;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.utils.ExternalTaskUtils;
import org.onap.so.utils.RetrySequenceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.woorea.openstack.heat.model.Stack;

@Component
public class PollService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(PollService.class);

    @Autowired
    private MsoHeatUtils msoHeatUtils;

    @Autowired
    private AuditMDCSetup mdcSetup;

    public PollService() {
        super(RetrySequenceLevel.SHORT);
    }

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        logger.trace("Executing External Task Poll Service");
        Map<String, Object> variables = new HashMap<>();
        MutableBoolean success = new MutableBoolean();
        String errorMessage = null;
        try {
            String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
            if (xmlRequest != null) {
                Optional<String> requestType = findRequestType(xmlRequest);
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    determineCreateVolumeGroupStatus(xmlRequest, externalTask, success);
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    determineCreateVfModuleStatus(xmlRequest, externalTask, success);
                } else if ("deleteVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Vf Module");
                    DeleteVfModuleRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVfModuleRequest.class);
                    pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                } else if ("deleteVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Volume Group");
                    DeleteVolumeGroupRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVolumeGroupRequest.class);
                    pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    determineCreateNetworkStatus(xmlRequest, externalTask, success);
                } else if ("deleteNetworkRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Network");
                    DeleteNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteNetworkRequest.class);
                    pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                } else if ("updateNetworkRequest".equals(requestType.get())) {
                    UpdateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), UpdateNetworkRequest.class);
                    pollUpdateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                }
            }
        } catch (Exception e) {
            logger.error("Error during External Task Poll Service", e);
            errorMessage = e.getMessage();
        }

        variables.put("OpenstackPollSuccess", success.booleanValue());
        variables.put("openstackAdapterErrorMessage", errorMessage);
        if (success.isTrue()) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task Id: {}  Successful", externalTask.getId());
        } else {
            if (externalTask.getRetries() == null) {
                logger.debug("The External Task Id: {}  Failed, Setting Retries to Default Start Value: {}",
                        externalTask.getId(), getRetrySequence().length);
                externalTaskService.handleFailure(externalTask, "errorMessage", "errorDetails",
                        getRetrySequence().length, 10000);
            } else if (externalTask.getRetries() != null && externalTask.getRetries() - 1 == 0) {
                logger.debug("The External Task Id: {}  Failed, All Retries Exhausted", externalTask.getId());
                externalTaskService.complete(externalTask, variables);
            } else {
                logger.debug("The External Task Id: {}  Failed, Decrementing Retries: {} , Retry Delay: {}",
                        externalTask.getId(), externalTask.getRetries() - 1,
                        calculateRetryDelay(externalTask.getRetries()));
                externalTaskService.handleFailure(externalTask, "errorMessage", "errorDetails",
                        externalTask.getRetries() - 1, calculateRetryDelay(externalTask.getRetries()));
            }
        }
    }

    private void determineCreateVolumeGroupStatus(String xmlRequest, ExternalTask externalTask, MutableBoolean success)
            throws MsoException {
        CreateVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Create Volume Group");
            pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        } else {
            pollCreateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        }
    }

    private void determineCreateVfModuleStatus(String xmlRequest, ExternalTask externalTask, MutableBoolean success)
            throws MsoException {
        CreateVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Create Vf Module");
            pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        } else {
            logger.debug("Executing External Task Poll Service for Create Vf Module");
            pollCreateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        }
    }

    private void determineCreateNetworkStatus(String xmlRequest, ExternalTask externalTask, MutableBoolean success)
            throws MsoException {
        CreateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateNetworkRequest.class);
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Create Network");
            pollDeleteResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        } else {
            logger.debug("Executing External Task Poll Service for Create Network");
            pollCreateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
        }
    }

    private void pollCreateResource(String cloudSiteId, String tenantId, ExternalTask externalTask,
            MutableBoolean success) throws MsoException {
        Stack currentStack = createCurrentStack(externalTask.getVariable("stackId"));
        Stack stack =
                msoHeatUtils.pollStackForStatus(1, currentStack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        msoHeatUtils.postProcessStackCreate(stack, false, 0, false, cloudSiteId, tenantId, null);
        success.setTrue();
    }

    private void pollDeleteResource(String cloudSiteId, String tenantId, ExternalTask externalTask,
            MutableBoolean success) throws MsoException {
        Stack currentStack = createCurrentStack(externalTask.getVariable("stackId"));
        Stack stack =
                msoHeatUtils.pollStackForStatus(1, currentStack, "DELETE_IN_PROGRESS", cloudSiteId, tenantId, true);
        if (stack != null) { // if stack is null it was not found and no need to do post process
            msoHeatUtils.postProcessStackDelete(stack);
        }
        success.setTrue();
    }

    private void pollUpdateResource(String cloudSiteId, String tenantId, ExternalTask externalTask,
            MutableBoolean success) throws MsoException {
        Stack currentStack = createCurrentStack(externalTask.getVariable("stackId"));
        Stack stack =
                msoHeatUtils.pollStackForStatus(1, currentStack, "UPDATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        msoHeatUtils.postProcessStackUpdate(stack);
        success.setTrue();
    }

    protected Optional<String> findRequestType(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc;
            doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
            return Optional.of(doc.getDocumentElement().getNodeName());
        } catch (Exception e) {
            logger.error("Error Finding Request Type", e);
            return Optional.empty();
        }
    }

    private Stack createCurrentStack(String canonicalStackId) {
        Stack currentStack = new Stack();
        String stackName = canonicalStackId;
        if (canonicalStackId.contains("/")) {
            String[] stacks = canonicalStackId.split("/");
            stackName = stacks[0];
            currentStack.setId(stacks[1]);
        }
        currentStack.setStackName(stackName);
        return currentStack;
    }

}
