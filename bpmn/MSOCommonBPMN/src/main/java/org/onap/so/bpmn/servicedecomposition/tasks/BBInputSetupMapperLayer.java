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

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.onap.so.bpmn.servicedecomposition.bbobjects.AggregateRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CtagAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Entitlement;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Evc;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ForwarderEvc;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LagInterface;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget;
import org.onap.so.bpmn.servicedecomposition.bbobjects.SegmentationAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Vnfc;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceProxy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("BBInputSetupMapperLayer")
public class BBInputSetupMapperLayer {
    private static final String USER_PARAM_NAME_KEY = "name";
    private static final String USER_PARAM_VALUE_KEY = "value";

    private static final Logger logger = LoggerFactory.getLogger(BBInputSetupMapperLayer.class);

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

    protected AggregateRoute mapAAIAggregateRoute(org.onap.aai.domain.yang.AggregateRoute aaiAggregateRoute) {
        return modelMapper.map(aaiAggregateRoute, AggregateRoute.class);
    }

    protected CtagAssignment mapAAICtagAssignment(org.onap.aai.domain.yang.CtagAssignment aaiCtagAssignment) {
        return modelMapper.map(aaiCtagAssignment, CtagAssignment.class);
    }

    protected Subnet mapAAISubnet(org.onap.aai.domain.yang.Subnet aaiSubnet) {
        Subnet subnet = modelMapper.map(aaiSubnet, Subnet.class);
        mapAllHostRoutesIntoSubnet(aaiSubnet, subnet);
        return subnet;
    }

    protected void mapAllHostRoutesIntoSubnet(org.onap.aai.domain.yang.Subnet aaiSubnet, Subnet subnet) {
        if (aaiSubnet.getHostRoutes() != null) {
            for (org.onap.aai.domain.yang.HostRoute aaiHostRoute : aaiSubnet.getHostRoutes().getHostRoute()) {
                subnet.getHostRoutes().add(mapAAIHostRoute(aaiHostRoute));
            }
        }
    }

    protected HostRoute mapAAIHostRoute(org.onap.aai.domain.yang.HostRoute aaiHostRoute) {
        return modelMapper.map(aaiHostRoute, HostRoute.class);
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

        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setIsBaseBoolean(aaiVfModule.isIsBaseVfModule());
        vfModule.setModelInfoVfModule(modelInfoVfModule);
        return vfModule;
    }

    public NetworkPolicy mapAAINetworkPolicy(org.onap.aai.domain.yang.NetworkPolicy aaiNetworkPolicy) {
        return modelMapper.map(aaiNetworkPolicy, NetworkPolicy.class);
    }

