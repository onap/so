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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ServiceModelInstaller {

    private static final Logger logger = LoggerFactory.getLogger(ServiceModelInstaller.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ServiceRepository serviceRepo;
    private final ToscaCsarRepository toscaCsarRepo;

    public ServiceModelInstaller(ServiceRepository serviceRepo, ToscaCsarRepository toscaCsarRepo) {
        this.serviceRepo = serviceRepo;
        this.toscaCsarRepo = toscaCsarRepo;
    }

    List<IEntityDetails> getRequirementList(List<IEntityDetails> resultList, List<IEntityDetails> entityDetails) {
        Set<String> nodeNames = new HashSet<>();
        Set<String> placedNames = new HashSet<>();
        resultList.forEach(entity -> placedNames.add(entity.getName()));
        nodeNames.addAll(placedNames);
        entityDetails.forEach(entity -> nodeNames.add(entity.getName()));

        List<IEntityDetails> pending = new ArrayList<>(entityDetails);
        boolean progress = true;
        while (!pending.isEmpty() && progress) {
            progress = false;
            Iterator<IEntityDetails> it = pending.iterator();
            while (it.hasNext()) {
                IEntityDetails entity = it.next();
                if (requirementsPlaced(entity.getRequirements(), nodeNames, placedNames)) {
                    resultList.add(entity);
                    placedNames.add(entity.getName());
                    it.remove();
                    progress = true;
                }
            }
        }

        // The E2E service flows only instantiate resources that are listed in the order, so a requirement
        // cycle must not drop a resource from it.
        resultList.addAll(pending);
        return resultList;
    }

    private static boolean requirementsPlaced(List<RequirementAssignment> requirements, Set<String> nodeNames,
            Set<String> placedNames) {
        if (requirements == null) {
            return true;
        }
        for (RequirementAssignment ra : requirements) {
            String reqNode = ra.getNodeTemplateName();
            if (nodeNames.contains(reqNode) && !placedNames.contains(reqNode)) {
                return false;
            }
        }
        return true;
    }

    // This method retrieve resource sequence from csar file
    void processResourceSequence(ToscaResourceStructure toscaResourceStructure, Service service) {
        List<String> resouceSequence = new ArrayList<>();
        List<IEntityDetails> resultList = new ArrayList<>();

        List<IEntityDetails> vfEntityList =
                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<IEntityDetails> entities = new ArrayList<>();
        entities.addAll(vfEntityList);

        for (IEntityDetails entityDetails : vfEntityList) {
            List<RequirementAssignment> requirements = entityDetails.getRequirements();

            if (requirements == null || requirements.isEmpty()) {
                resultList.add(entityDetails);
                entities.remove(entityDetails);
            }
        }

        resultList = getRequirementList(resultList, entities);

        for (IEntityDetails entity : resultList) {
            String templateName = entity.getMetadata().getValue("name");
            if (!resouceSequence.contains(templateName)) {
                resouceSequence.add(templateName);
            }
        }

        String resourceSeqStr = resouceSequence.stream().collect(Collectors.joining(","));
        service.setResourceOrder(resourceSeqStr);
        logger.debug(" resourceSeq for service uuid {}: {}", service.getModelUUID(), resourceSeqStr);
    }

    protected Service createService(ToscaResourceStructure toscaResourceStructure,
            ResourceStructure resourceStructure) {

        Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();
        List<Service> services =
                serviceRepo.findByModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        Service service;
        if (!services.isEmpty() && services.size() > 0) {
            service = services.get(0);
        } else {
            service = new Service();
        }

        if (serviceMetadata != null) {

            if (toscaResourceStructure.getServiceVersion() != null) {
                service.setModelVersion(toscaResourceStructure.getServiceVersion());
            }

            service.setServiceType(serviceMetadata.getValue("serviceType"));
            service.setServiceRole(serviceMetadata.getValue("serviceRole"));
            service.setServiceFunction(serviceMetadata.getValue("serviceFunction"));
            service.setCategory(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));

            service.setDescription(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
            service.setModelName(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            service.setModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            service.setEnvironmentContext(serviceMetadata.getValue("environmentContext"));

            if (resourceStructure != null)
                service.setWorkloadContext(resourceStructure.getNotification().getWorkloadContext());

            service.setModelInvariantUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            service.setCsar(toscaResourceStructure.getCatalogToscaCsar());
            service.setNamingPolicy(serviceMetadata.getValue("namingPolicy"));
            String generateNaming = serviceMetadata.getValue("ecompGeneratedNaming");
            Boolean generateNamingValue = null;
            if (generateNaming != null) {
                generateNamingValue = "true".equalsIgnoreCase(generateNaming);
            }
            service.setOnapGeneratedNaming(generateNamingValue);

            List<Input> serviceInputs = toscaResourceStructure.getSdcCsarHelper().getServiceInputs();
            logger.debug("serviceInputs: {} {}", serviceInputs);
            if (!serviceInputs.isEmpty()) {
                serviceInputs.forEach(input -> {
                    if (CDS_MODEL_NAME.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setBlueprintName(value);
                    }
                    if (CDS_MODEL_VERSION.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setBlueprintVersion(value);
                    }
                    if (CONTROLLER_ACTOR.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : null;
                        service.setControllerActor(value);
                    }
                    if (SKIP_POST_INST_CONF.equalsIgnoreCase(input.getName())) {
                        String value = input.getDefault() != null ? input.getDefault().toString() : "false";
                        service.setSkipPostInstConf(Boolean.valueOf(value));
                    }
                });
            }
        }
        toscaResourceStructure.setCatalogService(service);
        return service;
    }

    protected void createToscaCsar(ToscaResourceStructure toscaResourceStructure) {
        Optional<ToscaCsar> toscaCsarOpt =
                toscaCsarRepo.findById(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
        ToscaCsar toscaCsar;
        if (!toscaCsarOpt.isPresent()) {
            toscaCsar = new ToscaCsar();
            toscaCsar.setArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
        } else {
            toscaCsar = toscaCsarOpt.get();
        }
        if (toscaResourceStructure.getToscaArtifact().getArtifactChecksum() != null) {
            toscaCsar.setArtifactChecksum(toscaResourceStructure.getToscaArtifact().getArtifactChecksum());
        } else {
            toscaCsar.setArtifactChecksum(MANUAL_RECORD);
        }
        toscaCsar.setName(toscaResourceStructure.getToscaArtifact().getArtifactName());
        toscaCsar.setVersion(toscaResourceStructure.getToscaArtifact().getArtifactVersion());
        toscaCsar.setDescription(toscaResourceStructure.getToscaArtifact().getArtifactDescription());
        toscaCsar.setUrl(toscaResourceStructure.getToscaArtifact().getArtifactURL());

        toscaResourceStructure.setCatalogToscaCsar(toscaCsar);
    }

    private String getServiceInput(ToscaResourceStructure toscaResourceStructure) {
        String serviceInput = null;

        List<Object> serviceInputList;
        ISdcCsarHelper sdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();
        List<Input> serviceInputs = sdcCsarHelper.getServiceInputs();
        if (!serviceInputs.isEmpty()) {
            serviceInputList = new ArrayList<>();
            List<Input> filterList;
            filterList = serviceInputs.stream()
                    .filter(input -> !SKIP_POST_INST_CONF.equals(input.getName())
                            && !CDS_MODEL_NAME.equalsIgnoreCase(input.getName())
                            && !CDS_MODEL_VERSION.equalsIgnoreCase(input.getName())
                            && !CONTROLLER_ACTOR.equalsIgnoreCase(input.getName()))
                    .collect(Collectors.toList());

            filterList.forEach(input -> {
                Map<String, Object> serviceInputMap = new HashMap<>();
                serviceInputMap.put("name", input.getName());
                serviceInputMap.put("type", input.getType());
                serviceInputMap.put("default", input.getDefault() == null ? "" : input.getDefault());
                serviceInputMap.put("required", input.isRequired());
                serviceInputList.add(serviceInputMap);

            });
            try {
                serviceInput = mapper.writeValueAsString(serviceInputList);
                serviceInput = serviceInput.replace("\"", "\\\"");
            } catch (JsonProcessingException e) {
                logger.error("service input could not be deserialized for service uuid:  {}",
                        sdcCsarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            }
        } else {
            logger.debug("serviceInput is null");
        }
        return serviceInput;
    }

    void createServiceArtifact(Service service, VfResourceStructure vfResourceStruct, String artifactContent) {
        if (null == vfResourceStruct) {
            return;
        }
        List<ServiceArtifact> serviceArtifactList = new ArrayList<>();
        ServiceArtifact serviceArtifact;
        List<IArtifactInfo> artifactInfoList = vfResourceStruct.getNotification().getServiceArtifacts().stream()
                .filter(artifact -> artifact.getArtifactType().equalsIgnoreCase("WORKFLOW"))
                .collect(Collectors.toList());
        for (IArtifactInfo artifactInfo : artifactInfoList) {
            serviceArtifact = new ServiceArtifact();
            serviceArtifact.setArtifactUUID(artifactInfo.getArtifactUUID());
            serviceArtifact.setName(artifactInfo.getArtifactName());
            serviceArtifact.setType("OTHER");
            serviceArtifact.setVersion(artifactInfo.getArtifactVersion());
            serviceArtifact.setDescription(artifactInfo.getArtifactDescription());
            serviceArtifact.setChecksum(artifactInfo.getArtifactChecksum());
            serviceArtifact.setContent(artifactContent);
            serviceArtifact.setService(service);
            serviceArtifactList.add(serviceArtifact);
        }
        service.setServiceArtifactList(serviceArtifactList);
    }

    void createServiceInfo(ToscaResourceStructure toscaResourceStruct, Service service) {
        if (!service.getServiceInfos().isEmpty()) {
            return;
        }

        List<ServiceInfo> serviceInfos = new ArrayList<>();

        ServiceInfo serviceInfo = new ServiceInfo();
        String serviceInput = getServiceInput(toscaResourceStruct);
        serviceInfo.setServiceInput(serviceInput);

        String serviceProperties = getServiceProperties(toscaResourceStruct);
        serviceInfo.setServiceProperties(serviceProperties);
        serviceInfo.setService(service);
        serviceInfos.add(serviceInfo);

        service.setServiceInfos(serviceInfos);
    }

    private String getServiceProperties(ToscaResourceStructure toscaResourceStruct) {
        String propertiesJson = null;
        ISdcCsarHelper helper = toscaResourceStruct.getSdcCsarHelper();
        String typeName = helper.getServiceSubstitutionMappingsTypeName();
        Optional<NodeTemplate> nodeTemplate = helper.getServiceNodeTemplates().stream().findAny();

        if (nodeTemplate.isPresent()) {
            String serviceUUID = nodeTemplate.get().getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
            LinkedHashMap<String, Object> customDef = nodeTemplate.get().getCustomDef();
            List<Object> serviceProperties = getPropertiesFromCustomDef(customDef, typeName);

            try {
                propertiesJson = mapper.writeValueAsString(serviceProperties);
                propertiesJson = propertiesJson.replace("\"", "\\\"");
            } catch (JsonProcessingException e) {
                logger.error("serviceProperties could not be deserialized for service uuid:  {}", serviceUUID);
            } catch (Exception ex) {
                logger.error("service properties parsing failed. service uuid:{}", serviceUUID);
            }

        } else {
            logger.debug("ServiceNodeTemplates is null");
        }
        return propertiesJson;
    }

    private static List<Object> getPropertiesFromCustomDef(LinkedHashMap<String, Object> customDef,
            final String typeName) {
        Optional<String> machKey =
                customDef.keySet().stream().filter(key -> key.equalsIgnoreCase(typeName)).findFirst();
        Map<String, Object> servicePropertiesMap;
        List<Object> serviceProperties = new ArrayList<>();

        if (machKey.isPresent()) {
            Object obj = customDef.get(machKey.get());
            if (obj instanceof Map) {
                Object properties = ((HashMap) obj).get("properties");
                if (null != properties) {
                    for (Object propertyName : ((Map) properties).keySet()) {
                        if (propertyName.toString().split("_").length >= 2) {
                            continue;
                        }

                        servicePropertiesMap = new HashMap<>();
                        servicePropertiesMap.put("name", propertyName);
                        Object object = ((Map) properties).get(propertyName);
                        for (Object entry : ((Map) object).entrySet()) {
                            servicePropertiesMap.put((String) ((Map.Entry) entry).getKey(),
                                    ((Map.Entry) entry).getValue());
                        }

                        String type = servicePropertiesMap.get("type").toString();
                        if (type.split("\\.").length >= 2) {
                            List<Object> subProperties = getPropertiesFromCustomDef(customDef, type);
                            if (subProperties.size() > 0) {
                                serviceProperties.addAll(subProperties);
                            }
                            continue;
                        }
                        servicePropertiesMap.remove("description");
                        serviceProperties.add(servicePropertiesMap);
                    }
                }
            }
        }
        return serviceProperties;
    }
}
