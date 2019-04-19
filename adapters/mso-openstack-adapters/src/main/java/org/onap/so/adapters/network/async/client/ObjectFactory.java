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

package org.onap.so.adapters.network.async.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * org.onap.so.adapters.network.async.client package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final String URL = "http://org.onap.so/networkNotify";
    private static final QName _RollbackNetworkNotification_QNAME = new QName(URL, "rollbackNetworkNotification");
    private static final QName _UpdateNetworkNotification_QNAME = new QName(URL, "updateNetworkNotification");
    private static final QName _QueryNetworkNotificationResponse_QNAME =
            new QName(URL, "queryNetworkNotificationResponse");
    private static final QName _UpdateNetworkNotificationResponse_QNAME =
            new QName(URL, "updateNetworkNotificationResponse");
    private static final QName _CreateNetworkNotificationResponse_QNAME =
            new QName(URL, "createNetworkNotificationResponse");
    private static final QName _DeleteNetworkNotification_QNAME = new QName(URL, "deleteNetworkNotification");
    private static final QName _DeleteNetworkNotificationResponse_QNAME =
            new QName(URL, "deleteNetworkNotificationResponse");
    private static final QName _CreateNetworkNotification_QNAME = new QName(URL, "createNetworkNotification");
    private static final QName _QueryNetworkNotification_QNAME = new QName(URL, "queryNetworkNotification");
    private static final QName _RollbackNetworkNotificationResponse_QNAME =
            new QName(URL, "rollbackNetworkNotificationResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.onap.so.adapters.network.async.client
     * 
     */
    public ObjectFactory() {}

    /**
     * Create an instance of {@link QueryNetworkNotification }
     * 
     */
    public QueryNetworkNotification createQueryNetworkNotification() {
        return new QueryNetworkNotification();
    }

    /**
     * Create an instance of {@link QueryNetworkNotification.SubnetIdMap }
     * 
     */
    public QueryNetworkNotification.SubnetIdMap createQueryNetworkNotificationSubnetIdMap() {
        return new QueryNetworkNotification.SubnetIdMap();
    }

    /**
     * Create an instance of {@link CreateNetworkNotification }
     * 
     */
    public CreateNetworkNotification createCreateNetworkNotification() {
        return new CreateNetworkNotification();
    }

    /**
     * Create an instance of {@link CreateNetworkNotification.SubnetIdMap }
     * 
     */
    public CreateNetworkNotification.SubnetIdMap createCreateNetworkNotificationSubnetIdMap() {
        return new CreateNetworkNotification.SubnetIdMap();
    }

    /**
     * Create an instance of {@link UpdateNetworkNotification }
     * 
     */
    public UpdateNetworkNotification createUpdateNetworkNotification() {
        return new UpdateNetworkNotification();
    }

    /**
     * Create an instance of {@link UpdateNetworkNotification.SubnetIdMap }
     * 
     */
    public UpdateNetworkNotification.SubnetIdMap createUpdateNetworkNotificationSubnetIdMap() {
        return new UpdateNetworkNotification.SubnetIdMap();
    }

    /**
     * Create an instance of {@link UpdateNetworkNotificationResponse }
     * 
     */
    public UpdateNetworkNotificationResponse createUpdateNetworkNotificationResponse() {
        return new UpdateNetworkNotificationResponse();
    }

    /**
     * Create an instance of {@link CreateNetworkNotificationResponse }
     * 
     */
    public CreateNetworkNotificationResponse createCreateNetworkNotificationResponse() {
        return new CreateNetworkNotificationResponse();
    }

    /**
     * Create an instance of {@link RollbackNetworkNotification }
     * 
     */
    public RollbackNetworkNotification createRollbackNetworkNotification() {
        return new RollbackNetworkNotification();
    }

    /**
     * Create an instance of {@link QueryNetworkNotificationResponse }
     * 
     */
    public QueryNetworkNotificationResponse createQueryNetworkNotificationResponse() {
        return new QueryNetworkNotificationResponse();
    }

    /**
     * Create an instance of {@link RollbackNetworkNotificationResponse }
     * 
     */
    public RollbackNetworkNotificationResponse createRollbackNetworkNotificationResponse() {
        return new RollbackNetworkNotificationResponse();
    }

    /**
     * Create an instance of {@link DeleteNetworkNotification }
     * 
     */
    public DeleteNetworkNotification createDeleteNetworkNotification() {
        return new DeleteNetworkNotification();
    }

    /**
     * Create an instance of {@link DeleteNetworkNotificationResponse }
     * 
     */
    public DeleteNetworkNotificationResponse createDeleteNetworkNotificationResponse() {
        return new DeleteNetworkNotificationResponse();
    }

    /**
     * Create an instance of {@link NetworkRollback }
     * 
     */
    public NetworkRollback createNetworkRollback() {
        return new NetworkRollback();
    }

    /**
     * Create an instance of {@link MsoRequest }
     * 
     */
    public MsoRequest createMsoRequest() {
        return new MsoRequest();
    }

    /**
     * Create an instance of {@link QueryNetworkNotification.SubnetIdMap.Entry }
     * 
     */
    public QueryNetworkNotification.SubnetIdMap.Entry createQueryNetworkNotificationSubnetIdMapEntry() {
        return new QueryNetworkNotification.SubnetIdMap.Entry();
    }

    /**
     * Create an instance of {@link CreateNetworkNotification.SubnetIdMap.Entry }
     * 
     */
    public CreateNetworkNotification.SubnetIdMap.Entry createCreateNetworkNotificationSubnetIdMapEntry() {
        return new CreateNetworkNotification.SubnetIdMap.Entry();
    }

    /**
     * Create an instance of {@link UpdateNetworkNotification.SubnetIdMap.Entry }
     * 
     */
    public UpdateNetworkNotification.SubnetIdMap.Entry createUpdateNetworkNotificationSubnetIdMapEntry() {
        return new UpdateNetworkNotification.SubnetIdMap.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RollbackNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "rollbackNetworkNotification")
    public JAXBElement<RollbackNetworkNotification> createRollbackNetworkNotification(
            RollbackNetworkNotification value) {
        return new JAXBElement<>(_RollbackNetworkNotification_QNAME, RollbackNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "updateNetworkNotification")
    public JAXBElement<UpdateNetworkNotification> createUpdateNetworkNotification(UpdateNetworkNotification value) {
        return new JAXBElement<>(_UpdateNetworkNotification_QNAME, UpdateNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "queryNetworkNotificationResponse")
    public JAXBElement<QueryNetworkNotificationResponse> createQueryNetworkNotificationResponse(
            QueryNetworkNotificationResponse value) {
        return new JAXBElement<>(_QueryNetworkNotificationResponse_QNAME, QueryNetworkNotificationResponse.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "updateNetworkNotificationResponse")
    public JAXBElement<UpdateNetworkNotificationResponse> createUpdateNetworkNotificationResponse(
            UpdateNetworkNotificationResponse value) {
        return new JAXBElement<>(_UpdateNetworkNotificationResponse_QNAME, UpdateNetworkNotificationResponse.class,
                null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "createNetworkNotificationResponse")
    public JAXBElement<CreateNetworkNotificationResponse> createCreateNetworkNotificationResponse(
            CreateNetworkNotificationResponse value) {
        return new JAXBElement<>(_CreateNetworkNotificationResponse_QNAME, CreateNetworkNotificationResponse.class,
                null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "deleteNetworkNotification")
    public JAXBElement<DeleteNetworkNotification> createDeleteNetworkNotification(DeleteNetworkNotification value) {
        return new JAXBElement<>(_DeleteNetworkNotification_QNAME, DeleteNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "deleteNetworkNotificationResponse")
    public JAXBElement<DeleteNetworkNotificationResponse> createDeleteNetworkNotificationResponse(
            DeleteNetworkNotificationResponse value) {
        return new JAXBElement<>(_DeleteNetworkNotificationResponse_QNAME, DeleteNetworkNotificationResponse.class,
                null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "createNetworkNotification")
    public JAXBElement<CreateNetworkNotification> createCreateNetworkNotification(CreateNetworkNotification value) {
        return new JAXBElement<>(_CreateNetworkNotification_QNAME, CreateNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "queryNetworkNotification")
    public JAXBElement<QueryNetworkNotification> createQueryNetworkNotification(QueryNetworkNotification value) {
        return new JAXBElement<>(_QueryNetworkNotification_QNAME, QueryNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RollbackNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = URL, name = "rollbackNetworkNotificationResponse")
    public JAXBElement<RollbackNetworkNotificationResponse> createRollbackNetworkNotificationResponse(
            RollbackNetworkNotificationResponse value) {
        return new JAXBElement<>(_RollbackNetworkNotificationResponse_QNAME, RollbackNetworkNotificationResponse.class,
                null, value);
    }

}
