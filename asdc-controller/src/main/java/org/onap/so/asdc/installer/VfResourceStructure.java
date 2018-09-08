/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.logger.MsoLogger;
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
/**
 * This structure exists to avoid having issues if the order of the vfResource/vfmodule artifact is not good (tree structure).
 *
 */


public class VfResourceStructure {
	
	protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC, VfResourceStructure.class);

	private boolean isDeployedSuccessfully=false;
	/**
	 * The Raw notification data.
	 */
	private final INotificationData notification;
	/**
	 * The Raw notification data.
	 */
	private boolean isAlreadyDeployed=false;

	/**
	 * The resource we will try to deploy.
	 */
	private final IResourceInstance resourceInstance;

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
	
	/**
	 * The list of artifacts existing in this resource hashed by UUID.
	 */
	private final Map<String, VfModuleArtifact> artifactsMapByUUID;


	public VfResourceStructure(INotificationData notificationdata, IResourceInstance resourceinstance) {
		notification=notificationdata;
		resourceInstance=resourceinstance;
		vfModulesStructureList = new LinkedList<>();
		artifactsMapByUUID = new HashMap<>();
	}
	
	public void addArtifactToStructure(IDistributionClient distributionClient,IArtifactInfo artifactinfo,IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException {
		VfModuleArtifact vfModuleArtifact = new VfModuleArtifact(artifactinfo,clientResult);
		addArtifactByType(artifactinfo,clientResult,vfModuleArtifact);
	}
	
	public void addArtifactToStructure(IDistributionClient distributionClient,IArtifactInfo artifactinfo,IDistributionClientDownloadResult clientResult, String modifiedHeatTemplate) throws UnsupportedEncodingException {
		VfModuleArtifact vfModuleArtifact = new VfModuleArtifact(artifactinfo,clientResult,modifiedHeatTemplate);
		addArtifactByType(artifactinfo,clientResult,vfModuleArtifact);
	}
	
	protected void addArtifactByType(IArtifactInfo artifactinfo,IDistributionClientDownloadResult clientResult, VfModuleArtifact vfModuleArtifact) throws UnsupportedEncodingException {

		switch(artifactinfo.getArtifactType()) {
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

	public void createVfModuleStructures() throws ArtifactInstallerException {

		//for vender tosca VNF there is no VFModule in VF
		if (vfModulesMetadataList == null) {
		    LOGGER.info(MessageEnum.ASDC_GENERAL_INFO,"There is no VF mudules in the VF.", "ASDC", "createVfModuleStructures");
		    return;
		}
			for (IVfModuleData vfModuleMeta:vfModulesMetadataList) {
				vfModulesStructureList.add(new VfModuleStructure(this,vfModuleMeta));
			}
		}

	public INotificationData getNotification() {
		return notification;
	}

	public IResourceInstance getResourceInstance() {
		return resourceInstance;
	}

	public List<VfModuleStructure> getVfModuleStructure() {
		return vfModulesStructureList;
	}

	public boolean isDeployedSuccessfully() {
		return isDeployedSuccessfully;
	}

	public void setSuccessfulDeployment() {
		isDeployedSuccessfully = true;
	}
	
	public boolean isAlreadyDeployed() {
		return isAlreadyDeployed;
	}

	public void setAlreadyDeployed(boolean isAlreadyDeployed) {
		this.isAlreadyDeployed = isAlreadyDeployed;
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
	public void setCatalogNetworkResourceCustomization(NetworkResourceCustomization catalogNetworkResourceCustomization) {
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
			List<IVfModuleData> listVFModuleMetaData = new ObjectMapper().readValue(arg0, new TypeReference<List<VfModuleMetaData>>(){});
			return listVFModuleMetaData;

		} catch (JsonParseException e) {
			LOGGER.debug("JsonParseException : ",e);
		} catch (JsonMappingException e) {
			LOGGER.debug("JsonMappingException : ",e);
		} catch (IOException e) {
			LOGGER.debug("IOException : ",e);
		}
		return null;
	}
}
