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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionCandidates;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceProxy;
import java.io.Serializable;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonRootName("service-proxy")
public class ServiceProxy extends SolutionCandidates implements Serializable, ShallowCopy<ServiceProxy> {
    private static final long serialVersionUID = 1491890223056651430L;

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("service-instance")
    private ServiceInstance serviceInstance;

    @JsonProperty("model-info-service-proxy")
    private ModelInfoServiceProxy modelInfoServiceProxy;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Way to identify the type of proxy i.e. "infrastructure", "transport", etc.
     */
    public String getType() {
        return type;
    }

    /**
     * Way to identify the type of proxy i.e. "infrastructure", "transport", etc.
     */
    public void setType(String type) {
        this.type = type;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public ModelInfoServiceProxy getModelInfoServiceProxy() {
        return modelInfoServiceProxy;
    }

    public void setModelInfoServiceProxy(ModelInfoServiceProxy modelInfoServiceProxy) {
        this.modelInfoServiceProxy = modelInfoServiceProxy;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServiceProxy)) {
            return false;
        }
        ServiceProxy castOther = (ServiceProxy) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
