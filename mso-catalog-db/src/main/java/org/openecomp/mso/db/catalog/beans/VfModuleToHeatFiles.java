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

public class VfModuleToHeatFiles implements Serializable {
	
    private String vfModuleModelUuid;
    private String heatFilesArtifactUuid;
    public static final long serialVersionUID = -1322322139926390329L;

	public VfModuleToHeatFiles() {
		super();
	}
	
	public String getVfModuleModelUuid() {
		return this.vfModuleModelUuid;
	}
	public void setVfModuleModelUuid(String vfModuleModelUuid) {
		this.vfModuleModelUuid = vfModuleModelUuid;
	}
	public String getHeatFilesArtifactUuid() {
		return this.heatFilesArtifactUuid;
	}
	public void setHeatFilesArtifactUuid(String heatFilesArtifactUuid) {
		this.heatFilesArtifactUuid = heatFilesArtifactUuid;
	}

	@Override
	public String toString() {
		return "vfModuleModelUuid=" + this.vfModuleModelUuid + ", heatFilesArtifactUuid=" + this.heatFilesArtifactUuid;
	}

}
