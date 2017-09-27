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

package org.openecomp.camunda.bpmn.plugin.urnmap.db;

public class URNData {

	  private String urnName;
	  private String urnValue;
	  private String ver_;
	public String getURNName() {
		return urnName;
	}
	public void setURNName(String uRNName) {
		urnName = uRNName;
	}
	public String getURNValue() {
		return urnValue;
	}
	public void setURNValue(String uRNValue) {
		urnValue = uRNValue;
	}
	public String getVer_() {
		return ver_;
	}
	public void setVer_(String ver) {
		ver_ = ver;
	}
	  
}
