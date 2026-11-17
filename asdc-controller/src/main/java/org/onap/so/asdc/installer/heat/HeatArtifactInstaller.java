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
import java.util.List;
import java.util.Set;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.installer.BigDecimalVersion;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleArtifact;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.util.YamlEditor;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.data.repository.HeatEnvironmentRepository;
import org.onap.so.db.catalog.data.repository.HeatFilesRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HeatArtifactInstaller {

    private static final Logger logger = LoggerFactory.getLogger(HeatArtifactInstaller.class);

    private final HeatTemplateRepository heatRepo;
    private final HeatEnvironmentRepository heatEnvRepo;
    private final HeatFilesRepository heatFilesRepo;

    public HeatArtifactInstaller(HeatTemplateRepository heatRepo, HeatEnvironmentRepository heatEnvRepo,
            HeatFilesRepository heatFilesRepo) {
        this.heatRepo = heatRepo;
        this.heatEnvRepo = heatEnvRepo;
        this.heatFilesRepo = heatFilesRepo;
    }

    protected void extractHeatInformation(ToscaResourceStructure toscaResourceStruct,
            VfResourceStructure vfResourceStructure) {
        for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

            switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
                case ASDCConfiguration.HEAT:
                case ASDCConfiguration.HEAT_NESTED:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_VOL:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    VfModuleArtifact envModuleArtifact =
                            getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
                    createHeatEnvFromArtifact(vfResourceStructure, envModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ENV:
                    createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ARTIFACT:
                    createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact, toscaResourceStruct);
                    break;
                case ASDCConfiguration.HEAT_NET:
                case ASDCConfiguration.OTHER:
                case ASDCConfiguration.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT:
                    logger.warn(LoggingAnchor.FOUR, MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT.toString(),
                            vfModuleArtifact.getArtifactInfo().getArtifactType() + "(Artifact Name:"
                                    + vfModuleArtifact.getArtifactInfo().getArtifactName() + ")",
                            ErrorCode.DataError.getValue(), "Artifact type not supported");
                    break;
                default:
                    break;

            }
        }
    }

    protected VfModuleArtifact getHeatEnvArtifactFromGeneratedArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact) {
        String artifactName = vfModuleArtifact.getArtifactInfo().getArtifactName();
        artifactName = artifactName.substring(0, artifactName.indexOf('.'));
        for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {
            if (moduleArtifact.getArtifactInfo().getArtifactName().contains(artifactName)
                    && moduleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
                return moduleArtifact;
            }
        }
        return null;
    }

    public String verifyTheFilePrefixInArtifacts(String filebody, VfResourceStructure vfResourceStructure,
            List<String> listTypes) {
        String newFileBody = filebody;
        for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

            if (listTypes.contains(moduleArtifact.getArtifactInfo().getArtifactType())) {

                newFileBody =
                        verifyTheFilePrefixInString(newFileBody, moduleArtifact.getArtifactInfo().getArtifactName());
            }
        }
        return newFileBody;
    }

    public String verifyTheFilePrefixInString(final String body, final String filenameToVerify) {

        String needlePrefix = "file:///";
        String prefixedFilenameToVerify = needlePrefix + filenameToVerify;

        if ((body == null) || (body.length() == 0) || (filenameToVerify == null) || (filenameToVerify.length() == 0)) {
            return body;
        }

        StringBuilder sb = new StringBuilder(body.length());

        int currentIndex = 0;
        int startIndex = 0;

        while (currentIndex != -1) {
            startIndex = currentIndex;
            currentIndex = body.indexOf(prefixedFilenameToVerify, startIndex);

            if (currentIndex == -1) {
                break;
            }
            // We append from the startIndex up to currentIndex (start of File
            // Name)
            sb.append(body.substring(startIndex, currentIndex));
            sb.append(filenameToVerify);

            currentIndex += prefixedFilenameToVerify.length();
        }

        sb.append(body.substring(startIndex));

        return sb.toString();
    }

    protected void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,
            ToscaResourceStructure toscaResourceStruct, VfModuleArtifact vfModuleArtifact) {

        HeatTemplate existingHeatTemplate =
                heatRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatTemplate == null) {
            HeatTemplate heatTemplate = new HeatTemplate();
            List<String> typeList = new ArrayList<>();
            typeList.add(ASDCConfiguration.HEAT_NESTED);
            typeList.add(ASDCConfiguration.HEAT_ARTIFACT);

            heatTemplate.setTemplateBody(
                    verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
            heatTemplate.setTemplateName(vfModuleArtifact.getArtifactInfo().getArtifactName());

            if (vfModuleArtifact.getArtifactInfo().getArtifactTimeout() != null) {
                heatTemplate.setTimeoutMinutes(vfModuleArtifact.getArtifactInfo().getArtifactTimeout());
            } else {
                heatTemplate.setTimeoutMinutes(240);
            }

            heatTemplate.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
            heatTemplate.setVersion(BigDecimalVersion
                    .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
            heatTemplate.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

            if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
                heatTemplate.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
            } else {
                heatTemplate.setArtifactChecksum(MANUAL_RECORD);
            }

            Set<HeatTemplateParam> heatParam = extractHeatTemplateParameters(vfModuleArtifact.getResult(),
                    vfModuleArtifact.getArtifactInfo().getArtifactUUID());
            heatTemplate.setParameters(heatParam);
            vfModuleArtifact.setHeatTemplate(heatTemplate);
        } else {
            if (vfModuleArtifact.getArtifactInfo().getArtifactTimeout() != null) {
                existingHeatTemplate.setTimeoutMinutes(vfModuleArtifact.getArtifactInfo().getArtifactTimeout());
            }
            vfModuleArtifact.setHeatTemplate(existingHeatTemplate);
        }
    }

    protected void createHeatEnvFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact) {

        HeatEnvironment existingHeatEnvironment =
                heatEnvRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatEnvironment == null) {
            HeatEnvironment heatEnvironment = new HeatEnvironment();
            heatEnvironment.setName(vfModuleArtifact.getArtifactInfo().getArtifactName());
            List<String> typeList = new ArrayList<>();
            typeList.add(ASDCConfiguration.HEAT);
            typeList.add(ASDCConfiguration.HEAT_VOL);
            heatEnvironment.setEnvironment(
                    verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
            heatEnvironment.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
            heatEnvironment.setVersion(BigDecimalVersion
                    .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
            heatEnvironment.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

            if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
                heatEnvironment.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
            } else {
                heatEnvironment.setArtifactChecksum(MANUAL_RECORD);
            }
            vfModuleArtifact.setHeatEnvironment(heatEnvironment);
        } else {
            vfModuleArtifact.setHeatEnvironment(existingHeatEnvironment);
        }
    }

    protected void createHeatFileFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact, ToscaResourceStructure toscaResourceStruct) {

        HeatFiles existingHeatFiles =
                heatFilesRepo.findByArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (existingHeatFiles == null) {
            HeatFiles heatFile = new HeatFiles();
            heatFile.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
            heatFile.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
            heatFile.setFileBody(vfModuleArtifact.getResult());
            heatFile.setFileName(vfModuleArtifact.getArtifactInfo().getArtifactName());
            heatFile.setVersion(BigDecimalVersion
                    .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
            toscaResourceStruct.setHeatFilesUUID(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
            if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
                heatFile.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
            } else {
                heatFile.setArtifactChecksum(MANUAL_RECORD);
            }
            vfModuleArtifact.setHeatFiles(heatFile);
        } else {
            vfModuleArtifact.setHeatFiles(existingHeatFiles);
        }
    }

    protected Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {
        // Scan the payload downloadResult and extract the HeatTemplate
        // parameters
        YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
        return yamlEditor.getParameterList(artifactUUID);
    }

    protected static String identifyParentOfNestedTemplate(VfModuleStructure vfModuleStructure,
            VfModuleArtifact heatNestedArtifact) {

        if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT) != null && vfModuleStructure
                .getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).getArtifactInfo().getRelatedArtifacts() != null) {
            for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0)
                    .getArtifactInfo().getRelatedArtifacts()) {
                if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
                    return ASDCConfiguration.HEAT;
                }

            }
        }

        if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL) != null
                && vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo()
                        .getRelatedArtifacts() != null) {
            for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL)
                    .get(0).getArtifactInfo().getRelatedArtifacts()) {
                if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
                    return ASDCConfiguration.HEAT_VOL;
                }

            }
        }

        // Does not belong to anything
        return null;

    }

    protected static String createVNFName(VfResourceStructure vfResourceStructure) {

        return vfResourceStructure.getNotification().getServiceName() + "/"
                + vfResourceStructure.getResourceInstance().getResourceInstanceName();
    }

    protected static String createVfModuleName(VfModuleStructure vfModuleStructure) {

        return createVNFName(vfModuleStructure.getParentVfResource()) + "::"
                + vfModuleStructure.getVfModuleMetadata().getVfModuleModelName();
    }
}
