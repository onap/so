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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.adapters.nwrest.ContrailNetwork;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.ProviderVlanNetwork;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.MsoVnfPluginAdapterImpl;
import org.onap.so.adapters.vnf.VnfAdapterUtils;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.utils.ExternalTaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class StackService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(StackService.class);

    @Autowired
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Autowired
    private MsoNetworkAdapterImpl networkAdapterImpl;

    @Autowired
    private MsoVnfPluginAdapterImpl vnfPluginImpl;

    @Autowired
    private VnfAdapterUtils vnfAdapterUtils;

    @Autowired
    private AuditMDCSetup mdcSetup;

    private static final String SHARED = "shared";
    private static final String EXTERNAL = "external";

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        mdcSetup.setupMDC(externalTask);
        String xmlRequest = externalTask.getVariable("openstackAdapterTaskRequest");
        logger.debug("Starting External Task Stack Service. {}", xmlRequest);
        MutableBoolean success = new MutableBoolean();
        MutableBoolean backout = new MutableBoolean();
        MutableBoolean os3Nw = new MutableBoolean();
        jakarta.xml.ws.Holder<String> canonicalStackId = new Holder<>();
        String errorMessage = "";
        try {
            if (xmlRequest != null) {
                Optional<String> requestType = findRequestType(xmlRequest);
                if ("createVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Create Volume Group");
                    createVolumeGroup(xmlRequest, canonicalStackId, backout, success);
                } else if ("createVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Create Vf Module");
                    createVfModule(xmlRequest, canonicalStackId, backout, success);
                } else if ("deleteVfModuleRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Delete Vf Module");
                    deleteVfModule(xmlRequest, canonicalStackId, backout, success);
                } else if ("deleteVolumeGroupRequest".equals(requestType.get())) {
                    logger.debug("Executing External Task Stack Service For Delete Volume Group");
                    deleteVolumeGroup(xmlRequest, canonicalStackId, backout, success);
                } else if ("createNetworkRequest".equals(requestType.get())) {
                    createNetwork(xmlRequest, canonicalStackId, backout, success, os3Nw);
                } else if ("deleteNetworkRequest".equals(requestType.get())) {
                    deleteNetwork(xmlRequest, canonicalStackId, backout, success);
                } else if ("updateNetworkRequest".equals(requestType.get())) {
                    updateNetwork(xmlRequest, canonicalStackId, backout, success);
                } else if ("rollbackNetworkRequest".equals(requestType.get())) {
                    rollbackNetwork(xmlRequest, canonicalStackId, backout, success);
                }
            }
        } catch (Exception e) {
            logger.error("Error during External Task Stack Service", e);
            errorMessage = e.getMessage();
        }
        variables.put("backout", backout.booleanValue());
        variables.put("OpenstackInvokeSuccess", success.booleanValue());
        variables.put("stackId", canonicalStackId.value);
        variables.put("openstackAdapterErrorMessage", errorMessage);
        variables.put("PollRollbackStatus", false);
        variables.put("rollbackPerformed", false);
        variables.put("OpenstackRollbackSuccess", false);
        variables.put("OpenstackPollSuccess", false);
        variables.put("os3Nw", os3Nw.booleanValue());

        if (success.isTrue()) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task Id: {}  Successful", externalTask.getId());
        } else {
            logger.debug("The External Task Id: {}  Failed. Not Retrying", externalTask.getId());
            externalTaskService.complete(externalTask, variables);
        }
    }

    private void createVolumeGroup(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws VnfException {
        CreateVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVolumeGroupRequest.class);
        String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
        boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
        if (isMulticloud) {
            vnfPluginImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    completeVnfVfModuleType, req.getVnfVersion(), "", req.getVolumeGroupName(), "", "VOLUME", null,
                    null, req.getModelCustomizationUuid(), req.getVolumeGroupParams(), false, true,
                    req.getEnableBridge(), req.getMsoRequest(), canonicalStackId);
        } else {
            vnfAdapterImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    completeVnfVfModuleType, req.getVnfVersion(), "", req.getVolumeGroupName(), "", "VOLUME", null,
                    null, req.getModelCustomizationUuid(), req.getVolumeGroupParams(), false, true,
                    req.getEnableBridge(), req.getMsoRequest(), canonicalStackId);
        }
        success.setTrue();
        backout.setValue(!req.getSuppressBackout());
    }

    private void createVfModule(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws VnfException {
        CreateVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), CreateVfModuleRequest.class);
        String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
        boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
        if (isMulticloud) {
            vnfPluginImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    completeVnfVfModuleType, req.getVnfVersion(), req.getVnfId(), req.getVfModuleName(),
                    req.getVfModuleId(), req.getRequestType(), req.getVolumeGroupStackId(),
                    req.getBaseVfModuleStackId(), req.getModelCustomizationUuid(), req.getVfModuleParams(), false,
                    false, req.getEnableBridge(), req.getMsoRequest(), canonicalStackId);
        } else {
            vnfAdapterImpl.createVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    completeVnfVfModuleType, req.getVnfVersion(), req.getVnfId(), req.getVfModuleName(),
                    req.getVfModuleId(), req.getRequestType(), req.getVolumeGroupStackId(),
                    req.getBaseVfModuleStackId(), req.getModelCustomizationUuid(), req.getVfModuleParams(), false,
                    false, req.getEnableBridge(), req.getMsoRequest(), canonicalStackId);
        }
        success.setTrue();
        backout.setValue(req.getBackout());
    }

    private void deleteVfModule(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws VnfException {
        backout.setFalse();
        jakarta.xml.ws.Holder<Map<String, String>> outputs = new jakarta.xml.ws.Holder<>();
        DeleteVfModuleRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteVfModuleRequest.class);
        boolean isMulticloud = vnfAdapterUtils.isMulticloudMode(null, req.getCloudSiteId());
        if (isMulticloud) {
            vnfPluginImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    req.getVfModuleStackId(), req.getMsoRequest(), outputs);
        } else {
            vnfAdapterImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                    req.getVfModuleStackId(), req.getModelCustomizationUuid(), req.getMsoRequest(), outputs);
        }
        success.setTrue();
        if (outputs != null && outputs.value != null) {
            canonicalStackId.value = outputs.value.get("canonicalStackId");
        } else {
            canonicalStackId.value = req.getVfModuleStackId();
        }
    }

    private void deleteVolumeGroup(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws VnfException {
        backout.setFalse();
        DeleteVolumeGroupRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteVolumeGroupRequest.class);
        vnfAdapterImpl.deleteVfModule(req.getCloudSiteId(), req.getCloudOwner(), req.getTenantId(),
                req.getVolumeGroupStackId(), null, req.getMsoRequest(), new jakarta.xml.ws.Holder<>());
        success.setTrue();
        canonicalStackId.value = req.getVolumeGroupStackId();
    }

    private void createNetwork(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success, MutableBoolean os3) throws NetworkException {

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
                canonicalStackId, os3);
        success.setTrue();
        backout.setValue(req.getBackout());
    }

    private void deleteNetwork(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws NetworkException {
        backout.setFalse();
        DeleteNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), DeleteNetworkRequest.class);

        networkAdapterImpl.deleteNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getMsoRequest());

        canonicalStackId.value = req.getNetworkStackId();
        success.setTrue();
    }

    private void rollbackNetwork(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws NetworkException {
        backout.setFalse();
        RollbackNetworkRequest req = JAXB.unmarshal(new StringReader(xmlRequest), RollbackNetworkRequest.class);

        NetworkRollback rollback = req.getNetworkRollback();
        networkAdapterImpl.rollbackNetwork(rollback);

        canonicalStackId.value = rollback.getNetworkStackId();
        success.setTrue();
    }

    private void updateNetwork(String xmlRequest, jakarta.xml.ws.Holder<String> canonicalStackId,
            MutableBoolean backout, MutableBoolean success) throws NetworkException {
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
                canonicalStackId);

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
}
