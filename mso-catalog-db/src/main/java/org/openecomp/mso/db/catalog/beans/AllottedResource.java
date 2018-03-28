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

package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;
import java.sql.Timestamp;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class AllottedResource extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;
	
	private String modelUuid = null;
	private String modelInvariantUuid = null;
	private String modelVersion = null; 
	private String modelName = null;
	private String toscaNodeType = null;
	private String subcategory = null;
	private String description = null;
	private Timestamp created = null;

	public AllottedResource() {
	}
	
	public String getModelUuid() {
		return this.modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}
	public String getModelInvariantUuid() {
		return this.modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}
	public String getModelVersion() {
		return this.modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public String getModelName() {
		return this.modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getToscaNodeType() {
		return this.toscaNodeType;
	}
	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}	
	public String getSubcategory() {
		return this.subcategory;
	}
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}	
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Timestamp getCreated() {
		return created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}	

}
