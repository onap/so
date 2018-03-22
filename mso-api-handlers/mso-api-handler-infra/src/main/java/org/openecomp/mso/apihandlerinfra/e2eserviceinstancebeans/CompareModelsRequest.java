/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class CompareModelsRequest {

	private String globalSubscriberId;
	
	private String serviceType;

	private String modelInvariantIdTarget;
	
	private String modelVersionIdTarget;


	public String getGlobalSubscriberId() {
		return globalSubscriberId;
	}

	public void setGlobalSubscriberId(String globalSubscriberId) {
		this.globalSubscriberId = globalSubscriberId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getModelInvariantIdTarget() {
		return modelInvariantIdTarget;
	}

	public void setModelInvariantIdTarget(String modelInvariantIdTarget) {
		this.modelInvariantIdTarget = modelInvariantIdTarget;
	}

	public String getModelVersionIdTarget() {
		return modelVersionIdTarget;
	}

	public void setModelVersionIdTarget(String modelVersionIdTarget) {
		this.modelVersionIdTarget = modelVersionIdTarget;
	}
}
