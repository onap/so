/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vnfrest;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfStatus;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("queryVfModuleResponse")
@XmlRootElement(name = "queryVfModuleResponse")
public class QueryVfModuleResponse extends VfResponseCommon{
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private String vnfId;
	private String vfModuleId;
	private String vfModuleStackId;
	private VnfStatus vnfStatus;
	private Map<String, String> vfModuleOutputs;

	public QueryVfModuleResponse() {
		super();
	}

	public QueryVfModuleResponse(String vnfId, String vfModuleId, String vfModuleStackId, VnfStatus vnfStatus,
			Map<String, String> vfModuleOutputs) {
		super();
		this.vnfId = vnfId;
		this.vfModuleId = vfModuleId;
		this.vfModuleStackId = vfModuleStackId;
		this.vnfStatus = vnfStatus;
		this.vfModuleOutputs = vfModuleOutputs;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getVfModuleStackId() {
		return vfModuleStackId;
	}

	public void setVfModuleStackId(String vfModuleStackId) {
		this.vfModuleStackId = vfModuleStackId;
	}

	public VnfStatus getVnfStatus() {
		return vnfStatus;
	}

	public void setVnfStatus(VnfStatus vnfStatus) {
		this.vnfStatus = vnfStatus;
	}

	public Map<String, String> getVfModuleOutputs() {
		return vfModuleOutputs;
	}

	public void setVfModuleOutputs(Map<String, String> vfModuleOutputs) {
		this.vfModuleOutputs = vfModuleOutputs;
	}
	
}
