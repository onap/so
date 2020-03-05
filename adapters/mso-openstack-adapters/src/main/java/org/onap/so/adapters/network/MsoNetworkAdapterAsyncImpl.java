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


import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jws.WebService;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;
import org.onap.so.adapters.network.async.client.CreateNetworkNotification;
import org.onap.so.adapters.network.async.client.MsoExceptionCategory;
import org.onap.so.adapters.network.async.client.NetworkAdapterNotify;
import org.onap.so.adapters.network.async.client.NetworkAdapterNotify_Service;
import org.onap.so.adapters.network.async.client.QueryNetworkNotification;
import org.onap.so.adapters.network.async.client.UpdateNetworkNotification;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.entity.MsoRequest;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.NetworkStatus;
import org.onap.so.openstack.beans.Subnet;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@WebService(serviceName = "NetworkAdapterAsync",
        endpointInterface = "org.onap.so.adapters.network.MsoNetworkAdapterAsync",
        targetNamespace = "http://org.onap.so/networkA")
public class MsoNetworkAdapterAsyncImpl implements MsoNetworkAdapterAsync {

    private static final Logger logger = LoggerFactory.getLogger(MsoNetworkAdapterAsyncImpl.class);

    private static final String BPEL_AUTH_PROP = "org.onap.so.adapters.network.bpelauth";
    private static final String ENCRYPTION_KEY_PROP = "mso.msoKey";
    private static final String NETWORK_EXCEPTION_MSG = "Got a NetworkException on createNetwork: ";
    private static final String CREATE_NETWORK_ERROR_LOGMSG = "{} {} Error sending createNetwork notification {} ";
    private static final String FAULT_INFO_ERROR_LOGMSG = "{} {} Exception - fault info ";
    private static final String SHARED = "shared";
    private static final String EXTERNAL = "external";

    @Autowired
    private Environment environment;

