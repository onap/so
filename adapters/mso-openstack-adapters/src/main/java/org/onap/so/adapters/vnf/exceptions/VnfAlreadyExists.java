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

package org.onap.so.adapters.vnf.exceptions;



import jakarta.xml.ws.WebFault;

/**
 * This class reports an exception when trying to create a VNF when another VNF of the same name already exists in the
 * target cloud/tenant. Note that the createVnf method suppresses this exception by default.
 *
 *
 */
@WebFault(name = "VnfAlreadyExists", faultBean = "org.onap.so.adapters.vnf.exceptions.VnfExceptionBean",
        targetNamespace = "http://org.onap.so/vnf")
public class VnfAlreadyExists extends VnfException {

    private static final long serialVersionUID = 1L;

    public VnfAlreadyExists(String name, String cloudId, String cloudOwner, String tenantId, String vnfId) {
        super("Resource " + name + " already exists in owner/cloud/tenant " + cloudOwner + "/" + cloudId + "/"
                + tenantId + " with ID " + vnfId);
    }
}
