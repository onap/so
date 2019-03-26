/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.asdc.installer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    /**
     * The list of VfModules defined for this resource.
     */
    private final List<VfModuleStructure> vfModulesStructureList;

    /**
     * The list of VfModulesMetadata defined for this resource.
     */
    private List<IVfModuleData> vfModulesMetadataList;

    private VnfResource catalogVnfResource;

    private NetworkResourceCustomization catalogNetworkResourceCustomization;

    private AllottedResourceCustomization catalogResourceCustomization;

    private Service catalogService;


    public VfResourceStructure(INotificationData notificationdata, IResourceInstance resourceinstance) {
        super(notificationdata, resourceinstance);
        this.resourceType = ResourceType.VF_RESOURCE;
        vfModulesStructureList = new LinkedList<>();
    }

    public void addArtifactToStructure(IDistributionClient distributionClient, IArtifactInfo artifactinfo,
        IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException {
        this.addArtifactToStructure(distributionClient, artifactinfo, clientResult, null);
    }

    public void addArtifactToStructure(IDistributionClient distributionClient, IArtifactInfo artifactinfo,
        IDistributionClientDownloadResult clientResult, String modifiedHeatTemplate)
        throws UnsupportedEncodingException {
        VfModuleArtifact vfModuleArtifact = new VfModuleArtifact(artifactinfo, clientResult, modifiedHeatTemplate);
        addArtifactByType(artifactinfo, clientResult, vfModuleArtifact);
        if (ASDCConfiguration.VF_MODULES_METADATA.equals(artifactinfo.getArtifactType())) {
            logger.debug("VF_MODULE_ARTIFACT: " + new String(clientResult.getArtifactPayload(), "UTF-8"));
            logger.debug(ASDCNotificationLogging.dumpVfModuleMetaDataList(vfModulesMetadataList));
        }
    }

    protected void addArtifactByType(IArtifactInfo artifactinfo, IDistributionClientDownloadResult clientResult,
        VfModuleArtifact vfModuleArtifact) throws UnsupportedEncodingException {

        switch (artifactinfo.getArtifactType()) {
            case ASDCConfiguration.HEAT:
            case ASDCConfiguration.HEAT_ENV:
            case ASDCConfiguration.HEAT_VOL:
            case ASDCConfiguration.HEAT_NESTED:    // For 1607 only 1 level tree is supported
            case ASDCConfiguration.HEAT_ARTIFACT:
            case ASDCConfiguration.HEAT_NET:
            case ASDCConfiguration.OTHER:
                artifactsMapByUUID.put(artifactinfo.getArtifactUUID(), vfModuleArtifact);
                break;
            case ASDCConfiguration.VF_MODULES_METADATA:
                vfModulesMetadataList = this.decodeVfModuleArtifact(clientResult.getArtifactPayload());
                break;
            default:
                break;
        }
    }

    public void prepareInstall() throws ArtifactInstallerException{
        createVfModuleStructures();
    }

    public void createVfModuleStructures() throws ArtifactInstallerException {

        //for vender tosca VNF there is no VFModule in VF
        if (vfModulesMetadataList == null) {
            logger.info("{} {} {} {}", MessageEnum.ASDC_GENERAL_INFO.toString(), "There is no VF mudules in the VF.",
                "ASDC",
                "createVfModuleStructures");
            return;
        }
        for (IVfModuleData vfModuleMeta : vfModulesMetadataList) {
            vfModulesStructureList.add(new VfModuleStructure(this, vfModuleMeta));
        }
        setNumberOfResources(vfModulesMetadataList.size());
    }

    public List<VfModuleStructure> getVfModuleStructure() {
        return vfModulesStructureList;
    }

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

    public void setCatalogResourceCustomization(
        AllottedResourceCustomization catalogResourceCustomization) {
        this.catalogResourceCustomization = catalogResourceCustomization;
    }

    public Service getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(Service catalogService) {
        this.catalogService = catalogService;
    }

    public List<IVfModuleData> decodeVfModuleArtifact(byte[] arg0) {
        try {
            List<IVfModuleData> listVFModuleMetaData = new ObjectMapper()
                .readValue(arg0, new TypeReference<List<VfModuleMetaData>>() {
                });
            return listVFModuleMetaData;

        } catch (JsonParseException e) {
            logger.debug("JsonParseException : ", e);
        } catch (JsonMappingException e) {
            logger.debug("JsonMappingException : ", e);
        } catch (IOException e) {
            logger.debug("IOException : ", e);
        }
        return null;
    }
}
