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

package org.onap.so.db.request.beans;

import java.io.Serializable;
import java.util.Objects;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class OperationalEnvServiceModelStatusId implements Serializable {

    private static final long serialVersionUID = 6885534735320068787L;

    @BusinessKey
    private String requestId;
    @BusinessKey
    private String operationalEnvId;
    @BusinessKey
    private String serviceModelVersionId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOperationalEnvId() {
        return operationalEnvId;
    }

    public void setOperationalEnvId(String operationalEnvId) {
        this.operationalEnvId = operationalEnvId;
    }

    public String getServiceModelVersionId() {
        return serviceModelVersionId;
    }

    public void setServiceModelVersionId(String serviceModelVersionId) {
        this.serviceModelVersionId = serviceModelVersionId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OperationalEnvServiceModelStatusId)) {
            return false;
        }
        OperationalEnvServiceModelStatusId castOther = (OperationalEnvServiceModelStatusId) other;
        return Objects.equals(this.getRequestId(), castOther.getRequestId())
                && Objects.equals(this.getOperationalEnvId(), castOther.getOperationalEnvId())
                && Objects.equals(this.getServiceModelVersionId(), castOther.getServiceModelVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRequestId(), this.getOperationalEnvId(), this.getServiceModelVersionId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestId", getRequestId())
                .append("operationalEnvId", getOperationalEnvId())
                .append("serviceModelVersionId", getServiceModelVersionId()).toString();
    }

}
