/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.network.async.client;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b14002
 * Generated source version: 2.2
 * 
 */
@WebService(name = "networkAdapterNotify", targetNamespace = "http://org.openecomp.mso/networkNotify")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface NetworkAdapterNotify {


    /**
     * 
     * @param exception
     * @param errorMessage
     * @param messageId
     * @param completed
     */
    @WebMethod
    @RequestWrapper(localName = "rollbackNetworkNotification", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.RollbackNetworkNotification")
    @ResponseWrapper(localName = "rollbackNetworkNotificationResponse", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.RollbackNetworkNotificationResponse")
    @Action(input = "http://org.openecomp.mso/notify/adapterNotify/rollbackNetworkNotificationRequest", output = "http://org.openecomp.mso/notify/adapterNotify/rollbackNetworkNotificationResponse")
    public void rollbackNetworkNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage);

    /**
     * 
     * @param exception
     * @param vlans
     * @param networkExists
     * @param errorMessage
     * @param messageId
     * @param networkId
     * @param completed
     * @param neutronNetworkId
     * @param status
     * @param subnetIdMap
     */
    @WebMethod
    @RequestWrapper(localName = "queryNetworkNotification", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.QueryNetworkNotification")
    @ResponseWrapper(localName = "queryNetworkNotificationResponse", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.QueryNetworkNotificationResponse")
    @Action(input = "http://org.openecomp.mso/notify/adapterNotify/queryNetworkNotificationRequest", output = "http://org.openecomp.mso/notify/adapterNotify/queryNetworkNotificationResponse")
    public void queryNetworkNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "networkExists", targetNamespace = "")
        Boolean networkExists,
        @WebParam(name = "networkId", targetNamespace = "")
        String networkId,
        @WebParam(name = "neutronNetworkId", targetNamespace = "")
        String neutronNetworkId,
        @WebParam(name = "status", targetNamespace = "")
        NetworkStatus status,
        @WebParam(name = "vlans", targetNamespace = "")
        List<Integer> vlans,
        @WebParam(name = "subnetIdMap", targetNamespace = "")
        org.openecomp.mso.adapters.network.async.client.QueryNetworkNotification.SubnetIdMap subnetIdMap);

    /**
     * 
     * @param exception
     * @param rollback
     * @param errorMessage
     * @param messageId
     * @param networkId
     * @param completed
     * @param neutronNetworkId
     * @param subnetIdMap
     */
    @WebMethod
    @RequestWrapper(localName = "createNetworkNotification", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.CreateNetworkNotification")
    @ResponseWrapper(localName = "createNetworkNotificationResponse", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.CreateNetworkNotificationResponse")
    @Action(input = "http://org.openecomp.mso/notify/adapterNotify/createNetworkNotificationRequest", output = "http://org.openecomp.mso/notify/adapterNotify/createNetworkNotificationResponse")
    public void createNetworkNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "networkId", targetNamespace = "")
        String networkId,
        @WebParam(name = "neutronNetworkId", targetNamespace = "")
        String neutronNetworkId,
        @WebParam(name = "subnetIdMap", targetNamespace = "")
        org.openecomp.mso.adapters.network.async.client.CreateNetworkNotification.SubnetIdMap subnetIdMap,
        @WebParam(name = "rollback", targetNamespace = "")
        NetworkRollback rollback);

    /**
     * 
     * @param exception
     * @param networkDeleted
     * @param errorMessage
     * @param messageId
     * @param completed
     */
    @WebMethod
    @RequestWrapper(localName = "deleteNetworkNotification", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.DeleteNetworkNotification")
    @ResponseWrapper(localName = "deleteNetworkNotificationResponse", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.DeleteNetworkNotificationResponse")
    @Action(input = "http://org.openecomp.mso/notify/adapterNotify/deleteNetworkNotificationRequest", output = "http://org.openecomp.mso/notify/adapterNotify/deleteNetworkNotificationResponse")
    public void deleteNetworkNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "networkDeleted", targetNamespace = "")
        Boolean networkDeleted);

    /**
     * 
     * @param exception
     * @param rollback
     * @param errorMessage
     * @param messageId
     * @param completed
     * @param subnetIdMap
     */
    @WebMethod
    @RequestWrapper(localName = "updateNetworkNotification", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.UpdateNetworkNotification")
    @ResponseWrapper(localName = "updateNetworkNotificationResponse", targetNamespace = "http://org.openecomp.mso/networkNotify", className = "org.openecomp.mso.adapters.network.async.client.UpdateNetworkNotificationResponse")
    @Action(input = "http://org.openecomp.mso/notify/adapterNotify/updateNetworkNotificationRequest", output = "http://org.openecomp.mso/notify/adapterNotify/updateNetworkNotificationResponse")
    public void updateNetworkNotification(
        @WebParam(name = "messageId", targetNamespace = "")
        String messageId,
        @WebParam(name = "completed", targetNamespace = "")
        boolean completed,
        @WebParam(name = "exception", targetNamespace = "")
        MsoExceptionCategory exception,
        @WebParam(name = "errorMessage", targetNamespace = "")
        String errorMessage,
        @WebParam(name = "subnetIdMap", targetNamespace = "")
        org.openecomp.mso.adapters.network.async.client.UpdateNetworkNotification.SubnetIdMap subnetIdMap,
        @WebParam(name = "rollback", targetNamespace = "")
        NetworkRollback rollback);

}
