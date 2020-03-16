/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.OwningEntities;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIServiceInstanceResources {

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public boolean existsServiceInstance(ServiceInstance serviceInstance) {
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        return injectionHelper.getAaiClient().exists(serviceInstanceURI);
    }

    public void createServiceInstance(ServiceInstance serviceInstance, Customer customer) {
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, customer.getGlobalCustomerId(),
                        customer.getServiceSubscription().getServiceType(), serviceInstance.getServiceInstanceId());
        serviceInstance.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance =
                aaiObjectMapper.mapServiceInstance(serviceInstance);
        injectionHelper.getAaiClient().createIfNotExists(serviceInstanceURI, Optional.of(aaiServiceInstance));
    }

    /**
     * Create ServiceSubscription in A&AI
     * 
     * @param customer
     */
    public void createServiceSubscription(Customer customer) {
        AAIResourceUri serviceSubscriptionURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION,
                customer.getGlobalCustomerId(), customer.getServiceSubscription().getServiceType());
        org.onap.aai.domain.yang.ServiceSubscription serviceSubscription =
                aaiObjectMapper.mapServiceSubscription(customer.getServiceSubscription());
        injectionHelper.getAaiClient().createIfNotExists(serviceSubscriptionURI, Optional.of(serviceSubscription));
    }

    public void deleteServiceInstance(ServiceInstance serviceInstance) {
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        injectionHelper.getAaiClient().delete(serviceInstanceURI);
    }

    public void createProject(Project project) {
        AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, project.getProjectName());
        org.onap.aai.domain.yang.Project aaiProject = aaiObjectMapper.mapProject(project);
        injectionHelper.getAaiClient().createIfNotExists(projectURI, Optional.of(aaiProject));
    }

    public void createProjectandConnectServiceInstance(Project project, ServiceInstance serviceInstance) {
        AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, project.getProjectName());
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        org.onap.aai.domain.yang.Project aaiProject = aaiObjectMapper.mapProject(project);
        injectionHelper.getAaiClient().createIfNotExists(projectURI, Optional.of(aaiProject)).connect(projectURI,
                serviceInstanceURI);
    }

    public void createOwningEntity(OwningEntity owningEntity) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntity.getOwningEntityId());
        org.onap.aai.domain.yang.OwningEntity aaiOwningEntity = aaiObjectMapper.mapOwningEntity(owningEntity);
        injectionHelper.getAaiClient().createIfNotExists(owningEntityURI, Optional.of(aaiOwningEntity));
    }

    public boolean existsOwningEntity(OwningEntity owningEntity) {
        AAIResourceUri owningEntityUri =
                AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntity.getOwningEntityId());
        return injectionHelper.getAaiClient().exists(owningEntityUri);
    }

    public boolean existsOwningEntityName(String owningEntityName) {
        AAIPluralResourceUri owningEntityUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.OWNING_ENTITY)
                .queryParam("owning-entity-name", owningEntityName);
        AAIResourcesClient aaiRC = injectionHelper.getAaiClient();
        return aaiRC.exists(owningEntityUri);
    }

    public org.onap.aai.domain.yang.OwningEntity getOwningEntityByName(String owningEntityName)
            throws AAIEntityNotFoundException {
        AAIPluralResourceUri owningEntityUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.OWNING_ENTITY)
                .queryParam("owning-entity-name", owningEntityName);
        AAIResourcesClient aaiRC = injectionHelper.getAaiClient();
        Optional<OwningEntities> owningEntities = aaiRC.get(OwningEntities.class, owningEntityUri);
        if (owningEntities.isPresent()) {
            List<org.onap.aai.domain.yang.OwningEntity> owningEntityList = owningEntities.get().getOwningEntity();
            if (owningEntityList.size() > 1) {
                throw new AAIEntityNotFoundException(
                        "Non unique result returned for owning entity name: " + owningEntityName);
            } else {
                return owningEntityList.get(0);
            }
        } else {
            throw new AAIEntityNotFoundException("No result returned for owning entity name: " + owningEntityName);
        }
    }

    public void connectOwningEntityandServiceInstance(OwningEntity owningEntity, ServiceInstance serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntity.getOwningEntityId());
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        injectionHelper.getAaiClient().connect(owningEntityURI, serviceInstanceURI);
    }

    public void createOwningEntityandConnectServiceInstance(OwningEntity owningEntity,
            ServiceInstance serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntity.getOwningEntityId());
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        org.onap.aai.domain.yang.OwningEntity aaiOwningEntity = aaiObjectMapper.mapOwningEntity(owningEntity);
        injectionHelper.getAaiClient().createIfNotExists(owningEntityURI, Optional.of(aaiOwningEntity))
                .connect(owningEntityURI, serviceInstanceURI);
    }

    public void updateOrchestrationStatusServiceInstance(ServiceInstance serviceInstance,
            OrchestrationStatus orchestrationStatus) {
        ServiceInstance copiedServiceInstance = serviceInstance.shallowCopyId();

        copiedServiceInstance.setOrchestrationStatus(orchestrationStatus);
        copiedServiceInstance.setServiceInstanceName(serviceInstance.getServiceInstanceName());
        serviceInstance.setOrchestrationStatus(orchestrationStatus);
        updateServiceInstance(copiedServiceInstance);
    }

    public void updateServiceInstance(ServiceInstance serviceInstance) {
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance =
                aaiObjectMapper.mapServiceInstance(serviceInstance);
        injectionHelper.getAaiClient().update(serviceInstanceURI, aaiServiceInstance);
    }

    public boolean checkInstanceServiceNameInUse(ServiceInstance serviceInstance) {
        AAIPluralResourceUri uriSI = AAIUriFactory.createNodesUri(AAIObjectPlurals.SERVICE_INSTANCE)
                .queryParam("service-instance-name", serviceInstance.getServiceInstanceName());
        return injectionHelper.getAaiClient().exists(uriSI);
    }

}
