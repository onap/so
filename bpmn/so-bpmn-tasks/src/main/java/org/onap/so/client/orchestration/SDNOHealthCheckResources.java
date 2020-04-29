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

package org.onap.so.client.orchestration;

import java.util.UUID;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.helper.TasksInjectionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNOHealthCheckResources {
    @Autowired
    private TasksInjectionHelper injectionHelper;

    /**
     * SDNO Call to Check Health Status
     *
     * @param vnf
     * @param requestContext *
     * @return healthCheckResult
     * @throws @throws Exception
     */
    public boolean healthCheck(GenericVnf vnf, RequestContext requestContext) throws Exception {
        String requestId = requestContext.getMsoRequestId();
        String requestorId = requestContext.getRequestorId();
        String vnfId = vnf.getVnfId();
        UUID uuid = UUID.fromString(requestId);

        return injectionHelper.getSdnoValidator().healthDiagnostic(vnfId, uuid, requestorId);
    }
}
