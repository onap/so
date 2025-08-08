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

package org.onap.so.client.adapter.vnf.mapper;

import static java.util.Arrays.asList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSubInterfaceNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSubinterfacenetworkdataSubInterfaceNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleTopology;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduleassignmentsVfModuleAssignments;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduleassignmentsVfmoduleassignmentsVms;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduletopologyVfModuleTopology;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmTopologyData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataFloatingIps;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataInterfaceRoutePrefixes;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataNetworkInformationItems;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataNetworkinformationitemsNetworkInformationItem;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataNetworkinformationitemsNetworkinformationitemNetworkIps;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataSriovParameters;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmnetworkdataSriovparametersHeatVlanFilters;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmtopologydataVmNames;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmtopologydataVmNetworks;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmtopologydataVmnamesVnfcNames;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfTopology;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfcNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfcnetworkdataVnfcNetworkData;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfcnetworkdataVnfcnetworkdataVnfcPorts;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfcnetworkdataVnfcnetworkdataVnfcportsVnfcPort;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfresourceassignmentsVnfResourceAssignments;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfresourceassignmentsVnfresourceassignmentsAvailabilityZones;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfresourceassignmentsVnfresourceassignmentsVnfNetworks;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnftopologyVnfTopology;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.vnf.mapper.exceptions.MissingValueTagException;
import org.onap.so.entity.MsoRequest;
import org.onap.so.jsonpath.JsonPathUtil;
import org.onap.so.openstack.utils.MsoMulticloudUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;


@Component
public class VnfAdapterVfModuleObjectMapper {
    @Autowired
    protected VnfAdapterObjectMapperUtils vnfAdapterObjectMapperUtils;

    private static final Logger logger = LoggerFactory.getLogger(VnfAdapterVfModuleObjectMapper.class);
    private static List<String> sdncResponseParamsToSkip =
            asList("vnf_id", "vf_module_id", "vnf_name", "vf_module_name");

    private ObjectMapper mapper = new ObjectMapper();
    private static final JsonPathUtil jsonPath = JsonPathUtil.getInstance();
    private static final String SUB_INT = "subint";
    private static final String SUBNET_ID = "_subnet_id";
    private static final String V6_SUBNET_ID = "_v6_subnet_id";
    private static final String PORT = "port";
    private static final String SUB_INT_COUNT = "_subintcount";
    private static final String VLAN_IDS = "_vlan_ids";
    private static final String NET_NAMES = "_net_names";
    private static final String NET_IDS = "_net_ids";
    private static final String IP = "_ip";
    private static final String V6_IP = "_v6_ip";
    private static final String FLOATING_IP = "_floating_ip";
    private static final String FLOATING_V6_IP = "_floating_v6_ip";
    private static final String UNDERSCORE = "_";
    private static final String ENABLE_BRIDGE = "mso.bridgeEnabled";

