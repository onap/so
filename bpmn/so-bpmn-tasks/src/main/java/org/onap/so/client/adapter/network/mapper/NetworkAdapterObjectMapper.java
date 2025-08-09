/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.adapter.network.mapper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.onap.so.adapters.nwrest.ContrailNetwork;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.ProviderVlanNetwork;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.openstack.beans.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.SegmentationAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
public class NetworkAdapterObjectMapper {
    private static final ModelMapper modelMapper = new ModelMapper();
    private static String FORWARD_SLASH = "/";
    private static final Logger logger = LoggerFactory.getLogger(NetworkAdapterObjectMapper.class);

    public CreateNetworkRequest createNetworkRequestMapper(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network,
            Map<String, String> userInput, String cloudRegionPo, Customer customer) {
        CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest();

        // set cloudSiteId as determined for cloud region PO instead of cloudRegion.getLcpCloudRegionId()
        createNetworkRequest.setCloudSiteId(cloudRegionPo);
        createNetworkRequest.setTenantId(cloudRegion.getTenantId());
        createNetworkRequest.setNetworkId(l3Network.getNetworkId());
        createNetworkRequest.setNetworkName(l3Network.getNetworkName());
        // TODO fields not available
        createNetworkRequest.setNetworkType(l3Network.getNetworkType());
        // createNetworkRequest.setNetworkTypeVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        ModelInfoNetwork modelInfoNetwork = l3Network.getModelInfoNetwork();
        if (modelInfoNetwork != null) {
            createNetworkRequest.setModelCustomizationUuid(modelInfoNetwork.getModelCustomizationUUID());
        }

        // build and set Subnet list
        createNetworkRequest.setSubnets(buildOpenstackSubnetList(l3Network));

        // build and set provider Vlan Network
        ProviderVlanNetwork providerVlanNetwork = buildProviderVlanNetwork(l3Network);
        createNetworkRequest.setProviderVlanNetwork(providerVlanNetwork);
        String networkTechnology = l3Network.getModelInfoNetwork().getNetworkTechnology();
        if (networkTechnology == null) {
            networkTechnology = l3Network.getNetworkTechnology();
            logger.warn("NetworkTechnology was null in CatalogDB. Using field from AAI: {}", networkTechnology);
        }
        if (networkTechnology != null) {
            createNetworkRequest.setNetworkTechnology(networkTechnology.toUpperCase());
            if (createNetworkRequest.getNetworkTechnology().contains("CONTRAIL")) {
                createNetworkRequest.setContrailRequest(true);
            }
        }

        // build and set Contrail Network
        ContrailNetwork contrailNetwork = buildContrailNetwork(l3Network, customer);
        createNetworkRequest.setContrailNetwork(contrailNetwork);

        // set Network Parameters from VID request, add "shared" and "external" to this map
        createNetworkRequest.setNetworkParams(addSharedAndExternal(userInput, l3Network));

        createNetworkRequest = setFlowFlags(createNetworkRequest, orchestrationContext);

        createNetworkRequest.setMsoRequest(createMsoRequest(requestContext, serviceInstance));

        String messageId = getRandomUuid();
        createNetworkRequest.setMessageId(messageId);
        createNetworkRequest.setNotificationUrl(createCallbackUrl("NetworkAResponse", messageId));

        return createNetworkRequest;
    }

    public DeleteNetworkRequest deleteNetworkRequestMapper(RequestContext requestContext, CloudRegion cloudRegion,
            ServiceInstance serviceInstance, L3Network l3Network) {
        DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();

        deleteNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());

        String messageId = getRandomUuid();
        deleteNetworkRequest.setMessageId(messageId);

        ModelInfoNetwork modelInfoNetwork = l3Network.getModelInfoNetwork();
        if (modelInfoNetwork != null) {
            deleteNetworkRequest.setModelCustomizationUuid(modelInfoNetwork.getModelCustomizationUUID());
        }

        deleteNetworkRequest.setMsoRequest(createMsoRequest(requestContext, serviceInstance));
        deleteNetworkRequest.setNetworkId(l3Network.getNetworkId());
        if (!StringUtils.isEmpty(l3Network.getHeatStackId())) {
            deleteNetworkRequest.setNetworkStackId(l3Network.getHeatStackId());
        } else {
            deleteNetworkRequest.setNetworkStackId(l3Network.getNetworkName());
        }
        deleteNetworkRequest.setNetworkType(l3Network.getNetworkType());
        deleteNetworkRequest.setSkipAAI(true);
        deleteNetworkRequest.setTenantId(cloudRegion.getTenantId());

