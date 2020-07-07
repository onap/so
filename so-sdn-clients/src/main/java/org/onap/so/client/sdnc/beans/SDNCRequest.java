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

package org.onap.so.client.sdnc.beans;

import java.io.Serializable;
import java.util.UUID;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class SDNCRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4679678988657593282L;
    private String timeOut = "PT1H";
    private SDNCTopology topology;
    private String correlationValue = UUID.randomUUID().toString();
    private String correlationName = "SDNCCallback";
    private transient Object sdncPayload;


    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public SDNCTopology getTopology() {
        return topology;
    }

    public void setTopology(SDNCTopology topology) {
        this.topology = topology;
    }

    public String getCorrelationValue() {
        return correlationValue;
    }

    public void setCorrelationValue(String correlationValue) {
        this.correlationValue = correlationValue;
    }

    public String getCorrelationName() {
        return correlationName;
    }

    public void setCorrelationName(String correlationName) {
        this.correlationName = correlationName;
    }

    public Object getSDNCPayload() {
        return sdncPayload;
    }

    public void setSDNCPayload(Object sDNCPayload) {
        this.sdncPayload = sDNCPayload;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SDNCRequest)) {
            return false;
        }
        SDNCRequest castOther = (SDNCRequest) other;
        return new EqualsBuilder().append(correlationValue, castOther.correlationValue)
                .append(correlationName, castOther.correlationName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(correlationValue).append(correlationName).toHashCode();
    }

}
