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

public class NetworkResource extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String orchestrationMode = null;
	private String description = null;
	private String neutronNetworkType = null;
	private String aicVersionMin = null;
	private String aicVersionMax = null;
	private String modelName;
	private String modelInvariantUUID;
	private String modelVersion;
	private String toscaNodeType;
	private Timestamp created;
	private String modelUUID;
    private String category;
    private String subCategory;
	private String heatTemplateArtifactUUID;
	
	public NetworkResource() {}
	
	public String getOrchestrationMode() {
		return orchestrationMode;
	}
	
	public void setOrchestrationMode(String orchestrationMode) {
		this.orchestrationMode = orchestrationMode;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getNeutronNetworkType() {
		return neutronNetworkType;
	}

	public void setNeutronNetworkType(String neutronNetworkType) {
		this.neutronNetworkType = neutronNetworkType;
	}
	
	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
		
	public String getAicVersionMin() {
		return aicVersionMin;
	}

	public void setAicVersionMin(String aicVersionMin) {
		this.aicVersionMin = aicVersionMin;
	}

	public String getAicVersionMax() {
		return aicVersionMax;
	}

	public void setAicVersionMax(String aicVersionMax) {
		this.aicVersionMax = aicVersionMax;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelInvariantUUID() {
		return modelInvariantUUID;
	}

	public void setModelInvariantUUID(String modelInvariantUUID) {
		this.modelInvariantUUID = modelInvariantUUID;
	}

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	public String getModelUUID() {
		return modelUUID;
	}

	public void setModelUUID(String modelUUID) {
		this.modelUUID = modelUUID;
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

    
    /**
     * @return Returns the subCategory.
     */
    public String getSubCategory() {
        return subCategory;
    }

    
    /**
     * @param subCategory The subCategory to set.
     */
    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getHeatTemplateArtifactUUID() {
		return heatTemplateArtifactUUID;
	}

	public void setHeatTemplateArtifactUUID(String heatTemplateArtifactUUID) {
		this.heatTemplateArtifactUUID = heatTemplateArtifactUUID;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("NETWORK Resource:");
		sb.append("modelVersion=");
		sb.append(modelVersion);
		sb.append(",mode=");
		sb.append(orchestrationMode);
		sb.append(",neutronType=");
		sb.append(neutronNetworkType);
		sb.append(",aicVersionMin=");
		sb.append(aicVersionMin);
		sb.append(",aicVersionMax=");
		sb.append(aicVersionMax);
		sb.append(",modelName=");
		sb.append(modelName);
		sb.append(",modelInvariantUUID=");
		sb.append(modelInvariantUUID);
		sb.append(",toscaNodeType=");
		sb.append(toscaNodeType);
		sb.append(",modelUUID=");
		sb.append(modelUUID);
		sb.append(",heatTemplateArtifactUUID=");
		sb.append(heatTemplateArtifactUUID);
		
		if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		
		return sb.toString();
	}
}
