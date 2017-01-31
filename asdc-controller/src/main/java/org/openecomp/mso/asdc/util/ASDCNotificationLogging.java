/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.asdc.util;


import java.util.List;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;

public class ASDCNotificationLogging {

	public static String dumpASDCNotification(INotificationData asdcNotification) {
		
		if (asdcNotification == null) {
			return "NULL";
		}
		StringBuffer buffer = new StringBuffer("ASDC Notification:");
		buffer.append(System.lineSeparator());
		
		buffer.append("DistributionID:");
		buffer.append(testNull(asdcNotification.getDistributionID()));
		buffer.append(System.lineSeparator());
		
		
		buffer.append("ServiceName:");
		buffer.append(testNull(asdcNotification.getServiceName()));
		buffer.append(System.lineSeparator());
		
		
		buffer.append("ServiceVersion:");
		buffer.append(testNull(asdcNotification.getServiceVersion()));
		buffer.append(System.lineSeparator());
		
		
		buffer.append("ServiceUUID:");
		buffer.append(testNull(asdcNotification.getServiceUUID()));
		buffer.append(System.lineSeparator());
	
		
		buffer.append("ServiceInvariantUUID:");
		buffer.append(testNull(asdcNotification.getServiceInvariantUUID()));
		buffer.append(System.lineSeparator());
		
		
		buffer.append("ServiceDescription:");
		buffer.append(testNull(asdcNotification.getServiceDescription()));
		buffer.append(System.lineSeparator());
	
		
		buffer.append("Service Artifacts List:");
		buffer.append(System.lineSeparator());
		buffer.append(testNull(dumpArtifactInfoList(asdcNotification.getServiceArtifacts())));
		buffer.append(System.lineSeparator());
		
		buffer.append("Resource Instances List:");
		buffer.append(System.lineSeparator());
		buffer.append(testNull(dumpASDCResourcesList(asdcNotification)));
		buffer.append(System.lineSeparator());
		
		
		return buffer.toString();
	}
	
