/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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


package org.onap.so.bpmn.servicedecomposition.tasks;

import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.serviceinstancebeans.Pnfs;

final class BBInputSetupPnf {

    private BBInputSetupPnf() {
        throw new IllegalStateException("Utility class");
    }

    static void populatePnfToServiceInstance(Pnfs pnfs, String pnfId, ServiceInstance serviceInstance) {
        Pnf pnf = new Pnf();
        pnf.setPnfId(pnfId);
        pnf.setPnfName(pnfs.getInstanceName());
        pnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        serviceInstance.getPnfs().add(pnf);
    }
}
