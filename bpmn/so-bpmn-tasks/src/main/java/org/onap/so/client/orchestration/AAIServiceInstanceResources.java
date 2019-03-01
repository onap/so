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

package org.onap.so.client.orchestration;

import java.util.Optional;

import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIServiceInstanceResources {
	private static final Logger logger = LoggerFactory.getLogger(AAIServiceInstanceResources
		.class);
	
	@Autowired
	private InjectionHelper injectionHelper;
	
	@Autowired
	private AAIObjectMapper aaiObjectMapper;

	public boolean existsServiceInstance(ServiceInstance serviceInstance) {
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		return injectionHelper.getAaiClient().exists(serviceInstanceURI);
	}

	public void createServiceInstance(ServiceInstance serviceInstance, Customer customer) {
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				customer.getGlobalCustomerId(), customer.getServiceSubscription().getServiceType(), serviceInstance.getServiceInstanceId());
		serviceInstance.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		org.onap.aai.domain.yang.ServiceInstance AAIServiceInstance = aaiObjectMapper.mapServiceInstance(serviceInstance);
		injectionHelper.getAaiClient().createIfNotExists(serviceInstanceURI, Optional.of(AAIServiceInstance));
	}

    /**
     * Create ServiceSubscription in A&AI
     * @param customer
     */
	public void createServiceSubscription(Customer customer) {
        AAIResourceUri serviceSubscriptionURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION,
                customer.getGlobalCustomerId(),customer.getServiceSubscription().getServiceType());
        org.onap.aai.domain.yang.ServiceSubscription serviceSubscription = aaiObjectMapper.mapServiceSubscription(customer.getServiceSubscription());
        injectionHelper.getAaiClient().createIfNotExists(serviceSubscriptionURI , Optional.of(serviceSubscription));
    }

	public void deleteServiceInstance(ServiceInstance serviceInstance) {
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		injectionHelper.getAaiClient().delete(serviceInstanceURI);
	}

	public void createProject(Project project) {
		AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, project.getProjectName());
		org.onap.aai.domain.yang.Project AAIProject = aaiObjectMapper.mapProject(project);
		injectionHelper.getAaiClient().createIfNotExists(projectURI, Optional.of(AAIProject));
	}

	public void createProjectandConnectServiceInstance(Project project, ServiceInstance serviceInstance) {
		AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, project.getProjectName());
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		org.onap.aai.domain.yang.Project AAIProject = aaiObjectMapper.mapProject(project);
		injectionHelper.getAaiClient().createIfNotExists(projectURI, Optional.of(AAIProject)).connect(projectURI, serviceInstanceURI);
	}

	public void createOwningEntity(OwningEntity owningEntity) {
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY,
				owningEntity.getOwningEntityId());
		org.onap.aai.domain.yang.OwningEntity AAIOwningEntity = aaiObjectMapper.mapOwningEntity(owningEntity);
		injectionHelper.getAaiClient().createIfNotExists(owningEntityURI, Optional.of(AAIOwningEntity));
	}

	public boolean existsOwningEntity(OwningEntity owningEntity) {
		AAIResourceUri owningEntityUri = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY,
				owningEntity.getOwningEntityId());
		return injectionHelper.getAaiClient().exists(owningEntityUri);
	}
	
	public boolean existsOwningEntityName(String owningEntityName) {
		AAIResourceUri owningEntityUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.OWNING_ENTITY).queryParam("owning-entity-name", owningEntityName);
		AAIResourcesClient aaiRC = injectionHelper.getAaiClient();
		return aaiRC.exists(owningEntityUri);
	}

	public void connectOwningEntityandServiceInstance(OwningEntity owningEntity, ServiceInstance serviceInstance) {
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY,
				owningEntity.getOwningEntityId());
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		injectionHelper.getAaiClient().connect(owningEntityURI, serviceInstanceURI);
	}

	public void createOwningEntityandConnectServiceInstance(OwningEntity owningEntity,
			ServiceInstance serviceInstance) {
		AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY,
				owningEntity.getOwningEntityId());
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				serviceInstance.getServiceInstanceId());
		org.onap.aai.domain.yang.OwningEntity AAIOwningEntity = aaiObjectMapper.mapOwningEntity(owningEntity);
		injectionHelper.getAaiClient().createIfNotExists(owningEntityURI, Optional.of(AAIOwningEntity)).connect(owningEntityURI,
				serviceInstanceURI);
	}
	
	public void updateOrchestrationStatusServiceInstance(ServiceInstance serviceInstance, OrchestrationStatus orchestrationStatus){
		ServiceInstance copiedServiceInstance = serviceInstance.shallowCopyId();

		copiedServiceInstance.setOrchestrationStatus(orchestrationStatus);
		copiedServiceInstance.setServiceInstanceName(serviceInstance.getServiceInstanceName());
		serviceInstance.setOrchestrationStatus(orchestrationStatus);
		updateServiceInstance(copiedServiceInstance);
	}
	
	public void updateServiceInstance(ServiceInstance serviceInstance) {
		AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
		org.onap.aai.domain.yang.ServiceInstance AAIServiceInstance = aaiObjectMapper.mapServiceInstance(serviceInstance);
		injectionHelper.getAaiClient().update(serviceInstanceURI, AAIServiceInstance);
	}
	

}
