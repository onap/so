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
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class VnfResource extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String modelUuid;
	private String modelInvariantUuid;
	private String modelName;
    private String toscaNodeType;
    private String description;
    private String orchestrationMode;
    private String aicVersionMin;
    private String aicVersionMax;
    private String category;
    private String subCategory;
    private String heatTemplateArtifactUUId;
    private Timestamp created;
    private String modelVersion;
    private Set<VnfResourceCustomization> vnfResourceCustomizations;
    private Set<VfModule> vfModules;
    private List<VfModule> vfModuleList;
    private List<VfModuleCustomization> vfModuleCustomizations;

    public VnfResource () { }

    public String getOrchestrationMode () {
        return orchestrationMode;
    }

    public void setOrchestrationMode (String orchestrationMode) {
        this.orchestrationMode = orchestrationMode;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public String getTemplateId () {
        return heatTemplateArtifactUUId;
    }

    public void setTemplateId (String heatTemplateArtifactUUId) {
        this.heatTemplateArtifactUUId = heatTemplateArtifactUUId;
    }
    public String getHeatTemplateArtifactUUId () {
        return heatTemplateArtifactUUId;
    }

    public void setHeatTemplateArtifactUUId (String heatTemplateArtifactUUId) {
        this.heatTemplateArtifactUUId = heatTemplateArtifactUUId;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getAicVersionMin() {
		return this.aicVersionMin;
	}

	public void setAicVersionMin(String aicVersionMin) {
		this.aicVersionMin = aicVersionMin;
	}

	public String getAicVersionMax() {
		return this.aicVersionMax;
	}

	public void setAicVersionMax(String aicVersionMax) {
		this.aicVersionMax = aicVersionMax;
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

    public String getModelInvariantUuid() {
		return this.modelInvariantUuid;
	}

	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelUuid() {
		return modelUuid;
	}

	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}

	public String getModelInvariantId() {
		return this.modelInvariantUuid;
	}

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	public Set<VnfResourceCustomization> getVnfResourceCustomizations() {
		return vnfResourceCustomizations;
	}

	public void setVnfResourceCustomizations(Set<VnfResourceCustomization> vnfResourceCustomizations) {
		this.vnfResourceCustomizations = vnfResourceCustomizations;
	}

	public Set<VfModule> getVfModules() {
		return vfModules;
	}

	public void setVfModules(Set<VfModule> vfModules) {
		this.vfModules = vfModules;
	}

	public List<VfModuleCustomization> getVfModuleCustomizations() {
		return this.vfModuleCustomizations == null ? new ArrayList<>() : this.vfModuleCustomizations;
	}
	public void setVfModuleCustomizations(ArrayList<VfModuleCustomization> vfModuleCustomizations) {
		this.vfModuleCustomizations = vfModuleCustomizations;
	}
	public void addVfModuleCustomization(VfModuleCustomization vfmc) {
		if (vfmc != null) {
			if (this.vfModuleCustomizations != null) {
				this.vfModuleCustomizations.add(vfmc);
			} else {
				this.vfModuleCustomizations = new ArrayList<>();
				this.vfModuleCustomizations.add(vfmc);
			}
	}
	}

	public void addVfModule(VfModule vfm) {
		if (vfm != null) {
			if (this.vfModules != null) {
				this.vfModules.add(vfm);
			} else {
				this.vfModules = new HashSet<>();
				this.vfModules.add(vfm);
			}
		}
	}
	public ArrayList<VfModule> getVfModuleList() {
		if (this.vfModules == null || this.vfModules.size() < 1) {
			return null;
	}
		ArrayList<VfModule> list = new ArrayList<>();
		list.addAll(this.vfModules);
		return list;
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

		sb.append("VNF=");
		sb.append(",modelVersion=");
		sb.append(modelVersion);
		sb.append(",mode=");
		sb.append(orchestrationMode);
		sb.append(",heatTemplateArtifactUUId=");
		sb.append(heatTemplateArtifactUUId);
		sb.append(",envtId=");
		sb.append(",asdcUuid=");
		sb.append(",aicVersionMin=");
		sb.append(this.aicVersionMin);
		sb.append(",aicVersionMax=");
		sb.append(this.aicVersionMax);
        sb.append(",modelInvariantUuid=");
        sb.append(this.modelInvariantUuid);
        sb.append(",modelVersion=");
        sb.append(",modelCustomizationName=");
        sb.append(",modelName=");
        sb.append(this.modelName);
        sb.append(",serviceModelInvariantUUID=");
		sb.append(",modelCustomizationUuid=");
        sb.append(",toscaNodeType=");
        sb.append(toscaNodeType);

		if (created != null) {
			sb.append(",created=");
			sb.append(DateFormat.getInstance().format(created));
		}
		
		for(VnfResourceCustomization vrc : vnfResourceCustomizations) {
			sb.append("/n").append(vrc.toString());
			}
		
		for(VfModule vfm : vfModules) {
			sb.append("/n").append(vfm.toString());
		}
		return sb.toString();
    }

}
