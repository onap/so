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
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessDefinition {

    private String id;
    private String bpmn20Xml;

    public ProcessDefinition() {}

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
     * @return the bpmn20Xml
     */
    public String getBpmn20Xml() {
        return bpmn20Xml;
    }

    /**
     * @param bpmn20Xml the bpmn20Xml to set
     */
    public void setBpmn20Xml(final String bpmn20Xml) {
        this.bpmn20Xml = bpmn20Xml;
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((bpmn20Xml == null) ? 0 : bpmn20Xml.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProcessDefinition) {
            final ProcessDefinition other = (ProcessDefinition) obj;
            return isEqual(id, other.id) && isEqual(bpmn20Xml, other.bpmn20Xml);
        }
        return false;
    }


}
