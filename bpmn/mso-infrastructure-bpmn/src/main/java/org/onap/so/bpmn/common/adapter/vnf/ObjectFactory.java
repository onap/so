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

package org.onap.so.bpmn.common.adapter.vnf;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * org.onap.so.adapters.vnf.async.client package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {
    private static final String VNF_NOTIFY = "http://org.onap.so/vnfNotify";
    private static final QName _RollbackVnfNotification_QNAME = new QName(VNF_NOTIFY, "rollbackVnfNotification");
    private static final QName _DeleteVnfNotification_QNAME = new QName(VNF_NOTIFY, "deleteVnfNotification");
    private static final QName _CreateVnfNotification_QNAME = new QName(VNF_NOTIFY, "createVnfNotification");
    private static final QName _UpdateVnfNotification_QNAME = new QName(VNF_NOTIFY, "updateVnfNotification");
    private static final QName _QueryVnfNotification_QNAME = new QName(VNF_NOTIFY, "queryVnfNotification");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.onap.so.adapters.vnf.async.client
     * 
     */
    public ObjectFactory() {}

    /**
     * Create an instance of {@link UpdateVnfNotification }
     * 
     */
    public UpdateVnfNotification createUpdateVnfNotification() {
        return new UpdateVnfNotification();
    }

    /**
     * Create an instance of {@link UpdateVnfNotification.Outputs }
     * 
     */
    public UpdateVnfNotification.Outputs createUpdateVnfNotificationOutputs() {
        return new UpdateVnfNotification.Outputs();
    }

    /**
     * Create an instance of {@link QueryVnfNotification }
     * 
     */
    public QueryVnfNotification createQueryVnfNotification() {
        return new QueryVnfNotification();
    }

    /**
     * Create an instance of {@link QueryVnfNotification.Outputs }
     * 
     */
    public QueryVnfNotification.Outputs createQueryVnfNotificationOutputs() {
        return new QueryVnfNotification.Outputs();
    }

    /**
     * Create an instance of {@link CreateVnfNotification }
     * 
     */
    public CreateVnfNotification createCreateVnfNotification() {
        return new CreateVnfNotification();
    }

    /**
     * Create an instance of {@link CreateVnfNotification.Outputs }
     * 
     */
    public CreateVnfNotification.Outputs createCreateVnfNotificationOutputs() {
        return new CreateVnfNotification.Outputs();
    }

    /**
     * Create an instance of {@link DeleteVnfNotification }
     * 
     */
    public DeleteVnfNotification createDeleteVnfNotification() {
        return new DeleteVnfNotification();
    }

    /**
     * Create an instance of {@link RollbackVnfNotification }
     * 
     */
    public RollbackVnfNotification createRollbackVnfNotification() {
        return new RollbackVnfNotification();
    }

    /**
     * Create an instance of {@link MsoRequest }
     * 
     */
    public MsoRequest createMsoRequest() {
        return new MsoRequest();
    }

    /**
     * Create an instance of {@link VnfRollback }
     * 
     */
    public VnfRollback createVnfRollback() {
        return new VnfRollback();
    }

    /**
     * Create an instance of {@link UpdateVnfNotification.Outputs.Entry }
     * 
     */
    public UpdateVnfNotification.Outputs.Entry createUpdateVnfNotificationOutputsEntry() {
        return new UpdateVnfNotification.Outputs.Entry();
    }

    /**
     * Create an instance of {@link QueryVnfNotification.Outputs.Entry }
     * 
     */
    public QueryVnfNotification.Outputs.Entry createQueryVnfNotificationOutputsEntry() {
        return new QueryVnfNotification.Outputs.Entry();
    }

    /**
     * Create an instance of {@link CreateVnfNotification.Outputs.Entry }
     * 
     */
    public CreateVnfNotification.Outputs.Entry createCreateVnfNotificationOutputsEntry() {
        return new CreateVnfNotification.Outputs.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RollbackVnfNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.onap.so/vnfNotify", name = "rollbackVnfNotification")
    public JAXBElement<RollbackVnfNotification> createRollbackVnfNotification(RollbackVnfNotification value) {
        return new JAXBElement<>(_RollbackVnfNotification_QNAME, RollbackVnfNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteVnfNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.onap.so/vnfNotify", name = "deleteVnfNotification")
    public JAXBElement<DeleteVnfNotification> createDeleteVnfNotification(DeleteVnfNotification value) {
        return new JAXBElement<>(_DeleteVnfNotification_QNAME, DeleteVnfNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateVnfNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.onap.so/vnfNotify", name = "createVnfNotification")
    public JAXBElement<CreateVnfNotification> createCreateVnfNotification(CreateVnfNotification value) {
        return new JAXBElement<>(_CreateVnfNotification_QNAME, CreateVnfNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateVnfNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.onap.so/vnfNotify", name = "updateVnfNotification")
    public JAXBElement<UpdateVnfNotification> createUpdateVnfNotification(UpdateVnfNotification value) {
        return new JAXBElement<>(_UpdateVnfNotification_QNAME, UpdateVnfNotification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryVnfNotification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.onap.so/vnfNotify", name = "queryVnfNotification")
    public JAXBElement<QueryVnfNotification> createQueryVnfNotification(QueryVnfNotification value) {
        return new JAXBElement<>(_QueryVnfNotification_QNAME, QueryVnfNotification.class, null, value);
    }

}
