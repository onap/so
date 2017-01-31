/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;

public class Service extends MavenLikeVersioning {
	private int id;
	private String serviceName;
	private String description;
	private String httpMethod;
	private String serviceNameVersionId;
	private String serviceVersion;
	private Map<String,ServiceRecipe> recipes;
	
	private String modelInvariantUUID;
	private Timestamp created;
	
	public Service() {}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
		
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getServiceNameVersionId() {
		return serviceNameVersionId;
	}

	public void setServiceNameVersionId(String serviceNameVersionId) {
		this.serviceNameVersionId = serviceNameVersionId;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	
	public String getModelInvariantUUID() {
		return modelInvariantUUID;
	}

	public void setModelInvariantUUID(String modelInvariantUUID) {
		this.modelInvariantUUID = modelInvariantUUID;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SERVICE: id=" + id + ",name=" + serviceName + ",version=" + version + ",description=" + description+",modelInvariantUUID="+modelInvariantUUID);
		for (String recipeAction : recipes.keySet()) {
			ServiceRecipe recipe = recipes.get(recipeAction);
			sb.append ("\n" + recipe.toString());
		}
		if (created != null) {
		        sb.append (",created=");
		        sb.append (DateFormat.getInstance().format(created));
		}
		return sb.toString();
	}
}
