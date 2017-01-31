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


import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class VnfResource extends MavenLikeVersioning {

    private int id;
    private String vnfType;
    
    private String orchestrationMode = null;
    private String description = null;
    private Integer templateId;
    private Integer environmentId = null;
    
    private Map <String, HeatFiles> heatFiles;
    
    private String asdcUuid;

    private Timestamp created;    
    
    private String aicVersionMin = null;
    private String aicVersionMax = null;
        
    private String modelInvariantUuid = null;
    private String modelVersion = null;

    private String modelCustomizationName = null;
    
    private String modelName = null;
    private String serviceModelInvariantUUID = null;
    
    public VnfResource () {
    }

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public String getVnfType () {
        return vnfType;
    }

    public void setVnfType (String vnfType) {
        this.vnfType = vnfType;
    }

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

    public Integer getTemplateId () {
        return templateId;
    }

    public void setTemplateId (Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getEnvironmentId () {
        return this.environmentId;
    }

    public void setEnvironmentId (Integer environmentId) {
        this.environmentId = environmentId;
    }
    
    public Map <String, HeatFiles> getHeatFiles () {
        return this.heatFiles;
    }

    public void setHeatFiles (Map <String, HeatFiles> heatFiles) {
        this.heatFiles = heatFiles;
    }

	public String getAsdcUuid() {
		return asdcUuid;
	}

	public void setAsdcUuid(String asdcUuidp) {
		this.asdcUuid = asdcUuidp;
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
	
	public String getModelCustomizationName() {
		return modelCustomizationName;
	}

	public void setModelCustomizationName(String modelCustomizationName) {
		this.modelCustomizationName = modelCustomizationName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getServiceModelInvariantUUID() {
		return serviceModelInvariantUUID;
	}

	public void setServiceModelInvariantUUID(String serviceModelInvariantUUID) {
		this.serviceModelInvariantUUID = serviceModelInvariantUUID;
	}

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();

		buf.append("VNF=");
		buf.append(vnfType);
		buf.append(",version=");
		buf.append(version);
		buf.append(",mode=");
		buf.append(orchestrationMode);
		buf.append(",template=");
		buf.append(templateId);
		buf.append(",envtId=");
		buf.append(environmentId);
		buf.append(",asdcUuid=");
		buf.append(asdcUuid);
		buf.append(",aicVersionMin=");
		buf.append(this.aicVersionMin);
		buf.append(",aicVersionMax=");
		buf.append(this.aicVersionMax);
        buf.append(",modelInvariantUuid=");
        buf.append(this.modelInvariantUuid);
        buf.append(",modelVersion=");
        buf.append(this.modelVersion);
        buf.append(",modelCustomizationName=");
        buf.append(this.modelCustomizationName);
        buf.append(",modelName=");
        buf.append(this.modelName);
        buf.append(",serviceModelInvariantUUID=");
        buf.append(this.serviceModelInvariantUUID);
        
		if (created != null) {
			buf.append(",created=");
			buf.append(DateFormat.getInstance().format(created));
		}
		return buf.toString();
    }

}
