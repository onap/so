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
import java.util.List;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class AllottedResourceInstaller {

    private final AllottedResourceRepository allottedRepo;
    private final AllottedResourceCustomizationRepository allottedCustomizationRepo;

    public AllottedResourceInstaller(AllottedResourceRepository allottedRepo,
            AllottedResourceCustomizationRepository allottedCustomizationRepo) {
        this.allottedRepo = allottedRepo;
        this.allottedCustomizationRepo = allottedCustomizationRepo;
    }

    protected void processAllottedResources(List<IEntityDetails> arEntityDetails,
            ToscaResourceStructure toscaResourceStruct, Service service) throws ArtifactInstallerException {

        List<IEntityDetails> pnfAREntityList = getEntityDetails(toscaResourceStruct,
                EntityQuery.newBuilder(SdcTypes.PNF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        for (IEntityDetails pnfEntity : pnfAREntityList) {

            Metadata metadata = pnfEntity.getMetadata();
            String category = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);
            if (ALLOTTED_RESOURCE.equalsIgnoreCase(category)) {
                arEntityDetails.add(pnfEntity);
            }

        }

        if (arEntityDetails != null) {
            for (IEntityDetails arEntity : arEntityDetails) {
                AllottedResourceCustomization allottedResource =
                        createAllottedResource(arEntity, toscaResourceStruct, service);
                String resourceInput =
                        getResourceInput(toscaResourceStruct, allottedResource.getModelCustomizationUUID());
                if (!"{}".equals(resourceInput)) {
                    allottedResource.setResourceInput(resourceInput);
                }
                if (!service.getAllottedCustomizations().contains(allottedResource)) {
                    service.getAllottedCustomizations().add(allottedResource);
                }
            }
        }
    }

    protected AllottedResourceCustomization createAllottedResource(IEntityDetails arEntity,
            ToscaResourceStructure toscaResourceStructure, Service service) {
        AllottedResourceCustomization allottedResourceCustomization =
                allottedCustomizationRepo.findOneByModelCustomizationUUID(
                        arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        if (allottedResourceCustomization == null) {
            AllottedResource allottedResource = findExistingAllottedResource(service,
                    arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (allottedResource == null)
                allottedResource = createAR(arEntity);

            toscaResourceStructure.setAllottedResource(allottedResource);
            allottedResourceCustomization = createAllottedResourceCustomization(arEntity, toscaResourceStructure);
            allottedResourceCustomization.setAllottedResource(allottedResource);
            allottedResource.getAllotedResourceCustomization().add(allottedResourceCustomization);
        }
        return allottedResourceCustomization;
    }

    protected AllottedResource findExistingAllottedResource(Service service, String modelUUID) {
        AllottedResource allottedResource = null;
        for (AllottedResourceCustomization allottedResourceCustom : service.getAllottedCustomizations()) {
            if (allottedResourceCustom.getAllottedResource() != null
                    && allottedResourceCustom.getAllottedResource().getModelUUID().equals(modelUUID)) {
                allottedResource = allottedResourceCustom.getAllottedResource();
            }
        }
        if (allottedResource == null)
            allottedResource = allottedRepo.findResourceByModelUUID(modelUUID);

        return allottedResource;
    }

    protected AllottedResourceCustomization createAllottedResourceCustomization(IEntityDetails arEntity,
            ToscaResourceStructure toscaResourceStructure) {
        AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
        allottedResourceCustomization.setModelCustomizationUUID(
                testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        allottedResourceCustomization.setModelInstanceName(arEntity.getName());

        allottedResourceCustomization
                .setNfFunction(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
        allottedResourceCustomization.setNfNamingCode(getLeafPropertyValue(arEntity, "nf_naming_code"));
        allottedResourceCustomization.setNfRole(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFROLE));
        allottedResourceCustomization.setNfType(getLeafPropertyValue(arEntity, SdcPropertyNames.PROPERTY_NAME_NFTYPE));

        EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.VFC).build();

        TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.VF)
                .customizationUUID(allottedResourceCustomization.getModelCustomizationUUID()).build();

        List<IEntityDetails> vfcEntities =
                toscaResourceStructure.getSdcCsarHelper().getEntity(entityQuery, topologyTemplateQuery, false);


        if (vfcEntities != null) {
            for (IEntityDetails vfcEntity : vfcEntities) {

                allottedResourceCustomization
                        .setProvidingServiceModelUUID(getLeafPropertyValue(vfcEntity, "providing_service_uuid"));
                allottedResourceCustomization.setProvidingServiceModelInvariantUUID(
                        getLeafPropertyValue(vfcEntity, "providing_service_invariant_uuid"));
                allottedResourceCustomization
                        .setProvidingServiceModelName(getLeafPropertyValue(vfcEntity, "providing_service_name"));
            }
        }

        List<CapabilityAssignment> capAssignmentList = arEntity.getCapabilities();

        if (capAssignmentList != null) {

            for (CapabilityAssignment arCapability : capAssignmentList) {

                if (arCapability != null) {

                    String capabilityName = arCapability.getName();

                    if (capabilityName.equals(SCALABLE)) {

                        allottedResourceCustomization
                                .setMinInstances(Integer.getInteger(getCapabilityLeafPropertyValue(arCapability,
                                        SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                        allottedResourceCustomization
                                .setMinInstances(Integer.getInteger(getCapabilityLeafPropertyValue(arCapability,
                                        SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));

                    }
                }

            }
        }

        return allottedResourceCustomization;
    }

    protected AllottedResource createAR(IEntityDetails arEntity) {
        AllottedResource allottedResource = new AllottedResource();
        allottedResource.setModelUUID(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        allottedResource.setModelInvariantUUID(
                testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        allottedResource.setModelName(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        allottedResource
                .setModelVersion(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        allottedResource.setToscaNodeType(testNull(arEntity.getToscaType()));
        allottedResource
                .setSubcategory(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        allottedResource.setDescription(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        return allottedResource;
    }
}