	public static String dumpVfModuleMetaDataList(List<IVfModuleMetadata> moduleMetaDataList) {
		if (moduleMetaDataList == null ) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer();		
		buffer.append("{");
		
		for (IVfModuleMetadata moduleMetaData:moduleMetaDataList) {
			buffer.append(System.lineSeparator());
			buffer.append(testNull(dumpVfModuleMetaData(moduleMetaData)));
			buffer.append(System.lineSeparator());
			buffer.append(",");
			
		}
		buffer.replace(buffer.length()-1,buffer.length(), System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}
	
	private static String dumpVfModuleMetaData(IVfModuleMetadata moduleMetaData) {
				
		if (moduleMetaData == null ) {
			return "NULL";
		}
		
		StringBuffer buffer = new StringBuffer("VfModuleMetaData:");
		buffer.append(System.lineSeparator());
		
		buffer.append("VfModuleModelName:");
		buffer.append(testNull(moduleMetaData.getVfModuleModelName()));
		buffer.append(System.lineSeparator());
		
		buffer.append("VfModuleModelVersion:");
		buffer.append(testNull(moduleMetaData.getVfModuleModelVersion()));
		buffer.append(System.lineSeparator());
		
		buffer.append("VfModuleModelUUID:");
		buffer.append(testNull(moduleMetaData.getVfModuleModelUUID()));
		buffer.append(System.lineSeparator());
		
		buffer.append("VfModuleModelInvariantUUID:");
		buffer.append(testNull(moduleMetaData.getVfModuleModelInvariantUUID()));
		buffer.append(System.lineSeparator());
		
		buffer.append("VfModuleModelDescription:");
		buffer.append(testNull(moduleMetaData.getVfModuleModelDescription()));
		buffer.append(System.lineSeparator());
		
		buffer.append("Artifacts UUID List:");
		
		if (moduleMetaData.getArtifacts() != null) {
			buffer.append("{");
			
			for (String artifactUUID:moduleMetaData.getArtifacts()) {
				buffer.append(System.lineSeparator());
				buffer.append(testNull(artifactUUID));
				buffer.append(System.lineSeparator());
				buffer.append(",");
			}
			buffer.replace(buffer.length()-1,buffer.length(), System.lineSeparator());
			buffer.append("}");
			buffer.append(System.lineSeparator());
		} else {
			buffer.append("NULL");
		}
		
		
		buffer.append(System.lineSeparator());
		
		buffer.append("isBase:");
		buffer.append(moduleMetaData.isBase());
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}
	
	private static String testNull(Object object) {
		if (object == null) {
			return "NULL";
		} else if (object instanceof Integer) {
			return object.toString();
		} else if (object instanceof String) {
			return (String)object;
		} else {
			return "Type not recognized";
		}
	}
	
	private static String dumpASDCResourcesList(INotificationData asdcNotification) {
		if (asdcNotification == null || asdcNotification.getResources() == null) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer();		
		buffer.append("{");
		
		for (IResourceInstance resourceInstanceElem:asdcNotification.getResources()) {
			buffer.append(System.lineSeparator());
			buffer.append(testNull(dumpASDCResourceInstance(resourceInstanceElem)));
			buffer.append(System.lineSeparator());
			buffer.append(",");
		}
		buffer.replace(buffer.length()-1,buffer.length(), System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
		
	}
	
	private static String dumpASDCResourceInstance(IResourceInstance resourceInstance) {
		
		if (resourceInstance == null) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer("Resource Instance Info:");
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceInstanceName:");
		buffer.append(testNull(resourceInstance.getResourceInstanceName()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceInvariantUUID:");
		buffer.append(testNull(resourceInstance.getResourceInvariantUUID()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceName:");
		buffer.append(testNull(resourceInstance.getResourceName()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceType:");
		buffer.append(testNull(resourceInstance.getResourceType()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceUUID:");
		buffer.append(testNull(resourceInstance.getResourceUUID()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ResourceVersion:");
		buffer.append(testNull(resourceInstance.getResourceVersion()));
		buffer.append(System.lineSeparator());
		
		buffer.append("Resource Artifacts List:");
		buffer.append(System.lineSeparator());
		buffer.append(testNull(dumpArtifactInfoList(resourceInstance.getArtifacts())));
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
		
	}
	
	
	private static String dumpArtifactInfoList(List<IArtifactInfo> artifactsList) {
		
		if (artifactsList == null || artifactsList.isEmpty()) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer();		
		buffer.append("{");
		for (IArtifactInfo artifactInfoElem:artifactsList) {
			buffer.append(System.lineSeparator());
			buffer.append(testNull(dumpASDCArtifactInfo(artifactInfoElem)));
			buffer.append(System.lineSeparator());
			buffer.append(",");
			
		}
		buffer.replace(buffer.length()-1,buffer.length(), System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}
	
	private static String dumpASDCArtifactInfo(IArtifactInfo artifactInfo) {
		
		if (artifactInfo == null) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer("Service Artifacts Info:");
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactName:");
		buffer.append(testNull(artifactInfo.getArtifactName()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactVersion:");
		buffer.append(testNull(artifactInfo.getArtifactVersion()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactType:");
		buffer.append(testNull(artifactInfo.getArtifactType()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactDescription:");
		buffer.append(testNull(artifactInfo.getArtifactDescription()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactTimeout:");
		buffer.append(testNull(artifactInfo.getArtifactTimeout()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactURL:");
		buffer.append(testNull(artifactInfo.getArtifactURL()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactUUID:");
		buffer.append(testNull(artifactInfo.getArtifactUUID()));
		buffer.append(System.lineSeparator());
		
		buffer.append("ArtifactChecksum:");
		buffer.append(testNull(artifactInfo.getArtifactChecksum()));
		buffer.append(System.lineSeparator());
		
		buffer.append("GeneratedArtifact:");
		buffer.append("{");
		buffer.append(testNull(dumpASDCArtifactInfo(artifactInfo.getGeneratedArtifact())));
		buffer.append(System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());
		
		buffer.append("RelatedArtifacts:");
		
		
		if (artifactInfo.getRelatedArtifacts() != null) {
			buffer.append("{");
			buffer.append(System.lineSeparator());
			for (IArtifactInfo artifactInfoElem:artifactInfo.getRelatedArtifacts()) {
							
				buffer.append(testNull(dumpASDCArtifactInfo(artifactInfoElem)));
				buffer.append(System.lineSeparator());
				buffer.append(",");
			
			}
			buffer.replace(buffer.length()-1,buffer.length(), System.lineSeparator());
			buffer.append("}");
			buffer.append(System.lineSeparator());
		} else {
			buffer.append("NULL");
		}
		
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}
}
