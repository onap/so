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
import java.util.Date;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.logger.MsoLogger;

public class ServiceRecipe extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;
	
	private int id;
	private String serviceModelUUID;
	private String action;
	private String description;
	private String orchestrationUri;
	private String serviceParamXSD;
	private int recipeTimeout;
	private Integer serviceTimeoutInterim;
	private Timestamp created;

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);
	
	// This 'default' CTR is now needed for backward compatibility since a new CTR was added below
	public ServiceRecipe() {
		super();
	}
	
	// This CTR is needed by the HQL SELECT JOIN between the SERVICE and SERVICE_RECIPE tables
	// in CatalogDatabase::getServiceRecipe()
	public ServiceRecipe(int id, String serviceModelUUID, String action,
			String description, String orchestrationUri,
			String serviceParamXSD, int recipeTimeout,
			int serviceTimeoutInterim, Date created) {
		super();
		LOGGER.debug("ServiceRecipe id=" + id + ", serviceModelUUID=" + serviceModelUUID + ", action=" + action + ", description=" + description +
				", orchestrationUri=" + orchestrationUri + ", serviceParamXSD=" + serviceParamXSD +
				", recipeTimeout=" + recipeTimeout + ", serviceTimeoutInterim=" + serviceTimeoutInterim + ", created=" + created);
		this.id = id;
		this.serviceModelUUID = serviceModelUUID;
		this.action = action;
		this.description = description;
		this.orchestrationUri = orchestrationUri;
		this.serviceParamXSD = serviceParamXSD;
		this.recipeTimeout = recipeTimeout;
		this.serviceTimeoutInterim = serviceTimeoutInterim;
		long date = created.getTime();
		this.created = new Timestamp(date);
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getServiceModelUUID() {
		return serviceModelUUID;
	}
	public void setServiceModelUUID(String serviceModelUUID) {
		this.serviceModelUUID = serviceModelUUID;
	}

	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrchestrationUri() {
		return orchestrationUri;
	}
	public void setOrchestrationUri(String orchestrationUri) {
		this.orchestrationUri = orchestrationUri;
	}

	public String getServiceParamXSD() {
		return serviceParamXSD;
	}
	public void setServiceParamXSD(String serviceParamXSD) {
		this.serviceParamXSD = serviceParamXSD;
	}

	public int getRecipeTimeout() {
		return recipeTimeout;
	}
	public void setRecipeTimeout(int recipeTimeout) {
		this.recipeTimeout = recipeTimeout;
	}

	public Integer getServiceTimeoutInterim() {
		return serviceTimeoutInterim;
	}

	public void setServiceTimeoutInterim(Integer serviceTimeoutInterim) {
		this.serviceTimeoutInterim = serviceTimeoutInterim;
	}
	
	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RECIPE: ").append(action);
		sb.append(",uri=").append(orchestrationUri);
        if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
