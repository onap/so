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

package org.onap.so.bpmn.infrastructure.aai.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.RouteTargets;
import org.onap.aai.domain.yang.Subnet;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIQueryTasks {

    private static final Logger logger = LoggerFactory.getLogger(AAIQueryTasks.class);
    private static final String ERROR_MSG = "No relationships were returned from AAIResultWrapper.getRelationships()";
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private AAINetworkResources aaiNetworkResources;
    private static final ModelMapper modelMapper = new ModelMapper();

    /**
     * BPMN access method to query data for VPN bindings from the AAI result wrapper. The resulting VPN bindings are
     * mapped to the corresponding bbobject and placed in the customer bbobject
     * 
     * @param execution
     */

    public void queryNetworkVpnBinding(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            Optional<Relationships> networkRelationships = aaiResultWrapper.getRelationships();
            if (!networkRelationships.isPresent()) {
                throw (new Exception(ERROR_MSG));
            }
            List<AAIResourceUri> netBindingsUriList =
                    networkRelationships.get().getRelatedAAIUris(AAIObjectType.VPN_BINDING);

            List<org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding> mappedVpnBindings = new ArrayList<>();
            if (netBindingsUriList != null && !netBindingsUriList.isEmpty()) {
                for (AAIResourceUri netBindingUri : netBindingsUriList) {
                    Optional<VpnBinding> oVpnBinding = aaiNetworkResources.getVpnBinding(netBindingUri);
                    if (oVpnBinding.isPresent()) {
                        org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding mappedVpnBinding = modelMapper.map(
                                oVpnBinding.get(), org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding.class);
                        if (oVpnBinding.get().getRouteTargets() != null) {
                            mappedVpnBinding.getRouteTargets()
                                    .addAll(mapRouteTargets(oVpnBinding.get().getRouteTargets().getRouteTarget()));
                            mappedVpnBindings.add(mappedVpnBinding);
                        }
                    }
                }
            }
            execution.getGeneralBuildingBlock().getCustomer().getVpnBindings().addAll(mappedVpnBindings);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to extract VPN Binding data from AAI result and populate proper fields into
     * CreateNetworkRequest
     */
    public void getNetworkVpnBinding(BuildingBlockExecution execution) {

        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            CreateNetworkRequest createNetworkRequest = execution.getVariable("createNetworkRequest");

            Optional<Relationships> networkRelationships = aaiResultWrapper.getRelationships();
            if (!networkRelationships.isPresent()) {
                throw (new Exception(ERROR_MSG));
            }
            List<AAIResourceUri> netBindingsUriList =
                    networkRelationships.get().getRelatedAAIUris(AAIObjectType.VPN_BINDING);
            List<org.onap.so.openstack.beans.RouteTarget> routeTargets = new ArrayList<>();
            for (AAIResourceUri netBindingUri : netBindingsUriList) {
                logger.info("Get Route Targests");
                Optional<VpnBinding> oVpnBinding = aaiNetworkResources.getVpnBinding(netBindingUri);
                if (oVpnBinding.isPresent()) {
                    VpnBinding vpnBinding = oVpnBinding.get();
                    RouteTargets rts = vpnBinding.getRouteTargets();
                    if (rts != null) {
                        List<org.onap.aai.domain.yang.RouteTarget> rtList = rts.getRouteTarget();
                        if (!rtList.isEmpty()) {
                            PropertyMap<org.onap.aai.domain.yang.RouteTarget, org.onap.so.openstack.beans.RouteTarget> personMap =
                                    new PropertyMap<org.onap.aai.domain.yang.RouteTarget, org.onap.so.openstack.beans.RouteTarget>() {
                                        @Override
                                        protected void configure() {
                                            map().setRouteTarget(source.getGlobalRouteTarget());
                                            map(source.getRouteTargetRole(), destination.getRouteTargetRole());
                                        }
                                    };
                            modelMapper.addMappings(personMap);
                            for (org.onap.aai.domain.yang.RouteTarget rt : rtList) {
                                org.onap.so.openstack.beans.RouteTarget openstackRtBean =
                                        modelMapper.map(rt, org.onap.so.openstack.beans.RouteTarget.class);
                                routeTargets.add(openstackRtBean);
                            }
                        }
                    }
                }
            }
            // store route targets data in execution - to be used as part of
            // Network adapter input
            createNetworkRequest.getContrailNetwork().setRouteTargets(routeTargets);
            execution.setVariable("createNetworkRequest", createNetworkRequest);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to query data for network policies from the AAI result wrapper From the resulting network
     * policy, the network policy fqdn parameter is added to the network bbobject contrail network policy fqdns list
     * 
     * @param execution
     */
    public void queryNetworkPolicy(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            Optional<Relationships> networkRelationships = aaiResultWrapper.getRelationships();
            if (!networkRelationships.isPresent()) {
                throw (new Exception(ERROR_MSG));
            }
            List<AAIResourceUri> netPoliciesUriList =
                    networkRelationships.get().getRelatedAAIUris(AAIObjectType.NETWORK_POLICY);

            if (!netPoliciesUriList.isEmpty()) {
                for (AAIResourceUri netPolicyUri : netPoliciesUriList) {
                    Optional<NetworkPolicy> oNetPolicy = aaiNetworkResources.getNetworkPolicy(netPolicyUri);
                    if (oNetPolicy.isPresent()) {
                        l3network.getNetworkPolicies().add(modelMapper.map(oNetPolicy.get(),
                                org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy.class));
                    }
                }
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to query data for network table ref from the AAI result wrapper The resulting route table
     * reference is mapped to the corresponding bbobject and added to the network bbobject contrail network route table
     * references list
     * 
     * @param execution
     */
    public void queryNetworkTableRef(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            Optional<Relationships> networkRelationships = aaiResultWrapper.getRelationships();
            if (!networkRelationships.isPresent()) {
                throw (new Exception(ERROR_MSG));
            }
            List<AAIResourceUri> routeTableUriList =
                    networkRelationships.get().getRelatedAAIUris(AAIObjectType.ROUTE_TABLE_REFERENCE);

            if (!routeTableUriList.isEmpty()) {
                for (AAIResourceUri routeTableUri : routeTableUriList) {
                    Optional<RouteTableReference> oRouteTableReference =
                            aaiNetworkResources.getRouteTable(routeTableUri);
                    if (oRouteTableReference.isPresent()) {
                        org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference mappedRouteTableReference =
                                modelMapper.map(oRouteTableReference.get(),
                                        org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference.class);
                        l3network.getContrailNetworkRouteTableReferences().add(mappedRouteTableReference);
                    }
                }
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    private List<org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget> mapRouteTargets(
            List<org.onap.aai.domain.yang.RouteTarget> routeTargets) {
        List<org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget> mappedRouteTargets = new ArrayList<>();
        if (!routeTargets.isEmpty()) {
            for (org.onap.aai.domain.yang.RouteTarget routeTarget : routeTargets) {
                org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget mappedRouteTarget =
                        modelMapper.map(routeTarget, org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget.class);
                mappedRouteTargets.add(mappedRouteTarget);
            }
        }
        return mappedRouteTargets;
    }

    public void querySubnet(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            Optional<Relationships> networkRelationships = aaiResultWrapper.getRelationships();
            if (!networkRelationships.isPresent()) {
                throw (new Exception(ERROR_MSG));
            }
            List<AAIResourceUri> subnetsUriList = networkRelationships.get().getRelatedAAIUris(AAIObjectType.SUBNET);

            if (!subnetsUriList.isEmpty()) {
                for (AAIResourceUri subnetUri : subnetsUriList) {
                    Optional<Subnet> oSubnet = aaiNetworkResources.getSubnet(subnetUri);
                    if (oSubnet.isPresent()) {
                        l3network.getSubnets().add(modelMapper.map(oSubnet.get(),
                                org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet.class));
                    }
                }
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
