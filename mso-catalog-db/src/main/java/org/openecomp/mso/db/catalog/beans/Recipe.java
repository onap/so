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

public class Recipe extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

    private int id;
    protected String action;
    private String description;
    protected String orchestrationUri;
    private int recipeTimeout;
    private String serviceType;
    private String paramXSD;
	private Timestamp created;
    
    public Recipe () {
        super ();
    }

    public int getId () {
    	return id;
    }

    public void setId (int id) {
    	this.id = id;
    }

    public String getAction () {
    	return action;
    }

    public void setAction (String action) {
    	this.action = action;
    }

    public String getDescription () {
    	return description;
    }

    public void setDescription (String description) {
    	this.description = description;
    }

    public String getOrchestrationUri () {
    	return orchestrationUri;
    }

    public void setOrchestrationUri (String orchestrationUri) {
    	this.orchestrationUri = orchestrationUri;
    }

    public int getRecipeTimeout () {
    	return recipeTimeout;
    }

    public void setRecipeTimeout (int recipeTimeout) {
    	this.recipeTimeout = recipeTimeout;
    }

    public String getServiceType () {
    	return serviceType;
    }

    public void setServiceType (String serviceType) {
    	this.serviceType = serviceType;
    }

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

    /**
     * @return Returns the paramXSD.
     */
    public String getParamXSD() {
        return paramXSD;
    }
  
    /**
     * @param paramXSD The paramXSD to set.
     */
    public void setParamXSD(String paramXSD) {
        this.paramXSD = paramXSD;
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
