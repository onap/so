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

package org.openecomp.mso.bpmn.infrastructure.aai;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

public class AAICreateResources {


	public void createAAIProject (String projectName, String serviceInstance){
		AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, projectName);
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance);
		AAIResourcesClient aaiRC = new AAIResourcesClient();	  
		aaiRC.createIfNotExists(projectURI, Optional.empty()).connect(projectURI, serviceInstanceURI);
		
	}
	
	public void createAAIOwningEntity(String owningEntityId, String owningEntityName,String serviceInstance){
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntityId);
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance);
		Map<String, String> hashMap= new HashMap<>();
		hashMap.put("owning-entity-name", owningEntityName);	
		AAIResourcesClient aaiRC = new AAIResourcesClient();
		aaiRC.createIfNotExists(owningEntityURI, Optional.of(hashMap)).connect(owningEntityURI, serviceInstanceURI);
	}

	public boolean existsOwningEntity(String owningEntityId){
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntityId);
		AAIResourcesClient aaiRC = new AAIResourcesClient();	  
		return aaiRC.exists(owningEntityURI);
	}
	
	public void connectOwningEntityandServiceInstance (String owningEntityId, String serviceInstance){
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntityId);
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance);
		AAIResourcesClient aaiRC = new AAIResourcesClient();
		aaiRC.connect(owningEntityURI, serviceInstanceURI);
	}
	
	public void createAAIPlatform(String platformName,String vnfId){
		AAIResourceUri platformURI = AAIUriFactory.createResourceUri(AAIObjectType.PLATFORM, platformName);
		AAIResourceUri genericVnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF,vnfId);
		AAIResourcesClient aaiRC = new AAIResourcesClient();				  
		aaiRC.createIfNotExists(platformURI, Optional.empty()).connect(platformURI, genericVnfURI);
	}
	
	public void createAAILineOfBusiness(String lineOfBusiness,String vnfId){
		AAIResourceUri lineOfBusinessURI = AAIUriFactory.createResourceUri(AAIObjectType.LINE_OF_BUSINESS, lineOfBusiness);
		AAIResourceUri genericVnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF,vnfId);
		AAIResourcesClient aaiRC = new AAIResourcesClient();				  
		aaiRC.createIfNotExists(lineOfBusinessURI, Optional.empty()).connect(lineOfBusinessURI, genericVnfURI);
	}
	public void createAAIServiceInstance(String globalCustomerId, String serviceType, String serviceInstanceId){
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustomerId,serviceType,serviceInstanceId);
		AAIResourcesClient aaiRC = new AAIResourcesClient();	  
		aaiRC.createIfNotExists(serviceInstanceURI, Optional.empty());
	}
	
}
