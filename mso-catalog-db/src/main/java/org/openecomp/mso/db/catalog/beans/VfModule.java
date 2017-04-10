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

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class VfModule extends MavenLikeVersioning {

	private int id;
	private Integer vnfResourceId;
	private String type;
	private String modelName;
	private int isBase;
	private Integer templateId;
	private Integer environmentId;
	private Integer volTemplateId;
	private Integer volEnvironmentId;
	private String description;
	private String asdcUuid;
    private Timestamp created;
    private String modelInvariantUuid;
    private String modelVersion;
	private String modelCustomizationUuid = null;
	private Integer minInstances;
	private Integer maxInstances;
	private Integer initialCount;
	private String label;

    public VfModule() {
		super();
	}

	public int getId(){
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public Integer getVnfResourceId() {
		return this.vnfResourceId;
	}
	public void setVnfResourceId(Integer vnfResourceId) {
		this.vnfResourceId = vnfResourceId;
	}

	public String getModelName() {
		return this.modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIsBase() {
		return this.isBase;
	}
	public void setIsBase(int isBase) {
		this.isBase = isBase;
	}
	public boolean isBase() {
		if (this.isBase == 0) {
			return false;
		} else {
			return true;
		}
	}

	public Integer getTemplateId() {
		return this.templateId;
	}
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public Integer getEnvironmentId() {
		return this.environmentId;
	}
	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public Integer getVolTemplateId() {
		return this.volTemplateId;
	}
	public void setVolTemplateId(Integer volTemplateId) {
		this.volTemplateId = volTemplateId;
	}

	public Integer getVolEnvironmentId() {
		return this.volEnvironmentId;
	}
	public void setVolEnvironmentId(Integer volEnvironmentId) {
		this.volEnvironmentId = volEnvironmentId;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public Integer getMinInstances() {
		return this.minInstances;
	}
	public void setMinInstances(Integer minInstances) {
		this.minInstances = minInstances;
	}
	public Integer getMaxInstances() {
		return this.maxInstances;
	}
	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
	}
	public Integer getInitialCount() {
		return this.initialCount;
	}
	public void setInitialCount(Integer initialCount) {
		this.initialCount = initialCount;
	}
	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString () {
       StringBuffer buf = new StringBuffer();

       buf.append("VF=");
       buf.append(this.type);
       buf.append(",modelName=");
       buf.append(modelName);
       buf.append(",version=");
       buf.append(version);
       buf.append(",id=");
       buf.append(this.id);
       buf.append(",vnfResourceId=");
       buf.append(this.vnfResourceId);
       buf.append(",templateId=");
       buf.append(this.templateId);
       buf.append(",envtId=");
       buf.append(this.environmentId);
       buf.append(",volTemplateId=");
       buf.append(this.volTemplateId);
       buf.append(",volEnvtId=");
       buf.append(this.volEnvironmentId);
       buf.append(", description=");
       buf.append(this.description);
       buf.append(",asdcUuid=");
       buf.append(asdcUuid);
       buf.append(",modelVersion=");
       buf.append(this.modelVersion);
       buf.append(",modelCustomizationUuid=");
       buf.append(this.modelCustomizationUuid);
       buf.append(",minInstances=");
       buf.append(this.minInstances);
       buf.append(",maxInstances=");
       buf.append(this.maxInstances);
       buf.append(",initialCount=");
       buf.append(this.initialCount);
       buf.append(",label=");
       buf.append(this.label);

    	 if (this.created != null) {
    		 buf.append (",created=");
    		 buf.append (DateFormat.getInstance().format(this.created));
         }
    	return buf.toString();
    }

}
