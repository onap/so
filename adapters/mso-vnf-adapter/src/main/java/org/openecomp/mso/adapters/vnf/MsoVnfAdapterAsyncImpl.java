/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;


import java.net.MalformedURLException;
import java.net.URL;
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

import org.openecomp.mso.adapters.vnf.async.client.CreateVnfNotification;
import org.openecomp.mso.adapters.vnf.async.client.QueryVnfNotification;
import org.openecomp.mso.adapters.vnf.async.client.UpdateVnfNotification;
import org.openecomp.mso.adapters.vnf.async.client.VnfAdapterNotify;
import org.openecomp.mso.adapters.vnf.async.client.VnfAdapterNotify_Service;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.properties.MsoPropertiesFactory;

@WebService(serviceName = "VnfAdapterAsync", endpointInterface = "org.openecomp.mso.adapters.vnf.MsoVnfAdapterAsync", targetNamespace = "http://org.openecomp.mso/vnfA")
public class MsoVnfAdapterAsyncImpl implements MsoVnfAdapterAsync {

	MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	CloudConfigFactory cloudConfigFactory=new CloudConfigFactory();

	public static final String MSO_PROP_VNF_ADAPTER="MSO_PROP_VNF_ADAPTER";
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static final String BPEL_AUTH_PROP = "org.openecomp.mso.adapters.vnf.bpelauth";
    private static final String ENCRYPTION_KEY = "aa3871669d893c7fb8abbcda31b88b4f";

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheckA () {
        LOGGER.debug ("Health check call in VNF Adapter");
    }

    /**
     * This is the asynchronous "Create VNF" web service implementation.
     * It will create a new VNF of the requested type in the specified cloud
     * and tenant. The tenant must exist before this service is called.
     *
     * If a VNF with the same name already exists, this can be considered a
     * success or failure, depending on the value of the 'failIfExists' parameter.
     *
     * All VNF types will be defined in the MSO catalog. The caller must request
     * one of these pre-defined types or an error will be returned. Within the
     * catalog, each VNF type references (among other things) a Heat template
     * which is used to deploy the required VNF artifacts (VMs, networks, etc.)
     * to the cloud.
     *
     * Depending on the Heat template, a variable set of input parameters will
     * be defined, some of which are required. The caller is responsible to
     * pass the necessary input data for the VNF or an error will be thrown.
     *
     * The method sends an asynchronous response to the notification URL when
     * processing completes. The createAsyncResponse contains the vnfId (the
     * canonical name of the stack), a Map of VNF output attributes, and a
     * VnfRollback object. This last object can be passed as-is to the
     * rollbackVnf operation to undo everything that was created for the VNF.
     * This is useful if a VNF is successfully created but the orchestrator
     * fails on a subsequent operation.
     *
     * Note: this method is implemented by calling the synchronous web method
     * and translating the response to an asynchronous notification.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vnfType VNF type key, should match a VNF definition in catalog DB
     * @param vnfName Name to be assigned to the new VNF
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered
     *        a success or failure
     * @param msoRequest Request tracking information for logs
     * @param notificationURL the target URL for asynchronous response
     */
    @Override
    public void createVnfA (String cloudSiteId,
                            String tenantId,
                            String vnfType,
                            String vnfVersion,
                            String vnfName,
                            String requestType,
                            String volumeGroupHeatStackId,
                            Map <String, String> inputs,
                            Boolean failIfExists,
                            Boolean backout,
                            String messageId,
                            MsoRequest msoRequest,
                            String notificationUrl) {
        String error;
        String serviceName = "CreateVnfA";
        MsoLogger.setLogContext (msoRequest);
        MsoLogger.setServiceName (serviceName);
        LOGGER.info (MessageEnum.RA_ASYNC_CREATE_VNF, vnfName, vnfType, cloudSiteId, tenantId, "", "createVnfA");
        // Use the synchronous method to perform the actual Create
        MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory, cloudConfigFactory);

