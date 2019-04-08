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
import org.onap.so.openstack.exceptions.MsoExceptionCategory;


/**
 * This class reports an exception when trying to create a VNF when another VNF of the same name already exists in the
 * target cloud/tenant. Note that the createVnf method suppresses this exception by default.
 * 
 *
 */
@WebFault(name = "TenantAlreadyExists", faultBean = "org.onap.so.adapters.tenant.exceptions.TenantExceptionBean",
        targetNamespace = "http://org.onap.so/tenant")
public class TenantAlreadyExists extends TenantException {

    private static final long serialVersionUID = 1L;

    public TenantAlreadyExists(String name, String cloudId, String tenantId) {
        super("Tenant " + name + " already exists in " + cloudId + " with ID " + tenantId,
                MsoExceptionCategory.USERDATA);
    }
}
