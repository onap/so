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

package org.onap.so.adapters.vnf;


import java.util.Map;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.mappers.MapAdapter;

/**
 * This webservice defines the Asynchronous versions of VNF adapter calls. The notification messages for final responses
 * are documented elsewhere (by the client service WSDL).
 *
 */
@Deprecated
@WebService(name = "VnfAdapterAsync", targetNamespace = "http://org.onap.so/vnfA")
public interface MsoVnfAdapterAsync {
    /**
     * This is the "Create VNF" Web Service Endpoint definition.
     */
    @WebMethod
    @Oneway
    public void createVnfA(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "cloudOwner") @XmlElement(required = false) String cloudOwner,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "vnfType") @XmlElement(required = true) String vnfType,
            @WebParam(name = "vnfVersion") @XmlElement(required = false) String vnfVersion,
            @WebParam(name = "vnfName") @XmlElement(required = true) String vnfName,
            @WebParam(name = "requestType") @XmlElement(required = false) String requestType,
            @WebParam(name = "volumeGroupHeatStackId") @XmlElement(required = false) String volumeGroupHeatStackId,
            @WebParam(name = "inputs") @XmlJavaTypeAdapter(MapAdapter.class) Map<String, Object> inputs,
            @WebParam(name = "failIfExists") Boolean failIfExists, @WebParam(name = "backout") Boolean backout,
            @WebParam(name = "enableBridge") Boolean enableBridge,
            @WebParam(name = "messageId") @XmlElement(required = true) String messageId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "notificationUrl") @XmlElement(required = true) String notificationUrl);

    @WebMethod
    @Oneway
    public void deleteVnfA(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "cloudOwner") @XmlElement(required = false) String cloudOwner,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "vnfName") @XmlElement(required = true) String vnfName,
            @WebParam(name = "messageId") @XmlElement(required = true) String messageId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "notificationUrl") @XmlElement(required = true) String notificationUrl);


    @WebMethod
    public void healthCheckA();
}
