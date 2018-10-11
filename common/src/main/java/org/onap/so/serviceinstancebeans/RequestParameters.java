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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestParameters")
@JsonInclude(Include.NON_DEFAULT)
public class RequestParameters implements Serializable {

	private static final long serialVersionUID = -5979049912538894930L;
	@JsonProperty("subscriptionServiceType")
	private String subscriptionServiceType;
	@JsonProperty("userParams")
	private List<Map<String, Object>> userParams = new ArrayList<>();
	@JsonProperty("aLaCarte")
	private Boolean aLaCarte;
	@JsonProperty("payload")
	private String payload;
	
	// DONOT USE. It is intended to handle older VID requests(prior to 1802)
	@Deprecated
	@JsonProperty("alaCarte")
	private Boolean alaCarte;
	@JsonProperty("autoBuildVfModules")
	private Boolean autoBuildVfModules;
	@JsonProperty("cascadeDelete")
	private Boolean cascadeDelete;
	@JsonProperty("testApi")
	private String testApi; // usePreload would always be true for Update
	@JsonProperty("usePreload")
	private Boolean usePreload; // usePreload would always be true for Update
	@JsonProperty("rebuildVolumeGroups")
	private Boolean rebuildVolumeGroups;	

	public String getSubscriptionServiceType() {
		return subscriptionServiceType;
	}

	public void setSubscriptionServiceType(String subscriptionServiceType) {
		this.subscriptionServiceType = subscriptionServiceType;
	}
	@JsonProperty("aLaCarte")
	public Boolean getALaCarte() {
		return aLaCarte;
	}
	@JsonProperty("aLaCarte")
	public void setaLaCarte(Boolean aLaCarte) {
		this.aLaCarte = aLaCarte;
	}
	
	public Boolean isaLaCarte() {
		return aLaCarte;
	}
	
	public String getPayload(){
		return payload;
	}
	public void setPayload(String value){
		this.payload = value;
	}

	@Deprecated
	@Transient
	@JsonProperty("alaCarte")
	public Boolean getAlaCarte() {
		return alaCarte;
	}

	@Deprecated
	@JsonProperty("alaCarte")
	public void setAlaCarte(Boolean alaCarte) {
		this.alaCarte = alaCarte;
	}

	public String getTestApi() {
		return testApi;
	}

	public void setTestApi(String testApi) {
		this.testApi = testApi;
	}
	
	public List<Map<String, Object>> getUserParams() {
		return userParams;
	}

	public void setUserParams(List<Map<String, Object>> userParams) {
		this.userParams = userParams;
	}

	public String getUserParamValue(String name){
		if(userParams!=null){
			for(Map<String, Object> param:userParams){
				if(param.containsKey("name") && param.get("name").equals(name) && param.containsKey("value")){
					return param.get("value").toString();
				}
			}
		}
		return null;
	}
	
	public Boolean getAutoBuildVfModules() {
		return autoBuildVfModules;
	}
	
	public void setAutoBuildVfModules(Boolean autoBuildVfModules) {
		this.autoBuildVfModules = autoBuildVfModules;
	}

	public Boolean getCascadeDelete() {
		return cascadeDelete;
	}

	public void setCascadeDelete(Boolean cascadeDelete) {
		this.cascadeDelete = cascadeDelete;
	}

	public Boolean isUsePreload() {
		return usePreload;
	}

	public void setUsePreload(Boolean usePreload) {
		this.usePreload = usePreload;
	}
	
	public Boolean getRebuildVolumeGroups() {
		return rebuildVolumeGroups;
	}

	public void setRebuildVolumeGroups(Boolean rebuildVolumeGroups) {
		this.rebuildVolumeGroups = rebuildVolumeGroups;
	}	
	
	@Override
	public String toString() {
		return "RequestParameters [subscriptionServiceType="
				+ subscriptionServiceType + ", userParams=" + userParams
				+ ", aLaCarte=" + aLaCarte + ", testApi= " + testApi + ", autoBuildVfModules="
				+ autoBuildVfModules + ", usePreload="
				+ usePreload + ", rebuildVolumeGroups="
				+ rebuildVolumeGroups + ", payload=" + payload + "]";
	}
}