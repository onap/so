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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;

public class Subscriber implements Serializable {

	private static final long serialVersionUID = -2416018315129127022L;
	private String globalId;
	private String name;
	private String commonSiteId;

	public Subscriber(String globalId, String name, String commonSiteId){
		super();
		this.globalId = globalId;
		this.name = name;
		this.commonSiteId = commonSiteId;
	}


	public String getGlobalId(){
		return globalId;
	}


	public void setGlobalId(String globalId){
		this.globalId = globalId;
	}


	public String getName(){
		return name;
	}


	public void setName(String name){
		this.name = name;
	}


	public String getCommonSiteId(){
		return commonSiteId;
	}

	public void setCommonSiteId(String commonSiteId){
		this.commonSiteId = commonSiteId;
	}


}