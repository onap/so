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

package org.onap.so.client.aai.mapper;

import java.util.List;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.onap.aai.domain.yang.RouteTargets;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CtagAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget;
import org.onap.so.bpmn.servicedecomposition.bbobjects.SegmentationAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.springframework.stereotype.Component;

@Component
public class AAIObjectMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public org.onap.aai.domain.yang.ServiceInstance mapServiceInstance(ServiceInstance serviceInstance) {
        if (modelMapper.getTypeMap(ServiceInstance.class, org.onap.aai.domain.yang.ServiceInstance.class) == null) {
            modelMapper.addMappings(new PropertyMap<ServiceInstance, org.onap.aai.domain.yang.ServiceInstance>() {
                @Override
                protected void configure() {
                    map().setServiceType(source.getModelInfoServiceInstance().getServiceType());
                    map().setServiceRole(source.getModelInfoServiceInstance().getServiceRole());
                    map().setServiceFunction(source.getModelInfoServiceInstance().getServiceFunction());
                    map().setModelInvariantId(source.getModelInfoServiceInstance().getModelInvariantUuid());
                    map().setModelVersionId(source.getModelInfoServiceInstance().getModelUuid());
                    map().setEnvironmentContext(source.getModelInfoServiceInstance().getEnvironmentContext());
                    map().setWorkloadContext(source.getModelInfoServiceInstance().getWorkloadContext());
                }
            });
        }

