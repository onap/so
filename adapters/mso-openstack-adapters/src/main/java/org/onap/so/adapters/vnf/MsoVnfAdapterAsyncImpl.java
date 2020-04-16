/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.vnf;


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
import org.onap.so.adapters.vnf.async.client.CreateVnfNotification;
import org.onap.so.adapters.vnf.async.client.VnfAdapterNotify;
import org.onap.so.adapters.vnf.async.client.VnfAdapterNotify_Service;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.entity.MsoRequest;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@WebService(serviceName = "VnfAdapterAsync", endpointInterface = "org.onap.so.adapters.vnf.MsoVnfAdapterAsync",
        targetNamespace = "http://org.onap.so/vnfA")
@Component
public class MsoVnfAdapterAsyncImpl implements MsoVnfAdapterAsync {

    private static final Logger logger = LoggerFactory.getLogger(MsoVnfAdapterAsyncImpl.class);

    private static final String BPEL_AUTH_PROP = "org.onap.so.adapters.vnf.bpelauth";
    private static final String ENCRYPTION_KEY_PROP = "mso.msoKey";

    @Autowired
    private Environment environment;

    @Autowired
    private MsoVnfAdapterImpl vnfImpl;

    /**
     * Health Check web method. Does nothing but return to show the adapter is deployed.
     */
    @Override
    public void healthCheckA() {
        logger.debug("Health check call in VNF Adapter");
    }

