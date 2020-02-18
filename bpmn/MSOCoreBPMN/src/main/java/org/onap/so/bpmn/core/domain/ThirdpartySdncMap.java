/*-
 ** ============LICENSE_START=======================================================
 ** ONAP - SO
 ** ================================================================================
 ** Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
 ** ================================================================================
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** 
 **      http://www.apache.org/licenses/LICENSE-2.0
 ** 
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 ** ============LICENSE_END=========================================================
 **/

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ThirdpartySdncMap extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 5629921809747079453L;

    private String esrThirdpartySdncId;
    private PInterface uni;
    private PInterface enni;

    public String getEsrThirdpartySdncId() {
        return esrThirdpartySdncId;
    }

    public void setEsrThirdpartySdncId(String sdnc) {
        this.esrThirdpartySdncId = sdnc;
    }

    public PInterface getUni() {
        return uni;
    }

    public void setUni(PInterface uni) {
        this.uni = uni;
    }

    public PInterface getEnni() {
        return enni;
    }

    public void setEnni(PInterface enni) {
        this.enni = enni;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ThirdpartySdncMap)) {
            return false;
        }
        ThirdpartySdncMap castOther = (ThirdpartySdncMap) other;
        return new EqualsBuilder().append(esrThirdpartySdncId, castOther.getEsrThirdpartySdncId()).isEquals();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((esrThirdpartySdncId == null) ? 0 : esrThirdpartySdncId.hashCode());
        return result;
    }
}
