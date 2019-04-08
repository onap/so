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

package org.onap.so.adapters.tenant.exceptions;



import javax.xml.ws.WebFault;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

/**
 * This class simply extends Exception (without addition additional functionality) to provide an identifier for Tenant
 * related exceptions on create, delete, query.
 * 
 *
 */
@WebFault(name = "TenantException", faultBean = "org.onap.so.adapters.tenant.exceptions.TenantExceptionBean",
        targetNamespace = "http://org.onap.so/tenant")
public class TenantException extends Exception {

    private static final long serialVersionUID = 1L;

    private TenantExceptionBean faultInfo;

    public TenantException(String msg) {
        super(msg);
        faultInfo = new TenantExceptionBean(msg);
    }

    public TenantException(String msg, Throwable e) {
        super(msg, e);
        faultInfo = new TenantExceptionBean(msg);
    }

    public TenantException(String msg, MsoExceptionCategory category) {
        super(msg);
        faultInfo = new TenantExceptionBean(msg, category);
    }

    public TenantException(String msg, MsoExceptionCategory category, Throwable e) {
        super(msg, e);
        faultInfo = new TenantExceptionBean(msg, category);
    }

    public TenantException(MsoException e) {
        super(e);
        faultInfo = new TenantExceptionBean(e.getContextMessage(), e.getCategory());
    }

    public TenantExceptionBean getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(TenantExceptionBean faultInfo) {
        this.faultInfo = faultInfo;
    }
}
