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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "northbound_request_ref_lookup")
public class NorthBoundRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2238991039015148725L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@BusinessKey
	@Column(name = "MACRO_ACTION")
	private String macroAction;

	@BusinessKey
	@Column(name = "ACTION")
	private String action;

	@BusinessKey
	@Column(name = "REQUEST_SCOPE")
	private String requestScope;

	@BusinessKey
	@Column(name = "IS_ALACARTE")
	private Boolean isAlacarte;

	@BusinessKey
	@Column(name = "IS_TOPLEVELFLOW")
	private Boolean isToplevelflow;

	@BusinessKey
	@Column(name = "MIN_API_VERSION")
	private Double minApiVersion;

	@BusinessKey
	@Column(name = "MAX_API_VERSION")
	private Double maxApiVersion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "northBoundRequest")
	private List<OrchestrationFlow> orchestrationFlowList;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("action", action).append("requestScope", requestScope)
				.append("isAlacarte", isAlacarte).append("isToplevelflow", isToplevelflow)
				.append("minApiVersion", minApiVersion).append("maxApiVersion", maxApiVersion).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof NorthBoundRequest)) {
			return false;
		}
		NorthBoundRequest castOther = (NorthBoundRequest) other;
		return new EqualsBuilder().append(action, castOther.action).append(requestScope, castOther.requestScope)
				.append(isAlacarte, castOther.isAlacarte).append(isToplevelflow, castOther.isToplevelflow)
				.append(minApiVersion, castOther.minApiVersion).append(maxApiVersion, castOther.maxApiVersion)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(action).append(requestScope).append(isAlacarte).append(isToplevelflow)
				.append(minApiVersion).append(maxApiVersion).toHashCode();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMacroAction() {
		return macroAction;
	}

	public void setMacroAction(String macroAction) {
		this.macroAction = macroAction;
	}

	public String getRequestScope() {
		return requestScope;
	}

	public void setRequestScope(String requestScope) {
		this.requestScope = requestScope;
	}

	public Boolean getIsAlacarte() {
		return isAlacarte;
	}

	public void setIsAlacarte(Boolean isAlacarte) {
		this.isAlacarte = isAlacarte;
	}

	public Boolean getIsToplevelflow() {
		return isToplevelflow;
	}

	public void setIsToplevelflow(Boolean isToplevelflow) {
		this.isToplevelflow = isToplevelflow;
	}

	public Double getMinApiVersion() {
		return minApiVersion;
	}

	public void setMinApiVersion(Double minApiVersion) {
		this.minApiVersion = minApiVersion;
	}

	public Double getMaxApiVersion() {
		return maxApiVersion;
	}

	public void setMaxApiVersion(Double maxApiVersion) {
		this.maxApiVersion = maxApiVersion;
	}

	@LinkedResource
	public List<OrchestrationFlow> getOrchestrationFlowList() {
		return orchestrationFlowList;
	}

	public void setOrchestrationFlowList(List<OrchestrationFlow> orchestrationFlowList) {
		this.orchestrationFlowList = orchestrationFlowList;
	}
}
