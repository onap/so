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

package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class is used to store instance
 * data of owningEntity for ServiceDecomposition
 *
 * @author bb3476
 *
 */
@JsonRootName("owningEntity")
public class OwningEntity extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String owningEntityId;
	private String owningEntityName;
	public String getOwningEntityId() {
		return owningEntityId;
	}
	public void setOwningEntityId(String owningEntityId) {
		this.owningEntityId = owningEntityId;
	}
	public String getOwningEntityName() {
		return owningEntityName;
	}
	public void setOwningEntityName(String owningEntityName) {
		this.owningEntityName = owningEntityName;
	}
	
}
