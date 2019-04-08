/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.tenant;


import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Holder;
import org.onap.so.adapters.tenant.exceptions.TenantException;
import org.onap.so.adapters.tenantrest.TenantRollback;
import org.onap.so.entity.MsoRequest;

@WebService(name = "TenantAdapter", targetNamespace = "http://org.onap.so/tenant")
public interface MsoTenantAdapter {
    /**
     * This is the "Create Tenant" Web Service Endpoint definition.
     */
    @WebMethod
    public void createTenant(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantName") @XmlElement(required = true) String tenantName,
            @WebParam(name = "metadata") Map<String, String> metadata,
            @WebParam(name = "failIfExists") Boolean failIfExists, @WebParam(name = "backout") Boolean backout,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "tenantId", mode = Mode.OUT) Holder<String> tenantId,
            @WebParam(name = "rollback", mode = Mode.OUT) Holder<TenantRollback> rollback) throws TenantException;

    @WebMethod
    public void queryTenant(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantNameOrId") @XmlElement(required = true) String tenantNameOrId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "tenantId", mode = Mode.OUT) Holder<String> tenantId,
            @WebParam(name = "tenantName", mode = Mode.OUT) Holder<String> tenantName,
            @WebParam(name = "metadata", mode = Mode.OUT) Holder<Map<String, String>> metadata) throws TenantException;

    @WebMethod
    public void deleteTenant(@WebParam(name = "cloudSiteId") @XmlElement(required = true) String cloudSiteId,
            @WebParam(name = "tenantId") @XmlElement(required = true) String tenantId,
            @WebParam(name = "request") MsoRequest msoRequest,
            @WebParam(name = "tenantDeleted", mode = Mode.OUT) Holder<Boolean> tenantDeleted) throws TenantException;

    @WebMethod
    public void rollbackTenant(@WebParam(name = "rollback") @XmlElement(required = true) TenantRollback rollback)
            throws TenantException;

    @WebMethod
    public void healthCheck();
}
