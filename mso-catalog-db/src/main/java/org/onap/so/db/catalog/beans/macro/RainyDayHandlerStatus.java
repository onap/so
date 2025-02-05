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

package org.onap.so.db.catalog.beans.macro;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "rainy_day_handler_macro")
public class RainyDayHandlerStatus implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "FLOW_NAME")
    private String flowName;

    @BusinessKey
    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @BusinessKey
    @Column(name = "VNF_TYPE")
    private String vnfType;

    @BusinessKey
    @Column(name = "ERROR_CODE")
    private String errorCode;

    @BusinessKey
    @Column(name = "REG_EX_ERROR_MESSAGE")
    private String errorMessage;

    @BusinessKey
    @Column(name = "WORK_STEP")
    private String workStep;

    @BusinessKey
    @Column(name = "POLICY")
    private String policy;

    @Column(name = "SECONDARY_POLICY")
    private String secondaryPolicy;

    @BusinessKey
    @Column(name = "SERVICE_ROLE")
    private String serviceRole;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("flowName", flowName)
                .append("serviceType", serviceType).append("vnfType", vnfType).append("errorCode", errorCode)
                .append("errorMessage", errorMessage).append("workStep", workStep).append("policy", policy)
                .append("secondaryPolicy", secondaryPolicy).append("serviceRole", serviceRole).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RainyDayHandlerStatus)) {
            return false;
        }
        RainyDayHandlerStatus castOther = (RainyDayHandlerStatus) other;
        return new EqualsBuilder().append(flowName, castOther.flowName).append(serviceType, castOther.serviceType)
                .append(vnfType, castOther.vnfType).append(errorCode, castOther.errorCode)
                .append(workStep, castOther.workStep).append(policy, castOther.policy)
                .append(serviceRole, castOther.serviceRole).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(flowName).append(serviceType).append(vnfType).append(errorCode)
                .append(workStep).append(policy).append(serviceRole).toHashCode();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getWorkStep() {
        return workStep;
    }

    public void setWorkStep(String workStep) {
        this.workStep = workStep;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getSecondaryPolicy() {
        return secondaryPolicy;
    }

    public void setSecondaryPolicy(String secondaryPolicy) {
        this.secondaryPolicy = secondaryPolicy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

}
