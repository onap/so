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

public class VnfResCustomToVfModuleCustom implements Serializable {
	
	private String vnfResourceCustModelCustomizationUuid;
	private String vfModuleCustModelCustomizationUuid;
	private Timestamp created;
	
    public static final long serialVersionUID = -1322322139926390329L;


	public VnfResCustomToVfModuleCustom() {
		super();
	}
	public String getVnfResourceCustModelCustomizationUuid() {
		return this.vnfResourceCustModelCustomizationUuid;
	}
	public void setVnfResourceCustModelCustomizationUuid(String vnfResourceCustModelCustomizationUuid) {
		this.vnfResourceCustModelCustomizationUuid = vnfResourceCustModelCustomizationUuid;
	}
	public String getVfModuleCustModelCustomizationUuid() {
		return this.vfModuleCustModelCustomizationUuid;
	}
	public void setVfModuleCustModelCustomizationUuid(String vfModuleCustModelCustomizationUuid) {
		this.vfModuleCustModelCustomizationUuid = vfModuleCustModelCustomizationUuid;
	}
	public Timestamp getCreated() {
		return created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Override
	public String toString() {
		return "vnfResourceCustModelCustomizationUuid=" + this.vnfResourceCustModelCustomizationUuid +
			"vfModuleCustModelCustomizationUuid=" + this.vfModuleCustModelCustomizationUuid + "created=" + this.created;
	}

	@Override
    public boolean equals (Object o) {
        if (!(o instanceof VnfResCustomToVfModuleCustom)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        VnfResCustomToVfModuleCustom vrctvmc = (VnfResCustomToVfModuleCustom) o;
        if (vrctvmc.getVnfResourceCustModelCustomizationUuid().equals(this.getVnfResourceCustModelCustomizationUuid()) && vrctvmc.getVfModuleCustModelCustomizationUuid().equals(this.getVfModuleCustModelCustomizationUuid())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result = 0;
        result = (this.vnfResourceCustModelCustomizationUuid != null ? this.vnfResourceCustModelCustomizationUuid.hashCode() : 0) + (this.vfModuleCustModelCustomizationUuid != null ? this.vfModuleCustModelCustomizationUuid.hashCode() : 0);
        return result;
    }


}
