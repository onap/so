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
import java.util.Map;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
public class Model extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private int id;
	private String modelCustomizationId;
	private String modelCustomizationName;
	private String modelInvariantId;
	private String modelName;
	private String modelType;
	private String modelVersion;
	private String modelVersionId;
	private Timestamp created;
	private Map<String,ServiceRecipe> recipes;

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
	 * @return the modelCustomizationId
	 */
	public String getModelCustomizationId() {
		return modelCustomizationId;
	}

	/**
	 * @param modelCustomizationId the modelCustomizationId to set
	 */
	public void setModelCustomizationId(String modelCustomizationId) {
		this.modelCustomizationId = modelCustomizationId;
	}

	/**
	 * @return the modelCustomizationName
	 */
	public String getModelCustomizationName() {
		return modelCustomizationName;
	}

	/**
	 * @param modelCustomizationName the modelCustomizationName to set
	 */
	public void setModelCustomizationName(String modelCustomizationName) {
		this.modelCustomizationName = modelCustomizationName;
	}

	/**
	 * @return the modelInvariantId
	 */
	public String getModelInvariantId() {
		return modelInvariantId;
	}

	/**
	 * @param modelInvariantId the modelInvariantId to set
	 */
	public void setModelInvariantId(String modelInvariantId) {
		this.modelInvariantId = modelInvariantId;
	}

	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * @param modelName the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * @return the modelType
	 */
	public String getModelType() {
		return modelType;
	}

	/**
	 * @param modelType the modelType to set
	 */
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	/**
	 * @return the modelVersion
	 */
	public String getModelVersion() {
		return modelVersion;
	}

	/**
	 * @param modelVersion the modelVersion to set
	 */
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	/**
	 * @return the modelVersionId
	 */
	public String getModelVersionId() {
		return modelVersionId;
	}

	/**
	 * @param modelVersionId the modelVersionId to set
	 */
	public void setModelVersionId(String modelVersionId) {
		this.modelVersionId = modelVersionId;
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

	/**
	 * @return the recipes
	 */
	public Map<String, ServiceRecipe> getRecipes() {
		return recipes;
	}

	/**
	 * @param recipes the recipes to set
	 */
	public void setRecipes(Map<String, ServiceRecipe> recipes) {
		this.recipes = recipes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Model: ");
		sb.append("modelCustomizationId=").append(modelCustomizationId);
		sb.append(",modelCustomizationName=").append(modelCustomizationName);
		sb.append(",modelInvariantId=").append(modelInvariantId);
		sb.append(",modelName=").append(modelName);
		sb.append(",modelType=").append(modelType);
		sb.append(",modelVersion=").append(modelVersion);
		sb.append(",modelVersionId=").append(modelVersionId);
        if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