    protected VolumeGroup mapAAIVolumeGroup(org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup) {
        VolumeGroup volumeGroup = modelMapper.map(aaiVolumeGroup, VolumeGroup.class);
        ModelInfoVfModule modelInfo = new ModelInfoVfModule();
        modelInfo.setModelCustomizationUUID(aaiVolumeGroup.getModelCustomizationId());
        volumeGroup.setModelInfoVfModule(modelInfo);
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

    protected ModelInfoInstanceGroup mapCatalogInstanceGroupToInstanceGroup(
            CollectionResourceCustomization collectionCust, InstanceGroup instanceGroup) {
        ModelInfoInstanceGroup modelInfoInstanceGroup = modelMapper.map(instanceGroup, ModelInfoInstanceGroup.class);
        if (instanceGroup.getType() != null && instanceGroup.getType().equals(InstanceGroupType.L3_NETWORK))
            modelInfoInstanceGroup.setType(ModelInfoInstanceGroup.TYPE_L3_NETWORK);
        else
            modelInfoInstanceGroup.setType(ModelInfoInstanceGroup.TYPE_VNFC);
        if (collectionCust != null) {
            List<CollectionResourceInstanceGroupCustomization> instanceGroupCustList =
                    instanceGroup.getCollectionInstanceGroupCustomizations();
            for (CollectionResourceInstanceGroupCustomization collectionInsatnceGroupCust : instanceGroupCustList) {
                if (collectionInsatnceGroupCust.getModelCustomizationUUID()
                        .equalsIgnoreCase(collectionCust.getModelCustomizationUUID())) {
                    modelInfoInstanceGroup.setFunction(collectionInsatnceGroupCust.getFunction());
                    modelInfoInstanceGroup.setDescription(collectionInsatnceGroupCust.getDescription());
                    break;
                }
            }
        }
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
        modelInfoCollection.setModelVersionId(collectionResource.getModelUUID());
        modelInfoCollection.setModelCustomizationUUID(collectionCust.getModelCustomizationUUID());
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
        return allottedResource;
    }

    protected L3Network mapAAIL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network) {
        L3Network network = modelMapper.map(aaiL3Network, L3Network.class);
        mapAllSubnetsIntoL3Network(aaiL3Network, network);
        mapAllCtagAssignmentsIntoL3Network(aaiL3Network, network);
        mapAllSegmentationAssignmentsIntoL3Network(aaiL3Network, network);
        mapAllAggregateRoutesIntoL3Network(aaiL3Network, network);
        network.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiL3Network.getOrchestrationStatus()));
        return network;
    }

    protected void mapAllAggregateRoutesIntoL3Network(org.onap.aai.domain.yang.L3Network aaiL3Network,
            L3Network network) {
        if (aaiL3Network.getAggregateRoutes() != null) {
            for (org.onap.aai.domain.yang.AggregateRoute aaiAggregateRoute : aaiL3Network.getAggregateRoutes()
                    .getAggregateRoute()) {
                network.getAggregateRoutes().add(mapAAIAggregateRoute(aaiAggregateRoute));
            }
        }
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

    protected Pnf mapAAIPnfIntoPnf(org.onap.aai.domain.yang.Pnf aaiPnf) {
        return modelMapper.map(aaiPnf, Pnf.class);
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

        Optional<OrchestrationStatus> result = Arrays.asList(OrchestrationStatus.values()).stream()
                .filter(item -> item.fuzzyMap(orchestrationStatus)).findFirst();

        return result.orElse(null);

    }

    public RequestContext mapRequestContext(RequestDetails requestDetails) {
        RequestContext context = new RequestContext();
        if (null != requestDetails.getRequestInfo()) {
            modelMapper.map(requestDetails.getRequestInfo(), context);
        }
        org.onap.so.serviceinstancebeans.RequestParameters requestParameters = requestDetails.getRequestParameters();
        if (null != requestParameters) {
            context.setSubscriptionServiceType(requestParameters.getSubscriptionServiceType());
            context.setRequestParameters(this.mapRequestParameters(requestDetails.getRequestParameters()));
            context.setUserParams(this.mapNameValueUserParams(requestDetails.getRequestParameters()));
        }
        if (requestDetails.getConfigurationParameters() != null) {
            context.setConfigurationParameters(requestDetails.getConfigurationParameters());
        }
        return context;
    }

    protected RequestParameters mapRequestParameters(
            org.onap.so.serviceinstancebeans.RequestParameters requestParameters) {
        RequestParameters requestParams = new RequestParameters();
        requestParams.setaLaCarte(requestParameters.getALaCarte());
        requestParams.setUsePreload(requestParameters.getUsePreload());
        requestParams.setSubscriptionServiceType(requestParameters.getSubscriptionServiceType());
        requestParams.setUserParams(requestParameters.getUserParams());
        requestParams.setPayload(requestParameters.getPayload());
        return requestParams;
    }

    protected Map<String, Object> mapNameValueUserParams(
            org.onap.so.serviceinstancebeans.RequestParameters requestParameters) {
        Map<String, Object> userParamsResult = new HashMap<String, Object>();
        if (requestParameters.getUserParams() != null) {
            List<Map<String, Object>> userParams = requestParameters.getUserParams();
            for (Map<String, Object> userParamsMap : userParams) {
                if (userParamsMap.containsKey(USER_PARAM_NAME_KEY)
                        && (userParamsMap.get(USER_PARAM_NAME_KEY) instanceof String)
                        && userParamsMap.containsKey(USER_PARAM_VALUE_KEY)) {
                    userParamsResult.put((String) userParamsMap.get(USER_PARAM_NAME_KEY),
                            userParamsMap.get(USER_PARAM_VALUE_KEY));
                }
            }
        }
        return userParamsResult;
    }

    protected OrchestrationContext mapOrchestrationContext(RequestDetails requestDetails) {
        OrchestrationContext context = new OrchestrationContext();
        if (requestDetails.getRequestInfo() != null) {
            context.setIsRollbackEnabled(!(requestDetails.getRequestInfo().getSuppressRollback()));
        } else {
            context.setIsRollbackEnabled(false);
        }
        return context;
    }

    protected CloudRegion mapCloudRegion(CloudConfiguration cloudConfiguration,
            org.onap.aai.domain.yang.CloudRegion aaiCloudRegion) {
        CloudRegion cloudRegion = new CloudRegion();
        if (cloudConfiguration != null)
            cloudRegion = modelMapper.map(cloudConfiguration, CloudRegion.class);
        if (aaiCloudRegion != null)
            modelMapper.map(aaiCloudRegion, cloudRegion);
        return cloudRegion;
    }

    protected Tenant mapTenant(org.onap.aai.domain.yang.Tenant aaiTenant) {
        Tenant tenant = new Tenant();
        if (aaiTenant != null) {
            modelMapper.map(aaiTenant, tenant);
        }
        return tenant;
    }

    protected Collection mapAAICollectionIntoCollection(org.onap.aai.domain.yang.Collection aaiCollection) {
        Collection collection = new Collection();
        collection.setId(aaiCollection.getCollectionId());
        collection.setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(aaiCollection.getOrchestrationStatus()));
        return collection;
    }

    protected org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup mapAAIInstanceGroupIntoInstanceGroup(
            org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup) {
        return modelMapper.map(aaiInstanceGroup, org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup.class);
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

    protected Platform mapRequestPlatform(org.onap.so.serviceinstancebeans.Platform platform) {
        return modelMapper.map(platform, Platform.class);
    }

    protected LineOfBusiness mapRequestLineOfBusiness(org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness) {
        return modelMapper.map(lineOfBusiness, LineOfBusiness.class);
    }

    public Configuration mapAAIConfiguration(org.onap.aai.domain.yang.Configuration configurationAAI) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Configuration configuration = modelMapper.map(configurationAAI, Configuration.class);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        configuration.getForwarderEvcs().addAll(mapAllForwarderEvcs(configurationAAI));
        configuration.getEvcs().addAll(mapAllEvcs(configurationAAI));
        configuration
                .setOrchestrationStatus(this.mapOrchestrationStatusFromAAI(configurationAAI.getOrchestrationStatus()));
        return configuration;
    }

    protected List<Evc> mapAllEvcs(org.onap.aai.domain.yang.Configuration configurationAAI) {
        List<Evc> listOfEvcs = new ArrayList<>();
        if (configurationAAI.getEvcs() != null) {
            for (org.onap.aai.domain.yang.Evc aaiEvc : configurationAAI.getEvcs().getEvc()) {
                listOfEvcs.add(mapEvc(aaiEvc));
            }
        }
        return listOfEvcs;
    }

    protected Evc mapEvc(org.onap.aai.domain.yang.Evc aaiEvc) {
        return modelMapper.map(aaiEvc, Evc.class);
    }

    protected List<ForwarderEvc> mapAllForwarderEvcs(org.onap.aai.domain.yang.Configuration configurationAAI) {
        List<ForwarderEvc> listOfForwarderEvcs = new ArrayList<>();
        if (configurationAAI.getForwarderEvcs() != null) {
            for (org.onap.aai.domain.yang.ForwarderEvc aaiForwarderEvc : configurationAAI.getForwarderEvcs()
                    .getForwarderEvc()) {
                listOfForwarderEvcs.add(mapForwarderEvc(aaiForwarderEvc));
            }
        }
        return listOfForwarderEvcs;
    }

    protected ForwarderEvc mapForwarderEvc(org.onap.aai.domain.yang.ForwarderEvc aaiForwarderEvc) {
        return modelMapper.map(aaiForwarderEvc, ForwarderEvc.class);
    }

    protected OwningEntity mapRequestOwningEntity(org.onap.so.serviceinstancebeans.OwningEntity owningEntity) {
        return modelMapper.map(owningEntity, OwningEntity.class);
    }

    protected Project mapRequestProject(org.onap.so.serviceinstancebeans.Project project) {
        return modelMapper.map(project, Project.class);
    }

    protected ModelInfoConfiguration mapCatalogConfigurationToConfiguration(
            ConfigurationResourceCustomization configurationResourceCustomization,
            CvnfcConfigurationCustomization cvnfcConfigurationCustomization) {

        ModelInfoConfiguration modelInfoConfiguration = new ModelInfoConfiguration();
        modelInfoConfiguration
                .setModelVersionId(configurationResourceCustomization.getConfigurationResource().getModelUUID());
        modelInfoConfiguration.setModelCustomizationId(configurationResourceCustomization.getModelCustomizationUUID());
        modelInfoConfiguration.setModelInvariantId(
                configurationResourceCustomization.getConfigurationResource().getModelInvariantUUID());
        modelInfoConfiguration.setConfigurationRole(configurationResourceCustomization.getRole());
        modelInfoConfiguration.setConfigurationType(configurationResourceCustomization.getType());
        if (cvnfcConfigurationCustomization != null) {
            modelInfoConfiguration.setPolicyName(cvnfcConfigurationCustomization.getPolicyName());
        }
        return modelInfoConfiguration;
    }

    protected ModelInfoConfiguration mapCatalogConfigurationToConfiguration(
            CvnfcConfigurationCustomization cvnfcConfigurationCustomization) {
        ModelInfoConfiguration modelInfoConfiguration = new ModelInfoConfiguration();
        modelInfoConfiguration
                .setModelVersionId(cvnfcConfigurationCustomization.getConfigurationResource().getModelUUID());
        modelInfoConfiguration.setModelCustomizationId(cvnfcConfigurationCustomization.getModelCustomizationUUID());
        modelInfoConfiguration.setModelInvariantId(
                cvnfcConfigurationCustomization.getConfigurationResource().getModelInvariantUUID());
        modelInfoConfiguration.setPolicyName(cvnfcConfigurationCustomization.getPolicyName());
        modelInfoConfiguration.setConfigurationType(cvnfcConfigurationCustomization.getConfigurationType());
        modelInfoConfiguration.setConfigurationRole(cvnfcConfigurationCustomization.getConfigurationRole());
        return modelInfoConfiguration;
    }

    public NetworkResourceCustomization mapCollectionNetworkResourceCustToNetworkResourceCust(
            CollectionNetworkResourceCustomization collectionNetworkResourceCust) {
        return modelMapper.map(collectionNetworkResourceCust, NetworkResourceCustomization.class);
    }

    public Vnfc mapAAIVnfc(org.onap.aai.domain.yang.Vnfc vnfcAAI) {
        return modelMapper.map(vnfcAAI, Vnfc.class);
    }

    public VpnBinding mapAAIVpnBinding(org.onap.aai.domain.yang.VpnBinding aaiVpnBinding) {
        VpnBinding vpnBinding = modelMapper.map(aaiVpnBinding, VpnBinding.class);
        mapAllRouteTargetsToAAIVpnBinding(aaiVpnBinding, vpnBinding);
        return vpnBinding;
    }

    protected void mapAllRouteTargetsToAAIVpnBinding(org.onap.aai.domain.yang.VpnBinding aaiVpnBinding,
            VpnBinding vpnBinding) {
        if (aaiVpnBinding.getRouteTargets() != null) {
            for (org.onap.aai.domain.yang.RouteTarget aaiRouteTarget : aaiVpnBinding.getRouteTargets()
                    .getRouteTarget()) {
                vpnBinding.getRouteTargets().add(mapAAIRouteTarget(aaiRouteTarget));
            }
        }
    }

    public RouteTarget mapAAIRouteTarget(org.onap.aai.domain.yang.RouteTarget aaiRouteTarget) {
        return modelMapper.map(aaiRouteTarget, RouteTarget.class);
    }

    protected ModelInfoServiceProxy mapServiceProxyCustomizationToServiceProxy(
            ServiceProxyResourceCustomization serviceProxyCustomization) {
        ModelInfoServiceProxy modelInfoServiceProxy =
                modelMapper.map(serviceProxyCustomization.getSourceService(), ModelInfoServiceProxy.class);
        modelInfoServiceProxy.setModelInstanceName(serviceProxyCustomization.getModelInstanceName());
        return modelInfoServiceProxy;
    }
}
