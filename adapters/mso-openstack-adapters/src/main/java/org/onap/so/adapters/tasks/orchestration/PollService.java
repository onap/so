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
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.VnfAdapterUtils;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.VfModuleRollback;
import org.onap.so.adapters.vnfrest.VolumeGroupRollback;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.mappers.StackInfoMapper;
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
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Autowired
    private MsoNetworkAdapterImpl networkAdapterImpl;

    @Autowired
    private MsoHeatUtils msoHeatUtils;

    @Autowired
    private VnfAdapterUtils vnfAdapterUtils;

    @Autowired
    private AuditMDCSetup mdcSetup;

    public PollService() {
        super(RetrySequenceLevel.SHORT);
    }

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        logger.debug("Starting External Task Poll Service");
        Map<String, Object> variables = new HashMap<>();
        MutableBoolean success = new MutableBoolean();
        String errorMessage = null;
        String response = "";
        try {
            String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
            if (xmlRequest != null) {
                Optional<String> requestType = findRequestType(xmlRequest);
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    response = determineCreateVolumeGroupStatus(xmlRequest, externalTask, success);
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    response = determineCreateVfModuleStatus(xmlRequest, externalTask, success);
                } else if ("deleteVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Vf Module");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteVfModuleRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVfModuleRequest.class);
                    boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), false);
                        StackInfo stack = pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(),
                                stackId, success);
                        DeleteVfModuleResponse deleteResponse =
                                new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(), Boolean.TRUE,
                                        req.getMessageId(), vnfAdapterImpl.copyStringOutputs(stack.getOutputs()));
                        response = deleteResponse.toXmlString();
                    }
                } else if ("deleteVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Volume Group");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteVolumeGroupRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVolumeGroupRequest.class);
                    boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                        DeleteVolumeGroupResponse deleteResponse =
                                new DeleteVolumeGroupResponse(true, req.getMessageId());
                        response = deleteResponse.toXmlString();
                    } else {
                        success.setTrue();
                    }
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    response = determineCreateNetworkStatus(xmlRequest, externalTask, success);
                } else if ("deleteNetworkRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Network");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteNetworkRequest.class);
                    pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                    DeleteNetworkResponse deleteResponse =
                            new DeleteNetworkResponse(req.getNetworkId(), true, req.getMessageId());
                    response = deleteResponse.toXmlString();
                } else if ("updateNetworkRequest".equals(requestType.get())) {
                    UpdateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), UpdateNetworkRequest.class);
                    pollUpdateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                    UpdateNetworkResponse updateResponse =
                            new UpdateNetworkResponse(req.getNetworkId(), null, null, req.getMessageId());
                    response = updateResponse.toXmlString();
                }
            }
        } catch (

        Exception e) {
            logger.error("Error during External Task Poll Service", e);
            errorMessage = e.toString();
            variables.put("openstackAdapterErrorMessage", errorMessage);
        }

        variables.put("WorkflowResponse", response);
        variables.put("OpenstackPollSuccess", success.booleanValue());
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

    private String determineCreateVolumeGroupStatus(String xmlRequest, ExternalTask externalTask,
            MutableBoolean success) throws MsoException {
        CreateVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
        boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
        if (!isMulticloud) {
            boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
            String stackId = externalTask.getVariable("stackId");
            if (pollRollbackStatus) {
                logger.debug("Executing External Task Poll Service for Rollback Create Volume Group");
                pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                DeleteVolumeGroupResponse deleteResponse = new DeleteVolumeGroupResponse(true, req.getMessageId());
                return deleteResponse.toXmlString();
            } else {
                int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), true);
                StackInfo stack =
                        pollCreateResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                VolumeGroupRollback rb =
                        new VolumeGroupRollback(req.getVolumeGroupId(), stackId, true, req.getTenantId(),
                                req.getCloudOwner(), req.getCloudSiteId(), req.getMsoRequest(), req.getMessageId());
                CreateVolumeGroupResponse createResponse = new CreateVolumeGroupResponse(req.getVolumeGroupId(),
                        stackId, true, vnfAdapterImpl.copyStringOutputs(stack.getOutputs()), rb, req.getMessageId());
                return createResponse.toXmlString();
            }
        } else {
            success.setTrue();
            return null;
        }
    }

    private String determineCreateVfModuleStatus(String xmlRequest, ExternalTask externalTask, MutableBoolean success)
            throws MsoException {
        CreateVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
        boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
        String stackId = externalTask.getVariable("stackId");
        if (!isMulticloud) {
            boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
            int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), false);
            if (pollRollbackStatus) {
                logger.debug("Executing External Task Poll Service for Rollback Create Vf Module");
                StackInfo stack =
                        pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                DeleteVfModuleResponse deleteResponse = new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
                        Boolean.TRUE, req.getMessageId(), vnfAdapterImpl.copyStringOutputs(stack.getOutputs()));
                return deleteResponse.toXmlString();
            } else {
                logger.debug("Executing External Task Poll Service for Create Vf Module");
                StackInfo stack =
                        pollCreateResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                VfModuleRollback modRollback = new VfModuleRollback(buildVnfRollback(req, stackId, isMulticloud),
                        req.getVfModuleId(), stackId, req.getMessageId());
                CreateVfModuleResponse createResponse =
                        new CreateVfModuleResponse(req.getVnfId(), req.getVfModuleId(), stackId, Boolean.TRUE,
                                vnfAdapterImpl.copyStringOutputs(stack.getOutputs()), modRollback, req.getMessageId());
                return createResponse.toXmlString();
            }
        } else {
            success.setTrue();
            return null;
        }
    }

    private String determineCreateNetworkStatus(String xmlRequest, ExternalTask externalTask, MutableBoolean success)
            throws MsoException {
        CreateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateNetworkRequest.class);
        String stackId = externalTask.getVariable("stackId");
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        int timeoutMinutes =
                msoHeatUtils.getNetworkHeatTimeoutValue(req.getModelCustomizationUuid(), req.getNetworkType());
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Create Network");
            pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
            DeleteNetworkResponse response = new DeleteNetworkResponse(req.getNetworkId(), true, req.getMessageId());
            return response.toXmlString();
        } else {
            logger.debug("Executing External Task Poll Service for Create Network");
            boolean os3Nw = externalTask.getVariable("os3Nw");
            StackInfo stack =
                    pollCreateResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
            String networkFqdn = "";
            String neutronNetworkId = "";
            Map<String, String> subnetMap = new HashMap<>();
            if (stack.getOutputs() != null) {
                networkFqdn = (String) stack.getOutputs().get("network_fqdn");
                neutronNetworkId = (String) stack.getOutputs().get("network_id");
                subnetMap = networkAdapterImpl.buildSubnetMap(stack.getOutputs(), req.getSubnets(), os3Nw);
            }
            CreateNetworkResponse response = new CreateNetworkResponse(req.getNetworkId(), neutronNetworkId, stackId,
                    networkFqdn, true, subnetMap, buildNetworkRollback(req, stackId), req.getMessageId());
            return response.toXmlString();

        }
    }

    private StackInfo pollCreateResource(int pollingTimeout, String cloudSiteId, String tenantId, String stackId,
            MutableBoolean success) throws MsoException {
        Stack currentStack = createCurrentStack(stackId);
        Stack stack = msoHeatUtils.pollStackForStatus(pollingTimeout, currentStack, "CREATE_IN_PROGRESS", cloudSiteId,
                tenantId, false);
        msoHeatUtils.postProcessStackCreate(stack, false, 0, false, cloudSiteId, tenantId, null);
        success.setTrue();
        return new StackInfoMapper(stack).map();
    }

    private StackInfo pollDeleteResource(int pollingTimeout, String cloudSiteId, String tenantId, String stackId,
            MutableBoolean success) throws MsoException {
        Stack currentStack = createCurrentStack(stackId);
        Stack stack = msoHeatUtils.pollStackForStatus(pollingTimeout, currentStack, "DELETE_IN_PROGRESS", cloudSiteId,
                tenantId, true);
        if (stack != null) { // if stack is null it was not found and no need to do post process
            msoHeatUtils.postProcessStackDelete(stack);
        }
        success.setTrue();
        return new StackInfoMapper(stack).map();
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

    private VnfRollback buildVnfRollback(CreateVfModuleRequest req, String stackId, boolean isMulticloud) {
        VnfRollback vfRollback = new VnfRollback();
        vfRollback.setCloudSiteId(req.getCloudSiteId());
        vfRollback.setCloudOwner(req.getCloudOwner());
        vfRollback.setTenantId(req.getTenantId());
        vfRollback.setMsoRequest(req.getMsoRequest());
        vfRollback.setRequestType(req.getRequestType());
        vfRollback.setVolumeGroupHeatStackId(req.getVolumeGroupStackId());
        vfRollback.setBaseGroupHeatStackId(req.getBaseVfModuleStackId());
        vfRollback.setIsBase(false);
        vfRollback.setModelCustomizationUuid(req.getModelCustomizationUuid());
        vfRollback.setVnfId(stackId);
        vfRollback.setVnfCreated(true);
        if (isMulticloud) {
            vfRollback.setMode("CFY");
        }
        return vfRollback;
    }

    private NetworkRollback buildNetworkRollback(CreateNetworkRequest req, String stackId) {
        NetworkRollback networkRollback = new NetworkRollback();
        networkRollback.setCloudId(req.getCloudSiteId());
        networkRollback.setTenantId(req.getTenantId());
        networkRollback.setMsoRequest(req.getMsoRequest());
        networkRollback.setModelCustomizationUuid(req.getModelCustomizationUuid());
        networkRollback.setNetworkStackId(stackId);
        networkRollback.setNetworkCreated(true);
        networkRollback.setNetworkType(req.getNetworkType());

        return networkRollback;
    }
}
