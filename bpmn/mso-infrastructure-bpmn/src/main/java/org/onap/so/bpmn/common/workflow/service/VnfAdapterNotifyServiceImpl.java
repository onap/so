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

package org.onap.so.bpmn.common.workflow.service;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.ws.rs.core.Context;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.WebServiceContext;
import org.onap.so.bpmn.common.adapter.vnf.CreateVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.DeleteVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.MsoExceptionCategory;
import org.onap.so.bpmn.common.adapter.vnf.QueryVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.RollbackVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.UpdateVnfNotification;
import org.onap.so.bpmn.common.adapter.vnf.VnfAdapterNotify;
import org.onap.so.bpmn.common.adapter.vnf.VnfRollback;
import org.onap.so.bpmn.common.adapter.vnf.VnfStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the VnfAdapterNotify service.
 */
@WebService(serviceName = "vnfAdapterNotify", targetNamespace = "http://org.onap.so/vnfNotify")
@Service
public class VnfAdapterNotifyServiceImpl extends ProcessEngineAwareService implements VnfAdapterNotify {

    private final String logMarker = "[VNF-NOTIFY]";

    @Autowired
    CallbackHandlerService callback;

    @Context
    WebServiceContext wsContext;

    @WebMethod(operationName = "rollbackVnfNotification")
    @Oneway
    @RequestWrapper(localName = "rollbackVnfNotification", targetNamespace = "http://org.onap.so/vnfNotify",
            className = "org.onap.so.adapters.vnf.async.client.RollbackVnfNotification")
    @Action(input = "http://org.onap.so/notify/adapterNotify/rollbackVnfNotificationRequest")
    public void rollbackVnfNotification(@WebParam(name = "messageId", targetNamespace = "") String messageId,
            @WebParam(name = "completed", targetNamespace = "") boolean completed,
            @WebParam(name = "exception", targetNamespace = "") MsoExceptionCategory exception,
            @WebParam(name = "errorMessage", targetNamespace = "") String errorMessage) {

        RollbackVnfNotification rollbackVnfNotification = new RollbackVnfNotification();

        rollbackVnfNotification.setMessageId(messageId);
        rollbackVnfNotification.setCompleted(completed);
        rollbackVnfNotification.setException(exception);
        rollbackVnfNotification.setErrorMessage(errorMessage);

        String method = "rollbackVnfNotification";
        Object message = rollbackVnfNotification;
        String messageEventName = "rollbackVnfNotificationCallback";
        String messageVariable = "rollbackVnfNotificationCallback";
        String correlationVariable = "VNFRB_messageId";
        String correlationValue = messageId;

        callback.handleCallback(method, message, messageEventName, messageVariable, correlationVariable,
                correlationValue, logMarker, null);
    }

    @WebMethod(operationName = "queryVnfNotification")
    @Oneway
    @RequestWrapper(localName = "queryVnfNotification", targetNamespace = "http://org.onap.so/vnfNotify",
            className = "org.onap.so.adapters.vnf.async.client.QueryVnfNotification")
    @Action(input = "http://org.onap.so/notify/adapterNotify/queryVnfNotificationRequest")
    public void queryVnfNotification(@WebParam(name = "messageId", targetNamespace = "") String messageId,
            @WebParam(name = "completed", targetNamespace = "") boolean completed,
            @WebParam(name = "exception", targetNamespace = "") MsoExceptionCategory exception,
            @WebParam(name = "errorMessage", targetNamespace = "") String errorMessage,
            @WebParam(name = "vnfExists", targetNamespace = "") Boolean vnfExists,
            @WebParam(name = "vnfId", targetNamespace = "") String vnfId,
            @WebParam(name = "status", targetNamespace = "") VnfStatus status,
            @WebParam(name = "outputs", targetNamespace = "") QueryVnfNotification.Outputs outputs) {

        String method = "queryVnfNotification";
        String messageEventName = "queryVnfNotificationCallback";
        String messageVariable = "queryVnfNotificationCallback";
        String correlationVariable = "VNFQ_messageId";
        String correlationValue = messageId;

        QueryVnfNotification message = new QueryVnfNotification();

        message.setMessageId(messageId);
        message.setCompleted(completed);
        message.setException(exception);
        message.setErrorMessage(errorMessage);
        message.setVnfExists(vnfExists);
        message.setVnfId(vnfId);
        message.setStatus(status);
        message.setOutputs(outputs);

        callback.handleCallback(method, message, messageEventName, messageVariable, correlationVariable,
                correlationValue, logMarker, null);
    }