        return modelMapper.map(serviceInstance, org.onap.aai.domain.yang.ServiceInstance.class);
    }

    public org.onap.aai.domain.yang.Project mapProject(Project project) {
        return modelMapper.map(project, org.onap.aai.domain.yang.Project.class);
    }

    public org.onap.aai.domain.yang.ServiceSubscription mapServiceSubscription(
            ServiceSubscription serviceSubscription) {
        return modelMapper.map(serviceSubscription, org.onap.aai.domain.yang.ServiceSubscription.class);
    }

    public org.onap.aai.domain.yang.OwningEntity mapOwningEntity(OwningEntity owningEntity) {
        return modelMapper.map(owningEntity, org.onap.aai.domain.yang.OwningEntity.class);
    }

    public org.onap.aai.domain.yang.GenericVnf mapVnf(GenericVnf vnf) {
        if (modelMapper.getTypeMap(GenericVnf.class, org.onap.aai.domain.yang.GenericVnf.class) == null) {
            modelMapper.addMappings(new PropertyMap<GenericVnf, org.onap.aai.domain.yang.GenericVnf>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoGenericVnf().getModelCustomizationUuid());
                    map().setModelInvariantId(source.getModelInfoGenericVnf().getModelInvariantUuid());
                    map().setModelVersionId(source.getModelInfoGenericVnf().getModelUuid());
                    map().setNfRole(source.getModelInfoGenericVnf().getNfRole());
                    map().setNfType(source.getModelInfoGenericVnf().getNfType());
                    map().setNfFunction(source.getModelInfoGenericVnf().getNfFunction());
                    map().setNfNamingCode(source.getModelInfoGenericVnf().getNfNamingCode());
                }
            });
        }

        return modelMapper.map(vnf, org.onap.aai.domain.yang.GenericVnf.class);
    }

    public org.onap.aai.domain.yang.Pnf mapPnf(Pnf pnf) {
        if (modelMapper.getTypeMap(Pnf.class, org.onap.aai.domain.yang.Pnf.class) == null) {
            modelMapper.addMappings(new PropertyMap<Pnf, org.onap.aai.domain.yang.Pnf>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoPnf().getModelCustomizationUuid());
                    map().setModelInvariantId(source.getModelInfoPnf().getModelInvariantUuid());
                    map().setModelVersionId(source.getModelInfoPnf().getModelUuid());
                    map().setNfType(source.getModelInfoPnf().getNfType());
                    map().setInMaint(source.isInMaint());
                }
            });
        }

        return modelMapper.map(pnf, org.onap.aai.domain.yang.Pnf.class);
    }

    public org.onap.aai.domain.yang.VfModule mapVfModule(VfModule vfModule) {
        if (modelMapper.getTypeMap(VfModule.class, org.onap.aai.domain.yang.VfModule.class) == null) {
            modelMapper.addMappings(new PropertyMap<VfModule, org.onap.aai.domain.yang.VfModule>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoVfModule().getModelCustomizationUUID());
                    map().setModelInvariantId(source.getModelInfoVfModule().getModelInvariantUUID());
                    map().setModelVersionId(source.getModelInfoVfModule().getModelUUID());
                    map().setPersonaModelVersion(source.getModelInfoVfModule().getModelInvariantUUID());
                    map().setIsBaseVfModule(source.getModelInfoVfModule().getIsBaseBoolean());

                }
            });
        }

        return modelMapper.map(vfModule, org.onap.aai.domain.yang.VfModule.class);
    }

    public org.onap.aai.domain.yang.VolumeGroup mapVolumeGroup(VolumeGroup volumeGroup) {
        if (modelMapper.getTypeMap(VolumeGroup.class, org.onap.aai.domain.yang.VolumeGroup.class) == null) {
            modelMapper.addMappings(new PropertyMap<VolumeGroup, org.onap.aai.domain.yang.VolumeGroup>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoVfModule().getModelCustomizationUUID());
                    map().setVfModuleModelCustomizationId(source.getModelInfoVfModule().getModelCustomizationUUID());
                }
            });
        }
        return modelMapper.map(volumeGroup, org.onap.aai.domain.yang.VolumeGroup.class);
    }

    public org.onap.aai.domain.yang.L3Network mapNetwork(L3Network l3Network) {
        if (modelMapper.getTypeMap(L3Network.class, org.onap.aai.domain.yang.L3Network.class) == null) {
            modelMapper.addMappings(new PropertyMap<L3Network, org.onap.aai.domain.yang.L3Network>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoNetwork().getModelCustomizationUUID());
                    map().setModelInvariantId(source.getModelInfoNetwork().getModelInvariantUUID());
                    map().setModelVersionId(source.getModelInfoNetwork().getModelUUID());
                    map().setNetworkType(source.getModelInfoNetwork().getNetworkType());
                    map().setNetworkRole(source.getModelInfoNetwork().getNetworkRole());
                    map().setNetworkTechnology(source.getModelInfoNetwork().getNetworkTechnology());
                    modelMapper.addConverter(convertSubnets);
                    modelMapper.addConverter(convertCtagAssignments);
                    modelMapper.addConverter(convertSegmentationAssignments);
                }
            });
        }
        return modelMapper.map(l3Network, org.onap.aai.domain.yang.L3Network.class);
    }

    public org.onap.aai.domain.yang.InstanceGroup mapInstanceGroup(InstanceGroup instanceGroup) {
        if (modelMapper.getTypeMap(InstanceGroup.class, org.onap.aai.domain.yang.InstanceGroup.class) == null) {
            modelMapper.addMappings(new PropertyMap<InstanceGroup, org.onap.aai.domain.yang.InstanceGroup>() {
                @Override
                protected void configure() {
                    map().setInstanceGroupRole(source.getModelInfoInstanceGroup().getInstanceGroupRole());
                    map().setModelInvariantId(source.getModelInfoInstanceGroup().getModelInvariantUUID());
                    map().setModelVersionId(source.getModelInfoInstanceGroup().getModelUUID());
                    map().setInstanceGroupType(source.getModelInfoInstanceGroup().getType());
                    map().setDescription(source.getModelInfoInstanceGroup().getDescription());
                    map().setInstanceGroupFunction(source.getModelInfoInstanceGroup().getFunction());
                }
            });
        }
        return modelMapper.map(instanceGroup, org.onap.aai.domain.yang.InstanceGroup.class);
    }

    public org.onap.aai.domain.yang.Customer mapCustomer(Customer customer) {
        return modelMapper.map(customer, org.onap.aai.domain.yang.Customer.class);
    }

    private Converter<List<Subnet>, org.onap.aai.domain.yang.Subnets> convertSubnets =
            new Converter<List<Subnet>, org.onap.aai.domain.yang.Subnets>() {
                @Override
                public org.onap.aai.domain.yang.Subnets convert(
                        MappingContext<List<Subnet>, org.onap.aai.domain.yang.Subnets> context) {
                    return mapToAAISubNets(context.getSource());
                }
            };

    private Converter<List<CtagAssignment>, org.onap.aai.domain.yang.CtagAssignments> convertCtagAssignments =
            new Converter<List<CtagAssignment>, org.onap.aai.domain.yang.CtagAssignments>() {
                @Override
                public org.onap.aai.domain.yang.CtagAssignments convert(
                        MappingContext<List<CtagAssignment>, org.onap.aai.domain.yang.CtagAssignments> context) {
                    return mapToAAICtagAssignmentList(context.getSource());
                }
            };

    private Converter<List<SegmentationAssignment>, org.onap.aai.domain.yang.SegmentationAssignments> convertSegmentationAssignments =
            new Converter<List<SegmentationAssignment>, org.onap.aai.domain.yang.SegmentationAssignments>() {
                @Override
                public org.onap.aai.domain.yang.SegmentationAssignments convert(
                        MappingContext<List<SegmentationAssignment>, org.onap.aai.domain.yang.SegmentationAssignments> context) {
                    return mapToAAISegmentationAssignmentList(context.getSource());
                }
            };

    public org.onap.aai.domain.yang.Subnets mapToAAISubNets(List<Subnet> subnetList) {
        org.onap.aai.domain.yang.Subnets subnets = null;

        if (!subnetList.isEmpty()) {
            subnets = new org.onap.aai.domain.yang.Subnets();
            org.onap.aai.domain.yang.Subnet subnet = null;
            for (Subnet subnetSource : subnetList) {
                subnet = new org.onap.aai.domain.yang.Subnet();
                subnet.setSubnetId(subnetSource.getSubnetId());
                subnet.setSubnetName(subnetSource.getSubnetName());
                subnet.setNeutronSubnetId(subnetSource.getNeutronSubnetId());
                subnet.setGatewayAddress(subnetSource.getGatewayAddress());
                subnet.setCidrMask(subnetSource.getCidrMask());
                subnet.setIpVersion(subnetSource.getIpVersion());
                subnet.setOrchestrationStatus(subnetSource.getOrchestrationStatus().toString());
                subnet.setCidrMask(subnetSource.getCidrMask());
                subnet.setDhcpEnabled(subnetSource.isDhcpEnabled());
                subnet.setDhcpStart(subnetSource.getDhcpStart());
                subnet.setDhcpEnd(subnetSource.getDhcpEnd());
                subnet.setSubnetRole(subnetSource.getSubnetRole());
                subnet.setIpAssignmentDirection(subnetSource.getIpAssignmentDirection());
                subnet.setSubnetSequence(subnetSource.getSubnetSequence());

                org.onap.aai.domain.yang.HostRoutes hostRoutes = new org.onap.aai.domain.yang.HostRoutes();
                org.onap.aai.domain.yang.HostRoute hostRoute = null;
                for (HostRoute hostRouteSource : subnetSource.getHostRoutes()) {
                    hostRoute = new org.onap.aai.domain.yang.HostRoute();
                    hostRoute.setHostRouteId(hostRouteSource.getHostRouteId());
                    hostRoute.setRoutePrefix(hostRouteSource.getRoutePrefix());
                    hostRoute.setNextHop(hostRouteSource.getNextHop());
                    hostRoute.setNextHopType(hostRouteSource.getNextHopType());
                    hostRoutes.getHostRoute().add(hostRoute);
                }
                subnet.setHostRoutes(hostRoutes);
                subnets.getSubnet().add(subnet);
            }
        }
        return subnets;
    }

    public org.onap.aai.domain.yang.CtagAssignments mapToAAICtagAssignmentList(
            List<CtagAssignment> ctagAssignmentsList) {
        org.onap.aai.domain.yang.CtagAssignments ctagAssignments = null;
        if (!ctagAssignmentsList.isEmpty()) {
            ctagAssignments = new org.onap.aai.domain.yang.CtagAssignments();

            org.onap.aai.domain.yang.CtagAssignment ctagAssignment = null;
            for (CtagAssignment ctagAssignmentSource : ctagAssignmentsList) {
                ctagAssignment = new org.onap.aai.domain.yang.CtagAssignment();
                ctagAssignment.setVlanIdInner(ctagAssignmentSource.getVlanIdInner());
                ctagAssignments.getCtagAssignment().add(ctagAssignment);
            }
        }
        return ctagAssignments;
    }

    public org.onap.aai.domain.yang.SegmentationAssignments mapToAAISegmentationAssignmentList(
            List<SegmentationAssignment> segmentationAssignmentList) {
        org.onap.aai.domain.yang.SegmentationAssignments segmentationAssignments = null;
        if (!segmentationAssignmentList.isEmpty()) {
            segmentationAssignments = new org.onap.aai.domain.yang.SegmentationAssignments();
            org.onap.aai.domain.yang.SegmentationAssignment segmentationAssignment = null;
            for (SegmentationAssignment segmentationAssignmentSource : segmentationAssignmentList) {
                segmentationAssignment = new org.onap.aai.domain.yang.SegmentationAssignment();
                segmentationAssignment.setSegmentationId(segmentationAssignmentSource.getSegmentationId());
                segmentationAssignments.getSegmentationAssignment().add(segmentationAssignment);
            }
        }
        return segmentationAssignments;
    }

    public org.onap.aai.domain.yang.Configuration mapConfiguration(Configuration configuration) {
        if (null == modelMapper.getTypeMap(Configuration.class, org.onap.aai.domain.yang.Configuration.class)) {
            modelMapper.addMappings(new PropertyMap<Configuration, org.onap.aai.domain.yang.Configuration>() {
                @Override
                protected void configure() {
                    map().setModelCustomizationId(source.getModelInfoConfiguration().getModelCustomizationId());
                    map().setModelVersionId(source.getModelInfoConfiguration().getModelVersionId());
                    map().setModelInvariantId(source.getModelInfoConfiguration().getModelInvariantId());
                    map().setConfigurationType(source.getModelInfoConfiguration().getConfigurationType());
                    map().setConfigurationSubType(source.getModelInfoConfiguration().getConfigurationRole());
                    map().setConfigPolicyName(source.getModelInfoConfiguration().getPolicyName());
                    skip().setConfigurationRole(null);
                }
            });
        }
        return modelMapper.map(configuration, org.onap.aai.domain.yang.Configuration.class);
    }

    public org.onap.aai.domain.yang.Collection mapCollection(Collection networkCollection) {
        if (modelMapper.getTypeMap(Collection.class, org.onap.aai.domain.yang.Collection.class) == null) {
            modelMapper.addMappings(new PropertyMap<Collection, org.onap.aai.domain.yang.Collection>() {
                @Override
                protected void configure() {
                    map().setModelInvariantId(source.getModelInfoCollection().getModelInvariantUUID());
                    map().setModelVersionId(source.getModelInfoCollection().getModelVersionId());
                    map().setCollectionCustomizationId(source.getModelInfoCollection().getModelCustomizationUUID());
                    map().setCollectionFunction(source.getModelInfoCollection().getCollectionFunction());
                    map().setCollectionRole(source.getModelInfoCollection().getCollectionRole());
                    map().setCollectionType(source.getModelInfoCollection().getCollectionType());
                    map().setCollectionName(source.getName());
                }
            });
        }
        return modelMapper.map(networkCollection, org.onap.aai.domain.yang.Collection.class);
    }


    public org.onap.aai.domain.yang.VpnBinding mapVpnBinding(VpnBinding vpnBinding) {
        org.onap.aai.domain.yang.VpnBinding aaiVpnBinding =
                modelMapper.map(vpnBinding, org.onap.aai.domain.yang.VpnBinding.class);
        mapRouteTargetToVpnBinding(aaiVpnBinding, vpnBinding);
        return aaiVpnBinding;
    }

    public org.onap.aai.domain.yang.RouteTarget mapRouteTarget(RouteTarget routeTarget) {
        return modelMapper.map(routeTarget, org.onap.aai.domain.yang.RouteTarget.class);
    }

    private void mapRouteTargetToVpnBinding(org.onap.aai.domain.yang.VpnBinding aaiVpnBinding, VpnBinding vpnBinding) {
        if (vpnBinding.getRouteTargets() != null && !vpnBinding.getRouteTargets().isEmpty()) {
            RouteTargets routeTargets = new RouteTargets();
            for (RouteTarget routeTarget : vpnBinding.getRouteTargets()) {
                routeTargets.getRouteTarget().add(mapRouteTarget(routeTarget));
            }
            aaiVpnBinding.setRouteTargets(routeTargets);
        }
    }

    public org.onap.aai.domain.yang.Subnet mapSubnet(Subnet subnet) {
        return modelMapper.map(subnet, org.onap.aai.domain.yang.Subnet.class);
    }

    public org.onap.aai.domain.yang.NetworkPolicy mapNetworkPolicy(NetworkPolicy networkPolicy) {
        return modelMapper.map(networkPolicy, org.onap.aai.domain.yang.NetworkPolicy.class);
    }
}
