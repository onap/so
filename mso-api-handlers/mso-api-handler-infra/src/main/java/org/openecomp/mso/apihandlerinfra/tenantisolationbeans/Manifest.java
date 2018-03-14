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

package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "manifest")
@JsonInclude(Include.NON_DEFAULT)
public class Manifest implements Serializable {

	private static final long serialVersionUID = -3460949513229380541L;
	@JsonProperty("serviceModelList")
	private List<ServiceModelList> serviceModelList = new ArrayList<ServiceModelList>();		

	public List<ServiceModelList> getServiceModelList() {
		return serviceModelList;
	}

	public void setServiceModelList(List<ServiceModelList> serviceModelList) {
		this.serviceModelList = serviceModelList;
	}
	
	@Override
	public String toString() {
		return "Manifest [serviceModelList=" + serviceModelList.toString() + "]";
	}		
	
}
