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

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;
import java.util.Set;

public class Service extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String modelName;
	private String description;
	private String modelUUID;
	private String modelInvariantUUID;
	private Timestamp created;
	private String toscaCsarArtifactUUID;
	private String modelVersion;
	private String category;
	private String serviceType;
	private String serviceRole;
	private Map<String,ServiceRecipe> recipes;
	private Set<ServiceToResourceCustomization> serviceResourceCustomizations;
	
	public Service() {}
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Map<String, ServiceRecipe> getRecipes() {
		return recipes;
	}
	public void setRecipes(Map<String, ServiceRecipe> recipes) {
		this.recipes = recipes;
	}
	
	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
		
	public String getModelUUID() {
		return modelUUID;
	}

	public void setModelUUID(String modelUUID) {
		this.modelUUID = modelUUID;
	}

	public String getModelInvariantUUID() {
		return modelInvariantUUID;
	}

	public void setModelInvariantUUID(String modelInvariantUUID) {
		this.modelInvariantUUID = modelInvariantUUID;
	}

	public String getToscaCsarArtifactUUID() {
		return toscaCsarArtifactUUID;
	}

	public void setToscaCsarArtifactUUID(String toscaCsarArtifactUUID) {
		this.toscaCsarArtifactUUID = toscaCsarArtifactUUID;
	}

	public Set<ServiceToResourceCustomization> getServiceResourceCustomizations() {
		return serviceResourceCustomizations;
	}

	public void setServiceResourceCustomizations(Set<ServiceToResourceCustomization> serviceResourceCustomizations) {
		this.serviceResourceCustomizations = serviceResourceCustomizations;
	}
	
	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return category;
    }

    
    /**
     * @param category The category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SERVICE: name=").append(modelName).append(",modelVersion=").append(modelVersion)
            .append(",description=").append(description).append(",modelInvariantUUID=").append(modelInvariantUUID)
            .append(",toscaCsarArtifactUUID=").append(toscaCsarArtifactUUID).append(",serviceType=").append(serviceType)
            .append(",serviceRole=").append(serviceRole);
		for (String recipeAction : recipes.keySet()) {
			ServiceRecipe recipe = recipes.get(recipeAction);
			sb.append("\n").append(recipe.toString());
		}
		
		for(ServiceToResourceCustomization serviceResourceCustomization : serviceResourceCustomizations) {
			sb.append("\n").append(serviceResourceCustomization.toString());
		}
		if (created != null) {
		        sb.append (",created=");
		        sb.append (DateFormat.getInstance().format(created));
		}
		return sb.toString();
	}
}
