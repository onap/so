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

public class VfModule extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String modelInvariantUUID;
	private String modelName;
	private String modelVersion;
	private String description;
	private int isBase;
	private String heatTemplateArtifactUUId;
	private String volHeatTemplateArtifactUUId;
    private Timestamp created;
	private String modelUUID;
	private String vnfResourceModelUUId;

    public VfModule() {
		super();
	}

	public String getVnfResourceModelUUId() {
		return this.vnfResourceModelUUId;
	}

	public void setVnfResourceModelUUId(String vnfResourceModelUUId) {
		this.vnfResourceModelUUId = vnfResourceModelUUId;
	}

	public String getModelName() {
		return this.modelName;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
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

	public String getHeatTemplateArtifactUUId() {
		return this.heatTemplateArtifactUUId;
	}

	public void setHeatTemplateArtifactUUId(String heatTemplateArtifactUUId) {
		this.heatTemplateArtifactUUId = heatTemplateArtifactUUId;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
	
	public String getModelInvariantUuid() {
		return this.modelInvariantUUID;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUUID = modelInvariantUuid;
	}
	public String getModelInvariantUUID() {
		return this.modelInvariantUUID;
	}
	public void setModelInvariantUUID(String modelInvariantUuid) {
		this.modelInvariantUUID = modelInvariantUuid;
	}
	
	public String getVolHeatTemplateArtifactUUId() {
		return this.volHeatTemplateArtifactUUId;
	}
	
	public void setVolHeatTemplateArtifactUUId(String volHeatTemplateArtifactUUId) {
		this.volHeatTemplateArtifactUUId = volHeatTemplateArtifactUUId;
	}

    public String getModelUUID() {
		return modelUUID;
	}

	public void setModelUUID(String modelUUID) {
		this.modelUUID = modelUUID;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	@Override
	public String toString () {
       StringBuilder buf = new StringBuilder();

       buf.append("VFModule:");
       buf.append("modelName=");
       buf.append(modelName);
       buf.append(",modelVersion=");
       buf.append(modelVersion);
       buf.append(",vnfResourceModelUUId=");
       buf.append(this.vnfResourceModelUUId);
       buf.append(",heatTemplateArtifactUUId=");
       buf.append(this.heatTemplateArtifactUUId);
       buf.append(", description=");
       buf.append(this.description);
       buf.append(",volHeatTemplateArtifactUUId=");
       buf.append(this.volHeatTemplateArtifactUUId);
       buf.append(",isBase=");
       buf.append(this.isBase);
       buf.append(",modelInvariantUUID=");
       buf.append(this.modelInvariantUUID);
       buf.append(",modelUUID=");
       buf.append(this.modelUUID);

    	 if (this.created != null) {
    		 buf.append (",created=");
    		 buf.append (DateFormat.getInstance().format(this.created));
         }
		 
    	return buf.toString();
    }

}
