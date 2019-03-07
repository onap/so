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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.L3Networks;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;

import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component("BBInputSetupUtils")
public class BBInputSetupUtils {

	private static final Logger logger = LoggerFactory.getLogger(BBInputSetupUtils.class);
	private ObjectMapper objectMapper = new ObjectMapper();
	private static final String REQUEST_ERROR = "Could not find request.";

	@Autowired
	protected CatalogDbClient catalogDbClient;

	@Autowired
	protected RequestsDbClient requestsDbClient;

	@Autowired
	protected InjectionHelper injectionHelper;

	public void updateInfraActiveRequestVnfId(InfraActiveRequests request, String vnfId) {
		if(request != null) {
			request.setVnfId(vnfId);
			this.requestsDbClient.updateInfraActiveRequests(request);
		} else {
			logger.debug(REQUEST_ERROR);
		}
	}

	public void updateInfraActiveRequestVfModuleId(InfraActiveRequests request, String vfModuleId) {
		if(request != null) {
			request.setVfModuleId(vfModuleId);
			this.requestsDbClient.updateInfraActiveRequests(request);
		} else {
			logger.debug(REQUEST_ERROR);
		}
	}

	public void updateInfraActiveRequestVolumeGroupId(InfraActiveRequests request, String volumeGroupId) {
		if(request != null) {
			request.setVolumeGroupId(volumeGroupId);
			this.requestsDbClient.updateInfraActiveRequests(request);
		} else {
			logger.debug(REQUEST_ERROR);
		}
	}

	public void updateInfraActiveRequestNetworkId(InfraActiveRequests request, String networkId) {
		if(request != null) {
			request.setNetworkId(networkId);
			this.requestsDbClient.updateInfraActiveRequests(request);
		} else {
			logger.debug(REQUEST_ERROR);
		}
	}
	
	public Service getCatalogServiceByModelUUID(String modelUUID) {
		return catalogDbClient.getServiceByID(modelUUID);
	}
	
	public Service getCatalogServiceByModelVersionAndModelInvariantUUID(String modelVersion, String modelInvariantUUID) {
		return catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(modelVersion, modelInvariantUUID);
	}

	public CollectionNetworkResourceCustomization getCatalogCollectionNetworkResourceCustByID(String key) {
		return catalogDbClient.getCollectionNetworkResourceCustomizationByID(key);
	}

	public NetworkCollectionResourceCustomization getCatalogNetworkCollectionResourceCustByID(
			String collectionCustomizationId) {
		return catalogDbClient.getNetworkCollectionResourceCustomizationByID(collectionCustomizationId);
	}

	public VfModuleCustomization getVfModuleCustomizationByModelCuztomizationUUID(String modelCustomizationUUID) {
		return catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(modelCustomizationUUID);
	}
	
	public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroups(String modelCustomizationUUID) {
		return catalogDbClient.getVnfcInstanceGroupsByVnfResourceCust(modelCustomizationUUID);
	}

	public Map<String, String> getURIKeysFromServiceInstance(String serviceInstanceId) {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
		return uri.getURIKeys();
	}

	protected RequestDetails getRequestDetails(String requestId) throws IOException {
		if (requestId != null && !requestId.isEmpty()) {
			InfraActiveRequests activeRequest = this.getInfraActiveRequest(requestId);
			String requestBody = activeRequest.getRequestBody().replaceAll("\\\\", "");
			objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
			objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
			return objectMapper.readValue(requestBody, RequestDetails.class);
		} else {
			return null;
		}
	}

	protected InfraActiveRequests getInfraActiveRequest(String requestId) {
		if (requestId != null && !requestId.isEmpty()) {
			return requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
		} else {
			return null;
		}
	}

