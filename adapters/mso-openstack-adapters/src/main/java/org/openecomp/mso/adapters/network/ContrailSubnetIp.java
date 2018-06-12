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

package org.openecomp.mso.adapters.network;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ContrailSubnetIp {
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_subnet_ip_prefix")
	private String ipPrefix;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_subnet_ip_prefix_len")
	private String ipPrefixLen;

	public ContrailSubnetIp() {
		/* Empty constructor */
	}

	public String getIpPrefix() {
		return ipPrefix;
	}

	public void setIpPrefix(String ipPrefix) {
		this.ipPrefix = ipPrefix;
	}

	public String getIpPrefixLen() {
		return ipPrefixLen;
	}

	public void setIpPrefixLen(String ipPrefixLen) {
		this.ipPrefixLen = ipPrefixLen;
	}

	@Override
	public String toString() {
		return "ContrailSubnetIp [ip_prefix=" + ipPrefix + ", ip_prefix_len=" + ipPrefixLen + "]";
	}

}
