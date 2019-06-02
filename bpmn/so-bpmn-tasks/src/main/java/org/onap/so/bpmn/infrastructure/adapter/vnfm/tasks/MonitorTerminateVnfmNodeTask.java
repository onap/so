/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.DELETE_VNF_NODE_STATUS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNF_ASSIGNED;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author waqas.ikram@est.tech
 *
 */
@Component
public class MonitorTerminateVnfmNodeTask extends MonitorVnfmNodeTask {

    @Autowired
    public MonitorTerminateVnfmNodeTask(final ExtractPojosForBB extractPojosForBB, final ExceptionBuilder exceptionUtil,
            final AAIVnfResources aaiVnfResources) {
        super(extractPojosForBB, exceptionUtil, aaiVnfResources);
    }

    @Override
    public String getNodeStatusVariableName() {
        return DELETE_VNF_NODE_STATUS;
    }

    @Override
    public boolean isOrchestrationStatusValid(final String orchestrationStatus) {
        return VNF_ASSIGNED.equalsIgnoreCase(orchestrationStatus);
    }

}
