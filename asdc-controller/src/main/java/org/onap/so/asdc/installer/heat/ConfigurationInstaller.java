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
import java.util.Optional;
import java.util.Set;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.data.repository.ConfigurationResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationInstaller {

    protected static final String NODES_VRF_ENTRY = "org.openecomp.nodes.VRFEntry";
    protected static final String VLAN_NETWORK_RECEPTOR = "org.openecomp.nodes.VLANNetworkReceptor";

    private final ConfigurationResourceCustomizationRepository configCustomizationRepo;
    private final ServiceRepository serviceRepo;

    public ConfigurationInstaller(ConfigurationResourceCustomizationRepository configCustomizationRepo,
            ServiceRepository serviceRepo) {
        this.configCustomizationRepo = configCustomizationRepo;
        this.serviceRepo = serviceRepo;
    }


    protected ConfigurationResource getConfigurationResource(IEntityDetails configEntity) {
        Metadata metadata = configEntity.getMetadata();
        ConfigurationResource configResource = new ConfigurationResource();
        configResource.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(configEntity.getToscaType());
        return configResource;
    }

    protected ConfigurationResourceCustomization getConfigurationResourceCustomization(IEntityDetails configEntity,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service) {
        Metadata metadata = configEntity.getMetadata();

        ConfigurationResource configResource = getConfigurationResource(configEntity);

        ConfigurationResourceCustomization configCustomizationResource = new ConfigurationResourceCustomization();

        Set<ConfigurationResourceCustomization> configResourceCustomizationSet = new HashSet<>();

        configCustomizationResource
                .setModelCustomizationUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        configCustomizationResource.setModelInstanceName(configEntity.getName());

        configCustomizationResource.setFunction(getLeafPropertyValue(configEntity, "function"));
        configCustomizationResource.setRole(getLeafPropertyValue(configEntity, "role"));
        configCustomizationResource.setType(getLeafPropertyValue(configEntity, "type"));

        configCustomizationResource.setServiceProxyResourceCustomization(spResourceCustomization);

        configCustomizationResource.setConfigurationResource(configResource);
        configCustomizationResource.setService(service);
        configResourceCustomizationSet.add(configCustomizationResource);

        configResource.setConfigurationResourceCustomization(configResourceCustomizationSet);

        return configCustomizationResource;
    }

    protected void processServiceProxyAndConfiguration(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<IEntityDetails> spEntityList =
                getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.SERVICE_PROXY),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<IEntityDetails> configEntityList =
                getEntityDetails(toscaResourceStruct, EntityQuery.newBuilder(SdcTypes.CONFIGURATION),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<ServiceProxyResourceCustomization> serviceProxyList = new ArrayList<>();
        List<ConfigurationResourceCustomization> configurationResourceList = new ArrayList<>();

        ServiceProxyResourceCustomization serviceProxy = null;

        if (spEntityList != null) {
            for (IEntityDetails spEntity : spEntityList) {
                serviceProxy = createServiceProxy(spEntity, service, toscaResourceStruct);
                serviceProxyList.add(serviceProxy);

                for (IEntityDetails configEntity : configEntityList) {

                    List<RequirementAssignment> requirements = configEntity.getRequirements();

                    for (RequirementAssignment requirement : requirements) {

                        if (requirement.getNodeTemplateName().equals(spEntity.getName())) {
                            ConfigurationResourceCustomization configurationResource = createConfiguration(configEntity,
                                    toscaResourceStruct, serviceProxy, service, configurationResourceList);

                            Optional<ConfigurationResourceCustomization> matchingObject =
                                    configurationResourceList.stream()
                                            .filter(configurationResourceCustomization -> configEntity.getMetadata()
                                                    .getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)
                                                    .equals(configurationResource.getModelCustomizationUUID()))
                                            .filter(configurationResourceCustomization -> configurationResourceCustomization
                                                    .getModelInstanceName()
                                                    .equals(configurationResource.getModelInstanceName()))
                                            .findFirst();
                            if (!matchingObject.isPresent()) {
                                configurationResourceList.add(configurationResource);
                            }
                            break;
                        }
                    }
                }

            }
        }

        service.setConfigurationCustomizations(configurationResourceList);
        service.setServiceProxyCustomizations(serviceProxyList);
    }

    /*
     * ConfigurationResourceCustomization objects have their IDs auto incremented in the database. Unless we know their
     * IDs we cannot possibly associate their related records. So these ConfigResourceCustomizations are persisted first
     * and subsequently correlated.
     */

    protected void correlateConfigCustomResources(Service service) {
        /* Assuming that we have only one pair of VRF-VNR */
        ConfigurationResourceCustomization vrfConfigCustomResource = null;
        ConfigurationResourceCustomization vnrConfigCustomResource = null;
        List<ConfigurationResourceCustomization> configCustomList = service.getConfigurationCustomizations();
        for (ConfigurationResourceCustomization configResource : configCustomList) {
            String nodeType = configResource.getConfigurationResource().getToscaNodeType();
            if (NODES_VRF_ENTRY.equalsIgnoreCase(nodeType)) {
                vrfConfigCustomResource = configResource;
            } else if (VLAN_NETWORK_RECEPTOR.equalsIgnoreCase(nodeType)) {
                vnrConfigCustomResource = configResource;
            }
        }

        if (vrfConfigCustomResource != null) {
            vrfConfigCustomResource.setConfigResourceCustomization(vnrConfigCustomResource);
            configCustomizationRepo.save(vrfConfigCustomResource);

        }
        if (vnrConfigCustomResource != null) {
            vnrConfigCustomResource.setConfigResourceCustomization(vrfConfigCustomResource);
            configCustomizationRepo.save(vnrConfigCustomResource);
        }
    }

    protected ServiceProxyResourceCustomization createServiceProxy(IEntityDetails spEntity, Service service,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata spMetadata = spEntity.getMetadata();

        ServiceProxyResourceCustomization spCustomizationResource = new ServiceProxyResourceCustomization();

        Set<ServiceProxyResourceCustomization> serviceProxyCustomizationSet = new HashSet<>();

        spCustomizationResource.setModelName(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        spCustomizationResource
                .setModelInvariantUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        spCustomizationResource.setModelUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        spCustomizationResource.setModelVersion(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        spCustomizationResource.setDescription(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        spCustomizationResource
                .setModelCustomizationUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        spCustomizationResource.setModelInstanceName(spEntity.getName());
        spCustomizationResource.setToscaNodeType(spEntity.getToscaType());

        String sourceServiceUUID = spMetadata.getValue("sourceModelUuid");

        Service sourceService = serviceRepo.findOneByModelUUID(sourceServiceUUID);

        spCustomizationResource.setSourceService(sourceService);
        spCustomizationResource.setToscaNodeType(spEntity.getToscaType());
        serviceProxyCustomizationSet.add(spCustomizationResource);


        toscaResourceStructure.setCatalogServiceProxyResourceCustomization(spCustomizationResource);

        return spCustomizationResource;
    }

    protected ConfigurationResourceCustomization createConfiguration(IEntityDetails configEntity,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service, List<ConfigurationResourceCustomization> configurationResourceList) {

        ConfigurationResourceCustomization configCustomizationResource = getConfigurationResourceCustomization(
                configEntity, toscaResourceStructure, spResourceCustomization, service);

        ConfigurationResource configResource = null;

        ConfigurationResource existingConfigResource = findExistingConfiguration(service,
                configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID), configurationResourceList);

        if (existingConfigResource == null) {
            configResource = getConfigurationResource(configEntity);
        } else {
            configResource = existingConfigResource;
        }

        configCustomizationResource.setConfigurationResource(configResource);

        return configCustomizationResource;
    }

    protected ConfigurationResource createFabricConfiguration(IEntityDetails fabricEntity,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata fabricMetadata = fabricEntity.getMetadata();

        ConfigurationResource configResource = new ConfigurationResource();

        configResource.setModelName(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(fabricEntity.getToscaType());

        return configResource;
    }

    protected CvnfcConfigurationCustomization createCvnfcConfigurationCustomization(IEntityDetails fabricEntity,
            ToscaResourceStructure toscaResourceStruct, VnfResourceCustomization vnfResource,
            VfModuleCustomization vfModuleCustomization, CvnfcCustomization cvnfcCustomization,
            ConfigurationResource configResource, String vfModuleMemberName) {

        Metadata fabricMetadata = fabricEntity.getMetadata();

        CvnfcConfigurationCustomization cvnfcConfigurationCustomization = new CvnfcConfigurationCustomization();

        cvnfcConfigurationCustomization.setConfigurationResource(configResource);

        cvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);

        cvnfcConfigurationCustomization
                .setModelCustomizationUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        cvnfcConfigurationCustomization.setModelInstanceName(fabricEntity.getName());

        cvnfcConfigurationCustomization.setConfigurationFunction(getLeafPropertyValue(fabricEntity, "function"));
        cvnfcConfigurationCustomization.setConfigurationRole(getLeafPropertyValue(fabricEntity, "role"));
        cvnfcConfigurationCustomization.setConfigurationType(getLeafPropertyValue(fabricEntity, "type"));

        return cvnfcConfigurationCustomization;
    }

    protected ConfigurationResource findExistingConfiguration(
            List<CvnfcConfigurationCustomization> existingCvnfcConfigurationCustom, String modelUUID) {
        ConfigurationResource configResource = null;
        for (CvnfcConfigurationCustomization cvnfcConfigCustom : existingCvnfcConfigurationCustom) {
            if (cvnfcConfigCustom != null) {
                if (cvnfcConfigCustom.getConfigurationResource().getModelUUID().equals(modelUUID)) {
                    configResource = cvnfcConfigCustom.getConfigurationResource();
                }
            }
        }

        return configResource;
    }

    protected ConfigurationResource findExistingConfiguration(Service service, String modelUUID,
            List<ConfigurationResourceCustomization> configurationResourceList) {
        ConfigurationResource configResource = null;
        for (ConfigurationResourceCustomization configurationResourceCustom : configurationResourceList) {
            if (configurationResourceCustom.getConfigurationResource() != null
                    && configurationResourceCustom.getConfigurationResource().getModelUUID().equals(modelUUID)) {
                configResource = configurationResourceCustom.getConfigurationResource();
            }
        }

        return configResource;
    }
}
