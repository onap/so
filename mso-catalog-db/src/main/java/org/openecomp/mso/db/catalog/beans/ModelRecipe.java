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
import java.text.DateFormat;
import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class ModelRecipe extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;
	
	private int id;
	private Integer modelId;
	private String action;
	private String schemaVersion;
	private String description;
	private String orchestrationUri;
	private String modelParamXSD;
	private Integer recipeTimeout;
	private Timestamp created;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the modelId
	 */
	public Integer getModelId() {
		return modelId;
	}

	/**
	 * @param modelId the modelId to set
	 */
	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the versionStr
	 */
	public String getSchemaVersion() {
		return schemaVersion;
	}

	/**
	 * @param schemaVersion the versionStr to set
	 */
	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the orchestrationUri
	 */
	public String getOrchestrationUri() {
		return orchestrationUri;
	}

	/**
	 * @param orchestrationUri the orchestrationUri to set
	 */
	public void setOrchestrationUri(String orchestrationUri) {
		this.orchestrationUri = orchestrationUri;
	}

	/**
	 * @return the modelParamXSD
	 */
	public String getModelParamXSD() {
		return modelParamXSD;
	}

	/**
	 * @param modelParamXSD the modelParamXSD to set
	 */
	public void setModelParamXSD(String modelParamXSD) {
		this.modelParamXSD = modelParamXSD;
	}

	/**
	 * @return the recipeTimeout
	 */
	public Integer getRecipeTimeout() {
		return recipeTimeout;
	}

	/**
	 * @param recipeTimeout the recipeTimeout to set
	 */
	public void setRecipeTimeout(Integer recipeTimeout) {
		this.recipeTimeout = recipeTimeout;
	}

	/**
	 * @return the created
	 */
	public Timestamp getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ModelRecipe: ");
		sb.append("modelId=").append(modelId.toString());
		sb.append(",action=").append(action);
		sb.append(",schemaVersion=").append(schemaVersion);
		sb.append(",orchestrationUri=").append(orchestrationUri);
		sb.append(",modelParamXSD=").append(modelParamXSD);
		sb.append(",recipeTimeout=").append(recipeTimeout.toString());
        if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
