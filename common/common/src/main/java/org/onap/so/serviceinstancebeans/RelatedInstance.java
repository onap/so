/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonRootName(value = "relatedInstance")
@JsonInclude(Include.NON_DEFAULT)
public class RelatedInstance implements Serializable {

    private static final long serialVersionUID = 137250604008221644L;
    @JsonProperty("instanceName")
    protected String instanceName;
    @JsonProperty("instanceId")
    protected String instanceId;
    @JsonProperty("modelInfo")
    protected ModelInfo modelInfo;
    // Configuration field
    @JsonProperty("instanceDirection")
    protected InstanceDirection instanceDirection;


    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public InstanceDirection getInstanceDirection() {
        return instanceDirection;
    }

    public void setInstanceDirection(InstanceDirection instanceDirection) {
        this.instanceDirection = instanceDirection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instanceName", instanceName).append("instanceId", instanceId)
                .append("modelInfo", modelInfo).append("instanceDirection", instanceDirection).toString();
    }
}
