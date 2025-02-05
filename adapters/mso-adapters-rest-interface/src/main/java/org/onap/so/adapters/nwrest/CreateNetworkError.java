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

package org.onap.so.adapters.nwrest;


import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

@XmlRootElement(name = "createNetworkError")
public class CreateNetworkError extends NetworkExceptionResponse implements Serializable {
    private static final long serialVersionUID = -4283402447149144456L;

    public CreateNetworkError() {
        super("");
    }

    public CreateNetworkError(String message) {
        super(message);
    }

    public CreateNetworkError(String message, MsoExceptionCategory category, boolean rolledBack, String messageid) {
        super(message, category, rolledBack, messageid);
    }
}
