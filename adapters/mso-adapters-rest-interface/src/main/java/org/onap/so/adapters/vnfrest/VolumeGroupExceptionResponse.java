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

package org.onap.so.adapters.vnfrest;


import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

@XmlRootElement(name = "volumeGroupException")
public class VolumeGroupExceptionResponse extends VfModuleExceptionResponse {
    // Exactly the same as a VfModuleExceptionResponse

    private static final long serialVersionUID = 1168681288205898800L;

    public VolumeGroupExceptionResponse() {
        super();
    }

    public VolumeGroupExceptionResponse(String message) {
        super(message);
    }

    public VolumeGroupExceptionResponse(String message, MsoExceptionCategory category, boolean rolledBack,
            String messageid) {
        super(message, category, rolledBack, messageid);
    }
}
