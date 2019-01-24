/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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
import org.onap.so.logger.MessageEnum;

import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.NetworkStatus;
import org.onap.so.openstack.beans.Subnet;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@WebService(serviceName = "NetworkAdapterAsync", endpointInterface = "org.onap.so.adapters.network.MsoNetworkAdapterAsync", targetNamespace = "http://org.onap.so/networkA")
public class MsoNetworkAdapterAsyncImpl implements MsoNetworkAdapterAsync {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA,MsoNetworkAdapterAsyncImpl.class);

    private static final String BPEL_AUTH_PROP = "org.onap.so.adapters.network.bpelauth";
    private static final String ENCRYPTION_KEY_PROP = "org.onap.so.adapters.network.encryptionKey";
    private static final String NETWORK_EXCEPTION_MSG="Got a NetworkException on createNetwork: ";
    private static final String CREATE_NETWORK_ERROR_MSG="Error sending createNetwork notification ";
    private static final String CREATE_NETWORK_EXCEPTON_MSG="Exception sending createNetwork notification";
    private static final String MSO_INTERNAL_ERROR_MSG="MsoInternalError";
    @Autowired
    private Environment environment;

    @Autowired
    private MsoNetworkAdapter networkAdapter;
    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheckA () {
        LOGGER.debug ("Health check call in Network Adapter");
    }

    /**
     * This is the "Create Network" web service implementation.
     * It will create a new Network of the requested type in the specified cloud
     * and tenant. The tenant must exist at the time this service is called.
     *
     * If a network with the same name already exists, this can be considered a
     * success or failure, depending on the value of the 'failIfExists' parameter.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog.
     * All such networks will have a similar configuration, based on the allowable
     * Openstack networking definitions. This includes basic networks, provider
     * networks (with a single VLAN), and multi-provider networks (one or more VLANs)
     *
     * Initially, all provider networks must be "vlan" type, and multiple segments in
     * a multi-provider network must be multiple VLANs on the same physical network.
     *
     * This service supports two modes of Network creation/update:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition. All Heat-based templates must support some subset of
     * the same input parameters: network_name, physical_network, vlan(s).
     *
     * The method returns the network ID and a NetworkRollback object. This latter
     * object can be passed as-is to the rollbackNetwork operation to undo everything
     * that was created. This is useful if a network is successfully created but
     * the orchestration fails on a subsequent operation.
     */
    @Override
    public void createNetworkA (String cloudSiteId,
                                String tenantId,
                                String networkType,
                                String modelCustomizationUuid,
                                String networkName,
                                String physicalNetworkName,
                                List <Integer> vlans,
                                Boolean failIfExists,
                                Boolean backout,
                                List <Subnet> subnets,
                                Map<String,String> networkParams,
                                String messageId,
                                MsoRequest msoRequest,
                                String notificationUrl) {
        String error;

        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName ("CreateNetworkA");
        LOGGER.debug ("Async Create Network: " + networkName
                                      + " of type "
                                      + networkType
                                      + " in "
                                      + cloudSiteId
                                      + "/"
                                      + tenantId);

        // Use the synchronous method to perform the actual Create
        

        // Synchronous Web Service Outputs
        Holder <String> networkId = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <NetworkRollback> networkRollback = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        
        HashMap<String, String> params = (HashMap<String,String>) networkParams;
        if (params == null)
        	params = new HashMap<String,String>();
        String shared = null;
        String external = null;
        if (params.containsKey("shared"))
        	shared = params.get("shared");
        if (params.containsKey("external"))
        	external = params.get("external");

        try {
            networkAdapter.createNetwork (cloudSiteId,
                                          tenantId,
                                          networkType,
                                          modelCustomizationUuid,
                                          networkName,
                                          physicalNetworkName,
                                          vlans,
                                          shared,
                                          external,
                                          failIfExists,
                                          backout,
                                          subnets,
                                          params,
                                          msoRequest,
                                          networkId,
                                          neutronNetworkId,
                                          subnetIdMap,
                                          networkRollback);
        } catch (NetworkException e) {
            LOGGER.debug (NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = MsoExceptionCategory.fromValue (e.getFaultInfo ().getCategory ().name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.createNetworkNotification (messageId, false, exCat, eMsg, null, null, null, null);
            } catch (Exception e1) {
                error = CREATE_NETWORK_ERROR_MSG + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError,CREATE_NETWORK_EXCEPTON_MSG, e1);

            }
            return;
        }
        LOGGER.debug ("Async Create Network:Name " + networkName + " physicalNetworkName:" + physicalNetworkName);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.createNetworkNotification (messageId,
                                                  true,
                                                  null,
                                                  null,
                                                  networkId.value,
                                                  neutronNetworkId.value,
                                                  copyCreateSubnetIdMap (subnetIdMap),
                                                  copyNrb (networkRollback));
        } catch (Exception e) {
            error = CREATE_NETWORK_ERROR_MSG + e.getMessage ();
            LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, CREATE_NETWORK_EXCEPTON_MSG, e);

        }
        return;
    }

    /**
     * This is the "Update Network" web service implementation.
     * It will update an existing Network of the requested type in the specified cloud
     * and tenant. The typical use will be to replace the VLANs with the supplied
     * list (to add or remove a VLAN), but other properties may be updated as well.
     *
     * There will be a pre-defined set of network types defined in the MSO Catalog.
     * All such networks will have a similar configuration, based on the allowable
     * Openstack networking definitions. This includes basic networks, provider
     * networks (with a single VLAN), and multi-provider networks (one or more VLANs).
     *
     * Initially, all provider networks must currently be "vlan" type, and multi-provider
     * networks must be multiple VLANs on the same physical network.
     *
     * This service supports two modes of Network update:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition. All Heat-based templates must support some subset of
     * the same input parameters: network_name, physical_network, vlan, segments.
     *
     * The method returns a NetworkRollback object. This object can be passed
     * as-is to the rollbackNetwork operation to undo everything that was updated.
     * This is useful if a network is successfully updated but orchestration
     * fails on a subsequent operation.
     */
    @Override
    public void updateNetworkA (String cloudSiteId,
                                String tenantId,
                                String networkType,
                                String modelCustomizationUuid,
                                String networkId,
                                String networkName,
                                String physicalNetworkName,
                                List <Integer> vlans,
                                List <Subnet> subnets,
                                Map <String,String> networkParams,
                                String messageId,
                                MsoRequest msoRequest,
                                String notificationUrl) {
        String error;

        String serviceName = "UpdateNetworkA";
        MsoLogger.setServiceName (serviceName);
        MsoLogger.setLogContext (msoRequest);
        LOGGER.debug ("Async Update Network: " + networkId
                      + " of type "
                      + networkType
                      + "in "
                      + cloudSiteId
                      + "/"
                      + tenantId);

        // Use the synchronous method to perform the actual Create
        

        // Synchronous Web Service Outputs
        Holder <NetworkRollback> networkRollback = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();
        
        HashMap<String, String> params = (HashMap<String,String>) networkParams;
        if (params == null)
        	params = new HashMap<String,String>();
        String shared = null;
        String external = null;
        if (params.containsKey("shared"))
        	shared = params.get("shared");
        if (params.containsKey("external"))
        	external = params.get("external");

        try {
            networkAdapter.updateNetwork (cloudSiteId,
                                          tenantId,
                                          networkType,
                                          modelCustomizationUuid,
                                          networkId,
                                          networkName,
                                          physicalNetworkName,
                                          vlans,
                                          shared,
                                          external,
                                          subnets,
                                          params,
                                          msoRequest,
                                          subnetIdMap,
                                          networkRollback);
            MsoLogger.setServiceName (serviceName);
        } catch (NetworkException e) {
        	MsoLogger.setServiceName (serviceName);
            LOGGER.debug ("Got a NetworkException on updateNetwork: ", e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = MsoExceptionCategory.fromValue (e.getFaultInfo ().getCategory ().name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.updateNetworkNotification (messageId, false, exCat, eMsg, null, copyNrb (networkRollback));
            } catch (Exception e1) {
                error = "Error sending updateNetwork notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending updateNetwork notification", e1);

            }
            return;
        }
        LOGGER.debug ("Async Update Network:Name " + networkName + " NetworkId:" + networkId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.updateNetworkNotification (messageId,
                                                  true,
                                                  null,
                                                  null,
                                                  copyUpdateSubnetIdMap (subnetIdMap),
                                                  copyNrb (networkRollback));
        } catch (Exception e) {
            error = "Error sending updateNotification request" + e.getMessage ();
            LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending updateNotification request", e);

        }
        return;
    }

    /**
     * This is the queryNetwork method. It returns the existence and status of
     * the specified network, along with its Neutron UUID and list of VLANs.
     * This method attempts to find the network using both Heat and Neutron.
     * Heat stacks are first searched based on the provided network name/id.
     * If none is found, the Neutron is directly queried.
     */
    @Override
    public void queryNetworkA (String cloudSiteId,
                               String tenantId,
                               String networkNameOrId,
                               String messageId,
                               MsoRequest msoRequest,
                               String notificationUrl) {
        String error;

        MsoLogger.setLogContext (msoRequest);
        String serviceName = "QueryNetworkA";
        MsoLogger.setServiceName (serviceName);
        LOGGER.debug ("Async Query Network " + networkNameOrId + " in " + cloudSiteId + "/" + tenantId);

        // Use the synchronous method to perform the actual Create
        

        // Synchronous Web Service Outputs
        Holder <Boolean> networkExists = new Holder <> ();
        Holder <String> networkId = new Holder <> ();
        Holder <String> neutronNetworkId = new Holder <> ();
        Holder <NetworkStatus> status = new Holder <> ();
        Holder <List <Integer>> vlans = new Holder <> ();
        Holder <Map <String, String>> subnetIdMap = new Holder <> ();

        try {
            networkAdapter.queryNetwork (cloudSiteId,
                                         tenantId,
                                         networkNameOrId,
                                         msoRequest,
                                         networkExists,
                                         networkId,
                                         neutronNetworkId,
                                         status,
                                         vlans,
                                         subnetIdMap);
            MsoLogger.setServiceName (serviceName);
        } catch (NetworkException e) {
        	MsoLogger.setServiceName (serviceName);
            LOGGER.debug (NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = MsoExceptionCategory.fromValue (e.getFaultInfo ().getCategory ().name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.queryNetworkNotification (messageId, false, exCat, eMsg, null, null, null, null, null, null);
            } catch (Exception e1) {
                error = "Error sending createNetwork notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending createNetwork notification", e1);

            }
            return;
        }
        LOGGER.debug ("Async Query Network:NameOrId " + networkNameOrId + " tenantId:" + tenantId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            org.onap.so.adapters.network.async.client.NetworkStatus networkS = org.onap.so.adapters.network.async.client.NetworkStatus.fromValue (status.value.name ());
            notifyPort.queryNetworkNotification (messageId,
                                                 true,
                                                 null,
                                                 null,
                                                 networkExists.value,
                                                 networkId.value,
                                                 neutronNetworkId.value,
                                                 networkS,
                                                 vlans.value,
                                                 copyQuerySubnetIdMap (subnetIdMap));
        } catch (Exception e) {
            error = "Error sending createNetwork notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending createNetwork notification", e);

        }
        return;
    }

    /**
     * This is the "Delete Network" web service implementation.
     * It will delete a Network in the specified cloud and tenant.
     *
     * If the network is not found, it is treated as a success.
     *
     * This service supports two modes of Network creation/update/delete:
     * - via Heat Templates
     * - via Neutron API
     * The network orchestration mode for each network type is declared in its
     * catalog definition.
     *
     * For Heat-based orchestration, the networkId should be the stack ID.
     * For Neutron-based orchestration, the networkId should be the Neutron network UUID.
     *
     * The method returns nothing on success. Rollback is not possible for delete
     * commands, so any failure on delete will require manual fallout in the client.
     */
    @Override
    public void deleteNetworkA (String cloudSiteId,
                                String tenantId,
                                String networkType,
                                String modelCustomizationUuid,
                                String networkId,
                                String messageId,
                                MsoRequest msoRequest,
                                String notificationUrl) {
        String error;
        MsoLogger.setLogContext (msoRequest);
        String serviceName = "DeleteNetworkA";
        MsoLogger.setServiceName (serviceName);
        LOGGER.debug ("Async Delete Network " + networkId + " in " + cloudSiteId + "/" + tenantId);

        // Use the synchronous method to perform the actual Create
        

        // Synchronous Web Service Outputs
        Holder <Boolean> networkDeleted = new Holder <> ();

        try {
            networkAdapter.deleteNetwork (cloudSiteId, tenantId, networkType, modelCustomizationUuid, networkId, msoRequest, networkDeleted);
            MsoLogger.setServiceName (serviceName);
        } catch (NetworkException e) {
        	MsoLogger.setServiceName (serviceName);
            LOGGER.debug (NETWORK_EXCEPTION_MSG, e);
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = MsoExceptionCategory.fromValue (e.getFaultInfo ().getCategory ().name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.deleteNetworkNotification (messageId, false, exCat, eMsg, null);
            } catch (Exception e1) {
                error = "Error sending createNetwork notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending createNetwork notification", e1);

            }
            return;
        }
        LOGGER.debug ("Async Delete NetworkId: " + networkId + " tenantId:" + tenantId);
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.deleteNetworkNotification (messageId, true, null, null, networkDeleted.value);
        } catch (Exception e) {
            error = "Error sending deleteNetwork notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception sending deleteNetwork notification", e);

        }
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation.
     * A rollback object is returned to the client in a successful creation
     * response. The client can pass that object as-is back to the rollbackNetwork
     * operation to undo the creation.
     *
     * The rollback includes removing the VNF and deleting the tenant if the
     * tenant did not exist prior to the VNF creation.
     */
    @Override
    public void rollbackNetworkA (NetworkRollback rollback, String messageId, String notificationUrl) {
        String error;
        String serviceName = "RollbackNetworkA";
        MsoLogger.setServiceName (serviceName);
        // rollback may be null (e.g. if network already existed when Create was called)
        if (rollback == null) {
            LOGGER.warn (MessageEnum.RA_ROLLBACK_NULL, "", "", MsoLogger.ErrorCode.SchemaError, "Rollback is null");
            return;
        }

        MsoLogger.setLogContext (rollback.getMsoRequest ());
        LOGGER.info (MessageEnum.RA_ASYNC_ROLLBACK, rollback.getNetworkStackId (), "", "");
        // Use the synchronous method to perform the actual Create
        

        try {
            networkAdapter.rollbackNetwork (rollback);
            MsoLogger.setServiceName (serviceName);
        } catch (NetworkException e) {
        	MsoLogger.setServiceName (serviceName);
            LOGGER.debug ("Got a NetworkException on rollbackNetwork: ", e);
            // Build and send Asynchronous error response
            MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = MsoExceptionCategory.fromValue (e.getFaultInfo ().getCategory ().name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception in get fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.rollbackNetworkNotification (rollback.getMsoRequest ().getRequestId (), false, exCat, eMsg);
            } catch (Exception e1) {
                error = CREATE_NETWORK_ERROR_MSG + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception in sending createNetwork notification ", e1);

            }
            return;
        }
        LOGGER.debug ("Async Rollback NetworkId: " + rollback.getNetworkStackId () + " tenantId:" + rollback.getTenantId ());
        // Build and send Asynchronous response
        try {
            NetworkAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.rollbackNetworkNotification (rollback.getMsoRequest ().getRequestId (), true, null, null);
        } catch (Exception e) {
            error = "Error sending rollbackNetwork notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_CREATE_NETWORK_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception in sending rollbackNetwork notification", e);

        }
        return;
    }

    private org.onap.so.adapters.network.async.client.NetworkRollback copyNrb (Holder <NetworkRollback> hNrb) {
        org.onap.so.adapters.network.async.client.NetworkRollback cnrb = new org.onap.so.adapters.network.async.client.NetworkRollback ();

        if (hNrb != null && hNrb.value != null) {
            org.onap.so.adapters.network.async.client.MsoRequest cmr = new org.onap.so.adapters.network.async.client.MsoRequest ();

            cnrb.setCloudId (hNrb.value.getCloudId ());
            cmr.setRequestId (hNrb.value.getMsoRequest ().getRequestId ());
            cmr.setServiceInstanceId (hNrb.value.getMsoRequest ().getServiceInstanceId ());
            cnrb.setMsoRequest (cmr);
            cnrb.setNetworkId (hNrb.value.getNetworkId ());
            cnrb.setNetworkStackId (hNrb.value.getNetworkStackId ());
            cnrb.setNeutronNetworkId (hNrb.value.getNeutronNetworkId ());
            cnrb.setTenantId (hNrb.value.getTenantId ());
            cnrb.setNetworkType (hNrb.value.getNetworkType ());
            cnrb.setNetworkCreated (hNrb.value.getNetworkCreated ());
            cnrb.setNetworkName (hNrb.value.getNetworkName ());
            cnrb.setPhysicalNetwork (hNrb.value.getPhysicalNetwork ());
            List <Integer> vlansc = cnrb.getVlans ();
            List <Integer> vlansh = hNrb.value.getVlans ();
            if (vlansh != null) {
                vlansc.addAll (vlansh);
            }
        }
        return cnrb;
    }

    private NetworkAdapterNotify getNotifyEP (String notificationUrl) {

        URL warWsdlLoc = null;
        try {
            warWsdlLoc = Thread.currentThread ().getContextClassLoader ().getResource ("NetworkAdapterNotify.wsdl");
        } catch (Exception e) {
            LOGGER.error (MessageEnum.RA_WSDL_NOT_FOUND, "NetworkAdpaterNotify.wsdl", "", "", MsoLogger.ErrorCode.DataError, "Exception - WSDL not found", e);
        }
        if (warWsdlLoc == null) {
            LOGGER.error (MessageEnum.RA_WSDL_NOT_FOUND, "NetworkAdpaterNotify.wsdl", "", "", MsoLogger.ErrorCode.DataError, "WSDL not found");
        } else {
            try {
                LOGGER.debug ("NetworkAdpaterNotify.wsdl location:" + warWsdlLoc.toURI ().toString ());
            } catch (Exception e) {
                LOGGER.error (MessageEnum.RA_WSDL_URL_CONVENTION_EXC, "NetworkAdpaterNotify.wsdl", "", "", MsoLogger.ErrorCode.SchemaError, "Exception - WSDL URL convention", e);
            }
        }

        NetworkAdapterNotify_Service notifySvc = new NetworkAdapterNotify_Service (warWsdlLoc,
                                                                                   new QName ("http://org.onap.so/networkNotify",
                                                                                              "networkAdapterNotify"));

        NetworkAdapterNotify notifyPort = notifySvc.getMsoNetworkAdapterAsyncImplPort ();

        BindingProvider bp = (BindingProvider) notifyPort;

        URL epUrl = null;
        try {
            epUrl = new URL (notificationUrl);
        } catch (MalformedURLException e1) {
            LOGGER.error (MessageEnum.RA_INIT_NOTIF_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - init notification", e1);
        }

        if(null != epUrl) {
            LOGGER.debug ("Notification Endpoint URL: " + epUrl.toExternalForm ());
            bp.getRequestContext ().put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epUrl.toExternalForm ());
        }
        else {
        	LOGGER.debug ("Notification Endpoint URL is NULL: ");
        }

        // authentication
        try {
            Map <String, Object> reqCtx = bp.getRequestContext ();
            Map <String, List <String>> headers = new HashMap <> ();

            String userCredentials = this.getEncryptedProperty (BPEL_AUTH_PROP, "", ENCRYPTION_KEY_PROP);

            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary (userCredentials.getBytes ());
            reqCtx.put (MessageContext.HTTP_REQUEST_HEADERS, headers);
            headers.put ("Authorization", Collections.singletonList (basicAuth));
        } catch (Exception e) {
            String error1 = "Unable to set authorization in callback request" + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SET_CALLBACK_AUTH_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception - Unable to set authorization in callback request", e);

        }

        return notifyPort;
    }
    
    public String getEncryptedProperty(String key, String defaultValue, String encryptionKey) {
    	try {
			return CryptoUtils.decrypt(this.environment.getProperty(key), this.environment.getProperty(encryptionKey));
		} catch (GeneralSecurityException e) {
			LOGGER.debug("Exception while decrypting property: " + this.environment.getProperty(key), e);
		}
		return defaultValue;

	}

    private CreateNetworkNotification.SubnetIdMap copyCreateSubnetIdMap (Holder <Map <String, String>> hMap) {

        CreateNetworkNotification.SubnetIdMap subnetIdMap = new CreateNetworkNotification.SubnetIdMap ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = hMap.value;
            CreateNetworkNotification.SubnetIdMap.Entry entry = new CreateNetworkNotification.SubnetIdMap.Entry ();

            for (Map.Entry<String,String> mapEntry : sMap.entrySet ()) {
            	String key = mapEntry.getKey();
        		String value = mapEntry.getValue();
                entry.setKey (key);
                entry.setValue (value);
                subnetIdMap.getEntry ().add (entry);
            }
        }
        return subnetIdMap;
    }

    private UpdateNetworkNotification.SubnetIdMap copyUpdateSubnetIdMap (Holder <Map <String, String>> hMap) {

        UpdateNetworkNotification.SubnetIdMap subnetIdMap = new UpdateNetworkNotification.SubnetIdMap ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = hMap.value;
            UpdateNetworkNotification.SubnetIdMap.Entry entry = new UpdateNetworkNotification.SubnetIdMap.Entry ();

            for (Map.Entry<String,String> mapEntry : sMap.entrySet ()) {
		String key = mapEntry.getKey();
		String value = mapEntry.getValue();
                entry.setKey (key);
                entry.setValue (value);
                subnetIdMap.getEntry ().add (entry);
            }
        }
        return subnetIdMap;
    }

    private QueryNetworkNotification.SubnetIdMap copyQuerySubnetIdMap (Holder <Map <String, String>> hMap) {

        QueryNetworkNotification.SubnetIdMap subnetIdMap = new QueryNetworkNotification.SubnetIdMap ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = hMap.value;
            QueryNetworkNotification.SubnetIdMap.Entry entry = new QueryNetworkNotification.SubnetIdMap.Entry ();

            for (Map.Entry<String,String> mapEntry : sMap.entrySet ()) {
		String key = mapEntry.getKey();
		String value = mapEntry.getValue();
                entry.setKey (key);
                entry.setValue (value);
                subnetIdMap.getEntry ().add (entry);
            }
        }
        return subnetIdMap;
    }
}
