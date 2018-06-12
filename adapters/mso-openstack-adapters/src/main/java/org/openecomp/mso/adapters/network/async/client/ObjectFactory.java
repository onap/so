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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openecomp.mso.adapters.network.async.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RollbackNetworkNotification_QNAME = new QName("http://org.openecomp.mso/networkNotify", "rollbackNetworkNotification");
    private final static QName _UpdateNetworkNotification_QNAME = new QName("http://org.openecomp.mso/networkNotify", "updateNetworkNotification");
    private final static QName _QueryNetworkNotificationResponse_QNAME = new QName("http://org.openecomp.mso/networkNotify", "queryNetworkNotificationResponse");
    private final static QName _UpdateNetworkNotificationResponse_QNAME = new QName("http://org.openecomp.mso/networkNotify", "updateNetworkNotificationResponse");
    private final static QName _CreateNetworkNotificationResponse_QNAME = new QName("http://org.openecomp.mso/networkNotify", "createNetworkNotificationResponse");
    private final static QName _DeleteNetworkNotification_QNAME = new QName("http://org.openecomp.mso/networkNotify", "deleteNetworkNotification");
    private final static QName _DeleteNetworkNotificationResponse_QNAME = new QName("http://org.openecomp.mso/networkNotify", "deleteNetworkNotificationResponse");
    private final static QName _CreateNetworkNotification_QNAME = new QName("http://org.openecomp.mso/networkNotify", "createNetworkNotification");
    private final static QName _QueryNetworkNotification_QNAME = new QName("http://org.openecomp.mso/networkNotify", "queryNetworkNotification");
    private final static QName _RollbackNetworkNotificationResponse_QNAME = new QName("http://org.openecomp.mso/networkNotify", "rollbackNetworkNotificationResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openecomp.mso.adapters.network.async.client
     * 
     */
    public ObjectFactory() {
    }

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
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "rollbackNetworkNotification")
    public JAXBElement<RollbackNetworkNotification> createRollbackNetworkNotification(RollbackNetworkNotification value) {
        return new JAXBElement<RollbackNetworkNotification>(_RollbackNetworkNotification_QNAME, RollbackNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "updateNetworkNotification")
    public JAXBElement<UpdateNetworkNotification> createUpdateNetworkNotification(UpdateNetworkNotification value) {
        return new JAXBElement<UpdateNetworkNotification>(_UpdateNetworkNotification_QNAME, UpdateNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "queryNetworkNotificationResponse")
    public JAXBElement<QueryNetworkNotificationResponse> createQueryNetworkNotificationResponse(QueryNetworkNotificationResponse value) {
        return new JAXBElement<QueryNetworkNotificationResponse>(_QueryNetworkNotificationResponse_QNAME, QueryNetworkNotificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "updateNetworkNotificationResponse")
    public JAXBElement<UpdateNetworkNotificationResponse> createUpdateNetworkNotificationResponse(UpdateNetworkNotificationResponse value) {
        return new JAXBElement<UpdateNetworkNotificationResponse>(_UpdateNetworkNotificationResponse_QNAME, UpdateNetworkNotificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "createNetworkNotificationResponse")
    public JAXBElement<CreateNetworkNotificationResponse> createCreateNetworkNotificationResponse(CreateNetworkNotificationResponse value) {
        return new JAXBElement<CreateNetworkNotificationResponse>(_CreateNetworkNotificationResponse_QNAME, CreateNetworkNotificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "deleteNetworkNotification")
    public JAXBElement<DeleteNetworkNotification> createDeleteNetworkNotification(DeleteNetworkNotification value) {
        return new JAXBElement<DeleteNetworkNotification>(_DeleteNetworkNotification_QNAME, DeleteNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "deleteNetworkNotificationResponse")
    public JAXBElement<DeleteNetworkNotificationResponse> createDeleteNetworkNotificationResponse(DeleteNetworkNotificationResponse value) {
        return new JAXBElement<DeleteNetworkNotificationResponse>(_DeleteNetworkNotificationResponse_QNAME, DeleteNetworkNotificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "createNetworkNotification")
    public JAXBElement<CreateNetworkNotification> createCreateNetworkNotification(CreateNetworkNotification value) {
        return new JAXBElement<CreateNetworkNotification>(_CreateNetworkNotification_QNAME, CreateNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryNetworkNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "queryNetworkNotification")
    public JAXBElement<QueryNetworkNotification> createQueryNetworkNotification(QueryNetworkNotification value) {
        return new JAXBElement<QueryNetworkNotification>(_QueryNetworkNotification_QNAME, QueryNetworkNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RollbackNetworkNotificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.openecomp.mso/networkNotify", name = "rollbackNetworkNotificationResponse")
    public JAXBElement<RollbackNetworkNotificationResponse> createRollbackNetworkNotificationResponse(RollbackNetworkNotificationResponse value) {
        return new JAXBElement<RollbackNetworkNotificationResponse>(_RollbackNetworkNotificationResponse_QNAME, RollbackNetworkNotificationResponse.class, null, value);
    }

}
