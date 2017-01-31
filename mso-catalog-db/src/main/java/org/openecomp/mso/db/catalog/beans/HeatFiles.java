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

public class HeatFiles extends MavenLikeVersioning {
	private int id;
	private String description = null;
	private String fileName;
	private String fileBody;
	private int vnfResourceId;
	private Timestamp created;
	private String asdcUuid;
	private String asdcLabel;
    private String asdcResourceName;
	
	public HeatFiles() {}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileBody() {
		return this.fileBody;
	}
	public void setFileBody(String fileBody) {
		this.fileBody = fileBody;
	}
	
	public int getVnfResourceId() {
		return this.vnfResourceId;
	}
	public void setVnfResourceId(int vnfResourceId) {
		this.vnfResourceId = vnfResourceId;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
	
	public String getAsdcUuid() {
		return this.asdcUuid;
	}
	public void setAsdcUuid(String asdcUuid) {
		this.asdcUuid = asdcUuid;
	}
	public String getAsdcLabel() {
		return this.asdcLabel;
	}
	public void setAsdcLabel(String asdcLabel) {
		this.asdcLabel = asdcLabel;
	}
	public String getAsdcResourceName() {
		return asdcResourceName;
	}

	public void setAsdcResourceName(String asdcResourceName) {
		this.asdcResourceName = asdcResourceName;
	}

	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append ("ID=" + this.id);
		if (this.description == null) {
			sb.append(", description=null");
		} else {
			sb.append(", description=" + this.description);
		}
		if (this.fileName == null) {
			sb.append(", fileName=null");
		} else {
			sb.append(",fileName=" + this.fileName);
		}
		if (this.fileBody == null) {
			sb.append(", fileBody=null");
		} else {
			sb.append(",fileBody=" + this.fileBody);
		}
		if (this.asdcResourceName == null) {
			sb.append(", asdcResourceName=null");
		} else {
			sb.append(",asdcResourceName=" + this.asdcResourceName);
		}
		if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		sb.append(", vnfResourceId=" + this.vnfResourceId);
		return sb.toString();
	}
}
