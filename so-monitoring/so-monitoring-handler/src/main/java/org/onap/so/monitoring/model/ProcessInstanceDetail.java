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
public class ProcessInstanceDetail {

    private final String processInstanceId;
    private final String processDefinitionId;
    private final String processDefinitionName;
    private final String superProcessInstanceId;


    public ProcessInstanceDetail(final String processInstanceId, final String processDefinitionId,
            final String processDefinitionName, final String superProcessInstanceId) {
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionName = processDefinitionName;
        this.superProcessInstanceId = superProcessInstanceId;
    }

    /**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @return the processDefinitionId
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * @return the processDefinitionName
     */
    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    /**
     * @return the superProcessInstanceId
     */
    public String getSuperProcessInstanceId() {
        return superProcessInstanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        result = prime * result + ((processDefinitionId == null) ? 0 : processDefinitionId.hashCode());
        result = prime * result + ((processDefinitionName == null) ? 0 : processDefinitionName.hashCode());
        result = prime * result + ((superProcessInstanceId == null) ? 0 : superProcessInstanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessInstanceDetail) {
            final ProcessInstanceDetail other = (ProcessInstanceDetail) obj;

            return isEqual(processInstanceId, other.processInstanceId)
                    && isEqual(processDefinitionId, other.processDefinitionId)
                    && isEqual(processDefinitionName, other.processDefinitionName)
                    && isEqual(superProcessInstanceId, other.superProcessInstanceId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ProcessInstanceDetail [processInstanceId=" + processInstanceId + ", processDefinitionId="
                + processDefinitionId + ", processDefinitionName=" + processDefinitionName + ", superProcessInstanceId="
                + superProcessInstanceId + "]";
    }


}
