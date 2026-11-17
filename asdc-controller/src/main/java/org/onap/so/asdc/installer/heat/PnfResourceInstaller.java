/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import java.util.Map;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.PnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.PnfResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PnfResourceInstaller {

    private static final Logger logger = LoggerFactory.getLogger(PnfResourceInstaller.class);
    private static final String DEFAULT_SOFTWARE_VERSION = "default_software_version";

    private final PnfResourceRepository pnfResourceRepository;
    private final PnfCustomizationRepository pnfCustomizationRepository;

    public PnfResourceInstaller(PnfResourceRepository pnfResourceRepository,
            PnfCustomizationRepository pnfCustomizationRepository) {
        this.pnfResourceRepository = pnfResourceRepository;
        this.pnfCustomizationRepository = pnfCustomizationRepository;
    }

    /**
     * This is used to process the PNF specific resource, including resource and resource_customization.
     * {@link IEntityDetails} based API is used to retrieve information. Please check {@link ISdcCsarHelper} for
     * details.
     */
    void processPnfResources(ToscaResourceStructure toscaResourceStruct, Service service,
            PnfResourceStructure resourceStructure) throws Exception {
        logger.info("Processing PNF resource: {}", resourceStructure.getResourceInstance().getResourceUUID());

        ISdcCsarHelper sdcCsarHelper = toscaResourceStruct.getSdcCsarHelper();
        EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.PNF).build();
        TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();

        List<IEntityDetails> entityDetailsList = sdcCsarHelper.getEntity(entityQuery, topologyTemplateQuery, false);
        for (IEntityDetails entityDetails : entityDetailsList) {
            Metadata metadata = entityDetails.getMetadata();
            String customizationUUID = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
            String modelUuid = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
            String notifCustomizationUUID = resourceStructure.getResourceInstance().getResourceCustomizationUUID();
            if (customizationUUID != null && customizationUUID.equals(notifCustomizationUUID)) {
                logger.info("Resource customization UUID: {} is the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);

                if (checkExistingPnfResourceCutomization(customizationUUID)) {
                    logger.info("Resource customization UUID: {} already deployed", customizationUUID);
                } else {
                    PnfResource pnfResource = findExistingPnfResource(service, modelUuid);
                    if (pnfResource == null) {
                        pnfResource = createPnfResource(entityDetails);
                    }
                    PnfResourceCustomization pnfResourceCustomization =
                            createPnfResourceCustomization(entityDetails, pnfResource);
                    pnfResource.getPnfResourceCustomizations().add(pnfResourceCustomization);
                    toscaResourceStruct.setPnfResourceCustomization(pnfResourceCustomization);
                    service.getPnfCustomizations().add(pnfResourceCustomization);
                }
            } else {
                logger.warn(
                        "Resource customization UUID: {} is NOT the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);
            }
        }
    }

    private PnfResource findExistingPnfResource(Service service, String modelUuid) {
        PnfResource pnfResource = null;
        for (PnfResourceCustomization pnfResourceCustomization : service.getPnfCustomizations()) {
            if (pnfResourceCustomization.getPnfResources() != null
                    && pnfResourceCustomization.getPnfResources().getModelUUID().equals(modelUuid)) {
                pnfResource = pnfResourceCustomization.getPnfResources();
            }
        }
        if (pnfResource == null) {
            pnfResource = pnfResourceRepository.findById(modelUuid).orElse(pnfResource);
        }
        return pnfResource;
    }

    private boolean checkExistingPnfResourceCutomization(String customizationUUID) {
        return pnfCustomizationRepository.findById(customizationUUID).isPresent();
    }

    /**
     * Construct the {@link PnfResource} from {@link IEntityDetails} object.
     */
    private PnfResource createPnfResource(IEntityDetails entity) {
        PnfResource pnfResource = new PnfResource();
        Metadata metadata = entity.getMetadata();
        pnfResource.setModelInvariantUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        pnfResource.setModelName(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        pnfResource.setModelUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        pnfResource.setModelVersion(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        pnfResource.setDescription(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        pnfResource.setCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY)));
        pnfResource.setSubCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        pnfResource.setToscaNodeType(entity.getToscaType());
        return pnfResource;
    }

    /**
     * Construct the {@link PnfResourceCustomization} from {@link IEntityDetails} object.
     */
    private PnfResourceCustomization createPnfResourceCustomization(IEntityDetails entityDetails,
            PnfResource pnfResource) {

        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        Metadata metadata = entityDetails.getMetadata();
        Map<String, Property> properties = entityDetails.getProperties();
        pnfResourceCustomization.setModelCustomizationUUID(
                testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        pnfResourceCustomization.setModelInstanceName(entityDetails.getName());
        pnfResourceCustomization
                .setNfFunction(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
        pnfResourceCustomization.setNfNamingCode(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFCODE)));
        pnfResourceCustomization.setNfRole(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFROLE)));
        pnfResourceCustomization.setNfType(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFTYPE)));
        pnfResourceCustomization.setMultiStageDesign(getStringValue(properties.get(MULTI_STAGE_DESIGN)));
        pnfResourceCustomization.setBlueprintName(getStringValue(properties.get(SDNC_MODEL_NAME)));
        pnfResourceCustomization.setBlueprintVersion(getStringValue(properties.get(SDNC_MODEL_VERSION)));
        pnfResourceCustomization.setSkipPostInstConf(getBooleanValue(properties.get(SKIP_POST_INST_CONF)));
        pnfResourceCustomization.setControllerActor(getStringValue(properties.get(CONTROLLER_ACTOR)));
        pnfResourceCustomization.setDefaultSoftwareVersion(extractDefaultSoftwareVersionFromSwVersions(properties));
        pnfResourceCustomization.setPnfResources(pnfResource);
        return pnfResourceCustomization;
    }

    String extractDefaultSoftwareVersionFromSwVersions(Map<String, Property> properties) {
        final String SOFTWARE_VERSIONS = "software_versions";
        final String EMPTY_STRING = "";
        String defaultSoftwareVersionValue = getStringValue(properties.get(DEFAULT_SOFTWARE_VERSION));
        if (defaultSoftwareVersionValue != null && !defaultSoftwareVersionValue.isEmpty()) {
            return defaultSoftwareVersionValue;
        }
        if (properties.containsKey(SOFTWARE_VERSIONS) && properties.get(SOFTWARE_VERSIONS).getValue() != null) {
            if (properties.get(SOFTWARE_VERSIONS).getValue() instanceof List) {
                List<String> propertyValueList = (List) properties.get(SOFTWARE_VERSIONS).getValue();
                return propertyValueList.get(0);
            } else if (properties.get(SOFTWARE_VERSIONS).getValue() instanceof String) {
                return getStringValue(properties.get(SOFTWARE_VERSIONS));
            }
        }

        return EMPTY_STRING;
    }

    /**
     * Get value from {@link Property} and cast to boolean value. Return true if property is null.
     */
    boolean getBooleanValue(Property property) {
        if (null == property) {
            return true;
        }
        Object value = property.getValue();
        return new Boolean(String.valueOf(value));
    }

    /**
     * Get value from {@link Property} and cast to String value. Return empty String if property is null value.
     */
    private String getStringValue(Property property) {
        if (null == property) {
            return "";
        }
        Object value = property.getValue();
        return String.valueOf(value);
    }
}
