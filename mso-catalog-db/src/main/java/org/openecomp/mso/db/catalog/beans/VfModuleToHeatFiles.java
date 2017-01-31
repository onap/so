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



import java.io.Serializable;

public class VfModuleToHeatFiles implements Serializable {
	
    private int vfModuleId;
    private int heatFilesId;
    public static final long serialVersionUID = -1322322139926390329L;

	public VfModuleToHeatFiles() {
		super();
	}
	
	public int getVfModuleId() {
		return this.vfModuleId;
	}
	public void setVfModuleId(int vfModuleId) {
		this.vfModuleId = vfModuleId;
	}
	
	public int getHeatFilesId() {
		return this.heatFilesId;
	}
	public void setHeatFilesId(int heatFilesId) {
		this.heatFilesId = heatFilesId;
	}
	
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("VF_MODULE_ID=" + this.vfModuleId);
        sb.append (", HEAT_FILES_ID=" + this.heatFilesId);
        return sb.toString ();
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof VfModuleToHeatFiles)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        VfModuleToHeatFiles vmthf = (VfModuleToHeatFiles) o;
        if (vmthf.getVfModuleId() == this.getVfModuleId() && vmthf.getVfModuleId() == this.getVfModuleId()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result = 0;
        result = this.vfModuleId + this.heatFilesId;
        return result;
    }

}