    /**
     * This is the asynchronous "Create VNF" web service implementation. It will create a new VNF of the requested type
     * in the specified cloud and tenant. The tenant must exist before this service is called.
     *
     * If a VNF with the same name already exists, this can be considered a success or failure, depending on the value
     * of the 'failIfExists' parameter.
     *
     * All VNF types will be defined in the MSO catalog. The caller must request one of these pre-defined types or an
     * error will be returned. Within the catalog, each VNF type references (among other things) a Heat template which
     * is used to deploy the required VNF artifacts (VMs, networks, etc.) to the cloud.
     *
     * Depending on the Heat template, a variable set of input parameters will be defined, some of which are required.
     * The caller is responsible to pass the necessary input data for the VNF or an error will be thrown.
     *
     * The method sends an asynchronous response to the notification URL when processing completes. The
     * createAsyncResponse contains the vnfId (the canonical name of the stack), a Map of VNF output attributes, and a
     * VnfRollback object. This last object can be passed as-is to the rollbackVnf operation to undo everything that was
     * created for the VNF. This is useful if a VNF is successfully created but the orchestrator fails on a subsequent
     * operation.
     *
     * Note: this method is implemented by calling the synchronous web method and translating the response to an
     * asynchronous notification.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to create the VNF
     * @param cloudOwner cloud owner of the cloud site in which to create the VNF
     * @param tenantId Openstack tenant identifier
     * @param vnfType VNF type key, should match a VNF definition in catalog DB
     * @param vnfName Name to be assigned to the new VNF
     * @param inputs Map of key=value inputs for VNF stack creation
     * @param failIfExists Flag whether already existing VNF should be considered a success or failure
     * @param msoRequest Request tracking information for logs
     * @param notificationURL the target URL for asynchronous response
     */
    @Override
    public void createVnfA(String cloudSiteId, String cloudOwner, String tenantId, String vnfType, String vnfVersion,
            String vnfName, String requestType, String volumeGroupHeatStackId, Map<String, Object> inputs,
            Boolean failIfExists, Boolean backout, Boolean enableBridge, String messageId, MsoRequest msoRequest,
            String notificationUrl) {

        logger.info("{} createVnfA", MessageEnum.RA_ASYNC_CREATE_VNF);
        // Synchronous Web Service Outputs
        Holder<String> vnfId = new Holder<>();
        Holder<Map<String, String>> outputs = new Holder<>();
        Holder<VnfRollback> vnfRollback = new Holder<>();

        try {
            vnfImpl.createVnf(cloudSiteId, cloudOwner, tenantId, vnfType, vnfVersion, vnfName, requestType,
                    volumeGroupHeatStackId, inputs, failIfExists, backout, enableBridge, msoRequest, vnfId, outputs,
                    vnfRollback);
        } catch (VnfException e) {
            logger.error("{} {} VnfException in createVnfA ", MessageEnum.RA_CREATE_VNF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), e);
            org.onap.so.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = org.onap.so.adapters.vnf.async.client.MsoExceptionCategory
                        .fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error("{} {} Exception - Fault info ", MessageEnum.RA_FAULT_INFO_EXC,
                        ErrorCode.BusinessProcessError.getValue(), e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.createVnfNotification(messageId, false, exCat, eMsg, null, null, null);
            } catch (Exception e1) {
                logger.error("{} {} Exception sending createVnf notification ", MessageEnum.RA_SEND_VNF_NOTIF_ERR,
                        ErrorCode.BusinessProcessError.getValue(), e1);
            }
            logger.info(LoggingAnchor.ONE, MessageEnum.RA_ASYNC_CREATE_VNF_COMPLETE);
            return;
        }
        logger.debug("Async Create VNF: {} VnfId:{}", vnfName, vnfId.value);
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.createVnfNotification(messageId, true, null, null, vnfId.value, copyCreateOutputs(outputs),
                    copyVrb(vnfRollback));
        } catch (Exception e) {
            logger.error("{} {} Exception sending createVnf notification ", MessageEnum.RA_SEND_VNF_NOTIF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), e);
        }
        logger.info("{} createVnfA", MessageEnum.RA_ASYNC_CREATE_VNF_COMPLETE);
        return;
    }

    /**
     * This is the Asynchronous "Delete VNF" web service implementation. It will delete a VNF by name or ID in the
     * specified cloud and tenant.
     *
     * The method has no outputs.
     *
     * @param cloudSiteId CLLI code of the cloud site in which to delete
     * @param cloudOwner cloud owner of cloud site in which to delete
     * @param tenantId Openstack tenant identifier
     * @param vnfName VNF Name or Openstack ID
     * @param msoRequest Request tracking information for logs
     * @param notificationURL the target URL for asynchronous response
     */
    @Override
    public void deleteVnfA(String cloudSiteId, String cloudOwner, String tenantId, String vnfName, String messageId,
            MsoRequest msoRequest, String notificationUrl) {

        logger.info(LoggingAnchor.ONE, MessageEnum.RA_ASYNC_DELETE_VNF);

        try {
            vnfImpl.deleteVnf(cloudSiteId, cloudOwner, tenantId, vnfName, msoRequest);
        } catch (VnfException e) {
            logger.error("{} {} Exception sending deleteVnfA notification ", MessageEnum.RA_DELETE_VNF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), e);
            org.onap.so.adapters.vnf.async.client.MsoExceptionCategory exCat = null;
            String eMsg = null;
            try {
                eMsg = e.getFaultInfo().getMessage();
                exCat = org.onap.so.adapters.vnf.async.client.MsoExceptionCategory
                        .fromValue(e.getFaultInfo().getCategory().name());
            } catch (Exception e1) {
                logger.error("{} {} Exception - fault info ", MessageEnum.RA_FAULT_INFO_EXC,
                        ErrorCode.BusinessProcessError.getValue(), e1);
            }
            // Build and send Asynchronous error response
            try {
                VnfAdapterNotify notifyPort = getNotifyEP(notificationUrl);
                notifyPort.deleteVnfNotification(messageId, false, exCat, eMsg);
            } catch (Exception e1) {
                logger.error("{} {} Exception sending deleteVnfA notification ", MessageEnum.RA_SEND_VNF_NOTIF_ERR,
                        ErrorCode.BusinessProcessError.getValue(), e1);
            }
            logger.info("{} deleteVnfA", MessageEnum.RA_ASYNC_DELETE_VNF_COMPLETE);
            return;
        }

        logger.debug("Async Delete VNF: {}", vnfName);
        // Build and send Asynchronous response
        try {
            VnfAdapterNotify notifyPort = getNotifyEP(notificationUrl);
            notifyPort.deleteVnfNotification(messageId, true, null, null);

        } catch (Exception e) {
            logger.error("{} {} Exception sending deleteVnfA notification ", MessageEnum.RA_SEND_VNF_NOTIF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), e);
        }

        logger.info("{} deleteVnfA", MessageEnum.RA_ASYNC_DELETE_VNF_COMPLETE);
        return;
    }

    private org.onap.so.adapters.vnf.async.client.VnfRollback copyVrb(Holder<VnfRollback> hVrb) {
        org.onap.so.adapters.vnf.async.client.VnfRollback cvrb =
                new org.onap.so.adapters.vnf.async.client.VnfRollback();

        if (hVrb != null && hVrb.value != null) {
            org.onap.so.adapters.vnf.async.client.MsoRequest cmr =
                    new org.onap.so.adapters.vnf.async.client.MsoRequest();

            cvrb.setCloudSiteId(hVrb.value.getCloudSiteId());
            if (hVrb.value.getMsoRequest() != null) {
                cmr.setRequestId(hVrb.value.getMsoRequest().getRequestId());
                cmr.setServiceInstanceId(hVrb.value.getMsoRequest().getServiceInstanceId());
            } else {
                cmr.setRequestId(null);
                cmr.setServiceInstanceId(null);
            }
            cvrb.setMsoRequest(cmr);
            cvrb.setVnfId(hVrb.value.getVnfId());
            cvrb.setTenantId(hVrb.value.getTenantId());
            cvrb.setTenantCreated(hVrb.value.getTenantCreated());
            cvrb.setVnfCreated(hVrb.value.getVnfCreated());
        }
        return cvrb;
    }

    private CreateVnfNotification.Outputs copyCreateOutputs(Holder<Map<String, String>> hMap) {

        CreateVnfNotification.Outputs outputs = new CreateVnfNotification.Outputs();

        if (hMap != null && hMap.value != null) {
            Map<String, String> sMap;
            sMap = hMap.value;
            CreateVnfNotification.Outputs.Entry entry = new CreateVnfNotification.Outputs.Entry();

            for (String key : sMap.keySet()) {
                entry.setKey(key);
                entry.setValue(sMap.get(key));
                outputs.getEntry().add(entry);
            }
        }
        return outputs;
    }

    private VnfAdapterNotify getNotifyEP(String notificationUrl) {

        URL warWsdlLoc = null;
        try {
            warWsdlLoc = Thread.currentThread().getContextClassLoader().getResource("VnfAdapterNotify.wsdl");
        } catch (Exception e) {
            logger.error("{} {} Exception - WSDL not found ", MessageEnum.RA_WSDL_NOT_FOUND,
                    ErrorCode.BusinessProcessError.getValue(), e);
        }
        if (warWsdlLoc == null) {
            logger.error("{} {} WSDL not found", MessageEnum.RA_WSDL_NOT_FOUND,
                    ErrorCode.BusinessProcessError.getValue());
        } else {
            try {
                logger.debug("VnfAdpaterNotify.wsdl location:{}", warWsdlLoc.toURI().toString());
            } catch (Exception e) {
                logger.error("{} {} Exception - WSDL URL convention ", MessageEnum.RA_WSDL_URL_CONVENTION_EXC,
                        ErrorCode.BusinessProcessError.getValue(), e);
            }
        }

        VnfAdapterNotify_Service notifySvc =
                new VnfAdapterNotify_Service(warWsdlLoc, new QName("http://org.onap.so/vnfNotify", "vnfAdapterNotify"));

        VnfAdapterNotify notifyPort = notifySvc.getMsoVnfAdapterAsyncImplPort();

        BindingProvider bp = (BindingProvider) notifyPort;

        URL epUrl = null;
        try {
            epUrl = new URL(notificationUrl);
        } catch (MalformedURLException e1) {
            logger.error("{} {} MalformedURLException ", MessageEnum.RA_INIT_NOTIF_EXC,
                    ErrorCode.BusinessProcessError.getValue(), e1);
        }

        if (null != epUrl) {
            logger.debug("Notification Endpoint URL: {}", epUrl.toExternalForm());
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epUrl.toExternalForm());
        } else {
            logger.debug("epUrl is NULL:");
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
            logger.error("{} {} Exception - Unable to set authorization in callback request ",
                    MessageEnum.RA_SET_CALLBACK_AUTH_EXC, ErrorCode.BusinessProcessError.getValue(), e);
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

}
