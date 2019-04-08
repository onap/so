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

public class OperationStatusId implements Serializable {

    private static final long serialVersionUID = 3114959241155882723L;

    @BusinessKey
    private String serviceId;
    @BusinessKey
    private String operationId;

    public OperationStatusId() {

    }

    public OperationStatusId(String serviceId, String operationId) {
        this.serviceId = serviceId;
        this.operationId = operationId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OperationStatusId)) {
            return false;
        }
        OperationStatusId castOther = (OperationStatusId) other;
        return Objects.equals(this.getServiceId(), castOther.getServiceId())
                && Objects.equals(this.getOperationId(), castOther.getOperationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getServiceId(), this.getOperationId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceId", getServiceId()).append("operationId", getOperationId())
                .toString();
    }

}
