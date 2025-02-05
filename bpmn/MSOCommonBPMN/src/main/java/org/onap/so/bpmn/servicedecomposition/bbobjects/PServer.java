/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;


public class PServer implements Serializable, ShallowCopy<PServer> {

    private static final long serialVersionUID = 1378547515775540874L;

    @Id
    @JsonProperty("pserver-id")
    private String pserverId;
    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("physical-links")
    private List<PhysicalLink> physicalLinks = new ArrayList<>(); // TODO techincally there is a pInterface
                                                                  // between (pserver <--> physical-link)
                                                                  // but dont really need that pojo

    public String getPserverId() {
        return pserverId;
    }

    public void setPserverId(String pserverId) {
        this.pserverId = pserverId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<PhysicalLink> getPhysicalLinks() {
        return physicalLinks;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PServer)) {
            return false;
        }
        PServer castOther = (PServer) other;
        return new EqualsBuilder().append(pserverId, castOther.pserverId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pserverId).toHashCode();
    }

}
