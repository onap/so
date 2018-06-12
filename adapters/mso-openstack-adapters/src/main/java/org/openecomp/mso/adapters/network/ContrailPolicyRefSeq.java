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

public class ContrailPolicyRefSeq {
	
	@JsonProperty("network_policy_refs_data_sequence_major")
	private String major;
	
	@JsonProperty("network_policy_refs_data_sequence_minor")
	private String minor;

	public ContrailPolicyRefSeq() {
		/* To be done */
	}
	
	public ContrailPolicyRefSeq(String major, String minor) {
		super();
		this.major = major;
		this.minor = minor;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	@Override
	public String toString() {
		return "ContrailPolicyRefSeq [major=" + major + ", minor=" + minor
				+ "]";
	} 

}
