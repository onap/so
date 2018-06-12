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

package org.openecomp.mso.bpmn.servicedecomposition.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.L3Networks;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.entities.uri.Depth;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.client.db.request.RequestsDbClient;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component("BBInputSetupUtils")
public class BBInputSetupUtils {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, BBInputSetupUtils.class);
	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	protected CatalogDbClient catalogDbClient;

	@Autowired
	protected RequestsDbClient requestsDbClient;

	@Autowired
	protected InjectionHelper injectionHelper;

	public Service getCatalogServiceByModelUUID(String modelUUID) {
		return catalogDbClient.getServiceByID(modelUUID);
	}
	
	public Service getCatalogServiceByModelVersionAndModelInvariantUUID(String modelVersion, String modelInvariantUUID) {
		return catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(modelVersion, modelInvariantUUID);
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

	protected CloudRegion getCloudRegion(RequestDetails requestDetails, String cloudOwner) {
		if (requestDetails.getCloudConfiguration() != null) {
			String cloudRegionId = requestDetails.getCloudConfiguration().getLcpCloudRegionId();
			if (cloudRegionId != null && !cloudRegionId.isEmpty()) {
				return injectionHelper.getAaiClient().get(CloudRegion.class,
						AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, cloudOwner,
								requestDetails.getCloudConfiguration().getLcpCloudRegionId())).orElse(null);
			
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
					msoLogger.debug("No Service Instance matched by name");
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
					msoLogger.debug("No Service Instance matched by name");
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

	public org.openecomp.mso.db.catalog.beans.InstanceGroup getCatalogInstanceGroup(String modelUUID) {
		return this.catalogDbClient.getInstanceGroupByModelUUID(modelUUID);
	}

	public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomization(
			String modelCustomizationUUID) {
		return this.catalogDbClient
				.getCollectionResourceInstanceGroupCustomizationByModelCustUUID(modelCustomizationUUID);
	}

	public AAIResultWrapper getAAIGenericVnf(AAIResourceUri aaiResourceUri) {
		return this.injectionHelper.getAaiClient().get(aaiResourceUri.depth(Depth.ONE));
	}
	
	public AAIResultWrapper getAAIL3Network(AAIResourceUri aaiResourceUri) {
		return this.injectionHelper.getAaiClient().get(aaiResourceUri.depth(Depth.TWO));
	}
	
	public GenericVnf getAAIGenericVnf(String vnfId) {
		
		return this.injectionHelper.getAaiClient().get(GenericVnf.class,
				AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE))
				.orElseGet(() -> {
					msoLogger.debug("No Generic Vnf matched by id");
					return null;
				});
	}
	
	public Optional<L3Network> getRelatedNetworkByNameFromServiceInstance(String serviceInstanceId, String networkName) throws Exception{
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
		uri.relatedTo(AAIObjectPlurals.L3_NETWORK).queryParam("network-name", networkName);
		Optional<L3Networks> networks = injectionHelper.getAaiClient().get(L3Networks.class, uri);
		L3Network network = null;
		if (!networks.isPresent()) {
			msoLogger.debug("No Networks matched by name");
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
			msoLogger.debug("No Vnfs matched by name");
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
			msoLogger.debug("No VolumeGroups matched by name");
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

	public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVfModule(String vfModuleId, String volumeGroupName) throws Exception {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vfModuleId);
		uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP).queryParam("volume-group-name", volumeGroupName);
		Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);
		VolumeGroup volumeGroup = null;
		if (!volumeGroups.isPresent()) {
			msoLogger.debug("No VolumeGroups matched by name");
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
