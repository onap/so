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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;

import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "orchestration_flow_reference")
public class OrchestrationFlow implements Serializable {

	private static final long serialVersionUID = 2457818854397870011L;

	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Integer id;

	@BusinessKey
	@Column(name = "COMPOSITE_ACTION")
	private String action;

	@BusinessKey
	@Column(name = "SEQ_NO")
	private Integer sequenceNumber;

	@BusinessKey
	@Column(name = "FLOW_NAME")
	private String flowName;

	@BusinessKey
	@Column(name = "FLOW_VERSION")
	private Double flowVersion;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "NB_REQ_REF_LOOKUP_ID")
	private NorthBoundRequest northBoundRequest;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("action", action)
				.append("sequenceNumber", sequenceNumber).append("flowName", flowName)
				.append("flowVersion", flowVersion).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof OrchestrationFlow)) {
			return false;
		}
		OrchestrationFlow castOther = (OrchestrationFlow) other;
		return new EqualsBuilder().append(action, castOther.action).append(sequenceNumber, castOther.sequenceNumber)
				.append(flowName, castOther.flowName).append(flowVersion, castOther.flowVersion).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(action).append(sequenceNumber).append(flowName).append(flowVersion)
				.toHashCode();
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public Double getFlowVersion() {
		return flowVersion;
	}

	public void setFlowVersion(Double flowVersion) {
		this.flowVersion = flowVersion;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@LinkedResource
	public NorthBoundRequest getNorthBoundRequest() {
		return northBoundRequest;
	}

	public void setNorthBoundRequest(NorthBoundRequest northBoundRequest) {
		this.northBoundRequest = northBoundRequest;
	}
}
