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

package org.openecomp.mso.asdc.util;


import java.util.List;
import java.util.Map;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.impl.SdcPropertyNames;
import org.openecomp.sdc.toscaparser.api.Group;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.mso.asdc.installer.IVfModuleData;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;

public class ASDCNotificationLogging {

	public static String dumpASDCNotification(INotificationData asdcNotification) {

		if (asdcNotification == null) {
			return "NULL";
		}
		StringBuilder buffer = new StringBuilder("ASDC Notification:");
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

	public static String dumpCSARNotification(INotificationData asdcNotification, ToscaResourceStructure toscaResourceStructure) {
		
		if (asdcNotification == null) {
			return "NULL";
		}
		

		StringBuilder buffer = new StringBuilder("CSAR Notification:");
		buffer.append(System.lineSeparator());
		buffer.append(System.lineSeparator());
		
		
		ISdcCsarHelper csarHelper = toscaResourceStructure.getSdcCsarHelper();

	
		buffer.append("Service Level Properties:");
		buffer.append(System.lineSeparator());
		buffer.append("Name:");
		buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
		buffer.append(System.lineSeparator());
		buffer.append("Description:");
		buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
		buffer.append(System.lineSeparator());
		buffer.append("Model UUID:");
		buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
		buffer.append(System.lineSeparator());
		buffer.append("Model Version:");
		buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
		buffer.append(System.lineSeparator());
		buffer.append("Model InvariantUuid:");
		buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
		
		buffer.append(System.lineSeparator());
		buffer.append(System.lineSeparator());
		buffer.append("VNF Level Properties:");
		buffer.append(System.lineSeparator());
		
        List<NodeTemplate> vfNodeTemplatesList = toscaResourceStructure.getSdcCsarHelper().getServiceVfList();
        for (NodeTemplate vfNodeTemplate :  vfNodeTemplatesList) {
        	
    		buffer.append("Model Name:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
    		buffer.append(System.lineSeparator());
       		buffer.append("Description:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION).trim()));
    		buffer.append(System.lineSeparator());
       		buffer.append("Version:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("Type:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_TYPE).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("InvariantUuid:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("Max Instances:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("Min Instances:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES).trim()));
    		buffer.append(System.lineSeparator());
    		
    		buffer.append(System.lineSeparator());
    		buffer.append("VNF Customization Properties:");
    		buffer.append(System.lineSeparator());
    		
      		buffer.append("Customization UUID:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("NFFunction:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NFFUNCTION).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("NFCode:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NFCODE).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("NFRole:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NFROLE).trim()));
    		buffer.append(System.lineSeparator());
      		buffer.append("NFType:");
    		buffer.append(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NFTYPE).trim()));
    		buffer.append(System.lineSeparator());      
    		
    		buffer.append(System.lineSeparator());
    		buffer.append("VF Module Properties:");
    		buffer.append(System.lineSeparator());
    		List<Group> vfGroups = toscaResourceStructure.getSdcCsarHelper().getVfModulesByVf(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
    		
    		for(Group group : vfGroups){
        		
    			Metadata vfMetadata = group.getMetadata();
    			
          		buffer.append("ModelInvariantUuid:");
        		buffer.append(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID).trim()));
        		buffer.append(System.lineSeparator());
         		buffer.append("ModelName:");
        		buffer.append(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME).trim()));
        		buffer.append(System.lineSeparator()); 
         		buffer.append("ModelUuid:");
        		buffer.append(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID).trim()));
        		buffer.append(System.lineSeparator());
         		buffer.append("ModelVersion:");
        		buffer.append(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION).trim()));
        		buffer.append(System.lineSeparator()); 
         		buffer.append("Description:");
        		buffer.append(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_DESCRIPTION).trim()));
        		buffer.append(System.lineSeparator());     
    		}
  
        }
        
		
		List<NodeTemplate> nodeTemplatesVLList = toscaResourceStructure.getSdcCsarHelper().getServiceVlList();
					
    	if(nodeTemplatesVLList != null){
    		
    		buffer.append(System.lineSeparator());
    		buffer.append("NETWORK Level Properties:");
    		buffer.append(System.lineSeparator());
    		
    		for(NodeTemplate vlNode : nodeTemplatesVLList){
			
    			buffer.append("Model Name:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
    			buffer.append(System.lineSeparator()); 
    			buffer.append("Model InvariantUuid:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID).trim()));
    			buffer.append(System.lineSeparator());   
    			buffer.append("Model UUID:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
    			buffer.append(System.lineSeparator()); 
    			buffer.append("Model Version:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION).trim()));
    			buffer.append(System.lineSeparator());   
    			buffer.append("AIC Max Version:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES).trim()));
    			buffer.append(System.lineSeparator()); 
       			buffer.append("AIC Min Version:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES).trim()));
    			buffer.append(System.lineSeparator());  
       			buffer.append("Tosca Node Type:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_TYPE).trim()));
    			buffer.append(System.lineSeparator());  
       			buffer.append("Description:");
    			buffer.append(testNull(vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION).trim()));
    			buffer.append(System.lineSeparator());  
    		
    		}
    			
    	}
    	
        List<NodeTemplate> allottedResourceList = toscaResourceStructure.getSdcCsarHelper().getAllottedResources();
    	
    		if(allottedResourceList != null){
    			
    			buffer.append(System.lineSeparator());
    			buffer.append("Allotted Resource Properties:");
    			buffer.append(System.lineSeparator());
    		
    			for(NodeTemplate allottedNode : allottedResourceList){
    				
           			buffer.append("Model Name:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
        			buffer.append(System.lineSeparator());
           			buffer.append("Model Name:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME).trim()));
        			buffer.append(System.lineSeparator()); 
           			buffer.append("Model InvariantUuid:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID).trim()));
        			buffer.append(System.lineSeparator());  
           			buffer.append("Model Version:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION).trim()));
        			buffer.append(System.lineSeparator()); 
           			buffer.append("Model UUID:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID).trim()));
        			buffer.append(System.lineSeparator());
        			
    				
        			buffer.append("Allotted Resource Customization Properties:");
        			buffer.append(System.lineSeparator());
        		
           			buffer.append("Model Cutomization UUID:");
        			buffer.append(testNull(allottedNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID).trim()));
        			buffer.append(System.lineSeparator());
    				
				
    			}
    		}
		
		
		return buffer.toString();
	}
	
	public static String dumpVfModuleMetaDataList(List<IVfModuleData> moduleMetaDataList) {
		if (moduleMetaDataList == null ) {
			return null;
		}

		StringBuilder buffer = new StringBuilder("VfModuleMetaData List:");
		buffer.append(System.lineSeparator());

		buffer.append("{");

		for (IVfModuleData moduleMetaData:moduleMetaDataList) {
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

	private static String dumpVfModuleMetaData(IVfModuleData moduleMetaData) {

		if (moduleMetaData == null ) {
			return "NULL";
		}

		StringBuilder stringBuilder = new StringBuilder("VfModuleMetaData:");
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("VfModuleModelName:");
		stringBuilder.append(testNull(moduleMetaData.getVfModuleModelName()));
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("VfModuleModelVersion:");
		stringBuilder.append(testNull(moduleMetaData.getVfModuleModelVersion()));
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("VfModuleModelUUID:");
		stringBuilder.append(testNull(moduleMetaData.getVfModuleModelUUID()));
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("VfModuleModelInvariantUUID:");
		stringBuilder.append(testNull(moduleMetaData.getVfModuleModelInvariantUUID()));
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("VfModuleModelDescription:");
		stringBuilder.append(testNull(moduleMetaData.getVfModuleModelDescription()));
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Artifacts UUID List:");

		if (moduleMetaData.getArtifacts() != null) {
			stringBuilder.append("{");

			for (String artifactUUID:moduleMetaData.getArtifacts()) {
				stringBuilder.append(System.lineSeparator());
				stringBuilder.append(testNull(artifactUUID));
				stringBuilder.append(System.lineSeparator());
				stringBuilder.append(",");
			}
			stringBuilder.replace(stringBuilder.length()-1,stringBuilder.length(), System.lineSeparator());
			stringBuilder.append("}");
			stringBuilder.append(System.lineSeparator());
		} else {
			stringBuilder.append("NULL");
		}

		if (moduleMetaData.getProperties() != null) {
			Map<String, String> vfModuleMap = moduleMetaData.getProperties();
			stringBuilder.append("Properties List:");
			stringBuilder.append("{");

			for (Map.Entry<String, String> entry : vfModuleMap.entrySet()) {
				stringBuilder.append(System.lineSeparator());
				stringBuilder.append("  ").append(entry.getKey()).append(" : ").append(entry.getValue());
			}
			stringBuilder.replace(stringBuilder.length()-1,stringBuilder.length(), System.lineSeparator());
			stringBuilder.append("}");
			stringBuilder.append(System.lineSeparator());
		} else {
			stringBuilder.append("NULL");
		}


		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("isBase:");
		stringBuilder.append(moduleMetaData.isBase());
		stringBuilder.append(System.lineSeparator());

		return stringBuilder.toString();
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

		StringBuilder buffer = new StringBuilder();
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

		StringBuilder buffer = new StringBuilder("Resource Instance Info:");
		buffer.append(System.lineSeparator());

		buffer.append("ResourceInstanceName:");
		buffer.append(testNull(resourceInstance.getResourceInstanceName()));
		buffer.append(System.lineSeparator());

		buffer.append("ResourceCustomizationUUID:");
		buffer.append(testNull(resourceInstance.getResourceCustomizationUUID()));
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

		buffer.append("Category:");
		buffer.append(testNull(resourceInstance.getCategory()));
		buffer.append(System.lineSeparator());

		buffer.append("SubCategory:");
		buffer.append(testNull(resourceInstance.getSubcategory()));
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

		StringBuilder buffer = new StringBuilder();
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

		StringBuilder buffer = new StringBuilder("Service Artifacts Info:");
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
