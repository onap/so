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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
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
import org.w3c.dom.Document;
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
        Optional<String> response = Optional.empty();
        boolean isMulticloud = false;
        try {
            String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
            if (xmlRequest != null) {
                Optional<String> requestType = findRequestType(xmlRequest);
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    CreateVolumeGroupRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
                    isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    response = determineCreateVolumeGroupStatus(req, externalTask, success, isMulticloud);
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    CreateVfModuleRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
                    isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    response = determineCreateVfModuleStatus(req, externalTask, success, isMulticloud);
                } else if ("deleteVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Vf Module");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteVfModuleRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVfModuleRequest.class);
                    isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), false);
                        StackInfo stack = pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(),
                                stackId, success);
                        DeleteVfModuleResponse deleteResponse =
                                new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(), Boolean.TRUE,
                                        req.getMessageId(), vnfAdapterImpl.copyStringOutputs(stack.getOutputs()));
                        response = Optional.of(deleteResponse.toXmlString());
                    }
                } else if ("deleteVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Volume Group");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteVolumeGroupRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), DeleteVolumeGroupRequest.class);
                    isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
                    if (!isMulticloud) {
                        pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                        DeleteVolumeGroupResponse deleteResponse =
                                new DeleteVolumeGroupResponse(true, req.getMessageId());
                        response = Optional.of(deleteResponse.toXmlString());
                    }
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    CreateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateNetworkRequest.class);
                    response = determineCreateNetworkStatus(req, externalTask, success);
                } else if ("deleteNetworkRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Delete Network");
                    String stackId = externalTask.getVariable("stackId");
                    DeleteNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteNetworkRequest.class);
                    pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                    DeleteNetworkResponse deleteResponse =
                            new DeleteNetworkResponse(req.getNetworkId(), true, req.getMessageId());
                    response = Optional.of(deleteResponse.toXmlString());
                } else if ("updateNetworkRequest".equals(requestType.get())) {
                    UpdateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), UpdateNetworkRequest.class);
                    pollUpdateResource(req.getCloudSiteId(), req.getTenantId(), externalTask, success);
                    UpdateNetworkResponse updateResponse =
                            new UpdateNetworkResponse(req.getNetworkId(), null, null, req.getMessageId());
                    response = Optional.of(updateResponse.toXmlString());
                } else if ("rollbackNetworkRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Poll Service for Rollback Network");
                    String stackId = externalTask.getVariable("stackId");
                    RollbackNetworkRequest req =
                            JAXB.unmarshal(new StringReader(xmlRequest), RollbackNetworkRequest.class);
                    pollDeleteResource(118, req.getNetworkRollback().getCloudId(),
                            req.getNetworkRollback().getTenantId(), stackId, success);
                }
            }
        } catch (Exception e) {
            logger.error("Error during External Task Poll Service", e);
            errorMessage = e.toString();
            variables.put("openstackAdapterErrorMessage", errorMessage);
        }

        if (isMulticloud) {
            success.setTrue();
        }
        if (response.isPresent()) {
            variables.put("WorkflowResponse", response.get());
        }
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

    private Optional<String> determineCreateVolumeGroupStatus(CreateVolumeGroupRequest req, ExternalTask externalTask,
            MutableBoolean success, boolean isMulticloud) throws MsoException {
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        String stackId = externalTask.getVariable("stackId");
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Volume Group");
            if (!isMulticloud) {
                pollDeleteResource(118, req.getCloudSiteId(), req.getTenantId(), stackId, success);
            }
            return Optional.empty();
        } else {
            logger.debug("Executing External Task Poll Service for Create Volume Group");
            Map<String, String> outputs = new HashMap<String, String>();
            if (!isMulticloud) {
                int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), true);
                StackInfo stack =
                        pollCreateResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                outputs = vnfAdapterImpl.copyStringOutputs(stack.getOutputs());
            }
            VolumeGroupRollback rb = new VolumeGroupRollback(req.getVolumeGroupId(), stackId, true, req.getTenantId(),
                    req.getCloudOwner(), req.getCloudSiteId(), req.getMsoRequest(), req.getMessageId());
            CreateVolumeGroupResponse createResponse = new CreateVolumeGroupResponse(req.getVolumeGroupId(), stackId,
                    true, outputs, rb, req.getMessageId());
            return Optional.of(createResponse.toXmlString());
        }
    }

    private Optional<String> determineCreateVfModuleStatus(CreateVfModuleRequest req, ExternalTask externalTask,
            MutableBoolean success, boolean isMulticloud) throws MsoException {
        String stackId = externalTask.getVariable("stackId");
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        int timeoutMinutes = msoHeatUtils.getVfHeatTimeoutValue(req.getModelCustomizationUuid(), false);
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Vf Module");
            if (!isMulticloud) {
                pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
            }
            return Optional.empty();
        } else {
            logger.debug("Executing External Task Poll Service for Create Vf Module");
            Map<String, String> outputs = new HashMap<String, String>();
            if (!isMulticloud) {
                StackInfo stack =
                        pollCreateResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
                outputs = vnfAdapterImpl.copyStringOutputs(stack.getOutputs());

            }
            VfModuleRollback modRollback = new VfModuleRollback(buildVnfRollback(req, stackId, isMulticloud),
                    req.getVfModuleId(), stackId, req.getMessageId());
            CreateVfModuleResponse createResponse = new CreateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
                    stackId, Boolean.TRUE, outputs, modRollback, req.getMessageId());
            return Optional.of(createResponse.toXmlString());
        }
    }

    private Optional<String> determineCreateNetworkStatus(CreateNetworkRequest req, ExternalTask externalTask,
            MutableBoolean success) throws MsoException {
        String stackId = externalTask.getVariable("stackId");
        boolean pollRollbackStatus = externalTask.getVariable("PollRollbackStatus");
        int timeoutMinutes =
                msoHeatUtils.getNetworkHeatTimeoutValue(req.getModelCustomizationUuid(), req.getNetworkType());
        if (pollRollbackStatus) {
            logger.debug("Executing External Task Poll Service for Rollback Network");
            pollDeleteResource(timeoutMinutes, req.getCloudSiteId(), req.getTenantId(), stackId, success);
            return Optional.empty();
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
            return Optional.of(response.toXmlString());

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

    protected Optional<String> findRequestType(final String xmlString) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);

            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
            return Optional.of(doc.getDocumentElement().getNodeName());
        } catch (final Exception e) {
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
