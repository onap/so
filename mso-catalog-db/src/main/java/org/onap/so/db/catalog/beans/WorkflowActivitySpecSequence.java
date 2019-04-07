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
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@IdClass(WorkflowActivitySpecSequenceId.class)
@Table(name = "workflow_activity_spec_sequence")
public class WorkflowActivitySpecSequence implements Serializable {

	private static final long serialVersionUID = -8788505759463286871L;

	@Id
	@Column(name = "ID", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ID;

	@BusinessKey
	@Id
	@Column(name = "ACTIVITY_SPEC_ID")
	private Integer activitySpecId;

	@BusinessKey
	@Id
	@Column(name = "WORKFLOW_ID")
	private Integer workflowId;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "ACTIVITY_SPEC_ID", updatable = false, insertable = false)
	private ActivitySpec activitySpec;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKFLOW_ID", updatable = false, insertable = false)
	private Workflow workflow;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("workflowId", workflowId)
				.append("activitySpecId", activitySpecId)
				.toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof WorkflowActivitySpecSequence)) {
			return false;
		}
		WorkflowActivitySpecSequence castOther = (WorkflowActivitySpecSequence) other;
		return new EqualsBuilder().append(activitySpecId, castOther.activitySpecId)
				.append(workflowId, castOther.workflowId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(activitySpecId).append(workflowId).toHashCode();
	}

	public Integer getID() {
		return ID;
	}

	public Integer getActivitySpecId() {
		return activitySpecId;
	}

	public void setActivitySpecId(Integer activitySpecId) {
		this.activitySpecId = activitySpecId;
	}

	public Integer getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}

	public ActivitySpec getActivitySpec() {
		return activitySpec;
	}

	public void setActivitySpec(ActivitySpec activitySpec) {
		this.activitySpec = activitySpec;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	
}