    @PostConstruct
    public void init() {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    public CreateVfModuleRequest createVfModuleRequestMapper(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, GenericVnf genericVnf,
            VfModule vfModule, VolumeGroup volumeGroup, String sdncVnfQueryResponse, String sdncVfModuleQueryResponse)
            throws IOException, MissingValueTagException {
        CreateVfModuleRequest createVfModuleRequest = new CreateVfModuleRequest();

        createVfModuleRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        createVfModuleRequest.setCloudOwner(cloudRegion.getCloudOwner());
        createVfModuleRequest.setTenantId(cloudRegion.getTenantId());
        createVfModuleRequest.setVfModuleId(vfModule.getVfModuleId());
        createVfModuleRequest.setVfModuleName(vfModule.getVfModuleName());
        createVfModuleRequest.setVnfId(genericVnf.getVnfId());
        createVfModuleRequest.setVnfType(genericVnf.getVnfType());
        createVfModuleRequest.setVnfVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        createVfModuleRequest.setVfModuleType(vfModule.getModelInfoVfModule().getModelName());
        createVfModuleRequest.setModelCustomizationUuid(vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        if (volumeGroup != null) {
            createVfModuleRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());
            createVfModuleRequest.setVolumeGroupStackId(volumeGroup.getHeatStackId());
        }
        VfModule baseVfModule = getBaseVfModule(genericVnf);
        if (baseVfModule != null) {
            createVfModuleRequest.setBaseVfModuleId(baseVfModule.getVfModuleId());
            createVfModuleRequest.setBaseVfModuleStackId(baseVfModule.getHeatStackId());
        }
        createVfModuleRequest.setVfModuleParams(buildVfModuleParamsMap(requestContext, serviceInstance, genericVnf,
                vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse));

        createVfModuleRequest.setSkipAAI(true);
        createVfModuleRequest.setBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        createVfModuleRequest.setFailIfExists(false);

        MsoRequest msoRequest = buildMsoRequest(requestContext, serviceInstance);
        createVfModuleRequest.setMsoRequest(msoRequest);

        String messageId = vnfAdapterObjectMapperUtils.getRandomUuid();
        createVfModuleRequest.setMessageId(messageId);
        createVfModuleRequest
                .setNotificationUrl(vnfAdapterObjectMapperUtils.createCallbackUrl("VNFAResponse", messageId));

        String enableBridge = getProperty(ENABLE_BRIDGE);
        if (enableBridge == null || Boolean.valueOf(enableBridge)) {
            createVfModuleRequest.setEnableBridge(true);
        }

        return createVfModuleRequest;
    }

    private MsoRequest buildMsoRequest(RequestContext requestContext, ServiceInstance serviceInstance) {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        return msoRequest;
    }

    public Map<String, Object> buildVfModuleParamsMap(RequestContext requestContext, ServiceInstance serviceInstance,
            GenericVnf genericVnf, VfModule vfModule, String sdncVnfQueryResponse, String sdncVfModuleQueryResponse)
            throws IOException, MissingValueTagException {


        GenericResourceApiVnfTopology vnfTop =
                mapper.readValue(sdncVnfQueryResponse, GenericResourceApiVnfTopology.class);
        GenericResourceApiVfModuleTopology vfModuleTop =
                mapper.readValue(sdncVfModuleQueryResponse, GenericResourceApiVfModuleTopology.class);
        GenericResourceApiVnftopologyVnfTopology vnfTopology = vnfTop.getVnfTopology();
        GenericResourceApiVfmoduletopologyVfModuleTopology vfModuleTopology = vfModuleTop.getVfModuleTopology();
        Map<String, Object> paramsMap = new HashMap<>();

        if (Boolean.TRUE.equals(vfModuleTopology.getSdncGeneratedCloudResources())) {
            buildParamsMapFromVfModuleSdncResponse(paramsMap, vfModuleTopology, true);
            buildParamsMapFromVnfSdncResponse(paramsMap, vnfTopology, null, true);
        } else {
            Map<String, String> networkRoleMap = buildNetworkRoleMap(vfModuleTopology);
            buildParamsMapFromVfModuleSdncResponse(paramsMap, vfModuleTopology, false);
            buildParamsMapFromVnfSdncResponse(paramsMap, vnfTopology, networkRoleMap, false);
        }

        // build the sdnc_directives from paramsMap
        buildDirectivesParamFromMap(paramsMap, MsoMulticloudUtils.SDNC_DIRECTIVES, paramsMap);
        buildDirectivesParamFromMap(paramsMap, MsoMulticloudUtils.USER_DIRECTIVES, requestContext.getUserParams());

        buildMandatoryParamsMap(paramsMap, serviceInstance, genericVnf, vfModule);

        // Parameters received from the request should overwrite any parameters received from SDNC
        paramsMap.putAll(requestContext.getUserParams());

        if (vfModule.getCloudParams() != null) {
            paramsMap.putAll(vfModule.getCloudParams());
        }
        return paramsMap;
    }

    protected void buildDirectivesParamFromMap(Map<String, Object> paramsMap, String directive,
            Map<String, Object> srcMap) throws MissingValueTagException {
        StringBuilder directives = new StringBuilder();
        int noOfDirectivesSize = 0;
        if (directive.equals(MsoMulticloudUtils.USER_DIRECTIVES)
                && srcMap.containsKey(MsoMulticloudUtils.OOF_DIRECTIVES)) {
            noOfDirectivesSize = 1;
        }
        if (srcMap.size() > noOfDirectivesSize) {
            directives.append("{ \"attributes\": [ ");
            int i = 0;
            for (Map.Entry<String, Object> attributeEntry : srcMap.entrySet()) {
                String attributeName = attributeEntry.getKey();
                Object attributeValue = attributeEntry.getValue();
                if (!(MsoMulticloudUtils.USER_DIRECTIVES.equals(directive)
                        && attributeName.equals(MsoMulticloudUtils.OOF_DIRECTIVES))) {
                    if (attributeValue == null) {
                        logger.error("No value tag found for attribute: {}", attributeName);
                        throw new MissingValueTagException("No value tag found for " + attributeName);
                    }
                    String nameValue = new StringBuilder().append("{\"attribute_name\": \"").append(attributeName)
                            .append("\", \"attribute_value\": \"").append(attributeValue.toString()).append("\"}")
                            .toString();
                    directives.append(nameValue);
                    if (i < (srcMap.size() - 1 + noOfDirectivesSize))
                        directives.append(", ");
                    i++;
                }
            }
            directives.append("] }");
        } else {
            directives.append("{}");
        }
        paramsMap.put(directive, directives.toString());
    }

    private void buildMandatoryParamsMap(Map<String, Object> paramsMap, ServiceInstance serviceInstance,
            GenericVnf genericVnf, VfModule vfModule) {
        paramsMap.put("vnf_id", genericVnf.getVnfId());
        paramsMap.put("vnf_name", genericVnf.getVnfName());
        paramsMap.put("vf_module_id", vfModule.getVfModuleId());
        paramsMap.put("vf_module_name", vfModule.getVfModuleName());
        paramsMap.put("environment_context", serviceInstance.getModelInfoServiceInstance().getEnvironmentContext());
        paramsMap.putIfAbsent("environment_context", "");
        paramsMap.put("workload_context", serviceInstance.getModelInfoServiceInstance().getWorkloadContext());
        paramsMap.putIfAbsent("workload_context", "");
        Integer vfModuleIndex = vfModule.getModuleIndex();
        if (vfModuleIndex != null) {
            paramsMap.put("vf_module_index", vfModuleIndex.toString());
        }
    }

    private void buildParamsMapFromVnfSdncResponse(Map<String, Object> paramsMap,
            GenericResourceApiVnftopologyVnfTopology vnfTopology, Map<String, String> networkRoleMap,
            boolean skipVnfResourceAssignments) {
        // Get VNF parameters from SDNC response
        GenericResourceApiParam vnfParametersData = vnfTopology.getVnfParametersData();
        buildParamsMapFromSdncParams(paramsMap, vnfParametersData);

        if (!skipVnfResourceAssignments) {
            GenericResourceApiVnfresourceassignmentsVnfResourceAssignments vnfResourceAssignments =
                    vnfTopology.getVnfResourceAssignments();
            if (vnfResourceAssignments != null) {
                // Availability Zones
                buildAvailabilityZones(paramsMap, vnfResourceAssignments);
                // VNF Networks
                buildVnfNetworks(paramsMap, vnfResourceAssignments, networkRoleMap);
            }
        }
    }

    private void buildAvailabilityZones(Map<String, Object> paramsMap,
            GenericResourceApiVnfresourceassignmentsVnfResourceAssignments vnfResourceAssignments) {
        GenericResourceApiVnfresourceassignmentsVnfresourceassignmentsAvailabilityZones availabilityZones =
                vnfResourceAssignments.getAvailabilityZones();
        if (availabilityZones != null) {
            List<String> availabilityZonesList = availabilityZones.getAvailabilityZone();
            if (availabilityZonesList != null) {
                for (int i = 0; i < availabilityZonesList.size(); i++) {
                    paramsMap.put("availability_zone_" + i, availabilityZonesList.get(i));
                }
            }
        }
    }

    private void buildVnfNetworks(Map<String, Object> paramsMap,
            GenericResourceApiVnfresourceassignmentsVnfResourceAssignments vnfResourceAssignments,
            Map<String, String> networkRoleMap) {
        GenericResourceApiVnfresourceassignmentsVnfresourceassignmentsVnfNetworks vnfNetworks =
                vnfResourceAssignments.getVnfNetworks();
        if (vnfNetworks != null) {
            List<GenericResourceApiVnfNetworkData> vnfNetworksList = vnfNetworks.getVnfNetwork();
            if (vnfNetworksList != null) {
                for (int i = 0; i < vnfNetworksList.size(); i++) {
                    GenericResourceApiVnfNetworkData vnfNetwork = vnfNetworksList.get(i);
                    String networkRole = vnfNetwork.getNetworkRole();
                    String vnfNetworkKey = networkRoleMap.get(networkRole);
                    if (vnfNetworkKey == null || vnfNetworkKey.isEmpty()) {
                        vnfNetworkKey = networkRole;
                    }

                    String vnfNetworkNeutronIdValue = vnfNetwork.getNeutronId();
                    paramsMap.put(vnfNetworkKey + "_net_id", vnfNetworkNeutronIdValue);
                    String vnfNetworkNetNameValue = vnfNetwork.getNetworkName();
                    paramsMap.put(vnfNetworkKey + "_net_name", vnfNetworkNetNameValue);
                    String vnfNetworkNetFqdnValue = vnfNetwork.getContrailNetworkFqdn();
                    paramsMap.put(vnfNetworkKey + "_net_fqdn", vnfNetworkNetFqdnValue);

                    buildVnfNetworkSubnets(paramsMap, vnfNetwork, vnfNetworkKey);

                }
            }
        }
    }

    private void buildVnfNetworkSubnets(Map<String, Object> paramsMap, GenericResourceApiVnfNetworkData vnfNetwork,
            String vnfNetworkKey) {
        String vnfNetworkString = convertToString(vnfNetwork);
        Optional<String> ipv4Ips = jsonPath.locateResult(vnfNetworkString,
                "$.subnets-data.subnet-data[*].[?(@.ip-version == 'ipv4' && @.dhcp-enabled == 'Y')].subnet-id");
        if (ipv4Ips.isPresent())
            addPairToMap(paramsMap, vnfNetworkKey, SUBNET_ID, ipv4Ips.get());

        Optional<String> ipv6Ips = jsonPath.locateResult(vnfNetworkString,
                "$.subnets-data.subnet-data[*].[?(@.ip-version == 'ipv6' && @.dhcp-enabled == 'Y')].subnet-id");
        if (ipv6Ips.isPresent())
            addPairToMap(paramsMap, vnfNetworkKey, V6_SUBNET_ID, ipv6Ips.get());
    }

    private void buildParamsMapFromVfModuleSdncResponse(Map<String, Object> paramsMap,
            GenericResourceApiVfmoduletopologyVfModuleTopology vfModuleTopology, boolean skipVfModuleAssignments) {
        // Get VF Module parameters from SDNC response
        GenericResourceApiParam vfModuleParametersData = vfModuleTopology.getVfModuleParameters();
        buildParamsMapFromSdncParams(paramsMap, vfModuleParametersData);

        if (!skipVfModuleAssignments) {
            GenericResourceApiVfmoduleassignmentsVfModuleAssignments vfModuleAssignments =
                    vfModuleTopology.getVfModuleAssignments();
            if (vfModuleAssignments != null) {
                // VNF-VMS
                GenericResourceApiVfmoduleassignmentsVfmoduleassignmentsVms vms = vfModuleAssignments.getVms();
                if (vms != null) {
                    List<GenericResourceApiVmTopologyData> vmsList = vms.getVm();
                    if (vmsList != null) {
                        for (GenericResourceApiVmTopologyData vm : vmsList) {
                            String key = vm.getVmType();
                            buildVfModuleVmNames(paramsMap, vm, key);
                            GenericResourceApiVmtopologydataVmNetworks vmNetworks = vm.getVmNetworks();
                            if (vmNetworks != null) {
                                List<GenericResourceApiVmNetworkData> vmNetworksList = vmNetworks.getVmNetwork();
                                if (vmNetworksList != null) {
                                    for (int n = 0; n < vmNetworksList.size(); n++) {
                                        GenericResourceApiVmNetworkData network = vmNetworksList.get(n);
                                        network.getNetworkRoleTag();
                                        String networkKey = network.getNetworkRole();
                                        // Floating IPs
                                        buildVfModuleFloatingIps(paramsMap, network, key, networkKey);
                                        // Interface Route Prefixes
                                        buildVfModuleInterfaceRoutePrefixes(paramsMap, network, key, networkKey);
                                        // SRIOV Parameters
                                        buildVfModuleSriovParameters(paramsMap, network, networkKey);
                                        // IPV4 and IPV6 Addresses
                                        buildVfModuleNetworkInformation(paramsMap, network, key, networkKey);

                                        buildVlanInformation(paramsMap, network, key, networkKey);

                                    }
                                }
                            }

                            buildParamsMapFromVfModuleForHeatTemplate(paramsMap, vm);
                        }
                    }
                }
            }
        }
    }

    protected void buildVlanInformation(Map<String, Object> paramsMap, GenericResourceApiVmNetworkData network,
            String key, String networkKey) {

        String networkString = convertToString(network);
        String vlanFilterKey = key + UNDERSCORE + networkKey + UNDERSCORE + "vlan_filter";
        String privateVlansKey = key + UNDERSCORE + networkKey + UNDERSCORE + "private_vlans";
        String publicVlansKey = key + UNDERSCORE + networkKey + UNDERSCORE + "public_vlans";
        String guestVlansKey = key + UNDERSCORE + networkKey + UNDERSCORE + "guest_vlans";

        if (network.getSegmentationId() != null) {
            paramsMap.put(vlanFilterKey, network.getSegmentationId());
        }

        List<String> privateVlans = jsonPath.locateResultList(networkString,
                "$.related-networks.related-network[?(@.vlan-tags.is-private == true)].vlan-tags.upper-tag-id");
        List<String> publicVlans = jsonPath.locateResultList(networkString,
                "$.related-networks.related-network[?(@.vlan-tags.is-private == false)].vlan-tags.upper-tag-id");
        List<String> concat = new ArrayList<>(privateVlans);
        concat.addAll(publicVlans);
        Collection<String> guestVlans = new HashSet<>(concat);

        if (!privateVlans.isEmpty()) {
            paramsMap.put(privateVlansKey, Joiner.on(",").join(privateVlans));
        }
        if (!publicVlans.isEmpty()) {
            paramsMap.put(publicVlansKey, Joiner.on(",").join(publicVlans));
        }
        if (!guestVlans.isEmpty()) {
            paramsMap.put(guestVlansKey, Joiner.on(",").join(guestVlans));
        }
    }

    private void buildVfModuleVmNames(Map<String, Object> paramsMap, GenericResourceApiVmTopologyData vm, String key) {
        String values = "";
        GenericResourceApiVmtopologydataVmNames vmNames = vm.getVmNames();
        if (vmNames != null) {
            List<String> valueList = vmNames.getVmName();
            if (valueList != null) {
                for (int i = 0; i < valueList.size(); i++) {
                    String value = valueList.get(i);
                    if (i != valueList.size() - 1) {
                        values += value + ",";
                    } else {
                        values += value;
                    }
                    paramsMap.put(key + "_name_" + i, value);
                }
                paramsMap.put(key + "_names", values);
            }
        }
    }

    private void buildVfModuleFloatingIps(Map<String, Object> paramsMap, GenericResourceApiVmNetworkData network,
            String key, String networkKey) {
        GenericResourceApiVmnetworkdataFloatingIps floatingIps = network.getFloatingIps();
        if (floatingIps != null) {
            List<String> floatingIpV4List = floatingIps.getFloatingIpV4();
            if (floatingIpV4List != null) {
                // add only one ipv4 floating ip for now
                String floatingIPKey = key + UNDERSCORE + networkKey + FLOATING_IP;
                String floatingIPKeyValue = floatingIpV4List.get(0);
                if (floatingIPKeyValue != null && !floatingIPKeyValue.isEmpty()) {
                    paramsMap.put(floatingIPKey, floatingIPKeyValue);
                }
            }
            // add only one ipv6 floating ip for now
            List<String> floatingIpV6List = floatingIps.getFloatingIpV6();
            if (floatingIpV6List != null) {
                String floatingIPV6Key = key + UNDERSCORE + networkKey + FLOATING_V6_IP;
                String floatingIPV6KeyValue = floatingIpV6List.get(0);
                if (floatingIPV6KeyValue != null && !floatingIPV6KeyValue.isEmpty()) {
                    paramsMap.put(floatingIPV6Key, floatingIPV6KeyValue);
                }
            }
        }
    }

    private void buildVfModuleInterfaceRoutePrefixes(Map<String, Object> paramsMap,
            GenericResourceApiVmNetworkData network, String key, String networkKey) {
        GenericResourceApiVmnetworkdataInterfaceRoutePrefixes interfaceRoutePrefixes =
                network.getInterfaceRoutePrefixes();
        if (interfaceRoutePrefixes != null) {
            List<String> interfaceRoutePrefixesList = interfaceRoutePrefixes.getInterfaceRoutePrefix();
            StringBuilder sbInterfaceRoutePrefixes = new StringBuilder();
            sbInterfaceRoutePrefixes.append("[");
            if (interfaceRoutePrefixesList != null) {
                for (int a = 0; a < interfaceRoutePrefixesList.size(); a++) {
                    String interfaceRoutePrefixValue = interfaceRoutePrefixesList.get(a);
                    if (a != interfaceRoutePrefixesList.size() - 1) {
                        sbInterfaceRoutePrefixes.append("{\"interface_route_table_routes_route_prefix\": \""
                                + interfaceRoutePrefixValue + "\"}" + ",");
                    } else {
                        sbInterfaceRoutePrefixes.append("{\"interface_route_table_routes_route_prefix\": \""
                                + interfaceRoutePrefixValue + "\"}");
                    }
                }
                sbInterfaceRoutePrefixes.append("]");
                if (!interfaceRoutePrefixesList.isEmpty()) {
                    paramsMap.put(key + UNDERSCORE + networkKey + "_route_prefixes",
                            sbInterfaceRoutePrefixes.toString());
                }
            }
        }
    }

    private void buildVfModuleSriovParameters(Map<String, Object> paramsMap, GenericResourceApiVmNetworkData network,
            String networkKey) {
        // SRIOV Parameters
        GenericResourceApiVmnetworkdataSriovParameters sriovParameters = network.getSriovParameters();
        if (sriovParameters != null) {
            GenericResourceApiVmnetworkdataSriovparametersHeatVlanFilters heatVlanFilters =
                    sriovParameters.getHeatVlanFilters();
            if (heatVlanFilters != null) {
                List<String> heatVlanFiltersList = heatVlanFilters.getHeatVlanFilter();
                StringBuilder sriovFilterBuf = new StringBuilder();
                if (heatVlanFiltersList != null) {
                    for (int a = 0; a < heatVlanFiltersList.size(); a++) {
                        String heatVlanFilterValue = heatVlanFiltersList.get(a);
                        if (a != heatVlanFiltersList.size() - 1) {
                            sriovFilterBuf.append(heatVlanFilterValue).append(",");
                        } else {
                            sriovFilterBuf.append(heatVlanFilterValue);
                        }
                    }
                    if (!heatVlanFiltersList.isEmpty()) {
                        paramsMap.put(networkKey + "_ATT_VF_VLAN_FILTER", sriovFilterBuf.toString());
                    }
                }
            }
        }
    }

    private void buildVfModuleNetworkInformation(Map<String, Object> paramsMap, GenericResourceApiVmNetworkData network,
            String key, String networkKey) {

        GenericResourceApiVmnetworkdataNetworkInformationItems networkInformationItems =
                network.getNetworkInformationItems();
        StringBuilder sbIpv4Ips = new StringBuilder();
        StringBuilder sbIpv6Ips = new StringBuilder();

        if (networkInformationItems != null) {
            List<GenericResourceApiVmnetworkdataNetworkinformationitemsNetworkInformationItem> networkInformationItemList =
                    networkInformationItems.getNetworkInformationItem();
            if (networkInformationItemList != null) {
                for (int a = 0; a < networkInformationItemList.size(); a++) {
                    GenericResourceApiVmnetworkdataNetworkinformationitemsNetworkInformationItem ipAddress =
                            networkInformationItemList.get(a);
                    if (ipAddress != null) {
                        GenericResourceApiVmnetworkdataNetworkinformationitemsNetworkinformationitemNetworkIps ips =
                                ipAddress.getNetworkIps();
                        if (ips != null) {
                            List<String> ipsList = ips.getNetworkIp();
                            if (ipsList != null) {
                                String ipVersion = ipAddress.getIpVersion();
                                for (int b = 0; b < ipsList.size(); b++) {
                                    String ipAddressValue = ipsList.get(b);
                                    if ("ipv4".equals(ipVersion)) {
                                        if (b != ipsList.size() - 1) {
                                            sbIpv4Ips.append(ipAddressValue + ",");
                                        } else {
                                            sbIpv4Ips.append(ipAddressValue);
                                        }
                                        paramsMap.put(key + UNDERSCORE + networkKey + IP + UNDERSCORE + b,
                                                ipAddressValue);
                                    } else if ("ipv6".equals(ipVersion)) {
                                        if (b != ipsList.size() - 1) {
                                            sbIpv6Ips.append(ipAddressValue + ",");
                                        } else {
                                            sbIpv6Ips.append(ipAddressValue);
                                        }
                                        paramsMap.put(key + UNDERSCORE + networkKey + V6_IP + UNDERSCORE + b,
                                                ipAddressValue);
                                    }
                                }
                                paramsMap.put(key + UNDERSCORE + networkKey + "_ips", sbIpv4Ips.toString());
                                paramsMap.put(key + UNDERSCORE + networkKey + "_v6_ips", sbIpv6Ips.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Build Mapping from GenericResourceApi SDNC for Heat Template so that AIC - PO gets accurate requests for vf
     * module assignments. Build Count of SubInterfaces, VLAN Tag, network_name, network_id, ip_address (V4 and V6) and
     * Floating IPs Addresses (V4 and V6) for Heat Template
     */
    private void buildParamsMapFromVfModuleForHeatTemplate(Map<String, Object> paramsMap,
            GenericResourceApiVmTopologyData vm) {
        GenericResourceApiVmtopologydataVmNames vmNames = vm.getVmNames();

        if (vmNames != null) {

            List<GenericResourceApiVmtopologydataVmnamesVnfcNames> vnfcNamesList = vmNames.getVnfcNames();
            if (vnfcNamesList != null) {

                for (int i = 0; i < vnfcNamesList.size(); i++) {

                    GenericResourceApiVmtopologydataVmnamesVnfcNames vnfcNames = vnfcNamesList.get(i);
                    parseVnfcNamesData(paramsMap, vnfcNames);
                }
            }
        }
    }

    /*
     * Parse vnfcNames data to build Mapping from GenericResourceApi SDNC for Heat Template.
     */
    private void parseVnfcNamesData(Map<String, Object> paramsMap,
            GenericResourceApiVmtopologydataVmnamesVnfcNames vnfcNames) {

        if (vnfcNames != null) {
            GenericResourceApiVnfcNetworkData vnfcNetworks = vnfcNames.getVnfcNetworks();
            if (vnfcNetworks != null) {
                List<GenericResourceApiVnfcnetworkdataVnfcNetworkData> vnfcNetworkdataList =
                        vnfcNetworks.getVnfcNetworkData();

                if (vnfcNetworkdataList != null) {

                    for (int networkDataIdx = 0; networkDataIdx < vnfcNetworkdataList.size(); networkDataIdx++) {

                        GenericResourceApiVnfcnetworkdataVnfcNetworkData vnfcNetworkdata =
                                vnfcNetworkdataList.get(networkDataIdx);
                        parseVnfcNetworkData(paramsMap, vnfcNetworkdata, networkDataIdx);
                    }
                }
            }
        }
    }

    /*
     * Parse VnfcNetworkData to build Mapping from GenericResourceApi SDNC for Heat Template. Build Count of
     * SubInterfaces, VLAN Tag, network_name, network_id, ip_address (V4 and V6) and Floating IPs Addresses (V4 and V6)
     * for Heat Template
     */
    private void parseVnfcNetworkData(Map<String, Object> paramsMap,
            GenericResourceApiVnfcnetworkdataVnfcNetworkData vnfcNetworkdata, int networkDataIdx) {

        String vmTypeKey = vnfcNetworkdata.getVnfcType();
        GenericResourceApiVnfcnetworkdataVnfcnetworkdataVnfcPorts vnfcPorts = vnfcNetworkdata.getVnfcPorts();
        if (vnfcPorts != null) {
            List<GenericResourceApiVnfcnetworkdataVnfcnetworkdataVnfcportsVnfcPort> vnfcPortList =
                    vnfcPorts.getVnfcPort();
            if (vnfcPortList != null) {
                for (int portIdx = 0; portIdx < vnfcPortList.size(); portIdx++) {

                    GenericResourceApiVnfcnetworkdataVnfcnetworkdataVnfcportsVnfcPort vnfcPort =
                            vnfcPortList.get(portIdx);
                    GenericResourceApiSubInterfaceNetworkData vnicSubInterfaces = vnfcPort.getVnicSubInterfaces();

                    String vnicSubInterfacesString = convertToString(vnicSubInterfaces);
                    String networkRoleKey = vnfcPort.getCommonSubInterfaceRole();
                    String subInterfaceKey =
                            createVnfcSubInterfaceKey(vmTypeKey, networkDataIdx, networkRoleKey, portIdx);
                    String globalSubInterfaceKey = createGlobalVnfcSubInterfaceKey(vmTypeKey, networkRoleKey, portIdx);

                    buildVfModuleSubInterfacesCount(paramsMap, globalSubInterfaceKey, vnicSubInterfaces);

                    buildVfModuleVlanTag(paramsMap, subInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleNetworkName(paramsMap, subInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleNetworkId(paramsMap, subInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleIpV4AddressHeatTemplate(paramsMap, subInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleIpV6AddressHeatTemplate(paramsMap, subInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleFloatingIpV4HeatTemplate(paramsMap, globalSubInterfaceKey, vnicSubInterfacesString);

                    buildVfModuleFloatingIpV6HeatTemplate(paramsMap, globalSubInterfaceKey, vnicSubInterfacesString);
                }
            }
        }
    }

    /*
     * Build "count" (calculating the total number of sub-interfaces) for Heat Template Building Criteria :
     * {vm-type}_subint_{network-role}_port_{index}_subintcount vmTypeKey = vm-type, networkRoleKey =
     * common-sub-interface-role Example: fw_subint_ctrl_port_0_subintcount
     *
     */
    private void buildVfModuleSubInterfacesCount(Map<String, Object> paramsMap, String keyPrefix,
            GenericResourceApiSubInterfaceNetworkData vnicSubInterfaces) {

        List<GenericResourceApiSubinterfacenetworkdataSubInterfaceNetworkData> subInterfaceNetworkDataList =
                vnicSubInterfaces.getSubInterfaceNetworkData();

        if ((subInterfaceNetworkDataList != null) && !subInterfaceNetworkDataList.isEmpty()) {
            addPairToMap(paramsMap, keyPrefix, SUB_INT_COUNT, String.valueOf(subInterfaceNetworkDataList.size()));
        }
    }

    protected String createVnfcSubInterfaceKey(String vmTypeKey, int networkDataIdx, String networkRoleKey,
            int portIdx) {

        return Joiner.on(UNDERSCORE)
                .join(Arrays.asList(vmTypeKey, networkDataIdx, SUB_INT, networkRoleKey, PORT, portIdx));
    }

    protected String createGlobalVnfcSubInterfaceKey(String vmTypeKey, String networkRoleKey, int portIdx) {

        return Joiner.on(UNDERSCORE).join(Arrays.asList(vmTypeKey, SUB_INT, networkRoleKey, PORT, portIdx));
    }

    /*
     * Build VLAN Tag for Heat Template Building Criteria :
     * {vm-type}_{index}_subint_{network-role}_port_{index}_vlan_ids vmTypeKey = vm-type, networkRoleKey =
     * common-sub-interface-role Example: fw_0_subint_ctrl_port_0_vlan_ids
     *
     */
    protected void buildVfModuleVlanTag(Map<String, Object> paramsMap, String keyPrefix, String vnicSubInterfaces) {

        List<String> vlanTagIds =
                jsonPath.locateResultList(vnicSubInterfaces, "$.sub-interface-network-data[*].vlan-tag-id");

        addPairToMap(paramsMap, keyPrefix, VLAN_IDS, vlanTagIds);
    }

    /*
     * Build "network_name" for Heat Template Building Criteria :
     * {vm-type}_{index}_subint_{network-role}_port_{index}_net_names vmTypeKey = vm-type, networkRoleKey =
     * common-sub-interface-role Example: fw_0_subint_ctrl_port_0_net_names
     *
     */
    protected void buildVfModuleNetworkName(Map<String, Object> paramsMap, String keyPrefix, String vnicSubInterfaces) {

        List<String> neworkNames =
                jsonPath.locateResultList(vnicSubInterfaces, "$.sub-interface-network-data[*].network-name");

        addPairToMap(paramsMap, keyPrefix, NET_NAMES, neworkNames);
    }

    /*
     * Build "network_id" for Heat Template Building Criteria :
     * {vm-type}_{index}_subint_{network-role}_port_{index}_net_ids vmTypeKey = vm-type, networkRoleKey =
     * common-sub-interface-role Example: fw_0_subint_ctrl_port_0_net_ids
     *
     */
    protected void buildVfModuleNetworkId(Map<String, Object> paramsMap, String keyPrefix, String vnicSubInterfaces) {

        List<String> neworkIds =
                jsonPath.locateResultList(vnicSubInterfaces, "$.sub-interface-network-data[*].network-id");

        addPairToMap(paramsMap, keyPrefix, NET_IDS, neworkIds);
    }

    /*
     * Build ip_address for V4 for Heat Template Building Criteria :
     * {vm-type}_{index}_subint_{network-role}_port_{index}_ip_{index} -- for ipV4 key = vm-type, networkRoleKey =
     * NetWork-Role
     */
    protected void buildVfModuleIpV4AddressHeatTemplate(Map<String, Object> paramsMap, String keyPrefix,
            String vnicSubInterfaces) {

        List<String> ipv4Ips = jsonPath.locateResultList(vnicSubInterfaces,
                "$.sub-interface-network-data[*].network-information-items.network-information-item[?(@.ip-version == 'ipv4')].network-ips.network-ip[*]");

        addPairToMap(paramsMap, keyPrefix, IP, ipv4Ips);

        for (int i = 0; i < ipv4Ips.size(); i++) {
            addPairToMap(paramsMap, keyPrefix, IP + UNDERSCORE + i, ipv4Ips.get(i));
        }

    }

    /*
     * Build ip_address for Heat Template Building Criteria :
     * {vm-type}_{index}_subint_{network-role}_port_{index}_v6_ip_{index} -- for ipV6 key = vm-type, networkRoleKey =
     * NetWork-Role
     */
    protected void buildVfModuleIpV6AddressHeatTemplate(Map<String, Object> paramsMap, String keyPrefix,
            String vnicSubInterfaces) {

        List<String> ipv6Ips = jsonPath.locateResultList(vnicSubInterfaces,
                "$.sub-interface-network-data[*].network-information-items.network-information-item[?(@.ip-version == 'ipv6')].network-ips.network-ip[*]");

        addPairToMap(paramsMap, keyPrefix, V6_IP, ipv6Ips);

        for (int i = 0; i < ipv6Ips.size(); i++) {
            addPairToMap(paramsMap, keyPrefix, V6_IP + UNDERSCORE + i, ipv6Ips.get(i));
        }
    }

    /*
     * Build floatingip_address for Heat Template Building Criteria :
     * {vm-type}_subint_{network-role}_port_{index}_floating_ip -- for ipV4
     */
    protected void buildVfModuleFloatingIpV4HeatTemplate(Map<String, Object> paramsMap, String keyPrefix,
            String vnicSubInterfaces) {

        List<String> floatingV4 = jsonPath.locateResultList(vnicSubInterfaces,
                "$.sub-interface-network-data[*].floating-ips.floating-ip-v4[*]");

        if (!floatingV4.isEmpty()) {
            floatingV4 = Collections.singletonList(floatingV4.get(0));
        }
        addPairToMap(paramsMap, keyPrefix, FLOATING_IP, floatingV4);

    }

    /*
     * Build floatingip_address for Heat Template Building Criteria :
     * {vm-type}_subint_{network-role}_port_{index}_floating_v6_ip -- for ipV6
     */
    protected void buildVfModuleFloatingIpV6HeatTemplate(Map<String, Object> paramsMap, String keyPrefix,
            String vnicSubInterfaces) {

        List<String> floatingV6 = jsonPath.locateResultList(vnicSubInterfaces,
                "$.sub-interface-network-data[*].floating-ips.floating-ip-v6[*]");

        if (!floatingV6.isEmpty()) {
            floatingV6 = Collections.singletonList(floatingV6.get(0));
        }
        addPairToMap(paramsMap, keyPrefix, FLOATING_V6_IP, floatingV6);
    }

    protected void addPairToMap(Map<String, Object> paramsMap, String keyPrefix, String key, String value) {

        addPairToMap(paramsMap, keyPrefix, key, Collections.singletonList(value));
    }

    protected void addPairToMap(Map<String, Object> paramsMap, String keyPrefix, String key, List<String> value) {

        if (!value.isEmpty()) {
            paramsMap.put(keyPrefix + key, Joiner.on(",").join(value));
        }
    }

    private void buildParamsMapFromSdncParams(Map<String, Object> paramsMap, GenericResourceApiParam parametersData) {
        if (parametersData != null) {
            List<GenericResourceApiParamParam> paramsList = parametersData.getParam();
            if (paramsList != null) {
                for (int i = 0; i < paramsList.size(); i++) {
                    GenericResourceApiParamParam param = paramsList.get(i);
                    String parameterName = param.getName();
                    if (!sdncResponseParamsToSkip.contains(parameterName)) {
                        String parameterValue = param.getValue();
                        paramsMap.put(parameterName, parameterValue);
                    }
                }
            }
        }
    }

    private Map<String, String> buildNetworkRoleMap(
            GenericResourceApiVfmoduletopologyVfModuleTopology vfModuleTopology) {
        Map<String, String> networkRoleMap = new HashMap<>();
        GenericResourceApiVfmoduleassignmentsVfModuleAssignments vfModuleAssignments =
                vfModuleTopology.getVfModuleAssignments();
        if (vfModuleAssignments != null) {
            GenericResourceApiVfmoduleassignmentsVfmoduleassignmentsVms vms = vfModuleAssignments.getVms();
            if (vms != null) {
                List<GenericResourceApiVmTopologyData> vmsList = vms.getVm();
                if (vmsList != null) {
                    for (GenericResourceApiVmTopologyData vm : vmsList) {
                        GenericResourceApiVmtopologydataVmNetworks vmNetworks = vm.getVmNetworks();
                        if (vmNetworks != null) {
                            List<GenericResourceApiVmNetworkData> vmNetworksList = vmNetworks.getVmNetwork();
                            if (vmNetworksList != null) {
                                for (int n = 0; n < vmNetworksList.size(); n++) {
                                    GenericResourceApiVmNetworkData network = vmNetworksList.get(n);
                                    String networkRole = network.getNetworkRole();
                                    String networkRoleValue = network.getNetworkRoleTag();
                                    if (networkRoleValue == null || networkRoleValue.isEmpty()) {
                                        networkRoleValue = networkRole;
                                    }
                                    networkRoleMap.put(networkRole, networkRoleValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return networkRoleMap;
    }

    public DeleteVfModuleRequest deleteVfModuleRequestMapper(RequestContext requestContext, CloudRegion cloudRegion,
            ServiceInstance serviceInstance, GenericVnf genericVnf, VfModule vfModule) throws IOException {
        DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
        deleteVfModuleRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        deleteVfModuleRequest.setCloudOwner(cloudRegion.getCloudOwner());
        deleteVfModuleRequest.setTenantId(cloudRegion.getTenantId());
        deleteVfModuleRequest.setVnfId(genericVnf.getVnfId());
        deleteVfModuleRequest.setVfModuleId(vfModule.getVfModuleId());
        if (vfModule.getModelInfoVfModule() != null) {
            deleteVfModuleRequest
                    .setModelCustomizationUuid(vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        }
        if (!StringUtils.isEmpty(vfModule.getHeatStackId())) {
            deleteVfModuleRequest.setVfModuleStackId(vfModule.getHeatStackId());// DoDVfMod_heatStackId
        } else {
            deleteVfModuleRequest.setVfModuleStackId(vfModule.getVfModuleName());
        }

        deleteVfModuleRequest.setSkipAAI(true);
        setIdAndUrl(deleteVfModuleRequest);
        MsoRequest msoRequest = buildMsoRequest(requestContext, serviceInstance);
        deleteVfModuleRequest.setMsoRequest(msoRequest);
        return deleteVfModuleRequest;
    }

    protected void setIdAndUrl(DeleteVfModuleRequest deleteVfModuleRequest) throws UnsupportedEncodingException {
        String messageId = vnfAdapterObjectMapperUtils.getRandomUuid();
        deleteVfModuleRequest.setMessageId(messageId);
        deleteVfModuleRequest
                .setNotificationUrl(vnfAdapterObjectMapperUtils.createCallbackUrl("VNFAResponse", messageId));
    }

    private String convertToString(Object obj) {
        String json;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException in convertToString", e);
            json = "{}";
        }

        return json;
    }

    private VfModule getBaseVfModule(GenericVnf genericVnf) {
        List<VfModule> vfModules = genericVnf.getVfModules();
        VfModule baseVfModule = null;
        if (vfModules != null) {
            for (int i = 0; i < vfModules.size(); i++) {
                if (vfModules.get(i).getModelInfoVfModule().getIsBaseBoolean()) {
                    baseVfModule = vfModules.get(i);
                    break;
                }
            }
        }
        return baseVfModule;
    }

    protected String getProperty(String key) {
        return UrnPropertiesReader.getVariable(key);
    }
}
