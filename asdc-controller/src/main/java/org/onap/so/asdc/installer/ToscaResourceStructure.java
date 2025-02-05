/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkInstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToscaResourceStructure {

    protected static final Logger logger = LoggerFactory.getLogger(ToscaResourceStructure.class);

    /**
     * mso config path, used for the config files, like download csar files.
     */
    private String msoConfigPath;

    Metadata serviceMetadata;
    private Service catalogService;
    ISdcCsarHelper sdcCsarHelper;
    List<NodeTemplate> allottedList;
    List<NodeTemplate> networkTypes;
    List<NodeTemplate> vfTypes;
    String heatTemplateUUID;
    String volHeatTemplateUUID;
    String volHeatEnvTemplateUUID;
    String envHeatTemplateUUID;
    String heatFilesUUID;
    String workloadPerformance;
    String serviceVersion;
    private boolean isDeployedSuccessfully = false;


    private NetworkResourceCustomization catalogNetworkResourceCustomization;

    private NetworkResource catalogNetworkResource;

    private List<NetworkInstanceGroup> catalogNetworkInstanceGroup;

    private CollectionNetworkResourceCustomization catalogCollectionNetworkResourceCustomization;

    private CollectionResource catalogCollectionResource;

    private CollectionResourceCustomization catalogCollectionResourceCustomization;

    private NetworkCollectionResourceCustomization catalogNetworkCollectionResourceCustomization;

    private ServiceProxyResourceCustomization catalogServiceProxyResourceCustomization;

    private ConfigurationResource catalogConfigurationResource;

    private ConfigurationResourceCustomization catalogConfigurationResourceCustomization;

    private AllottedResourceCustomization catalogResourceCustomization;

    private VfModule vfModule;

    private VfModuleCustomization vfModuleCustomization;

    private VnfResourceCustomization vnfResourceCustomization;

    private PnfResourceCustomization pnfResourceCustomization;

    private AllottedResource allottedResource;

    private AllottedResourceCustomization allottedResourceCustomization;

    private TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup;

    private IArtifactInfo toscaArtifact;

    private ToscaCsar toscaCsar;

    public ToscaResourceStructure() {
        this(System.getProperty("mso.config.path"));
    }

    public ToscaResourceStructure(final String msoConfigPath) {
        this.msoConfigPath = msoConfigPath;
        logger.info("MSO config path is: {}", msoConfigPath);
    }

    public void updateResourceStructure(IArtifactInfo artifact) throws ASDCDownloadException {

        try {
            SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();// Autoclosable

            String filePath =
                    Paths.get(msoConfigPath, "ASDC", artifact.getArtifactVersion(), artifact.getArtifactName())
                            .normalize().toString();

            File spoolFile = new File(filePath);

            logger.debug("ASDC File path is: {}", spoolFile.getAbsolutePath());
            logger.info(LoggingAnchor.FOUR, MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF.toString(), "***PATH", "ASDC",
                    spoolFile.getAbsolutePath());

            sdcCsarHelper = factory.getSdcCsarHelper(spoolFile.getAbsolutePath(), false);

        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            logger.error(LoggingAnchor.SIX, MessageEnum.ASDC_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception caught during parser *****LOOK********* " + artifact.getArtifactName(), "ASDC",
                    "processResourceNotification", ErrorCode.BusinessProcessError.getValue(),
                    "Exception in " + "processResourceNotification", e);

            throw new ASDCDownloadException("Exception caught when passing the csar file to the parser ", e);
        }

        serviceMetadata = sdcCsarHelper.getServiceMetadata();

    }

    public String getHeatTemplateUUID() {
        return heatTemplateUUID;
    }

    public void setHeatTemplateUUID(String heatTemplateUUID) {
        this.heatTemplateUUID = heatTemplateUUID;
    }

    public List<NodeTemplate> getAllottedList() {
        return allottedList;
    }

    public void setAllottedList(List<NodeTemplate> allottedList) {
        this.allottedList = allottedList;
    }

    public ISdcCsarHelper getSdcCsarHelper() {
        return sdcCsarHelper;
    }

    public void setSdcCsarHelper(ISdcCsarHelper sdcCsarHelper) {
        this.sdcCsarHelper = sdcCsarHelper;
    }

    public Metadata getServiceMetadata() {
        return serviceMetadata;
    }

    public Service getCatalogService() {
        return catalogService;
    }

    public void setServiceMetadata(Metadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public void setCatalogService(Service catalogService) {
        this.catalogService = catalogService;
    }

    public List<NodeTemplate> getNetworkTypes() {
        return networkTypes;
    }

    public void setNetworkTypes(List<NodeTemplate> networkTypes) {
        this.networkTypes = networkTypes;
    }

    public List<NodeTemplate> getVfTypes() {
        return vfTypes;
    }

    public void setVfTypes(List<NodeTemplate> vfTypes) {
        this.vfTypes = vfTypes;
    }

    public AllottedResourceCustomization getCatalogResourceCustomization() {
        return catalogResourceCustomization;
    }

    public void setCatalogResourceCustomization(AllottedResourceCustomization catalogResourceCustomization) {
        this.catalogResourceCustomization = catalogResourceCustomization;
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

    public NetworkResource getCatalogNetworkResource() {
        return catalogNetworkResource;
    }

    public void setCatalogNetworkResource(NetworkResource catalogNetworkResource) {
        this.catalogNetworkResource = catalogNetworkResource;
    }

    public VfModule getCatalogVfModule() {
        return vfModule;
    }

    public void setCatalogVfModule(VfModule vfModule) {
        this.vfModule = vfModule;
    }
    /*
     * public VnfResource getCatalogVnfResource() { return vnfResource; }
     * 
     * public void setCatalogVnfResource(VnfResource vnfResource) { this.vnfResource = vnfResource; }
     * 
     */

    public VnfResourceCustomization getCatalogVnfResourceCustomization() {
        return vnfResourceCustomization;
    }

    public void setCatalogVnfResourceCustomization(VnfResourceCustomization vnfResourceCustomization) {
        this.vnfResourceCustomization = vnfResourceCustomization;
    }

    public PnfResourceCustomization getPnfResourceCustomization() {
        return pnfResourceCustomization;
    }

    public void setPnfResourceCustomization(PnfResourceCustomization pnfResourceCustomization) {
        this.pnfResourceCustomization = pnfResourceCustomization;
    }


    public VfModuleCustomization getCatalogVfModuleCustomization() {
        return vfModuleCustomization;
    }

    public void setCatalogVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
        this.vfModuleCustomization = vfModuleCustomization;
    }

    public AllottedResource getAllottedResource() {
        return allottedResource;
    }

    public void setAllottedResource(AllottedResource allottedResource) {
        this.allottedResource = allottedResource;
    }

    public AllottedResourceCustomization getCatalogAllottedResourceCustomization() {
        return allottedResourceCustomization;
    }

    public void setCatalogAllottedResourceCustomization(AllottedResourceCustomization allottedResourceCustomization) {
        this.allottedResourceCustomization = allottedResourceCustomization;
    }

    public TempNetworkHeatTemplateLookup getCatalogTempNetworkHeatTemplateLookup() {
        return tempNetworkHeatTemplateLookup;
    }

    public void setCatalogTempNetworkHeatTemplateLookup(TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup) {
        this.tempNetworkHeatTemplateLookup = tempNetworkHeatTemplateLookup;
    }

    public String getHeatFilesUUID() {
        return heatFilesUUID;
    }

    public void setHeatFilesUUID(String heatFilesUUID) {
        this.heatFilesUUID = heatFilesUUID;
    }

    public IArtifactInfo getToscaArtifact() {
        return toscaArtifact;
    }

    public void setToscaArtifact(IArtifactInfo toscaArtifact) {
        this.toscaArtifact = toscaArtifact;
    }

    public ToscaCsar getCatalogToscaCsar() {
        return toscaCsar;
    }

    public void setCatalogToscaCsar(ToscaCsar toscaCsar) {
        this.toscaCsar = toscaCsar;
    }

    public String getVolHeatTemplateUUID() {
        return volHeatTemplateUUID;
    }

    public void setVolHeatTemplateUUID(String volHeatTemplateUUID) {
        this.volHeatTemplateUUID = volHeatTemplateUUID;
    }

    public String getEnvHeatTemplateUUID() {
        return envHeatTemplateUUID;
    }

    public void setEnvHeatTemplateUUID(String envHeatTemplateUUID) {
        this.envHeatTemplateUUID = envHeatTemplateUUID;
    }

    public String getVolHeatEnvTemplateUUID() {
        return volHeatEnvTemplateUUID;
    }

    public void setVolHeatEnvTemplateUUID(String volHeatEnvTemplateUUID) {
        this.volHeatEnvTemplateUUID = volHeatEnvTemplateUUID;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getWorkloadPerformance() {
        return workloadPerformance;
    }

    public void setWorkloadPerformance(String workloadPerformance) {
        this.workloadPerformance = workloadPerformance;
    }

    public VfModule getVfModule() {
        return vfModule;
    }

    public void setVfModule(VfModule vfModule) {
        this.vfModule = vfModule;
    }

    public VfModuleCustomization getVfModuleCustomization() {
        return vfModuleCustomization;
    }

    public void setVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
        this.vfModuleCustomization = vfModuleCustomization;
    }


    public VnfResourceCustomization getVnfResourceCustomization() {
        return vnfResourceCustomization;
    }

    public void setVnfResourceCustomization(VnfResourceCustomization vnfResourceCustomization) {
        this.vnfResourceCustomization = vnfResourceCustomization;
    }

    public AllottedResourceCustomization getAllottedResourceCustomization() {
        return allottedResourceCustomization;
    }

    public void setAllottedResourceCustomization(AllottedResourceCustomization allottedResourceCustomization) {
        this.allottedResourceCustomization = allottedResourceCustomization;
    }

    public TempNetworkHeatTemplateLookup getTempNetworkHeatTemplateLookup() {
        return tempNetworkHeatTemplateLookup;
    }

    public void setTempNetworkHeatTemplateLookup(TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup) {
        this.tempNetworkHeatTemplateLookup = tempNetworkHeatTemplateLookup;
    }

    public List<NetworkInstanceGroup> getCatalogNetworkInstanceGroup() {
        return catalogNetworkInstanceGroup;
    }

    public void setCatalogNetworkInstanceGroup(List<NetworkInstanceGroup> catalogNetworkInstanceGroup) {
        this.catalogNetworkInstanceGroup = catalogNetworkInstanceGroup;
    }

    public CollectionNetworkResourceCustomization getCatalogCollectionNetworkResourceCustomization() {
        return catalogCollectionNetworkResourceCustomization;
    }

    public void setCatalogCollectionNetworkResourceCustomization(
            CollectionNetworkResourceCustomization catalogCollectionNetworkResourceCustomization) {
        this.catalogCollectionNetworkResourceCustomization = catalogCollectionNetworkResourceCustomization;
    }

    public CollectionResource getCatalogCollectionResource() {
        return catalogCollectionResource;
    }

    public void setCatalogCollectionResource(CollectionResource catalogCollectionResource) {
        this.catalogCollectionResource = catalogCollectionResource;
    }

    public CollectionResourceCustomization getCatalogCollectionResourceCustomization() {
        return catalogCollectionResourceCustomization;
    }

    public void setCatalogCollectionResourceCustomization(
            CollectionResourceCustomization catalogCollectionResourceCustomization) {
        this.catalogCollectionResourceCustomization = catalogCollectionResourceCustomization;
    }

    public NetworkCollectionResourceCustomization getCatalogNetworkCollectionResourceCustomization() {
        return catalogNetworkCollectionResourceCustomization;
    }

    public void setCatalogNetworkCollectionResourceCustomization(
            NetworkCollectionResourceCustomization catalogNetworkCollectionResourceCustomization) {
        this.catalogNetworkCollectionResourceCustomization = catalogNetworkCollectionResourceCustomization;
    }

    public ServiceProxyResourceCustomization getCatalogServiceProxyResourceCustomization() {
        return catalogServiceProxyResourceCustomization;
    }

    public void setCatalogServiceProxyResourceCustomization(
            ServiceProxyResourceCustomization catalogServiceProxyResourceCustomization) {
        this.catalogServiceProxyResourceCustomization = catalogServiceProxyResourceCustomization;
    }

    public ConfigurationResource getCatalogConfigurationResource() {
        return catalogConfigurationResource;
    }

    public void setCatalogConfigurationResource(ConfigurationResource catalogConfigurationResource) {
        this.catalogConfigurationResource = catalogConfigurationResource;
    }

    public ConfigurationResourceCustomization getCatalogConfigurationResourceCustomization() {
        return catalogConfigurationResourceCustomization;
    }

    public void setCatalogConfigurationResourceCustomization(
            ConfigurationResourceCustomization catalogConfigurationResourceCustomization) {
        this.catalogConfigurationResourceCustomization = catalogConfigurationResourceCustomization;
    }

    public ToscaCsar getToscaCsar() {
        return toscaCsar;
    }

    public void setToscaCsar(ToscaCsar toscaCsar) {
        this.toscaCsar = toscaCsar;
    }

    public boolean isDeployedSuccessfully() {
        return isDeployedSuccessfully;
    }

    public void setSuccessfulDeployment() {
        isDeployedSuccessfully = true;
    }

}
