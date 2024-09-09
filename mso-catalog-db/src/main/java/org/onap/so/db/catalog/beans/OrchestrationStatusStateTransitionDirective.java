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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/orchestrationStatusStateTransitionDirective")
@Table(name = "orchestration_status_state_transition_directive")
public class OrchestrationStatusStateTransitionDirective implements Serializable {
    private static final long serialVersionUID = -4626396955833442376L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE")
    @BusinessKey
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORCHESTRATION_STATUS")
    @BusinessKey
    private OrchestrationStatus orchestrationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_ACTION")
    @BusinessKey
    private OrchestrationAction targetAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "FLOW_DIRECTIVE")
    @BusinessKey
    private OrchestrationStatusValidationDirective flowDirective;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("resourceType", resourceType)
                .append("orchestrationStatus", orchestrationStatus).append("targetAction", targetAction)
                .append("flowDirective", flowDirective).toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public OrchestrationAction getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(OrchestrationAction targetAction) {
        this.targetAction = targetAction;
    }

    public OrchestrationStatusValidationDirective getFlowDirective() {
        return flowDirective;
    }

    public void setFlowDirective(OrchestrationStatusValidationDirective flowDirective) {
        this.flowDirective = flowDirective;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OrchestrationStatusStateTransitionDirective)) {
            return false;
        }
        OrchestrationStatusStateTransitionDirective castOther = (OrchestrationStatusStateTransitionDirective) other;
        return new EqualsBuilder().append(getResourceType(), castOther.getResourceType())
                .append(getOrchestrationStatus(), castOther.getOrchestrationStatus())
                .append(getTargetAction(), castOther.getTargetAction())
                .append(getFlowDirective(), castOther.getFlowDirective()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getResourceType()).append(getOrchestrationStatus())
                .append(getTargetAction()).append(getFlowDirective()).toHashCode();
    }
}
