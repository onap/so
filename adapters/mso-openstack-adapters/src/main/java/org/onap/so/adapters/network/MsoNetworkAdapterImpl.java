/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.jws.WebService;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.adapters.network.beans.ContrailPolicyRef;
import org.onap.so.adapters.network.beans.ContrailPolicyRefSeq;
import org.onap.so.adapters.network.beans.ContrailSubnet;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.adapters.network.mappers.ContrailSubnetMapper;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.data.repository.CollectionNetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.NetworkInfo;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.Pool;
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.beans.Subnet;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.utils.MsoCommonUtils;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.MsoHeatUtilsWithUpdate;
import org.onap.so.openstack.utils.MsoNeutronUtils;
import org.onap.so.openstack.utils.MsoNeutronUtils.NetworkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Transactional
@WebService(serviceName = "NetworkAdapter", endpointInterface = "org.onap.so.adapters.network.MsoNetworkAdapter",
        targetNamespace = "http://org.onap.so/network")
public class MsoNetworkAdapterImpl {

    private static final String OS3_NW_PROPERTY = "org.onap.so.adapters.network.aic3nw";
    private static final String OS3_NW = "OS::ContrailV2::VirtualNetwork";
    private static final String VLANS = "vlans";
    private static final String PHYSICAL_NETWORK = "physical_network";
    private static final String UPDATE_NETWORK_CONTEXT = "UpdateNetwork";
    private static final String CREATE_NETWORK_CONTEXT = "CreateNetwork";
    private static final String NEUTRON_MODE = "NEUTRON";
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String LOG_DEBUG_MSG = "Got Network definition from Catalog: {}";
    private static final String NETWORK_EXIST_STATUS_MESSAGE =
            "The network was found to already exist, thus no new network was created in the cloud via this request";
    private static final String NETWORK_CREATED_STATUS_MESSAGE =
            "The new network was successfully created in the cloud";
    private static final String NETWORK_NOT_EXIST_STATUS_MESSAGE =
            "The network was not found, thus no network was deleted in the cloud via this request";
    private static final String NETWORK_DELETED_STATUS_MESSAGE = "The network was successfully deleted in the cloud";

    private static final Logger logger = LoggerFactory.getLogger(MsoNetworkAdapterImpl.class);

    @Autowired
    private CloudConfig cloudConfig;
    @Autowired
    private Environment environment;
    @Autowired
    private MsoNeutronUtils neutron;
    @Autowired
    private MsoHeatUtils heat;
    @Autowired
    private MsoHeatUtilsWithUpdate heatWithUpdate;
    @Autowired
    private MsoCommonUtils commonUtils;

    @Autowired
    private NetworkResourceCustomizationRepository networkCustomRepo;

    @Autowired
    private CollectionNetworkResourceCustomizationRepository collectionNetworkCustomRepo;

    @Autowired
    private NetworkResourceRepository networkResourceRepo;

    public MsoNetworkAdapterImpl() {}

    /**
     * This is the "Create Network" web service implementation. It will create a new Network of the requested type in
     * the specified cloud and tenant. The tenant must exist at the time this service is called.
     *
     * If a network with the same name already exists, this can be considered a success or failure, depending on the
     * value of the 'failIfExists' parameter.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog. All such networks will have a
     * similar configuration, based on the allowable Openstack networking definitions. This includes basic networks,
     * provider networks (with a single VLAN), and multi-provider networks (one or more VLANs)
     *
     * Initially, all provider networks must be "vlan" type, and multiple segments in a multi-provider network must be
     * multiple VLANs on the same physical network.
     *
     * This service supports two modes of Network creation/update: - via Heat Templates - via Neutron API The network
     * orchestration mode for each network type is declared in its catalog definition. All Heat-based templates must
     * support some subset of the same input parameters: network_name, physical_network, vlan(s).
     *
     * The method returns the network ID and a NetworkRollback object. This latter object can be passed as-is to the
     * rollbackNetwork operation to undo everything that was created. This is useful if a network is successfully
     * created but the orchestration fails on a subsequent operation.
     */

    public void createNetwork(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkName, String physicalNetworkName, List<Integer> vlans, List<RouteTarget> routeTargets,
            String shared, String external, Boolean failIfExists, Boolean backout, List<Subnet> subnets,
            List<String> policyFqdns, List<String> routeTableFqdns, MsoRequest msoRequest, Holder<String> stackId,
            MutableBoolean isOs3Nw) throws NetworkException {
        logger.debug("*** CREATE Network: {} of type {} in {}/{}", networkName, networkType, cloudSiteId, tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis();

        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOpt.isPresent()) {
            String error = String.format(
                    "Configuration Error. Stack %s in %s/%s: CloudSite does not exist in MSO Configuration",
                    networkName, cloudSiteId, tenantId);
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_CONFIG_EXC, ErrorCode.DataError.getValue(), error);
            // Set the detailed error as the Exception 'message'
            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }


        NetworkResource networkResource = networkCheck(startTime, networkType, modelCustomizationUuid, networkName,
                physicalNetworkName, vlans, routeTargets, cloudSiteId, cloudSiteOpt.get());
        NetworkType neutronNetworkType = NetworkType.valueOf(networkResource.getNeutronNetworkType());

