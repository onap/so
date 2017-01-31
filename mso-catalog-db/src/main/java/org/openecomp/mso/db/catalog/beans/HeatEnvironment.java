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

public class HeatEnvironment extends MavenLikeVersioning {
	private int id;
	private String name = null;
	private String description = null;
	private String environment = null;

	private String asdcUuid;
	private String asdcResourceName;
	private String asdcLabel;

	private Timestamp created;
	
	public HeatEnvironment() {}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}


    /**
     * @return the name
     */
    public String getName () {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName (String name) {
        this.name = name;
    }

    public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnvironment() {
		return this.environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getAsdcUuid() {
		return asdcUuid;
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


    /**
     * @return the asdcResourceName
     */
    public String getAsdcResourceName () {
        return asdcResourceName;
    }


    /**
     * @param asdcResourceName the asdcResourceName to set
     */
    public void setAsdcResourceName (String asdcResourceName) {
        this.asdcResourceName = asdcResourceName;
    }

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
    
    @Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append ("ID=" + this.id);
        sb.append (", name=");
        sb.append (name);
        sb.append (", version=");
        sb.append (version);
        sb.append(", description=");
        sb.append (description == null ? "null" : description);
        sb.append(", environment=");
        sb.append (environment == null ? "null" : environment);
        sb.append(", asdcUuid=");
        sb.append (asdcUuid == null ? "null" : asdcUuid);
		sb.append (", asdcResourceName=");
		sb.append (asdcResourceName == null ? "null" : asdcResourceName);
		if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
