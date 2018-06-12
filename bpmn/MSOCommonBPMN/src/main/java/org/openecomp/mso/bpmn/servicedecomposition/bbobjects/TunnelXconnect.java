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

package org.openecomp.mso.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openecomp.mso.bpmn.servicedecomposition.ShallowCopy;

import javax.persistence.Id;

@JsonRootName("tunnel-Xconnect")
public class TunnelXconnect implements Serializable, ShallowCopy<TunnelXconnect> {

	private static final long serialVersionUID = 4547694053883088046L;

	@Id
	@JsonProperty("id")
	private String id;
	@JsonProperty("up-bandwidth")
	private String upBandwidth;
	@JsonProperty("down-bandwidth")
	private String downBandwidth;
	@JsonProperty("up-bandwidth-2")
	private String upBandwidth2;
	@JsonProperty("down-bandwidth-2")
	private String downBandwidth2;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUpBandwidth() {
		return upBandwidth;
	}
	public void setUpBandwidth(String upBandwidth) {
		this.upBandwidth = upBandwidth;
	}
	public String getDownBandwidth() {
		return downBandwidth;
	}
	public void setDownBandwidth(String downBandwidth) {
		this.downBandwidth = downBandwidth;
	}
	public String getUpBandwidth2() {
		return upBandwidth2;
	}
	public void setUpBandwidth2(String upBandwidth2) {
		this.upBandwidth2 = upBandwidth2;
	}
	public String getDownBandwidth2() {
		return downBandwidth2;
	}
	public void setDownBandwidth2(String downBandwidth2) {
		this.downBandwidth2 = downBandwidth2;
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof TunnelXconnect)) {
			return false;
		}
		TunnelXconnect castOther = (TunnelXconnect) other;
		return new EqualsBuilder().append(id, castOther.id).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}
}
