/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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
 
 package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties({ "additionalProperties" })
public class E2EAdditionalParamForNs {

	@JsonProperty("externalDataNetworkName")
	private String externalDataNetworkName;
	@JsonProperty("m6000_mng_ip")
	private String m6000MngIp;
	@JsonProperty("externalCompanyFtpDataNetworkName")
	private String externalCompanyFtpDataNetworkName;
	@JsonProperty("externalPluginManageNetworkName")
	private String externalPluginManageNetworkName;
	@JsonProperty("externalManageNetworkName")
	private String externalManageNetworkName;
	@JsonProperty("sfc_data_network")
	private String sfcDataNetwork;
	@JsonProperty("NatIpRange")
	private String natIpRange;
	@JsonProperty("location")
	private String location;
	@JsonProperty("sdncontroller")
	private String sdncontroller;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	public String getExternalDataNetworkName() {
		return externalDataNetworkName;
	}

	public void setExternalDataNetworkName(String externalDataNetworkName) {
		this.externalDataNetworkName = externalDataNetworkName;
	}

	public String getM6000MngIp() {
		return m6000MngIp;
	}

	public void setM6000MngIp(String m6000MngIp) {
		this.m6000MngIp = m6000MngIp;
	}

	public String getExternalCompanyFtpDataNetworkName() {
		return externalCompanyFtpDataNetworkName;
	}

	public void setExternalCompanyFtpDataNetworkName(
			String externalCompanyFtpDataNetworkName) {
		this.externalCompanyFtpDataNetworkName = externalCompanyFtpDataNetworkName;
	}

	public String getExternalPluginManageNetworkName() {
		return externalPluginManageNetworkName;
	}

	public void setExternalPluginManageNetworkName(
			String externalPluginManageNetworkName) {
		this.externalPluginManageNetworkName = externalPluginManageNetworkName;
	}

	public String getExternalManageNetworkName() {
		return externalManageNetworkName;
	}

	public void setExternalManageNetworkName(String externalManageNetworkName) {
		this.externalManageNetworkName = externalManageNetworkName;
	}

	public String getSfcDataNetwork() {
		return sfcDataNetwork;
	}

	public void setSfcDataNetwork(String sfcDataNetwork) {
		this.sfcDataNetwork = sfcDataNetwork;
	}

	public String getNatIpRange() {
		return natIpRange;
	}

	public void setNatIpRange(String natIpRange) {
		this.natIpRange = natIpRange;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSdncontroller() {
		return sdncontroller;
	}

	public void setSdncontroller(String sdncontroller) {
		this.sdncontroller = sdncontroller;
	}

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

}
