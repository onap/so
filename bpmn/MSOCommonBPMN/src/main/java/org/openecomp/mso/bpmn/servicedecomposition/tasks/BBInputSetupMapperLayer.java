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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Configuration;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CtagAssignment;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Entitlement;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.LagInterface;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.License;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Platform;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Project;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestParameters;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.SegmentationAssignment;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Subnet;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.TunnelXconnect;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.InstanceGroup;
import org.openecomp.mso.db.catalog.beans.InstanceGroupType;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.springframework.stereotype.Component;

@Component("BBInputSetupMapperLayer")
public class BBInputSetupMapperLayer {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
			BBInputSetupMapperLayer.class);

	private ModelMapper modelMapper = new ModelMapper();

	public Customer mapAAICustomer(org.onap.aai.domain.yang.Customer customerAAI) {
		return modelMapper.map(customerAAI, Customer.class);
	}

	public ServiceSubscription mapAAIServiceSubscription(
			org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionAAI) {
		return modelMapper.map(serviceSubscriptionAAI, ServiceSubscription.class);
	}

	protected Project mapAAIProject(org.onap.aai.domain.yang.Project aaiProject) {
		return modelMapper.map(aaiProject, Project.class);
	}

	protected OwningEntity mapAAIOwningEntity(org.onap.aai.domain.yang.OwningEntity aaiOwningEntity) {
		return modelMapper.map(aaiOwningEntity, OwningEntity.class);
	}

	protected Platform mapAAIPlatform(org.onap.aai.domain.yang.Platform aaiPlatform) {
		return modelMapper.map(aaiPlatform, Platform.class);
	}

	protected LineOfBusiness mapAAILineOfBusiness(org.onap.aai.domain.yang.LineOfBusiness aaiLineOfBusiness) {
		return modelMapper.map(aaiLineOfBusiness, LineOfBusiness.class);
	}

	protected SegmentationAssignment mapAAISegmentationAssignment(
			org.onap.aai.domain.yang.SegmentationAssignment aaiSegmentationAssignment) {
		return modelMapper.map(aaiSegmentationAssignment, SegmentationAssignment.class);
	}

	protected CtagAssignment mapAAICtagAssignment(org.onap.aai.domain.yang.CtagAssignment aaiCtagAssignment) {
		return modelMapper.map(aaiCtagAssignment, CtagAssignment.class);
	}

	protected Subnet mapAAISubnet(org.onap.aai.domain.yang.Subnet aaiSubnet) {
		return modelMapper.map(aaiSubnet, Subnet.class);
	}

	protected License mapAAILicense(org.onap.aai.domain.yang.License aaiLicense) {
		return modelMapper.map(aaiLicense, License.class);
	}

	protected Entitlement mapAAIEntitlement(org.onap.aai.domain.yang.Entitlement aaiEntitlement) {
		return modelMapper.map(aaiEntitlement, Entitlement.class);
	}

	protected LagInterface mapAAILagInterface(org.onap.aai.domain.yang.LagInterface aaiLagInterface) {
		return modelMapper.map(aaiLagInterface, LagInterface.class);
	}

	protected VfModule mapAAIVfModule(org.onap.aai.domain.yang.VfModule aaiVfModule) {
		VfModule vfModule = modelMapper.map(aaiVfModule, VfModule.class);
		vfModule.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiVfModule.getOrchestrationStatus()));
		return vfModule;
	}

	public NetworkPolicy mapAAINetworkPolicy(org.onap.aai.domain.yang.NetworkPolicy aaiNetworkPolicy) {
		return modelMapper.map(aaiNetworkPolicy, NetworkPolicy.class);
	}

	protected VolumeGroup mapAAIVolumeGroup(org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup) {
		VolumeGroup volumeGroup = modelMapper.map(aaiVolumeGroup, VolumeGroup.class);
		volumeGroup.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiVolumeGroup.getOrchestrationStatus()));
		return volumeGroup;
	}

	protected void setPlatformAndLOBIntoServiceInstance(Platform platformMSO, LineOfBusiness lineOfBusinessMSO,
			ServiceInstance serviceInstance, Map<ResourceKey, String> resourcesToBeOrchestrated) {
		String vnfId = resourcesToBeOrchestrated.get(ResourceKey.GENERIC_VNF_ID);
		if (vnfId != null && !serviceInstance.getVnfs().isEmpty()) {
			for (GenericVnf vnf : serviceInstance.getVnfs()) {
				if (vnf.getVnfId().equalsIgnoreCase(vnfId)) {
					vnf.setPlatform(platformMSO);
					vnf.setLineOfBusiness(lineOfBusinessMSO);
					break;
				}
			}
		}
	}

	public ModelInfoServiceInstance mapCatalogServiceIntoServiceInstance(Service service) {
		return modelMapper.map(service, ModelInfoServiceInstance.class);
	}

	protected ModelInfoInstanceGroup mapCatalogInstanceGroupToInstanceGroup(InstanceGroup instanceGroup) {
		ModelInfoInstanceGroup modelInfoInstanceGroup = modelMapper.map(instanceGroup, ModelInfoInstanceGroup.class);
		if(instanceGroup.getType().equals(InstanceGroupType.L3_NETWORK))
			modelInfoInstanceGroup.setType(ModelInfoInstanceGroup.TYPE_L3_NETWORK);
		else
			modelInfoInstanceGroup.setType(ModelInfoInstanceGroup.TYPE_VNFC);
		return modelInfoInstanceGroup;
	}

	protected ModelInfoCollection mapCatalogCollectionToCollection(CollectionResourceCustomization collectionCust,
			CollectionResource collectionResource) {
		ModelInfoCollection modelInfoCollection = new ModelInfoCollection();
		modelInfoCollection.setCollectionFunction(collectionCust.getFunction());
		modelInfoCollection.setCollectionRole(collectionCust.getRole());
		modelInfoCollection.setCollectionType(collectionCust.getType());
		modelInfoCollection.setDescription(collectionResource.getDescription());
		modelInfoCollection.setModelInvariantUUID(collectionResource.getModelInvariantUUID());
		return modelInfoCollection;
	}

	public ServiceInstance mapAAIServiceInstanceIntoServiceInstance(
			org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance) {
		ServiceInstance serviceInstance = modelMapper.map(aaiServiceInstance, ServiceInstance.class);
		if (aaiServiceInstance.getAllottedResources() != null) {
			for (org.onap.aai.domain.yang.AllottedResource allottedResource : aaiServiceInstance.getAllottedResources()
					.getAllottedResource()) {
				serviceInstance.getAllottedResources().add(mapAAIAllottedResource(allottedResource));
			}
		}
		serviceInstance.setOrchestrationStatus(
				this.mapOrchestrationStatusFromAAI(aaiServiceInstance.getOrchestrationStatus()));
		return serviceInstance;
	}

	protected AllottedResource mapAAIAllottedResource(org.onap.aai.domain.yang.AllottedResource aaiAllottedResource) {
		AllottedResource allottedResource = modelMapper.map(aaiAllottedResource, AllottedResource.class);
		if(aaiAllottedResource.getTunnelXconnects() != null) {
			allottedResource.getTunnelXconnects().addAll(
					mapAAITunnelXConnect(aaiAllottedResource.getTunnelXconnects().getTunnelXconnect()));
		}
		return allottedResource;
	}
	
	protected List<TunnelXconnect> mapAAITunnelXConnect(List<org.onap.aai.domain.yang.TunnelXconnect> aaiTunnelXConnects) {
		List<TunnelXconnect> tunnelXConnectList = new ArrayList<>();
		if(modelMapper.getTypeMap(org.onap.aai.domain.yang.TunnelXconnect.class, TunnelXconnect.class) == null) {
			modelMapper.addMappings(new PropertyMap<org.onap.aai.domain.yang.TunnelXconnect, TunnelXconnect>() {
				@Override
				protected void configure() {
					map().setUpBandwidth(source.getBandwidthUpWan1());
					map().setUpBandwidth2(source.getBandwidthUpWan2());
					map().setDownBandwidth(source.getBandwidthDownWan1());
					map().setDownBandwidth2(source.getBandwidthDownWan2());
				}
			});
		}
		
		for(org.onap.aai.domain.yang.TunnelXconnect tunnelXConnect : aaiTunnelXConnects) {
			tunnelXConnectList.add(modelMapper.map(tunnelXConnect, TunnelXconnect.class));
		}
		return tunnelXConnectList;
	}

	protected L3Network mapAAIL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network) {
		L3Network network = modelMapper.map(aaiL3Network, L3Network.class);
		mapAllSubnetsIntoL3Network(aaiL3Network, network);
		mapAllCtagAssignmentsIntoL3Network(aaiL3Network, network);
		mapAllSegmentationAssignmentsIntoL3Network(aaiL3Network, network);
		network.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiL3Network.getOrchestrationStatus()));
		return network;
	}

	protected void mapAllSegmentationAssignmentsIntoL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network,
			L3Network network) {
		if (aaiL3Network.getSegmentationAssignments() != null) {
			for (org.onap.aai.domain.yang.SegmentationAssignment aaiSegmentationAssignment : aaiL3Network
					.getSegmentationAssignments().getSegmentationAssignment()) {
				network.getSegmentationAssignments().add(mapAAISegmentationAssignment(aaiSegmentationAssignment));
			}
		}
	}

	protected void mapAllCtagAssignmentsIntoL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network,
			L3Network network) {
		if (aaiL3Network.getCtagAssignments() != null) {
			for (org.onap.aai.domain.yang.CtagAssignment aaiCtagAssignment : aaiL3Network.getCtagAssignments()
					.getCtagAssignment()) {
				network.getCtagAssignments().add(mapAAICtagAssignment(aaiCtagAssignment));
			}
		}
	}

	protected void mapAllSubnetsIntoL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network, L3Network network) {
		if (aaiL3Network.getSubnets() != null) {
			for (org.onap.aai.domain.yang.Subnet aaiSubnet : aaiL3Network.getSubnets().getSubnet()) {
				network.getSubnets().add(mapAAISubnet(aaiSubnet));
			}
		}
	}

	protected GenericVnf mapAAIGenericVnfIntoGenericVnf(org.onap.aai.domain.yang.GenericVnf aaiGenericVnf) {
		GenericVnf genericVnf = modelMapper.map(aaiGenericVnf, GenericVnf.class);
		mapAllVfModulesIntoGenericVnf(aaiGenericVnf, genericVnf);
		mapAllLagInterfacesIntoGenericVnf(aaiGenericVnf, genericVnf);
		mapAllEntitlementsIntoGenericVnf(aaiGenericVnf, genericVnf);
		mapAllLicensesIntoGenericVnf(aaiGenericVnf, genericVnf);
		genericVnf.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiGenericVnf.getOrchestrationStatus()));
		return genericVnf;
	}

	protected void mapAllLicensesIntoGenericVnf(org.onap.aai.domain.yang.GenericVnf aaiGenericVnf,
			GenericVnf genericVnf) {
		if (aaiGenericVnf.getLicenses() != null) {
			for (org.onap.aai.domain.yang.License aaiLicense : aaiGenericVnf.getLicenses().getLicense()) {
				genericVnf.setLicense(mapAAILicense(aaiLicense));
			}
		}
	}

	protected void mapAllEntitlementsIntoGenericVnf(org.onap.aai.domain.yang.GenericVnf aaiGenericVnf,
			GenericVnf genericVnf) {
		if (aaiGenericVnf.getEntitlements() != null) {
			for (org.onap.aai.domain.yang.Entitlement aaiEntitlement : aaiGenericVnf.getEntitlements()
					.getEntitlement()) {
				genericVnf.getEntitlements().add(mapAAIEntitlement(aaiEntitlement));
			}
		}
	}

	protected void mapAllLagInterfacesIntoGenericVnf(org.onap.aai.domain.yang.GenericVnf aaiGenericVnf,
			GenericVnf genericVnf) {
		if (aaiGenericVnf.getLagInterfaces() != null) {
			for (org.onap.aai.domain.yang.LagInterface aaiLagInterface : aaiGenericVnf.getLagInterfaces()
					.getLagInterface()) {
				genericVnf.getLagInterfaces().add(mapAAILagInterface(aaiLagInterface));
			}
		}
	}

	protected void mapAllVfModulesIntoGenericVnf(org.onap.aai.domain.yang.GenericVnf aaiGenericVnf,
			GenericVnf genericVnf) {
		if (aaiGenericVnf.getVfModules() != null) {
			for (org.onap.aai.domain.yang.VfModule aaiVfModule : aaiGenericVnf.getVfModules().getVfModule()) {
				VfModule vfModule = mapAAIVfModule(aaiVfModule);
				genericVnf.getVfModules().add(vfModule);
			}
		}
	}

	public OrchestrationStatus mapOrchestrationStatusFromAAI(String orchestrationStatus) {
		if (orchestrationStatus != null) {
			if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.INVENTORIED.toString())) {
				return OrchestrationStatus.INVENTORIED;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.ASSIGNED.toString())) {
				return OrchestrationStatus.ASSIGNED;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.ACTIVE.toString())) {
				return OrchestrationStatus.ACTIVE;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.CREATED.toString())) {
				return OrchestrationStatus.CREATED;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.PRECREATED.toString())) {
				return OrchestrationStatus.PRECREATED;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.PENDING_CREATE.toString())) {
				return OrchestrationStatus.PENDING_CREATE;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.PENDING_DELETE.toString())) {
				return OrchestrationStatus.PENDING_DELETE;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.PENDING.toString())) {
				return OrchestrationStatus.PENDING;
			} else if (orchestrationStatus.equalsIgnoreCase(OrchestrationStatus.PENDING_ACTIVATION.toString())) {
				return OrchestrationStatus.PENDING_ACTIVATION;
			} else
				return null;
		} else {
			return null;
		}
	}

	public RequestContext mapRequestContext(RequestDetails requestDetails) {
		RequestContext context = new RequestContext();
		modelMapper.map(requestDetails.getRequestInfo(), context);
		org.openecomp.mso.serviceinstancebeans.RequestParameters requestParameters = requestDetails.getRequestParameters();
		if (null != requestParameters) {
			context.setSubscriptionServiceType(requestParameters.getSubscriptionServiceType());
			context.setRequestParameters(this.mapRequestParameters(requestDetails.getRequestParameters()));
		}
		return context;
	}

	protected RequestParameters mapRequestParameters(org.openecomp.mso.serviceinstancebeans.RequestParameters requestParameters) {
		RequestParameters requestParams = new RequestParameters();
		requestParams.setaLaCarte(requestParameters.getALaCarte());
		requestParams.setSubscriptionServiceType(requestParameters.getSubscriptionServiceType());
		requestParams.setUserParams(requestParameters.getUserParams());
		return requestParams;
	}

	protected OrchestrationContext mapOrchestrationContext(RequestDetails requestDetails) {
		OrchestrationContext context = new OrchestrationContext();
		context.setIsRollbackEnabled((requestDetails.getRequestInfo().getSuppressRollback()));
		return context;
	}

	protected CloudRegion mapCloudRegion(RequestDetails requestDetails, org.onap.aai.domain.yang.CloudRegion aaiCloudRegion, String cloudOwner) {
		CloudRegion cloudRegion = new CloudRegion();
		if(requestDetails.getCloudConfiguration() != null)
			cloudRegion = modelMapper.map(requestDetails.getCloudConfiguration(), CloudRegion.class);
		if(aaiCloudRegion != null)
			modelMapper.map(aaiCloudRegion, cloudRegion);
		if(cloudOwner != null)
			cloudRegion.setCloudOwner(cloudOwner);
		return cloudRegion;
	}

	protected Collection mapAAICollectionIntoCollection(org.onap.aai.domain.yang.Collection aaiCollection) {
		Collection collection = new Collection();
		collection.setId(aaiCollection.getCollectionId());
		collection.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiCollection.getOrchestrationStatus()));
		return collection;
	}

	protected org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup mapAAIInstanceGroupIntoInstanceGroup(
			org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup) {
		return modelMapper.map(aaiInstanceGroup,
				org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup.class);
	}

	public RouteTableReference mapAAIRouteTableReferenceIntoRouteTableReference(
			org.onap.aai.domain.yang.RouteTableReference aaiRouteTableReference) {
		return modelMapper.map(aaiRouteTableReference, RouteTableReference.class);
	}

	protected ModelInfoNetwork mapCatalogNetworkToNetwork(NetworkResourceCustomization networkResourceCustomization) {
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ModelInfoNetwork modelInfoNetwork = modelMapper.map(networkResourceCustomization, ModelInfoNetwork.class);
		modelMapper.map(networkResourceCustomization.getNetworkResource(), modelInfoNetwork);
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
		return modelInfoNetwork;
	}

	protected ModelInfoGenericVnf mapCatalogVnfToVnf(VnfResourceCustomization vnfResourceCustomization) {
		ModelInfoGenericVnf modelInfoVnf = modelMapper.map(vnfResourceCustomization, ModelInfoGenericVnf.class);
		modelMapper.map(vnfResourceCustomization.getVnfResources(), modelInfoVnf);
		return modelInfoVnf;
	}

	protected ModelInfoVfModule mapCatalogVfModuleToVfModule(VfModuleCustomization vfResourceCustomization) {
		ModelInfoVfModule modelInfoVfModule = modelMapper.map(vfResourceCustomization, ModelInfoVfModule.class);
		modelMapper.map(vfResourceCustomization.getVfModule(), modelInfoVfModule);
		return modelInfoVfModule;
	}

	protected Platform mapRequestPlatform(org.openecomp.mso.serviceinstancebeans.Platform platform) {
		return modelMapper.map(platform, Platform.class);
	}

	protected LineOfBusiness mapRequestLineOfBusiness(
			org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness) {
		return modelMapper.map(lineOfBusiness, LineOfBusiness.class);
	}

	public Configuration mapAAIConfiguration(org.onap.aai.domain.yang.Configuration configurationAAI) {
		Configuration configuration = modelMapper.map(configurationAAI, Configuration.class);
		configuration.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(configurationAAI.getOrchestrationStatus()));
		return configuration;
	}

	public OwningEntity mapRequestOwningEntity(org.openecomp.mso.serviceinstancebeans.OwningEntity owningEntity) {
		return modelMapper.map(owningEntity, OwningEntity.class);
	}

	public Project mapRequestProject(org.openecomp.mso.serviceinstancebeans.Project project) {
		return modelMapper.map(project, Project.class);
	}
}