        // Synchronous Web Service Outputs
        Holder <String> vnfId = new Holder <> ();
        Holder <Map <String, String>> outputs = new Holder <> ();
        Holder <VnfRollback> vnfRollback = new Holder <> ();

        try {
            vnfAdapter.createVnf (cloudSiteId,
                                  tenantId,
                                  vnfType,
                                  vnfVersion,
                                  vnfName,
                                  requestType,
                                  volumeGroupHeatStackId,
                                  inputs,
                                  failIfExists,
                                  backout,
                                  msoRequest,
                                  vnfId,
                                  outputs,
                                  vnfRollback);
            MsoLogger.setServiceName (serviceName);
        } catch (VnfException e) {
        	MsoLogger.setServiceName (serviceName);
        	LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR,  vnfName, cloudSiteId, tenantId, "", "createVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "VnfException in createVnfA", e);
            org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory.fromValue (e.getFaultInfo ()
                                                                                                     .getCategory ()
                                                                                                     .name ());
            } catch (Exception e1) {
                LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "createVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.createVnfNotification (messageId, false, exCat, eMsg, null, null, null);
            } catch (Exception e1) {
                error = "Error sending createVnf notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "createVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending createVnf notification", e1);
                alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            }
            LOGGER.info (MessageEnum.RA_ASYNC_CREATE_VNF_COMPLETE, "", "createVnfA", "", "createVnfA");
            return;
        }
        LOGGER.debug ("Async Create VNF: " + vnfName + " VnfId:" + vnfId.value);
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.createVnfNotification (messageId,
                                              true,
                                              null,
                                              null,
                                              vnfId.value,
                                              copyCreateOutputs (outputs),
                                              copyVrb (vnfRollback));
        } catch (Exception e) {
            error = "Error sending createVnf notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "createVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending createVnf notification", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
        }
        LOGGER.info (MessageEnum.RA_ASYNC_CREATE_VNF_COMPLETE, "", "createVnfA");
        return;
    }

    @Override
    public void updateVnfA (String cloudSiteId,
                            String tenantId,
                            String vnfType,
                            String vnfVersion,
                            String vnfName,
                            String requestType,
                            String volumeGroupHeatStackId,
                            Map <String, String> inputs,
                            String messageId,
                            MsoRequest msoRequest,
                            String notificationUrl) {
        String error;
        String serviceName = "UpdateVnfA";
        MsoLogger.setServiceName (serviceName);
        MsoLogger.setLogContext (msoRequest);
        LOGGER.info (MessageEnum.RA_ASYNC_UPDATE_VNF, vnfName, vnfType, cloudSiteId, tenantId, "", "UpdateVnfA");

        // Use the synchronous method to perform the actual Create
        MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory,cloudConfigFactory);

        // Synchronous Web Service Outputs
        Holder <String> vnfId = new Holder <> ();
        Holder <Map <String, String>> outputs = new Holder <> ();
        Holder <VnfRollback> vnfRollback = new Holder <> ();

        try {
            vnfAdapter.updateVnf (cloudSiteId, tenantId, vnfType,vnfVersion, vnfName, requestType, volumeGroupHeatStackId, inputs, msoRequest, outputs, vnfRollback);
            MsoLogger.setServiceName (serviceName);
        } catch (VnfException e) {
        	MsoLogger.setServiceName (serviceName);
        	LOGGER.error (MessageEnum.RA_UPDATE_VNF_ERR,  vnfName, cloudSiteId, tenantId, "", "UpdateVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending updateVnf notification", e);
            org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory.fromValue (e.getFaultInfo ()
                                                                                                     .getCategory ()
                                                                                                     .name ());
            } catch (Exception e1) {
            	LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "UpdateVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.updateVnfNotification (messageId, false, exCat, eMsg, null, null);
            } catch (Exception e1) {
                error = "Error sending updateVnf notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "UpdateVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending updateVnf notification", e1);
                alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            }
            LOGGER.info (MessageEnum.RA_ASYNC_UPDATE_VNF_COMPLETE, "", "UpdateVnfA");
            return;
        }
        LOGGER.debug ("Async Update VNF: " + vnfName + " VnfId:" + vnfId.value);
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.updateVnfNotification (messageId,
                                              true,
                                              null,
                                              null,
                                              copyUpdateOutputs (outputs),
                                              copyVrb (vnfRollback));
        } catch (Exception e) {
            error = "Error sending updateVnf notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "UpdateVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending updateVnf notification", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
        }
        LOGGER.info (MessageEnum.RA_ASYNC_UPDATE_VNF_COMPLETE, "", "UpdateVnfA");
        return;
    }

    /**
     * This is the "Query VNF" web service implementation.
     * It will look up a VNF by name or ID in the specified cloud and tenant.
     *
     * The method returns an indicator that the VNF exists, its Openstack internal
     * ID, its status, and the set of outputs (from when the stack was created).
     *
     * @param cloudSiteId CLLI code of the cloud site in which to query
     * @param tenantId Openstack tenant identifier
     * @param vnfName VNF Name or Openstack ID
     * @param msoRequest Request tracking information for logs
     * @param notificationURL the target URL for asynchronous response
     */
    @Override
    public void queryVnfA (String cloudSiteId,
                           String tenantId,
                           String vnfName,
                           String messageId,
                           MsoRequest msoRequest,
                           String notificationUrl) {
        String error;
        String serviceName = "QueryVnfA";
        MsoLogger.setServiceName (serviceName);
        MsoLogger.setLogContext (msoRequest);
        LOGGER.info (MessageEnum.RA_ASYNC_QUERY_VNF, vnfName, cloudSiteId, tenantId);

        // Use the synchronous method to perform the actual query
        MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory,cloudConfigFactory);

        // Synchronous Web Service Outputs
        Holder <Boolean> vnfExists = new Holder <> ();
        Holder <String> vnfId = new Holder <> ();
        Holder <VnfStatus> status = new Holder <> ();
        Holder <Map <String, String>> outputs = new Holder <> ();

        try {
            vnfAdapter.queryVnf (cloudSiteId, tenantId, vnfName, msoRequest, vnfExists, vnfId, status, outputs);
            MsoLogger.setServiceName (serviceName);
        } catch (VnfException e) {
        	MsoLogger.setServiceName (serviceName);
            LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  vnfName, cloudSiteId, tenantId, "", "queryVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending queryVnfA notification", e);
            org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory.fromValue (e.getFaultInfo ()
                                                                                                     .getCategory ()
                                                                                                     .name ());
            } catch (Exception e1) {
            	LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "queryVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.queryVnfNotification (messageId, false, exCat, eMsg, null, null, null, null);
            } catch (Exception e1) {
                error = "Error sending queryVnf notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "queryVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending queryVnf notification", e1);
                alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            }
            LOGGER.info (MessageEnum.RA_ASYNC_QUERY_VNF_COMPLETE, "", "queryVnfA");
            return;
        }

        if (!vnfExists.value) {
            LOGGER.debug ("Async Query, VNF not found");
        } else {
            LOGGER.debug ("Async Query, VNF=" + vnfId.value + ", status=" + status.value);
        }
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            org.openecomp.mso.adapters.vnf.async.client.VnfStatus vnfS = org.openecomp.mso.adapters.vnf.async.client.VnfStatus.fromValue (status.value.name ());
            notifyPort.queryVnfNotification (messageId,
                                             true,
                                             null,
                                             null,
                                             vnfExists.value,
                                             vnfId.value,
                                             vnfS,
                                             copyQueryOutputs (outputs));
        } catch (Exception e) {
            error = "Error sending queryVnf notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "queryVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending queryVnf notification", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
        }

        LOGGER.info (MessageEnum.RA_ASYNC_QUERY_VNF_COMPLETE, "", "queryVnfA");
        return;
    }

    /**
     * This is the Asynchronous "Delete VNF" web service implementation.
     * It will delete a VNF by name or ID in the specified cloud and tenant.
     *
     * The method has no outputs.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to delete
     * @param tenantId Openstack tenant identifier
     * @param vnfName VNF Name or Openstack ID
     * @param messageId
     * @param msoRequest Request tracking information for logs
     * @param notificationUrl the target URL for asynchronous response
     */
    @Override
    public void deleteVnfA (String cloudSiteId,
                            String tenantId,
                            String vnfName,
                            String messageId,
                            MsoRequest msoRequest,
                            String notificationUrl) {
        String error;
        String serviceName = "DeleteVnfA";
        MsoLogger.setServiceName (serviceName);
        MsoLogger.setLogContext (msoRequest);
        LOGGER.info (MessageEnum.RA_ASYNC_DELETE_VNF, vnfName, cloudSiteId, tenantId);

        // Use the synchronous method to perform the actual delete
        MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory,cloudConfigFactory);

        try {
            vnfAdapter.deleteVnf (cloudSiteId, tenantId, vnfName, msoRequest);
            MsoLogger.setServiceName (serviceName);
        } catch (VnfException e) {
        	MsoLogger.setServiceName (serviceName);
        	LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR,  vnfName, cloudSiteId, tenantId, "", "deleteVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending deleteVnfA notification", e);
            org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory.fromValue (e.getFaultInfo ()
                                                                                                     .getCategory ()
                                                                                                     .name ());
            } catch (Exception e1) {
            	LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "deleteVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.deleteVnfNotification (messageId, false, exCat, eMsg);
            } catch (Exception e1) {
                error = "Error sending deleteVnf notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "deleteVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending deleteVnfA notification", e1);
                alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            }
            LOGGER.info (MessageEnum.RA_ASYNC_DELETE_VNF_COMPLETE, "", "deleteVnfA");
            return;
        }

        LOGGER.debug ("Async Delete VNF: " + vnfName);
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.deleteVnfNotification (messageId, true, null, null);

        } catch (Exception e) {
            error = "Error sending deleteVnf notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "deleteVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending deleteVnfA notification", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
        }

        LOGGER.info (MessageEnum.RA_ASYNC_DELETE_VNF_COMPLETE, "", "deleteVnfA");
        return;
    }

    /**
     * This web service endpoint will rollback a previous Create VNF operation.
     * A rollback object is returned to the client in a successful creation
     * response. The client can pass that object as-is back to the rollbackVnf
     * operation to undo the creation.
     */
    @Override
    public void rollbackVnfA (VnfRollback rollback, String messageId, String notificationUrl) {
        String serviceName = "RollbackVnfA";
        MsoLogger.setServiceName (serviceName);
        String error;
        // rollback may be null (e.g. if stack already existed when Create was called)
        if (rollback == null) {
            error = "Empty Rollback: No action to perform";
            LOGGER.info (MessageEnum.RA_ROLLBACK_NULL, "", "rollbackVnfA");
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            return;
        }

        MsoLogger.setLogContext (rollback.getMsoRequest ());
        LOGGER.info (MessageEnum.RA_ASYNC_ROLLBACK_VNF, "", "rollbackVnfA");

        // Use the synchronous method to perform the actual rollback
        MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory,cloudConfigFactory);

        try {
            vnfAdapter.rollbackVnf (rollback);
            MsoLogger.setServiceName (serviceName);
        } catch (VnfException e) {
        	MsoLogger.setServiceName (serviceName);
        	LOGGER.error (MessageEnum.RA_ROLLBACK_VNF_ERR, "", "rollbackVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending rollbackVnfA notification", e);
            org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo ().getMessage ();
                exCat = org.openecomp.mso.adapters.vnf.async.client.MsoExceptionCategory.fromValue (e.getFaultInfo ()
                                                                                                     .getCategory ()
                                                                                                     .name ());
            } catch (Exception e1) {
            	LOGGER.error (MessageEnum.RA_FAULT_INFO_EXC, "", "rollbackVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - fault info", e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
                notifyPort.rollbackVnfNotification (messageId, false, exCat, eMsg);
            } catch (Exception e1) {
                error = "Error sending rollbackVnf notification " + e1.getMessage ();
                LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "rollbackVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending rollbackVnfA notification", e1);
                alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
            }
            LOGGER.info (MessageEnum.RA_ASYNC_ROLLBACK_VNF_COMPLETE, "", "rollbackVnfA");
            return;
        }

        LOGGER.debug ("Async Rollback VNF:" + rollback.getVnfId ());
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP (notificationUrl);
            notifyPort.rollbackVnfNotification (messageId, true, null, null);
        } catch (Exception e) {
            error = "Error sending rollbackVnf notification " + e.getMessage ();
            LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, "", "rollbackVnfA", MsoLogger.ErrorCode.BusinessProcesssError, "Exception sending rollbackVnfA notification", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
        }

        LOGGER.info (MessageEnum.RA_ASYNC_ROLLBACK_VNF_COMPLETE, "", "rollbackVnfA");
        return;
    }

    private org.openecomp.mso.adapters.vnf.async.client.VnfRollback copyVrb (Holder <VnfRollback> hVrb) {
        org.openecomp.mso.adapters.vnf.async.client.VnfRollback cvrb = new org.openecomp.mso.adapters.vnf.async.client.VnfRollback ();

        if (hVrb != null && hVrb.value != null) {
            org.openecomp.mso.adapters.vnf.async.client.MsoRequest cmr = new org.openecomp.mso.adapters.vnf.async.client.MsoRequest ();

            cvrb.setCloudSiteId (hVrb.value.getCloudSiteId ());
            if (hVrb.value.getMsoRequest() != null) {
            	cmr.setRequestId (hVrb.value.getMsoRequest ().getRequestId ());
            	cmr.setServiceInstanceId (hVrb.value.getMsoRequest ().getServiceInstanceId ());
            } else {
            	cmr.setRequestId (null);
            	cmr.setServiceInstanceId (null);
            }
            cvrb.setMsoRequest (cmr);
            cvrb.setVnfId (hVrb.value.getVnfId ());
            cvrb.setTenantId (hVrb.value.getTenantId ());
            cvrb.setTenantCreated (hVrb.value.getTenantCreated ());
            cvrb.setVnfCreated (hVrb.value.getVnfCreated ());
        }
        return cvrb;
    }

    private CreateVnfNotification.Outputs copyCreateOutputs (Holder <Map <String, String>> hMap) {

        CreateVnfNotification.Outputs outputs = new CreateVnfNotification.Outputs ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = new HashMap <> ();
            sMap = hMap.value;
            CreateVnfNotification.Outputs.Entry entry = new CreateVnfNotification.Outputs.Entry ();

            for (String key : sMap.keySet ()) {
                entry.setKey (key);
                entry.setValue (sMap.get (key));
                outputs.getEntry ().add (entry);
            }
        }
        return outputs;
    }

    private UpdateVnfNotification.Outputs copyUpdateOutputs (Holder <Map <String, String>> hMap) {

        UpdateVnfNotification.Outputs outputs = new UpdateVnfNotification.Outputs ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = new HashMap <> ();
            sMap = hMap.value;
            UpdateVnfNotification.Outputs.Entry entry = new UpdateVnfNotification.Outputs.Entry ();

            for (Map.Entry<String,String> mapEntry : sMap.entrySet ()) {
		String key = mapEntry.getKey();
		String value = mapEntry.getValue();
                entry.setKey (key);
                entry.setValue (value);
                outputs.getEntry ().add (entry);
            }
        }
        return outputs;
    }

    private QueryVnfNotification.Outputs copyQueryOutputs (Holder <Map <String, String>> hMap) {

        QueryVnfNotification.Outputs outputs = new QueryVnfNotification.Outputs ();

        if (hMap != null && hMap.value != null) {
            Map <String, String> sMap = new HashMap <> ();
            sMap = hMap.value;

            QueryVnfNotification.Outputs.Entry entry = new QueryVnfNotification.Outputs.Entry ();

            for (Map.Entry<String,String> mapEntry : sMap.entrySet ()) {
		String key = mapEntry.getKey();
		String value = mapEntry.getValue();
                entry.setKey (key);
                entry.setValue (value);
                outputs.getEntry ().add (entry);
            }
        }
        return outputs;
    }

    private VnfAdapterNotify getNotifyEP (String notificationUrl) {

        URL warWsdlLoc = null;
        try {
            warWsdlLoc = Thread.currentThread ().getContextClassLoader ().getResource ("VnfAdapterNotify.wsdl");
        } catch (Exception e) {
            LOGGER.error (MessageEnum.RA_WSDL_NOT_FOUND, "VnfAdapterNotify.wsdl", "", "getNotifyEP", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - WSDL not found", e);
        }
        if (warWsdlLoc == null) {
        	LOGGER.error (MessageEnum.RA_WSDL_NOT_FOUND, "VnfAdapterNotify.wsdl", "", "getNotifyEP", MsoLogger.ErrorCode.BusinessProcesssError, "WSDL not found");
        } else {
            try {
                LOGGER.debug ("VnfAdpaterNotify.wsdl location:" + warWsdlLoc.toURI ().toString ());
            } catch (Exception e) {
                LOGGER.error (MessageEnum.RA_WSDL_URL_CONVENTION_EXC, "VnfAdapterNotify.wsdl", "", "getNotifyEP", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - WSDL URL convention", e);
            }
        }

        VnfAdapterNotify_Service notifySvc = new VnfAdapterNotify_Service (warWsdlLoc,
                                                                           new QName ("http://org.openecomp.mso/vnfNotify",
                                                                                      "vnfAdapterNotify"));

        VnfAdapterNotify notifyPort = notifySvc.getMsoVnfAdapterAsyncImplPort ();

        BindingProvider bp = (BindingProvider) notifyPort;

        URL epUrl = null;
        try {
            epUrl = new URL (notificationUrl);
        } catch (MalformedURLException e1) {
            LOGGER.error (MessageEnum.RA_INIT_NOTIF_EXC, "", "getNotifyEP", MsoLogger.ErrorCode.BusinessProcesssError, "MalformedURLException", e1);
        }

        if(null != epUrl) {
            LOGGER.debug ("Notification Endpoint URL: " + epUrl.toExternalForm ());
            bp.getRequestContext ().put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epUrl.toExternalForm ());
        }
        else {
            LOGGER.debug ("epUrl is NULL:");
        }

        // authentication
        try {
            Map <String, Object> reqCtx = bp.getRequestContext ();
            Map <String, List <String>> headers = new HashMap <> ();

            String userCredentials = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER).getEncryptedProperty (BPEL_AUTH_PROP,
                                                                                             "",
                                                                                             ENCRYPTION_KEY);

            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary (userCredentials.getBytes ());
            reqCtx.put (MessageContext.HTTP_REQUEST_HEADERS, headers);
            headers.put ("Authorization", Collections.singletonList (basicAuth));
        } catch (Exception e) {
            LOGGER.error (MessageEnum.RA_SET_CALLBACK_AUTH_EXC, "", "getNotifyEP", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Unable to set authorization in callback request", e);
            alarmLogger.sendAlarm ("MsoInternalError", MsoAlarmLogger.CRITICAL, "Unable to set authorization in callback request");
        }

        return notifyPort;
    }

}
