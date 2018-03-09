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

package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IConfiguration;
import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.impl.DistributionClientDownloadResultImpl;
import org.openecomp.sdc.impl.DistributionClientResultImpl;
import org.openecomp.sdc.utils.DistributionActionResultEnum;

import org.openecomp.mso.asdc.installer.IVfModuleData;

public class DistributionClientEmulator implements IDistributionClient {

	private String resourcePath;

	private List<IVfModuleData> listVFModuleMetaData;

	private List<IDistributionStatusMessage> distributionMessageReceived = new LinkedList<>();

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

	/* @Override
	public List<IVfModuleData> decodeVfModuleArtifact(byte[] arg0) {
		try {
			listVFModuleMetaData = new ObjectMapper().readValue(arg0, new TypeReference<List<JsonVfModuleMetaData>>(){});
			return listVFModuleMetaData;

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	} */

	public List<IVfModuleData> getListVFModuleMetaData() {
		return listVFModuleMetaData;
	}

    @Override
	public IDistributionClientDownloadResult download (IArtifactInfo arg0) {

		
		//String filename = resourcePath+"/artifacts/"+arg0.getArtifactURL();
		String filename = arg0.getArtifactURL();
		System.out.println("Emulating the download from resources files:"+filename);
		
		InputStream inputStream = null;
		
		if(arg0.getArtifactName().equals("service_Rg516VmmscSrvc_csar.csar")){
			try{
				inputStream = new FileInputStream(System.getProperty("java.io.tmpdir") + File.separator + "service_Rg516VmmscSrvc_csar.csar");
			}catch(Exception e){
				System.out.println("Error " + e.getMessage());
			}
		}else{
		
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath + filename);
		}

		if (inputStream == null) {
			System.out.println("InputStream is NULL for:"+filename);
		}
		try {
			return new DistributionClientDownloadResultImpl(DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name(),arg0.getArtifactName(),IOUtils.toByteArray(inputStream));
		} catch (IOException e) {
			
			e.printStackTrace();
			}
				return null;
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

}
