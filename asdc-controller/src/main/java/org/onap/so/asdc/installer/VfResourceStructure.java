/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.asdc.installer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.so.logger.MessageEnum;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This structure exists to avoid having issues if the order of the vfResource/vfmodule artifact is not good (tree
 * structure).
 */
public class VfResourceStructure extends ResourceStructure {

    protected static final Logger logger = LoggerFactory.getLogger(VfResourceStructure.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * The list of VfModules defined for this resource.
     */
    private final List<VfModuleStructure> vfModulesStructureList;

    /**
     * The list of VfModulesMetadata defined for this resource.
     */
    private List<VfModuleMetaData> vfModulesMetadataList;

    private VnfResource catalogVnfResource;

    private NetworkResourceCustomization catalogNetworkResourceCustomization;

    private AllottedResourceCustomization catalogResourceCustomization;

    private Service catalogService;


    public VfResourceStructure(INotificationData notificationData, IResourceInstance resourceInstance) {
        super(notificationData, resourceInstance);
        this.resourceType = ResourceType.VF_RESOURCE;
        vfModulesStructureList = new LinkedList<>();
        vfModulesMetadataList = new ArrayList<>();
    }

    public void addArtifactToStructure(IDistributionClient distributionClient, IArtifactInfo artifactInfo,
            IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException {
        this.addArtifactToStructure(artifactInfo, clientResult, null);
    }

    public void addArtifactToStructure(IArtifactInfo artifactInfo, IDistributionClientDownloadResult clientResult,
            String modifiedHeatTemplate) throws UnsupportedEncodingException {
        VfModuleArtifact vfModuleArtifact = new VfModuleArtifact(artifactInfo, clientResult, modifiedHeatTemplate);
        addArtifactByType(artifactInfo, clientResult, vfModuleArtifact);
        if (ASDCConfiguration.VF_MODULES_METADATA.equals(artifactInfo.getArtifactType())) {
            logger.debug("VF_MODULE_ARTIFACT: " + new String(clientResult.getArtifactPayload(), "UTF-8"));
            logger.debug(ASDCNotificationLogging.dumpVfModuleMetaDataList(vfModulesMetadataList));
        }
    }

    public void addWorkflowArtifactToStructure(IArtifactInfo artifactInfo,
            IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException {
        WorkflowArtifact workflowArtifact = new WorkflowArtifact(artifactInfo, clientResult);
        workflowArtifactsMapByUUID.put(artifactInfo.getArtifactUUID(), workflowArtifact);
    }

    protected void addArtifactByType(IArtifactInfo artifactInfo, IDistributionClientDownloadResult clientResult,
            VfModuleArtifact vfModuleArtifact) {

        switch (artifactInfo.getArtifactType()) {
            case ASDCConfiguration.HEAT:
            case ASDCConfiguration.HEAT_ENV:
            case ASDCConfiguration.HEAT_VOL:
            case ASDCConfiguration.HEAT_NESTED: // For 1607 only 1 level tree is supported
            case ASDCConfiguration.HEAT_ARTIFACT:
            case ASDCConfiguration.HEAT_NET:
            case ASDCConfiguration.OTHER:
            case ASDCConfiguration.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT:
            case ASDCConfiguration.HELM:
                if (artifactInfo.getArtifactName().contains("dummy")
                        && artifactInfo.getArtifactName().contains("ignore")) {
                    break;
                }
                artifactsMapByUUID.put(artifactInfo.getArtifactUUID(), vfModuleArtifact);
                break;
            case ASDCConfiguration.VF_MODULES_METADATA:
                vfModulesMetadataList = this.decodeVfModuleArtifact(clientResult.getArtifactPayload());
                break;
            default:
                break;
        }
    }

    public void prepareInstall() throws ArtifactInstallerException {
        createVfModuleStructures();
    }

    public void createVfModuleStructures() throws ArtifactInstallerException {

        // for vender tosca VNF there is no VFModule in VF
        if (vfModulesMetadataList.isEmpty()) {
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_GENERAL_INFO.toString(),
                    "There is no VF mudules in the VF.", "ASDC", "createVfModuleStructures");
            return;
        }
        for (IVfModuleData vfModuleMeta : vfModulesMetadataList) {
            if (vfModuleMeta.getVfModuleModelName().contains("dummy")
                    && vfModuleMeta.getVfModuleModelName().contains("ignore"))
                continue;
            vfModulesStructureList.add(new VfModuleStructure(this, vfModuleMeta));
        }
        setNumberOfResources(vfModulesMetadataList.size());
    }

    public List<VfModuleStructure> getVfModuleStructure() {
        return vfModulesStructureList;
    }

    @Override
    public Map<String, VfModuleArtifact> getArtifactsMapByUUID() {
        return artifactsMapByUUID;
    }

    public List<VfModuleStructure> getVfModulesStructureList() {
        return vfModulesStructureList;
    }

    public VnfResource getCatalogVnfResource() {
        return catalogVnfResource;
    }

    public void setCatalogVnfResource(VnfResource catalogVnfResource) {
        this.catalogVnfResource = catalogVnfResource;
    }

    // Network Only
    public NetworkResourceCustomization getCatalogNetworkResourceCustomization() {
        return catalogNetworkResourceCustomization;
    }

    // Network Only
    public void setCatalogNetworkResourceCustomization(
            NetworkResourceCustomization catalogNetworkResourceCustomization) {
        this.catalogNetworkResourceCustomization = catalogNetworkResourceCustomization;
    }

    public AllottedResourceCustomization getCatalogResourceCustomization() {
        return catalogResourceCustomization;
    }

    public void setCatalogResourceCustomization(AllottedResourceCustomization catalogResourceCustomization) {
        this.catalogResourceCustomization = catalogResourceCustomization;
    }

    public Service getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(Service catalogService) {
        this.catalogService = catalogService;
    }

    public List<VfModuleMetaData> decodeVfModuleArtifact(byte[] arg0) {
        try {
            return mapper.readValue(arg0, new TypeReference<List<VfModuleMetaData>>() {});

        } catch (JsonParseException e) {
            logger.debug("JsonParseException : ", e);
        } catch (JsonMappingException e) {
            logger.debug("JsonMappingException : ", e);
        } catch (IOException e) {
            logger.debug("IOException : ", e);
        }
        return new ArrayList<>();
    }
}
