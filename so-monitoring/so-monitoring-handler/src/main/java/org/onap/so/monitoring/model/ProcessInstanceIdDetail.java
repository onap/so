/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

/**
 * @author waqas.ikram@ericsson.com
 */
public class ProcessInstanceIdDetail {

    private final String processInstanceId;


    public ProcessInstanceIdDetail(final String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * @return the processInstancId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessInstanceIdDetail) {
            final ProcessInstanceIdDetail other = (ProcessInstanceIdDetail) obj;

            return isEqual(processInstanceId, other.processInstanceId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ProcessInstanceIdDetail [processInstanceId=" + processInstanceId + "]";
    }

}