    @Autowired
    private MsoNetworkAdapter networkAdapter;

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheckA() {
        logger.debug("Health check call in Network Adapter");
    }

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
    @Override
    public void createNetworkA(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkName, String physicalNetworkName, List<Integer> vlans, Boolean failIfExists, Boolean backout,
            List<Subnet> subnets, Map<String, String> networkParams, String messageId, MsoRequest msoRequest,
            String notificationUrl) {

        logger.debug("Async Create Network: {} of type {} in {}/{}", networkName, networkType, cloudSiteId, tenantId);

        // Use the synchronous method to perform the actual Create


        // Synchronous Web Service Outputs
        Holder<String> networkId = new Holder<>();
        Holder<String> neutronNetworkId = new Holder<>();
        Holder<NetworkRollback> networkRollback = new Holder<>();
        Holder<Map<String, String>> subnetIdMap = new Holder<>();

        HashMap<String, String> params = (HashMap<String, String>) networkParams;
        if (params == null)
            params = new HashMap<>();
        String shared = null;
        String external = null;
        if (params.containsKey(SHARED))
            shared = params.get(SHARED);
        if (params.containsKey(EXTERNAL))
            external = params.get(EXTERNAL);

        try {
            networkAdapter.createNetwork(cloudSiteId, tenantId, networkType, modelCustomizationUuid, networkName,
                    physicalNetworkName, vlans, shared, external, failIfExists, backout, subnets, params, msoRequest,
                    networkId, neutronNetworkId, subnetIdMap, networkRollback);
        } catch (NetworkException e) {
            logger.debug(NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = MsoExceptionCategory.fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error(FAULT_INFO_ERROR_LOGMSG, MessageEnum.RA_FAULT_INFO_EXC, ErrorCode.DataError.getValue(),
                        e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.createNetworkNotification(messageId, false, exCat, eMsg, null, null, null, null);
            } catch (Exception e1) {
                logger.error(CREATE_NETWORK_ERROR_LOGMSG, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                        ErrorCode.DataError.getValue(), e1.getMessage(), e1);

            }
            return;
        }
        logger.debug("Async Create Network:Name {} physicalNetworkName:{}", networkName, physicalNetworkName);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.createNetworkNotification(messageId, true, null, null, networkId.value, neutronNetworkId.value,
                    copyCreateSubnetIdMap(subnetIdMap), copyNrb(networkRollback));
        } catch (Exception e) {
            logger.error(CREATE_NETWORK_ERROR_LOGMSG, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                    ErrorCode.DataError.getValue(), e.getMessage(), e);

        }
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
    @Override
    public void updateNetworkA(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkId, String networkName, String physicalNetworkName, List<Integer> vlans, List<Subnet> subnets,
            Map<String, String> networkParams, String messageId, MsoRequest msoRequest, String notificationUrl) {

        logger.debug("Async Update Network: {} of type {} in {}/{}", networkId, networkType, cloudSiteId, tenantId);

        // Use the synchronous method to perform the actual Create


        // Synchronous Web Service Outputs
        Holder<NetworkRollback> networkRollback = new Holder<>();
        Holder<Map<String, String>> subnetIdMap = new Holder<>();

        HashMap<String, String> params = (HashMap<String, String>) networkParams;
        if (params == null)
            params = new HashMap<>();
        String shared = null;
        String external = null;
        if (params.containsKey(SHARED))
            shared = params.get(SHARED);
        if (params.containsKey(EXTERNAL))
            external = params.get(EXTERNAL);

        try {
            networkAdapter.updateNetwork(cloudSiteId, tenantId, networkType, modelCustomizationUuid, networkId,
                    networkName, physicalNetworkName, vlans, shared, external, subnets, params, msoRequest, subnetIdMap,
                    networkRollback);
        } catch (NetworkException e) {
            logger.debug("Got a NetworkException on updateNetwork: ", e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = MsoExceptionCategory.fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error(FAULT_INFO_ERROR_LOGMSG, MessageEnum.RA_FAULT_INFO_EXC, ErrorCode.DataError.getValue(),
                        e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.updateNetworkNotification(messageId, false, exCat, eMsg, null, copyNrb(networkRollback));
            } catch (Exception e1) {
                logger.error("{} {} Error sending updateNetwork notification {} ",
                        MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, ErrorCode.DataError.getValue(), e1.getMessage(), e1);

            }
            return;
        }
        logger.debug("Async Update Network:Name {} NetworkId:{}", networkName, networkId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.updateNetworkNotification(messageId, true, null, null, copyUpdateSubnetIdMap(subnetIdMap),
                    copyNrb(networkRollback));
        } catch (Exception e) {
            logger.error("{} {} Error sending updateNotification request {} ", MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                    ErrorCode.DataError.getValue(), e.getMessage(), e);
        }
        return;
    }

    /**
     * This is the queryNetwork method. It returns the existence and status of the specified network, along with its
     * Neutron UUID and list of VLANs. This method attempts to find the network using both Heat and Neutron. Heat stacks
     * are first searched based on the provided network name/id. If none is found, the Neutron is directly queried.
     */
    @Override
    public void queryNetworkA(String cloudSiteId, String tenantId, String networkNameOrId, String messageId,
            MsoRequest msoRequest, String notificationUrl) {

        logger.debug("Async Query Network {} in {}/{}", networkNameOrId, cloudSiteId, tenantId);
        String errorCreateNetworkMessage = CREATE_NETWORK_ERROR_LOGMSG;

        // Use the synchronous method to perform the actual Create


        // Synchronous Web Service Outputs
        Holder<Boolean> networkExists = new Holder<>();
        Holder<String> networkId = new Holder<>();
        Holder<String> neutronNetworkId = new Holder<>();
        Holder<NetworkStatus> status = new Holder<>();
        Holder<List<Integer>> vlans = new Holder<>();
        Holder<Map<String, String>> subnetIdMap = new Holder<>();

        try {
            networkAdapter.queryNetwork(cloudSiteId, tenantId, networkNameOrId, msoRequest, networkExists, networkId,
                    neutronNetworkId, status, vlans, subnetIdMap);
        } catch (NetworkException e) {
            logger.debug(NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = MsoExceptionCategory.fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error(FAULT_INFO_ERROR_LOGMSG, MessageEnum.RA_FAULT_INFO_EXC, ErrorCode.DataError.getValue(),
                        e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.queryNetworkNotification(messageId, false, exCat, eMsg, null, null, null, null, null, null);
            } catch (Exception e1) {
                logger.error(errorCreateNetworkMessage, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                        ErrorCode.DataError.getValue(), e1.getMessage(), e1);
            }
            return;
        }
        logger.debug("Async Query Network:NameOrId {} tenantId:{}", networkNameOrId, tenantId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            org.onap.so.adapters.network.async.client.NetworkStatus networkS =
                    org.onap.so.adapters.network.async.client.NetworkStatus.fromValue(status.value.name());
            notifyPort.queryNetworkNotification(messageId, true, null, null, networkExists.value, networkId.value,
                    neutronNetworkId.value, networkS, vlans.value, copyQuerySubnetIdMap(subnetIdMap));
        } catch (Exception e) {
            logger.error(errorCreateNetworkMessage, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                    ErrorCode.DataError.getValue(), e.getMessage(), e);
        }
        return;
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
    @Override
    public void deleteNetworkA(String cloudSiteId, String tenantId, String networkType, String modelCustomizationUuid,
            String networkId, String messageId, MsoRequest msoRequest, String notificationUrl) {

        String serviceName = "DeleteNetworkA";
        logger.debug("Async Delete Network {} in {}/{}", networkId, cloudSiteId, tenantId);

        // Use the synchronous method to perform the actual Create


        // Synchronous Web Service Outputs
        Holder<Boolean> networkDeleted = new Holder<>();

        try {
            networkAdapter.deleteNetwork(cloudSiteId, tenantId, networkType, modelCustomizationUuid, networkId,
                    msoRequest, networkDeleted);
        } catch (NetworkException e) {
            logger.debug(NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = MsoExceptionCategory.fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error(FAULT_INFO_ERROR_LOGMSG, MessageEnum.RA_FAULT_INFO_EXC, ErrorCode.DataError.getValue(),
                        e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.deleteNetworkNotification(messageId, false, exCat, eMsg, null);
            } catch (Exception e1) {
                logger.error(CREATE_NETWORK_ERROR_LOGMSG, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                        ErrorCode.DataError.getValue(), e1.getMessage(), e1);

            }
            return;
        }
        logger.debug("Async Delete NetworkId: {} tenantId:{}", networkId, tenantId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.deleteNetworkNotification(messageId, true, null, null, networkDeleted.value);
        } catch (Exception e) {
            logger.error("{} {} Error sending deleteNetwork notification {} ", MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                    ErrorCode.DataError.getValue(), e.getMessage(), e);

        }
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation. A rollback object is returned to the
     * client in a successful creation response. The client can pass that object as-is back to the rollbackNetwork
     * operation to undo the creation.
     *
     * The rollback includes removing the VNF and deleting the tenant if the tenant did not exist prior to the VNF
     * creation.
     */
    @Override
    public void rollbackNetworkA(NetworkRollback rollback, String messageId, String notificationUrl) {
        // rollback may be null (e.g. if network already existed when Create was called)
        if (rollback == null) {
            logger.warn("{} {} Rollback is null", MessageEnum.RA_ROLLBACK_NULL, ErrorCode.SchemaError.getValue());
            return;
        }

        logger.info(LoggingAnchor.TWO, MessageEnum.RA_ASYNC_ROLLBACK, rollback.getNetworkStackId());
        // Use the synchronous method to perform the actual Create


        try {
            networkAdapter.rollbackNetwork(rollback);
        } catch (NetworkException e) {
            logger.debug("Got a NetworkException on rollbackNetwork: ", e);
            // Build and send Asynchronous error response
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = MsoExceptionCategory.fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error("{} {} Exception in get fault info ", MessageEnum.RA_FAULT_INFO_EXC,
                        ErrorCode.DataError.getValue(), e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.rollbackNetworkNotification(rollback.getMsoRequest().getRequestId(), false, exCat, eMsg);
            } catch (Exception e1) {
                logger.error(CREATE_NETWORK_ERROR_LOGMSG, MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC,
                        ErrorCode.DataError.getValue(), e1.getMessage(), e1);

            }
            return;
        }
        logger.debug("Async Rollback NetworkId: {} tenantId:{}", rollback.getNetworkStackId(), rollback.getTenantId());
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.rollbackNetworkNotification(rollback.getMsoRequest().getRequestId(), true, null, null);
        } catch (Exception e) {
            logger.error("{} {} Error sending rollbackNetwork notification {} ",
                    MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, ErrorCode.DataError.getValue(), e.getMessage(), e);

        }
        return;
    }

    private org.onap.so.adapters.network.async.client.NetworkRollback copyNrb(Holder<NetworkRollback> hNrb) {
        org.onap.so.adapters.network.async.client.NetworkRollback cnrb =
                new org.onap.so.adapters.network.async.client.NetworkRollback();

        if (hNrb != null && hNrb.value != null) {
            org.onap.so.adapters.network.async.client.MsoRequest cmr =
                    new org.onap.so.adapters.network.async.client.MsoRequest();

            cnrb.setCloudId(hNrb.value.getCloudId());
            cmr.setRequestId(hNrb.value.getMsoRequest().getRequestId());
            cmr.setServiceInstanceId(hNrb.value.getMsoRequest().getServiceInstanceId());
            cnrb.setMsoRequest(cmr);
            cnrb.setNetworkId(hNrb.value.getNetworkId());
            cnrb.setNetworkStackId(hNrb.value.getNetworkStackId());
            cnrb.setNeutronNetworkId(hNrb.value.getNeutronNetworkId());
            cnrb.setTenantId(hNrb.value.getTenantId());
            cnrb.setNetworkType(hNrb.value.getNetworkType());
            cnrb.setNetworkCreated(hNrb.value.getNetworkCreated());
            cnrb.setNetworkName(hNrb.value.getNetworkName());
            cnrb.setPhysicalNetwork(hNrb.value.getPhysicalNetwork());
            List<Integer> vlansc = cnrb.getVlans();
            List<Integer> vlansh = hNrb.value.getVlans();
            if (vlansh != null) {
                vlansc.addAll(vlansh);
            }
        }
        return cnrb;
    }

    private NetworkAdapterNotify getNotifyEP(String notificationUrl) {

        URL warWsdlLoc = null;
        try {
            warWsdlLoc = Thread.currentThread().getContextClassLoader().getResource("NetworkAdapterNotify.wsdl");
        } catch (Exception e) {
            logger.error("{} {} Exception - WSDL not found ", MessageEnum.RA_WSDL_NOT_FOUND,
                    ErrorCode.DataError.getValue(), e);
        }
        if (warWsdlLoc == null) {
            logger.error("{} {} WSDL not found", MessageEnum.RA_WSDL_NOT_FOUND, ErrorCode.DataError.getValue());
        } else {
            try {
                logger.debug("NetworkAdpaterNotify.wsdl location: {}", warWsdlLoc.toURI().toString());
            } catch (Exception e) {
                logger.error("{} {} Exception - WSDL URL convention ", MessageEnum.RA_WSDL_URL_CONVENTION_EXC,
                        ErrorCode.SchemaError.getValue(), e);
            }
        }

        NetworkAdapterNotify_Service notifySvc = new NetworkAdapterNotify_Service(warWsdlLoc,
                new QName("http://org.onap.so/networkNotify", "networkAdapterNotify"));

        NetworkAdapterNotify notifyPort = notifySvc.getMsoNetworkAdapterAsyncImplPort();

        BindingProvider bp = (BindingProvider) notifyPort;

        URL epUrl = null;
        try {
            epUrl = new URL(notificationUrl);
        } catch (MalformedURLException e1) {
            logger.error("{} {} Exception - init notification ", MessageEnum.RA_INIT_NOTIF_EXC,
                    ErrorCode.DataError.getValue(), e1);
        }

        if (null != epUrl) {
            logger.debug("Notification Endpoint URL: {}", epUrl.toExternalForm());
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epUrl.toExternalForm());
        } else {
            logger.debug("Notification Endpoint URL is NULL: ");
        }

        // authentication
        try {
            Map<String, Object> reqCtx = bp.getRequestContext();
            Map<String, List<String>> headers = new HashMap<>();

            String userCredentials = this.getEncryptedProperty(BPEL_AUTH_PROP, "", ENCRYPTION_KEY_PROP);

            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
            reqCtx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
            headers.put("Authorization", Collections.singletonList(basicAuth));
        } catch (Exception e) {
            logger.error("{} {} Unable to set authorization in callback request {} ",
                    MessageEnum.RA_SET_CALLBACK_AUTH_EXC, ErrorCode.DataError.getValue(), e.getMessage(), e);
        }

        return notifyPort;
    }

    public String getEncryptedProperty(String key, String defaultValue, String encryptionKey) {
        try {
            return CryptoUtils.decrypt(this.environment.getProperty(key), this.environment.getProperty(encryptionKey));
        } catch (GeneralSecurityException e) {
            logger.debug("Exception while decrypting property: {} ", this.environment.getProperty(key), e);
        }
        return defaultValue;

    }

    private CreateNetworkNotification.SubnetIdMap copyCreateSubnetIdMap(Holder<Map<String, String>> hMap) {

        CreateNetworkNotification.SubnetIdMap subnetIdMap = new CreateNetworkNotification.SubnetIdMap();

        if (hMap != null && hMap.value != null) {
            Map<String, String> sMap = hMap.value;
            CreateNetworkNotification.SubnetIdMap.Entry entry = new CreateNetworkNotification.SubnetIdMap.Entry();

            for (Map.Entry<String, String> mapEntry : sMap.entrySet()) {
                String key = mapEntry.getKey();
                String value = mapEntry.getValue();
                entry.setKey(key);
                entry.setValue(value);
                subnetIdMap.getEntry().add(entry);
            }
        }
        return subnetIdMap;
    }

    private UpdateNetworkNotification.SubnetIdMap copyUpdateSubnetIdMap(Holder<Map<String, String>> hMap) {

        UpdateNetworkNotification.SubnetIdMap subnetIdMap = new UpdateNetworkNotification.SubnetIdMap();

        if (hMap != null && hMap.value != null) {
            Map<String, String> sMap = hMap.value;
            UpdateNetworkNotification.SubnetIdMap.Entry entry = new UpdateNetworkNotification.SubnetIdMap.Entry();

            for (Map.Entry<String, String> mapEntry : sMap.entrySet()) {
                String key = mapEntry.getKey();
                String value = mapEntry.getValue();
                entry.setKey(key);
                entry.setValue(value);
                subnetIdMap.getEntry().add(entry);
            }
        }
        return subnetIdMap;
    }

    private QueryNetworkNotification.SubnetIdMap copyQuerySubnetIdMap(Holder<Map<String, String>> hMap) {

        QueryNetworkNotification.SubnetIdMap subnetIdMap = new QueryNetworkNotification.SubnetIdMap();

        if (hMap != null && hMap.value != null) {
            Map<String, String> sMap = hMap.value;
            QueryNetworkNotification.SubnetIdMap.Entry entry = new QueryNetworkNotification.SubnetIdMap.Entry();

            for (Map.Entry<String, String> mapEntry : sMap.entrySet()) {
                String key = mapEntry.getKey();
                String value = mapEntry.getValue();
                entry.setKey(key);
                entry.setValue(value);
                subnetIdMap.getEntry().add(entry);
            }
        }
        return subnetIdMap;
    }
}
