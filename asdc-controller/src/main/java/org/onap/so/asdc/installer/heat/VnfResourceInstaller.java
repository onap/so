/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.asdc.installer.heat;

import static org.onap.so.asdc.installer.heat.ToscaEntityHelper.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleArtifact;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.data.repository.CvnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.data.repository.VnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VnfResourceInstaller {

    private static final Logger logger = LoggerFactory.getLogger(VnfResourceInstaller.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final VnfResourceRepository vnfRepo;
    private final VFModuleRepository vfModuleRepo;
    private final VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo;
    private final VnfcCustomizationRepository vnfcCustomizationRepo;
    private final CvnfcCustomizationRepository cvnfcCustomizationRepo;
    private final InstanceGroupRepository instanceGroupRepo;
    private final ServiceRepository serviceRepo;
    private final HeatArtifactInstaller heatArtifactInstaller;
    private final ConfigurationInstaller configurationInstaller;

    public VnfResourceInstaller(VnfResourceRepository vnfRepo, VFModuleRepository vfModuleRepo,
            VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo,
            VnfcCustomizationRepository vnfcCustomizationRepo, CvnfcCustomizationRepository cvnfcCustomizationRepo,
            InstanceGroupRepository instanceGroupRepo, ServiceRepository serviceRepo,
            HeatArtifactInstaller heatArtifactInstaller, ConfigurationInstaller configurationInstaller) {
        this.vnfRepo = vnfRepo;
        this.vfModuleRepo = vfModuleRepo;
        this.vnfcInstanceGroupCustomizationRepo = vnfcInstanceGroupCustomizationRepo;
        this.vnfcCustomizationRepo = vnfcCustomizationRepo;
        this.cvnfcCustomizationRepo = cvnfcCustomizationRepo;
        this.instanceGroupRepo = instanceGroupRepo;
        this.serviceRepo = serviceRepo;
        this.heatArtifactInstaller = heatArtifactInstaller;
        this.configurationInstaller = configurationInstaller;
    }

    protected void processVfModules(IEntityDetails vfEntityDetails, ToscaResourceStructure toscaResourceStruct,
            VfResourceStructure vfResourceStructure, Service service, Metadata metadata) throws Exception {

        String vfCustomizationCategory =
                vfEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);

        logger.debug("VF Category is: {} ", vfCustomizationCategory);

        String vfCustomizationUUID =
                vfEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

        logger.debug("VFCustomizationUUID= {}", vfCustomizationUUID);

        IResourceInstance vfNotificationResource = vfResourceStructure.getResourceInstance();

        // Make sure the VF ResourceCustomizationUUID from the notification and tosca customizations match before
        // comparing their VF Modules UUID's
        logger.debug(
                "Checking if Notification VF ResourceCustomizationUUID: {} matches Tosca VF Customization UUID: {}",
                vfNotificationResource.getResourceCustomizationUUID(), vfCustomizationUUID);

        if (vfCustomizationUUID.equals(vfNotificationResource.getResourceCustomizationUUID())) {

            logger.debug("vfCustomizationUUID: {}  matches vfNotificationResource CustomizationUUID ",
                    vfCustomizationUUID);

            VnfResourceCustomization vnfResource = createVnfResource(vfEntityDetails, toscaResourceStruct, service);

            if (vfResourceStructure.getVfModuleStructure() != null
                    && !vfResourceStructure.getVfModuleStructure().isEmpty()) {
                Set<CvnfcCustomization> existingCvnfcSet = new HashSet<>();
                Set<VnfcCustomization> existingVnfcSet = new HashSet<>();
                List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom = new ArrayList<>();

                for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {

                    logger.debug("vfModuleStructure: {}", vfModuleStructure);

                    List<IEntityDetails> vfModuleEntityList =
                            getEntityDetails(toscaResourceStruct,
                                    EntityQuery.newBuilder("org.openecomp.groups.VfModule"), TopologyTemplateQuery
                                            .newBuilder(SdcTypes.SERVICE).customizationUUID(vfCustomizationUUID),
                                    false);

                    IVfModuleData vfMetadata = vfModuleStructure.getVfModuleMetadata();

                    logger.debug("Comparing Vf_Modules_Metadata CustomizationUUID : {}",
                            vfMetadata.getVfModuleModelCustomizationUUID());

                    Optional<IEntityDetails> matchingObject = vfModuleEntityList.stream()
                            .peek(group -> logger.debug("To Csar Group VFModuleModelCustomizationUUID {}",
                                    group.getMetadata().getValue("vfModuleModelCustomizationUUID")))
                            .filter(group -> group.getMetadata().getValue("vfModuleModelCustomizationUUID")
                                    .equals(vfMetadata.getVfModuleModelCustomizationUUID()))
                            .findFirst();
                    if (matchingObject.isPresent()) {
                        VfModuleCustomization vfModuleCustomization = createVFModuleResource(matchingObject.get(),
                                toscaResourceStruct, vfResourceStructure, vfMetadata, vnfResource, service,
                                existingCvnfcSet, existingVnfcSet, existingCvnfcConfigurationCustom);
                        vfModuleCustomization.getVfModule().setVnfResources(vnfResource.getVnfResources());
                    } else
                        throw new Exception(
                                "Cannot find matching VFModule Customization in Csar for Vf_Modules_Metadata: "
                                        + vfMetadata.getVfModuleModelCustomizationUUID());

                }
            }

            // Check for VNFC Instance Group info and add it if there is
            List<IEntityDetails> vfcEntityList = getEntityDetails(toscaResourceStruct,
                    EntityQuery.newBuilder("org.openecomp.groups.VfcInstanceGroup"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.VF).customizationUUID(vfCustomizationUUID), false);

            Set<VnfcCustomization> existingVnfcGroupSet = new HashSet<>();

            for (IEntityDetails groupEntity : vfcEntityList) {
                VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = createVNFCInstanceGroup(groupEntity,
                        vfEntityDetails, vnfResource, toscaResourceStruct, existingVnfcGroupSet);
                vnfcInstanceGroupCustomizationRepo.saveAndFlush(vnfcInstanceGroupCustomization);
            }

            List<String> seqResult = processVNFCGroupSequence(toscaResourceStruct, vfcEntityList);
            if (!CollectionUtils.isEmpty(seqResult)) {
                String resultStr = seqResult.stream().collect(Collectors.joining(","));
                vnfResource.setVnfcInstanceGroupOrder(resultStr);
                logger.debug("vnfcGroupOrder result for service uuid {}: {}", service.getModelUUID(), resultStr);
            }
            // add this vnfResource with existing vnfResource for this service
            addVnfCustomization(service, vnfResource);
        } else {
            logger.debug(
                    "Notification VF ResourceCustomizationUUID: {} doesn't match " + "Tosca VF Customization UUID: {}",
                    vfNotificationResource.getResourceCustomizationUUID(), vfCustomizationUUID);
        }
    }

    private List<String> processVNFCGroupSequence(ToscaResourceStructure toscaResourceStructure,
            List<IEntityDetails> groupEntityDetails) {
        if (CollectionUtils.isEmpty(groupEntityDetails)) {
            return Collections.emptyList();
        }

        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();
        List<String> strSequence = new ArrayList<>(groupEntityDetails.size());
        List<IEntityDetails> tempEntityList = new ArrayList<>(groupEntityDetails.size());
        List<IEntityDetails> entities = new ArrayList<>();
        tempEntityList.addAll(groupEntityDetails);

        for (IEntityDetails vnfcEntityDetails : groupEntityDetails) {

            List<IEntityDetails> vnfcMemberNodes = vnfcEntityDetails.getMemberNodes();

            boolean hasRequirements = false;
            for (IEntityDetails vnfcDetails : vnfcMemberNodes) {

                List<RequirementAssignment> requirements = vnfcDetails.getRequirements();

                if (requirements != null && !requirements.isEmpty()) {
                    hasRequirements = true;
                    break;
                }
            }

            if (!hasRequirements) {
                strSequence.add(vnfcEntityDetails.getName());
                tempEntityList.remove(vnfcEntityDetails);
                entities.addAll(vnfcMemberNodes);
            }
        }

        getVNFCGroupSequenceList(strSequence, tempEntityList, entities, iSdcCsarHelper);

        return strSequence;

    }

    private void getVNFCGroupSequenceList(List<String> strSequence, List<IEntityDetails> vnfcGroupDetails,
            List<IEntityDetails> vnfcMemberNodes, ISdcCsarHelper iSdcCsarHelper) {
        if (CollectionUtils.isEmpty(vnfcGroupDetails)) {
            return;
        }

        List<IEntityDetails> tempGroupList = new ArrayList<>();
        tempGroupList.addAll(vnfcGroupDetails);

        for (IEntityDetails vnfcGroup : vnfcGroupDetails) {
            List<IEntityDetails> members = vnfcGroup.getMemberNodes();
            for (IEntityDetails memberNode : members) {
                boolean isAllExists = true;


                List<RequirementAssignment> requirements = memberNode.getRequirements();

                if (requirements == null || requirements.isEmpty()) {
                    continue;
                }


                for (RequirementAssignment rqa : requirements) {
                    String name = rqa.getNodeTemplateName();
                    for (IEntityDetails node : vnfcMemberNodes) {
                        if (name.equals(node.getName())) {
                            break;
                        }
                    }

                    isAllExists = false;
                    break;
                }

                if (isAllExists) {
                    strSequence.add(vnfcGroup.getName());
                    tempGroupList.remove(vnfcGroupDetails);
                    vnfcMemberNodes.addAll(vnfcGroupDetails);
                }
            }

            if (!tempGroupList.isEmpty() && tempGroupList.size() < vnfcGroupDetails.size()) {
                getVNFCGroupSequenceList(strSequence, tempGroupList, vnfcMemberNodes, iSdcCsarHelper);
            }
        }
    }

    protected VnfcCustomization findExistingVfc(Set<VnfcCustomization> vnfcCustomizations, String customizationUUID) {
        VnfcCustomization vnfcCustomization = null;
        for (VnfcCustomization vnfcCustom : vnfcCustomizations) {
            if (vnfcCustom != null && vnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                vnfcCustomization = vnfcCustom;
            }
        }

        if (vnfcCustomization == null)
            vnfcCustomization = vnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);

        return vnfcCustomization;
    }

    protected CvnfcCustomization findExistingCvfc(Set<CvnfcCustomization> cvnfcCustomizations,
            String customizationUUID) {
        CvnfcCustomization cvnfcCustomization = null;
        for (CvnfcCustomization cvnfcCustom : cvnfcCustomizations) {
            if (cvnfcCustom != null && cvnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                cvnfcCustomization = cvnfcCustom;
            }
        }

        if (cvnfcCustomization == null)
            cvnfcCustomization = cvnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);

        return cvnfcCustomization;
    }

    protected VnfcInstanceGroupCustomization createVNFCInstanceGroup(IEntityDetails vfcInstanceEntity,
            IEntityDetails vfEntityDetails, VnfResourceCustomization vnfResourceCustomization,
            ToscaResourceStructure toscaResourceStructure, Set<VnfcCustomization> existingVnfcGroupSet) {

        Metadata instanceMetadata = vfcInstanceEntity.getMetadata();

        InstanceGroup existingInstanceGroup =
                instanceGroupRepo.findByModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

        VFCInstanceGroup vfcInstanceGroup;

        if (existingInstanceGroup == null) {
            // Populate InstanceGroup
            vfcInstanceGroup = new VFCInstanceGroup();
            vfcInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            vfcInstanceGroup
                    .setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            vfcInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            vfcInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
            vfcInstanceGroup.setToscaNodeType(vfcInstanceEntity.getToscaType());
            vfcInstanceGroup.setRole("SUB-INTERFACE"); // Set Role
            vfcInstanceGroup.setType(InstanceGroupType.VNFC); // Set type
        } else {
            vfcInstanceGroup = (VFCInstanceGroup) existingInstanceGroup;
        }

        // Populate VNFCInstanceGroupCustomization
        VnfcInstanceGroupCustomization vfcInstanceGroupCustom = new VnfcInstanceGroupCustomization();

        vfcInstanceGroupCustom.setVnfResourceCust(vnfResourceCustomization);
        vnfResourceCustomization.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);
        vfcInstanceGroup.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setDescription(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        String getInputName = null;

        Map<String, Property> groupProperties = vfcInstanceEntity.getProperties();

        for (String key : groupProperties.keySet()) {
            Property property = groupProperties.get(key);

            String vfcName = property.getName();

            if (vfcName != null) {
                if (vfcName.equals("vfc_instance_group_function")) {

                    String vfcValue = property.getValue().toString();
                    int getInputIndex = vfcValue.indexOf("{get_input=");
                    if (getInputIndex > -1) {
                        getInputName = vfcValue.substring(getInputIndex + 11, vfcValue.length() - 1);
                    }

                }
            }

        }

        List<IEntityDetails> serviceEntityList = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.VF)
                        .customizationUUID(vnfResourceCustomization.getModelCustomizationUUID()),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (serviceEntityList != null && !serviceEntityList.isEmpty()) {
            vfcInstanceGroupCustom.setFunction(getLeafPropertyValue(serviceEntityList.get(0), getInputName));
        }

        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);

        List<Input> inputs = vfEntityDetails.getInputs();

        createVFCInstanceGroupMembers(vfcInstanceGroupCustom, vfcInstanceEntity, inputs, existingVnfcGroupSet);

        return vfcInstanceGroupCustom;
    }

    private void createVFCInstanceGroupMembers(VnfcInstanceGroupCustomization vfcInstanceGroupCustom,
            IEntityDetails vfcModuleEntity, List<Input> inputList, Set<VnfcCustomization> existingVnfcGroupSet) {
        List<IEntityDetails> members = vfcModuleEntity.getMemberNodes();
        if (!CollectionUtils.isEmpty(members)) {
            for (IEntityDetails vfcEntity : members) {

                VnfcCustomization existingVfcGroup = findExistingVfc(existingVnfcGroupSet,
                        vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                if (existingVfcGroup == null) {
                    VnfcCustomization vnfcCustomization = new VnfcCustomization();

                    Metadata metadata = vfcEntity.getMetadata();
                    vnfcCustomization.setModelCustomizationUUID(
                            metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                    vnfcCustomization.setModelInstanceName(vfcEntity.getName());
                    vnfcCustomization.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                    vnfcCustomization
                            .setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                    vnfcCustomization.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                    vnfcCustomization.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                    vnfcCustomization.setToscaNodeType(testNull(vfcEntity.getToscaType()));
                    vnfcCustomization
                            .setDescription(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                    vnfcCustomization.setResourceInput(getVnfcResourceInput(vfcEntity, inputList));
                    vnfcCustomization.setVnfcInstanceGroupCustomization(vfcInstanceGroupCustom);
                    List<VnfcCustomization> vnfcCustomizations = vfcInstanceGroupCustom.getVnfcCustomizations();

                    if (vnfcCustomizations == null) {
                        vnfcCustomizations = new ArrayList<>();
                        vfcInstanceGroupCustom.setVnfcCustomizations(vnfcCustomizations);
                    }
                    vnfcCustomizations.add(vnfcCustomization);

                    existingVnfcGroupSet.add(vnfcCustomization);
                }
            }
        }
    }

    public String getVnfcResourceInput(IEntityDetails vfcEntity, List<Input> inputList) {
        Map<String, String> resouceRequest = new HashMap<>();
        Map<String, Property> vfcTemplateProperties = vfcEntity.getProperties();
        for (String key : vfcTemplateProperties.keySet()) {
            Property property = vfcTemplateProperties.get(key);
            String resourceValue = getValue(property.getValue(), inputList);
            resouceRequest.put(key, resourceValue);
        }

        String resourceCustomizationUuid =
                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(resouceRequest);
            jsonStr = jsonStr.replace("\"", "\\\"");
            logger.debug("vfcResource request for resource customization id {}: {}", resourceCustomizationUuid,
                    jsonStr);
        } catch (JsonProcessingException e) {
            logger.debug("Json Exception: {}", e.getMessage());
            logger.error("Exception occurred", e);
        }

        return jsonStr;
    }

    protected VfModuleCustomization createVFModuleResource(IEntityDetails vfModuleEntityDetails,
            ToscaResourceStructure toscaResourceStructure, VfResourceStructure vfResourceStructure,
            IVfModuleData vfModuleData, VnfResourceCustomization vnfResource, Service service,
            Set<CvnfcCustomization> existingCvnfcSet, Set<VnfcCustomization> existingVnfcSet,
            List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom) {

        VfModuleCustomization vfModuleCustomization =
                findExistingVfModuleCustomization(vnfResource, vfModuleData.getVfModuleModelCustomizationUUID());

        if (vfModuleCustomization == null) {

            VfModule vfModule = findExistingVfModule(vnfResource,
                    vfModuleEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));

            Metadata vfMetadata = vfModuleEntityDetails.getMetadata();
            if (vfModule == null)
                vfModule = createVfModule(vfModuleEntityDetails, toscaResourceStructure, vfModuleData, vfMetadata);

            vfModuleCustomization =
                    createVfModuleCustomization(vfModuleEntityDetails, toscaResourceStructure, vfModule, vfModuleData);
            vfModuleCustomization.setVnfCustomization(vnfResource);
            setHeatInformationForVfModule(toscaResourceStructure, vfResourceStructure, vfModule, vfModuleCustomization,
                    vfMetadata);
            vfModuleCustomization.setVfModule(vfModule);
            vfModule.getVfModuleCustomization().add(vfModuleCustomization);
            vnfResource.getVfModuleCustomizations().add(vfModuleCustomization);
        } else {
            vfResourceStructure.setAlreadyDeployed(true);
        }

        // ******************************************************************************************************************
        // * Extract VFC's and CVFC's then add them to VFModule
        // ******************************************************************************************************************

        List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomizations = new ArrayList<>();
        List<CvnfcCustomization> cvnfcCustomizations = new ArrayList<>();
        Set<VnfcCustomization> vnfcCustomizations = new HashSet<>();

        // Only set the CVNFC if this vfModule group is a member of it.

        List<IEntityDetails> groupMembers = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder("org.openecomp.groups.VfModule")
                        .uUID(vfModuleCustomization.getVfModule().getModelUUID()),
                TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

        String vfModuleMemberName = null;

        // Extract CVFC lists
        List<IEntityDetails> cvnfcEntityList = getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.CVFC), TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);


        for (IEntityDetails cvfcEntity : cvnfcEntityList) {
            boolean cvnfcVfModuleNameMatch = false;

            for (IEntityDetails entity : groupMembers) {

                List<IEntityDetails> groupMembersNodes = entity.getMemberNodes();
                for (IEntityDetails groupMember : groupMembersNodes) {

                    vfModuleMemberName = groupMember.getName();

                    if (vfModuleMemberName.equalsIgnoreCase(cvfcEntity.getName())) {
                        cvnfcVfModuleNameMatch = true;
                        break;
                    }

                }
            }


            if (vfModuleMemberName != null && cvnfcVfModuleNameMatch) {

                // Extract associated VFC - Should always be just one
                List<IEntityDetails> vfcEntityList = getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder(SdcTypes.VFC),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CVFC).customizationUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);


                for (IEntityDetails vfcEntity : vfcEntityList) {

                    VnfcCustomization vnfcCustomization = new VnfcCustomization();
                    VnfcCustomization existingVnfcCustomization = null;

                    existingVnfcCustomization = findExistingVfc(existingVnfcSet,
                            vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                    if (existingVnfcCustomization == null) {
                        vnfcCustomization = new VnfcCustomization();
                    } else {
                        vnfcCustomization = existingVnfcCustomization;
                    }

                    // Only Add Abstract VNFC's to our DB, ignore all others
                    if (existingVnfcCustomization == null && vfcEntity.getMetadata()
                            .getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY).equalsIgnoreCase("Abstract")) {

                        vnfcCustomization.setModelCustomizationUUID(
                                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        vnfcCustomization.setModelInstanceName(vfcEntity.getName());
                        vnfcCustomization.setModelInvariantUUID(
                                vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        vnfcCustomization
                                .setModelName(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        vnfcCustomization
                                .setModelUUID(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        vnfcCustomization.setModelVersion(
                                testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        vnfcCustomization.setDescription(
                                testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        vnfcCustomization.setToscaNodeType(testNull(vfcEntity.getToscaType()));

                        vnfcCustomizations.add(vnfcCustomization);
                        existingVnfcSet.add(vnfcCustomization);
                    }

                    // This check is needed incase the VFC subcategory is
                    // something other than Abstract. In that case we want to
                    // skip adding that record to our DB.
                    if (vnfcCustomization.getModelCustomizationUUID() != null) {
                        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
                        cvnfcCustomization.setModelCustomizationUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        cvnfcCustomization.setModelInstanceName(cvfcEntity.getName());
                        cvnfcCustomization.setModelInvariantUUID(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        cvnfcCustomization
                                .setModelName(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        cvnfcCustomization
                                .setModelUUID(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        cvnfcCustomization.setModelVersion(
                                testNull(cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        cvnfcCustomization.setDescription(testNull(
                                cvfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        cvnfcCustomization.setToscaNodeType(testNull(cvfcEntity.getToscaType()));

                        if (existingVnfcCustomization != null) {
                            cvnfcCustomization.setVnfcCustomization(existingVnfcCustomization);
                        } else {
                            cvnfcCustomization.setVnfcCustomization(vnfcCustomization);
                        }

                        cvnfcCustomization.setNfcFunction(getLeafPropertyValue(cvfcEntity, "nfc_function"));
                        cvnfcCustomization.setNfcNamingCode(getLeafPropertyValue(cvfcEntity, "nfc_naming_code"));

                        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

                        // *****************************************************************************************************************************************
                        // * Extract Fabric Configuration
                        // *****************************************************************************************************************************************

                        List<IEntityDetails> fabricEntityList =
                                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(SdcTypes.CONFIGURATION),
                                        TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

                        for (IEntityDetails fabricEntity : fabricEntityList) {

                            List<RequirementAssignment> requirements = fabricEntity.getRequirements();

                            for (RequirementAssignment requirement : requirements) {

                                if (requirement.getNodeTemplateName().equals(cvfcEntity.getName())) {

                                    ConfigurationResource fabricConfig = null;

                                    ConfigurationResource existingConfig = configurationInstaller
                                            .findExistingConfiguration(existingCvnfcConfigurationCustom, fabricEntity
                                                    .getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                                    if (existingConfig == null) {

                                        fabricConfig = configurationInstaller.createFabricConfiguration(fabricEntity,
                                                toscaResourceStructure);

                                    } else {
                                        fabricConfig = existingConfig;
                                    }

                                    CvnfcConfigurationCustomization cvnfcConfigurationCustomization =
                                            configurationInstaller.createCvnfcConfigurationCustomization(fabricEntity,
                                                    toscaResourceStructure, vnfResource, vfModuleCustomization,
                                                    cvnfcCustomization, fabricConfig, vfModuleMemberName);

                                    cvnfcConfigurationCustomizations.add(cvnfcConfigurationCustomization);

                                    existingCvnfcConfigurationCustom.add(cvnfcConfigurationCustomization);

                                }
                            }

                        }
                        cvnfcCustomization.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizations);
                        cvnfcCustomizations.add(cvnfcCustomization);
                        existingCvnfcSet.add(cvnfcCustomization);

                    }

                }

            }
        }
        vfModuleCustomization.setCvnfcCustomization(cvnfcCustomizations);

        return vfModuleCustomization;
    }

    protected VfModuleCustomization findExistingVfModuleCustomization(VnfResourceCustomization vnfResource,
            String vfModuleModelCustomizationUUID) {
        VfModuleCustomization vfModuleCustomization = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getModelCustomizationUUID().equalsIgnoreCase(vfModuleModelCustomizationUUID)) {
                vfModuleCustomization = vfModuleCustom;
            }
        }
        return vfModuleCustomization;
    }

    protected VfModule findExistingVfModule(VnfResourceCustomization vnfResource, String modelUUID) {
        VfModule vfModule = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getVfModule() != null && vfModuleCustom.getVfModule().getModelUUID().equals(modelUUID)) {
                vfModule = vfModuleCustom.getVfModule();
            }
        }
        if (vfModule == null)
            vfModule = vfModuleRepo.findByModelUUID(modelUUID);

        return vfModule;
    }

    protected VfModuleCustomization createVfModuleCustomization(IEntityDetails vfModuleEntityDetails,
            ToscaResourceStructure toscaResourceStructure, VfModule vfModule, IVfModuleData vfModuleData) {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();

        vfModuleCustomization.setModelCustomizationUUID(vfModuleData.getVfModuleModelCustomizationUUID());

        vfModuleCustomization.setVfModule(vfModule);

        String initialCount = getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT);


        if (initialCount != null && initialCount.length() > 0) {
            vfModuleCustomization.setInitialCount(Integer.valueOf(initialCount));
        }

        String availabilityZoneCount =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);

        if (availabilityZoneCount != null && availabilityZoneCount.length() > 0) {
            vfModuleCustomization.setAvailabilityZoneCount(Integer.valueOf(availabilityZoneCount));
        }

        vfModuleCustomization
                .setLabel(getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_VFMODULELABEL));

        String maxInstances =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_MAXVFMODULEINSTANCES);

        if (maxInstances != null && maxInstances.length() > 0) {
            vfModuleCustomization.setMaxInstances(Integer.valueOf(maxInstances));
        }

        String minInstances =
                getLeafPropertyValue(vfModuleEntityDetails, SdcPropertyNames.PROPERTY_NAME_MINVFMODULEINSTANCES);

        if (minInstances != null && minInstances.length() > 0) {
            vfModuleCustomization.setMinInstances(Integer.valueOf(minInstances));
        }

        String skipPostInstConfText = getLeafPropertyValue(vfModuleEntityDetails, SKIP_POST_INST_CONF);

        if (skipPostInstConfText != null) {
            vfModuleCustomization.setSkipPostInstConf(
                    Boolean.parseBoolean(getLeafPropertyValue(vfModuleEntityDetails, SKIP_POST_INST_CONF)));
        }

        return vfModuleCustomization;
    }

    protected VfModule createVfModule(IEntityDetails groupEntityDetails, ToscaResourceStructure toscaResourceStructure,
            IVfModuleData vfModuleData, Metadata vfMetadata) {
        VfModule vfModule = new VfModule();
        String vfModuleModelUUID = vfModuleData.getVfModuleModelUUID();

        if (vfModuleModelUUID == null) {

            vfModuleModelUUID = testNull(
                    groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));

        } else if (vfModuleModelUUID.indexOf('.') > -1) {
            vfModuleModelUUID = vfModuleModelUUID.substring(0, vfModuleModelUUID.indexOf('.'));
        }

        vfModule.setModelInvariantUUID(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID));
        vfModule.setModelName(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME));
        vfModule.setModelUUID(vfModuleModelUUID);
        vfModule.setModelVersion(
                groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION));
        vfModule.setDescription(groupEntityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        String vfModuleType = getLeafPropertyValue(groupEntityDetails, SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE);

        if (vfModuleType != null && "Base".equalsIgnoreCase(vfModuleType)) {
            vfModule.setIsBase(true);
        } else {
            vfModule.setIsBase(false);
        }
        return vfModule;
    }

    protected void setHeatInformationForVfModule(ToscaResourceStructure toscaResourceStructure,
            VfResourceStructure vfResourceStructure, VfModule vfModule, VfModuleCustomization vfModuleCustomization,
            Metadata vfMetadata) {

        Optional<VfModuleStructure> matchingObject = vfResourceStructure.getVfModuleStructure().stream()
                .filter(vfModuleStruct -> vfModuleStruct.getVfModuleMetadata().getVfModuleModelUUID()
                        .equalsIgnoreCase(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)))
                .findFirst();

        if (matchingObject.isPresent()) {
            List<HeatFiles> heatFilesList = new ArrayList<>();
            List<HeatTemplate> volumeHeatChildTemplates = new ArrayList<>();
            List<HeatTemplate> heatChildTemplates = new ArrayList<>();
            HeatTemplate parentHeatTemplate = new HeatTemplate();
            String parentArtifactType = null;
            Set<String> artifacts = new HashSet<>(matchingObject.get().getVfModuleMetadata().getArtifacts());
            for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

                List<HeatTemplate> childNestedHeatTemplates = new ArrayList<>();

                if (artifacts.contains(vfModuleArtifact.getArtifactInfo().getArtifactUUID())) {
                    checkVfModuleArtifactType(vfModule, vfModuleCustomization, heatFilesList, vfModuleArtifact,
                            childNestedHeatTemplates, parentHeatTemplate, vfResourceStructure);
                }

                if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
                    parentArtifactType = HeatArtifactInstaller.identifyParentOfNestedTemplate(matchingObject.get(),
                            vfModuleArtifact);

                    if (!childNestedHeatTemplates.isEmpty()) {

                        if (parentArtifactType != null
                                && parentArtifactType.equalsIgnoreCase(ASDCConfiguration.HEAT_VOL)) {
                            volumeHeatChildTemplates.add(childNestedHeatTemplates.get(0));
                        } else {
                            heatChildTemplates.add(childNestedHeatTemplates.get(0));
                        }
                    }
                }

            }
            if (!heatFilesList.isEmpty()) {
                vfModule.setHeatFiles(heatFilesList);
            }


            // Set all Child Templates related to HEAT_VOLUME
            if (!volumeHeatChildTemplates.isEmpty()) {
                if (vfModule.getVolumeHeatTemplate() != null) {
                    vfModule.getVolumeHeatTemplate().setChildTemplates(volumeHeatChildTemplates);
                } else {
                    logger.debug("VolumeHeatTemplate not set in setHeatInformationForVfModule()");
                }
            }

            // Set all Child Templates related to HEAT
            if (!heatChildTemplates.isEmpty()) {
                if (vfModule.getModuleHeatTemplate() != null) {
                    vfModule.getModuleHeatTemplate().setChildTemplates(heatChildTemplates);
                } else {
                    logger.debug("ModuleHeatTemplate not set in setHeatInformationForVfModule()");
                }
            }
        }
    }

    protected void checkVfModuleArtifactType(VfModule vfModule, VfModuleCustomization vfModuleCustomization,
            List<HeatFiles> heatFilesList, VfModuleArtifact vfModuleArtifact, List<HeatTemplate> nestedHeatTemplates,
            HeatTemplate parentHeatTemplate, VfResourceStructure vfResourceStructure) {
        if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT)) {
            vfModuleArtifact.incrementDeployedInDB();
            vfModule.setModuleHeatTemplate(vfModuleArtifact.getHeatTemplate());
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_VOL)) {
            vfModule.setVolumeHeatTemplate(vfModuleArtifact.getHeatTemplate());
            VfModuleArtifact volVfModuleArtifact = heatArtifactInstaller
                    .getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
            vfModuleCustomization.setVolumeHeatEnv(volVfModuleArtifact.getHeatEnvironment());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
            if (vfModuleArtifact.getHeatEnvironment() != null) {
                if (vfModuleArtifact.getHeatEnvironment().getName().contains("volume")) {
                    vfModuleCustomization.setVolumeHeatEnv(vfModuleArtifact.getHeatEnvironment());
                } else {
                    vfModuleCustomization.setHeatEnvironment(vfModuleArtifact.getHeatEnvironment());
                }
            }
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ARTIFACT)) {
            heatFilesList.add(vfModuleArtifact.getHeatFiles());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
            nestedHeatTemplates.add(vfModuleArtifact.getHeatTemplate());
            vfModuleArtifact.incrementDeployedInDB();
        }
    }

    protected VnfResourceCustomization createVnfResource(IEntityDetails entityDetails,
            ToscaResourceStructure toscaResourceStructure, Service service) throws ArtifactInstallerException {
        VnfResourceCustomization vnfResourceCustomization = null;
        if (vnfResourceCustomization == null) {

            VnfResource vnfResource = findExistingVnfResource(service,
                    entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (vnfResource == null) {
                vnfResource = createVnfResource(entityDetails);
            }

            vnfResourceCustomization =
                    createVnfResourceCustomization(entityDetails, toscaResourceStructure, vnfResource);
            vnfResourceCustomization.setVnfResources(vnfResource);
            vnfResourceCustomization.setService(service);

            // setting resource input for vnf customization
            vnfResourceCustomization.setResourceInput(
                    getResourceInput(toscaResourceStructure, vnfResourceCustomization.getModelCustomizationUUID()));

        }
        return vnfResourceCustomization;
    }

    protected VnfResource findExistingVnfResource(Service service, String modelUUID) {
        VnfResource vnfResource = null;
        for (VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()) {
            if (vnfResourceCustom.getVnfResources() != null
                    && vnfResourceCustom.getVnfResources().getModelUUID().equals(modelUUID)) {
                vnfResource = vnfResourceCustom.getVnfResources();
            }
        }
        if (vnfResource == null)
            vnfResource = vnfRepo.findResourceByModelUUID(modelUUID);

        return vnfResource;
    }

    protected VnfResourceCustomization createVnfResourceCustomization(IEntityDetails entityDetails,
            ToscaResourceStructure toscaResourceStructure, VnfResource vnfResource) {
        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID(
                entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        vnfResourceCustomization.setModelInstanceName(entityDetails.getName());
        vnfResourceCustomization
                .setNfFunction(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
        vnfResourceCustomization.setNfNamingCode(getLeafPropertyValue(entityDetails, "nf_naming_code"));
        vnfResourceCustomization.setNfRole(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFROLE));
        vnfResourceCustomization.setNfType(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFTYPE));

        vnfResourceCustomization.setMultiStageDesign(getLeafPropertyValue(entityDetails, MULTI_STAGE_DESIGN));
        vnfResourceCustomization.setBlueprintName(getLeafPropertyValue(entityDetails, SDNC_MODEL_NAME));
        vnfResourceCustomization.setBlueprintVersion(getLeafPropertyValue(entityDetails, SDNC_MODEL_VERSION));

        String skipPostInstConfText = getLeafPropertyValue(entityDetails, SKIP_POST_INST_CONF);

        if (skipPostInstConfText != null) {
            vnfResourceCustomization.setSkipPostInstConf(
                    Boolean.parseBoolean(getLeafPropertyValue(entityDetails, SKIP_POST_INST_CONF)));
        }

        vnfResourceCustomization.setControllerActor(getLeafPropertyValue(entityDetails, CONTROLLER_ACTOR));
        vnfResourceCustomization.setVnfResources(vnfResource);
        vnfResourceCustomization.setAvailabilityZoneMaxCount(Integer.getInteger(
                entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT)));

        List<CapabilityAssignment> capAssignList = entityDetails.getCapabilities();

        if (capAssignList != null) {

            for (CapabilityAssignment capAssign : capAssignList) {


                if (capAssign != null) {

                    String capabilityName = capAssign.getName();

                    if (capabilityName.equalsIgnoreCase(SCALABLE)) {
                        vnfResourceCustomization.setMinInstances(Integer.getInteger(
                                getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                        vnfResourceCustomization.setMaxInstances(Integer.getInteger(
                                getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
                    }
                }

            }
        }

        if (vnfResourceCustomization.getMinInstances() == null && vnfResourceCustomization.getMaxInstances() == null) {
            vnfResourceCustomization.setMinInstances(Integer
                    .getInteger(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
            vnfResourceCustomization.setMaxInstances(Integer
                    .getInteger(getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
        }

        toscaResourceStructure.setCatalogVnfResourceCustomization(vnfResourceCustomization);

        return vnfResourceCustomization;
    }

    protected VnfResource createVnfResource(IEntityDetails entityDetails) {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelInvariantUUID(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        vnfResource.setModelName(testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        vnfResource.setModelUUID(testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));

        vnfResource.setModelVersion(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        vnfResource.setDescription(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        vnfResource.setOrchestrationMode(HEAT);
        vnfResource.setToscaNodeType(testNull(entityDetails.getToscaType()));
        vnfResource.setAicVersionMax(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
        vnfResource.setAicVersionMin(
                testNull(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
        vnfResource.setCategory(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        vnfResource.setSubCategory(entityDetails.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));

        return vnfResource;
    }

    // this method add provided vnfCustomization to service with
    // existing customization available in db.
    private void addVnfCustomization(Service service, VnfResourceCustomization vnfResourceCustomization) {
        List<Service> services = serviceRepo.findByModelUUID(service.getModelUUID());
        if (!services.isEmpty()) {
            // service exist in db
            Service existingService = services.get(0);
            List<VnfResourceCustomization> existingVnfCustomizations = existingService.getVnfCustomizations();
            if (existingService != null) {
                // it is duplicating entries, so added a check
                for (VnfResourceCustomization existingVnfResourceCustomization : existingVnfCustomizations) {
                    if (!service.getVnfCustomizations().contains(existingVnfResourceCustomization)) {
                        service.getVnfCustomizations().add(existingVnfResourceCustomization);
                    }
                }
            }
        }
        service.getVnfCustomizations().add(vnfResourceCustomization);

    }
}