    @WebMethod(operationName = "createVnfNotification")
    @Oneway
    @RequestWrapper(localName = "createVnfNotification", targetNamespace = "http://org.onap.so/vnfNotify",
            className = "org.onap.so.adapters.vnf.async.client.CreateVnfNotification")
    @Action(input = "http://org.onap.so/notify/adapterNotify/createVnfNotificationRequest")
    public void createVnfNotification(@WebParam(name = "messageId", targetNamespace = "") String messageId,
            @WebParam(name = "completed", targetNamespace = "") boolean completed,
            @WebParam(name = "exception", targetNamespace = "") MsoExceptionCategory exception,
            @WebParam(name = "errorMessage", targetNamespace = "") String errorMessage,
            @WebParam(name = "vnfId", targetNamespace = "") String vnfId,
            @WebParam(name = "outputs", targetNamespace = "") CreateVnfNotification.Outputs outputs,
            @WebParam(name = "rollback", targetNamespace = "") VnfRollback rollback) {

        String method = "createVnfNotification";
        String messageEventName = "createVnfNotificationCallback";
        String messageVariable = "createVnfNotificationCallback";
        String correlationVariable = "VNFC_messageId";
        String correlationValue = messageId;

        CreateVnfNotification message = new CreateVnfNotification();

        message.setMessageId(messageId);
        message.setCompleted(completed);
        message.setException(exception);
        message.setErrorMessage(errorMessage);
        message.setVnfId(vnfId);
        message.setOutputs(outputs);
        message.setRollback(rollback);

        callback.handleCallback(method, message, messageEventName, messageVariable, correlationVariable,
                correlationValue, logMarker, null);
    }

    @WebMethod(operationName = "updateVnfNotification")
    @Oneway
    @RequestWrapper(localName = "updateVnfNotification", targetNamespace = "http://org.onap.so/vnfNotify",
            className = "org.onap.so.adapters.vnf.async.client.UpdateVnfNotification")
    @Action(input = "http://org.onap.so/notify/adapterNotify/updateVnfNotificationRequest")
    public void updateVnfNotification(@WebParam(name = "messageId", targetNamespace = "") String messageId,
            @WebParam(name = "completed", targetNamespace = "") boolean completed,
            @WebParam(name = "exception", targetNamespace = "") MsoExceptionCategory exception,
            @WebParam(name = "errorMessage", targetNamespace = "") String errorMessage,
            @WebParam(name = "outputs", targetNamespace = "") UpdateVnfNotification.Outputs outputs,
            @WebParam(name = "rollback", targetNamespace = "") VnfRollback rollback) {

        String method = "updateVnfNotification";
        String messageEventName = "updateVnfNotificationCallback";
        String messageVariable = "updateVnfNotificationCallback";
        String correlationVariable = "VNFU_messageId";
        String correlationValue = messageId;

        UpdateVnfNotification message = new UpdateVnfNotification();

        message.setMessageId(messageId);
        message.setCompleted(completed);
        message.setException(exception);
        message.setErrorMessage(errorMessage);
        message.setOutputs(outputs);
        message.setRollback(rollback);

        callback.handleCallback(method, message, messageEventName, messageVariable, correlationVariable,
                correlationValue, logMarker, null);
    }

    @WebMethod(operationName = "deleteVnfNotification")
    @Oneway
    @RequestWrapper(localName = "deleteVnfNotification", targetNamespace = "http://org.onap.so/vnfNotify",
            className = "org.onap.so.adapters.vnf.async.client.DeleteVnfNotification")
    @Action(input = "http://org.onap.so/notify/adapterNotify/deleteVnfNotificationRequest")
    public void deleteVnfNotification(@WebParam(name = "messageId", targetNamespace = "") String messageId,
            @WebParam(name = "completed", targetNamespace = "") boolean completed,
            @WebParam(name = "exception", targetNamespace = "") MsoExceptionCategory exception,
            @WebParam(name = "errorMessage", targetNamespace = "") String errorMessage) {

        String method = "deleteVnfNotification";
        String messageEventName = "deleteVnfACallback";
        String messageVariable = "deleteVnfACallback";
        String correlationVariable = "VNFDEL_uuid";
        String correlationValue = messageId;

        DeleteVnfNotification message = new DeleteVnfNotification();

        message.setMessageId(messageId);
        message.setCompleted(completed);
        message.setException(exception);
        message.setErrorMessage(errorMessage);

        callback.handleCallback(method, message, messageEventName, messageVariable, correlationVariable,
                correlationValue, logMarker, null);
    }
}