	protected CloudRegion getCloudRegion(CloudConfiguration cloudConfiguration) {
		if (cloudConfiguration != null) {
			String cloudRegionId = cloudConfiguration.getLcpCloudRegionId();
			String cloudOwner = cloudConfiguration.getCloudOwner();
			if (cloudRegionId != null && cloudOwner != null && !cloudRegionId.isEmpty() && !cloudOwner.isEmpty()) {
				return injectionHelper.getAaiClient().get(CloudRegion.class,
						AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, cloudOwner,
								cloudRegionId).depth(Depth.TWO)).orElse(null);
			
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	protected InstanceGroup getAAIInstanceGroup(String instanceGroupId) {
		return injectionHelper.getAaiClient().get(InstanceGroup.class,
			AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroupId))
			.orElse(null);
	}

	public org.onap.aai.domain.yang.Customer getAAICustomer(String globalSubscriberId) {
		return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Customer.class,
			AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalSubscriberId))
			.orElse(null);
	}

	public ServiceSubscription getAAIServiceSubscription(String globalSubscriberId, String subscriptionServiceType) {
	
		if(globalSubscriberId == null || globalSubscriberId.equals("") || subscriptionServiceType == null || subscriptionServiceType.equals("")) {
			return null;
		} else {
			return injectionHelper.getAaiClient().get(ServiceSubscription.class, AAIUriFactory.createResourceUri(
					AAIObjectType.SERVICE_SUBSCRIPTION, globalSubscriberId, subscriptionServiceType))
					.orElse(null);
		}
		
	}

	public ServiceInstance getAAIServiceInstanceById(String serviceInstanceId) {
		return injectionHelper.getAaiClient().get(ServiceInstance.class,
				AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId).depth(Depth.TWO))
				.orElse(null);
	}

	protected ServiceInstance getAAIServiceInstanceByIdAndCustomer(String globalCustomerId, String serviceType,
			String serviceInstanceId) {
		return injectionHelper.getAaiClient().get(ServiceInstance.class, AAIUriFactory.createResourceUri(
				AAIObjectType.SERVICE_INSTANCE, globalCustomerId, serviceType, serviceInstanceId).depth(Depth.TWO))
				.orElse(null);
	}

	protected org.onap.aai.domain.yang.ServiceInstances getAAIServiceInstancesByName(String serviceInstanceName,
			Customer customer) {
		
		return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.ServiceInstances.class,
				AAIUriFactory
						.createResourceUri(AAIObjectPlurals.SERVICE_INSTANCE, customer.getGlobalCustomerId(),
								customer.getServiceSubscription().getServiceType())
						.queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO))
				.orElseGet(() -> {
					logger.debug("No Service Instance matched by name");
					return null;
				});
	}

	public org.onap.aai.domain.yang.ServiceInstance getAAIServiceInstanceByName(String serviceInstanceName,
			Customer customer) throws Exception {
		org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = null;
		org.onap.aai.domain.yang.ServiceInstances aaiServiceInstances = null;
		aaiServiceInstances = getAAIServiceInstancesByName(serviceInstanceName, customer);

		if (aaiServiceInstances == null) {
			return null;
		} else if (aaiServiceInstances.getServiceInstance().size() > 1) {
			throw new Exception("Multiple Service Instances Returned");
		} else {
			aaiServiceInstance = aaiServiceInstances.getServiceInstance().get(0);
		}
		return aaiServiceInstance;
	}

	protected ServiceInstances getAAIServiceInstancesByName(String globalCustomerId, String serviceType,
			String serviceInstanceName) {
		
		return injectionHelper.getAaiClient().get(ServiceInstances.class,
				AAIUriFactory.createResourceUri(AAIObjectPlurals.SERVICE_INSTANCE, globalCustomerId, serviceType)
						.queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO))
				.orElseGet(() -> {
					logger.debug("No Service Instance matched by name");
					return null;
				});
	}

	public Optional<ServiceInstance> getAAIServiceInstanceByName(String globalCustomerId, String serviceType,
			String serviceInstanceName) throws Exception {
		ServiceInstance aaiServiceInstance = null;
		ServiceInstances aaiServiceInstances = null;
		aaiServiceInstances = getAAIServiceInstancesByName(globalCustomerId, serviceType, serviceInstanceName);

		if (aaiServiceInstances == null) {
			return Optional.empty();
		} else if (aaiServiceInstances.getServiceInstance().size() > 1) {
			throw new Exception("Multiple Service Instances Returned");
		} else {
			aaiServiceInstance = aaiServiceInstances.getServiceInstance().get(0);
		}
		return Optional.of(aaiServiceInstance);
	}

	public org.onap.so.db.catalog.beans.InstanceGroup getCatalogInstanceGroup(String modelUUID) {
		return this.catalogDbClient.getInstanceGroupByModelUUID(modelUUID);
	}

	public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomization(
			String modelCustomizationUUID) {
		return this.catalogDbClient
				.getCollectionResourceInstanceGroupCustomizationByModelCustUUID(modelCustomizationUUID);
	}

	public AAIResultWrapper getAAIResourceDepthOne(AAIResourceUri aaiResourceUri) {
		AAIResourceUri clonedUri = aaiResourceUri.clone();
		return this.injectionHelper.getAaiClient().get(clonedUri.depth(Depth.ONE));
	}
	
	public AAIResultWrapper getAAIResourceDepthTwo(AAIResourceUri aaiResourceUri) {
		AAIResourceUri clonedUri = aaiResourceUri.clone();
		return this.injectionHelper.getAaiClient().get(clonedUri.depth(Depth.TWO));
	}

	public Configuration getAAIConfiguration(String configurationId) {
		return this.injectionHelper.getAaiClient().get(Configuration.class,
				AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, configurationId).depth(Depth.ONE))
				.orElseGet(() -> {
					logger.debug("No Configuration matched by id");
					return null;
				});
	}
	
	public GenericVnf getAAIGenericVnf(String vnfId) {
		
		return this.injectionHelper.getAaiClient().get(GenericVnf.class,
				AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE))
				.orElseGet(() -> {
					logger.debug("No Generic Vnf matched by id");
					return null;
				});
	}
	
	public VolumeGroup getAAIVolumeGroup(String cloudOwnerId, String cloudRegionId, String volumeGroupId) {
		return this.injectionHelper.getAaiClient().get(VolumeGroup.class,
				AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudOwnerId, cloudRegionId, volumeGroupId).depth(Depth.ONE))
				.orElseGet(() -> {
					logger.debug("No Generic Vnf matched by id");
					return null;
				});
	}

	public VfModule getAAIVfModule(String vnfId, String vfModuleId) {
		return this.injectionHelper.getAaiClient().get(VfModule.class,
				AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId).depth(Depth.ONE))
				.orElseGet(() -> {
					logger.debug("No Generic Vnf matched by id");
					return null;
				});
	}
	
	public L3Network getAAIL3Network(String networkId) {

		return this.injectionHelper.getAaiClient().get(L3Network.class,
				AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId).depth(Depth.ONE))
				.orElseGet(() -> {
					logger.debug("No Generic Vnf matched by id");
					return null;
				});
		
	}
	
	public Optional<L3Network> getRelatedNetworkByNameFromServiceInstance(String serviceInstanceId, String networkName) throws Exception{
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
		uri.relatedTo(AAIObjectPlurals.L3_NETWORK).queryParam("network-name", networkName);
		Optional<L3Networks> networks = injectionHelper.getAaiClient().get(L3Networks.class, uri);
		L3Network network = null;
		if (!networks.isPresent()) {
			logger.debug("No Networks matched by name");
			return Optional.empty();
		} else {
			if (networks.get().getL3Network().size() > 1) {
				throw new Exception("Multiple Networks Returned");
			} else {
				network = networks.get().getL3Network().get(0);
			}
			return Optional.of(network);
		}
	}

	public Optional<GenericVnf> getRelatedVnfByNameFromServiceInstance(String serviceInstanceId, String vnfName) throws Exception {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
		uri.relatedTo(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName);
		Optional<GenericVnfs> vnfs = injectionHelper.getAaiClient().get(GenericVnfs.class, uri);
		GenericVnf vnf = null;
		if (!vnfs.isPresent()) {
			logger.debug("No Vnfs matched by name");
			return Optional.empty();
		} else {
			 if (vnfs.get().getGenericVnf().size() > 1) {
				throw new Exception("Multiple Vnfs Returned");
			} else {
				vnf = vnfs.get().getGenericVnf().get(0);
			}
			return Optional.of(vnf);
		}
	}

	public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVnf(String vnfId, String volumeGroupName) throws Exception{
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
		uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP).queryParam("volume-group-name", volumeGroupName);
		Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);
		VolumeGroup volumeGroup = null;
		if (!volumeGroups.isPresent()) {
			logger.debug("No VolumeGroups matched by name");
			return Optional.empty();
		} else {
			if (volumeGroups.get().getVolumeGroup().size() > 1) {
				throw new Exception("Multiple VolumeGroups Returned");
			} else {
				volumeGroup = volumeGroups.get().getVolumeGroup().get(0);
			}
			return Optional.of(volumeGroup);
		}
	}

	public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVfModule(String vnfId, String vfModuleId, String volumeGroupName) throws Exception {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId);
		uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP).queryParam("volume-group-name", volumeGroupName);
		Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);
		VolumeGroup volumeGroup = null;
		if (!volumeGroups.isPresent()) {
			logger.debug("No VolumeGroups matched by name");
			return Optional.empty();
		} else {
			if (volumeGroups.get().getVolumeGroup().size() > 1) {
				throw new Exception("Multiple VolumeGroups Returned");
			} else {
				volumeGroup = volumeGroups.get().getVolumeGroup().get(0);
			}
			return Optional.of(volumeGroup);
		}
	}
}
