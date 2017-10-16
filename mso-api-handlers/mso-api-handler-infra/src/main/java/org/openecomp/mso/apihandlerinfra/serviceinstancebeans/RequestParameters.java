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

package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class RequestParameters {

	private String subscriptionServiceType;
	private List<Map<String, String>> userParams = new ArrayList<>();
	@JsonSerialize(include=Inclusion.ALWAYS)
	private boolean aLaCarte = false;
	@JsonIgnore
	private boolean aLaCarteSet = false;
	@JsonSerialize(include=Inclusion.ALWAYS)
	private boolean autoBuildVfModules = false;
	@JsonSerialize(include=Inclusion.ALWAYS)
	private boolean cascadeDelete = false;
	@JsonSerialize(include=Inclusion.ALWAYS)
	private boolean usePreload=true; // usePreload would always be true for Update
	@JsonSerialize(include=Inclusion.ALWAYS)
	private boolean rebuildVolumeGroups = false;


	public String getSubscriptionServiceType() {
		return subscriptionServiceType;
	}

	public void setSubscriptionServiceType(String subscriptionServiceType) {
		this.subscriptionServiceType = subscriptionServiceType;
	}

	public void setaLaCarte(boolean aLaCarte) {
		this.aLaCarte = aLaCarte;
		this.aLaCarteSet = true;
	}

	//returns true if aLaCarte param came in on request
	public boolean isaLaCarteSet() {
		return aLaCarteSet;
	}

	public boolean isaLaCarte() {
		return aLaCarte;
	}

	public List<Map<String, String>> getUserParams() {
		return userParams;
	}

	public void setUserParams(List<Map<String, String>> userParams) {
		this.userParams = userParams;
	}

	public String getUserParamValue(String name){
	    	if(userParams!=null){
	    		for(Map<String, String> param:userParams){
	    			if(param.get("name").equals(name)){
	    				return param.get("value");
	    			}
	    		}
	    	}
	    	return null;
	}

	public boolean getAutoBuildVfModules() {
		return autoBuildVfModules;
	}

	public void setAutoBuildVfModules(boolean autoBuildVfModules) {
		this.autoBuildVfModules = autoBuildVfModules;
	}

	public boolean getCascadeDelete() {
		return cascadeDelete;
	}

	public void setCascadeDelete(boolean cascadeDelete) {
		this.cascadeDelete = cascadeDelete;
	}

	public boolean isUsePreload() {
		return usePreload;
	}

	public void setUsePreload(boolean usePreload) {
		this.usePreload = usePreload;
	}
	
	public boolean rebuildVolumeGroups() {
		return rebuildVolumeGroups;
	}

	public void setRebuildVolumeGroups(boolean rebuildVolumeGroups) {
		this.rebuildVolumeGroups = rebuildVolumeGroups;
	}

	@Override
	public String toString() {
		return "RequestParameters [subscriptionServiceType="
				+ subscriptionServiceType + ", userParams=" + userParams
				+ ", aLaCarte=" + aLaCarte + ", autoBuildVfModules="
				+ autoBuildVfModules + ", usePreload="
				+ usePreload + ", rebuildVolumeGroups="
				+ rebuildVolumeGroups +"]";
	}


}