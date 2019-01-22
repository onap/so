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

package org.onap.so.asdc.client.test.emulators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.logger.MsoLogger;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IComponentDoneStatusMessage;
import org.onap.sdc.api.consumer.IConfiguration;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.consumer.IStatusCallback;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IVfModuleMetadata;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientDownloadResultImpl;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.utils.DistributionActionResultEnum;

public class DistributionClientEmulator implements IDistributionClient {

	private String resourcePath;
	
	private List<IVfModuleData> listVFModuleMetaData;
	
	private List<IDistributionStatusMessage> distributionMessageReceived = new LinkedList<>();
	
	
	private static final MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,DistributionClientEmulator.class );
	
	public DistributionClientEmulator() {			
	}
	
	public DistributionClientEmulator(String notifFolderInResource) {		
		resourcePath = notifFolderInResource;
	}

	public List<IDistributionStatusMessage> getDistributionMessageReceived() {
		return distributionMessageReceived;
	}
	
	@Override
	public List<IVfModuleMetadata> decodeVfModuleArtifact(byte[] arg0) {
		return null;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public List<IVfModuleData> getListVFModuleMetaData() {
		return listVFModuleMetaData;
	}

	@Override
	public IDistributionClientDownloadResult download (IArtifactInfo arg0) {		
		
		String filename = arg0.getArtifactURL();
	
		byte[] inputStream=null;
		try {
			inputStream = getData(filename);
		} catch (IOException e) {	
			
			logger.error("IOException in DistributionClientEmulator.download() method :",e);
		}		
	
		return new DistributionClientDownloadResultImpl(DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name(),arg0.getArtifactName(),inputStream);		
	}
	
	private byte[] getData(String filename) throws IOException {
		 return Files.readAllBytes(Paths.get(resourcePath + filename));
	}

	@Override
	public IConfiguration getConfiguration() {
		return null;
	}

	@Override
	public IDistributionClientResult init(IConfiguration arg0, INotificationCallback arg1) {
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}
	
	@Override
	public IDistributionClientResult init(IConfiguration arg0, INotificationCallback arg1, IStatusCallback arg2) {
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult sendDeploymentStatus(IDistributionStatusMessage arg0) {
		this.distributionMessageReceived.add(arg0);
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult sendDeploymentStatus(IDistributionStatusMessage arg0, String arg1) {
		this.distributionMessageReceived.add(arg0);
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult sendDownloadStatus(IDistributionStatusMessage arg0) {
		this.distributionMessageReceived.add(arg0);
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult sendDownloadStatus(IDistributionStatusMessage arg0, String arg1) {
		this.distributionMessageReceived.add(arg0);
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult start() {
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult stop() {
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
		
	}

	@Override
	public IDistributionClientResult updateConfiguration(IConfiguration arg0) {
		return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,DistributionActionResultEnum.SUCCESS.name());
	}

	@Override
	public IDistributionClientResult sendComponentDoneStatus(
			IComponentDoneStatusMessage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDistributionClientResult sendFinalDistrStatus(
			IFinalDistrStatusMessage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDistributionClientResult sendComponentDoneStatus(
			IComponentDoneStatusMessage arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDistributionClientResult sendFinalDistrStatus(
			IFinalDistrStatusMessage arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
