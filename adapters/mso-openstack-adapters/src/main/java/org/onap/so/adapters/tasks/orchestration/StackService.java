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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.adapters.nwrest.ContrailNetwork;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.ProviderVlanNetwork;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.exceptions.VnfException;
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
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.utils.ExternalTaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StackService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(StackService.class);

    @Autowired
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Autowired
    private MsoNetworkAdapterImpl networkAdapterImpl;

    @Autowired
    private AuditMDCSetup mdcSetup;

    private static final String SHARED = "shared";
    private static final String EXTERNAL = "external";

    // TODO set backout earlier in case of exception??
    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        mdcSetup.setupMDC(externalTask);
        String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
        logger.debug("Executing External Task Stack Service. {}", xmlRequest);
        MutableBoolean success = new MutableBoolean();
        MutableBoolean backout = new MutableBoolean();
        String response = "";
        Holder<String> canonicalStackId = new Holder<>();
        String errorMessage = "";
        try {
            if (xmlRequest != null) {
                Holder<Map<String, String>> outputs = new Holder<>();
                Holder<VnfRollback> vnfRollback = new Holder<>();
                Optional<String> requestType = findRequestType(xmlRequest);
                Holder<String> networkId = new Holder<>();
                Holder<String> neutronNetworkId = new Holder<>();
                Holder<String> networkFqdn = new Holder<>();
                Holder<Map<String, String>> subnetIdMap = new Holder<>();
                Holder<NetworkRollback> networkRollback = new Holder<>();
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Create Volume Group");
                    response = createVolumeGroup(xmlRequest, outputs, vnfRollback, canonicalStackId, backout, success);
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Create Vf Module");
                    response = createVfModule(xmlRequest, outputs, vnfRollback, canonicalStackId, backout, success);
                } else if ("deleteVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Delete Vf Module");
                    response = deleteVfModule(xmlRequest, outputs, vnfRollback, canonicalStackId, backout, success);
                } else if ("deleteVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Delete Volume Group");
                    response = deleteVolumeGroup(xmlRequest, outputs, vnfRollback, canonicalStackId, backout, success);
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    response = createNetwork(xmlRequest, networkId, neutronNetworkId, networkFqdn, subnetIdMap,
                            networkRollback, canonicalStackId, backout, success);
                } else if ("deleteNetworkRequest".equals(requestType.get())) {
                    response = deleteNetwork(xmlRequest, canonicalStackId, backout, success);
                } else if ("updateNetworkRequest".equals(requestType.get())) {
                    response =
                            updateNetwork(xmlRequest, subnetIdMap, networkRollback, canonicalStackId, backout, success);
                } else if ("rollbackNetworkRequest".equals(requestType.get())) {
                    response = rollbackNetwork(xmlRequest, canonicalStackId, backout, success);
                }
            }
        } catch (Exception e) {
            logger.error("Error during External Task Stack Service", e);
            errorMessage = e.getMessage();
        }
        variables.put("backout", backout.booleanValue());
        variables.put("WorkflowResponse", response);
        variables.put("OpenstackInvokeSuccess", success.booleanValue());
        variables.put("stackId", canonicalStackId.value);
        variables.put("openstackAdapterErrorMessage", errorMessage);
        variables.put("PollRollbackStatus", false);
        variables.put("rollbackPerformed", false);
        variables.put("OpenstackRollbackSuccess", false);
        variables.put("OpenstackPollSuccess", false);

        if (success.isTrue()) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task Id: {}  Successful", externalTask.getId());
        } else {
            logger.debug("The External Task Id: {}  Failed. Not Retrying", externalTask.getId());
            externalTaskService.complete(externalTask, variables);
        }
    }

    private String createVolumeGroup(String xmlRequest, Holder<Map<String, String>> outputs,
            Holder<VnfRollback> vnfRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws VnfException {
        Holder<String> stackId = new Holder<>();
        CreateVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
        String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
        vnfAdapterImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                completeVnfVfModuleType, req.getVnfVersion(), "", req.getVolumeGroupName(), "", "VOLUME", null, null,
                req.getModelCustomizationUuid(), req.getVolumeGroupParams(), false, true, req.getEnableBridge(),
                req.getMsoRequest(), stackId, outputs, vnfRollback);
        success.setTrue();
        backout.setValue(!req.getSuppressBackout());
        VolumeGroupRollback rb = new VolumeGroupRollback(req.getVolumeGroupId(), stackId.value,
                vnfRollback.value.getVnfCreated(), req.getTenantId(), req.getCloudOwner(), req.getCloudSiteId(),
                req.getMsoRequest(), req.getMessageId());
        canonicalStackId.value = stackId.value;
        CreateVolumeGroupResponse createResponse = new CreateVolumeGroupResponse(req.getVolumeGroupId(), stackId.value,
                vnfRollback.value.getVnfCreated(), outputs.value, rb, req.getMessageId());
        return createResponse.toXmlString();
    }

    private String createVfModule(String xmlRequest, Holder<Map<String, String>> outputs,
            Holder<VnfRollback> vnfRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws VnfException {
        Holder<String> stackId = new Holder<>();
        CreateVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
        String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
        vnfAdapterImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                completeVnfVfModuleType, req.getVnfVersion(), req.getVnfId(), req.getVfModuleName(),
                req.getVfModuleId(), req.getRequestType(), req.getVolumeGroupStackId(), req.getBaseVfModuleStackId(),
                req.getModelCustomizationUuid(), req.getVfModuleParams(), false, false, req.getEnableBridge(),
                req.getMsoRequest(), stackId, outputs, vnfRollback);
        success.setTrue();
        backout.setValue(req.getBackout());
        canonicalStackId.value = stackId.value;
        VfModuleRollback modRollback =
                new VfModuleRollback(vnfRollback.value, req.getVfModuleId(), stackId.value, req.getMessageId());
        CreateVfModuleResponse createResponse = new CreateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
                stackId.value, Boolean.TRUE, outputs.value, modRollback, req.getMessageId());
        return createResponse.toXmlString();
    }

    private String deleteVfModule(String xmlRequest, Holder<Map<String, String>> outputs,
            Holder<VnfRollback> vnfRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws VnfException {
        backout.setFalse();
        DeleteVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteVfModuleRequest.class);
        vnfAdapterImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                req.getVfModuleStackId(), req.getVnfId(), req.getVfModuleId(), req.getModelCustomizationUuid(),
                req.getMsoRequest(), outputs);
        success.setTrue();
        if (outputs != null && outputs.value != null) {
            canonicalStackId.value = outputs.value.get("canonicalStackId");
        } else {
            canonicalStackId.value = req.getVfModuleStackId();
        }
        DeleteVfModuleResponse deleteResponse = new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
                Boolean.TRUE, req.getMessageId(), outputs.value);
        return deleteResponse.toXmlString();
    }

    private String deleteVolumeGroup(String xmlRequest, Holder<Map<String, String>> outputs,
            Holder<VnfRollback> vnfRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws VnfException {
        backout.setFalse();
        DeleteVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteVolumeGroupRequest.class);

        vnfAdapterImpl.deleteVnf(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                req.getVolumeGroupStackId(), req.getMsoRequest(), false);
        success.setTrue();
        canonicalStackId.value = req.getVolumeGroupStackId();
        DeleteVolumeGroupResponse deleteResponse = new DeleteVolumeGroupResponse(true, req.getMessageId());
        return deleteResponse.toXmlString();
    }

    private String createNetwork(String xmlRequest, Holder<String> networkId, Holder<String> neutronNetworkId,
            Holder<String> networkFqdn, Holder<Map<String, String>> subnetIdMap,
            Holder<NetworkRollback> networkRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws NetworkException {
        CreateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateNetworkRequest.class);
        HashMap<String, String> params = (HashMap<String, String>) req.getNetworkParams();
        if (params == null) {
            params = new HashMap<>();
        }
        String shared = null;
        String external = null;
        String physicalNetworkName = null;
        List<Integer> vlans = null;
        List<RouteTarget> routeTargets = null;
        List<String> fqdns = null;
        List<String> routeTable = null;
        if (params.containsKey(SHARED))
            shared = params.get(SHARED);
        if (params.containsKey(EXTERNAL))
            external = params.get(EXTERNAL);
        if (req.isContrailRequest()) {
            ContrailNetwork ctn = req.getContrailNetwork();
            if (ctn == null) {
                ctn = new ContrailNetwork();
                req.setContrailNetwork(ctn);
            }
            if (shared == null && ctn.getShared() != null) {
                shared = ctn.getShared();
            }
            if (shared == null && ctn.getExternal() != null) {
                external = ctn.getExternal();
            }
            routeTargets = req.getContrailNetwork().getRouteTargets();
            fqdns = req.getContrailNetwork().getPolicyFqdns();
            routeTable = req.getContrailNetwork().getRouteTableFqdns();
        } else {
            ProviderVlanNetwork pvn = req.getProviderVlanNetwork();
            if (pvn == null) {
                pvn = new ProviderVlanNetwork();
                req.setProviderVlanNetwork(pvn);
            }
            physicalNetworkName = req.getProviderVlanNetwork().getPhysicalNetworkName();
            vlans = req.getProviderVlanNetwork().getVlans();
        }
        networkAdapterImpl.createNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                req.getModelCustomizationUuid(), req.getNetworkName(), physicalNetworkName, vlans, routeTargets, shared,
                external, req.getFailIfExists(), false, req.getSubnets(), fqdns, routeTable, req.getMsoRequest(),
                networkId, neutronNetworkId, networkFqdn, subnetIdMap, networkRollback, false);
        success.setTrue();
        backout.setValue(req.getBackout());
        canonicalStackId.value = networkRollback.value.getNetworkStackId();

        CreateNetworkResponse response = new CreateNetworkResponse(req.getNetworkId(), neutronNetworkId.value,
                networkRollback.value.getNetworkStackId(), networkFqdn.value, networkRollback.value.getNetworkCreated(),
                subnetIdMap.value, networkRollback.value, req.getMessageId());
        return response.toXmlString();
    }

    private String deleteNetwork(String xmlRequest, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws NetworkException {
        backout.setFalse();
        DeleteNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteNetworkRequest.class);
        Holder<Boolean> networkDeleted = new Holder<>();

        networkAdapterImpl.deleteNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getMsoRequest(), networkDeleted, false);

        canonicalStackId.value = req.getNetworkStackId();
        success.setTrue();

        DeleteNetworkResponse response =
                new DeleteNetworkResponse(req.getNetworkId(), networkDeleted.value, req.getMessageId());
        return response.toXmlString();
    }

    private String rollbackNetwork(String xmlRequest, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws NetworkException {
        backout.setFalse();
        RollbackNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), RollbackNetworkRequest.class);

        NetworkRollback rollback = req.getNetworkRollback();
        networkAdapterImpl.rollbackNetwork(rollback, false);

        canonicalStackId.value = rollback.getNetworkStackId();
        success.setTrue();

        RollbackNetworkResponse response = new RollbackNetworkResponse(true, req.getMessageId());
        return response.toXmlString();
    }

    private String updateNetwork(String xmlRequest, Holder<Map<String, String>> subnetIdMap,
            Holder<NetworkRollback> networkRollback, Holder<String> canonicalStackId, MutableBoolean backout,
            MutableBoolean success) throws NetworkException {
        backout.setFalse();
        UpdateNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), UpdateNetworkRequest.class);
        HashMap<String, String> params = (HashMap<String, String>) req.getNetworkParams();
        if (params == null) {
            params = new HashMap<>();
        }
        String shared = null;
        String external = null;
        String physicalNetworkName = null;
        List<Integer> vlans = null;
        List<RouteTarget> routeTargets = null;
        List<String> fqdns = null;
        List<String> routeTable = null;
        if (params.containsKey(SHARED))
            shared = params.get(SHARED);
        if (params.containsKey(EXTERNAL))
            external = params.get(EXTERNAL);
        if (req.isContrailRequest()) {
            ContrailNetwork ctn = req.getContrailNetwork();
            if (ctn == null) {
                ctn = new ContrailNetwork();
                req.setContrailNetwork(ctn);
            }
            if (shared == null && ctn.getShared() != null) {
                shared = ctn.getShared();
            }
            if (shared == null && ctn.getExternal() != null) {
                external = ctn.getExternal();
            }
            routeTargets = req.getContrailNetwork().getRouteTargets();
            fqdns = req.getContrailNetwork().getPolicyFqdns();
            routeTable = req.getContrailNetwork().getRouteTableFqdns();
        } else {
            ProviderVlanNetwork pvn = req.getProviderVlanNetwork();
            if (pvn == null) {
                pvn = new ProviderVlanNetwork();
                req.setProviderVlanNetwork(pvn);
            }
            physicalNetworkName = req.getProviderVlanNetwork().getPhysicalNetworkName();
            vlans = req.getProviderVlanNetwork().getVlans();
        }

        networkAdapterImpl.updateNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getNetworkName(), physicalNetworkName,
                vlans, routeTargets, shared, external, req.getSubnets(), fqdns, routeTable, req.getMsoRequest(),
                subnetIdMap, networkRollback);

        success.setTrue();
        canonicalStackId.value = req.getNetworkStackId();

        UpdateNetworkResponse response =
                new UpdateNetworkResponse(req.getNetworkId(), null, subnetIdMap.value, req.getMessageId());
        return response.toXmlString();
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
}