        deleteNetworkRequest.setNotificationUrl(createCallbackUrl("NetworkAResponse", messageId));

        return deleteNetworkRequest;
    }

    /**
     * Access method to build Rollback Network Request
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public RollbackNetworkRequest createNetworkRollbackRequestMapper(RequestContext requestContext,
            CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance,
            L3Network l3Network, Map<String, String> userInput, String cloudRegionPo,
            CreateNetworkResponse createNetworkResponse) {
        RollbackNetworkRequest rollbackNetworkRequest = new RollbackNetworkRequest();

        rollbackNetworkRequest = setCommonRollbackRequestFields(rollbackNetworkRequest, requestContext);

        NetworkRollback networkRollback =
                buildNetworkRollback(l3Network, cloudRegion, cloudRegionPo, createNetworkResponse);
        rollbackNetworkRequest.setNetworkRollback(networkRollback);

        return rollbackNetworkRequest;
    }

    public UpdateNetworkRequest createNetworkUpdateRequestMapper(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network,
            Map<String, String> userInput, Customer customer) {
        UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();

        updateNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        updateNetworkRequest.setTenantId(cloudRegion.getTenantId());
        updateNetworkRequest.setNetworkId(l3Network.getNetworkId());
        updateNetworkRequest.setNetworkStackId(l3Network.getHeatStackId());
        updateNetworkRequest.setNetworkName(l3Network.getNetworkName());
        updateNetworkRequest.setNetworkType(l3Network.getModelInfoNetwork().getNetworkType());
        updateNetworkRequest.setNetworkTypeVersion(l3Network.getModelInfoNetwork().getModelVersion());
        updateNetworkRequest.setModelCustomizationUuid(l3Network.getModelInfoNetwork().getModelCustomizationUUID());
        updateNetworkRequest.setSubnets(buildOpenstackSubnetList(l3Network));
        updateNetworkRequest.setProviderVlanNetwork(buildProviderVlanNetwork(l3Network));
        updateNetworkRequest.setContrailNetwork(buildContrailNetwork(l3Network, customer));
        updateNetworkRequest.setNetworkParams(addSharedAndExternal(userInput, l3Network));
        updateNetworkRequest.setMsoRequest(createMsoRequest(requestContext, serviceInstance));

        setFlowFlags(updateNetworkRequest, orchestrationContext);

        String messageId = getRandomUuid();
        updateNetworkRequest.setMessageId(messageId);
        updateNetworkRequest.setNotificationUrl(createCallbackUrl("NetworkAResponse", messageId));

        return updateNetworkRequest;
    }

    private RollbackNetworkRequest setCommonRollbackRequestFields(RollbackNetworkRequest request,
            RequestContext requestContext) {
        request.setSkipAAI(true);
        String messageId = requestContext.getMsoRequestId();
        request.setMessageId(messageId);
        return request;
    }

    private NetworkRollback buildNetworkRollback(L3Network l3Network, CloudRegion cloudRegion, String cloudRegionPo,
            CreateNetworkResponse createNetworkResponse) {
        NetworkRollback networkRollback = new NetworkRollback();
        networkRollback.setNetworkId(l3Network.getNetworkId());
        networkRollback.setNeutronNetworkId(createNetworkResponse.getMessageId());
        networkRollback.setNetworkStackId(createNetworkResponse.getNetworkStackId());
        networkRollback.setTenantId(cloudRegion.getTenantId());
        networkRollback.setCloudId(cloudRegionPo);
        networkRollback.setNetworkType(l3Network.getNetworkType());
        ModelInfoNetwork modelInfoNetwork = l3Network.getModelInfoNetwork();
        if (modelInfoNetwork != null) {
            networkRollback.setModelCustomizationUuid(modelInfoNetwork.getModelCustomizationUUID());
        }
        // rollback will only be called when network was actually created
        networkRollback.setNetworkCreated(createNetworkResponse.getNetworkCreated());
        // TODO confirm below not required for create rollback
        // NetworkName
        // PhysicalNetwork
        // Vlans
        // msoRequest
        return networkRollback;
    }

    public MsoRequest createMsoRequest(RequestContext requestContext, ServiceInstance serviceInstance) {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        return msoRequest;
    }

    protected String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

    protected String createCallbackUrl(String messageType, String correlator) {
        String endpoint = this.getEndpoint();

        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + UriUtils.encodePathSegment(messageType, "UTF-8") + "/"
                + UriUtils.encodePathSegment(correlator, "UTF-8");
    }

    protected String getEndpoint() {
        return UrnPropertiesReader.getVariable("mso.workflow.message.endpoint");
    }

    /**
     * Use BB L3Network object to build subnets list of type org.onap.so.openstack.beans.Subnet
     *
     * @param L3Network
     * @return List<org.onap.so.openstack.beans.Subnet>
     */
    protected List<Subnet> buildOpenstackSubnetList(L3Network l3Network) {

        List<org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet> subnets = l3Network.getSubnets();
        List<org.onap.so.openstack.beans.Subnet> subnetList = new ArrayList<>();
        // create mapper from onap Subnet to openstack bean Subnet
        if (modelMapper.getTypeMap(org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet.class,
                org.onap.so.openstack.beans.Subnet.class) == null) {
            PropertyMap<org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet, org.onap.so.openstack.beans.Subnet> personMap =
                    new PropertyMap<org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet, org.onap.so.openstack.beans.Subnet>() {
                        @Override
                        protected void configure() {
                            map().setSubnetName(source.getSubnetName());
                            map(source.getSubnetId(), destination.getSubnetId());
                            map(source.getNeutronSubnetId(), destination.getNeutronId());
                            map(source.getGatewayAddress(), destination.getGatewayIp());
                            map(source.getIpVersion(), destination.getIpVersion());
                            map(source.isDhcpEnabled(), destination.getEnableDHCP());
                            map(source.getSubnetSequence(), destination.getSubnetSequence());
                        }
                    };
            modelMapper.addMappings(personMap);
        }

        for (org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet subnet : subnets) {
            org.onap.so.openstack.beans.Subnet openstackSubnet =
                    modelMapper.map(subnet, org.onap.so.openstack.beans.Subnet.class);
            if (StringUtils.isEmpty(openstackSubnet.getGatewayIp())) {
                openstackSubnet.setGatewayIp("NULL");
            }
            // update cidr value
            if (subnet.getNetworkStartAddress() != null && subnet.getCidrMask() != null)
                openstackSubnet
                        .setCidr(subnet.getNetworkStartAddress().concat(FORWARD_SLASH).concat(subnet.getCidrMask()));
            List<org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute> hostRouteList = subnet.getHostRoutes();
            List<org.onap.so.openstack.beans.HostRoute> openstackHostRouteList = new ArrayList<>();
            org.onap.so.openstack.beans.HostRoute openstackHostRoute;
            // TODO only 2 fields available on openstack object. Confirm it is sufficient or add as needed
            for (org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute hostRoute : hostRouteList) {
                openstackHostRoute = new org.onap.so.openstack.beans.HostRoute();
                openstackHostRoute.setNextHop(hostRoute.getNextHop());
                openstackHostRoute.setPrefix(hostRoute.getRoutePrefix());
                // add host route to the list
                openstackHostRouteList.add(openstackHostRoute);
            }
            if (subnet.getDhcpStart() != null && !subnet.getDhcpStart().equals("")) {
                org.onap.so.openstack.beans.Pool openstackAllocationPool = new org.onap.so.openstack.beans.Pool();
                openstackAllocationPool.setStart(subnet.getDhcpStart());
                openstackAllocationPool.setEnd(subnet.getDhcpEnd());
                List<org.onap.so.openstack.beans.Pool> allocationPools = new ArrayList<>();
                allocationPools.add(openstackAllocationPool);
                openstackSubnet.setAllocationPools(allocationPools);
            }
            openstackSubnet.setHostRoutes(openstackHostRouteList);
            // add subnet to the list
            subnetList.add(openstackSubnet);
        }
        return subnetList;
    }

    private ProviderVlanNetwork buildProviderVlanNetwork(L3Network l3Network) {
        ProviderVlanNetwork providerVlanNetwork = new ProviderVlanNetwork();
        providerVlanNetwork.setPhysicalNetworkName(l3Network.getPhysicalNetworkName());
        List<Integer> vlans = new ArrayList<>();
        List<SegmentationAssignment> segmentationAssignments = l3Network.getSegmentationAssignments();
        for (SegmentationAssignment assignment : segmentationAssignments) {
            vlans.add(Integer.valueOf(assignment.getSegmentationId()));
        }
        providerVlanNetwork.setVlans(vlans);
        return providerVlanNetwork;
    }

    private ContrailNetwork buildContrailNetwork(L3Network l3Network, Customer customer) {
        ContrailNetwork contrailNetwork = new ContrailNetwork();
        contrailNetwork.setExternal(Optional.ofNullable(l3Network.isIsExternalNetwork()).orElse(false).toString());
        contrailNetwork.setShared(Optional.ofNullable(l3Network.isIsSharedNetwork()).orElse(false).toString());
        contrailNetwork.setPolicyFqdns(buildPolicyFqdns(l3Network.getNetworkPolicies()));
        contrailNetwork.setRouteTableFqdns(buildRouteTableFqdns(l3Network.getContrailNetworkRouteTableReferences()));
        if (customer != null)
            contrailNetwork.setRouteTargets(buildRouteTargets(customer.getVpnBindings()));
        // PolicyFqdns(policyFqdns); --- is set in getAAINetworkPolicy
        // RouteTableFqdns(routeTableFqdns); --- is set in getAAINetworkTableRef
        // RouteTargets(routeTargets); --- is set in getAAINetworkVpnBinding
        return contrailNetwork;
    }

    private List<String> buildPolicyFqdns(List<NetworkPolicy> networkPolicies) {
        List<String> policyFqdns = new ArrayList<>();
        for (NetworkPolicy networkPolicy : networkPolicies) {
            policyFqdns.add(networkPolicy.getNetworkPolicyFqdn());
        }
        return policyFqdns;
    }

    private List<String> buildRouteTableFqdns(List<RouteTableReference> contrailNetworkRouteTableReferences) {
        List<String> routeTableFqdns = new ArrayList<>();
        for (RouteTableReference routeTableReference : contrailNetworkRouteTableReferences) {
            routeTableFqdns.add(routeTableReference.getRouteTableReferenceFqdn());
        }
        return routeTableFqdns;
    }

    private List<RouteTarget> buildRouteTargets(List<VpnBinding> vpnBindings) {
        if (modelMapper.getTypeMap(org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget.class,
                RouteTarget.class) == null) {
            modelMapper.addMappings(
                    new PropertyMap<org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget, RouteTarget>() {
                        @Override
                        protected void configure() {
                            map().setRouteTarget(source.getGlobalRouteTarget());
                            map().setRouteTargetRole(source.getRouteTargetRole());
                        }
                    });
        }

        List<RouteTarget> routeTargets = new ArrayList<>();
        for (VpnBinding vpnBinding : vpnBindings) {
            for (org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget routeTarget : vpnBinding
                    .getRouteTargets()) {
                routeTargets.add(modelMapper.map(routeTarget, RouteTarget.class));
            }
        }
        return routeTargets;
    }

    private CreateNetworkRequest setFlowFlags(CreateNetworkRequest createNetworkRequest,
            OrchestrationContext orchestrationContext) {
        // TODO confirm flag value
        createNetworkRequest.setSkipAAI(true);
        createNetworkRequest.setBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        // TODO confirm value - false by default
        createNetworkRequest.setFailIfExists(false);
        // NetworkTechnology(NetworkTechnology.NEUTRON); NOOP - default
        return createNetworkRequest;
    }

    private void setFlowFlags(UpdateNetworkRequest updateNetworkRequest, OrchestrationContext orchestrationContext) {
        updateNetworkRequest.setSkipAAI(true);
        updateNetworkRequest.setBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        // NetworkTechnology(NetworkTechnology.NEUTRON); NOOP - default
    }

    private Map<String, String> addSharedAndExternal(Map<String, String> userInput, L3Network l3Network) {
        if (userInput == null)
            userInput = new HashMap<>();
        if (!userInput.containsKey("shared")) {
            userInput.put("shared", Optional.ofNullable(l3Network.isIsSharedNetwork()).orElse(false).toString());
        }
        if (!userInput.containsKey("external")) {
            userInput.put("external", Optional.ofNullable(l3Network.isIsExternalNetwork()).orElse(false).toString());
        }
        return userInput;
    }
}
