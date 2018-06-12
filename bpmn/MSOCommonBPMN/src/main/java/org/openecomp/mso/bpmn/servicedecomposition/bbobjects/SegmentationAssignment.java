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

@JsonRootName("segmentation-assignment")
public class SegmentationAssignment implements Serializable, ShallowCopy<SegmentationAssignment>{

	private static final long serialVersionUID = 5751570091375657521L;

	@Id
	@JsonProperty("segmentation-id")
    private String segmentationId;

	public String getSegmentationId() {
		return segmentationId;
	}
	public void setSegmentationId(String segmentationId) {
		this.segmentationId = segmentationId;
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof SegmentationAssignment)) {
			return false;
		}
		SegmentationAssignment castOther = (SegmentationAssignment) other;
		return new EqualsBuilder().append(segmentationId, castOther.segmentationId).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(segmentationId).toHashCode();
	}
}
