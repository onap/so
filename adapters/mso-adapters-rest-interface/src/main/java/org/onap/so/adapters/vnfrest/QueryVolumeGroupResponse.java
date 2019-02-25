/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.vnfrest;


import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.onap.so.openstack.beans.VnfStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "queryVolumeGroupResponse")
public class QueryVolumeGroupResponse {
	private static final Logger logger = LoggerFactory.getLogger(QueryVolumeGroupResponse.class);
	private String volumeGroupId;
	private String volumeGroupStackId;
	private VnfStatus volumeGroupStatus;
	private Map<String,String> volumeGroupOutputs;

	public QueryVolumeGroupResponse() {
	}

	public QueryVolumeGroupResponse(
			String volumeGroupId,
			String volumeGroupStackId,
			VnfStatus volumeGroupStatus,
			Map<String, String> volumeGroupOutputs)
	{
		super();
		this.volumeGroupId = volumeGroupId;
		this.volumeGroupStackId = volumeGroupStackId;
		this.volumeGroupStatus = volumeGroupStatus;
		this.volumeGroupOutputs = volumeGroupOutputs;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public String getVolumeGroupStackId() {
		return volumeGroupStackId;
	}

	public void setVolumeGroupStackId(String volumeGroupStackId) {
		this.volumeGroupStackId = volumeGroupStackId;
	}

	public VnfStatus getVolumeGroupStatus() {
		return volumeGroupStatus;
	}

	public void setVolumeGroupStatus(VnfStatus volumeGroupStatus) {
		this.volumeGroupStatus = volumeGroupStatus;
	}

	public Map<String, String> getVolumeGroupOutputs() {
		return volumeGroupOutputs;
	}

	public void setVolumeGroupOutputs(Map<String, String> volumeGroupOutputs) {
		this.volumeGroupOutputs = volumeGroupOutputs;
	}

	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
			jsonString = mapper.writeValueAsString(this);
		}
		catch (Exception e) {
			logger.debug("Exception :", e);
		}
		return jsonString;
	}
}
