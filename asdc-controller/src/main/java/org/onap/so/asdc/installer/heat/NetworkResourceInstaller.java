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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkInstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.SubType;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.data.repository.CollectionResourceRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.TempNetworkHeatTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NetworkResourceInstaller {

    private static final Logger logger = LoggerFactory.getLogger(NetworkResourceInstaller.class);

    protected static final String BASIC = "BASIC";
    protected static final String PROVIDER = "PROVIDER";

    private final HeatTemplateRepository heatRepo;
    private final TempNetworkHeatTemplateRepository tempNetworkLookupRepo;
    private final NetworkResourceRepository networkRepo;
    private final NetworkResourceCustomizationRepository networkCustomizationRepo;
    private final CollectionResourceRepository collectionRepo;
    private final InstanceGroupRepository instanceGroupRepo;

    public NetworkResourceInstaller(HeatTemplateRepository heatRepo,
            TempNetworkHeatTemplateRepository tempNetworkLookupRepo, NetworkResourceRepository networkRepo,
            NetworkResourceCustomizationRepository networkCustomizationRepo,
            CollectionResourceRepository collectionRepo, InstanceGroupRepository instanceGroupRepo) {
        this.heatRepo = heatRepo;
        this.tempNetworkLookupRepo = tempNetworkLookupRepo;
        this.networkRepo = networkRepo;
        this.networkCustomizationRepo = networkCustomizationRepo;
        this.collectionRepo = collectionRepo;
        this.instanceGroupRepo = instanceGroupRepo;
    }

    protected void processNetworks(ToscaResourceStructure toscaResourceStruct, Service service)
            throws ArtifactInstallerException {

        List<IEntityDetails> vlEntityList = getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.VL),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (vlEntityList != null) {
            for (IEntityDetails vlEntity : vlEntityList) {
                String networkResourceModelName = vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME);

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(networkResourceModelName);

                if (tempNetworkLookUp != null) {
                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    if (heatTemplate != null) {
                        NetworkResourceCustomization networkCustomization = createNetwork(vlEntity, toscaResourceStruct,
                                heatTemplate, tempNetworkLookUp.getAicVersionMax(),
                                tempNetworkLookUp.getAicVersionMin(), service);
                        // only insert unique entries
                        if (!service.getNetworkCustomizations().contains(networkCustomization)) {
                            service.getNetworkCustomizations().add(networkCustomization);
                        }
                    } else {
                        throw new ArtifactInstallerException("No HeatTemplate found for artifactUUID: "
                                + tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    }
                } else {
                    NetworkResourceCustomization networkCustomization =
                            createNetwork(vlEntity, toscaResourceStruct, null, null, null, service);
                    networkCustomization.setResourceInput(
                            getResourceInput(toscaResourceStruct, networkCustomization.getModelCustomizationUUID()));
                    service.getNetworkCustomizations().add(networkCustomization);
                    logger.debug("No NetworkResourceName found in TempNetworkHeatTemplateLookup for {}",
                            networkResourceModelName);
                }

            }
        }
    }

    protected void processNetworkCollections(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<IEntityDetails> crEntityList = getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.CR),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (crEntityList != null) {
            for (IEntityDetails ncEntity : crEntityList) {

                createNetworkCollection(ncEntity, toscaResourceStruct, service);
                collectionRepo.saveAndFlush(toscaResourceStruct.getCatalogCollectionResource());

                List<NetworkInstanceGroup> networkInstanceGroupList =
                        toscaResourceStruct.getCatalogNetworkInstanceGroup();
                for (NetworkInstanceGroup networkInstanceGroup : networkInstanceGroupList) {
                    instanceGroupRepo.saveAndFlush(networkInstanceGroup);
                }

            }
        }
        service.getCollectionResourceCustomizations()
                .add(toscaResourceStruct.getCatalogCollectionResourceCustomization());
    }

    protected NetworkResourceCustomization createNetwork(IEntityDetails networkEntity,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin,
            Service service) {

        NetworkResourceCustomization networkResourceCustomization =
                networkCustomizationRepo.findOneByModelCustomizationUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        boolean networkUUIDsMatch = true;
        // Check to make sure the NetworkResourceUUID on the Customization record matches the NetworkResourceUUID from
        // the distribution.
        // If not we'll update the Customization record with latest from the distribution
        if (networkResourceCustomization != null) {
            String existingNetworkModelUUID = networkResourceCustomization.getNetworkResource().getModelUUID();
            String latestNetworkModelUUID = networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID);

            if (!existingNetworkModelUUID.equals(latestNetworkModelUUID)) {
                networkUUIDsMatch = false;
            }

        }

        if (networkResourceCustomization != null && !networkUUIDsMatch) {

            NetworkResource networkResource =
                    createNetworkResource(networkEntity, toscaResourceStructure, heatTemplate, aicMax, aicMin);

            networkResourceCustomization.setNetworkResource(networkResource);

            networkCustomizationRepo.saveAndFlush(networkResourceCustomization);


        } else if (networkResourceCustomization == null) {
            networkResourceCustomization = createNetworkResourceCustomization(networkEntity, toscaResourceStructure);

            NetworkResource networkResource = findExistingNetworkResource(service,
                    networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            if (networkResource == null)
                networkResource =
                        createNetworkResource(networkEntity, toscaResourceStructure, heatTemplate, aicMax, aicMin);

            networkResource.addNetworkResourceCustomization(networkResourceCustomization);
            networkResourceCustomization.setNetworkResource(networkResource);
        }

        return networkResourceCustomization;
    }

    protected NetworkResource findExistingNetworkResource(Service service, String modelUUID) {
        NetworkResource networkResource = null;
        for (NetworkResourceCustomization networkCustom : service.getNetworkCustomizations()) {
            if (networkCustom.getNetworkResource() != null
                    && networkCustom.getNetworkResource().getModelUUID().equals(modelUUID)) {
                networkResource = networkCustom.getNetworkResource();
            }
        }
        if (networkResource == null)
            networkResource = networkRepo.findResourceByModelUUID(modelUUID);

        return networkResource;
    }

    protected NetworkResourceCustomization createNetworkResourceCustomization(IEntityDetails networkEntity,
            ToscaResourceStructure toscaResourceStructure) {
        NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
        networkResourceCustomization.setModelInstanceName(
                testNull(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        networkResourceCustomization.setModelCustomizationUUID(
                testNull(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));

        networkResourceCustomization.setNetworkTechnology(
                getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));

        networkResourceCustomization
                .setNetworkType(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));

        networkResourceCustomization
                .setNetworkRole(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));

        networkResourceCustomization
                .setNetworkScope(getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));

        return networkResourceCustomization;
    }

    protected NetworkResource createNetworkResource(IEntityDetails vlEntity,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin) {
        NetworkResource networkResource = new NetworkResource();
        String providerNetwork =
                getLeafPropertyValue(vlEntity, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

        if ("true".equalsIgnoreCase(providerNetwork)) {
            networkResource.setNeutronNetworkType(PROVIDER);
        } else {
            networkResource.setNeutronNetworkType(BASIC);
        }

        networkResource.setModelName(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));

        networkResource.setModelInvariantUUID(
                testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        networkResource.setModelUUID(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        networkResource
                .setModelVersion(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));

        networkResource.setAicVersionMax(aicMax);
        networkResource.setAicVersionMin(aicMin);
        networkResource.setToscaNodeType(vlEntity.getToscaType());
        networkResource
                .setDescription(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        networkResource.setOrchestrationMode(HEAT);
        networkResource.setHeatTemplate(heatTemplate);
        return networkResource;
    }

    protected CollectionNetworkResourceCustomization createNetworkCollection(IEntityDetails cnrEntity,
            ToscaResourceStructure toscaResourceStructure, Service service) {

        CollectionNetworkResourceCustomization collectionNetworkResourceCustomization =
                new CollectionNetworkResourceCustomization();

        // **** Build Object to populate Collection_Resource table
        CollectionResource collectionResource = new CollectionResource();

        collectionResource.setModelName(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        collectionResource
                .setModelInvariantUUID(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        collectionResource.setModelUUID(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        collectionResource.setModelVersion(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        collectionResource.setDescription(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        collectionResource.setToscaNodeType(cnrEntity.getToscaType());

        toscaResourceStructure.setCatalogCollectionResource(collectionResource);

        // **** Build object to populate Collection_Resource_Customization table
        NetworkCollectionResourceCustomization ncfc = new NetworkCollectionResourceCustomization();

        ncfc.setFunction(getLeafPropertyValue(cnrEntity, "cr_function"));
        ncfc.setRole(getLeafPropertyValue(cnrEntity, "cr_role"));
        ncfc.setType(getLeafPropertyValue(cnrEntity, "cr_type"));

        ncfc.setModelInstanceName(cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        ncfc.setModelCustomizationUUID(
                cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        Set<CollectionNetworkResourceCustomization> networkResourceCustomizationSet = new HashSet<>();
        networkResourceCustomizationSet.add(collectionNetworkResourceCustomization);

        ncfc.setNetworkResourceCustomization(networkResourceCustomizationSet);

        ncfc.setCollectionResource(collectionResource);
        toscaResourceStructure.setCatalogCollectionResourceCustomization(ncfc);

        // *** Build object to populate the Instance_Group table
        List<IEntityDetails> ncEntityList =
                getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder("org.openecomp.groups.NetworkCollection"),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);

        List<NetworkInstanceGroup> networkInstanceGroupList = new ArrayList<>();

        List<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationList =
                new ArrayList<>();

        for (IEntityDetails ncGroupEntity : ncEntityList) {

            NetworkInstanceGroup networkInstanceGroup = new NetworkInstanceGroup();
            Metadata instanceMetadata = ncGroupEntity.getMetadata();
            networkInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            networkInstanceGroup
                    .setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            networkInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            networkInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
            networkInstanceGroup.setToscaNodeType(ncGroupEntity.getToscaType());
            networkInstanceGroup.setRole(SubType.SUB_INTERFACE.toString()); // Set
            // Role
            networkInstanceGroup.setType(InstanceGroupType.L3_NETWORK); // Set
            // type
            networkInstanceGroup.setCollectionResource(collectionResource);

            // ****Build object to populate
            // Collection_Resource_Instance_Group_Customization table
            CollectionResourceInstanceGroupCustomization crInstanceGroupCustomization =
                    new CollectionResourceInstanceGroupCustomization();
            crInstanceGroupCustomization.setInstanceGroup(networkInstanceGroup);
            crInstanceGroupCustomization.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            crInstanceGroupCustomization.setModelCustomizationUUID(
                    cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

            // Loop through the template policy to find the subinterface_network_quantity property name. Then extract
            // the value for it.
            List<IEntityDetails> policyEntityList = getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder("org.openecomp.policies.scaling.Fixed"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), true);

            if (policyEntityList != null) {
                for (IEntityDetails policyEntity : policyEntityList) {
                    for (String policyNetworkCollection : policyEntity.getTargets()) {

                        if (policyNetworkCollection.equalsIgnoreCase(ncGroupEntity.getName())) {

                            Map<String, Property> propMap = policyEntity.getProperties();

                            if (propMap.get("quantity") != null) {

                                String quantity = getLeafPropertyValue(cnrEntity,
                                        getPropertyInput(propMap.get("quantity").toString()));

                                if (quantity != null) {
                                    crInstanceGroupCustomization
                                            .setSubInterfaceNetworkQuantity(Integer.parseInt(quantity));
                                }

                            }

                        }
                    }
                }
            }

            crInstanceGroupCustomization.setDescription(
                    getLeafPropertyValue(cnrEntity, instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
                            + "_network_collection_description"));

            crInstanceGroupCustomization.setFunction(getLeafPropertyValue(cnrEntity,
                    instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME) + "_network_collection_function"));

            crInstanceGroupCustomization.setCollectionResourceCust(ncfc);
            collectionResourceInstanceGroupCustomizationList.add(crInstanceGroupCustomization);

            networkInstanceGroup
                    .setCollectionInstanceGroupCustomizations(collectionResourceInstanceGroupCustomizationList);

            networkInstanceGroupList.add(networkInstanceGroup);


            toscaResourceStructure.setCatalogNetworkInstanceGroup(networkInstanceGroupList);

            List<IEntityDetails> networkEntityList =
                    getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(SdcTypes.VL),
                            TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                    cnrEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                            false);

            List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizationList = new ArrayList<>();

            // *****Build object to populate the NetworkResource table
            NetworkResource networkResource = new NetworkResource();

            for (IEntityDetails networkEntity : networkEntityList) {

                String providerNetwork = getLeafPropertyValue(networkEntity,
                        SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

                if ("true".equalsIgnoreCase(providerNetwork)) {
                    networkResource.setNeutronNetworkType(PROVIDER);
                } else {
                    networkResource.setNeutronNetworkType(BASIC);
                }

                networkResource.setModelName(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                networkResource.setModelInvariantUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                networkResource.setModelUUID(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                networkResource
                        .setModelVersion(networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));

                networkResource.setAicVersionMax(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(
                                networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                if (tempNetworkLookUp != null) {

                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    networkResource.setHeatTemplate(heatTemplate);

                    networkResource.setAicVersionMin(tempNetworkLookUp.getAicVersionMin());

                }

                networkResource.setToscaNodeType(networkEntity.getToscaType());
                networkResource.setDescription(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                networkResource.setOrchestrationMode(HEAT);

                // Build object to populate the
                // Collection_Network_Resource_Customization table
                for (IEntityDetails networkMemberEntity : ncGroupEntity.getMemberNodes()) {
                    collectionNetworkResourceCustomization.setModelInstanceName(networkMemberEntity.getName());
                }

                collectionNetworkResourceCustomization.setModelCustomizationUUID(
                        networkEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                collectionNetworkResourceCustomization.setNetworkTechnology(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
                collectionNetworkResourceCustomization.setNetworkType(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
                collectionNetworkResourceCustomization.setNetworkRole(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
                collectionNetworkResourceCustomization.setNetworkScope(
                        getLeafPropertyValue(networkEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
                collectionNetworkResourceCustomization.setInstanceGroup(networkInstanceGroup);
                collectionNetworkResourceCustomization.setNetworkResource(networkResource);
                collectionNetworkResourceCustomization.setNetworkResourceCustomization(ncfc);

                collectionNetworkResourceCustomizationList.add(collectionNetworkResourceCustomization);
            }

        }

        return collectionNetworkResourceCustomization;
    }
}
