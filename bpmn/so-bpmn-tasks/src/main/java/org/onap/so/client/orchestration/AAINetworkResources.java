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

package org.onap.so.client.orchestration;

import java.util.Optional;
import org.onap.aai.domain.yang.NetworkPolicies;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIEdgeLabel;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIBaseResourceUri;
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAINetworkResources {

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void updateNetwork(L3Network network) {
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId());
        org.onap.aai.domain.yang.L3Network aaiL3Network = aaiObjectMapper.mapNetwork(network);
        injectionHelper.getAaiClient().update(networkURI, aaiL3Network);
    }

    public void updateSubnet(L3Network network, Subnet subnet) {
        AAIResourceUri subnetURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SUBNET, network.getNetworkId(), subnet.getSubnetId());
        org.onap.aai.domain.yang.Subnet aaiSubnet = aaiObjectMapper.mapSubnet(subnet);
        injectionHelper.getAaiClient().update(subnetURI, aaiSubnet);
    }

    public void createNetworkConnectToServiceInstance(L3Network network, ServiceInstance serviceInstance) {
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId());
        network.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstance.getServiceInstanceId());
        org.onap.aai.domain.yang.L3Network aaiL3Network = aaiObjectMapper.mapNetwork(network);
        injectionHelper.getAaiClient().createIfNotExists(networkURI, Optional.of(aaiL3Network)).connect(networkURI,
                serviceInstanceURI);
    }

    public void createLineOfBusinessAndConnectNetwork(LineOfBusiness lineOfBusiness, L3Network network) {
        AAIResourceUri lineOfBusinessURI =
                AAIUriFactory.createResourceUri(AAIObjectType.LINE_OF_BUSINESS, lineOfBusiness.getLineOfBusinessName());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId());
        injectionHelper.getAaiClient().createIfNotExists(lineOfBusinessURI, Optional.of(lineOfBusiness))
                .connect(networkURI, lineOfBusinessURI);
    }

    public void createPlatformAndConnectNetwork(Platform platform, L3Network network) {
        AAIResourceUri platformURI =
                AAIUriFactory.createResourceUri(AAIObjectType.PLATFORM, platform.getPlatformName());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId());
        injectionHelper.getAaiClient().createIfNotExists(platformURI, Optional.of(platform)).connect(networkURI,
                platformURI);
    }

    public void deleteNetwork(L3Network network) {
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId());
        injectionHelper.getAaiClient().delete(networkURI);
    }

    public Optional<VpnBinding> getVpnBinding(AAIResourceUri netBindingUri) {
        return injectionHelper.getAaiClient().get(netBindingUri.depth(Depth.TWO)).asBean(VpnBinding.class);
    }

    public Optional<NetworkPolicy> getNetworkPolicy(AAIBaseResourceUri netPolicyUri) {
        return injectionHelper.getAaiClient().get(netPolicyUri).asBean(NetworkPolicy.class);
    }

    public Optional<NetworkPolicies> getNetworkPolicies(AAIBaseResourceUri netPoliciesUri) {
        return injectionHelper.getAaiClient().get(netPoliciesUri).asBean(NetworkPolicies.class);
    }

    public Optional<org.onap.aai.domain.yang.Subnet> getSubnet(AAIResourceUri subnetUri) {
        return injectionHelper.getAaiClient().get(subnetUri).asBean(org.onap.aai.domain.yang.Subnet.class);
    }

    public Optional<RouteTableReference> getRouteTable(AAIResourceUri rTableUri) {
        return injectionHelper.getAaiClient().get(rTableUri).asBean(RouteTableReference.class);
    }

    public Optional<org.onap.aai.domain.yang.L3Network> queryNetworkById(L3Network l3network) {
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId()).depth(Depth.ALL);
        AAIResultWrapper aaiWrapper = injectionHelper.getAaiClient().get(uri);
        return aaiWrapper.asBean(org.onap.aai.domain.yang.L3Network.class);
    }

    public AAIResultWrapper queryNetworkWrapperById(L3Network l3network) {
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId()).depth(Depth.ALL);
        return injectionHelper.getAaiClient().get(uri);
    }

    public void createNetworkInstanceGroup(InstanceGroup instanceGroup) {
        AAIResourceUri instanceGroupURI =
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = aaiObjectMapper.mapInstanceGroup(instanceGroup);
        injectionHelper.getAaiClient().create(instanceGroupURI, aaiInstanceGroup);
    }

    public void createNetworkCollection(Collection networkCollection) {
        AAIResourceUri networkCollectionURI =
                AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId());
        networkCollection.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        org.onap.aai.domain.yang.Collection aaiCollection = aaiObjectMapper.mapCollection(networkCollection);
        injectionHelper.getAaiClient().create(networkCollectionURI, aaiCollection);
    }

    public void connectNetworkToTenant(L3Network l3network, CloudRegion cloudRegion) {
        AAIResourceUri tenantURI = AAIUriFactory.createResourceUri(AAIObjectType.TENANT, cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), cloudRegion.getTenantId());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId());
        injectionHelper.getAaiClient().connect(tenantURI, networkURI);
    }

    public void connectNetworkToCloudRegion(L3Network l3network, CloudRegion cloudRegion) {
        AAIResourceUri cloudRegionURI = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId());
        injectionHelper.getAaiClient().connect(networkURI, cloudRegionURI);
    }

    public void connectNetworkToNetworkCollectionInstanceGroup(L3Network l3network, InstanceGroup instanceGroup) {
        AAIResourceUri netwrokCollectionInstanceGroupURI =
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId());
        injectionHelper.getAaiClient().connect(netwrokCollectionInstanceGroupURI, networkURI);
    }

    public void connectNetworkToNetworkCollectionServiceInstance(L3Network l3network,
            ServiceInstance networkCollectionServiceInstance) {
        AAIResourceUri networkCollectionServiceInstanceUri = AAIUriFactory.createResourceUri(
                AAIObjectType.SERVICE_INSTANCE, networkCollectionServiceInstance.getServiceInstanceId());
        AAIResourceUri networkURI = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3network.getNetworkId());
        injectionHelper.getAaiClient().connect(networkCollectionServiceInstanceUri, networkURI);
    }

    public void connectNetworkCollectionInstanceGroupToNetworkCollection(InstanceGroup instanceGroup,
            Collection networkCollection) {
        AAIResourceUri networkCollectionUri =
                AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId());
        AAIResourceUri netwrokCollectionInstanceGroupURI =
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
        injectionHelper.getAaiClient().connect(networkCollectionUri, netwrokCollectionInstanceGroupURI);
    }

    public void connectInstanceGroupToCloudRegion(InstanceGroup instanceGroup, CloudRegion cloudRegion) {
        AAIResourceUri cloudRegionURI = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId());
        AAIResourceUri instanceGroupURI =
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
        injectionHelper.getAaiClient().connect(instanceGroupURI, cloudRegionURI, AAIEdgeLabel.USES);
    }

    public void connectNetworkCollectionToServiceInstance(Collection networkCollection,
            ServiceInstance networkCollectionServiceInstance) {
        AAIResourceUri networkCollectionUri =
                AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, networkCollection.getId());
        AAIResourceUri networkCollectionServiceInstanceUri = AAIUriFactory.createResourceUri(
                AAIObjectType.SERVICE_INSTANCE, networkCollectionServiceInstance.getServiceInstanceId());
        injectionHelper.getAaiClient().connect(networkCollectionUri, networkCollectionServiceInstanceUri);
    }

    public void deleteCollection(Collection collection) {
        AAIResourceUri collectionURI = AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, collection.getId());
        injectionHelper.getAaiClient().delete(collectionURI);
    }

    public void deleteNetworkInstanceGroup(InstanceGroup instanceGroup) {
        AAIResourceUri instanceGroupURI =
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
        injectionHelper.getAaiClient().delete(instanceGroupURI);
    }

    public void createNetworkPolicy(org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy networkPolicy) {
        NetworkPolicy aaiNetworkPolicy = aaiObjectMapper.mapNetworkPolicy(networkPolicy);
        String networkPolicyId = networkPolicy.getNetworkPolicyId();
        AAIResourceUri netUri = AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_POLICY, networkPolicyId);
        injectionHelper.getAaiClient().create(netUri, aaiNetworkPolicy);
    }

    public void deleteNetworkPolicy(String networkPolicyId) {
        AAIResourceUri networkPolicyURI =
                AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_POLICY, networkPolicyId);
        injectionHelper.getAaiClient().delete(networkPolicyURI);
    }

    public boolean checkNetworkNameInUse(String networkName) {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectPlurals.L3_NETWORK).queryParam("network-name", networkName);
        return injectionHelper.getAaiClient().exists(uri);
    }

}