        HeatTemplate heatTemplate = networkResource.getHeatTemplate();
        if (heatTemplate == null) {
            String error = String.format("Network error - undefined Heat Template. Network Type = %s", networkType);
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_PARAM_NOT_FOUND, ErrorCode.DataError.getValue(), error);
            throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
        }

        logger.debug("Got HEAT Template from DB: {}", heatTemplate);

        // "Fix" the template if it has CR/LF (getting this from Oracle)
        String template = heatTemplate.getHeatTemplate();
        template = template.replaceAll("\r\n", "\n");

        boolean os3template = false;

        String os3nw = environment.getProperty(OS3_NW_PROPERTY, OS3_NW);

        if (template.contains(os3nw))
            os3template = true;

        isOs3Nw.setValue(os3template);
        // First, look up to see if the Network already exists (by name).
        // For HEAT orchestration of networks, the stack name will always match the network name
        StackInfo heatStack = null;
        try {
            heatStack = heat.queryStack(cloudSiteId, CLOUD_OWNER, tenantId, networkName);
        } catch (MsoException me) {
            me.addContext(CREATE_NETWORK_CONTEXT);
            logger.error("{} {} Create Network (heat): query network {} in {}/{}: ", MessageEnum.RA_QUERY_NETWORK_EXC,
                    ErrorCode.DataError.getValue(), networkName, cloudSiteId, tenantId, me);
            throw new NetworkException(me);
        }

        if (heatStack != null && (heatStack.getStatus() != HeatStatus.NOTFOUND)) {
            // Stack exists. Return success or error depending on input directive
            if (failIfExists != null && failIfExists) {
                String error = String.format("CreateNetwork: Stack %s already exists in %s/%s as %s", networkName,
                        cloudSiteId, tenantId, heatStack.getCanonicalName());
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_NETWORK_ALREADY_EXIST, ErrorCode.DataError.getValue(),
                        error);
                throw new NetworkException(error, MsoExceptionCategory.USERDATA);
            } else {
                logger.warn("{} {} Found Existing network stack, status={} networkName={} for {}/{}",
                        MessageEnum.RA_NETWORK_ALREADY_EXIST, ErrorCode.DataError.getValue(), heatStack.getStatus(),
                        networkName, cloudSiteId, tenantId);
            }
            heat.updateResourceStatus(msoRequest.getRequestId(), NETWORK_EXIST_STATUS_MESSAGE);
            return;
        }

        // Ready to deploy the new Network
        // Build the common set of HEAT template parameters
        Map<String, Object> stackParams = populateNetworkParams(neutronNetworkType, networkName, physicalNetworkName,
                vlans, routeTargets, shared, external, os3template);

        // Validate (and update) the input parameters against the DB definition
        // Shouldn't happen unless DB config is wrong, since all networks use same inputs
        // and inputs were already validated.
        try {
            stackParams = heat.validateStackParams(stackParams, heatTemplate);
        } catch (IllegalArgumentException e) {
            String error = "Create Network: Configuration Error: " + e.getMessage();
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_CONFIG_EXC, ErrorCode.DataError.getValue(), error, e);
            // Input parameters were not valid
            throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
        }

        if (subnets != null) {
            try {
                if (os3template) {
                    template = mergeSubnetsAIC3(template, subnets, stackParams);
                } else {
                    template = mergeSubnets(template, subnets);
                }
            } catch (MsoException me) {
                me.addContext(CREATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception Create Network, merging subnets for network (heat) type {} in {}/{} ",
                        MessageEnum.RA_CREATE_NETWORK_EXC, ErrorCode.DataError.getValue(),
                        neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }
        }

        if (policyFqdns != null && !policyFqdns.isEmpty() && os3template) {
            try {
                mergePolicyRefs(policyFqdns, stackParams);
            } catch (MsoException me) {
                me.addContext(CREATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception Create Network, merging policyRefs type {} in {}/{} ",
                        MessageEnum.RA_CREATE_NETWORK_EXC, ErrorCode.DataError.getValue(),
                        neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }
        }

        if (routeTableFqdns != null && !routeTableFqdns.isEmpty() && os3template) {
            try {
                mergeRouteTableRefs(routeTableFqdns, stackParams);
            } catch (MsoException me) {
                me.addContext(CREATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception Create Network, merging routeTableRefs type {} in {}/{} ",
                        MessageEnum.RA_CREATE_NETWORK_EXC, ErrorCode.DataError.getValue(),
                        neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }
        }

        // Deploy the network stack
        // Ignore MsoStackAlreadyExists exception because we already checked.
        try {
            if (backout == null)
                backout = true;
            heatStack = heat.createStack(cloudSiteId, CLOUD_OWNER, tenantId, networkName, null, template, stackParams,
                    false, heatTemplate.getTimeoutMinutes(), null, null, null, backout.booleanValue(), failIfExists);
        } catch (MsoException me) {
            me.addContext(CREATE_NETWORK_CONTEXT);
            logger.error("{} {} Exception creating network type {} in {}/{} ", MessageEnum.RA_CREATE_NETWORK_EXC,
                    ErrorCode.DataError.getValue(), networkName, cloudSiteId, tenantId, me);
            throw new NetworkException(me);
        }

        // Reach this point if createStack is successful.
        stackId.value = heatStack.getCanonicalName();

        try {
            heat.updateResourceStatus(msoRequest.getRequestId(), NETWORK_CREATED_STATUS_MESSAGE);
        } catch (Exception e) {
            logger.warn("Exception while updating infra active request", e);
        }

        logger.debug("Network {} successfully created via HEAT", networkName);

        return;
    }

    /**
     * This is the "Update Network" web service implementation. It will update an existing Network of the requested type
     * in the specified cloud and tenant. The typical use will be to replace the VLANs with the supplied list (to add or
     * remove a VLAN), but other properties may be updated as well.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog. All such networks will have a
     * similar configuration, based on the allowable Openstack networking definitions. This includes basic networks,
     * provider networks (with a single VLAN), and multi-provider networks (one or more VLANs).
     *
     * Initially, all provider networks must currently be "vlan" type, and multi-provider networks must be multiple
     * VLANs on the same physical network.
     *
     * This service supports two modes of Network update: - via Heat Templates - via Neutron API The network
     * orchestration mode for each network type is declared in its catalog definition. All Heat-based templates must
     * support some subset of the same input parameters: network_name, physical_network, vlan, segments.
     *
     * The method returns a NetworkRollback object. This object can be passed as-is to the rollbackNetwork operation to
     * undo everything that was updated. This is useful if a network is successfully updated but orchestration fails on
     * a subsequent operation.
     */
    public void updateNetwork(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkId, String networkName, String physicalNetworkName, List<Integer> vlans,
            List<RouteTarget> routeTargets, String shared, String external, List<Subnet> subnets,
            List<String> policyFqdns, List<String> routeTableFqdns, MsoRequest msoRequest, Holder<String> stackId)
            throws NetworkException {

        logger.debug("***UPDATE Network adapter with Network: {} of type {} in {}/{}", networkName, networkType,
                cloudSiteId, tenantId);

        // Will capture execution time for metrics
        long startTime = System.currentTimeMillis();

        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOpt.isPresent()) {
            String error = String.format(
                    "UpdateNetwork: Configuration Error. Stack %s in %s/%s: CloudSite does not exist in MSO Configuration",
                    networkName, cloudSiteId, tenantId);
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_CONFIG_EXC, ErrorCode.DataError.getValue(), error);
            // Set the detailed error as the Exception 'message'
            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }

        NetworkResource networkResource = networkCheck(startTime, networkType, modelCustomizationUuid, networkName,
                physicalNetworkName, vlans, routeTargets, cloudSiteId, cloudSiteOpt.get());
        String mode = networkResource.getOrchestrationMode();
        NetworkType neutronNetworkType = NetworkType.valueOf(networkResource.getNeutronNetworkType());

        // Use an MsoNeutronUtils for all Neutron commands

        if (NEUTRON_MODE.equals(mode)) {

            // Verify that the Network exists
            // For Neutron-based orchestration, the networkId is the Neutron Network UUID.
            NetworkInfo netInfo = null;
            try {
                netInfo = neutron.queryNetwork(networkId, tenantId, cloudSiteId);
            } catch (MsoException me) {
                me.addContext(UPDATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception - queryNetwork query {} in {}/{} ", MessageEnum.RA_QUERY_NETWORK_EXC,
                        ErrorCode.BusinessProcessError.getValue(), networkId, cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }

            if (netInfo == null) {
                String error = String.format("Update Nework: Network %s does not exist in %s/%s", networkId,
                        cloudSiteId, tenantId);
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_NETWORK_NOT_FOUND,
                        ErrorCode.BusinessProcessError.getValue(), error);
                // Does not exist. Throw an exception (can't update a non-existent network)
                throw new NetworkException(error, MsoExceptionCategory.USERDATA);
            }
            try {
                netInfo = neutron.updateNetwork(cloudSiteId, tenantId, networkId, neutronNetworkType,
                        physicalNetworkName, vlans);
            } catch (MsoException me) {
                me.addContext(UPDATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception - updateNetwork {} in {}/{} ", MessageEnum.RA_UPDATE_NETWORK_ERR,
                        ErrorCode.DataError.getValue(), networkId, cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }

            logger.debug("Network {} updated, id = {}", networkId, netInfo.getId());
        } else if ("HEAT".equals(mode)) {

            // First, look up to see that the Network already exists.
            // For Heat-based orchestration, the networkId is the network Stack ID.
            StackInfo heatStack = null;
            try {
                heatStack = heat.queryStack(cloudSiteId, CLOUD_OWNER, tenantId, networkName);
            } catch (MsoException me) {
                me.addContext(UPDATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception - QueryStack query {} in {}/{} ", MessageEnum.RA_QUERY_NETWORK_EXC,
                        ErrorCode.DataError.getValue(), networkId, cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }

            if (heatStack == null || (heatStack.getStatus() == HeatStatus.NOTFOUND)) {
                String error = String.format("UpdateNetwork: Stack %s does not exist in %s/%s", networkName,
                        cloudSiteId, tenantId);
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_NETWORK_NOT_FOUND, ErrorCode.DataError.getValue(),
                        error);
                // Network stack does not exist. Return an error
                throw new NetworkException(error, MsoExceptionCategory.USERDATA);
            }

            // Get the previous parameters for rollback
            Map<String, Object> heatParams = heatStack.getParameters();

            List<Integer> previousVlans = new ArrayList<>();
            String vlansParam = (String) heatParams.get(VLANS);
            if (vlansParam != null) {
                for (String vlan : vlansParam.split(",")) {
                    try {
                        previousVlans.add(Integer.parseInt(vlan));
                    } catch (NumberFormatException e) {
                        logger.warn("{} {} Exception - VLAN parse for params {} ", MessageEnum.RA_VLAN_PARSE,
                                ErrorCode.DataError.getValue(), vlansParam, e);
                    }
                }
            }
            logger.debug("Update Stack:  Previous VLANS: {}", previousVlans);

            // Ready to deploy the updated Network via Heat
            HeatTemplate heatTemplate = networkResource.getHeatTemplate();
            if (heatTemplate == null) {
                String error = "Network error - undefined Heat Template. Network Type=" + networkType;
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_PARAM_NOT_FOUND, ErrorCode.DataError.getValue(),
                        error);
                throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
            }

            logger.debug("Got HEAT Template from DB: {}", heatTemplate);

            // "Fix" the template if it has CR/LF (getting this from Oracle)
            String template = heatTemplate.getHeatTemplate();
            template = template.replaceAll("\r\n", "\n");

            boolean os3template = false;
            String os3nw = OS3_NW;

            os3nw = environment.getProperty(OS3_NW_PROPERTY, OS3_NW);

            if (template.contains(os3nw))
                os3template = true;

            // Build the common set of HEAT template parameters
            Map<String, Object> stackParams = populateNetworkParams(neutronNetworkType, networkName,
                    physicalNetworkName, vlans, routeTargets, shared, external, os3template);

            // Validate (and update) the input parameters against the DB definition
            // Shouldn't happen unless DB config is wrong, since all networks use same inputs
            try {
                stackParams = heat.validateStackParams(stackParams, heatTemplate);
            } catch (IllegalArgumentException e) {
                String error = "UpdateNetwork: Configuration Error: Network Type=" + networkType;
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_CONFIG_EXC, ErrorCode.DataError.getValue(), error);
                throw new NetworkException(error, MsoExceptionCategory.INTERNAL, e);
            }

            if (subnets != null) {
                try {
                    if (os3template) {
                        template = mergeSubnetsAIC3(template, subnets, stackParams);
                    } else {
                        template = mergeSubnets(template, subnets);
                    }
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    logger.error("{} {} Exception - UpdateNetwork mergeSubnets for network type {} in {}/{} ",
                            MessageEnum.RA_UPDATE_NETWORK_ERR, ErrorCode.DataError.getValue(),
                            neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                    throw new NetworkException(me);
                }
            }

            if (policyFqdns != null && os3template) {
                try {
                    mergePolicyRefs(policyFqdns, stackParams);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    logger.error("{} {} Exception - UpdateNetwork mergePolicyRefs type {} in {}/{} ",
                            MessageEnum.RA_UPDATE_NETWORK_ERR, ErrorCode.DataError.getValue(),
                            neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                    throw new NetworkException(me);
                }
            }

            if (routeTableFqdns != null && !routeTableFqdns.isEmpty() && os3template) {
                try {
                    mergeRouteTableRefs(routeTableFqdns, stackParams);
                } catch (MsoException me) {
                    me.addContext(UPDATE_NETWORK_CONTEXT);
                    logger.error("{} {} Exception - UpdateNetwork mergeRouteTableRefs type {} in {}/{} ",
                            MessageEnum.RA_UPDATE_NETWORK_ERR, ErrorCode.DataError.getValue(),
                            neutronNetworkType.toString(), cloudSiteId, tenantId, me);
                    throw new NetworkException(me);
                }
            }

            // Update the network stack
            // Ignore MsoStackNotFound exception because we already checked.
            try {
                heatStack = heatWithUpdate.updateStack(cloudSiteId, CLOUD_OWNER, tenantId, networkId, template,
                        stackParams, false, heatTemplate.getTimeoutMinutes());
            } catch (MsoException me) {
                me.addContext(UPDATE_NETWORK_CONTEXT);
                logger.error("{} {} Exception - update network {} in {}/{} ", MessageEnum.RA_UPDATE_NETWORK_ERR,
                        ErrorCode.DataError.getValue(), networkId, cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }

            stackId.value = heatStack.getCanonicalName();

            logger.debug("Network {} successfully updated via HEAT", networkId);
        }

        return;
    }

    private NetworkResource networkCheck(long startTime, String networkType, String modelCustomizationUuid,
            String networkName, String physicalNetworkName, List<Integer> vlans, List<RouteTarget> routeTargets,
            String cloudSiteId, CloudSite cloudSite) throws NetworkException {
        // Retrieve the Network Resource definition
        NetworkResource networkResource = null;
        NetworkResourceCustomization networkCust = null;
        CollectionNetworkResourceCustomization collectionNetworkCust = null;
        if (commonUtils.isNullOrEmpty(modelCustomizationUuid)) {
            if (!commonUtils.isNullOrEmpty(networkType)) {
                networkResource = networkResourceRepo.findFirstByModelNameOrderByModelVersionDesc(networkType);
            }
        } else {
            networkCust = networkCustomRepo.findOneByModelCustomizationUUID(modelCustomizationUuid);
            if (networkCust == null) {
                collectionNetworkCust =
                        collectionNetworkCustomRepo.findOneByModelCustomizationUUID(modelCustomizationUuid);
            }
        }
        if (networkCust != null) {
            logger.debug("Got Network Customization definition from Catalog: {}", networkCust);

            networkResource = networkCust.getNetworkResource();
        } else if (collectionNetworkCust != null) {
            logger.debug("Retrieved Collection Network Resource Customization from Catalog: {}", collectionNetworkCust);
            networkResource = collectionNetworkCust.getNetworkResource();
        }
        if (networkResource == null) {
            String error = String.format(
                    "Create/UpdateNetwork: Unable to get network resource with NetworkType: %s or ModelCustomizationUUID:%s",
                    networkType, modelCustomizationUuid);
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_UNKOWN_PARAM, ErrorCode.DataError.getValue(), error);

            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }
        logger.debug(LOG_DEBUG_MSG, networkResource);

        String mode = networkResource.getOrchestrationMode();
        NetworkType neutronNetworkType = NetworkType.valueOf(networkResource.getNeutronNetworkType());

        // All Networks are orchestrated via HEAT or Neutron
        if (!("HEAT".equals(mode) || NEUTRON_MODE.equals(mode))) {
            String error = "CreateNetwork: Configuration Error: Network Type = " + networkType;
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_NETWORK_ORCHE_MODE_NOT_SUPPORT,
                    ErrorCode.DataError.getValue(), error);
            throw new NetworkException(error, MsoExceptionCategory.INTERNAL);
        }

        MavenLikeVersioning osV = new MavenLikeVersioning();
        osV.setVersion(cloudSite.getCloudVersion());
        if ((osV.isMoreRecentThan(networkResource.getAicVersionMin())
                || osV.isTheSameVersion(networkResource.getAicVersionMin())) // os
                // >=
                // min
                && (osV.isTheSameVersion(networkResource.getAicVersionMax())
                        || !(osV.isMoreRecentThan(networkResource.getAicVersionMax())))) // os <= max
        {
            logger.debug("Network Type:{} VersionMin:{} VersionMax:{} supported on Cloud:{} with AIC_Version:{}",
                    networkType, networkResource.getAicVersionMin(), networkResource.getAicVersionMax(), cloudSiteId,
                    cloudSite.getCloudVersion());
        } else {
            String error = String.format(
                    "Network Type:%s Version_Min:%s Version_Max:%s not supported on Cloud:%s with AIC_Version:%s",
                    networkType, networkResource.getAicVersionMin(), networkResource.getAicVersionMax(), cloudSiteId,
                    cloudSite.getCloudVersion());
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_CONFIG_EXC, ErrorCode.DataError.getValue(), error);
            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }

        // Validate the Network parameters.
        String missing =
                validateNetworkParams(neutronNetworkType, networkName, physicalNetworkName, vlans, routeTargets);
        if (!missing.isEmpty()) {
            String error = "Create Network: Missing parameters: " + missing;
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MISSING_PARAM, ErrorCode.DataError.getValue(), error);

            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }

        return networkResource;
    }

    /**
     * This is the "Delete Network" web service implementation. It will delete a Network in the specified cloud and
     * tenant.
     *
     * If the network is not found, it is treated as a success.
     *
     * This service supports two modes of Network creation/update/delete: - via Heat Templates - via Neutron API The
     * network orchestration mode for each network type is declared in its catalog definition.
     *
     * For Heat-based orchestration, the networkId should be the stack ID. For Neutron-based orchestration, the
     * networkId should be the Neutron network UUID.
     *
     * The method returns nothing on success. Rollback is not possible for delete commands, so any failure on delete
     * will require manual fallout in the client.
     */
    public void deleteNetwork(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkId, MsoRequest msoRequest) throws NetworkException {
        logger.debug("*** DELETE Network adapter with Network: {} in {}/{}", networkId, cloudSiteId, tenantId);
        if (commonUtils.isNullOrEmpty(cloudSiteId) || commonUtils.isNullOrEmpty(tenantId)
                || commonUtils.isNullOrEmpty(networkId)) {
            String error = "Missing mandatory parameter cloudSiteId, tenantId or networkId";
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MISSING_PARAM, ErrorCode.DataError.getValue(), error);
            throw new NetworkException(error, MsoExceptionCategory.USERDATA);
        }

        int timeoutMinutes = heat.getNetworkHeatTimeoutValue(modelCustomizationUuid, networkType);

        boolean networkDeleted = false;
        try {
            StackInfo stack = heat.deleteStack(tenantId, CLOUD_OWNER, cloudSiteId, networkId, false, timeoutMinutes);
            networkDeleted = stack.isOperationPerformed();
        } catch (MsoException me) {
            me.addContext("DeleteNetwork");
            logger.error("{} {} Delete Network (heat): {} in {}/{} ", MessageEnum.RA_DELETE_NETWORK_EXC,
                    ErrorCode.DataError.getValue(), networkId, cloudSiteId, tenantId, me);
            throw new NetworkException(me);
        }

        try {
            heat.updateResourceStatus(msoRequest.getRequestId(),
                    networkDeleted ? NETWORK_DELETED_STATUS_MESSAGE : NETWORK_NOT_EXIST_STATUS_MESSAGE);
        } catch (Exception e) {
            logger.warn("Exception while updating infra active request", e);
        }
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation. A rollback object is returned to the
     * client in a successful creation response. The client can pass that object as-is back to the rollbackVnf operation
     * to undo the creation.
     *
     * The rollback includes removing the VNF and deleting the tenant if the tenant did not exist prior to the VNF
     * creation.
     */
    public void rollbackNetwork(NetworkRollback rollback) throws NetworkException {
        if (rollback == null) {
            logger.error("{} {} rollback is null", MessageEnum.RA_ROLLBACK_NULL, ErrorCode.DataError.getValue());
            return;
        }

        // Get the elements of the VnfRollback object for easier access
        String cloudSiteId = rollback.getCloudId();
        String tenantId = rollback.getTenantId();
        String networkId = rollback.getNetworkStackId();

        logger.debug("*** ROLLBACK Network {} in {}/{}", networkId, cloudSiteId, tenantId);

        if (rollback.getNetworkCreated()) {
            try {
                heat.deleteStack(tenantId, CLOUD_OWNER, cloudSiteId, networkId, false, 120);
            } catch (MsoException me) {
                me.addContext("RollbackNetwork");
                logger.error("{} {} Exception - Rollback Network (heat): {} in {}/{} ",
                        MessageEnum.RA_DELETE_NETWORK_EXC, ErrorCode.BusinessProcessError.getValue(), networkId,
                        cloudSiteId, tenantId, me);
                throw new NetworkException(me);
            }

        }
    }

    private String validateNetworkParams(NetworkType neutronNetworkType, String networkName, String physicalNetwork,
            List<Integer> vlans, List<RouteTarget> routeTargets) {
        String sep = "";
        StringBuilder missing = new StringBuilder();
        if (commonUtils.isNullOrEmpty(networkName)) {
            missing.append("networkName");
            sep = ",";
        }

        if (neutronNetworkType == NetworkType.PROVIDER || neutronNetworkType == NetworkType.MULTI_PROVIDER) {
            if (commonUtils.isNullOrEmpty(physicalNetwork)) {
                missing.append(sep).append("physicalNetworkName");
                sep = ",";
            }
            if (vlans == null || vlans.isEmpty()) {
                missing.append(sep).append(VLANS);
            }
        }

        return missing.toString();
    }

    private Map<String, Object> populateNetworkParams(NetworkType neutronNetworkType, String networkName,
            String physicalNetwork, List<Integer> vlans, List<RouteTarget> routeTargets, String shared, String external,
            boolean os3template) {
        // Build the common set of HEAT template parameters
        Map<String, Object> stackParams = new HashMap<>();
        stackParams.put("network_name", networkName);

        if (neutronNetworkType == NetworkType.PROVIDER) {
            // For Provider type
            stackParams.put(PHYSICAL_NETWORK, physicalNetwork);
            stackParams.put("vlan", vlans.get(0).toString());
        } else if (neutronNetworkType == NetworkType.MULTI_PROVIDER) {
            // For Multi-provider, PO supports a custom resource extension of ProviderNet.
            // It supports all ProviderNet properties except segmentation_id, and adds a
            // comma-separated-list of VLANs as a "segments" property.
            // Note that this does not match the Neutron definition of Multi-Provider network,
            // which contains a list of 'segments', each having physical_network, network_type,
            // and segmentation_id.
            StringBuilder buf = new StringBuilder();
            String sep = "";
            for (Integer vlan : vlans) {
                buf.append(sep).append(vlan.toString());
                sep = ",";
            }
            String csl = buf.toString();

            stackParams.put(PHYSICAL_NETWORK, physicalNetwork);
            stackParams.put(VLANS, csl);
        }
        if (routeTargets != null) {

            String rtGlobal = "";
            String rtImport = "";
            String rtExport = "";
            String sep = "";
            for (RouteTarget rt : routeTargets) {
                boolean rtIsNull = false;
                if (rt != null) {
                    String routeTarget = rt.getRouteTarget();
                    String routeTargetRole = rt.getRouteTargetRole();
                    logger.debug("Checking for an actually null route target: {}", rt);
                    if (routeTarget == null || routeTarget.equals("") || routeTarget.equalsIgnoreCase("null"))
                        rtIsNull = true;
                    if (routeTargetRole == null || routeTargetRole.equals("")
                            || routeTargetRole.equalsIgnoreCase("null"))
                        rtIsNull = true;
                } else {
                    rtIsNull = true;
                }
                if (!rtIsNull) {
                    logger.debug("Input RT:{}", rt);
                    String role = rt.getRouteTargetRole();
                    String rtValue = rt.getRouteTarget();

                    if ("IMPORT".equalsIgnoreCase(role)) {
                        sep = rtImport.isEmpty() ? "" : ",";
                        rtImport = os3template ? rtImport + sep + "target:" + rtValue : rtImport + sep + rtValue;
                    } else if ("EXPORT".equalsIgnoreCase(role)) {
                        sep = rtExport.isEmpty() ? "" : ",";
                        rtExport = os3template ? rtExport + sep + "target:" + rtValue : rtExport + sep + rtValue;
                    } else // covers BOTH, empty etc
                    {
                        sep = rtGlobal.isEmpty() ? "" : ",";
                        rtGlobal = os3template ? rtGlobal + sep + "target:" + rtValue : rtGlobal + sep + rtValue;
                    }

                }
            }

            if (!rtImport.isEmpty()) {
                stackParams.put("route_targets_import", rtImport);
            }
            if (!rtExport.isEmpty()) {
                stackParams.put("route_targets_export", rtExport);
            }
            if (!rtGlobal.isEmpty()) {
                stackParams.put("route_targets", rtGlobal);
            }
        }
        if (commonUtils.isNullOrEmpty(shared)) {
            stackParams.put("shared", "False");
        } else {
            stackParams.put("shared", shared);
        }
        if (commonUtils.isNullOrEmpty(external)) {
            stackParams.put("external", "False");
        } else {
            stackParams.put("external", external);
        }
        return stackParams;
    }



    /**
     * policyRef_list structure in stackParams [ { "network_policy_refs_data_sequence": {
     * "network_policy_refs_data_sequence_major": "1", "network_policy_refs_data_sequence_minor": "0" } }, {
     * "network_policy_refs_data_sequence": { "network_policy_refs_data_sequence_major": "2",
     * "network_policy_refs_data_sequence_minor": "0" } } ]
     **/
    private void mergePolicyRefs(List<String> pFqdns, Map<String, Object> stackParams) throws MsoException {
        // Resource Property
        List<ContrailPolicyRef> prlist = new ArrayList<>();
        int index = 1;

        if (pFqdns != null) {
            for (String pf : pFqdns) {
                if (!commonUtils.isNullOrEmpty(pf)) {
                    ContrailPolicyRef pr = new ContrailPolicyRef();
                    ContrailPolicyRefSeq refSeq = new ContrailPolicyRefSeq(String.valueOf(index), "0");
                    pr.setSeq(refSeq);
                    index++;
                    logger.debug("Contrail PolicyRefs Data:{}", pr);
                    prlist.add(pr);
                }
            }
        } else {
            String error = "Null pFqdns at start of mergePolicyRefs";
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MARSHING_ERROR, ErrorCode.BusinessProcessError.getValue(),
                    error);
            throw new MsoAdapterException(error);
        }

        JsonNode node = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            node = mapper.convertValue(prlist, JsonNode.class);
            String jsonString = mapper.writeValueAsString(prlist);
            logger.debug("Json PolicyRefs Data:{}", jsonString);
        } catch (Exception e) {
            String error = "Error creating JsonNode for policyRefs Data";
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MARSHING_ERROR, ErrorCode.BusinessProcessError.getValue(),
                    error, e);
            throw new MsoAdapterException(error);
        }
        // update parameters
        if (pFqdns != null && node != null) {
            StringBuilder buf = new StringBuilder();
            String sep = "";
            for (String pf : pFqdns) {
                if (!commonUtils.isNullOrEmpty(pf)) {
                    buf.append(sep).append(pf);
                    sep = ",";
                }
            }
            String csl = buf.toString();
            stackParams.put("policy_refs", csl);
            stackParams.put("policy_refsdata", node);
        }

        logger.debug("StackParams updated with policy refs");
        return;
    }

    private void mergeRouteTableRefs(List<String> rtFqdns, Map<String, Object> stackParams) throws MsoException {

        // update parameters
        if (rtFqdns != null) {
            StringBuilder buf = new StringBuilder();
            String sep = "";
            for (String rtf : rtFqdns) {
                if (!commonUtils.isNullOrEmpty(rtf)) {
                    buf.append(sep).append(rtf);
                    sep = ",";
                }
            }
            String csl = buf.toString();
            stackParams.put("route_table_refs", csl);
        }

        logger.debug("StackParams updated with route_table refs");
        return;
    }


    /***
     * Subnet Output structure from Juniper { "ipam_subnets": [ { "subnet": { "ip_prefix": "10.100.1.0",
     * "ip_prefix_len": 28 }, "addr_from_start": null, "enable_dhcp": false, "default_gateway": "10.100.1.1",
     * "dns_nameservers": [], "dhcp_option_list": null, "subnet_uuid": "10391fbf-6b9c-4160-825d-2d018b7649cf",
     * "allocation_pools": [ { "start": "10.100.1.3", "end": "10.100.1.5" }, { "start": "10.100.1.6", "end":
     * "10.100.1.9" } ], "host_routes": null, "dns_server_address": "10.100.1.13", "subnet_name":
     * "subnet_MsoNW1_692c9032-e1a2-4d64-828c-7b9a4fcc05b0" }, { "subnet": { "ip_prefix": "10.100.2.16",
     * "ip_prefix_len": 28 }, "addr_from_start": null, "enable_dhcp": true, "default_gateway": "10.100.2.17",
     * "dns_nameservers": [], "dhcp_option_list": null, "subnet_uuid": "c7aac5ea-66fe-443a-85f9-9c38a608c0f6",
     * "allocation_pools": [ { "start": "10.100.2.18", "end": "10.100.2.20" } ], "host_routes": null,
     * "dns_server_address": "10.100.2.29", "subnet_name": "subnet_MsoNW1_692c9032-e1a2-4d64-828c-7b9a4fcc05b1" } ],
     * "host_routes": null }
     ***/
    private String mergeSubnetsAIC3(String heatTemplate, List<Subnet> subnets, Map<String, Object> stackParams)
            throws MsoException {

        // Resource Property
        List<ContrailSubnet> cslist = new ArrayList<>();
        for (Subnet subnet : subnets) {
            logger.debug("Input Subnet:{}", subnet);
            ContrailSubnet cs = new ContrailSubnetMapper(subnet).map();
            logger.debug("Contrail Subnet:{}", cs);
            cslist.add(cs);
        }

        JsonNode node = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            node = mapper.convertValue(cslist, JsonNode.class);
            String jsonString = mapper.writeValueAsString(cslist);
            logger.debug("Json Subnet List:{}", jsonString);
        } catch (Exception e) {
            String error = "Error creating JsonNode from input subnets";
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MARSHING_ERROR, ErrorCode.DataError.getValue(), error, e);
            throw new MsoAdapterException(error);
        }
        // update parameters
        if (node != null) {
            stackParams.put("subnet_list", node);
        }
        // Outputs - All subnets are in one ipam_subnets structure
        String outputTempl = "  subnet:\n" + "    description: Openstack subnet identifier\n"
                + "    value: { get_attr: [network, network_ipam_refs, 0, attr]}\n";

        // append outputs in heatTemplate
        int outputsIdx = heatTemplate.indexOf("outputs:");
        heatTemplate = insertStr(heatTemplate, outputTempl, outputsIdx + 8);
        logger.debug("Template updated with all AIC3.0 subnets:{}", heatTemplate);
        return heatTemplate;
    }


    private String mergeSubnets(String heatTemplate, List<Subnet> subnets) throws MsoException {

        String resourceTempl = "  subnet_%subnetId%:\n" + "    type: OS::Neutron::Subnet\n" + "    properties:\n"
                + "      name: %name%\n" + "      network_id: { get_resource: network }\n" + "      cidr: %cidr%\n";

        /*
         * make these optional + "      ip_version: %ipversion%\n" + "      enable_dhcp: %enabledhcp%\n" +
         * "      gateway_ip: %gatewayip%\n" + "      allocation_pools:\n" + "       - start: %poolstart%\n" +
         * "         end: %poolend%\n";
         *
         */

        String outputTempl = "  subnet_id_%subnetId%:\n" + "    description: Openstack subnet identifier\n"
                + "    value: {get_resource: subnet_%subnetId%}\n";

        String curR;
        String curO;
        StringBuilder resourcesBuf = new StringBuilder();
        StringBuilder outputsBuf = new StringBuilder();
        for (Subnet subnet : subnets) {

            // build template for each subnet
            curR = resourceTempl;
            if (subnet.getSubnetId() != null) {
                curR = curR.replace("%subnetId%", subnet.getSubnetId());
            } else {
                String error = "Missing Required AAI SubnetId for subnet in HEAT Template";
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_MISSING_PARAM, ErrorCode.DataError.getValue(), error);
                throw new MsoAdapterException(error);
            }

            if (subnet.getSubnetName() != null) {
                curR = curR.replace("%name%", subnet.getSubnetName());
            } else {
                curR = curR.replace("%name%", subnet.getSubnetId());
            }

            if (subnet.getCidr() != null) {
                curR = curR.replace("%cidr%", subnet.getCidr());
            } else {
                String error = "Missing Required cidr for subnet in HEAT Template";
                logger.error(LoggingAnchor.THREE, MessageEnum.RA_MISSING_PARAM, ErrorCode.DataError.getValue(), error);
                throw new MsoAdapterException(error);
            }

            if (subnet.getIpVersion() != null) {
                curR = curR + "      ip_version: " + subnet.getIpVersion() + "\n";
            }
            if (subnet.getEnableDHCP() != null) {
                curR = curR + "      enable_dhcp: " + Boolean.toString(subnet.getEnableDHCP()) + "\n";
            }
            if (subnet.getGatewayIp() != null && !subnet.getGatewayIp().isEmpty()) {
                curR = curR + "      gateway_ip: " + subnet.getGatewayIp() + "\n";
            }

            if (subnet.getAllocationPools() != null) {
                StringBuilder tempBuf = new StringBuilder();
                tempBuf.append(curR);
                tempBuf.append("      allocation_pools:\n");
                for (Pool pool : subnet.getAllocationPools()) {
                    if (!commonUtils.isNullOrEmpty(pool.getStart()) && !commonUtils.isNullOrEmpty(pool.getEnd())) {
                        tempBuf.append("       - start: ");
                        tempBuf.append(pool.getStart());
                        tempBuf.append("\n         end: ");
                        tempBuf.append(pool.getEnd());
                        tempBuf.append("\n");
                    }
                }
                curR = tempBuf.toString();
            }

            resourcesBuf.append(curR);

            curO = outputTempl;
            curO = curO.replace("%subnetId%", subnet.getSubnetId());

            outputsBuf.append(curO);
        }
        // append resources and outputs in heatTemplate
        logger.debug("Tempate initial:{}", heatTemplate);
        int outputsIdx = heatTemplate.indexOf("outputs:");
        heatTemplate = insertStr(heatTemplate, outputsBuf.toString(), outputsIdx + 8);
        int resourcesIdx = heatTemplate.indexOf("resources:");
        heatTemplate = insertStr(heatTemplate, resourcesBuf.toString(), resourcesIdx + 10);

        logger.debug("Template updated with all subnets:{}", heatTemplate);
        return heatTemplate;
    }

    public Map<String, String> getSubnetUUId(String key, Map<String, Object> outputs, List<Subnet> subnets) {

        Map<String, String> sMap = new HashMap<>();

        try {
            Object obj = outputs.get(key);
            ObjectMapper mapper = new ObjectMapper();
            String jStr = mapper.writeValueAsString(obj);
            logger.debug("Subnet_Ipam Output JSON String:{} {}", obj.getClass(), jStr);

            JsonNode rootNode = mapper.readTree(jStr);
            if (rootNode != null) {
                for (JsonNode sNode : rootNode.path("ipam_subnets")) {
                    logger.debug("Output Subnet Node {}", sNode);
                    String name = sNode.path("subnet_name").textValue();
                    String uuid = sNode.path("subnet_uuid").textValue();
                    String aaiId = name; // default
                    // try to find aaiId for name in input subnetList
                    if (subnets != null) {
                        for (Subnet subnet : subnets) {
                            if (subnet != null && !commonUtils.isNullOrEmpty(subnet.getSubnetName())
                                    && subnet.getSubnetName().equals(name)) {
                                aaiId = subnet.getSubnetId();
                                break;
                            }
                        }
                    }
                    sMap.put(aaiId, uuid); // bpmn needs aaid to uuid map
                }
            } else {
                logger.error("{} {} null rootNode - cannot get subnet-uuids", MessageEnum.RA_MARSHING_ERROR,
                        ErrorCode.DataError.getValue());
            }
        } catch (Exception e) {
            logger.error("{} {} Exception getting subnet-uuids ", MessageEnum.RA_MARSHING_ERROR,
                    ErrorCode.DataError.getValue(), e);
        }

        logger.debug("Return sMap {}", sMap);
        return sMap;
    }

    private static String insertStr(String template, String snippet, int index) {

        String updatedTemplate;

        logger.debug("Index:{} Snippet:{}", index, snippet);

        String templateBeg = template.substring(0, index);
        String templateEnd = template.substring(index);

        updatedTemplate = templateBeg + "\n" + snippet + templateEnd;

        logger.debug("Template updated with a subnet:{}", updatedTemplate);
        return updatedTemplate;
    }

    public Map<String, String> buildSubnetMap(Map<String, Object> outputs, List<Subnet> subnets, boolean os3template) {

        Map<String, String> sMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.startsWith("subnet")) {
                if (os3template) // one subnet_id output
                {
                    Map<String, String> map = getSubnetUUId(key, outputs, subnets);
                    sMap.putAll(map);
                } else // multiples subnet_%aaid% outputs
                {
                    String subnetUUId = (String) outputs.get(key);
                    sMap.put(key.substring("subnet_id_".length()), subnetUUId);
                }
            }
        }
        return sMap;
    }

}
