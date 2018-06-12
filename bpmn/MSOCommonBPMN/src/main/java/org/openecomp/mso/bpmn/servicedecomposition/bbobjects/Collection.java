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
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.openecomp.mso.bpmn.servicedecomposition.ShallowCopy;


@JsonRootName("collection")
public class Collection implements Serializable, ShallowCopy<Collection> {
	
	private static final long serialVersionUID = 6389915895863066288L;

	@Id
	@JsonProperty("id")
	private String id;
	@JsonProperty("model-info-collection")
	private ModelInfoCollection modelInfoCollection;
	@JsonProperty("instance-group")
	private InstanceGroup instanceGroup;
	@JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ModelInfoCollection getModelInfoCollection() {
		return modelInfoCollection;
	}
	public void setModelInfoCollection(ModelInfoCollection modelInfoCollection) {
		this.modelInfoCollection = modelInfoCollection;
	}
	public InstanceGroup getInstanceGroup() {
		return instanceGroup;
	}
	public void setInstanceGroup(InstanceGroup instanceGroup) {
		this.instanceGroup = instanceGroup;
	}
	public OrchestrationStatus getOrchestrationStatus() {
		return orchestrationStatus;
	}
	public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Collection)) {
			return false;
		}
		Collection castOther = (Collection) other;
		return new EqualsBuilder().append(id, castOther.id).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}


}
