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
package org.onap.so.monitoring.camunda.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author waqas.ikram@ericsson.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstance {

    private String id;
    private String processDefinitionId;
    private String processDefinitionName;
    private String superProcessInstanceId;

    public ProcessInstance() {}

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }


    /**
     * @return the processDefinitionId
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * @param processDefinitionId the processDefinitionId to set
     */
    public void setProcessDefinitionId(final String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    /**
     * @return the processDefinitionName
     */
    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    /**
     * @param processDefinitionName the processDefinitionName to set
     */
    public void setProcessDefinitionName(final String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    /**
     * @return the superProcessInstanceId
     */
    public String getSuperProcessInstanceId() {
        return superProcessInstanceId;
    }

    /**
     * @param superProcessInstanceId the superProcessInstanceId to set
     */
    public void setSuperProcessInstanceId(final String superProcessInstanceId) {
        this.superProcessInstanceId = superProcessInstanceId;
    }


    @JsonIgnore
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((processDefinitionId == null) ? 0 : processDefinitionId.hashCode());
        result = prime * result + ((processDefinitionName == null) ? 0 : processDefinitionName.hashCode());
        result = prime * result + ((superProcessInstanceId == null) ? 0 : superProcessInstanceId.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessInstance) {
            final ProcessInstance other = (ProcessInstance) obj;
            return isEqual(id, other.id) && isEqual(processDefinitionId, other.processDefinitionId)
                    && isEqual(processDefinitionName, other.processDefinitionName)
                    && isEqual(superProcessInstanceId, other.superProcessInstanceId);
        }

        return false;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "ProcessInstance [id=" + id + ", processDefinitionId=" + processDefinitionId + ", processDefinitionName="
                + processDefinitionName + "]";
    }
}
