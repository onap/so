/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import org.onap.so.logger.LoggingAnchor;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.exceptions.MarshallerException;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class VnfAdapterImpl {
    private static final Logger logger = LoggerFactory.getLogger(VnfAdapterImpl.class);
    private static final String CONTRAIL_SERVICE_INSTANCE_FQDN = "contrailServiceInstanceFqdn";
    private static final String OAM_MANAGEMENT_V4_ADDRESS = "oamManagementV4Address";
    private static final String OAM_MANAGEMENT_V6_ADDRESS = "oamManagementV6Address";
    private static final String CONTRAIL_NETWORK_POLICY_FQDN_LIST = "contrailNetworkPolicyFqdnList";
    public static final String HEAT_STACK_ID = "heatStackId";

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    public void preProcessVnfAdapter(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            execution.setVariable("mso-request-id", gBBInput.getRequestContext().getMsoRequestId());
            execution.setVariable("mso-service-instance-id", serviceInstance.getServiceInstanceId());
            execution.setVariable(HEAT_STACK_ID, null);
            execution.setVariable(CONTRAIL_SERVICE_INSTANCE_FQDN, null);
            execution.setVariable(OAM_MANAGEMENT_V4_ADDRESS, null);
            execution.setVariable(OAM_MANAGEMENT_V6_ADDRESS, null);
            execution.setVariable(CONTRAIL_NETWORK_POLICY_FQDN_LIST, null);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void postProcessVnfAdapter(BuildingBlockExecution execution) {
        try {
            String vnfAdapterResponse = execution.getVariable("WorkflowResponse");
            if (!StringUtils.isEmpty(vnfAdapterResponse)) {
                Object vnfRestResponse = unMarshal(vnfAdapterResponse);
                if (vnfRestResponse instanceof CreateVfModuleResponse) {
                    VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                    String heatStackId = ((CreateVfModuleResponse) vnfRestResponse).getVfModuleStackId();
                    if (!StringUtils.isEmpty(heatStackId)) {
                        vfModule.setHeatStackId(heatStackId);
                        execution.setVariable(HEAT_STACK_ID, heatStackId);
                    }
                    Map<String, String> vfModuleOutputs =
                            ((CreateVfModuleResponse) vnfRestResponse).getVfModuleOutputs();
                    if (vfModuleOutputs != null) {
                        processVfModuleOutputs(execution, vfModuleOutputs);
                    }
                } else if (vnfRestResponse instanceof DeleteVfModuleResponse) {
                    VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                    GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                    Boolean vfModuleDelete = ((DeleteVfModuleResponse) vnfRestResponse).getVfModuleDeleted();
                    if (null != vfModuleDelete && vfModuleDelete) {
                        vfModule.setHeatStackId(null);
                        execution.setVariable(HEAT_STACK_ID, null);
                        Map<String, String> vfModuleOutputs =
                                ((DeleteVfModuleResponse) vnfRestResponse).getVfModuleOutputs();
                        if (vfModuleOutputs != null) {
                            processVfModuleOutputs(execution, vfModuleOutputs);
                            if (execution.getVariable(OAM_MANAGEMENT_V4_ADDRESS) != null) {
                                genericVnf.setIpv4OamAddress("");
                                execution.setVariable(OAM_MANAGEMENT_V4_ADDRESS, "");
                            }
                            if (execution.getVariable(OAM_MANAGEMENT_V6_ADDRESS) != null) {
                                genericVnf.setManagementV6Address("");
                                execution.setVariable(OAM_MANAGEMENT_V6_ADDRESS, "");
                            }
                            if (execution.getVariable(CONTRAIL_SERVICE_INSTANCE_FQDN) != null) {
                                vfModule.setContrailServiceInstanceFqdn("");
                                execution.setVariable(CONTRAIL_SERVICE_INSTANCE_FQDN, "");
                            }
                        }
                    }
                } else if (vnfRestResponse instanceof CreateVolumeGroupResponse) {
                    VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
                    String heatStackId = ((CreateVolumeGroupResponse) vnfRestResponse).getVolumeGroupStackId();
                    if (!StringUtils.isEmpty(heatStackId)) {
                        volumeGroup.setHeatStackId(heatStackId);
                        execution.setVariable(HEAT_STACK_ID, heatStackId);
                    } else {
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000,
                                "HeatStackId is missing from create VolumeGroup Vnf Adapter response.");
                    }
                } else if (vnfRestResponse instanceof DeleteVolumeGroupResponse) {
                    VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
                    Boolean volumeGroupDelete = ((DeleteVolumeGroupResponse) vnfRestResponse).getVolumeGroupDeleted();
                    if (null != volumeGroupDelete && volumeGroupDelete) {
                        volumeGroup.setHeatStackId(null);
                        execution.setVariable(HEAT_STACK_ID, null);
                    }
                }
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    private Object unMarshal(String input) throws MarshallerException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            JAXBContext jaxbContext = JAXBContext.newInstance(CreateVfModuleResponse.class,
                    CreateVolumeGroupResponse.class, DeleteVfModuleResponse.class, DeleteVolumeGroupResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputSource inputSource = new InputSource(new StringReader(input));
            SAXSource source = new SAXSource(xmlReader, inputSource);
            return jaxbUnmarshaller.unmarshal(source);
        } catch (Exception e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.GENERAL_EXCEPTION.toString(),
                    ErrorCode.SchemaError.getValue(), e.getMessage(), e);
            throw new MarshallerException("Error parsing VNF Adapter response. " + e.getMessage(),
                    ErrorCode.SchemaError.getValue(), e);
        }
    }

    private void processVfModuleOutputs(BuildingBlockExecution execution, Map<String, String> vfModuleOutputs) {
        if (vfModuleOutputs == null) {
            return;
        }
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            List<String> contrailNetworkPolicyFqdnList = new ArrayList<>();
            Iterator<String> keys = vfModuleOutputs.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals("contrail-service-instance-fqdn")) {
                    String contrailServiceInstanceFqdn = vfModuleOutputs.get(key);
                    logger.debug("Obtained contrailServiceInstanceFqdn: {}", contrailServiceInstanceFqdn);
                    vfModule.setContrailServiceInstanceFqdn(contrailServiceInstanceFqdn);
                    execution.setVariable(CONTRAIL_SERVICE_INSTANCE_FQDN, contrailServiceInstanceFqdn);
                } else if (key.endsWith("contrail_network_policy_fqdn")) {
                    String contrailNetworkPolicyFqdn = vfModuleOutputs.get(key);
                    logger.debug("Obtained contrailNetworkPolicyFqdn: {}", contrailNetworkPolicyFqdn);
                    contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn);
                } else if (key.equals("oam_management_v4_address")) {
                    String oamManagementV4Address = vfModuleOutputs.get(key);
                    logger.debug("Obtained oamManagementV4Address: {}", oamManagementV4Address);
                    genericVnf.setIpv4OamAddress(oamManagementV4Address);
                    execution.setVariable(OAM_MANAGEMENT_V4_ADDRESS, oamManagementV4Address);
                } else if (key.equals("oam_management_v6_address")) {
                    String oamManagementV6Address = vfModuleOutputs.get(key);
                    logger.debug("Obtained oamManagementV6Address: {}", oamManagementV6Address);
                    genericVnf.setManagementV6Address(oamManagementV6Address);
                    execution.setVariable(OAM_MANAGEMENT_V6_ADDRESS, oamManagementV6Address);
                }

                if (!contrailNetworkPolicyFqdnList.isEmpty()) {
                    execution.setVariable(CONTRAIL_NETWORK_POLICY_FQDN_LIST,
                            String.join(",", contrailNetworkPolicyFqdnList));
                }
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }

    }
}
