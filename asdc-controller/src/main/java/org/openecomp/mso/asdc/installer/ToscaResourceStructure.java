/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.asdc.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import java.util.List;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
//import org.openecomp.generic.tosca.parser.model.Metadata;
//import org.openecomp.sdc.tosca.parser.factory.SdcCsarHelperFactory;
//import org.openecomp.sdc.tosca.parser.factory.SdcCsarHelperFactory;
///import org.openecomp.generic.tosca.parser.model.Metadata;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.impl.SdcCsarHelperImpl;
import org.openecomp.sdc.tosca.parser.impl.SdcPropertyNames;


import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;



import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.mso.asdc.client.exceptions.ASDCDownloadException;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfResCustomToVfModuleCustom;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public class ToscaResourceStructure {
	
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
	boolean isVnfAlreadyInstalled = false;
	String serviceVersion;
	private boolean isDeployedSuccessfully=false;
	
	
	private NetworkResourceCustomization catalogNetworkResourceCustomization;
	
	private NetworkResource catalogNetworkResource;
		
	private AllottedResourceCustomization catalogResourceCustomization;
	
	private VfModule vfModule;
	
	private VfModuleCustomization vfModuleCustomization;
	
	private VnfResource vnfResource;
	
	private VnfResourceCustomization vnfResourceCustomization;
	
	private ServiceToResourceCustomization serviceToResourceCustomization;
	
	private AllottedResource allottedResource;
	
	private AllottedResourceCustomization allottedResourceCustomization;
	
	private VnfResCustomToVfModuleCustom vnfResCustomToVfModuleCustom;
	
	private TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup;
	
	private VfModuleToHeatFiles vfModuleToHeatFiles;
	
	private IArtifactInfo toscaArtifact;
	
	private ToscaCsar toscaCsar;
	
	private ServiceToResourceCustomization vfServiceToResourceCustomization;
	
	private ServiceToResourceCustomization allottedServiceToResourceCustomization;
	
	private ServiceToResourceCustomization vlServiceToResourceCustomization;
	
	protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
		
	
	public ToscaResourceStructure(){
	}
	
	public void updateResourceStructure(IArtifactInfo artifact) throws ASDCDownloadException {
		
				
		try {
				
			SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();//Autoclosable
			
			LOGGER.debug("MSO config path is: " + System.getProperty("mso.config.path"));
			
			File spoolFile = new File(System.getProperty("mso.config.path") + "ASDC/" + artifact.getArtifactName());
 
			LOGGER.debug("ASDC File path is: " + spoolFile.getAbsolutePath());
			LOGGER.info(MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF, "***PATH", "ASDC", spoolFile.getAbsolutePath());
			

			sdcCsarHelper = factory.getSdcCsarHelper(spoolFile.getAbsolutePath());

		}catch(Exception e){
			System.out.println("System out " + e.getMessage());
			LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
					"Exception caught during parser *****LOOK********* " + artifact.getArtifactName(), "ASDC", "processResourceNotification", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in processResourceNotification", e);
			
			throw new ASDCDownloadException ("Exception caught when passing the csar file to the parser ", e);
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

	public void setCatalogResourceCustomization(
			AllottedResourceCustomization catalogResourceCustomization) {
		this.catalogResourceCustomization = catalogResourceCustomization;
	}
	
	// Network Only
	public NetworkResourceCustomization getCatalogNetworkResourceCustomization() {
		return catalogNetworkResourceCustomization;
	}
	// Network Only
	public void setCatalogNetworkResourceCustomization(NetworkResourceCustomization catalogNetworkResourceCustomization) {
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

	public VnfResource getCatalogVnfResource() {
		return vnfResource;
	}

	public void setCatalogVnfResource(VnfResource vnfResource) {
		this.vnfResource = vnfResource;
	}

	public VnfResourceCustomization getCatalogVnfResourceCustomization() {
		return vnfResourceCustomization;
	}

	public void setCatalogVnfResourceCustomization(
			VnfResourceCustomization vnfResourceCustomization) {
		this.vnfResourceCustomization = vnfResourceCustomization;
	}

	public VfModuleCustomization getCatalogVfModuleCustomization() {
		return vfModuleCustomization;
	}

	public void setCatalogVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
		this.vfModuleCustomization = vfModuleCustomization;
	}

	public ServiceToResourceCustomization getServiceToResourceCustomization() {
		return serviceToResourceCustomization;
	}

	public void setServiceToResourceCustomization(
			ServiceToResourceCustomization serviceToResourceCustomization) {
		this.serviceToResourceCustomization = serviceToResourceCustomization;
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

	public void setCatalogAllottedResourceCustomization(
			AllottedResourceCustomization allottedResourceCustomization) {
		this.allottedResourceCustomization = allottedResourceCustomization;
	}

	public VnfResCustomToVfModuleCustom getCatalogVnfResCustomToVfModuleCustom() {
		return vnfResCustomToVfModuleCustom;
	}

	public void setCatalogVnfResCustomToVfModuleCustom(
			VnfResCustomToVfModuleCustom vnfResCustomToVfModuleCustom) {
		this.vnfResCustomToVfModuleCustom = vnfResCustomToVfModuleCustom;
	}

	public TempNetworkHeatTemplateLookup getCatalogTempNetworkHeatTemplateLookup() {
		return tempNetworkHeatTemplateLookup;
	}

	public void setCatalogTempNetworkHeatTemplateLookup(
			TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup) {
		this.tempNetworkHeatTemplateLookup = tempNetworkHeatTemplateLookup;
	}

	public String getHeatFilesUUID() {
		return heatFilesUUID;
	}

	public void setHeatFilesUUID(String heatFilesUUID) {
		this.heatFilesUUID = heatFilesUUID;
	}

	public VfModuleToHeatFiles getCatalogVfModuleToHeatFiles() {
		return vfModuleToHeatFiles;
	}

	public void setCatalogVfModuleToHeatFiles(VfModuleToHeatFiles vfModuleToHeatFiles) {
		this.vfModuleToHeatFiles = vfModuleToHeatFiles;
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

	public boolean isVnfAlreadyInstalled() {
		return isVnfAlreadyInstalled;
	}

	public void setVnfAlreadyInstalled(boolean isVnfAlreadyInstalled) {
		this.isVnfAlreadyInstalled = isVnfAlreadyInstalled;
	}

	public ServiceToResourceCustomization getCatalogVfServiceToResourceCustomization() {
		return vfServiceToResourceCustomization;
	}

	public void setCatalogVfServiceToResourceCustomization(
			ServiceToResourceCustomization vfServiceToResourceCustomization) {
		this.vfServiceToResourceCustomization = vfServiceToResourceCustomization;
	}

	public ServiceToResourceCustomization getCatalogAllottedServiceToResourceCustomization() {
		return allottedServiceToResourceCustomization;
	}

	public void setCatalogAllottedServiceToResourceCustomization(
			ServiceToResourceCustomization allottedServiceToResourceCustomization) {
		this.allottedServiceToResourceCustomization = allottedServiceToResourceCustomization;
	}

	public ServiceToResourceCustomization getCatalogVlServiceToResourceCustomization() {
		return vlServiceToResourceCustomization;
	}

	public void setCatalogVlServiceToResourceCustomization(
			ServiceToResourceCustomization vlServiceToResourceCustomization) {
		this.vlServiceToResourceCustomization = vlServiceToResourceCustomization;
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

	public VnfResource getVnfResource() {
		return vnfResource;
	}

	public void setVnfResource(VnfResource vnfResource) {
		this.vnfResource = vnfResource;
	}

	public VnfResourceCustomization getVnfResourceCustomization() {
		return vnfResourceCustomization;
	}

	public void setVnfResourceCustomization(
			VnfResourceCustomization vnfResourceCustomization) {
		this.vnfResourceCustomization = vnfResourceCustomization;
	}

	public AllottedResourceCustomization getAllottedResourceCustomization() {
		return allottedResourceCustomization;
	}

	public void setAllottedResourceCustomization(
			AllottedResourceCustomization allottedResourceCustomization) {
		this.allottedResourceCustomization = allottedResourceCustomization;
	}

	public VnfResCustomToVfModuleCustom getVnfResCustomToVfModuleCustom() {
		return vnfResCustomToVfModuleCustom;
	}

	public void setVnfResCustomToVfModuleCustom(
			VnfResCustomToVfModuleCustom vnfResCustomToVfModuleCustom) {
		this.vnfResCustomToVfModuleCustom = vnfResCustomToVfModuleCustom;
	}

	public TempNetworkHeatTemplateLookup getTempNetworkHeatTemplateLookup() {
		return tempNetworkHeatTemplateLookup;
	}

	public void setTempNetworkHeatTemplateLookup(
			TempNetworkHeatTemplateLookup tempNetworkHeatTemplateLookup) {
		this.tempNetworkHeatTemplateLookup = tempNetworkHeatTemplateLookup;
	}

	public VfModuleToHeatFiles getVfModuleToHeatFiles() {
		return vfModuleToHeatFiles;
	}

	public void setVfModuleToHeatFiles(VfModuleToHeatFiles vfModuleToHeatFiles) {
		this.vfModuleToHeatFiles = vfModuleToHeatFiles;
	}

	public ToscaCsar getToscaCsar() {
		return toscaCsar;
	}

	public void setToscaCsar(ToscaCsar toscaCsar) {
		this.toscaCsar = toscaCsar;
	}

	public ServiceToResourceCustomization getVfServiceToResourceCustomization() {
		return vfServiceToResourceCustomization;
	}

	public void setVfServiceToResourceCustomization(
			ServiceToResourceCustomization vfServiceToResourceCustomization) {
		this.vfServiceToResourceCustomization = vfServiceToResourceCustomization;
	}

	public ServiceToResourceCustomization getAllottedServiceToResourceCustomization() {
		return allottedServiceToResourceCustomization;
	}

	public void setAllottedServiceToResourceCustomization(
			ServiceToResourceCustomization allottedServiceToResourceCustomization) {
		this.allottedServiceToResourceCustomization = allottedServiceToResourceCustomization;
	}

	public ServiceToResourceCustomization getVlServiceToResourceCustomization() {
		return vlServiceToResourceCustomization;
	}

	public void setVlServiceToResourceCustomization(
			ServiceToResourceCustomization vlServiceToResourceCustomization) {
		this.vlServiceToResourceCustomization = vlServiceToResourceCustomization;
	}

	public static MsoLogger getLogger() {
		return LOGGER;
	}
	
	public boolean isDeployedSuccessfully() {
		return isDeployedSuccessfully;
	}

	public void setSuccessfulDeployment() {
		isDeployedSuccessfully = true;
	}

}
